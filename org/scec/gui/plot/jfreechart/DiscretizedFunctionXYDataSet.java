package org.scec.gui.plot.jfreechart;

import java.util.*;

import com.jrefinery.data.*;
import org.scec.data.function.*;
import org.scec.util.*;
import org.scec.data.*;

import java.util.*;

import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.DiscretizedFunction2DException;
import org.scec.data.DataPoint2D;
import org.scec.gui.plot.jfreechart.*;

/**
 *  <b>Title:</b> Function2DList<br>
 *  <b>Description:</b> List container for 2D Functions. Subclasses deal with
 *  particular types of 2D Functions. <p>
 *  Implements XYDataSet so that it can be passed into the JRefinery Graphing
 *  Package <p>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @see        XYDataSet
 * @version    1.0
 */

public abstract class DiscretizedFunctionXYDataSet implements XYDataset, NamedObjectAPI {

    /**
     *  Description of the Field
     */
    protected final static String C = "DiscretizedFunctionXYDataSet";
    /**
     *  Description of the Field
     */
    protected final static boolean D = false;

    /**
     *  Internal list of 2D Functions - indexed by name
     */
    protected Vector functions = new Vector();

    /**
     *  list of listeners for data changes
     */
    protected Vector listeners = new Vector();

    /**
     *  Name of this Function2DList. Used for display purposes and identifying
     *  unique Lists.
     */
    protected String name;


    /**
     *  no arg constructor
     */
    public DiscretizedFunctionXYDataSet() { }


    /**
     *  Sets the name attribute of the Function2DList object
     *
     * @param  newName  The new name value
     */
    public void setName( String newName ) {
        name = newName;
    }


    /**
     *  returns an iterator of all DiscretizedFunction2Ds in the list
     *
     * @return    The discretizedFunction2DsIterator value
     */
    public ListIterator getDiscretizedFunction2DsIterator() {
        return functions.listIterator();
    }



    /**
     *  Returns the number of series in the dataset.
     *
     * @return    The number of series in the dataset.
     */
    public int getSeriesCount() {
        return this.size();
    }


    /**
     *  Returns the name of a series.
     *
     * @param  series  The series (zero-based index).
     * @return         The seriesName value
     */
    public abstract String getSeriesName( int series );



    /**
     *  Returns the number of items in a series.
     *
     * @param  series  The series (zero-based index).
     * @return         The number of items within a series.
     */
    public int getItemCount( int series ) {

        if ( series < functions.size() ) {
            return ( ( DiscretizedFuncAPI ) this.functions.get( series ) ).getNum();
        } else {
            return -1;
        }
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
            DataPoint2D point = ( ( DiscretizedFuncAPI ) obj ).get( item );
            if ( D )  System.out.println( C + ": getXValue(): X = " + point.getX().toString() );
            return ( Number ) point.getX();
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
            DataPoint2D point = ( ( DiscretizedFuncAPI ) this.functions.get( series ) ).get( item );
            if ( D ) System.out.println( C + ": getYValue(): Y = " + point.getY().toString() );
            return ( Number ) point.getY();
        }
        return null;
    }


    /**
     *  Gets the name attribute of the Function2DList object
     *
     * @return    The name value
     */
    public String getName() {
        return name;
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
    public void clear() {
        functions.clear();
    }


    /**
     *  returns number of DiscretizedFunction2Ds in the list
     *
     * @return    Description of the Return Value
     */
    public int size() {
        return functions.size();
    }


    /**
     *  Returns true if all the Functions in this list are equal.
     *
     * @param  list  Description of the Parameter
     * @return       Description of the Return Value
     */
    public abstract boolean equals( DiscretizedFunctionXYDataSet list );


    /**
     *  Returns a copy of this list, therefore any changes to the copy cannot
     *  affect this original list.
     *
     * @return    Description of the Return Value
     */
    public abstract Object clone();


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
