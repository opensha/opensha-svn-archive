package org.scec.sha.earthquake.rupForecastImpl.remote;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

/**
 * <p>Title: RemoteERF_FactoryImpl.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class RemoteERF_FactoryImpl extends UnicastRemoteObject
    implements RemoteERF_FactoryAPI {
  public static final String dnsPrefix = "rmi://gravity.usc.edu:1099/ERF_";
  //public static int i=0;

  public RemoteERF_FactoryImpl() throws java.rmi.RemoteException  {

  }

  public RemoteERF_API getRemoteERF() throws java.rmi.RemoteException {
    try {
       RemoteERF_API erfServer = new ERFFrankel02ServerImpl();
       return erfServer;
    }catch(Exception e) { e.printStackTrace();}
    return null;
  }
}