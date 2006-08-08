package javaDevelopers.matt.calc;

import java.util.GregorianCalendar;

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

  public static String cubeFilePath = "/home/matt/STEP/QDDS/merge.nts";

  public static double minMagForMainshock = 3.0;
  public static double minForecastMag = 4.0;
  public static double maxForecastMag = 8.0;
  public static double deltaForecastMag = 0.1;

  public static double forecastLengthDays = 1;
  public static boolean startForecastAtCurrentTime = true;
  public static GregorianCalendar forecastStartTime;  // set this if startForecastAtCurrentTime is False
  public static double daysFromQDM_Cat = 7;

  public static double searchLatMin = 32.0;
  public static double searchLatMax = 42.2;
  public static double searchLongMin = -124.6;
  public static double searchLongMax = -112;

  public static double gridSpacing = 0.05;

  public static double addToMc = 0.02;

  // this is for defining the fault surface for the aftershock zone.
  // 2D for now so the values are the same.
  public static double lowerSeismoDepth = 10.0;
  public static double upperSeismoDepth = 10.0;

  public static boolean useFixed_cValue = true;
  
  // set the parameters for the AIC Calcs for the model elements
  public static int genNumFreeParams = 0;
  public static int seqNumFreeParams = 0;
  public static int spaNumFreeParams = 3;  // should be 4 if c is not fixed
  
  // the minimum mag to be used when comparing the cummulative of the 
  // background to that of an individual sequence
  public static int minCompareMag = 5;




}
