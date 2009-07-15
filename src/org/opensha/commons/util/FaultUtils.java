package org.opensha.commons.util;

import org.opensha.commons.exceptions.InvalidRangeException;

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
            "Dip angle cannot be less than zero; the value is "+dip);
        if( dip > 90 ) throw new InvalidRangeException( S2 +
            "Dip angle cannot be greater than 90; the value is "+dip);
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
   
        if( rake < -180 ) throw new InvalidRangeException( S3 +
            "Rake angle cannot be less than -180"
        );
        if( rake > 180 ) throw new InvalidRangeException( S3 +
            "Rake angle cannot be greater than 180"
        );
    }

}
