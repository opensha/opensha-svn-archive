package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: SimplePoissonFaultERFClient.java </p>
 * <p>Description: This is a client for Simple Poisson Fault ERF instance.
 * It will access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class SimplePoissonFaultERFClient extends RemoteERF_Client{
  public SimplePoissonFaultERFClient() {
    String className = "org.scec.sha.earthquake.rupForecastImpl.SimplePoissonFaultERF";
    getRemoteERF(className);
  }
}
