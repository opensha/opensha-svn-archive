package javaDevelopers.vipin.tests.dao.db;

import junit.framework.*;
import javaDevelopers.vipin.dao.db.*;
import javaDevelopers.vipin.dao.exception.*;
import javaDevelopers.vipin.vo.*;
import java.util.*;
import java.sql.SQLException;
import javaDevelopers.vipin.tests.AllTests;
/**
 *
 * <p>Title: TestPaleoSiteDB_DAO.java </p>
 * <p>Description: Test the Paleo Site DB DAO class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class TestPaleoSiteDB_DAO extends TestCase {
  private DB_Connection dbConnection;
  private ContributorDB_DAO contributorDB_DAO = null;
  private SiteTypeDB_DAO siteTypeDB_DAO = null;
  private PaleoSiteDB_DAO paleoSiteDB_DAO = null;
  private static int contributorKey1, siteTypeKey1, siteTypeKey2;

  public TestPaleoSiteDB_DAO(String name) {
    super(name);
    dbConnection = AllTests.dbConnection;
  }

  protected void setUp() throws Exception {
    super.setUp();
    contributorDB_DAO = new ContributorDB_DAO(dbConnection);
    siteTypeDB_DAO = new SiteTypeDB_DAO(dbConnection);
    paleoSiteDB_DAO = new PaleoSiteDB_DAO(dbConnection);
  }

  protected void tearDown() throws Exception {
    contributorDB_DAO = null;
    siteTypeDB_DAO=null;
    paleoSiteDB_DAO = null;
    super.tearDown();
  }


  public void testPaleoSiteDB_DAO() {
    paleoSiteDB_DAO = new PaleoSiteDB_DAO(dbConnection);
    this.assertNotNull("paleoSiteDB_DAO object should not be null",paleoSiteDB_DAO);
  }


  public void testAddPaleoSite() throws InsertException {
    Contributor contributor1 = new Contributor("Test1");
    contributorKey1 = contributorDB_DAO.addContributor(contributor1);
    contributor1.setId(contributorKey1);
    SiteType siteType1 = new SiteType("geologic",contributor1);
    SiteType siteType3 = new SiteType("paleosite",contributor1);

    siteTypeKey1 = siteTypeDB_DAO.addSiteType(siteType1);
    siteType1.setSiteTypeId(siteTypeKey1);
    siteTypeKey2 = siteTypeDB_DAO.addSiteType(siteType3);
    siteType3.setSiteTypeId(siteTypeKey2);

    // paleo site 1
    PaleoSite paleoSite = new PaleoSite();
    paleoSite.setSiteId(1);
    paleoSite.setSiteContributor(contributor1);
    paleoSite.setSiteType(siteType1);
    paleoSite.setSiteName("Test1");
    paleoSite.setSiteLat1(32.1f);
    paleoSite.setSiteLon1(-117.0f);
    paleoSite.setSiteElevation1(0.5f);
    paleoSite.setRepresentativeStrandIndex(1);
    paleoSite.setGeneralComments("Test comments");
    paleoSite.setOldSiteId(1);

    paleoSiteDB_DAO.addPaleoSite(paleoSite);

    try {
      paleoSite.setSiteId(2);
      Contributor contributor2 = new Contributor(2,"Test1");
      paleoSite.setSiteContributor(contributor2);
      paleoSiteDB_DAO.addPaleoSite(paleoSite);
      fail("should not insert this paleosite as contributor id 2 does not exist in contributor table");
    }catch(InsertException e) {}

    try {
      paleoSite.setSiteContributor(contributor1);
      SiteType siteType2 = new SiteType(2,"paleosite",contributor1);
      paleoSite.setSiteType(siteType2);
      paleoSiteDB_DAO.addPaleoSite(paleoSite);
      fail("should not insert this paleosite as site type id 2 does not exist in sitetype table");
    }catch(InsertException e) {}

    paleoSite.setSiteId(3);
    paleoSite.setSiteContributor(contributor1);
    paleoSite.setSiteType(siteType3);
    paleoSiteDB_DAO.addPaleoSite(paleoSite);
  }

  public void testGetPaleoSites() throws QueryException {
    ArrayList actualReturn = paleoSiteDB_DAO.getAllPaleoSites();
    assertEquals("Should have 2 paleoSites in the table", 2, actualReturn.size());
  }

  public void testGetPaleoSite() throws QueryException {
    PaleoSite actualReturn = paleoSiteDB_DAO.getPaleoSite(2);
    assertEquals("No paleoSite exists with id 2", null, actualReturn);
    actualReturn = paleoSiteDB_DAO.getPaleoSite(1);
    assertNotNull("should not be null as paloeSite exists with id = 1",actualReturn);

    //paleoSite.setEffectiveDate(new java.util.Date());
    assertEquals(1, actualReturn.getSiteId());
    assertEquals(contributorKey1, actualReturn.getSiteContributor().getId());
    assertEquals(siteTypeKey1, actualReturn.getSiteType().getSiteTypeId());
    assertEquals("Test1",actualReturn.getSiteName());
    assertEquals(32.1,actualReturn.getSiteLat1(),.0001);
    assertEquals(-117.0,actualReturn.getSiteLon1(),.0001);
    assertEquals(0.5,actualReturn.getSiteElevation1(),.0001);
    assertEquals(1, actualReturn.getRepresentativeStrandIndex());
    assertEquals("Test comments", actualReturn.getGeneralComments());
    assertEquals(1, actualReturn.getOldSiteId());
  }

  public void testUpdatePaleoSite() throws UpdateException {
    Contributor contributor2 = new Contributor(2,"Test2");

    PaleoSite paleoSite = paleoSiteDB_DAO.getPaleoSite(1);
    paleoSite.setSiteContributor(contributor2);
    try {
      boolean status  = paleoSiteDB_DAO.updatePaleoSite(1,paleoSite);
      this.assertFalse("cannot update paleosite as contributor id 2  does not exist", status);
    }catch(UpdateException e) { }
    paleoSite = paleoSiteDB_DAO.getPaleoSite(1);
    paleoSite.setSiteName("UpdatePaleosite1");
    boolean status = paleoSiteDB_DAO.updatePaleoSite(1, paleoSite);
    assertTrue("paleosite with id=1 should be updated in the database",status);

    PaleoSite actualReturn = paleoSiteDB_DAO.getPaleoSite(1);
    assertNotNull("should not be null as paleosite exists with id = 1",actualReturn);
    assertEquals("paloesite id 1 has name UpdatePaleosite1", "UpdatePaleosite1", actualReturn.getSiteName());
  }


  public void testRemovePaleoSite() throws UpdateException {
    boolean status = paleoSiteDB_DAO.removePaleoSite(2);
    this.assertFalse("cannot remove paleo site with 2 as it does not exist", status);
    status =  paleoSiteDB_DAO.removePaleoSite(3);
    assertTrue("paleoSite with id=3 should be removed from the database",status);
    assertEquals("should now contain only 1 paleoSite",1, paleoSiteDB_DAO.getAllPaleoSites().size());
    status=paleoSiteDB_DAO.removePaleoSite(1);
    assertTrue("paleosite with id=1 should be removed from the database",status);
    assertEquals("should now contain only 0 paleosite",0, paleoSiteDB_DAO.getAllPaleoSites().size());
    siteTypeDB_DAO.removeSiteType(siteTypeKey1);
    siteTypeDB_DAO.removeSiteType(siteTypeKey2);
    contributorDB_DAO.removeContributor(contributorKey1);

  }
}
