package org.scec.sha.magdist;

import org.scec.data.function.EvenlyDiscretizedFunc;

/**
 * <p>Title:IncrementalMagFreqDist </p>
 * <p>Description:This class give the rate of earthquakes (number per year) in succesion</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class IncrementalMagFreqDist extends EvenlyDiscretizedFunc implements IncrementalMagFreqDistAPI {

  /**
   * class variables
   * The two variables defined are the instances of the class EvenlyDiscretized
   */
    EvenlyDiscretizedFunc  cumRateDist;
    EvenlyDiscretizedFunc momentRateDist;

    /*
     These constructors call the functions setMomentRateDist and setCumRateDist which initialise
     the objects of the class EvenlydiscretizedFunc with the Moment Rate distribution
     and Cumulative Rate Distribution respectively for all data points.
    */


    /**
     * todo constructors
     * @param min
     * @param num
     * @param delta
     * using the parameters we call the parent class constructors to initialise the parent class variables
     */
    public IncrementalMagFreqDist(double min,int num,double delta){
     super(min,num,delta);
     momentRateDist=new EvenlyDiscretizedFunc(min,num,delta);
     cumRateDist =new EvenlyDiscretizedFunc(min,num,delta);
     setMomentRateDist();
     setCumRateDist();
    }

    /**
     * todo constructors
     * @param min
     * @param max
     * @param num
     * using the min, max and num we calculate the delta
     */
    public IncrementalMagFreqDist(double min,double max,int num) {
      super(min,num,(max-min)/num);
      double delta= (max-min)/num;
      momentRateDist=new EvenlyDiscretizedFunc(min,num,delta);
      cumRateDist =new EvenlyDiscretizedFunc(min,num,delta);
      setMomentRateDist();
      setCumRateDist();
   }

   /**
    * this function is only called once when new objects of this class are made, then it will make a new
    * copy of the EvenlyDiscretizedFunc class object momoentRateDist, which is initialised using this method call in
    * the constructor of the class.
    */
    private void setMomentRateDist() {

      for(int i=0;i<num;++i) {
        double x=momentRateDist.getX(i);
        double y=getIncrRate(x) *(Math.pow(10,1.5*x+9.05));
        momentRateDist.set(i,y);
      }
    }

    /**
    * this function is only called once when new objects of this class are made, then it will make a new
    * copy of the EvenlyDiscretizedFunc class object cumRateDist, which is initialised using this method call in
    * the constructor of the class.
    */

    private void setCumRateDist() {

      for(int i=0;i<num;++i){
         double x= cumRateDist.getX(i);
         double y=getCumRate(x);
         cumRateDist.set(i,y);
      }
    }

    /**
     * This function finds IncrRate for the given magnitude
     * @param mag
     * @return
     */

    public double getIncrRate(double mag) {
        return getIncrRate(getXIndex(mag));
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
     * This function find the cumulative Rate till a specified magnitude
     * @param mag
     * @return
     */
    public double getCumRate(double mag) {
        int index=getXIndex(mag);
        double sum=0.0;
        for(int i=0;i<index;++i)
            sum+=getIncrRate(i);
        return sum;
    }

    /**
     * This function find the cumulative Rate till a specified index
     * @param index
     * @return
     */
    public double getCumRate(int index) {
        double sum=0.0;
        for(int i=0;i<index;++i)
            sum+=getIncrRate(i);
        return sum;
    }

    /**
     * This function return the sum of all the moment rates as a double variable
     * @return
     */
    public double getTotalMomentRate() {
      double sum=0.0;
      for(int i=0;i<num;++i)
         sum+=momentRateDist.getY(i);
      return sum;
    }

    /**
     * This function returns the sum of all the incremental rate as the double varibale
     * @return
     */
    public double getTotalIncrRate() {
      double sum=0.0;
      for(int i=0;i<num;++i)
         sum+=cumRateDist.getY(i);
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
          double y = getIncrRate(i);
          double yy= y/totalIncrRate;
          points.set(i,new Double(yy));
      }
      return;
    }

    /**
     * This returns the object of the class EvenlyDiscretizedFunc which contains all the points
     * with Incr Rate Distribution
     * @return
     */
    public EvenlyDiscretizedFunc getCumRateDist() {
      return cumRateDist;
    }

    /**
     * This returns the object of the class EvenlyDiscretizedFunc which contains all the points
     * with Moment Rate Distribution
     * @return
     */
    public EvenlyDiscretizedFunc getMomentRateDist() {
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
            double y=getIncrRate(i);
            y*=scaleRate;
            points.set(i,new Double(y));
        }

    }

    /**
     * Using this function each data point is scaled to the ratio of the CumRate at a given
     * magnitude and the specified rate.
     * @param mag
     * @param rate
     */
    public void scaleToCumRate(double mag,double rate) {
        double temp=getCumRate(mag);
        double scaleCumRate=rate/temp;
        for(int i=0;i<num;++i) {
            double y=getCumRate(i);
            y*=scaleCumRate;
            points.set(i,new Double(y));
        }
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
        for(int i=0;i<num;++i) {
            double y=getCumRate(i);
            y*=scaleCumRate;
            points.set(i,new Double(y));
        }
   }


   /**
    * Using this function each data point is scaled to the ratio of the IncrRate at a given
    * magnitude and the specified newRate
    * @param mag
    * @param newRate
    */
    public void scaleToIncrRate(double mag, double newRate) {
        double temp=getIncrRate(mag);
        double scaleIncrRate=newRate/temp;
        for(int i=0;i<num;++i) {
            double y=getIncrRate(i);
            y*=scaleIncrRate;
            points.set(i,new Double(y));
        }
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
        for(int i=0;i<num;++i) {
            double y=getIncrRate(i);
            y*=scaleIncrRate;
            points.set(i,new Double(y));
        }
    }
}