/**
 * 
 */
package scratch.UCERF3.inversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.geometry.Vector3D;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.util.FaultUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.analysis.DeformationModelsCalc;
import scratch.UCERF3.analysis.FaultSystemRupSetCalc;
import scratch.UCERF3.enumTreeBranches.AveSlipForRupModels;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.griddedSeismicity.SpatialSeisPDF;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.DeformationModelOffFaultMoRateData;
import scratch.UCERF3.utils.FaultSectionDataWriter;
import scratch.UCERF3.utils.IDPairing;

/**
 * This class represents a FaultSystemRupSet for the Grand Inversion.
 * 
 * Important Notes:
 * 
 * 1) If the sections are actually subsections of larger sections, then the method 
 * computeCloseSubSectionsListList() only allows one connection between parent sections
 * (to avoid ruptures jumping back and forth for closely spaced and parallel sections).
 * Is this potentially problematic?
 * 
 * 2) Aseismicity reduces area here
 *
 * 
 * TO DO:
 * 
 * a) Make the moment-rate reduction better (section specific)?
 * 
 * b) Add the following methods from the old version (../oldStuff/RupsInFaultSystemInversion) ????:
 * 
 * 		writeCloseSubSections() 
 * 
 * 
 * @author Field, Milner, Page, & Powers
 *
 */
public class InversionFaultSystemRupSet extends FaultSystemRupSet {
	
	protected final static boolean D = false;  // for debugging
	
	// following are defined in constructor
	DeformationModels defModName;
	FaultModels faultModel;
	List<MagAreaRelationship> magAreaRelList;
	String deformationModelString;
	SlipAlongRuptureModels slipModelType;
	AveSlipForRupModels aveSlipForRupModel;
	InversionModels inversionModel;
	double totalRegionRateMgt5;
	double mMaxOffFault;
	boolean applyImpliedCouplingCoeff;
	SpatialSeisPDF spatialSeisPDF;
	
	List<FaultSectionPrefData> faultSectionData;
	int numSections;
	
	SectionClusterList sectionClusterList;
	
	// section attributes (all in SI units)
	double[] sectSlipRateReduced;	// this gets reduced by moRateReduction (if non zero)
	double[] sectSlipRateStdDevReduced;	// this gets reduced by moRateReduction (if non zero)
	
	// rupture attributes (all in SI units)
	double[] rupMeanMag, rupMeanMoment, rupTotMoRateAvail, rupArea, rupLength, rupMeanSlip, rupRake;
	int[] clusterIndexForRup, rupIndexInClusterForRup;
	ArrayList<ArrayList<Integer>> clusterRupIndexList;
	int numRuptures=0;
	
	double fractionSeisOnFault;
	double impliedOnFaultCouplingCoeff;
	double impliedTotalCouplingCoeff;
	double finalOffFaultCouplingCoeff;
	GutenbergRichterMagFreqDist totalTargetGR;
	SummedMagFreqDist targetOnFaultSupraSeisMFD;
	IncrementalMagFreqDist trulyOffFaultMFD;
	ArrayList<GutenbergRichterMagFreqDist> subSeismoOnFaultMFD_List;
	SummedMagFreqDist totalSubSeismoOnFaultMFD;		// this is a sum of the MFDs in subSeismoOnFaultMFD_List
	
	// discretization parameters for MFDs
	public final static double MIN_MAG = 0.05;
	public final static int NUM_MAG = 90;
	public final static double DELTA_MAG = 0.1;
	
	// general info about this instance
	String infoString;
	
	private List<List<Integer>> sectionConnectionsListList;
	
	/**
	 * This generates a new InversionFaultSystemRupSet for the given fault/deformation mode and all other branch
	 * parameters.
	 * 
	 * @param faultModel
	 * @param defModel
	 * @param magAreaRelList
	 * @param moRateReduction
	 * @param slipModelType
	 * @param aveSlipForRupModel
	 * @param precomputedDataDir
	 * @param filter
	 * @param totalRegionRateMgt5
	 * @param mMaxOffFault
	 * @param applyImpliedCouplingCoeff
	 * @param spatialSeisPDF
	 */
	public InversionFaultSystemRupSet(FaultModels faultModel, DeformationModels defModel,
			List<MagAreaRelationship> magAreaRelList, InversionModels inversionModel, SlipAlongRuptureModels slipModelType,
			AveSlipForRupModels aveSlipForRupModel, File precomputedDataDir, LaughTestFilter filter, double totalRegionRateMgt5, 
			double mMaxOffFault, boolean applyImpliedCouplingCoeff, SpatialSeisPDF spatialSeisPDF) {
		this(new SectionClusterList(faultModel, defModel, precomputedDataDir, filter),
				defModel, null, magAreaRelList, inversionModel, slipModelType, aveSlipForRupModel, totalRegionRateMgt5, 
				mMaxOffFault, applyImpliedCouplingCoeff, spatialSeisPDF);
	}

	/**
	 * This creates a new InversionFaultSystemRupSet for the given cluster list, which may or may have been
	 * generated with this deformation model (but needs to be generated with this fault model!).
	 * 
	 * @param clusters
	 * @param defModel
	 * @param faultSectionData
	 * @param magAreaRelList
	 * @param moRateReduction
	 * @param slipModelType
	 * @param aveSlipForRupModel
	 * @param totalRegionRateMgt5
	 * @param mMaxOffFault
	 * @param applyImpliedCouplingCoeff
	 * @param spatialSeisPDF

	 */
	public InversionFaultSystemRupSet(SectionClusterList clusters, DeformationModels defModel, List<FaultSectionPrefData> faultSectionData,
			List<MagAreaRelationship> magAreaRelList, InversionModels inversionModel, SlipAlongRuptureModels slipModelType,
			AveSlipForRupModels aveSlipForRupModel, double totalRegionRateMgt5, double mMaxOffFault, boolean applyImpliedCouplingCoeff,
		SpatialSeisPDF spatialSeisPDF) {
		
		if (faultSectionData == null)
			// default to using the fault section data from the clusters
			faultSectionData = clusters.getFaultSectionData();
		
		this.faultModel = clusters.getFaultModel();
		this.defModName = defModel;
		this.magAreaRelList = magAreaRelList;
		this.slipModelType = slipModelType;
		this.aveSlipForRupModel = aveSlipForRupModel;
		this.sectionClusterList = clusters;
		this.faultSectionData = faultSectionData;
		this.inversionModel = inversionModel;
		this.totalRegionRateMgt5 = totalRegionRateMgt5;
		this.mMaxOffFault = mMaxOffFault;
		this.applyImpliedCouplingCoeff = applyImpliedCouplingCoeff;
		this.spatialSeisPDF = spatialSeisPDF;
		
		infoString = "FaultSystemRupSet Parameter Settings:\n\n";
		infoString += "\tfaultModel = " +faultModel+ "\n";
		infoString += "\tdefModName = " +defModName+ "\n";
		infoString += "\tdefMod filter basis = " +clusters.getDefModel()+ "\n";
		infoString += "\t" +clusters.getFilter()+ "\n";
		infoString += "\tmagAreaRelList = " +magAreaRelList+ "\n";
		infoString += "\tinversionModel = " +inversionModel+ "\n";
		infoString += "\tslipModelType = " +slipModelType+ "\n";

		if(D) System.out.println(infoString);
		
		// check that indices are same as sectionIDs (this is assumed here)
		for(int i=0; i<faultSectionData.size();i++)
			Preconditions.checkState(faultSectionData.get(i).getSectionId() == i,
				"RupsInFaultSystemInversion: Error - indices of faultSectionData don't match IDs");

		numSections = faultSectionData.size();
		
		// calculate rupture magnitude and other attributes
		calcRuptureAttributes();
	}
	
	public static Vector3D getSlipVector(FaultSectionPrefData section) {
		double[] strikeDipRake = { section.getFaultTrace().getAveStrike(), section.getAveDip(), section.getAveRake() };
		double[] vect = FaultUtils.getSlipVector(strikeDipRake);
		
		return new Vector3D(vect[0], vect[1], vect[2]);
	}
	
	
	/**
	 * Plot magnitude histogram for the inversion ruptures (how many rups at each mag)
	 */
	public void plotMagHistogram() {
		//IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,35,0.1);  // This doesn't go high enough if creeping section is left in for All-California
		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,40,0.1);
		magHist.setTolerance(0.2);	// this makes it a histogram
		for(int r=0; r<getNumRuptures();r++)
			magHist.add(rupMeanMag[r], 1.0);
		ArrayList funcs = new ArrayList();
		funcs.add(magHist);
		magHist.setName("Histogram of Inversion ruptures");
		magHist.setInfo("(number in each mag bin)");
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Magnitude Histogram"); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Num");
	}

	/**
	 * This computes mag and various other attributes of the ruptures
	 */
	private void calcRuptureAttributes() {
	
		if(numRuptures == 0) // make sure this has been computed
			getNumRuptures();
		rupMeanMag = new double[numRuptures];
		rupMeanMoment = new double[numRuptures];
		rupMeanSlip = new double[numRuptures];
		rupTotMoRateAvail = new double[numRuptures];
		rupArea = new double[numRuptures];
		rupLength = new double[numRuptures];
		rupRake = new double[numRuptures];
		clusterIndexForRup = new int[numRuptures];
		rupIndexInClusterForRup = new int[numRuptures];
		clusterRupIndexList = new ArrayList<ArrayList<Integer>>(sectionClusterList.size());
		
		double maxMag=0;
				
		int rupIndex=-1;
		for(int c=0;c<sectionClusterList.size();c++) {
			SectionCluster cluster = sectionClusterList.get(c);
			ArrayList<ArrayList<Integer>> clusterRups = cluster.getSectionIndicesForRuptures();
			ArrayList<Integer> clusterRupIndexes = new ArrayList<Integer>(clusterRups.size());
			clusterRupIndexList.add(clusterRupIndexes);
			for(int r=0;r<clusterRups.size();r++) {
				rupIndex+=1;
				clusterIndexForRup[rupIndex] = c;
				rupIndexInClusterForRup[rupIndex] = r;
				clusterRupIndexes.add(r);
				double totArea=0;
				double totLength=0;
				ArrayList<Integer> sectsInRup = clusterRups.get(r);
				ArrayList<Double> areas = new ArrayList<Double>();
				ArrayList<Double> rakes = new ArrayList<Double>();
				for(Integer sectID:sectsInRup) {
					double length = faultSectionData.get(sectID).getTraceLength()*1e3;	// km --> m
					totLength += length;
					double area = getAreaForSection(sectID);
					totArea += area;
					areas.add(area);
					rakes.add(faultSectionData.get(sectID).getAveRake());
				}
				rupArea[rupIndex] = totArea;
				rupLength[rupIndex] = totLength;
				rupRake[rupIndex] = FaultUtils.getInRakeRange(FaultUtils.getScaledAngleAverage(areas, rakes));
				double mag=0;
				for(MagAreaRelationship magArea: magAreaRelList) {
					mag += magArea.getMedianMag(totArea*1e-6)/magAreaRelList.size();
				}
				rupMeanMag[rupIndex] = mag;
				if(mag>maxMag)
					maxMag = mag;
				rupMeanMoment[rupIndex] = MagUtils.magToMoment(rupMeanMag[rupIndex]);
				// the above is meanMoment in case we add aleatory uncertainty later (aveMoment needed elsewhere); 
				// the above will have to be corrected accordingly as in SoSAF_SubSectionInversion
				// (mean moment != moment of mean mag if aleatory uncertainty included)
				// rupMeanMoment[rupIndex] = MomentMagCalc.getMoment(rupMeanMag[rupIndex])* gaussMFD_slipCorr; // increased if magSigma >0
//				rupMeanSlip[rupIndex] = rupMeanMoment[rupIndex]/(rupArea[rupIndex]*FaultMomentCalc.SHEAR_MODULUS);
				rupMeanSlip[rupIndex] = aveSlipForRupModel.getAveSlip(totArea, totLength);
			}
		}
		
		
		fractionSeisOnFault = DeformationModelsCalc.getFractSpatialPDF_InsideSectionPolygons(faultSectionData, spatialSeisPDF);
		double onFaultRegionRateMgt5 = totalRegionRateMgt5*fractionSeisOnFault;
		double offFaultRegionRateMgt5 = totalRegionRateMgt5-onFaultRegionRateMgt5;
		double onFltDefModMoRate = DeformationModelsCalc.calculateTotalMomentRate(getFaultSectionDataList(),true);
		double offFltDefModMoRate = DeformationModelsCalc.calcMoRateOffFaultsForDefModel(getFaultModel(), getDeformationModel());

		// make the total target GR for region
		totalTargetGR = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		double roundedMmax = totalTargetGR.getX(totalTargetGR.getClosestXIndex(maxMag));
		totalTargetGR.setAllButTotMoRate(MIN_MAG, roundedMmax, totalRegionRateMgt5*1e5, 1.0);

		if (inversionModel == InversionModels.CHAR) {
			
			// check that the following can be calculated since "this" is not fully instantiated
			double aveMinSeismoMag = FaultSystemRupSetCalc.getMeanMinMag(this, true);
			
			trulyOffFaultMFD = FaultSystemRupSetCalc.getTriLinearCharOffFaultTargetMFD(totalTargetGR, onFaultRegionRateMgt5, aveMinSeismoMag, mMaxOffFault);

			subSeismoOnFaultMFD_List = FaultSystemRupSetCalc.getCharSubSeismoOnFaultMFD_forEachSection(this, spatialSeisPDF, totalTargetGR);

			totalSubSeismoOnFaultMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			for(GutenbergRichterMagFreqDist mfd:subSeismoOnFaultMFD_List) {
				totalSubSeismoOnFaultMFD.addIncrementalMagFreqDist(mfd);
			}
			
			targetOnFaultSupraSeisMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			targetOnFaultSupraSeisMFD.addIncrementalMagFreqDist(totalTargetGR);
			targetOnFaultSupraSeisMFD.subtractIncrementalMagFreqDist(trulyOffFaultMFD);
			targetOnFaultSupraSeisMFD.subtractIncrementalMagFreqDist(totalSubSeismoOnFaultMFD);
			
			// compute coupling coefficients
			impliedOnFaultCouplingCoeff = (targetOnFaultSupraSeisMFD.getTotalMomentRate()+totalSubSeismoOnFaultMFD.getTotalMomentRate())/onFltDefModMoRate;
			finalOffFaultCouplingCoeff = trulyOffFaultMFD.getTotalMomentRate()/offFltDefModMoRate;
			impliedTotalCouplingCoeff = totalTargetGR.getTotalMomentRate()/(onFltDefModMoRate+offFltDefModMoRate);

		} else if (inversionModel == InversionModels.GR) {
			
			// get the total GR nucleation MFD for all fault section
			SummedMagFreqDist impliedOnFault_GR_NuclMFD = FaultSystemRupSetCalc.calcImpliedGR_NucleationMFD(this, MIN_MAG, NUM_MAG, DELTA_MAG);
			
			// compute coupling coefficient
			impliedOnFaultCouplingCoeff = onFaultRegionRateMgt5/impliedOnFault_GR_NuclMFD.getCumRate(5.05);
			double tempCoupCoeff = 1;	// defaults to 1.0; this is used below
			if(applyImpliedCouplingCoeff && impliedOnFaultCouplingCoeff < 1.0) 	// only apply if it's < 1
				tempCoupCoeff = impliedOnFaultCouplingCoeff;	
			
			// slit the on-fault MFDs into supra- vs sub-seismo MFDs, and apply tempCoupCoeff
			ArrayList<GutenbergRichterMagFreqDist> grNuclMFD_List = FaultSystemRupSetCalc.calcImpliedNuclMFD_ForEachSection(this, MIN_MAG, NUM_MAG, DELTA_MAG);
			subSeismoOnFaultMFD_List = new ArrayList<GutenbergRichterMagFreqDist>();
			totalSubSeismoOnFaultMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			targetOnFaultSupraSeisMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			for(int s=0;s<grNuclMFD_List.size();s++) {
				GutenbergRichterMagFreqDist grNuclMFD = grNuclMFD_List.get(s);
				int minSupraMagIndex = grNuclMFD.getClosestXIndex(getMinMagForSection(s));
				double maxMagSubSeismo = grNuclMFD.getX(minSupraMagIndex-1);
				GutenbergRichterMagFreqDist subSeisGR = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG, MIN_MAG, maxMagSubSeismo, 1.0, 1.0);
				double rateAtZeroMagBin = grNuclMFD.getY(0)*tempCoupCoeff;
				subSeisGR.scaleToIncrRate(0, rateAtZeroMagBin);
				subSeismoOnFaultMFD_List.add(subSeisGR);
				totalSubSeismoOnFaultMFD.addIncrementalMagFreqDist(subSeisGR);
				for(int i=minSupraMagIndex;i<grNuclMFD.getNum();i++)
					targetOnFaultSupraSeisMFD.add(i, grNuclMFD.getY(i)*tempCoupCoeff);
			}
			
			trulyOffFaultMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG, MIN_MAG, mMaxOffFault, 1.0, 1.0);
			trulyOffFaultMFD.scaleToCumRate(0, offFaultRegionRateMgt5*1e5);
			
			// compute coupling coefficients
			double testOnFaultCouplingCoeff = (targetOnFaultSupraSeisMFD.getTotalMomentRate()+totalSubSeismoOnFaultMFD.getTotalMomentRate())/onFltDefModMoRate;
			// testOnFaultCouplingCoeff should equal impliedOnFaultCouplingCoeff
			finalOffFaultCouplingCoeff = trulyOffFaultMFD.getTotalMomentRate()/offFltDefModMoRate;
			impliedTotalCouplingCoeff = (impliedOnFaultCouplingCoeff*onFltDefModMoRate+finalOffFaultCouplingCoeff*offFltDefModMoRate)/(onFltDefModMoRate+offFltDefModMoRate);
			
		} else if (inversionModel == InversionModels.UNCONSTRAINED) {
			throw new RuntimeException("Not yet implemented");
			
		} else {
			throw new RuntimeException("Not yet implement");
		}

		// compute sectSlipRateReduced
		sectSlipRateReduced = new double[numSections];
		sectSlipRateStdDevReduced = new double[numSections];
		for(int s=0; s<numSections; s++) {
			double subSeismoMoRate = subSeismoOnFaultMFD_List.get(s).getTotalMomentRate();
			double origMoRate = getOrigMomentRate(s);
			double coupCoeffMoRateReduction = 0;
			if(applyImpliedCouplingCoeff && impliedOnFaultCouplingCoeff<1)	// latter must be less than one
				coupCoeffMoRateReduction = origMoRate*impliedOnFaultCouplingCoeff;
			double fracMoRateReduction = (subSeismoMoRate+coupCoeffMoRateReduction)/origMoRate;
			sectSlipRateReduced[s] = faultSectionData.get(s).getReducedAveSlipRate()*1e-3*(1-fracMoRateReduction); // mm/yr --> m/yr; includes moRateReduction
			sectSlipRateStdDevReduced[s] = faultSectionData.get(s).getReducedSlipRateStdDev()*1e-3*(1-fracMoRateReduction); // mm/yr --> m/yr; includes moRateReduction
		}
		for (int r=0; r<numRuptures; r++) {
			double totMoRate=0;
			for (int sectID : getSectionsIndicesForRup(r)) {
				double area = getAreaForSection(sectID);
				totMoRate += FaultMomentCalc.getMoment(area, sectSlipRateReduced[sectID]);
			}
			rupTotMoRateAvail[rupIndex]=totMoRate;
		}
		if (D) System.out.println("DONE creating "+getNumRuptures()+" ruptures!");
	}
	

	/**
	   * This writes the rupture sections to an ASCII file
	   * @param filePathAndName
	   */
	  public void writeRupsToFiles(String filePathAndName) {
		  FileWriter fw;
		  try {
			  fw = new FileWriter(filePathAndName);
			  fw.write("rupID\tclusterID\trupInClustID\tmag\tnumSectIDs\tsect1_ID\tsect2_ID\t...\n");	// header
			  int rupIndex = 0;
			  
			  for(int c=0;c<sectionClusterList.size();c++) {
				  ArrayList<ArrayList<Integer>>  rups = sectionClusterList.get(c).getSectionIndicesForRuptures();
				  for(int r=0; r<rups.size();r++) {
					  ArrayList<Integer> rup = rups.get(r);
					  String line = Integer.toString(rupIndex)+"\t"+Integer.toString(c)+"\t"+Integer.toString(r)+"\t"+
					  				(float)rupMeanMag[rupIndex]+"\t"+rup.size();
					  for(Integer sectID: rup) {
						  line += "\t"+sectID;
					  }
					  line += "\n";
					  fw.write(line);
					  rupIndex+=1;
				  }				  
			  }
			  fw.close();
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
	  }
	  
	  
	  /**
	   * This writes the section data to an ASCII file
	   */
	  public void writeSectionsToFile(String filePathAndName) {
		  ArrayList<String> metaData = new ArrayList<String>();
		  metaData.add("defModName = "+defModName);
		  FaultSectionDataWriter.writeSectionsToFile(faultSectionData, metaData, filePathAndName);
	  }
	  
	  

	  
	/**
	 * This returns the total number of ruptures
	 * @return
	 */
	public int getNumRuptures() {
		if(numRuptures ==0) {
			for(int c=0; c<sectionClusterList.size();c++)
				numRuptures += sectionClusterList.get(c).getNumRuptures();
		}
		return numRuptures;
	}
	
	public int getNumSections() {
		return faultSectionData.size();
	}
	
	public List<FaultSectionPrefData> getFaultSectionDataList() {
		return faultSectionData;
	}
	
	public FaultSectionPrefData getFaultSectionData(int sectIndex) {
		return faultSectionData.get(sectIndex);
	}
	
	@Override
	public List<FaultSectionPrefData> getFaultSectionDataForRupture(int rupIndex) {
		List<Integer> inds = getSectionsIndicesForRup(rupIndex);
		ArrayList<FaultSectionPrefData> datas = new ArrayList<FaultSectionPrefData>();
		for (int ind : inds)
			datas.add(getFaultSectionData(ind));
		return datas;
	}
	
	public List<List<Integer>> getSectionIndicesForAllRups() {
		List<List<Integer>> sectInRupList = new ArrayList<List<Integer>>();
		for(int i=0; i<sectionClusterList.size();i++) {
//			if(D) System.out.println("Working on rupture list for cluster "+i);
			sectInRupList.addAll(sectionClusterList.get(i).getSectionIndicesForRuptures());
		}
		return sectInRupList;
	}
	
	public ArrayList<Integer> getSectionsIndicesForRup(int rupIndex) {
		return sectionClusterList.get(clusterIndexForRup[rupIndex]).getSectionIndicesForRupture(rupIndexInClusterForRup[rupIndex]);
	}

	public double[] getMagForAllRups() {
		return rupMeanMag;
	}

	public double getMagForRup(int rupIndex) {
		return rupMeanMag[rupIndex];
	}

	public double[] getAveSlipForAllRups() {
		return rupMeanSlip;
	}
	
	public double getAveSlipForRup(int rupIndex) {
		return rupMeanSlip[rupIndex];
	}
	
	@Override
	protected double calcTotalAvailableMomentRate(int rupIndex) {
		return rupTotMoRateAvail[rupIndex];
	}
	
	/**
	 * This gets the slip on each section based on the value of slipModelType.
	 * The slips are in meters.  Note that taper slipped model wts slips by area
	 * to maintain moment balance (so it doesn't plot perfectly); do something about this?
	 * 
	 * Note that for two parallel faults that have some overlap, the slip won't be reduced
	 * along the overlap the way things are implemented here.
	 * 
	 * This has been spot checked, but needs a formal test.
	 *
	 */
	public double[] getSlipOnSectionsForRup(int rthRup) {
		return calcSlipOnSectionsForRup(rthRup);
	}
	
	// TODO not yet implemented
	public double[] getAveRakeForAllRups() {
		double[] rakes = new double[numRuptures];
		for (int i=0; i<numRuptures; i++)
			rakes[i] = getAveRakeForRup(i);
		return rakes;
	}
	
	// TODO not yet implemented...
	public double getAveRakeForRup(int rupIndex) {
		return Double.NaN;
	}

	public double[] getAreaForAllRups() {
		return rupArea;
	}
	/**
	 * Area is in sq-m (SI units)
	 */
	public double getAreaForRup(int rupIndex) {
		return rupArea[rupIndex];
	}
	
	@Override
	public double[] getAreaForAllSections() {
		double[] areas = new double[numSections];
		for (int i=0; i<numSections; i++)
			areas[i] = getAreaForSection(i);
		return areas;
	}
	
	/**
	 * Area is in sq-m (SI units)
	 */
	@Override
	public double getAreaForSection(int sectIndex) {
		FaultSectionPrefData sectData = faultSectionData.get(sectIndex);
		return sectData.getTraceLength()*1e3*sectData.getReducedDownDipWidth()*1e3;	// aseismicity reduces area; km --> m on length & DDW
	}
	
	
	/**
	 * Area is in m (SI units)
	 */
	@Override
	public double getLengthForRup(int rupIndex) {
		return rupLength[rupIndex];
	}


	public String getInfoString() {
		return infoString;
	}
	
	public void setInfoString(String info) {
		this.infoString = info;
	}
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getAveLongTermSlipRate()
	 * because of the moment rate reduction (e.g., for smaller events).
	 * @return
	 */
	public double getSlipRateForSection(int sectIndex) {
		return sectSlipRateReduced[sectIndex];
	}
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getAveLongTermSlipRate()
	 * because of the moment rate reduction (e.g., for smaller events).
	 * @return
	 */
	public double[] getSlipRateForAllSections() {
		return sectSlipRateReduced;
	}
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getSlipRateStdDev()
	 * because of the moment rate reduction (e.g., for smaller events).
	 * @return
	 */
	public double getSlipRateStdDevForSection(int sectIndex) {
		return sectSlipRateStdDevReduced[sectIndex];
	}
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getSlipRateStdDev()
	 * because of the moment rate reduction (e.g., for smaller events).
	 * @return
	 */
	public double[] getSlipRateStdDevForAllSections() {
		return sectSlipRateStdDevReduced;
	}
	
	
	@Override
	public boolean isClusterBased() {
		return true;
	}

	@Override
	public int getNumClusters() {
		return sectionClusterList.size();
	}


	@Override
	public int getNumRupturesForCluster(int index) {
		return sectionClusterList.get(index).getNumRuptures();
	}


	@Override
	public ArrayList<Integer> getRupturesForCluster(int index)
			throws IndexOutOfBoundsException {
		return clusterRupIndexList.get(index);
	}


	@Override
	public List<Integer> getSectionsForCluster(int index) {
		return sectionClusterList.get(index);
	}


	@Override
	public List<Integer> getCloseSectionsList(int sectIndex) {
		return sectionConnectionsListList.get(sectIndex);
	}


	@Override
	public List<List<Integer>> getCloseSectionsListList() {
		return sectionConnectionsListList;
	}


	@Override
	public DeformationModels getDeformationModel() {
		return defModName;
	}
	
	@Override
	public FaultModels getFaultModel() {
		return faultModel;
	}

	@Override
	public SlipAlongRuptureModels getSlipAlongRuptureModel() {
		return slipModelType;
	}
	
	
	public double getTotalRegionRateMgt5() {return totalRegionRateMgt5;}
	
	public double getMmaxOffFault() {return mMaxOffFault;}
	
	public boolean getApplyImpliedCouplingCoeffBoolean() {return applyImpliedCouplingCoeff;}
	
	public SpatialSeisPDF getSpatialSeisPDF() {return spatialSeisPDF;}
	
	public double getFractionSeisOnFault() {return fractionSeisOnFault;}
	
	public double getImpliedOnFaultCouplingCoeff() {return impliedOnFaultCouplingCoeff;}
	
	public double getImpliedTotalCouplingCoeff() {return impliedTotalCouplingCoeff;}
	
	public double getFinalOffFaultCouplingCoeff() {return finalOffFaultCouplingCoeff;}
	
	public SummedMagFreqDist getTargetOnFaultSupraSeisMFD() {return targetOnFaultSupraSeisMFD;}
	
	public IncrementalMagFreqDist getTrulyOffFaultMFD() {return trulyOffFaultMFD;}
	
	public ArrayList<GutenbergRichterMagFreqDist> getSubSeismoOnFaultMFD_List() {return subSeismoOnFaultMFD_List;}
	
	public SummedMagFreqDist getTotalSubSeismoOnFaultMFD() {return totalSubSeismoOnFaultMFD;}
	
	public GutenbergRichterMagFreqDist getTotalTargetGR() {return totalTargetGR;}

}
