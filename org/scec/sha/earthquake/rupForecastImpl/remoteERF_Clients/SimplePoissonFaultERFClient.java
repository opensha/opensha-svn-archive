package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: SimplePoissonFaultERFClient.java </p>
 * <p>Description: This is a client for Simple Poisson Fault ERF instance.
 * It will access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class SimplePoissonFaultERFClient extends RemoteERF_Client{

  //name of the ERF
  //public final static String  NAME = "Server Poisson Fault ERF";

  public SimplePoissonFaultERFClient() throws java.rmi.RemoteException{
    String className = "org.scec.sha.earthquake.rupForecastImpl.SimplePoissonFaultERF";
    getRemoteERF(className);
  }



  /*
  * Returns the name of the ERF
  */
  /*public String getName() {
    try {
      return NAME;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }*/

}
