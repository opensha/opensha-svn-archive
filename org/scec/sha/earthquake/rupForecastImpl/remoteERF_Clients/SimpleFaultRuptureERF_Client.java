package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;
import org.scec.param.event.*;

/**
 * <p>Title: SimplePoissonFaultRuptureERF_Client</p>
 * <p>Description: This is a client for remote Simple Fault Rupture ERF instance.
 * It will access ERF from the remote machine</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class SimplePoissonFaultRuptureERF_Client extends RemoteERF_Client {

  public SimplePoissonFaultRuptureERF_Client() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.SimplePoissonFaultRuptureERF";
    getRemoteERF(className);
  }


}