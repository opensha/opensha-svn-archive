package org.scec.sha.earthquake;

import org.scec.sha.magdist.*;
import org.scec.data.*;
import org.scec.data.function.*;
import org.scec.calc.RelativeLocation;

import java.util.Vector;
import java.util.Iterator;

/**
 * <p>Title: PointPoissonEqkSource </p>
 * <p>Description: This takes a Location, an IncrementalMagFreqDist (of Poissonian
 * rates), a duration, an aveRake, an aveDip, and creates a ProbEqkRupture for each
 * magnitude with a non-zero rate.  It is assumed that the duration units are the same
 * as those for the rates in the IncrementalMagFreqDist.</p>
 *
 * @author Edward Field
 * @date Sep 2, 2002
 * @version 1.0
 */

public class PointPoissonEqkSource extends ProbEqkSource implements java.io.Serializable{


  //for Debug purposes
  private static String  C = new String("PointPoissonEqkSource");
  private boolean D = false;

  private IncrementalMagFreqDist magFreqDist;
  private Location location;
  private double aveDip=Double.NaN;
  private double aveRake=Double.NaN;
  private double duration;

  private double minMag = 0.0;

  // to hold the non-zero mags and rates
  //ArbitrarilyDiscretizedFunc magsAndRates;
  //vector of Mag and Rates. I am using it over the ArbitrarilyDiscretisedFunc becuase
  //that class cannot be serialsed as it uses the tree map. So I am using the vector of Mag and Rate
  Vector mags, rates;


  /**
   * Constructor specifying the location object, the IncrementalMagFreqDist
   * object, the duration, the average rake, the dip, and the minimum magnitude
   * to consider from the magFreqDist in making the source (those below are ingored).
   *
   */
  public PointPoissonEqkSource(Location loc, IncrementalMagFreqDist magFreqDist,double duration,
                        double aveRake, double aveDip, double minMag){
    this.location =loc;
    this.duration=duration;
    this.aveRake=aveRake;
    this.aveDip=aveDip;
    this.minMag=minMag;

    // set the magFreqDist
    this.setMagFreqDist(magFreqDist);

    // make the prob qk rupture
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setPointSurface(location, aveDip);
    probEqkRupture.setAveRake(aveRake);
    /*if( D ) System.out.println("PointEqkSource Constructor: totNumRups="+magsAndRates.getNum()+
                               "; aveDip="+probEqkRupture.getRuptureSurface().getAveDip()+
                               "; aveRake="+ probEqkRupture.getAveRake());*/
  }


  /**
   * Constructor specifying the location object, the IncrementalMagFreqDist
   * object, the duration, the average rake, and the dip.  This sets minMag to
   * zero (magnitudes from magFreqDist below are ignored in making the source)
   *
   */
  public PointPoissonEqkSource(Location loc, IncrementalMagFreqDist magFreqDist,double duration,
                        double aveRake, double aveDip){
    this( loc,  magFreqDist, duration, aveRake,  aveDip, 0.0);

  }


  /**
   * This sets the magFreqDist
   * @param magFreqDist
   */
  public void setMagFreqDist(IncrementalMagFreqDist magFreqDist) {

    this.magFreqDist=magFreqDist;

    // make list of non-zero rates and mags (if mag >= minMag)
    //magsAndRates = new ArbitrarilyDiscretizedFunc();
    mags = new Vector();
    rates = new Vector();
    for (int i=0; i<magFreqDist.getNum(); ++i){
        if(magFreqDist.getY(i) > 0 && magFreqDist.getX(i) >= minMag){
            //magsAndRates.set(magFreqDist.getX(i),magFreqDist.getY(i));
          mags.add(new Double(magFreqDist.getX(i)));
          rates.add(new Double(magFreqDist.getY(i)));
        }
    }

   // if (D) System.out.println(C+" numNonZeroMagDistPoints="+magsAndRates.getNum());
  }


  /**
   * @return the number of rutures (equals number of mags with non-zero rates)
   */
  public int getNumRuptures() {
    //return magsAndRates.getNum();
    return mags.size();
  }


  /**
   * This makes and returns the nth probEqkRupture for this source.
   */
  public ProbEqkRupture getRupture(int nthRupture){

    // set the magnitude
    //probEqkRupture.setMag(magsAndRates.getX(nthRupture));
    probEqkRupture.setMag(((Double)mags.get(nthRupture)).doubleValue());

    // compute and set the probability
    double prob = 1 - Math.exp(-duration*((Double)rates.get(nthRupture)).doubleValue());
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
  * @param duration
  */
  public double getDuration() {
    return duration;
  }


  /**
   * This sets the location
   * @param loc
   */
  public void setLocation(Location loc) {
    location = loc;
    probEqkRupture.setPointSurface(location, aveDip);
  }

  /**
   * This gets the location
   * @return Location
   */
  public Location getLocation(){
    return location;
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
      return RelativeLocation.getHorzDistance(site.getLocation(), location);
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