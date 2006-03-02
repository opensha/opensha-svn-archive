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
import org.opensha.refFaultParamDb.vo.PaleoSitePublication;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.CombinedDisplacementInfo;
import org.opensha.refFaultParamDb.vo.CombinedSlipRateInfo;
import org.opensha.refFaultParamDb.vo.CombinedNumEventsInfo;

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
  private final static String UNKNOWN = "Unknown";
  private String measuredComponent, senseOfMotion;
  private CombinedDisplacementInfo combinedDispInfo;
  private CombinedSlipRateInfo combinedSlipRateInfo;
  private CombinedNumEventsInfo combinedNumEventsInfo;
  private boolean isDisp, isSlipRate, isNumEvents;

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
        // paleo site publication
        PaleoSitePublication paleoSitePub = new PaleoSitePublication();
        // site types
        ArrayList siteTypeNames = new ArrayList();
        siteTypeNames.add(UNKNOWN);
        paleoSitePub.setSiteTypeNames(siteTypeNames);


        // make objects of displacement, slip rate as well as num events
        combinedDispInfo = new CombinedDisplacementInfo();
        combinedSlipRateInfo = new CombinedSlipRateInfo();
        combinedNumEventsInfo = new CombinedNumEventsInfo();
        isDisp=false;
        isSlipRate=false;
        isNumEvents=false;

        // get value of each column in the row
        for(int c=MIN_COL; c<=MAX_COL; ++c) {
          HSSFCell cell = row.getCell( (short) c);
          System.out.println(c);
          String value=null;
          if(cell!=null && !(cell.getCellType()==HSSFCell.CELL_TYPE_BLANK))
            value = cell.getStringCellValue().trim();
          process(c, value, paleoSite, combinedEventsInfo, paleoSitePub);
        }

        // set displacement in combined events info
        if(isDisp) {
          combinedDispInfo.setSenseOfMotionQual(this.senseOfMotion);
          combinedDispInfo.setMeasuredComponentQual(this.measuredComponent);
          combinedEventsInfo.setCombinedDisplacementInfo(combinedDispInfo);
        }

        // set slip rate in combined events info
        if(isSlipRate) {
          combinedSlipRateInfo.setSenseOfMotionQual(this.senseOfMotion);
          combinedSlipRateInfo.setMeasuredComponentQual(this.measuredComponent);
          combinedEventsInfo.setCombinedSlipRateInfo(combinedSlipRateInfo);
        }

        // set num events in combined events info
        if(isNumEvents) combinedEventsInfo.setCombinedNumEventsInfo(combinedNumEventsInfo);

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
                       CombinedEventsInfo combinedEventsInfo,
                       PaleoSitePublication paleoSitePub) {
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
        paleoSite.setOldSiteId(value);
        break;
      case 7: // site name
        // if site name starts with "per", then we will set its name as lat,lon
        if(value.startsWith("per")) value="";
        paleoSite.setSiteName(value);
        break;
      case 8: // Site longitude
        paleoSite.setSiteLon1(Float.parseFloat(value));
        break;
      case 9: // Site latitude
        paleoSite.setSiteLat1(Float.parseFloat(value));
        break;
      case 10: // Site Elevation
        if(value!=null)
          paleoSite.setSiteElevation1(Float.parseFloat(value));
         else  paleoSite.setSiteElevation1(Float.NaN);
        break;
      case 11: // reference summary
        break;
      case 12: // reference Id in qfaults
        if(value!=null) paleoSitePub.setReference(referenceDAO.getReferenceByQfaultId(Integer.parseInt(value)));
        break;
      case 13: // combined info comments
        if(value==null) value="";
        combinedEventsInfo.setDatedFeatureComments(value);
        break;
      case 14: // representative strand name
        if(value==null) value = UNKNOWN;
        paleoSitePub.setRepresentativeStrandName(value);
        break;
      case 15: // measured component
        if(value==null) value=this.UNKNOWN;
        this.measuredComponent = value;
        break;
      case 16: // sense of motion
        if(value==null) value=this.UNKNOWN;
        this.senseOfMotion = value;
        break;
      case 17: //aseismic slip factor for displacement
         // No need to handle this at this time as this needs to be an estimate
         break;
      case 18: // preferred displacement
        break;
      case 19: // No need to migrate (offset error)
        break;
      case 20: // min displacement
        break;
      case 21: // max displacement
        break;
      case 22: // diplacement comments
        break;
      case 23 : // preferred num events
        break;
      case 24 : //min num events
        break;
      case 25: // max num events
        break;
      case 26: // num events comments
        break;
      case 27: // timespan comments
        break;
      case 28: // preffered start time
        break;
      case 29:  // start time units (NEED to handle MA here)
        break;
      case 30: // No need to migrate (start time error)
        break;
      case 31: // max start time (NEED to handle for earliest)
        break;
      case 32: // min start time
        break;
      case 33: // max end time
        break;
      case 34: // pref end time
        break;
      case 35: // min end time
        break;
      case 36: // end time units
        break;
      case 37: // dated feature comments
        break;
      case 38: // aseismic slip factor for Slip Rate
         // No need to handle this at this time as this needs to be an estimate
        break;
      case 39: // preferred slip rate
        break;
      case 40: // no need to migrate (slip rate error)
        break;
      case 41: // min slip rate
        break;
      case 42: // max slip rate
        break;
      case 43: // slip rate comments
        break;
    }
  }

  public static void main(String[] args) {
    PutCombinedInfoIntoDatabase putCombinedInfoIntoDatabase1 = new PutCombinedInfoIntoDatabase();
  }

}