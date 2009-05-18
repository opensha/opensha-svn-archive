package org.opensha.sha.earthquake;


import java.util.ArrayList;
import java.util.ListIterator;
import java.util.EventObject;

import org.opensha.param.event.*;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.param.ParameterList;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteEqkRupForecastAPI;


/**
 * <p>Title: ERF_List </p>
 * <p>Description: This is the abstract implementation of ERF_EpistemicListAPI </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public abstract class ERF_EpistemicList implements ERF_EpistemicListAPI,
    TimeSpanChangeListener,ParameterChangeListener {

  // vector to hold the instances of Eqk Rup Forecasts
  protected ArrayList erf_List = new ArrayList();
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
  public EqkRupForecastAPI getERF(int index) {
    EqkRupForecastBaseAPI eqkRupForecast = (EqkRupForecastBaseAPI)erf_List.get(index);
    eqkRupForecast.setTimeSpan(timeSpan);
    return (EqkRupForecastAPI)eqkRupForecast;
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
        ((EqkRupForecastAPI)this.getERF(i)).updateForecast();
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
 public void timeSpanChange(EventObject event) {
   this.parameterChangeFlag = true;
 }

 /**
  *  This is the main function of this interface. Any time a control
  *  paramater or independent paramater is changed by the user in a GUI this
  *  function is called, and a paramater change event is passed in.
  *
  *  This sets the flag to indicate that the sources need to be updated
  *
  * @param  event
  */
 public void parameterChange(ParameterChangeEvent event) {
   parameterChangeFlag = true;
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
    * Loops over all the adjustable parameters and set parameter with the given
    * name to the given value.
    * First checks if the parameter is contained within the ERF adjustable parameter
    * list or TimeSpan adjustable parameters list. If not then return false.
    * @param name String Name of the Adjustable Parameter
    * @param value Object Parameeter Value
    * @return boolean boolean to see if it was successful in setting the parameter
    * value.
    */
   public boolean setParameter(String name, Object value){
    if(getAdjustableParameterList().containsParameter(name)){
      getAdjustableParameterList().getParameter(name).setValue(value);
      return true;
    }
    else if(timeSpan.getAdjustableParams().containsParameter(name)){
      timeSpan.getAdjustableParams().getParameter(name).setValue(value);
      return true;
    }
    return false;
   }



  /**
   *
   * @param index
   * @returns the instance of the remotely existing ERF in the ERF List
   * on the server given the index.
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
    return null;
  }


}

