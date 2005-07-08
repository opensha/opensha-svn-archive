package javaDevelopers.vipin.dao.db;

import javaDevelopers.vipin.dao.EstimateDAO_API;
import org.opensha.data.estimate.DiscreteValueEstimate;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.estimate.Estimate;
import javaDevelopers.vipin.dao.exception.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
/**
 * <p>Title: DiscreteValueEstimateDB_DAO.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DiscreteValueEstimateDB_DAO implements EstimateDAO_API {

  public final static String EST_TYPE_NAME="DiscreteValueEstimate";
  private final static String ERR_MSG = "This class just deals with Discrete Estimates";
  private XY_EstimateDB_DAO xyEstimateDB_DAO  = new XY_EstimateDB_DAO();

 /**
  * Constructor.
  * @param dbConnection
  */
 public DiscreteValueEstimateDB_DAO(DB_Connection dbConnection) {
   setDB_Connection(dbConnection);
 }

 public DiscreteValueEstimateDB_DAO() { }


 public void setDB_Connection(DB_Connection connection) {
   xyEstimateDB_DAO.setDB_Connection(connection);
 }

 /**
  * Add the normal estimate into the database table
  * @param estimateInstanceId
  * @param estimate
  * @throws InsertException
  */
  public void addEstimate(int estimateInstanceId, Estimate estimate) throws InsertException {
    if(!(estimate instanceof DiscreteValueEstimate)) throw new InsertException(ERR_MSG);
    DiscreteValueEstimate discreteValueEstimate = (DiscreteValueEstimate)estimate;
    xyEstimateDB_DAO.addEstimate(estimateInstanceId, discreteValueEstimate.getValues());
  }

  /**
   *
   * @param estimateInstanceId
   * @return
   * @throws QueryException
   */
  public Estimate getEstimate(int estimateInstanceId) throws QueryException {
    ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
    xyEstimateDB_DAO.getEstimate(estimateInstanceId,func);
    DiscreteValueEstimate estimate=new DiscreteValueEstimate(func);
    return estimate;
  }

  /**
   *
   * @param estimateInstanceId
   * @return
   * @throws UpdateException
   */
  public boolean removeEstimate(int estimateInstanceId) throws UpdateException {
    return xyEstimateDB_DAO.removeEstimate(estimateInstanceId);
  }

  public String getEstimateTypeName() {
    return EST_TYPE_NAME;
  }

}
