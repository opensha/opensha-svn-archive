package org.scec.sha.magdist;

import org.scec.exceptions.*;
import org.scec.data.DataPoint2D;



/**
 * <p>Title: YC_1985_CharMagFreqDist.java </p>
 *
 * <p>Description: This is the "characteristic" magnitude-frequency distribution
 * defined by Youngs and Coppersmith (1985, Bull. Seism. Soc. Am., 939-964).
 * The distribution is Gutenberg-Richter between magLower and magPrime, and
 * constant between (magUpper-deltaMagChar) and magUpper with a rate equal to
 * that of the Gutenberg-Richter part at (magPrime-deltaMagPrime).
 * See their figure 10 for a graphical explanation of these parameters.
 * Note that magLower, magUpper, magPrime, magUpper-deltaMagChar, and
 * magPrime-deltaMagPrime must all be exactly equal one of the descrete x-axis points. </p>
 *
 * @author Edward H. Field   Date: Sept. 26, 2002
 * @version 1.0
 */


public class YC_1985_CharMagFreqDist extends IncrementalMagFreqDist {

  private String C = new String("YC_1985_CharMagFreqDist"); // for showing messages
  public static String NAME = new String("Youngs and Coppersmith Dist.");
  private double magLower;
  private double magUpper;
  private double deltaMagChar;
  private double magPrime;
  private double deltaMagPrime;
  private double bValue;


  /**
    * constructor : this is same as parent class constructor
    */
   public YC_1985_CharMagFreqDist(double min,int num,double delta){
     super(min,num,delta);
   }


   /**
    * constructor: this is sameas parent class constructor
    */

   public YC_1985_CharMagFreqDist(double min,double max,int num) {
     super(min,max,num);
   }


   /**
    * constructor: this constructor assumes magLower is minX and
    *               magUpper to be maxX
    */
   public YC_1985_CharMagFreqDist(double min,int num,double delta, double deltaMagChar, double magPrime,
                              double deltaMagPrime, double bValue, double totMoRate)
                              throws DataPoint2DException {
     super(min,num,delta);
     // assumes magLower = minX and magUpper = maxX
     magLower=minX;
     magUpper=maxX;

     this.deltaMagChar = deltaMagChar;
     this.magPrime = magPrime;
     this.deltaMagPrime = deltaMagPrime;
     this.bValue = bValue;

     calculateRelativeRates();
     scaleToTotalMomentRate(totMoRate);
   }


   /**
    * constructor: this is the full constructor
    */

   public YC_1985_CharMagFreqDist(double min,int num,double delta, double magLower,
                              double magUpper, double deltaMagChar, double magPrime,
                              double deltaMagPrime, double bValue, double totMoRate)
                              throws DataPoint2DException {
     super(min,num,delta);

     this.magLower = magLower;
     this.magUpper = magUpper;
     this.deltaMagChar = deltaMagChar;
     this.magPrime = magPrime;
     this.deltaMagPrime = deltaMagPrime;
     this.bValue = bValue;

     calculateRelativeRates();
     scaleToTotalMomentRate(totMoRate);
   }



   /**
    * Set all values
    */

   public void setAll(double magLower, double magUpper, double deltaMagChar,
                      double magPrime, double deltaMagPrime, double bValue,
                      double totMoRate) throws DataPoint2DException {

        this.magLower = magLower;
        this.magUpper = magUpper;
        this.deltaMagChar = deltaMagChar;
        this.magPrime = magPrime;
        this.deltaMagPrime = deltaMagPrime;
        this.bValue = bValue;

        calculateRelativeRates();
        scaleToTotalMomentRate(totMoRate);
   }





  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of this class and calling the set functions of this from outside
   * @param point
   * @throws MagFreqDistException
   */
  public void set(DataPoint2D point) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the YC_1985_CharMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of this class and calling the set functions of this from outside
   * @param x
   * @param y
   * @throws MagFreqDistException
   */
  public void set(double x,double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the YC_1985_CharMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of this class and calling the set functions of this from outside.
   * @param index
   * @param y
   * @throws MagFreqDistException
   */
  public void set(int index,double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the YC_1985_CharMagFreqDist from outside this class");
  }


  /**
    * private function to set the rate values
    */

  private void calculateRelativeRates() throws DataPoint2DException {


    // checks that magUpper, magLower, magPrime, deltaMagPrime, and deltaMagChar
    // are well posed.
    if( deltaMagChar < 0 )
        throw new InvalidRangeException("deltaMagChar must be positive");
    if( deltaMagPrime < 0 )
        throw new InvalidRangeException("deltaMagPrime must be positive");
    if(magLower < minX || magLower > maxX)
        throw new DataPoint2DException("magLower should lie between minX and maxX");
    if(magLower > magUpper)
        throw new InvalidRangeException("magLower cannot be less than magUpper");
    if(magPrime > magUpper || magPrime < magLower)
        throw new InvalidRangeException("magPrime must be between magLower and magUpper");
    if( (magPrime-deltaMagPrime) < magLower)
        throw new InvalidRangeException("magPrime-deltaMagPrime must be greater than magLower");
    if( deltaMagChar > (magUpper-magPrime+deltaMagPrime) )
        throw new InvalidRangeException("deltaMagChar > (magUpper-magPrime+deltaMagPrime), which is not allowed");
    if( magPrime > (magUpper-deltaMagChar) )
        throw new InvalidRangeException("magPrime > (magUpper-deltaMagChar), which is not allowed");


    double magForRate = magPrime - deltaMagPrime;

    int indexLower     = getXIndex(magLower);
    int indexUpper     = getXIndex(magUpper);
    int indexMagPrime  = getXIndex(magPrime);
    int indexForRate   = getXIndex(magForRate);
    int indexCharStart = getXIndex(magUpper-deltaMagChar);

    int i;

    for(i=0;i<num;++i) // initialize all rates to 0
       super.set(i,0.0);

    for(i=indexLower;i<=indexMagPrime;++i) // assign correct values to rates between magLower and magPrime
       super.set(i,Math.pow(10,-bValue*getX(i)));

    for(i=indexCharStart;i<=indexUpper;++i) // set rates over the characteristic-mag range
       super.set(i, Math.pow(10,-bValue*magForRate));

  }


  /**
   *
   * @returns the cumulative rate at magLower
   */

  public double getTotCumRate() {
    return getCumRate(magLower);
  }



  /**
   * @returns the bValue for this distribution
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
   * @returns the magUpper : highest magnitude that has non zero rate
   */
  public double getMagUpper() {
    return magUpper;
  }

  /**
   *
   * @returns the magPrime
   */
  public double getMagPrime() {
    return magPrime;
  }

  /**
   *
   * @returns the deltaMagPrime
   */
  public double getDeltaMagPrime() {
    return deltaMagPrime;
  }

  /**
   *
   * @returns the deltaMagChar
   */
  public double getDeltaMagChar() {
    return deltaMagChar;
  }

  /**
   * returns the name of this class
   * @return
   */

  public String getName() {
     return NAME;
  }



  /**
   * this function returns String for drawing Legen in JFreechart
   * @return : returns the String which is needed for Legend in graph
   */
  public String getInfo() {
    return (";magLower="+magLower+";magUpper="+magUpper+";deltaMagChar="+this.getDeltaMagChar()+
        ";magPrime="+this.getMagPrime()+";deltaMagPrime="+getDeltaMagPrime()+
        "bValue="+bValue+";totMoRate="+(float)this.getTotalMomentRate()+
        ";totCumRate="+(float)getCumRate(magLower));

  }

  /**
   * this function is for setting the name
   **/

  public void setName(String name) {
    throw new UnsupportedOperationException(C+"::setName not allowed for MagFreqDist.");

  }

  /**
   * this function is for setting the info
   **/
  public void setInfo(String info) {
    throw new UnsupportedOperationException(C+"::::setInfo not allowed for MagFreqDist.");

  }

}
