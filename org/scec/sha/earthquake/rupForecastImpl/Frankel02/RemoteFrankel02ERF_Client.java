/*
 * Created on Apr 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.scec.sha.earthquake.rupForecastImpl.Frankel02;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 *
 * <p>Title:ERFFrankel02Client.java </p>
 * <p>Description: Proxy which delegates all the calls to the remote ERF </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class RemoteFrankel02ERF_Client extends RemoteERF_Client {

  public RemoteFrankel02ERF_Client() throws Exception {
    className = "org.scec.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast";
    getRemoteERF();
  }

}