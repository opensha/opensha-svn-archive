package org.opensha.sha.surface;

import java.util.*;
import org.opensha.exceptions.LocationException;
import org.opensha.sha.fault.*;
import org.opensha.data.Location;
import org.opensha.data.*;

/**
 * <b>Title:</b> GriddedSubsetSurface<p>
 *
 * <b>Description:</b> Implements the same functionality as a GriddedSurface,
 * but only maintains a small read only window view into a GriddedSurface. The
 * Gridded Surface actually stores the data points.<p>
 *
 * <b>Note:</b> This class is purely a convinience class that translates indexes so the
 * user can deal with a smaller window than the full GriddedSurface. Think of
 * this as a "ZOOM" function into a GriddedSurface.<p>
 *
 * <b>Note:</b> SetLocation, setAveStrike, setAveDip have been disabled, this
 * class is read-only into the dataset. <p>
 *
 * @see Window2D
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */
public class GriddedSubsetSurface extends ContainerSubset2D implements GriddedSurfaceAPI {

    /**
     *  Constructor for the GriddedSubsetSurface object
     *
     * @param  numRows                             Specifies the length of the window.
     * @param  numCols                             Specifies the height of the window
     * @param  startRow                            Start row into the main GriddedSurface.
     * @param  startCol                            Start column into the main GriddedSurface.
     * @exception  ArrayIndexOutOfBoundsException  Thrown if window indexes exceed the
     * main GriddedSurface indexes.
     */
    public GriddedSubsetSurface( int numRows, int numCols, int startRow, int startCol )
             throws ArrayIndexOutOfBoundsException {
        super( numRows, numCols, startRow, startCol );
    }


    /**
     *  Constructor for the GriddedSubsetSurface object
     *
     * @param  numRows                             Specifies the length of the window.
     * @param  numCols                             Specifies the height of the window
     * @param  startRow                            Start row into the main GriddedSurface.
     * @param  startCol                            Start column into the main GriddedSurface.
     * @param  data                                The main GriddedSurface this is a window into
     * @exception  ArrayIndexOutOfBoundsException  Thrown if window indexes exceed the
     * main GriddedSurface indexes.
     */
    public GriddedSubsetSurface( int numRows, int numCols, int startRow, int startCol, GriddedSurfaceAPI data )
             throws ArrayIndexOutOfBoundsException {
        super( numRows, numCols, startRow, startCol, ( Container2DAPI ) data );
    }


    /** Add a Location to the grid. This method throws UnsupportedOperationException as it is disabled. */
    public void setLocation( int row, int col,
            org.opensha.data.Location location ) {
        throw new java.lang.UnsupportedOperationException( "This function is not implemented in this subclass" );
    }


    /**
     * Resizes the window view into the main GriddedSurface data. <p>
     *
     * Note: This function uses some advanced features of a transactional nature.
     * A transaction is basically a series of steps that must follow each other.
     * If any of the steps fails, the previous steps must be rolled back. I
     * perform this by calling initTransaction(), rollback() if error, else commit().
     * It sounds more complicated than it it. This approach basically resets
     * the window size to the starting size if any of the new indexes fail. Each
     * is checked one at a time.
     *
     * @param  startRow                            The Start row into the main GriddedSurface.
     * @param  startCol                            Start column into the main GriddedSurface.
     * @param  numRows                             The new length of the window.
     * @param  numCols                             The new height of the window
     * @exception  ArrayIndexOutOfBoundsException  Thrown if window indexes exceed the
     * main GriddedSurface indexes.
     */
    public void setLimits(
            int startRow,
            int startCol,
            int numRows,
            int numCols )
             throws ArrayIndexOutOfBoundsException {

        String S = C + ": setLimits():";
        initTransaction();


        window.setStartRow(startRow);
        window.setStartCol(startCol);
        window.setNumRows(numRows);
        window.setNumCols(numCols);

        try {
            window.checkLowerBounds( S );
            window.calcUpperBounds();
            if ( data != null ) {
                window.checkUpperBounds( S );
            }
        } catch ( ArrayIndexOutOfBoundsException e ) {
            rollback();
            throw e;
        }
        commit();
    }


     /**
     * Replaces the real data of the GriddedSurface with
     * a new surface. <p>
     *
     * Note: This could be a dangerous thing to do if the
     * indexes are invalid for the new surface. I am not
     * sure if this is being checked. Please consult the
     * Container2D documentation for further information.
     *
     * @param  gs  The new newMainSurface value
     */
    public void setNewMainSurface( GriddedSurface gs ) {
        super.setContainer2D( ( Container2D ) gs );
    }


    /** Sets the aveStrike attribute of the GriddedSubsetSurface object. */
    public void setAveStrike( double aveStrike ) {
        ( ( GriddedSurface ) data ).setAveStrike( aveStrike );
    }





    /** Sets the aveDip attribute of the GriddedSubsetSurface object */
    public void setAveDip( double aveDip ) {
        ( ( GriddedSurface ) data ).setAveDip( aveDip );
    }




    /**
     *  Get a Location to the grid, unless it doesn't exist. Note
     *  these points are translated to the real grid. FOr example
     *  0 row and column returns from the GriddedSurface the Location
     *  at (startRow, startColumn). <p>
     *
     * Recall that the grid point may be a valid point but there
     * is no Location object stored at that grid point.
     *
     * @param  row                    The row index from which to obtain the Location, in subset coordinates.
     * @param  col                    The column index from which to obtain the Location, in subset coordinates.
     * @return                        The location value if found.
     * @exception  LocationException  Thrown if a Location doesn't exist
     * at the specified grid point.
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


    /**
     *  Gets the locationsIterator attribute of the GriddedSubsetSurface object.
     *  Will return a subset iterator, not iterating over the whole GriddedSurface,
     *  but just over the subset window points.
     *
     * @return    A listIterator of Location obejcts.
     */
    public ListIterator getLocationsIterator() {
        return this.listIterator();
    }

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




    /** Proxy method that returns the number of rows in the main GriddedSurface. */
    public int getMainNumRows() {
        return data.getNumRows();
    }


    /** Proxy method that returns the number of colums in the main GriddedSurface. */
    public int getMainNumCols() {
        return data.getNumCols();
    }


    /**
     *  Proxy method that returns the aveStrike of the main GriddedSurface. <P>
     *
     *  SWR: Note - should we be returning the main GriddedSurface ave strike,
     *  or the ave. strike for the subsurface, which may be different from the
     *  main surface.
     *
     * @return    The aveStrike value
     */
    public double getAveStrike() {
        return ( ( GriddedSurface ) data ).getAveStrike();
    }


    /**
     *  Proxy method that returns the aveDip of the main GriddedSurface. <P>
     *
     *  SWR: Note - should we be returning the main GriddedSurface ave. dip, or
     *  the ave. dip for the subsurface, which may be different from the main
     *  surface.  This is especially important now that we have a
     *  SimpleListricGriddedFaultFactory (Ned's comment).
     *
     * @return    The aveDip value
     */
    public double getAveDip() {
        return ( ( GriddedSurface ) data ).getAveDip();
    }

    /** Debug string to represent a tab. Used by toString().  */
    final static char TAB = '\t';

    /** Prints out each location and fault information for debugging */
    public String toString(){

        StringBuffer b = new StringBuffer();
        b.append( C + '\n');
        if ( data != null ) b.append( "Ave. Strike = " + ( ( GriddedSurface ) data ).getAveStrike() + '\n' );
        if ( data != null ) b.append( "Ave. Dip = " + ( ( GriddedSurface ) data ).getAveDip() + '\n' );
        if ( data != null ) b.append( "Surface Area = " +  ( ( GriddedSurface ) data ).getSurfaceArea() + '\n' );

        b.append( "Row" + TAB + "Col" + TAB + "Latitude" + TAB + "Longitude" + TAB + "Depth");

        String superStr = super.toString();
        //int index = superStr.indexOf('\n');
        //if( index > 0 ) superStr = superStr.substring(index + 1);
        b.append( '\n' + superStr );

        return b.toString();
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
     * This returns the total length of the surface
     * @return double
     */
    public double getSurfaceLength() {
      if(data instanceof EvenlyGriddedSurface)
        return ((EvenlyGriddedSurface)data).getGridSpacing() * (getNumCols()-1);
      return Double.NaN;
    }

    /**
     * This returns the surface width (down dip)
     * @return double
     */
    public double getSurfaceWidth() {

     if(data instanceof EvenlyGriddedSurface)
       return ((EvenlyGriddedSurface)data).getGridSpacing() * (getNumRows()-1);
     return Double.NaN;

    }

}
