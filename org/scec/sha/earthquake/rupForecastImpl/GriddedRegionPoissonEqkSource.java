package org.scec.sha.earthquake.rupForecastImpl;

import org.scec.sha.magdist.*;
import org.scec.data.*;
import org.scec.data.region.*;
import org.scec.data.function.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.earthquake.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>Title: GriddedRegionPoissonEqkSource </p>
 * <p>Description: This takes a EvenlyGriddedGeographicRegionAPI, an IncrementalMagFreqDist (of Poissonian
 * rates), a duration, an aveRake, an aveDip, and creates a ProbEqkRupture for each
 * magnitude (with a non-zero rate) and location.The MagFreqDist applied to each point is just the
 * original divided by the number of points.  It is assumed that the duration units are the same
 * as those for the rates in the IncrementalMagFreqDist.</p>
 *
 * @author Edward Field
 * @date Sep 2, 2002
 * @version 1.0
 */

public class GriddedRegionPoissonEqkSource extends ProbEqkSource implements java.io.Serializable{


  //for Debug purposes
  private static String  C = new String("GriddedRegionPoissonEqkSource");
  private boolean D = false;

  private IncrementalMagFreqDist magFreqDist;
  private EvenlyGriddedGeographicRegionAPI region;
  private double aveDip=Double.NaN;
  private double aveRake=Double.NaN;
  private double duration;
  int numLocs, numMags;

  private double minMag = 0.0;

  // to hold the non-zero mags and rates
  ArrayList mags, rates;


  /**
   * Constructor specifying the region, the IncrementalMagFreqDist
   * object, the duration, the average rake, the dip, and the minimum magnitude
   * to consider from the magFreqDist in making the source (those below are ingored).
   *
   */
  public GriddedRegionPoissonEqkSource(EvenlyGriddedGeographicRegionAPI region, IncrementalMagFreqDist
                                       magFreqDist,double duration,double aveRake, double aveDip,
                                       double minMag){
    this.region =region;
    this.numLocs = region.getNumGridLocs();
    this.duration=duration;
    this.aveRake=aveRake;
    this.aveDip=aveDip;
    this.minMag=minMag;

    // set the magFreqDist
    setMagFreqDist(magFreqDist);

    // make the prob qk rupture
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setAveRake(aveRake);
    if( D ) System.out.println("GriddedRegionPoissonEqkSource Constructor: numLocs="+numLocs+
                               "; numMags="+numMags+"; aveDip="+aveDip+
                               "; aveRake="+ probEqkRupture.getAveRake());
  }


  /**
   * Constructor specifying the region object, the IncrementalMagFreqDist
   * object, the duration, the average rake, and the dip.  This sets minMag to
   * zero (magnitudes from magFreqDist below are ignored in making the source)
   *
   */
  public GriddedRegionPoissonEqkSource(EvenlyGriddedGeographicRegionAPI region, IncrementalMagFreqDist
                                       magFreqDist, double duration, double aveRake, double aveDip){

    this( region,  magFreqDist, duration, aveRake,  aveDip, 0.0);

  }


  /**
   * This sets the magFreqDist
   * @param magFreqDist
   */
  public void setMagFreqDist(IncrementalMagFreqDist magFreqDist) {

    this.magFreqDist=magFreqDist;

    // make list of non-zero rates and mags (if mag >= minMag)
    mags = new ArrayList();
    rates = new ArrayList();

    for (int i=0; i<magFreqDist.getNum(); ++i){
        if(magFreqDist.getY(i) > 0 && magFreqDist.getX(i) >= minMag){
          mags.add(new Double(magFreqDist.getX(i)));
          rates.add(new Double(magFreqDist.getY(i)/numLocs));   // normalized by numLocs!
        }
    }
    this.numMags = mags.size();
  }


  /**
   * @return the number of rutures (equals number of mags with non-zero rates time number of locations in region)
   */
  public int getNumRuptures() {
    return numMags*numLocs;
  }


  /**
   * This makes and returns the nth probEqkRupture for this source.
   */
  public ProbEqkRupture getRupture(int nthRupture){

    int ithMag = nthRupture/numLocs;
    int ithLoc = nthRupture%numLocs;

    if(D) System.out.println(nthRupture+"th rupture; "+ithMag+"th mag; "+ithLoc+"th loc");

    // set the magnitude
    probEqkRupture.setMag(((Double)mags.get(ithMag)).doubleValue());

    // set the location & aveDip
    probEqkRupture.setPointSurface(region.getGridLocation(ithLoc), aveDip);

    // compute and set the probability
    double prob = 1.0 - Math.exp(-duration*((Double)rates.get(ithMag)).doubleValue());
    probEqkRupture.setProbability(prob);

    // return the ProbEqkRupture
    return probEqkRupture;
  }


  /**
   * This sets the duration used in computing Poisson probabilities.  This assumes
   * the same units as in the magFreqDist rates.
   * @param duration
   */
  public void setDuration(double duration) {
    this.duration=duration;
  }


  /**
   * This sets minimum magnitude to be used from the mag-freq dist (those
   * below are ignored in making the source).  Default is zero.
   */
  public void setMinMag(double minMag) {
    this.minMag=minMag;
    // redo the mag & rate vectors:
    setMagFreqDist(this.magFreqDist);
  }




  /**
  * This gets the duration used in computing Poisson probabilities
  * @param duration (in same units as in the MagFreqDist)
  */
  public double getDuration() {
    return duration;
  }


  /**
   * This gets the region
   * @return region
   */
  public EvenlyGriddedGeographicRegionAPI getRegion(){
    return region;
  }


  /**
   * This gets the minimum magnitude considered from the mag-freq dist (those
   * below are ignored in making the source).
   * @return minMag
   */
  public double getMinMag(){
    return minMag;
  }


     /**
   * This returns the shortest horizontal dist to the point source.
   * @param site
   * @return minimum distance
   */
   public  double getMinDistance(Site site) {
     return ((GeographicRegion) region).getMinHorzDistToRegion(site.getLocation());
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