package org.scec.gui.plot.jfreechart;

import java.text.*;
import java.util.*;

import com.jrefinery.data.*;
import org.scec.data.*;
import org.scec.sha.fault.*;
import org.scec.util.*;

/**
 * <p>Title: FaultTraceXYDataSet</p>
 * <p>Description: Proxy for the FaultTrace to conform to the JFreeChart API
 * for XYDataSet, we can now pass a FaultTrace wrapped in this class into a plot</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Steven W. ROck
 * @version 1.0
 */

public class FaultTraceXYDataSet implements XYDataset, NamedObjectAPI {


    protected final static String C = "FaultTraceXYDataSet";
    protected final static boolean D = false;

    /**
     *  Internal trace of Locations
     */
    protected FaultTrace trace = null;

    /**
     *  list of listeners for data changes
     */
    protected Vector listeners = new Vector();

    /**
     *  Name of this Function2DList. Used for display purposes and identifying
     *  unique Lists.
     */
    protected String name;

    DecimalFormat format = new DecimalFormat("#,###.##");


    public boolean checkFaultTrace(){
        if( trace != null ) return true;
        else return false;
    }

    /**
     *  no arg constructor
     */
    public FaultTraceXYDataSet(FaultTrace trace) { this.trace = trace; }


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

        if ( checkFaultTrace() )  return 1;
        else return 0;
    }


    /**
     *  Returns the name of a series.
     *
     * @param  series  The series (zero-based index).
     * @return         The seriesName value
     */
    public String getSeriesName( int series ){
        if ( !checkFaultTrace() )  return name + ' ' + series;
        else return "Trace = " + trace.getName();
    }

    /**
     *  Returns the number of items in a series.
     *
     * @param  series  The series (zero-based index).
     * @return         The number of items within a series.
     */
    public int getItemCount( int series ) {
        if ( !checkFaultTrace() ) return 0;
        if( series != 0 ) return 0;
        if( trace != null ) return trace.getNumLocations();
        return 0;
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

        if ( checkFaultTrace() && series < getSeriesCount() ) {
            Location loc = trace.getLocationAt(item);
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

        if ( checkFaultTrace() && series < getSeriesCount() ) {
            Location loc = trace.getLocationAt(item);
            Double lat = new Double( loc.getLatitude() );
            return ( Number ) lat;
        }
        else return null;

    }


    /**
     *  removes all DiscretizedFunction2Ds from the trace, making it empty, ready
     *  for new DiscretizedFunction2Ds
     */
    public void clear() { trace = null; }




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
