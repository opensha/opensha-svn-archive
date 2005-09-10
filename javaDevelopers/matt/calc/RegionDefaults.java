package javaDevelopers.matt.calc;

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
public class RegionDefaults {
  public RegionDefaults() {
  }

  /**
   * This class contains many of the variables that are specific
   * to a region.  Default values are set.
   */

  private String cubeFilePath = "/home/matt/STEP/QDDS/merge.nts";

  private double minMagForMainshock = 3.0;
  private double minForecastMag = 4.0;
  private double maxForecastMag = 8.0;
  private double deltaForecastMag = 0.1;

  private double forecastLengthDays = 1;
  private double daysFromQDM_Cat = 7;

  private double searchLatMin = 32.0;
  private double searchLatMax = 42.2;
  private double searchLongMin = -124.6;
  private double searchLongMax = -112;

  /**
   * getCubeFilePath
   */
  public String getCubeFilePath() {
    return cubeFilePath;
  }


  /**
   * getMinMagForMainshock
   */
  public double getMinMagForMainshock() {
    return minMagForMainshock;
  }

  /**
   * getMinForecastMag
   */
  public double getMinForecastMag() {
    return minForecastMag;
  }

  /**
   * getMaxForecastMag
   */
  public double getMaxForecastMag() {
    return maxForecastMag;
  }

  /**
   * getDeltaForecastMag
   */
  public double getDeltaForecastMag() {
    return deltaForecastMag;
  }

  /**
   * getForecastLengthDays
   */
  public double getForecastLengthDays() {
    return forecastLengthDays;
  }

  /**
   * getDaysFromQDM_Cat
   */
  public double getDaysFromQDM_Cat() {
    return daysFromQDM_Cat;
  }

  /**
   * getSearchLatMin
   */
  public double getSearchLatMin() {
    return searchLatMin;
  }

  /**
   * getSearchLatMax
   */
  public double getSearchLatMax() {
    return searchLatMax;
  }

  /**
   * getSearchLongMin
   */
  public double getSearchLongMin() {
    return searchLongMin;
  }

  /**
   * getSearchLongMax
   */
  public double getSearchLongMax() {
    return searchLongMax;
  }


}
