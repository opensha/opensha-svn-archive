package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: PEER_MultiSourceForecastClient </p>
 * <p>Description: This is a client for remote PEER Multi Source Forecast instance. It will
 * access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class PEER_MultiSourceForecastClient extends RemoteERF_Client {

  public PEER_MultiSourceForecastClient() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast";
    getRemoteERF(className);
  }

}