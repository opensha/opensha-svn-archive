package edu.uah.math.psol.distributions;

/**
 *  Gamma distribution with a specified shape parameter and scale parameter
 *
 *  
 *  
 */
public class GammaDistribution extends Distribution {
    //Parameters
    /**
     *  Description of the Field
     */
    private double shape, scale, c;

    /**
     *  General Constructor: creates a new gamma distribution with shape
     *  parameter k and scale parameter b
     *
     * @param  k  Description of the Parameter
     * @param  b  Description of the Parameter
     */
    public GammaDistribution( double k, double b ) {
        setParameters( k, b );
    }

    /**
     *  Default Constructor: creates a new gamma distribution with shape
     *  parameter 1 and scale parameter 1
     */
    public GammaDistribution() {
        this( 1, 1 );
    }

    /**
     *  Set parameters and assign the default partition
     *
     * @param  k  The new parameters value
     * @param  b  The new parameters value
     */
    public void setParameters( double k, double b ) {
        double upperBound;
        //Correct invalid parameters
        if ( k < 0 )
            k = 1;
        if ( b < 0 )
            b = 1;
        shape = k;
        scale = b;
        //Normalizing constant
        c = shape * Math.log( scale ) + logGamma( shape );
        //Assign default partition:
        upperBound = getMean() + 4 * getSD();
        super.setParameters( 0, upperBound, 0.01 * upperBound, CONTINUOUS );
    }

    /**
     *  Get shape parameters
     *
     * @return    The shape value
     */
    public double getShape() {
        return shape;
    }

    /**
     *  Get scale parameters
     *
     * @return    The scale value
     */
    public double getScale() {
        return scale;
    }

    /**
     *  Density function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( x < 0 )
            return 0;
        else if ( x == 0 & shape < 1 )
            return Double.POSITIVE_INFINITY;
        else if ( x == 0 & shape == 1 )
            return Math.exp( -c );
        else if ( x == 0 & shape > 1 )
            return 0;
        else
            return Math.exp( -c + ( shape - 1 ) * Math.log( x ) - x / scale );
    }

    /**
     *  Maximum value of getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode;
        if ( shape < 1 )
            mode = 0.01;
        else
            mode = scale * ( shape - 1 );
        return getDensity( mode );
    }

    /**
     *  Mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return shape * scale;
    }

    /**
     *  Variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        return shape * scale * scale;
    }

    /**
     *  Cumulative distribution function
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        return gammaCDF( x / scale, shape );
    }

    /**
     *  Simulate a value
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        /*
         *  If shape parameter k is an integer, simulate as the k'th arrival time
         *  in a Poisson proccess
         */
        if ( shape == Math.rint( shape ) ) {
            double sum = 0;
            for ( int i = 1; i <= shape; i++ )
                sum = sum - scale * Math.log( 1 - Math.random() );

            return sum;
        }
        else
            return super.simulate();
    }
}

