package org.scec.data.function;

import org.scec.exceptions.*;
import org.scec.data.NamedObjectAPI;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Steven W. Rock
 * @version 1.0
 */

public abstract class DiscretizedFunc implements DiscretizedFuncAPI, NamedObjectAPI{

    /**
     * The tolerance allowed in specifying a x-value near a real x-value,
     * so that the real x-value is used. Note that the tolerance must be smaller
     * than 1/2 the delta between data points.
     */
    protected Double tolerance = new Double(0);

    /** Information about this function, will be used in making the legend from
     *  a parameter list of variables
     */
    protected String info = "";

    /** The X-Axis name */
    protected String xAxisName = "";

    /** The Y-Axis name */
    protected String yAxisName = "";

    protected boolean yLog = false;
    protected boolean xLog = false;



    public String getXAxisName(){ return xAxisName; }
    public void setXAxisName(String name){ this.xAxisName = name; }

    public String getYAxisName(){ return yAxisName; }
    public void setYAxisName(String name){ this.yAxisName = name; }

    /** Combo Name of the X and Y axis, used for determining if tow DiscretizedFunction2DAPI */
    public String getXYAxesName(){ return xAxisName + ',' + yAxisName; }

    public String getInfo(){ return info; }
    public void setInfo(String info){ this.info = info; }

    public boolean isYLog() { return yLog; }
    public void setYLog(boolean yLog) { if( yLog != this.yLog ) { this.yLog = yLog; } }

    public boolean isXLog() { return xLog; }
    public void setXLog(boolean xLog) { if( xLog != this.xLog ) { this.xLog = xLog; } }

    public Double getTolerance() { return tolerance; }
    public void setTolerance(Double newTolerance) throws InvalidRangeException {

        if( newTolerance.doubleValue() < 0 )
            throw new InvalidRangeException("Tolerance must be larger or equal to 0");
        tolerance = newTolerance;
    }

    //public int getNumberDataPoints(){ return num; }


}