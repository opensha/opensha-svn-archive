package org.scec.gui.plot.jfreechart;

import java.util.*;

import com.jrefinery.data.*;
import org.scec.data.*;
import org.scec.data.function.DiscretizedFuncAPI;
import org.scec.data.function.DiscretizedFuncList;


/**
 * <b>Title:</b> DiscretizedFunctionXYDataSet<p>
 *
 * <b>Description:</b> Wrapper for a DiscretizedFuncList. Implements
 * XYDataSet so that it can be passed into the JRefinery Graphing Package <p>
 *
 * This class contains a pointer to a DiscretizedFuncList. It also implements
 * an XYDataset which is JFreChart's interface that all datasets must implement
 * so they can be passed to the graphing routines. This class transforms the
 * DiscretizedFuncList data into the format as required by this interface.<p>
 *
 * Please consult the JFreeChart documentation for further information
 * on XYDataSets. <p>
 *
 * Note: The FaultTraceXYDataSet and GriddedSurfaceXYDataSet are
 * handled in exactly the same manner as for DiscretizedFunction.<p>
 *
 * Modified 7/21/2002 SWR: I  mede this list more generic to handle any type
 * of DiscretizedFunc that implements DiscretizedFuncAPI. Previously it only
 * handled ArbDiscrFunctWithParams.<p>
 *
 * Modified 7/21/2002 SWR: (Still need to do) Made this list handle log-log
 * plots by hiding zero values in x and y axis when choosen. If not
 * JFreeeChart will throw an arithmatic exception.<p>
 *
 * Modified Gupta Brothers: Expanded the log-log capabilities. <p>
 *
 * @see FaultTraceXYDataSet
 * @see DiscretizedFunctionXYDataSet
 * @see        DiscretizedFuncList
 * @author     Steven W. Rock, Gupta Brothers
 * @created    February 26, 2002
 * @version    1.2
 */

public class DiscretizedFunctionXYDataSet implements XYDataset, NamedObjectAPI {

    /** Class name used for debug statements */
    protected final static String C = "DiscretizedFunctionXYDataSet";
    /** If true prints out debug statements */
    protected final static boolean D = false;

    protected boolean yLog = false;
    protected boolean xLog = false;

    public boolean isYLog() { return yLog; }
    public void setYLog(boolean yLog) { this.yLog = yLog; }

    public boolean isXLog() { return xLog; }
    public void setXLog(boolean xLog) { this.xLog = xLog; }


    /**
     * Internal list of 2D Functions - indexed by name. This
     * is the real data that is "wrapped" by this class.
     */
    protected DiscretizedFuncList functions = null;

    /** list of listeners for data changes */
    protected Vector listeners = new Vector();

    /** SWR: Not sure what this is used for - Gupta code */
    protected LinkedList xLogs = new LinkedList();

    /** closet possible value to zero */
    private double minVal = Double.MIN_VALUE;

    /**
     * Flag to indicate how to handle zeros, if true if a
     * y-value is zero, will be converted to the minVal.
     */
    private boolean convertZeroToMin = false;

    /** The group that the dataset belongs to. */
    private DatasetGroup group;

    /** no arg constructor -  */
    public DiscretizedFunctionXYDataSet() {
      this.group = new DatasetGroup();
    }


    /** Sets the name of the functions list */
    public void setName( String name ) { functions.setName( name ); }
    /** Gets the name of the functions list */
    public String getName() { return functions.getName(); }


    /** Returns an iterator of all DiscretizedFunction2Ds in the list */
    public ListIterator listIterator() { return functions.listIterator(); }



    /**
     *  XYDataSetAPI - Returns the number of series in the dataset.
     *  For a DiscretizedFuncList returns the number of functions
     * in the list.
     */
    public int getSeriesCount() { return functions.size(); }



    /**
     *  XYDataSetAPI - Returns the name of a series. To make this
     * name unique, info string of the particulare discretized
     * function is returned at the name of that series. Typically
     * the info string represents the key-value input paramters.
     */
    public String getSeriesName(int series)
    {
        if( series < functions.size() ){
            String str = ( (DiscretizedFuncAPI)this.functions.get(series) ).getInfo();
            return str;
        }
        else return "";
    }

    /**
     * XYDataSetAPI - Returns the number of items in a series.
     * The particular DiscretizedFuncAPI at the specified index ( series ),
     * is obtained, then getNum() is called on that function. This
     * number is reduced by one if the first x point is zero and xLog is choosen.<p>
     */
     public int getItemCount( int series ) {
        int num = -1;
        if ( series < functions.size() ) {
            DiscretizedFuncAPI f = functions.get( series );
            num = f.getNum();
            if( DiscretizedFunctionXYDataSet.isAdjustedIndexIfFirstXZero( f, xLog, yLog) ) num -= 1;
        }
        return num;
    }


    /**
     * XYDatasetAPI - Returns the x-value for an item within a series. <P>
     *
     * The implementation is responsible for ensuring that the x-values are
     * presented in ascending order.
     *
     * Note: If xlog is choosen, and first x point is zero the index is incresed
     * to return the second point.
     *
     * @param  series  The series (zero-based index).
     * @param  item    The item (zero-based index).
     * @return         The x-value for an item within a series.
     */
    public Number getXValue( int series, int item ) {

        if ( series < functions.size() ) {
            Object obj = functions.get( series );
            if( obj != null && obj instanceof DiscretizedFuncAPI){

                if( DiscretizedFunctionXYDataSet.isAdjustedIndexIfFirstXZero(( DiscretizedFuncAPI ) obj, xLog, yLog) )
                  ++item;

                // get the value
                double x = ( ( DiscretizedFuncAPI ) obj ).getX(item);

                // return if not NaN
                if( x != Double.NaN ) return (Number)(new Double(x));
            }
        }
        return null;

    }

    /**
     * XYDatasetAPI - Returns the y-value for an item within a series. <P>
     *
     * Note: If xlog is choosen, and first x point is zero the index is incresed
     * to return the second point.
     *
     * @param  series  The series (zero-based index).
     * @param  item    The item (zero-based index).
     * @return         The y-value for an item within a series.
     */
    public Number getYValue( int series, int item ) {

        if ( series < functions.size() ) {
            Object obj = functions.get( series );
            if( obj != null && obj instanceof DiscretizedFuncAPI){

                if( DiscretizedFunctionXYDataSet.isAdjustedIndexIfFirstXZero(( DiscretizedFuncAPI ) obj, xLog, yLog) )
                  ++item;

                // get the value
                double y = ( ( DiscretizedFuncAPI ) obj ).getY(item);

                if(convertZeroToMin && y<=minVal && yLog)
                     return (Number)(new Double(minVal));
                // return if not NaN
                if( y != Double.NaN ) return (Number)(new Double(y));


            }
        }
        return null;
    }

    /**
     * Very important function to handle log plotting. That is why this
     * function is made final, so subclasses can't overide this functionality.
     * This is an internal helper function used when getX() or getY()m numberPoints(),
     * etc. are called.<p>
     *
     * This returns truw if the first point should be skipped. The criteria is based
     * on if xLog and yLog are true, and the first point x or y values are zero.
     * If these conditions are met, true is returned, false otherwise.
     */
    protected final static boolean isAdjustedIndexIfFirstXZero(DiscretizedFuncAPI func, boolean xLog, boolean yLog){

        // if xlog and first x value = 0 increment index, even if y first value not zero,
        // and vice versa fro yLog. This call used by both getXValue and getYValue
        if( ( xLog && func.getX(0) == 0 ) || ( yLog && func.getY(0) == 0 ) ) return true;
        else return false;
    }

    /** Removes all DiscretizedFunction2Ds from the list, making it an empty list. */
    public void clear() { functions.clear(); }


    /** Returns number of DiscretizedFunction2Ds in the list. */
    public int size() { return functions.size(); }


    /**
     *  Returns true if all the Functions in this list are equal. See
     * DiscretizedFunctList.equals() for further details.
     */
    public boolean equals( DiscretizedFunctionXYDataSet list ){
        if( list.getFunctions().equals( this.functions ) ) return true;
        else return false;
    }


    /**
     * Returns a copy of this list, therefore any changes to the copy cannot
     * affect this original list. A deep clone() indicates that all the
     * list fields are cloned, as well as all the fucntions in the list, and
     * each point in each function. This is a very expensive operations
     * if there are a large numbe of functions and/or points.
     */
    public DiscretizedFunctionXYDataSet deepClone(){

        DiscretizedFunctionXYDataSet set = new DiscretizedFunctionXYDataSet();
        DiscretizedFuncList list = functions.deepClone();
        set.setFunctions(list);
        return set;

    }

    /** XYDatasetAPI- Registers an object for notification of changes to the dataset. */
    public void addChangeListener( DatasetChangeListener listener ) {
        if ( !listeners.contains( listener ) ) {
            listeners.add( listener );
        }
    }

    /** XYDatasetAPI- Deregisters an object for notification of changes to the dataset. */
    public void removeChangeListener( DatasetChangeListener listener ) {
        if ( listeners.contains( listener ) ) {
            listeners.remove( listener );
        }
    }

    /** Returns the "wrapped" dataset, i.e. the DiscretizedFunctionList */
    public DiscretizedFuncList getFunctions() { return functions; }
    /** Sets the "wrapped" dataset, i.e. the DiscretizedFunctionList */
    public void setFunctions(DiscretizedFuncList functions) {
        this.functions = functions;
    }

    /** In case of Y-log, set' swhether you want to convert 0 value to minValue. */
    public void setConvertZeroToMin(boolean zeroToMin) { convertZeroToMin = zeroToMin; }

    /**
     * In case of Y-log, you can specify the minValue so that 0 values on y - axis
     * will be converted to this value.
     *
     * @param zeroMin true if you want to convert 0 values in Y-log to small value
     * @param minVal  value which will be returned if we have 0 on Y-axis in case of log
     */
    public void setConvertZeroToMin(boolean zeroMin,double minVal){
       convertZeroToMin = zeroMin;
       this.minVal = minVal;
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



