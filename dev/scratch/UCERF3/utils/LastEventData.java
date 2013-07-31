package scratch.UCERF3.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This class loads and stores Last Event Data for UCERF3 subsections.
 * @author kevin
 *
 */
public class LastEventData {
	
	private static final String SUB_DIR = "FaultModels";
	private static final String FILE_NAME = "UCERF3_OpenIntervals_ver5.xls";
	
	private static final GregorianCalendar OPEN_INTERVAL_BASIS = new GregorianCalendar(2013, 0, 0);
//	private static final double MILLIS_TO_YEARS = (double)(1000*60*24);
	
	private String sectName;
	private int parentSectID;
	private double lastOffset;
	private double openInterval;
	private GregorianCalendar eventDate;
	private Location startLoc;
	private Location endLoc;
	
	/**
	 * Loads last event data for every section where the open interval is specified, grouped
	 * by parent section ID
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Map<Integer, List<LastEventData>> load() throws IOException {
		return load(UCERF3_DataUtils.locateResourceAsStream(SUB_DIR, FILE_NAME));
	}
	
	/**
	 * Loads last event data for every section where the open interval is specified, grouped
	 * by parent section ID
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static Map<Integer, List<LastEventData>> load(InputStream is) throws IOException {
		POIFSFileSystem fs = new POIFSFileSystem(is);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		
		Map<Integer, List<LastEventData>> datas = Maps.newHashMap();
		
		for (int rowIndex=0; rowIndex<=sheet.getLastRowNum(); rowIndex++) {
			HSSFRow row = sheet.getRow(rowIndex);
			HSSFCell intervalCell = row.getCell(4);
			// only cells with open intervals
			if (intervalCell == null || intervalCell.getCellType() != HSSFCell.CELL_TYPE_NUMERIC)
				continue;
			double interval = intervalCell.getNumericCellValue();
			String name = row.getCell(0).getStringCellValue();
			int parentID = (int)row.getCell(1).getNumericCellValue();
			Preconditions.checkState(parentID >= 0);
			HSSFCell offsetCell = row.getCell(3);
			double offset;
			if (offsetCell == null || offsetCell.getCellType() != HSSFCell.CELL_TYPE_NUMERIC)
				offset = Double.NaN;
			else
				offset = offsetCell.getNumericCellValue();
			// make sure it has a location
			HSSFCell locStartCell = row.getCell(6);
			if (locStartCell == null || locStartCell.getCellType() != HSSFCell.CELL_TYPE_NUMERIC) {
				System.err.println("WARNING: no location for "+name+"...skipping!");
				continue;
			}
			double startLat = row.getCell(6).getNumericCellValue();
			double startLon = row.getCell(7).getNumericCellValue();
			double endLat = row.getCell(8).getNumericCellValue();
			double endLon = row.getCell(9).getNumericCellValue();
			Location startLoc = new Location(startLat, startLon);
			Location endLoc = new Location(endLat, endLon);
			
			List<LastEventData> parentList = datas.get(parentID);
			if (parentList == null) {
				parentList = Lists.newArrayList();
				datas.put(parentID, parentList);
			}
			
			parentList.add(new LastEventData(name, parentID, offset, interval, startLoc, endLoc));
		}
		return datas;
	}
	
	/**
	 * This populates the last event data in the given subsections list from the
	 * given last event data
	 * @param sects
	 * @param datas
	 */
	public static void populateSubSects(List<FaultSectionPrefData> sects,
			Map<Integer, List<LastEventData>> datas) {
		int populated = 0;
		// start/end location tolerance (km)
		final double locTol = 1d;
		HashSet<LastEventData> usedDatas = new HashSet<LastEventData>();
		for (FaultSectionPrefData sect : sects) {
			int parentID = sect.getParentSectionId();
			List<LastEventData> parentDatas = datas.get(parentID);
			if (parentDatas == null)
				// no data for this section
				continue;
			// now find closest
			for (LastEventData data : parentDatas) {
				if (data.matchesLocation(sect, locTol)) {
					Preconditions.checkState(!usedDatas.contains(data), "Duplicate on: "+data.getSectName());
					sect.setDateOfLastEvent(data.getDateOfLastEvent().getTimeInMillis());
					sect.setSlipInLastEvent(data.getLastOffset());
					populated++;
					usedDatas.add(data);
					break;
				}
			}
		}
		int numDatas = 0;
		List<String> unusedData = Lists.newArrayList();
		for (List<LastEventData> dataList : datas.values()) {
			numDatas += dataList.size();
			for (LastEventData data : dataList)
				if (!usedDatas.contains(data))
					unusedData.add(data.getSectName());
		}
		System.out.println("Populated "+populated+"/"+sects.size()+" sects from "+numDatas+" last event data");
		System.out.println("Unused: "+Joiner.on(",").join(unusedData));
	}
	
	/**
	 * Calculates a date from the open interval and open interval basis to day precision
	 * ignoring leap years
	 * @param openInterval
	 * @return
	 */
	private static GregorianCalendar calcDate(double openInterval) {
		GregorianCalendar eventDate = (GregorianCalendar)OPEN_INTERVAL_BASIS.clone();
		
		// go back years
		int years = (int)openInterval;
		if (years > 0)
			eventDate.add(Calendar.YEAR, -years);
		
		double fractYears = openInterval - Math.floor(openInterval);
		if ((float)fractYears == 0f)
			return eventDate;
		
		int days = (int)(fractYears*365d);
		if (days > 0)
			eventDate.add(Calendar.DAY_OF_YEAR, -days);
		
		return eventDate;
	}

	public LastEventData(String sectName, int parentSectID, double lastOffset,
			double openInterval, Location startLoc, Location endLoc) {
		this(sectName, parentSectID, lastOffset, openInterval, calcDate(openInterval), startLoc, endLoc);
	}
	
	private LastEventData(String sectName, int parentSectID, double lastOffset,
			double openInterval, GregorianCalendar eventDate, Location startLoc, Location endLoc) {
		super();
		this.sectName = sectName;
		this.parentSectID = parentSectID;
		this.lastOffset = lastOffset;
		this.openInterval = openInterval;
		this.eventDate = eventDate;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
	}

	public String getSectName() {
		return sectName;
	}

	public int getParentSectID() {
		return parentSectID;
	}

	/**
	 * 
	 * @return last offset (m) or NaN if unknown
	 */
	public double getLastOffset() {
		return lastOffset;
	}

	/**
	 * 
	 * @return open interval from the OPEN_INTERVAL_BASIS reference date
	 */
	public double getRefOpenInterval() {
		return openInterval;
	}
	
	/**
	 * 
	 * @return Date of last event
	 */
	public GregorianCalendar getDateOfLastEvent() {
		return eventDate;
	}
	
	public boolean matchesLocation(FaultSectionPrefData sect, double toleranceKM) {
		Location sectStartLoc = sect.getFaultTrace().first();
		Location sectEndLoc = sect.getFaultTrace().last();
		
		if (LocationUtils.horzDistanceFast(sectStartLoc, startLoc) <= toleranceKM
				&& LocationUtils.horzDistanceFast(sectEndLoc, endLoc) <= toleranceKM)
			return true;
		
		return LocationUtils.horzDistanceFast(sectStartLoc, endLoc) <= toleranceKM
				&& LocationUtils.horzDistanceFast(sectEndLoc, startLoc) <= toleranceKM;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		GregorianCalendar date = calcDate(14000);
//		System.out.println(date);
//		System.out.println(date.get(Calendar.YEAR));
//		System.out.println(date.getTimeInMillis());
//		date.setTimeInMillis(date.getTimeInMillis());
//		System.out.println(date.get(Calendar.YEAR));
//		System.out.println(date.getTimeInMillis());
//		System.out.println(Long.MAX_VALUE);
		
		Map<Integer, List<LastEventData>> datas = load();
		List<FaultSectionPrefData> subSects = new DeformationModelFetcher(
				FaultModels.FM3_1, DeformationModels.GEOLOGIC,
				UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1d).getSubSectionList();
		populateSubSects(subSects, datas);
		
		subSects = new DeformationModelFetcher(
				FaultModels.FM3_2, DeformationModels.GEOLOGIC,
				UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1d).getSubSectionList();
		populateSubSects(subSects, datas);
	}

}
