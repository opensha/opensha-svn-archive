package edu.uah.math.psol.distributions;

/**
 *  This class defines the standard exponential distribution with rate parameter
 *  r
 *
 *  
 *  
 */
public class ExponentialDistribution extends GammaDistribution {
    //Parameter
    /**
     *  Description of the Field
     */
    double rate;

    /**
     *  This general constructor creates a new exponential distribution with a
     *  specified rate
     *
     * @param  r  Description of the Parameter
     */
    public ExponentialDistribution( double r ) {
        setRate( r );
    }

    /**
     *  This default constructor creates a new exponential distribution with
     *  rate 1
     */
    public ExponentialDistribution() {
        this( 1 );
    }

    /**
     *  This method sets the rate parameter
     *
     * @param  r  The new rate value
     */
    public void setRate( double r ) {
        if ( r <= 0 )
            r = 1;
        rate = r;
        super.setParameters( 1, 1 / rate );
    }

    /**
     *  This method gets the rate
     *
     * @return    The rate value
     */
    public double getRate() {
        return rate;
    }

    /**
     *  This method defines the getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( x < 0 )
            return 0;
        else
            return rate * Math.exp( -rate * x );
    }

    /**
     *  This method defines the cumulative distribution function
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        return 1 - Math.exp( -rate * x );
    }

    /**
     *  The method defines the getQuantile function
     *
     * @param  p  Description of the Parameter
     * @return    The quantile value
     */
    public double getQuantile( double p ) {
        return -Math.log( 1 - p ) / rate;
    }
}

