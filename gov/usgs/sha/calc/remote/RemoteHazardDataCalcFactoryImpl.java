package gov.usgs.sha.calc.remote;

import java.rmi.*;
import java.rmi.server.*;

import gov.usgs.sha.calc.*;
import gov.usgs.sha.calc.api.*;
import gov.usgs.sha.calc.remote.api.*;

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
   * @return gov.usgs.sha.calc.api.HazardDataCalcAPI
   * @throws RemoteException
   * @todo Implement this
   *   gov.usgs.sha.calc.remote.api.RemoteHazardDataCalcFactoryAPI method
   */
  public HazardDataCalcAPI getRemoteHazardDataCalc() throws RemoteException {
    HazardDataCalcAPI hazardDataCalc = new HazardDataCalc();
    return hazardDataCalc;
  }
}
