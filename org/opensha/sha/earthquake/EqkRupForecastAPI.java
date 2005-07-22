package org.opensha.sha.earthquake;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.ListIterator;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;

/**
 * <b>Title:</b> EqkRupForecast<br>
 * <b>Description: Earthquake Rupture Forecast</b> <br>
 *
 * @author Nitin Gupta & Vipin Gupta
 * @date Aug 27, 2002
 * @version 1.0
 */

public interface EqkRupForecastAPI extends ERF_API{

     /**
      *
      * @returns the total number os sources
      */
     public int getNumSources();

     /**
      *
      * @returns the sourceList
      */
     public ArrayList getSourceList();

     /**
      * Return the earhthquake source at index i.   Note that this returns a
      * pointer to the source held internally, so that if any parameters
      * are changed, and this method is called again, the source obtained
      * by any previous call to this method will no longer be valid.
      *
      * @param iSource : index of the desired source (only "0" allowed here).
      *
      * @return Returns the ProbEqkSource at index i
      *
      */
     public ProbEqkSource getSource(int iSource);


     /**
      *
      * @param iSource
      * @returns the number of ruptures for the ithSource
      */
     public int getNumRuptures(int iSource);



     /**
      *
      * @param iSource
      * @param nRupture
      * @returns the ProbEqkRupture object for the ithSource and nth rupture
      */
     public ProbEqkRupture getRupture(int iSource,int nRupture);

     /**
      * This function returns the total Rate above a given magnitude ,
      * for the given geographic region.
      * Calcuated Rates depend on the ERF model instantiated by the user.
      * @param magnitude double  : Amgnitude above which rate needs to be returned
      *
      * @param region GeographicRegion : Region whose rates need to be returned
      * @return double : Total Rate for the region
      */
     public double getTotalRateAbove(double magnitude, GeographicRegion region);

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
         EvenlyGriddedGeographicRegionAPI region);

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
         EvenlyGriddedGeographicRegionAPI region);


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
         GeographicRegion region);

}






