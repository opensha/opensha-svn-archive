package org.opensha.refFaultParamDb.tests.dao.db;

import junit.framework.TestCase;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.LogTypeDB_DAO;
import org.opensha.refFaultParamDb.dao.exception.QueryException;
import org.opensha.refFaultParamDb.tests.AllTests;
/**
 *
 * <p>Title: TestLogTypeDB_DAO.java </p>
 * <p>Description: Test the LogType DB DAO class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class TestLogTypeDB_DAO extends TestCase {
  private DB_AccessAPI dbConnection;
  private LogTypeDB_DAO logTypeDB_DAO = null;

  public TestLogTypeDB_DAO(String name) {
    super(name);
    dbConnection = AllTests.dbConnection;
  }

  protected void setUp() throws Exception {
    super.setUp();
    logTypeDB_DAO = new LogTypeDB_DAO(dbConnection);
  }

  protected void tearDown() throws Exception {
    logTypeDB_DAO = null;
    super.tearDown();
  }


  public void testLogTypeDB_DAO() {
    logTypeDB_DAO = new LogTypeDB_DAO(dbConnection);
    this.assertNotNull("logTypeDB_DAO object should not be null",logTypeDB_DAO);
  }

  public void testGetLogType() throws QueryException {
    int actualReturn = logTypeDB_DAO.getLogTypeId("12");
    assertEquals( -1, actualReturn);
    actualReturn = logTypeDB_DAO.getLogTypeId("10");
    assertEquals( 1, actualReturn);
    actualReturn = logTypeDB_DAO.getLogTypeId("E");
    assertEquals( 2, actualReturn);
    actualReturn = logTypeDB_DAO.getLogTypeId("e");
    assertEquals( -1, actualReturn);
    String logBase = logTypeDB_DAO.getLogBase(1);
    assertEquals("10",logBase);
    logBase = logTypeDB_DAO.getLogBase(2);
    assertEquals("E",logBase);
  }

}
