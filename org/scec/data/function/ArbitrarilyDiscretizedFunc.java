package org.scec.data.function;

import org.scec.data.DataPoint2D;
import org.scec.exceptions.DataPoint2DException;

import java.util.*;

import org.scec.util.*;
import org.scec.exceptions.*;
import org.scec.param.ParameterList;
import org.scec.data.*;

/**
 * <b>Title:</b> ArbitrarilyDiscretizedFunction2D<br>
 * <b>Description:</b> This class allows any spacing betwee the x-points, i.e.
 * there is no order to the spacing.<p>
 *
 * SWR: Note - not all funtionality has been implemented yet, such as all
 * iterators except getPointsIterator().<p>
 *
 *
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class ArbitrarilyDiscretizedFunc extends DiscretizedFunc {

    protected final static String C = "ArbitrarilyDiscretizedFunc";
    protected final static boolean D = true;

    /**
     * The set of DataPoints2D that conprise the discretized function. These
     * are stored in a DataPoint2D TreeMap so they are sorted on the X-Values
     */
    protected DataPoint2DTreeMap points = null;


    /**
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
     * The passed in comparator must be an implementor of DataPoint2DComparatorAPI.
     * These comparators know they are dealing with a DataPoint2D and usually
     * only compare the x-values for sorting. Special comparators may wish to
     * sort on both the x and y values, i.e. the data points are geographical
     * locations.
     */
    public ArbitrarilyDiscretizedFunc(DataPoint2DComparatorAPI comparator) {
        points = new DataPoint2DTreeMap(comparator);
    }

    /** Easiest one to use, uses the default DataPoint2DToleranceComparator comparator. */
    public ArbitrarilyDiscretizedFunc(Double toleranace) {
        DataPoint2DToleranceComparator comparator = new DataPoint2DToleranceComparator();
        comparator.setTolerance(tolerance);
        points = new DataPoint2DTreeMap(comparator);
    }

    public ArbitrarilyDiscretizedFunc() { points = new DataPoint2DTreeMap(); }



    public void setTolerance(Double newTolerance) throws InvalidRangeException {

        if( newTolerance.doubleValue() < 0 )
            throw new InvalidRangeException("Tolerance must be larger or equal to 0");
        tolerance = newTolerance;
        points.setTolerance(newTolerance);
    }

    public int getNum(){ return points.size(); }

    public Double getMinX(){ return ((DataPoint2D)points.firstKey()).getX(); }
    public Double getMaxX(){ return ((DataPoint2D)points.lastKey()).getX(); }

    public Double getMinY(){ return points.getMinY(); }
    public Double getMaxY(){ return points.getMaxY(); }


    public DataPoint2D get(int index){ return points.get(index); }


    /** Returns the x value of a point given the index */
    public Double getX(int index){ return get(index).getX(); }

    /** Returns the y value of a point given the index */
    public Double getY(int index){ return get(index).getY(); }

    /** returns the Y value given an x value - within tolerance, returns null if not found */
    public Double getY(Double x){ return points.get( x ).getY(); }


    /** returns the Y value given an x value - within tolerance, returns null if not found */
    public int getIndex(DataPoint2D point){ return points.getIndex( point ); }

    /** Returns the x value of a point given the index */
    public int getXIndex(Double x){ return points.getIndex( new DataPoint2D(x, new Double(0.0) ) ); }

    /** Returns the y value of a point given the index */
    public int getYIndex(Double y){
        throw new UnsupportedOperationException(C + ": Not implemented yet,l needs a Y comparator similar to the X value comparator.");
    }


    public void set(DataPoint2D point) throws DataPoint2DException{ points.put(point); }
    public void set(Double x, Double y) throws DataPoint2DException{ set(new DataPoint2D(x,y)); }
    public void set(int index, Double y) throws DataPoint2DException{
        DataPoint2D point = get(index);
        point.setY(y);
        set(point);
    }


    public boolean hasPoint(DataPoint2D point){
        int index = getIndex(point);
        if( index < 0 ) return false;
        else return true;
    }

    public boolean hasPoint(Double x, Double y){
        return hasPoint( new DataPoint2D(x, y) );
    }



     /** Values returned in Ascending order, returns null if nothing present */
    public Iterator getPointsIterator(){
        Set keys = points.keySet();
        if( keys != null ) return keys.iterator();
        else return null;
    }

    /**  */
    public ListIterator getXValuesIterator(){
        ArrayList list = new ArrayList();
        int max = points.size();
        for( int i = 0; i < max; i++){
            list.add( this.getX(i) );
        }
        return list.listIterator();
    }

    /**  */
    public ListIterator getYValuesIterator(){
        ArrayList list = new ArrayList();
        int max = points.size();
        for( int i = 0; i < max; i++){
            list.add( this.getY(i) );
        }
        return list.listIterator();
    }


    /** FIX */
    public Double getInterpolatedX(Double y){ throw new UnsupportedOperationException(C + ": Not implemented yet."); }

    /** FIX */
    public Double getInterpolatedY(Double x){ throw new UnsupportedOperationException(C + ": Not implemented yet."); }



    /** Returns a copy of this and all points in this DiscretizedFunction */
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
     * that each point x value is the same
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
     * Returns the x and y values of all the data points in two columns
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
