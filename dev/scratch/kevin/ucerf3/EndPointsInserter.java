package scratch.kevin.ucerf3;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.FaultEndPointCategory;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.sha.faultSurface.FaultTrace;

import com.google.common.base.Preconditions;

public class EndPointsInserter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File excelFile = new File("/home/kevin/OpenSHA/UCERF3/db_inserts/UCERF3 fault section endpoint categories_2012_01_17.xls");
		
		DB_AccessAPI readDB = null;
		DB_AccessAPI writeDB = null;
		
		final double horizDist = 0.01d; // KM
		
		try {
			readDB = DB_ConnectionPool.getLatestReadOnlyConn();
//			DB_ConnectionPool.authenticateDBConnection(true, true);
			
			POIFSFileSystem fs = new POIFSFileSystem(new BufferedInputStream(new FileInputStream(excelFile)));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			
			FaultSectionVer2_DB_DAO fs2db = new FaultSectionVer2_DB_DAO(readDB);
			System.out.print("Fetching sections...");
			List<FaultSectionData> sects = fs2db.getAllFaultSections();
			System.out.println("DONE.");
			HashMap<String, FaultSectionData> sectsNameMap = new HashMap<String, FaultSectionData>();
			for (FaultSectionData data : sects) {
				Preconditions.checkState(!sectsNameMap.containsKey(data.getName()));
				sectsNameMap.put(data.getName(), data);
			}
			
			HashSet<FaultSectionData> modifieds = new HashSet<FaultSectionData>();
			
			for (int i=1; i<=sheet.getLastRowNum(); i++) {
				HSSFRow row = sheet.getRow(i);
				String name = row.getCell(1).getStringCellValue();
				if (name == null || name.isEmpty())
					break;
				
				FaultSectionData data = sectsNameMap.get(name);
				Preconditions.checkNotNull(data, "Section not found: "+name);
				
				double lon = row.getCell(2).getNumericCellValue();
				double lat = row.getCell(3).getNumericCellValue();
				
				FaultEndPointCategory cat = null;
				
				HSSFCell typeCell = row.getCell(4);
				if (typeCell != null) {
					double typeVal = -1;
					try {
						typeVal = typeCell.getNumericCellValue();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Offending cell: "+i+": "+typeCell.getStringCellValue()+" ("+name+")");
					}
					if (typeVal == 0d)
						cat = FaultEndPointCategory.TERMINATION;
					else if (typeVal == 1d)
						cat = FaultEndPointCategory.CONTINUATION;
					else if (typeVal == 2d)
						cat = FaultEndPointCategory.FORK;
					else
						throw new RuntimeException("Unknown type: "+typeVal+" ("+name+")");
				}
				
				if (cat != null) {
					Location catLoc = new Location(lat, lon);
					FaultTrace trace = data.getFaultTrace();
					
					Preconditions.checkState(!trace.isEmpty(), "Trace is empty!!! ("+name+")");
					Preconditions.checkState(trace.size() > 1, "Trace only has one point!!! ("+name+")");
					
					if (LocationUtils.horzDistance(catLoc, trace.get(0))<horizDist) {
						if (cat != data.getStartPointCategory()) {
							data.setStartPointCategory(cat);
							if (!modifieds.contains(data))
								modifieds.add(data);
						}
					} else if (LocationUtils.horzDistance(catLoc, trace.get(trace.size()-1))<horizDist) {
						if (cat != data.getEndPointCategory()) {
							data.setEndPointCategory(cat);
							if (!modifieds.contains(data))
								modifieds.add(data);
						}
					} else {
						String str = "Category is for point that's not start or end of trace! ("+name+")";
						str += "\nCat loc: "+catLoc;
						str += "\nTrace locs:";
						for (Location loc : trace)
							str += "\n\tCat loc: "+loc;
						throw new RuntimeException(str);
					}
				}
			}
			
			for (FaultSectionData data : modifieds) {
				System.out.println(data.getName()+"\t"+data.getStartPointCategory()+"\t"+data.getEndPointCategory());
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (readDB != null) {
				try {
					readDB.destroy();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if (writeDB != null) {
				try {
					writeDB.destroy();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		System.exit(0);
	}

}
