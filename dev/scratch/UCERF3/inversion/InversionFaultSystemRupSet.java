/**
 * 
 */
package scratch.UCERF3.inversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.eq.MagUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.DeformationModelFetcher.DefModName;
import scratch.UCERF3.utils.FaultSectionDataWriter;

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
	double[] sectSlipRateStdDevReduced;	// this gets reduced by moRateReduction (if non zero)
	
	// rupture attributes (all in SI units)
	double[] rupMeanMag, rupMeanMoment, rupTotMoRateAvail, rupArea, rupLength, rupMeanSlip;
	int[] clusterIndexForRup, rupIndexInClusterForRup;
	ArrayList<ArrayList<Integer>> clusterRupIndexList;
	int numRuptures=0;
	
	// general info about this instance
	String infoString;
	
	private static EvenlyDiscretizedFunc taperedSlipPDF, taperedSlipCDF;
	
	private List<List<Integer>> sectionConnectionsListList;


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
		
		infoString = "FaultSystemRupSet Parameter Settings:\n\n";
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
//		DeformationModelFetcher deformationModelFetcher = new DeformationModelFetcher(DeformationModelFetcher.DefModName.UCERF2_NCAL,precomputedDataDir); // this assumes NCAL, which is set in Run Inversion!  fix below:
		DeformationModelFetcher deformationModelFetcher = new DeformationModelFetcher(defModName,precomputedDataDir);
		faultSectionData = deformationModelFetcher.getSubSectionList();
		double[][] subSectionAzimuths = deformationModelFetcher.getSubSectionAzimuthMatrix();
		double[][] subSectionDistances = deformationModelFetcher.getSubSectionDistanceMatrix();

		
		// check that indices are same as sectionIDs (this is assumed here)
		for(int i=0; i<faultSectionData.size();i++)
			if(faultSectionData.get(i).getSectionId() != i)
				throw new RuntimeException("RupsInFaultSystemInversion: Error - indices of faultSectionData don't match IDs");

		numSections = faultSectionData.size();
		
		// compute sectSlipRateReduced
		sectSlipRateReduced = new double[numSections];
		sectSlipRateStdDevReduced = new double[numSections];
		for(int s=0; s<numSections; s++) {
			sectSlipRateReduced[s] = faultSectionData.get(s).getOrigAveSlipRate()*1e-3*(1-moRateReduction); // mm/yr --> m/yr; includes moRateReduction
			sectSlipRateStdDevReduced[s] = faultSectionData.get(s).getOrigSlipRateStdDev()*1e-3*(1-moRateReduction); // mm/yr --> m/yr; includes moRateReduction
		}

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
	 * For each section, create a list of sections that are within maxJumpDist.  
	 * This generates an ArrayList of ArrayLists (named sectionConnectionsList).  
	 * Reciprocal duplicates are not filtered out.
	 * If sections are actually subsections (meaning getParentSectionId() != -1), then each parent section can only
	 * have one connection to another parent section (whichever subsections are closest).  This prevents parallel 
	 * and closely space faults from having connections back and forth all the way down the section.
	 */
	private List<List<Integer>> computeCloseSubSectionsListList(double[][] sectionDistances) {

		ArrayList<List<Integer>> sectionConnectionsListList = new ArrayList<List<Integer>>();
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
				List<Integer> sectionConnections = sectionConnectionsListList.get(sectIndex);
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
		sectionConnectionsListList = computeCloseSubSectionsListList(subSectionDistances);
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


	private void addClusterLinks(int subSectIndex, SectionCluster list, List<List<Integer>> sectionConnectionsListList) {
		List<Integer> branches = sectionConnectionsListList.get(subSectIndex);
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
			getNumRuptures();
		rupMeanMag = new double[numRuptures];
		rupMeanMoment = new double[numRuptures];
		rupMeanSlip = new double[numRuptures];
		rupTotMoRateAvail = new double[numRuptures];
		rupArea = new double[numRuptures];
		rupLength = new double[numRuptures];
		clusterIndexForRup = new int[numRuptures];
		rupIndexInClusterForRup = new int[numRuptures];
		clusterRupIndexList = new ArrayList<ArrayList<Integer>>(sectionClusterList.size());
				
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
				double totMoRate=0;
				ArrayList<Integer> sectsInRup = clusterRups.get(r);
				for(Integer sectID:sectsInRup) {
					double length = faultSectionData.get(sectID).getTraceLength()*1e3;	// km --> m
					totLength += length;
					double area = getAreaForSection(sectID);
					totArea += area;
					totMoRate += FaultMomentCalc.getMoment(area, sectSlipRateReduced[sectID]);
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
	
	public ArrayList<FaultSectionPrefData> getFaultSectionDataList() {
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
	
	@Override
	public List<Integer> getRupturesForSection(int secIndex) {
		ArrayList<Integer> rups = new ArrayList<Integer>();
		for (int rupID=0; rupID<getNumRuptures(); rupID++) {
			if (getSectionsIndicesForRup(rupID).contains(secIndex))
				rups.add(rupID);
		}
		return rups;
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
			sectArea[index] = sectData.getTraceLength()*sectData.getOrigDownDipWidth()*1e6*(1.0-sectData.getAseismicSlipFactor());	// aseismicity reduces area; 1e6 for sq-km --> sq-m
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
	public double getAreaForSection(int sectIndex) {
		FaultSectionPrefData sectData = faultSectionData.get(sectIndex);
		return sectData.getTraceLength()*1e3*sectData.getOrigDownDipWidth()*1e3*(1.0-sectData.getAseismicSlipFactor());	// aseismicity reduces area; km --> m on length & DDW
	}

	public String getInfoString() {
		return infoString;
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
	public DefModName getDeformationModelName() {
		return defModName;
	}
}
