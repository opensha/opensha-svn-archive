package edu.uah.math.psol.distributions;

/**
 *  Description of the Class
 *
 *  
 *  
 */
public class RandomVariable {
    /**
     *  Description of the Field
     */
    private Distribution distribution;
    /**
     *  Description of the Field
     */
    private IntervalData intervalData;
    /**
     *  Description of the Field
     */
    private String name;

    /**
     *  General constructor: create a new random variable with a specified
     *  probability distribution and name
     *
     * @param  d  Description of the Parameter
     * @param  n  Description of the Parameter
     */
    public RandomVariable( Distribution d, String n ) {
        distribution = d;
        name = n;
        intervalData = new IntervalData( distribution.getDomain(), name );

    }

    /**
     *  Special constructor: create a new random variable with a specified
     *  probability distribution and the name X
     *
     * @param  d  Description of the Parameter
     */
    public RandomVariable( Distribution d ) {
        this( d, "X" );
    }

    /**
     *  Assign the probability distribution and create a corresponding data
     *  distribution
     *
     * @param  d  The new distribution value
     */
    public void setDistribution( Distribution d ) {
        distribution = d;
        intervalData.setDomain( distribution.getDomain() );
    }

    /**
     *  Assign a value to the random variable
     *
     * @param  x  The new value value
     */
    public void setValue( double x ) {
        intervalData.setValue( x );
    }

    /**
     *  Assign a name to the random variable
     *
     * @param  n  The new name value
     */
    public void setName( String n ) {
        name = n;
        intervalData.setName( name );
    }

    /**
     *  Get the probability distribution
     *
     * @return    The distribution value
     */
    public Distribution getDistribution() {
        return distribution;
    }

    /**
     *  Get the data distribution
     *
     * @return    The intervalData value
     */
    public IntervalData getIntervalData() {
        return intervalData;
    }

    /**
     *  Get the current value of the random variable
     *
     * @return    The value value
     */
    public double getValue() {
        return intervalData.getValue();
    }

    /**
     *  Get the name of the random variable
     *
     * @return    The name value
     */
    public String getName() {
        return name;
    }

    /**
     *  Simulate a value of the probability distribution and assign the value to
     *  the data distribution
     */
    public void sample() {
        intervalData.setValue( distribution.simulate() );
    }

    /**
     *  Simulate a value of the probability distribution, assign the value to
     *  the data distribution and return the value
     *
     * @return    Description of the Return Value
     */
    public double simulate() {
        double x = distribution.simulate();
        intervalData.setValue( x );
        return x;
    }

    /**
     *  Reset the data distribution
     */
    public void reset() {
        intervalData.setDomain( distribution.getDomain() );
    }
}

