package org.scec.gui.plot.jfreechart;

import java.text.*;
import java.util.*;

import com.jrefinery.data.*;
import org.scec.data.*;
import org.scec.sha.fault.*;
import org.scec.util.*;

/**
 * <p>Title: FaultTraceXYDataSet</p>
 * <p>Description: Proxy for the FaultTraceList to conform to the JFreeChart API
 * for XYDataSet, we can now pass a FaultTraceList wrapped in this class into a plot</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Steven W. ROck
 * @version 1.0
 */

public class FaultTraceListXYDataSet implements XYDataset, NamedObjectAPI {


    protected final static String C = "FaultTraceListXYDataSet";
    protected final static boolean D = false;

    /**
     *  Internal list of 2D Functions - indexed by name
     */
    protected SimpleFaultDataList list = null;

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


    public boolean checkFaultTraces(){
        if( list != null ) return true;
        else return false;
    }

    /**
     *  no arg constructor
     */
    public FaultTraceListXYDataSet(SimpleFaultDataList list) { this.list = list; }


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

        if ( checkFaultTraces() ) return list.size();
        else return 0;
    }


    /**
     *  Returns the name of a series.
     *
     * @param  series  The series (zero-based index).
     * @return         The seriesName value
     */
    public String getSeriesName( int series ){

        if ( !checkFaultTraces() )  return name + ' ' + series;

        FaultTrace trace = list.getSimpleFaultDataAt(0).getFaultTrace();
        if( trace != null ) return "Trace = " + trace.getName();

        else return name + ' ' + series;
    }

    /**
     *  Returns the number of items in a series.
     *
     * @param  series  The series (zero-based index).
     * @return         The number of items within a series.
     */
    public int getItemCount( int series ) {
        if ( !checkFaultTraces() ) return 0;

        FaultTrace trace = list.getSimpleFaultDataAt(series).getFaultTrace();
        if( trace != null ) return trace.getNumLocations();

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

        if ( checkFaultTraces() && series < getSeriesCount() ) {

            FaultTrace trace = list.getSimpleFaultDataAt(0).getFaultTrace();
            if( trace != null ) {
                Location loc = trace.getLocationAt(item);
                Double lon = new Double( loc.getLongitude() );
                return ( Number ) lon;
            }
            else return null;
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

        if ( checkFaultTraces() && series < getSeriesCount() ) {

            FaultTrace trace = list.getSimpleFaultDataAt(0).getFaultTrace();
            if( trace != null ) {
                Location loc = trace.getLocationAt(item);
                Double lat = new Double( loc.getLatitude() );
                return ( Number ) lat;
            }
            else return null;
        }
        else return null;

    }


    /**
     *  removes all DiscretizedFunction2Ds from the list, making it empty, ready
     *  for new DiscretizedFunction2Ds
     */
    public void clear() { list = null; }




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
