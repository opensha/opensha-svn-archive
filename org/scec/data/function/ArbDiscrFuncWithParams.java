package org.scec.data.function;

import java.util.*;
import org.scec.data.*;
import org.scec.param.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @see DiscretizedFunction2D
 * @see XYDiscretizedFunction2DAPI
 * @see DiscretizedFunction2DAPI
 * @see ParameterList
 * @author
 * @version 1.0
 */

public class ArbDiscrFuncWithParams
    extends ArbitrarilyDiscretizedFunc
    implements FuncWithParamsAPI
{


    // *******************
    /** @todo  Variables */
    // *******************



    /**
     * This parameter list is the set of parameters that went into
     * calculation this DiscretizedFunction. Useful for determining if two
     * data sets are the same, i.e. have the same x/y axis and the same
     * set of independent parameters. Bypasses the more numerically intensive
     * task of comparing each DataPoint2D of two DiscretizedFunction2D.
     */
    protected ParameterList list = new ParameterList();



    // **********************
    /** @todo  Constructors */
    // **********************

    /**
     * The passed in comparator must be an implementor of DataPoint2DComparatorAPI.
     * These comparators know they are dealing with a DataPoint2D and usually
     * only compare the x-values for sorting. Special comparators may wish to
     * sort on both the x and y values, i.e. the data points are geographical
     * locations.
     */
    public ArbDiscrFuncWithParams(Comparator comparator) { super(comparator); }

    /**
     * The passed in comparator must be an implementor of DataPoint2DComparatorAPI.
     * These comparators know they are dealing with a DataPoint2D and usually
     * only compare the x-values for sorting. Special comparators may wish to
     * sort on both the x and y values, i.e. the data points are geographical
     * locations.
     */
    public ArbDiscrFuncWithParams(DataPoint2DComparatorAPI comparator) { super(comparator); }

    /**
     *  Easiest one to use, uses the default DataPoint2DToleranceComparator comparator.
     */
    public ArbDiscrFuncWithParams(double tolerance) { super(tolerance); }


    /**
     *  basic No-Arg constructor
     */
    public ArbDiscrFuncWithParams() { super(); }

    /**
     * Sets all values for this special type of DiscretizedFunction
     */
    public ArbDiscrFuncWithParams( ParameterList list ) {
        super();
        this.list = list;
    }





    // **********************************
    /** @todo  Data Accessors / Setters */
    // **********************************

    public String getInfo(){ return list.toString(); }

    /**
     * This parameter list is the set of parameters that went into
     * calculation this DiscretizedFunction. Useful for determining if two
     * data sets are the same, i.e. have the same x/y axis and the same
     * set of independent parameters. Bypasses the more numerically intensive
     * task of comparing each DataPoint2D of two DiscretizedFunction2D.
     */
    public ParameterList getParameterList(){ return list; }

    /** Set the parameter list from an external source */
    public void setParameterList(ParameterList list){ this.list = list; }

    /** Returns name/value pairs, separated with commas, as one string, usefule for legends, etc. */
    public String getParametersString(){ return list.toString(); }

    /** Returns true if two DefaultXYDiscretizedFunction2D have the same independent parameters */
    public boolean equalParameterNamesAndValues(FuncWithParamsAPI function){
        if( function.getParametersString().equals( getParametersString() ) ) return true;
        else return false;
    }

    /** Returns true if the second function has the same named parameters in
     *  it's list, values may be different
     */
    public boolean equalParameterNames(FuncWithParamsAPI function){
        return function.getParameterList().equalNames( this.getParameterList() );
    }




    /** Returns a copy of this and all points in this DiscretizedFunction */
    public DiscretizedFuncAPI deepClone(){

        ArbDiscrFuncWithParams function = new ArbDiscrFuncWithParams();
        function.setTolerance( this.getTolerance() );
        function.setParameterList( (ParameterList)this.getParameterList().clone() );

        Iterator it = this.getPointsIterator();
        if( it != null ) {
            while(it.hasNext()) {
                function.set( (DataPoint2D)((DataPoint2D)it.next()).clone() );
            }
        }

        return function;

    }

    /**
     * Determines if two functions are the same with respect to the parameters that
     * were used to calculate the function, NOT THAT EACH POINT IS THE SAME. This is used
     * by the DiscretizedFunction2DAPIList to determine if it should add a new function
     * to the list.
     */
    public boolean equalParameters(FuncWithParamsAPI function){
        String S = C + ": equalParameters():";
        return function.getParameterList().equals( this.list );
    }


    /**
     * Returns all the parameters associated with the function as one string with
     * no new lines, of the format:<P>
     * name = value, name2 = value2, etc.
     */
    public String toString(){ return this.list.toString(); }

}
