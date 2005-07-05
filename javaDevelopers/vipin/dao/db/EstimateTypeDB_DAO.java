package javaDevelopers.vipin.dao.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import javaDevelopers.vipin.dao.EstimateTypeDAO_API;
import javaDevelopers.vipin.dao.exception.QueryException;
import java.util.ArrayList;
import javaDevelopers.vipin.vo.EstimateType;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EstimateTypeDB_DAO implements EstimateTypeDAO_API {

  private final static String TABLE_NAME="Est_Type";
  private final static String EST_TYPE_ID="Est_Type_Id";
  private final static String EST_NAME="Est_Name";
  private final static String EFFECTIVE_DATE="Effective_Date";
  private DB_Connection dbConnection;

 /**
  * Constructor.
  * @param dbConnection
  */
 public EstimateTypeDB_DAO(DB_Connection dbConnection) {
   setDB_Connection(dbConnection);
 }


 public void setDB_Connection(DB_Connection connection) {
   this.dbConnection = connection;
 }

 /**
  * Return a list of all available estimates
  * @return
  * @throws QueryException
  */
  public ArrayList getAllEstimateTypes() throws QueryException {
    return query(" ");
  }

  /**
   * Get a estimate based on estimate name
   * @param estimateName
   * @return
   * @throws QueryException
   */
  public EstimateType getEstimateType(String estimateName) throws QueryException {
    EstimateType estimateType=null;
    String condition = " where "+EST_NAME+"='"+estimateName+"'";
    ArrayList estimateTypeList=query(condition);
    if(estimateTypeList.size()>0) estimateType = (EstimateType)estimateTypeList.get(0);
    return estimateType;
  }

  /**
   * Get estimate based on estimate type id
   *
   * @param estimateTypeId
   * @return
   * @throws QueryException
   */
  public EstimateType getEstimateType(int estimateTypeId) throws QueryException {
    EstimateType estimateType=null;
    String condition = " where "+EST_TYPE_ID+"="+estimateTypeId+"";
    ArrayList estimateTypeList=query(condition);
    if(estimateTypeList.size()>0) estimateType = (EstimateType)estimateTypeList.get(0);
    return estimateType;
  }


  private ArrayList query(String condition) throws QueryException {
   ArrayList estimateTypeList = new ArrayList();
   String sql =  "select "+EST_TYPE_ID+","+EST_NAME+","+EFFECTIVE_DATE+" from "+TABLE_NAME+condition;
   try {
     ResultSet rs  = dbConnection.queryData(sql);
     while(rs.next()) estimateTypeList.add(new EstimateType(rs.getInt(EST_TYPE_ID),
           rs.getString(EST_NAME),rs.getDate(EFFECTIVE_DATE)));
     rs.close();
     rs.getStatement().close();
   } catch(SQLException e) { throw new QueryException(e.getMessage()); }
   return estimateTypeList;
 }


}