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
        if( index < 0 || index > ( num -1 ) ) return Double.NaN;
        else return ( minX + delta * index );
    }

    /**
     * Returns the ith y element in this function. Returns null
     * if index is negative or greater than number of points.
     */
    public double getY(int index){
        if( index < 0 || index > ( num -1 ) ) return Double.NaN;
        Object obj = points.get(index);
        if( obj == null ) return Double.NaN;
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


        points.set( index,new Double(point.getY()));

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
        if( y == Double.NaN ) return false;
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
        for( int i = 0; i < this.num; i++){ list.add(new Double(getX(i))); }
        return list.listIterator();
    }

    public ListIterator getYValuesIterator(){
        ArrayList list = new ArrayList();
        for( int i = 0; i < this.num; i++){ list.add(new Double(getY(i))); }
        return list.listIterator();
    }


     /**
     * This function interpolates the first x-axis value corresponding to the given value of y
     * @param y(value for which interpolated first x value has to be found
     * @return x(this  is the interpolated x based on the given y value)
     */
    public double getFirstInterpolatedX(double y){
      // finds the size of the point array
       int max=points.size();
       int i;
       double y1=Double.NaN;
       double y2=Double.NaN;
       //if passed parameter(y value) is not within range then throw exception
       if(y>getY(max-1) || y<getY(0))
          throw new InvalidRangeException("Y Value must be within the range: "+getY(0)+" and "+getY(max-1));
      //if y value is equal to the maximum value of all given Y's then return the corresponding X value
       if(y==getY(max-1))
         return getX(max-1);
      //finds the Y values within which the the given y value lies
       for(i=0;i<max-1;++i) {
         y1=getY(i);
         y2=getY(i+1);
        if(y>=y1 && y<=y2)
           break;
       }
       //finding the x values for the coressponding y values
       double x1=getX(i);
       double x2=getX(i+1);
       //using the linear interpolation equation finding the value of x for given y
       double x= ((y-y1)*(x2-x1))/(y2-y1) + x1;
       return x;
    }

    /**
     * This function interpolates the y-axis value corresponding to the given value of x
     * @param x(value for which interpolated first y value has to be found
     * @return y(this  is the interpolated x based on the given x value)
     */
    public double getInterpolatedY(double x){
    // finds the size of the point array
       int max=points.size();
       double x1=Double.NaN;
       double x2=Double.NaN;
       //if passed parameter(x value) is not within range then throw exception
       if(x>getX(max-1) || x<getX(0))
          throw new InvalidRangeException("x Value must be within the range: "+getX(0)+" and "+getX(max-1));
      //if x value is equal to the maximum value of all given X's then return the corresponding Y value
       if(x==getX(max-1))
         return getY(x);
      //finds the X values within which the the given x value lies
       for(int i=0;i<max-1;++i) {
         x1=getX(i);
         x2=getX(i+1);
        if(x>=x1 && x<=x2)
           break;
       }
       //finding the y values for the coressponding x values
       double y1=getY(x1);
       double y2=getY(x2);
       //using the linear interpolation equation finding the value of y for given x
       double y= ((y2-y1)*(x-x1))/(x2-x1) + y1;
       return y;
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
            b.append(point.getX() + ", " + point.getY()+ '\n');
        }

        return b.toString();

    }



}
