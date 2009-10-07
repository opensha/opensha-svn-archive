package org.opensha.refFaultParamDb.tests.dao.db;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.opensha.refFaultParamDb.dao.db.ContributorDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.dao.exception.QueryException;
import org.opensha.refFaultParamDb.dao.exception.UpdateException;
import org.opensha.refFaultParamDb.tests.AllTests;
import org.opensha.refFaultParamDb.vo.Contributor;
import org.opensha.refFaultParamDb.vo.FaultModelSummary;

/**
 *
 * <p>Title: TestFaultModelDB_DAO.java </p>
 * <p>Description: Test the fault Model DB DAO class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class TestFaultModelDB_DAO extends TestCase {
  private DB_AccessAPI dbConnection ;
  private ContributorDB_DAO contributorDB_DAO = null;
  private FaultModelSummaryDB_DAO faultModelDB_DAO = null;
  private static int contributorKey1, contributorKey2;
  private static int faultModelKey1, faultModelKey2;

  public TestFaultModelDB_DAO(String name) {
    super(name);
    dbConnection = AllTests.dbConnection;
  }

  protected void setUp() throws Exception {
    super.setUp();
    contributorDB_DAO = new ContributorDB_DAO(dbConnection);
    faultModelDB_DAO = new FaultModelSummaryDB_DAO(dbConnection);
  }

  protected void tearDown() throws Exception {
    contributorDB_DAO = null;
    faultModelDB_DAO=null;
    super.tearDown();
    //dbConnection.disconnect();
  }


  public void testFaultModelDB_DAO() {
    faultModelDB_DAO = new FaultModelSummaryDB_DAO(dbConnection);
    this.assertNotNull("faultModelDB_DAO object should not be null",faultModelDB_DAO);
  }


  public void testAddFaultModel() throws InsertException {
    Contributor contributor1 = new Contributor("Test1");
    Contributor contributor2 = new Contributor("Test2");
    contributorKey1 = contributorDB_DAO.addContributor(contributor1,"testpass1");
    contributor1.setId(contributorKey1);

    FaultModelSummary faultModel1 = new FaultModelSummary("usgs2002",contributor1);
    FaultModelSummary faultModel2 = new FaultModelSummary("usgs1996",contributor2);
    FaultModelSummary faultModel3 = new FaultModelSummary("CFM",contributor1);

    faultModelKey1= faultModelDB_DAO.addFaultModel(faultModel1);
    try {
      faultModelDB_DAO.addFaultModel(faultModel2);
      fail("should not insert this fault model as contributor id 2 does not exist in contributor table");
    }catch(InsertException e) {}
    faultModelKey2 = faultModelDB_DAO.addFaultModel(faultModel3);
  }

  public void testGetAllFaultModels() throws QueryException {
    ArrayList actualReturn = faultModelDB_DAO.getAllFaultModels();
    assertEquals("Should have 2 faultmodels in the table", 2, actualReturn.size());
  }

  public void testGetFaultModel() throws QueryException {
    FaultModelSummary actualReturn = faultModelDB_DAO.getFaultModel(98989);
    assertEquals("No faultmodel exists with id 98989", null, actualReturn);
    actualReturn = faultModelDB_DAO.getFaultModel(faultModelKey1);
    assertNotNull("should not be null as faultmodel exists with id = "+faultModelKey1,actualReturn);
    assertEquals("faultmodel id "+faultModelKey1+" has name geologic", "usgs2002", actualReturn.getFaultModelName());
    assertEquals("faultmodel id "+faultModelKey1+" has id "+faultModelKey1, faultModelKey1, actualReturn.getFaultModelId());
    assertEquals("faultmodel id "+faultModelKey1 +" has contributor name Test1", "Test1", actualReturn.getContributor().getName());
  }

  public void testUpdateFaultModel() throws UpdateException {
    Contributor contributor2 = new Contributor("Test2");
    contributorKey2 = contributorDB_DAO.addContributor(contributor2,"testpass2");
    contributor2.setId(contributorKey2);
    FaultModelSummary faultModel = new FaultModelSummary("Test1",contributor2);
    boolean status  = faultModelDB_DAO.updateFaultModel(7878, faultModel);
    this.assertFalse("cannot update fault model with 7878 as it does not exist", status);
    faultModel = new FaultModelSummary("usgs/cgs2002",contributor2);
    status = faultModelDB_DAO.updateFaultModel(faultModelKey1, faultModel);
    assertTrue("faultmodel with id="+faultModelKey1+" should be updated in the database",status);
    FaultModelSummary actualReturn = faultModelDB_DAO.getFaultModel(faultModelKey1);
    assertNotNull("should not be null as faultmodel exists with id = "+faultModelKey1,actualReturn);
    assertEquals("faultmodel id "+faultModelKey1+" has name usgs/cgs2002", "usgs/cgs2002", actualReturn.getFaultModelName());
    assertEquals("faultmodel id "+faultModelKey1+" has contributor id "+contributorKey2, contributorKey2, actualReturn.getContributor().getId());
  }

  public void testRemoveFaultModel() throws UpdateException {
    boolean status = faultModelDB_DAO.removeFaultModel(8989);
    this.assertFalse("cannot remove fault model with 8989 as it does not exist", status);
    status = faultModelDB_DAO.removeFaultModel(faultModelKey2);
    assertTrue("fault model with id="+faultModelKey1+" should be removed from the database",status);
    assertEquals("should now contain only 1 faultmodel",1, faultModelDB_DAO.getAllFaultModels().size());
    status=faultModelDB_DAO.removeFaultModel(faultModelKey1);
    assertTrue("faultmodel with id="+faultModelKey1+" should be removed from the database",status);
    assertEquals("should now contain only 0 fault models",0, faultModelDB_DAO.getAllFaultModels().size());
    contributorDB_DAO.removeContributor(contributorKey1);
    contributorDB_DAO.removeContributor(contributorKey2);
  }
}
