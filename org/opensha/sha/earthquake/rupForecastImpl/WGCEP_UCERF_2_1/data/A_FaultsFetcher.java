/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data;

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
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.FaultSegmentData;
import org.opensha.util.FileUtils;

/**
 *
 * This class read the  segments from a text file and then go to database to fetch the fault sections
 * 
 * @author vipingupta
 *
 */
public class A_FaultsFetcher extends FaultsFetcher{
	private final static String RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/A_FaultsSegmentData_v8.xls";
	private final static String SEGMENT_MODELS_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/SegmentModels.txt";
	private HashMap segmentIntvAndRupRates = new HashMap();
	public final static String MIN_RATE_RUP_MODEL = "Min Rate Model";
	public final static String MAX_RATE_RUP_MODEL = "Max Rate Model";
	public final static String GEOL_INSIGHT_RUP_MODEL = "Geol Insight Solution";

	
	
	public A_FaultsFetcher() {
		super(SEGMENT_MODELS_FILE_NAME);
		// this may be dependent on deformation model (fix later?):
		this.readRupAndSegRatesFromExcelFile();
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
}
