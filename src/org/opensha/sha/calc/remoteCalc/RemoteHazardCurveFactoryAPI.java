package org.opensha.sha.calc.remoteCalc;

import java.rmi.Remote;

import org.opensha.sha.calc.HazardCurveCalculatorAPI;

/**
 * <p>Title: RemoteHazardCurveFactoryAPI</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @date April 28, 2004
 * @version 1.0
 */

public interface RemoteHazardCurveFactoryAPI extends Remote {

  /**
   * Get the reference to the instance of the HazardCurveCalculator
   * @return
   * @throws java.rmi.RemoteException
   */
  public HazardCurveCalculatorAPI getRemoteHazardCurveCalculator() throws java.rmi.RemoteException;

}
