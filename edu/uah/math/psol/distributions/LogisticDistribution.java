package edu.uah.math.psol.distributions;

/**
 *  This class models the logistic distribution
 *
 *  
 *  
 */
public class LogisticDistribution extends Distribution {

    /**
     *  This default constructor creates a new logsitic distribution
     */
    public LogisticDistribution() {
        super.setParameters( -7, 7, 0.14, CONTINUOUS );
    }

    /**
     *  This method computes the getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        double e = Math.exp( x );
        return e / ( ( 1 + e ) * ( 1 + e ) );
    }

    /**
     *  This method computes the maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return 0.25;
    }

    /**
     *  This method computes the cumulative distribution function
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        double e = Math.exp( x );
        return e / ( 1 + e );
    }

    /**
     *  This method comptues the getQuantile function
     *
     * @param  p  Description of the Parameter
     * @return    The quantile value
     */
    public double getQuantile( double p ) {
        return Math.log( p / ( 1 - p ) );
    }

    /**
     *  This method returns the mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return 0;
    }

    /**
     *  This method computes the variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        return Math.PI * Math.PI / 3;
    }
}

