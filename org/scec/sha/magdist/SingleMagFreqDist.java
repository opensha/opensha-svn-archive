package org.scec.sha.magdist;

import org.scec.calc.*;
/**
 * <p>Title: SingleMagFreqDist</p>
 * <p>Description: This has only a single magnitude with a non-zero rate.
 * Note that this magnitude must equal one of the descrete x-axis points.</p>
 *
 * @author :Nitin Gupta Date:Aug,8,2002
 * @version 1.0
 */

public class SingleMagFreqDist extends IncrementalMagFreqDist {

  /**
   * todo variables
   */
  public static String NAME = "Single-Mag Dist" ;
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
  public SingleMagFreqDist(double min,double max,int num) {
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
    rate = moRate/MomentMagCalc.getMoment(mag);
    setMagAndRate(mag, rate);
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
    for(int i=0;i<num;++i)
       set(i,0.0);
    set(mag,rate);
  }

  /**
   * Sets the magnitude
   * For this magnitude it calculates the non-zero rate from a static function
   * getMoment of the class MomentMagCalc and moRate
   * @param mag
   * @param moRate
   */
  public void setMagAndMomentRate(double mag,double moRate) {
    this.rate=moRate/MomentMagCalc.getMoment(mag);
    setMagAndRate(mag,rate);
  }

  /**
   * sets the non-zero rate
   * For this rate the magnitude is calculated using the static function
   * getMag of the class MomentMagCalc  and moRate.  NOTE: this does not
   * give the exact magnitude, but rather the closest magnitude given the
   * discretization.
   * @param rate
   * @param moRate
   */
  public void setRateAndMomentRate(double rate,double moRate) {

    this.mag = MomentMagCalc.getMag(moRate/rate);
    int index = (int) Math.rint((mag - minX)/delta);
    setMagAndRate(getX(index),rate);
  }

  /**
   *
   * @return the name of the class which was invoked by the user
   */
 public String getName() {
   return NAME;
 }

 /**
  *
  * @return the total information stored in the class in form of a string
  */
 public String getInfo() {
   double totMoRate= this.rate * MomentMagCalc.getMoment(this.mag);
   return "mag="+this.mag+"; rate="+(float)rate+"; totMoRate="+(float)totMoRate;
 }

 /**
   * this method (defined in parent) is deactivated here (name is finalized)
   **/

  public void setName(String name) {
    throw new UnsupportedOperationException(C+"::setName not allowed for MagFreqDist.");

  }

  /**
   * this method (defined in parent) is deactivated here (info is generated internally)
   **/
  public void setInfo(String info) {
    throw new UnsupportedOperationException("setInfo not allowed for MagFreqDist.");

  }

}