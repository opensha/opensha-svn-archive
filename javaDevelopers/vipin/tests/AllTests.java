package javaDevelopers.vipin.tests;

import junit.framework.*;

public class AllTests
    extends TestCase {

  public AllTests(String s) {
    super(s);
  }

  public static Test suite() {
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
    return suite;
  }
}
