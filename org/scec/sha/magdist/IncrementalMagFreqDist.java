package org.scec.sha.magdist;

import org.scec.data.function.*;
import org.scec.data.DataPoint2D;
import org.scec.exceptions.MagFreqDistException;
import org.scec.exceptions.DataPoint2DException;

/**
 * <p>Title:IncrementalMagFreqDist </p>
 * <p>Description:This class give the rate of earthquakes (number per year) in succesion</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta Date:July,26,2002
 * @version 1.0
 */

public class IncrementalMagFreqDist extends EvenlyDiscretizedFunc implements IncrementalMagFreqDistAPI {




    /**
     * todo constructors
     * @param min
     * @param num
     * @param delta
     * using the parameters we call the parent class constructors to initialise the parent class variables
     */
    public IncrementalMagFreqDist(double min,int num,double delta){
     super(min,num,delta);
     setTolerance(delta/1000000);
    }

    /**
     * todo constructors
     * @param min
     * @param max
     * @param num
     * using the min, max and num we calculate the delta
     */
    public IncrementalMagFreqDist(double min,double max,int num) {
      super(min,max,num);
      setTolerance(delta/1000000);
   }


   /**
    * This function finds IncrRate for the given magnitude
    * @param mag
    * @return
    */
    public double getIncrRate(double mag) {
         int xIndex = getXIndex(mag);
         return getIncrRate(xIndex);
    }


    /**
     * This function finds the IncrRate at the given index
     * @param index
     * @return
     */
    public double getIncrRate(int index) {
        return getY(index);
    }


      /**
       * This function finds the cumulative Rate at a specified magnitude
       * @param mag
       * @return
       */
    public double getCumRate(double mag) {
        int index=getXIndex(mag);
        return getCumRate(index);
    }


    /**
     * This function finds the cumulative Rate at a specified index
     * @param index
     * @return
     */

    public double getCumRate(int index) {
        double sum=0.0;
        for(int i=index;i<num;++i)
            sum+=getIncrRate(i);
        return sum;
    }



    /**
     * This function finds the moment Rate at a specified magnitude
     * @param mag
     * @return
     */

    public double getMomentRate(double mag) {
      return getIncrRate(mag) *(Math.pow(10,1.5*mag+9.05));
    }


    /**
     * This function finds the moment Rate at a specified index
     * @param index
     * @return
     */

    public double getMomentRate(int index) {
      double mag = getX(index);
      return getIncrRate(index) *(Math.pow(10,1.5*mag+9.05));
    }



    /**
     * This function return the sum of all the moment rates as a double variable
     * @return
     */

    public double getTotalMomentRate() {
      double sum=0.0;
      for(int i=0;i<num;++i)
         sum+=getMomentRate(i);
      return sum;
    }


    /**
     * This function returns the sum of all the incremental rate as the double varibale
     * @return
     */

    public double getTotalIncrRate() {
      double sum=0.0;
      for(int i=0;i<num;++i)
         sum+=getIncrRate(i);
      return sum;
    }

    /**
     * This function normalises the values of all the Incremental rate at each point, by dividing each one
     * by the totalIncrRate, so that after normalization the sum addition of all incremental rate at each point
     * comes to be 1.
     */

    public void normalizeByTotalRate() {
      double totalIncrRate=getTotalIncrRate();
      for(int i=0;i<num;++i) {
          double newRate= getIncrRate(i)/totalIncrRate;
          super.set(i,newRate);
      }
    }



     /**
      * This returns the object of the class EvenlyDiscretizedFunc which contains all the points
      * with Cum Rate Distribution
      * @return
      */

    public EvenlyDiscretizedFunc getCumRateDist() {
      EvenlyDiscretizedFunc cumRateDist = new EvenlyDiscretizedFunc(minX,num,delta);
      double sum=0.0;
      for(int i=num-1;i>=0;--i) {
         sum+=getIncrRate(i);
         cumRateDist.set(i,sum);
      }
      cumRateDist.setInfo(this.getInfo());
      cumRateDist.setName(this.getName());
      return cumRateDist;
    }


    /**
     * This returns the object of the class EvenlyDiscretizedFunc which contains all the points
     * with Moment Rate Distribution
     * @return
     */

    public EvenlyDiscretizedFunc getMomentRateDist() {
        EvenlyDiscretizedFunc momentRateDist = new EvenlyDiscretizedFunc(minX,num,delta);
        for(int i=num-1;i>=0;--i) {
            momentRateDist.set(i,getMomentRate(i));
        }
        momentRateDist.setInfo(this.getInfo());
        momentRateDist.setName(this.getName());
        return momentRateDist;
    }



    /**
     * Using this function each data point is scaled to ratio of specified newTotalMomentRate
     * and oldTotalMomentRate.
     * @param newTotMoRate
     */

    public void scaleToTotalMomentRate(double newTotMoRate) {
        double oldTotMoRate=getTotalMomentRate();
        double scaleRate=newTotMoRate/oldTotMoRate;
        for(int i=0;i<num;++i) {
            super.set(i,scaleRate*getIncrRate(i));
        }

    }


    /**
     * Using this function each data point is scaled to the ratio of the CumRate at a given
     * magnitude and the specified rate.
     * @param mag
     * @param rate
     */

    public void scaleToCumRate(double mag,double rate) {
        int index = getXIndex(mag);
        scaleToCumRate(index,rate);
    }



    /**
     * Using this function each data point is scaled to the ratio of the CumRate at a given
     * index and the specified rate
     * @param index
     * @param rate
     */

   public void scaleToCumRate(int index,double rate) {
        double temp=getCumRate(index);
        double scaleCumRate=rate/temp;
        for(int i=0;i<num;++i)
            super.set(i,scaleCumRate*getIncrRate(i));
   }



   /**
    * Using this function each data point is scaled to the ratio of the IncrRate at a given
    * magnitude and the specified newRate
    * @param mag
    * @param newRate
    */

    public void scaleToIncrRate(double mag, double newRate) {
        int index = getXIndex(mag);
        scaleToIncrRate(index,newRate);
    }


    /**
     * Using this function each data point is scaled to the ratio of the IncrRate at a given
     * index and the specified newRate
     * @param index
     * @param newRate
     */

    public void scaleToIncrRate(int index, double newRate) {
        double temp=getIncrRate(index);
        double scaleIncrRate=newRate/temp;
        for(int i=0;i<num;++i)
            super.set(i,scaleIncrRate*getIncrRate(i));
    }


    /** Returns a copy of this and all points in this DiscretizedFunction */
   public DiscretizedFuncAPI deepClone(){

       IncrementalMagFreqDist f = new IncrementalMagFreqDist(
           minX, num, delta
       );

       f.info = info;
       f.minX = minX;
       f.maxX = maxX;
       f.name = name;
       f.tolerance = tolerance;
       f.setInfo(this.getInfo());
       f.setName(this.getName());
       for(int i = 0; i<num; i++)
           f.set(i, points[i]);

       return f;
   }

}