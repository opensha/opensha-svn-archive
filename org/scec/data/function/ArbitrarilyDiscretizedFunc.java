package org.scec.data.function;

import org.scec.data.DataPoint2D;
import org.scec.exceptions.DataPoint2DException;

import java.util.*;
import java.io.Serializable;
import org.scec.util.*;
import org.scec.exceptions.*;
import org.scec.param.ParameterList;
import org.scec.data.*;

/**
 * <b>Title:</b> ArbitrarilyDiscretizedFunc<p>
 *
 * <b>Description:</b> This class is a sublcass implementation
 * of a DiscretizedFunc that stores the data internaly as a
 * sorted TreeMap of DataPoint2D. This subclass distinguishes itself
 * by the fact that it assumes no spacing interval along the x-axis.
 * Consecutive points can be spread out or bunched up in no predicatable
 * order. <p>
 *
 * @author Steven W. Rock, Gupta Brothers
 * @version 1.0
 */

public class ArbitrarilyDiscretizedFunc extends DiscretizedFunc
                                        implements Serializable {

    /* Class name Debbuging variables */
    protected final static String C = "ArbitrarilyDiscretizedFunc";

    /* Boolean debugging variable to switch on and off debug printouts */
    protected final static boolean D = true;

    /**
     * The set of DataPoints2D that conprise the discretized function. These
     * are stored in a DataPoint2D TreeMap so they are sorted on the X-Values.<p>
     *
     * This TreeMap will not allow identical DataPoint2D. A comparator and equals()
     * is used to determine equality. Since you can specify any comparator you
     * want, this ArbitrarilyDiscretizedFunc can be adopted for most purposes.<p>
     *
     * Note: SWR: I had to make a special org.scec. version of the Java TreeMap and
     * subclass DataPoint2DTreeMap to access internal objects in the Java TreeMap.
     * Java's Treemap had internal objects hidden as private, I exposed them
     * to subclasses by making them protected in org.scec.data.TreeMap. This
     * was neccessary for index access to the points in the TreeMap. Seems like a poor
     * oversight on the part of Java.<p>
     */
    protected DataPoint2DTreeMap points = null;


    /**
     * Constructor that takes a DataPoint2D Comparator. The comparator is used
     * for sorting the DataPoint2D. Using the no-arg constructor instantiates
     * the default Comparator that compares only x-values within tolerance to
     * determine if two points are equal.<p>
     *
     * The passed in comparator must be an implementor of DataPoint2DComparatorAPI.
     * These comparators know they are dealing with a DataPoint2D and usually
     * only compare the x-values for sorting. Special comparators may wish to
     * sort on both the x and y values, i.e. the data points are geographical
     * locations.
     */
    public ArbitrarilyDiscretizedFunc(Comparator comparator) {
        if( !( comparator instanceof DataPoint2DComparatorAPI ) ){
            throw new DataPoint2DException("Comparator must implement DataPoint2DComparatorAPI");
        }
        points = new DataPoint2DTreeMap(comparator);
    }

    /**
     * Constructor that takes a DataPoint2D Comparator. The comparator is used
     * for sorting the DataPoint2D. Using the no-arg constructor instantiates
     * the default Comparator that compares only x-values within tolerance to
     * determine if two points are equal.<p>
     *
     * The passed in comparator must be an implementor of DataPoint2DComparatorAPI.
     * These comparators know they are dealing with a DataPoint2D and usually
     * only compare the x-values for sorting. Special comparators may wish to
     * sort on both the x and y values, i.e. the data points are geographical
     * locations.
     */
    public ArbitrarilyDiscretizedFunc(DataPoint2DComparatorAPI comparator) {
        points = new DataPoint2DTreeMap(comparator);
    }

    /**
     * No-Arg Constructor that uses the default DataPoint2DToleranceComparator comparator.
     * The comparator is used for sorting the DataPoint2D. This default Comparator
     * compares only x-values within tolerance to determine if two points are equal.<p>
     */
    public ArbitrarilyDiscretizedFunc(double toleranace) {
        DataPoint2DToleranceComparator comparator = new DataPoint2DToleranceComparator();
        comparator.setTolerance(tolerance);
        points = new DataPoint2DTreeMap(comparator);
    }

    /**
     * No-Arg Constructor that uses the default DataPoint2DToleranceComparator comparator.
     * The comparator is used for sorting the DataPoint2D. This default Comparator
     * compares only x-values within tolerance to determine if two points are equal.<p>
     *
     * The default tolerance of 0 is used. This means that two x-values must be exactly
     * equal doubles to be considered equal.
     */
    public ArbitrarilyDiscretizedFunc() { points = new DataPoint2DTreeMap(); }



    /**
     * Sets the tolerance of this function. Overides the default function in the
     * abstract class in that it calls setTolerance in the tree map which
     * updates the comparator in there.
     *
     * These field getters and setters provide the basic information to describe
     * a function. All functions have a name, information string,
     * and a tolerance level that specifies how close two points
     * have to be along the x axis to be considered equal.
     */

    public void setTolerance(double newTolerance) throws InvalidRangeException {
        if( newTolerance < 0 )
            throw new InvalidRangeException("Tolerance must be larger or equal to 0");
        tolerance = newTolerance;
        points.setTolerance(newTolerance);
    }

    /** returns the number of points in this function list */
    public int getNum(){ return points.size(); }

     /**
      * return the minimum x value along the x-axis. Since the values
      * are sorted this is a very quick lookup
      */
    public double getMinX(){ return ((DataPoint2D)points.firstKey()).getX(); }
    /**
      * return the maximum x value along the x-axis. Since the values
      * are sorted this is a very quick lookup
      */
    public double getMaxX(){ return ((DataPoint2D)points.lastKey()).getX(); }

    /**
     * Return the minimum y value along the y-axis. This value is calculated
     * every time a DataPoint2D is added to the list and cached as a variable
     * so this function returns very quickly. Slows down adding new points
     * slightly.  I assume that most of the time these lists will be created
     * once, then used for plotting and in other functions, in other words
     * more lookups than inserts.
     */
    public double getMinY(){ return points.getMinY(); }
    /**
     * Return the maximum y value along the y-axis. This value is calculated
     * every time a DataPoint2D is added to the list and cached as a variable
     * so this function returns very quickly. Slows down adding new points
     * slightly.  I assume that most of the time these lists will be created
     * once, then used for plotting and in other functions, in other words
     * more lookups than inserts.
     */
     public double getMaxY(){ return points.getMaxY(); }


    /**
     * Returns the nth (x,y) point in the Function, else null
     * if this index point doesn't exist */
    public DataPoint2D get(int index){ return points.get(index); }


    /** Returns the x value of a point given the index */
    public double getX(int index){ return get(index).getX(); }

    /** Returns the y value of a point given the index */
    public double getY(int index){ return get(index).getY(); }

    /** returns the Y value given an x value - within tolerance, returns null if not found */
    public double getY(double x){ return points.get( x ).getY(); }


    /** returns the Y value given an x value - within tolerance, returns null if not found */
    public int getIndex(DataPoint2D point){ return points.getIndex( point ); }

    /** Returns the x value of a point given the index */
    public int getXIndex(double x){ return points.getIndex( new DataPoint2D(x, 0.0 ) ); }

    /** Returns the y value of a point given the index */
    public int getYIndex(double y){
        throw new UnsupportedOperationException(C + ": Not implemented yet,l needs a Y comparator similar to the X value comparator.");
    }

    /** Either adds a new DataPoint, or replaces an existing one, within tolerance */
    public void set(DataPoint2D point) throws DataPoint2DException{ points.put(point); }

    /**
     * Either adds a new DataPoint, or replaces an existing one, within tolerance,
     * created from the input x and y values.
     */
    public void set(double x, double y) throws DataPoint2DException{ set(new DataPoint2D(x,y)); }


    /**
     * Replaces a y value for an existing point, accessed by index. If no DataPoint exists
     * nothing is done.
     */
    public void set(int index, double y) throws DataPoint2DException{
        DataPoint2D point = get(index);
        if( point != null ) {
            point.setY(y);
            set(point);
        }
    }

    /**
     * Determinces if a DataPoit2D exists in the treemap base on it's x value lookup.
     * Returns true if found, else false if point not in list.
     */
    public boolean hasPoint(DataPoint2D point){
        int index = getIndex(point);
        if( index < 0 ) return false;
        else return true;
    }

    /**
     * Determinces if a DataPoit2D exists in the treemap base on it's x value lookup.
     * Returns true if found, else false if point not in list.
     */
    public boolean hasPoint(double x, double y){
        return hasPoint( new DataPoint2D(x, y) );
    }



    /**
     * Returns an iterator over all datapoints in the list. Results returned
     * in sorted order. Returns null if no points present.
     * @return
     */
    public Iterator getPointsIterator(){
        Set keys = points.keySet();
        if( keys != null ) return keys.iterator();
        else return null;
    }

    /**
     * Returns an iterator over all x-values in the list. Results returned
     * in sorted order. Returns null if no points present.
     * @return
     */
    public ListIterator getXValuesIterator(){
        ArrayList list = new ArrayList();
        int max = points.size();
        for( int i = 0; i < max; i++){
            list.add( new Double(this.getX(i)) );
        }
        return list.listIterator();
    }

    /**
     * Returns an iterator over all y-values in the list. Results returned
     * in sorted order along the x-axis. Returns null if no points present.
     * @return
     */
    public ListIterator getYValuesIterator(){
        ArrayList list = new ArrayList();
        int max = points.size();
        for( int i = 0; i < max; i++){
            list.add( new Double(this.getY(i)));
        }
        return list.listIterator();
    }


    /**
     * Given the imput y value, finds the two sequential
     * x values with the closest y values, then calculates an
     * interpolated x value for this y value, fitted to the curve. <p>
     *
     * Since there may be multiple y values with the same value, this
     * function just matches the first found.
     *
     * @param y(value for which interpolated first x value has to be found
     * @return x(this  is the interpolated x based on the given y value)
     */

    public double getFirstInterpolatedX(double y){
      // finds the size of the point array
       int max=points.size();
       double y1=Double.NaN;
       double y2=Double.NaN;
       int i;

       //if passed parameter(y value) is not within range then throw exception
       if(y<getY(max-1) || y>getY(0))
          throw new InvalidRangeException("Y Value must be within the range: "+getY(0)+" and "+getY(max-1));

      //finds the Y values within which the the given y value lies
       for(i=0;i<max-1;++i) {
         y1=getY(i);
         y2=getY(i+1);
        if(y<=y1 && y>=y2)
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
     * Given the imput x value, finds the two sequential
     * x values with the closest x values, then calculates an
     * interpolated y value for this x value, fitted to the curve.
     *
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


    /**
     * This function returns a new copy of this list, including copies
     * of all the points. A shallow clone would only create a new DiscretizedFunc
     * instance, but would maintain a reference to the original points. <p>
     *
     * Since this is a clone, you can modify it without changing the original.
     * @return
     */
    public DiscretizedFuncAPI deepClone(){

        ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc(  );
        function.setTolerance( this.getTolerance() );

        Iterator it = this.getPointsIterator();
        if( it != null ) {
            while(it.hasNext()) {
                function.set( (DataPoint2D)((DataPoint2D)it.next()).clone() );
            }
        }

        return function;

    }

    /**
     * Determines if two functions are the same by comparing
     * that each point x value is the same. This requires
     * the two lists to have the same number of points.
     */
    public boolean equalXValues(DiscretizedFuncAPI function){
        String S = C + ": equalXValues():";
        if( this.getNum() != function.getNum() ) return false;
        Iterator it = this.getPointsIterator();
        while(it.hasNext()) {
            DataPoint2D point = (DataPoint2D)it.next();
            if( !function.hasPoint( point ) ) return false;
        }
        return true;

    }



    /**
     * Standard java function, usually used for debugging, prints out
     * the state of the list, such as number of points, the value of each point, etc.
     * @return
     */
    public String toString(){

        StringBuffer b = new StringBuffer();

        Iterator it = this.getPointsIterator();
        while(it.hasNext()) {

            DataPoint2D point = (DataPoint2D)it.next();
            b.append( point.toString() );

        }

        return b.toString();
    }


    /**
     * Almost the same as toString() but used
     * specifically in a debugging context. Formatted slightly different
     * @return
     */
    public String toDebugString(){

        StringBuffer b = new StringBuffer();
        b.append(C + ": Log values:\n");
        Iterator it = this.getPointsIterator();
        while(it.hasNext()) {

            DataPoint2D point = (DataPoint2D)it.next();
            b.append( point.toString() + '\n');

        }


        return b.toString();
    }


    /*
    public void rebuild(){

        // make temporary storage
        ArrayList points = new ArrayList();

        // get all points
        Iterator it = getPointsIterator();
        if( it != null ) while(it.hasNext()) { points.add( (DataPoint2D)it.next() ); }

        // get all non-log points if any
        it = getNonLogPointsIterator();
        if( it != null ) while(it.hasNext()) { points.add( (DataPoint2D)it.next() ); }

        // clear permanent storage
        points.clear();
        nonPositivepoints.clear();

        // rebuild permanent storage
        it = points.listIterator();
        if( it != null ) while(it.hasNext()) { set( (DataPoint2D)it.next() ); }

        if( D ) System.out.println("rebuild: " + toDebugString());
        points = null;
    }

    public boolean isYLog() { return yLog; }
    public void setYLog(boolean yLog) {

        if( yLog != this.yLog ) {
            this.yLog = yLog;
            rebuild();
        }
    }

    public boolean isXLog() { return xLog; }
    public void setXLog(boolean xLog) {
        if( xLog != this.xLog ) {
            this.xLog = xLog;
            rebuild();
        }
    }

    */


}
