package edu.uah.math.psol.distributions;

/**
 *  This class models the crcle distribution with parameter a. This is the
 *  distribution of X and Y when (X, Y) has the uniform distribution on a
 *  circular region with a specified radius.
 *
 *  
 *  
 */
public class CircleDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    private double radius;

    /**
     *  This general constructor creates a new circle distribution with a
     *  specified radius.
     *
     * @param  r  Description of the Parameter
     */
    public CircleDistribution( double r ) {
        setRadius( r );
    }

    /**
     *  This special constructor creates a new circle distribution with radius 1
     */
    public CircleDistribution() {
        this( 1 );
    }

    /**
     *  This method sets the radius parameter
     *
     * @param  r  The new radius value
     */
    public void setRadius( double r ) {
        if ( r <= 0 )
            r = 1;
        radius = r;
        super.setParameters( -radius, radius, 0.02 * radius, CONTINUOUS );
    }

    /**
     *  This method computes the getDensity function.
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( -radius <= x & x <= radius )
            return 2 * Math.sqrt( radius * radius - x * x ) / ( Math.PI * radius * radius );
        else
            return 0;
    }

    /**
     *  This method computes the maximum value of the getDensity function.
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return getDensity( 0 );
    }

    /**
     *  This method computes the mean
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
        return radius * radius / 4;
    }

    /**
     *  This method computes the median.
     *
     * @return    The median value
     */
    public double getMedian() {
        return 0;
    }

    /**
     *  This method returns the radius parameter.
     *
     * @return    The radius value
     */
    public double getRadius() {
        return radius;
    }

    /**
     *  This method compute the cumulative distribution function.
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        return 0.5 + Math.asin( x / radius ) / Math.PI
                 + x * Math.sqrt( 1 - x * x / ( radius * radius ) ) / ( Math.PI * radius );
    }

    /**
     *  This method simulates a value from the distribution.
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        double u = radius * Math.random();
        double v = radius * Math.random();
        double r = Math.max( u, v );
        double theta = 2 * Math.PI * Math.random();
        return r * Math.cos( theta );
    }
}

