package edu.uah.math.psol.distributions;

/**
 *  The distribution of the number of matches in a random permutation
 *
 *  
 *  
 */
public class MatchDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    int parameter;
    /**
     *  Description of the Field
     */
    int[] b;

    /**
     *  This general constructor creates a new matching distribution with a
     *  specified parameter
     *
     * @param  n  Description of the Parameter
     */
    public MatchDistribution( int n ) {
        setParameter( n );
    }

    /**
     *  this default constructor creates a new mathcing distribuiton with
     *  parameter 5
     */
    public MatchDistribution() {
        this( 5 );
    }

    /**
     *  This method sets the parameter of the distribution (the size of the
     *  random permutation
     *
     * @param  n  The new parameter value
     */
    public void setParameter( int n ) {
        if ( n < 1 )
            n = 1;
        parameter = n;
        super.setParameters( 0, parameter, 1, DISCRETE );
        b = new int[n];
    }

    /**
     *  This method computes the getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int k = ( int ) Math.rint( x );
        double sum = 0;
        int sign = -1;
        for ( int j = 0; j <= parameter - k; j++ ) {
            sign = -sign;
            sum = sum + sign / factorial( j );
        }
        return sum / factorial( k );
    }

    /**
     *  This method gives the maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        if ( parameter == 2 )
            return getDensity( 0 );
        else
            return getDensity( 1 );
    }

    /**
     *  This method returns the mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return 1;
    }

    /**
     *  This method returns the variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        return 1;
    }

    /**
     *  This method gets the parameter
     *
     * @return    The parameter value
     */
    public int getParameter() {
        return parameter;
    }

    /**
     *  This method simulates a value from the distribution, by generating a
     *  random permutation and computing the number of matches
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        int j;
        int k;
        int u;
        double matches = 0;
        for ( int i = 0; i < parameter; i++ )
            b[i] = i + 1;
        for ( int i = 0; i < parameter; i++ ) {
            j = parameter - i;
            u = ( int ) ( j * Math.random() );
            if ( b[u] == i + 1 )
                matches = matches + 1;
            k = b[j - 1];
            b[j - 1] = b[u];
            b[u] = k;
        }
        return matches;
    }
}

