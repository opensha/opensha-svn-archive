package org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases;

import java.rmi.RemoteException;

import org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_NonPlanarFaultForecastClient;
import org.scec.sha.earthquake.*;


/**
 * <p>Title: PEER_LogicTreeServerBasedERF_List</p>
 * <p>Description: This class is the extension for the PEER_LogicTreeERF_List
 * to provide the user with capability getting each ERF from the ERF List.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @ created Aug 13, 2004
 * @version 1.0
 */

public class PEER_LogicTreeServerBasedERF_List extends PEER_LogicTreeERF_List {

  public PEER_LogicTreeServerBasedERF_List() {

  }

  /**
   * this method will create the instance of the non-planar fault based on the
   * provided segmentation, slip rate and mag upper
   * @param slipRate
   * @param maxMag
   * @return
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




}