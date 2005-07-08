package javaDevelopers.vipin.dao.db;


import javaDevelopers.vipin.dao.exception.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.opensha.data.function.DiscretizedFuncAPI;
/**
 * <p>Title: XY_EstimateDB_DAO.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class XY_EstimateDB_DAO  {

  private final static String TABLE_NAME="XY_Est";
  private final static String EST_ID="Est_Id";
  private final static String X="X";
  private final static String Y="Y";
  private DB_Connection dbConnection;

 /**
  * Constructor.
  * @param dbConnection
  */
 public XY_EstimateDB_DAO(DB_Connection dbConnection) {
   setDB_Connection(dbConnection);
 }

 public XY_EstimateDB_DAO() { }


 public void setDB_Connection(DB_Connection connection) {
   this.dbConnection = connection;
 }

 /**
  * Add the normal estimate into the database table
  * @param estimateInstanceId
  * @param estimate
  * @throws InsertException
  */
  public void addEstimate(int estimateInstanceId, DiscretizedFuncAPI func) throws InsertException {
    int numPoints = func.getNum();
    try {
      for (int i = 0; i < numPoints; ++i) {
        // insert into log normal table
        String sql = "insert into " + TABLE_NAME + "(" + EST_ID + "," + X + "," +
            Y + ")" +
            " values (" + estimateInstanceId + "," + func.getX(i) + "," +
            func.getY(i) + ")";
        dbConnection.insertUpdateOrDeleteData(sql);
      }
    }
    catch (SQLException e) {
      //e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
  }

  /**
   *
   * @param estimateInstanceId
   * @return
   * @throws QueryException
   */
  public void getEstimate(int estimateInstanceId, DiscretizedFuncAPI func) throws QueryException {
    String condition = " where " + EST_ID + "=" + estimateInstanceId;
    String sql = "select "+EST_ID+","+X+","+Y+" from "+TABLE_NAME+" "+condition;
   try {
     ResultSet rs  = dbConnection.queryData(sql);
     while(rs.next()) {
       func.set(rs.getFloat("X"),rs.getFloat("Y"));
     }
     rs.close();
     rs.getStatement().close();
   } catch(SQLException e) { throw new QueryException(e.getMessage()); }
  }

  /**
   *
   * @param estimateInstanceId
   * @return
   * @throws UpdateException
   */
  public boolean removeEstimate(int estimateInstanceId) throws UpdateException {
    String sql = "delete from "+TABLE_NAME+"  where "+EST_ID+"="+estimateInstanceId;
     try {
       int numRows = dbConnection.insertUpdateOrDeleteData(sql);
       if(numRows>=1) return true;
     }
     catch(SQLException e) { throw new UpdateException(e.getMessage()); }
     return false;
  }

}
