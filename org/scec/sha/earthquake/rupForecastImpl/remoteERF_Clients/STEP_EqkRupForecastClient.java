package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: STEP_EqkRupForecastClient.java </p>
 * <p>Description: This is a client for remote STEP ERF instance.
 * It will access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class STEP_EqkRupForecastClient extends RemoteERF_Client{

  public STEP_EqkRupForecastClient() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast";
    getRemoteERF(className);
  }

}
