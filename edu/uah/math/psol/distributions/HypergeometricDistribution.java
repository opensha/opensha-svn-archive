package edu.uah.math.psol.distributions;

/**
 *  This class models the hypergeometric distribution with parameters m
 *  (population size), n (sample size), and r (number of type 1 objects)
 *
 *  
 *  
 */
public class HypergeometricDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    private int populationSize, sampleSize, type1Size;
    /**
     *  Description of the Field
     */
    double c;

    /**
     *  General constructor: creates a new hypergeometric distribution with
     *  specified values of the parameters
     *
     * @param  m  Description of the Parameter
     * @param  r  Description of the Parameter
     * @param  n  Description of the Parameter
     */
    public HypergeometricDistribution( int m, int r, int n ) {
        setParameters( m, r, n );
    }

    /**
     *  Default constructor: creates a new hypergeometric distribuiton with
     *  parameters m = 100, r = 50, n = 10
     */
    public HypergeometricDistribution() {
        this( 100, 50, 10 );
    }

    /**
     *  Set the parameters of the distribution
     *
     * @param  m  The new parameters value
     * @param  r  The new parameters value
     * @param  n  The new parameters value
     */
    public void setParameters( int m, int r, int n ) {
        //Correct for invalid parameters
        if ( m < 1 )
            m = 1;
        if ( r < 0 )
            r = 0;
        else if ( r > m )
            r = m;
        if ( n < 0 )
            n = 0;
        else if ( n > m )
            n = m;
        //Assign parameter values
        populationSize = m;
        type1Size = r;
        sampleSize = n;
        c = comb( populationSize, sampleSize );
        super.setParameters( Math.max( 0, sampleSize - populationSize + type1Size ), Math.min( type1Size, sampleSize ), 1, DISCRETE );
    }

    /**
     *  Set population size
     *
     * @param  m  The new populationSize value
     */
    public void setPopulationSize( int m ) {
        setParameters( m, type1Size, sampleSize );
    }

    /**
     *  Set sub-population size
     *
     * @param  r  The new type1Size value
     */
    public void setType1Size( int r ) {
        setParameters( populationSize, r, sampleSize );
    }

    /**
     *  Set sample size
     *
     * @param  n  The new sampleSize value
     */
    public void setSampleSize( int n ) {
        setParameters( populationSize, type1Size, n );
    }

    /**
     *  Density function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int k = ( int ) Math.rint( x );
        return comb( type1Size, k ) * comb( populationSize - type1Size, sampleSize - k ) / c;
    }

    /**
     *  Maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode = Math.floor( ( ( double ) ( sampleSize + 1 ) * ( type1Size + 1 ) ) / ( populationSize + 2 ) );
        return getDensity( mode );
    }

    /**
     *  Mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return ( double ) sampleSize * type1Size / populationSize;
    }

    /**
     *  Variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        return ( double ) sampleSize * type1Size * ( populationSize - type1Size ) *
                ( populationSize - sampleSize ) / ( populationSize * populationSize * ( populationSize - 1 ) );
    }

    /**
     *  Get population size
     *
     * @return    The populationSize value
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     *  Get sub-population size
     *
     * @return    The type1Size value
     */
    public int getType1Size() {
        return type1Size;
    }

    /**
     *  Get sample size
     *
     * @return    The sampleSize value
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     *  Simulate a value from the distribution
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int j;
        int k;
        int u;
        int m0;
        double x = 0;
        m0 = ( int ) populationSize;
        int[] b = new int[m0];
        for ( int i = 0; i < m0; i++ )
            b[i] = i;
        for ( int i = 0; i < sampleSize; i++ ) {
            k = m0 - i;
            u = ( int ) ( k * Math.random() );
            if ( u < type1Size )
                x = x + 1;
            j = b[k - 1];
            b[k - 1] = b[u];
            b[u] = j;
        }
        return x;
    }
}

