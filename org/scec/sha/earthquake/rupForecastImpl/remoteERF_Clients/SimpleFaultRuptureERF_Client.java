package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;
import org.scec.param.event.*;

/**
 * <p>Title: SimpleFaultRuptureERF_Client</p>
 * <p>Description: This is a client for remote Simple Fault Rupture ERF instance.
 * It will access ERF from the remote machine</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class SimpleFaultRuptureERF_Client extends RemoteERF_Client {

  public SimpleFaultRuptureERF_Client() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.SimpleFaultRuptureERF";
    getRemoteERF(className);
  }


}