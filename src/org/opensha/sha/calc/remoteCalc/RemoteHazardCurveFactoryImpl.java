package org.opensha.sha.calc.remoteCalc;

import java.rmi.server.UnicastRemoteObject;

import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.HazardCurveCalculatorAPI;

/**
 * <p>Title: RemoteHazardCurveFactoryImpl</p>
 * <p>Description: This class returns the instance of the Hazard Curve Calculator
 * to the application. This is the RMI based interface, so when a server based
 * instance is returned to the application it calls the methods of the calculator
 * in the same manner as it would call the Hazard Curve Calulator on its own machine.</p>
 * @author : Ned (Edward) Field , Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RemoteHazardCurveFactoryImpl extends UnicastRemoteObject implements RemoteHazardCurveFactoryAPI{

  public RemoteHazardCurveFactoryImpl() throws java.rmi.RemoteException {
  }

  /**
   * Get the reference to the instance of the HazardCurveCalculator
   * @return
   * @throws java.rmi.RemoteException
   */
  public HazardCurveCalculatorAPI getRemoteHazardCurveCalculator() throws java.rmi.RemoteException{
    try{
      HazardCurveCalculatorAPI hazardCurve = new HazardCurveCalculator();
      return hazardCurve;
    }catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }
}
