package org.opensha.nshmp.sha.calc;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: StdDisplacementCalc</p>
 *
 * <p>Description: Calculates the Standard displacement using the SA value function.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class StdDisplacementCalc {

  /**
   * Calculates the Std Displacement function Vs Period using the Sa Values.
   * @param saFunction ArbitrarilyDiscretizedFunc Sa Values function
   * where X values are the Periods and Y are the SA vals
   * @return ArbitrarilyDiscretizedFunc
   */
  public ArbitrarilyDiscretizedFunc getStdDisplacement(
      ArbitrarilyDiscretizedFunc saFunction) {

    ArbitrarilyDiscretizedFunc sdTFunction = new ArbitrarilyDiscretizedFunc();

    int numPoints = saFunction.getNum();
    for (int i = 0; i < numPoints; ++i) {
      double tempPeriod = Math.pow(saFunction.getX(i), 2.0);
      double sdVal = 9.77 * saFunction.getY(i) * tempPeriod;
      sdTFunction.set(saFunction.getX(i), sdVal);
    }
    return sdTFunction;
  }

}
