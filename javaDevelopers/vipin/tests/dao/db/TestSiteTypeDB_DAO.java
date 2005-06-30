package javaDevelopers.vipin.tests.dao.db;

import junit.framework.*;
import javaDevelopers.vipin.dao.db.*;
import javaDevelopers.vipin.dao.exception.*;
import javaDevelopers.vipin.vo.*;
import java.util.*;
import java.sql.SQLException;

/**
 *
 * <p>Title: TestSiteTypeDB_DAO.java </p>
 * <p>Description: Test the Site Type DB DAO class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class TestSiteTypeDB_DAO extends TestCase {
  private DB_Connection dbConnection = new DB_Connection();
  private ContributorDB_DAO contributorDB_DAO = null;
  private SiteTypeDB_DAO siteTypeDB_DAO = null;

  public TestSiteTypeDB_DAO(String name) {
    super(name);
    try{
      dbConnection.connect("fault_sandbox", "perry");
    }catch(SQLException e) {
      e.printStackTrace();
    }
  }

  protected void setUp() throws Exception {
    super.setUp();
    contributorDB_DAO = new ContributorDB_DAO(dbConnection);
    siteTypeDB_DAO = new SiteTypeDB_DAO(dbConnection);
  }

  protected void tearDown() throws Exception {
    contributorDB_DAO = null;
    siteTypeDB_DAO=null;
    super.tearDown();
    dbConnection.disconnect();
  }


  public void testSiteTypeDB_DAO() {
    siteTypeDB_DAO = new SiteTypeDB_DAO(dbConnection);
    this.assertNotNull("siteTypeDB_DAO object should not be null",siteTypeDB_DAO);
  }


  public void testAddSiteType() throws InsertException {
    Contributor contributor1 = new Contributor(1,"Test1");
    Contributor contributor2 = new Contributor(2,"Test2");

    SiteType siteType1 = new SiteType(1,"geologic",contributor1);
    SiteType siteType2 = new SiteType(2,"trench",contributor2);
    SiteType siteType3 = new SiteType(3,"paleosite",contributor1);


    contributorDB_DAO.addContributor(contributor1);
    siteTypeDB_DAO.addSiteType(siteType1);
    try {
      siteTypeDB_DAO.addSiteType(siteType2);
      fail("should not insert this site type as contributor id 2 does not exist in contributor table");
    }catch(InsertException e) {}
    siteTypeDB_DAO.addSiteType(siteType3);
  }

  public void testGetAllSiteTypes() throws QueryException {
    ArrayList actualReturn = siteTypeDB_DAO.getAllSiteTypes();
    assertEquals("Should have 2 sitetypes in the table", 2, actualReturn.size());
    SiteType siteType = (SiteType)actualReturn.get(0);
    assertEquals("sitetype id expected is 1",1, siteType.getSiteTypeId());
    siteType = (SiteType)actualReturn.get(1);
    assertEquals("sitetype id expected is 3",siteType.getSiteTypeId(),3);
  }

  public void testGetSiteType() throws QueryException {
    SiteType actualReturn = siteTypeDB_DAO.getSiteType(2);
    assertEquals("No sitetype exists with id 2", null, actualReturn);
    actualReturn = siteTypeDB_DAO.getSiteType(1);
    assertNotNull("should not be null as sitetype exists with id = 1",actualReturn);
    assertEquals("sitetype id 1 has name geologic", "geologic", actualReturn.getSiteType());
    assertEquals("sitetype id 1 has id 1", 1, actualReturn.getSiteTypeId());
    assertEquals("sitetype id 1 has contributor name Test1", "Test1", actualReturn.getContributor().getName());
  }

  public void testUpdateSiteType() throws UpdateException {
    Contributor contributor2 = new Contributor(2,"Test2");
    contributorDB_DAO.addContributor(contributor2);
    SiteType siteType = new SiteType(2,"SiteTest2",contributor2);
    boolean status  = siteTypeDB_DAO.updateSiteType(2, siteType);
    this.assertFalse("cannot update contributor with 2 as it does not exist", status);
    siteType = new SiteType(1,"UpdateSiteTest1",contributor2);
    status = siteTypeDB_DAO.updateSiteType(1, siteType);
    assertTrue("contributor with id=1 should be updated in the database",status);
    SiteType actualReturn = siteTypeDB_DAO.getSiteType(1);
    assertNotNull("should not be null as siteType exists with id = 1",actualReturn);
    assertEquals("sitetype id 1 has name UpdateSiteTest1", "UpdateSiteTest1", actualReturn.getSiteType());
    assertEquals("sitetype id 1 has contributor id 2", 2, actualReturn.getContributor().getId());
  }

  public void testRemoveSiteType() throws UpdateException {
    boolean status = siteTypeDB_DAO.removeSiteType(2);
    this.assertFalse("cannot remove contributor with 2 as it does not exist", status);
    status = siteTypeDB_DAO.removeSiteType(3);
    assertTrue("sitetype with id=3 should be removed from the database",status);
    assertEquals("should now contain only 1 sitetype",1, siteTypeDB_DAO.getAllSiteTypes().size());
    status=siteTypeDB_DAO.removeSiteType(1);
    assertTrue("sitetype with id=1 should be removed from the database",status);
    assertEquals("should now contain only 0 contributor",0, siteTypeDB_DAO.getAllSiteTypes().size());
    contributorDB_DAO.removeContributor(1);
    contributorDB_DAO.removeContributor(2);
  }
}
