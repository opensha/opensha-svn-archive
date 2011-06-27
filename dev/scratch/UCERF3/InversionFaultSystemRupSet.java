/**
 * 
 */
package scratch.UCERF3;

import java.io.File;
import java.util.ArrayList;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.eq.MagUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.utils.DeformationModelFetcher;

/**
 * This class represents a FaultSystemRupSet for the Grand Inversion.
 * 
 * TO DO:
 * 
 * a) Add ability to save this to a file or database; I see two options:  
 * 
 *    1) save everything here (including the sectionClusterList) and add a constructor 
 *       to build this class back from a file or database (or an if statement in
 *       the current constructor to read it in if it already exists)
 *       
 *    2) save everything except the sectionClusterList and make a new class to read that 
 *       (with the same methods, but rewritten to not require use of the clusterList)
 *       
 *    3) Save everything except the sectionClusterList and change this class to not require
 *       the sectionClusterList after construction (but then it won't be avail to SCEC VDO)
 * 
 * b) Add comments
 * 
 * @author Field, Milner, Page, & Powers
 *
 */
public class InversionFaultSystemRupSet implements FaultSystemRupSet {
	
	protected final static boolean D = true;  // for debugging
	
	// following are defined in constructor
	DeformationModelFetcher.DefModName defModName;
	double maxJumpDist, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff, moRateReduction;
	int minNumSectInRup;
	ArrayList<MagAreaRelationship> magAreaRelList;
	String deformationModelString;
	public enum SlipModelType {
		CHAR_SLIP_MODEL,	// "Characteristic (Dsr=Ds)"
		UNIFORM_SLIP_MODEL,	// "Uniform/Boxcar (Dsr=Dr)"
		WG02_SLIP_MODEL,	// "WGCEP-2002 model (Dsr prop to Vs)"
		TAPERED_SLIP_MODEL;	// "Tapered Ends ([Sin(x)]^0.5)"
	}
	SlipModelType slipModelType;
	File precomputedDataDir;
	
	ArrayList<FaultSectionPrefData> faultSectionData;
	int numSections;
	
	ArrayList<SectionCluster> sectionClusterList;
	
	// section attributes (all in SI units)
	double[] sectSlipRateReduced;	// this gets reduced by moRateReduction (if non zero)
	
	// rupture attributes (all in SI units)
	double[] rupMeanMag, rupMeanMoment, rupTotMoRateAvail, rupArea, rupLength, rupMeanSlip;
	int[] clusterIndexForRup, rupIndexInClusterForRup;
	int numRuptures=0;
	
	// general info about this instance
	String infoString;
	
	private static EvenlyDiscretizedFunc taperedSlipPDF, taperedSlipCDF;


	/**
	 * Constructor.
	 * 
	 * @param defModName
	 * @param maxJumpDist
	 * @param maxAzimuthChange
	 * @param maxTotAzimuthChange
	 * @param maxRakeDiff
	 * @param minNumSectInRup
	 * @param magAreaRelList
	 * @param moRateReduction
	 * @param slipModelType
	 * @param precomputedDataDir
	 */
	public InversionFaultSystemRupSet(DeformationModelFetcher.DefModName defModName,double maxJumpDist, 
			double maxAzimuthChange, double maxTotAzimuthChange, double maxRakeDiff, 
			int minNumSectInRup, ArrayList<MagAreaRelationship> magAreaRelList, 
			double moRateReduction, SlipModelType slipModelType, File precomputedDataDir) {

		this.defModName=defModName;
		this.maxJumpDist=maxJumpDist;
		this.maxAzimuthChange=maxAzimuthChange; 
		this.maxTotAzimuthChange=maxTotAzimuthChange; 
		this.maxRakeDiff=maxRakeDiff;
		this.minNumSectInRup=minNumSectInRup;
		this.magAreaRelList=magAreaRelList;
		this.moRateReduction=moRateReduction;
		this.slipModelType=slipModelType;
		this.precomputedDataDir = precomputedDataDir;
		
		infoString += "FaultSystemRupSet Parameter Settings:\n\n";
		infoString += "\tdefModName = " +defModName+ "\n";
		infoString += "\tmaxJumpDist = " +maxJumpDist+ "\n";
		infoString += "\tmaxAzimuthChange = " +maxAzimuthChange+ "\n";
		infoString += "\tmaxTotAzimuthChange = " +maxTotAzimuthChange+ "\n";
		infoString += "\tmaxRakeDiff = " +maxRakeDiff+ "\n";
		infoString += "\tminNumSectInRup = " +minNumSectInRup+ "\n";
		infoString += "\tmagAreaRelList = " +magAreaRelList+ "\n";
		infoString += "\tmoRateReduction = " +moRateReduction+ "\n";
		infoString += "\tslipModelType = " +slipModelType+ "\n";
		infoString += "\tprecomputedDataDir = " +precomputedDataDir+ "\n";

		if(D) System.out.println(infoString);

		// Get stuff from the DeformationModelFetcher
		DeformationModelFetcher deformationModelFetcher = new DeformationModelFetcher(DeformationModelFetcher.DefModName.UCERF2_NCAL,precomputedDataDir);
		faultSectionData = deformationModelFetcher.getSubSectionList();
		double[][] subSectionAzimuths = deformationModelFetcher.getSubSectionAzimuthMatrix();
		double[][] subSectionDistances = deformationModelFetcher.getSubSectionDistanceMatrix();

		
		// check that indices are same as sectionIDs (this is assumed here)
		for(int i=0; i<faultSectionData.size();i++)
			if(faultSectionData.get(i).getSectionId() != i)
				throw new RuntimeException("RupsInFaultSystemInversion: Error - indices of faultSectionData don't match IDs");

		numSections = faultSectionData.size();
		
		// compute sectSlipRateReduced (add standard deviations here as well?)
		sectSlipRateReduced = new double[numSections];
		for(int s=0; s<numSections; s++)
			sectSlipRateReduced[s] = faultSectionData.get(s).getAveLongTermSlipRate()*1e-3*(1-moRateReduction); // mm/yr --> m/yr; includes moRateReduction

		// make the list of SectionCluster objects 
		// (each represents a set of nearby sections and computes the possible
		//  "ruptures", each defined as a list of sections in that rupture)
		makeClusterList(subSectionAzimuths,subSectionDistances);
		
		// calculate rupture magnitude and other attributes
		calcRuptureAttributes();
		
	}
	
	
	/**
	 * Plot magnitude histogram for the inversion ruptures (how many rups at each mag)
	 */
	public void plotMagHistogram() {
		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,35,0.1);
		magHist.setTolerance(0.2);	// this makes it a histogram
		for(int r=0; r<getNumRupRuptures();r++)
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
	 * For each section, create a list of sections that are within maxJumpDist.  
	 * This generates an ArrayList of ArrayLists (named sectionConnectionsList).  
	 * Reciprocal duplicates are not filtered out.
	 * If sections are actually subsections (meaning getParentSectionId() != -1), then each parent section can only
	 * have one connection to another parent section (whichever subsections are closest).  This prevents parallel 
	 * and closely space faults from having connections back and forth all the way down the section.
	 */
	private ArrayList<ArrayList<Integer>> computeCloseSubSectionsListList(double[][] sectionDistances) {

		ArrayList<ArrayList<Integer>> sectionConnectionsListList = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<numSections;i++)
			sectionConnectionsListList.add(new ArrayList<Integer>());

		// in case the sections here are subsections of larger sections, create a subSectionDataListList where each
		// ArrayList<FaultSectionPrefData> is a list of subsections from the parent section
		ArrayList<ArrayList<FaultSectionPrefData>> subSectionDataListList = new ArrayList<ArrayList<FaultSectionPrefData>>();
		int lastID=-1;
		ArrayList<FaultSectionPrefData> newList = new ArrayList<FaultSectionPrefData>();
		for(int i=0; i<faultSectionData.size();i++) {
			FaultSectionPrefData subSect = faultSectionData.get(i);
			int parentID = subSect.getParentSectionId();
			if(parentID != lastID || parentID == -1) { // -1 means there is no parent
				newList = new ArrayList<FaultSectionPrefData>();
				subSectionDataListList.add(newList);
				lastID = subSect.getParentSectionId();
			}
			newList.add(subSect);
		}


		// First, if larger sections have been sub-sectioned, fill in neighboring subsection connections
		// (using the other algorithm below might lead to subsections being skipped if their width is < maxJumpDist) 
		for(int i=0; i<subSectionDataListList.size(); ++i) {
			ArrayList<FaultSectionPrefData> subSectList = subSectionDataListList.get(i);
			int numSubSect = subSectList.size();
			for(int j=0;j<numSubSect;j++) {
				// get index of section
				int sectIndex = subSectList.get(j).getSectionId();
				ArrayList<Integer> sectionConnections = sectionConnectionsListList.get(sectIndex);
				if(j != 0) // skip the first one since it has no previous subsection
					sectionConnections.add(subSectList.get(j-1).getSectionId());
				if(j != numSubSect-1) // the last one has no subsequent subsection
					sectionConnections.add(subSectList.get(j+1).getSectionId());
			}
		}

		// now add subsections on other sections, keeping only one connection between each section (the closest)
		for(int i=0; i<subSectionDataListList.size(); ++i) {
			ArrayList<FaultSectionPrefData> sect1_List = subSectionDataListList.get(i);
			for(int j=i+1; j<subSectionDataListList.size(); ++j) {
				ArrayList<FaultSectionPrefData> sect2_List = subSectionDataListList.get(j);
				double minDist=Double.MAX_VALUE;
				int subSectIndex1 = -1;
				int subSectIndex2 = -1;
				// find the closest pair
				for(int k=0;k<sect1_List.size();k++) {
					for(int l=0;l<sect2_List.size();l++) {
						int index1 = sect1_List.get(k).getSectionId();
						int index2 = sect2_List.get(l).getSectionId();;
						double dist = sectionDistances[index1][index2];
						if(dist < minDist) {
							minDist = dist;
							subSectIndex1 = index1;
							subSectIndex2 = index2;
						}					  
					}
				}
				// add to lists for each subsection
				if (minDist<maxJumpDist) {
					sectionConnectionsListList.get(subSectIndex1).add(subSectIndex2);
					sectionConnectionsListList.get(subSectIndex2).add(subSectIndex1);  // reciprocal of the above
				}
			}
		}
		return sectionConnectionsListList;
	}
	
	
	private void makeClusterList(double[][] sectionAzimuths, double[][] subSectionDistances) {
		
		// make the list of nearby sections for each section (branches)
		if(D) System.out.println("Making sectionConnectionsListList");
		ArrayList<ArrayList<Integer>> sectionConnectionsListList = computeCloseSubSectionsListList(subSectionDistances);
		if(D) System.out.println("Done making sectionConnectionsListList");

		// make an arrayList of section indexes
		ArrayList<Integer> availableSections = new ArrayList<Integer>();
		for(int i=0; i<numSections; i++) availableSections.add(i);

		sectionClusterList = new ArrayList<SectionCluster>();
		while(availableSections.size()>0) {
			if (D) System.out.println("WORKING ON CLUSTER #"+(sectionClusterList.size()+1));
			int firstSubSection = availableSections.get(0);
			SectionCluster newCluster = new SectionCluster(faultSectionData, minNumSectInRup,sectionConnectionsListList,
					sectionAzimuths, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff);
			newCluster.add(firstSubSection);
			if (D) System.out.println("\tfirst is "+faultSectionData.get(firstSubSection).getName());
			addClusterLinks(firstSubSection, newCluster, sectionConnectionsListList);
			// remove the used subsections from the available list
			for(int i=0; i<newCluster.size();i++) availableSections.remove(newCluster.get(i));
			// add this cluster to the list
			sectionClusterList.add(newCluster);
			if (D) System.out.println(newCluster.size()+"\tsubsections in cluster #"+sectionClusterList.size()+"\t"+
					availableSections.size()+"\t subsections left to allocate");
		}
	}


	private void addClusterLinks(int subSectIndex, SectionCluster list, ArrayList<ArrayList<Integer>> sectionConnectionsListList) {
		ArrayList<Integer> branches = sectionConnectionsListList.get(subSectIndex);
		for(int i=0; i<branches.size(); i++) {
			Integer subSect = branches.get(i);
			if(!list.contains(subSect)) {
				list.add(subSect);
				addClusterLinks(subSect, list, sectionConnectionsListList);
			}
		}
	}
	

	/**
	 * This computes mag and various other attributes of the ruptures
	 */
	private void calcRuptureAttributes() {
	
		if(numRuptures == 0) // make sure this has been computed
			getNumRupRuptures();
		rupMeanMag = new double[numRuptures];
		rupMeanMoment = new double[numRuptures];
		rupMeanSlip = new double[numRuptures];
		rupTotMoRateAvail = new double[numRuptures];
		rupArea = new double[numRuptures];
		rupLength = new double[numRuptures];
		clusterIndexForRup = new int[numRuptures];
		rupIndexInClusterForRup = new int[numRuptures];
				
		int rupIndex=-1;
		for(int c=0;c<sectionClusterList.size();c++) {
			SectionCluster cluster = sectionClusterList.get(c);
			ArrayList<ArrayList<Integer>> clusterRups = cluster.getSectionIndicesForRuptures();
			for(int r=0;r<clusterRups.size();r++) {
				rupIndex+=1;
				clusterIndexForRup[rupIndex] = c;
				rupIndexInClusterForRup[rupIndex] = r;
				double totArea=0;
				double totLength=0;
				double totMoRate=0;
				ArrayList<Integer> sectsInRup = clusterRups.get(r);
				for(Integer sectID:sectsInRup) {
					double length = faultSectionData.get(sectID).getLength()*1e3;	// km --> m
					totLength += length;
					double area = getAreaForSection(sectID);
					totArea += area;
					totMoRate = FaultMomentCalc.getMoment(area, sectSlipRateReduced[sectID]);
				}
				rupArea[rupIndex] = totArea;
				rupLength[rupIndex] = totLength;
				double mag=0;
				for(MagAreaRelationship magArea: magAreaRelList) {
					mag += magArea.getMedianMag(totArea*1e-6)/magAreaRelList.size();
				}
				rupMeanMag[rupIndex] = mag;
				rupMeanMoment[rupIndex] = MagUtils.magToMoment(rupMeanMag[rupIndex]);
				// the above is meanMoment in case we add aleatory uncertainty later (aveMoment needed elsewhere); 
				// the above will have to be corrected accordingly as in SoSAF_SubSectionInversion
				// (mean moment != moment of mean mag if aleatory uncertainty included)
				// rupMeanMoment[rupIndex] = MomentMagCalc.getMoment(rupMeanMag[rupIndex])* gaussMFD_slipCorr; // increased if magSigma >0
				rupTotMoRateAvail[rupIndex]=totMoRate;
				rupMeanSlip[rupIndex] = rupMeanMoment[rupIndex]/(rupArea[rupIndex]*FaultMomentCalc.SHEAR_MODULUS);
			}
		}
	}
	
	/**
	 * This returns the total number of ruptures
	 * @return
	 */
	public int getNumRupRuptures() {
		if(numRuptures ==0) {
			for(int c=0; c<sectionClusterList.size();c++)
				numRuptures += sectionClusterList.get(c).getNumRuptures();
		}
		return numRuptures;
	}
	
	public int getNumSections() {
		return faultSectionData.size();
	}
	
	public ArrayList<FaultSectionPrefData> getFaultSectionList() {
		return faultSectionData;
	}
	
	public FaultSectionPrefData getFaultSectionData(int sectIndex) {
		return faultSectionData.get(sectIndex);
	}

	
	public ArrayList<ArrayList<Integer>> getSectionIndicesForAllRups() {
		ArrayList<ArrayList<Integer>> sectInRupList = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<sectionClusterList.size();i++) {
			if(D) System.out.println("Working on rupture list for cluster "+i);
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
	
	public ArrayList<double[]> getSlipOnSectionsForAllRups() {
		ArrayList<double[]> rupSlipOnSect = new ArrayList<double[]>();
		for(int r=0;r<this.numRuptures;r++)
			rupSlipOnSect.add(getSlipOnSectionsForRup(r));
		return rupSlipOnSect;
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
		
		ArrayList<Integer> sectionIndices = getSectionsIndicesForRup(rthRup);
		int numSects = sectionIndices.size();

		double[] slipsForRup = new double[numSects];
		
		// compute rupture area
		double[] sectArea = new double[numSects];
		double[] sectMoRate = new double[numSects];
		int index=0;
		for(Integer sectID: sectionIndices) {	
			FaultSectionPrefData sectData = faultSectionData.get(sectID);
			sectArea[index] = sectData.getLength()*sectData.getDownDipWidth()*1e6*(1.0-sectData.getAseismicSlipFactor());	// aseismicity reduces area; 1e6 for sq-km --> sq-m
			sectMoRate[index] = FaultMomentCalc.getMoment(sectArea[index], sectSlipRateReduced[sectID]);
			index += 1;
		}
			 		
		double aveSlip = rupMeanSlip[rthRup];  // in meters
		
		// for case segment slip is independent of rupture (constant), and equal to slip-rate * MRI
		if(slipModelType == SlipModelType.CHAR_SLIP_MODEL) {
			throw new RuntimeException("SlipModelType.CHAR_SLIP_MODEL not yet supported");
		}
		// for case where ave slip computed from mag & area, and is same on all segments 
		else if (slipModelType == SlipModelType.UNIFORM_SLIP_MODEL) {
			for(int s=0; s<slipsForRup.length; s++)
				slipsForRup[s] = aveSlip;
		}
		// this is the model where section slip is proportional to section slip rate 
		// (bumped up or down based on ratio of seg slip rate over wt-ave slip rate (where wts are seg areas)
		else if (slipModelType == SlipModelType.WG02_SLIP_MODEL) {
			for(int s=0; s<slipsForRup.length; s++) {
				slipsForRup[s] = aveSlip*sectMoRate[s]*rupArea[rthRup]/(rupTotMoRateAvail[rthRup]*sectArea[s]);
			}
		}
		else if (slipModelType == SlipModelType.TAPERED_SLIP_MODEL) {
			// note that the ave slip is partitioned by area, not length; this is so the final model is moment balanced.

			// make the taper function if hasn't been done yet
			if(taperedSlipCDF == null) {
				taperedSlipCDF = new EvenlyDiscretizedFunc(0, 5001, 0.0002);
				taperedSlipPDF = new EvenlyDiscretizedFunc(0, 5001, 0.0002);
				double x,y, sum=0;
				int num = taperedSlipPDF.getNum();
				for(int i=0; i<num;i++) {
					x = taperedSlipPDF.getX(i);
					y = Math.pow(Math.sin(x*Math.PI), 0.5);
					taperedSlipPDF.set(i,y);
					sum += y;
				}
				// now make final PDF & CDF
				y=0;
				for(int i=0; i<num;i++) {
						y += taperedSlipPDF.getY(i);
						taperedSlipCDF.set(i,y/sum);
						taperedSlipPDF.set(i,taperedSlipPDF.getY(i)/sum);
//						System.out.println(taperedSlipCDF.getX(i)+"\t"+taperedSlipPDF.getY(i)+"\t"+taperedSlipCDF.getY(i));
				}
			}
			double normBegin=0, normEnd, scaleFactor;
			for(int s=0; s<slipsForRup.length; s++) {
				normEnd = normBegin + sectArea[s]/rupArea[rthRup];
				// fix normEnd values that are just past 1.0
				if(normEnd > 1 && normEnd < 1.00001) normEnd = 1.0;
				scaleFactor = taperedSlipCDF.getInterpolatedY(normEnd)-taperedSlipCDF.getInterpolatedY(normBegin);
				scaleFactor /= (normEnd-normBegin);
				slipsForRup[s] = aveSlip*scaleFactor;
				normBegin = normEnd;
			}
		}
/*		*/
		// check the average
//		if(D) {
//			double aveCalcSlip =0;
//			for(int s=0; s<slipsForRup.length; s++)
//				aveCalcSlip += slipsForRup[s]*sectArea[s];
//			aveCalcSlip /= rupArea[rthRup];
//			System.out.println("AveSlip & CalcAveSlip:\t"+(float)aveSlip+"\t"+(float)aveCalcSlip);
//		}

//		if (D) {
//			System.out.println("\tsectionSlip\tsectSlipRate\tsectArea");
//			for(int s=0; s<slipsForRup.length; s++) {
//				FaultSectionPrefData sectData = faultSectionData.get(sectionIndices.get(s));
//				System.out.println(s+"\t"+(float)slipsForRup[s]+"\t"+(float)sectData.getAveLongTermSlipRate()+"\t"+sectArea[s]);
//			}
//					
//		}
		return slipsForRup;		
	}
	
	// not yet implemented...
	public double getAveRakeForRup(int rupIndex) {
		return Double.NaN;
	}
	
	/**
	 * Area is in sq-m (SI units)
	 */
	public double getAreaForRup(int rupIndex) {
		return rupArea[rupIndex];
	}
	
	/**
	 * Area is in sq-m (SI units)
	 */
	public double getAreaForSection(int sectIndex) {
		FaultSectionPrefData sectData = faultSectionData.get(sectIndex);
		return sectData.getLength()*1e3*sectData.getDownDipWidth()*1e3*(1.0-sectData.getAseismicSlipFactor());	// aseismicity reduces area; km --> m on length & DDW
	}

	public String getInfoString() {
		return infoString;
	}
	
	
	public double[] getSectSlipRateReduced() {
		return sectSlipRateReduced;
	}
	
}
