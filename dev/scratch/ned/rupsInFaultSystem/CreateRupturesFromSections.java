package scratch.ned.rupsInFaultSystem;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.opensha.commons.data.NamedObjectComparator;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;

public class CreateRupturesFromSections {
	
    protected final static boolean D = false;  // for debugging

	
	ArrayList<FaultSectionPrefData> allFaultSectionPrefData;
	double subSectionDistances[][];
	String endPointNames[];
	Location endPointLocs[];
	int numSections, numSubSections, minNumSubSectInRup;
	ArrayList<ArrayList<Integer>> subSectionConnectionsListList, endToEndSectLinksList;
	double maxJumpDist, maxAngle, maxTotStrikeChange, maxSubSectionLength;
	ArrayList<ArrayList<FaultSectionPrefData>> subSectionPrefDataListList;
	ArrayList<FaultSectionPrefData> subSectionPrefDataList; // same as above, but a sequential list (not list of lists)
	
	// this is to store the section and subsection indices for the ith subsection.
	int[]  sectForSubSectionMapping,subSectForSubSectMapping, firstSubsectOfSectMapping;
	// this gives the section index for the ith sections and jth subsection withing
	int[][] subSectForSectAndSubsectMapping;
	
	ArrayList<SectionCluster> sectionClusterList;
	
	/**
	 * 
	 * @param maxJumpDist
	 * @param maxAngle
	 * @param maxStrikeChange
	 * @param maxSubSectionLength - In units of down-dip width
	 * @param minNumSubSectInRup
	 */
	public CreateRupturesFromSections(double maxJumpDist, double maxAngle, double maxStrikeChange, 
			double maxSubSectionLength, int minNumSubSectInRup) {
		
		System.out.println("maxDist="+maxJumpDist+"\tmaxAngle="+maxAngle+"\tmaxStrikeChange="+
				maxStrikeChange+"\tmaxSubSectionLength="+maxSubSectionLength+"\tminNumSubSectInRup="+minNumSubSectInRup);
		
		this.maxJumpDist=maxJumpDist;
		this.maxAngle=maxAngle;  					// not used right now
		this.maxTotStrikeChange=maxStrikeChange;	// not used right now
		this.maxSubSectionLength=maxSubSectionLength;
		this.minNumSubSectInRup=minNumSubSectInRup;
		
		Boolean includeSectionsWithNaN_slipRates = false;
		
		// Create the sections and subsections and mappings
		createSubSections(includeSectionsWithNaN_slipRates);
		
		// write the number of sections and subsections
		System.out.println("numSections="+numSections+";  numSubSections="+numSubSections);
		
		// compute the distances between subsections
		long startTime=System.currentTimeMillis();
		calcSubSectionDistances();
		int runtime = (int)(System.currentTimeMillis()-startTime)/1000;
		System.out.println("calcSubSectionDistances Run took "+runtime+" seconds");

		// a faster way
/*		startTime=System.currentTimeMillis();
		calcSubSectionDistancesFast();
		runtime = (int)(System.currentTimeMillis()-startTime)/1000;
		System.out.println("calcSubSectionDistancesFast Run took "+runtime+" seconds");
*/
		
		// make the list of nearby subsections for each subsection (branches)
		computeCloseSubSectionsListList();
		
		// make the list of SectionCluser objects 
		// (each represents a set of nearby subsections and computes the possible
		//  "ruptures", each defined as a list of subsections in that rupture)
		makeClusterList();
		
		writeCloseSubSections();

/*		
		// this writes out the ruptures for a given cluster
		int clusterIndex = 0;
		ArrayList<ArrayList<Integer>> rups = sectionClusterList.get(clusterIndex).getRuptures();
		ArrayList<ArrayList<Integer>> rupsIndices = sectionClusterList.get(clusterIndex).getRupturesByIndices();
		System.out.println("Cluster "+clusterIndex+" has "+sectionClusterList.get(clusterIndex).size()+
				" subsections and "+rups.size()+" ruptures");
		Integer id = rups.get(0).get(0);  // ID of first subsection of first rupture
		for(int i=0;i<50;i++) {
			ArrayList<Integer> rup = rups.get(i);
			ArrayList<Integer> rupIndices = rupsIndices.get(i);
//			if(rup.get(0) != id) break;
			System.out.println("rup "+i+":");
			for(int j=0;j<rup.size();j++)
				System.out.println("\t"+subSectionPrefDataList.get(rupIndices.get(j)).getName());
		}
*/
		/*		
		for(int i=1; i<clusterList.size(); i++) {
			SectionCluster cluster = clusterList.get(i);
			System.out.println("CONTENTS OF CLUSTER #"+i);
			ArrayList<Integer> allSubSectionsIdList = cluster.getAllSubSectionsIdList();
			for(int j=0; j< cluster.size();j++)
				System.out.println("\t"+subSectionPrefDataList.get(cluster.get(j)).getName()+"\t"+allSubSectionsIdList.get(j));
		}
				*/
//		System.out.println("numSubSections="+numSubSections+"\tnumRups="+getNumRupRuptures());
		
//		for(int i=0; i<sectionClusterList.size();i++)
//		System.out.println("sectionClusterList.get(0).getRuptures().size()="+sectionClusterList.get(0).getRuptures().size());
///			System.out.println("Cluster "+i+" has "+sectionClusterList.get(i).getNumSubSections()+" subsections");
	}
	
	
	
	public int getNumClusters() {
		return sectionClusterList.size();
	}
	
	public SectionCluster getCluster(int clusterIndex) {
		return sectionClusterList.get(clusterIndex);
	}
	
	
	
	  public ArrayList<ArrayList<Integer>> getRupList() {
		  ArrayList<ArrayList<Integer>> rupList = new ArrayList<ArrayList<Integer>>();
		  for(int i=0; i<sectionClusterList.size();i++) {
System.out.println("Working on rupture list for cluster "+i);
			  rupList.addAll(sectionClusterList.get(i).getRuptures());
		  }
		  return rupList;
	  }

	  
	  public int getNumRupRuptures() {
		  int num = 0;
		  for(int i=0; i<sectionClusterList.size();i++) {
			  num += sectionClusterList.get(i).getNumRuptures();
		  }
		  return num;
	  }

	
	
	  /**
	   * This gets the section data, creates subsections, and fills in arrays giving the 
	   * name of section endpoints, angles between section endpoints, and distances between
	   * section endpoints (these latter arrays are for sections, not subsections)
	   * @param includeSectionsWithNaN_slipRates
	   */
	  private void createSubSections(boolean includeSectionsWithNaN_slipRates) {

		  /** Set the deformation model
		   * D2.1 = 82
		   * D2.2 = 83
		   * D2.3 = 84
		   * D2.4 = 85
		   * D2.5 = 86
		   * D2.6 = 87
		   */
		  int deformationModelId = 82;

		  // fetch the sections
		  DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		  allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelId);

		  //Alphabetize:
		  Collections.sort(allFaultSectionPrefData, new NamedObjectComparator());
		  
/*		  
		  // write sections IDs and names
		  for(int i=0; i< this.allFaultSectionPrefData.size();i++)
				System.out.println(allFaultSectionPrefData.get(i).getSectionId()+"\t"+allFaultSectionPrefData.get(i).getName());
*/

		  // remove those with no slip rate if appropriate
		  if(!includeSectionsWithNaN_slipRates) {
			  if (D)System.out.println("Removing the following due to NaN slip rate:");
			  for(int i=allFaultSectionPrefData.size()-1; i>=0;i--)
				  if(Double.isNaN(allFaultSectionPrefData.get(i).getAveLongTermSlipRate())) {
					  if(D) System.out.println("\t"+allFaultSectionPrefData.get(i).getSectionName());
					  allFaultSectionPrefData.remove(i);
				  }	 
		  }
		  
/*	
		  // find and print max Down-dip width
		  double maxDDW=0;
		  int index=-1;
		  for(int i=0; i<allFaultSectionPrefData.size(); i++) {
			  double ddw = allFaultSectionPrefData.get(i).getDownDipWidth();
			  if(ddw>maxDDW) {
				  maxDDW = ddw;
				  index=i;
			  }
		  }
		  System.out.println("Max Down-Dip Width = "+maxDDW+" for "+allFaultSectionPrefData.get(index).getSectionName());
*/	
/*		  
		  // this is a trimmed list for testing
		  ArrayList<FaultSectionPrefData> trimmedFaultSectionPrefData = new ArrayList<FaultSectionPrefData>();
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(77));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(75));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(164));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(42));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(108));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(0));
		  allFaultSectionPrefData = trimmedFaultSectionPrefData;
		  // write out these
		  for(int i=0; i< allFaultSectionPrefData.size();i++) 
			  System.out.println(i+"\t"+allFaultSectionPrefData.get(i).getName());
*/	  

		  // make subsection data
		  subSectionPrefDataListList = new ArrayList<ArrayList<FaultSectionPrefData>>();
		  subSectionPrefDataList = new ArrayList<FaultSectionPrefData>();
		  numSubSections=0;
		  numSections = allFaultSectionPrefData.size();
		  int maxNumSubSections=0;
		  for(int i=0; i<numSections; ++i) {
			  FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)allFaultSectionPrefData.get(i);
			  double maxSectLength = faultSectionPrefData.getDownDipWidth()*maxSubSectionLength;
			  ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength);
//			  ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSubSectionLength);
			  if(subSectData.size()>maxNumSubSections) maxNumSubSections = numSubSections;
			  numSubSections += subSectData.size();
			  subSectionPrefDataListList.add(subSectData);
			  subSectionPrefDataList.addAll(subSectData);
		  }
		  
		  
		  // Set the mappings among sections and subsection lists
		  sectForSubSectionMapping = new int[subSectionPrefDataList.size()];
		  subSectForSubSectMapping = new int[subSectionPrefDataList.size()];
		  subSectForSectAndSubsectMapping = new int[numSections][maxNumSubSections];
		  firstSubsectOfSectMapping = new int [numSections];
		  for(int i=0;i<numSections;i++)
			  for(int j=0;j<maxNumSubSections;j++)
				  subSectForSectAndSubsectMapping[i][j]=-1; // initialize with bogus indices
		  // mow make the mappings
		  int counter =0;
		  for(int i=0; i<subSectionPrefDataListList.size(); ++i) {
			  firstSubsectOfSectMapping[i] = counter;
			  for(int j=0; j<subSectionPrefDataListList.get(i).size();j++) {
				  sectForSubSectionMapping[counter] = i;
				  subSectForSubSectMapping[counter] = j;
				  subSectForSectAndSubsectMapping[i][j]=counter;
				  counter += 1;
			  }
		  }

	  }
	  
	  private void calcSubSectionDistances() {
		  
		  subSectionDistances = new double[numSubSections][numSubSections];
		  
		  // Calculate the distance matrix
		  int progress = 0, progressInterval=10;  // for progress report
		  System.out.print("Dist Calc % Done:");
		  for(int a=0;a<numSubSections;a++) {
			  if (100*a/numSubSections > progress) {
				  System.out.print("\t"+progress);
				  progress += progressInterval;
			  }
			  StirlingGriddedSurface surf1 = new StirlingGriddedSurface(subSectionPrefDataList.get(a).getSimpleFaultData(false), 2.0);

			  for(int b=a+1;b<numSubSections;b++) {
				  StirlingGriddedSurface surf2 = new StirlingGriddedSurface(subSectionPrefDataList.get(b).getSimpleFaultData(false), 2.0);
				  double minDist = surf1.getMinDistance(surf2);
				  subSectionDistances[a][b] = minDist;
				  subSectionDistances[b][a] = minDist;
			  }
		  }
		  System.out.print("\n");

	  }
	  
	  private void calcSubSectionDistancesFast() {

		  double[][] subSectionDistances2 = new double[numSubSections][numSubSections];

		  // Calculate the distance matrix
		  int progress = 0, progressInterval=10;  // for progress report
		  for(int a=0;a<numSubSections;a++) {
			  if (100*a/numSubSections > progress) {
				  System.out.println(progress+"% done");
				  progress += progressInterval;
			  }
			  StirlingGriddedSurface surf1 = new StirlingGriddedSurface(subSectionPrefDataList.get(a).getSimpleFaultData(false), 2.0);

			  for(int b=a+1;b<numSubSections;b++) {
				  StirlingGriddedSurface surf2 = new StirlingGriddedSurface(subSectionPrefDataList.get(b).getSimpleFaultData(false), 2.0);
				  Iterator<Location> it = surf1.listIterator();
				  double min3dDist = Double.POSITIVE_INFINITY;
				  double dist;
				  // find distance between all location pairs in the two surfaces
				  search:
					  while(it.hasNext()) { // iterate over all locations in this surface
						  Location loc1 = (Location)it.next();
						  Iterator<Location> it2 = surf2.listIterator();
						  while(it2.hasNext()) { // iterate over all locations on the user provided surface
							  Location loc2 = (Location)it2.next();
							  dist = LocationUtils.linearDistanceFast(loc1, loc2);
							  if(dist<min3dDist){
								  min3dDist = dist;
								  if (dist > 50) break search;  // skip if it's clearly too far
							  }
						  }
					  }
				  subSectionDistances2[a][b] = min3dDist;
				  subSectionDistances2[b][a] = min3dDist;
			  }

		  }
		  for(int i=0;i<numSubSections; i++)
			  for(int j=i+1;j<numSubSections; j++) {
				  double diff = Math.abs(subSectionDistances2[i][j]-subSectionDistances[i][j]);
				  if(subSectionDistances[i][j] < maxJumpDist && diff > 0.1)
					  System.out.println("Distance discrepancy: "+i+"\t"+j+"\t"+subSectionDistances[i][j]+"\t"+subSectionDistances2[i][j]+"\t");
			  }
	  }

	  
	  
	  /**
	   * For each sub section, create a list of subsections that are within maxJumpDist.  
	   * This generates an ArrayList of ArrayLists (named sectionConnectionsList).  
	   * Reciprocal duplicates are not filtered out.
	   * @param maxJumpDist
	   * @param maxAngle
	   */
	  private void computeCloseSubSectionsListList() {
		  
		  subSectionConnectionsListList = new ArrayList<ArrayList<Integer>>();

		  // first add the adjacent subsections in that section
		  for(int i=0; i<subSectionPrefDataListList.size(); ++i) {
			  int numSubSect = subSectionPrefDataListList.get(i).size();
			  for(int j=0;j<numSubSect;j++) {
				  ArrayList<Integer> sectionConnections = new ArrayList<Integer>();
				  if(j != 0) // the first one has no previous subsection
					  sectionConnections.add(subSectForSectAndSubsectMapping[i][j-1]);
				  if(j != numSubSect-1) // the last one has no subsequent subsection
					  sectionConnections.add(subSectForSectAndSubsectMapping[i][j+1]);
				  subSectionConnectionsListList.add(sectionConnections);
			  }
		  }
		  
		  // now add subsections on other sections, keeping only one connection between each section (the closest)
		  for(int i=0; i<subSectionPrefDataListList.size(); ++i) {
			  ArrayList<FaultSectionPrefData> sect1_List = subSectionPrefDataListList.get(i);
			  for(int j=i+1; j<subSectionPrefDataListList.size(); ++j) {
				  ArrayList<FaultSectionPrefData> sect2_List = subSectionPrefDataListList.get(j);
				  double minDist=Double.MAX_VALUE;
				  int subSectIndex1 = -1;
				  int subSectIndex2 = -1;
				  // find the closest pair
				  for(int k=0;k<sect1_List.size();k++) {
					  for(int l=0;l<sect2_List.size();l++) {
						  int index1 = subSectForSectAndSubsectMapping[i][k];
						  int index2 = subSectForSectAndSubsectMapping[j][l];
						  double dist = subSectionDistances[index1][index2];
						  if(dist < minDist) {
							  minDist = dist;
							  subSectIndex1 = index1;
							  subSectIndex2 = index2;
						  }					  
					  }
				  }
				  // add to lists for each subsection
				  if (minDist<maxJumpDist) {
					  subSectionConnectionsListList.get(subSectIndex1).add(subSectIndex2);
					  subSectionConnectionsListList.get(subSectIndex2).add(subSectIndex1);  // reciprocal of the above
				  }
			  }
		  }
	  }
	  
	  
	  private void makeClusterList() {
		  
		  // make an arrayList of subsection integers
		  ArrayList<Integer> availableSubSections = new ArrayList<Integer>();
		  for(int i=0; i<numSubSections; i++) availableSubSections.add(i);
		  
		  sectionClusterList = new ArrayList<SectionCluster>();
		  while(availableSubSections.size()>0) {
			  if (D) System.out.println("WORKING ON CLUSTER #"+(sectionClusterList.size()+1));
			  int firstSubSection = availableSubSections.get(0);
			  SectionCluster newCluster = new SectionCluster(subSectionPrefDataList, minNumSubSectInRup,subSectionConnectionsListList);
			  newCluster.add(firstSubSection);
			  if (D) System.out.println("\tfirst is "+this.subSectionPrefDataList.get(firstSubSection).getName());
			  addLinks(firstSubSection, newCluster);
			  // remove the used subsections from the available list
			  for(int i=0; i<newCluster.size();i++) availableSubSections.remove(newCluster.get(i));
			  // add this cluster to the list
			  sectionClusterList.add(newCluster);
System.out.println(newCluster.size()+"\tsubsections in cluster #"+sectionClusterList.size()+"\t"+
					  availableSubSections.size()+"\t subsections left to allocate");
		  }
	  }
	  
	  
	  private void addLinks(int subSectIndex, SectionCluster list) {
		  ArrayList<Integer> branches = subSectionConnectionsListList.get(subSectIndex);
		  for(int i=0; i<branches.size(); i++) {
			  Integer subSect = branches.get(i);
			  if(!list.contains(subSect)) {
				  list.add(subSect);
				  addLinks(subSect, list);
			  }
		  }
	  }
	  

	/**
	 * This writes out the close subsections to each subsection (and the distance)
	 */
	public void writeCloseSubSections() {
		System.out.print("writing file...");
		try{
			FileWriter fw = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/ned/rupsInFaultSystem/closeSubSections.txt");
			String outputString = new String();
			
			for(int sIndex1=0; sIndex1<subSectionConnectionsListList.size();sIndex1++) {
				ArrayList<Integer> sectList = subSectionConnectionsListList.get(sIndex1);
				outputString += "\n"+subSectionPrefDataList.get(sIndex1).getName() + "  connections:\n\n";
				for(int i=0;i<sectList.size();i++) {
					int sIndex2 = sectList.get(i);
					outputString += "\t"+subSectionPrefDataList.get(sIndex2).getName()+
							"\t"+(float)this.subSectionDistances[sIndex1][sIndex2]+"\n";
				}
			}
		
			fw.write(outputString);
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println(" - done");

	}
	
	
    /**
     * This returns the change in strike direction in going from this azimuth1 to azimuth2,
     * where these azimuths are assumed to be defined between -180 and 180 degrees.
     * The output is between -180 and 180 degrees.
     * @return
     */
    private double getStrikeDirectionDifference(double azimuth1, double azimuth2) {
    	double diff = azimuth2 - azimuth1;
    	if(diff>180)
    		return diff-360;
    	else if (diff<-180)
    		return diff+360;
    	else
    		return diff;
     }
    

    /**
     * This reverses the given azimuth (assumed to be between -180 and 180 degrees).
     * The output is between -180 and 180 degrees.
     * @return
     */
    private double reverseAzimuth(double azimuth) {
    	if(azimuth<0) return azimuth+180;
    	else return azimuth-180;
     }
    
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime=System.currentTimeMillis();
		CreateRupturesFromSections createRups = new CreateRupturesFromSections(10, 45, 60, 0.5, 2);
		int runtime = (int)(System.currentTimeMillis()-startTime)/1000;
		System.out.println("Run took "+runtime+" seconds");
	}
	
}
