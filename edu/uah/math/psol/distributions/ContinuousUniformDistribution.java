package edu.uah.math.psol.distributions;

/**
 *  This class models the uniform distribution on a specified interval.
 *
 *  
 *  
 */
public class ContinuousUniformDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    private double minValue, maxValue;

    /**
     *  This general constructor creates a new uniform distribution on a
     *  specified interval.
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     */
    public ContinuousUniformDistribution( double a, double b ) {
        setParameters( a, b );
    }

    /**
     *  This default constructor creates a new uniform distribuiton on (0, 1).
     */
    public ContinuousUniformDistribution() {
        this( 0, 1 );
    }

    /**
     *  This method sets the parameters: the minimum and maximum values of the
     *  interval.
     *
     * @param  a  The new parameters value
     * @param  b  The new parameters value
     */
    public void setParameters( double a, double b ) {
        minValue = a;
        maxValue = b;
        double step = 0.01 * ( maxValue - minValue );
        super.setParameters( minValue, maxValue, step, CONTINUOUS );
    }

    /**
     *  This method computes the density function.
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( minValue <= x & x <= maxValue )
            return 1 / ( maxValue - minValue );
        else
            return 0;
    }

    /**
     *  This method computes the maximum value of the getDensity function.
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return 1 / ( maxValue - minValue );
    }

    /**
     *  This method computes the mean.
     *
     * @return    The mean value
     */
    public double getMean() {
        return ( minValue + maxValue ) / 2;
    }

    /**
     *  This method computes the variance.
     *
     * @return    The variance value
     */
    public double getVariance() {
        return ( maxValue - minValue ) * ( maxValue - minValue ) / 12;
    }

    /**
     *  This method computes the cumulative distribution function.
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        if ( x < minValue )
            return 0;
        else if ( x >= maxValue )
            return 1;
        else
            return ( x - minValue ) / ( maxValue - minValue );
    }

    /**
     *  This method computes the getQuantile function.
     *
     * @param  p  Description of the Parameter
     * @return    The quantile value
     */
    public double getQuantile( double p ) {
        if ( p < 0 )
            p = 0;
        else if ( p > 1 )
            p = 1;
        return minValue + ( maxValue - minValue ) * p;
    }

    /**
     *  This method gets the minimum value.
     *
     * @return    The minValue value
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     *  This method returns the maximum value.
     *
     * @return    The maxValue value
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     *  This method simulates a value from the distribution.
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        return minValue + Math.random() * ( maxValue - minValue );
    }
}

