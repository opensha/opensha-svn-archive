package edu.uah.math.psol.distributions;

/**
 *  The binomial distribution with specified parameters: the number of trials
 *  and the probability of success
 *
 *  
 *  
 */
public class BinomialDistribution extends Distribution {
    //Variables
    /**
     *  Description of the Field
     */
    private int trials;
    /**
     *  Description of the Field
     */
    private double probability;

    /**
     *  General constructor: creates the binomial distribution with specified
     *  parameters
     *
     * @param  n  Description of the Parameter
     * @param  p  Description of the Parameter
     */
    public BinomialDistribution( int n, double p ) {
        setParameters( n, p );
    }

    /**
     *  Default constructor: creates the binomial distribution with 10 trials
     *  and probability of success 1/2
     */
    public BinomialDistribution() {
        this( 10, 0.5 );
    }

    /**
     *  Set the parameters
     *
     * @param  n  The new parameters value
     * @param  p  The new parameters value
     */
    public void setParameters( int n, double p ) {
        //Correct invalid parameters
        if ( n < 1 )
            n = 1;
        if ( p < 0 )
            p = 0;
        else if ( p > 1 )
            p = 1;
        trials = n;
        probability = p;
        super.setParameters( 0, trials, 1, DISCRETE );
    }

    /**
     *  Set the number of trails
     *
     * @param  n  The new trials value
     */
    public void setTrials( int n ) {
        setParameters( n, probability );
    }

    /**
     *  Set the probability of success
     *
     * @param  p  The new probability value
     */
    public void setProbability( double p ) {
        setParameters( trials, p );
    }

    /**
     *  Get the number of trials
     *
     * @return    The trials value
     */
    public int getTrials() {
        return trials;
    }

    /**
     *  Get the probability of success
     *
     * @return    The probability value
     */
    public double getProbability() {
        return probability;
    }

    /**
     *  Define the binomial getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int k = ( int ) Math.rint( x );
        if ( k < 0 | k > trials )
            return 0;
        if ( probability == 0 ) {
            if ( k == 0 )
                return 1;
            else
                return 0;
        }
        else if ( probability == 1 ) {
            if ( k == trials )
                return 1;
            else
                return 0;
        }
        else
            return comb( trials, k ) * Math.pow( probability, k ) * Math.pow( 1 - probability, trials - k );
    }

    /**
     *  Specify the maximum getDensity
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode = Math.min( Math.floor( ( trials + 1 ) * probability ), trials );
        return getDensity( mode );
    }

    /**
     *  Give the mean in closed form
     *
     * @return    The mean value
     */
    public double getMean() {
        return trials * probability;
    }

    /**
     *  Specify the variance in close form
     *
     * @return    The variance value
     */
    public double getVariance() {
        return trials * probability * ( 1 - probability );
    }

    /**
     *  Specify the CDF in terms of the beta CDF
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        if ( x < 0 )
            return 0;
        else if ( x >= trials )
            return 1;
        else
            return 1 - betaCDF( probability, x + 1, trials - x );
    }

    /**
     *  Simulate the binomial distribution as the number of successes in n
     *  trials
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int successes = 0;
        for ( int i = 1; i <= trials; i++ )
            if ( Math.random() < probability )
                successes++;

        return successes;
    }
}

