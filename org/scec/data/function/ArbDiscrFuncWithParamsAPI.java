package org.scec.data.function;

import java.util.ListIterator;
import java.util.Iterator;
import org.scec.data.DataPoint2D;
import org.scec.exceptions.DataPoint2DException;
import org.scec.param.ParameterList;

/**
 * <b>Title:</b> ArbDiscrFunction2DWithParamsAPI<br>
 * <b>Description:</b> Interface that all Discretized Functions must implement.
 * A Discretized Function are the x and y points that a function describes. Instead
 * of having y=x^2, you would have a sample of possible x and y values. <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public interface ArbDiscrFuncWithParamsAPI
    extends DiscretizedFuncAPI {

    /** Name of the X-Axis - this class is usually used for plotting */
    public String getXAxisName();

    /** Set the name of the X-Axis */
    public void setXAxisName(String name);

    /** Name of the Y-Axis */
    public String getYAxisName();

    /** Set the name of the Y-Axis  - this class is usually used for plotting */
    public void setYAxisName(String name);

    /** Combo Name of the X and Y axis, used for determining if tow DiscretizedFunction2DAPI */
    public String getXYAxesName();

    /** Returns name/value pairs, separated with commas, as one string, usefule for legends, etc. */
    public String getParametersString();

    /** Returns true if the second function has the same named parameter values,
     *  used to determine if two XYDiscretizedFunction2DAPIs are the same
     */
    public boolean sameParameters(ArbDiscrFuncWithParamsAPI function);

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
