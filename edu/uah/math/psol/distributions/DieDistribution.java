package edu.uah.math.psol.distributions;

/**
 *  Distribution for a standard 6-sided die
 *
 *  
 *  
 */
public class DieDistribution extends FiniteDistribution {
    /**
     *  Description of the Field
     */
    public final static int FAIR = 0, FLAT16 = 1, FLAT25 = 2, FLAT34 = 3, LEFT = 4, RIGHT = 5;

    /**
     *  General Constructor: creates a new die distribution with specified
     *  probabilities
     *
     * @param  p  Description of the Parameter
     */
    public DieDistribution( double[] p ) {
        super( 1, 6, 1, p );
    }

    /**
     *  Special constructor: creates a new die distribution of a special type
     *
     * @param  n  Description of the Parameter
     */
    public DieDistribution( int n ) {
        super( 1, 6, 1 );
        setProbabilities( n );
    }

    /**
     *  Default constructor: creates a new fair die distribution
     */
    public DieDistribution() {
        this( FAIR );
    }

    /**
     *  Specify probabilities of a special type
     *
     * @param  n  The new probabilities value
     */
    public void setProbabilities( int n ) {
        if ( n == FLAT16 )
            setProbabilities( new double[]{1.0 / 4, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 4} );
        else if ( n == FLAT25 )
            setProbabilities( new double[]{1.0 / 8, 1.0 / 4, 1.0 / 8, 1.0 / 8, 1.0 / 4, 1.0 / 8} );
        else if ( n == FLAT34 )
            setProbabilities( new double[]{1.0 / 8, 1.0 / 8, 1.0 / 4, 1.0 / 4, 1.0 / 8, 1.0 / 8} );
        else if ( n == LEFT )
            setProbabilities( new double[]{1.0 / 21, 2.0 / 21, 3.0 / 21, 4.0 / 21, 5.0 / 21, 6.0 / 21} );
        else if ( n == RIGHT )
            setProbabilities( new double[]{6.0 / 21, 5.0 / 21, 4.0 / 21, 3.0 / 21, 2.0 / 21, 1.0 / 21} );
        else
            setProbabilities( new double[]{1.0 / 6, 1.0 / 6, 1.0 / 6, 1.0 / 6, 1.0 / 6, 1.0 / 6} );
    }
}

