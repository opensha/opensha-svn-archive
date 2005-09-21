package org.opensha.refFaultParamDb.excelToDatabase;

import org.opensha.refFaultParamDb.dao.db.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.vo.Reference;

/**
 * <p>Title: ReadSitesFile.java </p>
 * <p>Description: This program reads the sites file and puts the sites into the database
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ReadSitesFile {
  private final static String FILENAME = "org\\opensha\\refFaultParamDb\\PaleoSites.txt";
  private PaleoSiteDB_DAO paleoSiteDAO = new PaleoSiteDB_DAO(DB_AccessAPI.dbConnection);
  private ReferenceDB_DAO referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
  private final static String COMMENTS_DEFAULT = "Site Information provided by Chris Wills from Excel file";
  private final static String STRAND_DEFAULT = "Unknown";
  private final static String SITE_TYPE_DAFULT = "Unknown";
  private final static float  SITE_ELEVATION_DEFAULT = 0.0f;
  public ReadSitesFile() {
    try {
     ArrayList referencesList = org.opensha.util.FileUtils.loadFile(FILENAME);
     String line;
     StringTokenizer tokenizer;
     SessionInfo.setUserName("");
     SessionInfo.setPassword("");
     SessionInfo.setContributorInfo();
     for (int i = 0; i < referencesList.size(); ++i) {
       line = (String)referencesList.get(i);
       // make the paleoSite VO
       PaleoSite paleoSite = new PaleoSite();
       tokenizer = new StringTokenizer(line,"|");
       paleoSite.setOldSiteId(tokenizer.nextToken().trim());
       paleoSite.setSiteName(tokenizer.nextToken().trim());
       paleoSite.setFaultName(tokenizer.nextToken().trim());
       String references = tokenizer.nextToken().trim();
       paleoSite.setSiteLon1(Float.parseFloat(tokenizer.nextToken().trim()));
       paleoSite.setSiteLat1(Float.parseFloat(tokenizer.nextToken().trim()));
       paleoSite.setSiteElevation1(SITE_ELEVATION_DEFAULT);
       paleoSite.setSiteLon2(paleoSite.getSiteLon1());
       paleoSite.setSiteLat2(paleoSite.getSiteLat1());
       paleoSite.setSiteElevation2(SITE_ELEVATION_DEFAULT);
       paleoSite.setGeneralComments(COMMENTS_DEFAULT);
       paleoSite.setEntryComments(COMMENTS_DEFAULT);
       paleoSite.setRepresentativeStrandName(STRAND_DEFAULT);
       paleoSite.setSiteTypeName(SITE_TYPE_DAFULT);
       //parse the references
       ArrayList referenceList = new ArrayList();
        // there may be more than one references for this site

       StringTokenizer referencesTokenizer = new StringTokenizer(references,";");
       while(referencesTokenizer.hasMoreTokens()) referenceList.add(referencesTokenizer.nextToken().trim());
       paleoSite.setReferenceShortCitationList(referenceList);
       paleoSite.setSiteContributor(SessionInfo.getContributor());
       /*for(int j=0; j< referenceList.size(); ++j) {
         String rf = (String)referenceList.get(j);
         Reference ref = referenceDAO.getReference(rf);
         if(ref==null) {
           System.out.println(rf + " does not exist");
         }
       }*/
      paleoSiteDAO.addPaleoSite(paleoSite);
     }
   }catch(Exception e) {
     e.printStackTrace();
   }

  }

  public static void main(String[] args) {
    ReadSitesFile readSitesFile1 = new ReadSitesFile();
  }

}