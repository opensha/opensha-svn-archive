package edu.uah.math.psol.distributions;

/**
 *  A basic discrete distribution on a finite set of points, with specified
 *  probabilities
 *
 *  
 *  
 */
public class FiniteDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    private int n;
    /**
     *  Description of the Field
     */
    private double[] prob;

    /**
     *  Constructs a new finite distribution on a finite set of points with a
     *  specified array of probabilities
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @param  w  Description of the Parameter
     * @param  p  Description of the Parameter
     */
    public FiniteDistribution( double a, double b, double w, double[] p ) {
        setParameters( a, b, w, p );
    }

    /**
     *  Constructs the uniform distribuiton on the finite set of points
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @param  w  Description of the Parameter
     */
    public FiniteDistribution( double a, double b, double w ) {
        super.setParameters( a, b, w, DISCRETE );
        n = getDomain().getSize();
        prob = new double[n];
        for ( int i = 0; i < n; i++ )
            prob[i] = 1.0 / n;
    }

    /**
     *  This special constructor creates a new uniform distribution on {1, 2,
     *  ..., 10}.
     */
    public FiniteDistribution() {
        this( 1, 10, 1 );
    }

    /**
     *  This method sets the parameters: the domain and the probabilities.
     *
     * @param  a  The new parameters value
     * @param  b  The new parameters value
     * @param  w  The new parameters value
     * @param  p  The new parameters value
     */
    public void setParameters( double a, double b, double w, double[] p ) {
        super.setParameters( a, b, w, DISCRETE );
        n = getDomain().getSize();
        prob = new double[n];
        if ( p.length != n )
            p = new double[n];
        double sum = 0;
        for ( int i = 0; i < n; i++ ) {
            if ( p[i] < 0 )
                p[i] = 0;
            sum = sum + p[i];
        }
        if ( sum == 0 )
            for ( int i = 0; i < n; i++ )
                prob[i] = 1.0 / n;
        else
            for ( int i = 0; i < n; i++ )
                prob[i] = p[i] / sum;
    }

    /**
     *  Set the probabilities
     *
     * @param  p  The new probabilities value
     */
    public void setProbabilities( double[] p ) {
        if ( p.length != n )
            p = new double[n];
        double sum = 0;
        for ( int i = 0; i < n; i++ ) {
            if ( p[i] < 0 )
                p[i] = 0;
            sum = sum + p[i];
        }
        if ( sum == 0 )
            for ( int i = 0; i < n; i++ )
                prob[i] = 1.0 / n;
        else
            for ( int i = 0; i < n; i++ )
                prob[i] = p[i] / sum;
    }

    /**
     *  Density function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        int j = getDomain().getIndex( x );
        if ( 0 <= j & j < n )
            return prob[j];
        else
            return 0;
    }

    /**
     *  This method gets the probability for a specified index
     *
     * @param  i  Description of the Parameter
     * @return    The probability value
     */
    public double getProbability( int i ) {
        if ( i < 0 )
            i = 0;
        else if ( i >= n )
            i = n - 1;
        return prob[i];
    }

    /**
     *  This method gets the probability vector.
     *
     * @return    The probabilities value
     */
    public double[] getProbabilities() {
        return prob;
    }
}


