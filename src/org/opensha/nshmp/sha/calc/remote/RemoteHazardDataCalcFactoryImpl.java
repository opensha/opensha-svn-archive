package org.opensha.nshmp.sha.calc.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.opensha.nshmp.sha.calc.HazardDataCalc;
import org.opensha.nshmp.sha.calc.api.HazardDataCalcAPI;
import org.opensha.nshmp.sha.calc.remote.api.RemoteHazardDataCalcFactoryAPI;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RemoteHazardDataCalcFactoryImpl
    extends UnicastRemoteObject implements RemoteHazardDataCalcFactoryAPI {

  public RemoteHazardDataCalcFactoryImpl() throws RemoteException {
  }

  /**
   *
   * @return org.opensha.nshmp.sha.calc.api.HazardDataCalcAPI
   * @throws RemoteException
   * @todo Implement this
   *   org.opensha.nshmp.sha.calc.remote.api.RemoteHazardDataCalcFactoryAPI method
   */
  public HazardDataCalcAPI getRemoteHazardDataCalc() throws RemoteException {
    HazardDataCalcAPI hazardDataCalc = new HazardDataCalc();
    return hazardDataCalc;
  }
}
