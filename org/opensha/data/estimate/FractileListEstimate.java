package org.opensha.data.estimate;

import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: FractileListEstimate.java </p>
 * <p>Description: This estimate is a list of arbitrary points from a cumulative
 * distribution (CDF).
 *
 * The rules for this etimate are:
 * 1. 1>=y>=0
 * 2. y(i+1)>=y(i)
 * 3. To ensure that median is available:
 *    If number of values==1, ensure that y =  0.5
 *    If number of values > 1, first_y<=0.5 and last_y>=0.5
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field, Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class FractileListEstimate extends Estimate {
  public final static String NAME  =  "Fractile List";
  private final static double tol = 1e-6;
  private DiscretizedFunc func=null;

   /**
    * Construnctor - Accepts the ArbDiscrEmpiricalDistFunc of X and Y values.
    * The values specified should follow the constraints as specified in
    * the setValues() function.
    *
    * @param func ArbitrarilyDiscretizedFunc function of  X and Y values
    */
   public FractileListEstimate(DiscretizedFunc func) {
     setValues(func);
   }

   public String toString() {
    String text =  "EstimateType="+getName()+"\n";
    for(int i=0; func!=null && i<func.getNum(); ++i)
      text+=   decimalFormat.format(func.getX(i))+"\t"+decimalFormat.format(func.getY(i))+"\n";
    return text;
  }


   /**
    * This checks:
    * 1. y(i+1)>=y(i)
    * 2. All Y >=0
    *
    * Func is cloned and held internally
    *
    * @param func
    */
   public void setValues(DiscretizedFunc func) {
     maxX = func.getMaxX();
     minX = func.getMinX();
     int numValues = func.getNum();
     // check that 0�Y�1
     double y,y_last=-1;
     double sum=0;
     for(int i = 0; i<numValues;++i) {
       y = func.getY(i);
       sum+=y;
       if(y<0 || y>1) throw new InvalidParamValException(EST_MSG_INVLID_RANGE);
       if(y_last >= y) throw new InvalidParamValException(EST_MSG_PROBS_NOT_INCREASING);
       y_last = y;
     }
     if(Math.abs(sum-1)>tol)
       throw new InvalidParamValException(EST_MSG_NOT_NORMALIZED);
     this.func = (DiscretizedFunc)func.deepClone();
   }



   /**
    * Returns the X value corresponding to Y = 0.5
    * If there is no Y where Y =0.5, then linear interpolation is used to find
    * the interpolated X value (this object requires that a median exist).
    *
    * @return median value for this set of X and Y values
    */
   public double getMedian() {
     // check that median is defined
     int numValues = func.getNum();
     if(numValues==1 && func.getY(0)!=0.5)
       throw new InvalidParamValException(MEDIAN_UNDEFINED);
     else if(numValues>1 && (func.getY(0)>0.5 || func.getY(numValues-1)<0.5))
       throw new InvalidParamValException(MEDIAN_UNDEFINED);
     return func.getFirstInterpolatedX(0.5);
  }


   /**
    * If a point with the associated probability does not exist, the fractal is
    * found by linear interpolation between the two points that bracket the prob.
    * An exeption is thrown if no such points exist.
    * @param prob
    * @return
    */
   public double getFractile(double prob) {
     if(prob > func.getY(0) && prob > func.getY(func.getNum()-1))
        return func.getFirstInterpolatedX(prob);
     else
       throw new InvalidParamValException(FRACTILE_UNDEFINED);
   }


  public DiscretizedFunc getValues() {
    return this.func;
  }

  public String getName() {
   return NAME;
 }

}
