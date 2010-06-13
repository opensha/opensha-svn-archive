package scratch.ned.rupsInFaultSystem;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

import org.opensha.commons.data.NamedObjectComparator;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.refFaultParamDb.calc.sectionDists.FaultSectDistRecord;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

public class CreateRupturesFromSections {
	
	/*
	SubSectionsRupCalc

DONE	getNumClusters()
DONE	getCluster(i).getNumSubSections()
DONE	getCluster(i).getRuptures().size()				mid method is ArrayList
DONE	getCluster(clusterIndex).getAllSubSectionsIdList()	ArrayList<Integer>
DONE	getCluster(clusterIndex).getRuptures()			ArrayList
DONE	getRupList()								ArrayList<MultipleSectionRup>

	 */
	
	ArrayList<FaultSectionPrefData> allFaultSectionPrefData;
	double subSectionDistances[][], subSectionAngleDiffs[][];
	String endPointNames[];
	Location endPointLocs[];
	int numSections, counter, numSubSections, minNumSubSectInRup;
	ArrayList<ArrayList<Integer>> subSectionConnectionsListList, endToEndSectLinksList;
	double maxJumpDist, maxAngle, maxTotStrikeChange, maxSubSectionLength;
	ArrayList<ArrayList<FaultSectionPrefData>> subSectionPrefDataListList;
	ArrayList<FaultSectionPrefData> subSectionPrefDataList; // same as above, but a sequential list (not list of lists)
	
	// this is to store the section and subsection indices for the ith subsection.
	int[]  sectForSubSectionMapping,subSectForSubSectMapping, firstSubsectOfSectMapping;
	// this gives the section index for the ith sections and jth subsection withing
	int[][] subSectForSectAndSubsectMapping;
	
	ArrayList<SectionCluster> clusterList;


	
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
		this.maxAngle=maxAngle;
		this.maxTotStrikeChange=maxStrikeChange;
		this.maxSubSectionLength=maxSubSectionLength;
		this.minNumSubSectInRup=minNumSubSectInRup;
		
		Boolean includeSectionsWithNaN_slipRates = true;
		getAllSections(includeSectionsWithNaN_slipRates);
		
		
		calcSubSectionDistances();
		
		computeCloseSubSectionsListList();
		
		makeClusterList();
		
		for(int i=1; i<clusterList.size(); i++) {
			SectionCluster cluster = clusterList.get(i);
			System.out.println("CONTENTS OF CLUSTER #"+i);
			ArrayList<Integer> allSubSectionsIdList = cluster.getAllSubSectionsIdList();
			for(int j=0; j< cluster.size();j++)
				System.out.println("\t"+subSectionPrefDataList.get(cluster.get(j)).getName()+"\t"+allSubSectionsIdList.get(j));
		}
		/*		*/
//		System.out.println("numSubSections="+numSubSections+"\tgetRupList().size()="+getRupList().size());
		
//		for(int i=0; i<sectionClusterList.size();i++)
//		System.out.println("sectionClusterList.get(0).getRuptures().size()="+sectionClusterList.get(0).getRuptures().size());
///			System.out.println("Cluster "+i+" has "+sectionClusterList.get(i).getNumSubSections()+" subsections");
//		testClusters();
	}
	
	
	
	public int getNumClusters() {
		return clusterList.size();
	}
	
	public SectionCluster getCluster(int clusterIndex) {
		return clusterList.get(clusterIndex);
	}
	
	
	
	  public ArrayList<ArrayList<Integer>> getRupList() {
		  ArrayList<ArrayList<Integer>> rupList = new ArrayList<ArrayList<Integer>>();
		  for(int i=0; i<clusterList.size();i++)
			  rupList.addAll(clusterList.get(i).getRuptures());
		  return rupList;
	  }

	
	
	  /**
	   * This gets the section data, creates subsections, and fills in arrays giving the 
	   * name of section endpoints, angles between section endpoints, and distances between
	   * section endpoints (these latter arrays are for sections, not subsections)
	   * @param includeSectionsWithNaN_slipRates
	   */
	  private void getAllSections(boolean includeSectionsWithNaN_slipRates) {
		  /** Set the deformation model
		   * D2.1 = 82
		   * D2.2 = 83
		   * D2.3 = 84
		   * D2.4 = 85
		   * D2.5 = 86
		   * D2.6 = 87
		   */
		  int deformationModelId = 82;

		  DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		  allFaultSectionPrefData = deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelId);
		  
		  /* The following is painfully slow!
		  System.out.println("beginning database read");
		  DeformationModelPrefDataDB_DAO deformationModelDB_DAO = new DeformationModelPrefDataDB_DAO(DB_ConnectionPool.getDB2ReadOnlyConn());
		  ArrayList<Integer> idList = deformationModelDB_DAO.getFaultSectionIdsForDeformationModel(deformationModelId);
		  allFaultSectionPrefData = new ArrayList<FaultSectionPrefData>();
		  for(int i=0;i<idList.size();i++)
			  allFaultSectionPrefData.add(deformationModelDB_DAO.getFaultSectionPrefData(deformationModelId,idList.get(i)));
		  System.out.println("done with database read");
		  */
		  
		  

		  //Alphabetize:
		  Collections.sort(allFaultSectionPrefData, new NamedObjectComparator());
/*		  
			for(int i=0; i< this.allFaultSectionPrefData.size();i++){
				System.out.println(allFaultSectionPrefData.get(i).getSectionId()+"\t"+allFaultSectionPrefData.get(i).getName());
			}
*/


		  // remove those with no slip rate
		  if(!includeSectionsWithNaN_slipRates) {
			  System.out.println("Removing the following due to NaN slip rate:");
			  for(int i=allFaultSectionPrefData.size()-1; i>=0;i--)
				  if(Double.isNaN(allFaultSectionPrefData.get(i).getAveLongTermSlipRate())) {
					  System.out.println("\t"+allFaultSectionPrefData.get(i).getSectionName());
					  allFaultSectionPrefData.remove(i);
				  }	 
		  }

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
/*		  
		  ArrayList<FaultSectionPrefData> trimmedFaultSectionPrefData = new ArrayList<FaultSectionPrefData>();
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(77));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(75));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(164));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(42));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(108));
		  trimmedFaultSectionPrefData.add(allFaultSectionPrefData.get(0));
		  allFaultSectionPrefData = trimmedFaultSectionPrefData;
		
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
//			  double maxSectLength = faultSectionPrefData.getDownDipWidth()*maxSubSectionLength;
//			  ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSectLength);
//System.out.println(i+"\t"+faultSectionPrefData.getName());
			  ArrayList<FaultSectionPrefData> subSectData = faultSectionPrefData.getSubSectionsList(maxSubSectionLength);
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
		  
/*		  
		  counter =0;
		  for(int i=0; i<subSectionPrefDataListList.size(); ++i) {
			  ArrayList<FaultSectionPrefData> dataList = subSectionPrefDataListList.get(i);
			  for(int j=0; j<dataList.size();j++) {
				  System.out.println(indexMapping[i][j]+","+counter+"\t"+i+","+sectionMapping[counter]+"\t"+j+","+subSectionMapping[counter]);
				  counter += 1;
			  }
		  }
*/  


		  // write the number of sections and subsections
		  System.out.println("numSections="+numSections+";  numSubSections="+numSubSections);
		  // write index/names
		  //		 for(int i=0;i<allFaultSectionPrefData.size();i++) System.out.println(i+"\t"+allFaultSectionPrefData.get(i).getSectionName());
		  // write out strike directions
		  //		 for(int s=0;s<num_sections;s++) System.out.println(allFaultSectionPrefData.get(s).getFaultTrace().getStrikeDirection());


	  }
	  
	  private void calcSubSectionDistances() {
		  
		  subSectionDistances = new double[numSubSections][numSubSections];
		  subSectionAngleDiffs = new double[numSubSections][numSubSections];
		  
		  // Calculate the distance matrix
		  counter = 5;  // for progress report
		  for(int a=0;a<numSubSections;a++) {
			  if (100*a/numSubSections > counter) {
				  System.out.println(counter+"% done");
				  counter += 5;
			  }
			  StirlingGriddedSurface surf1 = new StirlingGriddedSurface(subSectionPrefDataList.get(a).getSimpleFaultData(false), 2.0);

			  for(int b=a+1;b<numSubSections;b++) {
				  StirlingGriddedSurface surf2 = new StirlingGriddedSurface(subSectionPrefDataList.get(b).getSimpleFaultData(false), 2.0);
				  double minDist = surf1.getMinDistance(surf2);
				  subSectionDistances[a][b] = minDist;
				  subSectionDistances[b][a] = minDist;
			  }
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

		  // create the lists and add the neighboring subsections in each section
		  for(int i=0; i<subSectionPrefDataListList.size(); ++i) {
			  int numSubSect = subSectionPrefDataListList.get(i).size();
			  for(int j=0;j<numSubSect;j++) {
				  ArrayList<Integer> sectionConnections = new ArrayList<Integer>();
				  if(j != 0) 
					  sectionConnections.add(subSectForSectAndSubsectMapping[i][j-1]);
				  if(j != numSubSect-1)
					  sectionConnections.add(subSectForSectAndSubsectMapping[i][j+1]);
				  subSectionConnectionsListList.add(sectionConnections);
			  }
		  }
		  
		  // now add subsections on other sections, keeping only one connection between each section
		  for(int i=0; i<subSectionPrefDataListList.size(); ++i) {
			  ArrayList<FaultSectionPrefData> sect1_List = subSectionPrefDataListList.get(i);
			  for(int j=i+1; j<subSectionPrefDataListList.size(); ++j) {
				  ArrayList<FaultSectionPrefData> sect2_List = subSectionPrefDataListList.get(j);
				  double minDist=Double.MAX_VALUE;
				  int subSectIndex1 = -1;
				  int subSectIndex2 = -1;
				  for(int k=0;k<sect1_List.size();k++) {
					  for(int l=0;l<sect2_List.size();l++) {
						  int index1 = subSectForSectAndSubsectMapping[i][k];
						  int index2 = subSectForSectAndSubsectMapping[j][l];
						  double dist = subSectionDistances[index1][index2];
/*
						  if(i==0 && j==88 ) {
							  System.out.println(index1+"\t"+subSectionPrefDataList.get(index1).getName()+
									  "\t"+ index2+"\t"+subSectionPrefDataList.get(index2).getName()+"\t"+ dist);
						  }
*/
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
		  
		  clusterList = new ArrayList<SectionCluster>();
		  while(availableSubSections.size()>0) {
			  
			  System.out.println("WORKING ON CLUSTER #"+(clusterList.size()+1));
			  int firstSubSection = availableSubSections.get(0);
			  SectionCluster newCluster = new SectionCluster(subSectionPrefDataList, minNumSubSectInRup);
			  newCluster.add(firstSubSection);
			  System.out.println("\tfirst is "+this.subSectionPrefDataList.get(firstSubSection).getName());
			  addLinks(firstSubSection, newCluster);
			  for(int i=0; i<newCluster.size();i++) availableSubSections.remove(newCluster.get(i));
			  clusterList.add(newCluster);
			  System.out.println(newCluster.size()+"\tsubsections in cluster #"+clusterList.size()+"\t"+
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
//				  System.out.println("\tadded "+this.subSectionPrefDataList.get(subSect).getName());
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
		CreateRupturesFromSections createRups = new CreateRupturesFromSections(10, 45, 60, 7, 2);
		int runtime = (int)(System.currentTimeMillis()-startTime)/1000;
		System.out.println("Run took "+runtime+" seconds");
		createRups.writeCloseSubSections();
	}
	
}
