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


/**
 * <p>Title: ERF2GriddedSeisRatesCalc</p>
 *
 * <p>Description: This class calculates the rates of the Ekq Rupture for the given magnitude.
 * </p>
 * @author Edward (Ned) Field, Nitin Gupta
 * @version 1.0
 */
public class ERF2GriddedSeisRatesCalc {



  //Keep track of how many rows and cols are there in the gridded region.
  private int numCols, numRows;

  private double magnitude;

  //List for storing Mag-Rates Empirical Distributions for all the rupture locations.
  private ArrayList regionMagRatesEmpDistList;

  private DecimalFormat magFormat = new DecimalFormat("0.00");

  private EqkRupForecastAPI eqkRupForecast;
  private EvenlyGriddedGeographicRegionAPI region;

  /**
   * default class Constructor.
   * This is equivalent Poission Annual Rate.
   */
  public ERF2GriddedSeisRatesCalc() {}


  /**
   * This function computes the rates above the given Magnitude for each rupture
   * location. Once computed , magnitude and rates are stored empirically , using
   * the object ArbDiscrEmpiricalDistFunc, for each location on all ruptures in
   * Eqk Rupture forecast model.
   * Once all Mag-Rate distribution has been computed for each location within the
   * ERF, this function returns the HashMap of locations as the Keys and Mag-Rate
   * Empirical Distribution as the Values.
   * @param mag double
   * @return HashMap with ruptures location being the keys and Mag-Rate distribution
   * being the values.
   */
  public HashMap getRatesAbove(double mag,EqkRupForecastAPI eqkRupForecast,
                                  EvenlyGriddedGeographicRegionAPI region){
    magnitude = mag;

    this.eqkRupForecast = eqkRupForecast;
    this.region = region;

    //computing the rates for magnitudes for each location on rupture in the ERF.
    calcSeisRatesForGriddedRegion();

    //creating the Hashmap
    HashMap map = new HashMap();
    int size = regionMagRatesEmpDistList.size();

    //iterates over all the ArbDiscrEmpiricalDistFunc ( for each location in the region),
    //and adds those distribution that store mag-rate distriution.
    for(int i=0;i<size;++i){
      int numEmpDistElemnents = ((ArbDiscrEmpiricalDistFunc)regionMagRatesEmpDistList.get(i)).getNum();

      if(numEmpDistElemnents ==0)continue;
      else{
        ArbitrarilyDiscretizedFunc func = ((ArbDiscrEmpiricalDistFunc)regionMagRatesEmpDistList.get(i)).getCumDist();
        int numFuncs = func.getNum();
        ArbitrarilyDiscretizedFunc magRateFunction = new ArbitrarilyDiscretizedFunc();
        int magIndex = func.getYIndex(mag);
        for(int j=magIndex;j<numFuncs;++j,++magIndex){
          double rates = func.getY(func.getNum()-1) - func.getY(magIndex) ;
          magRateFunction.set(func.getX(magIndex),rates);
        }

        map.put(region.getGridLocation(i),magRateFunction);
      }
    }

    return map;
  }

  /**
   *
   * @param eqkRupForecast EqkRupForecastAPI
   * @param region EvenlyGriddedGeographicRegionAPI
   */
  private void calcSeisRatesForGriddedRegion(){


    int numSources = eqkRupForecast.getNumSources();

    //Initializing the Region Mag-Rate List with empty Empirical DistFunc.
    regionMagRatesEmpDistList = new ArrayList();
    int numLocations = region.getNumGridLocs();
    for (int i = 0; i < numLocations; ++i)
      regionMagRatesEmpDistList.add(new ArbDiscrEmpiricalDistFunc());


    //gets the mi minimum Lat and Lon for the gridded region
    double minLat = ( (EvenlyGriddedRectangularGeographicRegion) region).
        getMinLat();
    double maxLat = ( (EvenlyGriddedRectangularGeographicRegion) region).
        getMaxLat();
    double minLon = ( (EvenlyGriddedRectangularGeographicRegion) region).
        getMinLon();
    double maxLon = ( (EvenlyGriddedRectangularGeographicRegion) region).
        getMaxLon();
    double gridSpacing = region.getGridSpacing();


    //Going over each and every source in the forecast
    for(int sourceIndex=0;sourceIndex < numSources;++sourceIndex){
        // get the ith source
        ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
        int numRuptures = source.getNumRuptures();

        //going over all the ruptures in the source
        for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
          ProbEqkRupture rupture = source.getRupture(rupIndex);

          double mag = rupture.getMag();
          //if rupture magnitude is less then given magnitude then skip those ruptures
          if(mag < magnitude)
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

         //going over all the locations in the ruptures and mapping those to nearest
         //location on the gridded region
         if (region instanceof EvenlyGriddedRectangularGeographicRegion) {

           //gets the rows and cols for the gridded region, considering if it would have
           //stored as a Container2D object.
           numCols = (int) ( (maxLon - minLon) / gridSpacing);
           numRows = (int) ( (maxLat - minLat) / gridSpacing);

           while (it.hasNext()) {
             Location ptLoc = (Location) it.next();
             int regionIndex = getLocationIndexForRectangularRegion(ptLoc, minLat, maxLat,
                 minLon, maxLon, gridSpacing);
             createMagRateEmpDist(mag,ptRate, regionIndex);
           }
         }
         else if(region instanceof EvenlyGriddedGeographicRegion){

         }
        }
      }


    }


    /**
     *
     */
    private void getMagFreqDist(){

    }

    /**
     * This function , for a given location index in the region adds data to Mag-Rate
     * Empirilcal Dist. at that index.
     * @param mag double : Magnitude
     * @param ptRate double : Rate for a given location on the Eqk rupture
     * @param regionIndex int
     */
    private void createMagRateEmpDist(double mag,double ptRate,int regionIndex){

      ArbDiscrEmpiricalDistFunc magRateDist = (ArbDiscrEmpiricalDistFunc)regionMagRatesEmpDistList.get(regionIndex);
      String magnitude = magFormat.format(mag);
      magRateDist.set(Double.parseDouble(magnitude),ptRate);
    }


    /**
     * This function returns the nearest location from the EvenlyGridded Geographic Region for
     * point on the rupture.
     * It compares each location on the rupture with the location in the EvenlyGridded
     * Geographic region,to get the nearest location.
     * The comparison is done by calculation of distance between the 2 locations.
     * @param rupPointLoc Location
     * @return int
     */
    private int getLocationIndex(Location rupPointLoc){


      return 0;
    }

    /**
     *
     * @param rupPointLoc Location
     * @return int
     */
    private int getLocationIndexForRectangularRegion(Location rupPointLoc, double minLat,
        double maxLat,double minLon,double maxLon, double gridSpacing) {

      double rupPtLat = rupPointLoc.getLatitude();
      double rupPtLon = rupPointLoc.getLongitude();

      //getting the
      int row = (int) ( (rupPtLat - minLat) / gridSpacing);
      int col = (int) ( (rupPtLon - minLon) / gridSpacing);

      int locIndex = 0;
      if (col != 0)
        locIndex = row * (numCols) + col;
      else
        locIndex = row * (numCols);

      return locIndex;
    }

    /**
     *
     * @param args String[]
     */
    public static void main(String[] args) {
      ERF2GriddedSeisRatesCalc erf2griddedseisratescalc = new
          ERF2GriddedSeisRatesCalc();
    }
}
