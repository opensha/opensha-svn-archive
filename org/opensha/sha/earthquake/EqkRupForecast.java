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

  private DecimalFormat magFormat = new DecimalFormat("0.00");
  private DecimalFormat latLonFormat = new DecimalFormat("0.00##");


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
   * This function returns the total Rate above a given magnitude ,
   * for the given geographic region.
   * Calcuated Rates depend on the ERF model instantiated by the user.
   * @param magnitude double  : magnitude above which rate needs to be returned
   *
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */
  public double getTotalRateAbove(double magnitude, GeographicRegion region) {

    int numSources = getNumSources();

    double totalRate = 0;
    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = this.getSource(sourceIndex);
      int numRuptures = source.getNumRuptures();

      //going over all the ruptures in the source
      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
        ProbEqkRupture rupture = source.getRupture(rupIndex);

        double mag = rupture.getMag();
        //if rupture magnitude is less then given magnitude then skip those ruptures
        if (mag < magnitude)
          continue;

        GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
        long numPts = rupSurface.size();

        //getting the rate at each Point on the rupture( calculated by first
        //getting the rate of the rupture and then dividing by number of points
        //on that rupture.
        double ptRate = ( -Math.log(1 - rupture.getProbability()) /
                         getTimeSpan().getDuration()) / numPts;

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();

        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          if (!region.isLocationInside(ptLoc))
            continue;
          totalRate +=ptRate;
        }
      }
    }
    return totalRate;
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




    double gridSpacing = region.getGridSpacing();

    //computing the rates for magnitudes for each location on rupture in the ERF.
    HashMap regionMagRatesEmpDistList = calcSeisRatesForGriddedRegion(region,mag,gridSpacing);

    //creating the Hashmap
    ArrayList locsMagRateDist = new ArrayList();

    Set set = regionMagRatesEmpDistList.keySet();
    Iterator it = set.iterator();
    while (it.hasNext()) {
      Location loc = (Location) it.next();

      ArbDiscrEmpiricalDistFunc empDist = (ArbDiscrEmpiricalDistFunc)
          regionMagRatesEmpDistList.get(loc);
      //iterates over all the ArbDiscrEmpiricalDistFunc ( for each location in the region),
      //and adds those distribution that store mag-rate distriution.
      int numEmpDistElemnents = empDist.getNum();

      if (numEmpDistElemnents == 0) {
        //System.out.println(loc);
        continue;
      }
      else {
        ArbitrarilyDiscretizedFunc func = empDist.getCumDist();

        int numFuncs = func.getNum();
        ArbitrarilyDiscretizedFunc magRateFunction = new
            ArbitrarilyDiscretizedFunc();
        int magIndex = 0;
        for (int j = magIndex; j < numFuncs; ++j, ++magIndex) {
          double rates = func.getY(func.getNum() - 1) - func.getY(magIndex);
          magRateFunction.set(func.getX(magIndex), rates);
        }
        locsMagRateDist.add(new DataObject2D(loc,magRateFunction));
      }
    }
    return locsMagRateDist;
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

   double gridSpacing = region.getGridSpacing();

   //computing the rates for magnitudes for each location on rupture in the ERF.
   HashMap regionMagRatesEmpDistList = calcTotalSeisRatesForGriddedRegion(region, mag, gridSpacing);
   ArrayList locTotalSiesRatesList = new ArrayList();
   Set set = regionMagRatesEmpDistList.keySet();
   Iterator it = set.iterator();
   while(it.hasNext()){
     Location loc = (Location)it.next();
     Double totalRate = (Double)regionMagRatesEmpDistList.get(loc);
     locTotalSiesRatesList.add(new DataObject2D(loc,totalRate));
   }
   return locTotalSiesRatesList;
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
    ArbDiscrEmpiricalDistFunc magRateDist = new ArbDiscrEmpiricalDistFunc();
    int numSources = getNumSources();
    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = getSource(sourceIndex);
      int numRuptures = source.getNumRuptures();

      //going over all the ruptures in the source
      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
        ProbEqkRupture rupture = source.getRupture(rupIndex);

        double mag = rupture.getMag();
        //if rupture magnitude is less then given magnitude then skip those ruptures
        if (mag < magnitude)
          continue;

        GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
        long numPts = rupSurface.size();

        //getting the rate at each Point on the rupture( calculated by first
        //getting the rate of the rupture and then dividing by number of points
        //on that rupture.
        double ptRate = getRupturePtRate(rupture,numPts);

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();
        //looping over all the rupture pt location and finding the nearest location
        //to them in the Geographical Region.
        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          //discard the pt location on the rupture if outside the region polygon
          if (!region.isLocationInside(ptLoc))
            continue;
          String ruptureMag = magFormat.format(mag);
          magRateDist.set(Double.parseDouble(ruptureMag), ptRate);
        }
      }
    }
    return magRateDist;
  }



  /*
   * Computing the Rate for each location on the rupture
   * @param eqkRupForecast EqkRupForecastAPI
   * @param rupture ProbEqkRupture
   * @param numPts long
   * @return double
   */
  private double getRupturePtRate(ProbEqkRupture rupture, long numPts) {
    return (-Math.log(1 - rupture.getProbability()) /
        getTimeSpan().getDuration())/numPts;
  }




  /*
   * computes the Total Seis Rate for each location in the region
   */
  private HashMap calcTotalSeisRatesForGriddedRegion(
      EvenlyGriddedGeographicRegionAPI region, double magnitude, double gridSpacing) {

     int numSources = getNumSources();
     HashMap regionMagRatesEmpDistList = new HashMap();
     int numLocations = region.getNumGridLocs();
     for (int i = 0; i < numLocations; ++i)
       regionMagRatesEmpDistList.put(region.getGridLocation(i),
                                     new Double(0.0));

     //Going over each and every source in the forecast
     for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
       // get the ith source
       ProbEqkSource source = getSource(sourceIndex);
       int numRuptures = source.getNumRuptures();

       //going over all the ruptures in the source
       for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
         ProbEqkRupture rupture = source.getRupture(rupIndex);

         double mag = rupture.getMag();
         //if rupture magnitude is less then given magnitude then skip those ruptures
         if (mag < magnitude)
           continue;

         GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
         long numPts = rupSurface.size();

         //getting the rate at each Point on the rupture( calculated by first
         //getting the rate of the rupture and then dividing by number of points
         //on that rupture.
         double ptRate = getRupturePtRate(rupture,numPts);

         //getting the iterator for all points on the rupture
         ListIterator it = rupSurface.getAllByRowsIterator();
         //looping over all the rupture pt location and finding the nearest location
         //to them in the Geographical Region.
         while (it.hasNext()) {
           Location ptLoc = (Location) it.next();
           //discard the pt location on the rupture if outside the region polygon
           if (!((GeographicRegion) region).isLocationInside(ptLoc))
             continue;
           Location loc = getNearestLocationForGriddedRegion(ptLoc, gridSpacing);
           double rate = ((Double)regionMagRatesEmpDistList.get(loc)).doubleValue();
           rate += ptRate;
           regionMagRatesEmpDistList.put(loc,new Double(rate));
         }
       }
     }
     return regionMagRatesEmpDistList;
   }



   /*
    * computes the Mag-Rate distribution for each location within the provided region.
    */
   private HashMap calcSeisRatesForGriddedRegion(EvenlyGriddedGeographicRegionAPI region,
       double magnitude, double gridSpacing) {

    int numSources = getNumSources();

    HashMap regionMagRatesEmpDistList = new HashMap();
    int numLocations = region.getNumGridLocs();
    for (int i = 0; i < numLocations; ++i)
      regionMagRatesEmpDistList.put(region.getGridLocation(i),
                                    new ArbDiscrEmpiricalDistFunc());

    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = getSource(sourceIndex);
      int numRuptures = source.getNumRuptures();

      //going over all the ruptures in the source
      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
        ProbEqkRupture rupture = source.getRupture(rupIndex);

        double mag = rupture.getMag();
        //if rupture magnitude is less then given magnitude then skip those ruptures
        if (mag < magnitude)
          continue;

        GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
        long numPts = rupSurface.size();

        //getting the rate at each Point on the rupture( calculated by first
        //getting the rate of the rupture and then dividing by number of points
        //on that rupture.
        double ptRate = getRupturePtRate(rupture,numPts);

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();
        //looping over all the rupture pt location and finding the nearest location
        //to them in the Geographical Region.
        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          //discard the pt location on the rupture if outside the region polygon
          if (!((GeographicRegion) region).isLocationInside(ptLoc))
            continue;
          Location loc = getNearestLocationForGriddedRegion(ptLoc, gridSpacing);
          createMagRateEmpDist(mag, ptRate, loc,regionMagRatesEmpDistList);
        }
      }
    }
    return regionMagRatesEmpDistList;
  }


  /*
   * This function , for a given location index in the region adds data to Mag-Rate
   * Empirilcal Dist. at that index.
   * @param mag double : Magnitude
   * @param ptRate double : Rate for a given location on the Eqk rupture
   * @param loc location of the point on rupture mapped to nearest location
   * in Gridded Region.
   */
  private void createMagRateEmpDist(double mag, double ptRate, Location loc,
      HashMap regionMagRatesEmpDistList) {


    ArbDiscrEmpiricalDistFunc magRateDist = (ArbDiscrEmpiricalDistFunc)
        regionMagRatesEmpDistList.get(loc);
    String magnitude = magFormat.format(mag);
    magRateDist.set(Double.parseDouble(magnitude), ptRate);
  }

  /**
   * This function returns the nearest location from the EvenlyGridded Geographic Region for
   * point on the rupture.
   * It compares each location on the rupture with the location in the EvenlyGridded
   * Geographic region,to get the nearest location.
   * The comparison is done by calculation of distance between the 2 locations.
   * @param rupPointLoc Location Rupture Pt Location
   * @param gridSpacing Regional gridSpacing
   * @return Location  Nearest location in the Gridded Region to the rupture
   * point location.
   */
  private Location getNearestLocationForGriddedRegion(Location rupPointLoc,double gridSpacing) {

    //Getting the nearest Location to the rupture point location
    double lat = Math.rint(rupPointLoc.getLatitude() / gridSpacing) *
        gridSpacing;
    double lon = Math.rint(rupPointLoc.getLongitude() / gridSpacing) *
        gridSpacing;
    lat = Double.parseDouble(latLonFormat.format(lat));
    lon = Double.parseDouble(latLonFormat.format(lon));
    return new Location(lat, lon);
  }

}
