package org.opensha.data.estimate;

import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFunc;
/**
 * <p>Title: PDF_Estimate.java </p>
 * <p>Description:  This can be used to specify probabilities associated with
 * discrete values from an EvenlyDiscretizedFunction. (it is asssumed that the first and last values are the
 * first and last non-zero values, respectively)
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PDF_Estimate extends DiscretizedFuncEstimate {
  public final static String NAME  =  "PDF";


  /**
   * Constructor - Accepts a EvenlyDiscretizedFunction and an indication of whether it is
   * normalized. Note that the function passed in is cloned..
   * MaxX and MinX are set according to those of the function
   * passed in. (it is asssumed that the first and last values are the
   * first and last non-zero values, respectively)
   * @param func
   */
  public PDF_Estimate(EvenlyDiscretizedFunc func, boolean isNormalized) {
    super(func, isNormalized);
  }

  public String getName() {
   return NAME;
 }

 /**
   * Get the cumulative distribution function
   * @return
   */
  public DiscretizedFunc getCDF() {
    ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
    return func;
  }


}
