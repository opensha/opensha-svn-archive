package org.scec.sha.calc.remoteHazardCalc;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.ListIterator;



/**
 * <p>Title: RemoteHazardCurveClient</p>
 * <p>Description: This class establishes the remote RMI to the server based
 * Hazard Curve Calculation.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RemoteHazardCurveClient {

  /**
   * Get the reference to the remote
   */
  protected RemoteHazardCurveFactoryAPI getRemoteHazardCurveCalc() {
    try {
      RemoteHazardCurveFactoryAPI remoteHazardCurveFactory= (RemoteHazardCurveFactoryAPI) Naming.lookup(RegisterRemoteHazardCurveFactory.registrationName);
      return remoteHazardCurveFactory;
    }
    catch (NotBoundException n) {
      n.printStackTrace();
    }
    catch (MalformedURLException m) {
      m.printStackTrace();
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