package javaDevelopers.vipin.dao;

import org.opensha.data.estimate.Estimate;
import javaDevelopers.vipin.dao.exception.*;
/**
 * <p>Title: NormalEstimateDAO_API.java </p>
 * <p>Description: Inserts/gets/delete normal estimates from the tables</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface EstimateDAO_API {

  /**
   * Add a new  estimate object to the database
   *
   */
  public void addEstimate(int estimateInstanceId, Estimate estimate) throws InsertException;


  /**
   * Get the  Estimate Instance info for a particular estimateInstanceId
   */
  public Estimate getEstimate(int estimateInstanceId) throws QueryException;


  /**
   * Remove the  Estimate from the list
   */
  public boolean removeEstimate(int estimateInstanceId) throws UpdateException;

  public String getEstimateTypeName();
}