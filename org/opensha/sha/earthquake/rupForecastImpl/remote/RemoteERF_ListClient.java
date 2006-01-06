package org.opensha.sha.earthquake.rupForecastImpl.remote;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

import org.opensha.sha.earthquake.*;
import org.opensha.param.event.*;
import org.opensha.param.*;
import org.opensha.data.TimeSpan;
import org.opensha.data.Location;
import org.opensha.data.region.GeographicRegion;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.UnknownEventException;
import java.rmi.server.ExportException;
import net.jini.jeri.BasicILFactory;
import net.jini.core.event.RemoteEventListener;
import net.jini.export.Exporter;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

/**
 * <p>Title: RemoteERF_ListClient</p>
 * <p>Description: This class provides the interface to connect to the ERF_List
 * object on the server.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created Aug 6,2004
 * @version 1.0
 */

public class RemoteERF_ListClient extends ERF_List implements
    RemoteEventListener,TimeSpanChangeListener{

  private RemoteERF_ListAPI erfListServer = null;
  //adds the listeners to this list
  private ArrayList listenerList = new ArrayList();

  //creates the EventObject to send to the listeners for parameter change
  //and timespan change
  private EventObject eventObj;


  //checks if within the notify function
  private boolean withinNotify = false;
  //named of the parameter whose value is being changed
  private String changedParameterName;


  /**
   * Get the reference to the remote ERF
   */
  protected void getRemoteERF_List(String className, String rmiRemoteRegistrationName) throws RemoteException{
    try {
      RemoteERF_ListFactoryAPI remoteERF_ListFactory= (RemoteERF_ListFactoryAPI) Naming.lookup(rmiRemoteRegistrationName);
      erfListServer = remoteERF_ListFactory.getRemoteERF_List(className);
      adjustableParams = erfListServer.getAdjustableParameterList();
      ListIterator it = adjustableParams.getParametersIterator();
      while(it.hasNext())
        ((ParameterAPI)it.next()).addParameterChangeListener(this);
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

    //Make a proxy of myself to pass to the server/filter
    Exporter exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                                              new BasicILFactory());
    try {
      RemoteEventListener proxy = (RemoteEventListener) exporter.export(this);
      erfListServer.addParameterAndTimeSpanChangeListener(proxy);
    }
    catch (ExportException ex) {
      ex.printStackTrace();
    }

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
        ( (ParameterAPI) it.next()).addParameterChangeListener(timeSpan);
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
      if(!(withinNotify && eventParamName.equals(changedParameterName)))
        erfListServer.setParameter(event.getParameterName(), event.getNewValue());
    }
    catch (RemoteException ex) {
      ex.printStackTrace();
    }
  }


  /**
   *  Function that must be implemented by all Timespan Listeners for
   *  ParameterChangeEvents.
   *
   * @param  event  The Event which triggered this function call
   */
  public void timeSpanChange(EventObject event) {
    try {
      ParameterChangeEvent chgEvent = ((ParameterChangeEvent)event);
      erfListServer.setParameter(chgEvent.getParameterName(), chgEvent.getNewValue());
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
  * adds the listener obj to list. When the change events come, all
  * listeners added to it are notified of it.
  * @param obj Object
  */
 public void addParameterAndTimeSpanChangeListener(
     ParameterAndTimeSpanChangeListener obj) {
   listenerList.add(obj);
 }



 /**
  * This method is called from the remote event is received from the Server by the client.
  * @param remoteEvent RemoteEvent
  * @throws UnknownEventException
  * @throws RemoteException
  */
 public void notify(RemoteEvent remoteEvent) throws UnknownEventException,
     RemoteException {
   withinNotify = true;
   Object obj = remoteEvent.getSource();
   eventObj = new EventObject(obj);
   if(obj instanceof ParameterAPI){
     ParameterAPI param = (ParameterAPI)obj;
     changedParameterName = param.getName();
     adjustableParams.getParameter(param.getName()).setValue(param.getValue());
   }
   else if(obj instanceof TimeSpan){
     timeSpan = (TimeSpan)obj;
     addListenersToTimeSpan();
   }

   int size = listenerList.size();
   for(int i=0;i<size;++i){
     ParameterAndTimeSpanChangeListener listener = (ParameterAndTimeSpanChangeListener)listenerList.get(i);
     listener.parameterOrTimeSpanChange(eventObj);
   }
   withinNotify = false;
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
   System.out.println("ParameterChange Flag: "+parameterChangeFlag);
   try{
     if(this.parameterChangeFlag) {
       erfListServer.updateForecast();
       setParameterChangeFlag(false);
     }
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
