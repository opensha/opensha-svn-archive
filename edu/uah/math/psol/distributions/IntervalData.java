package edu.uah.math.psol.distributions;

/**
 *  This class defines a simple implementation of an interval data distribution.
 *  The data distribution is based on a specified domain (that is, a partition
 *  of an interval). When values are added, frequency counts for the
 *  subintervals are computed and various statistic updated.
 *
 *  
 *  
 */
public class IntervalData {
    //Variables
    /**
     *  Description of the Field
     */
    private int size, maxFreq;
    /**
     *  Description of the Field
     */
    private double value, minValue, maxValue, mean, meanSquare, mode;
    /**
     *  Description of the Field
     */
    private int[] freq;
    //Objects
    /**
     *  Description of the Field
     */
    private Domain domain;
    /**
     *  Description of the Field
     */
    private String name;

    /**
     *  This general constructor creates a new data distribution with a
     *  specified domain and a specified name
     *
     * @param  d  Description of the Parameter
     * @param  n  Description of the Parameter
     */
    public IntervalData( Domain d, String n ) {
        name = n;
        setDomain( d );
    }

    /**
     *  This general constructor creates a new data distribution with a
     *  specified domain and a specified name.
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @param  w  Description of the Parameter
     * @param  n  Description of the Parameter
     */
    public IntervalData( double a, double b, double w, String n ) {
        this( new Domain( a, b, w ), n );
    }

    /**
     *  This special constructor creates a new data distribution with a
     *  specified domain and the default name "X".
     *
     * @param  d  Description of the Parameter
     */
    public IntervalData( Domain d ) {
        this( d, "X" );
    }

    /**
     *  This spcial constructor creates a new data distribution with a specified
     *  domain and the name "X"
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @param  w  Description of the Parameter
     */
    public IntervalData( double a, double b, double w ) {
        this( a, b, w, "X" );
    }

    /**
     *  This default constructor creates a new data distribution on the interval
     *  [0, 1] with subintervals of length 0.1, and the default name "X".
     */
    public IntervalData() {
        this( 0, 1, 0.1 );
    }

    /**
     *  This method sets the domain of the data set.
     *
     * @param  d  The new domain value
     */
    public void setDomain( Domain d ) {
        domain = d;
        reset();
    }

    /**
     *  This method sets the name of the data set.
     *
     * @param  n  The new name value
     */
    public void setName( String n ) {
        name = n;
    }

    /**
     *  This method adds a new number to the data set and re-compute the mean,
     *  mean square, minimum and maximum values, the frequency distribution, and
     *  the mode
     *
     * @param  x  The new value value
     */
    public void setValue( double x ) {
        value = x;
        //Update the size of the data set:
        size++;
        //Re-compute mean and mean square
        mean = ( ( double ) ( size - 1 ) / size ) * mean + value / size;
        meanSquare = ( ( double ) ( size - 1 ) / size ) * meanSquare + value * value / size;
        //Recompute minimum and maximum values
        if ( value < minValue )
            minValue = value;
        if ( value > maxValue )
            maxValue = value;
        //Update frequency distribution
        int i = domain.getIndex( x );
        if ( i >= 0 & i < domain.getSize() ) {
            freq[i]++;
            //Re-compute mode
            if ( freq[i] > maxFreq ) {
                maxFreq = freq[i];
                mode = domain.getValue( i );
            }
            else if ( freq[i] == maxFreq )
                mode = Double.NaN;
            //There are two or more modes
        }
    }

    /**
     *  This method returns the domain.
     *
     * @return    The domain value
     */
    public Domain getDomain() {
        return domain;
    }

    /**
     *  This method gets the name of the data set.
     *
     * @return    The name value
     */
    public String getName() {
        return name;
    }

    /**
     *  This method returns the current value of the data set
     *
     * @return    The value value
     */
    public double getValue() {
        return value;
    }

    /**
     *  This method returns the domain value (midpoint) closest to given value
     *  of x
     *
     * @param  x  Description of the Parameter
     * @return    The domainValue value
     */
    public double getDomainValue( double x ) {
        return domain.getValue( domain.getIndex( x ) );
    }

    /**
     *  This method returns the frequency of the class containing a given value
     *  of x.
     *
     * @param  x  Description of the Parameter
     * @return    The freq value
     */
    public int getFreq( double x ) {
        int i = domain.getIndex( x );
        if ( i < 0 | i >= domain.getSize() )
            return 0;
        else
            return freq[i];
    }

    /**
     *  This method returns the relative frequency of the class containing a
     *  given value.
     *
     * @param  x  Description of the Parameter
     * @return    The relFreq value
     */
    public double getRelFreq( double x ) {
        if ( size > 0 )
            return ( double ) ( getFreq( x ) ) / size;
        else
            return 0;
    }

    /**
     *  This method returns the getDensity for a given value
     *
     * @param  x  Description of the Parameter
     * @return    The density value
     */
    public double getDensity( double x ) {
        return getRelFreq( x ) / domain.getWidth();
    }

    /**
     *  This method returns the mean of the data set.
     *
     * @return    The mean value
     */
    public double getMean() {
        return mean;
    }

    /**
     *  This method returns the mean of the frequency distribution. The interval
     *  mean is an approximation to the true mean of the data set.
     *
     * @return    The intervalMean value
     */
    public double getIntervalMean() {
        double sum = 0;
        for ( int i = 0; i < domain.getSize(); i++ )
            sum = sum + domain.getValue( i ) * freq[i];
        return sum / size;
    }

    /**
     *  This method returns the population variance
     *
     * @return    The varianceP value
     */
    public double getVarianceP() {
        double var = meanSquare - mean * mean;
        if ( var < 0 )
            var = 0;
        return var;
    }

    /**
     *  This method returns the population standard deviation.
     *
     * @return    The sDP value
     */
    public double getSDP() {
        return Math.sqrt( getVarianceP() );
    }

    /**
     *  This method returns the sample variance.
     *
     * @return    The variance value
     */
    public double getVariance() {
        return ( ( double ) size / ( size - 1 ) ) * getVarianceP();
    }

    /**
     *  This method returns the sample standard deviation.
     *
     * @return    The sD value
     */
    public double getSD() {
        return Math.sqrt( getVariance() );
    }

    /**
     *  This method returns the interval variance.
     *
     * @return    The intervalVariance value
     */
    public double getIntervalVariance() {
        double m = getIntervalMean();
        double sum = 0;
        double x;
        for ( int i = 0; i < domain.getSize(); i++ ) {
            x = domain.getValue( i );
            sum = sum + ( x - m ) * ( x - m ) * freq[i];
        }
        return sum / size;
    }

    /**
     *  This method returns the interval standard deviation.
     *
     * @return    The intervalSD value
     */
    public double getIntervalSD() {
        return Math.sqrt( getIntervalVariance() );
    }

    /**
     *  This method returns the minimum value of the data set
     *
     * @return    The minValue value
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     *  This method returns the maximum value of the data set
     *
     * @return    The maxValue value
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     *  This method computes the median of the values in the data set between
     *  two specified values
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @return    The median value
     */
    public double getMedian( double a, double b ) {
        int sumFreq = 0;
        int numValues = 0;
        int lRank;
        int uRank;
        double lValue = a - 1;
        double uValue = b + 1;
        double w = domain.getWidth();
        //Compute sum of frequencies between a and b
        for ( double x = a; x <= b + 0.5 * w; x = x + w )
            numValues = numValues + getFreq( x );
        //Determine parity and ranks
        if ( 2 * ( numValues / 2 ) == numValues ) {
            lRank = numValues / 2;
            uRank = lRank + 1;
        }
        else {
            lRank = ( numValues + 1 ) / 2;
            uRank = lRank;
        }
        //Determine values
        for ( double x = a; x <= b + 0.5 * w; x = x + w ) {
            sumFreq = sumFreq + getFreq( x );
            if ( ( lValue == a - 1 ) & ( sumFreq >= lRank ) )
                lValue = x;
            if ( ( uValue == b + 1 ) & ( sumFreq >= uRank ) )
                uValue = x;
        }
        //Return average of upper and lower values
        return ( uValue + lValue ) / 2;
    }

    /**
     *  This method computes the median of the entire data set
     *
     * @return    The median value
     */
    public double getMedian() {
        return getMedian( domain.getLowerValue(), domain.getUpperValue() );
    }

    /**
     *  This method returns the quartiles of the data set.
     *
     * @param  i  Description of the Parameter
     * @return    The quartile value
     */
    public double getQuartile( int i ) {
        if ( i < 1 )
            i = 1;
        else if ( i > 3 )
            i = 3;
        if ( i == 1 )
            return getMedian( domain.getLowerValue(), getMedian() );
        else if ( i == 2 )
            return getMedian();
        else
            return getMedian( getMedian(), domain.getUpperValue() );
    }

    /**
     *  This method computes the mean absoulte deviation
     *
     * @return    The mAD value
     */
    public double getMAD() {
        double mad = 0;
        double x;
        double m = getMedian();
        for ( int i = 0; i < domain.getSize(); i++ ) {
            x = domain.getValue( i );
            mad = mad + getRelFreq( x ) * Math.abs( x - m );
        }
        return mad;
    }

    /**
     *  This method returns the number of pointCount in the data set
     *
     * @return    The size value
     */
    public int getSize() {
        return size;
    }

    /**
     *  This method returns the maximum frequency
     *
     * @return    The maxFreq value
     */
    public int getMaxFreq() {
        return maxFreq;
    }

    /**
     *  This method returns the maximum relative frequency.
     *
     * @return    The maxRelFreq value
     */
    public double getMaxRelFreq() {
        if ( size > 0 )
            return ( double ) maxFreq / size;
        else
            return 0;
    }

    /**
     *  This method returns the maximum getDensity.
     *
     * @return    The maxDensity value
     */
    public double getMaxDensity() {
        return getMaxRelFreq() / domain.getWidth();
    }

    /**
     *  This method returns the mode of the distribution. The mode may not exist
     *
     * @return    The mode value
     */
    public double getMode() {
        return mode;
    }

    /**
     *  This method resets the data set
     */
    public void reset() {
        freq = new int[domain.getSize()];
        size = 0;
        minValue = domain.getUpperBound();
        maxValue = domain.getLowerBound();
        maxFreq = 0;
    }
}

