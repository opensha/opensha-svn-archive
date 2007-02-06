package org.opensha.util;

import java.util.*;
import java.text.*;
import org.opensha.sha.surface.*;

import org.opensha.calc.RelativeLocation;
import org.opensha.sha.fault.*;
import org.opensha.exceptions.*;


import org.opensha.param.*;
import org.opensha.data.function.*;
import org.opensha.data.*;
import org.opensha.gui.plot.jfreechart.*;

/**
 * <b>Title:</b> FaultUtils<p>
 *
 * <b>Description:</b> Collection of static utilities used in conjunction with
 * strike, dip and rake angles of faults. These functions are assertion functions,
 * in that they validate the angles as valid strike, dip, and rake angles, and
 * return true or false if valid. <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public final class FaultUtils {

    /** Class name used for debug strings */
    protected final static String C = "FaultUtils";
    /** boolean that indicates if print out debug statements */
    protected final static boolean D = false;

    /** debugging string */
    private static final String S1 = C + ": assertValidStrike(): ";
    /** debugging string */
    private static final String S2 = C + ": assertValidDip(): ";
    /** debugging string */
    private static final String S3 = C + ": assertValidRake(): ";
    private final static double TOLERANCE = 1e-6;


    /**
     * Checks that the strike angle fits within the definition<p>
     * <code>0 <= strike <= 360</code><p>
     * @param strike                    Angle to validate
     * @throws InvalidRangeException    Thrown if not valid angle
     */
    public static void assertValidStrike( double strike)
        throws InvalidRangeException
    {

        if( strike < 0 ) throw new InvalidRangeException( S1 +
            "Strike angle cannot be less than zero (value = "+ strike+")");
        if( strike > 360 ) throw new InvalidRangeException( S1 +
            "Strike angle cannot be greater than 360 (value = "+ strike+")");
    }


    /**
     * Checks that the dip angle fits within the definition<p>
     * <code>0 <= dip <= 90</code><p>
     * @param dip                       Angle to validate
     * @throws InvalidRangeException    Thrown if not valid angle
     */
    public static void assertValidDip( double dip)
        throws InvalidRangeException
    {
   
        if( dip < 0 ) throw new InvalidRangeException( S2 +
            "Dip angle cannot be less than zero"
        );
        if( dip-90 > TOLERANCE ) throw new InvalidRangeException( S2 +
            "Dip angle cannot be greater than 90"
        );
    }


     /**
     * This makes sure that a depth on the fault is a positive number<p>
     * @param depth
     * @throws InvalidRangeException
     */
    public static void assertValidDepth( double depth)
        throws InvalidRangeException
    {
        if( !(depth >= 0) ) throw new InvalidRangeException( S2 +
            "Depth on fault must be positive" );
    }


    /**
     * This makes sure that a depth on the fault is a positive number<p>
     * @param depth
     * @throws InvalidRangeException
     */
    public static void assertValidSeisUpperAndLower(double upperSeis, double lowerSeis)
        throws InvalidRangeException {

        assertValidDepth(upperSeis);
        assertValidDepth(lowerSeis);
        if( upperSeis > lowerSeis ) throw new InvalidRangeException( S2 +
            "upperSeisDepth must be < lowerSeisDepth" );
    }
    /**
     * Checks that the rake angle fits within the definition<p>
     * <code>-180 <= rake <= 180</code><p>
     * @param rake                      Angle to validate
     * @throws InvalidRangeException    Thrown if not valid angle
     */
    public static void assertValidRake( double rake)
        throws InvalidRangeException
    {
    	if(Math.abs(rake)-180 > TOLERANCE)
    		throw new InvalidRangeException( S3 +
    		"Strike angle should be between -180 and 180");
       /* if( rake < -180 ) throw new InvalidRangeException( S3 +
            "Rake angle cannot be less than -180"
        );
        if( rake > 180 ) throw new InvalidRangeException( S3 +
            "Rake angle cannot be greater than 180"
        );*/
    }

}
