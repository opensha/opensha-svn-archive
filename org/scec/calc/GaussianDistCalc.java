package org.scec.calc;

//  The following is needed only by getCDF_Alt() which is commented out below.
//  import edu.uah.math.psol.distributions.*;

// The following is needed only for the tests commented out below
// import java.text.DecimalFormat;

/**
 * <b>Title:</b> GaussianDistCalc.jav <p>
 * <b>Description:</p> This is a utility to calculate probability of exceedance
 * assuming a gaussian distribution.   A main and a bunch of test methods are
 * commented out in the source code.  <p>
 *
 * @author Edward Field
 * @created    aug 22, 2002
 * @version 1.0
 */

public final class GaussianDistCalc {

// if using edu.uah.math.psol.distributions package:
//   static NormalDistribution gauss = new NormalDistribution( 0.0, 1.0 );

  // if computing pdf here (not using above)
  	static double d1= 0.0498673470;
	static double d2=0.0211410061;
	static double d3=0.0032776263;
	static double d4=0.0000380036;
	static double d5=0.0000488906;
	static double d6=0.0000053830;


/*
    public static void main(String args[]) {

        GaussianDistCalc.test5();
    }
*?

  /*
    * This function calculates the Gaussian exceedance probability for the standardized
    * random variable assuming no truncation of the distribution.
    */
    public static double getExceedProb(double standRandVariable) {

        return 1.0 - getCDF(standRandVariable);
    }



  /**
    * This function calculates the exceedance probability for a truncated Gaussian
    * distribution (truncType=0 for none; truncType=1 for upper truncation, and
    * truncType=2 for two-sided truncation).
    */
    public static double getExceedProb(double standRandVariable, int truncType, double truncLevel) {

        double prob = getCDF( standRandVariable );

        // compute probability based on truncation type

        if (  truncType == 1 ) {  // upper truncation
            if ( standRandVariable > truncLevel )
                return  0.0;
            else {
                double pUp = getCDF( truncLevel );
                return  (1.0 - prob/pUp) ;
            }
        }

        else if ( truncType == 2 ) {  // the two sided case
            if ( standRandVariable > truncLevel )
                return (0.0);
            else if ( standRandVariable < -truncLevel )
                return (1.0);
            else {
                double pUp = getCDF( truncLevel );
                double pLow = getCDF( -truncLevel );
                return ( (pUp-prob)/(pUp-pLow) );
            }
        }

        else  return (1.0 - prob );  // no truncation
    }

  /**
    * This function calculates the cumulative density function for a Gaussian
    * distribution (the area under the curve up to standRandVariable).  The object
    * edu.uah.math.psol.distributions.NormalDistribution is not used here because
    * it was found to be ~3 times slower, even if the object was created only in
    * the constructor (see getCDF_Alt()).
    */
    public static double getCDF(double standRandVariable) {

      double val;
      double result;

      if( standRandVariable >= 0 )  val = standRandVariable;
      else                          val = -standRandVariable;

      result = 0.5 * Math.pow( (((((d6*val+d5)*val+d4)*val+d3)*val+d2)*val+d1)*val+1, -16);

      if(standRandVariable < 0) return result;
      else                      return 1-result;
    }


    /**
     * This returns the standardized random variable (SRV) associated with the
     * given exceedance probability.  More specifically, for probabilities < 0.5
     * this finds the smallest (within tolerance) SRV that is within tolerance
     * of the target probability (and visa versa for probabilities greater than
     * 0.5).  For example, if the target probability is 0.0, and the tolerance
     * is 0.001, then the SRV returned is 3.09 (all SRV values above this will also
     * have an exceedance probability within tolerance of 0.0; we give the lowest
     * just to be consistent).  More specifically, the following are the SRVs for
     * an exceedance probability of 0.0 (opposite sign for 1.0): 1.29 for tol=0.1;
     * 2.33 for tol=0.01; 3.09 for tol=0.001; 3.72 for tol=1e-4; 4.27 for tol=1e-5;
     * -4.76 for tol=1e-6; 5.21 for tol=1e-7; 5.64 for tol=1e-8; -6.04 for tol=1e-9;
     * and 6.42 for tol=1e-10.
     * @param exceedProb
     * @return standardized random variable
     */
    public static double getStandRandVar(double exceedProb, int truncType, double truncLevel, double tolerance) {

	double delta = 1;
	double testNum = 100;
	double oldNum = 0;
        double prob = 100;

	if( exceedProb <= 0.5 && exceedProb >= 0.0 ) {
		do {
			testNum = oldNum;
			do {
				testNum += delta;
				prob = getExceedProb(testNum, truncType, truncLevel);
//  System.out.println(prob + "  " + testNum);
			}
                        while ( prob >= (exceedProb+tolerance) );
			oldNum = testNum - delta;
			delta /= 10;
		}
                while (testNum-oldNum > tolerance);
//  System.out.println(testNum);
	        return testNum;
        }
	else if ( exceedProb <= 1.0 ) {
		do {
			testNum = oldNum;
			do {
				testNum -= delta;
				prob = getExceedProb(testNum, truncType, truncLevel);
			}
                        while (prob <= (exceedProb-tolerance) );
			oldNum = testNum  + delta;
			delta /= 10;
		}
                while ( oldNum-testNum > tolerance);
	        return testNum;
	}
        else
                throw new RuntimeException("invalid exceed probability");
    }


/*
    public static double getCDF_Alt(double standRandVariable) {

        return gauss.getCDF( standRandVariable );
    }
*/

    /*
     * This test getCDF() and getCDF_Alt() against each other (greatest diff is
     * 1.3E-7 at 0.4), and with the values listed in Appendix C of Shaum's 1975
     * Outline Series "Probability and Statistics" by M.R. Spiegel (the match is
     * is exact).

    public static void test1() {

        double val;
        double diff = 0;
        double maxDiff = 0;
        double maxDiffVal = 100;

        DecimalFormat df1 = new DecimalFormat("#.0000");
        DecimalFormat df2 = new DecimalFormat("#.0");

        for (val=-5; val <=0; val += 0.1) {
          diff = Math.abs (getCDF_Alt(val) - getCDF(val));
          System.out.println( df2.format(val) + "  " + df1.format(0.5 - getCDF(val)) + "  " + diff );
          if ( diff > maxDiff ) {
            maxDiff = diff;
            maxDiffVal = val;
          }
        }
        for (val=0; val <= 5.0; val += 0.1) {
          diff = Math.abs (getCDF_Alt(val) - getCDF(val));
          System.out.println( df2.format(val) + "  " + df1.format(getCDF(val) - 0.5) + "  " + diff );
          if ( diff > maxDiff ) {
            maxDiff = diff;
            maxDiffVal = val;
          }
        }

        System.out.println( "Max Diff = " + maxDiff + " at " + df2.format(maxDiffVal) );
    }

    /**
     * This tests the speed of getCDF() vs getCDF_Alt(); the latter is about
     * three times slower.

    public static void test2() {

        System.out.println("Starting getCDF()");
        for(int i = 1; i < 500000; i++ )
            getCDF(0.5);
        System.out.println("Done with getCDF()");

        System.out.println("Starting getCDF_Alt()");
        for( int i = 1; i < 500000; i++ )
            getCDF_Alt(0.5);
        System.out.println("Done with getCDF_Alt()");

    }



    /**
     * This makes a table to see where getCDF() and getCDF_Alt() break down.
     * is exact).

    public static void test3() {

        double val;

        DecimalFormat df2 = new DecimalFormat("#.00");

        System.out.println( "val  getCDF getCDF_Alt");
        for (val=-6; val <= 6; val += 0.1)
          System.out.println( df2.format(val) + "  " + getCDF(val) + "  " + getCDF_Alt(val) );


    }

    public static void test4() {

        double p, n, p2, p3;
        double t = 0.000001;
        int     trTyp=1;
        double  trVal=3;

        DecimalFormat df2 = new DecimalFormat("#.0000");

        System.out.println("startNRD  foundNRD -- 3rd & 4th cols should be < & > than tolerance, respectively");
        System.out.println("For tolerance = " + t + ":");

        for (double val=-6; val <= 0; val += 0.1) {
          p = getExceedProb(val,trTyp,trVal);
          n = getStandRandVar(p,trTyp,trVal,t);
          p2 = getExceedProb(n,trTyp,trVal);
          p3 = getExceedProb(n+t,trTyp,trVal);
          System.out.println( df2.format(val) + "  " + df2.format(n) + "  " + (float) (p-p2)  + "  " + (float) (p-p3) );
        }
        for (double val=0; val <= 6; val += 0.1) {
          p = getExceedProb(val,trTyp,trVal);
          n = getStandRandVar(p,trTyp,trVal,t);
          p2 = getExceedProb(n,trTyp,trVal);
          p3 = getExceedProb(n-t,trTyp,trVal);
          System.out.println( df2.format(val) + "  " + df2.format(n) + "  " + (float) (p2-p) + "  " + (float) (p3-p) );
        }

        System.out.println( getStandRandVar(0.0,trTyp,trVal,t) + "  " + getStandRandVar(1.0,trTyp,trVal,t));
    }

    public static void test5() {

        DecimalFormat df2 = new DecimalFormat("#.00");
        for (double t=1e-1; t >=1e-10; t /=10) {
            System.out.println("For tol = " + (float) t + ", SRV for 1.0 = " + df2.format(getStandRandVar(1,0,0,t)) );
            System.out.println("For tol = " + (float) t + ", SRV for 0.0 = " + df2.format(getStandRandVar(0,0,0,t)) );
        }
    }
*/
}