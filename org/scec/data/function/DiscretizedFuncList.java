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

//public class DiscretizedFuncList extends DiscretizedFunctionXYDataSet{
public class DiscretizedFuncList implements NamedObjectAPI{



    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "DiscretizedFuncList";
    protected final static boolean D = false;


    /**
     * List of DiscretizedFuncAPI
     */
    ArrayList functions = new ArrayList();

    protected String info = "";
    protected String name = "";

    /** The X-Axis name */
    protected String xAxisName = "";

    /** The Y-Axis name */
    protected String yAxisName = "";



    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    /** no arg constructor */
    public DiscretizedFuncList() { super(); }



    /* **************************************/
    /** @todo  Simple field getters/setters */
    /* **************************************/

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getInfo(){ return info; }
    public void setInfo(String info){ this.info = info; }

    public String getXAxisName(){ return xAxisName; }
    public void setXAxisName(String name){ this.xAxisName = name; }

    public String getYAxisName(){ return yAxisName; }
    public void setYAxisName(String name){ this.yAxisName = name; }

    /** Combo Name of the X and Y axis, used for determining if tow DiscretizedFunction2DAPI */
    public String getXYAxesName(){ return xAxisName + ',' + yAxisName; }



    /* **************************/
    /** @todo  Function Helpers */
    /* **************************/

    private boolean hasFunctionAtIndex(int index){
        if( (index + 1) > functions.size() ) return false;
        else return true;
    }

    private DiscretizedFuncAPI getFunction(int index){
        return (DiscretizedFuncAPI)functions.get(index);
    }


    /**
     * Currently only returns true. Further
     * functionality may be added in the future or by subclasses.
     * @param function
     * @return
     */
    public boolean isFuncAllowed(DiscretizedFuncAPI function){
        return true;
    }

    /* ******************************/
    /** @todo  Basic List functions */
    /* ******************************/

    public void clear(){ functions.clear(); }
    public ListIterator listIterator(){ return functions.listIterator( );  }
    public Iterator iterator(){ return functions.iterator( ); }
    public int size(){ return functions.size(); }

    /**
     * Returns the index of the first occurrence of the argument in this
     * list; returns -1 if the object is not found. Uses equals() to determing
     * if the same.
     * @param function
     * @return
     */
    public int indexOf(DiscretizedFunc function){
        int counter = 0;
        ListIterator it = listIterator();
        while( it.hasNext() ){
            DiscretizedFuncAPI f1 = (DiscretizedFuncAPI)it.next();
            if( f1.equals(function) ) return counter;
            counter++;
        }
        return -1;
    }

    /**
     * Removes the function at the specified index
     * @param index
     */
    public void remove(int index){ functions.remove(index); }

    /**
     * Removes the specified function if it exists as determined by equals()
     * @param function
     */
    public void remove(DiscretizedFuncAPI function){
        ListIterator it = listIterator();
        if( it != null ) {
            while(it.hasNext() ){
                DiscretizedFuncAPI f1 = (org.scec.data.function.DiscretizedFuncAPI)it.next();
                if( f1.equals(function) ) {
                    functions.remove(f1);
                    return;
                }
            }
        }
    }

    /**
     * Removes any functions in the passed in list that exist
     * in this list. Only removes the union of these two function
     * list. Will leave functions in this list that doesn't exist
     * in second. Use clear() to compleatly empty this list. This
     * function calls remove() on every function in the passed in list.
     * @param list
     */
    public void removeAll(DiscretizedFuncList list){
        ListIterator it = list.listIterator();
        while( it.hasNext() ){
            DiscretizedFuncAPI function = (DiscretizedFuncAPI)it.next();
            remove(function);
        }
    }


    /**
     * Checks if the DiscretizedFuncAPI exists in the list. Loops through
     * each function in the list and calls function.equals( function2 )
     * against the passed in function to determine if this function exists.
     */
    public boolean contains(DiscretizedFuncAPI function){
        ListIterator it = listIterator();
        if( it != null ) {
            while(it.hasNext() ){
                DiscretizedFuncAPI f1 = (org.scec.data.function.DiscretizedFuncAPI)it.next();
                if( f1.equals(function) ) return true;
            }
            return false;
        }
        else return false;
    }

    /**
     * checks if the DiscretizedFuncAPI exists in the list. Loops through
     * each function in the list and calls function.equals( function2 )
     * against the passed in function to determine if this function exists.
     */
    public boolean containsAll(DiscretizedFuncList list){
        ListIterator it = list.listIterator();
        while( it.hasNext() ){
            DiscretizedFuncAPI function = (DiscretizedFuncAPI)it.next();
            if( !contains(function) ) return false;
        }
        return true;
    }


    /** adds the DiscretizedFuncAPI if it doesn't exist based on the equals() method, else throws exception */
    public void add(DiscretizedFuncAPI function) throws DiscretizedFunction2DException{

        if( !isFuncAllowed(function) ) throw new DiscretizedFunction2DException(C + ": add(): " + "This function is not allowed.");
        //if( contains(function) ) throw new DiscretizedFunction2DException(C + ": add(): " + "This function is already in the list.");
        functions.add( function );

    }


    /**
     * Adds all DiscretizedFuncAPI of the DiscretizedFuncList to this one, if the
     * named DiscretizedFuncAPI is not already in the list
     */
    public void addAll(DiscretizedFuncList list) throws DiscretizedFunction2DException{

        ListIterator it = list.listIterator();
        while( it.hasNext() ){
            DiscretizedFuncAPI function = (DiscretizedFuncAPI)it.next();
            try{ add(function); }
            catch( DiscretizedFunction2DException ex) {}
        }



    }

    /**
     * Returns the DiscretizedFuncAPI at the specified index. If index is larger than the number
     * of functions, null is returned.
     * @param index
     * @return DiscretizedFuncAPI function
     */
    public DiscretizedFuncAPI get(int index){
        DiscretizedFuncAPI f = null;
        if( hasFunctionAtIndex(index) )  f = (DiscretizedFuncAPI)functions.get(index);
        return f;
    }

    /**
     * Updates an existing function in the list or adds it if it doesn't exist. This
     * function simply calls remove() then add().
     */
    public void update(DiscretizedFuncAPI function)  { remove(function); add(function); }



    /**
     * Returns true if all the DisctetizedFunctions in this list are equal
     * and the boolean flag setting are the same. Equals is determined if
     * the two lists are the same size, then calls containsAll()
     */
    public boolean equals(DiscretizedFuncList list){

        // Not same size, can't be equal
        if( this.size() != list.size() ) return false;

        // next check boolean flags
        // if( allowedDifferentAxisNames != list.allowedDifferentAxisNames ) return false;
        // if( allowedIdenticalFunctions != list.allowedIdenticalFunctions ) return false;

        // All functions must be present
        if ( !containsAll(list) ) return false;

        // Passed all tests - return true
        return true;

    }



    /**
     * Returns a copy of this list, therefore any changes to the copy
     * cannot affect this original list.
     */
    public DiscretizedFuncList deepClone(){

        DiscretizedFuncList l = new DiscretizedFuncList();

        ListIterator it = listIterator();
        while(it.hasNext()){

            // This list's parameter
            DiscretizedFuncAPI f = ((DiscretizedFuncAPI)it.next()).deepClone();
            l.add( f );

        }

        //l.allowedDifferentAxisNames = allowedDifferentAxisNames;
        //l.allowedIdenticalFunctions = allowedIdenticalFunctions;

        l.name = name;
        l.info = info;
        l.xAxisName = xAxisName;
        l.yAxisName = yAxisName;

        return l;

    }

    private static String TAB = "   ";
    public String toString(){

        String S = C + ": toString(): ";

        StringBuffer b = new StringBuffer();
        b.append("\n");
        b.append("X-Axis: " + this.xAxisName + '\n');
        b.append("Y-Axis: " + this.yAxisName + '\n');
        b.append("Number of Data Sets: " + this.size() + '\n');

        ListIterator it = listIterator();
        boolean first = true;
        int counter = 0;
        while( it.hasNext() ){

            DiscretizedFuncAPI function = (DiscretizedFuncAPI)it.next();
            Iterator it2 = function.getPointsIterator();

            b.append("\n\nFunction: " + function.getName() + '\n');
            b.append("Function: " + function.getNum() + '\n');
            b.append("Function: " + function.getInfo() + '\n');

            while(it2.hasNext()){

                DataPoint2D point = (DataPoint2D)it2.next();
                double x = point.getX();
                double y = point.getY();

                b.append(TAB + x + ", " + y + '\n');
            }

        }

        return b.toString();
    }


    /**
     * Returns all datapoints in a matrix, x values in first column,
     * first functions y vaules in second, second function's y values
     * in the third, etc. This function should be optimized by actually accessing
     * the underlying TreeMap.
     */
    public String toStringOld(){

        String S = C + ": toString(): ";

        StringBuffer b = new StringBuffer();
        b.append("Discretized Function Data Points\n");
        b.append("X-Axis: " + this.xAxisName + '\n');
        b.append("Y-Axis: " + this.yAxisName + '\n');
        b.append("Number of Data Sets: " + this.size() + '\n');


        StringBuffer b2 = new StringBuffer();

        final int numPoints = 100;
        final int numSets = this.size();

        Number[][] model = new Number[numSets][numPoints];
        double[] xx = new double[numPoints];
        String[] rows = new String[numPoints];

        ListIterator it1 = listIterator();
        boolean first = true;
        int counter = 0;
        b.append("\nColumn " + (counter + 1) + ". X-Axis" );
        while( it1.hasNext() ){

            DiscretizedFuncAPI function = (DiscretizedFuncAPI)it1.next();
            b.append("\nColumn " + (counter + 2) + ". Y-Axis: " + function.toString());

            Iterator it = function.getPointsIterator();

            // Conversion
            if(D) System.out.println(S + "Converting Function to 2D Array");
            int i = 0;
            while(it.hasNext()){

                DataPoint2D point = (DataPoint2D)it.next();

                if(first){
                    double x1 = point.getX();
                    xx[i] = x1;
                }

                double y1 = point.getY();
                model[counter][i] = new Double(y1);

                i++;
            }
            //rows[counter] = function.getXYAxesName();
            if(first) first = false;
            counter++;

        }


        for(int i = 0; i < numPoints; i++){
            b2.append('\n');
            for(int j = 0; j < numSets; j++){

                if( j == 0 ) b2.append( "" + xx[i] + '\t' + model[j][i].toString() );
                else b2.append( '\t' + model[j][i].toString() );

            }

        }

        b.append("\n\n-------------------------");
        b.append( b2.toString() );
        b.append("\n-------------------------\n");

        return b.toString();

    }

}


    // private boolean allowedDifferentAxisNames = false;
    // private boolean allowedIdenticalFunctions = false;

    /*
    public void setAllowedDifferentAxisNames(boolean allowedDifferentAxisNames) {
        this.allowedDifferentAxisNames = allowedDifferentAxisNames;
    }
    public boolean isAllowedDifferentAxisNames() {
        return allowedDifferentAxisNames;
    }
    public void setAllowedIdenticalFunctions(boolean allowedIdenticalFunctions) {
        this.allowedIdenticalFunctions = allowedIdenticalFunctions;
    }
    public boolean isAllowedIdenticalFunctions() {
        return allowedIdenticalFunctions;
    }

    */