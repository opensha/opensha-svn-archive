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
     * than 1/2 the delta between data points for evenly discretized function, no
     * restriction for arb discretized function, no standard delta.
     */
    protected Double tolerance = new Double(0);

    /** Information about this function, will be used in making the legend from
     *  a parameter list of variables
     */
    protected String info = "";
    protected String name = "";

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getInfo(){ return info; }
    public void setInfo(String info){ this.info = info; }

    public Double getTolerance() { return tolerance; }
    public void setTolerance(Double newTolerance) throws InvalidRangeException {
        if( newTolerance.doubleValue() < 0 )
            throw new InvalidRangeException("Tolerance must be larger or equal to 0");
        tolerance = newTolerance;
    }

    /**
     * Default equals for all Discretized Functions. Determines if two functions
     * are the same by comparing that the name and info are the same. Can
     * be overridded by subclasses for different requirments
     */
    public boolean equals(DiscretizedFuncAPI function){
        if( !getName().equals(function.getName() )  ) return false;
        if( !getInfo().equals(function.getInfo() )  ) return false;
        return true;
    }

}