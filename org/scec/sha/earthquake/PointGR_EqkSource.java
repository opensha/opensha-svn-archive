package org.scec.sha.earthquake;

import org.scec.sha.magdist.GutenbergRichterMagFreqDist;
import org.scec.data.*;
import org.scec.calc.RelativeLocation;

import java.util.Vector;
import java.util.Iterator;

/**
 * <p>Title: PointGR_EqkSource </p>
 * <p>Description: </p>
 *
 * @author Edward Field
 * @date Sep 2, 2002
 * @version 1.0
 */

public class PointGR_EqkSource extends ProbEqkSource {


  //for Debug purposes
  private static String  C = new String("PointGR_EqkSource");
  private boolean D = false;

  private GutenbergRichterMagFreqDist gR;
  private double timeSpan = Double.NaN;
  //these are the static static defined varibles to be used to find the number of ruptures.
  private int totNumRups;

  private Location location;

  /**
   * constructor specifying the values needed for Gutenberg Richter
   * and also for constructing the rupture
   *
   * @param rake  : Average rake of the surface
   * @param aValue : cumRate of GR distribution (events/yr at mag=magLower)
   * @param bValue : b Value in the GR distribution
   * @param magLower : magLower as in GR distribution
   * @param magUpper : magUpper as in GR distribution
   * @param delta  : delta as in GR distribution
   * @param surface : Fault Surface
   */
  public PointGR_EqkSource(     double lat,
                                double lon,
                                double depth,
                                double rake,
                                double cumRate,
                                double bValue,
                                double magLower,
                                double magUpper,
                                double delta) {

    // see here that we have rounded num to nearest integer value
    int num = (int)Math.rint((magUpper - magLower)/delta + 1);
    if( D ) System.out.println("PointGR_EqkSource:magUpper::"+magUpper);
    if( D ) System.out.println("PointGR_EqkSource:magLower::"+magLower);
    if( D ) System.out.println("PointGR_EqkSource:delta::"+delta);
    if( D ) System.out.println("PointGR_EqkSource:num::"+num);

    //Setting the GutenbergDistribution
    gR = new GutenbergRichterMagFreqDist(magLower,magUpper,num);
    gR.setAllButTotMoRate(magLower,magUpper,cumRate,bValue );

    // Determine number of ruptures
    totNumRups = gR.getNum();

    // make the prob qk rupture
    probEqkRupture = new ProbEqkRupture();
    location = new Location(lat,lon,depth);
    probEqkRupture.setPointSurface(location);
    probEqkRupture.setAveRake(rake);



    if( D ) System.out.println("PointGR_EqkSource:momentRate::"+gR.getTotalMomentRate());
  }

  /**
   * this functions sums up all the ruptures for all magnitudes
   * @return the total num of rutures for all magnitudes
   */
  public int getNumRuptures() { return totNumRups; }


  /**
   * This method sets the probability of the different rupture surface for different mag
   * @param nthRupture : it is to find the mag and rate to which that rupture number correspond
   * @return the object of the ProbEqkRupture class after setting the probability
   */
  public ProbEqkRupture getRupture(int nthRupture){
     probEqkRupture.setMag(gR.getX(nthRupture));
     double prob = 1 - Math.exp(-timeSpan*gR.getY(nthRupture));
     probEqkRupture.setProbability(prob);
     return probEqkRupture;
  }


  /** Set the time span in years
   *
   * @param yrs : timeSpan as specified in  Number of years
   */
  public void setTimeSpan(double yrs) {
   //set the time span in yrs
    timeSpan = yrs;
  }



  /** Set the time span in years
   * FIX Mehthod not implemented yet
   *
   * @param yrs : timeSpan
   *
   */
  public void setTimeSpan(TimeSpan timeSpan) {

     // set the probability according to the specifed timespan
    throw new UnsupportedOperationException(C+"setTimeSpan(timeSpan) Not implemented.");
  }


 /**
  * Returns the Vector consisting of all ruptures for this source
  * all the objects are cloned. so this vector can be saved by the user
  *
  * @return Vector consisting of
  */
  public Vector getRuptureList(){
    Vector v= new Vector();
    for(int i=0;i<totNumRups;++i)
      v.add(this.getRuptureClone(i));
    return v;
  }

     /**
   * This returns the shortest dist to either end of the fault trace, or to the
   * mid point of the fault trace.
   * @param site
   * @return minimum distance
   */
   public  double getMinDistance(Site site) {

      // get first location on fault trace
      Direction dir = RelativeLocation.getDirection(site.getLocation(), location);
      return dir.getHorzDistance();

    }

 /**
  * get the name of this class
  *
  * @return
  */
 public String getName() {
   return C;
  }
}