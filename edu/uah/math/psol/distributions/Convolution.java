package edu.uah.math.psol.distributions;

/**
 *  This class creates the n-fold convolution of a given distribution
 *
 *  
 *  
 */
public class Convolution extends Distribution {
    /**
     *  Description of the Field
     */
    private Distribution distribution;
    /**
     *  Description of the Field
     */
    private int power;
    /**
     *  Description of the Field
     */
    private double[][] pdf;

    /**
     *  This general constructor: creates a new convolution distribution
     *  corresponding to a specified distribution and convolution power
     *
     * @param  d  Description of the Parameter
     * @param  n  Description of the Parameter
     */
    public Convolution( Distribution d, int n ) {
        setParameters( d, n );
    }

    /**
     *  This defalut constructor creates a new convolution distribution
     *  corrrepsonding to the uniform distribution on (0,1), with convolution
     *  power 5.
     */
    public Convolution() {
        this( new ContinuousUniformDistribution( 0, 1 ), 5 );
    }

    /**
     *  This method sets the parameters: the distribution and convolution power.
     *  The method computes and store pdf values
     *
     * @param  d  The new parameters value
     * @param  n  The new parameters value
     */
    public void setParameters( Distribution d, int n ) {
        //Correct for invalid parameters
        if ( n < 1 )
            n = 1;
        distribution = d;
        power = n;
        Domain domain = distribution.getDomain();
        double l = domain.getLowerValue();
        double u = domain.getUpperValue();
        double w = domain.getWidth();
        double p;
        double dx;
        int t = distribution.getType();
        if ( t == DISCRETE )
            dx = 1;
        else
            dx = w;
        super.setParameters( power * l, power * u, w, t );
        int m = domain.getSize();
        pdf = new double[power][];
        for ( int k = 0; k < n; k++ )
            pdf[k] = new double[( k + 1 ) * m - k];
        for ( int j = 0; j < m; j++ )
            pdf[0][j] = distribution.getDensity( domain.getValue( j ) );
        for ( int k = 1; k < n; k++ )
            for ( int j = 0; j < ( k + 1 ) * m - k; j++ ) {
                p = 0;
                for ( int i = Math.max( 0, j - m + 1 ); i < Math.min( j + 1, k * m - k + 1 ); i++ )
                    p = p + pdf[k - 1][i] * pdf[0][j - i];

                pdf[k][j] = p;
            }

    }

    /**
     *  This method sets the convolution power.
     *
     * @param  n  The new power value
     */
    public void setPower( int n ) {
        setParameters( distribution, n );
    }

    /**
     *  This method sets the distribution.
     *
     * @param  d  The new distribution value
     */
    public void setDistribution( Distribution d ) {
        setParameters( d, power );
    }

    /**
     *  Density function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        return pdf[power - 1][getDomain().getIndex( x )];
    }

    /**
     *  Mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return power * distribution.getMean();
    }

    /**
     *  Variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        return power * distribution.getVariance();
    }

    /**
     *  This method returns the convolution power.
     *
     * @return    The power value
     */
    public int getPower() {
        return power;
    }

    /**
     *  This method returns the distribution.
     *
     * @return    The distribution value
     */
    public Distribution getDistribution() {
        return distribution;
    }

    /**
     *  Simulate a value from the distribution
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        double sum = 0;
        for ( int i = 0; i < power; i++ )
            sum = sum + distribution.simulate();
        return sum;
    }
}


