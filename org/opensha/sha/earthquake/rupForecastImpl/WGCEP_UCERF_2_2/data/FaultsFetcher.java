package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;


import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.FaultSegmentData;
import org.opensha.util.FileUtils;

/**
*
* This class read the  segments from a text file and then go to database to fetch the fault sections
* 
* @author vipingupta
*/
public abstract class FaultsFetcher {
	protected HashMap faultModels;
	// DAO to access the fault section database
	private FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	protected DeformationModelPrefDataDB_DAO deformationModelPrefDB_DAO = new DeformationModelPrefDataDB_DAO(DB_AccessAPI.dbConnection);
	private final static String FAULT_MODEL_NAME_PREFIX = "-";
	protected ArrayList<String> faultModelNames = new ArrayList<String>();
	protected HashMap segmentNamesMap = new HashMap();
	protected int deformationModelId=-1;
	private ArrayList faultDataListInSelectedSegment=null;
	// Stepover fix for Elsinore in case of Unsegmented model
	private final static int GLEN_IVY_STEPOVER_FAULT_SECTION_ID = 297;
	private final static int TEMECULA_STEPOVER_FAULT_SECTION_ID = 298;
	private final static int ELSINORE_COMBINED_STEPOVER_FAULT_SECTION_ID = 402;
	//	 Stepover fix for San Jacinto in case of Unsegmented model
	private final static int SJ_VALLEY_STEPOVER_FAULT_SECTION_ID = 290;
	private final static int SJ_ANZA_STEPOVER_FAULT_SECTION_ID = 291;
	private final static int SJ_COMBINED_STEPOVER_FAULT_SECTION_ID = 401;
	
	protected boolean isUnsegmented = false;
	
//	private ArrayList faultSectionList=null;
	
	
	public FaultsFetcher() {
		
	}
	
	public FaultsFetcher(String fileName) {
		// make faultModels hashMap:
		this.loadSegmentModels(fileName);
		
	}
	
	/**
	 * Create the faultModels hashMap by reading a file that defines what 
	 * sections are in each segment and what segments are in each fault model
	 *
	 */
	public void loadSegmentModels(String fileName) {
		faultModelNames = new ArrayList<String>();
		faultModels = new HashMap();
		// read file 
		try {
			// read the text file that defines the sctions in each segment for each fault model
			ArrayList fileLines = FileUtils.loadFile(fileName);
			ArrayList segmentsList=null;  // segments in a given fault model
			String faultModelName=null;
			for(int i=0; i<fileLines.size(); ++i) {
				// read the file line by line
				String line = ((String)fileLines.get(i)).trim();
				// skip the comment and blank lines
				if(line.equalsIgnoreCase("") || line.startsWith("#")) continue;
				// check if this is a fault model name
				if(line.startsWith(FAULT_MODEL_NAME_PREFIX)) {
					if(faultModelName!=null ){
						// put segment model and corresponding ArrayList of segments in a HashMap
						faultModels.put(faultModelName, segmentsList);
					}
					faultModelName = getSegmentModelName(line);
					faultModelNames.add(faultModelName);
					segmentsList = new ArrayList();
				} else  {
					// read the section ids
					segmentsList.add(getSegment(line));
				}
				
			}
			faultModels.put(faultModelName, segmentsList);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This returns a list of FaultSegmentData object for all faults defined in text file
	 * @param deformationModelId
	 * @param isAseisReducesArea
	 * @return
	 */
	public ArrayList getFaultSegmentDataList(boolean isAseisReducesArea) {
		ArrayList<FaultSegmentData> faultSegDataList = new ArrayList<FaultSegmentData>();
		for(int i=0; i< faultModelNames.size(); ++i)
			faultSegDataList.add(getFaultSegmentData((String)faultModelNames.get(i), isAseisReducesArea));
		return faultSegDataList;
	}
	
	
	
	/**
	 * Return a list of ids of all fault sections in any of faults defined in text file.
	 * @return
	 */
	public ArrayList<Integer> getAllFaultSectionsIdList() {
		ArrayList<Integer> faultSectionIdList = new ArrayList<Integer>();
		for(int i=0; i< this.faultModelNames.size(); ++i)
			faultSectionIdList.addAll(getFaultSectionsIdList((String)faultModelNames.get(i)));
		return faultSectionIdList;
	}
	
	/**
	 * Get a list of fault section Ids within the selected segment model
	 * @return
	 */
	private ArrayList<Integer> getFaultSectionsIdList(String faultModel) {
		ArrayList segmentsList = (ArrayList)this.faultModels.get(faultModel);
		ArrayList<Integer>  faultSectionIdList= new ArrayList<Integer>();
		// iterate over all segment
		for(int i=0; i<segmentsList.size(); ++i) {
			ArrayList segment = (ArrayList)segmentsList.get(i);
			// iterate over all sections in a segment
			for(int j=0; j<segment.size(); ++j) {
				int faultSectionId = ((FaultSectionSummary)segment.get(j)).getSectionId();
				faultSectionIdList.add(new Integer(faultSectionId));
			}
		}
		return faultSectionIdList;
	}
	
	
	/**
	 * Get segmented fault data for selected fault
	 * 
	 * @return
	 */
	public FaultSegmentData getFaultSegmentData(String faultName, boolean isAseisReducesArea) {
		// get the segment array list of section array lists
		ArrayList segmentsList = (ArrayList)faultModels.get(faultName);
		faultDataListInSelectedSegment = new ArrayList();
//		faultSectionList = new ArrayList();
		// iterate over all segment
		for(int i=0; i<segmentsList.size(); ++i) {
			ArrayList sectionList = (ArrayList)segmentsList.get(i); // secionList is an array list of faultSectionSummary objects
			ArrayList newSegment = new ArrayList();
			// iterate over all sections in a segment
			for(int j=0; j<sectionList.size(); ++j) {
				//System.out.println(faultModel+","+j);
				int faultSectionId = ((FaultSectionSummary)sectionList.get(j)).getSectionId();
				FaultSectionPrefData faultSectionPrefData = this.deformationModelPrefDB_DAO.getFaultSectionPrefData(deformationModelId, faultSectionId);
				if(Double.isNaN(faultSectionPrefData.getAveLongTermSlipRate())) {
					//System.out.println(faultSectionPrefData.getSectionName());
					continue;
				}
				
//System.out.println(faultSectionPrefData.getSectionName()+"  "+faultSectionPrefData.getSlipRateStdDev());
				
				if(this.isUnsegmented && faultName.equalsIgnoreCase("Elsinore") &&  // SKIP for Temecula Stepover
						faultSectionPrefData.getSectionId()==TEMECULA_STEPOVER_FAULT_SECTION_ID) continue; 
				
				// If we encounter Glen Ivy Stepover, then we need to replace it with combined stepover section
				if(this.isUnsegmented && faultName.equalsIgnoreCase("Elsinore") &&  
						faultSectionPrefData.getSectionId()==GLEN_IVY_STEPOVER_FAULT_SECTION_ID)  {
					FaultSectionPrefData glenIvyStepoverfaultSectionPrefData = this.deformationModelPrefDB_DAO.getFaultSectionPrefData(deformationModelId, GLEN_IVY_STEPOVER_FAULT_SECTION_ID);
					FaultSectionPrefData temeculaStepoverfaultSectionPrefData = this.deformationModelPrefDB_DAO.getFaultSectionPrefData(deformationModelId, TEMECULA_STEPOVER_FAULT_SECTION_ID);
					faultSectionPrefData = faultSectionDAO.getFaultSection(ELSINORE_COMBINED_STEPOVER_FAULT_SECTION_ID).getFaultSectionPrefData();
					faultSectionPrefData.setAveLongTermSlipRate(glenIvyStepoverfaultSectionPrefData.getAveLongTermSlipRate()+temeculaStepoverfaultSectionPrefData.getAveLongTermSlipRate());
					faultSectionPrefData.setSlipRateStdDev(glenIvyStepoverfaultSectionPrefData.getSlipRateStdDev()+temeculaStepoverfaultSectionPrefData.getSlipRateStdDev());
				}
				
				if(this.isUnsegmented && faultName.equalsIgnoreCase("San Jacinto (SB to C)") &&  // SKIP for Anza Stepover
						faultSectionPrefData.getSectionId()==this.SJ_ANZA_STEPOVER_FAULT_SECTION_ID) continue; 
				
				// If we encounter SJ Valley Stepover, then we need to replace it with combined stepover section
				if(this.isUnsegmented && faultName.equalsIgnoreCase("San Jacinto (SB to C)") &&  
						faultSectionPrefData.getSectionId()==this.SJ_VALLEY_STEPOVER_FAULT_SECTION_ID)  {
					FaultSectionPrefData anzaStepoverfaultSectionPrefData = this.deformationModelPrefDB_DAO.getFaultSectionPrefData(deformationModelId, SJ_ANZA_STEPOVER_FAULT_SECTION_ID);
					FaultSectionPrefData valleyStepoverfaultSectionPrefData = this.deformationModelPrefDB_DAO.getFaultSectionPrefData(deformationModelId, SJ_VALLEY_STEPOVER_FAULT_SECTION_ID);
					faultSectionPrefData = faultSectionDAO.getFaultSection(this.SJ_COMBINED_STEPOVER_FAULT_SECTION_ID).getFaultSectionPrefData();
					faultSectionPrefData.setAveLongTermSlipRate(anzaStepoverfaultSectionPrefData.getAveLongTermSlipRate()+valleyStepoverfaultSectionPrefData.getAveLongTermSlipRate());
					faultSectionPrefData.setSlipRateStdDev(anzaStepoverfaultSectionPrefData.getSlipRateStdDev()+valleyStepoverfaultSectionPrefData.getSlipRateStdDev());
				}
				
				
				//System.out.println(faultSectionPrefData.getSectionName());
//				faultSectionList.add(faultSectionPrefData);
				newSegment.add(faultSectionPrefData);		
			}
			faultDataListInSelectedSegment.add(newSegment);
		}
		
		// make SegmentedFaultData 
		ArrayList<SegRateConstraint> segRates = getSegRateConstraints(faultName);
		FaultSegmentData segmetedFaultData = new FaultSegmentData(faultDataListInSelectedSegment, 
				(String[])this.segmentNamesMap.get(faultName), isAseisReducesArea, faultName, segRates);
		return segmetedFaultData;		
	}
	
	/**
	 * Get a list of all segment names
	 * @return
	 */
	public ArrayList<String> getAllFaultNames() {
		return this.faultModelNames;
	}
	
	/**
	 * Get recurrence intervals for selected segment model
	 * @param selectedSegmentModel
	 * @return
	 */
	public abstract ArrayList<SegRateConstraint> getSegRateConstraints(String selectedSegmentModel);
	
	
	/**
	 * Get the Segment model name
	 * 
	 * @param line
	 * @return
	 */
	private String getSegmentModelName(String line) {
		int index = line.indexOf("-");
		return line.substring(index+1).trim();
	}
	
	/*
	 * Get a list of fault sections for the current segment 
	 */ 
	private ArrayList getSegment(String line) {
		line = line.substring(0, line.indexOf(':'));
		ArrayList faultSectionsIdList = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(line,"\n,");
		while(tokenizer.hasMoreTokens()) {
			faultSectionsIdList.add(faultSectionDAO.getFaultSectionSummary(Integer.parseInt(tokenizer.nextToken().trim())));
		}
		return faultSectionsIdList;
	}
}
