package org.scec.sha.calc.remoteHazardCalc;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.scec.sha.calc.*;

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