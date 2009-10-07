package org.opensha.sha.earthquake.rupForecastImpl.remote;


import java.rmi.Remote;
import java.util.ArrayList;

/**
 * <p>Title: RemoteERF_FactoryAPI.java </p>
 * <p>Description: This will act as the factory for giving the ERF references to
 * the client. After getting the ERF reference, client can interact with the
 * ERF as usual </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @date April 28, 2004
 * @version 1.0
 */

public interface RemoteERF_FactoryAPI extends Remote {

  /**
   * Get the reference to the remote ERF
   * @param className
   * @return
   * @throws java.rmi.RemoteException
   */
  public RemoteEqkRupForecastAPI getRemoteERF(String className) throws java.rmi.RemoteException;


  /**
   * Gets the reference to the remote ERF
   * @param params : List of arguments required to create the ERF
   * @param className
   * @return
   * @throws java.rmi.RemoteException
   */
  public RemoteEqkRupForecastAPI getRemoteERF(ArrayList params,ArrayList paramTypes,String className) throws java.rmi.RemoteException;

}
