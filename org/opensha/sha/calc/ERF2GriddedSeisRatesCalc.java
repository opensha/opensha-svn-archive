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


  private DecimalFormat magFormat = new DecimalFormat("0.00");

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
   * ERF, this function returns Hashmap that constitutes of
   * 2 objects, with key being Location and value being
   * ArbitrarilyDiscretizedFunc. This ArbitrarilyDiscretizedFunc for each location
   * is the Mag-Rate distribution with X values being Mag and Y values being Rate.
   * @param mag double : Magnitude above which Mag-Rate distribution is to be computed.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Ruptureforecast model
   * @param region EvenlyGriddedGeographicRegionAPI Region within which ruptures
   * are to be considered.
   * @return Hashmap with key being Location and value being ArbitrarilyDiscretizedFunc
   * @see ArbitrarilyDiscretizedFunc, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion
   */
  public ArrayList getMagRateDistForEachLocationInRegion(double mag,
      EqkRupForecastAPI eqkRupForecast,
      EvenlyGriddedGeographicRegionAPI region) {
    magnitude = mag;

    this.eqkRupForecast = eqkRupForecast;
    this.region = region;

    //Initializing the Region Mag-Rate List with empty Empirical DistFunc.

    //computing the rates for magnitudes for each location on rupture in the ERF.
    ArbDiscrEmpiricalDistFunc[] funcs = calcSeisRatesForGriddedRegion();

    //List to store Mag-Rate dist. at each location in the gridded region
    ArrayList magRateDist = new ArrayList();
    int size = funcs.length;
    for (int i = 0; i < size; ++i) {
      //iterates over all the ArbDiscrEmpiricalDistFunc ( for each location in the region),
      //and adds those distribution that store mag-rate distriution.
      int numEmpDistElemnents = funcs[i].getNum();

      if (numEmpDistElemnents == 0) {
        System.out.println(region.getGridLocation(i));
        continue;
      }
      else {
        ArbitrarilyDiscretizedFunc func = funcs[i].getCumDist();

        int numFuncs = func.getNum();
        ArbitrarilyDiscretizedFunc magRateFunction = new
            ArbitrarilyDiscretizedFunc();
        int magIndex = 0;
        for (int j = magIndex; j < numFuncs; ++j, ++magIndex) {
          double rates = func.getY(func.getNum() - 1) - func.getY(magIndex);
          magRateFunction.set(func.getX(magIndex), rates);
        }
        //putting the Mag-Rate distribution for each location in the gridded region.
        magRateDist.add(magRateFunction);
      }
    }

    return magRateDist;
  }

  /**
   * This function computes the total SiesRate for each location on all the ruptures,
   * if they are within the provided Geographical Region.
   * It returns a HashMap with key being location and value being
   * total seis rate(Double Object) .
   * @param mag double : Only those ruptures above this magnitude are considered
   * for calculation of the total seis rates in the region.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Rupture forecast model
   * @param region EvenlyGriddedGeographicRegionAPI
   * @return Hashmap with key being Location and value being Double Object
   * respresenating the TotalSeisRate.
   * @see Double, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion
   */
  public double[] getTotalSeisRateAtEachLocationInRegion(double mag,
      EqkRupForecastAPI eqkRupForecast,
      EvenlyGriddedGeographicRegionAPI region) {
    magnitude = mag;

    this.eqkRupForecast = eqkRupForecast;
    this.region = region;
    gridSpacing = region.getGridSpacing();

    //computing the rates for magnitudes for each location on rupture in the ERF.
    double[] rates = calcTotalSeisRatesForGriddedRegion();

    return rates;
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
          totalRate += ptRate;
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
  private double[] calcTotalSeisRatesForGriddedRegion() {

    int numSources = eqkRupForecast.getNumSources();

    int numLocations = region.getNumGridLocs();

    double[] rates = new double[numLocations];
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
          int  locIndex = region.getNearestLocationIndex(ptLoc);
          rates[locIndex] += ptRate;
        }
      }
    }
    return rates;
  }

  /*
   * computes the Mag-Rate distribution for each location within the provided region.
   */
  private ArbDiscrEmpiricalDistFunc[] calcSeisRatesForGriddedRegion() {

    int numSources = eqkRupForecast.getNumSources();

    int numLocations = region.getNumGridLocs();
    ArbDiscrEmpiricalDistFunc[] funcs = new ArbDiscrEmpiricalDistFunc[numLocations];

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
          int locIndex = region.getNearestLocationIndex(ptLoc);
          String magString = magFormat.format(magnitude);
          funcs[locIndex].set(Double.parseDouble(magString), ptRate);        }
      }
    }
    return funcs;
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
  public double getTotalProbAbove(EqkRupForecastAPI eqkRupForecast,
                                  double magnitude, GeographicRegion region) {

    int numSources = eqkRupForecast.getNumSources();
    int numRuptures;
    double totalProb = 1.0; // intialize as probability of none
    double srcProb;

    //Going over every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
      numRuptures = source.getNumRuptures();
      //if Source is Poission
      boolean isSourcePoission = source.isPoissonianSource();
      // initialize the source probability
      if (isSourcePoission)
        srcProb = 1; // initial probability of no events
      else
        srcProb = 0; // initial probability of an event

      //going over all the ruptures in the source
      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
        ProbEqkRupture rupture = source.getRupture(rupIndex);

        //if rupture magnitude is less then given magnitude then skip those ruptures
        if (rupture.getMag() < magnitude)
          continue;

        GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
        double ptProb = rupture.getProbability() / rupSurface.size();

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();

        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          // jump out if not inside region
          if (!region.isLocationInside(ptLoc))
            continue;

          if (isSourcePoission)
            srcProb *= (1 - ptProb); // the probability of none
          else
            srcProb += ptProb; // the probability of an event
        }
      }
      // convert back to prob of one or more if Poissonian
      if (isSourcePoission)
        srcProb = 1.0 - srcProb;
      totalProb *= (1.0 - srcProb); // the total probability of none
    }
    return 1 - totalProb; // return the probability of one or more
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
          new EvenlyGriddedRectangularGeographicRegion(33.00, 38.30, -123.00,
          -115.00, 0.1);
      double[] rates = erf2griddedseisratescalc.getTotalSeisRateAtEachLocationInRegion(5.0, frankelForecast,
          region);
             int size = rates.length;
             try {
               FileWriter fw = new FileWriter("magRates.txt");
               for(int i=0;i<size;++i){
                 Location loc = region.getGridLocation(i);
                 if(rates[i] !=0)
                   fw.write(loc + "\t" + rates[i] + "" + "\n");
                 else
                   System.out.println(loc + "\t" + rates[i] + "" + "\n");
               }
               fw.close();
             }
             catch (IOException ex2) {
             }
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
