package org.opensha.sha.earthquake;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.data.Location;
import org.opensha.data.TimeSpan;
import org.opensha.data.region.GeographicRegion;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.event.TimeSpanChangeListener;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.DataObject2D;
import java.util.HashMap;
import java.util.Set;
import java.text.DecimalFormat;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class EqkRupForecast implements EqkRupForecastAPI,
    TimeSpanChangeListener,ParameterChangeListener {

  // adjustable params for each forecast
  protected ParameterList adjustableParams = new ParameterList();
  // timespan object for each forecast
  protected TimeSpan timeSpan;

  // it is flag which indiactes whether any parameter have changed.
  // if it is true it means that forecast needs to be updated
  protected boolean parameterChangeFlag = true;


  /**
   * get the adjustable parameters for this forecast
   *
   * @return
   */
  public ListIterator getAdjustableParamsIterator() {
    return adjustableParams.getParametersIterator();
  }

  /**
   * This function finds whether a particular location lies in applicable
   * region of the forecast
   *
   * @param loc : location
   * @return: True if this location is within forecast's applicable region, else false
   */
  public boolean isLocWithinApplicableRegion(Location loc) {
    return true;
  }

  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
  public GeographicRegion getApplicableRegion() {
    return null;
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
   * set the TimeSpan in the ERF
   * @param timeSpan : TimeSpan object
   */
  public void setTimeSpan(TimeSpan time) {
    // set the start time
    if (!time.getStartTimePrecision().equalsIgnoreCase(TimeSpan.NONE))
      this.timeSpan.setStartTime(time.getStartTimeCalendar());
    //set the duration as well
    this.timeSpan.setDuration(time.getDuration(), time.getDurationUnits());
  }

  /**
   * return the time span object
   *
   * @return : time span object is returned which contains start time and duration
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
  public void parameterChange(EventObject event) {
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
   * Get the number of earthquake sources
   *
   * @return integer value spcifying the number of earthquake sources
   */
  public abstract int getNumSources();

  /**
   * Return the earhthquake source at index i. This methos returns the reference to
   * the class variable. So, when you call this method again, result from previous
   * method call is no longer valid.
   * this is secret, fast but dangerous method
   *
   * @param i : index of the source needed
   *
   * @return Returns the ProbEqkSource at index i
   *
   */
  public abstract ProbEqkSource getSource(int iSource);

  /**
   * Get the list of all earthquake sources. Clone is returned.
   * So, list can be save in ArrayList and this object subsequently destroyed
   *
   * @return ArrayList of Prob Earthquake sources
   */
  public abstract ArrayList getSourceList();

  /**
   * Get number of ruptures for source at index iSource
   * This method iterates through the list of 3 vectors for charA , charB and grB
   * to find the the element in the vector to which the source corresponds
   * @param iSource index of source whose ruptures need to be found
   */
  public int getNumRuptures(int iSource) {
    return getSource(iSource).getNumRuptures();
  }

  /**
   * Get the ith rupture of the source. this method DOES NOT return reference
   * to the object. So, when you call this method again, result from previous
   * method call is valid. This behavior is in contrast with
   * getRupture(int source, int i) method
   *
   * @param source
   * @param i
   * @return
   */
  public ProbEqkRupture getRuptureClone(int iSource, int nRupture) {
    return getSource(iSource).getRuptureClone(nRupture);
  }

  /**
   * Get the ith rupture of the source. this method DOES NOT return reference
   * to the object. So, when you call this method again, result from previous
   * method call is valid. This behavior is in contrast with
   * getRupture(int source, int i) method
   *
   * @param source
   * @param i
   * @return
   */
  public ProbEqkRupture getRupture(int iSource, int nRupture) {
    return getSource(iSource).getRupture(nRupture);
  }

  /**
   * Return  iterator over all the earthquake sources
   *
   * @return Iterator over all earhtquake sources
   */
  public Iterator getSourcesIterator() {
    Iterator i = getSourceList().iterator();
    return i;
  }

  /**
   * Return the earthquake source at index i. This methos DOES NOT return the
   * reference to the class variable. So, when you call this method again,
   * result from previous method call is still valid. This behavior is in contrast
   * with the behavior of method getSource(int i)
   *
   * @param iSource : index of the source needed
   *
   * @return Returns the ProbEqkSource at index i
   *
   * FIX:FIX :: This function has not been implemented yet. Have to give a thought on that
   *
   */
  public ProbEqkSource getSourceClone(int iSource) {
    return null;
  }

  /**
   *
   * @returns the adjustable ParameterList for the ERF
   */
  public ParameterList getAdjustableParameterList() {
    return this.adjustableParams;
  }

  /**
   * sets the value for the parameter change flag
   * @param flag
   */
  public void setParameterChangeFlag(boolean flag) {
    this.parameterChangeFlag = flag;
  }

  /**
   * Update the forecast and save it in serialized mode into a file
   * @return
   */
  public String updateAndSaveForecast() {
    throw new UnsupportedOperationException(
        "updateAndSaveForecast() not supported");
  }



  /**
   * This function returns the total probability of events above a given magnitude
   * within the given geographic region.  The calcuated Rates depend on the  ERF
   * subclass.  Note that it is assumed that the forecast has been updated.
   * @param magnitude double  : magnitude above which rate needs to be returned
   *
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */
  public double getTotalProbAbove(double magnitude, GeographicRegion region) {

    int numSources = getNumSources();
    int numRuptures;
    double totalProb = 1.0;  // intialize as probability of none
    double srcProb;

    //Going over every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = this.getSource(sourceIndex);
      numRuptures = source.getNumRuptures();

      // initialize the source probability
      if(source.isPoissonian)
        srcProb = 1;        // initial probability of no events
      else
        srcProb = 0;        // initial probability of an event

      //going over all the ruptures in the source
      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
        ProbEqkRupture rupture = source.getRupture(rupIndex);

        //if rupture magnitude is less then given magnitude then skip those ruptures
        if (rupture.getMag() < magnitude)
          continue;

        GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
        double ptProb = rupture.getProbability()/rupSurface.size();

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();

        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          // jump out if not inside region
          if (!region.isLocationInside(ptLoc))
            continue;

          if(source.isPoissonian)
            srcProb *= (1-ptProb);     // the probability of none
          else
            srcProb += ptProb;         // the probability of an event
        }
      }
      // convert back to prob of one or more if Poissonian
      if(source.isPoissonian)
        srcProb = 1.0-srcProb;
      totalProb *= (1.0-srcProb);        // the total probability of none
    }
    return 1-totalProb;    // return the probability of one or more
  }



  /**
   * This function returns the total equivalent Poissonian rate above a given magnitude
   * for the given geographic region.  The result should be exact for ERFs where all
   * sources are Poissonian, or for ERFs with non-Poisson sources but low probabilities.
   *  The calcuated Rates depend on the  ERF subclass.
   * @param magnitude double  : magnitude above which rate needs to be returned
   *
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */
  public double getTotalRateAbove(double magnitude, GeographicRegion region) {

    return 0.0;
  }


  /**
   * This function computes the rates above the given Magnitude for each rupture
   * location. Once computed , magnitude-rate distribution is stored for each
   * location on all ruptures in Eqk Rupture forecast model, if that lies within the
   * provided EvenlyGriddedGeographicRegion.
   * Once all Mag-Rate distribution has been computed for each location within the
   * ERF, this function returns list of DataObject2D object that constitutes of
   * 2 objects, with X object being Location and Y object being
   * ArbitrarilyDiscretizedFunc. This ArbitrarilyDiscretizedFunc for each location
   * is the Mag-Rate distribution with X values being Mag and Y values being Rate.
   * @param mag double : Magnitude above which Mag-Rate distribution is to be computed.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Ruptureforecast model
   * @param region EvenlyGriddedGeographicRegionAPI Region within which ruptures
   * are to be considered.
   * @return ArrayList of DataObject2D with X object being Location and Y object
   * being ArbitrarilyDiscretizedFunc
   * @see ArbitrarilyDiscretizedFunc, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion
   */
  public ArrayList getMagRateDistForEachLocationInRegion(double mag,
                               EvenlyGriddedGeographicRegionAPI region) {


    return null;

  }


  /**
   * This function computes the total SiesRate for each location on all the ruptures,
   * if they are within the provided Geographical Region.
   * It returns a list of DataObject2D that store location as X-Object and its
   * corresponding total seis rate(Double Object) as Y-Object.
   * @param mag double : Only those ruptures above this magnitude are considered
   * for calculation of the total seis rates in the region.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Rupture forecast model
   * @param region EvenlyGriddedGeographicRegionAPI
   * @return ArrayList of DataObject2D with X object being Location and Y object
   * being Double Object respresenating the TotalSeisRate
   * @see Double, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion

   */
  public ArrayList getTotalSeisRateAtEachLocationInRegion(double mag,
                              EvenlyGriddedGeographicRegionAPI region) {


   return null;
 }


  /**
   * This function returns the ArbDiscrEmpirical object that holds the
   * Mag-Rate of the entire region.
   * @param magnitude double  Ruptures above this magnitude will be the ones that
   * will considered within the provided region  for computing the Mag-Rate Dist.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Rupture Forecast from which
   * ruptures will computed.
   * @param region GeographicRegion Region for which mag-rate distribution has to be
   * computed.
   * @return ArbDiscrEmpiricalDistFunc : Distribution function that holds X values
   * as the magnitude and Y values as the sies rate for corresponding magnitude within
   * the region.
   */
  public ArbDiscrEmpiricalDistFunc getMagRateDistForRegion(double magnitude,
                                  GeographicRegion region) {
      return null;
  }



}
