package org.opensha.sha.earthquake;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.ListIterator;
import org.opensha.data.region.GeographicRegion;

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

}






