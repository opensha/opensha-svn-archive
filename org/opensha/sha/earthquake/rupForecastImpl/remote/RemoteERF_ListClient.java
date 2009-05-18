package org.opensha.sha.earthquake.rupForecastImpl.remote;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

import org.opensha.sha.earthquake.*;
import org.opensha.param.event.*;
import org.opensha.param.*;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.region.GeographicRegion;


/**
 * <p>Title: RemoteERF_ListClient</p>
 * <p>Description: This class provides the interface to connect to the ERF_List
 * object on the server.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created Aug 6,2004
 * @version 1.0
 */

public class RemoteERF_ListClient extends ERF_EpistemicList implements 
	ParameterChangeListener{

  private RemoteERF_ListAPI erfListServer = null;


  /**
   * Get the reference to the remote ERF
   */
  protected void getRemoteERF_List(String className, String rmiRemoteRegistrationName) throws RemoteException{
    try {
      RemoteERF_ListFactoryAPI remoteERF_ListFactory= (RemoteERF_ListFactoryAPI) Naming.lookup(rmiRemoteRegistrationName);
      erfListServer = remoteERF_ListFactory.getRemoteERF_List(className);
      adjustableParams = erfListServer.getAdjustableParameterList();
      addListenersToParamaters();
      //getting the timespan and adjustable params
      timeSpan =erfListServer.getTimeSpan();
      addListenersToTimeSpan();
    }catch (NotBoundException n) {
      n.printStackTrace();
    }
    catch (MalformedURLException m) {
      m.printStackTrace();
    }
    catch (java.rmi.UnmarshalException u) {
      u.printStackTrace();
    }

  }


  /**
   * Adding the change Listeners to the Parameters
   */
  private void addListenersToParamaters() {
    ListIterator it = adjustableParams.getParametersIterator();
    while (it.hasNext())
      ( (ParameterAPI) it.next()).addParameterChangeListener(this);
  }

  //add the listeners to the timespan parameters
  private void addListenersToTimeSpan() {
    //if timespan is not null then add the change listeners to its parameters.
    //we are again adding listeners here becuase they are transient and cannot be serialized.
    if (timeSpan != null) {
      timeSpan.addParameterChangeListener(this);
      ParameterList timeSpanParamList = timeSpan.getAdjustableParams();
      ListIterator it = timeSpanParamList.getParametersIterator();
      while (it.hasNext())
        ( (ParameterAPI) it.next()).addParameterChangeListener(this);
    }
  }


  /**
   * get the number of Eqk Rup Forecasts in this list
   * @return : number of eqk rup forecasts in this list
   */
  public int getNumERFs(){
    try{
      return erfListServer.getNumERFs();
    }catch(RemoteException e){
      e.printStackTrace();
    }
    return -1;
  }


  /**
   * get the ERF in the list with the specified index
   * @param index : index of Eqk rup forecast to return
   * @return
   */
  public EqkRupForecastAPI getERF(int index) {
    try{
      RemoteERF_Client erfClient = new RemoteERF_Client();
      RemoteEqkRupForecastAPI remoteERF = erfListServer.getRemoteERF(index);
      erfClient.setERF_Server(remoteERF);
      return erfClient;
    }catch(RemoteException e){
      e.printStackTrace();
    }
    return null;
  }

  /**
   * get the weight of the ERF at the specified index
   * @param index : index of ERF
   * @return : relative weight of ERF
   */
  public double getERF_RelativeWeight(int index) {
    try{
      return erfListServer.getERF_RelativeWeight(index);
    }catch(RemoteException e){
      e.printStackTrace();
    }
    return 1.0;
  }

  /**
   * Return the vector containing the Double values with
   * relative weights for each ERF
   * @return : ArrayList of Double values
   */
  public ArrayList getRelativeWeightsList() {
    try{
      return erfListServer.getRelativeWeightsList();
    }catch(RemoteException e){
      e.printStackTrace();
    }
    return null;
  }



  /**
   * get the name of this class
   * @return
   */
  public String getName() {
    try{
      return erfListServer.getName();
    }catch(RemoteException e){
      e.printStackTrace();
    }
    return null;
  }




  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
  public GeographicRegion getApplicableRegion() {
    try{
      return erfListServer.getApplicableRegion();
    }catch(RemoteException e){
      e.printStackTrace();
    }
    return null;
  }

  /**
   * This method sets the time-span field.
   * @param time
   */
  public void setTimeSpan(TimeSpan time) {
    try{
      erfListServer.setTimeSpan(time);
    }catch(RemoteException e){
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see org.opensha.param.event.ParameterChangeListener#parameterChange(org.opensha.param.event.ParameterChangeEvent)
   */
  public void parameterChange(ParameterChangeEvent event) {
    try {
      String eventParamName = event.getParameterName();
      erfListServer.setParameter(event.getParameterName(), event.getNewValue());
      adjustableParams = erfListServer.getAdjustableParameterList();
      addListenersToParamaters();
      //getting the timespan and adjustable params
      timeSpan =erfListServer.getTimeSpan();
      addListenersToTimeSpan();
    }
    catch (RemoteException ex) {
      ex.printStackTrace();
    }
  }



 /**
  *
  * @returns the adjustable ParameterList for the ERF
  */
 public ParameterList getAdjustableParameterList(){
   try{
     return adjustableParams;
   }catch(Exception e){
     e.printStackTrace();
    }
    return null;
 }

 




 /**
  * get the timespan
  *
  * @return : TimeSpan
  */
 public TimeSpan getTimeSpan() {
   try{
     return timeSpan;
   }catch(Exception e){
     e.printStackTrace();
   }
   return null;
 }


 /**
  * update the list of the ERFs based on the new parameters
  */
 public void updateForecast() {

   try{
       erfListServer.updateForecast();

   }catch(RemoteException e){
     e.printStackTrace();
   }
 }


 /**
  * Update the forecast and save it in serialized mode into a file
  * @return
  */
 public String updateAndSaveForecast() {
   try {
     updateForecast();
     return erfListServer.saveForecast();
   }
   catch (Exception e) {
     e.printStackTrace();
   }
   return null;
 }

}
