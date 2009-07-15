package org.opensha.refFaultParamDb.dao.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.vo.Contributor;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.security.MessageDigest;
import java.util.Random;

/**
 * <p>Title:ContributorDB_DAO.java</p>
 * <p>Description: This class connects with database to access the Contributor table </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ContributorDB_DAO  {
  private final static String SEQUENCE_NAME="Contributors_Sequence";
  private final static String TABLE_NAME="Contributors";
  private final static String CONTRIBUTOR_ID="Contributor_Id";
  public final static String CONTRIBUTOR_NAME="Contributor_Name";
  private final static String FIRST_NAME = "First_Name";
  private final static String LAST_NAME = "Last_Name";
  private final static String EMAIL = "Email";
  private final static String PASSWORD = "Password";

  private DB_AccessAPI dbAccessAPI;

  /**
   * Constructor.
   * @param dbConnection
   */
  public ContributorDB_DAO(DB_AccessAPI dbAccessAPI) {
   setDB_Connection(dbAccessAPI);
  }


  public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
    this.dbAccessAPI = dbAccessAPI;
  }

  /**
   * Add a contributor to the contributor table
   * @param contributor
   * @return
   * @throws InsertException
   */
  public int addContributor(Contributor contributor, String password) throws InsertException {
    int contributorId = -1;
    try {
      contributorId = dbAccessAPI.getNextSequenceNumber(SEQUENCE_NAME);
   }catch(SQLException e) {
     throw new InsertException(e.getMessage());
   }
   String passwordStr=getEnryptedPassword(password);

   // insert into the table
   String sql = "insert into "+TABLE_NAME+"("+ CONTRIBUTOR_ID+","+CONTRIBUTOR_NAME+
       ","+FIRST_NAME+","+LAST_NAME+","+EMAIL+","+PASSWORD+")"+
       " values ("+contributorId+",'"+contributor.getName()+"','"+
       contributor.getFirstName()+"','"+contributor.getLastName()+"','"+
       contributor.getEmail()+"','"+passwordStr+"')";
   try { dbAccessAPI.insertUpdateOrDeleteData(sql); }
   catch(SQLException e) {
     //e.printStackTrace();
     throw new InsertException(e.getMessage());
   }
   return contributorId;
 }

  private String getEnryptedPassword(String password) {
    try {
     MessageDigest md = MessageDigest.getInstance("MD5");
     md.update(password.getBytes());
     return new sun.misc.BASE64Encoder().encode(md.digest());
     //return new String(md.digest());
   }catch(Exception e) {
     e.printStackTrace();
   }
   return null;
  }

  /**
   * Update a contributor in the table
   * @param contributorId
   * @param contributor
   * @throws UpdateException
   */
  public boolean updatePassword(String userName, String oldPassword,
                                String newPassword) throws UpdateException {
    String sql = "update "+TABLE_NAME+" set "+PASSWORD+"= '"+
        getEnryptedPassword(newPassword)+"' where "+this.CONTRIBUTOR_NAME+"='"+
        userName+"' and "+this.PASSWORD+"='"+getEnryptedPassword(oldPassword)+"'";
    try {
      int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;
  }


  /**
   * reset the password for a contributor in the database
   * @throws UpdateException
   */
  public String resetPasswordByEmail(String email) throws UpdateException {
    String randomPass = getRandomPassword();
    String sql = "update "+TABLE_NAME+" set "+PASSWORD+"= '"+
        getEnryptedPassword(randomPass)+"' where "+this.EMAIL+"='"+
        email+"'";
    try {
      int numRows = dbAccessAPI.resetPasswordByEmail(sql);
      if(numRows==1) return randomPass;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return null;
  }


  private int rand(int lo, int hi, Random rn) {
    int n = hi - lo + 1;
    int i = rn.nextInt() % n;
    if (i < 0)
      i = -i;
    return lo + i;
  }

  /**
   * Get random string
   * @return
   */
  private String getRandomPassword() {
    Random rn = new Random();
    int n = rand(8, 12, rn);
    byte b[] = new byte[n];
    for (int i = 0; i < n; i++)
      b[i] = (byte)rand('a', 'z', rn);
    return new String(b, 0);
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
   * Get the contributor info for a particular contributor name
   * @param name username for the contributor
   * @return
   */
  public Contributor getContributor(String name) throws QueryException {
    Contributor contributor=null;
    String condition  =  " where "+this.CONTRIBUTOR_NAME+"='"+name+"'";
    ArrayList contributorList = query(condition);
    if(contributorList.size()>0) contributor = (Contributor)contributorList.get(0);
    return contributor;
  }

  /**
  * Get the contributor info for a particular contributor email address
  * @param name username for the contributor
  * @return
  */
 public Contributor getContributorByEmail(String emailAdd) throws QueryException {
   Contributor contributor=null;
   String condition  =  " where "+this.EMAIL+"='"+emailAdd+"'";
   ArrayList contributorList = query(condition);
   if(contributorList.size()>0) contributor = (Contributor)contributorList.get(0);
   return contributor;
 }


  /**
   * Whether the provided username/password is valid
   * @param name
   * @param password
   * @return
   */
  public Contributor isContributorValid(String name, String password) {
    Contributor contributor=null;
    String condition  =  " where "+this.CONTRIBUTOR_NAME+"='"+name+"' and "+
        this.PASSWORD+"='"+getEnryptedPassword(password)+"'";
    ArrayList contributorList = query(condition);
    if(contributorList.size()>0) return (Contributor)contributorList.get(0);
    else return null;

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
      int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
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
      ResultSet rs  = dbAccessAPI.queryData(sql);
      while(rs.next()) contributorList.add(new Contributor(rs.getInt(CONTRIBUTOR_ID), rs.getString(CONTRIBUTOR_NAME)));
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return contributorList;
  }
}