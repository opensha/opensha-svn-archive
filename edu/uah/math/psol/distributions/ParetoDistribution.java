package edu.uah.math.psol.distributions;

/**
 *  This class models the Pareto distribution with a specified parameter
 *
 *  
 *  
 */
public class ParetoDistribution extends Distribution {
    //Variable
    /**
     *  Description of the Field
     */
    private double parameter;

    /**
     *  This general constructor creates a new Pareto distribuiton with a
     *  specified parameter
     *
     * @param  a  Description of the Parameter
     */
    public ParetoDistribution( double a ) {
        setParameter( a );
    }

    /**
     *  The default constructor creates a new Pareto distribution with parameter
     *  1
     */
    public ParetoDistribution() {
        this( 1 );
    }

    /**
     *  This method sets the parameter and computes the default interval
     *
     * @param  a  The new parameter value
     */
    public void setParameter( double a ) {
        if ( a <= 0 )
            a = 1;
        parameter = a;
        double upper = 20 / parameter;
        double width = ( upper - 1 ) / 100;
        super.setParameters( 1, upper, width, CONTINUOUS );
    }

    /**
     *  This method returns the parameter
     *
     * @return    The parameter value
     */
    public double getParameter() {
        return parameter;
    }

    /**
     *  This method computes the getDensity function
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( x < 1 )
            return 0;
        else
            return parameter / Math.pow( x, parameter + 1 );
    }

    /**
     *  This method returns the maximum value of the getDensity function
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return parameter;
    }

    /**
     *  This method computes the mean
     *
     * @return    The mean value
     */
    public double getMean() {
        if ( parameter > 1 )
            return parameter / ( parameter - 1 );
        else
            return Double.POSITIVE_INFINITY;
    }

    /**
     *  This method computes the variance
     *
     * @return    The variance value
     */
    public double getVariance() {
        if ( parameter > 2 )
            return parameter / ( ( parameter - 1 ) * ( parameter - 1 ) * ( parameter - 2 ) );
        else if ( parameter > 1 )
            return Double.POSITIVE_INFINITY;
        else
            return Double.NaN;
    }

    /**
     *  This method comptues the cumulative distribution function
     *
     * @param  x  Description of the Parameter
     * @return    The cDF value
     */
    public double getCDF( double x ) {
        return 1 - Math.pow( 1 / x, parameter );
    }

    /**
     *  This method computes the getQuantile function
     *
     * @param  p  Description of the Parameter
     * @return    The quantile value
     */
    public double getQuantile( double p ) {
        return 1 / Math.pow( 1 - p, 1 / parameter );
    }
}

