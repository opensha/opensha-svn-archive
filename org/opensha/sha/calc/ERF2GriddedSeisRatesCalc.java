package org.opensha.sha.calc;

import java.util.*;
import java.text.*;

import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.data.region.*;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
import org.opensha.data.Location;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.exceptions.*;
import java.io.*;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.
    Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;


/**
 * <p>Title: ERF2GriddedSeisRatesCalc</p>
 *
 * <p>Description: This class calculates the rates of the Ekq Rupture above a
 * given magnitude.
 * </p>
 * <p>
 * This class provides the functionality of computing Mag-Rate distribution or
 * total seismic rates using the Earthquake Ruptures from the provided Earthquake
 * Rupture Forecast with a given Geographic Region.
 * </p>
 * <p>
 * This class has not been fully tested to see if the rates for each location that
 * we are getting is correct. This class has a main method that will iterate over all
 * the ruptures in the Frankel-2002 model and maps these locations on the ruptures
 * to the nearest location in the region. It includes the Frankel background model
 * for computation. This main generates a file "magRates.txt" in sha project
 * home directory, which contains each location in the region along with the
 * total rate at that location. One way of testing we discussed was to test it
 * with a main method in the Frankel-02 ERF , but then that will utilizing this
 * method too.
 * </p>
 * <p>
 * As this model includes the background so user will need to increase the memory
 * by specifying the in the Runtime configuration as "-Xmx500M", or turn-off the
 * background.
 * </p>
 * @author Nitin Gupta , Vipin Gupta
 * @version 1.0
 */
public class ERF2GriddedSeisRatesCalc {

  //Magnitude above which rates need to be calculated
  private double minMagnitude;


  private DecimalFormat magFormat = new DecimalFormat("0.00");

  private EqkRupForecastAPI eqkRupForecast;
  private EvenlyGriddedGeographicRegionAPI region;

  /**
   * default class Constructor.
   * This is equivalent Poission Annual Rate.
   */
  public ERF2GriddedSeisRatesCalc() {}


  /**
   * This class creates a IncrementalMagFreqDist for each location in the provided
   * region. Each element of the arraylist contains a IncrementalMagFreqDist object
   * and the number of elements in the arraylist are equal to number of locations
   * in the gridded region. This function was tested for Frankel02 ERF.
   * Following testing procedure was applied (Region used was RELM Gridded region and
   *  min mag=5, max Mag=9, Num mags=41):
   * 1. Choose an arbitrary location say 31.5, -117.2
   * 2. Make Frankel02 ERF with Background only sources
   * 3. Modify Frankel02 ERF for testing purposes to use ONLY CAmapC_OpenSHA input file
   * 4. Now print the Magnitude Frequency distribution in Frankel02 ERF for that location
   * 5. Using the same ERF settings, get the Magnitude Frequency distribution using
   * this function and it should be same as we printed out in ERF.
   * 6. In another test, we printed out cum dist above Mag 5.0 for All locations.
   * The cum dist from Frankel02 ERF and from MFD retured from this function should
   * be same.
   *
   *
   *
   * @param minMag - Center of first mag bin
   * @param maxMag - center of last mag bin
   * @param numMagBins
   * @param eqkRupForecast
   * @param region - region for which the rates need to be calculated
   * @return
   */
  public ArrayList calcMFD_ForGriddedRegion(double minMag, double maxMag,
                                            int numMagBins, EqkRupForecastAPI eqkRupForecast,
                                            EvenlyGriddedGeographicRegionAPI region) {
    double delta = (maxMag-minMag)/(numMagBins-1);
    ArrayList cumMFDList  = calcCumMFD_ForGriddedRegion(minMag-delta, eqkRupForecast, region);
    // number of locations in this region
    int numLocs = cumMFDList.size();
    // list to save the mag freq dist for each location
    ArrayList mfdList = new ArrayList();
    double cumRate2, cumRate1, mag;
    for(int i=0; i<numLocs; ++i) {
      // incremental mag freq dist for each location
      IncrementalMagFreqDist magFreqDist = new IncrementalMagFreqDist(minMag, maxMag, numMagBins);
      mfdList.add(magFreqDist);
      ArbitrarilyDiscretizedFunc cumFunc = (ArbitrarilyDiscretizedFunc)cumMFDList.get(i);
      // if(i==0) System.out.println(" CUM FUNC::::\n"+cumFunc.toString());
      if(cumFunc==null) continue;
      for(int j=0; j<magFreqDist.getNum(); ++j ) {
        mag = magFreqDist.getX(j);
        // get interpolated rate
        cumRate1 = getRateForMag(cumFunc, mag - delta/2);
        // get interpolated rate for the mag
        cumRate2 = getRateForMag(cumFunc, mag + delta/2);
        // set the rate in Incremental Mag Freq Dist
        magFreqDist.set(mag, cumRate1-cumRate2);
      }
      //if(i==0) System.out.println("Mag Freq Dist::::\n"+magFreqDist.getMetadataString());
      
    }
    
    
    /* test:
    ArbitrarilyDiscretizedFunc cumFunc = (ArbitrarilyDiscretizedFunc)cumMFDList.get(500);
    System.out.println(cumFunc.toDebugString());
    IncrementalMagFreqDist magFreqDist = (IncrementalMagFreqDist) mfdList.get(500);
    System.out.println(magFreqDist.toString());
*/

    return mfdList;
  }

  /**
   * Get the rate based on mag
   *
   * @param func
   * @param mag
   * @return
   */
  private double getRateForMag(ArbitrarilyDiscretizedFunc func, double mag) {
    if(mag<func.getMinX()) return func.getY(0);
    if(mag>func.getMaxX()) return 0.0;
    return func.getInterpolatedY(mag);
  }

  /**
   * This function computes the rates above the given Magnitude for each rupture
   * location. Once computed , magnitude-rate distribution is stored for each
   * location on all ruptures in Eqk Rupture forecast model, if that lies within the
   * provided EvenlyGriddedGeographicRegion.
   * Once all Mag-Rate distribution has been computed for each location within the
   * ERF, this function returns ArrayList that constitutes of
   * ArbitrarilyDiscretizedFunc object. This ArbitrarilyDiscretizedFunc for each location
   * is the Mag-Rate distribution with X values being Mag and Y values being Rate.
   * @param minMag double : Magnitude above which Mag-Rate distribution is to be computed.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Ruptureforecast model
   * @param region EvenlyGriddedGeographicRegionAPI Region within which ruptures
   * are to be considered.
   * @return ArrayList with values being ArbitrarilyDiscretizedFunc
   * @see ArbitrarilyDiscretizedFunc, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion
   *
   * Note : We will have to think of returning the actual Mag-Freq dist. , which
   * can done, but just have to get the user input for discretization.
   */
  public ArrayList calcCumMFD_ForGriddedRegion(double minMag,
      EqkRupForecastAPI eqkRupForecast,
      EvenlyGriddedGeographicRegionAPI region) {
    minMagnitude = minMag;

    this.eqkRupForecast = eqkRupForecast;
    this.region = region;

    //Initializing the Region Mag-Rate List with empty Empirical DistFunc.

    //computing the rates for magnitudes for each location on rupture in the ERF.
    ArbDiscrEmpiricalDistFunc[] funcs = calcSeisRatesForGriddedRegion();
    //System.out.println("Arb Emp Func:::\n"+funcs[0]);
    //List to store Mag-Rate dist. at each location in the gridded region
    ArrayList magRateDist = new ArrayList();
    int size = funcs.length;
    for (int i = 0; i < size; ++i) {
      //iterates over all the ArbDiscrEmpiricalDistFunc ( for each location in the region),
      //and adds those distribution that store mag-rate distriution.
      int numEmpDistElemnents = funcs[i].getNum();

      if (numEmpDistElemnents == 0) {
        //System.out.println(region.getGridLocation(i));
        //continue;
        magRateDist.add(null);
      }
      else {
    	  // get the cumulative dist of the ArbDiscrFunc (sum of values � X)
        ArbitrarilyDiscretizedFunc cumDist = funcs[i].getCumDist();
        //if(i==0) System.out.println("cum function>>>>>>\n"+func.toString());
        int numMags = cumDist.getNum();
        ArbitrarilyDiscretizedFunc magRateFunction = new ArbitrarilyDiscretizedFunc();
        // now convert it to the cumulative mag-freq-dist (sum of values � each mag)
        magRateFunction.set(cumDist.getX(0), cumDist.getY(numMags- 1));
        for (int magIndex=1; magIndex < numMags;  ++magIndex) {
          double rates = cumDist.getY(numMags- 1) - cumDist.getY(magIndex-1);
          magRateFunction.set(cumDist.getX(magIndex), rates);
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
   * It returns a double[] value being total seis rate for each location in region.
   * @param minMag double : Only those ruptures above this magnitude are considered
   * for calculation of the total seis rates in the region.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Rupture forecast model
   * @param region EvenlyGriddedGeographicRegionAPI
   * @return double[] with each element in the array being totalSeisRate for each
   * location in the region.
   * @see Double, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion
   */
  public double[] getTotalSeisRateAtEachLocationInRegion(double minMag,
      EqkRupForecastAPI eqkRupForecast,
      EvenlyGriddedGeographicRegionAPI region) {
    minMagnitude = minMag;

    this.eqkRupForecast = eqkRupForecast;
    this.region = region;

    //computing the rates for magnitudes for each location on rupture in the ERF.
    double[] rates = calcTotalSeisRatesForGriddedRegion();

    return rates;
  }

  /**
   * This function returns the total Rate above a given magnitude ,
   * for the given geographic region.
   * Calcuated Rates depend on the ERF model instantiated by the user.
   * @param minMag double  : A magnitude above which rate needs to be returned
   * @param eqkRupForecast Earthquake Rupture Forecast Model
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */

  public double getTotalSeisRateInRegion(double minMag,
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
        if (mag < minMag)
          continue;

        EvenlyGriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
        long numPts = rupSurface.size();

        //getting the rate at each Point on the rupture( calculated by first
        //getting the rate of the rupture and then dividing by number of points
        //on that rupture.
        double ptRate  = this.getRupturePtRate(eqkRupForecast, rupture,
                                               numPts);

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
   * @param minMag double  Ruptures above this magnitude will be the ones that
   * will considered within the provided region  for computing the Mag-Rate Dist.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Rupture Forecast from which
   * ruptures will computed.
   * @param region GeographicRegion Region for which mag-rate distribution has to be
   * computed.
   * @return ArbDiscrEmpiricalDistFunc : Distribution function that holds X values
   * as the magnitude and Y values as the sies rate for corresponding magnitude within
   * the region.
   *
   * Note : We will have to think of returning the actual Mag-Freq dist. , which
   * can done, but just have to get the user input for discretization
   */
  public ArbDiscrEmpiricalDistFunc getMagRateDistForRegion(double minMag,
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
        if (mag < minMag)
          continue;

        EvenlyGriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
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
   * Computing the annualized rate for each location on the rupture
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
        if (mag < minMagnitude)
          continue;

        EvenlyGriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
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
          int locIndex = 0;
          //if rupture location is outside the region bounds then keep continuing
          //it returns -1 if location onn rupture is outside the regio bounds
          locIndex = region.getNearestLocationIndex(ptLoc);
          if(locIndex < 0)
            continue;
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
    for(int i=0; i<numLocations; ++i) funcs[i] = new ArbDiscrEmpiricalDistFunc();
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
        if (mag < minMagnitude)
          continue;

        EvenlyGriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
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
          int locIndex = 0;

          //returns -1 if location not in the region
          locIndex = region.getNearestLocationIndex(ptLoc);
          //continue if location not in the region
          if(locIndex < 0)
            continue;
          String magString = magFormat.format(mag);
          funcs[locIndex].set(Double.parseDouble(magString), ptRate);        }
      }
    }
    return funcs;
  }




  /**
   * This function returns the total probability of events above a given magnitude
   * within the given geographic region.  The calcuated Rates depend on the  ERF
   * subclass.  Note that it is assumed that the forecast has been updated.
   * @param minMag double  : magnitude above which rate needs to be returned
   *
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */
  public double getTotalProbAbove(EqkRupForecastAPI eqkRupForecast,
                                  double minMag, GeographicRegion region) {

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
        if (rupture.getMag() < minMag)
          continue;

        EvenlyGriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
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
        setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_POINT);
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(10.0));
    frankelForecast.getTimeSpan().setDuration(50.0);
    frankelForecast.updateForecast();

    try {
     EvenlyGriddedRELM_Region region = new EvenlyGriddedRELM_Region();
     ArbDiscrEmpiricalDistFunc func = erf2griddedseisratescalc.getMagRateDistForRegion(5.5, frankelForecast,
          region);
     SummedMagFreqDist mfd = new SummedMagFreqDist(4.0, 9.0, 101);
     mfd.addResampledMagFreqDist(func, true);
     //ArbitrarilyDiscretizedFunc func1 = func.getMagFreqBasedCumDist();
     System.out.println(mfd.getCumRateDist().toString());
            /* try {
               FileWriter fw = new FileWriter("magRates_With_BG.txt");
               for(int i=0;i<size;++i){
                 Location loc = region.getGridLocation(i);
                 //if(rates[i] !=0)
                   fw.write(loc + "\t" + (float)rates[i] + "" + "\n");
                 //else
                   //System.out.println(loc + "\t" + rates[i] + "" + "\n");
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
    }catch(Exception e){
    	e.printStackTrace();
    }
  }
}
