package edu.uah.math.psol.distributions;

/**
 *  The Bernoulli distribution with parameter p
 *
 *  
 *  
 */
public class BernoulliDistribution extends BinomialDistribution {

    /**
     *  This general constructor creates a new Bernoulli distribution with a
     *  specified parameter
     *
     * @param  p  Description of the Parameter
     */
    public BernoulliDistribution( double p ) {
        super( 1, p );
    }

    /**
     *  This default constructor creates a new Bernoulli distribution with
     *  parameter p = 0.5
     */
    public BernoulliDistribution() {
        this( 0.5 );
    }

    /**
     *  This method overrides the corresponding method in BinomialDistribution
     *  so that the number of trials 1 cannot be changed
     *
     * @param  n  The new trials value
     */
    public void setTrials( int n ) {
        super.setTrials( 1 );
    }

    /**
     *  This method returns the maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return 1;
    }

}

