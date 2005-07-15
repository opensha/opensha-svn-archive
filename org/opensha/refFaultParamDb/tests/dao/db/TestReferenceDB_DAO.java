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
 * <p>Title: TestReferenceDB_DAO.java </p>
 * <p>Description: Test the Reference DB DAO class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class TestReferenceDB_DAO extends TestCase {
  private DB_AccessAPI dbConnection;
  private ReferenceDB_DAO referenceDB_DAO = null;
  private static int primaryKey1, primaryKey2;

  public TestReferenceDB_DAO(String name) {
    super(name);
    dbConnection = AllTests.dbConnection;
  }

  protected void setUp() throws Exception {
    super.setUp();
    referenceDB_DAO = new ReferenceDB_DAO(dbConnection);
  }

  protected void tearDown() throws Exception {
    referenceDB_DAO = null;
    super.tearDown();
  }


  public void testReferenceDB_DAO() {
    referenceDB_DAO = new ReferenceDB_DAO(dbConnection);
    this.assertNotNull("reference_DAO object should not be null",referenceDB_DAO);
  }


  public void testAddReference() throws InsertException {
    Reference reference1 = new Reference("Test1");
    Reference reference2 = new Reference("Test2");
    Reference reference3 = new Reference("Test3");
    primaryKey1 = referenceDB_DAO.addReference(reference1);
    primaryKey2 = referenceDB_DAO.addReference(reference3);
    assertTrue(primaryKey1!=primaryKey2);
  }

  public void testGetAllReferences() throws QueryException {
    ArrayList actualReturn = referenceDB_DAO.getAllReferences();
    assertEquals("Should have 2 references in the table", 2, actualReturn.size());
  }

  public void testGetReference() throws QueryException {
    Reference actualReturn = referenceDB_DAO.getReference(67866);
    assertEquals("No reference exists with id 67866", null, actualReturn);
    actualReturn = referenceDB_DAO.getReference(primaryKey1);
    assertNotNull("should not be null as reference exists with id = "+primaryKey1,actualReturn);
    assertEquals("Test1", actualReturn.getReferenceName());
  }

  public void testUpdateReference() throws UpdateException {
    Reference reference = new Reference(7879,"Test2");
    boolean status  = referenceDB_DAO.updateReference(7879, reference);
    this.assertFalse("cannot update reference with 7879 as it does not exist", status);
    reference = new Reference(primaryKey1,"TestTest1");
    status = referenceDB_DAO.updateReference(primaryKey1, reference);
    assertTrue("reference with id="+primaryKey1+ " should be updated in the database",status);
    Reference actualReturn = referenceDB_DAO.getReference(primaryKey1);
    assertNotNull("should not be null as reference exists with id = "+primaryKey1,actualReturn);
    assertEquals("reference id "+primaryKey1+" has name test 1", "TestTest1", actualReturn.getReferenceName());
  }

  public void testRemoveReference() throws UpdateException {
    boolean status = referenceDB_DAO.removeReference(7878);
    this.assertFalse("cannot remove reference with 7878 as it does not exist", status);
    status = referenceDB_DAO.removeReference(primaryKey2);
    assertTrue("reference with id="+primaryKey2+" should be removed from the database",status);
    assertEquals("should now contain only 1 reference",1, referenceDB_DAO.getAllReferences().size());
    status=referenceDB_DAO.removeReference(primaryKey1);
    assertTrue("reference with id="+primaryKey1+" should be removed from the database",status);
    assertEquals("should now contain only 0 reference",0, referenceDB_DAO.getAllReferences().size());
  }
}
