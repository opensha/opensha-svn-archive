package org.scec.gui.plot.jfreechart;

import java.text.*;
import java.util.*;

import com.jrefinery.data.*;
import org.scec.data.*;
import org.scec.sha.surface.*;
import org.scec.util.*;


/**
 * <p>Title: GeoLocations2DList</p>
 * <p>Description: Proxy class for GriddedSurfaceAPI to map to the
 * JFreeChart XYDataSet API so a GriddedSurface can be passed into a chart
 * for plotting</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Steven W. Rock
 * @version 1.0
 */

public class GriddedSurfaceXYDataSet implements XYDataset, NamedObjectAPI {


    protected final static String C = "GriddedSurfaceXYDataSet";
    protected final static boolean D = false;

    /**
     *  Internal list of 2D Functions - indexed by name
     */
    protected GriddedSurfaceAPI surface = null;

    /**
     *  list of listeners for data changes
     */
    protected Vector listeners = new Vector();

    /**
     *  Name of this Function2DList. Used for display purposes and identifying
     *  unique Lists.
     */
    protected String name;

    public boolean checkSurface(){
        if( surface != null ) return true;
        else return false;
    }

    /**
     *  no arg constructor
     */
    public GriddedSurfaceXYDataSet(GriddedSurfaceAPI surface) { this.surface = surface; }


    /**
     *  Sets the name attribute of the Function2DList object
     * @param  newName  The new name value
     */
    public void setName( String newName ) { name = newName; }

    /**
     *  Gets the name attribute of the Function2DList object
     * @param  newName  The new name value
     */
    public String getName(  ) { return name; }




    /**
     *  Returns the number of series in the dataset.
     *
     * @return    The number of series in the dataset.
     */
    public int getSeriesCount() {

        if ( checkSurface() ) return surface.getNumRows();
        else return 0;
    }

    DecimalFormat format = new DecimalFormat("#,###.##");
    /**
     *  Returns the name of a series.
     *
     * @param  series  The series (zero-based index).
     * @return         The seriesName value
     */
    public String getSeriesName( int series ){

        if( surface == null ) return name + ' ' + series;

        Location loc = surface.getLocation( series, 0 );
        if( loc != null ){
            double depth = loc.getDepth();
            String depthStr = format.format(depth);
            return "Depth = " + depthStr;
        }
        else return name + ' ' + series;
    }

    /**
     *  Returns the number of items in a series.
     *
     * @param  series  The series (zero-based index).
     * @return         The number of items within a series.
     */
    public int getItemCount( int series ) {
        if ( checkSurface() ) return surface.getNumCols();
        else return 0;
    }


    /**
     *  Returns the x-value for an item within a series. <P>
     *
     *  The implementation is responsible for ensuring that the x-values are
     *  presented in ascending order.
     *
     * @param  series  The series (zero-based index).
     * @param  item    The item (zero-based index).
     * @return         The x-value for an item within a series.
     */
    public Number getXValue( int series, int item ) {

        if ( checkSurface() && series < surface.getNumRows() ) {

            Location loc = surface.getLocation( series, item );
            Double lon = new Double( loc.getLongitude() );
            return ( Number ) lon;

        }
        else return null;

    }


    /**
     *  Returns the y-value for an item within a series.
     *
     * @param  series  The series (zero-based index).
     * @param  item    The item (zero-based index).
     * @return         The y-value for an item within a series.
     */
    public Number getYValue( int series, int item ) {

        if ( checkSurface() && series < surface.getNumRows() ) {

            Location loc = surface.getLocation( series, item );
            Double lat = new Double( loc.getLatitude() );
            return ( Number ) lat;

        }
        else return null;
    }


    /**
     *  removes all DiscretizedFunction2Ds from the list, making it empty, ready
     *  for new DiscretizedFunction2Ds
     */
    public void clear() { surface = null; }




    /**
     *  Registers an object for notification of changes to the dataset.
     *
     * @param  listener  The object to register.
     */
    public void addChangeListener( DatasetChangeListener listener ) {
        if ( !listeners.contains( listener ) ) {
            listeners.add( listener );
        }
    }


    /**
     *  Deregisters an object for notification of changes to the dataset.
     *
     * @param  listener  The object to deregister.
     */
    public void removeChangeListener( DatasetChangeListener listener ) {
        if ( listeners.contains( listener ) ) {
            listeners.remove( listener );
        }
    }





}
