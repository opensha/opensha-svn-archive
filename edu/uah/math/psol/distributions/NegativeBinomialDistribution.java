package edu.uah.math.psol.distributions;

/**
 *  This class models the negative binomial distribution with specified
 *  successes parameter and probability parameter.
 *
 *  
 *  
 */
public class NegativeBinomialDistribution extends Distribution {
    //Paramters
    /**
     *  Description of the Field
     */
    private int successes;
    /**
     *  Description of the Field
     */
    private double probability;

    /**
     *  General Constructor: creates a new negative binomial distribution with
     *  given parameter values.
     *
     * @param  k  Description of the Parameter
     * @param  p  Description of the Parameter
     */
    public NegativeBinomialDistribution( int k, double p ) {
        setParameters( k, p );
    }

    /**
     *  Default Constructor: creates a new negative binomial distribution with
     *  successes parameter 1 and probability parameter 0.5,
     */
    public NegativeBinomialDistribution() {
        this( 1, 0.5 );
    }

    /**
     *  This method set the paramters and the set of values.
     *
     * @param  k  The new parameters value
     * @param  p  The new parameters value
     */
    public void setParameters( int k, double p ) {
        //Correct for invalid parameters
        if ( k < 1 )
            k = 1;
        if ( p <= 0 )
            p = 0.05;
        if ( p > 1 )
            p = 1;
        //Assign parameters
        successes = k;
        probability = p;
        //Set truncated values
        super.setParameters( successes, Math.ceil( getMean() + 4 * getSD() ), 1, DISCRETE );
    }

    /**
     *  Set the successes parameters
     *
     * @param  k  The new successes value
     */
    public void setSuccesses( int k ) {
        setParameters( k, probability );
    }

    /**
     *  Set the probability parameters
     *
     * @param  p  The new probability value
     */
    public void setProbability( double p ) {
        setParameters( successes, p );
    }

    /**
     *  Get the successes parameter
     *
     * @return    The successes value
     */
    public int getSuccesses() {
        return successes;
    }

    /**
     *  Get the probability parameter
     *
     * @return    The probability value
     */
    public double getProbability() {
        return probability;
    }

    /**
     *  Density function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int n = ( int ) Math.rint( x );
        if ( n < successes )
            return 0;
        else
            return comb( n - 1, successes - 1 ) * Math.pow( probability, successes )
                     * Math.pow( 1 - probability, n - successes );
    }

    /**
     *  Maximum value of getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode = ( successes - 1 ) / probability + 1;
        return getDensity( mode );
    }

    /**
     *  Mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return successes / probability;
    }

    /**
     *  Variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        return ( successes * ( 1 - probability ) ) / ( probability * probability );
    }

    /**
     *  Simulate a value
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int count = 0;
        int trials = 0;
        while ( count <= successes ) {
            if ( Math.random() < probability )
                count++;
            trials++;
        }
        return trials;
    }
}

