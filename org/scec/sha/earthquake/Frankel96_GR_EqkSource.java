package org.scec.sha.earthquake;

import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.sha.magdist.GuttenbergRichterMagFreqDist;
import org.scec.sha.calc.WC1994_MagLengthRelationship;
import org.scec.data.TimeSpan;

import java.util.Vector;
import java.util.Iterator;

/**
 * <p>Title: Frankel96_GR_EqkSource </p>
 * <p>Description: frankel 1996 Guttenberg Richter Type B earthquake source</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Sep 2, 2002
 * @version 1.0
 */

public class Frankel96_GR_EqkSource extends ProbEqkSource {


  //for Debug purposes
  private static String  C = new String("Frankel96_GR_EqkSource");
  private boolean D =false;

  private GuttenbergRichterMagFreqDist gR;
  private double rake;
  private double timeSpan;
  //these are the static static defined varibles to be used to find the number of ruptures.
  private final static double RUPTURE_WIDTH =100.0;
  private final static double RUPTURE_OFFSET =10.0;
  private int totNumRups;
  private EvenlyGriddedSurface surface;

  /**
   * constructor specifying the values needed for Guttenberg Richter
   * and also for constructing the rupture
   *
   * @param rake  : Average rake of the surface
   * @param aValue : a  Value of GR distribution (events/yr at mag=0 increment)
   * @param bValue : b Value in the GR distribution
   * @param magLower : magLower as in GR distribution
   * @param magUpper : magUpper as in GR distribution
   * @param delta  : delta as in GR distribution
   * @param surface : Fault Surface
   */
  public Frankel96_GR_EqkSource(double rake,
                                double aValue,
                                double bValue,
                                double magLower,
                                double magUpper,
                                double delta,
                                EvenlyGriddedSurface surface) {

    this.rake=rake;
    this.surface=surface;
    // see here that we have rounded num to nearest integer value

    int num = (int)Math.rint((magUpper - magLower)/delta + 1);
    if( D ) System.out.println("Frankel96_GR_EqkSource:magUpper::"+magUpper);
    if( D ) System.out.println("Frankel96_GR_EqkSource:magLower::"+magLower);
    if( D ) System.out.println("Frankel96_GR_EqkSource:delta::"+delta);
    if( D ) System.out.println("Frankel96_GR_EqkSource:num::"+num);
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setAveRake(rake);

    //Setting the GuttenbergDistribution
    gR = new GuttenbergRichterMagFreqDist(magLower,magUpper,num);
    gR.setAllButTotMoRate(magLower,magUpper,1,bValue );
    double rate = Math.pow(10,aValue - bValue*magLower);
    gR.scaleToIncrRate(magLower,rate);

    // Determine number of ruptures
    int numMags = gR.getNum();
    int totNumRups=0;
    WC1994_MagLengthRelationship magLength = new WC1994_MagLengthRelationship();
    for(int i=0;i<num;++i){
      double rupLen = magLength.getMeanLength(gR.getX(i),rake);
      totNumRups += getNumRuptures(rupLen);
    }

    if( D ) System.out.println("Frankel96_GR_EqkSource:Frankel96_GR_EqkSource:momentRate::"+gR.getTotalMomentRate());
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
    int numMags = gR.getNum();
    double mag=0, rupLen=0;
    int numRups=0, tempNumRups=0;

    if(nthRupture < 1 || nthRupture>getNumRuptures())
       throw new RuntimeException(C+":getRupture():: Invalid rupture index. This index does not exist");

    WC1994_MagLengthRelationship magLength = new WC1994_MagLengthRelationship();
    for(int i=0;i<numMags;++i){
      mag=gR.getX(i);
      rupLen = magLength.getMeanLength(gR.getX(i),rake);
      numRups = getNumRuptures(rupLen);
      tempNumRups += numRups;
      if(nthRupture <= tempNumRups)
        break;
    }
    // set probability
    double rate = gR.getY(mag);
    double prob = 1- Math.exp(-timeSpan*rate/numRups);
    probEqkRupture.setProbability(prob);

    // set rupture surface
    probEqkRupture.setRuptureSurface( surface.getNthSubsetSurface(rupLen,
                                      RUPTURE_WIDTH,RUPTURE_OFFSET,
                                      nthRupture+numRups-tempNumRups-1));

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
   * @param mag
   * @return the total number of ruptures associated with the given mag
   */
  private int getNumRuptures(double rupLen){
    return surface.getNumSubsetSurfaces(rupLen,RUPTURE_WIDTH,RUPTURE_OFFSET);
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