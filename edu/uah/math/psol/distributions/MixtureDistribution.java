package edu.uah.math.psol.distributions;

/**
 *  Description of the Class
 *
 *  
 *  
 */
public class MixtureDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    Distribution[] dist;
    /**
     *  Description of the Field
     */
    int n, type;
    /**
     *  Description of the Field
     */
    double minValue, maxValue, lowerValue, upperValue, stepSize;
    /**
     *  Description of the Field
     */
    double[] prob;

    //Constructors
    /**
     *  Constructor for the MixtureDistribution object
     *
     * @param  d  Description of the Parameter
     * @param  p  Description of the Parameter
     */
    public MixtureDistribution( Distribution[] d, double[] p ) {
        setParameters( d, p );
    }

    /**
     *  Constructor for the MixtureDistribution object
     *
     * @param  d0  Description of the Parameter
     * @param  d1  Description of the Parameter
     * @param  a   Description of the Parameter
     */
    public MixtureDistribution( Distribution d0, Distribution d1, double a ) {
        setParameters( d0, d1, a );
    }

    /**
     *  Sets the parameters attribute of the MixtureDistribution object
     *
     * @param  d  The new parameters value
     * @param  p  The new parameters value
     */
    public void setParameters( Distribution[] d, double[] p ) {
        double minLower = Double.POSITIVE_INFINITY;
        double maxUpper = Double.NEGATIVE_INFINITY;
        double minWidth = Double.POSITIVE_INFINITY;
        double a;
        double b;
        double w;
        dist = d;
        prob = p;
        int t0 = dist[0].getType();
        int t;
        n = dist.length;
        boolean mixed = false;
        for ( int i = 0; i < n; i++ ) {
            t = dist[i].getType();
            if ( t == DISCRETE )
                a = dist[i].getDomain().getLowerValue();
            else
                a = dist[i].getDomain().getLowerBound();
            if ( a < minLower )
                minLower = a;
            if ( t == DISCRETE )
                b = dist[i].getDomain().getUpperValue();
            else
                b = dist[i].getDomain().getUpperBound();
            if ( b > maxUpper )
                maxUpper = b;
            w = dist[i].getDomain().getWidth();
            if ( w < minWidth )
                minWidth = w;
            if ( t != t0 )
                mixed = true;
        }
        if ( mixed )
            t = 2;
        else
            t = t0;
        super.setParameters( minLower, maxUpper, minWidth, t );
    }

    /**
     *  Sets the parameters attribute of the MixtureDistribution object
     *
     * @param  d0  The new parameters value
     * @param  d1  The new parameters value
     * @param  a   The new parameters value
     */
    public void setParameters( Distribution d0, Distribution d1, double a ) {
        setParameters( new Distribution[]{d0, d1}, new double[]{1 - a, a} );
    }

    //Density
    /**
     *  Gets the density attribute of the MixtureDistribution object
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        double d = 0;
        for ( int i = 0; i < n; i++ )
            d = d + prob[i] * dist[i].getDensity( x );
        return d;
    }

    //Mean
    /**
     *  Gets the mean attribute of the MixtureDistribution object
     *
     * @return    The mean value
     */
    public double getMean() {
        double sum = 0;
        for ( int i = 0; i < n; i++ )
            sum = sum + prob[i] * dist[i].getMean();
        return sum;
    }

    //Variance
    /**
     *  Gets the variance attribute of the MixtureDistribution object
     *
     * @return    The variance value
     */
    public double getVariance() {
        double sum = 0;
        double mu = getMean();
        double m;
        for ( int i = 0; i < n; i++ ) {
            m = dist[i].getMean();
            sum = sum + prob[i] * ( dist[i].getVariance() + m * m );
        }
        return sum - mu * mu;
    }

    //Simulate
    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        double sum = 0;
        double p = Math.random();
        int i = -1;
        while ( sum < p & i < n ) {
            sum = sum + prob[i];
            i = i + 1;
        }
        return dist[i].simulate();
    }
}

