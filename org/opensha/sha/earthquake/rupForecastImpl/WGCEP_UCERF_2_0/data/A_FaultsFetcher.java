/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.data;

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
import org.opensha.commons.data.ValueWeight;
import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.FaultSegmentData;

/**
 *
 * This class read the  segments from a text file and then go to database to fetch the fault sections
 * 
 * @author vipingupta
 *
 */
public class A_FaultsFetcher {
	private final static String RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_0/data/A_FaultsSegmentData_v8.xls";
	private final static String SEGMENT_MODELS_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_0/data/SegmentModels.txt";
	private HashMap faultModels = new HashMap();
	// DAO to access the fault section database
	private FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	private DeformationModelDB_DAO deformationModelDB_DAO = new DeformationModelDB_DAO(DB_AccessAPI.dbConnection);
	private PrefFaultSectionDataDB_DAO prefFaultSectionDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
	private final static String FAULT_MODEL_NAME_PREFIX = "-";
	private ArrayList faultModelNames;
	private HashMap segmentIntvAndRupRates = new HashMap();
	private HashMap segmentNamesMap = new HashMap();
	public final static String MIN_RATE_RUP_MODEL = "Min Rate Model";
	public final static String MAX_RATE_RUP_MODEL = "Max Rate Model";
	public final static String GEOL_INSIGHT_RUP_MODEL = "Geol Insight Solution";
	private String selectedFaultModel=null;
	private int deformationModelId=-1;
	private int prevDeformationModelId=-1;
	private boolean prevIsAseisReducesArea;
	private ArrayList faultDataListInSelectedSegment=null;
	private ArrayList faultSectionList=null;
	private ArrayList faultSegDataList = null;
	
	
	public A_FaultsFetcher() {
		// make faultModels hashMap:
		this.loadSegmentModels();
		// this may be dependent on deformation model (fix later?):
		this.readRupAndSegRatesFromExcelFile();
	}
	
	/**
	 * Create the faultModels hashMap by reading a file that defines what 
	 * sections are in each segment and what segments are in each fault model
	 *
	 */
	private void loadSegmentModels() {
		faultModelNames = new ArrayList();
		// read file 
		try {
			// read the text file that defines the sctions in each segment for each fault model
			ArrayList fileLines = FileUtils.loadFile(SEGMENT_MODELS_FILE_NAME);
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
	 * This returns a list of FaultSegmentData object for all the Type A faults
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
	 * Return a list of ids of all fault sections in any of the A fault models.
	 * This is used, or example, to filter out A-fault sections to get B-fault sections from
	 * a master list.
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
					int faultSectionId = ((FaultSectionSummary)sectionList.get(j)).getSectionId();
					FaultSectionPrefData faultSectionPrefData = prefFaultSectionDAO.getFaultSectionPrefData(faultSectionId);
					// get slip rate and aseimic slip factor from deformation model
					faultSectionPrefData.setAseismicSlipFactor(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getAseismicSlipEstimate(deformationModelId, faultSectionId)));
					faultSectionPrefData.setAveLongTermSlipRate(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getSlipRateEstimate(deformationModelId, faultSectionId)));
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
	 * Get a list of all fault sections in A_Faults
	 * @param deformationModelId
	 * @return
	 
	public ArrayList getAllPrefFaultSectionDataList(int deformationModelId) {
		ArrayList faultSectionsList = new ArrayList();
		for(int i=0; i< this.segmentModelNames.size(); ++i)
			faultSectionsList.addAll(getPrefFaultSectionDataList((String)segmentModelNames.get(i), deformationModelId));
		return faultSectionsList;

	}
*/
	
	
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
				String faultName = cell.getStringCellValue().trim();
				RupSegRates rupSegRates = new RupSegRates(faultName);
				ArrayList segmentNames = new ArrayList();
				r=r+2;
				while(true) {
					row = sheet.getRow(r++);
					cell = row.getCell( (short) 0);
					String name = cell.getStringCellValue().trim();
					if(name.equalsIgnoreCase("Total"))
						break;
					else segmentNames.add(name);
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
				// convert segment names ArrayLList to String[] 
				String segNames[] = new String[segmentNames.size()];
				for(int i=0; i<segmentNames.size(); ++i) segNames[i] = (String) segmentNames.get(i);
				this.segmentIntvAndRupRates.put(faultName, rupSegRates);
				this.segmentNamesMap.put(faultName, segNames);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read the high low and mean recur intervals from the excel sheet
	 * @return
	 */
	public HashMap getHighLowMeanRecurIntv() {
		HashMap highLowMeanRecurIntv=new HashMap();
		String RI_AVE = "RI Ave";
		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(RATE_FILE_NAME));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			for(int sheetIndex=1; sheetIndex<wb.getNumberOfSheets(); ++sheetIndex) {
				HSSFSheet sheet = wb.getSheetAt(sheetIndex);
				String faultName = wb.getSheetName(sheetIndex);
				SegmentRecurIntv segRecurIntv= new SegmentRecurIntv(faultName);
				int lastRowIndex = sheet.getLastRowNum();
				
				// read data for each row and find the place where recurreence intervals are given in the sheet
				boolean found = false;
				int r=2;
				int col=0;
				for(; r<=lastRowIndex && !found; ++r) {	
					HSSFRow row = sheet.getRow(r);
					if(row==null) continue;
					col = 0;
					while(col<10) {
						//System.out.println(sheetIndex+","+r+","+col);
						HSSFCell cell = row.getCell( (short) col);
						//System.out.println(cell.getCellType()+","+cell.getStringCellValue().trim());
						if(cell!=null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING && cell.getStringCellValue().trim().equalsIgnoreCase(RI_AVE)) {
							found = true;
							break;
						}
						++col;
					}
				}
				
				int count=1;
				// now we have the row and col where mean recurrence interval starts
				for(; r<=lastRowIndex; ++r, ++count) {
					
					// skip SJV model B
					if(count==3 && faultName.equalsIgnoreCase("San Jacinto")) continue; 
					
					HSSFRow row = sheet.getRow(r);
					if(row==null) break;
					//System.out.println("||||||||"+sheetIndex+","+r+","+col);
					// mean recur intv 
					HSSFCell cell = row.getCell( (short) col);
					if(cell == null || cell.getCellType()==HSSFCell.CELL_TYPE_BLANK) break;
					else if(cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC ||
							cell.getCellType()== HSSFCell.CELL_TYPE_FORMULA) {
						segRecurIntv.addMeanRecurIntv(cell.getNumericCellValue());
						//System.out.println("$$$$$$$"+sheetIndex+","+r+","+col);
					}
					else {
						try {
							//System.out.println(cell.getStringCellValue().trim()+","+sheetIndex+","+r+","+col);
							Double d = new Double(cell.getStringCellValue().trim());
							
							segRecurIntv.addMeanRecurIntv(d.doubleValue());
							//System.out.println("*******"+sheetIndex+","+r+","+col);
						}catch(NumberFormatException e) {
							segRecurIntv.addMeanRecurIntv(Double.NaN);
							//System.out.println("&&&&&&&&"+sheetIndex+","+r+","+col);
						}
					}
					
					// low recur intv
					HSSFCell lowCell = row.getCell( (short) (col+1));
					if(lowCell==null || lowCell.getCellType()==HSSFCell.CELL_TYPE_BLANK) segRecurIntv.addLowRecurIntv(Double.NaN);
					else if( lowCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC ||
							lowCell.getCellType()== HSSFCell.CELL_TYPE_FORMULA) {
						segRecurIntv.addLowRecurIntv(lowCell.getNumericCellValue());
					}
					else  {
						try {
							Double d = new Double(lowCell.getStringCellValue());
							segRecurIntv.addLowRecurIntv(d.doubleValue());
						}catch(NumberFormatException e) {
							segRecurIntv.addLowRecurIntv(Double.NaN);
						}
					}
					
					
					// high recur intv
					HSSFCell highCell = row.getCell( (short) (col+2));
					if(highCell==null || highCell.getCellType()==HSSFCell.CELL_TYPE_BLANK) segRecurIntv.addHighRecurIntv(Double.NaN);
					else if(highCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC ||
							highCell.getCellType()== HSSFCell.CELL_TYPE_FORMULA) {
						segRecurIntv.addHighRecurIntv(highCell.getNumericCellValue());
					}
					else {
						try {
							Double d = new Double(highCell.getStringCellValue());
							segRecurIntv.addHighRecurIntv(d.doubleValue());
						}catch(NumberFormatException e) {
							segRecurIntv.addHighRecurIntv(Double.NaN);
						}
					}
					
				}
				highLowMeanRecurIntv.put(faultName, segRecurIntv);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return highLowMeanRecurIntv;
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
	public ValueWeight[] getAprioriRupRates(String faultModel, String rupModelType) {
		
		RupSegRates rupSegRates = (RupSegRates)this.segmentIntvAndRupRates.get(faultModel);
		ValueWeight[] rupRates = new ValueWeight[rupSegRates.getNumRups()];
		// geol insight rup model
		if(rupModelType.equalsIgnoreCase(GEOL_INSIGHT_RUP_MODEL)) {
			for(int i=0; i<rupSegRates.getNumRups(); ++i)
				rupRates[i] = new ValueWeight(rupSegRates.getPrefModelRupRate(i), 1.0);
		}
		// min rup model
		else if(rupModelType.equalsIgnoreCase(MIN_RATE_RUP_MODEL)) {
			for(int i=0; i<rupSegRates.getNumRups(); ++i)
				rupRates[i] = new ValueWeight(rupSegRates.getMinModelRupRate(i), 1.0);
	
		}
		// max rup model
		else if (rupModelType.equalsIgnoreCase(MAX_RATE_RUP_MODEL)) {
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
