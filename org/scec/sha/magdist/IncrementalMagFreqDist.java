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



    public IncrementalMagFreqDist(double min,int num,double delta){
     super(min,num,delta);
     momentRateDist=new EvenlyDiscretizedFunc(min,num,delta);
     cumRateDist =new EvenlyDiscretizedFunc(min,num,delta);
     setMomentRateDist();
     setCumRateDist();
    }

    public IncrementalMagFreqDist(double min,double max,int num) {
      super(min,num,(max-min)/num);
      double delta= (max-min)/num;
      momentRateDist=new EvenlyDiscretizedFunc(min,num,delta);
      cumRateDist =new EvenlyDiscretizedFunc(min,num,delta);
      setMomentRateDist();
      setCumRateDist();
   }

    private void setMomentRateDist() {

      for(int i=0;i<num;++i) {
        double x=momentRateDist.getX(i);
        double y=getIncrRate(x) *(Math.pow(10,1.5*x+9.05));
        momentRateDist.set(i,y);
      }
    }

    private void setCumRateDist() {

      for(int i=0;i<num;++i){
         double x= cumRateDist.getX(i);
         double y=getCumRate(x);
         cumRateDist.set(i,y);
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
      for(int i=0;i<num;++i)
         sum+=momentRateDist.getY(i);
      return sum;
    }

    public double getTotalIncrRate() {
      double sum=0.0;
      for(int i=0;i<num;++i)
         sum+=cumRateDist.getY(i);
      return sum;
    }

    public void normalizeByTotalRate() {
      double totalIncrRate=getTotalIncrRate();
      for(int i=0;i<num;++i) {
          double y = getIncrRate(i);
          double yy= y/totalIncrRate;
          points.set(i,new Double(yy));
      }
      return;
    }

    public EvenlyDiscretizedFunc getCumRateDist() {
      return cumRateDist;
    }

    public EvenlyDiscretizedFunc getMomentRateDist() {
      return momentRateDist;
    }

    public void scaleToTotalMomentRate(double newTotMoRate) {
        double oldTotMoRate=getTotalMomentRate();
        double scaleRate=newTotMoRate/oldTotMoRate;
        for(int i=0;i<num;++i) {
            double y=getIncrRate(i);
            y*=scaleRate;
            points.set(i,new Double(y));
        }

    }

    public void scaleToCumRate(double mag,double rate) {
        double temp=getCumRate(mag);
        double scaleCumRate=rate/temp;
        for(int i=0;i<num;++i) {
            double y=getCumRate(i);
            y*=scaleCumRate;
            points.set(i,new Double(y));
        }
    }

   public void scaleToCumRate(int index,double rate) {
        double temp=getCumRate(index);
        double scaleCumRate=rate/temp;
        for(int i=0;i<num;++i) {
            double y=getCumRate(i);
            y*=scaleCumRate;
            points.set(i,new Double(y));
        }
   }

    public void scaleToIncrRate(double mag, double newRate) {
        double temp=getIncrRate(mag);
        double scaleIncrRate=newRate/temp;
        for(int i=0;i<num;++i) {
            double y=getIncrRate(i);
            y*=scaleIncrRate;
            points.set(i,new Double(y));
        }
    }

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