package org.scec.sha.earthquake.rupForecastImpl.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

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

  public RemoteERF_API getRemoteERF() throws java.rmi.RemoteException;

}