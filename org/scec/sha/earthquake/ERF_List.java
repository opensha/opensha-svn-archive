package org.scec.sha.earthquake;


import java.util.Vector;
import java.util.ListIterator;

import org.scec.data.Location;
import org.scec.data.TimeSpan;
import org.scec.param.ParameterList;
import org.scec.data.region.GeographicRegion;
/**
 * <p>Title: ERF_List </p>
 * <p>Description: This class holds the list of Eqk Rup Forecast </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class ERF_List implements EqkRupForecastAPI {

  // vector to hold the instances of Eqk Rup Forecasts
  private Vector erf_List = new Vector();
  //vector to hold relative weight of each ERF
  private Vector relativeWeight  = new Vector();
  // declaration of the flag to check if any parameter has been changed from its original value.
  protected boolean  parameterChangeFlag = true;
  // parameter list for adjustable params
  protected ParameterList adjustableParams = new ParameterList();



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
  public EqkRupForecast getERF(int index) {
    return (EqkRupForecast)erf_List.get(index);
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
   * return the list of adjustable params
   * Presently there are no adjustable params.
   * @return
   */
  public ListIterator getAdjustableParamsList() {
    return adjustableParams.getParametersIterator();
  }




  /**
   * get the name of this class
   * @return
   */
  public String getName() {
    return "list of Earthquake Rupture Forecasts";
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
        this.getERF(i).updateForecast();
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
   * This is not implemented yet
   * @param time
   */
  public void setTimeSpan(TimeSpan time) {

  }


  /**
   * This method sets the tim span field
   * this is not implemented yet
   * @param time
   */
  public void setTimeSpan(double yrs) {

  }


}

