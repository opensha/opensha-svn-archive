package javaDevelopers.vipin.dao.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import javaDevelopers.vipin.vo.Contributor;
import javaDevelopers.vipin.dao.exception.*;
import javaDevelopers.vipin.dao.ContributorDAO_API;
import javaDevelopers.vipin.vo.Contributor;

/**
 * <p>Title:ContributorDB_DAO.java</p>
 * <p>Description: This class connects with database to access the Contributor table </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ContributorDB_DAO implements ContributorDAO_API {
  private final static String SEQUENCE_NAME="Contributors_Sequence";
  private final static String TABLE_NAME="Contributors";
  private final static String CONTRIBUTOR_ID="Contributor_Id";
  private final static String CONTRIBUTOR_NAME="Contributor_Name";
  private DB_Connection dbConnection;

  /**
   * Constructor.
   * @param dbConnection
   */
  public ContributorDB_DAO(DB_Connection dbConnection) {
   setDB_Connection(dbConnection);
  }


  public void setDB_Connection(DB_Connection connection) {
    this.dbConnection = connection;
  }

  /**
   * Add a contributor to the contributor table
   * @param contributor
   * @return
   * @throws InsertException
   */
  public int addContributor(Contributor contributor) throws InsertException {
    int contributorId = -1;
    try {
      contributorId = dbConnection.getNextSequenceNumber(SEQUENCE_NAME);
   }catch(SQLException e) {
     throw new InsertException(e.getMessage());
   }

    String sql = "insert into "+TABLE_NAME+"("+ CONTRIBUTOR_ID+","+CONTRIBUTOR_NAME+")"+
        " values ("+contributorId+",'"+contributor.getName()+"')";
    try { dbConnection.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) {
      //e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
    return contributorId;
  }

  /**
   * Update a contributor in the table
   * @param contributorId
   * @param contributor
   * @throws UpdateException
   */
  public boolean updateContributor(int contributorId, Contributor contributor) throws UpdateException {
    String sql = "update "+TABLE_NAME+" set "+CONTRIBUTOR_NAME+"= '"+
        contributor.getName()+"' where "+CONTRIBUTOR_ID+"="+contributorId;
    try {
      int numRows = dbConnection.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;
  }


  /**
   * Get contributor corresponding to an Id
   * @param contributorId
   * @return
   * @throws QueryException
   */
  public Contributor getContributor(int contributorId) throws QueryException {
    Contributor contributor=null;
    String condition  =  " where "+CONTRIBUTOR_ID+"="+contributorId;
    ArrayList contributorList = query(condition);
    if(contributorList.size()>0) contributor = (Contributor)contributorList.get(0);
    return contributor;
  }

  /**
   * Remove a contributor from the table
   *
   * @param contributorId
   * @throws UpdateException
   */
  public boolean removeContributor(int contributorId) throws UpdateException {
    String sql = "delete from "+TABLE_NAME+"  where "+CONTRIBUTOR_ID+"="+contributorId;
    try {
      int numRows = dbConnection.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;
  }

  /**
   * Get a list of all the contributors
   *
   * @return
   * @throws QueryException
   */
  public ArrayList getAllContributors() throws QueryException {
    return query(" ");
  }

  private ArrayList query(String condition) throws QueryException {
    ArrayList contributorList = new ArrayList();
    String sql = "select "+CONTRIBUTOR_ID+","+CONTRIBUTOR_NAME+" from "+TABLE_NAME+" "+condition;
    try {
      ResultSet rs  = dbConnection.queryData(sql);
      while(rs.next()) contributorList.add(new Contributor(rs.getInt(CONTRIBUTOR_ID), rs.getString(CONTRIBUTOR_NAME)));
      rs.close();
      rs.getStatement().close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return contributorList;
  }
}