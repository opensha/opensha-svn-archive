package edu.uah.math.psol.distributions;

/**
 *  This class models the triangle distribution on a specified interval. If (X,
 *  Y) is uniformly distributed on a triangular region, then X and Y have
 *  triangular distribuitons.
 *
 *  
 *  
 */
public class TriangleDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    private int orientation;
    /**
     *  Description of the Field
     */
    private double c, minValue, maxValue;
    /**
     *  Description of the Field
     */
    public final static int UP = 0, DOWN = 1;

    /**
     *  This general constructor creates a new triangle distribution on a
     *  specified interval and with a specified orientation.
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @param  i  Description of the Parameter
     */
    public TriangleDistribution( double a, double b, int i ) {
        setParameters( a, b, i );
    }

    /**
     *  This default constructor creates a new triangle distribution on the
     *  interval (0, 1) with positive slope
     */
    public TriangleDistribution() {
        this( 0, 1, UP );
    }

    /**
     *  This method sets the parameters: the minimum value, maximum value, and
     *  orientation.
     *
     * @param  a  The new parameters value
     * @param  b  The new parameters value
     * @param  i  The new parameters value
     */
    public void setParameters( double a, double b, int i ) {
        minValue = a;
        maxValue = b;
        orientation = i;
        double stepSize = ( maxValue - minValue ) / 100;
        super.setParameters( minValue, maxValue, stepSize, CONTINUOUS );
        //Compute normalizing constant
        c = ( maxValue - minValue ) * ( maxValue - minValue );
    }

    //**This method computes the density.*/
    /**
     *  Gets the density attribute of the TriangleDistribution object
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( minValue <= x & x <= maxValue ) {
            if ( orientation == UP )
                return 2 * ( x - minValue ) / c;
            else
                return 2 * ( maxValue - x ) / c;
        }
        else
            return 0;
    }

    /**
     *  This method computes the maximum value of the getDensity function.
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode;
        if ( orientation == UP )
            mode = maxValue;
        else
            mode = minValue;
        return getDensity( mode );
    }

    /**
     *  This method computes the mean.
     *
     * @return    The mean value
     */
    public double getMean() {
        if ( orientation == UP )
            return minValue / 3 + 2 * maxValue / 3;
        else
            return 2 * minValue / 3 + maxValue / 3;
    }

    /**
     *  This method computes the variance.
     *
     * @return    The variance value
     */
    public double getVariance() {
        return ( maxValue - minValue ) * ( maxValue - minValue ) / 18;
    }

    /**
     *  This method returns the minimum value.
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
     *  This method returns the orientation.
     *
     * @return    The orientation value
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     *  This method computes the cumulative distribution function.
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        if ( orientation == UP )
            return ( x - minValue ) * ( x - minValue ) / c;
        else
            return 1 - ( maxValue - x ) * ( maxValue - x ) / c;
    }

    /**
     *  This method simulates a value from the distribution.
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        double u = minValue + ( maxValue - minValue ) * Math.random();
        double v = minValue + ( maxValue - minValue ) * Math.random();
        if ( orientation == UP )
            return Math.max( u, v );
        else
            return Math.min( u, v );
    }
}

