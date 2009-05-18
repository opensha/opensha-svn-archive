package org.opensha.sha.earthquake.rupForecastImpl;

import org.opensha.sha.magdist.*;
import org.opensha.data.*;
import org.opensha.data.function.*;
import org.opensha.commons.calc.RelativeLocation;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>Title: PointEqkSource </p>
 * <p>Description: This makes a point source based on the following inputs:</p>
 * <UL>Location
 * <LI>IncrementalMagFreqDist and duration (or magnitude and probability)
 * <LI>average rake
 * <LI>average dip
 * <LI>minimum magnitude (if a mag.-freq. dist. has been given).
 * </UL><p>
 *
 * If an IncrementalMagFreqDist and duration have been given, then the source is
 * Poissonian and it is assumed that the duration units are the same
 * as those for the rates in the IncrementalMagFreqDist.  Also, magnitudes below the minimum
 * are ignores, as are those with zero rates.  If magnitude/probability have
 * been given, the source has only one rupture and is not Poissonian.</p>
 *
 * @author Edward Field
 * @date Sep 2, 2002
 * @version 1.0
 */

public class PointEqkSource extends ProbEqkSource implements java.io.Serializable{

  //for Debug purposes
  private static String  C = new String("PointEqkSource");
  private static String NAME = "Point Eqk Source";
  private boolean D = false;

  private Location location;
  private double aveDip=Double.NaN;
  private double aveRake=Double.NaN;
  private double duration=Double.NaN;
  private double minMag = Double.NaN;

  // to hold the non-zero mags and rates
  private ArrayList mags, rates;


  /**
   * Constructor specifying the Location, the IncrementalMagFreqDist, the duration,
   * the average rake, the dip, and the minimum magnitude to consider from the magFreqDist
   * in making the source (those below are ingored).  The source is set as Poissonian
   * with this constructor.
   *
   */
  public PointEqkSource(Location loc, IncrementalMagFreqDist magFreqDist,double duration,
                        double aveRake, double aveDip, double minMag){
    this.location =loc;
    this.duration=duration;
    this.aveRake=aveRake;
    this.aveDip=aveDip;
    this.minMag=minMag;

    // set the magFreqDist
    setMagFreqDist(magFreqDist);

    isPoissonian = true;

    // make the prob qk rupture
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setPointSurface(location, aveDip);
    probEqkRupture.setAveRake(aveRake);
    /*if( D ) System.out.println("PointEqkSource Constructor: totNumRups="+magsAndRates.getNum()+
                               "; aveDip="+probEqkRupture.getRuptureSurface().getAveDip()+
                               "; aveRake="+ probEqkRupture.getAveRake());*/
  }


  /**
   * Constructor specifying the location, the IncrementalMagFreqDist, the duration,
   * the average rake, and the dip.  This sets minMag to zero (magnitudes from magFreqDist
   * below are ignored in making the source). The source is set as Poissonian with this constructor.
   *
   */
  public PointEqkSource(Location loc, IncrementalMagFreqDist magFreqDist,double duration,
                        double aveRake, double aveDip){
    this( loc,  magFreqDist, duration, aveRake,  aveDip, 0.0);

  }


  /**
   * Constructor specifying the location, a magnitude and probability,
   * the average rake, and the dip.  The source is set as Poissonian with this
   * constructor.
   *
   */
  public PointEqkSource(Location loc, double magnitude,double probability,
                        double aveRake, double aveDip){
    this.location =loc;
    this.aveRake=aveRake;
    this.aveDip=aveDip;

    // add the one magnitude to the mags list
    mags = new ArrayList();
    mags.add(new Double(magnitude));

    this.isPoissonian = false;

    // make the prob qk rupture
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setPointSurface(location, aveDip);
    probEqkRupture.setAveRake(aveRake);
    probEqkRupture.setProbability(probability);
  }

  /**
   * It returns a list of all the locations which make up the surface for this
   * source.
   *
   * @return LocationList - List of all the locations which constitute the surface
   * of this source
   */
 public LocationList getAllSourceLocs() {
   LocationList locList = new LocationList();
   locList.addLocation(this.location);
   return locList;
 }
 
 public EvenlyGriddedSurfaceAPI getSourceSurface() { return probEqkRupture.getRuptureSurface(); }


  /**
   * This creates the lists of mags and non-zero rates (above minMag).
   * @param magFreqDist
   */
  private void setMagFreqDist(IncrementalMagFreqDist magFreqDist) {

    // make list of non-zero rates and mags (if mag >= minMag)
    //magsAndRates = new ArbitrarilyDiscretizedFunc();
    mags = new ArrayList();
    rates = new ArrayList();
    for (int i=0; i<magFreqDist.getNum(); ++i){
        if(magFreqDist.getY(i) > 0 && magFreqDist.getX(i) >= minMag){
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
    return mags.size();
  }


  /**
   * This makes and returns the nth probEqkRupture for this source.
   */
  public ProbEqkRupture getRupture(int nthRupture){

    // set the magnitude
    //probEqkRupture.setMag(magsAndRates.getX(nthRupture));
    probEqkRupture.setMag(((Double)mags.get(nthRupture)).doubleValue());

    // set the probability if it's Poissonian (otherwise this was already set)
    if(isPoissonian)
      probEqkRupture.setProbability(1 - Math.exp(-duration*((Double)rates.get(nthRupture)).doubleValue()));

    // return the ProbEqkRupture
    return probEqkRupture;
  }


  /**
   * This sets the duration used in computing Poisson probabilities.  This assumes
   * the same units as in the magFreqDist rates.  This is ignored if the source in non-Poissonian.
   * @param duration
   */
  public void setDuration(double duration) {
    this.duration=duration;
  }


  /**
  * This gets the duration used in computing Poisson probabilities (it may be NaN
  * if the source is not Poissonian).
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
   * This gets the minimum magnitude to be considered from the mag-freq dist (those
   * below are ignored in making the source).  This will be NaN if the source is not
   * Poissonian.
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
