package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import java.util.ArrayList;

import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;
import org.scec.sha.earthquake.EqkRupture;

/**
 * <p>Title: SingleFaultRuptureERF_Client </p>
 * <p>Description: This is a client for Single Fault Rupture ERF.
 * It will access ERF from the remote machine </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class SingleFaultRuptureERF_Client extends RemoteERF_Client{

  private final static  String className = "org.scec.sha.earthquake.rupForecastImpl.SingleFaultRuptureERF";


  public SingleFaultRuptureERF_Client() throws java.rmi.RemoteException{
    getRemoteERF(className);
  }

  /**
   * Constructor for this source
   */
  public SingleFaultRuptureERF_Client(EqkRupture eqkRupture, double prob)
    throws java.rmi.RemoteException{
    //Class array to define the types of arguments that the constructor accepts
    ArrayList paramTypes = new ArrayList();
    paramTypes.add(eqkRupture.getClass());
    paramTypes.add(Double.TYPE);

    //ArrayList to store the actual vlaue of the objects.
    ArrayList params = new ArrayList();
    params.add(eqkRupture);
    params.add(new Double(prob));

    try{
      getRemoteERF(params,paramTypes,className);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

}