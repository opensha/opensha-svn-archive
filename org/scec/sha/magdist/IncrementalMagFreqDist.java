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

   }


    public double getIncrRate(double mag) {
        return getIncrRate(getXIndex(mag));
    }


    public double getIncrRate(int index) {
        return getY(index);
    }


    public double getCumRate(double mag) {
        int index=getXIndex(mag);
        double sum=0.0;
        for(int i=index;i>=0;--i)
            sum+=getIncrRate(i);
        return sum;
    }

    public double getCumRate(int index) {
        double sum=0.0;
        for(int i=index;i>=0;--i)
            sum+=getIncrRate(i);
        return sum;
    }


    public double getMomentRate(double mag) {
      return getIncrRate(mag) *(Math.pow(10,1.5*mag+9.05));
    }


    public double getMomentRate(int index) {
      double mag = getX(index);
      return getIncrRate(index) *(Math.pow(10,1.5*mag+9.05));
    }


    public double getTotalMomentRate() {
      double sum=0.0;
      for(int i=0;i<num;++i)
         sum+=getMomentRate(i);
      return sum;
    }


    public double getTotalIncrRate() {
      double sum=0.0;
      for(int i=0;i<num;++i)
         sum+=getIncrRate(i);
      return sum;
    }


    public void normalizeByTotalRate() {
      double totalIncrRate=getTotalIncrRate();
      for(int i=0;i<num;++i) {
          double newRate= getIncrRate(i)/totalIncrRate;
          set(i,newRate);
      }
    }


        public EvenlyDiscretizedFunc getCumRateDist() {
        EvenlyDiscretizedFunc cumRateDist = new EvenlyDiscretizedFunc(minX,num,delta);
        double sum=0.0;
        for(int i=num-1;i>=0;--i) {
            sum+=getIncrRate(i);
            cumRateDist.set(i,sum);
        }
        return cumRateDist;
    }


    public EvenlyDiscretizedFunc getMomentRateDist() {
        EvenlyDiscretizedFunc momentRateDist = new EvenlyDiscretizedFunc(minX,num,delta);
        for(int i=num-1;i>=0;--i) {
            momentRateDist.set(i,getMomentRate(i));
        }
        return momentRateDist;
    }


    public void scaleToTotalMomentRate(double newTotMoRate) {
        double oldTotMoRate=getTotalMomentRate();
        double scaleRate=newTotMoRate/oldTotMoRate;
        for(int i=0;i<num;++i) {
            set(i,scaleRate*getIncrRate(i));
        }

    }


    public void scaleToCumRate(double mag,double rate) {
        int index = getXIndex(mag);
        scaleToCumRate(index,rate);
    }


   public void scaleToCumRate(int index,double rate) {
        double temp=getCumRate(index);
        double scaleCumRate=rate/temp;
        for(int i=0;i<num;++i)
            set(i,scaleCumRate*getCumRate(i));
   }


    public void scaleToIncrRate(double mag, double newRate) {
        int index = getXIndex(mag);
        scaleToIncrRate(index,newRate);
    }


    public void scaleToIncrRate(int index, double newRate) {
        double temp=getIncrRate(index);
        double scaleIncrRate=newRate/temp;
        for(int i=0;i<num;++i)
            set(i,scaleIncrRate*getIncrRate(i));
    }
}