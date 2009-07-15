package org.opensha.refFaultParamDb.tests.dao.db;

import junit.framework.*;
import org.opensha.refFaultParamDb.dao.db.*;
import org.opensha.refFaultParamDb.dao.exception.*;
import org.opensha.refFaultParamDb.vo.*;
import java.util.*;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.tests.AllTests;

/**
 *
 * <p>Title: Test_QFault2002B_DB_DAO.java </p>
 * <p>Description: Test the QFault2002B DB DAO class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class Test_QFault2002B_DB_DAO extends TestCase {
  private DB_AccessAPI dbConnection;
  private QFault2002B_DB_DAO qFaultDB_DAO = null;

  public Test_QFault2002B_DB_DAO(String name) {
    super(name);
    dbConnection = AllTests.dbConnection;
  }

  protected void setUp() throws Exception {
    super.setUp();
    qFaultDB_DAO = new QFault2002B_DB_DAO(dbConnection);
  }

  protected void tearDown() throws Exception {
    qFaultDB_DAO = null;
    super.tearDown();
  }


  public void testQFault2002B_DB_DAO() {
    qFaultDB_DAO = new QFault2002B_DB_DAO(dbConnection);
    this.assertNotNull("QFault2002B_DB_DAO object should not be null",qFaultDB_DAO);
  }


  public void testGetAllFaultSections() throws QueryException {
    ArrayList actualReturn = qFaultDB_DAO.getAllFaultSections();
    assertEquals("Should have 57 fault sections in the table", 57, actualReturn.size());
  }

  public void testGetFaultSection() throws QueryException {
    QFault2002B actualReturn = qFaultDB_DAO.getFaultSection("69a");
    assertNotNull("should not be null as fault section exists with id = 69a",actualReturn);
  }

}
