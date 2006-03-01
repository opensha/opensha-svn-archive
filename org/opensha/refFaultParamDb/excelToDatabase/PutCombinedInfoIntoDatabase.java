package org.opensha.refFaultParamDb.excelToDatabase;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import java.io.FileInputStream;
import org.opensha.refFaultParamDb.dao.db.PaleoSiteDB_DAO;
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.vo.CombinedEventsInfo;
import org.opensha.refFaultParamDb.dao.db.FaultDB_DAO;

/**
 * <p>Title: PutCombinedInfoIntoDatabase.java </p>
 * <p>Description: It reads the excel file and puts combined events into the
 * oracle database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PutCombinedInfoIntoDatabase {
  private final static String FILE_NAME = "AllColumns_MMPref_SR_CumDispl.xls";
  // rows (number of records) in this excel file. First 2 rows are neglected as they have header info
  private final static int MIN_ROW = 2;
  private final static int MAX_ROW = 108;
  // columns in this excel file
  private final static int MIN_COL = 1;
  private final static int MAX_COL = 44;
  private PaleoSiteDB_DAO paleoSiteDAO = new PaleoSiteDB_DAO(DB_AccessAPI.dbConnection);
  private ReferenceDB_DAO referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
  private FaultDB_DAO faultDAO = new FaultDB_DAO(DB_AccessAPI.dbConnection);

  public PutCombinedInfoIntoDatabase() {
    try {
      // read the excel file
      POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(FILE_NAME));
      HSSFWorkbook wb = new HSSFWorkbook(fs);
      HSSFSheet sheet = wb.getSheetAt(0);
      // read data for each row
      for(int r = MIN_ROW; r<=MAX_ROW; ++r) {
        HSSFRow row = sheet.getRow(r);

        // in case new paleo site needs to be entered into database
        PaleoSite paleoSite = new PaleoSite();
        // combined event info for this site
        CombinedEventsInfo combinedEventsInfo = new CombinedEventsInfo();

        // get value of each column in the row
        for(int c=MIN_COL; c<=MAX_COL; ++c) {
          HSSFCell cell = row.getCell( (short) c);
          System.out.println(c);
          String value=null;
          if(cell!=null && !(cell.getCellType()==HSSFCell.CELL_TYPE_BLANK))
            value = cell.getStringCellValue();
          process(c, value, paleoSite, combinedEventsInfo);
        }
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Process the excel sheet according to the specific column number
   *
   * @param columnNumber
   * @param value
   * @param paleoSite
   * @param combinedEventsInfo
   */
  private void process(int columnNumber, String value, PaleoSite paleoSite,
                       CombinedEventsInfo combinedEventsInfo) {
    switch (columnNumber) {
      case 1: // fault Id
        paleoSite.setFaultName(faultDAO.getFault(Integer.parseInt(value)).getFaultName());
        break;
      case 2: // TODO, NEO-KINEMA FAULT ID
        break;
      case 3: // NEO-KINEMA Fault name
        break;
      case 4: // Peter Bird Reference category
        break;
      case 5: // fault name
        break;
      case 6: // qfault Site-Id
        paleoSite.setOldSiteId(value.trim());
        break;
      case 7: // site name
        paleoSite.setSiteName(value.trim());
        break;
      case 8: // Site longitude
        paleoSite.setSiteLon1(Float.parseFloat(value.trim()));
        break;
      case 9: // Site latitude
        paleoSite.setSiteLat1(Float.parseFloat(value.trim()));
        break;
      case 10: // Site Elevation
        if(value!=null)
          paleoSite.setSiteElevation1(Float.parseFloat(value.trim()));
         else  paleoSite.setSiteElevation1(Float.NaN);
        break;
      case 11:
        break;
    }
  }

  public static void main(String[] args) {
    PutCombinedInfoIntoDatabase putCombinedInfoIntoDatabase1 = new PutCombinedInfoIntoDatabase();
  }

}