package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: Frankel96_EqkRupForecastClient.java </p>
 * <p>Description: This is a client for remote Frankel 96 ERF instance
 * (with adjustable parameters). It will access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class Frankel96_AdjustableEqkRupForecastClient extends RemoteERF_Client{

  // name of this ERF
  //public final static String NAME = new String("Server USGS/CGS 1996 Adj. Cal. ERF");

  public Frankel96_AdjustableEqkRupForecastClient() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast";
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
