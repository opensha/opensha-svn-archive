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

  private GuttenbergRichterMagFreqDist gR;
  private double rake;
  private double timeSpan;
  //these are the static static defined varibles to be used to find the number of ruptures.
  private final static double RUPTURE_WIDTH =100.0;
  private final static double RUPTURE_OFFSET =10.0;

  /**
   * constructor specifying the values needed for Guttenberg Richter
   * and also for constructing the rupture
   *
   * @param rake  : Average rake of the surface
   * @param aValue : a  Value in GR distribution
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
    // see here that we have rounded num to nearest integer value

    int num = (int)Math.rint((magUpper - magLower)/delta + 1);
    System.out.println("Frankel96_GR_EqkSource:magUpper::"+magUpper);
    System.out.println("Frankel96_GR_EqkSource:magLower::"+magLower);
    System.out.println("Frankel96_GR_EqkSource:delta::"+delta);
    System.out.println("Frankel96_GR_EqkSource:num::"+num);
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setAveRake(rake);
    probEqkRupture.setRuptureSurface(surface);

    /*
    This statement checks for the num to be 1, if it is then we make the num to be 2,
    otherwise EvenlyDiscretizedFunc will be throwing an exception for division by zero in the constructor
    */
    if(num==1)
      gR = new GuttenbergRichterMagFreqDist(magLower,magUpper,2);
    else
      gR = new GuttenbergRichterMagFreqDist(magLower,magUpper,num);

    //Setting the GuttenbergDistribution
    gR.setAllButTotMoRate(magLower,magUpper,1,bValue );
    double rate = Math.pow(10,aValue - bValue*magLower);
    gR.scaleToIncrRate(magLower,rate);

    System.out.println("Frankel96_GR_EqkSource:Frankel96_GR_EqkSource:momentRate::"+gR.getTotalMomentRate());
  }

  /**
   * this functions sums up all the ruptures for all magnitudes
   * @return the total num of rutures for all magnitudes
   */
  public int getNumRuptures() {
    int num = gR.getNum();
    int numRuptures=0;
    for(int i=0;i<num;++i){
      double mag=gR.getX(i);
      numRuptures += getNumRuptures(mag);
    }
    return numRuptures;
  }


  /**
   * This method sets the probability of the different rupture surface for different mag
   * @param nRupture : it is to find the mag and rate to which that rupture number correspond
   * @return the object of the ProbEqkRupture class after setting the probability
   */
  public ProbEqkRupture getRupture(int nRupture){
    int num = gR.getNum();
    double mag=0;
    int ruptures=0;
    for(int i=0;i<num;++i){
      mag=gR.getX(i);
      ruptures += getNumRuptures(mag);
      if(nRupture <= ruptures)
        break;
    }
   double rate = gR.getY(mag);
   double prob = 1- Math.exp(-(timeSpan*rate)/ruptures);
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

  }


 /**
  * Returns the Vector consisting of all ruptures for this source
  * all the objects are cloned. so this vector can be saved by the user
  *
  * @return Vector consisting of
  */
  public Vector getRuptureList(){
    Vector v= new Vector();
    int num = gR.getNum();
    for(int i=0;i<num;++i)
      v.add(this.getRuptureClone(i));
    return v;
  }


  /**
   * @param mag
   * @return the total number of ruptures associated with the given mag
   */
  private int getNumRuptures(double mag){
    int numRuptures=0;
    EvenlyGriddedSurface evenGS = (EvenlyGriddedSurface)probEqkRupture.getRuptureSurface();
    double ruptureLen=WC1994_MagLengthRelationship.getMeanLength(mag,rake);
    numRuptures = evenGS.getTotalRuptures(ruptureLen,RUPTURE_WIDTH,RUPTURE_OFFSET);
    return numRuptures;
 }
}