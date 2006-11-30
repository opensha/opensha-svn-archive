/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.data.Location;
import org.opensha.data.ValueWeight;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PaleoSiteDB_DAO;
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
	private final static String RUP_RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/A_FaultsSegmentData_v10.xls";
	private final static String SEG_RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/Rev_Poisson_table_3.xls";
	private final static String SEGMENT_MODELS_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/SegmentModels.txt";
	private HashMap<String,A_PrioriRupRates> aPrioriRupRatesMap = new HashMap<String,A_PrioriRupRates>();
	private HashMap<String,ArrayList> segRatesMap = new HashMap<String,ArrayList>();
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
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(RUP_RATE_FILE_NAME));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			int lastIndex = sheet.getLastRowNum();
			// read data for each row
			for(int r = 1; r<=lastIndex; ++r) {	
				HSSFRow row = sheet.getRow(r);
				HSSFCell cell = row.getCell( (short) 0);
				// segment name
				String faultName = cell.getStringCellValue().trim();
				A_PrioriRupRates aPrioriRupRates = new A_PrioriRupRates(faultName);
				ArrayList rupNames = new ArrayList();
				r=r+2;
				while(true) {
					row = sheet.getRow(r++);
					cell = row.getCell( (short) 0);
					String name = cell.getStringCellValue().trim();
					if(name.equalsIgnoreCase("Total"))
						break;
					else rupNames.add(name);
					// rup rate for the 3 models
					double prefRate = row.getCell((short)1).getNumericCellValue();
					double minRate = row.getCell((short)2).getNumericCellValue();
					double maxRate = row.getCell((short)3).getNumericCellValue();
					aPrioriRupRates.addRupRate(prefRate, minRate, maxRate);
					/*// if segment rate is available
					cell = row.getCell( (short) 5);
					//System.out.println("***** ==" +cell.getNumericCellValue());
					if(cell != null &&
							! (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)) 
						rupSegRates.addMeanSegRecurInterv(1.0/cell.getNumericCellValue());*/
				}
				r=r+1;
				// convert segment names ArrayLList to String[] 
				String ruptureNames[] = new String[rupNames.size()];
				for(int i=0; i<rupNames.size(); ++i) ruptureNames[i] = (String) rupNames.get(i);
				this.aPrioriRupRatesMap.put(faultName, aPrioriRupRates);
				this.segRatesMap.put(faultName,new  ArrayList());
				this.segmentNamesMap.put(faultName, ruptureNames);
			}
			readSegRates();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Read the segment recurrence intervals
	 *
	 */
	private void readSegRates() {
		try {				
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(SEG_RATE_FILE_NAME));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			int lastRowIndex = sheet.getLastRowNum();
			double lat, lon, rate, sigma;
			int faultSectionId;
			for(int r=2; r<=lastRowIndex; ++r) {	
				HSSFRow row = sheet.getRow(r);
				if(row==null) continue;
				HSSFCell cell = row.getCell( (short) 1);
				if(cell==null || cell.getCellType()==HSSFCell.CELL_TYPE_STRING) continue;
				lat = cell.getNumericCellValue();
				lon = row.getCell( (short) 2).getNumericCellValue();
				rate = row.getCell( (short) 18).getNumericCellValue();
				sigma =  row.getCell( (short) 19).getNumericCellValue();
				faultSectionId = PaleoSiteDB_DAO.getClosestFaultSection(new Location(lat,lon)).getSectionId();
				//System.out.println(lat+","+lon+","+faultSectionId);
				setRecurIntv(faultSectionId, rate, sigma);
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Set the recurrence intervals for this fault section. First we find the correct segment and then set the recurrence intervals.
	 * @param faultSectiondId
	 * @param rate
	 * @param sigma
	 */
	private void setRecurIntv(int faultSectionId, double rate, double sigma) {

		Iterator<String> it = faultModels.keySet().iterator();
		// Iterate over all A-Faults
		while(it.hasNext()) {
			String faultName = it.next();
			ArrayList segRatesList = this.segRatesMap.get(faultName);
			ArrayList segmentsList = (ArrayList)this.faultModels.get(faultName);
			// iterate over all segments in this fault
			for(int i=0; i<segmentsList.size(); ++i) {
				ArrayList segment = (ArrayList)segmentsList.get(i);
				// iterate over all sections in a segment
				for(int segIndex=0; segIndex<segment.size(); ++segIndex) {
					if(faultSectionId == ((FaultSectionSummary)segment.get(segIndex)).getSectionId()) {
						SegRateConstraint segRateConstraint = new SegRateConstraint(faultName);
						segRateConstraint.setSegRate(segIndex, rate, sigma);
						segRatesList.add(segRateConstraint);
						return;
					}
				}
			}
		}
		
		throw new RuntimeException ("The location cannot be mapped to a A-Fault segment");
	}
	
	/**
	 * Get recurrence intervals for selected segment model
	 * @param selectedSegmentModel
	 * @return
	 */
	public  ArrayList<SegRateConstraint> getSegRateConstraint(String selectedSegmentModel) {
		return this.segRatesMap.get(selectedSegmentModel);
	}
	
	/**
	 * Get recurrence intervals for selected segment model
	 * @param selectedSegmentModel
	 * @return
	 */
	public double[] getRecurIntv(String selectedSegmentModel) {
		ArrayList<SegRateConstraint> segRateConstraintList = getSegRateConstraint(selectedSegmentModel);
		ArrayList segmentsList = (ArrayList)this.faultModels.get(selectedSegmentModel);
		double[] recurIntv = new double[segmentsList.size()];
		// set all recurrence intervals to NaN
		for(int i=0; i<recurIntv.length; ++i)
			recurIntv[i] = Double.NaN;
		// set the recurrence intervals
		for(int i=0; i<segRateConstraintList.size(); ++i) {
			SegRateConstraint segRateConstraint = segRateConstraintList.get(i);
			recurIntv[segRateConstraint.getSegIndex()] = 1/segRateConstraint.getMean();
		}
		return recurIntv;
	}
	
	/**
	 * Get low recurrence intervals for selected segment model
	 * @param selectedSegmentModel
	 * @return
	 */
	public double[] getLowRecurIntv(String selectedSegmentModel) {
		ArrayList<SegRateConstraint> segRateConstraintList = getSegRateConstraint(selectedSegmentModel);
		ArrayList segmentsList = (ArrayList)this.faultModels.get(selectedSegmentModel);
		double[] recurIntv = new double[segmentsList.size()];
		// set all recurrence intervals to NaN
		for(int i=0; i<recurIntv.length; ++i)
			recurIntv[i] = Double.NaN;
		// set the recurrence intervals
		for(int i=0; i<segRateConstraintList.size(); ++i) {
			SegRateConstraint segRateConstraint = segRateConstraintList.get(i);
			recurIntv[segRateConstraint.getSegIndex()] = 1/(segRateConstraint.getMean()+2*segRateConstraint.getStdDevToMean());
		}
		return recurIntv;
	}
	
	/**
	 * Get high recurrence intervals for selected segment model
	 * @param selectedSegmentModel
	 * @return
	 */
	public double[] getHighRecurIntv(String selectedSegmentModel) {
		ArrayList<SegRateConstraint> segRateConstraintList = getSegRateConstraint(selectedSegmentModel);
		ArrayList segmentsList = (ArrayList)this.faultModels.get(selectedSegmentModel);
		double[] recurIntv = new double[segmentsList.size()];
		// set all recurrence intervals to NaN
		for(int i=0; i<recurIntv.length; ++i)
			recurIntv[i] = Double.NaN;
		// set the recurrence intervals
		for(int i=0; i<segRateConstraintList.size(); ++i) {
			SegRateConstraint segRateConstraint = segRateConstraintList.get(i);
			recurIntv[segRateConstraint.getSegIndex()] = 1/(segRateConstraint.getMean()-2*segRateConstraint.getStdDevToMean());
		}
		return recurIntv;
	}
	
	/**
	 * Get apriori rupture rates
	 * @param selectedSegmentModel
	 * @return
	 */
	public ValueWeight[] getAprioriRupRates(String faultModel, String rupModelType) {
		  
		A_PrioriRupRates rupSegRates = (A_PrioriRupRates)this.aPrioriRupRatesMap.get(faultModel);
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
