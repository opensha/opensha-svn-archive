package org.scec.sha.magdist;

/**
 *
 * <p>Title: IncrementalMagFreqDistAPI</p>
 * <p>Description:  thjis Class gives the rate of EarthQuakes(number per year)in  succession</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface IncrementalMagFreqDistAPI {


      public double getIncrRate(double mag) ;

      public double getIncrRate(int index) ;

      public double getCumRate(double mag) ;

      public double getCumRate(int index) ;

      public double getTotalMomentRate();

      public double getTotalIncrRate();

      public void normalizeByTotalRate();

      public double CumulativeDist() ;
      public double getMomentRateDist() ;

      public void scaleToCumRate(double mag,double rate);

      public void scaleToCumRate(int index,double rate);

      public void scaleToIncrRate(double mag, double newRate) ;

      public void scaleToIncrRate(int index, double newRate);

}