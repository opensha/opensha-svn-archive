package org.scec.data.function;

import java.util.ListIterator;
import java.util.Iterator;
import org.scec.data.DataPoint2D;
import org.scec.exceptions.DataPoint2DException;
import org.scec.param.ParameterList;

/**
 * <b>Title:</b> FuncWithParamsAPI<br>
 * <b>Description:</b> Any function that supports a parameter list should implement this interface.<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public interface FuncWithParamsAPI {


    /** Returns name/value pairs, separated with commas, as one string, usefule for legends, etc. */
    public String getParametersString();

    /** Returns true if the second function has the same named parameter values,
     *  used to determine if two XYDiscretizedFunction2DAPIs are the same
     */
    public boolean equalParameterNamesAndValues(FuncWithParamsAPI function);

    /** Returns true if the second function has the same named parameters in
     *  it's list, values may be different
     */
    public boolean equalParameterNames(FuncWithParamsAPI function);

    /**
     * This parameter list is the set of parameters that went into
     * calculation this DiscretizedFunction. Useful for determining if two
     * data sets are the same, i.e. have the same x/y axis and the same
     * set of independent parameters. Bypasses the more numerically intensive
     * task of comparing each DataPoint2D of two DiscretizedFunction2D.
     */
    public ParameterList getParameterList();

    /** Set the name of the Y-Axis */
    public void setParameterList(ParameterList list);



}
