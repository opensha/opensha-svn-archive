package edu.uah.math.psol.distributions;

/**
 *  This class models the distribution of the maximum value of a symmetric
 *  random walk on the interval [0, n].
 *
 *  
 *  
 */
public class WalkMaxDistribution extends Distribution {
    //Paramters
    /**
     *  Description of the Field
     */
    private int steps;

    /**
     *  This general constructor creates a new max walk distribution with a
     *  specified time parameter.
     *
     * @param  n  Description of the Parameter
     */
    public WalkMaxDistribution( int n ) {
        setSteps( n );
    }

    /**
     *  This default constructor creates a new walk max distribution with time
     *  parameter 10.
     */
    public WalkMaxDistribution() {
        this( 10 );
    }

    /**
     *  This method sets the time parameter.
     *
     * @param  n  The new steps value
     */
    public void setSteps( int n ) {
        if ( n < 1 )
            n = 1;
        steps = n;
        super.setParameters( 0, steps, 1, DISCRETE );
    }

    /**
     *  This method defines the density function.
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int k = ( int ) Math.rint( x );
        int m;
        if ( ( k + steps ) % 2 == 0 )
            m = ( k + steps ) / 2;
        else
            m = ( k + steps + 1 ) / 2;
        return comb( steps, m ) / Math.pow( 2, steps );
    }

    /**
     *  This method returns the maximum value of the density function.
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return getDensity( 0 );
    }

    /**
     *  This method returns the number ofsteps.
     *
     * @return    The steps value
     */
    public double getSteps() {
        return steps;
    }

    /**
     *  This method simulates a value from the distribution.
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int step;
        int max = 0;
        int position = 0;
        for ( int i = 1; i <= steps; i++ ) {
            if ( Math.random() < 0.5 )
                step = 1;
            else
                step = -1;
            position = position + step;
            if ( position > max )
                max = position;
        }
        return max;
    }
}

