package edu.uah.math.psol.distributions;

/**
 *  This class defines the chi-square distribution with a specifed degrees of
 *  freedom
 *
 *  
 *  
 */
public class ChiSquareDistribution extends GammaDistribution {
    /**
     *  Description of the Field
     */
    int degrees;

    /**
     *  This general constructor creates a new chi-square distribuiton with a
     *  specified degrees of freedom parameter
     *
     * @param  n  Description of the Parameter
     */
    public ChiSquareDistribution( int n ) {
        setDegrees( n );
    }

    /**
     *  Constructor for the ChiSquareDistribution object
     */
    public ChiSquareDistribution() {
        this( 1 );
    }

    /**
     *  This method sets the degrees of freedom
     *
     * @param  n  The new degrees value
     */
    public void setDegrees( int n ) {
        //Correct invalid parameter
        if ( n <= 0 )
            n = 1;
        degrees = n;
        super.setParameters( 0.5 * degrees, 2 );
    }

    /**
     *  This method returns the degrees of freedom
     *
     * @return    The degrees value
     */
    public int getDegrees() {
        return degrees;
    }

    /**
     *  This method simulates a value from the distribuiton, as the sum of
     *  squares of independent, standard normal distribution
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        double V;
        double Z;
        double r;
        double theta;
        V = 0;
        for ( int i = 1; i <= degrees; i++ ) {
            r = Math.sqrt( -2 * Math.log( Math.random() ) );
            theta = 2 * Math.PI * Math.random();
            Z = r * Math.cos( theta );
            V = V + Z * Z;
        }
        return V;
    }
}

