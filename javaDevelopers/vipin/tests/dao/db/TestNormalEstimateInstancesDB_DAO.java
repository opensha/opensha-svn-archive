package javaDevelopers.vipin.tests.dao.db;

import junit.framework.*;
import javaDevelopers.vipin.dao.db.*;
import javaDevelopers.vipin.dao.exception.*;
import javaDevelopers.vipin.vo.*;
import java.util.*;
import java.sql.SQLException;
import org.opensha.data.estimate.NormalEstimate;
import javaDevelopers.vipin.tests.AllTests;
/**
 *
 * <p>Title: TestNormalEstimateInstancesDB_DAO.java </p>
 * <p>Description: Test the Estimate Instances DB DAO</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class TestNormalEstimateInstancesDB_DAO extends TestCase {
  private DB_Connection dbConnection;
  private EstimateInstancesDB_DAO estimateInstancesDB_DAO = null;
  static int primaryKey1, primaryKey2;

  public TestNormalEstimateInstancesDB_DAO(String name) {
    super(name);
    dbConnection = AllTests.dbConnection;
  }

  protected void setUp() throws Exception {
    super.setUp();
    estimateInstancesDB_DAO = new EstimateInstancesDB_DAO(dbConnection);
  }

  protected void tearDown() throws Exception {
    estimateInstancesDB_DAO = null;
    super.tearDown();
  }


  public void testNormalEstimateInstancesDB_DAO() {
    estimateInstancesDB_DAO = new EstimateInstancesDB_DAO(dbConnection);
    this.assertNotNull("estimateInstancesDB_DAO object should not be null",estimateInstancesDB_DAO);
  }


  public void testAddNormalEstimateInstance() throws InsertException {
    NormalEstimate estimate1 = new NormalEstimate(5, 2);
    NormalEstimate estimate2 = new NormalEstimate(3, 5);
    EstimateInstances estimateInstance = new EstimateInstances();
    estimateInstance.setEstimate(estimate1);
    estimateInstance.setUnits("meters");
    primaryKey1 = estimateInstancesDB_DAO.addEstimateInstance(estimateInstance);
    estimateInstance.setEstimate(estimate2);
    primaryKey2 = estimateInstancesDB_DAO.addEstimateInstance(estimateInstance);
    assertTrue(primaryKey1!=primaryKey2);
    assertEquals("there should be 2 estimate instances", 2, estimateInstancesDB_DAO.getAllEstimateInstances().size());
  }


  public void testGetNormalEstimateInstance() throws QueryException {
    EstimateInstances actualReturn = estimateInstancesDB_DAO.getEstimateInstance(4546);
    assertEquals("No estimate exists with id 4546", null, actualReturn);

    actualReturn = estimateInstancesDB_DAO.getEstimateInstance(primaryKey1);
    assertNotNull("should not be null as estimate exists with id ="+primaryKey1,actualReturn);
    NormalEstimate estimate  = (NormalEstimate)actualReturn.getEstimate();
    assertEquals(5, estimate.getMean(),0.000001);
    assertEquals(2, estimate.getStdDev(),0.000001);
    assertEquals("meters", actualReturn.getUnits());

    actualReturn = estimateInstancesDB_DAO.getEstimateInstance(primaryKey2);
    assertNotNull("should not be null as estimate exists with id ="+primaryKey2,actualReturn);
    estimate  = (NormalEstimate)actualReturn.getEstimate();
    assertEquals(3, estimate.getMean(),0.000001);
    assertEquals(5, estimate.getStdDev(),0.000001);
    assertEquals("meters", actualReturn.getUnits());

  }


  public void testRemoveNormalEstimateInstance() throws UpdateException {
    boolean status = estimateInstancesDB_DAO.removeEstimateInstance(5225);
    this.assertFalse("cannot remove estimate with 5225 as it does not exist", status);
    assertEquals("there should be 2 estimate instances", 2, estimateInstancesDB_DAO.getAllEstimateInstances().size());
    status = estimateInstancesDB_DAO.removeEstimateInstance(primaryKey1);
    assertTrue(status);
    assertEquals("there should be 1 estimate instance", 1, estimateInstancesDB_DAO.getAllEstimateInstances().size());
    status = estimateInstancesDB_DAO.removeEstimateInstance(primaryKey2);
    assertTrue(status);
    assertEquals("there should be 0 estimate instances", 0, estimateInstancesDB_DAO.getAllEstimateInstances().size());

  }
}
