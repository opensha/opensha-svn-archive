package org.scec.sha.surface;

import java.util.*;
import org.scec.exceptions.LocationException;
import org.scec.sha.fault.*;
import org.scec.data.Location;
import org.scec.data.*;

/**
 *  <b>Title:</b> GriddedSubsetSurface<br>
 *  <b>Description:</b> Implements the same functionality as a GriddedSurface,
 *  but only maintains a small read only window view into a GriddedSurface. The
 *  Gridded Surface actually stores the data points.<br>
 *  <br>
 *  <b>Note:</b> SetLocation, setAveStrike, setAveDip have been disabled, this
 *  class is read-only into the dataset. <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public class GriddedSubsetSurface extends ContainerSubset2D implements GriddedSurfaceAPI {

    /**
     *  Constructor for the GriddedSubsetSurface object
     *
     * @param  numRows                             Description of the Parameter
     * @param  numCols                             Description of the Parameter
     * @param  startRow                            Description of the Parameter
     * @param  startCol                            Description of the Parameter
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public GriddedSubsetSurface( int numRows, int numCols, int startRow, int startCol )
             throws ArrayIndexOutOfBoundsException {
        super( numRows, numCols, startRow, startCol );
    }


    /**
     *  Constructor for the GriddedSubsetSurface object
     *
     * @param  numRows                             Description of the Parameter
     * @param  numCols                             Description of the Parameter
     * @param  startRow                            Description of the Parameter
     * @param  startCol                            Description of the Parameter
     * @param  data                                Description of the Parameter
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
     */
    public GriddedSubsetSurface( int numRows, int numCols, int startRow, int startCol, GriddedSurfaceAPI data )
             throws ArrayIndexOutOfBoundsException {
        super( numRows, numCols, startRow, startCol, ( Container2DAPI ) data );
    }


    /**
     *  add a Location to the grid
     *
     * @param  row       The new location value
     * @param  col       The new location value
     * @param  location  The new location value
     */
    public void setLocation( int row, int col,
            org.scec.data.Location location ) {
        throw new java.lang.UnsupportedOperationException( "This function is not implemented in this subclass" );
    }


    /**
     *  Change the extents of the underlying GriddedSurface that we are
     *  referencing.
     *
     * @param  startRow                            The new limits value
     * @param  startCol                            The new limits value
     * @param  numRows                             The new limits value
     * @param  numCols                             The new limits value
     * @exception  ArrayIndexOutOfBoundsException  Description of the Exception
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
     *  replace the underlying surface with this surface
     *
     * @param  gs  The new newMainSurface value
     */
    public void setNewMainSurface( GriddedSurface gs ) {
        super.setContainer2D( ( Container2D ) gs );
    }


    /**
     *  Sets the aveStrike attribute of the GriddedSubsetSurface object
     *
     * @param  aveStrike  The new aveStrike value
     */
    public void setAveStrike( double aveStrike ) {
        ( ( GriddedSurface ) data ).setAveStrike( aveStrike );
    }





    /**
     *  Sets the aveDip attribute of the GriddedSubsetSurface object
     *
     * @param  aveDip  The new aveDip value
     */
    public void setAveDip( double aveDip ) {
        ( ( GriddedSurface ) data ).setAveDip( aveDip );
    }




    /**
     *  Get a Location to the grid, unless it doesn't exist
     *
     * @param  row                    Description of the Parameter
     * @param  col                    Description of the Parameter
     * @return                        The location value
     * @exception  LocationException  Description of the Exception
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
     *  Gets the locationsIterator attribute of the GriddedSubsetSurface object
     *
     * @return    The locationsIterator value
     */
    public ListIterator getLocationsIterator() {
        return this.listIterator();
    }


    /**
     *  returns the number of rows in the main GriddedSurface
     *
     * @return    The mainNumRows value
     */
    public int getMainNumRows() {
        return data.getNumRows();
    }


    /**
     *  returns the number of colums in the main GriddedSurface
     *
     * @return    The mainNumCols value
     */
    public int getMainNumCols() {
        return data.getNumCols();
    }


    /**
     *  Gets the aveStrike attribute of the GriddedSubsetSurface object. <P>
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
     *  Gets the aveDip attribute of the GriddedSubsetSurface object. <P>
     *
     *  SWR: Note - should we be returning the main GriddedSurface ave. dip, or
     *  the ave. dip for the subsurface, which may be different from the main
     *  surface.
     *
     * @return    The aveDip value
     */
    public double getAveDip() {
        return ( ( GriddedSurface ) data ).getAveDip();
    }


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


}
