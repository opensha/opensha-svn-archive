package edu.uah.math.psol.distributions;

/**
 *  This class models the lognormal distribution with specified parameters
 *
 *  
 *  
 */
public class LogNormalDistribution extends Distribution {
    //variables
    /**
     *  Description of the Field
     */
    public final static double C = Math.sqrt( 2 * Math.PI );
    /**
     *  Description of the Field
     */
    private double mu, sigma;

    /**
     *  This general constructor creates a new lognormal distribution with
     *  specified parameters
     *
     * @param  m  Description of the Parameter
     * @param  s  Description of the Parameter
     */
    public LogNormalDistribution( double m, double s ) {
        setParameters( m, s );
    }

    /**
     *  This default constructor creates the standard lognormal distribution
     */
    public LogNormalDistribution() {
        this( 0, 1 );
    }

    /**
     *  This method sets the parameters, computes the default interval
     *
     * @param  m  The new parameters value
     * @param  s  The new parameters value
     */
    public void setParameters( double m, double s ) {
        if ( s <= 0 )
            s = 1;
        mu = m;
        sigma = s;
        double upper = getMean() + 3 * getSD();
        super.setParameters( 0, upper, 0.01 * upper, CONTINUOUS );
    }

    /**
     *  This method sets mu
     *
     * @param  m  The new mu value
     */
    public void setMu( double m ) {
        setParameters( m, sigma );
    }

    /**
     *  This method sets sigma
     *
     * @param  s  The new sigma value
     */
    public void setSigma( double s ) {
        setParameters( mu, s );
    }

    /**
     *  This method computes the getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        double z = ( Math.log( x ) - mu ) / sigma;
        return Math.exp( -z * z / 2 ) / ( x * C * sigma );
    }

    /**
     *  This method computes the maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        double mode = Math.exp( mu - sigma * sigma );
        return getDensity( mode );
    }

    /**
     *  This method computes the mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return Math.exp( mu + sigma * sigma / 2 );
    }

    /**
     *  This method computes the variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        double a = mu + sigma * sigma;
        return Math.exp( 2 * a ) - Math.exp( mu + a );
    }

    /**
     *  This method returns mu
     *
     * @return    The mu value
     */
    public double getMu() {
        return mu;
    }

    /**
     *  This method gets sigma
     *
     * @return    The sigma value
     */
    public double getSigma() {
        return sigma;
    }

    /**
     *  This method computes the cumulative distribution function
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        double z = ( Math.log( x ) - mu ) / sigma;
        if ( z >= 0 )
            return 0.5 + 0.5 * gammaCDF( z * z / 2, 0.5 );
        else
            return 0.5 - 0.5 * gammaCDF( z * z / 2, 0.5 );
    }

    /**
     *  This method simulates a value from the distribution
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        double r = Math.sqrt( -2 * Math.log( Math.random() ) );
        double theta = 2 * Math.PI * Math.random();
        return Math.exp( mu + sigma * r * Math.cos( theta ) );
    }
}

