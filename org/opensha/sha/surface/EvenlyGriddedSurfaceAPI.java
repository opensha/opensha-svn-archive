package org.opensha.sha.surface;
import java.lang.ArrayIndexOutOfBoundsException;

import java.util.*;
import org.opensha.data.Location;
import org.opensha.exceptions.InvalidRangeException;
import org.opensha.data.*;

/**
 * <b>Title:</b> EvenlyGriddedSurfaceAPI<p>
 * <b>Description:</b>
 *
 * The EvenlyGriddedSurfaceAPI represents a geographical
 * surface of Location objects slicing through or on the surface of the earth.
 * Recall that a Container2DAPI represents a collection of Objects in
 * a matrix, or grid, accessed by row and column inedexes. All GriddedSurfaces
 * do is to constrain the object at each grid point to be a Location object.
 * There are also additional calculation methods specific to the paradigm
 * model, such as aveDip, aveStrike, etc. that depends on the grid objects
 * being Location objects. <p>
 *
 * There are no constraints on what locations are put where, but the usual presumption
 * is that the the grid of locations map out the surface in some evenly space way.
 * it is also presumed that the zeroeth row represent the top edge (or trace). <p>
 *
 * @author     Steven W. Rock & others
 * @created    February 26, 2002
 * @version    1.0
 */
public interface EvenlyGriddedSurfaceAPI extends Container2DAPI {

    /** Returns the average dip of this surface into the Earth.  */
    public double getAveDip();


    /** Returns the average strike of this surface on the Earth.  */
    public double getAveStrike();


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

    /**
     * Put all the locations of this surface into a location list
     *
     * @return
     */
    public LocationList getLocationList();

    /** Common debug string that most Java classes implement */
    public String toString();

    /**
     * Returns the grid centered location on each grid surface.
     * @return GriddedSurfaceAPI returns a Surface that has one less
     * row and col then the original surface. It averages the 4 corner location
     * on each grid surface to get the grid centered location.
     */
    public EvenlyGriddedSurfaceAPI getGridCenteredSurface() ;


    /**
     * This returns the total length of the surface
     * @return double
     */
    public double getSurfaceLength() ;


    /**
     * This returns the surface width (down dip)
     * @return double
     */
    public double getSurfaceWidth() ;


    /**
     * Returns the Metadata for the surface
     * @return String
     */
    public String getSurfaceMetadata();

    /**
     * returns the grid spacing
     *
     * @return
     */
    public  double getGridSpacing() ;


}
