package org.scec.sha.magdist;

import org.scec.calc.*;
/**
 * <p>Title: SingleMagFreqDist</p>
 * <p>Description: This has only magnitude with the non-zero rate</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author :Nitin Gupta Date:Aug,8,2002
 * @version 1.0
 */

public class SingleMagFreqDist extends IncrementalMagFreqDist {

  /**
   * todo variables
   */
  private double mag;
  private double rate;

  /**
   * to do constructors
   */

  /**
   * Constructor
   * @param min
   * @param num
   * @param delta
   */
  public SingleMagFreqDist(double min,int num,double delta) {
    super(min,num,delta);
  }

  /**
   * Constructor
   * @param min
   * @param max
   * @param num
   */
  private SingleMagFreqDist(double min,double max,int num) {
    super(min,max,num);
  }

  /**
   * Constructor
   * @param min
   * @param delta
   * @param num
   * @param mag
   * @param moRate
   */

  public SingleMagFreqDist(double min,int num,double delta, double mag,double moRate) {
    super(min,num,delta);
    this.mag=mag;
    this.rate=moRate;
  }

  /**
   * returns the rate for which  magnitude has non-zero rate
   * @return
   */
  public double getRate() {
    return rate;
  }

  /**
   *Gets the magnitude which has non-zero rate
   * @return
   */
  public double getMag() {
    return mag;
  }

  /**
   * sets the magnitude for non-zero rate
   * @param mag
   * @param rate
   */
  public void setMagAndRate(double mag, double rate) {
    this.mag=mag;
    this.rate=rate;
  }

  /**
   * Sets the magnitude
   * For this magnitude it calculates the non-zero rate from a static function
   * getMoment of the class MomentMagCalc and moRate
   * @param mag
   * @param moRate
   */
  public void setMagAndMomentRate(double mag,double moRate) {
    this.mag=mag;
    this.rate=moRate/MomentMagCalc.getMoment(mag);
  }

  /**
   * sets the non-zero rate
   * For this rate the magnitude is calculated using the static function
   * getMag of the class MomentMagCalc  and moRate
   * @param rate
   * @param moRate
   */
  public void setRateAndMomentRate(double rate,double moRate) {
    this.rate=rate;
    this.mag=MomentMagCalc.getMag(moRate);
  }

  /**
   *
   * @return the name of the class which was invoked by the user
   */
 public String getName() {
   return "SingleMagFreqDist";
 }

 /**
  *
  * @return the total information stored in the class in form of a string
  */
 public String getInfo() {
   double moRate= this.rate * MomentMagCalc.getMoment(this.mag);
   return "mag="+this.mag+";"+"rate="+rate+";"+"moRate="+moRate;
 }

}