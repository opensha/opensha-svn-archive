package org.scec.data.function;

import org.scec.exceptions.*;
import org.scec.data.NamedObjectAPI;


/**
 * <b>Title:</b> DiscretizedFunc<p>
 *
 * <b>Description:</b> Abstract implementation of the DiscretizedFuncAPI. Performs standard
 * simple or default functions so that subclasses don't have to keep reimplementing the
 * same function bodies.<p>
 *
 * A Discretized Function is a collection of x and y values grouped together as
 * the points that describe a function. A discretized form of a function is the
 * only ways computers can represent functions. Instead of having y=x^2, you
 * would have a sample of possible x and y values. <p>
 *
 * The basic functions this abstract class implements are:<br>
 * <ul>
 * <li>get, set Name()
 * <li>get, set, Info()
 * <li>get, set, Tolerance()
 * <li>equals() - returns true if all three fields have the same values.
 * </ul>
 *
 * See the interface documentaion for further explanation of this framework<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public abstract class DiscretizedFunc implements DiscretizedFuncAPI,
    NamedObjectAPI,java.io.Serializable{


    /** Class name used for debbuging */
    protected final static String C = "DiscretizedFunc";
    /** if true print out debugging statements */
    protected final static boolean D = false;


    /**
     * The tolerance allowed in specifying a x-value near a real x-value,
     * so that the real x-value is used. Note that the tolerance must be smaller
     * than 1/2 the delta between data points for evenly discretized function, no
     * restriction for arb discretized function, no standard delta.
     */
    protected double tolerance = 0.0;

    /**
     * Information about this function, will be used in making the legend from
     * a parameter list of variables
     */
    protected String info = "";

    /**
     * Name of the function, useful for differentiation different instances
     * of a function, such as in an array of functions.
     */
    protected String name = "";


    /** Returns the name of this function. */
     public String getName(){ return name; }
    /** Sets the name of this function. */
    public void setName(String name){ this.name = name; }


    /** Returns the info of this function. */
    public String getInfo(){ return info; }
    /** Sets the info string of this function. */
     public void setInfo(String info){ this.info = info; }


    /**Returns the tolerance of this function. */
    public double getTolerance() { return tolerance; }
    /**
     * Sets the tolerance of this function. Throws an InvalidRangeException
     * if the tolerance is less than zero, an illegal value.
     */
     public void setTolerance(double newTolerance) throws InvalidRangeException {
        if( newTolerance < 0 )
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

        if( D ) {
            String S = C + ": equals(): ";
            System.out.println(S + "This info = " + getInfo() );
            System.out.println(S + "New info = " + function.getInfo() );

        }

        if( !getInfo().equals(function.getInfo() )  ) return false;
        return true;
    }

}