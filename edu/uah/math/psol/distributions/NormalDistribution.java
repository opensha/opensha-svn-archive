package edu.uah.math.psol.distributions;

/**
 *  This class encapsulates the normal distribution with specified parameters
 *
 *  
 *  
 */
public class NormalDistribution extends Distribution {
    //Paramters
    /**
     *  Description of the Field
     */
    public final static double C = Math.sqrt( 2 * Math.PI );
    /**
     *  Description of the Field
     */
    private double mu, sigma, cSigma;

    /**
     *  This general constructor creates a new normal distribution with
     *  specified parameter values
     *
     * @param  mu     Description of the Parameter
     * @param  sigma  Description of the Parameter
     */
    public NormalDistribution( double mu, double sigma ) {
        setParameters( mu, sigma );
    }

    /**
     *  This default constructor creates a new standard normal distribution
     */
    public NormalDistribution() {
        this( 0, 1 );
    }

    /**
     *  This method sets the parameters
     *
     * @param  m  The new parameters value
     * @param  s  The new parameters value
     */
    public void setParameters( double m, double s ) {
        double lower;
        double upper;
        double width;
        //Correct for invalid sigma
        if ( s < 0 )
            s = 1;
        mu = m;
        sigma = s;
        cSigma = C * sigma;
        upper = mu + 4 * sigma;
        lower = mu - 4 * sigma;
        width = ( upper - lower ) / 100;
        super.setParameters( lower, upper, width, CONTINUOUS );
    }

    /**
     *  This method sets the location parameter
     *
     * @param  m  The new mu value
     */
    public void setMu( double m ) {
        setParameters( m, sigma );
    }

    /**
     *  This method sets the scale parameter
     *
     * @param  s  The new sigma value
     */
    public void setSigma( double s ) {
        setParameters( mu, s );
    }

    /**
     *  This method defines the getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        double z = ( x - mu ) / sigma;
        return Math.exp( -z * z / 2 ) / cSigma;
    }

    /**
     *  This method returns the maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return getDensity( mu );
    }

    /**
     *  This method returns the median
     *
     * @return    The median value
     */
    public double getMedian() {
        return mu;
    }

    /**
     *  This method returns the mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return mu;
    }

    /**
     *  This method returns the variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        return sigma * sigma;
    }

    /**
     *  This method returns the location parameter
     *
     * @return    The mu value
     */
    public double getMu() {
        return mu;
    }

    /**
     *  This method gets the scale parameter
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
        double z = ( x - mu ) / sigma;
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
        return mu + sigma * r * Math.cos( theta );
    }
}

