package org.opensha.refFaultParamDb.deformationModelers;

import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.dao.db.*;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.vo.PaleoSitePublication;
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.vo.CombinedEventsInfo;
import org.opensha.refFaultParamDb.vo.CombinedSlipRateInfo;
import java.io.FileWriter;
import java.io.IOException;
import org.opensha.refFaultParamDb.vo.Fault;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.refFaultParamDb.data.ExactTime;
import org.opensha.refFaultParamDb.data.TimeEstimate;
import org.opensha.refFaultParamDb.vo.CombinedDisplacementInfo;

/**
 * <p>Title: GenerateFileFromDatabase.java </p>
 * <p>Description: This class reads the data from the database and generates a
 * file which can be used by Deformation Modelers. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class GenerateFileFromDatabase {
  /** Data Access objects (DAO) to retrieve data from database */
  private PaleoSiteDB_DAO paleoSiteDAO = new PaleoSiteDB_DAO(DB_AccessAPI.dbConnection);
  private CombinedEventsInfoDB_DAO combinedEventsInfoDAO = new CombinedEventsInfoDB_DAO(DB_AccessAPI.dbConnection);
  private FaultDB_DAO faultDAO = new FaultDB_DAO(DB_AccessAPI.dbConnection);
  private final static String OUT_FILENAME = "PaleoSiteData.txt";
  private final static String UNKNOWN = "Unknown";
  private final static String ESTIMATE = "Estimate";
  private final static String EXACT = "Exact";
  private final static String NOW = "Now";
  private final static String KA = "ka";

  public GenerateFileFromDatabase() {
    try {
      FileWriter fw = new FileWriter(OUT_FILENAME);
      ArrayList paleoSitesList = paleoSiteDAO.getAllPaleoSites();
      // loop over all the available sites
      for (int i = 0; i < paleoSitesList.size(); ++i) {
        PaleoSite paleoSite = (PaleoSite) paleoSitesList.get(i);
        System.out.println(i+". "+paleoSite.getSiteName());
        int faultId =  this.faultDAO.getFault(paleoSite.getFaultName()).getFaultId();
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

            // WRITE SLIP RATE INFO
            CombinedSlipRateInfo combinedSlipRateInfo = combinedEventsInfo.
                getCombinedSlipRateInfo();
            if (combinedSlipRateInfo != null) {
              writeSiteAndTimeSpanInfo(fw, paleoSite, faultId, paleoSitePub, combinedEventsInfo);
              // write slip rate info
              writeSlipAndDisplacementInfo(fw, "Slip Rate", combinedSlipRateInfo.getSlipRateEstimate(),
                                           combinedSlipRateInfo.getMeasuredComponentQual(),
                                           combinedSlipRateInfo.getSenseOfMotionQual(),
                                           combinedSlipRateInfo.getSenseOfMotionRake(),
                                           combinedSlipRateInfo.getASeismicSlipFactorEstimateForSlip());;
              // write comments
              writeComments(fw, paleoSite.getGeneralComments(),
                            combinedEventsInfo.getDatedFeatureComments(),
                            combinedSlipRateInfo.getSlipRateComments(), "Slip Rate Comments");
            }

            // WRITE CUMULATIVE DISPLACEMENT INFO
            CombinedDisplacementInfo combinedDisplacementInfo = combinedEventsInfo.getCombinedDisplacementInfo();
            if(combinedDisplacementInfo!=null) {
              writeSiteAndTimeSpanInfo(fw, paleoSite, faultId, paleoSitePub, combinedEventsInfo);
              // write displacement info
              writeSlipAndDisplacementInfo(fw, "Cumulative Displacement", combinedDisplacementInfo.getDisplacementEstimate(),
                                           combinedDisplacementInfo.getMeasuredComponentQual(),
                                           combinedDisplacementInfo.getSenseOfMotionQual(),
                                           combinedDisplacementInfo.getSenseOfMotionRake(),
                                           combinedDisplacementInfo.getASeismicSlipFactorEstimateForDisp());

              // write comments
              writeComments(fw, paleoSite.getGeneralComments(),
                            combinedEventsInfo.getDatedFeatureComments(),
                            combinedDisplacementInfo.getDisplacementComments(), "Cumulative Displacement Comments");

            }
          }
        }
      }
      fw.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Write comments
   * @param fw
   * @param generalComments
   * @param datedFeatureComments
   * @param comments
   * @param label
   */
  private void writeComments(FileWriter fw, String generalComments,
                             String datedFeatureComments, String comments,
                             String label) throws IOException {
    fw.write("General Comments="+generalComments+"\n");
    fw.write("Dating Methodology Comments="+datedFeatureComments+"\n");
    fw.write(label+"="+comments+"\n");
  }

  /**
   * Write slip rate/displacement info to thef file
   * @param fw
   * @param label
   * @param slipOrDispInstance
   * @param measuredComp
   * @param senseOfMotionQual
   * @param rakeInstance
   * @param aseismicSlipFactorInstance
   * @throws IOException
   */
  private void writeSlipAndDisplacementInfo(FileWriter fw, String label,
      EstimateInstances slipOrDispInstance, String measuredComp,
      String senseOfMotionQual, EstimateInstances rakeInstance,
      EstimateInstances aseismicSlipFactorInstance) throws IOException {
    fw.write(label+"="+ESTIMATE+"\n");
    fw.write(slipOrDispInstance.getEstimate().toString()+"\n");
    fw.write(label+" Units="+slipOrDispInstance.getUnits()+"\n");
    fw.write("Measured Component of Slip ="+measuredComp+"\n");
    fw.write(label+" Qualitative Sense of Motion="+senseOfMotionQual+"\n");
    // rake
    if(rakeInstance==null) // if rake in unavailable
      fw.write(label+" Rake (degrees, per Aki & Richards convention)="+UNKNOWN+"\n");
    else  { // if rake estimate is present
      fw.write(label+" Rake (degrees, per Aki & Richards convention)=" +
               ESTIMATE + "\n");
      fw.write(rakeInstance.getEstimate().toString()+"\n");
    }
    // aseismic slip factor
    if(aseismicSlipFactorInstance==null) // if rake in unavailable
      fw.write("Aseismic Slip Factor="+UNKNOWN+"\n");
    else  { // if rake estimate is present
      fw.write("Aseismic Slip Factor=" +
               ESTIMATE + "\n");
      fw.write(aseismicSlipFactorInstance.getEstimate().toString()+"\n");
    }

  }


  /**
   * Write site characteristics and start and end time to the file
   *
   * @param fw
   * @param paleoSite
   * @param faultId
   * @param paleoSitePub
   * @param combinedEventsInfo
   * @throws IOException
   */
  private void writeSiteAndTimeSpanInfo(FileWriter fw, PaleoSite paleoSite,
                                        int faultId,
                                        PaleoSitePublication paleoSitePub,
                                        CombinedEventsInfo combinedEventsInfo) throws
      IOException {
    writeEntryDate(fw, combinedEventsInfo.getEntryDate()); //write entry date
    // whether it is expert opinion or per publication
    writeTypeOfContribution(fw, combinedEventsInfo.getIsExpertOpinion());
    // write reference information
    writeReferenceInfo(fw, combinedEventsInfo.getReferenceList());
    // write site characteristics(lat/lon/elevation/siteid/name/faultid/name/dip/representative strand index)
    writeSiteCharacteristics(fw, paleoSite, paleoSitePub, faultId);
    //write the start time
    writeTime(fw, combinedEventsInfo.getStartTime(),"Start Time");
    // write the end time
    writeTime(fw, combinedEventsInfo.getEndTime(), "End Time");
  }

  /**
   * Write the start/end time for the data
   * @param time
   */
  private void writeTime(FileWriter fw, TimeAPI time, String label) throws IOException {
    if(time instanceof ExactTime) {
      writeExactTime(fw, (ExactTime)time, label);
    } else {
      writeTimeEstimate(fw, (TimeEstimate)time, label);
    }
  }

  /**
   * Write exact time in the file
   *
   * @param fw
   * @param exactTime
   * @param label
   * @throws IOException
   */
  private void writeExactTime(FileWriter fw, ExactTime exactTime, String label) throws IOException {
    if(exactTime.getIsNow()) { // now
      fw.write(label+"="+NOW+"\n");
    } else { // exact time
      fw.write(label+"="+EXACT+"\n");
      fw.write("Year="+exactTime.getYear()+"\n");
      fw.write("Era="+exactTime.getEra()+"\n");
      fw.write("Month="+exactTime.getMonth()+"\n");
      fw.write("Day="+exactTime.getDay()+"\n");
      fw.write("Hour="+exactTime.getHour()+"\n");
      fw.write("Minute="+exactTime.getMinute()+"\n");
      fw.write("Second="+exactTime.getSecond()+"\n");
    }
  }

  /**
   * Write time estimate
   *
   * @param fw
   * @param timeEstimate
   * @param label
   * @throws IOException
   */
  private void writeTimeEstimate(FileWriter fw, TimeEstimate timeEstimate, String label) throws IOException {
    fw.write(label+"="+ESTIMATE+"\n");
    fw.write(timeEstimate.getEstimate().toString()+"\n");
    String units = timeEstimate.getEra();
    if(timeEstimate.isKaSelected()) units=KA;
    fw.write(label+" units="+units+"\n");
    if(units.equalsIgnoreCase(KA)) fw.write(label+" Zero Year="+timeEstimate.getZeroYear()+"\n");
  }

  /**
   * Write entry date for this data
   * @param fw
   * @param entryDate
   * @throws IOException
   */
  private void writeEntryDate(FileWriter fw, String entryDate) throws IOException {
    fw.write("#Entry Date="+entryDate+"\n");
  }

  /**
   * Whether this is expert opinion or per publication data
   * @param fw
   * @param isExpert
   * @throws IOException
   */
  private void writeTypeOfContribution(FileWriter fw, boolean isExpert) throws IOException {
    if(isExpert) fw.write("Type of Contribution=E\n");
    else fw.write("Type of Contribution=P\n");
  }

  /**
   * Write the reference info to the file
   * @param references
   */
  private void writeReferenceInfo(FileWriter fw, ArrayList references) throws IOException {
    fw.write("Number of References="+references.size()+"\n");
    for(int i=0; i<references.size(); ++i) {
      Reference reference = (Reference)references.get(i);
      fw.write("Ref ID "+i+"="+reference.getReferenceId()+"\n");
      fw.write("Short Citation "+i+"="+reference.getSummary()+"\n");
      fw.write("Full Citation "+i+"="+reference.getFullBiblioReference()+"\n");
    }
  }

  /**
   * Write site characteristics to a file
   * @param fw
   * @param paleoSite
   * @throws IOException
   */
  private void writeSiteCharacteristics(FileWriter fw, PaleoSite paleoSite,
                                        PaleoSitePublication paleoSitePub, int faultId) throws IOException {
    fw.write("Representativeness of Site="+paleoSitePub.getRepresentativeStrandName()+"\n");
    fw.write("Site Id="+paleoSite.getSiteId()+"\n");
    fw.write("Site Name="+paleoSite.getSiteName()+"\n");
    fw.write("Fault Id="+faultId+"\n");
    fw.write("Fault Name="+paleoSite.getFaultName()+"\n");
    fw.write("Site Lat(degrees)="+paleoSite.getSiteLat1()+"\n");
    fw.write("Site Lon(degrees)="+paleoSite.getSiteLon1()+"\n");
    float elevation1 = paleoSite.getSiteElevation1();
    // check whether elevation is available or not
    if(Float.isNaN(elevation1)) fw.write("Site Elevation(m)="+UNKNOWN+"\n");
    else fw.write("Site Elevation(m)="+elevation1+"\n");
    //check that dip is available or not
    EstimateInstances dipEstInstance  = paleoSite.getDipEstimate();
    if(dipEstInstance==null) fw.write("Fault Dip(as measured at this site)="+UNKNOWN+"\n");
    else fw.write("Fault Dip(as measured at this site)="+ESTIMATE+"\n"+
                  dipEstInstance.toString());

  }



  public static void main(String[] args) {
    // set the username and password to access the database
    SessionInfo.setUserName(args[0]);
    SessionInfo.setPassword(args[1]);
    SessionInfo.setContributorInfo();
    new GenerateFileFromDatabase();
    System.exit(0);
  }

}