package org.scec.sha.surface;
import java.lang.ArrayIndexOutOfBoundsException;

import java.util.*;

import org.scec.sha.fault.*;
import org.scec.data.Location;
import org.scec.util.FaultUtils;
import org.scec.exceptions.InvalidRangeException;
import org.scec.exceptions.LocationException;
import org.scec.data.*;


/**
 * <b>Title:</b> GriddedSurface<p>
 *
 * <b>Description:</b> Base implementation of the GriddedSurfaceAPI.
 * Provides all basic functionality for a GriddedSurface. Subclasses
 * will just refine specific functions. Note that this class
 * also implements a Container2DAPI so it can be used anywhere that
 * interface is expected. <p>
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
public class GriddedSurface
         extends Container2D
         implements GriddedSurfaceAPI {


    /** Class name for debugging. */
    protected final static String C = "GriddedSurface";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    /** The average strike of this surface on the Earth.  */
    protected double aveStrike=Double.NaN;

    /** The average dip of this surface into the Earth.  */
    protected double aveDip=Double.NaN;

    /** The surface area that this GriddedSurface covers in KM**2 */
    protected double surfaceArea=Double.NaN;


    /** Constructor for the GriddedSurface object - just calls super(). */
    public GriddedSurface() { super();  }


    /**
     *  Constructor for the GriddedSurface object,
     *  sets the number of rows and columns in the super() call.
     *
     * @param  numRows  Number of rows in the gridded surface.
     * @param  numCols  Number of columns in the gridded surface.
     */
    public GriddedSurface( int numRows, int numCols ) {
        super( numRows, numCols );
    }


    /**
     *  Set an object in the 2D grid. Ensures the object passed in is a Location.
     *
     * @param  row                                 The row to set the Location.
     * @param  column                              The row to set the Location.
     * @param  obj                                 Must be a Location object
     * @exception  ArrayIndexOutOfBoundsException  Thrown if the row or column lies beyond the grid space indexes.
     * @exception  ClassCastException              Thrown if the passed in Obejct is not a Location.
     */
    public void set( int row, int column, Object obj ) throws ArrayIndexOutOfBoundsException, ClassCastException {

        String S = C + ": set(): ";
        if ( obj instanceof Location ) {
            setLocation( row, column, ( Location ) obj );
        } else {
            throw new ClassCastException( S + "Object must be a Location" );
        }
    }


    /**
     *  Add a Location to the grid - does the same thing as set except that it
     *  ensures the object is a Location object.
     *
     * @param  row                                 The row to set this Location at.
     * @param  column                              The column to set this Location at.
     * @param  location                            The new location value.
     * @exception  ArrayIndexOutOfBoundsException  Thrown if the row or column lies beyond the grid space indexes.
     */
    public void setLocation( int row, int column, Location location ) {
        super.set( row, column, location );
    }


    /**
     * Sets the average strike of this surface on the Earth. An InvalidRangeException
     * is thrown if the ave strike is not a valid value, i.e. must be > 0, etc.
     */
    public void setAveStrike( double aveStrike ) throws InvalidRangeException{
        FaultUtils.assertValidStrike( aveStrike );
        this.aveStrike =  aveStrike ;
    }





    /**
     * Sets the average dip of this surface into the Earth. An InvalidRangeException
     * is thrown if the ave strike is not a valid value, i.e. must be > 0, etc.
     */
    public void setAveDip( double aveDip ) throws InvalidRangeException{
        FaultUtils.assertValidDip( aveDip );
        this.aveDip = aveDip ;
    }




    /**
     *  Retrieves a Location in the 2D grid - does the same thing as get except
     *  that it casts the returned object to a Location.
     *
     * @param  row     The row to get this Location from.
     * @param  column  The column to get this Location from.
     * @return         The location stored at the row and column.
     * @exception  LocationException  Thown if the object being retrieved cannot be cast to a Location.
     */
    public Location getLocation( int row, int col )
             throws LocationException {
        String S = C + ": getLocation():";
        if ( exist( row, col ) ) {
            return ( Location ) get( row, col );
        } else {
            throw new LocationException( S + "Requested object doesn't exist in " + row + ", " + col );
        }
    }


    /** Returns the average strike of this surface on the Earth.  */
    public double getAveStrike() { return aveStrike; }

    /** Returns the average dip of this surface into the Earth.  */
    public double getAveDip() { return aveDip; }

    /** Returns the surface area that this GriddedSurface covers in KM**2 */
    public double getSurfaceArea() { return surfaceArea; }



    /** Does same thing as listIterator() in super Interface */
    public ListIterator getLocationsIterator() { return super.listIterator(); }


    /** FIX *** Needs to be implemented */
    public double computeAveStrike() { return 0; }

    /** FIX *** Needs to be implemented */
    public double computeAveDip() { return 0; }

    /** FIX *** Needs to be implemented */
    public double computeSurfaceArea() { return 0; }


    final static char TAB = '\t';
    /** Prints out each location and fault information for debugging */
    public String toString(){

        StringBuffer b = new StringBuffer();
        b.append( C + '\n');
        if ( aveStrike != Double.NaN ) b.append( "Ave. Strike = " + aveStrike + '\n' );
        if ( aveDip != Double.NaN ) b.append( "Ave. Dip = " + aveDip + '\n' );
        if ( surfaceArea != Double.NaN ) b.append( "Surface Area = " + surfaceArea + '\n' );

        b.append( "Row" + TAB + "Col" + TAB + "Latitude" + TAB + "Longitude" + TAB + "Depth");

        String superStr = super.toString();
        //int index = superStr.indexOf('\n');
        //if( index > 0 ) superStr = superStr.substring(index + 1);
        b.append( '\n' + superStr );

        return b.toString();
    }


}
