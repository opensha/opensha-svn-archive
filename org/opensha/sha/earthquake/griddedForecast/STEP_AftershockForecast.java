package org.opensha.sha.earthquake.griddedForecast;

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
public abstract class STEP_AftershockForecast
    extends AfterShockHypoMagFreqDistForecast {

  public double minForecastMag;
  private double maxForecastMag;
  private double deltaMag;
  private int numHypoLocation;
  private double[] grid_aVal, grid_bVal, grid_cVal, grid_pVal, grid_kVal;
  private double[] node_CompletenessMag;
  private SimpleFaultData mainshockFault;
  public boolean useFixed_cValue = true;
  private boolean hasExternalFaultModel = false;
  public double addToMc;
  private double zoneRadius;
  private double gridSpacing;
  private GregorianCalendar forecastEndTime, currentTime;
  private ArrayList griddedMagFreqDistForecast;
  private boolean isStatic = false, isPrimary = true,
      isSecondary = false, useSeqAndSpatial = false;
  private ObsEqkRupList newAftershocksInZone;
  private RegionDefaults rDefs;
  private TimeSpan timeSpan;
  double daysSinceMainshockStart, daysSinceMainshockEnd;

  /**
   * STEP_AftershockForecast
   */
  public STEP_AftershockForecast() {
  }

  /**
   * setRegionDefaults
   */
  public void setRegionDefaults(RegionDefaults rDefs) {
    this.rDefs = rDefs;
  }

  /**
   * set_useSeqAndSpatial
   */
  public void set_useSeqAndSpatial(boolean useSeqAndSpatial) {
    this.useSeqAndSpatial = useSeqAndSpatial;
  }

  /**
   * calc_NodeCompletenessMag
   * calculate the completeness at each node
   */
  //public abstract void calc_NodeCompletenessMag();

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
   * set_addToMcConstant
   */
  public void set_addToMcConstant(double mcConst) {
    addToMc = mcConst;
  }

  /**
   * set_isStatic
   * if true the sequence will take no more aftershocks
   */
  public void set_isStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  /**
   * set_isPrimary
   * if true the sequence can be any model type (generic, sequence, sp. var)
   * set_isPrimary controls both primary and secondary.
   */
  public void set_isPrimary(boolean isPrimary) {
    this.isPrimary = isPrimary;
    if (isPrimary) {
      this.set_isSecondary(false);
    }
    else {
      this.set_isSecondary(true);
    }

  }

  /**
   * set_isSecondary
   * if isSecondary is true the model will be forced to be generic.
   *
   */
  private void set_isSecondary(boolean isSecondary) {
    this.isSecondary = isSecondary;
  }

  /**
   * setNewObsEventsList
   * This should contain All new events - this is the list that will
   * be used to look for new aftershocks.
   */
  //public void setNewObsEventsList(ObsEqkRupList newObsEventList) {
  //}

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

  /**
   * calcTypeI_AftershockZone
   */
  public void calcTypeI_AftershockZone(EvenlyGriddedGeographicRegionAPI
                                       backGroundRatesGrid) {

    if (hasExternalFaultModel) {
      // This needs to be set up to read an external fault model.
    }
    else {
      ObsEqkRupture mainshock = this.getMainShock();
      Location mainshockLocation = mainshock.getHypocenterLocation();
      EvenlyGriddedCircularGeographicRegion aftershockZone =
          new EvenlyGriddedCircularGeographicRegion(mainshockLocation,
          zoneRadius, gridSpacing);
      aftershockZone.createRegionLocationsList(backGroundRatesGrid);
      this.region = aftershockZone;

      // make a fault that is only a single point.
      String faultName = "typeIfault";
      FaultTrace fault_trace = new FaultTrace(faultName);
      fault_trace.addLocation(mainshock.getHypocenterLocation());
      set_FaultSurface(fault_trace);
    }
  }

  /**
   * This will calculate the appropriate afershock zone based on the availability
   * of an external model, a circular Type I model, and a sausage shaped Type II model
   * Type II is only calculated if more than 100 events are found in the circular
   * Type II model.
   *
   * This will also set the aftershock list.
   */

  public void calcTypeII_AfterShockZone(ObsEqkRupList aftershockList,
                                        EvenlyGriddedGeographicRegionAPI
                                        backGroundRatesGrid) {
    if (hasExternalFaultModel) {
      // This needs to be set up to read an external fault model.
    }
    else {
      STEP_TypeIIAftershockZone_Calc typeIIcalc = new
          STEP_TypeIIAftershockZone_Calc(aftershockList, this);
      EvenlyGriddedSausageGeographicRegion typeII_Zone = typeIIcalc.
          get_TypeIIAftershockZone();
      typeII_Zone.createRegionLocationsList(backGroundRatesGrid);
      this.region = typeII_Zone;
      LocationList faultPoints = typeIIcalc.getTypeIIFaultModel();
      String faultName = "typeIIfault";
      // add the synthetic fault to the fault trace
      // do not add the 2nd element as it is the same as the 3rd (the mainshock location)
      FaultTrace fault_trace = new FaultTrace(faultName);
      fault_trace.addLocation(faultPoints.getLocationAt(0));
      fault_trace.addLocation(faultPoints.getLocationAt(1));
      fault_trace.addLocation(faultPoints.getLocationAt(3));
      set_FaultSurface(fault_trace);
    }
  }



  /**
   * set_CurrentTime
   * this sets the forecast start time as the current time.
   */
  private void set_CurrentTime() {
    Calendar curTime = new GregorianCalendar(TimeZone.getTimeZone(
        "UTC"));
    int year = curTime.get(Calendar.YEAR);
    int month = curTime.get(Calendar.MONTH);
    int day = curTime.get(Calendar.DAY_OF_MONTH);
    int hour24 = curTime.get(Calendar.HOUR_OF_DAY);
    int min = curTime.get(Calendar.MINUTE);
    int sec = curTime.get(Calendar.SECOND);

    GregorianCalendar currentTime = new GregorianCalendar(year, month,
        day, hour24, min, sec);
  }


  /**
   * calcTimeSpan
   */
  public void calcTimeSpan() {
    String durationUnits = "DAYS";
    String timePrecision = "SECONDS";
    TimeSpan timeSpan = new TimeSpan(timePrecision,durationUnits);

    if (rDefs.startForecastAtCurrentTime) {
      set_CurrentTime();
      timeSpan.setStartTime(currentTime);
      timeSpan.setDuration(rDefs.forecastLengthDays);
    }
    else{
      timeSpan.setStartTime(rDefs.forecastStartTime);
      timeSpan.setDuration(rDefs.forecastLengthDays);
    }
    this.setTimeSpan(timeSpan);
  }

  /**
   * setDaysSinceMainshock
   */
  public void setDaysSinceMainshock() {
    String durationUnits = "DAYS";
    GregorianCalendar startDate = timeSpan.getStartTimeCalendar();
    double duration = timeSpan.getDuration(durationUnits);
    ObsEqkRupture mainshock = this.getMainShock();
    GregorianCalendar mainshockDate = mainshock.getOriginTime();
    double startInMils = startDate.getTimeInMillis();
    double mainshockInMils = mainshockDate.getTimeInMillis();
    double timeDiffMils = startInMils - mainshockInMils;
    daysSinceMainshockStart = timeDiffMils/1000.0/60.0/60.0/24.0;
    daysSinceMainshockEnd = daysSinceMainshockStart + duration;
  }

  /**
   * getDaysSinceMainshockStart
   */
  public double getDaysSinceMainshockStart() {
    return daysSinceMainshockStart;
  }

  /**
   * getDaysSinceMainshockEnd
   */
  public double getDaysSinceMainshockEnd() {
    return daysSinceMainshockEnd;
  }


  /**
   * calc_GriddedForecastRates
   */
  public void calc_GriddedForecastRates() {
    double[] rjParms = new double[3];
    double[] timeParms = new double[2];
    //timeParms[0] = forecastStartTime;
    //timeParms[1] = forecastEndTime;
    EvenlyGriddedGeographicRegionAPI aftershockZone =
        this.getAfterShockZone();
    int numGridNodes = aftershockZone.getNumGridLocs();

    double[] singleGridMagFreqDist = new double[numGridNodes];
    ArrayList griddedMagFreqDistForecast = new ArrayList(numGridNodes);

    for (int gridLoop = 0; gridLoop < numGridNodes; ++gridLoop) {
      rjParms[0] = grid_kVal[gridLoop];
      rjParms[1] = grid_cVal[gridLoop];
      rjParms[2] = grid_pVal[gridLoop];
      OmoriRate_Calc calcOmoriRate = new OmoriRate_Calc(rjParms, timeParms);
      double totalForecastEvents = calcOmoriRate.get_OmoriRate();
      GutenbergRichterRate_Calc calcGR_Rate =
          new GutenbergRichterRate_Calc(grid_bVal[gridLoop],
                                        totalForecastEvents);
      singleGridMagFreqDist = calcGR_Rate.get_ForecastedRates();
      griddedMagFreqDistForecast.add(singleGridMagFreqDist);
    }
  }

  /**
   * setHasExternalFaultModel
   */
  public void setHasExternalFaultModel(boolean hasExternalFaultModel) {
    this.hasExternalFaultModel = hasExternalFaultModel;
  }

  /**
   * Set the fault surface that will be used do define a Type II
   * aftershock zone.
   * This will not be used in a spatially varying model.
   */

  public void set_FaultSurface(FaultTrace fault_trace) {
    mainshockFault = new SimpleFaultData();
    mainshockFault.setAveDip(90.0);
    mainshockFault.setFaultTrace(fault_trace);
    mainshockFault.setLowerSeismogenicDepth(rDefs.lowerSeismoDepth);
    mainshockFault.setUpperSeismogenicDepth(rDefs.upperSeismoDepth);
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
   * get_useSeqAndSpatial
   */
  public boolean get_useSeqAndSpatial() {
    return this.useSeqAndSpatial;
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
    return this.addToMc;
  }

  /**
   * get_isStatic
   */
  public boolean get_isStatic() {
    return this.isStatic;
  }

  /**
   * get_isPrimary
   */
  public boolean get_isPrimary() {
    return this.isPrimary;
  }

  /**
   * get_isSecondary
   */
  public boolean get_isSecondary() {
    return this.isSecondary;
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

  /**
   * getHasExternalFaultModel
   */
  public boolean getHasExternalFaultModel() {
    return this.hasExternalFaultModel;
  }


  /**
   * get_NewObsEventsList
   */
  //public ObsEqkRupList get_NewObsEventsList() {
  //  return newObsEventList;
  //}

  /**
   * get_PreviousAftershockList
   */
  //public ObsEqkRupList get_PreviousAftershockList() {
  //  return newObsEventList;
  //}

}
