package edu.uah.math.psol.distributions;

/**
 *  This class models the Cauchy distribution
 *
 *  
 *  
 */
public class CauchyDistribution extends StudentDistribution {
    //Constructor
    /**
     *  Constructor for the CauchyDistribution object
     */
    public CauchyDistribution() {
        super( 1 );
    }

    /**
     *  This method sets the degrees of freedom to 1.
     *
     * @param  n  The new degrees value
     */
    public void setDegrees( int n ) {
        super.setDegrees( 1 );
    }

    /**
     *  This method computes the CDF. This overrides the corresponding method in
     *  StudentDistribution.
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        return 0.5 + Math.atan( x ) / Math.PI;
    }

    /**
     *  This method computes the quantile function. This overrides the
     *  corresponding method in StudentDistribution.
     *
     * @param  p  Description of the Parameter
     * @return    The quantile value
     */
    public double getQuantile( double p ) {
        return Math.tan( Math.PI * ( p - 0.5 ) );
    }
}

