package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: STEP_AlaskanPipeForecastClient.java </p>
 * <p>Description: This is a client for remote STEP Alaskan Pipeline ERF instance.
 * It will access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class STEP_AlaskanPipeForecastClient extends RemoteERF_Client{
  public STEP_AlaskanPipeForecastClient() {
    String className = "org.scec.sha.earthquake.rupForecastImpl.step.STEP_AlaskanPipeForecastClient";
    getRemoteERF(className);
  }
}
