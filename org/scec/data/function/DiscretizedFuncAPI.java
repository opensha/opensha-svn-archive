package org.scec.data.function;

import java.util.ListIterator;
import java.util.Iterator;
import org.scec.data.DataPoint2D;
import org.scec.exceptions.DataPoint2DException;
import org.scec.param.ParameterList;

/**
 * <b>Title:</b> DiscretizedFunction2DAPI<br>
 * <b>Description:</b> Interface that all Discretized Functions must implement.
 * A Discretized Function are the x and y points that a function describes. Instead
 * of having y=x^2, you would have a sample of possible x and y values. <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public interface DiscretizedFuncAPI extends java.io.Serializable{


    public Double getMinX();
    public Double getMaxX();

    public Double getMinY();
    public Double getMaxY();

    /** Returns the nth point in the Function */
    public DataPoint2D get(int index);

    /** returns the Y value given an x value - within tolerance */
    public Double getY(Double x);

    public int getNum();


    public void setTolerance(Double newTolerance);
    public Double getTolerance();

    // public void set(DataPoint2D point, int index) throws DataPoint2DException;
    // public void set(Double x, Double y, int index) throws DataPoint2DException;

    /** Either adds a new DataPoint, or replaces an existing one, within tolerance */
    public void set(DataPoint2D point) throws DataPoint2DException;
    /** Either adds a new DataPoint, or replaces an existing one, within tolerance */
    public void set(Double x, Double y) throws DataPoint2DException;

    public boolean hasPoint(DataPoint2D point);
    public boolean hasPoint(Double x, Double y);


    public Iterator getPointsIterator();
    public ListIterator getXValuesIterator();
    public ListIterator getYValuesIterator();

    public Double getInterpolatedX(Double y);
    public Double getInterpolatedX(double y);

    public Double getInterpolatedY(Double x);
    public Double getInterpolatedY(double x);

    public String toString();
    public Object clone();
    public boolean equals( DiscretizedFuncAPI function );


    public boolean isYLog();
    public void setYLog(boolean yLog);

    public boolean isXLog();
    public void setXLog(boolean xLog);


}
