package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_ListClient;


/**
 * <p>Title: WG02_FortranWrappedERF_EpistemicListClient</p>
 * <p>Description: This class provides the access to the server side WG02
 * ERF List. It creates the objects of the WG-02 on the server.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created Aug 10,2004
 * @version 1.0
 */

public class WG02_FortranWrappedERF_EpistemicListClient extends RemoteERF_ListClient{

  public WG02_FortranWrappedERF_EpistemicListClient() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.WG02.WG02_FortranWrappedERF_EpistemicList";
    getRemoteERF_List(className);
  }

}

