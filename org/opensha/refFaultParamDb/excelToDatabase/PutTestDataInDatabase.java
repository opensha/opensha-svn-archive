package org.opensha.refFaultParamDb.excelToDatabase;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;

/**
 * <p>Title: PutTestDataInDatabase.java </p>
 * <p>Description: Put the test data in  the database </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PutTestDataInDatabase {
  public PutTestDataInDatabase() {
  }

  public static void main(String[] args) {
    PutTestDataInDatabase putTestDataInDatabase1 = new PutTestDataInDatabase();
    SessionInfo.setUserName(args[0]);
    SessionInfo.setPassword(args[1]);
    SessionInfo.setContributorInfo();
    new ReadReferencesFile(); // put references in the database
    new ReadSitesFile(); // put sites data in the database
  }

}