package org.opensha.refFaultParamDb.tests.dao.db;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.opensha.refFaultParamDb.dao.db.ContributorDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.SiteTypeDB_DAO;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.dao.exception.QueryException;
import org.opensha.refFaultParamDb.dao.exception.UpdateException;
import org.opensha.refFaultParamDb.tests.AllTests;
import org.opensha.refFaultParamDb.vo.Contributor;
import org.opensha.refFaultParamDb.vo.SiteType;
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
  private DB_AccessAPI dbConnection;
  private ContributorDB_DAO contributorDB_DAO = null;
  private SiteTypeDB_DAO siteTypeDB_DAO = null;
  private static int contributorKey1, contributorKey2;
  private static int siteTypeKey1, siteTypeKey2;

  public TestSiteTypeDB_DAO(String name) {
    super(name);
    dbConnection = AllTests.dbConnection;
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
  }


  public void testSiteTypeDB_DAO() {
    siteTypeDB_DAO = new SiteTypeDB_DAO(dbConnection);
    this.assertNotNull("siteTypeDB_DAO object should not be null",siteTypeDB_DAO);
  }


  public void testAddSiteType() throws InsertException {
    Contributor contributor1 = new Contributor("Test1");
    Contributor contributor2 = new Contributor("Test2");
    contributorKey1 = contributorDB_DAO.addContributor(contributor1,"testpass1");
    contributor1.setId(contributorKey1);

    SiteType siteType1 = new SiteType("geologic",contributor1, "TestComments1");
    SiteType siteType2 = new SiteType("trench",contributor2, "TestComments2");
    SiteType siteType3 = new SiteType("paleosite",contributor1, "TestComments3");

    siteTypeKey1= siteTypeDB_DAO.addSiteType(siteType1);
    try {
      siteTypeDB_DAO.addSiteType(siteType2);
      fail("should not insert this site type as contributor id 2 does not exist in contributor table");
    }catch(InsertException e) {}
    siteTypeKey2 = siteTypeDB_DAO.addSiteType(siteType3);
  }

  public void testGetAllSiteTypes() throws QueryException {
    ArrayList actualReturn = siteTypeDB_DAO.getAllSiteTypes();
    assertEquals("Should have 2 sitetypes in the table", 2, actualReturn.size());
  }

  public void testGetSiteType() throws QueryException {
    SiteType actualReturn = siteTypeDB_DAO.getSiteType(98989);
    assertEquals("No sitetype exists with id 98989", null, actualReturn);
    actualReturn = siteTypeDB_DAO.getSiteType(siteTypeKey1);
    assertNotNull("should not be null as sitetype exists with id = "+siteTypeKey1,actualReturn);
    assertEquals("sitetype id "+siteTypeKey1+" has name geologic", "geologic", actualReturn.getSiteType());
    assertEquals("sitetype id "+siteTypeKey1+" has id "+siteTypeKey1, siteTypeKey1, actualReturn.getSiteTypeId());
    assertEquals("sitetype id "+siteTypeKey1 +" has contributor name Test1", "Test1", actualReturn.getContributor().getName());
    assertEquals("sitetype id "+siteTypeKey1 +" has comments TestComments1", "TestComments1", actualReturn.getComments());
  }

  public void testUpdateSiteType() throws UpdateException {
    Contributor contributor2 = new Contributor("Test2");
    contributorKey2 = contributorDB_DAO.addContributor(contributor2,"testpass2");
    contributor2.setId(contributorKey2);
    SiteType siteType = new SiteType("SiteTest2",contributor2,"Comments1");
    boolean status  = siteTypeDB_DAO.updateSiteType(7878, siteType);
    this.assertFalse("cannot update contributor with 7878 as it does not exist", status);
    siteType = new SiteType("UpdateSiteTest1",contributor2,"Comments2");
    status = siteTypeDB_DAO.updateSiteType(siteTypeKey1, siteType);
    assertTrue("sitetype with id="+siteTypeKey1+" should be updated in the database",status);
    SiteType actualReturn = siteTypeDB_DAO.getSiteType(siteTypeKey1);
    assertNotNull("should not be null as siteType exists with id = "+siteTypeKey1,actualReturn);
    assertEquals("sitetype id "+siteTypeKey1+" has name UpdateSiteTest1", "UpdateSiteTest1", actualReturn.getSiteType());
    assertEquals("sitetype id "+siteTypeKey1+" has contributor id "+contributorKey2, contributorKey2, actualReturn.getContributor().getId());
    assertEquals("sitetype id "+siteTypeKey1+" has comments Comments2", "Comments2", actualReturn.getComments());
  }

  public void testRemoveSiteType() throws UpdateException {
    boolean status = siteTypeDB_DAO.removeSiteType(8989);
    this.assertFalse("cannot remove contributor with 8989 as it does not exist", status);
    status = siteTypeDB_DAO.removeSiteType(siteTypeKey2);
    assertTrue("sitetype with id="+siteTypeKey1+" should be removed from the database",status);
    assertEquals("should now contain only 1 sitetype",1, siteTypeDB_DAO.getAllSiteTypes().size());
    status=siteTypeDB_DAO.removeSiteType(siteTypeKey1);
    assertTrue("sitetype with id="+siteTypeKey1+" should be removed from the database",status);
    assertEquals("should now contain only 0 site types",0, siteTypeDB_DAO.getAllSiteTypes().size());
    contributorDB_DAO.removeContributor(contributorKey1);
    contributorDB_DAO.removeContributor(contributorKey2);
  }
}
