package org.opensha.sha.calc;

import java.util.*;
import java.text.*;

import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.data.region.*;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.data.Location;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.
    Frankel02_AdjustableEqkRupForecastClient;
import java.rmi.*;
import org.opensha.exceptions.*;
import java.io.FileWriter;
import java.io.*;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.
    Frankel02_AdjustableEqkRupForecast;
import org.opensha.data.DataObject2D;

/**
 * <p>Title: ERF2GriddedSeisRatesCalc</p>
 *
 * <p>Description: This class calculates the rates of the Ekq Rupture for the given magnitude.
 * </p>
 * <p>
 * This class provides the functionality of computing Mag-Rate distribution or
 * total seismic rates using the Earthquake Ruptures from the provided Earthquake
 * Rupture Forecast with a given Geographic Region.
 * </p>
 * @author Nitin Gupta , Vipin Gupta
 * @version 1.0
 */
public class ERF2GriddedSeisRatesCalc {

  //Magnitude above which rates need to be calculated
  private double magnitude;

  //List for storing Mag-Rates Empirical Distributions for all the rupture locations.
  private HashMap regionMagRatesEmpDistList;

  private DecimalFormat magFormat = new DecimalFormat("0.00");
  private DecimalFormat latLonFormat = new DecimalFormat("0.00##");

  private EqkRupForecastAPI eqkRupForecast;
  private EvenlyGriddedGeographicRegionAPI region;

  private double gridSpacing;

  /**
   * default class Constructor.
   * This is equivalent Poission Annual Rate.
   */
  public ERF2GriddedSeisRatesCalc() {}

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
      EqkRupForecastAPI eqkRupForecast,
      EvenlyGriddedGeographicRegionAPI region) {
    magnitude = mag;

    this.eqkRupForecast = eqkRupForecast;
    this.region = region;
    gridSpacing = region.getGridSpacing();
    //Initializing the Region Mag-Rate List with empty Empirical DistFunc.
    regionMagRatesEmpDistList = new HashMap();
    //computing the rates for magnitudes for each location on rupture in the ERF.
    calcSeisRatesForGriddedRegion();

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
        System.out.println(loc);
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
        locsMagRateDist.add(new DataObject2D(loc, magRateFunction));
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
      EqkRupForecastAPI eqkRupForecast,
      EvenlyGriddedGeographicRegionAPI region) {
    magnitude = mag;

    this.eqkRupForecast = eqkRupForecast;
    this.region = region;
    gridSpacing = region.getGridSpacing();
    //Initializing the Region Mag-Rate List with empty Empirical DistFunc.
    regionMagRatesEmpDistList = new HashMap();
    //computing the rates for magnitudes for each location on rupture in the ERF.
    calcTotalSeisRatesForGriddedRegion();
    ArrayList locTotalSiesRatesList = new ArrayList();
    Set set = regionMagRatesEmpDistList.keySet();
    Iterator it = set.iterator();
    while (it.hasNext()) {
      Location loc = (Location) it.next();
      Double totalRate = (Double) regionMagRatesEmpDistList.get(loc);
      locTotalSiesRatesList.add(new DataObject2D(loc, totalRate));
    }
    return locTotalSiesRatesList;
  }

  /**
   * This function returns the total Rate above a given magnitude ,
   * for the given geographic region.
   * Calcuated Rates depend on the ERF model instantiated by the user.
   * @param magnitude double  : A magnitude above which rate needs to be returned
   * @param eqkRupForecast Earthquake Rupture Forecast Model
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */

  public double getTotalSeisRateInRegion(double magnitude,
                                         EqkRupForecastAPI eqkRupForecast,
                                         GeographicRegion region) {
    int numSources = eqkRupForecast.getNumSources();

    double totalRate = 0;
    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
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
                         eqkRupForecast.getTimeSpan().getDuration()) / numPts;

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
      EqkRupForecastAPI eqkRupForecast,
      GeographicRegion region) {
    ArbDiscrEmpiricalDistFunc magRateDist = new ArbDiscrEmpiricalDistFunc();
    int numSources = eqkRupForecast.getNumSources();
    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
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
        double ptRate = getRupturePtRate(eqkRupForecast, rupture, numPts);

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
  private double getRupturePtRate(EqkRupForecastAPI eqkRupForecast,
                                  ProbEqkRupture rupture, long numPts) {
    return ( -Math.log(1 - rupture.getProbability()) /
            eqkRupForecast.getTimeSpan().getDuration()) / numPts;
  }

  /*
   * computes the total seis rate for each location in the region
   */
  private void calcTotalSeisRatesForGriddedRegion() {

    int numSources = eqkRupForecast.getNumSources();

    int numLocations = region.getNumGridLocs();
    for (int i = 0; i < numLocations; ++i)
      regionMagRatesEmpDistList.put(region.getGridLocation(i),
                                    new Double(0.0));

    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
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
        double ptRate = getRupturePtRate(eqkRupForecast, rupture, numPts);

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();
        //looping over all the rupture pt location and finding the nearest location
        //to them in the Geographical Region.
        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          //discard the pt location on the rupture if outside the region polygon
          if (! ( (GeographicRegion) region).isLocationInside(ptLoc))
            continue;
          Location loc = getNearestLocationForGriddedRegion(ptLoc);
          double rate = ( (Double) regionMagRatesEmpDistList.get(loc)).
              doubleValue();
          rate += ptRate;
          regionMagRatesEmpDistList.put(loc, new Double(rate));
        }
      }
    }
  }

  /*
   * computes the Mag-Rate distribution for each location within the provided region.
   */
  private void calcSeisRatesForGriddedRegion() {

    int numSources = eqkRupForecast.getNumSources();

    int numLocations = region.getNumGridLocs();
    for (int i = 0; i < numLocations; ++i)
      regionMagRatesEmpDistList.put(region.getGridLocation(i),
                                    new ArbDiscrEmpiricalDistFunc());

    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
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
        double ptRate = getRupturePtRate(eqkRupForecast, rupture, numPts);

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();
        //looping over all the rupture pt location and finding the nearest location
        //to them in the Geographical Region.
        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          //discard the pt location on the rupture if outside the region polygon
          if (! ( (GeographicRegion) region).isLocationInside(ptLoc))
            continue;
          Location loc = getNearestLocationForGriddedRegion(ptLoc);
          createMagRateEmpDist(mag, ptRate, loc);
        }
      }
    }
  }

  /*
   * This function , for a given location index in the region adds data to Mag-Rate
   * Empirilcal Dist. at that index.
   * @param mag double : Magnitude
   * @param ptRate double : Rate for a given location on the Eqk rupture
   * @param loc location of the point on rupture mapped to nearest location
   * in Gridded Region.
   */
  private void createMagRateEmpDist(double mag, double ptRate, Location loc) {

    ArbDiscrEmpiricalDistFunc magRateDist = (ArbDiscrEmpiricalDistFunc)
        regionMagRatesEmpDistList.get(loc);
    String magnitude = magFormat.format(mag);
    magRateDist.set(Double.parseDouble(magnitude), ptRate);
  }

  /*
   * This function returns the nearest location from the EvenlyGridded Geographic Region for
   * point on the rupture.
   * It compares each location on the rupture with the location in the EvenlyGridded
   * Geographic region,to get the nearest location.
   * The comparison is done by calculation of distance between the 2 locations.
   * @param rupPointLoc Location Rupture Pt Location
   * @return Location  Nearest location in the Gridded Region to the rupture
   * point location.
   */
  private Location getNearestLocationForGriddedRegion(Location rupPointLoc) {

    //Getting the nearest Location to the rupture point location
    double lat = Math.rint(rupPointLoc.getLatitude() / gridSpacing) *
        gridSpacing;
    double lon = Math.rint(rupPointLoc.getLongitude() / gridSpacing) *
        gridSpacing;
    lat = Double.parseDouble(latLonFormat.format(lat));
    lon = Double.parseDouble(latLonFormat.format(lon));
    return new Location(lat, lon);
  }

  /**
   *
   * @param args String[]
   */
  public static void main(String[] args) {
    ERF2GriddedSeisRatesCalc erf2griddedseisratescalc = new
        ERF2GriddedSeisRatesCalc();

    Frankel02_AdjustableEqkRupForecast frankelForecast = null;

    //try {
    frankelForecast = new
        Frankel02_AdjustableEqkRupForecast();
    //}
    //catch (RemoteException ex) {
    // ex.printStackTrace();
    //}

    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.
                                 BACK_SEIS_INCLUDE);
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME).
        setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_FINITE);
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(10.0));
    frankelForecast.getTimeSpan().setDuration(50.0);
    frankelForecast.updateForecast();

    try {
      EvenlyGriddedRectangularGeographicRegion region =
          new EvenlyGriddedRectangularGeographicRegion(33, 38.3, -120,
          -115, 0.1);
      /*HashMap map = erf2griddedseisratescalc.getRatesAbove(5.0, frankelForecast,
          region);
             Set set = map.keySet();
             Iterator it = set.iterator();
             try {
        FileWriter fw = new FileWriter("magRates.txt");
        while (it.hasNext()) {
          Location loc = (Location) it.next();
          Double rate = (Double) map.get(loc);
          fw.write(loc.toString() + "\t" + rate.doubleValue() + "" + "\n");
        }
        fw.close();
             }
             catch (IOException ex2) {
             }*/
      /*ArbDiscrEmpiricalDistFunc func = erf2griddedseisratescalc.getMagRateDistForRegion(5.0, frankelForecast,
              region);
         try {
              FileWriter fw = new FileWriter("magRates.txt");
                fw.write(func.toString()+ "\n");
              fw.close();
         } catch (IOException ex2) {
          }*/
    }
    catch (RegionConstraintException ex1) {
    }
  }
}
