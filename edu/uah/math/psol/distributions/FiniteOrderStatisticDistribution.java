package edu.uah.math.psol.distributions;

/**
 *  This class models the distribution of the k'th order statistic for a sample
 *  of size n chosen without replacement from {1, 2, ..., N} .
 *
 *  
 *  
 */
public class FiniteOrderStatisticDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    Distribution dist;
    /**
     *  Description of the Field
     */
    private int sampleSize, populationSize, order;

    /**
     *  This general constructor creates a new finite order statistic
     *  distribution with specified population and sample sizes, and specified
     *  order.
     *
     * @param  N  Description of the Parameter
     * @param  n  Description of the Parameter
     * @param  k  Description of the Parameter
     */
    public FiniteOrderStatisticDistribution( int N, int n, int k ) {
        setParameters( N, n, k );
    }

    /**
     *  This default constructor creates a new finite order statistic
     *  distribution with population size 50, sample size 10, and order 5.
     */
    public FiniteOrderStatisticDistribution() {
        this( 50, 10, 5 );
    }

    /**
     *  This method sets the parameters: the sample size, population size, and
     *  order.
     *
     * @param  N  The new parameters value
     * @param  n  The new parameters value
     * @param  k  The new parameters value
     */
    public void setParameters( int N, int n, int k ) {
        populationSize = N;
        sampleSize = n;
        order = k;
        super.setParameters( order, populationSize - sampleSize + order, 1, Distribution.DISCRETE );
    }

    /**
     *  This method sets the population size.
     *
     * @param  N  The new populationSize value
     */
    public void setPopulationSize( int N ) {
        setParameters( N, sampleSize, order );
    }

    /**
     *  This method sets the sample size.
     *
     * @param  n  The new sampleSize value
     */
    public void setSampleSize( int n ) {
        setParameters( populationSize, n, order );
    }

    /**
     *  This method sets the order.
     *
     * @param  k  The new order value
     */
    public void setOrder( int k ) {
        setParameters( populationSize, sampleSize, k );
    }

    /**
     *  This method computes the getDensity.
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int i = ( int ) Math.rint( x );
        return comb( i - 1, order - 1 )
                 * comb( populationSize - i, sampleSize - order ) / comb( populationSize, sampleSize );
    }

    /**
     *  This method computes the mean.
     *
     * @return    The mean value
     */
    public double getMean() {
        return ( double ) order * ( populationSize + 1 ) / ( sampleSize + 1 );
    }


    /**
     *  This method computes the variance.
     *
     * @return    The variance value
     */
    public double getVariance() {
        return ( double ) ( populationSize + 1 ) * ( populationSize - sampleSize )
                 * order * ( sampleSize + 1 - order ) / ( ( sampleSize + 1 ) * ( sampleSize + 1 ) * ( sampleSize + 2 ) );
    }

    /**
     *  This method returns the population size.
     *
     * @return    The populationSize value
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     *  This method returns the sampleSize.
     *
     * @return    The sampleSize value
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     *  This method returns the order.
     *
     * @return    The order value
     */
    public int getOrder() {
        return order;
    }
}

