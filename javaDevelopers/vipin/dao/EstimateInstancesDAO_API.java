package javaDevelopers.vipin.dao;

import javaDevelopers.vipin.vo.EstimateInstances;
import javaDevelopers.vipin.dao.exception.*;
import java.util.ArrayList;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface EstimateInstancesDAO_API {
  /**
   * Add a new estimate object to the database
   *
   * @param estimate
   *
   */
  public int addEstimateInstance(EstimateInstances estimate) throws InsertException;


 /**
  * Get the Estimate Instances info for a particular estimateInstanceId
  */
 public EstimateInstances getEstimateInstance(int estimateInstanceId) throws QueryException;

 public ArrayList getAllEstimateInstances() throws QueryException;


 /**
  * Remove the Estimate from the list
  */
 public boolean removeEstimateInstance(int estimateInstanceId) throws UpdateException;



}