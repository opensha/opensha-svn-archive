package edu.uah.math.psol.distributions;

/**
 *  The discrete uniform distribution on a finite set
 *
 *  
 *  
 */
public class DiscreteUniformDistribution extends Distribution {
    /**
     *  Description of the Field
     */
    double values;

    /**
     *  Constructor for the DiscreteUniformDistribution object
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @param  w  Description of the Parameter
     */
    public DiscreteUniformDistribution( double a, double b, double w ) {
        setParameters( a, b, w );
    }

    /**
     *  Constructor for the DiscreteUniformDistribution object
     */
    public DiscreteUniformDistribution() {
        this( 1, 6, 1 );
    }

    /**
     *  Sets the parameters attribute of the DiscreteUniformDistribution object
     *
     * @param  a  The new parameters value
     * @param  b  The new parameters value
     * @param  w  The new parameters value
     */
    public void setParameters( double a, double b, double w ) {
        super.setParameters( a, b, w, DISCRETE );
    }

    /**
     *  Gets the density attribute of the DiscreteUniformDistribution object
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        if ( getDomain().getLowerValue() <= x & x <= getDomain().getUpperValue() )
            return 1.0 / getDomain().getSize();
        else
            return 0;
    }

    /**
     *  Gets the maxDensity attribute of the DiscreteUniformDistribution object
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return 1.0 / getDomain().getSize();
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        return getDomain().getLowerValue() + Math.random() * getDomain().getSize() * getDomain().getWidth();
    }
}

