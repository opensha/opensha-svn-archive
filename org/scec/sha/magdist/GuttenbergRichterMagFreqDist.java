package org.scec.sha.magdist;

import org.scec.exceptions.*;
import org.scec.data.DataPoint2D;



/**
 * <p>Title: GuttenbergRichterMagFreqDist.java </p>
 * <p>Description: This is incremental Guttenberg-Richter distribution</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vipin Gupta   Date: Aug 8, 2002
 * @version 1.0
 */


public class GuttenbergRichterMagFreqDist extends IncrementalMagFreqDist {


  private String C = new String("GuttenbergRichterMagFreqDist"); // for showing messages

  private double magLower ; // lowest magnitude that has non zero rate
  private double magUpper ; // highest magnitude that has non zero rate
  private double aValue;    // the a value: NOT USED ANYWHERE PRESENTLY
  private double bValue;    // the b value


  /**
    * constructor : this is same as parent class constructor
    * @param min
    * @param num
    * @param delta
    * using the parameters we call the parent class constructors to initialise the parent class variables
    */

   public GuttenbergRichterMagFreqDist(double min,int num,double delta){
     super(min,num,delta);
   }




   /**
    * constructor: this is sameas parent class constructor
    * @param min
    * @param max
    * @param num
    * using the min, max and num we calculate the delta
    */

   public GuttenbergRichterMagFreqDist(double min,double max,int num) {
     super(min,max,num);

   }




   /**
    * constructor: this constructor assumes magLower is minX and
    *               magUpper to be maxX
    * @param min
    * @param num
    * @param delta
    * @param totMoRate : total Moment Rate
    * @param bValue : b value for this distribution
    */

   public GuttenbergRichterMagFreqDist(double min,int num,double delta,
                                        double totMoRate,double bValue) throws DataPoint2DException {
     super(min,num,delta);
     // assumes magLower = minX and magUpper = maxX
     setAllButTotCumRate(minX,maxX,totMoRate,bValue);
   }



   /**
    * constructor:
    * @param min
    * @param num
    * @param delta
    * @param magLower  :  lowest magnitude that has non zero rate
    * @param magUpper  :  highest magnitude that has non zero rate
    * @param totMoRate :  total Moment Rate
    * @param bValue : b value for this distribution
    */

   public GuttenbergRichterMagFreqDist(double min,int num,double delta,
                                       double magLower, double magUpper,
                                       double totMoRate,double bValue) throws DataPoint2DException {
     super(min,num,delta);
     setAllButTotCumRate(magLower,magUpper,totMoRate,bValue);
   }


   /**
    * Set all values except Cumulative Rate
    * @param magLower  : lowest magnitude that has non zero rate
    * @param magUpper  : highest magnitude that has non zero rate
    * @param totMoRate : Total Moment Rate
    * @param bValue    : b Value
    */
   public void setAllButTotCumRate(double magLower, double magUpper,
                               double totMoRate, double bValue) throws DataPoint2DException {

       this.magLower = magLower;
       this.magUpper = magUpper;
       this.bValue = bValue;
       calculateRelativeRates();
       scaleToTotalMomentRate(totMoRate);
   }

   /**
    * Set all values except total moment rate
    * @param magLower   : lowest magnitude that has non zero rate
    * @param magUpper   : highest magnitude that has non zero rate
    * @param totCumRate : Total Cumulative Rate
    * @param bValue     : b value
    */

   public void setAllButTotMoRate(double magLower, double magUpper,
                                  double totCumRate, double bValue) throws DataPoint2DException {

       this.magLower = magLower;
       this.magUpper = magUpper;
       this.bValue = bValue;
       calculateRelativeRates();
       scaleToCumRate(magLower,totCumRate);
   }




   /**
    * Set All but magUpper
    * @param magLower      : lowest magnitude that has non zero rate
    * @param totMoRate     : total moment rate
    * @param totCumRate    : total cumulative rate
    * @param bValue        : b value
    * @param relaxCumRate  : It is "true" or "false". It accounts for tha fact
    * that due to magnitude discretization, the specified totCumRate and totMoRate
    * cannot both be satisfied simultaneously. if it is true, it means that match
    * totMoRate exactly else it matches totCumRate exactly
    */
   public void setAllButMagUpper(double magLower, double totMoRate, double totCumRate,
                                 double bValue, boolean relaxCumRate) throws DiscretizedFunction2DException {
     magUpper = maxX; // temporarily set the vale of magUpper
     calculateRelativeRates();
     scaleToCumRate(magLower,totCumRate);
     double tempTotMoRate=0.0; // initialize this temporary moment rate

     // now we need to calculate magUpper.
     // for that we need to go upto index which is closest to totMoRate
     int index;

     for(index=0;tempTotMoRate<totMoRate;++index) {
       if(index==num)
         break;
       tempTotMoRate+=getMomentRate(index);
     }

     if(index==num) { // if we reached upperMag and tempTotMoRate > totMoRate
       double diff1 = Math.abs(tempTotMoRate-totMoRate);
       if(getMomentRate(index-1)/2 < diff1)
          throw new DiscretizedFunction2DException(C+":setAllButMagUpper():"+"Invalid Total moment Rate");
     }
     else { // if tempTotMoRate < totMoRate
       double diff1 = Math.abs(tempTotMoRate-totMoRate); // for next point
       double diff2 = Math.abs(tempTotMoRate- getMomentRate(index)-totMoRate); // previous point
       // check which point is nearer
       if(diff1 < diff2) ++index;

       // we have found magUpper upto this point
       // set all rates after magUpper to be 0
       for(int i=index;i<num;++i )
          super.set(i,0.0);
     }

    magUpper = getX(index-1);
    if(relaxCumRate) // if we can relax total cum Rate, then match moment rate exactly
       scaleToTotalMomentRate(totMoRate);
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the GuttenbergRitcherMagFreqDist class and calling the set functions of this from outside
   * @param point
   * @throws MagFreqDistException
   */
  public void set(DataPoint2D point) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the GuttenbergRichterMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the GuttenbergRitcherMagFreqDist class and calling the set functions of this from outside
   * @param x
   * @param y
   * @throws MagFreqDistException
   */
  public void set(double x,double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the GuttenbergRichterMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the GuttenbergRitcherMagFreqDist class and calling the set functions of this from outside.
   * @param index
   * @param y
   * @throws MagFreqDistException
   */
  public void set(int index,double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the GuttenbergRichterMagFreqDist from outside this class");
  }


  /**
    * private function to set the rate values
    */

  private void calculateRelativeRates() throws DataPoint2DException {


    // checks that magUpper, magLower lie between minX and maxX
    // it also checks that magUpper > magLower
    if(magLower < minX || magLower > maxX)
        throw new DataPoint2DException(C+":calculateRelativeRates():"+"magLower should lie between minX and maxX");
    if(magUpper < minX || magLower > maxX)
        throw new DataPoint2DException(C+":calculateRelativeRates():"+"magUpper should lie between minX and maxX");
    if(magLower > magUpper)
        throw new InvalidRangeException(C+":calculateRelativeRates():"+"magLower cannot be less than magUpper");



    int indexLow = getXIndex(magLower); // find the index of magLower
    // make sure magLower is within tolerance of a discrete point
    if(indexLow<0)
       throw new DataPoint2DException(C+":calculateRelativeRates():"+"Invalid Mag Lower value");    // make sure magLower is within tolerance of a discrete point

    int indexUp = getXIndex(magUpper); // find the index of magUpper
    // make sure magUpper is within tolerance of a discrete point
    if(indexUp<0)
       throw new DataPoint2DException(C+":calculateRelativeRates():"+"Invalid Mag Upper value");

    int i;

    for(i=0;i<indexLow;++i) // set all rates below magLower to 0
       super.set(i,0.0);

    for(i=indexLow;i<=indexUp;++i) // assign correct values to rates between magLower and magUpper
       super.set(i,Math.pow(10,-bValue*getX(i)));

    for(i=indexUp+1;i<num;++i) // set all rates above magUpper tp 0
       super.set(i,0.0);
  }


  /**
   *
   * @returns the cumulative rate at magLower
   */

  public double getTotCumRate() {
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
   * @returns the magUpper : highest magnitude that has non zero rate
   */
  public double getMagUpper() {
    return magUpper;
  }



  /**
   * returns the name of this class
   * @return
   */

  public String getName() {
     return "GR_MagFreqDist";
  }



  /**
   * this function returns String for drawing Legen in JFreechart
   * @return : returns the String which is needed for Legend in graph
   */
  public String getInfo() {
    return ("bValue="+bValue+";magLower="+magLower+";magUpper="+magUpper +
        ";moRate="+this.getTotalMomentRate()+";totCumRate="+getCumRate(magLower));
  }

}