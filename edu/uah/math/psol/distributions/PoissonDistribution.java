package edu.uah.math.psol.distributions;

/**
 *  The Poisson distribution with a specified rate parameter
 *
 *  
 *  
 */
public class PoissonDistribution extends Distribution {
    //Variables
    /**
     *  Description of the Field
     */
    double parameter;

    /**
     *  Default constructor: creates a new Poisson distribution with a given
     *  parameter value
     *
     * @param  r  Description of the Parameter
     */
    public PoissonDistribution( double r ) {
        setParameter( r );
    }

    /**
     *  Default constructor: creates a new Poisson distribtiton with parameter 1
     */
    public PoissonDistribution() {
        this( 1 );
    }

    /**
     *  Sets the parameter
     *
     * @param  r  The new parameter value
     */
    public void setParameter( double r ) {
        //Correct for invalid parameter:
        if ( r < 0 )
            r = 1;
        parameter = r;
        //Sets the truncated set of values
        double a = Math.ceil( getMean() - 4 * getSD() );
        //Sets the truncated set of values
        double b = Math.ceil( getMean() + 4 * getSD() );
        if ( a < 0 )
            a = 0;
        super.setParameters( a, b, 1, DISCRETE );
    }

    /**
     *  Parameter
     *
     * @return    The parameter value
     */
    public double getParameter() {
        return parameter;
    }

    /**
     *  Density function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int k = ( int ) Math.rint( x );
        if ( k < 0 )
            return 0;
        else
            return Math.exp( -parameter ) * ( Math.pow( parameter, k ) / factorial( k ) );
    }

    /**
     *  Maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode = Math.floor( parameter );
        return getDensity( mode );
    }

    /**
     *  Cumulative distribution function
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        return 1 - gammaCDF( parameter, x + 1 );
    }

    /**
     *  Mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return parameter;
    }

    /**
     *  Variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        return parameter;
    }

    /**
     *  Simulate a value
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int arrivals = 0;
        double sum = -Math.log( 1 - Math.random() );
        while ( sum <= parameter ) {
            arrivals++;
            sum = sum - Math.log( 1 - Math.random() );
        }
        return arrivals;
    }
}

