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

public class ArbitrarilyDiscretizedFunc implements DiscretizedFuncAPI {

    protected final static String C = "ArbitrarilyDiscretizedFunc";
    protected final static boolean D = true;

    private boolean yLog = false;
    private boolean xLog = false;


    /**
     * The set of DataPoints2D that conprise the discretized function. These
     * are stored in a DataPoint2D TreeMap so they are sorted on the X-Values
     */
    protected DataPoint2DTreeMap dataPoints2D = null;
    protected DataPoint2DTreeMap nonPositivedataPoints2D = null;


    /**
     * The tolerance allowed in specifying a x-value near a real x-value,
     * so that the real x-value is used. Note that the tolerance must be smaller
     * than 1/2 the delta between data points.
     */
    protected Double tolerance;


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
        dataPoints2D = new DataPoint2DTreeMap(comparator);
        nonPositivedataPoints2D = new DataPoint2DTreeMap(comparator);
    }

    /**
     * The passed in comparator must be an implementor of DataPoint2DComparatorAPI.
     * These comparators know they are dealing with a DataPoint2D and usually
     * only compare the x-values for sorting. Special comparators may wish to
     * sort on both the x and y values, i.e. the data points are geographical
     * locations.
     */
    public ArbitrarilyDiscretizedFunc(DataPoint2DComparatorAPI comparator) {
        dataPoints2D = new DataPoint2DTreeMap(comparator);
        nonPositivedataPoints2D = new DataPoint2DTreeMap(comparator);
    }

    /** Easiest one to use, uses the default DataPoint2DToleranceComparator comparator. */
    public ArbitrarilyDiscretizedFunc(Double toleranace) {
        DataPoint2DToleranceComparator comparator = new DataPoint2DToleranceComparator();
        comparator.setTolerance(tolerance);
        dataPoints2D = new DataPoint2DTreeMap(comparator);
        nonPositivedataPoints2D = new DataPoint2DTreeMap(comparator);
    }



    public ArbitrarilyDiscretizedFunc() {
        dataPoints2D = new DataPoint2DTreeMap();
        nonPositivedataPoints2D = new DataPoint2DTreeMap();
    }

    public void setTolerance(Double newTolerance) throws InvalidRangeException {

        if( newTolerance.doubleValue() < 0 )
            throw new InvalidRangeException("Tolerance must be larger or equal to 0");
        tolerance = newTolerance;
        dataPoints2D.setTolerance(newTolerance);
        nonPositivedataPoints2D.setTolerance(newTolerance);
    }
    public Double getTolerance() { return tolerance; }




    public void set(DataPoint2D point) throws DataPoint2DException{

        boolean positive = true;
        if( yLog && point.getY().doubleValue() <= 0) positive = false;
        if( xLog && point.getX().doubleValue() <= 0) positive = false;

        if( positive ) dataPoints2D.put(point);
        else nonPositivedataPoints2D.put(point);

    }

    public void set(Double x, Double y) throws DataPoint2DException{
        set(new DataPoint2D(x,y));
    }

    public DataPoint2D get(int index){ return this.dataPoints2D.get(index); }

     /** Values returned in Ascending order */
    public Iterator getPointsIterator(){
        Set keys = dataPoints2D.keySet();
        if( keys != null ) return keys.iterator();
        else return null;
    }

     /** Values returned in Ascending order */
    public Iterator getNonLogPointsIterator(){
        Set keys = nonPositivedataPoints2D.keySet();
        if( keys != null ) return keys.iterator();
        else return null;
    }

    /** FIX *** returns the Y value given an x value - within tolerance */
    public Double getY(Double x){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    public int getNum(){ return dataPoints2D.size(); }

    public Double getMinX(){ return ((DataPoint2D)dataPoints2D.firstKey()).getX(); }
    public Double getMaxX(){ return ((DataPoint2D)dataPoints2D.lastKey()).getX(); }

    public Double getMinY(){ return dataPoints2D.getMinY(); }
    public Double getMaxY(){ return dataPoints2D.getMaxY(); }

    /**
     * FIX *** Removes a point within tolerance length, throws an
     * exception if no point exist
     */
    public void removePoint(Double x, Double y){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    /**
     * FIX *** Removes a point within tolerance length, throws an
     * exception if no point exist
     */
    public void removePoint(DataPoint2D point) throws DataPoint2DException{
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }


    public boolean hasPoint(DataPoint2D point){
        if( dataPoints2D.containsKey ( point ) ) return true;
        else return false;
    }

    public boolean hasPoint(Double x, Double y){
        if( dataPoints2D.containsKey ( new DataPoint2D(x, y) ) ) return true;
        else return false;
    }







    /** FIX */
    public ListIterator getXValuesIterator(){ throw new UnsupportedOperationException(C + ": Not implemented yet."); }
    /** FIX */
    public ListIterator getYValuesIterator(){ throw new UnsupportedOperationException(C + ": Not implemented yet."); }

    /** FIX */
    public Double getInterpolatedX(Double y){ throw new UnsupportedOperationException(C + ": Not implemented yet."); }
    /** FIX */
    public Double getInterpolatedX(double y){ throw new UnsupportedOperationException(C + ": Not implemented yet."); }

    /** FIX */
    public Double getInterpolatedY(Double x){ throw new UnsupportedOperationException(C + ": Not implemented yet."); }
    /** FIX */
    public Double getInterpolatedY(double x){ throw new UnsupportedOperationException(C + ": Not implemented yet."); }


    /** Returns a copy of this and all points in this DiscretizedFunction */
    public Object clone(){

        ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
        function.setTolerance( this.getTolerance() );
        function.yLog = this.yLog;
        function.xLog = this.xLog;


        Iterator it = this.getPointsIterator();
        if( it != null ) {
            while(it.hasNext()) {
                function.set( (DataPoint2D)((DataPoint2D)it.next()).clone() );
            }
        }

        it = this.getNonLogPointsIterator();
        if( it != null ) {
            while(it.hasNext()) {
                function.set( (DataPoint2D)((DataPoint2D)it.next()).clone() );
            }
        }

        return function;

    }

    /**
     * Determines if two functions are the same by comparing
     * that each point is the same
     */
    public boolean equals(DiscretizedFuncAPI function){
        String S = C + ": equals():";

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

        b.append("Non-Log values:\n");
        it = this.getNonLogPointsIterator();
        while(it.hasNext()) {

            DataPoint2D point = (DataPoint2D)it.next();
            b.append( point.toString() + '\n');

        }

        return b.toString();
    }

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
        dataPoints2D.clear();
        nonPositivedataPoints2D.clear();

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



}
