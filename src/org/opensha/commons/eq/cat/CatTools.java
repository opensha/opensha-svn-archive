package org.opensha.commons.eq.cat;

import org.opensha.commons.util.DataUtils;

/**
 * Catalog utilities.
 * 
 * @author Peter Powers
 * @version $Id: CatTools.java 117 2010-10-05 22:10:39Z pmpowers $
 */
public class CatTools {
    
    /** Minimum earthquake magnitude value (-2) used for range checking. */
    public static final double MAG_MIN = -2.0;
    
    /** Maximum earthquake magnitude value (10) used for range checking. */
    public static final double MAG_MAX = 10.0;

    /**
     * Verifies that a set of magnitude values fall within range of 
     * <code>MAG_MIN</code> and <code>MAG_MAX</code> (inclusive).
     * 
     * @param mags magnitudes to validate
     * @throws IllegalArgumentException if a data value is out of range
     */
    public final static void validateMags(double[] mags) {
    	DataUtils.validate(mags, MAG_MIN, MAG_MAX);
    }

    /**
     * Verifies that a magnitude value falls within range of 
     * <code>MAG_MIN</code> and <code>MAG_MAX</code> (inclusive).
     * 
     * @param mag magnitude to validate
     * @throws IllegalArgumentException if data value is out of range
     */
    public final static void validateMag(double mag) {
    	DataUtils.validate(mag, MAG_MIN, MAG_MAX);
    }

}
