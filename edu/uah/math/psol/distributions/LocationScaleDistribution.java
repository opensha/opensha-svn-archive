package edu.uah.math.psol.distributions;

/**
 *  This class applies a location-scale tranformation to a given distribution.
 *  In terms of the corresponding random variable X, the transformation is Y = a
 *  + bX
 *
 *  
 *  
 */
public class LocationScaleDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    private Distribution dist;
    /**
     *  Description of the Field
     */
    private double location, scale;

    /**
     *  This general constructor creates a new location-scale transformation on
     *  a given distribuiton with given location and scale parameters
     *
     * @param  d  Description of the Parameter
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     */
    public LocationScaleDistribution( Distribution d, double a, double b ) {
        setParameters( d, a, b );
    }

    /**
     *  This method sets the parameters: the distribution and the location and
     *  scale parameters
     *
     * @param  d  The new parameters value
     * @param  a  The new parameters value
     * @param  b  The new parameters value
     */
    public void setParameters( Distribution d, double a, double b ) {
        dist = d;
        location = a;
        scale = b;
        Domain domain = dist.getDomain();
        double l;
        double u;
        double w = domain.getWidth();
        int t = dist.getType();
        if ( t == DISCRETE ) {
            l = domain.getLowerValue();
            u = domain.getUpperValue();
        }
        else {
            l = domain.getLowerBound();
            u = domain.getUpperBound();
        }
        if ( scale == 0 )
            super.setParameters( location, location, 1, DISCRETE );
        else if ( scale < 0 )
            super.setParameters( location + scale * u, location + scale * l, -scale * w, t );
        else
            super.setParameters( location + scale * l, location + scale * u, scale * w, t );
    }

    /**
     *  This method defines the getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( scale == 0 ) {
            if ( x == location )
                return 1;
            else
                return 0;
        }
        else
            return dist.getDensity( ( x - location ) / scale );
    }

    /**
     *  This method returns the maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return dist.getMaxDensity();
    }

    /**
     *  This mtehod returns the mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return location + scale * dist.getMean();
    }

    /**
     *  This method returns the variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        return ( scale * scale ) * dist.getVariance();
    }

    /**
     *  This method returns the cumulative distribution function
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        if ( scale > 0 )
            return dist.getCDF( ( x - location ) / scale );
        else
            return 1 - dist.getCDF( ( x - location ) / scale );
    }

    /**
     *  This method returns the getQuantile function
     *
     * @param  p  Description of the Parameter
     * @return    The quantile value
     */
    public double getQuantile( double p ) {
        if ( scale > 0 )
            return location + scale * dist.getQuantile( p );
        else
            return location + scale * dist.getQuantile( 1 - p );
    }

    /**
     *  This method returns a simulated value from the distribution
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        return location + scale * dist.simulate();
    }
}

