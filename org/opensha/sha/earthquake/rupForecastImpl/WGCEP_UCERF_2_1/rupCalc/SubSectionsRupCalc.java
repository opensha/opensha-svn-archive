
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.rupCalc;

import java.text.DecimalFormat;
import java.util.ArrayList;


import org.opensha.calc.RelativeLocation;
import org.opensha.data.Location;
import org.opensha.exceptions.InvalidRangeException;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.fault.FaultTrace;

/**
 * This class finds all the ruptures that includes 1 or more subsections. The subsections should
 * be within a cut-off distance from each other at the end points.
 * 
 * @author vipingupta
 *
 */
public class SubSectionsRupCalc {
	private final static double SUBSECTIONS_CUTOFF_DIST = 10; 
	private final static double MAX_SUBSECTION_LEN = 10;
	private final static Location LOCATION = new Location(31.5, -115.0);
	private final static DecimalFormat decimalFormat = new DecimalFormat("0.00###");
	private ArrayList rupList;
	private final PrefFaultSectionDataDB_DAO faultSectionPrefDataDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList subSectionList;
	private ArrayList doneList;

	  public SubSectionsRupCalc() {
	  }

	  public void doProcessing()  {
	    try {
	
	      rupList = new ArrayList();
	      subSectionList = getAllSubSections(); // get all the fault sections
	      createTreesForFaultSections(); // discretize the section in 5km
	      System.out.println("Total ruptures="+rupList.size());
	    }catch(Exception e) {
	      e.printStackTrace();
	    }
	  }

	  
	  /**
	   * Get all subsections 
	   * 
	   * @return
	   */
	  private ArrayList getAllSubSections() {
			ArrayList faultSectionPrefList = faultSectionPrefDataDAO.getAllFaultSectionPrefData();
			ArrayList<FaultSectionPrefData> subSecList = new ArrayList<FaultSectionPrefData>();
			for(int i=0; i<faultSectionPrefList.size(); ++i) {
				FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)faultSectionPrefList.get(i);
				subSecList.addAll(faultSectionPrefData.getSubSectionsList(MAX_SUBSECTION_LEN));
			}
			return subSecList;
	  }

	  /**
	   * Divide each section to subsections after sub sampling.
	   * Also create a tree for each section
	   *
	   * @param faultTraceMapping
	   * @throws InvalidRangeException
	   */
	  private void createTreesForFaultSections() throws
	      InvalidRangeException {
		  
	    ArrayList treeList = new ArrayList();
	    // create trees 
	     doneList = new ArrayList();
	    for(int i=0; i<subSectionList.size(); ++i) {
	    	FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)subSectionList.get(i);
	    	if(doneList.contains(faultSectionPrefData.getSectionName())) continue;
	    	Tree tree = new Tree();
	    	getAdjacentFaultSectionNodes(tree, i);
	    	treeList.add(tree);
	    	System.out.println("***********TREE "+treeList.size()+" ***********\n");
	    	tree.writeInfo();
	    	ArrayList treeRupList = tree.getRuptures();
	    	rupList.addAll(treeRupList);
	    	//System.exit(0);
	    }
	    System.out.println("Total Subsections ="+subSectionList.size());
	    System.out.println("Total Trees ="+treeList.size());
	    
	  }



	  /**
	   * Get all the faults within interFaultCutOffDistance kms of the location loc
	   * This allows to find adjacent fault for fault to fault jumps
	   * @param loc
	   * @param interFaultCutOffDistance
	   * @param adjacentFaultNames
	   */
	  private void getAdjacentFaultSectionNodes(Tree tree, int subSectionIndex) {
		  FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)subSectionList.get(subSectionIndex);
		  doneList.add(faultSectionPrefData.getSectionName());
		  for(int i=0; i<subSectionList.size(); ++i) {
			  if(i==subSectionIndex) continue;
			  FaultSectionPrefData faultSectionPrefData1 = (FaultSectionPrefData)subSectionList.get(i);
			  if(!isWithinCutOffDist(faultSectionPrefData, faultSectionPrefData1)) continue;
			  tree.connectInTree(faultSectionPrefData.getSectionName(), faultSectionPrefData1.getSectionName(), subSectionIndex, i);
			  if(doneList.contains(faultSectionPrefData1.getSectionName())) continue;
			  //System.out.println("Connected "+faultSectionPrefData.getSectionName()+ " AND "+ faultSectionPrefData1.getSectionName());
			  if(i>subSectionIndex) getAdjacentFaultSectionNodes(tree, i);
		  }
	  }
	  
	  /**
	   * It finds whether 2 subsections are within cutoff distance of each other
	   * @param faultSectionPrefData1
	   * @param faultSectionPrefData2
	   * @return
	   */
	  private boolean isWithinCutOffDist(FaultSectionPrefData faultSectionPrefData1, 
			  FaultSectionPrefData faultSectionPrefData2) {
		  FaultTrace trace1= faultSectionPrefData1.getFaultTrace();
		  int endIndex1 = trace1.getNumLocations()-1;
		  FaultTrace trace2 = faultSectionPrefData2.getFaultTrace();
		  int endIndex2 = trace2.getNumLocations()-1;
		  if(RelativeLocation.getApproxHorzDistance(trace1.getLocationAt(0), trace2.getLocationAt(0))<=SUBSECTIONS_CUTOFF_DIST) 
			  return true;
		  if(RelativeLocation.getApproxHorzDistance(trace1.getLocationAt(0), trace2.getLocationAt(endIndex2))<=SUBSECTIONS_CUTOFF_DIST) 
			  return true;
		  if(RelativeLocation.getApproxHorzDistance(trace1.getLocationAt(endIndex1), trace2.getLocationAt(0))<=SUBSECTIONS_CUTOFF_DIST) 
			  return true;
		  if(RelativeLocation.getApproxHorzDistance(trace1.getLocationAt(endIndex1), trace2.getLocationAt(endIndex2))<=SUBSECTIONS_CUTOFF_DIST) 
			  return true;
		  return false;
	  }

	  public static void main(String args[]) {
		  SubSectionsRupCalc rupCalc = new SubSectionsRupCalc();
		  rupCalc.doProcessing();
	  }
}