package edu.uah.math.psol.distributions;
import java.util.*;

/**
 *  A simple implementation of a data distribution
 *
 *  
 *  
 */
public class Data {
    //Variables
    /**
     *  Description of the Field
     */
    private Vector values = new Vector();
    /**
     *  Description of the Field
     */
    private int size;
    /**
     *  Description of the Field
     */
    private double value, mean, meanSquare, mode;
    /**
     *  Description of the Field
     */
    private String name;

    /**
     *  This general constructor creates a new data with a prescribed name.
     *
     * @param  n  Description of the Parameter
     */
    public Data( String n ) {
        setName( n );
    }

    /**
     *  This default constructor creates a new data with the name "X"
     */
    public Data() {
        this( "X" );
    }

    /**
     *  This method adds a new number to the data set and re-compute the mean,
     *  mean square, minimum and maximum values, and order statistics
     *
     * @param  x  The new value value
     */
    public void setValue( double x ) {
        double a;
        double b;
        value = x;
        boolean notInserted = true;
        //Add the value to the data set
        for ( int i = 0; i < size - 1; i++ ) {
            a = ( ( Double ) values.elementAt( i ) ).doubleValue();
            b = ( ( Double ) values.elementAt( i + 1 ) ).doubleValue();
            if ( ( a <= x ) & ( x >= b ) ) {
                values.insertElementAt( new Double( x ), i + 1 );
                notInserted = false;
            }
        }
        if ( notInserted )
            values.insertElementAt( new Double( x ), 0 );
        //Re-compute mean and mean square
        mean = ( ( double ) ( size - 1 ) / size ) * mean + value / size;
        meanSquare = ( ( double ) ( size - 1 ) / size ) * meanSquare + value * value / size;
    }

    /**
     *  Get the name of the data set
     *
     * @param  name  The new name value
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     *  Get the current value of the data set
     *
     * @return    The value value
     */
    public double getValue() {
        return value;
    }

    /**
     *  This method returns the i'th value of the data set.
     *
     * @param  i  Description of the Parameter
     * @return    The value value
     */
    public double getValue( int i ) {
        return ( ( Double ) values.elementAt( i ) ).doubleValue();
    }

    /**
     *  Get the mean
     *
     * @return    The mean value
     */
    public double getMean() {
        return mean;
    }

    /**
     *  Get the population variance
     *
     * @return    The pVariance value
     */
    public double getPVariance() {
        double var = meanSquare - mean * mean;
        if ( var < 0 )
            var = 0;
        return var;
    }

    /**
     *  Get the population standard deviation
     *
     * @return    The pSD value
     */
    public double getPSD() {
        return Math.sqrt( getPVariance() );
    }

    /**
     *  Get the sample variance of the data set
     *
     * @return    The variance value
     */
    public double getVariance() {
        return ( ( double ) size / ( size - 1 ) ) * getPVariance();
    }

    /**
     *  Get the sample standard deviation of the data set
     *
     * @return    The sD value
     */
    public double getSD() {
        return Math.sqrt( getVariance() );
    }

    /**
     *  Get the minimum value of the data set
     *
     * @return    The minValue value
     */
    public double getMinValue() {
        return getValue( 0 );
    }

    /**
     *  Get the maximum value of the data set
     *
     * @return    The maxValue value
     */
    public double getMaxValue() {
        return getValue( size - 1 );
        ;
    }

    /**
     *  Get the number of pointCount in the data set
     *
     * @return    The size value
     */
    public int getSize() {
        return size;
    }

    /**
     *  Set the name of the data set
     *
     * @return    The name value
     */
    public String getName() {
        return name;
    }

    /**
     *  Reset the data set
     */
    public void reset() {
        values.removeAllElements();
        size = 0;
    }
}

