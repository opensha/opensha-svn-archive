package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: Frankel02_AdjustableEqkRupForecast.java </p>
 * <p>Description: This is a client for remote Frankel 02 ERF instance. It will
 * access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class Frankel02_AdjustableEqkRupForecastClient extends RemoteERF_Client {

 // public final static String NAME = new String("Server USGS/CGS 2002 Adj. Cal. ERF");

  public Frankel02_AdjustableEqkRupForecastClient() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast";
    getRemoteERF(className);
  }


  /*
  * Returns the name of the ERF
  */
  /*public String getName() {
    try {
      return NAME;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }*/
}