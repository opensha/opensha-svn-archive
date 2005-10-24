package org.opensha.refFaultParamDb.excelToDatabase;

import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.dao.db.FaultSection2002DB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.FaultSection2002;
import java.io.FileWriter;

/**
 * <p>Title: Read2002FaultSections.java </p>
 * <p>Description: Read the 2002 fault sections from the database and save
 * in a file to see that the info is correct. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Read2002FaultSections {
  private FaultSection2002DB_DAO faultSection2002DAO = new FaultSection2002DB_DAO(DB_AccessAPI.dbConnection);
  private final static String OUT_FILENAME = "FaultSections2002.txt";
  public Read2002FaultSections() {
    ArrayList faultSections  = faultSection2002DAO.getAllFaultSections();
    try {
      FileWriter fw = new FileWriter(OUT_FILENAME);
      for (int i = 0; i < faultSections.size(); ++i) {
        FaultSection2002 faultSection = (FaultSection2002) faultSections.get(i);
        fw.write("Section Name=" + faultSection.getSectionName() + "\n");
        fw.write("\tFaultId=" + faultSection.getFaultId() +
                 ",SectionId=" + faultSection.getSectionId() +
                 ",NSHM02ID=" + faultSection.getNshm02Id() +
                 ", faultModel=" + faultSection.getFaultModel() + "\n");
        fw.write("\tDip=" + faultSection.getAveDip() +
                 ",Avg LT Slip Rate=" + faultSection.getAveLongTermSlipRate() +
                 ", Avg Upper Seis Depth=" + faultSection.getAveUpperSeisDepth() +
                 ", Avg Lower Seis Depth=" + faultSection.getAveLowerSeisDepth() +
                 "\n");
        fw.write("\tComments=" + faultSection.getComments() + ",entryDate=" +
                 faultSection.getEntryDate() + "\n");
        /*fw.write("\tFault Trace=" + faultSection.getFaultTrace().toString() +
                 "\n\n\n");*/
      }
      fw.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }
  public static void main(String[] args) {
    SessionInfo.setUserName(args[0]);
    SessionInfo.setPassword(args[1]);
    SessionInfo.setContributorInfo();
    Read2002FaultSections read2002FaultSections1 = new Read2002FaultSections();
  }

}