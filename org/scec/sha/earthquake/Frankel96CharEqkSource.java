package org.scec.sha.earthquake;

import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.data.TimeSpan;

/**
 * <p>Title: Frankel96CharEqkSource</p>
 * <p>Description: Frankel 1996 Characteristic type A earthquake sources </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author nitin Gupta & Vipin Gupta
 * @date Sep 2, 2002
 * @version 1.0
 */

public class Frankel96CharEqkSource extends ProbEqkSource {


  // rate for this source.
  // We need rate to set the probability when we come come to know about the timeSpan
  private double rate;





  /**
   * Constructor for this class
   *
   * @param rake : ave rake of the surface
   * @param mag  : Magnitude of the earthquake
   * @param rate : Rate at this mag
   * @param surface : Fault Surface
   */
  public Frankel96CharEqkSource(double rake,
                                double mag,
                                double rate,
                                EvenlyGriddedSurface surface) {

      probEqkRupture = new ProbEqkRupture();
      probEqkRupture.setAveRake(rake);
      probEqkRupture.setMag(mag);
      probEqkRupture.setRuptureSurface(surface);
  }



  /** Set the time span in years
   *
   * @param yrs : timeSpan as specified in  Number of years
   */
  public void setTimeSpan(double yrs) {
    // set the probability according to the specifed timespan
     probEqkRupture.setProbability(1-Math.exp(-yrs*rate));
  }



  /** Set the time span in years
   * FIX Mehthod not implemented yet
   *
   * @param yrs : timeSpan
   *
   */
  public void setTimeSpan(TimeSpan timeSpan) {

     // set the probability according to the specifed timespan

  }



}