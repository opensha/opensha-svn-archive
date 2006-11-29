package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data;

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
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.FaultSegmentData;
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
	private ArrayList faultModelNames;
	protected HashMap segmentNamesMap = new HashMap();
	private String selectedFaultModel=null;
	protected int deformationModelId=-1;
	private int prevDeformationModelId=-1;
	private boolean prevIsAseisReducesArea;
	private ArrayList faultDataListInSelectedSegment=null;
	private ArrayList faultSectionList=null;
	private ArrayList faultSegDataList = null;
	
	
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
		faultModelNames = new ArrayList();
		faultModels = new HashMap();
		deformationModelId=-1;
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
	public ArrayList getFaultSegmentDataList(int deformationModelId, boolean isAseisReducesArea) {
		// only make list if something has changed
		if(faultSegDataList==null || prevIsAseisReducesArea != isAseisReducesArea ||
				prevDeformationModelId!=deformationModelId)  {
		
			prevIsAseisReducesArea = isAseisReducesArea;
			prevDeformationModelId = deformationModelId;
			faultSegDataList = new ArrayList();
			for(int i=0; i< faultModelNames.size(); ++i)
				faultSegDataList.add(getFaultSegmentData((String)faultModelNames.get(i), deformationModelId, isAseisReducesArea));
		}
		
		return faultSegDataList;
	}
	
	
	
	/**
	 * Return a list of ids of all fault sections in any of faults defined in text file.
	 * @return
	 */
	public ArrayList getAllFaultSectionsIdList() {
		ArrayList faultSectionIdList = new ArrayList();
		for(int i=0; i< this.faultModelNames.size(); ++i)
			faultSectionIdList.addAll(getFaultSectionsIdList((String)faultModelNames.get(i)));
		return faultSectionIdList;
	}
	
	/**
	 * Get a list of fault section Ids within the selected segment model
	 * @return
	 */
	private ArrayList getFaultSectionsIdList(String faultModel) {
		ArrayList segmentsList = (ArrayList)this.faultModels.get(faultModel);
		ArrayList faultSectionIdList = new ArrayList();
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
	 * Get segmented fault data for selected segment
	 * 
	 * @param selectedSegmentName
	 * @return
	 */
	public FaultSegmentData getFaultSegmentData(String faultModel, int deformationModelId,
			boolean isAseisReducesArea) {
		
		// no need to re-fetch data from database if the data alraady exists in cache
		if(selectedFaultModel==null || !selectedFaultModel.equalsIgnoreCase(faultModel) ||
				this.deformationModelId!=deformationModelId)  {
			selectedFaultModel = faultModel;
			this.deformationModelId = deformationModelId;
			// get the segment array list of section array lists
			ArrayList segmentsList = (ArrayList)faultModels.get(faultModel);
			faultDataListInSelectedSegment = new ArrayList();
			faultSectionList = new ArrayList();
			// iterate over all segment
			for(int i=0; i<segmentsList.size(); ++i) {
				ArrayList sectionList = (ArrayList)segmentsList.get(i);
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
					//System.out.println(faultSectionPrefData.getSectionName());
					faultSectionList.add(faultSectionPrefData);
					newSegment.add(faultSectionPrefData);		
				}
				faultDataListInSelectedSegment.add(newSegment);
			}
		}
		
		// make SegmentedFaultData 
		double[]recurIntv = getRecurIntv(faultModel);
		FaultSegmentData segmetedFaultData = new FaultSegmentData(faultDataListInSelectedSegment, (String[])this.segmentNamesMap.get(faultModel), isAseisReducesArea, faultModel,
				recurIntv);
		return segmetedFaultData;
		
	}
	
	/**
	 * Get a list of all segment names
	 * @return
	 */
	public ArrayList getAllFaultNames() {
		return this.faultModelNames;
	}
	
	/**
	 * Get recurrence intervals for selected segment model
	 * @param selectedSegmentModel
	 * @return
	 */
	public abstract double[] getRecurIntv(String selectedSegmentModel);
	
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
		ArrayList faultSectionsIdList = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(line,"\n;");
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			StringTokenizer tokenizer2 = new StringTokenizer(token, ":");
			faultSectionsIdList.add(faultSectionDAO.getFaultSectionSummary(Integer.parseInt(tokenizer2.nextToken().trim())));
		}
		return faultSectionsIdList;
	}
}
