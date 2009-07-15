package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

/**
 * <p>Title: RemoteERF_ListFactoryImpl </p>
 * <p>Description: This class generates a new ERF List remote object and passes its
 * reference back to the calling client. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class RemoteERF_ListFactoryImpl extends UnicastRemoteObject
    implements RemoteERF_ListFactoryAPI {


  public RemoteERF_ListFactoryImpl() throws java.rmi.RemoteException  { }

  /**
   * Return the Remote ERF List reference back to the calling client
   * @return Remote ERF List
   * @throws java.rmi.RemoteException
   */
  public RemoteERF_ListAPI getRemoteERF_List(String className) throws java.rmi.RemoteException {
    try {
       RemoteERF_ListAPI erfServer = new RemoteERF_ListImpl(className);
       return erfServer;
    }catch(Exception e) { e.printStackTrace();}
    return null;
  }
}
