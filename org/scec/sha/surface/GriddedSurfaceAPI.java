package org.scec.sha.surface;
import java.lang.ArrayIndexOutOfBoundsException;

import java.util.*;
import org.scec.data.Location;
import org.scec.exceptions.InvalidRangeException;
import org.scec.data.*;

/**
 *  <b>Title:</b> GriddedSurfaceAPI<br>
 *  <b>Description:</b> All gridded surfaces must implement these functions<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public interface GriddedSurfaceAPI extends Container2DAPI {

    /**
     *  Gets the aveDip attribute of the GriddedSurfaceAPI object
     *
     * @return    The aveDip value
     */
    public double getAveDip();




    /**
     *  Sets the aveDip attribute of the GriddedSurfaceAPI object
     *
     * @param  aveDip  The new aveDip value
     */
    public void setAveDip( double aveDip ) throws InvalidRangeException;


    /**
     *  Gets the aveStrike attribute of the GriddedSurfaceAPI object
     *
     * @return    The aveStrike value
     */
    public double getAveStrike();




    /**
     *  Sets the aveStrike attribute of the GriddedSurfaceAPI object
     *
     * @param  aveStrike  The new aveStrike value
     */
    public void setAveStrike( double aveStrike ) throws InvalidRangeException;


    /**
     *  Add a Location to the grid - does the same thing as set except that it
     *  ensures the object is a Location object
     *
     * @param  row                                 The new location value
     * @param  column                              The new location value
     * @param  location                            The new location value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public void setLocation( int row, int column, Location location )
             throws ArrayIndexOutOfBoundsException;


    /**
     *  Retrieves a Location in the 2D grid - does the same thing as get except
     *  that it casts the returned object to a Location
     *
     * @param  row     Description of the Parameter
     * @param  column  Description of the Parameter
     * @return         The location value
     */
    public Location getLocation( int row, int column );


    /**
     *  does same thing as listIterator() in super Interface -
     *
     * @return    The locationsIterator value
     */
    public ListIterator getLocationsIterator();

    public String toString();




}
