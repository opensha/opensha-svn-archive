package org.opensha.sha.magdist;

import org.opensha.exceptions.*;
import org.opensha.data.DataPoint2D;

/**
 * <p>Title: TaperedGR_MagFreqDist </p>
 * <p>Description: This is a tapered incremental Gutenberg-Richter distribution.</p>
 *
 * @author Edward Field
 * @version 1.0
 */


public class TaperedGR_MagFreqDist
    extends IncrementalMagFreqDist {

  public static String NAME = new String("Tapered GR Dist"); // for showing messages

  //for Debug purposes
  private boolean D = false;

  private double magLower; // lowest magnitude that has non zero rate
  private double magCorner; // the taper magnitude
  private double bValue; // the b value

  /**
   * constructor : this is same as parent class constructor
   * @param min
   * @param num
   * @param delta
   * using the parameters we call the parent class constructors to initialise the parent class variables
   */

  public TaperedGR_MagFreqDist(double min, int num, double delta) throws
      InvalidRangeException {
    super(min, num, delta);
    this.magLower = min;
  }

  /**
   * constructor: this is sameas parent class constructor
   * @param min
   * @param max
   * @param num
   * using the min, max and num we calculate the delta
   */

  public TaperedGR_MagFreqDist(double min, double max, int num) throws
      DiscretizedFuncException, InvalidRangeException {
    super(min, max, num);
    
  }

  /**
   * constructor: this is sameas parent class constructor
   * @param min
   * @param max
   * @param num
   * using the min, max and num we calculate the delta
   */

  public TaperedGR_MagFreqDist(double bValue, double totCumRate,
                                     double min, double max, int num) throws
      DiscretizedFuncException, InvalidRangeException {
    super(min, max, num);
    setAllButTotMoRate(min, max, totCumRate, bValue);
  }


  /**
   * constructor:
   * @param min
   * @param num
   * @param delta
   * @param magLower  :  lowest magnitude that has non zero rate
   * @param magCorner  :  the corner magnitude
   * @param totMoRate :  total Moment Rate
   * @param bValue : b value for this distribution
   */

  public TaperedGR_MagFreqDist(double min, int num, double delta,
                                     double magLower, double magCorner,
                                     double totMoRate, double bValue) throws
      InvalidRangeException,
      DataPoint2DException {
    super(min, num, delta);
    setAllButTotCumRate(magLower, magCorner, totMoRate, bValue);
  }

  /**
   * Set all values except Cumulative Rate
   * @param magLower  : lowest magnitude that has non zero rate
   * @param magCorner  : the corner magnitude
   * @param totMoRate : Total Moment Rate
   * @param bValue    : b Value
   */
  public void setAllButTotCumRate(double magLower, double magCorner,
                                  double totMoRate, double bValue) throws
      DataPoint2DException {

    this.magLower = magLower;
    this.magCorner = magCorner;
    this.bValue = bValue;
    calculateRelativeRates();
    scaleToTotalMomentRate(totMoRate);
  }

  /**
   * Set all values except total moment rate
   * @param magLower   : lowest magnitude that has non zero rate
   * @param magCorner   : the corner magnitude
   * @param totCumRate : Total Cumulative Rate
   * @param bValue     : b value
   */

  public void setAllButTotMoRate(double magLower, double magCorner,
                                 double totCumRate, double bValue) throws
      DataPoint2DException {

    this.magLower = magLower;
    this.magCorner = magCorner;
    this.bValue = bValue;
    calculateRelativeRates();
    scaleToCumRate(magLower, totCumRate);
  }

  /**
   * Set All but magCorner
   * @param magLower      : lowest magnitude that has non zero rate
   * @param totMoRate     : total moment rate
   * @param totCumRate    : total cumulative rate
   * @param bValue        : b value
   * @param relaxCumRate  : It is "true" or "false". It accounts for tha fact
   * that due to magnitude discretization, the specified totCumRate and totMoRate
   * cannot both be satisfied simultaneously. if it is true, it means that match
   * totMoRate exactly else it matches totCumRate exactly
   */
  public void setAllButmagCorner(double magLower, double totMoRate,
		  double totCumRate,
		  double bValue) throws
		  MagFreqDistException, DiscretizedFuncException,
		  DataPoint2DException {
	  
	  if (D) System.out.println("magLower = " + magLower);
	  if (D) System.out.println("totMoRate = " + totMoRate);
	  if (D) System.out.println("totCumRate = " + totCumRate);
	  if (D) System.out.println("bValue = " + bValue);
	  
	  throw new MagFreqDistException("Not yet implemented");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the GutenbergRitcherMagFreqDist class and calling the set functions of this from outside
   * @param point
   * @throws MagFreqDistException
   */
  public void set(DataPoint2D point) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the GutenbergRichterMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the GutenbergRitcherMagFreqDist class and calling the set functions of this from outside
   * @param x
   * @param y
   * @throws MagFreqDistException
   */
  public void set(double x, double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the GutenbergRichterMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the GutenbergRitcherMagFreqDist class and calling the set functions of this from outside.
   * @param index
   * @param y
   * @throws MagFreqDistException
   */
  public void set(int index, double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the GutenbergRichterMagFreqDist from outside this class");
  }

  /**
   * private function to set the rate values
   */

  private void calculateRelativeRates() throws DataPoint2DException {

    // checks that magCorner, magLower lie between minX and maxX
    // it also checks that magCorner > magLower
    if (magLower < minX || magLower > maxX)
      throw new DataPoint2DException(
          "magLower should lie between minX and maxX");
    if (magLower > magCorner)
      throw new InvalidRangeException("magLower must be < magCorner");

    int indexLow = getXIndex(magLower); // find the index of magLower

    int i;

    for (i = 0; i < indexLow; ++i) // set all rates below magLower to 0
      super.set(i, 0.0);

    for (i = indexLow; i <= this.getNum(); ++i) { // assign correct values to rates between magLower and magCorner
    		double rate = Math.pow(10, -bValue * getX(i)) * Math.exp(-Math.pow(10, 1.5 * (getX(i)-magCorner)));
    		super.set(i, rate);
    }

  }

  /**
   *
   * @returns the cumulative rate at magLower
   */

  public double getTotCumRate() throws DataPoint2DException {
    return getCumRate(magLower);
  }

  /**
   * @returns th bValue for this distribution
   */

  public double get_bValue() {
    return bValue;
  }

  /**
   *
   * @returns the magLower : lowest magnitude that has non zero rate
   */
  public double getMagLower() {
    return magLower;
  }

  /**
   *
   * @returns the magCorner : highest magnitude that has non zero rate
   */
  public double getmagCorner() {
    return magCorner;
  }

  /**
   * returns the name of this class
   * @return
   */

  public String getDefaultName() {
    return NAME;
  }

  /**
   * this function returns String for drawing Legen in JFreechart
   * @return : returns the String which is needed for Legend in graph
   */
  public String getDefaultInfo() throws DataPoint2DException {
    return ("minMag=" + minX + "; maxMag=" + maxX + "; numMag=" + num +
            "; bValue=" + bValue + "; magLower=" + magLower + "; magCorner=" +
            (float) magCorner +
            "; totMoRate=" + (float)this.getTotalMomentRate() + "; totCumRate=" +
            (float) getCumRate(magLower));
  }

  /** Returns a rcopy of this and all points in this GutenbergRichter */
  /*public DiscretizedFuncAPI deepClone() throws DataPoint2DException {

    GutenbergRichterMagFreqDist f = new GutenbergRichterMagFreqDist(minX, num,
        delta);
    f.setAllButTotMoRate(this.magLower, this.magCorner, this.getTotCumRate(),
                         this.bValue);
    f.tolerance = tolerance;
    return f;
  }*/

  /**
   * this method (defined in parent) is deactivated here (name is finalized)

      public void setName(String name) throws  UnsupportedOperationException{
   throw new UnsupportedOperationException("setName not allowed for MagFreqDist.");

      }


   * this method (defined in parent) is deactivated here (name is finalized)

      public void setInfo(String info)throws  UnsupportedOperationException{
   throw new UnsupportedOperationException("setInfo not allowed for MagFreqDist.");

     }*/

}
