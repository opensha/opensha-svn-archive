package org.scec.data.function;

import java.util.*;
import org.scec.data.*;
import org.scec.exceptions.*;


import com.jrefinery.data.*;
import org.scec.gui.plot.jfreechart.*;

/**
 * <b>Title:</b> DiscretizedFunction2DList<br>
 * <b>Description:</b> List container for Discretized Functions.
 * <p>
 * Implement XYDataSet so that it can be passed into the
 * JRefinery Graphing Package
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @see Function2DList
 * @see XYDataSet
 * @author Steven W. Rock
 * @version 1.0
 */

public class DiscretizedFuncList extends DiscretizedFunctionXYDataSet{



    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "DiscretizedFunction2DList";
    protected final static boolean D = false;




    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    /** no arg constructor */
    public DiscretizedFuncList() { super(); }



    /* ****************************/
    /** @todo  Accessors, Setters */
    /* ****************************/

    /**
     * Adds all DiscretizedFunction2Ds of the DiscretizedFunction2Dlist to this one, if the
     * named DiscretizedFunction2D is not already in the list
     */
    public void addDiscretizedFunction2DList(DiscretizedFuncList list2) throws DiscretizedFunction2DException{

        ListIterator it = list2.getDiscretizedFunction2DsIterator();
        while( it.hasNext() ){
            DiscretizedFuncAPI function = (DiscretizedFuncAPI)it.next();
            if( !containsDiscretizedFunction2D(function) ){ this.addDiscretizedFunction2D(function); }
        }

    }

    /** adds the DiscretizedFunction2D if it doesn't exist, else throws exception */
    public void addDiscretizedFunction2D(DiscretizedFuncAPI function) throws DiscretizedFunction2DException{

        if( !containsDiscretizedFunction2D(function) ){ functions.add( function ); }
        else{
            String S = C + ": addDiscretizedFunction2D(): ";
            throw new DiscretizedFunction2DException(S + "This DiscretizedFunction2D already exists.");
        }

    }




    /**
     * FIX *** FIX *** <P>
     * checks if the DiscretizedFunction2D exists in the list. Loops through
     * each function in the list and calls function.equals( function2 )
     * against the passed in function to determine if this function exists.
     */
    public boolean containsDiscretizedFunction2D(DiscretizedFuncAPI function){

        ListIterator it = getDiscretizedFunction2DsIterator();

        while(it.hasNext() ){
            DiscretizedFuncAPI function1 = (org.scec.data.function.DiscretizedFuncAPI)it.next();
            //String params1 = function1.getParameterList().get
            //if( function1.sameParameters( function ) ) return true;
        }
        return false;

    }


    /** removes DiscretizedFunction2D if it exists, else
     *  throws exception
     */
    public void removeDiscretizedFunction2D(DiscretizedFuncAPI function) throws DiscretizedFunction2DException {

        ListIterator it = getDiscretizedFunction2DsIterator();

        while(it.hasNext() ){
            DiscretizedFuncAPI function1 = (org.scec.data.function.DiscretizedFuncAPI)it.next();
            if( function1.equals( function ) ) {
                functions.remove(function1);
                return;
            }
        }

        String S = C + ": removeDiscretizedFunction2D(): ";
        throw new DiscretizedFunction2DException(S + "The specified DiscretizedFunction2D exist.");

    }

    /**
     * updates an existing DiscretizedFunction2D with the new value,
     * throws exception if DiscretizedFunction2D doesn't exist
     */
    public void updateDiscretizedFunction2D(DiscretizedFuncAPI function) throws DiscretizedFunction2DException {
        removeDiscretizedFunction2D(function);
        addDiscretizedFunction2D(function);
    }





    /**
     * Returns true if all the DisctetizedFunctions in this list are equal.
     */
    public boolean equals(DiscretizedFunctionXYDataSet list) throws ClassCastException{

        // Not same size, can't be equal
        if( this.size() != list.size() ) return false;

        if( !( list instanceof DiscretizedFuncList ) )  throw new
            ClassCastException(C + ": equals(DiscretizedFunctionXYDataSet: list must be a DiscretizedFunction2DList.");

        return equals( (DiscretizedFuncList)list );
    }

    /**
     * Returns true if all the DisctetizedFunctions in this list are equal.
     */
    public boolean equals(DiscretizedFuncList list){

        // Not same size, can't be equal
        if( this.size() != list.size() ) return false;

        // if( this.get

        // Check each individual Parameter
        ListIterator it = this.getDiscretizedFunction2DsIterator();
        while(it.hasNext()){

            // This list's parameter
            DiscretizedFuncAPI function1 = (DiscretizedFuncAPI)it.next();

            // List may not contain parameter with this list's parameter name
            if ( !list.containsDiscretizedFunction2D( function1 ) ) return false;

        }

        // Passed all tests - return true
        return true;

    }

    /**
     * Returns a copy of this list, therefore any changes to the copy
     * cannot affect this original list.
     */
    public Object clone(){

        DiscretizedFuncList list1 = new DiscretizedFuncList();

        ListIterator it = this.getDiscretizedFunction2DsIterator();
        while(it.hasNext()){

            // This list's parameter
            DiscretizedFuncAPI function1 = (DiscretizedFuncAPI)((DiscretizedFuncAPI)it.next()).clone();

            list1.addDiscretizedFunction2D( function1 );

        }

        return list1;

    }


    /**
     * Returns the name of a series.
     * @param series The series (zero-based index).
     */
    public String getSeriesName(int series)
    {
        if( series < functions.size() ){
            String str = ( (DiscretizedFuncAPI)this.functions.get(series) ).toString();
            return str;
        }
        else return "";
    }

}
