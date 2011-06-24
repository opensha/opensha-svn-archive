package scratch.UCERF3.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.geo.Location;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;

public class UCERF2_PaleoSegRateData {
	
	final static String PALEO_DATA_FILE_NAME = "Appendix_C_Table7_091807.xls";
	
	protected final static boolean D = true;  // for debugging
	
	public static ArrayList<SegRateConstraint> getConstraints(File precomputedDataDir, ArrayList<FaultSectionPrefData> faultSectionData) {
		
		String fullpathname = precomputedDataDir.getAbsolutePath()+File.separator+PALEO_DATA_FILE_NAME;
		ArrayList<SegRateConstraint> segRateConstraints   = new ArrayList<SegRateConstraint>();
		try {				
			if(D) System.out.println("Reading Paleo Seg Rate Data from "+fullpathname);
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(fullpathname));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			int lastRowIndex = sheet.getLastRowNum();
			double lat, lon, rate, sigma, lower95Conf, upper95Conf;
			String siteName;
			for(int r=1; r<=lastRowIndex; ++r) {	
				HSSFRow row = sheet.getRow(r);
				if(row==null) continue;
				HSSFCell cell = row.getCell(1);
				if(cell==null || cell.getCellType()==HSSFCell.CELL_TYPE_STRING) continue;
				lat = cell.getNumericCellValue();
				siteName = row.getCell(0).getStringCellValue().trim();
				lon = row.getCell(2).getNumericCellValue();
				rate = row.getCell(3).getNumericCellValue();
				sigma =  row.getCell(4).getNumericCellValue();
				lower95Conf = row.getCell(7).getNumericCellValue();
				upper95Conf =  row.getCell(8).getNumericCellValue();
				
				// get Closest section
				double minDist = Double.MAX_VALUE, dist;
				int closestFaultSectionIndex=-1;
				Location loc = new Location(lat,lon);
				for(int sectionIndex=0; sectionIndex<faultSectionData.size(); ++sectionIndex) {
					dist  = faultSectionData.get(sectionIndex).getFaultTrace().minDistToLine(loc);
					if(dist<minDist) {
						minDist = dist;
						closestFaultSectionIndex = sectionIndex;
					}
				}
				if(minDist>2) continue; // closest fault section is at a distance of more than 2 km
				
				// add to Seg Rate Constraint list
				String name = faultSectionData.get(closestFaultSectionIndex).getSectionName();
				SegRateConstraint segRateConstraint = new SegRateConstraint(name);
				segRateConstraint.setSegRate(closestFaultSectionIndex, rate, sigma, lower95Conf, upper95Conf);
				if(D) System.out.println("\t"+siteName+" (lat="+lat+", lon="+lon+") associated with "+name+
						" (section index = "+closestFaultSectionIndex+")\tdist="+(float)minDist+"\trate="+(float)rate+
						"\tsigma="+(float)sigma+"\tlower95="+(float)lower95Conf+"\tupper95="+(float)upper95Conf);
				segRateConstraints.add(segRateConstraint);
			}
		}catch(Exception e) {
			System.out.println("UNABLE TO READ PALEO DATA");
		}
		return segRateConstraints;
	}
}
