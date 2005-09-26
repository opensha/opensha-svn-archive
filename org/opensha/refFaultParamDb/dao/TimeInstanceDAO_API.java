package org.opensha.refFaultParamDb.dao;

import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.util.ArrayList;

/**
 * <p>Title: TimeInstanceDAO_API.java </p>
 * <p>Description: This interface puts/gets time(which can be a time estimate or
 * exact time) </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface TimeInstanceDAO_API {
  /**
   * Add a new timeAPI object to the database
   *
   * @param estimate
   *
   */
  public int addTimeInstance(TimeAPI timeAPI) throws InsertException;


 /**
  * Get the time info for a particular timeInstanceId
  */
 public TimeAPI getTimeInstance(int timeInstanceId) throws QueryException;


 /**
  * Remove the time instance from the list
  */
 public boolean removeTimeInstance(int  timeInstanceId) throws UpdateException;

}
