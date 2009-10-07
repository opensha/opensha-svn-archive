package org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients;


import org.opensha.sha.earthquake.rupForecastImpl.remote.RegisterRemoteERF_ListFactory;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteERF_ListClient;


/**
 * <p>Title: PEER_LogicTreeERF_ListClient</p>
 * <p>Description: This class provides the access to the server side PEER_LogicTree
 * ERF List. It creates the objects of the PEER_NonPlanarFaultForecast on the server.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created Sept 28,2004
 * @version 1.0
 */

public class PEER_LogicTreeERF_ListClient extends RemoteERF_ListClient{

  public PEER_LogicTreeERF_ListClient() throws java.rmi.RemoteException{
    String className = "org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_LogicTreeServerBasedERF_List";
    String remoteRegistrationName = RegisterRemoteERF_ListFactory.registrationName;
    getRemoteERF_List(className,remoteRegistrationName);
  }

}
