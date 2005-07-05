package javaDevelopers.vipin.dao;

import java.util.ArrayList;
import javaDevelopers.vipin.vo.EstimateType;
import javaDevelopers.vipin.dao.exception.QueryException;

/**
 * <p>Title: EstimateTypeDAO_API.java </p>
 * <p>Description: Retrieves the various types of estimates from the
 * database </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface EstimateTypeDAO_API {
  /**
   * Get a list of all estimates.
   *
   * @return a list of estimateType objects
   */
  public ArrayList getAllEstimateTypes() throws QueryException;

  /**
   * Get the estimatetype object based on estimate name
   *
   * @param estimateName
   * @return
   */
  public EstimateType getEstimateType(String estimateName) throws QueryException ;

  /**
   * Get the estimate based on estimate type Id
   *
   * @param estimateTypeId
   * @return
   */
  public EstimateType getEstimateType(int estimateTypeId) throws QueryException;

}