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
  private double aveDip=Double.NaN;



  /**
   * Constructor specifying the location object, the Gutenberg Richter distribution
   * object, and the average rake and dip.
   *
   * @param lat  : The Latitude of the point source
   * @param lon  : The Longitude of the point source
   * @param depth  : The depth of the point source
   * @param aveRake  : Average rake of the rupture
   * @param aveDip  : Average dip of the surface
   * @param aValue : cumRate of GR distribution (events/yr at mag=magLower)
   * @param bValue : b Value in the GR distribution
   * @param magLower : magLower as in GR distribution
   * @param magUpper : magUpper as in GR distribution
   * @param delta  : delta as in GR distribution
   */

  public PointGR_EqkSource(Location loc, GutenbergRichterMagFreqDist gr,double aveRake, double aveDip){
    this.location =loc;
    this.gR=gr;
    this.aveDip=aveDip;

    // Determine number of ruptures (don't count mags with zero rate)
    totNumRups = 0;
    int grNum = gR.getNum();
    for (int i=0; i<grNum; ++i)
        if(gr.getY(i) > 0) totNumRups += 1;

    // make the prob qk rupture
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setPointSurface(location, aveDip);
    probEqkRupture.setAveRake(aveRake);
    if( D ) System.out.println("PointGR_EqkSource Constructor: totNumRups="+totNumRups+
                               "; aveDip="+probEqkRupture.getRuptureSurface().getAveDip()+
                               "; aveRake="+ probEqkRupture.getAveRake());
  }

  /**
   * Constructor specifying the location, the average rake and dip, and the values
   * needed to construce a Gutenberg Richter distribution.
   *
   * @param lat  : The Latitude of the point source
   * @param lon  : The Longitude of the point source
   * @param depth  : The depth of the point source
   * @param aveRake  : Average rake of the rupture
   * @param aveDip  : Average dip of the surface
   * @param aValue : cumRate of GR distribution (events/yr at mag=magLower)
   * @param bValue : b Value in the GR distribution
   * @param magLower : magLower as in GR distribution
   * @param magUpper : magUpper as in GR distribution
   * @param delta  : delta as in GR distribution
   */
  public PointGR_EqkSource(     double lat,
                                double lon,
                                double depth,
                                double aveRake,
                                double aveDip,
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

    // Determine number of ruptures (no zero rates here)
    totNumRups = gR.getNum();

    // make the prob qk rupture
    probEqkRupture = new ProbEqkRupture();
    location = new Location(lat,lon,depth);
    probEqkRupture.setPointSurface(location, aveDip);
    probEqkRupture.setAveRake(aveRake);



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
    // find index of fist non-zero rate
     int index = gR.getXIndex(gR.getMagLower());

     probEqkRupture.setMag(gR.getX(index+nthRupture));
     double prob = 1 - Math.exp(-timeSpan*gR.getY(index+nthRupture));
     probEqkRupture.setProbability(prob);
     return probEqkRupture;
  }


  /**
   * This method allows one to change the location without changing
   * anything else.
   * @param loc
   */
  public void setLocation(Location loc) {
    location = loc;
    probEqkRupture.setPointSurface(location, aveDip);
  }


  /** Set the time span in years
   *
   * @param yrs : timeSpan as specified in  Number of years
   */
  public void setTimeSpan(double yrs) {
   //set the time span in yrs
    timeSpan = yrs;
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