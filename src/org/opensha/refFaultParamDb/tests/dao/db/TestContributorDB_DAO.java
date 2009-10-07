package org.opensha.refFaultParamDb.tests.dao.db;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.opensha.refFaultParamDb.dao.db.ContributorDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.dao.exception.QueryException;
import org.opensha.refFaultParamDb.dao.exception.UpdateException;
import org.opensha.refFaultParamDb.tests.AllTests;
import org.opensha.refFaultParamDb.vo.Contributor;
/**
 *
 * <p>Title: TestContributorDB_DAO.java </p>
 * <p>Description: Test the Contributor DB DAO class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class TestContributorDB_DAO extends TestCase {
  private DB_AccessAPI dbConnection ;
  private ContributorDB_DAO contributorDB_DAO = null;
  private static int primaryKey1, primaryKey2;

  public TestContributorDB_DAO(String name) {
    super(name);
    dbConnection = AllTests.dbConnection;
  }

  protected void setUp() throws Exception {
    super.setUp();
    contributorDB_DAO = new ContributorDB_DAO(dbConnection);
  }

  protected void tearDown() throws Exception {
    contributorDB_DAO = null;
    super.tearDown();
  }


  public void testContributorDB_DAO() {
    contributorDB_DAO = new ContributorDB_DAO(dbConnection);
    this.assertNotNull("contributor_DAO object should not be null",contributorDB_DAO);
  }


  public void testAddContributor() throws InsertException {
    Contributor contributor1 = new Contributor("Test1");
    Contributor contributor2 = new Contributor("Test2");
    Contributor contributor3 = new Contributor("Test3");
    primaryKey1 = contributorDB_DAO.addContributor(contributor1,"testPass1");
    primaryKey2 = contributorDB_DAO.addContributor(contributor3,"testPass3");
    assertTrue(primaryKey1!=primaryKey2);
  }

  public void testGetAllContributors() throws QueryException {
    ArrayList actualReturn = contributorDB_DAO.getAllContributors();
    assertEquals("Should have 2 contributors in the table", 2, actualReturn.size());
  }

  public void testGetContributor() throws QueryException {
    Contributor actualReturn = contributorDB_DAO.getContributor(67866);
    assertEquals("No contributor exists with id 67866", null, actualReturn);
    actualReturn = contributorDB_DAO.getContributor(primaryKey1);
    assertNotNull("should not be null as contributor exists with id = "+primaryKey1,actualReturn);
    assertEquals("Test1", actualReturn.getName());
  }


  public void testRemoveContributor() throws UpdateException {
    boolean status = contributorDB_DAO.removeContributor(7878);
    this.assertFalse("cannot remove contributor with 7878 as it does not exist", status);
    status = contributorDB_DAO.removeContributor(primaryKey2);
    assertTrue("contributor with id="+primaryKey2+" should be removed from the database",status);
    assertEquals("should now contain only 1 contributor",1, contributorDB_DAO.getAllContributors().size());
    status=contributorDB_DAO.removeContributor(primaryKey1);
    assertTrue("contributor with id="+primaryKey1+" should be removed from the database",status);
    assertEquals("should now contain only 0 contributor",0, contributorDB_DAO.getAllContributors().size());
  }
}
