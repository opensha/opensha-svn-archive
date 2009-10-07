package org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;

import org.opensha.sha.earthquake.rupForecastImpl.remote.RegisterRemoteERF_Factory;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: PEER_AreaForecastClient </p>
 * <p>Description: This is a client for remote PEER Area Forecast instance. It will
 * access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class PEER_AreaForecastClient extends RemoteERF_Client {

  public PEER_AreaForecastClient() throws java.rmi.RemoteException, MalformedURLException, NotBoundException{
    String className = "org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast";
    String remoteRegistrationName = RegisterRemoteERF_Factory.registrationName;
    getRemoteERF(className,remoteRegistrationName);
  }

}
