package edu.uah.math.psol.distributions;

/**
 *  The distribution of the order statistic of a specified order from a random
 *  sample of a specified size from a specified sampling distribution
 *
 *  
 *  
 */
public class OrderStatisticDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    Distribution dist;
    /**
     *  Description of the Field
     */
    int sampleSize, order;

    /**
     *  General constructor: creates a new order statistic distribution
     *  corresponding to a specified sampling distribution, sample size, and
     *  order
     *
     * @param  d  Description of the Parameter
     * @param  n  Description of the Parameter
     * @param  k  Description of the Parameter
     */
    public OrderStatisticDistribution( Distribution d, int n, int k ) {
        setParameters( d, n, k );
    }

    /**
     *  Set the parameters: the sampling distribution, sample size, and order
     *
     * @param  d  The new parameters value
     * @param  n  The new parameters value
     * @param  k  The new parameters value
     */
    public void setParameters( Distribution d, int n, int k ) {
        //Correct for invalid parameters
        if ( n < 1 )
            n = 1;
        if ( k < 1 )
            k = 1;
        else if ( k > n )
            k = n;
        //Assign parameters
        dist = d;
        sampleSize = n;
        order = k;
        int t = dist.getType();
        Domain domain = dist.getDomain();
        if ( t == DISCRETE )
            super.setParameters( domain.getLowerValue(), domain.getUpperValue(), domain.getWidth(), t );
        else
            super.setParameters( domain.getLowerBound(), domain.getUpperBound(), domain.getWidth(), t );
    }

    /**
     *  Density function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        double p = dist.getCDF( x );
        if ( dist.getType() == DISCRETE )
            return getCDF( x ) - getCDF( x - getDomain().getWidth() );
        else
            return order * comb( sampleSize, order ) * Math.pow( p, order - 1 ) * Math.pow( 1 - p, sampleSize - order ) * dist.getDensity( x );
    }

    /**
     *  Cumulative distribution function
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        double sum = 0;
        double p = dist.getCDF( x );
        for ( int j = order; j <= sampleSize; j++ )
            sum = sum + comb( sampleSize, j ) * Math.pow( p, j ) * Math.pow( 1 - p, sampleSize - j );
        return sum;
    }
}

