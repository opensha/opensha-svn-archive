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

public class IncrementalMagFreqDist extends EvenlyDiscretizedFunc {


    EvenlyDiscretizedFunc  cumRateDist;
    EvenlyDiscretizedFunc momentRateDist;

    protected double min=Double.NaN;
    protected double max=Double.NaN;
    protected int num;
    protected double delta=Double.NaN;


    public IncrementalMagFreqDist(double min,int num,double delta){
      this.min=min;
      this.num=num;
      this.delta=delta;
    }

    public IncrementalMagFreqDist(double min,double max,int num) {
      this.min=min;
      this.num=num;
      this.max=max;
    }

    private void setMomentRateDist() {
      momentRateDist=new EvenlyDiscretizedFunc(this.min,this.num,this.delta);
      for(int i=0;i<num;++i) {
        double x=momentRateDist.getX(i);
      /*  Object obj=points.get(i);
        if(obj==null) {
          double y=Double.NaN;
        }
        else {
          double y=((Double)obj).doubleValue();
        }*/
         double y=getIncrRate(x) *(Math.pow(10,1.5*x+9.05));
         points.set(i,new Double(y));
      }
    }

    private void setCumRateDist() {
      cumRateDist =new EvenlyDiscretizedFunc(this.min,this.num,this.delta);
      for(int i=0;i<num;++i){
         double x= cumRateDist.getX(i);
       /*  Object obj=points.get(i);
         if(obj==null) {
           double y=Double.NaN;
         }
         else {
           double y=((Double)obj).doubleValue();
         } */

         double y=getIncrRate(x);
         points.set(i,new Double(y));
      }
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

    public EvenlyDiscretizedFunc getCumRateDist() {
      setCumRateDist();
      return cumRateDist;
    }

    public EvenlyDiscretizedFunc getMomentRateDist() {
       setMomentRateDist();
       return momentRateDist;
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