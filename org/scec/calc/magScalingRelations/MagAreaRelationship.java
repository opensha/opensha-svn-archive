package org.scec.calc.magScalingRelations;

//double check what's needed
import org.scec.util.FaultUtils;
import org.scec.exceptions.InvalidRangeException;
import org.scec.data.*;

/**
 * <b>Title:</b>MagAreaRelationship<br>
 *
 * <b>Description:  This is an abstract class that gives the median and standard
 * deviation of magnitude as a function of area (km-squared) or visa versa.  The
 * values can also be a function of rake</b>  <p>
 *
 * @author Edward H. Field
 * @version 1.0
 */

public abstract class MagAreaRelationship extends MagScalingRelationship {

    final static String C = "MagAreaRelationship";

    /**
     * Computes the median magnitude from rupture area
     * @param area in km-squared
     * @return median magnitude
     */
    public abstract double getMedianMag(double area);

    /**
     * Computes the median magnitude from rupture area & rake
     * @param area in km-squared
     * @param rake in degrees
     * @return median magnitude
     */
    public double getMedianMag(double area, double rake) {
      setRake(rake);
      return getMedianMag(area);
    }

    /**
     * Gives the standard deviation for the magnitude as a function of area
     * @param area in km-squared
     * @return standard deviation
     */
    public abstract double getMagStdDev(double area);

    /**
     * Gives the standard deviation for the magnitude as a function of area & rake
     * @param area in km-squared
     * @param rake in degrees
     * @return standard deviation
     */
    public double getMagStdDev(double area, double rake) {
      setRake(rake);
      return getMagStdDev(area);
    }

    /**
     * Computes the median rupture area from magnitude
     * @param mag - moment magnitude
     * @return median area in km-squared
     */
    public abstract double getMedianArea(double mag);

    /**
     * Computes the median rupture area from magnitude & rake
     * @param mag - moment magnitude
     * @param rake in degrees
     * @return median area in km-squared
     */
    public double getMedianArea(double mag, double rake) {
      setRake(rake);
      return getMedianArea(mag);
    }


    public abstract double getMedianAreaStdDev(double mag);

    public double getMedianAreaStdDev(double mag, double rake) {
      setRake(rake);
      return getMedianAreaStdDev(mag);
    }



}
