package org.scec.gui.plot.jfreechart;

import java.util.*;

import com.jrefinery.data.*;
import org.scec.data.*;
import org.scec.data.function.DiscretizedFuncAPI;
import org.scec.data.function.DiscretizedFuncList;


/**
 *  <b>Title:</b> DiscretizedFunctionXYDataSet<br>
 *  <b>Description:</b> Wrapper for a DiscretizedFuncList. Implements
 *  XYDataSet so that it can be passed into the JRefinery Graphing Package <p>
 *
 *  Modified 7/21/2002 SWR: I  mede this list more generic to handle any type
 *  of DiscretizedFunc that implements DiscretizedFuncAPI. Previously it only
 *  handled ArbDiscrFunctWithParams.<p>
 *
 *  Modified 7/21/2002 SWR: (Still need to do) Made this list handle log-log
 *  plots by hiding zero values in x and y axis when choosen. If not
 *  JFreeeChart will throw an arithmatic exception.<p>
 *
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @see        XYDataSet
 * @see        DiscretizedFuncList
 * @version    1.2
 */

public class DiscretizedFunctionXYDataSet implements XYDataset, NamedObjectAPI {


    protected final static String C = "DiscretizedFunctionXYDataSet";
    protected final static boolean D = false;

    protected boolean yLog = false;
    protected boolean xLog = false;

    public boolean isYLog() { return yLog; }

    public void setYLog(boolean yLog) {

      this.yLog = yLog;

    }

    public boolean isXLog() { return xLog; }

    public void setXLog(boolean xLog) { this.xLog = xLog; }


    /**
     *  Internal list of 2D Functions - indexed by name
     */
    protected DiscretizedFuncList functions = null;

    /**
     *  list of listeners for data changes
     */
    protected Vector listeners = new Vector();

    protected LinkedList xLogs = new LinkedList();

    private double minVal = Double.MIN_VALUE;
    private boolean convertZeroToMin = false;


    /**
     *  no arg constructor
     */
    public DiscretizedFunctionXYDataSet() { }


    /**
     *  Sets the name attribute of the Function2DList object
     *
     * @param  newName  The new name value
     */
    public void setName( String name ) { functions.setName( name ); }

    /**
     *  Gets the name attribute of the Function2DList object
     *
     * @return    The name value
     */
    public String getName() { return functions.getName(); }


    /**
     *  returns an iterator of all DiscretizedFunction2Ds in the list
     *
     * @return    The lsitIterator value
     */
    public ListIterator listIterator() { return functions.listIterator(); }



    /**
     *  Returns the number of series in the dataset.
     *
     * @return    The number of series in the dataset.
     */
    public int getSeriesCount() { return functions.size(); }



    /**
     * Returns the name of a series.
     * @param series The series (zero-based index).
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
     *  Returns the number of items in a series.
     *
     * @param  series  The series (zero-based index).
     * @return         The number of items within a series.
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
     *  Returns the y-value for an item within a series.
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

                if(convertZeroToMin && y==0 && yLog)
                     return (Number)(new Double(minVal));
                // return if not NaN
                if( y != Double.NaN ) return (Number)(new Double(y));


            }
        }
        return null;
    }


    protected final static boolean isAdjustedIndexIfFirstXZero(DiscretizedFuncAPI func, boolean xLog, boolean yLog){

        // if xlog and first x value = 0 increment index, even if y first value not zero,
        // and vice versa fro yLog. This call used by both getXValue and getYValue
        if( ( xLog && func.getX(0) == 0 ) || ( yLog && func.getY(0) == 0 ) ) return true;
        else return false;
    }



    /*
     *  {
     *  if( series < functions.size() ){
     *  return ((DiscretizedFunction2D)this.functions.get(series)).getParametersString();
     *  }
     *  else return "";
     *  }
     */
    /**
     *  removes all DiscretizedFunction2Ds from the list, making it empty, ready
     *  for new DiscretizedFunction2Ds
     */
    public void clear() { functions.clear(); }


    /**
     *  returns number of DiscretizedFunction2Ds in the list
     *
     * @return    Description of the Return Value
     */
    public int size() { return functions.size(); }


    /**
     *  Returns true if all the Functions in this list are equal.
     *
     * @param  list  Description of the Parameter
     * @return       Description of the Return Value
     */
    public boolean equals( DiscretizedFunctionXYDataSet list ){

        if( list.getFunctions().equals( this.functions ) ) return true;
        else return false;

    }


    /**
     *  Returns a copy of this list, therefore any changes to the copy cannot
     *  affect this original list.
     *
     * @return    Description of the Return Value
     */
    public DiscretizedFunctionXYDataSet deepClone(){

        DiscretizedFunctionXYDataSet set = new DiscretizedFunctionXYDataSet();
        DiscretizedFuncList list = functions.deepClone();
        set.setFunctions(list);
        return set;

    }


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



    public DiscretizedFuncList getFunctions() { return functions; }
    public void setFunctions(DiscretizedFuncList functions) {
        this.functions = functions;
    }

    /**
     * In case of Y-log, whther ypou want to convert 0 value to min value
     *
     * @param zeroToMin  true if you want to convert 0 values in Y-log to very small value
     */
    public void setConvertZeroToMin(boolean zeroToMin) {
       convertZeroToMin = zeroToMin;
    }

    /**
     * In case of Y-log, you can specify the minValue so that 0 values on y - axis
     * will be converted to this value
     *
     * @param zeroMin true if you want to convert 0 values in Y-log to small value
     * @param minVal  value which will be returned if we have 0 on Y-axis in case of log
     */
    public void setConvertZeroToMin(boolean zeroMin,double minVal){
       convertZeroToMin = zeroMin;
       this.minVal = minVal;
    }

}



