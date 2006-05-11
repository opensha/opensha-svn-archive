package org.opensha.data.estimate;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFunc;

/**
 * <p>Title: DiscreteValueEstimate.java </p>
 * <p>Description:  This can be used to specify probabilities associated with
 * discrete values from an ArbitrarilyDiscretizedFunction.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DiscreteValueEstimate extends DiscretizedFuncEstimate {
  public final static String NAME  =  "Discrete Values";

  /**
   * Constructor - Accepts a ArbitrarilyDiscretizedFunc and an indication of whether it is
   * normalized. Note that the function passed in is cloned.
   * MaxX and MinX are set according to those of the function
   * passed in.
   * @param func
   */
  public DiscreteValueEstimate(ArbitrarilyDiscretizedFunc func, boolean isNormalized) {
    super(func, isNormalized);
  }

  /**
  * As implemented, the function passed in is cloned.
  * MaxX and MinX are set by those in the function passed in.
  *
  * @param func
  */
 public void setValues(DiscretizedFunc newFunc, boolean isNormalized) {
   super.setValues(newFunc, isNormalized);

 }

 /**
  * Return the name of the estimate. This is the name viisble to the user
  * @return
  */
  public String getName() {
    return NAME;
  }


  public String toString() {
    String text =  "EstimateType="+getName()+"\n";
    text+="Number Of Discrete values="+func.getNum();
    for(int i=0; func!=null && i<func.getNum(); ++i) {
      text += "\n"+decimalFormat.format(func.getX(i)) + "\t"+decimalFormat.format(func.getY(i));
    }
    return text;
  }


  /**
  * Get the cumulative distribution function
  * @return
  */
 public DiscretizedFunc getCDF_Test() {
   ArbitrarilyDiscretizedFunc cdfFunc = new ArbitrarilyDiscretizedFunc();
   System.out.println(cumDistFunc);
   int num = func.getNum();
   double delta = 1e-3;
   double x ;
   for(int i=0; i<num; ++i) {
     x = func.getX(i);
     cdfFunc.set(x, getProbLessThanEqual(x));
     if(i<(num-1)) {
       x = func.getX(i + 1) - delta; // get the value to make staircase function
       cdfFunc.set(x, getProbLessThanEqual(x));
     }
   }
   cdfFunc.setInfo("CDF from Discrete Distribution");
   return cdfFunc;
 }

 /**
  * Get the probability for that the true value is less than or equal to provided
  * x value
  *
  * @param x
  * @return
  */
 public double getProbLessThanEqual(double x) {
   if(x<this.cumDistFunc.getX(0)) return 0;// return 0 if it less than 1st X value in this estimate
   int num = cumDistFunc.getNum();
   for(int i=1; i<num; ++i)
     if(cumDistFunc.getX(i)>x)
       return cumDistFunc.getY(i-1);
   return 1;
 }

 /**
 * Return the discrete fractile for this probability value.
 *
 * @param prob Probability for which fractile is desired
 * @return
 */
 public double getFractile(double prob) {
   int num = cumDistFunc.getNum();
   for(int i=0; i<num; ++i)
     if(cumDistFunc.getY(i)>prob)
       return cumDistFunc.getX(i);
   return 1;
 }


 public  DiscretizedFunc getPDF_Test() {
   return this.func;
 }

}
