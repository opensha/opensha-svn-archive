package org.scec.data.function;

import java.util.*;
import org.scec.util.*;
import org.scec.exceptions.*;
import org.scec.data.*;

// FIX - Needs more comments

/**
 * <b>Title:</b> EvenlyDiscretizedFunc<p>
 *
 * <b>Description:</b> Assumes even spacing between the x points represented by
 * the delta distance. Y Values are stored as doubles in an array of primitives. This
 * allows replacement of values at specified indexes.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class EvenlyDiscretizedFunc extends DiscretizedFunc{


    /** Class name used for debbuging */
    protected final static String C = "EvenlyDiscretizedFunc";

    /** if true print out debugging statements */
    protected final static boolean D = false;

    /** The internal storage collection of points, stored as a linked list */
    protected double points[];

    /** The minimum x-value in this series, pins the index values with delta */
    protected double minX=Double.NaN;

        /** The maximum x-value in this series */
    protected double maxX=Double.NaN;

    /** Distance between x points */
    protected double delta=Double.NaN;

    /** Number of points in this function */
    protected int num;

    /**
     * Boolean that indicates no values have been put into this function yet.
     * used only internally
     */
    protected boolean first = true;


    /**
     * This is one of two constructor options
     * to fully quantify the domain of this list.
     * @param min   - Starting x value
     * @param num   - number of points in list
     * @param delta - distance between x values
     */
    public EvenlyDiscretizedFunc(double min, int num, double delta) {

        this.minX = min;
        this.delta = delta;
        this.num = num;
        maxX = minX + (num-1)*delta;

        points = new double[num];
    }


    /**
     * The other three input options
     * to fully quantify the domain of this list.
     * @param min   - Starting x value
     * @param num   - number of points in list
     * @param max - Ending x value
     */
    public EvenlyDiscretizedFunc(double min, double max, int num) {

        if (min > max) throw new DiscretizedFuncException("min must be less than max");
        else if (min < max)
            delta = (max-min)/(num-1);
        else { // max == min
            if (num == 1)
                delta = 0;
            else
                throw new DiscretizedFuncException("num must = 1 if min==max");
        }

        this.minX = min;
        this.maxX = max;
        this.num = num;

        points = new double[num];

    }

    /**
     * Sets all y values to NaN
     */
    public void clear(){
        for( int i = 0; i < num; i++){ points[i] = Double.NaN; }
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
    public double getMaxX(){ return maxX; }

    public double getMinY(){
        double minY = Double.POSITIVE_INFINITY;
        for(int i = 0; i<num; ++i)
            if(points[i] < minY) minY = points[i];
        return minY;
    }

    public double getMaxY(){
        double maxY = Double.NEGATIVE_INFINITY;
        for(int i = 0; i<num; ++i)
            if(points[i] > maxY) maxY = points[i];
        return maxY;
    }



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
        return points[index];
    }

    /**
     * Returns the ith y element in this function. Returns null
     * if the x value is not within tolerance of any x points
     * along the x axis.
     */
    public double getY(double x){ return getY( getXIndex( x) ); }

    /**
     * Iterates from lowest to highest x value and compares to the
     * input argument if they are equal within tolerance. If a match is
     * found the index is returned, else -1 is returned.
     * @param index
     * @return
     */
    public int getXIndex( double x) throws DataPoint2DException{

        double xx = x;              // Why this?
        double xxMin = this.minX;   // Why this?

        for( int i = 0; i < num; i++){
            if( withinTolerance(xx, ( xxMin + i*delta ) ) ) return i;
        }
       throw new DataPoint2DException(C + ": set(): This point doesn't match a permitted x value.");
    }

   /**
     * this function will throw an exception if the given x-value is not
     * within tolerance of one of the x-values in the function
     */
    public void set(DataPoint2D point) throws DataPoint2DException {

        set( point.getX(), point.getY());
    }

    /**
     * this function will throw an exception if the given x-value is not
     * within tolerance of one of the x-values in the function
     */
    public void set(double x, double y) throws DataPoint2DException {
        int index = getXIndex( x );
        points[index] = y;
    }

    /**
     * this function will throw an exception if the index is not
     * within the range of 0 to num -1
     */
    public void set(int index, double y) throws DataPoint2DException {
        if( index < 0 || index > ( num -1 ) ) {
            throw new DataPoint2DException(C + ": set(): The specified index doesn't match this function domain.");
        }
        points[index] = y;
    }

    /**
     * This function may be slow if there are many points in the list. It has to
     * reconstitute all the data points x-values by index, only y values are stored
     * internally in this function type. A DataPoint2D is built for each y value and
     * added to a local ArrayList. Then the iterator of the local ArrayList is returned.
     * @return
     */
    public Iterator getPointsIterator(){
        ArrayList list = new ArrayList();
        for( int i = 0; i < num; i++){
            list.add( new DataPoint2D( getX(i), getY(i) ) );
        }
        return list.listIterator();
    }

    /**
     * This returns an iterator over x values as Double objects
     * @return
     */
    public ListIterator getXValuesIterator(){
        ArrayList list = new ArrayList();
        for( int i = 0; i < num; i++){ list.add(new Double(getX(i))); }
        return list.listIterator();
    }

    /**
     * This returns an iterator over y values as Double objects
     * @return
     */
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
       int i;
       double y1=Double.NaN;
       double y2=Double.NaN;
       //if passed parameter(y value) is not within range then throw exception
       if( y>getMaxY() || y<getMinY() )
          throw new InvalidRangeException("Y Value must be within the range: "+getMinY()+" and "+getMaxY());
      //finds the Y values within which the the given y value lies
       for(i=0;i<num-1;++i) {
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
       double x1=Double.NaN;
       double x2=Double.NaN;
       //if passed parameter(x value) is not within range then throw exception
       if(x>getX(num-1) || x<getX(0))
          throw new InvalidRangeException("x Value must be within the range: "+getX(0)+" and "+getX(num-1));
      //finds the X values within which the the given x value lies
       for(int i=0;i<num-1;++i) {
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
        f.minX = minX;
        f.maxX = maxX;
        f.name = name;
        f.tolerance = tolerance;
        f.setInfo(this.getInfo());
        f.setName(this.getName());
        for(int i = 0; i<num; i++)
            f.set(i, points[i]);

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

    /**
     * Returns true if the x value is withing tolerance of an x-value in this list,
     * and the y value is equal to y value in the list.
     */
    public boolean hasPoint(DataPoint2D point){
        return hasPoint(point.getX(),point.getY());
    }

    /**
    * Returns true if the x value is withing tolerance of an x-value in this list,
    * and the y value is equal to y value in the list.
     */
    public boolean hasPoint(double x, double y){
      try {
        int index = getXIndex( x );
        double yVal = this.getY(index);
        if( yVal == Double.NaN || yVal!=y) return false;
          return true;
      } catch(DataPoint2DException e) {
          return false;
      }
    }

     /** Returns the index of this DataPoint based on it's x any y value
      *  both the x-value and y-values in list should match with that of point
      * returns -1 if there is no such value in the list
      * */
     public int getIndex(DataPoint2D point){
       try {
         int index= getXIndex( point.getX() );
         if (index < 0) return -1;
         double y = this.getY(index);
         if(y!=point.getY()) return -1;
         return index;
       }catch(DataPoint2DException e) {
          return -1;
       }
    }

}
