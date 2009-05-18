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
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.ValueWeight;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PaleoSiteDB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
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
	private final static String RUP_RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/A_FaultsSegmentData_v12.xls";
	private final static String SEG_RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/BPT_rates2.xls";
	private HashMap<String,A_PrioriRupRates> aPrioriRupRatesMap;
	private HashMap<String,ArrayList> segRatesMap;
	public final static String MIN_RATE_RUP_MODEL = "Min Rate Model";
	public final static String MAX_RATE_RUP_MODEL = "Max Rate Model";
	public final static String GEOL_INSIGHT_RUP_MODEL = "Geol Insight Solution";
	private PrefFaultSectionDataDB_DAO faultSectionPrefDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
	private final static String A_FAULT_SEGMENTS_MODEL1 = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/SegmentModelsF2.1.txt";
	private final static String A_FAULT_SEGMENTS_MODEL2 = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/SegmentModelsF2.2.txt";
	private ArrayList<EventRates> eventRatesList; // Location list where event rates are available
	
	/**
	 * 
	 *
	 */
	public A_FaultsFetcher() {
		aPrioriRupRatesMap = new HashMap<String,A_PrioriRupRates>();
		this.readA_PrioriRupRates();
	}
	
	/**
	 * Set the file name for the segment models. This function needs to be called before any other function can be called.
	 * @param fileName
	 */
	public void setDeformationModel(DeformationModelSummary defModelSummary) {
		deformationModelId = defModelSummary.getDeformationModelId();
		//	find the deformation model
		String fileName=null;
		String faultModelName = defModelSummary.getFaultModel().getFaultModelName();
		// get the B-Fault filename based on selected fault model
		if(faultModelName.equalsIgnoreCase("F2.1")) fileName = A_FAULT_SEGMENTS_MODEL1;
		else if((faultModelName.equalsIgnoreCase("F2.2"))) fileName = A_FAULT_SEGMENTS_MODEL2;
		else throw new RuntimeException("Unsupported Fault Model");
		this.loadSegmentModels(fileName);
		segRatesMap = new HashMap<String,ArrayList>();
		readSegRates();
	}
	
	/**
	 * Read rupture rates and segment rates from Excel file
	 *
	 */
	private void readA_PrioriRupRates() {
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
				faultModelNames.add(faultName);
				A_PrioriRupRates aPrioriRupRates = new A_PrioriRupRates(faultName);
				ArrayList rupNames = new ArrayList();
				++r;
				row = sheet.getRow(r);
				// Get the supported rup model types
				int lastColIndex=0 ;
				ArrayList<String> rupModelTypes = new ArrayList<String>();;
				for(int i=1;true; ++i, ++lastColIndex) {
					cell = row.getCell((short)i);
					if(cell==null || cell.getCellType()==HSSFCell.CELL_TYPE_BLANK) break;
					rupModelTypes.add(row.getCell((short)i).getStringCellValue()); 
				}
				++r;
				while(true) {
					row = sheet.getRow(r++);
					cell = row.getCell( (short) 0);
					String name = cell.getStringCellValue().trim();
					if(name.equalsIgnoreCase("Total"))
						break;
					else rupNames.add(name);
					// get apriori rates
					for(int i=1;i<=lastColIndex; ++i) {
						aPrioriRupRates.putRupRate(rupModelTypes.get(i-1), row.getCell((short)i).getNumericCellValue());
					}
				}
				r=r+1;
				// convert segment names ArrayLList to String[] 
				String ruptureNames[] = new String[rupNames.size()];
				for(int i=0; i<rupNames.size(); ++i) ruptureNames[i] = (String) rupNames.get(i);
				this.aPrioriRupRatesMap.put(faultName, aPrioriRupRates);
				this.segmentNamesMap.put(faultName, ruptureNames);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Read the segment recurrence intervals
	 *
	 */
	private void readSegRates() {
		Iterator<String> it = aPrioriRupRatesMap.keySet().iterator();
		while(it.hasNext()) this.segRatesMap.put(it.next(),new  ArrayList());
		eventRatesList = new ArrayList<EventRates>();
		try {				
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(SEG_RATE_FILE_NAME));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			int lastRowIndex = sheet.getLastRowNum();
			double lat, lon, rate, sigma;
			String siteName;
			int faultSectionId;
			for(int r=2; r<=lastRowIndex; ++r) {	
				if(r==9) continue; // Ignore the Hayward North
				HSSFRow row = sheet.getRow(r);
				if(row==null) continue;
				HSSFCell cell = row.getCell( (short) 1);
				if(cell==null || cell.getCellType()==HSSFCell.CELL_TYPE_STRING) continue;
				lat = cell.getNumericCellValue();
				siteName = row.getCell( (short) 0).getStringCellValue().trim();
				lon = row.getCell( (short) 2).getNumericCellValue();
				rate = row.getCell( (short) 19).getNumericCellValue();
				sigma =  row.getCell( (short) 20).getNumericCellValue();
				faultSectionId = getClosestFaultSectionId(new Location(lat,lon));
				if(faultSectionId==-1) continue; // closest fault section is at a distance of more than 2 km
				eventRatesList.add(new EventRates(siteName, lat,lon, rate, sigma));
				setRecurIntv(faultSectionId, rate, sigma);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * It gets the list of all event rates.
	 * It gets them  from Tom Parson's excel sheet
	 * 
	 * @return
	 */
	public ArrayList<EventRates> getEventRatesList() {
		return this.eventRatesList;
	}
	
	/**
	 * Get closest fault section Id to this location. The fault section should be within 2 km distance of the location else it returns null
	 * 
	 * @param loc
	 * @return
	 */
	private int getClosestFaultSectionId(Location loc) {
		ArrayList<Integer> faultSectionIdList = getAllFaultSectionsIdList();
		double minDist = Double.MAX_VALUE, dist;
		FaultSectionPrefData closestFaultSection=null;
		for(int i=0; i<faultSectionIdList.size(); ++i) {
			FaultSectionPrefData  prefFaultSectionData = faultSectionPrefDAO.getFaultSectionPrefData(faultSectionIdList.get(i));
			//System.out.println(faultSectionIdList.get(i));
			dist  = prefFaultSectionData.getFaultTrace().getMinHorzDistToLine(loc);
			//System.out.println(prefFaultSectionData.getSectionId()+":"+dist);
			if(dist<minDist) {
				minDist = dist;
				closestFaultSection = prefFaultSectionData;
			}
		}
		//System.out.println(minDist);
		if(minDist>2) return -1;
		return closestFaultSection.getSectionId();
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
						segRateConstraint.setSegRate(i, rate, sigma);
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
	public  ArrayList<SegRateConstraint> getSegRateConstraints(String faultName) {
		return this.segRatesMap.get(faultName);
	}
	
	/**
	 * Get segment rate constraints for selected faultName and segment index. Returns an empty list, if there is no rate constraint for this segment
	 * @param faultModel
	 * @param segIndex
	 * @return
	 */
	public ArrayList<SegRateConstraint> getSegRateConstraints(String faultName, int segIndex) {
		ArrayList<SegRateConstraint> segRateConstraintList = getSegRateConstraints(faultName);
		ArrayList<SegRateConstraint> segmentRates= new ArrayList<SegRateConstraint>();
		// set the recurrence intervals
		for(int i=0; i<segRateConstraintList.size(); ++i) {
			SegRateConstraint segRateConstraint = segRateConstraintList.get(i);
			if(segRateConstraint.getSegIndex() == segIndex)
				segmentRates.add(segRateConstraint);
		}
		return segmentRates;
	}
	
	
	/**
	 * Get apriori rupture rates
	 * @param selectedSegmentModel
	 * @return
	 */
	public ValueWeight[] getAprioriRupRates(String faultName, String rupModelType) {
		A_PrioriRupRates aPrioriRatesList = this.aPrioriRupRatesMap.get(faultName);
		ArrayList<Double> aPrioriRates = aPrioriRatesList.getA_PrioriRates(rupModelType);
		ValueWeight[] rupRates = new ValueWeight[aPrioriRates.size()];
		for(int i=0; i<aPrioriRates.size(); ++i)
				rupRates[i] = new ValueWeight(aPrioriRates.get(i), 1.0);
		return rupRates;
	}
	
	/**
	 * Get a list of rup models(Eg. Min, Max, Geological Insight) for selected faultName
	 * @param faultModel
	 * @return
	 */
	public ArrayList<String> getRupModels(String faultName) {
		return aPrioriRupRatesMap.get(faultName).getSupportedModelNames(); 
	}
}
