package edu.uah.math.psol.distributions;

/**
 *  Distribution: An abstract implmentation of a real probability distribution
 *
 *  
 *  
 */
public abstract class Distribution {
    //Constants
    /**
     *  Description of the Field
     */
    public final static int DISCRETE = 0, CONTINUOUS = 1, MIXED = 2;
    //Variables
    /**
     *  Description of the Field
     */
    private int type;
    //Objects
    /**
     *  Description of the Field
     */
    private Domain domain;

    /**
     *  This method defines a partition of an interval that acts as a default
     *  domain for the distribution, for purposes of data collection and for
     *  default computations. For a discrete distribution, the specified
     *  parameters define the midpoints of the partition (these are typically
     *  the values on which the distribution is defined, although truncated if
     *  the true set of values is infinite). For a continuous distribution, the
     *  parameters define the boundary points of the interval on which the
     *  distribuiton is defined (truncated if the true interval is infinite)
     *
     * @param  a  The new parameters value
     * @param  b  The new parameters value
     * @param  w  The new parameters value
     * @param  t  The new parameters value
     */
    public void setParameters( double a, double b, double w, int t ) {
        if ( t < 0 )
            t = 0;
        else if ( t > 2 )
            t = 2;
        type = t;
        if ( type == DISCRETE )
            domain = new Domain( a - 0.5 * w, b + 0.5 * w, w );
        else
            domain = new Domain( a, b, w );
    }

    /**
     *  The getDensity method is abstract and must be overridden for any
     *  specific distribuiton
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public abstract double getDensity( double x );

    /**
     *  This method returns the domain of the distribution.
     *
     * @return    The domain value
     */
    public Domain getDomain() {
        return domain;
    }

    /**
     *  This method returns the type of the distribution (discrete or
     *  continuous)
     *
     * @return    The type value
     */
    public final int getType() {
        return type;
    }

    /**
     *  This method returns the largest (finite) value of the getDensity
     *  function on the finite set of domain values. This method should be
     *  overridden if the maximum value is known in closed form
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double max = 0;
        double d;
        for ( int i = 0; i < domain.getSize(); i++ ) {
            d = getDensity( domain.getValue( i ) );
            if ( d > max & d < Double.POSITIVE_INFINITY )
                max = d;
        }
        return max;
    }

    /**
     *  This method returns a default approximate mean, based on the finite set
     *  of domain values. This method should be overriden if the mean is known
     *  in closed form
     *
     * @return    The mean value
     */
    public double getMean() {
        double sum = 0;
        double x;
        double w;
        if ( type == DISCRETE )
            w = 1;
        else
            w = domain.getWidth();
        for ( int i = 0; i < domain.getSize(); i++ ) {
            x = domain.getValue( i );
            sum = sum + x * getDensity( x ) * w;
        }
        return sum;
    }

    /**
     *  This method returns a default approximate variance. This method should
     *  be overriden if the variance is known in closed form
     *
     * @return    The variance value
     */
    public double getVariance() {
        double sum = 0;
        double mu = getMean();
        double x;
        double w;
        if ( type == DISCRETE )
            w = 1;
        else
            w = domain.getWidth();
        for ( int i = 0; i < domain.getSize(); i++ ) {
            x = domain.getValue( i );
            sum = sum + ( x - mu ) * ( x - mu ) * getDensity( x ) * w;
        }
        return sum;
    }

    /**
     *  This method returns the standard deviation, as the square root of the
     *  variance
     *
     * @return    The sD value
     */
    public double getSD() {
        return Math.sqrt( getVariance() );
    }

    /**
     *  This method returns a default approximate cumulative distribution
     *  function. This should be overriden if the CDF is known in closed form
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        double sum = 0;
        double w;
        double y;
        if ( type == DISCRETE )
            w = 1;
        else
            w = domain.getWidth();
        int j = domain.getIndex( x );
        if ( j < 0 )
            return 0;
        else if ( j >= domain.getSize() )
            return 1;
        else {
            for ( int i = 0; i <= j; i++ )
                sum = sum + getDensity( domain.getValue( i ) ) * w;
            if ( type == CONTINUOUS ) {
                y = domain.getValue( j ) - 0.5 * w;
                sum = sum + getDensity( ( x + y ) / 2 ) * ( x - y );
            }
        }
        return sum;
    }

    /**
     *  This method computes an approximate getQuantile function. This should be
     *  overriden if the getQuantile function is known in closed form
     *
     * @param  p  Description of the Parameter
     * @return    The quantile value
     */
    public double getQuantile( double p ) {
        double sum = 0;
        double x;
        double w;
        if ( type == DISCRETE )
            w = 1;
        else
            w = domain.getWidth();
        if ( p <= 0 )
            return domain.getLowerValue();
        else if ( p >= 1 )
            return domain.getUpperValue();
        else {
            int n = domain.getSize();
            int i = 0;
            x = domain.getValue( i );
            sum = getDensity( x ) * w;
            while ( ( sum < p ) & ( i < n ) ) {
                i++;
                x = domain.getValue( i );
                sum = sum + getDensity( x ) * w;
            }
            return x;
        }
    }

    /**
     *  This method computes a default approximate median. This method should be
     *  overriden when there is a closed form expression for the median.
     *
     * @return    The median value
     */
    public double getMedian() {
        return getQuantile( 0.5 );
    }

    /**
     *  This method computes the failure rate function
     *
     * @param  x  Description of the Parameter
     * @return    The failureRate value
     */
    public double getFailureRate( double x ) {
        return getDensity( x ) / ( 1 - getCDF( x ) );
    }

    /**
     *  This method computes a default simulation of a value from the
     *  distribution, as a random getQuantile. This method should be overridden
     *  if a better method of simulation is known.
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        return getQuantile( Math.random() );
    }

    //Class methods
    /**
     *  This method computes the number of permuatations of k objects chosen
     *  from a population of n objects.
     *
     * @param  n  Description of the Parameter
     * @param  k  Description of the Parameter
     * @return    Description of the Return Value
     */
    public static double perm( double n, int k ) {
        double prod;
        if ( k > n | k < 0 )
            return 0;
        else {
            prod = 1;
            for ( int i = 1; i <= k; i++ )
                prod = prod * ( n - i + 1 );
            return prod;
        }
    }

    /**
     *  This method computes k!, the number of permutations of k objects.
     *
     * @param  k  Description of the Parameter
     * @return    Description of the Return Value
     */
    public static double factorial( int k ) {
        return perm( k, k );
    }

    /**
     *  This method computes the number of combinations of k objects chosen from
     *  a population of n objects
     *
     * @param  n  Description of the Parameter
     * @param  k  Description of the Parameter
     * @return    Description of the Return Value
     */
    public static double comb( double n, int k ) {
        return perm( n, k ) / factorial( k );
    }

    /**
     *  This method computes the log of the gamma function.
     *
     * @param  x  Description of the Parameter
     * @return    Description of the Return Value
     */
    public static double logGamma( double x ) {
        double coef[] = {76.18009173, -86.50532033, 24.01409822, -1.231739516, 0.00120858003, -0.00000536382};
        double step = 2.50662827465;
        double fpf = 5.5;
        double t;
        double tmp;
        double ser;
        double logGamma;
        t = x - 1;
        tmp = t + fpf;
        tmp = ( t + 0.5 ) * Math.log( tmp ) - tmp;
        ser = 1;
        for ( int i = 1; i <= 6; i++ ) {
            t = t + 1;
            ser = ser + coef[i - 1] / t;
        }
        return tmp + Math.log( step * ser );
    }

    /**
     *  This method computes the gamma function.
     *
     * @param  x  Description of the Parameter
     * @return    Description of the Return Value
     */
    public static double gamma( double x ) {
        return Math.exp( logGamma( x ) );
    }

    /**
     *  This method computes the CDF of the gamma distribution with shape
     *  parameter a and scale parameter 1
     *
     * @param  x  Description of the Parameter
     * @param  a  Description of the Parameter
     * @return    Description of the Return Value
     */
    public static double gammaCDF( double x, double a ) {
        if ( x <= 0 )
            return 0;
        else if ( x < a + 1 )
            return gammaSeries( x, a );
        else
            return 1 - gammaCF( x, a );
    }

    /**
     *  This method computes a gamma series that is used in the gamma CDF.
     *
     * @param  x  Description of the Parameter
     * @param  a  Description of the Parameter
     * @return    Description of the Return Value
     */
    private static double gammaSeries( double x, double a ) {
        //Constants
        int maxit = 100;
        double eps = 0.0000003;
        //Variables
        double sum = 1.0 / a;
        //Variables
        double ap = a;
        //Variables
        double gln = logGamma( a );
        //Variables
        double del = sum;
        for ( int n = 1; n <= maxit; n++ ) {
            ap++;
            del = del * x / ap;
            sum = sum + del;
            if ( Math.abs( del ) < Math.abs( sum ) * eps )
                break;
        }
        return sum * Math.exp( -x + a * Math.log( x ) - gln );
    }

    /**
     *  This method computes a gamma continued fraction function function that
     *  is used in the gamma CDF.
     *
     * @param  x  Description of the Parameter
     * @param  a  Description of the Parameter
     * @return    Description of the Return Value
     */
    private static double gammaCF( double x, double a ) {
        //Constants
        int maxit = 100;
        double eps = 0.0000003;
        //Variables
        double gln = logGamma( a );
        //Variables
        double g = 0;
        //Variables
        double gOld = 0;
        //Variables
        double a0 = 1;
        //Variables
        double a1 = x;
        //Variables
        double b0 = 0;
        //Variables
        double b1 = 1;
        //Variables
        double fac = 1;
        double an;
        double ana;
        double anf;
        for ( int n = 1; n <= maxit; n++ ) {
            an = 1.0 * n;
            ana = an - a;
            a0 = ( a1 + a0 * ana ) * fac;
            b0 = ( b1 + b0 * ana ) * fac;
            anf = an * fac;
            a1 = x * a0 + anf * a1;
            b1 = x * b0 + anf * b1;
            if ( a1 != 0 ) {
                fac = 1.0 / a1;
                g = b1 * fac;
                if ( Math.abs( ( g - gOld ) / g ) < eps )
                    break;
                gOld = g;
            }
        }
        return Math.exp( -x + a * Math.log( x ) - gln ) * g;
    }

    /**
     *  The method computes the beta CDF.
     *
     * @param  x  Description of the Parameter
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @return    Description of the Return Value
     */
    public static double betaCDF( double x, double a, double b ) {
        double bt;
        if ( ( x == 0 ) | ( x == 1 ) )
            bt = 0;
        else
            bt = Math.exp( logGamma( a + b ) - logGamma( a ) - logGamma( b ) + a * Math.log( x ) + b * Math.log( 1 - x ) );
        if ( x < ( a + 1 ) / ( a + b + 2 ) )
            return bt * betaCF( x, a, b ) / a;
        else
            return 1 - bt * betaCF( 1 - x, b, a ) / b;
    }

    /**
     *  This method computes a beta continued fractions function that is used in
     *  the beta CDF.
     *
     * @param  x  Description of the Parameter
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @return    Description of the Return Value
     */
    private static double betaCF( double x, double a, double b ) {
        int maxit = 100;
        double eps = 0.0000003;
        double am = 1;
        double bm = 1;
        double az = 1;
        double qab = a + b;
        double
                qap = a + 1;
        double qam = a - 1;
        double bz = 1 - qab * x / qap;
        double tem;
        double em;
        double d;
        double bpp;
        double bp;
        double app;
        double aOld;
        double ap;
        for ( int m = 1; m <= maxit; m++ ) {
            em = m;
            tem = em + em;
            d = em * ( b - m ) * x / ( ( qam + tem ) * ( a + tem ) );
            ap = az + d * am;
            bp = bz + d * bm;
            d = -( a + em ) * ( qab + em ) * x / ( ( a + tem ) * ( qap + tem ) );
            app = ap + d * az;
            bpp = bp + d * bz;
            aOld = az;
            am = ap / bpp;
            bm = bp / bpp;
            az = app / bpp;
            bz = 1;
            if ( Math.abs( az - aOld ) < eps * Math.abs( az ) )
                break;
        }
        return az;
    }
}

