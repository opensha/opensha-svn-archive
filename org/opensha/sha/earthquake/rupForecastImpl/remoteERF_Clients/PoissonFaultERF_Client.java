package org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

import org.opensha.sha.earthquake.rupForecastImpl.remote.RegisterRemoteERF_Factory;

/**
 * <p>Title: PoissonFaultERF_Client</p>
 * <p>Description: This is a client for remote Simple Fault Rupture ERF instance.
 * It will access ERF from the remote machine</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class PoissonFaultERF_Client extends RemoteERF_Client {

  public PoissonFaultERF_Client() throws java.rmi.RemoteException{
    String className = "org.opensha.sha.earthquake.rupForecastImpl.PoissonFaultERF";
    String remoteRegistrationName = RegisterRemoteERF_Factory.registrationName;
    getRemoteERF(className,remoteRegistrationName);
  }


}
