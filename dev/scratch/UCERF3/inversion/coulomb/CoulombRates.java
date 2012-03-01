package scratch.UCERF3.inversion.coulomb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.UCERF3_DataUtils;

import com.google.common.base.Preconditions;

public class CoulombRates extends HashMap<IDPairing, CoulombRatesRecord> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static HashMap<FaultModels, String> modelDataFilesMap;
	
	static {
		modelDataFilesMap = new HashMap<FaultModels, String>();
		modelDataFilesMap.put(FaultModels.FM3_1, "Stress_Table_FM3_1_2601_v2.xls");
	}
	
	private static final String DATA_SUB_DIR = "coulomb";

	private CoulombRates() {
		// private so that it can only be instantiated with the from data file methods
	}
	
	/**
	 * This is a simple test to make sure that coulomb data exists for each
	 * of the given pairings. Checks for data in both directions
	 * 
	 * @param prefData
	 * @return
	 */
	public boolean isApplicableTo(Collection<IDPairing> pairings) {
		for (IDPairing pairing : pairings)
			if (!containsKey(pairing) || !containsKey(pairing.getReversed()))
				return false;
		return true;
	}
	
	public static CoulombRates loadUCERF3CoulombRates(FaultModels faultModel) throws IOException {
		String fileName = modelDataFilesMap.get(faultModel);
		Preconditions.checkNotNull(fileName, "No coulomb file exists for the given fault model: "+faultModel);
		return loadExcelFile(UCERF3_DataUtils.locateResourceAsStream(DATA_SUB_DIR, fileName));
	}
	
	public static CoulombRates loadExcelFile(InputStream is) throws IOException {
		CoulombRates rates = new CoulombRates();
		
		POIFSFileSystem fs = new POIFSFileSystem(is);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		int lastRowIndex = sheet.getLastRowNum();
		int id1, id2;
		double ds, pds, dcff, pdcff;
		for(int r=1; r<=lastRowIndex; ++r) {
//			System.out.println("Coulomb row: "+r);
			HSSFRow row = sheet.getRow(r);
			if(row==null) continue;
			int cellNum = row.getFirstCellNum();
			if (cellNum < 0)
				continue;
			HSSFCell id1_cell = row.getCell(cellNum++);
			if(id1_cell==null || id1_cell.getCellType()!=HSSFCell.CELL_TYPE_NUMERIC)
				continue;
			id1 = (int)id1_cell.getNumericCellValue();
			id2 = (int)row.getCell(cellNum++).getNumericCellValue();
			ds = row.getCell(cellNum++).getNumericCellValue();
			dcff = row.getCell(cellNum++).getNumericCellValue();
			pds = row.getCell(cellNum++).getNumericCellValue();
			pdcff = row.getCell(cellNum++).getNumericCellValue();
			
			IDPairing pairing = new IDPairing(id1, id2);
			rates.put(pairing, new CoulombRatesRecord(pairing, ds, pds, dcff, pdcff));
		}
		
		return rates;
	}
	
	public static void main(String[] args) throws IOException {
		CoulombRates rates = loadUCERF3CoulombRates(FaultModels.FM3_1);
		
		IDPairing pairing = new IDPairing(621, 1759);
		
		System.out.println(rates.get(pairing));
		System.out.println(rates.get(pairing.getReversed()));
	}

}
