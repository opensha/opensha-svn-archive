package org.scec.sha.surface;

import java.util.ListIterator;
import java.util.Vector;

import org.scec.util.FaultUtils;
import org.scec.data.Location;
import org.scec.exceptions.InvalidRangeException;

// Fix - Needs more comments

/**
 *  <b>Title:</b> PointSurface<p>
 *
 *  <b>Description:</b> Represents a point source for a Potential Earthquake,
 *  i.e. the simplist model, with no rupture surface. However all the methods
 *  of a GriddedSurface are implemented so it looks like a surface instead of
 *  a point source<p>
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public class PointSurface extends Location implements GriddedSurfaceAPI {

    /**
     *  Description of the Field
     */
    protected double aveStrike=Double.NaN;
    /**
     *  Description of the Field
     */
    protected double aveDip=Double.NaN;

    protected String name;

    /**
     *  Constructor for the PointSurface object
     */
    public PointSurface() {
        super();
    }


    /**
     *  Constructor for the PointSurface object
     *
     * @param  lat    Description of the Parameter
     * @param  lon    Description of the Parameter
     * @param  depth  Description of the Parameter
     */
    public PointSurface( double lat, double lon, double depth ) {
        super( lat, lon, depth );
    }


    /**
     *  Sets the aveStrike attribute of the PointSurface object
     *
     * @param  aveStrike  The new aveStrike value
     */
    public void setAveStrike( double aveStrike ) throws InvalidRangeException{
        FaultUtils.assertValidStrike( aveStrike );
        this.aveStrike = aveStrike ;
    }



    /**
     *  Sets the aveDip attribute of the PointSurface object
     *
     * @param  aveDip  The new aveDip value
     */
    public void setAveDip( double aveDip ) throws InvalidRangeException{
        FaultUtils.assertValidDip( aveDip );
        this.aveDip =  aveDip ;
    }





    /**
     *  Sets the location attribute of the PointSurface object
     *
     * @param  x                                   The new location value
     * @param  y                                   The new location value
     * @param  location                            The new location value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public void setLocation( int x, int y, Location location ) throws ArrayIndexOutOfBoundsException {
        if ( x == 0 && y == 0 ) {
            this.setLocation( location );
        } else {
            throw new ArrayIndexOutOfBoundsException( "PointSurface can only have one point, i.e. x=0, y=0." );
        }
    }


    /**
     *  Sets the location attribute of the PointSurface object
     *
     * @param  location  The new location value
     */
    public void setLocation( Location location ) {
        this.setLatitude( location.getLatitude() );
        this.setLongitude( location.getLongitude() );
        this.setDepth( location.getDepth() );
    }


    /**
     *  set an object in the 2D grid
     *
     * @param  row                                 Description of the Parameter
     * @param  column                              Description of the Parameter
     * @param  obj                                 Description of the Parameter
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     * @exception  ClassCastException              Description of the Exception
     */
    public void set( int row, int column, Object obj )
             throws
            ArrayIndexOutOfBoundsException,
            ClassCastException {

        if ( row == 0 && column == 0 ) {

            if ( ( obj instanceof Location ) || ( obj instanceof PointSurface ) ) {

                Location location = ( Location ) obj;
            } else {
                throw new ArrayIndexOutOfBoundsException( "Passed in  object must be Location or PointSurface." );
            }
        } else {
            throw new ArrayIndexOutOfBoundsException( "PointSurface can only have one point, i.e. x=0, y=0." );
        }
    }


    /**
     *  Gets the aveStrike attribute of the PointSurface object
     *
     * @return    The aveStrike value
     */
    public double getAveStrike() {
        return aveStrike;
    }


    /**
     *  Gets the aveDip attribute of the PointSurface object
     *
     * @return    The aveDip value
     */
    public double getAveDip() {
        return aveDip;
    }


    /**
     *  Returns a clone of this PointSurface Location fields
     *
     * @return    The location value
     */
    public Location getLocation() {
        return cloneLocation();
    }


    /**
     *  Gets the location attribute of the PointSurface object
     *
     * @param  row     Description of the Parameter
     * @param  column  Description of the Parameter
     * @return         The location value
     */
    public Location getLocation( int row, int column ) {
        return getLocation();
    }


    /**
     *  Gets the locationsIterator attribute of the PointSurface object
     *
     * @return    The locationsIterator value
     */
    public ListIterator getLocationsIterator() {

        Vector v = new Vector();
        v.add( ( Location ) this );
        return v.listIterator();
    }


    /**
     *  iterate over all columns in one row of the surface
     *
     * @param  row                                 Description of the Parameter
     * @return                                     The columnIterator value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public ListIterator getColumnIterator( int row ) throws ArrayIndexOutOfBoundsException {
        return getLocationsIterator();
    }


    /**
     *  iterate over all rows in one column in the surface
     *
     * @param  column                              Description of the Parameter
     * @return                                     The rowIterator value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public ListIterator getRowIterator( int column ) throws ArrayIndexOutOfBoundsException {
        return getLocationsIterator();
    }


    /**
     *  iterate over all points, all rows per column, iterating over all columns
     *
     * @return    The allByColumnsIterator value
     */
    public ListIterator getAllByColumnsIterator() {
        return getLocationsIterator();
    }


    /**
     *  iterate over all points, all columns per row, iterating over all rows
     *
     * @return    The allByRowsIterator value
     */
    public ListIterator getAllByRowsIterator() {
        return getLocationsIterator();
    }


    /**
     *  Gets the numRows attribute of the PointSurface object
     *
     * @return    The numRows value
     */
    public int getNumRows() {
        return 1;
    }


    /**
     *  Gets the numCols attribute of the PointSurface object
     *
     * @return    The numCols value
     */
    public int getNumCols() {
        return 1;
    }


    /**
     *  set's an object in the 2D grid
     *
     * @param  row                                 Description of the Parameter
     * @param  column                              Description of the Parameter
     * @return                                     Description of the Return
     *      Value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public Object get( int row, int column )
             throws ArrayIndexOutOfBoundsException {

        if ( row == 0 && column == 0 ) {
            return getLocation();
        } else {
            throw new ArrayIndexOutOfBoundsException( "PointSurface can only have one point, i.e. x=0, y=0." );
        }
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    protected Location cloneLocation() {

        Location location = new Location(
                this.getLatitude(),
                this.getLongitude(),
                this.getDepth()
                 );

        return location;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public ListIterator listIterator() {
        return getLocationsIterator();
    }


    /**
     *  deletes all data
     */
    public void clear() { }


    /**
     *  check if this grid point has data
     *
     * @param  row     Description of the Parameter
     * @param  column  Description of the Parameter
     * @return         Description of the Return Value
     */
    public boolean exist( int row, int column ) {
        if ( row == 0 || column == 0 ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  returns number of elements in array
     *
     * @return    Description of the Return Value
     */
    public long size() {
        return 1L;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    /**
     *  set's an object in the 2D grid
     *
     * @param  row                                 Description of the Parameter
     * @param  column                              Description of the Parameter
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public void delete( int row, int column )
             throws ArrayIndexOutOfBoundsException {
        if ( row == 0 && column == 0 ) {

            this.latitude = -1;
            this.longitude = -1;
            this.depth = -1;
        } else {
            throw new ArrayIndexOutOfBoundsException( "PointSurface can only have one point, i.e. x=0, y=0." );
        }
    }
}
