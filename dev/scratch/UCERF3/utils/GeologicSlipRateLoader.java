package scratch.UCERF3.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.opensha.commons.geo.Location;

public class GeologicSlipRateLoader {
	
	private static String getCellAsString(HSSFCell cell) {
		
		if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
			return cell.getStringCellValue();
		else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
			return ""+cell.getNumericCellValue();
		else if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)
			return null;
		else
			throw new IllegalStateException("cell is neither a string, blank, nor numeric");
	}
	
	public static ArrayList<GeologicSlipRate> loadExcelFile(URL url) throws FileNotFoundException, IOException {
		return loadExcelFile(new BufferedInputStream(url.openStream()));
	}
	
	public static ArrayList<GeologicSlipRate> loadExcelFile(File excelFile) throws FileNotFoundException, IOException {
		return loadExcelFile(new FileInputStream(excelFile));
	}
	public static ArrayList<GeologicSlipRate> loadExcelFile(InputStream is) throws FileNotFoundException, IOException {
		ArrayList<GeologicSlipRate> rates = new ArrayList<GeologicSlipRate>();
		
		POIFSFileSystem fs = new POIFSFileSystem(is);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		wb.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
		HSSFSheet sheet = wb.getSheetAt(0);
		
		int colAdd = 6; // because it starts at column E!
		
		for (int rowInd=2; rowInd<=sheet.getLastRowNum(); rowInd++) {
			HSSFRow row = sheet.getRow(rowInd);
			double lat, lon;
			try {
				String lonStr = GeologicSlipRate.numbersSpacesOnly(getCellAsString(row.getCell(3+colAdd)), true);
				if (lonStr == null || lonStr.length() == 0)
					continue;
				String latStr = GeologicSlipRate.numbersSpacesOnly(getCellAsString(row.getCell(4+colAdd)), true);
				if (latStr == null || lonStr.length() == 0)
					continue;
				lat = Double.parseDouble(latStr);
				lon = Double.parseDouble(lonStr);
			} catch (Exception e) {
				System.out.println("Error parsint location: "+e.getMessage());
				continue;
			}
			Location loc = new Location(lat, lon);
			String valStr = getCellAsString(row.getCell(8+colAdd+2));
			if (valStr == null || valStr.length() == 0) {
				System.out.println("Skipping empty value at loc: "+loc);
				continue;
			}
			
			GeologicSlipRate geo;
			try {
				geo = GeologicSlipRate.fromString(loc, valStr);
			} catch (Exception e) {
				System.out.println("Couldn't parse slip rate: "+valStr+" ("+e.getMessage()+")");
				continue;
			}
			rates.add(geo);
		}
		
		return rates;
	}
	
	public static void main(String args[]) throws FileNotFoundException, IOException {
		ArrayList<GeologicSlipRate> rates =
			loadExcelFile(new File("D:\\Documents\\temp\\UCERF3_Geologic_Slip_Rates_version_1.2.xls"));
	}

}
