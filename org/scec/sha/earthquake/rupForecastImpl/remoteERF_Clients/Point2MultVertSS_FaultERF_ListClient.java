package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_ListClient;


/**
 * <p>Title: Point2MultVertSS_FaultERF_ListClient</p>
 * <p>Description: This class provides the access to the server side Point2MultVertSS Fault
 * ERF List. It creates the objects of the Single Fault Rupture ERF on the server.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created Sept 29,2004
 * @version 1.0
 */

public class Point2MultVertSS_FaultERF_ListClient extends RemoteERF_ListClient{

  public Point2MultVertSS_FaultERF_ListClient() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault.PEER_LogicTreeServerBasedERF_List";
    getRemoteERF_List(className);
  }

}

