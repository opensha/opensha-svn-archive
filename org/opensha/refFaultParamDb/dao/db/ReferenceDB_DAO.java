package org.opensha.refFaultParamDb.dao.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.dao.exception.*;

/**
 * <p>Title:ReferenceDB_DAO.java</p>
 * <p>Description: This class connects with database to access the Reference table </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ReferenceDB_DAO {
  private final static String SEQUENCE_NAME="Reference_Sequence";
  private final static String TABLE_NAME="Reference";
  private final static String REFERENCE_ID="Reference_Id";
  private final static String REF_YEAR="Ref_Year";
  private final static String REF_AUTH = "Ref_Auth";
  private final static String FULL_BIBLIOGRAPHIC_REFERENCE="Full_Bibliographic_Reference";
  private final static String QFAULT_REFERENCE_ID= "QFault_Reference_Id";
  private DB_AccessAPI dbAccessAPI;
  private static ArrayList referenceList;

  /**
   * Constructor.
   * @param dbConnection
   */
  public ReferenceDB_DAO(DB_AccessAPI dbAccessAPI) {
   setDB_Connection(dbAccessAPI);
  }


  public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
    this.dbAccessAPI = dbAccessAPI;
  }

  /**
   * Add a reference to the reference table
   * @param reference
   * @return
   * @throws InsertException
   */
  public int addReference(Reference reference) throws InsertException {
    int referenceId = -1;
    try {
      referenceId = dbAccessAPI.getNextSequenceNumber(SEQUENCE_NAME);
   }catch(SQLException e) {
     throw new InsertException(e.getMessage());
   }

    String sql = "insert into "+TABLE_NAME+"("+ REFERENCE_ID+","+REF_AUTH+","+
        REF_YEAR+","+this.FULL_BIBLIOGRAPHIC_REFERENCE+")"+
        " values ("+referenceId+",'"+reference.getRefAuth()+"','"+
        reference.getRefYear()+"','"+reference.getFullBiblioReference()+"')";
    try {
      dbAccessAPI.insertUpdateOrDeleteData(sql);
      referenceList.add(reference); // add to cached list of references
    }
    catch(SQLException e) {
      //e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
    return referenceId;
  }

  /**
   * Update a reference in the table
   * @param referenceId
   * @param reference
   * @throws UpdateException
   */
  public boolean updateReference(int referenceId, Reference reference) throws UpdateException {
    String sql = "update "+TABLE_NAME+" set "+REF_AUTH+"= '"+
        reference.getRefAuth()+"',"+ this.REF_YEAR+"="+reference.getRefYear()+","+
        this.FULL_BIBLIOGRAPHIC_REFERENCE+"='"+
        reference.getFullBiblioReference()+"' where "+REFERENCE_ID+"="+referenceId;
    try {
      int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;
  }


  /**
   * Get reference corresponding to an Id
   * @param referenceId
   * @return
   * @throws QueryException
   */
  public Reference getReference(int referenceId) throws QueryException {
    Reference reference=null;
    String condition  =  " where "+REFERENCE_ID+"="+referenceId;
    ArrayList referenceList = query(condition);
    if(referenceList.size()>0) reference = (Reference)referenceList.get(0);
    return reference;
  }

  /**
   * Remove a reference from the table
   *
   * @param referenceId
   * @throws UpdateException
   */
  public boolean removeReference(int referenceId) throws UpdateException {
    String sql = "delete from "+TABLE_NAME+"  where "+REFERENCE_ID+"="+referenceId;
    try {
      int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;
  }

  /**
   * Get a list of all the references ordered by short citation
   *
   * @return
   * @throws QueryException
   */
  public ArrayList getAllReferences() throws QueryException {
    return query(" ");
  }

  /**
   * Get a list of summary for all references
   * @return
   * @throws QueryException
   */
  public ArrayList getAllReferencesSummary() throws QueryException {
    if(referenceList!=null) return referenceList;
    referenceList = new ArrayList();
    String sql = "select "+REFERENCE_ID+","+this.REF_YEAR+","+
        this.REF_AUTH+" from "+TABLE_NAME+" order by "+this.REF_AUTH;
    try {
      ResultSet rs  = dbAccessAPI.queryData(sql);
      while(rs.next())  {
        Reference referenceSummary = new Reference();
        referenceSummary.setRefAuth(rs.getString(this.REF_AUTH));
        referenceSummary.setReferenceId(rs.getInt(REFERENCE_ID));
        referenceSummary.setRefYear(rs.getString(this.REF_YEAR));
        referenceList.add(referenceSummary);
      }
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return referenceList;
  }


  private ArrayList query(String condition) throws QueryException {
    ArrayList referenceList = new ArrayList();
    String sql = "select "+REFERENCE_ID+","+this.REF_YEAR+","+
        this.REF_AUTH+","+ QFAULT_REFERENCE_ID+","+this.FULL_BIBLIOGRAPHIC_REFERENCE+" from "+TABLE_NAME+
        " "+condition+" order by "+this.REF_AUTH;
    try {
      ResultSet rs  = dbAccessAPI.queryData(sql);
      while(rs.next())  {
        Reference reference = new Reference(rs.getInt(REFERENCE_ID),
                                            rs.getString(this.REF_AUTH),
                                            rs.getString(this.REF_YEAR),
                                            rs.getString(this.FULL_BIBLIOGRAPHIC_REFERENCE));
        int qFaultRefId = rs.getInt(QFAULT_REFERENCE_ID);
        if(!rs.wasNull()) reference.setQfaultReferenceId(qFaultRefId);
        referenceList.add(reference);
      }
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return referenceList;
  }
}
