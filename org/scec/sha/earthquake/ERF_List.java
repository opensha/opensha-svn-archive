package org.scec.sha.earthquake;


import java.util.Vector;
import java.util.ListIterator;

import org.scec.data.Location;
/**
 * <p>Title: ERF_List </p>
 * <p>Description: This class hols the list of Eqk Rup Forecast </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class ERF_List {

  // vector to hold the instances of Eqk Rup Forecasts
  private Vector erf_List = new Vector();
  //vector to hold relative weight of each ERF
  private Vector relativeWeight  = new Vector();



  /**
   * add a new Eqk Rup forecast to the list
   * @param eqkRupForecast
   */
  protected void addERF(EqkRupForecastAPI eqkRupForecast, double relWeight) {
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
    return (EqkRupForecastAPI)erf_List.get(index);
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
   * get the name of this class
   * @return
   */
  public String getName() {
    return "list of Earthquake Rupture Forecasts";
  }


  /**************
   *  add the method getApplicableRegion after geogriphical region classes are made
   *****************/


  /**
   * Checks whether this location lies wothin the applicable region of this ERF list
   * @param loc : Location to check
   */
  public abstract boolean isLocWithinApplicableRegion(Location loc);


  /**
   * get the adjustable parameters for this List
   *
   * @return
   */
  public abstract ListIterator getAdjustableParamsList();


  /**
   * update the list of the ERFs based on the new parameters
   */
  public abstract void updateERF_List();



}

