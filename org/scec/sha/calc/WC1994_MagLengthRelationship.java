package org.scec.sha.calc;

import org.scec.sha.fault.*;
import org.scec.util.FaultUtils;
import org.scec.exceptions.InvalidRangeException;

/**
 * <b>Title:</b>WC1994_MagLengthRelationship<br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Edward H. Field
 * @version 1.0
 */

public final class WC1994_MagLengthRelationship implements MagLengthRelationshipAPI{

    // to convert natural to log base 10
    private double lnToLog = 0.434294;

    public WC1994_MagLengthRelationship() {

    }

    /**
     * This method is for all fault types (all rakes)
     * @param length in km
     * @return mag
     */
    public double getMeanMag(double length) {

        return  5.08 +1.16*Math.log(length)*lnToLog;
    }

    /**
     * This method is for all fault types (all rakes)
     */
    public double getMagStdev() {
        return 0.28;
    }


    /**
     * This method is for all fault types (all rakes)
     * @param mag
     * @return length in km
     */
    public double getMeanLength(double mag)  {
        return Math.pow(10.0,-3.22+0.69*mag);
    }


    /**
     * This method is for all fault types (all rakes)
     */
    public double getLengthStdev() {
        return 0.22;
    }


    /**
     * This method is for rake dependent values
     * @param length in km
     * @param rake in degrees (-180 to 180)
     * @return mag
     */
    public double getMeanMag(double length, double rake) throws InvalidRangeException {

        FaultUtils.assertValidRake(rake);

        if (( rake <= 45 && rake >= -45 ) || (rake >= 135 && rake <= -135))
            // strike slip
            return  5.16 + 1.12*Math.log(length)*lnToLog;
        else if (rake > 0)
            // thrust/reverse
            return  5.0 + 1.22 * Math.log(length)*lnToLog;
        else
            // normal
            return  4.86 + 1.32*Math.log(length)*lnToLog;
    }

    /**
     * This method is for rake dependent values
     */
    public double getMagStdev(double rake) throws InvalidRangeException {

        FaultUtils.assertValidRake(rake);

        if (( rake <= 45 && rake >= -45 ) || (rake >= 135 && rake <= -135))
            // strike slip
            return  0.28;
        else if (rake > 0)
            // thrust/reverse
            return  0.28;
        else
            // normal
            return  0.34;
    }


    /**
     * This method is for rake dependent values
     * @param mag
     * @param rake in degrees (-180 to 180)
     * @return length in km
     */
    public double getMeanLength(double mag, double rake) throws InvalidRangeException {

        FaultUtils.assertValidRake(rake);

        if (( rake <= 45 && rake >= -45 ) || (rake >= 135 && rake <= -135))
            // strike slip
            return  Math.pow(10.0, -3.55 + 0.74*mag);
        else if (rake > 0)
            // thrust/reverse
            return  Math.pow(10.0, -2.86 + 0.63*mag);
        else
            // normal
            return  Math.pow(10.0, -2.01 + 0.50*mag);
    }


    /**
     * This method is for rake dependent values
     */
    public double getLengthStdev(double rake)throws InvalidRangeException {

        FaultUtils.assertValidRake(rake);

        if (( rake <= 45 && rake >= -45 ) || (rake >= 135 && rake <= -135))
            // strike slip
            return  0.23;
        else if (rake > 0)
            // thrust/reverse
            return  0.20;
        else
            // normal
            return  0.21;
    }
}
