package edu.uah.math.psol.distributions;

/**
 *  This class models the student t distribution with a specifed degrees of
 *  freeom parameter
 *
 *  
 *  
 */
public class StudentDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    private int degrees;
    /**
     *  Description of the Field
     */
    private double c;

    /**
     *  This general constructor creates a new student distribution with a
     *  specified degrees of freedom
     *
     * @param  n  Description of the Parameter
     */
    public StudentDistribution( int n ) {
        setDegrees( n );
    }

    /**
     *  This default constructor creates a new student distribuion with 1 degree
     *  of freedom
     */
    public StudentDistribution() {
        this( 1 );
    }

    /**
     *  This method sets the degrees of freedom
     *
     * @param  n  The new degrees value
     */
    public void setDegrees( int n ) {
        //Correct invalid parameter
        if ( n < 1 )
            n = 1;
        //Assign parameter
        degrees = n;
        //Compute normalizing constant
        c = logGamma( 0.5 * ( degrees + 1 ) ) - 0.5 * Math.log( degrees ) - 0.5 * Math.log( Math.PI ) - logGamma( 0.5 * degrees );
        //Compute upper bound
        double upper;
        if ( n == 1 )
            upper = 8;
        else if ( n == 2 )
            upper = 7;
        else
            upper = Math.ceil( getMean() + 4 * getSD() );
        super.setParameters( -upper, upper, upper / 50, CONTINUOUS );
    }

    /**
     *  This method computes the getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        return Math.exp( c - 0.5 * ( degrees + 1 ) * Math.log( 1 + x * x / degrees ) );
    }

    /**
     *  This method returns the maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return getDensity( 0 );
    }

    /**
     *  This method returns the mean
     *
     * @return    The mean value
     */
    public double getMean() {
        if ( degrees == 1 )
            return Double.NaN;
        else
            return 0;
    }

    /**
     *  This method returns the variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        if ( degrees == 1 )
            return Double.NaN;
        else if ( degrees == 2 )
            return Double.POSITIVE_INFINITY;
        else
            return ( double ) degrees / ( degrees - 2 );
    }

    /**
     *  This method computes the cumulative distribution function in terms of
     *  the beta CDF
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        double u = degrees / ( degrees + x * x );
        if ( x > 0 )
            return 1 - 0.5 * betaCDF( u, 0.5 * degrees, 0.5 );
        else
            return 0.5 * betaCDF( u, 0.5 * degrees, 0.5 );
    }

    /**
     *  This method returns the degrees of freedom
     *
     * @return    The degrees value
     */
    public double getDegrees() {
        return degrees;
    }

    /**
     *  This method simulates a value of the distribution
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        double v;
        double z;
        double r;
        double theta;
        v = 0;
        for ( int i = 1; i <= degrees; i++ ) {
            r = Math.sqrt( -2 * Math.log( Math.random() ) );
            theta = 2 * Math.PI * Math.random();
            z = r * Math.cos( theta );
            v = v + z * z;
        }
        r = Math.sqrt( -2 * Math.log( Math.random() ) );
        theta = 2 * Math.PI * Math.random();
        z = r * Math.cos( theta );
        return z / Math.sqrt( v / degrees );
    }
}

