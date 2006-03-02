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
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.data.estimate.MinMaxPrefEstimate;
import org.opensha.data.estimate.Estimate;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditCumDisplacement;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditNumEvents;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditSlipRate;
import org.opensha.refFaultParamDb.data.TimeEstimate;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.refFaultParamDb.data.ExactTime;

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
  private final static String MA = "MA";
  private final static String KA = "ka";
  private final static int ZERO_YEAR=1950;
  private String measuredComponent, senseOfMotion;
  private CombinedDisplacementInfo combinedDispInfo;
  private CombinedSlipRateInfo combinedSlipRateInfo;
  private CombinedNumEventsInfo combinedNumEventsInfo;
  private boolean isDisp, isSlipRate, isNumEvents;
  private double min, max, pref;
  private String refSummary;
  private TimeAPI startTime, endTime;
  private String startTimeUnits, endTimeUnits;

  public PutCombinedInfoIntoDatabase() {
    try {
      // read the excel file
      POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(FILE_NAME));
      HSSFWorkbook wb = new HSSFWorkbook(fs);
      HSSFSheet sheet = wb.getSheetAt(0);
      // read data for each row
      for(int r = MIN_ROW; r<=MAX_ROW; ++r) {
        //System.out.println("Processing Row:"+(r+1));
        HSSFRow row = sheet.getRow(r);

        // in case new paleo site needs to be entered into database
        PaleoSite paleoSite = new PaleoSite();
        // combined event info for this site
        CombinedEventsInfo combinedEventsInfo = new CombinedEventsInfo();
        // paleo site publication
        PaleoSitePublication paleoSitePub = new PaleoSitePublication();
        combinedEventsInfo.setPaleoSitePublication(paleoSitePub);
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

        // start time and end time
        startTime = new TimeEstimate();
        endTime = new TimeEstimate();
        try {
          // get value of each column in the row
          for (int c = MIN_COL; c <= MAX_COL; ++c) {
            HSSFCell cell = row.getCell( (short) c);
            String value = null;
            if (cell != null &&
                ! (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)) {
              if(cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
                value = cell.getStringCellValue().trim();
              else value = ""+cell.getNumericCellValue();
            }
            process(c, value, paleoSite, combinedEventsInfo, paleoSitePub);
          }
        }catch(InvalidRowException e) {
          System.out.println("Row "+(r+1)+":"+e.getMessage());
          continue;
        }catch(RuntimeException ex) {
          ex.printStackTrace();
          System.exit(0);
        }

        // set the start and end time
        combinedEventsInfo.setStartTime(this.startTime);
        combinedEventsInfo.setEndTime(this.endTime);

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
        paleoSite.setFaultName(faultDAO.getFault((int)Double.parseDouble(value)).getFaultName());
        break;
      case 2: // TODO, NEO-KINEMA FAULT ID
        break;
      case 3: // NEO-KINEMA Fault name
        break;
      case 4: // Peter Bird Reference category
        break;
      case 5: // fault name
              // no need to migrate as names here differ somewhat from database names
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
        if(value==null) throw new InvalidRowException("Site Longitude is missing");
        paleoSite.setSiteLon1(Float.parseFloat(value));
        paleoSite.setSiteLon2(Float.parseFloat(value));
        break;
      case 9: // Site latitude
        if(value==null) throw new InvalidRowException("Site latitude is missing");
        paleoSite.setSiteLat1(Float.parseFloat(value));
        paleoSite.setSiteLat2(Float.parseFloat(value));
        if(paleoSite.getSiteName().equalsIgnoreCase(""))
          paleoSite.setSiteName(paleoSite.getSiteLat1()+","+paleoSite.getSiteLon1());
        break;
      case 10: // Site Elevation
        if(value!=null)
          paleoSite.setSiteElevation1(Float.parseFloat(value));
         else  paleoSite.setSiteElevation1(Float.NaN);
        break;
      case 11: // reference summary
        refSummary = value;
        break;
      case 12: // reference Id in qfaults
        if(value!=null) paleoSitePub.setReference(referenceDAO.getReferenceByQfaultId((int)Double.parseDouble(value)));
        else paleoSitePub.setReference(addReferenceToDatabase(refSummary));
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
         if(value!=null) {
           Estimate estimate = new MinMaxPrefEstimate(Double.NaN,Double.NaN,Double.parseDouble(value),Double.NaN, Double.NaN, Double.NaN);
           combinedDispInfo.setASeismicSlipFactorEstimateForDisp(new EstimateInstances(estimate, AddEditCumDisplacement.ASEISMIC_SLIP_FACTOR_UNITS));
         }
         break;
      case 18: // preferred displacement
        if(value==null) this.pref = Double.NaN;
        else {
          this.isDisp = true;
          this.pref = Double.parseDouble(value);
        }
        break;
      case 19: // No need to migrate (offset error)
        break;
      case 20: // min displacement
        if(value==null) this.min = Double.NaN;
        else {
          this.isDisp = true;
          this.min = Double.parseDouble(value);
        }
        break;
      case 21: // max displacement
        if(value==null) this.max = Double.NaN;
        else {
          this.isDisp = true;
          this.max = Double.parseDouble(value);
        }
        if(isDisp) {
          Estimate estimate = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
          combinedDispInfo.setDisplacementEstimate(new EstimateInstances(estimate, AddEditCumDisplacement.CUMULATIVE_DISPLACEMENT_UNITS));
        }
        break;
      case 22: // diplacement comments
        if(value==null) value="";
        combinedDispInfo.setDisplacementComments(value);
        break;
      case 23 : // preferred num events
        if(value==null) this.pref = Double.NaN;
        else {
          this.isNumEvents = true;
          this.pref = Double.parseDouble(value);
        }
        break;
      case 24 : //min num events
        if(value==null) this.min = Double.NaN;
        else {
          this.isNumEvents = true;
          this.min = Double.parseDouble(value);
        }
        break;
      case 25: // max num events
        if(value==null) this.max = Double.NaN;
        else {
          this.isNumEvents = true;
          this.max = Double.parseDouble(value);
        }
        if(isNumEvents) {
          Estimate estimate = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
          this.combinedNumEventsInfo.setNumEventsEstimate(new EstimateInstances(estimate, AddEditNumEvents.NUM_EVENTS_UNITS));
        }
        break;
      case 26: // num events comments
        if(value==null) value="";
        this.combinedNumEventsInfo.setNumEventsComments(value);
        break;
      case 27: // timespan comments
        if(value==null) value="";
        combinedEventsInfo.setDatedFeatureComments(combinedEventsInfo.getDatedFeatureComments()+"\n"+value);
        break;
      case 28: // preferred start time
        if(value==null) this.pref = Double.NaN;
        else pref = Double.parseDouble(value);
        break;
      case 29:  // start time units
        if(value!=null) startTimeUnits = value;
        else startTimeUnits="";
        break;
      case 30: // No need to migrate (start time error)
        break;
      case 31: // max start time
        if(value==null) this.max = Double.NaN;
        else max = Double.parseDouble(value);
        break;
      case 32: // min start time
        if(value==null) this.min = Double.NaN;
        else min = Double.parseDouble(value);
        if(Double.isNaN(min) && Double.isNaN(max) && Double.isNaN(pref))
           throw new InvalidRowException("Start Time is missing");
         if(startTimeUnits.equalsIgnoreCase("")) throw new InvalidRowException("Start Time units are missing");
         // if units are MA
        if(startTimeUnits.equalsIgnoreCase(MA)) {
          min = min*1000;
          max=max*1000;
          pref=pref*1000;
          startTimeUnits = KA;
        }
        // swap min/max in case of AD/BC
        if(!startTimeUnits.equalsIgnoreCase(KA)) {
          double temp = min;
          min=max;
          max=temp;
        }

        // set the start time
        Estimate est = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
        if(!startTimeUnits.equalsIgnoreCase(TimeAPI.BC))  startTimeUnits = TimeAPI.AD;
        if(startTimeUnits.equalsIgnoreCase(KA))
          ((TimeEstimate)startTime).setForKaUnits(est, ZERO_YEAR);
        else ((TimeEstimate)startTime).setForCalendarYear(est, startTimeUnits);

        // set reference in start time
        ArrayList refList = new ArrayList();
        refList.add(paleoSitePub.getReference());
        startTime.setReferencesList(refList);
        break;
      case 33: // max end time
        if(value==null) this.max = Double.NaN;
        else max = Double.parseDouble(value);
        break;
      case 34: // pref end time
        if(value==null) this.pref = Double.NaN;
        else pref = Double.parseDouble(value);
        break;
      case 35: // min end time
        if(value==null) this.min = Double.NaN;
        else min  = Double.parseDouble(value);
        break;
      case 36: // end time units
        if(value!=null) endTimeUnits = value;
        else   endTimeUnits="";
        if(Double.isNaN(min) && Double.isNaN(max) && Double.isNaN(pref))
          endTime = new ExactTime(Integer.parseInt(paleoSitePub.getReference().getRefYear()), 0, 0, 0, 0, 0, TimeAPI.AD, true);
        else {
          if(endTimeUnits.equalsIgnoreCase("")) throw new InvalidRowException("End Time units are missing");
          // if units are MA
          if(endTimeUnits.equalsIgnoreCase(MA)) {
            min = min*1000;
            max=max*1000;
            pref=pref*1000;
            endTimeUnits = KA;
          }

          // swap min/max in case of AD/BC
          if(!endTimeUnits.equalsIgnoreCase(KA)) {
            double temp = min;
            min=max;
            max=temp;
          }


          // set the end time
          Estimate endTimeEst = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
          if(!endTimeUnits.equalsIgnoreCase(TimeAPI.BC))  endTimeUnits = TimeAPI.AD;
          if(endTimeUnits.equalsIgnoreCase(KA))
            ((TimeEstimate)endTime).setForKaUnits(endTimeEst, ZERO_YEAR);
          else ((TimeEstimate)endTime).setForCalendarYear(endTimeEst, endTimeUnits);
        }
        // set reference in start time
        ArrayList refList1 = new ArrayList();
        refList1.add(paleoSitePub.getReference());
        endTime.setReferencesList(refList1);
        break;
      case 37: // dated feature comments
        if(value==null) value ="";
        combinedEventsInfo.setDatedFeatureComments(combinedEventsInfo.getDatedFeatureComments()+"\n"+value);
        break;
      case 38: // aseismic slip factor for Slip Rate
        if(value!=null) {
          Estimate estimate = new MinMaxPrefEstimate(Double.NaN,Double.NaN,Double.parseDouble(value),Double.NaN, Double.NaN, Double.NaN);
          combinedSlipRateInfo.setASeismicSlipFactorEstimateForSlip(new EstimateInstances(estimate, AddEditSlipRate.ASEISMIC_SLIP_FACTOR_UNITS));
        }
        break;
      case 39: // preferred slip rate
        if(value==null) this.pref = Double.NaN;
        else {
          this.isSlipRate = true;
          this.pref = Double.parseDouble(value);
        }
        break;
      case 40: // no need to migrate (slip rate error)
        break;
      case 41: // min slip rate
        if(value==null) this.min = Double.NaN;
        else {
          this.isSlipRate = true;
          this.min = Double.parseDouble(value);
        }
        break;
      case 42: // max slip rate
        if(value==null) this.max = Double.NaN;
        else {
          this.isSlipRate = true;
          this.max = Double.parseDouble(value);
        }
        if(isSlipRate) {
         Estimate estimate = new MinMaxPrefEstimate(min,max,pref,Double.NaN, Double.NaN, Double.NaN);
         this.combinedSlipRateInfo.setSlipRateEstimate(new EstimateInstances(estimate, AddEditSlipRate.SLIP_RATE_UNITS));
       }
        break;
      case 43: // slip rate comments
        if(value==null) value="";
        this.combinedSlipRateInfo.setSlipRateComments(value);
        break;
    }
  }

  /**
   * Add reference to the database
   *
   * @param referenceSummary
   * @return
   */
  private Reference addReferenceToDatabase(String referenceSummary) {
    Reference ref = new Reference();
    ref.setFullBiblioReference("");
    int index = referenceSummary.indexOf("(");
    ref.setRefAuth(referenceSummary.substring(0,index));
    ref.setRefYear(referenceSummary.substring(index+1,referenceSummary.indexOf(")")));
    //int id = this.referenceDAO.addReference(ref);
    int id=-1;
    ref.setReferenceId(id);
    return ref;
  }

  public static void main(String[] args) {
    PutCombinedInfoIntoDatabase putCombinedInfoIntoDatabase1 = new PutCombinedInfoIntoDatabase();
    //System.out.println(Integer.parseInt("5760"));
  }

}

class InvalidRowException extends RuntimeException {
  public InvalidRowException(String msg) {
    super(msg);
  }
}