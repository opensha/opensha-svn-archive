package org.scec.data.function;

import java.util.TreeMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Comparator;

import org.scec.util.*;
import org.scec.exceptions.*;
import org.scec.param.ParameterList;
import org.scec.data.*;

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
    implements ArbDiscrFuncWithParamsAPI
{


    // *******************
    /** @todo  Variables */
    // *******************

    /** The X-Axis name */
    protected String xAxisName = "";

    /** The Y-Axis name */
    protected String yAxisName = "";

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
    public ArbDiscrFuncWithParams(Double tolerance) { super(tolerance); }


    /**
     *  basic No-Arg constructor
     */
    public ArbDiscrFuncWithParams() { super(); }

    /**
     * Sets all values for this special type of DiscretizedFunction
     */
    public ArbDiscrFuncWithParams(
        String xAxisName, String yAxisName,
        ParameterList list
    ) {
        super();
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
        this.list = list;
    }





    // **********************************
    /** @todo  Data Accessors / Setters */
    // **********************************

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

    /** Returns the current x axis name */
    public String getXAxisName(){ return xAxisName; }
    /** Sets the x axis name */
    public void setXAxisName(String name){ this.xAxisName = name; }

    /** Returns the current y axis name */
    public String getYAxisName(){ return yAxisName; }
    /** Sets the y axis name */
    public void setYAxisName(String name){ this.yAxisName = name; }

    /** Combo Name of the X and Y axis, used for determining if two DiscretizedFunction2DAPI */
    public String getXYAxesName(){ return xAxisName + ',' + yAxisName; }

    /** Returns name/value pairs, separated with commas, as one string, usefule for legends, etc. */
    public String getParametersString(){ return list.toString(); }

    /** Returns true if two DefaultXYDiscretizedFunction2D have the same independent parameters */
    public boolean sameParameters(ArbDiscrFuncWithParamsAPI function){
        String str1 = function.getParametersString();
        String str2 = getParametersString();
        if( str1.equals( str2 ) ) return true;
        else return false;
    }



    /** Returns a copy of this and all points in this DiscretizedFunction */
    public Object clone(){

        ArbDiscrFuncWithParams function = new ArbDiscrFuncWithParams();
        function.setTolerance( this.getTolerance() );
        function.setXAxisName( this.xAxisName );
        function.setYAxisName( this.yAxisName );
        function.setParameterList( (ParameterList)this.getParameterList().clone() );

        Iterator it = this.getPointsIterator();
        while(it.hasNext()) {

            DataPoint2D point = (DataPoint2D)it.next();
            DataPoint2D point2 = (DataPoint2D)point.clone();

            function.set( point2 );
        }

        return function;

    }

    /**
     * Determines if two functions are the same with respect to the parameters that
     * were used to calculate the function, NOT THAT EACH POINT IS THE SAME. This is used
     * by the DiscretizedFunction2DAPIList to determine if it should add a new function
     * to the list.
     */
    public boolean equals(ArbDiscrFuncWithParamsAPI function){
        String S = C + ": equals():";

        if( !function.getXYAxesName().equals( this.getXYAxesName() ) ) return false;

        return function.getParameterList().equals( this.list );
    }


    /**
     * Returns all the parameters associated with the function as one string with
     * no new lines, of the format:<P>
     * name = value, name2 = value2, etc.
     */
    public String toString(){ return this.list.toString(); }

}
