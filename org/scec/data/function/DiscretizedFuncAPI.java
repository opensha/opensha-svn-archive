package org.scec.data.function;

import java.util.*;
import org.scec.data.*;
import org.scec.exceptions.*;

/**
 * <b>Title:</b> DiscretizedFuncAPI<br>
 * <b>Description:</b> Interface that all Discretized Functions must implement.
 * A Discretized Function are the x and y points that a function describes. Instead
 * of having y=x^2, you would have a sample of possible x and y values. <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * @author Steven W. Rock
 * @version 1.0
 */

public interface DiscretizedFuncAPI extends java.io.Serializable, NamedObjectAPI{


    /* ******************************/
    /* Basic Fields Getters/Setters */
    /* ******************************/

    public void setName( String name );
    public String getName();

    public void setInfo( String info );
    public String getInfo();

    public void setTolerance(Double newTolerance);
    public Double getTolerance();



    /* ******************************/
    /* Metrics about list as whole  */
    /* ******************************/

    public int getNum();
    public Double getMinX();
    public Double getMaxX();
    public Double getMinY();
    public Double getMaxY();



    /* ****************/
    /* Point Getters  */
    /* ****************/

    /** Returns the nth point in the Function */
    public DataPoint2D get(int index);
    /** returns the Y value given an index */
    public Double getX(int index);
    /** returns the Y value given an index */
    public Double getY(int index);
    /** returns the Y value given an x value - within tolerance */
    public Double getY(Double x);

    public Double getInterpolatedX(Double y);
    public Double getInterpolatedY(Double x);



    /* ***************************/
    /* Index Getters From Points */
    /* ***************************/

    public int getXIndex(Double x);
    public int getYIndex(Double y);
    public int getIndex(DataPoint2D point);



    /* ***************/
    /* Point Setters */
    /* ***************/

    /** Either adds a new DataPoint, or replaces an existing one, within tolerance */
    public void set(DataPoint2D point) throws DataPoint2DException;
    /** Either adds a new DataPoint, or replaces an existing one, within tolerance */
    public void set(Double x, Double y) throws DataPoint2DException;
    public void set(int index, Double Y);



    /* **********/
    /* Queries  */
    /* **********/

    public boolean hasPoint(DataPoint2D point);
    public boolean hasPoint(Double x, Double y);



    /* ************/
    /* Iterators  */
    /* ************/

    public Iterator getPointsIterator();
    public ListIterator getXValuesIterator();
    public ListIterator getYValuesIterator();



    /* **************************/
    /* Standard Java Functions  */
    /* **************************/

    public String toString();
    public boolean equals( DiscretizedFuncAPI function );
    public DiscretizedFuncAPI deepClone();


}
