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

public class IncrementalMagFreqDist extends EvenlyDiscretizedFunc
             implements IncrementalMagFreqDistAPI {


    EvenlyDiscretizedFunc  cumRateDist;
    EvenlyDiscretizedFunc momentRateDist;

    public IncrementalMagFreqDist(double min,int num,double delta){
        super(min,num,delta);

    }

    public IncrementalMagFreqDist(double min,double max,int num) {
      super.minY=min;
      super.num=num;
      super.maxY=max;

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
        for(int i=0;i<index;++i)
            sum+=getIncrRate(i);
        return sum;
    }

    public double getCumRate(int index) {
        double sum=0.0;
        for(int i=0;i<index;++i)
            sum+=getIncrRate(i);
        return sum;
    }

    public double getTotalMomentRate() {
      double sum=0.0;
      for(int i=0;i<num;++i) {
          double x=getX(i);
          double rate=getIncrRate(x);
          sum+=rate*Math.pow(10,1.5*x+9.05);
      }
      return sum;
    }

    public double getTotalIncrRate() {
      double sum=0.0;
      for(int i=0;i<num;++i) {
          double x=getX(i);
          double rate=getIncrRate(x);
          sum+=rate;
      }
      return sum;
    }

    public void normalizeByTotalRate() {
      for(int i=0;i<num;++i) {
          double y = getIncrRate(i);
          double yy= y/getTotalIncrRate();
      }
      return;
    }

    public double CumulativeDist() {

    }

    public double getMomentRateDist() {
    }

    public void scaleToTotalMomentRate(double newTotMoRate) {
        double oldTotMoRate=getTotalMomentRate();
        double scaleRate=newTotMoRate/oldTotMoRate;
        for(int i=0;i<num;++i) {
            double y=getIncrRate(i);
            y*=scaleRate;
        }

    }

    public void scaleToCumRate(double mag,double rate) {
        double temp=getCumRate(mag);
        double scaleCumRate=rate/temp;
        for(int i=0;i<num;++i) {
            double y=getCumRate(i);
            y*=scaleCumRate;
        }
    }

   public void scaleToCumRate(int index,double rate) {
        double temp=getCumRate(index);
        double scaleCumRate=rate/temp;
        for(int i=0;i<num;++i) {
            double y=getCumRate(i);
            y*=scaleCumRate;
        }
   }

    public void scaleToIncrRate(double mag, double newRate) {
        double temp=getIncrRate(mag);
        double scaleIncrRate=temp/newRate;
        for(int i=0;i<num;++i) {
            double y=getIncrRate(i);
            y*=scaleIncrRate;
        }
    }

    public void scaleToIncrRate(int index, double newRate) {
        double temp=getIncrRate(index);
        double scaleIncrRate=temp/newRate;
        for(int i=0;i<num;++i) {
            double y=getIncrRate(i);
            y*=scaleIncrRate;
        }
    }
}