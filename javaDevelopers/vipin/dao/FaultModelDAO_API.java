package javaDevelopers.vipin.dao;
import javaDevelopers.vipin.vo.FaultModel;
import javaDevelopers.vipin.dao.exception.*;
import java.util.ArrayList;

/**
 * <p>Title: FaultModelDAO_API.java </p>
 * <p>Description: This saves the various fault models  </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface FaultModelDAO_API {

  /**
   * Add a new Fault Model to the list
   *
   * @param faultModel
   *
   */
  public int addFaultModel(FaultModel faultModel) throws InsertException;

 /**
  * Update the fault model info
  *
  * @param faultModelId  Id of the fault model which need to be updated
  * @param faultModel updated info about the fault model
  */
 public boolean updateFaultModel(int faultModelId, FaultModel faultModel) throws UpdateException;


 /**
  * Get the fault model info for a particular faultModelId
  * @param faultModelId
  * @return
  */
 public FaultModel getFaultModel(int faultModelId) throws QueryException;


 /**
  * Remove this Fault Model from the list
  * @param faultModelId
  */
 public boolean removeFaultModel(int faultModelId) throws UpdateException;

 /**
  * Returns a list of all Fault models.
  *
  * @return ArrayList containing list of FaultModel objects
  */
 public ArrayList getAllFaultModels() throws QueryException;

}
