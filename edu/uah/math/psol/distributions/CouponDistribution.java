package edu.uah.math.psol.distributions;

/**
 *  This class models the distribution of the sample size needed to get a
 *  specified number of distinct sample values when sampling with replacement
 *  from a finite population of a specified size
 *
 *  
 *  
 */
public class CouponDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    int popSize, distinctValues, upperValue;
    /**
     *  Description of the Field
     */
    double[][] prob;

    /**
     *  This general constructor: creates a new coupon distribution with
     *  specified population size and distinct sample size.
     *
     * @param  m  Description of the Parameter
     * @param  k  Description of the Parameter
     */
    public CouponDistribution( int m, int k ) {
        setParameters( m, k );
    }

    /**
     *  This general constructor creates a new coupon distribution with
     *  population size 10 and distinct sample size 10.
     */
    public CouponDistribution() {
        this( 10, 10 );
    }


    /**
     *  This method sets the parameters: the population size and number of
     *  distinct values needed
     *
     * @param  m  The new parameters value
     * @param  k  The new parameters value
     */
    public void setParameters( int m, int k ) {
        int upperIndex;
        int maxIndex;
        //Correct for invalid parameters
        if ( m < 1 )
            m = 1;
        if ( k < 1 )
            k = 1;
        else if ( k > m )
            k = m;
        popSize = m;
        distinctValues = k;
        upperValue = ( int ) Math.ceil( getMean() + 4 * getSD() );
        super.setParameters( distinctValues, upperValue, 1, DISCRETE );
        prob = new double[upperValue + 1][popSize + 1];
        prob[0][0] = 1;
        prob[1][1] = 1;
        for ( int i = 1; i < upperValue; i++ ) {
            if ( i < popSize )
                upperIndex = i + 1;
            else
                upperIndex = popSize;
            for ( int n = 1; n <= upperIndex; n++ )
                prob[i + 1][n] = prob[i][n] * ( ( double ) n / popSize ) + prob[i][n - 1] * ( ( double ) ( popSize - n + 1 ) / popSize );

        }
    }

    /**
     *  Set the population size
     *
     * @param  m  The new popSize value
     */
    public void setPopSize( int m ) {
        setParameters( m, distinctValues );
    }

    /**
     *  Set the number of distinct values
     *
     * @param  k  The new distinctValues value
     */
    public void setDistinctValues( int k ) {
        setParameters( popSize, k );
    }

    /**
     *  Density function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int k = ( int ) ( Math.rint( x ) );
        if ( k < distinctValues | k > upperValue )
            return 0;
        else
            return ( ( double ) ( popSize - distinctValues + 1 ) / popSize ) * prob[k - 1][distinctValues - 1];
    }

    /**
     *  Mean
     *
     * @return    The mean value
     */
    public double getMean() {
        double sum = 0;
        for ( int i = 1; i <= distinctValues; i++ )
            sum = sum + ( double ) popSize / ( popSize - i + 1 );
        return sum;
    }

    /**
     *  Variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        double sum = 0;
        for ( int i = 1; i <= distinctValues; i++ )
            sum = sum + ( double ) ( popSize * ( i - 1 ) ) / ( ( popSize - i + 1 ) * ( popSize - i + 1 ) );
        return sum;
    }

    /**
     *  Get the population size
     *
     * @return    The popSize value
     */
    public double getPopSize() {
        return popSize;
    }

    /**
     *  Get the number of distinct values
     *
     * @return    The distinctValues value
     */
    public double getDistinctValues() {
        return distinctValues;
    }

    /**
     *  Simulate a value from the distribution
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int[] cellCount = new int[( int ) popSize];
        double occupiedCells = 0;
        int ballCount = 0;
        while ( occupiedCells <= distinctValues ) {
            ballCount++;
            int ballIndex = ( int ) ( popSize * Math.random() );
            if ( cellCount[ballIndex] == 0 )
                occupiedCells++;
            cellCount[ballIndex] = cellCount[ballIndex]++;
        }
        return ballCount;
    }
}

