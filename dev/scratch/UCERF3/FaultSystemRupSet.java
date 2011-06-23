/**
 * 
 */
package scratch.UCERF3;

import java.io.File;
import java.util.ArrayList;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.eq.MagUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.utils.DeformationModelFetcher;

/**
 * TO DO:
 * 
 * 1) compute: rupAveSlip, rupSlipOnSect, infoString
 * 2) Separate this class from the one that reads a file (move last methods to parent?)
 * 3) Implement use of slipModelTypeString
 * 
 * @author Field, Milner, Page, & Powers
 *
 */
public class FaultSystemRupSet {
	
	protected final static boolean D = true;  // for debugging
	
	// following are defined in constructor
	DeformationModelFetcher.DefModName defModName;
	double maxJumpDist, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff,moRateReduction;
	int minNumSectInRup;
	ArrayList<MagAreaRelationship> magAreaRelList;
	String slipModelType,deformationModelString;
	File precomputedDataDir;
	
	
	ArrayList<FaultSectionPrefData> faultSectionData;
	int numSections;
	
	ArrayList<ArrayList<Integer>> sectionConnectionsListList, endToEndSectLinksList;
	
	ArrayList<SectionCluster> sectionClusterList;
	
	// section attributes (all in SI units)
	double[] sectSlipRateReduced;	// this gets reduced by moRateReduction (if non zero)
	
	// rupture attributes (all in SI units)
	double[] rupMeanMag, rupMeanMoment, rupTotMoRateAvail, rupArea, rupLength, rupAveSlip;
	int[] clusterIndexForRup, rupIndexInClusterForRup;
	double[][] rupSlipOnSect;
	int numRuptures=0;
	
	// general info about this instance
	String infoString;


	/**
	 * Constructor
	 * @param defModName
	 * @param maxJumpDist
	 * @param maxAzimuthChange
	 * @param maxTotAzimuthChange
	 * @param maxRakeDiff
	 * @param minNumSectInRup
	 * @param magAreaRelList
	 * @param moRateReduction
	 * @param slipModelTypeString
	 * @param precomputedDataDir
	 */
	public FaultSystemRupSet(DeformationModelFetcher.DefModName defModName,double maxJumpDist, 
			double maxAzimuthChange, double maxTotAzimuthChange, double maxRakeDiff, 
			int minNumSectInRup, ArrayList<MagAreaRelationship> magAreaRelList, 
			double moRateReduction, String slipModelTypeString,File precomputedDataDir) {

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
		
		DeformationModelFetcher deformationModelFetcher = new DeformationModelFetcher(DeformationModelFetcher.DefModName.UCERF2_NCAL,precomputedDataDir);
		faultSectionData = deformationModelFetcher.getSubSectionList();
		double[][] subSectionAzimuths = deformationModelFetcher.getSubSectionAzimuthMatrix();
		double[][] subSectionDistances = deformationModelFetcher.getSubSectionDistanceMatrix();

		
		// check that indices are same as sectionIDs
		for(int i=0; i<faultSectionData.size();i++)
			if(faultSectionData.get(i).getSectionId() != i)
				throw new RuntimeException("RupsInFaultSystemInversion: Error - indices of faultSectionData don't match IDs");

		numSections = faultSectionData.size();
		
		// compute sectSlipRateReduced (add standard deviations here as well?)
		sectSlipRateReduced = new double[numSections];
		for(int s=0; s<numSections; s++)
			sectSlipRateReduced[s] = faultSectionData.get(s).getAveLongTermSlipRate()*1e-3*(1-moRateReduction); // mm/yr --> m/yr; includes moRateReduction

		// make the list of nearby sections for each section (branches)
		if(D) System.out.println("Making sectionConnectionsListList");
		computeCloseSubSectionsListList(subSectionDistances);
		if(D) System.out.println("Done making sectionConnectionsListList");

		// make the list of SectionCluster objects 
		// (each represents a set of nearby sections and computes the possible
		//  "ruptures", each defined as a list of sections in that rupture)
		makeClusterList(subSectionAzimuths);
		
		// calculate rupture magnitude and other attributes
		calcRuptureAttributes();
		
		
		// plot magnitude histogram for the inversion ruptures (how many rups at each mag)
		// comment this out if you don't want it popping up (if you're using SCEC VDO)
/*		IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,35,0.1);
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
*/	
		
	}
	
	
	/**
	 * For each section, create a list of sections that are within maxJumpDist.  
	 * This generates an ArrayList of ArrayLists (named sectionConnectionsList).  
	 * Reciprocal duplicates are not filtered out.
	 * If sections are actually subsections (meaning getParentSectionId() != -1), then each parent section can only
	 * have one connection to another parent section (whichever subsections are closest).  This prevents parallel 
	 * and closely space faults from having connections back and forth all the way down the section.
	 */
	private void computeCloseSubSectionsListList(double[][] sectionDistances) {

		sectionConnectionsListList = new ArrayList<ArrayList<Integer>>();
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
	}
	
	
	
	private void makeClusterList(double[][] sectionAzimuths) {

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
			addClusterLinks(firstSubSection, newCluster);
			// remove the used subsections from the available list
			for(int i=0; i<newCluster.size();i++) availableSections.remove(newCluster.get(i));
			// add this cluster to the list
			sectionClusterList.add(newCluster);
			if (D) System.out.println(newCluster.size()+"\tsubsections in cluster #"+sectionClusterList.size()+"\t"+
					availableSections.size()+"\t subsections left to allocate");
		}
	}


	private void addClusterLinks(int subSectIndex, SectionCluster list) {
		ArrayList<Integer> branches = sectionConnectionsListList.get(subSectIndex);
		for(int i=0; i<branches.size(); i++) {
			Integer subSect = branches.get(i);
			if(!list.contains(subSect)) {
				list.add(subSect);
				addClusterLinks(subSect, list);
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
					FaultSectionPrefData sectData = faultSectionData.get(sectID);
					double length = sectData.getLength()*1e3;	// km --> m
					totLength += length;
					double area = length*sectData.getDownDipWidth()*1e3*(1.0-sectData.getAseismicSlipFactor());	// aseismicity reduces area; km --> m on DDW
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
			}
		}
	}
	
	// BOLOW HERE IS WHAT COULD BE IN A PARENT INTERFACE OR ABSTRACT CLASS

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
	
	public ArrayList<FaultSectionPrefData> getFaultSectionList() {
		return faultSectionData;
	}
	
	public ArrayList<ArrayList<Integer>> getSectionsInRutures() {
		ArrayList<ArrayList<Integer>> sectInRupList = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<sectionClusterList.size();i++) {
			if(D) System.out.println("Working on rupture list for cluster "+i);
			sectInRupList.addAll(sectionClusterList.get(i).getSectionIndicesForRuptures());
		}
		return sectInRupList;
	}
	
	public double[] getMags() {
		return rupMeanMag;
	}
	
	public double[] getAveSlip() {
		return rupAveSlip;
	}
	
	public double[][] getRupSlipOnSect() {
		return rupSlipOnSect;
	}
	
	public String getInfoString() {
		return infoString;
	}




}
