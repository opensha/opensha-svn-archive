package gov.usgs.sha.calc;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: SingleValueHazardCurveCalculator</p>
 *
 * <p>Description: This class calculates the single value for the Single Value
 * Hazard Curve. </p>
 * <p>It also calculates the Return Period if Prob. of Exceedance and Exposure time
 * are given. Calculates Prob. of Exceedance if return period and exposure time given.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class SingleValueHazardCurveCalculator {


  /**
   *
   * @param probExceed double
   * @param expTime double
   * @return double
   */
  public double calculateReturnPeriod(double probExceed, double expTime) {
    probExceed /=100.0;
    double returnPd = Math.round(-expTime/Math.log(1-probExceed));
    return returnPd;
  }

  /**
   *
   * @param fex double Frequency of exceedance = 1/ReturnPd
   * @param expTime double
   * @return double prob of exceedance in %(percentage) or out of 100
   */
  public double calculateProbExceed(double fex, double expTime) {
    double probExceed = (1.0 - Math.exp(-expTime*fex))*100;
    return probExceed;
  }

}
