package edu.uah.math.psol.distributions;

/**
 *  The geometric distribution with parameter p
 *
 *  
 *  
 */
public class GeometricDistribution extends NegativeBinomialDistribution {

    /**
     *  General Constructor: creates a new geometric distribution with parameter
     *  p
     *
     * @param  p  Description of the Parameter
     */
    public GeometricDistribution( double p ) {
        super( 1, p );
    }

    /**
     *  Default Constructor: creates a new geometric distribution with parameter
     *  0.5
     */
    public GeometricDistribution() {
        this( 0.5 );
    }

    /**
     *  Override set parameters
     *
     * @param  k  The new parameters value
     * @param  p  The new parameters value
     */
    public void setParameters( int k, double p ) {
        super.setParameters( 1, p );
    }

    /**
     *  Override set successes
     *
     * @param  k  The new successes value
     */
    public void setSuccesses( int k ) { }
}

