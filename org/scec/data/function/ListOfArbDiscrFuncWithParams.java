package org.scec.data.function;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Vector;

import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.DiscretizedFunction2DException;
import org.scec.data.DataPoint2D;
import org.scec.gui.plot.jfreechart.*;
//import org.scec.sha.surface.plot.PSHAChartDataModel;

// import de.progra.charting.model.*;
// import de.progra.charting.*;
// import de.progra.charting.swing.ChartPanel;
// import de.progra.charting.render.*;


/**
 * <b>Title:</b> XYDiscretizedFunction2DList<br>
 * <b>Description:</b> List container for Discretized Functions. This list only allows
 * functions with the same x and y axis names, and the same number of elements. This class
 * is useful for plotting multiple functions in the same plot that only differ by their
 * independent parameters
 * <br>

 * <p>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class ListOfArbDiscrFuncWithParams extends DiscretizedFunctionXYDataSet{



    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "ArbDiscrFunction2DWithParamsList";
    protected final static boolean D = false;

    /** The number of data points allowed in function sets - not sure if we need this */
    protected int num;

    /** The X-Axis name */
    protected String xAxisName;

    /** The Y-Axis name */
    protected String yAxisName;

    protected boolean emptyList = true;
    private boolean yLog = false;
    private boolean xLog = false;



    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    /** no arg constructor */
    public ListOfArbDiscrFuncWithParams() { super(); }



    /* ****************************/
    /** @todo  Accessors, Setters */
    /* ****************************/

    public String getXAxisName(){ return xAxisName; }
    public void setXAxisName(String name){ this.xAxisName = name; }

    public String getYAxisName(){ return yAxisName; }
    public void setYAxisName(String name){ this.yAxisName = name; }

    public int getNumberDataPoints(){ return num; }


    /** Combo Name of the X and Y axis, used for determining if tow DiscretizedFunction2DAPI */
    public String getXYAxesName(){ return xAxisName + ',' + yAxisName; }


    /** adds the DiscretizedFunction2D if it doesn't exist, else throws exception */
    public void addArbDiscrFunction2DWithParams(ArbDiscrFuncWithParamsAPI function) throws DiscretizedFunction2DException{

        String S = C + ": addXYDiscretizedFunction2D(): ";

        if( !containsArbDiscrFunction2DWithParams(function) ){
            if( functionFitsProfile(function) ) {
                functions.add( function );
                if( this.size() > 0) emptyList = false;
            }
            else{ throw new DiscretizedFunction2DException(S + "This ArbDiscrFunction2DWithParamsAPI doesn't have the same x or y axis name, or same number of points."); }
        }
        else{ throw new DiscretizedFunction2DException(S + "This ArbDiscrFunction2DWithParamsAPI already exists."); }

    }


    /**
     * Returns true if function has same x and y axis as this list, and the
     * same length as the rest of the functions in the list.
     */
    public boolean functionFitsProfile(ArbDiscrFuncWithParamsAPI function){

        if(emptyList){

            this.xAxisName = function.getXAxisName();
            this.yAxisName = function.getYAxisName();
            this.num = function.getNum();

            return true;
        }

        if( ! this.xAxisName.equals( function.getXAxisName() ) ) return false;
        if( ! this.yAxisName.equals( function.getYAxisName() ) ) return false;
        if( ! ( this.num == function.getNum() ) ) return false;

        return true;

    }


    /** removes all DiscretizedFunction2Ds from the list, making it empty, ready for
     *  new DiscretizedFunction2Ds
     */
    public void clear(){ functions.clear(); emptyList = true; }

    /**
     * Returns a copy of this list, therefore any changes to the copy
     * cannot affect this original list.
     */
    public Object clone(){

        ListOfArbDiscrFuncWithParams list1 = new ListOfArbDiscrFuncWithParams();

        list1.xAxisName = this.xAxisName;
        list1.yAxisName = this.yAxisName;
        list1.num = this.num;
        list1.emptyList = this.emptyList;

        ListIterator it = this.getDiscretizedFunction2DsIterator();
        while(it.hasNext()){
            // This list's parameter
            ArbDiscrFuncWithParamsAPI function1 = (ArbDiscrFuncWithParamsAPI)((ArbDiscrFuncWithParamsAPI)it.next()).clone();
            list1.addArbDiscrFunction2DWithParams( function1 );
        }

        return list1;

    }

    /**
     * Returns true if all the DisctetizedFunctions in this list are equal.
     */
    public boolean equals(ListOfArbDiscrFuncWithParams list){

        // Not same size, can't be equal
        if( this.size() != list.size() ) return false;
        if( ! this.xAxisName.equals( list.xAxisName ) ) return false;
        if( ! this.yAxisName.equals( list.yAxisName ) ) return false;
        if( ! ( this.num == list.num ) ) return false;

        // Check each individual Parameter
        ListIterator it = this.getDiscretizedFunction2DsIterator();
        while(it.hasNext()){

            // This list's parameter
            ArbDiscrFuncWithParamsAPI function1 = (ArbDiscrFuncWithParamsAPI)it.next();

            // List may not contain parameter with this list's parameter name
            if ( !list.containsArbDiscrFunction2DWithParams( function1 ) ) return false;

        }

        // Passed all tests - return true
        return true;

    }

    /**
     * Returns all datapoints in a matrix, x values in first column,
     * first functions y vaules in second, second function's y values
     * in the third, etc. This function should be optimized by actually accessing
     * the underlying TreeMap.
     */
    public String toString(){

        String S = C + ": toString(): ";

        StringBuffer b = new StringBuffer();
        b.append("Discretized Function Data Points\n");
        b.append("X-Axis: " + this.xAxisName + '\n');
        b.append("Y-Axis: " + this.yAxisName + '\n');
        b.append("Number of Points: " + this.num + '\n');
        b.append("Number of Data Sets: " + this.size() + '\n');


        StringBuffer b2 = new StringBuffer();

        final int numPoints = this.num;
        final int numSets = this.size();

        Number[][] model = new Number[numSets][numPoints];
        double[] xx = new double[numPoints];
        String[] rows = new String[numPoints];

        ListIterator it1 = this.getDiscretizedFunction2DsIterator();
        boolean first = true;
        int counter = 0;
        b.append("\nColumn " + (counter + 1) + ". X-Axis" );
        while( it1.hasNext() ){

            ArbDiscrFuncWithParamsAPI function = (ArbDiscrFuncWithParamsAPI)it1.next();
            b.append("\nColumn " + (counter + 2) + ". Y-Axis: " + function.toString());

            Iterator it = function.getPointsIterator();

            // Conversion
            if(D) System.out.println(S + "Converting Function to 2D Array");
            int i = 0;
            while(it.hasNext()){

                DataPoint2D point = (DataPoint2D)it.next();

                if(first){
                    double x1 = point.getX().doubleValue();
                    xx[i] = x1;
                }

                Double y1 = point.getY();
                model[counter][i] = y1;

                i++;
            }
            rows[counter] = function.getXYAxesName();
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


    /*
    public DefaultChartDataModel getDefaultChartDataModel(){

        String S = C + ": getDefaultChartDataModel(): ";
        // Variables
        final int numPoints = this.num;
        final int numSets = this.size();
        Number[][] model = new Number[numSets][numPoints];
        double[] xx = new double[numPoints];
        String[] rows = new String[numPoints];

        ListIterator it1 = this.getDiscretizedFunction2DsIterator();
        boolean first = true;
        int counter = 0;
        while( it1.hasNext() ){

            DiscretizedFunction2DAPI function = (DiscretizedFunction2DAPI)it1.next();
            Iterator it = function.getPointsIterator();

            // Conversion
            if(D) System.out.println(S + "Converting Function to 2D Array");
            int i = 0;
            while(it.hasNext()){

                DataPoint2D point = (DataPoint2D)it.next();

                if(first){
                    double x1 = point.getX().doubleValue();
                    xx[i] = x1;
                }

                Double y1 = point.getY();
                model[counter][i] = y1;

                i++;
            }
            rows[counter] = function.getXYAxesName();
            if(first) first = false;
            counter++;

        }

        // Make DefaultChartDataModel
        if(D) System.out.println(S + "Creating DefaultChartDataModel");
        DefaultChartDataModel data = new PSHAChartDataModel(model, xx, rows);

        return data;
    }
    */


    /* ****************************/
    /** @todo  Accessors, Setters */
    /* ****************************/

    /**
     * Adds all DiscretizedFunction2Ds of the DiscretizedFunction2Dlist to this one, if the
     * named DiscretizedFunction2D is not already in the list
     */
    public void addArbDiscrFunction2DWithParamsList(ListOfArbDiscrFuncWithParams list2) throws DiscretizedFunction2DException{

        ListIterator it = list2.getDiscretizedFunction2DsIterator();
        while( it.hasNext() ){
            ArbDiscrFuncWithParamsAPI function = (ArbDiscrFuncWithParamsAPI)it.next();
            if( !containsArbDiscrFunction2DWithParams(function) ){ this.addArbDiscrFunction2DWithParams(function); }
        }

    }



    /**
     * checks if the DiscretizedFunction2D exists in the list. Loops through
     * each function in the list and calls function.equals( function2 )
     * against the passed in function to determine if this function exists.
     */
    public boolean containsArbDiscrFunction2DWithParams(ArbDiscrFuncWithParamsAPI function){

        ListIterator it = getDiscretizedFunction2DsIterator();

        while(it.hasNext() ){
            ArbDiscrFuncWithParamsAPI function1 = (ArbDiscrFuncWithParamsAPI)it.next();
            //String params1 = function1.getParameterList().get
            if( function1.sameParameters( function ) ) return true;
        }
        return false;

    }


    /** removes DiscretizedFunction2D if it exists, else
     *  throws exception
     */
    public void removeArbDiscrFunction2DWithParams(ArbDiscrFuncWithParamsAPI function) throws DiscretizedFunction2DException {

        ListIterator it = getDiscretizedFunction2DsIterator();

        while(it.hasNext() ){
            ArbDiscrFuncWithParamsAPI function1 = (ArbDiscrFuncWithParamsAPI)it.next();
            if( function1.equals( function ) ) {
                functions.remove(function1);
                return;
            }
        }

        String S = C + ": removeArbDiscrFunction2DWithParams(): ";
        throw new DiscretizedFunction2DException(S + "The specified DiscretizedFunction2D exist.");

    }

    /**
     * updates an existing DiscretizedFunction2D with the new value,
     * throws exception if DiscretizedFunction2D doesn't exist
     */
    public void updateArbDiscrFunction2DWithParams(ArbDiscrFuncWithParamsAPI function) throws DiscretizedFunction2DException {
        removeArbDiscrFunction2DWithParams(function);
        addArbDiscrFunction2DWithParams(function);
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
     * Returns the name of a series.
     * @param series The series (zero-based index).
     */
    public String getSeriesName(int series)
    {
        if( series < functions.size() ){
            String str = ( (ArbDiscrFuncWithParamsAPI)this.functions.get(series) ).getParametersString();
            return str;
        }
        else return "";
    }

    public boolean isYLog() { return yLog; }
    public void setYLog(boolean yLog) {

        if( yLog != this.yLog ) {
            this.yLog = yLog;

            ListIterator it = this.getDiscretizedFunction2DsIterator();
            while( it.hasNext() ){

                ArbDiscrFuncWithParamsAPI function = (ArbDiscrFuncWithParamsAPI)it.next();
                function.setYLog(yLog);
                this.num = function.getNum();
            }
        }
    }

    public boolean isXLog() { return xLog; }
    public void setXLog(boolean xLog) {
        if( xLog != this.xLog ) {
            this.xLog = xLog;

            ListIterator it = this.getDiscretizedFunction2DsIterator();
            while( it.hasNext() ){

                ArbDiscrFuncWithParamsAPI function = (ArbDiscrFuncWithParamsAPI)it.next();
                function.setXLog(xLog);
                this.num = function.getNum();
            }

        }
    }


}
