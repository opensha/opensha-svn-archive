package edu.uah.math.psol.distributions;

/**
 *  The binomial distribution with a random number of trials
 *
 *  
 *  
 */
public class BinomialRandomNDistribution extends Distribution {
    //Variables
    /**
     *  Description of the Field
     */
    double probability, sum;
    /**
     *  Description of the Field
     */
    Distribution dist;

    /**
     *  This general constructor creates a new randomized binomial distribution
     *  with a specified probability of success and a specified distribution for
     *  the number of trials
     *
     * @param  d  Description of the Parameter
     * @param  p  Description of the Parameter
     */
    public BinomialRandomNDistribution( Distribution d, double p ) {
        setParameters( d, p );
    }

    /**
     *  Special constructor: creates a new randomized binomial distribution with
     *  a specified probability of success and the uniform distribution on {1,
     *  2, 3, 4, 5, 6} for the number of trials
     *
     * @param  p  Description of the Parameter
     */
    public BinomialRandomNDistribution( double p ) {
        this( new DiscreteUniformDistribution( 1, 6, 1 ), p );
    }

    /**
     *  This default constructor: creates a new randomized binomial distribution
     *  with probability of success 0.5 and the uniform distribution on {1, 2,
     *  3, 4, 5, 6} for the number of trials
     */
    public BinomialRandomNDistribution() {
        this( new DiscreteUniformDistribution( 1, 6, 1 ), 0.5 );
    }

    /**
     *  Set the parameters: the distribution for the number of trials and the
     *  probability of success
     *
     * @param  d  The new parameters value
     * @param  p  The new parameters value
     */
    public void setParameters( Distribution d, double p ) {
        dist = d;
        probability = p;
        super.setParameters( 0, dist.getDomain().getUpperValue(), 1, DISCRETE );
    }

    //Density
    /**
     *  Gets the density attribute of the BinomialRandomNDistribution object
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int k = ( int ) Math.rint( x );
        double trials;
        if ( probability == 0 ) {
            if ( k == 0 )
                return 1;
            else
                return 0;
        }
        else if ( probability == 1 )
            return dist.getDensity( k );
        else {
            sum = 0;
            for ( int i = 0; i < dist.getDomain().getSize(); i++ ) {
                trials = dist.getDomain().getValue( i );
                sum = sum + dist.getDensity( trials ) *
                        comb( trials, k ) * Math.pow( probability, k ) * Math.pow( 1 - probability, trials - k );
            }
            return sum;
        }
    }

    /**
     *  Gets the mean attribute of the BinomialRandomNDistribution object
     *
     * @return    The mean value
     */
    public double getMean() {
        return dist.getMean() * probability;
    }

    /**
     *  Gets the variance attribute of the BinomialRandomNDistribution object
     *
     * @return    The variance value
     */
    public double getVariance() {
        return dist.getMean() * probability * ( 1 - probability ) + dist.getVariance() * probability * probability;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int trials = ( int ) dist.simulate();
        int successes = 0;
        for ( int i = 1; i <= trials; i++ )
            if ( Math.random() < probability )
                successes++;

        return successes;
    }

}


