package org.opensha.sha.calc.remoteCalc;

import java.rmi.Remote;

import org.opensha.sha.calc.SpectrumCalculatorAPI;

/**
 * <p>Title: RemoteResponseSpectrumFactoryAPI</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @date June 21, 2006
 * @version 1.0
 */

public interface RemoteResponseSpectrumFactoryAPI extends Remote {

  /**
   * Get the reference to the instance of the HazardCurveCalculator
   * @return
   * @throws java.rmi.RemoteException
   */
  public SpectrumCalculatorAPI getRemoteResponseSpectrumCalculator()
      throws java.rmi.RemoteException;

}
