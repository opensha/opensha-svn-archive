package org.scec.sha.earthquake;


import java.util.ArrayList;
import java.util.ListIterator;
import java.util.EventObject;

import org.scec.param.event.*;
import org.scec.data.Location;
import org.scec.data.TimeSpan;
import org.scec.param.ParameterList;
import org.scec.param.ParameterAPI;
import org.scec.data.region.GeographicRegion;
import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_API;


/**
 * <p>Title: ERF_List </p>
 * <p>Description: This class holds the list of Eqk Rup Forecast </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public abstract class ERF_List implements EqkRupForecastAPI, ERF_ListAPI,
    TimeSpanChangeListener,ParameterChangeListener {

  // vector to hold the instances of Eqk Rup Forecasts
  private ArrayList erf_List = new ArrayList();
  //vector to hold relative weight of each ERF
  private ArrayList relativeWeight  = new ArrayList();
  // declaration of the flag to check if any parameter has been changed from its original value.
  protected boolean  parameterChangeFlag = true;
  // parameter list for adjustable params
  protected ParameterList adjustableParams = new ParameterList();
  // time span param
  protected TimeSpan timeSpan;



  /**
   * add a new Eqk Rup forecast to the list
   * @param eqkRupForecast
   */
  protected void addERF(EqkRupForecast eqkRupForecast, double relWeight) {
    erf_List.add(eqkRupForecast);
    this.relativeWeight.add(new Double(relWeight));
  }


  /**
   * get the number of Eqk Rup Forecasts in this list
   * @return : number of eqk rup forecasts in this list
   */
  public int getNumERFs() {
    return erf_List.size();
  }


  /**
   * get the ERF in the list with the specified index
   * @param index : index of Eqk rup forecast to return
   * @return
   */
  public ERF_API getERF(int index) {
    return (ERF_API)erf_List.get(index);
  }

  /**
   * get the weight of the ERF at the specified index
   * @param index : index of ERF
   * @return : relative weight of ERF
   */
  public double getERF_RelativeWeight(int index) {
    return ((Double)relativeWeight.get(index)).doubleValue();
  }

  /**
   * Return the vector containing the Double values with
   * relative weights for each ERF
   * @return : ArrayList of Double values
   */
  public ArrayList getRelativeWeightsList() {
    return relativeWeight;
  }

  /**
   * return the list of adjustable params
   * @return
   */
  public ListIterator getAdjustableParamsIterator() {
    return adjustableParams.getParametersIterator();
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

  /**
   * update the list of the ERFs based on the new parameters
   */
  public void updateForecast() {
    if(this.parameterChangeFlag) {
      int num = erf_List.size();
      for(int i=0; i< num; ++i)
        ((EqkRupForecast)this.getERF(i)).updateForecast();
    }
    this.parameterChangeFlag = false;
  }

  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
  public GeographicRegion getApplicableRegion() {
    return null;
  }

  /**
   * This method sets the time-span field.
   * @param time
   */
  public void setTimeSpan(TimeSpan time) {
    timeSpan = time;
    int  numERFs = erf_List.size();
    for(int i=0; i<numERFs; ++i)
      ((EqkRupForecast)erf_List.get(i)).setTimeSpan(time);
 }


 /**
  *
  * @returns the adjustable ParameterList for the ERF
  */
 public ParameterList getAdjustableParameterList(){
   return this.adjustableParams;
 }


 /**
  * get the timespan
  *
  * @return : TimeSpan
  */
 public TimeSpan getTimeSpan() {
   return this.timeSpan;
 }

 /**
  *  Function that must be implemented by all Timespan Listeners for
  *  ParameterChangeEvents.
  *
  * @param  event  The Event which triggered this function call
  */
 public void parameterChange( EventObject event ) {
   this.parameterChangeFlag = true;
 }


 /**
  * this function is called whenever any parameter changes in the
  * adjustable parameter list
  * @param e
  */
 public void parameterChange(ParameterChangeEvent e) {
   // set the parameter change flag to indicate that forecast needs to be updated
   this.parameterChangeFlag = true;
 }


 /**
  * sets the value for the parameter change flag
  * @param flag
  */
 public void setParameterChangeFlag(boolean flag){
   this.parameterChangeFlag = flag;
 }

 /**
   * Update the forecast and save it in serialized mode into a file
   * @return
   */
  public String updateAndSaveForecast() {
    throw new UnsupportedOperationException("updateAndSaveForecast() not supported");
  }

  /**
   * This function returns the parameter with specified name from adjustable param list
   * @param paramName : Name of the parameter needed from adjustable param list
   * @return : ParamterAPI instance
   */
  public ParameterAPI getParameter(String paramName) {
    return adjustableParams.getParameter(paramName);
  }

  /**
   *
   * @param index
   * @returns the instance of the remotely existing ERF in the ERF List
   * on the server given the index.
   * **NOTE: All the functionality in this functionlity remains same as that of getERF but only differs
   * when returning each ERF from the ERF List. getERF() return the instance of the
   * ERF_API which is transferring the whole object on to the user's machine, but this functin
   * return back the RemoteERF_API. This is useful becuase whole ERF object does not
   * get transfer to the users machine, just a stub of the remotely existing ERF gets
   * transferred.
   *
   * This function returns null, but if anyone needs to host his ERF as the remote
   * then he will have to implement this method.
   */
  public RemoteERF_API getRemoteERF(int index){
    return null;
  }


}

