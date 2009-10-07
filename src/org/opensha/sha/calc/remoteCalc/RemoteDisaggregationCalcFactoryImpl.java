package org.opensha.sha.calc.remoteCalc;

import java.rmi.server.UnicastRemoteObject;

import org.opensha.sha.calc.disaggregation.DisaggregationCalculator;
import org.opensha.sha.calc.disaggregation.DisaggregationCalculatorAPI;

/**
 * <p>Title: RemoteDisaggregationCalcFactoryImpl</p>
 * <p>Description: This class returns the instance of the disaggregation Calculator
 * to the application. This is the RMI based interface, so when a server based
 * instance is returned to the application it calls the methods of the calculator
 * in the same manner as it would call the disaggregation Calulator on its own machine.</p>
 * @author : Ned (Edward) Field , Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RemoteDisaggregationCalcFactoryImpl extends UnicastRemoteObject
    implements RemoteDisaggregationCalcFactoryAPI{

  public RemoteDisaggregationCalcFactoryImpl() throws java.rmi.RemoteException {
  }

  /**
   * Get the reference to the instance of the DisaggregationCalculator
   * @return
   * @throws java.rmi.RemoteException
   */
  public DisaggregationCalculatorAPI getRemoteDisaggregationCalculator() throws java.rmi.RemoteException{
    try{
      DisaggregationCalculatorAPI hazardCurve = new DisaggregationCalculator();
      return hazardCurve;
    }catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }
}
