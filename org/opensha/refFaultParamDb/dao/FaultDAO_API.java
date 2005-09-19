package org.opensha.refFaultParamDb.dao;

import org.opensha.refFaultParamDb.vo.Fault;
import java.util.ArrayList;

/**
 * <p>Title: FaultDAO_API.java </p>
 * <p>Description: This is interface to get information about faults </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface FaultDAO_API {
  /**
   * Get the information about a fault based on fault Id
   * @param faultId
   * @return
   */
  public Fault getFault(int faultId);

  /**
   * Get information about a fault based on fault name
   * @param faultName
   * @return
   */
  public Fault getFault(String faultName);

  /**
   * Get a list of all the faults existing itn database
   * @return
   */
  public ArrayList getAllFaults();
}