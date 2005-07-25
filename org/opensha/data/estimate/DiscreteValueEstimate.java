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
