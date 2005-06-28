package javaDevelopers.vipin.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import javaDevelopers.vipin.vo.Contributor;
import javaDevelopers.vipin.dao.db.DB_Connection;
import javaDevelopers.vipin.dao.exception.*;

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
  public int addContributor(Contributor contributor) throws InsertException {
    String sql = "insert into "+TABLE_NAME+"("+ CONTRIBUTOR_ID+","+CONTRIBUTOR_NAME+")"+
        " values("+contributor.getId()+",\""+contributor.getName()+"\")";
    try { dbConnection.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) { throw new InsertException(e.getMessage()); }
    return -1;
  }

  /**
   * Update a contributor in the table
   * @param contributorId
   * @param contributor
   * @throws UpdateException
   */
  public void updateContributor(int contributorId, Contributor contributor) throws UpdateException {
    String sql = "update "+TABLE_NAME+" set "+CONTRIBUTOR_NAME+"=\""+
        contributor.getName()+"\" where "+CONTRIBUTOR_ID+"="+contributorId;
    try { dbConnection.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
  }


  public Contributor getContributor(int contributorId) throws QueryException {
    /**@todo Implement this javaDevelopers.vipin.dao.ContributorDAO_API method*/
    throw new java.lang.UnsupportedOperationException("Method getContributor() not yet implemented.");
  }

  public void removeContributor(int contributorId) throws UpdateException {
    /**@todo Implement this javaDevelopers.vipin.dao.ContributorDAO_API method*/
    throw new java.lang.UnsupportedOperationException("Method removeContributor() not yet implemented.");
  }

  public ArrayList getAllContributors() throws QueryException {
    /**@todo Implement this javaDevelopers.vipin.dao.ContributorDAO_API method*/
    throw new java.lang.UnsupportedOperationException("Method getAllContributors() not yet implemented.");
  }

}