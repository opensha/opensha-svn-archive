package org.scec.data.function;

import java.util.ListIterator;
import java.util.Iterator;
import org.scec.data.DataPoint2D;
import org.scec.exceptions.DataPoint2DException;
import org.scec.param.ParameterList;

/**
 * <b>Title:</b> FuncWithParamsAPI<p>
 * <b>Description:</b> Any function that supports a parameter list should
 * implement this interface. This interface is for Functions that contain
 * parameter lists. These lists are currently used for storing the parameter
 * name and values that went into constructing the list, such as for an IMR
 * exceedence probability.<p>
 *
 * This interface was developed to be used by DiscretizedFuncAPI subclasses.
 * Of course any class that constains a ParameterList can implement this.
 *
 * @author Steven W. Rock
 * @see ParameterList
 * @see ParameterAPI
 * @version 1.0
 */

public interface FuncWithParamsAPI {


    /**
     * Returns name/value pairs of all the parametes in the ParameterList
     * stored in this DiscretizedFunc, separated with commas, as one string, usefule for legends, etc.
     */
    public String getParametersString();

    /**
     * Returns true if the second function has the same named parameter values. One
     * current use is to determine if two XYDiscretizedFunction2DAPIs are the same.
     */
    public boolean equalParameterNamesAndValues(FuncWithParamsAPI function);

    /**
     * Returns true if the second function has the same named parameters in
     * it's list, values may be different. Can be used to say that two lists
     * are different instances of the same function, i.e. same function with
     * different input parameters.
     */
    public boolean equalParameterNames(FuncWithParamsAPI function);

    /**
     * This parameter list is the set of parameters that went into
     * calculation this function. Useful for determining if two
     * data sets are the same, i.e. have the same x/y axis and the same
     * set of independent parameters. <p>
     *
     * Note: Another way to say that two functions are the same is if each DataPoint
     * is the same. That is much more numerically intensive
     * task of comparing each DataPoint2D using equals(). It depends on your
     * business logic how to determine if two lists are the same.
     */
    public ParameterList getParameterList();

    /** Set the Parameter list that went into calculating this function */
    public void setParameterList(ParameterList list);



}
