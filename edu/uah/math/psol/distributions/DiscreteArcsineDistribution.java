package edu.uah.math.psol.distributions;

/**
 *  This class models the discrete arcsine distribution that governs the last
 *  zero in a symmetric random walk on an interval.
 *
 *  
 *  
 */
public class DiscreteArcsineDistribution extends Distribution {
    //Paramters
    /**
     *  Description of the Field
     */
    private int parameter;

    /**
     *  This general constructor creates a new discrete arcsine distribution
     *  with a specified number of steps.
     *
     * @param  n  Description of the Parameter
     */
    public DiscreteArcsineDistribution( int n ) {
        setParameter( n );
    }

    /**
     *  This default constructor creates a new discrete arcsine distribution
     *  with 10 steps.
     */
    public DiscreteArcsineDistribution() {
        this( 10 );
    }

    /**
     *  This method sets the parameter, the number of steps.
     *
     * @param  n  The new parameter value
     */
    public void setParameter( int n ) {
        parameter = n;
        setParameters( 0, parameter, 2, DISCRETE );
    }

    /**
     *  This method computes the density function.
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int k = ( int ) x;
        return comb( k, k / 2 ) * comb( parameter - k, ( parameter - k ) / 2 ) / Math.pow( 2, parameter );
    }

    /**
     *  This method computes the maximum value of the density function.
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return getDensity( 0 );
    }

    /**
     *  This method gets the parameter, the number of steps.
     *
     * @return    The parameter value
     */
    public int getParameter() {
        return parameter;
    }

    /**
     *  This method simulates a value from the distribution, by simulating a
     *  random walk on the interval.
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int step;
        int lastZero = 0;
        int position = 0;
        for ( int i = 1; i <= parameter; i++ ) {
            if ( Math.random() < 0.5 )
                step = 1;
            else
                step = -1;
            position = position + step;
            if ( position == 0 )
                lastZero = i;
        }
        return lastZero;
    }
}

