package org.opensha.sha.surface;
import java.lang.ArrayIndexOutOfBoundsException;

import java.util.*;

import org.opensha.sha.fault.*;
import org.opensha.data.Location;
import org.opensha.util.FaultUtils;
import org.opensha.exceptions.InvalidRangeException;
import org.opensha.exceptions.LocationException;
import org.opensha.data.*;
import org.opensha.calc.RelativeLocation;


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

    /**
     * Put all the locations of this surface into a location list
     *
     * @return
     */
    public LocationList getLocationList() {
      LocationList locList = new LocationList();
      Iterator it = this.getLocationsIterator();
      locList.addLocation((Location)it.next());
      return locList;
    }



    /** FIX *** Needs to be implemented */
    public double computeAveStrike() { return Double.NaN; }

    /** FIX *** Needs to be implemented */
    public double computeAveDip() { return Double.NaN; }

    /** FIX *** Needs to be implemented */
    public double computeSurfaceArea() { return Double.NaN; }


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

    /**
     * This returns the total length of the surface
     * @return double
     */
    public double getSurfaceLength() {
      return Double.NaN;
    }

    /**
     * This returns the surface width (down dip)
     * @return double
     */
    public double getSurfaceWidth() {
      return Double.NaN;
    }

    /**
     * Returns the gridspacing between the first 2 locations on the surface
     * @return double
     */
    public double getGridSpacing() {

      Location loc1 = this.getLocation(0, 0);
      Location loc2 = this.getLocation(0, 1);
      return RelativeLocation.getHorzDistance(loc1, loc2);
    }



    /**
     * Returns the grid centered location on each grid surface.
     * @return GriddedSurfaceAPI returns a Surface that has one less
     * row and col then the original surface. It averages the 4 corner location
     * on each grid surface to get the grid centered location.
     */
    public GriddedSurfaceAPI getGridCenteredSurface() {

      int numRows = getNumRows() -1;
      int numCols = getNumCols() -1 ;
      //System.out.println("NumRows:"+numRows+" NumCols:"+numCols);
      GriddedSurfaceAPI surface = new GriddedSurface(numRows,
          numCols);
      for (int i = 0; i < numRows; ++i) {
        for (int j = 0; j < numCols; ++j) {
          Location loc;
          Location loc1 = getLocation(i, j);
          Location loc2 = getLocation(i, j + 1);
          Location loc3 = getLocation(i + 1, j);
          Location loc4 = getLocation(i + 1, j + 1);
          double locLat = (loc1.getLatitude() + loc2.getLatitude() +
                           loc3.getLatitude() +
                           loc4.getLatitude()) / 4;
          double locLon = (loc1.getLongitude() + loc2.getLongitude() +
                           loc3.getLongitude() +
                           loc4.getLongitude()) / 4;
          double locDepth = (loc1.getDepth() + loc2.getDepth() + loc3.getDepth() +
                             loc4.getDepth()) / 4;
          loc = new Location(locLat,locLon,locDepth);
          surface.set(i,j,loc);
        }
      }
      return surface;
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
      surfaceMetadata += (float)getGridSpacing() + "\t";
      int numRows = getNumRows();
      int numCols = getNumCols();
      surfaceMetadata += numRows + "\t";
      surfaceMetadata += numCols + "\t";
      surfaceMetadata += (numRows * numCols) + "\n";
      surfaceMetadata += "#Surface locations (Lat Lon Depth) \n";
      ListIterator it = getLocationsIterator();
      while (it.hasNext()) {
        Location loc = (Location) it.next();
        surfaceMetadata += (float)loc.getLatitude()+"\t";
        surfaceMetadata += (float)loc.getLongitude()+"\t";
        surfaceMetadata += (float)loc.getDepth()+"\n";
      }
      return surfaceMetadata;
    }
}
