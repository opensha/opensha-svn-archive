package org.opensha.tests.calc;

import junit.framework.TestCase;

import org.opensha.commons.calc.GaussianDistCalc;
/**
 * <p>Title: TestGaussianDistCalc.java </p>
 * <p>Description: Tests the GaussianDistCalc class functions </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TestGaussianDistCalc extends TestCase {
  double tolerance = 1e-6;
  public TestGaussianDistCalc() {
  }

  public TestGaussianDistCalc(String name) {
    super(name);
  }


  public void testGetExceedProbForNonSymmetricTruncation() {
    /**
     * We have two functions in Gaussian Dist Calc:
     *  1. public static double getExceedProb(double standRandVariable, int truncType, double truncLevel)
     *  2. public static double getExceedProb(double standRandVariable, double lowerTruncLevel, double upperTruncLevel)
     * If we provide the same values for lowerTruncLevel and upperTruncLevel in (2),
     * we can check that the values we get is same as we get from (1)
     */
    double upperTruncLevel = 0.5;
    double lowerTruncLevel = -0.5;
    int truncType = 2;
    double truncLevel = 0.5;
    double stdRandVar;
    // check when standRandVariable > truncLevel
    stdRandVar = 1.0;
    double d1 = GaussianDistCalc.getExceedProb(stdRandVar, lowerTruncLevel, upperTruncLevel);
    double d2 = GaussianDistCalc.getExceedProb(stdRandVar, truncType, truncLevel);
    assertEquals(d1,d2,tolerance);

    // check when standRandVariable < -truncLevel
    stdRandVar = -1.0;
    d1 = GaussianDistCalc.getExceedProb(stdRandVar, lowerTruncLevel, upperTruncLevel);
    d2 = GaussianDistCalc.getExceedProb(stdRandVar, truncType, truncLevel);
    assertEquals(d1,d2,tolerance);


    // check when -truncLevel < standRandVariable  < truncLevel
    stdRandVar = 0.1;
    d1 = GaussianDistCalc.getExceedProb(stdRandVar, lowerTruncLevel, upperTruncLevel);
    d2 = GaussianDistCalc.getExceedProb(stdRandVar, truncType, truncLevel);
    assertEquals(d1,d2,tolerance);


    try { // exception is thrown if upperTrunclevel<0 or lowerTruncLevel<0
      GaussianDistCalc.getExceedProb(stdRandVar, -lowerTruncLevel, upperTruncLevel);
      fail("Should not reach here as lower trunc level should be positive");
    }catch(RuntimeException e) { }
  }


  public void testGetStdRandVarForNonSymmetricTruncation() {
    /**
    * We have two functions in Gaussian Dist Calc:
    *  1.  public static double getStandRandVar(double exceedProb, int truncType, double truncLevel, double tolerance)
    *  2.  public static double getStandRandVar(double exceedProb, double lowerTruncLevel, double upperTruncLevel, double tolerance)
    * If we provide the same values for lowerTruncLevel and upperTruncLevel in (2),
    * we can check that the values we get is same as we get from (1)
    */
   double upperTruncLevel = 0.5;
   double lowerTruncLevel = -0.5;
   int truncType = 2;
   double truncLevel = 0.5;
   double prob;

   // if( exceedProb <= 0.5 && exceedProb > 0.0 )
   prob = 0.2;
   double d1 = GaussianDistCalc.getStandRandVar(prob, lowerTruncLevel, upperTruncLevel,
                                                tolerance);
   double d2 = GaussianDistCalc.getStandRandVar(prob, truncType, truncLevel, tolerance );
   assertEquals(d1,d2,tolerance);

   //if ( exceedProb > 0.5 && exceedProb < 1.0 )
   prob = 0.7;
   d1 = GaussianDistCalc.getStandRandVar(prob, lowerTruncLevel, upperTruncLevel,
                                                tolerance);
   d2 = GaussianDistCalc.getStandRandVar(prob, truncType, truncLevel, tolerance );
   assertEquals(d1,d2,tolerance);


   // if (exceedProb == 0.0)
   prob = 0.0;
   d1 = GaussianDistCalc.getStandRandVar(prob, lowerTruncLevel, upperTruncLevel,
                                         tolerance);
   d2 = GaussianDistCalc.getStandRandVar(prob, truncType, truncLevel, tolerance );
   assertEquals(d1,d2,tolerance);


  // if (exceedProb == 1.0)
  prob = 1.0;
  d1 = GaussianDistCalc.getStandRandVar(prob, lowerTruncLevel, upperTruncLevel,
                                        tolerance);
  d2 = GaussianDistCalc.getStandRandVar(prob, truncType, truncLevel, tolerance );
  assertEquals(d1,d2,tolerance);


  // if (exceedProb < 0) or (exceedProb > 1)
  prob = -0.7;
  try {
    d1 = GaussianDistCalc.getStandRandVar(prob, lowerTruncLevel,
                                          upperTruncLevel,
                                          tolerance);
    fail("should not reach here as probability is negative");
  }catch(RuntimeException e) { }

}



}