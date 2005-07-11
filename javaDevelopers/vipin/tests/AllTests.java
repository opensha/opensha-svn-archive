package javaDevelopers.vipin.tests;

import junit.framework.*;
import java.sql.SQLException;
import javaDevelopers.vipin.dao.db.DB_Connection;

public class AllTests
    extends TestCase {
  public static DB_Connection dbConnection;

  public AllTests(String s) {
    super(s);
  }


  public static Test suite() {
    dbConnection = new DB_Connection();
    try {
      dbConnection.connect(DB_Connection.USERNAME, DB_Connection.PASSWORD);
    }
    catch (SQLException ex) {
      ex.printStackTrace();
    }
    TestSuite suite = new TestSuite();
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestContributorDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestSiteTypeDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.Test_QFault2002B_DB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestPaleoSiteDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestEstimateTypeDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestNormalEstimateInstancesDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestLogTypeDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestLogNormalEstimateInstancesDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestIntegerEstimateInstancesDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestFractileListEstimateInstancesDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestDiscreteValueEstimateInstancesDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestReferenceDB_DAO.class);
    suite.addTestSuite(javaDevelopers.vipin.tests.dao.db.TestFaultModelDB_DAO.class);
    return suite;
  }
}
