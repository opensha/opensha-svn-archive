package org.scec.sha.magdist;

import org.scec.calc.*;
/**
 * <p>Title: SingleMagFreqDist</p>
 * <p>Description: This has only magnitude with the non-zero rate</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
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
   * @param momentRate
   */

  public SingleMagFreqDist(double min,double delta,int num,double mag,double momentRate) {
    super(min,num,delta);
    this.mag=mag;
    this.rate=momentRate;
  }

  public double getRate() {
    return rate;
  }

  public double getMag() {
    return mag;
  }

  public void setMagAndRate(double mag, double rate) {
    this.mag=mag;
    this.rate=rate;
  }

  public void setMagAndMomentRate(double mag,double momentRate) {
    this.mag=mag;
    this.rate=momentRate/MomentMagCalc.getMoment(mag);
  }

  public void setRateAndMomentRate(double rate,double momentRate) {
    this.rate=rate;
    this.mag=MomentMagCalc.getMag(momentRate);
  }

 public String getName() {
   return "SingleMagFreqDist";
 }

 public String getInfo() {
   double momentRate= this.rate * MomentMagCalc.getMoment(this.mag);
   return "mag="+this.mag+";"+"rate="+rate+";"+"moRate="+momentRate;
 }

}