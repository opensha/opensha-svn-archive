package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: WG02_EqkRupForecastClient </p>
 * <p>Description: This is a client for WG-02 Forecast. It will
 * access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class WG02_EqkRupForecastClient extends RemoteERF_Client {

  public final static String NAME = new String("Server WG02 Eqk Rup Forecast");

  public WG02_EqkRupForecastClient() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast";
    getRemoteERF(className);
  }


  /*
  * Returns the name of the ERF
  */
  public String getName() {
    try {
      return NAME;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}