package org.opensha.gui.plot.jfreechart;

import java.text.*;
import java.util.*;

import org.jfree.data.*;
import org.opensha.data.*;
import org.opensha.sha.fault.*;
import org.opensha.util.*;

/**
 * <b>Title:</b> FaultTraceXYDataSet<p>
 *
 * <b>Description:</b> Proxy for the FaultTrace to conform to the JFreeChart API
 * for XYDataSet, we can now pass a FaultTrace wrapped in this class into a plot<p>
 *
 * This class contains a pointer to a FaultTrace. It also implements
 * an XYDataset which is JFreChart's interface that all datasets must implement
 * so they can be passed to the graphing routines. This class transforms the
 * FaultTrace data into the format as required by this interface.<p>
 *
 * Please consult the JFreeChart documentation for further information
 * on XYDataSets. <p>
 *
 * Note: The GriddedSurfaceXYDataSet and DiscretizedFunctionXYDatasets are
 * handled in exactly the same manner as for FaultTraces.<p>
 *
 * @see GriddedSurfaceXYDataSet
 * @see DiscretizedFunctionXYDataSet
 * @author Steven W. ROck
 * @version 1.0
 */

public class FaultTraceXYDataSet extends ArrayList implements XYDataset, NamedObjectAPI {

    /** Class name used for debug statements */
    private final static String C = "FaultTraceXYDataSet";
    /** If true prints out debug statements */
    private final static boolean D = false;


    /** The real data of this class - Internal trace of Locations */
    //private ArrayList traces = new ArrayList();

    /** list of listeners for data changes */
    private ArrayList listeners = new ArrayList();

    /**
     *  Name of this Function2DList. Used for display purposes and identifying
     *  unique Lists.
     */
    private String name;

    /** The group that the dataset belongs to. */
    private DatasetGroup group;

    DecimalFormat format = new DecimalFormat("#,###.##");



    /** Constructor that set's the FaultTrace.  */
    public FaultTraceXYDataSet(FaultTrace trace) {
        this.add(trace);
        this.group = new DatasetGroup();
    }


    /** Sets the name of this FaultTraceXYDataset. */
    public void setName( String newName ) { name = newName; }

    /** Gets the name of this FaultTraceXYDataset. */
    public String getName(  ) { return name; }


    /**
     *  XYDataSetAPI - Returns the number of series in the dataset.
     *  For a fault trace the answer is always 1, unless the
     *  fault trace is null. It that case 0 is returned.
     */
    public int getSeriesCount() {
       return this.size();
    }


    /**
     *  XYDataSetAPI - Returns the name of a series. FaultTraces
     *  always only have one series. Useful for displays.
     */
    public String getSeriesName( int series ){
        return ((FaultTrace)get(series)).getName();
    }

    /**
     *  XYDataSetAPI - Returns the number of items in a series.
     *  FaultTraces only have one series so this returns the
     *  number of Locations in a FaultTrace.
     */
    public int getItemCount( int series ) {
        return ((FaultTrace)get(series)).getNumLocations();
    }


    /**
     *  XYDataSetAPI - Returns the x-value for an item within a series. <P>
     *
     *  The implementation is responsible for ensuring that the x-values are
     *  presented in ascending order.
     *
     * @param  series  The series (zero-based index).
     * @param  item    The item (zero-based index).
     * @return         The x-value for an item within a series.
     */
    public Number getXValue( int series, int item ) {
       Location loc= ((FaultTrace)get(series)).getLocationAt(item);
       Double lon = new Double( loc.getLongitude() );
       return ( Number ) lon;
    }


    /**
     * XYDataSetAPI -  Returns the y-value for an item within a series.
     *
     * @param  series  The series (zero-based index).
     * @param  item    The item (zero-based index).
     * @return         The y-value for an item within a series.
     */
    public Number getYValue( int series, int item ) {
      Location loc= ((FaultTrace)get(series)).getLocationAt(item);
      Double lat = new Double( loc.getLatitude() );
      return ( Number ) lat;

    }

    /** XYDataSetAPI - sets the FaultTrace reference to null */
    public void clear() { this.clear(); }

    /** XYDataSetAPI - Registers an object for notification of changes to the dataset. */
    public void addChangeListener( DatasetChangeListener listener ) {
        if ( !listeners.contains( listener ) ) {
            listeners.add( listener );
        }
    }

    /** XYDataSetAPI - Deregisters an object for notification of changes to the dataset. */
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
