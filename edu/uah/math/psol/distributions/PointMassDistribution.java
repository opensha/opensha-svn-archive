package edu.uah.math.psol.distributions;

/**
 *  Description of the Class
 *
 *  
 *  
 */
public class PointMassDistribution extends Distribution {
    //Paramter
    /**
     *  Description of the Field
     */
    double x0;

    //Constructor
    /**
     *  Constructor for the PointMassDistribution object
     *
     * @param  x0  Description of the Parameter
     */
    public PointMassDistribution( double x0 ) {
        setParameters( x0 );
    }

    /**
     *  Constructor for the PointMassDistribution object
     */
    public PointMassDistribution() {
        this( 0 );
    }

    /**
     *  Sets the parameters attribute of the PointMassDistribution object
     *
     * @param  x0  The new parameters value
     */
    public void setParameters( double x0 ) {
        this.x0 = x0;
        super.setParameters( x0, x0, 1, DISCRETE );
    }

    /**
     *  Gets the density attribute of the PointMassDistribution object
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( x == x0 )
            return 1;
        else
            return 0;
    }

    /**
     *  Gets the maxDensity attribute of the PointMassDistribution object
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return 1;
    }

    /**
     *  Gets the mean attribute of the PointMassDistribution object
     *
     * @return    The mean value
     */
    public double getMean() {
        return x0;
    }

    /**
     *  Gets the variance attribute of the PointMassDistribution object
     *
     * @return    The variance value
     */
    public double getVariance() {
        return 0;
    }

    /**
     *  Gets the parameter attribute of the PointMassDistribution object
     *
     * @param  i  Description of the Parameter
     * @return    The parameter value
     */
    public double getParameter( int i ) {
        return x0;
    }

    /**
     *  Gets the quantile attribute of the PointMassDistribution object
     *
     * @param  p  Description of the Parameter
     * @return    The quantile value
     */
    public double getQuantile( double p ) {
        return x0;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        return x0;
    }

    /**
     *  Description of the Method
     *
     * @param  x  Description of the Parameter
     * @return    Description of the Return Value
     */
    public double CDF( double x ) {
        if ( x < x0 )
            return 0;
        else
            return 1;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String name() {
        return "Point Mass Distribution";
    }
}

