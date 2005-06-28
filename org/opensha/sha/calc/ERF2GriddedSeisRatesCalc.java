package org.opensha.sha.calc;

import java.util.*;

import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.data.region.*;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.data.Location;

/**
 * <p>Title: ERF2GriddedSeisRatesCalc</p>
 *
 * <p>Description: This class calculates the rates of the Ekq Rupture for the given magnitude.
 * </p>
 * @author Edward (Ned) Field, Nitin Gupta
 * @version 1.0
 */
public class ERF2GriddedSeisRatesCalc {


  //gets the min/max lat/lon from the gridded region
  private double minLat,maxLat, minLon,maxLon, gridSpacing;

  //Keep track of how many rows and cols are there in the gridded region.
  private int numCols, numRows;

  private double magnitude;

  /**
   * This is equivalent Poission Annual Rate.
   * @param eqkForecast EqkRupForecastAPI
   * @param region EvenlyGriddedGeographicRegionAPI
   */
  public ERF2GriddedSeisRatesCalc() {}


  public ArrayList getRatesAbove(double mag){
    magnitude = mag;
    return null;
  }

  /**
   *
   * @param eqkRupForecast EqkRupForecastAPI
   * @param region EvenlyGriddedGeographicRegionAPI
   */
  private void calcSeisRatesForGriddedRegion(EqkRupForecastAPI eqkRupForecast,
                                             EvenlyGriddedGeographicRegionAPI region){

    //gets the mi minimum Lat and Lon for the gridded region
    minLat = ((GeographicRegion)region).getMinLat();
    maxLat = ((GeographicRegion)region).getMaxLat();
    minLon = ((GeographicRegion)region).getMinLon();
    maxLon = ((GeographicRegion)region).getMaxLon();
    gridSpacing = region.getGridSpacing();
    //gets the rows and cols for the gridded region, considering if it would have
    //stored as a Container2D object.
    numCols = (int)((maxLon-minLon)/gridSpacing);
    numRows = (int)((maxLat-minLat)/gridSpacing);


    int numLocs = region.getNumGridLocs();
    for(int i=0;i<numLocs;++i){

    }

    int numSources = eqkRupForecast.getNumSources();

    //Going over each and every source in the forecast
    for(int sourceIndex=0;sourceIndex < numSources;++sourceIndex){
        // get the ith source
        ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
        int numRuptures = source.getNumRuptures();

        //going over all the ruptures in the source
        for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
          ProbEqkRupture rupture = source.getRupture(rupIndex);

          //if rupture magnitude is less then given magnitude then skip those ruptures
          if(rupture.getMag() < magnitude)
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
          while(it.hasNext()){
            Location ptLoc = (Location)it.next();
            int regionIndex = getLocationIndex(ptLoc);
          }
        }
      }

    }


    /**
     *
     * @param rupPointLoc Location
     * @return int
     */
    private int getLocationIndex(Location rupPointLoc){

    double rupPtLat = rupPointLoc.getLatitude();
    double rupPtLon = rupPointLoc.getLongitude();

    //getting the
    int row = (int)((rupPtLat - minLat)/gridSpacing) ;
    int col = (int)((rupPtLon - minLon)/gridSpacing) ;

    int locIndex = 0;
    if(col !=0)
      locIndex = row *(numCols) +col;
    else
      locIndex = row *(numCols) ;

    return locIndex;
  }

  public static void main(String[] args) {
    ERF2GriddedSeisRatesCalc erf2griddedseisratescalc = new
        ERF2GriddedSeisRatesCalc();
  }
}
