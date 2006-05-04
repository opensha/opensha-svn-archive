package org.opensha.sha.surface;
import java.lang.ArrayIndexOutOfBoundsException;

import java.util.*;
import org.opensha.data.Location;
import org.opensha.exceptions.InvalidRangeException;
import org.opensha.data.*;

/**
 * <b>Title:</b> GriddedSurfaceAPI<p>
 * <b>Description:</b>
 *
 * The GriddedSurfaceAPI represents a geographical
 * surface of Location objects slicing through or on the surface of the earth.
 * Recall that a Container2DAPI represents a collection of Objects in
 * a matrix, or grid, accessed by row and column inedexes. All GriddedSurfaces
 * do is to constrain the object at each grid point to be a Location object.
 * There are also additional calculation methods specific to surfaces,
 * such as aveDip, aveStrike, etc. that depends on the grid objects
 * being Location objects. <p>
 *
 * There are no constraints on what locations are put where, but the presumption
 * is that the the grid of locations map out the surface .
 * it is also presumed that the zeroeth row represent the top edge (or trace). <p>
 *
 * @author     Steven W. Rock & others
 * @created    February 26, 2002
 * @version    1.0
 */
public interface GriddedSurfaceAPI extends Container2DAPI {

    /** Returns the average dip of the surface.  */
    public double getAveDip()throws UnsupportedOperationException;
;


    /** Returns the average strike of the surface.  */
    public double getAveStrike()throws UnsupportedOperationException;

    /**
     *  Retrieves a Location in the 2D grid - does the same thing as get() except
     *  that it casts the returned object to a Location.
     *
     * @param  row     The row to set this Location at.
     * @param  column  The column to set this Location at.
     * @return         The location value
     */
    public Location getLocation( int row, int column );


    /** Does same thing as listIterator() in super Interface */
    public ListIterator getLocationsIterator();

    /**
     * Put all the locations of this surface into a location list
     *
     * @return
     */
    public LocationList getLocationList();

    /** Common debug string that most Java classes implement */
    public String toString();


    /**
     * Returns the Metadata for the surface
     * @return String
     */
    public String getSurfaceMetadata();

}
