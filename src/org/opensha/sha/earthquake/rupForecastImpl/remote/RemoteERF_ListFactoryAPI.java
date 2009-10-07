package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.rmi.Remote;

/**
 * <p>Title: RemoteERF_ListFactoryAPI </p>
 * <p>Description: This will act as the factory for giving the ERF List references to
 * the client. After getting the ERF List reference, client can interact with the
 * ERF List as usual.</p>
 * @author Nitin Gupta, Vipin Gupta
 * @date April 28, 2004
 * @version 1.0
 */

public interface RemoteERF_ListFactoryAPI extends Remote {

  /**
   * Get the reference to the remote ERF_List
   * @param className
   * @return
   * @throws java.rmi.RemoteException
   */
  public RemoteERF_ListAPI getRemoteERF_List(String className) throws java.rmi.RemoteException;

}
