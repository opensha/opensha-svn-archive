package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.param.ParameterAPI;
import org.opensha.sha.earthquake.*;
import org.opensha.data.Location;
import org.opensha.data.TimeSpan;
import org.opensha.data.region.GeographicRegion;
import org.opensha.param.ParameterList;
import net.jini.core.event.RemoteEventListener;
import org.opensha.param.event.ParameterAndTimeSpanChangeListener;
import net.jini.core.event.RemoteEvent;
import java.util.EventObject;
import net.jini.core.event.UnknownEventException;

/**
 * <p>Title: RemoteERF_ListImpl</p>
 * <p>Description: This class provides the handle to remotely existing ERF List.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created Aug 05,2004
 * @version 1.0
 */

public class RemoteERF_ListImpl
    extends UnicastRemoteObject implements
    RemoteERF_ListAPI, ParameterAndTimeSpanChangeListener {

  private ERF_List erfList = null;
  private static final boolean D = false;
  private ArrayList listenerList = new ArrayList();

  /**
   * creates the EqkRupForecast object based on received className
   *
   * @param className
   * @throws java.rmi.RemoteException
   * @throws IOException
   */
  public RemoteERF_ListImpl(String className) throws java.rmi.RemoteException,
      IOException {
    erfList = (ERF_List) org.opensha.util.ClassUtils.
        createNoArgConstructorClassInstance(className);
    erfList.addParameterAndTimeSpanChangeListener(this);
  }

  /**
   * get the number of Eqk Rup Forecasts in this list
   * @return : number of eqk rup forecasts in this list
   */
  public int getNumERFs() {
    return erfList.getNumERFs();
  }

  /**
   * adds the listener obj to list. When the change events come, all
   * listeners added to it are notified of it.
   * @param obj Object
   */
  public void addParameterAndTimeSpanChangeListener(RemoteEventListener obj) throws
      RemoteException {
    listenerList.add(obj);
  }

  /**
   * Loops over all the adjustable parameters and set parameter with the given
   * name to the given value.
   * First checks if the parameter is contained within the ERF adjustable parameter
   * list or TimeSpan adjustable parameters list. If not then return false.
   * @param name String Name of the Adjustable Parameter
   * @param value Object Parameeter Value
   * @return boolean boolean to see if it was successful in setting the parameter
   * value.
   */
  public boolean setParameter(String name, Object value) throws
      RemoteException {
    return erfList.setParameter(name, value);
  }

  /**
   * get the ERF in the list with the specified index
   * @param index : index of Eqk rup forecast to return
   * @return
   */
  public RemoteEqkRupForecastAPI getRemoteERF(int index) {
    return erfList.getRemoteERF(index);
  }

  /**
   * get the weight of the ERF at the specified index
   * @param index : index of ERF
   * @return : relative weight of ERF
   */
  public double getERF_RelativeWeight(int index) {
    return erfList.getERF_RelativeWeight(index);
  }

  /**
   * Return the vector containing the Double values with
   * relative weights for each ERF
   * @return : ArrayList of Double values
   */
  public ArrayList getRelativeWeightsList() {
    return erfList.getRelativeWeightsList();
  }

  /**
   * return the list of adjustable params
   * @return
   */
  public ListIterator getAdjustableParamsIterator() {
    return erfList.getAdjustableParamsIterator();
  }

  /**
   * get the name of this class
   * @return
   */
  public String getName() {
    return erfList.getName();
  }

  /**
   * Checks whether this location lies wothin the applicable region of this ERF list
   * @param loc : Location to check
   */
  public boolean isLocWithinApplicableRegion(Location loc) {
    return true;
  }

  /* (non-Javadoc)
   * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#updateForecast()
   */
  public void updateForecast() throws
      RemoteException {
    erfList.updateForecast();
  }

  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
  public GeographicRegion getApplicableRegion() {
    return erfList.getApplicableRegion();
  }

  /**
   * This method sets the time-span field.
   * @param time
   */
  public void setTimeSpan(TimeSpan time) {
    erfList.setTimeSpan(time);
  }

  /**
   *
   * @returns the adjustable ParameterList for the ERF
   */
  public ParameterList getAdjustableParameterList() {
    return erfList.getAdjustableParameterList();
  }

  /**
   * get the timespan
   *
   * @return : TimeSpan
   */
  public TimeSpan getTimeSpan() {
    return erfList.getTimeSpan();
  }

  /**
   * sets the value for the parameter change flag
   * @param flag
   */
  public void setParameterChangeFlag(boolean flag) {
    erfList.setParameterChangeFlag(flag);
  }

  /**
   *
   * @param paramName
   * @returns the Parameter from the parameter list with param name.
   */
  public ParameterAPI getParameter(String paramName) throws RemoteException {
    // TODO Auto-generated method stub
    return erfList.getParameter(paramName);
  }

  /**
   * Notifys the Client when any parameter or Timespan changes on the Remote ERF Server
   * @param eventObj EventObject
   */
  public void parameterOrTimeSpanChange(EventObject eventObj) {
    Object obj = eventObj.getSource();
    RemoteEvent event = new RemoteEvent(obj, 0, 0, null);
    int size = listenerList.size();
    for (int i = 0; i < size; ++i) {
      RemoteEventListener listener = (RemoteEventListener) listenerList.get(i);
      try {
        listener.notify(event);
      }
      catch (RemoteException ex) {
      }
      catch (UnknownEventException ex) {
      }
    }
  }

  /**
   * Update the forecast and save it in serialized mode into a file
   * @return
   */
  public String saveForecast() throws
      RemoteException {
    String urlPrefix = "http://gravity.usc.edu/";
    String parentDir = "/opt/install/jakarta-tomcat-4.1.24/webapps/";
    String subDir = "OpenSHA/HazardMapDatasets/savedERFs/";
    String fileName = System.currentTimeMillis() + ".javaobject";
    org.opensha.util.FileUtils.saveObjectInFile(parentDir + subDir + fileName,
                                                erfList);
    return parentDir + subDir + fileName;
  }

}
