package org.opensha.refFaultParamDb.tests;

import junit.framework.*;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;

public class AllTests
    extends TestCase {
  public static DB_AccessAPI dbConnection = new DB_ConnectionPool();

  public AllTests(String s) {
    super(s);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    /*suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestContributorDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestSiteTypeDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.Test_QFault2002B_DB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestPaleoSiteDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestEstimateTypeDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestNormalEstimateInstancesDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestLogTypeDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestLogNormalEstimateInstancesDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestIntegerEstimateInstancesDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestFractileListEstimateInstancesDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestDiscreteValueEstimateInstancesDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestReferenceDB_DAO.class);
    suite.addTestSuite(org.opensha.refFaultParamDb.tests.dao.db.TestFaultModelDB_DAO.class);*/
    return suite;
  }
}
