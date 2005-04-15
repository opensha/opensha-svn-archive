package org.opensha.nshmp.sha.calc.remote.api;

import java.rmi.*;

import org.opensha.nshmp.sha.calc.api.*;

/**
 * <p>Title: RemoteHazardDataCalcFactoryAPI</p>
 *
 * <p>Description: This class gets the new instance of the
 * Remote Hazard Data Calc to the user.</p>
 *
 * @author Ned Field, Nitin Gupta , E.V.Leyendecker
 * @version 1.0
 */
public interface RemoteHazardDataCalcFactoryAPI
    extends Remote {

  /**
   * Get the reference to the instance of the HazardDataCalc
   * @return
   * @throws java.rmi.RemoteException
   */
  public HazardDataCalcAPI getRemoteHazardDataCalc() throws java.rmi.
      RemoteException;

}
