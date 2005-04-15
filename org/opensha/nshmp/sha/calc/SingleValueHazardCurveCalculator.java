package org.opensha.nshmp.sha.calc;

/**
 * <p>Title: SingleValueHazardCurveCalculator</p>
 *
 * <p>Description:It calculates the Return Period if Prob. of Exceedance and Exposure time
 * are given. Calculates Prob. of Exceedance if return period and exposure time given.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class SingleValueHazardCurveCalculator {

  /**
   * Calculates the Return Period for the given exceed probability and
   * exposure time.
   * @param probExceed double
   * @param expTime double
   * @return double
   */
  public double calculateReturnPeriod(double probExceed, double expTime) {
    probExceed /= 100.0;
    double returnPd = Math.round( -expTime / Math.log(1 - probExceed));
    return returnPd;
  }

  /**
   * Calculates Exceed Probability when Frequecy of Exceedance and Exposure time
   * are provided.
   * @param fex double Frequency of exceedance = 1/ReturnPd
   * @param expTime double
   * @return double prob of exceedance in %(percentage) or out of 100
   */
  public double calculateProbExceed(double fex, double expTime) {
    double probExceed = (1.0 - Math.exp( -expTime * fex)) * 100;
    return probExceed;
  }

}
