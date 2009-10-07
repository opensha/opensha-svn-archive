package org.opensha.sha.calc.remoteCalc;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.HazardCurveCalculatorAPI;

/**
 * <p>Title: RemoteHazardCurveClient</p>
 * <p>Description: This class establishes the remote RMI to the server based
 * Hazard Curve Calculation.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RemoteHazardCurveClient {

  /**
   * Get the reference to the remote Hazard Curve Factory
   */
  public HazardCurveCalculatorAPI getRemoteHazardCurveCalc() {
    try {
      RemoteHazardCurveFactoryAPI remoteHazardCurveFactory= (RemoteHazardCurveFactoryAPI)
          Naming.lookup(RegisterRemoteHazardCurveFactory.registrationName);
      return remoteHazardCurveFactory.getRemoteHazardCurveCalculator();
    }
    catch (NotBoundException n) {
      try{
        return new HazardCurveCalculator();
      }catch(RemoteException e){
        e.printStackTrace();
      }
    }
    catch (MalformedURLException m) {
      try{
        return new HazardCurveCalculator();
      }catch(RemoteException e){
        e.printStackTrace();
      }
    }
    catch(java.rmi.UnknownHostException r){
      try{
        return new HazardCurveCalculator();
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
