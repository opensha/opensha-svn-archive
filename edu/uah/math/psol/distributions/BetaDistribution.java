package edu.uah.math.psol.distributions;

/**
 *  A Java implmentation of the beta distribution with specified left and right
 *  parameters
 *
 *  
 *  
 */
public class BetaDistribution extends Distribution {
    //Parameters
    /**
     *  Description of the Field
     */
    private double left, right, c;

    /**
     *  General Constructor: creates a beta distribution with specified left and
     *  right parameters
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     */
    public BetaDistribution( double a, double b ) {
        setParameters( a, b );
    }

    /**
     *  Default constructor: creates a beta distribution with left and right
     *  parameters equal to 1
     */
    public BetaDistribution() {
        this( 1, 1 );
    }

    /**
     *  Set the parameters, compute the normalizing constant c, and specifies
     *  the interval and partition
     *
     * @param  a  The new parameters value
     * @param  b  The new parameters value
     */
    public void setParameters( double a, double b ) {
        double lower;
        double upper;
        double step;
        //Correct parameters that are out of bounds
        if ( a <= 0 )
            a = 1;
        if ( b <= 0 )
            b = 1;
        //Assign parameters
        left = a;
        right = b;
        //Compute the normalizing constant
        c = logGamma( left + right ) - logGamma( left ) - logGamma( right );
        //Specifiy the interval and partiton
        super.setParameters( 0, 1, 0.01, CONTINUOUS );
    }

    /**
     *  Sets the left parameter
     *
     * @param  a  The new left value
     */
    public void setLeft( double a ) {
        setParameters( a, right );
    }

    /**
     *  Sets the right parameter
     *
     * @param  b  The new right value
     */
    public void setRight( double b ) {
        setParameters( left, b );
    }

    /**
     *  Get the left paramter
     *
     * @return    The left value
     */
    public double getLeft() {
        return left;
    }

    /**
     *  Get the right parameter
     *
     * @return    The right value
     */
    public double getRight() {
        return right;
    }

    /**
     *  Define the beta getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( ( x < 0 ) | ( x > 1 ) )
            return 0;
        else if ( ( x == 0 ) & ( left == 1 ) )
            return right;
        else if ( ( x == 0 ) & ( left < 1 ) )
            return Double.POSITIVE_INFINITY;
        else if ( ( x == 0 ) & ( left > 1 ) )
            return 0;
        else if ( ( x == 1 ) & ( right == 1 ) )
            return left;
        else if ( ( x == 1 ) & ( right < 1 ) )
            return Double.POSITIVE_INFINITY;
        else if ( ( x == 1 ) & ( right > 1 ) )
            return 0;
        else
            return Math.exp( c + ( left - 1 ) * Math.log( x ) + ( right - 1 ) * Math.log( 1 - x ) );
    }

    /**
     *  Compute the maximum getDensity
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode;
        if ( left < 1 )
            mode = 0.01;
        else if ( right <= 1 )
            mode = 0.99;
        else
            mode = ( left - 1 ) / ( left + right - 2 );
        return getDensity( mode );
    }

    /**
     *  Compute the mean in closed form
     *
     * @return    The mean value
     */
    public double getMean() {
        return left / ( left + right );
    }

    /**
     *  Compute the variance in closed form
     *
     * @return    The variance value
     */
    public double getVariance() {
        return left * right / ( ( left + right ) * ( left + right ) * ( left + right + 1 ) );
    }

    /**
     *  Compute the cumulative distribution function. The beta CDF is built into
     *  the superclass Distribution
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        return betaCDF( x, left, right );
    }
}

