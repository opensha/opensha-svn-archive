package edu.uah.math.psol.distributions;

/**
 *  This class models the Weibull distribution with specified shape and scale
 *  parameters
 *
 *  
 *  
 */
public class WeibullDistribution extends Distribution {
    //Variables
    /**
     *  Description of the Field
     */
    double shape, scale, c;

    /**
     *  This general constructor creates a new Weibull distribution with
     *  spcified shape and scale parameters
     *
     * @param  k  Description of the Parameter
     * @param  b  Description of the Parameter
     */
    public WeibullDistribution( double k, double b ) {
        setParameters( k, b );
    }

    /**
     *  This default constructor creates a new Weibull distribution with shape
     *  parameter 1 and scale parameter 1
     */
    public WeibullDistribution() {
        this( 1, 1 );
    }

    /**
     *  This method sets the shape and scale parameter. The normalizing constant
     *  is computed and the default interval defined
     *
     * @param  k  The new parameters value
     * @param  b  The new parameters value
     */
    public void setParameters( double k, double b ) {
        double upper;
        double width;
        if ( k <= 0 )
            k = 1;
        if ( b <= 0 )
            b = 1;
        //Assign parameters
        shape = k;
        scale = b;
        //Compute normalizing constant
        c = shape / Math.pow( scale, shape );
        //Define interval
        upper = Math.ceil( getMean() + 4 * getSD() );
        width = upper / 100;
        super.setParameters( 0, upper, width, CONTINUOUS );
    }

    /**
     *  This method sets the shape parameter
     *
     * @param  k  The new shape value
     */
    public void setShape( double k ) {
        setParameters( k, scale );
    }

    /**
     *  This method sets the shape parameter
     *
     * @param  b  The new scale value
     */
    public void setScale( double b ) {
        setParameters( shape, b );
    }

    /**
     *  This method compues teh denstiy function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        return c * Math.pow( x, shape - 1 ) * Math.exp( -Math.pow( x / scale, shape ) );
    }

    /**
     *  This method returns the maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode;
        if ( shape < 1 )
            mode = getDomain().getLowerValue();
        else
            mode = scale * Math.pow( ( shape - 1 ) / shape, 1 / shape );
        return getDensity( mode );
    }

    /**
     *  The method returns the mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return scale * gamma( 1 + 1 / shape );
    }

    /**
     *  This method returns the variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        double mu = getMean();
        return scale * scale * gamma( 1 + 2 / shape ) - mu * mu;
    }

    /**
     *  This method computes the cumulative distribution function
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        return 1 - Math.exp( -Math.pow( x / scale, shape ) );
    }

    /**
     *  This method returns the getQuantile function
     *
     * @param  p  Description of the Parameter
     * @return    The quantile value
     */
    public double getQuantile( double p ) {
        return scale * Math.pow( -Math.log( 1 - p ), 1 / shape );
    }

    /**
     *  This method computes the failure rate function
     *
     * @param  x  Description of the Parameter
     * @return    The failureRate value
     */
    public double getFailureRate( double x ) {
        return shape * Math.pow( x, shape - 1 ) / Math.pow( scale, shape );
    }


    /**
     *  This method returns the shape parameter
     *
     * @return    The shape value
     */
    public double getShape() {
        return shape;
    }

    /**
     *  This method returns the scale parameter
     *
     * @return    The scale value
     */
    public double getScale() {
        return scale;
    }
}

