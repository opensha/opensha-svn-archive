package org.opensha.refFaultParamDb.excelToDatabase;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.refFaultParamDb.dao.db.CombinedEventsInfoDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PaleoSiteDB_DAO;
import org.opensha.refFaultParamDb.vo.CombinedEventsInfo;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.vo.PaleoSitePublication;
import org.opensha.refFaultParamDb.vo.Reference;

/**
 * This class creates spreadsheet which will be given to deformation modelers
 * 
 * @author vipingupta
 *
 */
public class FileForDeformationModelers {
	private final static int MIN_COL = 0;
	private final static int MAX_COL = 50;
	private final static String OUT_FILE_NAME = "ForDeformationModelers.xls";
	// DAO to get information from the database
	private PaleoSiteDB_DAO paleoSiteDAO = new PaleoSiteDB_DAO(DB_AccessAPI.dbConnection);
	private CombinedEventsInfoDB_DAO combinedEventsInfoDAO = new CombinedEventsInfoDB_DAO(DB_AccessAPI.dbConnection);
	private FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	
	public static void main(String[] args) {
		FileForDeformationModelers dmFile = new FileForDeformationModelers();
		dmFile.generateFile();
	}
	
	public void generateFile() {
		// write to the  the excel file
		try {
			HSSFWorkbook wb  = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet();
			writeHeader(sheet);
		
	  	 // Get all paleo sites from the database
	  	/*ArrayList paleoSitesList = paleoSiteDAO.getAllPaleoSites();
	  	// loop over all the available sites
	  	int row = 1;
	  	for (int i = 0; i < paleoSitesList.size(); ++i) {
	  		PaleoSite paleoSite = (PaleoSite) paleoSitesList.get(i);
	  		System.out.println(i+". "+paleoSite.getSiteName());
	  		ArrayList paleoSitePublicationList = paleoSite.getPaleoSitePubList();
	  		// loop over all the reference publications for that site
	  		for (int j = 0; j < paleoSitePublicationList.size(); ++j) {
	  			PaleoSitePublication paleoSitePub = (PaleoSitePublication)
	  			paleoSitePublicationList.get(j);
	  			Reference reference = paleoSitePub.getReference();
	  			// get combined events info for that site and reference
	  			ArrayList combinedInfoList = combinedEventsInfoDAO.
	  			getCombinedEventsInfoList(paleoSite.getSiteId(),
	                                        reference.getReferenceId());
	  			for (int k = 0; k < combinedInfoList.size(); ++k) {
	  				CombinedEventsInfo combinedEventsInfo = (CombinedEventsInfo)
	  				combinedInfoList.get(k);
	  	
	  				HSSFRow row = sheet.getRow(r);
	  				for (int c = MIN_COL; c <= MAX_COL; ++c) {
	  					HSSFCell cell = row.getCell( (short) c);
	  				}
	  			}
	  		}
	  	}*/
			FileOutputStream fileOut = new FileOutputStream(OUT_FILE_NAME);
			wb.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Write header for the excel sheet
	 *
	 */
	 private void writeHeader(HSSFSheet sheet) {
		 HSSFRow row = sheet.createRow((short)0);
		 int col=0;
		 row.createCell((short)col++).setCellValue("Entry Date");
		 row.createCell((short)col++).setCellValue("Entry Type");
		 row.createCell((short)col++).setCellValue("WG_Fault_ID");
		 row.createCell((short)col++).setCellValue("alternates?");
		 row.createCell((short)col++).setCellValue("Fault_Name");
		 row.createCell((short)col++).setCellValue("Bird Fault ID-RefCat");
		 row.createCell((short)col++).setCellValue("Temp Site Id");
		 row.createCell((short)col++).setCellValue("Site Name");
		 row.createCell((short)col++).setCellValue("Site Type");
		 row.createCell((short)col++).setCellValue("Data Source");
		 row.createCell((short)col++).setCellValue("Site_Long");
		 row.createCell((short)col++).setCellValue("Site_Lat");
		 row.createCell((short)col++).setCellValue("Site_Elev");
		 row.createCell((short)col++).setCellValue("Site Long 2");
		 row.createCell((short)col++).setCellValue("Site Lat 2");
		 row.createCell((short)col++).setCellValue("Site Elev 2");
		 row.createCell((short)col++).setCellValue("HOW REPRESENTATIVE IS SITE?");
		 row.createCell((short)col++).setCellValue("SHORT CITATION");
		 row.createCell((short)col++).setCellValue("QFAULTS REF ID");
		 row.createCell((short)col++).setCellValue("WG REF ID");
		 row.createCell((short)col++).setCellValue("MEASURED COMPONENT OF SLIP");
		 row.createCell((short)col++).setCellValue("SENSE OF MOTION");
		 row.createCell((short)col++).setCellValue("START TIME UNITS");
		 row.createCell((short)col++).setCellValue("PREF START TIME");
		 row.createCell((short)col++).setCellValue("MAX START TIME(earliest)");
		 row.createCell((short)col++).setCellValue("MIN START TIME");
		 row.createCell((short)col++).setCellValue("END TIME UNITS");
		 row.createCell((short)col++).setCellValue("PREF END TIME");
		 row.createCell((short)col++).setCellValue("MAX END TIME (earliest)");
		 row.createCell((short)col++).setCellValue("MIN END TIME");
		 row.createCell((short)col++).setCellValue("SR: Aseismic Slip Est Pref");
		 row.createCell((short)col++).setCellValue("SR: Aseismic Slip Est Max");
		 row.createCell((short)col++).setCellValue("SR: Aseismic Slip Est Min");
		 row.createCell((short)col++).setCellValue("PREF SLIP RATE (mm/yr)");
		 row.createCell((short)col++).setCellValue("MAX SLIP RATE");
		 row.createCell((short)col++).setCellValue("MIN SLIP RATE");
		 row.createCell((short)col++).setCellValue("Offset: Aseismic Slip Est Pref");
		 row.createCell((short)col++).setCellValue("Offset: Aseismic Slip Est Max");
		 row.createCell((short)col++).setCellValue("Offset: Aseismic Slip Est Min");
		 row.createCell((short)col++).setCellValue("PREF OFFSET (m)");
		 row.createCell((short)col++).setCellValue("MAX OFFSET");
		 row.createCell((short)col++).setCellValue("MIN OFFSET");
		 row.createCell((short)col++).setCellValue("PREF NUM EVENTS");
		 row.createCell((short)col++).setCellValue("MAX NUM EVENTS");
		 row.createCell((short)col++).setCellValue("MIN NUM EVENTS");
		 row.createCell((short)col++).setCellValue("NUM EVENTS COMMENTS");
		 row.createCell((short)col++).setCellValue("TIME AND DATING COMMENTS");
		 row.createCell((short)col++).setCellValue("SLIP RATE COMMENTS");
		 row.createCell((short)col++).setCellValue("SLIP RATE COMMENTS");
		 row.createCell((short)col++).setCellValue("OFFSET COMMENTS");
		 row.createCell((short)col++).setCellValue("GENERAL COMMENTS");
	 }
}
