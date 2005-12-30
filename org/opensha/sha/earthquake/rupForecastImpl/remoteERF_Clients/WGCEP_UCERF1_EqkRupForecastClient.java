package org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RegisterRemoteERF_Factory;

/**
 * <p>Title: Frankel02_AdjustableEqkRupForecast.java </p>
 * <p>Description: This is a client for remote Frankel 02 ERF instance. It will
 * access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class WGCEP_UCERF1_EqkRupForecastClient extends RemoteERF_Client {

  public WGCEP_UCERF1_EqkRupForecastClient() throws java.rmi.RemoteException{
    String className = "org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF1.WGCEP_UCERF1_EqkRupForecast";
    String remoteRegistrationName = RegisterRemoteERF_Factory.registrationName;
    getRemoteERF(className,remoteRegistrationName);
  }

}
