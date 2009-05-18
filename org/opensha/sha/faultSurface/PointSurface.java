package org.opensha.sha.faultSurface;

import java.util.ListIterator;
import java.util.ArrayList;

import org.opensha.util.FaultUtils;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.exceptions.InvalidRangeException;


/**
 * <b>Title:</b> PointSurface<p>
 *
 * <b>Description:</b> This is a special case of the GriddedSurface
 * that defaults to one point, i.e. there is only one Location, and the
 * grid size is only [1,1], one row and one column. <p>
 *
 * This will be used by point source models of Potential Earthquake.
 * This is the simplist model, with no rupture surface. <p>
 *
 * Note: all the methods of a GriddedSurface are implemented so it behaves
 * just like a surface instead of a point source. Thus this class can
 * be used anywhere a GriddedSurface can. It plugs right into the framework.<p>
 *
 * Since there is only one Location this class extends Location instead of the
 * base implementing GriddedSurface class. There is no need to set up an array,
 * etc. All the list accessor functions can be bypassed and simply return this
 * location everytime. Improves performace over the base class. <p>
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */
public class PointSurface extends Location implements EvenlyGriddedSurfaceAPI {


    /**
     * The average strike of this surface on the Earth. Even though this is a
     * point source, an average strike can be assigned to it to assist with
     * particular scientific caculations. Initially set to NaN.
     */
    protected double aveStrike=Double.NaN;

    /**
     * The average dip of this surface into the Earth. Even though this is a
     * point source, an average dip can be assigned to it to assist with
     * particular scientific caculations. Initially set to NaN.
     */
    protected double aveDip=Double.NaN;

    /** The name of this point source.  */
    protected String name;

    /** Constructor for the PointSurface object - just calls super(). */
    public PointSurface() { super(); }


    /**
     *  Constructor for the PointSurface object. Sets all the fields
     *  for a Location object. Mirrors the Location constructor.
     *
     * @param  lat    latitude for the Location of this point source.
     * @param  lon    longitude for the Location of this point source.
     * @param  depth  depth below the earth for the Location of this point source.
     */
    public PointSurface( double lat, double lon, double depth ) {
        super( lat, lon, depth );
    }

    /**
     *  Constructor for the PointSurface object. Sets all the fields
     *  for a Location object.
     *
     * @param  loc    the Location object for this point source.
     */
    public PointSurface( Location loc ) {
        super( loc.getLatitude(), loc.getLongitude(), loc.getDepth() );
    }


    /**
     * Sets the average strike of this surface on the Earth. An InvalidRangeException
     * is thrown if the ave strike is not a valid value, i.e. must be > 0, etc.
     * Even though this is a point source, an average strike can be assigned to
     * it to assist with particular scientific caculations.
     */
    public void setAveStrike( double aveStrike ) throws InvalidRangeException{
        FaultUtils.assertValidStrike( aveStrike );
        this.aveStrike = aveStrike ;
    }


    /**
     * Sets the average dip of this surface into the Earth. An InvalidRangeException
     * is thrown if the ave strike is not a valid value, i.e. must be > 0, etc.
     * Even though this is a point source, an average dip can be assigned to
     * it to assist with particular scientific caculations.
     */
    public void setAveDip( double aveDip ) throws InvalidRangeException{
        FaultUtils.assertValidDip( aveDip );
        this.aveDip =  aveDip ;
    }




    /**
     *  Add a Location to the grid - does the same thing as set except that it
     *  ensures the object is a Location object. Note that x and y must always
     *  be 0,0.
     *
     * @param  row                                 The row to set this Location at.
     * @param  column                              The column to set this Location at.
     * @param  location                            The new location value.
     * @exception  ArrayIndexOutOfBoundsException  Thrown if the row or column lies beyond the grid space indexes.
     */
    public void setLocation( int x, int y, Location location ) throws ArrayIndexOutOfBoundsException {
        if ( x == 0 && y == 0 ) {
            this.setLocation( location );
        } else {
            throw new ArrayIndexOutOfBoundsException( "PointSurface can only have one point, i.e. x=0, y=0." );
        }
    }


    /** Since this is a point source, the single Location can be set without indexes. Does a clone copy. */
    public void setLocation( Location location ) {
        this.setLatitude( location.getLatitude() );
        this.setLongitude( location.getLongitude() );
        this.setDepth( location.getDepth() );
    }


    /**
     *  Set an object in the 2D grid. Ensures the object passed in is a Location.
     *  Note that x and y must always be 0,0.
     *
     * @param  row                                 The row to set the Location. Must be 0.
     * @param  column                              The row to set the Location. Must be 0.
     * @param  obj                                 Must be a Location object
     * @exception  ArrayIndexOutOfBoundsException  Thrown if the row or column lies beyond the grid space indexes.
     * @exception  ClassCastException              Thrown if the passed in Obejct is not a Location.
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


    /** Returns the average strike of this surface on the Earth.  */
    public double getAveStrike() { return aveStrike; }

    /** Returns the average dip of this surface into the Earth.  */
    public double getAveDip() { return aveDip; }

    /** Returns a clone copy of the Location of this point source  */
    public Location getLocation(){ return cloneLocation(); }


    /**
     *  Gets the location attribute of the PointSurface object. Does the same
     *  thing as get except that it casts the returned object to a Location.
     *  Note that x and y must always be 0,0.
     *
     * @param  row     The row to get this Location from. Must be 0.
     * @param  column  The column to get this Location from. Must be 0.
     * @return         The location stored at the row and column.
     * @exception  LocationException  Thown if the object being retrieved cannot be cast to a Location.
     */
    public Location getLocation( int row, int column ) {
        return getLocation();
    }


    /** Does same thing as listIterator() in super Interface. Will contain only one Location */
    public ListIterator getLocationsIterator() {

        ArrayList v = new ArrayList();
        v.add( ( Location ) this );
        return v.listIterator();
    }

    /**
     * Put all the locations of this surface into a location list
     *
     * @return
     */
    public LocationList getLocationList() {
      LocationList locList = new LocationList();
      locList.addLocation(this);
      return locList;
    }



     /** return getLocationsIterator() */
    public ListIterator getColumnIterator( int row ) throws ArrayIndexOutOfBoundsException {
        return getLocationsIterator();
    }

    /** return getLocationsIterator() */
    public ListIterator getRowIterator( int column ) throws ArrayIndexOutOfBoundsException {
        return getLocationsIterator();
    }

    /** return getLocationsIterator() */
    public ListIterator getAllByColumnsIterator() { return getLocationsIterator(); }

    /** return getLocationsIterator() */
    public ListIterator getAllByRowsIterator() { return getLocationsIterator();}


    /** Gets the numRows of the PointSurface. Always returns 1. */
    public int getNumRows() { return 1; }


    /** Gets the numRows of the PointSurface. Always returns 1. */
    public int getNumCols() { return 1; }


    /**
     *  Get's the Location of this PointSource.
     *
     * @param  row              The row to get this Location from. Must be 0.
     * @param  column           The column to get this Location from. Must be 0.
     * @return Value            The Location.
     *
     * @exception  ArrayIndexOutOfBoundsException  Thrown if row or column not equal to 0.
     */
    public Object get( int row, int column )
             throws ArrayIndexOutOfBoundsException {

        if ( row == 0 && column == 0 ) {
            return getLocation();
        } else {
            throw new ArrayIndexOutOfBoundsException( "PointSurface can only have one point, i.e. x=0, y=0." );
        }
    }


    /** Make a clone ( copies all fields ) of the Location. */
    protected Location cloneLocation() {

        Location location = new Location(
                this.getLatitude(),
                this.getLongitude(),
                this.getDepth()
                 );

        return location;
    }


    /** return getLocationsIterator() */
    public ListIterator listIterator() { return getLocationsIterator();}


    /** FIX *** Does nothing - should clear the Location values  */
    public void clear() { }


    /**
     *  Check if this grid point has data. Will return false for all
     *  rows and columns != 0.
     *
     * @param  row     The row to get this Location from. Must be 0.
     * @param  column  The column to get this Location from. Must be 0.
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
     * Returns the grid centered location on each grid surface.
     * @return GriddedSurfaceAPI returns a Surface that has one less
     * row and col then the original surface. It averages the 4 corner location
     * on each grid surface to get the grid centered location.
     */
    public EvenlyGriddedSurfaceAPI getGridCenteredSurface() {
      return this;
    }


    /** returns number of elements in array. Returns 1.  */
    public long size() {
        return 1L;
    }

    /** Sets the name of this PointSource. Uesful for lookup in a list */
    public void setName(String name) { this.name = name; }
    /** Gets the name of this PointSource. Uesful for lookup in a list */
    public String getName() { return name; }

    /**
     *  this sets the lat, lon, and depth to be NaN
     *
     * @param  row            The row to get this Location from. Must be 0.
     * @param  column         The column to get this Location from. Must be 0.
     * @exception  ArrayIndexOutOfBoundsException  Thrown if row or column is not zero.
     */
    public void delete( int row, int column )
             throws ArrayIndexOutOfBoundsException {
        if ( row == 0 && column == 0 ) {

            this.latitude = Double.NaN;
            this.longitude = Double.NaN;
            this.depth = Double.NaN;
        } else {
            throw new ArrayIndexOutOfBoundsException( "PointSurface can only have one point, i.e. x=0, y=0." );
        }
    }

    /**
     * This returns the total length of the surface
     * @return double
     */
    public double getSurfaceLength() {

      return 0;
    }


    /**
     * This returns the surface width (down dip)
     * @return double
     */
    public double getSurfaceWidth() {
      return 0;
    }

    /**
     * Returns the Surface Metadata with the following info:
     * <ul>
     * <li>AveDip
     * <li>Surface length
     * <li>Surface DownDipWidth
     * <li>GridSpacing
     * <li>NumRows
     * <li>NumCols
     * <li>Number of locations on surface
     * <p>Each of these elements are represented in Single line with tab("\t") delimitation.
     * <br>Then follows the location of each point on the surface with the comment String
     * defining how locations are represented.</p>
     * <li>#Surface locations (Lat Lon Depth)
     * <p>Then until surface locations are done each line is the point location on the surface.
     *
     * </ul>
     * @return String
     */
    public String getSurfaceMetadata() {
      String surfaceMetadata;
      surfaceMetadata = (float)aveDip + "\t";
      surfaceMetadata += (float)getSurfaceLength() + "\t";
      surfaceMetadata += (float)getSurfaceWidth() + "\t";
      surfaceMetadata += (float)Double.NaN + "\t";
      surfaceMetadata += "1" + "\t";
      surfaceMetadata += "1" + "\t";
      surfaceMetadata += "1" + "\n";
      surfaceMetadata += "#Surface locations (Lat Lon Depth) \n";
      surfaceMetadata += (float) getLatitude() + "\t";
      surfaceMetadata += (float) getLongitude() + "\t";
      surfaceMetadata += (float) getDepth();

      return surfaceMetadata;
    }
    

    /** get a list of locations that constitutes the perimeter (forst row, last col, last row, and first col) */
    public LocationList getSurfacePerimeterLocsList() {
  	  LocationList locList = new LocationList();
  	  locList.addLocation(this.getLocation());
 	  return locList;
    }




    /**
     * returns the grid spacing
     *
     * @return
     */
    public double getGridSpacing() {
      return Double.NaN;
    }

}
