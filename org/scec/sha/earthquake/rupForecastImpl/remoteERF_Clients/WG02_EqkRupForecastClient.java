package org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import java.util.*;
import java.rmi.RemoteException;

import org.scec.data.TimeSpan;
import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: WG02_EqkRupForecastClient </p>
 * <p>Description: This is a client for WG-02 Forecast. It will
 * access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class WG02_EqkRupForecastClient extends RemoteERF_Client {

  //public final static String NAME = new String("Server WG02 Eqk Rup Forecast");
  public final static String className = "org.scec.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast";
  /**
   * Class default constructor
   * @throws java.rmi.RemoteException
   */
  public WG02_EqkRupForecastClient() throws java.rmi.RemoteException{
    getRemoteERF(className);
  }



  /**
   * Class constructor that takes in the arguments for the WG-02.
   * @param inputFileStrings
   * @param rupOffset
   * @param gridSpacing
   * @param deltaMag
   * @param backSeisValue
   * @param grTailValue
   * @param name
   * @param timespan
   */
  public WG02_EqkRupForecastClient(ArrayList inputFileStrings, double rupOffset, double gridSpacing,
                             double deltaMag, String backSeisValue, String grTailValue, String name,
    TimeSpan timespan) {

    //Class array to define the types of arguments that the constructor accepts
    ArrayList paramTypes = new ArrayList();
    paramTypes.add(inputFileStrings.getClass());
    paramTypes.add(Double.TYPE);
    paramTypes.add(Double.TYPE);
    paramTypes.add(Double.TYPE);
    paramTypes.add(backSeisValue.getClass());
    paramTypes.add(grTailValue.getClass());
    paramTypes.add(name.getClass());
    paramTypes.add(timespan.getClass());

    //ArrayList to store the actual vlaue of the objects.
    ArrayList params = new ArrayList();
    params.add(inputFileStrings);
    params.add(new Double(rupOffset));
    params.add(new Double(gridSpacing));
    params.add(new Double(deltaMag));
    params.add(backSeisValue);
    params.add(grTailValue);
    params.add(name);
    params.add(timespan);

    try{
      getRemoteERF(params,paramTypes,className);
    }catch(Exception e){
      e.printStackTrace();
    }
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