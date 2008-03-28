package org.opensha.sha.earthquake.rupForecastImpl.Frankel96;

import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.earthquake.*;
import org.opensha.data.*;
import org.opensha.calc.RelativeLocation;

import java.util.ArrayList;

/**
 * <p>Title: Frankel96CharEqkSource</p>
 * <p>Description: Frankel 1996 Characteristic type A earthquake sources </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Sep 2, 2002
 * @version 1.0
 */

public class Frankel96_CharEqkSource extends ProbEqkSource {


  // rate for this source.
  // We need rate to set the probability when we come to know about the timeSpan
  private double rate;

  /**
   * Name of this class
   */
  private static final String C = new String("Frankel96_CharEqkSource");

  private boolean D = false;
  private EvenlyGriddedSurface surface;


  /**
   * Constructor for this class
   *
   * @param rake : ave rake of the surface
   * @param mag  : Magnitude of the earthquake
   * @param rate : Rate (events/yr) at this mag
   * @param surface : Fault Surface
   */
  public Frankel96_CharEqkSource(double rake,
                                double mag,
                                double rate,
                                EvenlyGriddedSurface surface,
                                String faultName) {

      this.rate = rate;
      this.surface = surface;
      probEqkRupture = new ProbEqkRupture();
      this.rate  = rate;
      probEqkRupture.setAveRake(rake);
      probEqkRupture.setMag(mag);
      probEqkRupture.setRuptureSurface(surface);
      this.name = faultName+" Char";
  }

  /**
  * It returns a list of all the locations which make up the surface for this
  * source.
  *
  * @return LocationList - List of all the locations which constitute the surface
  * of this source
  */
  public LocationList getAllSourceLocs() {
    return this.surface.getLocationList();
  }
  
  public EvenlyGriddedSurfaceAPI getSourceSurface() { return this.surface; }




  /** Set the time span in years
   *
   * @param yrs : timeSpan as specified in  Number of years
   */
  public void setTimeSpan(double yrs) {
    // set the probability according to the specifed timespan
     probEqkRupture.setProbability(1-Math.exp(-yrs*rate));
     if(D) System.out.println("probability="+probEqkRupture.getProbability());
  }



 /**
  * @return the total num of rutures for the mag which is 1 for the char type fault
  */
  public int getNumRuptures() {
   return 1;
 }

 /**
  * @param nRupture
  * @return the object for the ProbEqkRupture
  */
  public ProbEqkRupture getRupture(int nRupture){
    if(nRupture!=0)
      throw new RuntimeException(name+":getRupture():: Char type faults have only"+
                            "1 rupture nRupture should be equal to 0");
    return probEqkRupture;
  }


  /**
   * This returns the shortest dist to either end of the fault trace, or to the
   * mid point of the fault trace.
   * @param site
   * @return minimum distance
   */
   public double getMinDistance(Site site) {

      double min;
      EvenlyGriddedSurface surface = (EvenlyGriddedSurface) probEqkRupture.getRuptureSurface();

      // get first location on fault trace
      Direction dir = RelativeLocation.getDirection(site.getLocation(),(Location) surface.get(0,0));
      min = dir.getHorzDistance();

      // get last location on fault trace
      dir = RelativeLocation.getDirection(site.getLocation(), (Location) surface.get(0,surface.getNumCols()-1));
      if (min > dir.getHorzDistance())
          min = dir.getHorzDistance();

      // get mid location on fault trace
      dir = RelativeLocation.getDirection(site.getLocation(), (Location) surface.get(0,(int) surface.getNumCols()/2));
      if (min > dir.getHorzDistance())
          min = dir.getHorzDistance();

      return min;
    }

  /**
    * get the name of this class
    *
    * @return
    */
   public String getName() {
     return name;
  }

}
