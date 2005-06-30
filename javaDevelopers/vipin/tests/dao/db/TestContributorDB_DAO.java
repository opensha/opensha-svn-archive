package javaDevelopers.vipin.tests.dao.db;

import junit.framework.*;
import javaDevelopers.vipin.dao.db.*;
import javaDevelopers.vipin.dao.exception.*;
import javaDevelopers.vipin.vo.*;
import java.util.*;
import java.sql.SQLException;

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
  private DB_Connection dbConnection = new DB_Connection();
  private ContributorDB_DAO contributorDB_DAO = null;

  public TestContributorDB_DAO(String name) {
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
  }

  protected void tearDown() throws Exception {
    contributorDB_DAO = null;
    super.tearDown();
    dbConnection.disconnect();
  }


  public void testContributorDB_DAO() {
    contributorDB_DAO = new ContributorDB_DAO(dbConnection);
    this.assertNotNull("contributor_DAO object should not be null",contributorDB_DAO);
  }


  public void testAddContributor() throws InsertException {
    Contributor contributor1 = new Contributor(1,"Test1");
    Contributor contributor2 = new Contributor(1,"Test2");
    Contributor contributor3 = new Contributor(3,"Test3");
    contributorDB_DAO.addContributor(contributor1);
    try {
    contributorDB_DAO.addContributor(contributor2);
    fail("should not reach here as tried to insert contributor with duplicate key");
    }catch(InsertException e) {}
    contributorDB_DAO.addContributor(contributor3);
  }

  public void testGetAllContributors() throws QueryException {
    ArrayList actualReturn = contributorDB_DAO.getAllContributors();
    assertEquals("Should have 2 contributors in the table", 2, actualReturn.size());
    Contributor contributor = (Contributor)actualReturn.get(0);
    assertEquals("contributor id expected is 1",1,contributor.getId());
    contributor = (Contributor)actualReturn.get(1);
    assertEquals("contributor id expected is 3",3,contributor.getId());
  }

  public void testGetContributor() throws QueryException {
    Contributor actualReturn = contributorDB_DAO.getContributor(2);
    assertEquals("No contributor exists with id 2", null, actualReturn);
    actualReturn = contributorDB_DAO.getContributor(1);
    assertNotNull("should not be null as contributor exists with id = 1",actualReturn);
    assertEquals("contributor id 1 has name test 1", "Test1", actualReturn.getName());
  }

  public void testUpdateContributor() throws UpdateException {
    Contributor contributor = new Contributor(2,"Test2");
    boolean status  = contributorDB_DAO.updateContributor(2, contributor);
    this.assertFalse("cannot update contributor with 2 as it does not exist", status);
    contributor = new Contributor(1,"TestTest1");
    status = contributorDB_DAO.updateContributor(1, contributor);
    assertTrue("contributor with id=1 should be updated in the database",status);
    Contributor actualReturn = contributorDB_DAO.getContributor(1);
    assertNotNull("should not be null as contributor exists with id = 1",actualReturn);
    assertEquals("contributor id 1 has name test 1", "TestTest1", actualReturn.getName());
  }

  public void testRemoveContributor() throws UpdateException {
    boolean status = contributorDB_DAO.removeContributor(2);
    this.assertFalse("cannot remove contributor with 2 as it does not exist", status);
    status = contributorDB_DAO.removeContributor(3);
    assertTrue("contributor with id=3 should be removed from the database",status);
    assertEquals("should now contain only 1 contributor",1, contributorDB_DAO.getAllContributors().size());
    status=contributorDB_DAO.removeContributor(1);
    assertTrue("contributor with id=1 should be removed from the database",status);
    assertEquals("should now contain only 0 contributor",0, contributorDB_DAO.getAllContributors().size());
  }
}
