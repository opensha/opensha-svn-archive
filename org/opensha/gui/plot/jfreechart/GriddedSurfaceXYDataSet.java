package org.opensha.gui.plot.jfreechart;

import java.text.*;
import java.util.*;

import org.jfree.data.*;
import org.opensha.data.*;
import org.opensha.sha.surface.*;
import org.opensha.util.*;

/**
 * <b>Title:</b> GriddedSurfaceXYDataSet<p>
 *
 * <b>Description:</b> Proxy class for GriddedSurfaceAPI to map to the
 * JFreeChart XYDataSet API so a GriddedSurface can be passed into a chart
 * for plotting.<p>
 *
 * This class contains a pointer to a GriddedSurfaceAPI. It also implements
 * an XYDataset which is JFreChart's interface that all datasets must implement
 * so they can be passed to the graphing routines. This class transforms the
 * GriddedSurfaceAPI data into the format as required by this interface.<p>
 *
 * Please consult the JFreeChart documentation for further information
 * on XYDataSets. <p>
 *
 * Note: The FaultTraceXYDataSet and DiscretizedFunctionXYDatasets are
 * handled in exactly the same manner as for GriddedSurfaceAPI.<p>
 *
 * @see FaultTraceXYDataSet
 * @see DiscretizedFunctionXYDataSet
 * @author Steven W. Rock
 * @version 1.0
 */

public class GriddedSurfaceXYDataSet implements XYDataset, NamedObjectAPI {

    /** Class name used for debug statements */
    protected final static String C = "GriddedSurfaceXYDataSet";
    /** If true prints out debug statements */
    protected final static boolean D = false;

    /**
     * Used to format the depth of a location to human readable form .
     * This format is used in getSeriesName(). The depth becomes part
     * of each row in the griddedsurface, i.e. each series name.
     */
    DecimalFormat format = new DecimalFormat("#,###.##");


    /**
     *   GriddedSurfaceAPI pointer. This is the real data for this
     *   class. Recall a gridded surface is a matrix of Location points.
     *   As far as XYDataset is concerned the data is a collection of
     *   data series, one series maps to one row in the GriddedSurfaceAPI.
     */
    protected EvenlyGriddedSurfaceAPI surface = null;

    /** XYDatasetAPI - list of listeners for data changes */
    protected ArrayList listeners = new ArrayList();

    /**
     *  Name of this Function2DList. Used for display purposes and identifying
     *  unique Lists.
     */
    protected String name;

    /** The group that the dataset belongs to. */
    private DatasetGroup group;

    /** Returns true is the GriddedSurfaceAPI reference is not null. */
    public boolean checkSurface(){
        if( surface != null ) return true;
        else return false;
    }

    /** Constructor that sets the GriddedSurfaceAPI dataset. */
    public GriddedSurfaceXYDataSet(EvenlyGriddedSurfaceAPI surface) {
        this.group = new DatasetGroup();
        this.surface = surface;
    }


    /** Sets the name of this XYDataset. */
    public void setName( String newName ) { name = newName; }
    /** Sets the name of this XYDataset. */
    public String getName(  ) { return name; }


    /**
     * XYDatasetAPI -  Returns the number of series in the dataset.
     * For a griddedsurface this simply returns the number of rows.
     */
    public int getSeriesCount() {
        if ( checkSurface() ) return surface.getNumRows();
        else return 0;
    }

    /**
     * XYDatasetAPI -  Returns the name of a series.
     * For a Gridded Surface each row represents another
     * series, so the depth of that row is used as part of the
     * series name. Uses DecimalFormat to translate the depth
     * into human readable form.
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
     * XYDatasetAPI -  Returns the number of items in a series.
     * For a GriddedSurface, this simply returns the number
     * of columns for this surface, a constant per row.
     *
     * @param  series  The series (zero-based index).
     * @return         The number of items within a series.
     */
    public int getItemCount( int series ) {
        if ( checkSurface() ) return surface.getNumCols();
        else return 0;
    }


    /**
     * XYDatasetAPI -  Returns the x-value for an item within a series. <P>
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
     * XYDatasetAPI -  Returns the y-value for an item within a series.
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


    /** XYDatasetAPI -  Set's the GriddedSurfaceAPI reference to null. */
    public void clear() { surface = null; }

    /** XYDatasetAPI -  Registers an object for notification of changes to the dataset. */
    public void addChangeListener( DatasetChangeListener listener ) {
        if ( !listeners.contains( listener ) ) {
            listeners.add( listener );
        }
    }

    /** XYDatasetAPI -  Deregisters an object for notification of changes to the dataset. */
    public void removeChangeListener( DatasetChangeListener listener ) {
        if ( listeners.contains( listener ) ) {
            listeners.remove( listener );
        }
    }

    /**
     * Returns the dataset group for the dataset.
     *
     * @return the dataset group.
     */
    public DatasetGroup getGroup() {
      return this.group;
    }

    /**
     * Sets the dataset group for the dataset.
     *
     * @param group  the dataset group.
     */
    public void setGroup(DatasetGroup group) {
      this.group = group;
    }


}
