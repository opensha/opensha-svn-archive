package org.scec.calc;

import edu.uah.math.psol.distributions.*;

/**
 * <p>Title: GaussianDistCalc.java </p>
 * <p>Description: This is a utility to calculate probability of exceedance assuming a gaussian distribution</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Edward Field  date: aug 22, 2002
 * @version 1.0
 */

public final class GaussianDistCalc {

  /**
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
    * distribution (the area under the curve up to standRandVariable).
    */
    public static double getCDF(double standRandVariable) {

      NormalDistribution gauss = new NormalDistribution( 0.0, 1.0 );
      return gauss.getCDF( standRandVariable );
    }
}