package org.scec.sha.earthquake.rupForecastImpl.remote;


import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.scec.param.ParameterAPI;
import org.scec.sha.earthquake.*;
import org.scec.data.Location;
import org.scec.data.TimeSpan;
import org.scec.data.region.GeographicRegion;
import org.scec.param.ParameterList;

/**
 * <p>Title: RemoteERF_ListImpl</p>
 * <p>Description: This class provides the handle to remotely existing ERF List.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created Aug 05,2004
 * @version 1.0
 */

public class RemoteERF_ListImpl extends UnicastRemoteObject implements RemoteERF_ListAPI{

  private ERF_List erfList = null;
  private static final boolean D = false;

  /**
   * creates the EqkRupForecast object based on received className
   *
   * @param className
   * @throws java.rmi.RemoteException
   * @throws IOException
   */
  public RemoteERF_ListImpl(String className)
      throws java.rmi.RemoteException, IOException {
    erfList = (ERF_List)org.scec.util.ClassUtils.createNoArgConstructorClassInstance(className);;
  }

  /**
   * add a new Eqk Rup forecast to the list
   * @param eqkRupForecast
   */
  public void addERF(EqkRupForecast eqkRupForecast, double relWeight) {
    erfList.addERF(eqkRupForecast,relWeight);
  }


  /**
   * get the number of Eqk Rup Forecasts in this list
   * @return : number of eqk rup forecasts in this list
   */
  public int getNumERFs() {
    return erfList.getNumERFs();
  }


  /**
   * get the ERF in the list with the specified index
   * @param index : index of Eqk rup forecast to return
   * @return
   */
  public ERF_API getERF(int index) {
    return (ERF_API)erfList.getERF(index);
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
    return "List of Earthquake Rupture Forecasts";
  }


  /**
   * Checks whether this location lies wothin the applicable region of this ERF list
   * @param loc : Location to check
   */
  public boolean isLocWithinApplicableRegion(Location loc) {
    return true;
  }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#updateForecast()
    */
   public void updateForecast(ParameterList list, TimeSpan timeSpan) throws
       RemoteException {

     erfList.setParameterChangeFlag(true);
     Iterator it = list.getParametersIterator();
     while (it.hasNext()) {
       ParameterAPI param = (ParameterAPI) it.next();
       erfList.getParameter(param.getName()).setValue(param.getValue());
       if(D) System.out.println("Param Name:"+param.getName()+",value="+param.getValue());
     }
     erfList.setTimeSpan(timeSpan);
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
 public ParameterList getAdjustableParameterList(){
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
 public void setParameterChangeFlag(boolean flag){
   erfList.setParameterChangeFlag(flag);
 }

 /**
   * Update the forecast and save it in serialized mode into a file
   * @return
   */
 public String updateAndSaveForecast(ParameterList list, TimeSpan timeSpan) throws
 RemoteException {
   this.updateForecast(list, timeSpan);
   String urlPrefix = "http://gravity.usc.edu/";
   String parentDir = "/opt/install/jakarta-tomcat-4.1.24/webapps/";
   String subDir = "OpenSHA/HazardMapDatasets/savedERFs/";
   String fileName = System.currentTimeMillis() + ".javaobject";
   org.scec.util.FileUtils.saveObjectInFile(parentDir + subDir + fileName,
       erfList);
   return parentDir + subDir + fileName;
 }

}