package org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases;

import java.rmi.RemoteException;

import org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_NonPlanarFaultForecastClient;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteEqkRupForecastAPI;
import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: PEER_LogicTreeServerBasedERF_List</p>
 * <p>Description: This class is the extension for the PEER_LogicTreeERF_List
 * to provide the user with capability getting each ERF from the List as the
 * Remote Server Object, rather than creating the creating the objects on the
 * users own machine.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @ created Aug 13, 2004
 * @version 1.0
 */

public class PEER_LogicTreeServerBasedERF_List extends PEER_LogicTreeERF_List {


  /**
   * this method will create the instance of the non-planar fault based on the
   * provided segmentation, slip rate and mag upper
   * @param slipRate
   * @param maxMag
   * @returnf
   */
  protected EqkRupForecast createERF(String segmentation,
                                     double slipRate, double magUpper) {
    try{
      PEER_NonPlanarFaultForecastClient forecast = new PEER_NonPlanarFaultForecastClient();
      forecast.getParameter(PEER_NonPlanarFaultForecast.SEGMENTATION_NAME).setValue(segmentation);
      forecast.getParameter(PEER_NonPlanarFaultForecast.SLIP_RATE_NAME).setValue(new Double(slipRate));
      forecast.getParameter(PEER_NonPlanarFaultForecast.GR_MAG_UPPER).setValue(new Double(magUpper));
      forecast.getParameter(PEER_NonPlanarFaultForecast.DIP_DIRECTION_NAME).setValue(PEER_NonPlanarFaultForecast.DIP_DIRECTION_EAST);
      return forecast;
    }catch(RemoteException e){
      e.printStackTrace();
    }
    return null;
  }

  /**
   *
   * @param index
   * @returns the instance of the remotely existing ERF in the ERF List
   * on the server given the index. It also sets the timespan in the returned ERF.
   * **NOTE: All the functionality in this functionlity remains same as that of getERF but only differs
   * when returning each ERF from the ERF List. getERF() return the instance of the
   * EqkRupForecastAPI which is transferring the whole object on to the user's machine, but this functin
   * return back the RemoteEqkRupForecastAPI. This is useful becuase whole ERF object does not
   * get transfer to the users machine, just a stub of the remotely existing ERF gets
   * transferred.
   *
   * This function returns null, but if anyone needs to host his ERF as the remote
   * then he will have to implement this method.
   */
  public RemoteEqkRupForecastAPI getRemoteERF(int index){
   RemoteEqkRupForecastAPI remoteERF =  ((RemoteERF_Client)erf_List.get(index)).getERF_Server();
   try{
     remoteERF.setTimeSpan(this.timeSpan);
     return remoteERF;
   }catch(RemoteException e){
     e.printStackTrace();
   }
   return null;
  }

  /**
   * Update the Remote EqkRupForecasts with the new set of parameters
   */
  public void updateForecast() {
    // set the new values for the parameters in all the EqkRupForecasts in the list
    if(parameterChangeFlag) {
      // set this new value of param in all the EqkRupForecast in the list
      int num = getNumERFs();
      for(int i=0; i<num; ++i) {
        RemoteEqkRupForecastAPI eqkRupForecast = (RemoteEqkRupForecastAPI)this.getRemoteERF(i);
        try{
          // see the new parameter values in all the forecasts in the list
          eqkRupForecast.getParameter(GRID_PARAM_NAME).setValue(gridParam.getValue());
          eqkRupForecast.getParameter(OFFSET_PARAM_NAME).setValue(offsetParam.getValue());
          eqkRupForecast.getParameter(SIGMA_PARAM_NAME).setValue(lengthSigmaParam.getValue());
          eqkRupForecast.getParameter(FAULT_MODEL_NAME).setValue(faultModelParam.getValue());
          eqkRupForecast.getTimeSpan().setDuration(timeSpan.getDuration());
          eqkRupForecast.updateForecast(eqkRupForecast.getAdjustableParameterList(),eqkRupForecast.getTimeSpan());
        }catch(RemoteException e){
          e.printStackTrace();
        }
      }
    }
    this.parameterChangeFlag = false;
  }
}