package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: PEER_NonPlanarFaultForecastClient </p>
 * <p>Description: This is a client for remote PEER Non Planar Fault Forecast instance. It will
 * access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class PEER_NonPlanarFaultForecastClient extends RemoteERF_Client {

  public final static String NAME = new String("Server PEER Non-Planar Fault");

  public PEER_NonPlanarFaultForecastClient() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast";
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