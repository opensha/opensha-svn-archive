package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: FloatingPoissonFaultERF_Client </p>
 * <p>Description: This is a client for Floating Poisson Fault ERF instance.
 * It will access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class FloatingPoissonFaultERF_Client extends RemoteERF_Client{

  public FloatingPoissonFaultERF_Client() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.FloatingPoissonFaultERF";
    getRemoteERF(className);
  }


}
