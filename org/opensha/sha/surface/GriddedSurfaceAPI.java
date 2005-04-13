package org.opensha.sha.surface;
import java.lang.ArrayIndexOutOfBoundsException;

import java.util.*;
import org.opensha.data.Location;
import org.opensha.exceptions.InvalidRangeException;
import org.opensha.data.*;

/**
 * <b>Title:</b> GriddedSurfaceAPI<p>
 * <b>Description:</b> All GriddedSurface classes must implement
 * this interface. This defines the public API that all surfaces
 * must implement. <p>
 *
 * This interface extends teh Container2DAPI so every GriddedSurface
 * class implementation will also implement that API. <p>
 *
 * The paradigm of the GriddedSurface is that it represents a geographical
 * surface of Location objects slicing through or on the surface of the earth.
 * Recall that a Container2DAPI represents a collection of Objects in
 * a matrix, or grid, accessed by row and column inedexes. All GriddedSurfaces
 * do is to constrain the object at each grid point to be a Location object.
 * There are also additional calculation methods specific to the paradigm
 * model, such as aveDip, aveStrike, etc. that depends on the grid objects
 * being Location objects. <p>
 *
 * The only constraint that will be imposed on these Locations is that
 * they are ordered geographcally. That means that if one Location is physically
 * next to another, those Locations will be next to each other in the grid.
 * Note this does not imply even spacing, nor a geographic straight line. This means
 * that all points along row one may not be at the same longitude. Subclasses
 * may impose further constraints. <p>
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */
public interface GriddedSurfaceAPI extends Container2DAPI {

    /** Returns the average dip of this surface into the Earth.  */
    public double getAveDip();

    /** Sets the average dip of this surface into the Earth.  */
    public void setAveDip( double aveDip ) throws InvalidRangeException;


    /** Returns the average strike of this surface on the Earth.  */
    public double getAveStrike();

    /** Sets the average strike of this surface on the Earth.  */
    public void setAveStrike( double aveStrike ) throws InvalidRangeException;


     /**
     *  Add a Location to the grid - does the same thing as set except that it
     *  ensures the object is a Location object.
     *
     * @param  row                                 The row to set this Location at.
     * @param  column                              The column to set this Location at.
     * @param  location                            The new location value.
     * @exception  ArrayIndexOutOfBoundsException  Thrown if the row or column lies beyond the grid space indexes.
     */
    public void setLocation( int row, int column, Location location )
             throws ArrayIndexOutOfBoundsException;


    /**
     *  Retrieves a Location in the 2D grid - does the same thing as get except
     *  that it casts the returned object to a Location.
     *
     * @param  row     The row to set this Location at.
     * @param  column  The column to set this Location at.
     * @return         The location value
     */
    public Location getLocation( int row, int column );


    /** Does same thing as listIterator() in super Interface */
    public ListIterator getLocationsIterator();

    /** Common debug string that most Java classes implement */
    public String toString();



}
