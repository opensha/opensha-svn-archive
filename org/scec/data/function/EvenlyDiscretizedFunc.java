package org.scec.data.function;

import java.util.*;
import org.scec.util.*;
import org.scec.exceptions.*;
import org.scec.data.*;

/**
 * <b>Title:</b> EvenlyDiscretizedFunction2D<br>
 * <b>Description:</b> Assumes even spacing between the x points represented by
 * the delta distance<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class EvenlyDiscretizedFunc implements DiscretizedFuncAPI{

    protected final static String C = "EvenlyDiscretizedFunc";
    protected final static boolean D = false;

    protected ArrayList points = null;
    protected Double delta;


    public EvenlyDiscretizedFunc(Double min, int num, Double delta) {
        this.minX = minX;
        this.delta = delta;
        calculatePoints(num);
    }

    /**
     * The tolerance allowed in specifying a x-value near a real x-value,
     * so that the real x-value is used. Note that the tolerance must be smaller
     * than 1/2 the delta between data points.
     */
    protected Double tolerance;

    /** The minimum x-value - not sure if we need this  */
    protected Double minX;

    /** The maximum x-Value - not sure if we need this  */
    protected Double maxX;


    public void setTolerance(Double newTolerance) throws InvalidRangeException {

        if( newTolerance.doubleValue() < 0 )
            throw new InvalidRangeException("Tolerance must be larger or equal to 0");
        tolerance = newTolerance;
    }
    public Double getTolerance() { return tolerance; }


    public Double getDelta() { return delta; }

    /**
     * Populates the Vector of DataPoint2Ds once the min, num, and delta set,
     * dependent variable is then max. (max -min)/num must equal an integer value
     */
    protected void calculatePoints(int num){

    }

    /**
     * this function will throw an exception if the given x-value is not
     * within tolerance of one of the x-values in the function
     */
    public void set(DataPoint2D point) throws DataPoint2DException {

    }

    /**
     * this function will throw an exception if the given x-value is not
     * within tolerance of one of the x-values in the function
     */
    public void set(Double x, Double y) throws DataPoint2DException {

    }

    public DataPoint2D get(int index){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    /** FIX *** returns the Y value given an x value - within tolerance */
    public Double getY(Double x){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    public int getNum(){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    public Double getMinX(){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }
    public Double getMaxX(){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    public Double getMinY(){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }
    public Double getMaxY(){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }


    public boolean hasPoint(DataPoint2D point){ return false; }
    public boolean hasPoint(Double x, Double y){ return false; }


    public Iterator getPointsIterator(){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }
    public ListIterator getXValuesIterator(){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }
    public ListIterator getYValuesIterator(){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    public Double getInterpolatedX(Double y){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }
    public Double getInterpolatedX(double y){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    public Double getInterpolatedY(Double x){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }
    public Double getInterpolatedY(double x){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }




    /** FIX *** Returns a copy of this and all points in this DiscretizedFunction */
    public Object clone(){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
    }

    /**
     * FIX *** Determines if two functions are the same by comparing
     * that each point is the same
     */
    public boolean equals(DiscretizedFuncAPI function){
        throw new UnsupportedOperationException(C + ": Not implemented yet.");
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

    public boolean isYLog() { return yLog; }
    public void setYLog(boolean yLog) {

        if( yLog != this.yLog ) {
            this.yLog = yLog;


        }
    }

    public boolean isXLog() { return xLog; }
    public void setXLog(boolean xLog) {
        if( xLog != this.xLog ) {
            this.xLog = xLog;



        }
    }

    private boolean yLog;
    private boolean xLog;

}
