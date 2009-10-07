package org.opensha.sha.calc.remoteCalc;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.opensha.sha.calc.SpectrumCalculator;
import org.opensha.sha.calc.SpectrumCalculatorAPI;

/**
 * <p>Title: RemoteResponseSpectrumClient</p>
 * <p>Description: This class establishes the remote RMI to the server based
 * Response Spectrum Calculation.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RemoteResponseSpectrumClient {

  /**
   * Get the reference to the remote resonse spectrum Factory
   */
  public SpectrumCalculatorAPI getRemoteSpectrumCalc() {
    try {
      RemoteResponseSpectrumFactoryAPI remoteResponseSpectrumFactory= (RemoteResponseSpectrumFactoryAPI)
          Naming.lookup(RegisterRemoteResponseSpectrumFactory.registrationName);
      return remoteResponseSpectrumFactory.getRemoteResponseSpectrumCalculator();
    }
    catch (NotBoundException n) {
      try{
        return new SpectrumCalculator();
      }catch(RemoteException e){
        e.printStackTrace();
      }
    }
    catch (MalformedURLException m) {
      try{
        return new SpectrumCalculator();
      }catch(RemoteException e){
        e.printStackTrace();
      }
    }
    catch(java.rmi.UnknownHostException r){
      try{
        return new SpectrumCalculator();
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
