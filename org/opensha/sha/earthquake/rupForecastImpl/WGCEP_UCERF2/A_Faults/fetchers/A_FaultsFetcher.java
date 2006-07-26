/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.fetchers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.data.ValueWeight;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.SegmentedFaultData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.gui.RupSegRates;
import org.opensha.util.FileUtils;

/**
 *
 * This class read the  segments from a text file and then go to database to fetch the fault sections
 * 
 * @author vipingupta
 *
 */
public class A_FaultsFetcher {
	private final static String RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF2/A_Faults/Ray-all-AfaultsTable1.1.xls";
	private final static String SEGMENT_MODELS_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF2/A_Faults/SegmentModels.txt";
	private HashMap segmentModels = new HashMap();
	// DAO to access the fault section database
	private FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	private DeformationModelDB_DAO deformationModelDB_DAO = new DeformationModelDB_DAO(DB_AccessAPI.dbConnection);
	private PrefFaultSectionDataDB_DAO prefFaultSectionDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
	private final static String SEGMENT_MODEL_NAME_PREFIX = "-";
	private ArrayList segmentModelNames;
	private HashMap segmentIntvAndRupRates = new HashMap();
	public final static String MIN_RATE_RUP_MODEL = "Min Rate Solution";
	public final static String MAX_RATE_RUP_MODEL = "Max Rate Solution";
	public final static String EQUAL_RATE_RUP_MODEL = "Equal Rate Solution";
	public final static String GEOL_INSIGHT_RUP_MODEL = "Geol Insight Solution";
	private String selectedSegmentModel=null;
	private int deformationModelId=-1;
	private ArrayList faultDataListInSelectedSegment=null;
	private ArrayList faultSectionList=null;
	
	
	public A_FaultsFetcher() {
		this.loadSegmentModels();
		this.readRupAndSegRatesFromExcelFile();
	}
	
	/**
	 * Load the Segment models from a text file
	 *
	 */
	private void loadSegmentModels() {
		segmentModelNames = new ArrayList();
		// add segment models 
		try {
			// read the text file
			ArrayList fileLines = FileUtils.loadFile(SEGMENT_MODELS_FILE_NAME);
			ArrayList segmentsList=null;
			String segmentModelName=null;
			for(int i=0; i<fileLines.size(); ++i) {
				// read the file line by line
				String line = ((String)fileLines.get(i)).trim();
				// skip the comment and blank lines
				if(line.equalsIgnoreCase("") || line.startsWith("#")) continue;
				// check if this is a segment model name
				if(line.startsWith(SEGMENT_MODEL_NAME_PREFIX)) {
					if(segmentModelName!=null ){
						// put segment model and corresponding ArrayList of segments in a HashMap
						this.segmentModels.put(segmentModelName, segmentsList);
					}
					segmentModelName = getSegmentModelName(line);
					segmentModelNames.add(segmentModelName);
					segmentsList = new ArrayList();
				} else  {
					// read the section ids
					segmentsList.add(getSegment(line));
				}
				
			}
			segmentModels.put(segmentModelName, segmentsList);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Get all segments in an ArrayList
	 * @param deformationModelId
	 * @param isAseisReducesArea
	 * @return
	 */
	public ArrayList getAllSegments(int deformationModelId, boolean isAseisReducesArea) {
		ArrayList segmentsList = new ArrayList();
		for(int i=0; i< this.segmentModelNames.size(); ++i)
			segmentsList.add(getSegmentedFaultData((String)segmentModelNames.get(i), deformationModelId, isAseisReducesArea));
		return segmentsList;
	}
	
	
	
	/**
	 * Return a list of ids of all fault sections which constitute A_faults
	 * @return
	 */
	public ArrayList getAllFaultSectionsIdList() {
		ArrayList faultSectionIdList = new ArrayList();
		for(int i=0; i< this.segmentModelNames.size(); ++i)
			faultSectionIdList.addAll(getFaultSectionsIdList((String)segmentModelNames.get(i)));
		return faultSectionIdList;
	}
	
	/**
	 * Get a list of fault section Ids within the selected segment model
	 * @return
	 */
	public ArrayList getFaultSectionsIdList(String selectedSegmentModel) {
		ArrayList segmentsList = (ArrayList)this.segmentModels.get(selectedSegmentModel);
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
	public SegmentedFaultData getSegmentedFaultData(String selectedSegmentModel, int deformationModelId,
			boolean isAseisReducesArea) {
		
		// no need to re-fetch data from database if the data alraady exists in cache
		if(this.selectedSegmentModel==null || !this.selectedSegmentModel.equalsIgnoreCase(selectedSegmentModel) ||
				this.deformationModelId!=deformationModelId)  {
			this.selectedSegmentModel = selectedSegmentModel;
			this.deformationModelId = deformationModelId;
			ArrayList segmentsList = (ArrayList)this.segmentModels.get(selectedSegmentModel);
			faultDataListInSelectedSegment = new ArrayList();
			faultSectionList = new ArrayList();
			// iterate over all segment
			for(int i=0; i<segmentsList.size(); ++i) {
				ArrayList segment = (ArrayList)segmentsList.get(i);
				ArrayList newSegment = new ArrayList();
				// iterate over all sections in a segment
				for(int j=0; j<segment.size(); ++j) {
					int faultSectionId = ((FaultSectionSummary)segment.get(j)).getSectionId();
					FaultSectionPrefData faultSectionPrefData = prefFaultSectionDAO.getFaultSectionPrefData(faultSectionId);
					//FaultSectionData faultSectionData = this.faultSectionDAO.getFaultSection(faultSectionId);
					// get slip rate and aseimic slip factor from deformation model
					faultSectionPrefData.setAseismicSlipFactor(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getAseismicSlipEstimate(deformationModelId, faultSectionId)));
					faultSectionPrefData.setAveLongTermSlipRate(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getSlipRateEstimate(deformationModelId, faultSectionId)));
					//FaultSectionPrefData faultSectionPrefData = faultSectionData.getFaultSectionPrefData();
					faultSectionList.add(faultSectionPrefData);
					newSegment.add(faultSectionPrefData);		
				}
				faultDataListInSelectedSegment.add(newSegment);
			}
		}
		
		// make SegmentedFaultData 
		double[]recurIntv = getRecurIntv(selectedSegmentModel);
		SegmentedFaultData segmetedFaultData = new SegmentedFaultData(faultDataListInSelectedSegment, isAseisReducesArea, selectedSegmentModel,
				recurIntv);
		return segmetedFaultData;
		
	}
	
	/**
	 * Get a list of FaultSectionPrefData for selected segment model
	 * @param selectedSegmentModel
	 * @param deformationModelId
	 * @return
	 */
	public ArrayList getPrefFaultSectionDataList(String selectedSegmentModel, int deformationModelId) {
		if(this.selectedSegmentModel==null || !this.selectedSegmentModel.equalsIgnoreCase(selectedSegmentModel) ||
				this.deformationModelId!=deformationModelId) this.getSegmentedFaultData(selectedSegmentModel, deformationModelId, true);
		return this.faultSectionList;
	}
	
	/**
	 * Get a list of all fault sections in A_Faults
	 * @param deformationModelId
	 * @return
	 */
	public ArrayList getAllPrefFaultSectionDataList(int deformationModelId) {
		ArrayList faultSectionsList = new ArrayList();
		for(int i=0; i< this.segmentModelNames.size(); ++i)
			faultSectionsList.addAll(getPrefFaultSectionDataList((String)segmentModelNames.get(i), deformationModelId));
		return faultSectionsList;

	}
	
	
	
	/**
	 * Read rupture rates and segment rates from Excel file
	 *
	 */
	private void readRupAndSegRatesFromExcelFile() {
		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(RATE_FILE_NAME));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			int lastIndex = sheet.getLastRowNum();
			// read data for each row
			for(int r = 1; r<=lastIndex; ++r) {	
				HSSFRow row = sheet.getRow(r);
				HSSFCell cell = row.getCell( (short) 0);
				// segment name
				String segmentName = cell.getStringCellValue().trim();
				RupSegRates rupSegRates = new RupSegRates(segmentName);
				r=r+2;
				while(true) {
					row = sheet.getRow(r++);
					cell = row.getCell( (short) 0);
					if(cell.getStringCellValue().trim().equalsIgnoreCase("Total"))
						break;
					// rup rate for the 3 models
					double prefRate = row.getCell((short)1).getNumericCellValue();
					double minRate = row.getCell((short)2).getNumericCellValue();
					double maxRate = row.getCell((short)3).getNumericCellValue();
					rupSegRates.addRupRate(prefRate, minRate, maxRate);
					// if segment rate is available
					cell = row.getCell( (short) 5);
					//System.out.println("***** ==" +cell.getNumericCellValue());
					if(cell != null &&
							! (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)) 
						rupSegRates.addSegRecurInterv(1.0/cell.getNumericCellValue());
				}
				r=r+1;
				this.segmentIntvAndRupRates.put(segmentName, rupSegRates);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a list of all segment names
	 * @return
	 */
	public ArrayList getSegmentNames() {
		return this.segmentModelNames;
	}
	
	/**
	 * Get recurrence intervals for selected segment model
	 * @param selectedSegmentModel
	 * @return
	 */
	public double[] getRecurIntv(String selectedSegmentModel) {
		RupSegRates rupSegRates = (RupSegRates)this.segmentIntvAndRupRates.get(selectedSegmentModel);
		double[] recurIntv = new double[rupSegRates.getNumSegments()];
		for(int i=0; i<rupSegRates.getNumSegments(); ++i)
			recurIntv[i] = rupSegRates.getSegRecurInterv(i);
		return recurIntv;
	}
	
	
	/**
	 * Get apriori rupture rates
	 * @param selectedSegmentModel
	 * @return
	 */
	public ValueWeight[] getAprioriRupRates(String selectedSegmentModel, String selectedRupModel) {
		RupSegRates rupSegRates = (RupSegRates)this.segmentIntvAndRupRates.get(selectedSegmentModel);
		ValueWeight[] rupRates = new ValueWeight[rupSegRates.getNumRups()];
		// geol insight rup model
		if(selectedRupModel.equalsIgnoreCase(GEOL_INSIGHT_RUP_MODEL)) {
			for(int i=0; i<rupSegRates.getNumRups(); ++i)
				rupRates[i] = new ValueWeight(rupSegRates.getPrefModelRupRate(i), 1.0);
		}
		// min rup model
		else if(selectedRupModel.equalsIgnoreCase(MIN_RATE_RUP_MODEL)) {
			for(int i=0; i<rupSegRates.getNumRups(); ++i)
				rupRates[i] = new ValueWeight(rupSegRates.getMinModelRupRate(i), 1.0);
	
		}
		// max rup model
		else if (selectedRupModel.equalsIgnoreCase(MAX_RATE_RUP_MODEL)) {
			for(int i=0; i<rupSegRates.getNumRups(); ++i)
				rupRates[i] = new ValueWeight(rupSegRates.getMaxModelRupRate(i), 1.0);
	
		} else rupRates = null;
		
		return rupRates;
	}
	
	
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
		StringTokenizer tokenizer = new StringTokenizer(line,"\n,");
		while(tokenizer.hasMoreTokens()) 
			faultSectionsIdList.add(faultSectionDAO.getFaultSectionSummary(Integer.parseInt(tokenizer.nextToken().trim())));
		return faultSectionsIdList;
	}
}
