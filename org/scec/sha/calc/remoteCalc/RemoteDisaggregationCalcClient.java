package org.scec.sha.calc.remoteCalc;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.ListIterator;
import org.scec.sha.calc.DisaggregationCalculatorAPI;
import org.scec.sha.calc.DisaggregationCalculator;

/**
 * <p>Title: RemoteDisaggregationCalcClient</p>
 * <p>Description: This class establishes the remote RMI to the server based
 * Hazard Curve Calculation.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RemoteDisaggregationCalcClient {

  /**
   * Get the reference to the remote Disaggregation calc Factory
   */
  public DisaggregationCalculatorAPI getRemoteDisaggregationCalc() {
    try {
      RemoteDisaggregationCalcFactoryAPI remoteDisaggregationCalcFactory= (RemoteDisaggregationCalcFactoryAPI)
          Naming.lookup(RegisterRemoteDisaggregationCalcFactory.registrationName);
      return remoteDisaggregationCalcFactory.getRemoteDisaggregationCalculator();
    }
    catch (NotBoundException n) {
      try{
        return new DisaggregationCalculator();
      }catch(RemoteException e){
        e.printStackTrace();
      }
    }
    catch (MalformedURLException m) {
      try{
        return new DisaggregationCalculator();
      }catch(RemoteException e){
        e.printStackTrace();
      }
    }
    catch(java.rmi.UnknownHostException r){
      try{
        return new DisaggregationCalculator();
      }catch(RemoteException e){
        e.printStackTrace();
      }
    }
    catch (java.rmi.UnmarshalException u) {
      u.printStackTrace();
    }
    catch (RemoteException r) {
      r.printStackTrace();
    }
    return null;
  }

}