package org.scec.data.function;

import java.util.*;
import org.scec.util.*;
import org.scec.exceptions.*;
import org.scec.data.*;

/**
 * <b>Title:</b> EvenlyDiscretizedFunction2D<br>
 * <b>Description:</b> Assumes even spacing between the x points represented by
 * the delta distance. Y Values are stored in a fixed size linked list. This
 * allows replacement of values at specified indexes, and permits null values.
 * In fact the LinkedList is initialized to have all null values, fixed to
 * the size of num.<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class EvenlyDiscretizedFunc extends DiscretizedFunc{

    protected final static String C = "EvenlyDiscretizedFunc";
    protected final static boolean D = false;

    /** The internal storage collection of points, stored as a linked list */
    protected LinkedList points = new LinkedList();

    /** The minimum x-value in this series, pins the index values with delta */
    protected double minX=Double.NaN;

    /** Distance between x points */
    protected double delta=Double.NaN;

    /** Number of points in this function */
    protected int num;

    /**
     * The minimum Y-Value in this data series, calculated everytime a
     * point is added to this function
     */
    protected double minY = Double.NaN;

    /**
     * The maximum Y-Value in this data series, calculated everytime a
     * point is added to this function
     */
    protected double maxY = Double.NaN;

    /**
     * Boolean that indicates no values have been put into this function yet.
     * used only internally
     */
    protected boolean first = true;


    /**
     * Only possible constructor, these three inputs are required
     * to fully quantify the domain of this list. The list is
     * initialized to this size all with null y values.
     * @param min   - Starting x value
     * @param num   - number of points in list
     * @param delta - distance between x values
     */
    public EvenlyDiscretizedFunc(double min, int num, double delta) {

        this.minX = minX;
        this.delta = delta;
        this.num = num;

        clear();

    }

    /**
     * Clears out the y values - initializes linked list of
     * y values to have all null points
     */
    public void clear(){
        points.clear();
        for( int i = 0; i < num; i++){ points.add( null ); }
    }
    /**
     * Returns true if two values are within tolerance to
     * be considered equal. Used internally
     */
    protected boolean withinTolerance(double x, double xx){
        if( Math.abs( x - xx)  <= this.tolerance) return true;
        else return false;
    }


    public double getDelta() { return delta; }
    public int getNum(){ return num; }
    public double getMinX(){ return minX; }
    public double getMaxX(){ return getX( num-1); }
    public double getMinY(){ return minY; }
    public double getMaxY(){ return maxY; }


    public DataPoint2D get(int index){
        return new DataPoint2D(getX(index), getY(index));
    }

    /**
     * Returns the ith x element in this function. Returns null
     * if index is negative or greater than number of points.
     * @param index
     * @return
     */
    public double getX(int index){
        if( index < 0 || index > ( num -1 ) ) return null;
        else return ( minX + delta * index );
    }

    /**
     * Returns the ith y element in this function. Returns null
     * if index is negative or greater than number of points.
     */
    public double getY(int index){
        if( index < 0 || index > ( num -1 ) ) return null;
        Object obj = points.get(index);
        if( obj == null ) return null;
        else return ((Double)obj).doubleValue();
    }

    /**
     * Returns the ith y element in this function. Returns null
     * if the x value is not within tolerance of any x points
     * along the x axis.
     */
    public double getY(double x){ return getY( getXIndex( x) ); }


    /** Returns the index of this DataPoint based on it's x-value */
    public int getIndex(DataPoint2D point){
        return getXIndex( point.getX() );
    }

    /**
     * Iterates from lowest to highest x value and compares to the
     * input argument if they are equal within tolerance. If a match is
     * found the index is returned, else -1 is returned.
     * @param index
     * @return
     */
    public int getXIndex( double x){

        double xx = x;
        double xxMin = this.minX;

        for( int i = 0; i < num; i++){
            if( withinTolerance(xx, ( xxMin + i*delta ) ) ) return i;
        }
        return -1;
    }

    /** FIX *** returns the Y value given an x value - within tolerance */
    public int getYIndex(double y){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }



    /**
     * this function will throw an exception if the given x-value is not
     * within tolerance of one of the x-values in the function
     */
    public void set(DataPoint2D point) throws DataPoint2DException {

        int index = getXIndex( point.getX() );
        if( index < 0 ){
            throw new DataPoint2DException(C + ": set(): This point doesn't match a permitted x value.");
        }

        // Calculate min and max values
        if ( !first ) {

            double y = point.getY();
            double min = minY;
            double max = maxY;

            if ( y < min ) minY = point.getY();
            else if ( y > max )  maxY = point.getY();

        }
        else {
            minY = point.getY();
            maxY = point.getY();
            first = false;
        }


        points.set( index, point.getY() );

    }

    /**
     * this function will throw an exception if the given x-value is not
     * within tolerance of one of the x-values in the function
     */
    public void set(double x, double y) throws DataPoint2DException {
        DataPoint2D point = new DataPoint2D(x,y);
        set(point);
    }

    /**
     * this function will throw an exception if the index is not
     * within the range of 0 to num -1
     */
    public void set(int index, double y) throws DataPoint2DException {
        if( index < 0 || index > ( num -1 ) ) {
            throw new DataPoint2DException(C + ": set(): The specified index doesn't match this function domain.");
        }
        double x = this.getX(index);
        set(x, y);
    }


    /**
     * Not sure if this is how we want to implement these two hasPoint functions.
     * Returns true if the x value is withing tolerance of an x-value in this list,
     * and the y value is not null. Another possiblility would be to have this method
     * return true if the x value is a valid point in this function.
     */
    public boolean hasPoint(DataPoint2D point){
        int index = getXIndex( point.getX() );
        if (index < 0) return false;
        double y = this.getY(index);
        if( y == null ) return false;
        return true;
    }

    /**
     * Not sure if this is how we want to implement these two hasPoint functions.
     * Returns true if the x value is withing tolerance of an x-value in this list,
     * and the y value is not null. Another possiblility would be to have this method
     * return true if the x value is a valid point in this function.
     */
    public boolean hasPoint(double x, double y){ return hasPoint( new DataPoint2D(x,y) ); }


    /**
     * This function may be slow if there are many points in the list. It has to
     * reconstitute all the data points x-values by index, only y values are stored
     * internally in this function type. A DataPoint2D is built for each y value and
     * added to a local ArrayList. Then the iterator of the local ArrayList is returned.
     * @return
     */
    public Iterator getPointsIterator(){
        ArrayList list = new ArrayList();
        for( int i = 0; i < this.num; i++){
            list.add( new DataPoint2D( getX(i), getY(i) ) );
        }
        return list.listIterator();
    }

    public ListIterator getXValuesIterator(){
        ArrayList list = new ArrayList();
        for( int i = 0; i < this.num; i++){ list.add(getX(i)); }
        return list.listIterator();
    }

    public ListIterator getYValuesIterator(){
        ArrayList list = new ArrayList();
        for( int i = 0; i < this.num; i++){ list.add(getY(i)); }
        return list.listIterator();
    }


    /** Not implemented yet, will throw exception if used */
    public double getInterpolatedX(double y){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    /** Not implemented yet, will throw exception if used */
    public double getInterpolatedY(double x){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }





    /** FIX *** Returns a copy of this and all points in this DiscretizedFunction */
    public DiscretizedFuncAPI deepClone(){

        EvenlyDiscretizedFunc f = new EvenlyDiscretizedFunc(
            minX, num, delta
        );

        f.info = info;
        f.maxY = maxY;
        f.minY = minY;
        f.minX = minX;
        f.name = name;
        f.tolerance = tolerance;

        ListIterator it = getYValuesIterator();
        int counter = 0;
        while( it.hasNext() ){
            Object obj = it.next();
            if( obj != null ) f.set(counter++, ((Double)obj).doubleValue() );
            else f.set(counter++, Double.NaN );
        }

        return f;
    }


    /**
     * Determines if two functions are the same by comparing
     * that each point x value is the same, within tolerance
     */
    public boolean equalXValues(DiscretizedFuncAPI function){
        String S = C + ": equalXValues():";

        if( !(function instanceof EvenlyDiscretizedFunc ) ) return false;
        if( num != function.getNum() ) return false;


        double min = minX;
        double min1 = ((EvenlyDiscretizedFunc)function).getMinX();
        if( !withinTolerance( min, min1 ) ) return false;

        double d = delta;
        double d1 = ((EvenlyDiscretizedFunc)function).getDelta();
        if( d != d1 ) return false;

        return true;

    }

    /**
     * Determines if two functions are the same by comparing
     * that each point x value is the same, within tolerance,
     * and that each y value is the same, including nulls.
     */
    public boolean equalXAndYValues(DiscretizedFuncAPI function){
        String S = C + ": equalXAndYValues():";

        if( !equalXValues(function) ) return false;

        for( int i = 0; i < num; i++){

            double y1 = getY(i);
            double y2 = function.getY(i);

            if( y1 == Double.NaN &&  y2 != Double.NaN ) return false;
            else if( y2 == Double.NaN &&  y1 != Double.NaN ) return false;
            else if( y1 != y2 ) return false;

        }

        return true;

    }

    /** Useful for debugging - prints out all points, one per line */
    public String toString(){

        StringBuffer b = new StringBuffer();

        b.append("X, Y\n");
        Iterator it = getPointsIterator();
        while( it.hasNext() ){

            DataPoint2D point = (DataPoint2D)it.next();
            b.append(point.getX().toString() + ", " + point.getY().toString() + '\n');
        }

        return b.toString();

    }



}
