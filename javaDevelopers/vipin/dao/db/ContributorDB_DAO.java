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
  private final static String TABLE_NAME="Contributors";
  private final static String CONTRIBUTOR_ID="Contributor_Id";
  private final static String CONTRIBUTOR_NAME="Contributor_Name";
  private DB_Connection dbConnection;

  /**
   * Constructor.
   * @param dbConnection
   */
  public ContributorDB_DAO(DB_Connection dbConnection) {
    this.dbConnection = dbConnection;
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
  public void addContributor(Contributor contributor) throws InsertException {
    String sql = "insert into "+TABLE_NAME+"("+ CONTRIBUTOR_ID+","+CONTRIBUTOR_NAME+")"+
        " values ("+contributor.getId()+",'"+contributor.getName()+"')";
    //String sql = "insert into Contributors(Contributor_Id,Contributor_Name) values (1,'Test 1')";
    System.out.println(sql);
    try { dbConnection.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) {
      e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
  }

  /**
   * Update a contributor in the table
   * @param contributorId
   * @param contributor
   * @throws UpdateException
   */
  public void updateContributor(int contributorId, Contributor contributor) throws UpdateException {
    String sql = "update "+TABLE_NAME+" set "+CONTRIBUTOR_NAME+"= '"+
        contributor.getName()+"' where "+CONTRIBUTOR_ID+"="+contributorId;
    try { dbConnection.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
  }


  /**
   * Get contributor corresponding to an Id
   * @param contributorId
   * @return
   * @throws QueryException
   */
  public Contributor getContributor(int contributorId) throws QueryException {
    Contributor contributor=null;
    String sql = "select "+CONTRIBUTOR_ID+","+CONTRIBUTOR_NAME+" from "+TABLE_NAME+
        " where "+CONTRIBUTOR_ID+"="+contributorId;
    try {
      ResultSet rs  = dbConnection.queryData(sql);
      while(rs.next()) contributor = new Contributor(rs.getInt(0), rs.getString(1));
      //rs.close();
      //rs.getStatement().close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return contributor;
  }

  /**
   * Remove a contributor from the table
   *
   * @param contributorId
   * @throws UpdateException
   */
  public void removeContributor(int contributorId) throws UpdateException {
    String sql = "delete from "+TABLE_NAME+"  where "+CONTRIBUTOR_ID+"="+contributorId;
    try { dbConnection.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
  }

  /**
   * Get a list of all the contributors
   *
   * @return
   * @throws QueryException
   */
  public ArrayList getAllContributors() throws QueryException {
    ArrayList contributorList = new ArrayList();
    String sql = "select "+CONTRIBUTOR_ID+","+CONTRIBUTOR_NAME+" from "+TABLE_NAME;
    try {
      ResultSet rs  = dbConnection.queryData(sql);
      while(rs.next()) contributorList.add(new Contributor(rs.getInt(0), rs.getString(1)));
      //rs.close();
      //rs.getStatement().close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return contributorList;
  }

}