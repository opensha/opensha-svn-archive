package javaDevelopers.vipin.tests.dao.db;

import junit.framework.*;
import javaDevelopers.vipin.dao.db.*;
import javaDevelopers.vipin.dao.exception.*;
import javaDevelopers.vipin.vo.*;
import java.util.*;
import java.sql.SQLException;

/**
 *
 * <p>Title: Test_EstimateTypeDB_DAO.java </p>
 * <p>Description: Test the Test_EstimateType DB DAO class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class TestEstimateTypeDB_DAO extends TestCase {
  private DB_Connection dbConnection = new DB_Connection();
  private EstimateTypeDB_DAO estimateTypeDB_DAO = null;

  public TestEstimateTypeDB_DAO(String name) {
    super(name);
    try{
      dbConnection.connect(DB_Connection.USERNAME, DB_Connection.PASSWORD);
    }catch(SQLException e) {
      e.printStackTrace();
    }
  }

  protected void setUp() throws Exception {
    super.setUp();
    estimateTypeDB_DAO = new EstimateTypeDB_DAO(dbConnection);
  }

  protected void tearDown() throws Exception {
    estimateTypeDB_DAO = null;
    super.tearDown();
    dbConnection.disconnect();
  }


  public void testEstimateTypeDB_DAO() {
    estimateTypeDB_DAO = new EstimateTypeDB_DAO(dbConnection);
    this.assertNotNull("estimateTypeDB_DAO object should not be null",estimateTypeDB_DAO);
  }


  public void testGetAllEstimateTypes() throws QueryException {
    ArrayList actualReturn = estimateTypeDB_DAO.getAllEstimateTypes();
    assertEquals("Should have 6 estimate types in the table", 6, actualReturn.size());
  }

  public void testGetEstimateTypeById() throws QueryException {
    EstimateType actualReturn = estimateTypeDB_DAO.getEstimateType(1);
    assertEquals("NormalEstimate",actualReturn.getEstimateName());
    actualReturn = estimateTypeDB_DAO.getEstimateType(2);
    assertEquals("LogNormalEstimate",actualReturn.getEstimateName());
    actualReturn = estimateTypeDB_DAO.getEstimateType(3);
    assertEquals("PDF_Estimate",actualReturn.getEstimateName());
    actualReturn = estimateTypeDB_DAO.getEstimateType(4);
    assertEquals("FractileListEstimate",actualReturn.getEstimateName());
    actualReturn = estimateTypeDB_DAO.getEstimateType(5);
    assertEquals("IntegerEstimate",actualReturn.getEstimateName());
    actualReturn = estimateTypeDB_DAO.getEstimateType(6);
    assertEquals("DiscreteValueEstimate",actualReturn.getEstimateName());
  }

  public void testGetEstimateTypeByName() throws QueryException {
    EstimateType actualReturn = estimateTypeDB_DAO.getEstimateType("NormalEstimate");
    assertEquals(1,actualReturn.getEstimateTypeId());
    actualReturn = estimateTypeDB_DAO.getEstimateType("LogNormalEstimate");
    assertEquals(2,actualReturn.getEstimateTypeId());
    actualReturn = estimateTypeDB_DAO.getEstimateType("PDF_Estimate");
    assertEquals(3,actualReturn.getEstimateTypeId());
    actualReturn = estimateTypeDB_DAO.getEstimateType("FractileListEstimate");
    assertEquals(4,actualReturn.getEstimateTypeId());
    actualReturn = estimateTypeDB_DAO.getEstimateType("IntegerEstimate");
    assertEquals(5,actualReturn.getEstimateTypeId());
    actualReturn = estimateTypeDB_DAO.getEstimateType("DiscreteValueEstimate");
    assertEquals(6,actualReturn.getEstimateTypeId());
 }


}
