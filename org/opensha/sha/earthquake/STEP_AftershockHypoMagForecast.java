package org.opensha.sha.earthquake;

import java.util.*;
import javaDevelopers.matt.calc.*;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.*;
import org.opensha.data.*;
import org.opensha.data.region.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.fault.*;
import org.opensha.sha.magdist.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public abstract class STEP_AftershockHypoMagForecast extends AfterShockHypoMagFreqDistForecast {
  public double minForecastMag = 4.0;
  private double maxForecastMag = 8.0;
  private double deltaMag = 0.1;
  private int numHypoLocation;
  private double[] grid_aVal, grid_bVal, grid_cVal, grid_pVal, grid_kVal;
  private double[] node_CompletenessMag;
  private SimpleFaultData mainshockFault;
  public boolean useFixed_cValue = true;
  private boolean hasExternalFaultModel = false;
  public double addToMc = .2;
  private double zoneRadius;
  public ObsEqkRupList newObsEventList;
  private double gridSpacing = 0.05;
  private double forecastEndTime, forecastStartTime;
  private ArrayList griddedMagFreqDistForecast;

  /**
  * calc_NodeCompletenessMag
  * calculate the completeness at each node
  */
 public abstract void calc_NodeCompletenessMag();

 /**
  * set_minForecastMag
  * the minimum forecast magnitude
  */
 public void set_minForecastMag(double min_forecastMag) {
 minForecastMag = min_forecastMag;
 }

 /**
  * set_maxForecastMag
  * the maximum forecast magnitude
  */
 public void set_maxForecastMag(double max_forecastMag) {
 maxForecastMag = max_forecastMag;
 }

  /**
   * set_deltaMag
   * the magnitude step for the binning of the forecasted magnitude
   */
  public void set_deltaMag(double delta_mag) {
  deltaMag = delta_mag;
  }

  /**
   * set_GridSpacing
   */
  public void set_GridSpacing(double grid_spacing) {
    gridSpacing = grid_spacing;
  }

  /**
   * setUseFixed_cVal
   * if true c will be fixed for the Omori calculations
   * default is fixed
   */
  public void setUseFixed_cVal(boolean fix_cVal) {
    useFixed_cValue = fix_cVal;
  }

  /**
   * set_Gridded_aValue
   */
  public abstract void set_Gridded_aValue();

  /**
    * set_Gridded_bValue
    */
   public abstract void set_Gridded_bValue();


   /**
  * set_Gridded_pValue
  */
 public abstract void set_Gridded_pValue();

 /**
   * set_Gridded_cValue
   */
  public abstract void set_Gridded_cValue();

  /**
   * set_addToMcConstant
   */
  public void set_addToMcConstant(double mcConst) {
    addToMc = mcConst;
  }

  /**
   * setNewObsEventsList
   * This should contain All new events - this is the list that will
   * be used to look for new aftershocks.
   */
  public void setNewObsEventsList(ObsEqkRupList newObsEventList) {
  }

  /**
   * set_PreviousAftershocks
   * this will pass the aftershocks for this sequence that were saved in
   * the last run of the code.
   */
  public void set_PreviousAftershocks(ObsEqkRupList previousAftershockList) {
  }

  /**
   * set_AftershockZoneRadius
   * set the radius based on Wells and Coppersmith
   *
   * THIS USES A DIFFERENT RADIUS THAN I HAVE PREVIOUSLY USED!
   * NEED TO ADD THE SUBSURFACE RUPTURE LENGTH REL TO WC1994
   */
  public void set_AftershockZoneRadius() {
    ObsEqkRupture mainshock = this.getMainShock();
    double mainshockMag = mainshock.getMag();
    WC1994_MagLengthRelationship WCRel = new WC1994_MagLengthRelationship();
    zoneRadius = WCRel.getMedianLength(mainshockMag);
  }

  public void calcAfterShockZone(){
    if (hasExternalFaultModel) {
    }
    else {
      ObsEqkRupture mainshock = this.getMainShock();
      Location mainshockLocation = mainshock.getHypocenterLocation();
      CircularGeographicRegion aftershockZone = new CircularGeographicRegion(
          mainshockLocation, zoneRadius);
      ObsEqkRupList eventsInZoneList = newObsEventList.getObsEqkRupsInside(aftershockZone);
      if (eventsInZoneList.size() > 100) {
        STEP_TypeIIAftershockZone_Calc typeIIcalc = new
            STEP_TypeIIAftershockZone_Calc(eventsInZoneList, this);
        EvenlyGriddedSausageGeographicRegion typeII_Zone = typeIIcalc.get_TypeIIAftershockZone();
        this.setAfterShockZone(typeII_Zone);
      }
    }
  }

  /**
   * calc_AftershocksInZone
   */
  public void calc_AftershocksInZone() {
     EvenlyGriddedGeographicRegionAPI aftershockZone = this.getAfterShockZone();
     //ObsEqkRupList eventsInZone =

  }

  /**
   * findEventsInRegion

  private ObsEqkRupList findEventsInRegion(GeographicRegion zoneRegion, ObsEqkRupList eventList) {
    ObsEqkRupList eventsInZoneList = new ObsEqkRupList();
    ListIterator eventIt = eventList.listIterator();
    while (eventIt.hasNext()) {
      Location loc = (Location)eventIt.next();
      if (zoneRegion.isLocationInside(loc))
          eventsInZoneList.add(loc);
      }

  }
//

/**
   * set_ForecastStartTime
   */
  public void set_ForecastStartTime(double timeStart) {
    forecastStartTime = timeStart;
  }

  /**
   * set_ForecastEndTime
   */
  public void set_ForecastEndTime(double timeEnd) {
    forecastEndTime = timeEnd;
  }

  /**
   * calc_GriddedForecastRates
   */
  public void calc_GriddedForecastRates() {
    double[] rjParms = new double[3];
    double[] timeParms = new double[2];
    timeParms[0] = forecastStartTime;
    timeParms[1] = forecastEndTime;
    EvenlyGriddedGeographicRegionAPI aftershockZone =
        this.getAfterShockZone();
    int numGridNodes = aftershockZone.getNumGridLocs();

    double[] singleGridMagFreqDist = new double[numGridNodes];
    ArrayList griddedMagFreqDistForecast = new ArrayList(numGridNodes);

    for (int gridLoop = 0; gridLoop < numGridNodes; ++gridLoop){
      rjParms[0] = grid_kVal[gridLoop];
      rjParms[1] = grid_cVal[gridLoop];
      rjParms[2] = grid_pVal[gridLoop];
      OmoriRate_Calc calcOmoriRate = new OmoriRate_Calc(rjParms,timeParms);
      double totalForecastEvents = calcOmoriRate.get_OmoriRate();
      GutenbergRichterRate_Calc calcGR_Rate =
          new GutenbergRichterRate_Calc(grid_bVal[gridLoop],totalForecastEvents);
      singleGridMagFreqDist = calcGR_Rate.get_ForecastedRates();
      griddedMagFreqDistForecast.add(singleGridMagFreqDist);
    }
  }

  /**
   * set_completenessMag
   */
  public void set_completenessMag() {
    calc_NodeCompletenessMag();
  }


   /**
    * Set the fault surface that will be used do define a Type II
    * aftershock zone.
    * This will not be used in a spatially varying model.
    */

   public void set_FaultSurface(){
     String faultName = "";
     FaultTrace fault_trace = new FaultTrace(faultName);
     mainshockFault = new SimpleFaultData();
     mainshockFault.setAveDip(90.0);

     //STILL NEED TO SET THE DIMENSIONS OF THE FAULT TRACE.
     mainshockFault.setFaultTrace(fault_trace);
   }

  /**
  * get_minForecastMag
  */
 public double get_minForecastMag() {
   return minForecastMag;
 }

 /**
  * get_maxForecastMag
  */
 public double get_maxForecastMag() {
   return maxForecastMag;
 }

  /**
   * get_deltaMag
   */
  public double get_deltaMag() {
    return deltaMag;
  }


  /**
   * get_Gridded_aVal
   */
  public double[] get_Gridded_aVal() {
    return grid_aVal;
  }

  /**
   * get_Gridded_bVal
   */
  public double[] get_Gridded_bVal() {
    return grid_bVal;
  }

  /**
   * get_Gridded_cVal
   */
  public double[] get_Gridded_cVal() {
    return grid_cVal;
  }

  /**
   * get_Gridded_pVal
   */
  public double[] get_Gridded_pVal() {
    return grid_pVal;
  }


  /**
  * get_Gridded_kVal
  */
  public double[] get_Gridded_kVal() {
    return grid_kVal;
  }

  /**
   * get_nodeCompletenessMag
   */
  public double[] get_nodeCompletenessMag() {
    return node_CompletenessMag;
  }

  /**
   * get_GridSpacing
   */
  public double get_GridSpacing() {
    return gridSpacing;
  }

  /**
   * get_FaultModel
   */
  public SimpleFaultData get_FaultModel() {
    return mainshockFault;
  }

  /**
   * get_addToMcConst
   */
  public double get_addToMcConst() {
    return addToMc;
  }

  /**
   * get_AftershockZoneRadius
   */
  public double get_AftershockZoneRadius() {
    return zoneRadius;
  }

  /**
   * get_griddedMagFreqDistForecast
   */
  public ArrayList get_griddedMagFreqDistForecast() {
    return griddedMagFreqDistForecast;
  }

}
