package org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;

import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RegisterRemoteERF_Factory;

/**
 * <p>Title: Frankel96_EqkRupForecastClient.java </p>
 * <p>Description: This is a client for remote Frankel 96 ERF instance
 * (with adjustable parameters). It will access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class Frankel96_AdjustableEqkRupForecastClient extends RemoteERF_Client{

  public Frankel96_AdjustableEqkRupForecastClient() throws java.rmi.RemoteException, MalformedURLException, NotBoundException{
    String className = "org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast";
    String remoteRegistrationName = RegisterRemoteERF_Factory.registrationName;
    getRemoteERF(className,remoteRegistrationName);
  }


}
