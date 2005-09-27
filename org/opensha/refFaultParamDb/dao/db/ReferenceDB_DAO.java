package org.opensha.refFaultParamDb.dao.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.dao.exception.*;
import org.opensha.refFaultParamDb.dao.ReferenceDAO_API;

/**
 * <p>Title:ReferenceDB_DAO.java</p>
 * <p>Description: This class connects with database to access the Reference table </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ReferenceDB_DAO implements ReferenceDAO_API {
  private final static String SEQUENCE_NAME="Reference_Sequence";
  private final static String TABLE_NAME="Reference";
  private final static String REFERENCE_ID="Reference_Id";
  private final static String SHORT_CITATION="Short_Citation";
  private final static String FULL_BIBLIOGRAPHIC_REFERENCE="Full_Bibliographic_Reference";
  private DB_AccessAPI dbAccessAPI;

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

    String sql = "insert into "+TABLE_NAME+"("+ REFERENCE_ID+","+SHORT_CITATION+
        ","+this.FULL_BIBLIOGRAPHIC_REFERENCE+")"+
        " values ("+referenceId+",'"+reference.getShortCitation()+"','"+
        reference.getFullBiblioReference()+"')";
    try { dbAccessAPI.insertUpdateOrDeleteData(sql); }
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
    String sql = "update "+TABLE_NAME+" set "+SHORT_CITATION+"= '"+
        reference.getShortCitation()+"', "+this.FULL_BIBLIOGRAPHIC_REFERENCE+" = '"+
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
   * Get the reference info based on short citation
   *
   * @param shortCitation
   * @return
   * @throws QueryException
   */
  public Reference getReference(String shortCitation) throws QueryException {
    Reference reference=null;
    String condition  =  " where "+SHORT_CITATION+"='"+shortCitation+"'";
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
   * Returns a list of all short citations
   * @return
   * @throws QueryException
   */
  public ArrayList getAllShortCitations() throws QueryException {
    ArrayList referenceVOs = getAllReferences();
    ArrayList referencesNamesList = new ArrayList();
    for(int i=0; i<referenceVOs.size(); ++i) {
      referencesNamesList.add(((Reference)referenceVOs.get(i)).getShortCitation());
    }
    return referencesNamesList;

  }


  private ArrayList query(String condition) throws QueryException {
    ArrayList referenceList = new ArrayList();
    String sql = "select "+REFERENCE_ID+","+SHORT_CITATION+","+
        this.FULL_BIBLIOGRAPHIC_REFERENCE+" from "+TABLE_NAME+" "+condition+
        " order by "+this.SHORT_CITATION;
    try {
      ResultSet rs  = dbAccessAPI.queryData(sql);
      while(rs.next()) referenceList.add(new Reference(rs.getInt(REFERENCE_ID),
            rs.getString(this.SHORT_CITATION), rs.getString(this.FULL_BIBLIOGRAPHIC_REFERENCE)));
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return referenceList;
  }
}
