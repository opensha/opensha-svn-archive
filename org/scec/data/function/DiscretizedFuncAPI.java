package org.scec.data.function;

import java.util.*;
import org.scec.data.*;
import org.scec.exceptions.*;

/**
 * <b>Title:</b> DiscretizedFuncAPI<br>
 * <b>Description:</b> Interface that all Discretized Functions must implement. <P>
 *
 * A Discretized Function is a collection of x and y values grouped together as
 * the points that describe a function. A discretized form of a function is the
 * only ways computers can represent functions. Instead of having y=x^2, you
 * would have a sample of possible x and y values. <p>
 *
 * This functional framework is modeled after mathmatical functions
 * such as sin(x), etc. It assumes that there are no duplicate x values,
 * and that if two points have the same x value but different y values,
 * they are still considered equal. The framework also sorts the points along the
 * x axis, so the first point contains the mimimum x-value and the last
 * point contains the maximum value.<p>
 *
 * Since this API represents the points in a list, alot of these API functions
 * are standard list access functions.<p>
 *
 * DataPoint2D = (x,y)
 *
 * @author Steven W. Rock
 * @see DataPoint2D
 * @version 1.0
 */

public interface DiscretizedFuncAPI extends java.io.Serializable, NamedObjectAPI{


    /* ******************************/
    /* Basic Fields Getters/Setters */
    /* ******************************/

    /**
     * Sets the name of this function.
     *
     * These field getters and setters provide the basic information to describe
     * a function. All functions have a name, information string,
     * and a tolerance level that specifies how close two points
     * have to be along the x axis to be considered equal.
     */
    public void setName( String name );
    /**
     * Returns the name of this function.
     *
     * These field getters and setters provide the basic information to describe
     * a function. All functions have a name, information string,
     * and a tolerance level that specifies how close two points
     * have to be along the x axis to be considered equal.
     */
    public String getName();

    /**
     * Sets the info string of this function.
     *
     * These field getters and setters provide the basic information to describe
     * a function. All functions have a name, information string,
     * and a tolerance level that specifies how close two points
     * have to be along the x axis to be considered equal.
     */
    public void setInfo( String info );
    /**
     * Returns the info of this function.
     *
     * These field getters and setters provide the basic information to describe
     * a function. All functions have a name, information string,
     * and a tolerance level that specifies how close two points
     * have to be along the x axis to be considered equal.
     */
    public String getInfo();

    /**
     * Sets the tolerance of this function.
     *
     * These field getters and setters provide the basic information to describe
     * a function. All functions have a name, information string,
     * and a tolerance level that specifies how close two points
     * have to be along the x axis to be considered equal.
     */
    public void setTolerance(double newTolerance);
    /**
     * Returns the tolerance of this function.
     *
     * These field getters and setters provide the basic information to describe
     * a function. All functions have a name, information string,
     * and a tolerance level that specifies how close two points
     * have to be along the x axis to be considered equal.
     */
    public double getTolerance();



    /* ******************************/
    /* Metrics about list as whole  */
    /* ******************************/


    /** returns the number of points in this function list */
    public int getNum();

    /** return the minimum x value along the x-axis */
    public double getMinX();

    /** return the maximum x value along the x-axis */
    public double getMaxX();

    /** return the minimum y value along the y-axis */
    public double getMinY();

    /** return the maximum y value along the y-axis */
    public double getMaxY();



    /* ****************/
    /* Point Getters  */
    /* ****************/

    /** Returns the nth (x,y) point in the Function */
    public DataPoint2D get(int index);

    /** returns the y value given an index */
    public double getX(int index);

    /** returns the y value given an index */
    public double getY(int index);

    /** returns the y value given an x value - within tolerance */
    public double getY(double x);

    /**
     * Given the imput y value, finds the two sequential
     * x values with the closest y values, then calculates an
     * interpolated x value for this y value, fitted to the curve. <p>
     *
     * Since there may be multiple y values with the same value, this
     * function just matches the first found.
     */
    public double getFirstInterpolatedX(double y);

    /**
     * Given the imput x value, finds the two sequential
     * x values with the closest x values, then calculates an
     * interpolated y value for this x value, fitted to the curve.
     */
    public double getInterpolatedY(double x);



    /* ***************************/
    /* Index Getters From Points */
    /* ***************************/

    /**
     * Since the x-axis is sorted and points stored in a list,
     * they can be accessed by index. This function returns the index
     * of the specified x value if found, else returns -1.
     */
    public int getXIndex(double x);

    /**
     * Since the x-axis is sorted and points stored in a list,
     * they can be accessed by index. This function returns the index
     * of the specified x value in the DataPoint2D if found, else returns -1.
     */
    public int getIndex(DataPoint2D point);



    /* ***************/
    /* Point Setters */
    /* ***************/

    /** Either adds a new DataPoint, or replaces an existing one, within tolerance */
    public void set(DataPoint2D point) throws DataPoint2DException;

    /**
     * Creates a new DataPoint, then either adds it if it doesn't exist,
     * or replaces an existing one, within tolerance
     */
    public void set(double x, double y) throws DataPoint2DException;

    /** Replaces a DataPoint y-value at the specifed index. */
    public void set(int index, double Y);



    /* **********/
    /* Queries  */
    /* **********/

    /**
     * Determine wheither a point exists in the list,
     * as determined by it's x-value within tolerance.
     */
    public boolean hasPoint(DataPoint2D point);


    /**
     * Determine wheither a point exists in the list,
     * as determined by it's x-value within tolerance.
     */
    public boolean hasPoint(double x, double y);



    /* ************/
    /* Iterators  */
    /* ************/

    /**
     * Returns an iterator over all datapoints in the list. Results returned
     * in sorted order.
     * @return
     */
    public Iterator getPointsIterator();


    /**
     * Returns an iterator over all x-values in the list. Results returned
     * in sorted order.
     * @return
     */
    public ListIterator getXValuesIterator();


    /**
     * Returns an iterator over all y-values in the list. Results returned
     * in sorted order along the x-axis.
     * @return
     */
    public ListIterator getYValuesIterator();



    /* **************************/
    /* Standard Java Functions  */
    /* **************************/

    /**
     * Standard java function, usually used for debugging, prints out
     * the state of the list, such as number of points, the value of each point, etc.
     * @return
     */
    public String toString();

    /**
     * Determines if two lists are equal. Typical implementation would verify
     * same number of points, and the all points are equal, using the DataPoint2D
     * equals() function.
     * @param function
     * @return
     */
    public boolean equals( DiscretizedFuncAPI function );

    /**
     * This function returns a new copy of this list, including copies
     * of all the points. A shallow clone would only create a new DiscretizedFunc
     * instance, but would maintain a reference to the original points. <p>
     *
     * Since this is a clone, you can modify it without changing the original.
     * @return
     */
    public DiscretizedFuncAPI deepClone();


}
