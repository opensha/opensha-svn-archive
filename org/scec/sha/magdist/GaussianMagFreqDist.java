package org.scec.sha.magdist;

import org.scec.data.DataPoint2D;
import org.scec.exceptions.MagFreqDistException;
import org.scec.exceptions.DataPoint2DException;




/**
 * <p>Title:GaussianMagFreqDist </p>
 * <p>Description: This assumes a Gaussian distribution with a given mean and standard deviation, with
 * the option of being truncated at some number of standard deviations(one or two sided truncation).
 * Note: applying thge scaleTo*() or normalizeBy*() methods of the parent class will not result in
 * totMoRate being updated</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta , Date: Aug,8,2002
 * @version 1.0
 */

public class GaussianMagFreqDist extends IncrementalMagFreqDist {



  /**
   * todo class fields
   * Have been initialized to Double.NaN
   */
  public  static String NAME = "Gaussian Dist.";
  private double mean= Double.NaN;
  private double stdDev= Double.NaN;
  private double totMoRate = Double.NaN;

  /*
   The # of stdDev(from Mean) where dist. cut to zero
   */
  private double truncLevel = Double.NaN;

  /*
   none if=0;one-sided,upper if=1,two-sided if=2
   */
  private int truncType =0  ;

  /**
   * todo constructors
   * All the constructors call the function computeRates which sets up the rate
   * as Y-axis values based on the X-axis values provided in the form of min,num,delta of mag.
   */

  /**
   * constructor
   * @param min
   * @param num
   * @param delta
   */
  public GaussianMagFreqDist(double min,int num,double delta) throws DataPoint2DException{
    super(min,num,delta);
    computeRates();
  }

  /**
   * Constructor
   * @param min
   * @param max
   * @param num
   */
  public GaussianMagFreqDist(double min,double max,int num) throws DataPoint2DException{
    super(min,max,num);
    computeRates();
  }


  /**
   * Constructor
   * @param min
   * @param max
   * @param num
   * @param mean
   * @param stdDev
   * @param totMoRate
   */
  public GaussianMagFreqDist(double min,double max,int num,double mean,double stdDev,
                             double totMoRate) throws DataPoint2DException{
    super(min,max,num);
    this.mean=mean;
    this.stdDev=stdDev;
    this.totMoRate=totMoRate;
    computeRates();
  }

  /**
   * Constructor
   * @param min
   * @param num
   * @param delta
   * @param mean
   * @param stdDev
   * @param totMoRate
   * @throws DataPoint2DException
   */

  public GaussianMagFreqDist(double min,int num,double delta,double mean,double stdDev,
                             double totMoRate) throws DataPoint2DException{
    super(min,num,delta);
    this.mean=mean;
    this.stdDev=stdDev;
    this.totMoRate=totMoRate;
    computeRates();
  }


  /**
   * Constructor
   * @param min
   * @param num
   * @param delta
   * @param mean
   * @param stdDev
   * @param totMoRate
   * @param truncLevel
   * @param truncType
   */
  public GaussianMagFreqDist(double min,int num,double delta,double mean,double stdDev,
                             double totMoRate,double truncLevel,int truncType) throws DataPoint2DException{
    super(min,num,delta);
    this.mean=mean;
    this.stdDev=stdDev;
    this.totMoRate=totMoRate;
    this.truncLevel=truncLevel;
    this.truncType = truncType;
    computeRates();
  }

  /**
   * Constructor
   * @param min
   * @param max
   * @param num
   * @param mean
   * @param stdDev
   * @param totMoRate
   * @param truncLevel
   * @param truncType
   */
  public GaussianMagFreqDist(double min,double max,int num,double mean,double stdDev,
                             double totMoRate,double truncLevel,int truncType) throws DataPoint2DException{
    super(min,max,num);
    this.mean=mean;
    this.stdDev=stdDev;
    this.totMoRate=totMoRate;
    this.truncLevel=truncLevel;
    this.truncType = truncType;
    computeRates();
  }


  /**
   * get the mean for this distribution
   * @return
   */
  public double getMean() {
    return this.mean;
  }


  /**
   * get the stdDev for this distribution
   * @return
   */
  public double getStdDev() {
    return this.stdDev;
  }


  /**
   * get the TotalMoRate of this distribution
   * @return
   */
  public double getTotMoRate() {
    return this.totMoRate;
  }


  /**
   * get the truncLevel which specifies the # of stdDev(from Mean) where the dist. cuts to zero.
   * @return
   */
  public double getTruncLevel() {
    return this.truncLevel;
  }


  /**
   * get the truncType which specifies whether it is no truncation  or 1 sided or 2 sided truncation
   * @return
   */
  public int getTruncType() {
   return this.truncType;
  }

  /**
   * returns the name of the class invoked by the user
   * @return
   */
  public String getName() {
    return NAME;
  }


  /**
   * return the info stored in the class in form of a String
   * @return
   */
  public String getInfo() {

    return "mean="+this.mean+";stdDev="+this.stdDev+";totMoRate="+this.totMoRate+";truncType="+
           this.truncType+";truncLevel="+this.truncLevel;

  }


  /**
   * All the set functions below call the computeRate() method to calulates the rates and set them
   * as the Y-axis values based X-axis data provided by the user in form of mag.
   */

  /**
   * sets the mean for this distribution
   * @param mean
   */
  public void setMean(double mean) throws DataPoint2DException{
    this.mean=mean;
    computeRates();
  }

  /**
   * set the StdDev for this distribution
   * @param stdDev
   */
  public void setStdDev(double stdDev) throws DataPoint2DException{
    this.stdDev=stdDev;
    computeRates();
  }

  /**
   * sets the totMoRate for this distribution
   * @param totMoRate
   */
  public void setTotMoRate(double totMoRate) throws DataPoint2DException{
    this.totMoRate=totMoRate;
    computeRates();
  }


  /**
   * sets the truncLevel(which is the number of the stdDev from mean where dist. cut to zero
   * and truncType (which specifies no truncation,1 sided truncation or both side truncation)
   * @param truncLevel
   * @param truncType
   */
  public void setTruncLevelAndType(double truncLevel,int truncType) throws DataPoint2DException{
    this.truncLevel=truncLevel;
    this.truncType=truncType;
    computeRates();
  }


  /**
   * sets the mean and totMoRate for this distribution
   * @param mean
   * @param totMoRate
   */
  public void setMeanAndTotalMomentRate(double mean,double totMoRate) throws DataPoint2DException{
    setMean(mean);
    setTotMoRate(totMoRate);
    computeRates();
  }


  /**
   * set the mean ,stdDev,totMoRate for this distribution.
   * if no truncType is specified then it takes default as zero.
   * @param mean
   * @param stdDev
   * @param totMoRate
   */
  public void setAll(double mean,double stdDev,double totMoRate) throws DataPoint2DException{
    setMean(mean);
    setStdDev(stdDev);
    setTotMoRate(totMoRate);
    computeRates();
  }


  /**
   * sets the mean,stdDev,totMoRate,TruncLevel and truncType for this distribution
   * @param mean
   * @param stdDev
   * @param totMoRate
   * @param truncLevel
   * @param truncType
   */
  public void setAll(double mean,double stdDev,double totMoRate,double truncLevel,
                     int truncType) throws DataPoint2DException{
    setMean(mean);
    setStdDev(stdDev);
    setTotMoRate(totMoRate);
    setTruncLevelAndType(truncLevel,truncType);
    computeRates();
  }


  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the GaussianMagFreqDist class and calling the set functions of this from outside
   * @param point
   * @throws MagFreqDistException
   */
  public void set(DataPoint2D point) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the GaussianMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the GaussianMagFreqDist class and calling the set functions of this from outside
   * @param x
   * @param y
   * @throws MagFreqDistException
   */
  public void set(double x,double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the GaussianMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the GaussianMagFreqDist class and calling the set functions of this from outside
   * @param index
   * @param y
   * @throws MagFreqDistException
   */
  public void set(int index,double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the GaussianMagFreqDist from outside this class");
  }

  /**
   * This functions call the method set(int,double) in the EvenlyDiscretized class
   * to set the y-axis values based on the x-axis data provided by the user,in form
   * of the mag,mean stdDev. it then sets up the rate as the Y-axis values.
   * Based on the truncType it sets the rate to be zero after setting the
   * truncLevel(which specifies the # of stdDev from mean where dist. cut to zero
   */
  private void computeRates()throws DataPoint2DException {
    for(int i=0;i<num;++i) {
      double mag=getX(i);
      double rate = Math.exp(-Math.pow((mag - this.mean),2)/(2*stdDev*stdDev));
      super.set(i,rate);
    }

    if(truncType !=0) {
      double magUpper = this.mean + this.truncLevel*this.stdDev;
      int index=(int)Math.ceil((magUpper -this.minX)/this.delta);
      for(int i=index;i<this.num;i++)
        super.set(i,0);
    }

    if(truncType ==2) {
      double magLower = this.mean - this.truncLevel *this.stdDev;
      int index=(int) Math.floor((magLower-this.minX)/this.delta);
      for(int i=0;i<=index;i++)
        super.set(i,0);
    }

//    normalizeByTotalRate();
    this.scaleToTotalMomentRate(this.totMoRate);
  }
}