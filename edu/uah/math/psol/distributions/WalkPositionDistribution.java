package edu.uah.math.psol.distributions;

/**
 *  Description of the Class
 *
 *  
 *  
 */
public class WalkPositionDistribution extends Distribution {
    //Paramters
    /**
     *  Description of the Field
     */
    private int steps;
    /**
     *  Description of the Field
     */
    private double probability;

    /**
     *  This general constructor creates a new distribution with specified time
     *  and probability parameters.
     *
     * @param  n  Description of the Parameter
     * @param  p  Description of the Parameter
     */
    public WalkPositionDistribution( int n, double p ) {
        setParameters( n, p );
    }

    /**
     *  This default constructor creates a new WalkPositionDistribution with
     *  time parameter 10 and probability p.
     */
    public WalkPositionDistribution() {
        this( 10, 0.5 );
    }

    /**
     *  This method sets the time and probability parameters.
     *
     * @param  n  The new parameters value
     * @param  p  The new parameters value
     */
    public void setParameters( int n, double p ) {
        if ( n < 0 )
            n = 0;
        if ( p < 0 )
            p = 0;
        else if ( p > 1 )
            p = 1;
        steps = n;
        probability = p;
        super.setParameters( -steps, steps, 2, DISCRETE );
    }

    /**
     *  This method computes the density function.
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int k = ( int ) Math.rint( x );
        int m = ( k + steps ) / 2;
        return comb( steps, m ) * Math.pow( probability, m ) * Math.pow( 1 - probability, steps - m );
    }

    /**
     *  This method returns the maximum value of the density function.
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode = 2 * Math.min( Math.floor( ( steps + 1 ) * probability ), steps ) - steps;
        return getDensity( mode );
    }

    /**
     *  This method computes the mean.
     *
     * @return    The mean value
     */
    public double getMean() {
        return 2 * steps * probability - steps;
    }

    /**
     *  This method computes the variance.
     *
     * @return    The variance value
     */
    public double getVariance() {
        return 4 * steps * probability * ( 1 - probability );
    }

    /**
     *  This method returns the number of steps.
     *
     * @return    The steps value
     */
    public double getSteps() {
        return steps;
    }

    /**
     *  This method returns the probability of a step to the right.
     *
     * @return    The probability value
     */
    public double getProbability() {
        return probability;
    }

    /**
     *  This method simulates a value from the distribution.
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int step;
        int position = 0;
        for ( int i = 1; i <= steps; i++ ) {
            if ( Math.random() < probability )
                step = 1;
            else
                step = -1;
            position = position + step;
        }
        return position;
    }
}

