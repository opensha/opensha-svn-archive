package org.scec.calc.magScalingRelations;

import org.scec.util.FaultUtils;
import org.scec.data.*;

/**
 * <b>Title:</b>WC1994_MagLengthRelationship<br>
 *
 * <b>Description:</b>  This implements the Wells and Coppersmith (1994, Bull.
 * Seism. Soc. Am., pages 974-2002) magnitude versus surface-rupture length relationships.  The
 * values are a function of rake.  Setting the rake to Double.NaN causes their "All"
 * rupture-types to be applied (and this is the default value for rake).  Note that the
 * standard deviation for length as a function of mag is given for log(area) (base-10)
 * not length.  <p>
 *
 * @author Edward H. Field
 * @version 1.0
 */

public class WC1994_MagLengthRelationship extends MagLengthRelationship {

    final static String C = "WC1994_MagLengthRelationship";
    final static String NAME = "W&C 1994 Mag-Length Rel.";


    /**
     * no-argument constructor.  All this does is set the rake to Double.NaN
     * (as the default)
     */
    public WC1994_MagLengthRelationship() {
      this.rake = Double.NaN;
    }


    /**
     * Computes the median magnitude from rupture length (for the previously set or default rake).
     * Note that thier "All" case is applied if rake=Double.NaN
     * @param length in km
     * @return median magnitude
     */
    public double getMedianMag(double length){

      if (rake == Double.NaN)
        // apply the "All" case
        return  5.08 +1.16*Math.log(length)*lnToLog;
      else if (( rake <= 45 && rake >= -45 ) || (rake >= 135 && rake <= -135))
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
     * Gives the standard deviation for the magnitude as a function of length
     *  (for the previously set or default rake). Note that thier "All" case is applied
     * if rake=Double.NaN
     * @param length in km
     * @return standard deviation
     */
    public double getMagStdDev(){
      if (rake == Double.NaN)
        // apply the "All" case
        return  0.28;
      else if (( rake <= 45 && rake >= -45 ) || (rake >= 135 && rake <= -135))
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
     * Computes the median rupture length from magnitude (for the previously set
     * or default rake). Note that thier "All" case is applied if rake=Double.NaN
     * @param mag - moment magnitude
     * @return median length in km
     */
    public double getMedianLength(double mag){
      if  (rake == Double.NaN)
          // their "All" case
          return Math.pow(10.0,-3.22+0.69*mag);
      else if (( rake <= 45 && rake >= -45 ) || (rake >= 135 && rake <= -135))
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
     * Computes the standard deviation of log(length) (base-10) from magnitude
     *  (for the previously set or default rake)
     * @param mag - moment magnitude
     * @param rake in degrees
     * @return standard deviation
     */
    public double getLengthStdDev() {
      if (rake == Double.NaN)
        // apply the "All" case
        return  0.22;
      else if (( rake <= 45 && rake >= -45 ) || (rake >= 135 && rake <= -135))
        // strike slip
        return  0.23;
      else if (rake > 0)
        // thrust/reverse
        return  0.20;
      else
        // normal
        return  0.21;

    }

    /**
     * This overides the parent method to allow a value of Double.NaN (which is used
     * to designate the "All" rupture-types option here).
     * @param rake
     */
    public void setRake(double rake) {
      if(rake != Double.NaN)
        FaultUtils.assertValidRake(rake);
      this.rake = rake;
    }


}
