package org.opensha.sha.magdist;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GutenbergRichterRate_Calc {
  private double minMag = 4;
  private double maxMag = 8;
  private double deltaMag = 0.1;
  private double[] evenlyDescMagRates;

  public GutenbergRichterRate_Calc(double b_value, double numForecastEvents) {
    set_aValForForecast(numForecastEvents, b_value);
  }



  /**
   * set_aValForForecast
   */
  public void set_aValForForecast(double numForecastEvents, double b_value) {
    double forecastGRa = (Math.log(numForecastEvents)/Math.log(10))+b_value*minMag;
    calc_EvenlyDiscGR_Rates(forecastGRa, b_value);
  }

  /**
   * calc_EvenlyDiscGR_Rates
   */
  public void calc_EvenlyDiscGR_Rates(double forecastGRa, double b_value) {
    int numMags = (int)((maxMag-minMag)/deltaMag)+1;
    double rateLower, rateUpper, magVal;
    evenlyDescMagRates = new double[numMags];

    //Calculate the cum. rate in each bin and find the difference
    // I know this can be done better...
    for(int magLoop = 0; magLoop < numMags; magLoop++) {
      magVal = minMag + magLoop*deltaMag;
      rateUpper = Math.pow(10,(forecastGRa-b_value*magVal));
      rateLower = Math.pow(10,(forecastGRa-b_value*(magVal+deltaMag)));
      evenlyDescMagRates[magLoop] = rateUpper-rateLower;
    }
  }

  /**
   * set_minMag
   * This is the minimum mag given a forecast rate
   * default = 4.0
   */
  public void set_minMag(double minmag) {
    minMag = minmag;
  }

  /**
     * set_maxMag
     * This is the maximum mag given a forecast rate
     * default = 8.0
     */
    public void set_maxMag(double maxmag) {
      maxMag = maxmag;
  }

  /**
   * set_deltaMag
   * This is the step size used in binning the magnitudes in
   * creating the forecasted rates.  default = 0.1
   */
  public void set_deltaMag(double deltamag) {
    deltaMag = deltamag;
  }

  /**
   * get_ForecastedRates
   */
  public double[] get_ForecastedRates() {
    return evenlyDescMagRates;
  }

}
