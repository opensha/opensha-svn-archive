package org.scec.sha.surface;
import java.lang.ArrayIndexOutOfBoundsException;

import java.util.*;

import org.scec.sha.fault.*;
import org.scec.data.Location;
import org.scec.util.FaultUtils;
import org.scec.exceptions.InvalidRangeException;
import org.scec.exceptions.LocationException;
import org.scec.data.*;


// Fix - Needs more comments

/**
 *  <b>Title:</b> GriddedSurface<p>
 *
 *  <b>Description:</b> Implements a GriddedSurfaceAPI and Container2DAPI. The
 *  main difference is that this class only accepts Location Objects in it's
 *  grid<p>
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public class GriddedSurface
         extends Container2D
         implements GriddedSurfaceAPI {


    protected final static String C = "GriddedSurface";
    protected final static boolean D = false;

    /**
     *  Description of the Field
     */
    protected double aveStrike=Double.NaN;

    /**
     *  Description of the Field
     */
    protected double aveDip=Double.NaN;

    /**
     *  KM**2
     */
    protected double surfaceArea=Double.NaN;


    /**
     *  Constructor for the GriddedSurface object
     */
    public GriddedSurface() {
        super();
    }


    /**
     *  Constructor for the GriddedSurface object
     *
     * @param  numRows  Description of the Parameter
     * @param  numCols  Description of the Parameter
     */
    public GriddedSurface( int numRows, int numCols ) {
        super( numRows, numCols );
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
    public void set( int row, int column, Object obj ) throws ArrayIndexOutOfBoundsException, ClassCastException {

        String S = C + ": set(): ";
        if ( obj instanceof Location ) {
            setLocation( row, column, ( Location ) obj );
        } else {
            throw new ClassCastException( S + "Object must be a Location" );
        }
    }


    /**
     *  add a Location to the grid
     *
     * @param  row       The new location value
     * @param  column    The new location value
     * @param  location  The new location value
     */
    public void setLocation( int row, int column, Location location ) {
        super.set( row, column, location );
    }


    /**
     *  Sets the aveStrike attribute of the GriddedSurface object
     *
     * @param  aveStrike  The new aveStrike value
     */
    public void setAveStrike( double aveStrike ) throws InvalidRangeException{
        FaultUtils.assertValidStrike( aveStrike );
        this.aveStrike =  aveStrike ;
    }





    /**
     *  Sets the aveDip attribute of the GriddedSurface object
     *
     * @param  aveDip  The new aveDip value
     */
    public void setAveDip( double aveDip ) throws InvalidRangeException{
        FaultUtils.assertValidDip( aveDip );
        this.aveDip = aveDip ;
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
     *  Gets the aveStrike attribute of the GriddedSurface object
     *
     * @return    The aveStrike value
     */
    public double getAveStrike() {
        return aveStrike;
    }


    /**
     *  Gets the aveDip attribute of the GriddedSurface object
     *
     * @return    The aveDip value
     */
    public double getAveDip() {
        return aveDip;
    }


    /**
     *  KM**2
     *
     * @return    The surfaceArea value
     */
    public double getSurfaceArea() {
        return surfaceArea;
    }


    /**
     *  Gets the locationsIterator attribute of the GriddedSurface object
     *
     * @return    The locationsIterator value
     */
    public ListIterator getLocationsIterator() {
        return super.listIterator();
    }


    /**
     *  FIX *** Needs to be implemented
     *
     * @return    Description of the Return Value
     */
    public double computeAveStrike() {
        return 0;
    }


    /**
     *  FIX *** Needs to be implemented
     *
     * @return    Description of the Return Value
     */
    public double computeAveDip() {
        return 0;
    }


    /**
     *  FIX *** Needs to be implemented
     *
     * @return    Description of the Return Value
     */
    public double computeSurfaceArea() {
        return 0;
    }

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
