package org.scec.calc.magScalingRelations;

//double check what's needed
import org.scec.util.FaultUtils;
import org.scec.exceptions.InvalidRangeException;
import org.scec.data.*;

/**
 * <b>Title:</b>MagLengthRelationship<br>
 *
 * <b>Description:  This is an abstract class that gives the median and standard
 * deviation of magnitude as a function of length (km-squared) or visa versa.  The
 * values can also be a function of rake</b>  <p>
 *
 * @author Edward H. Field
 * @version 1.0
 */

public abstract class MagLengthRelationship extends MagScalingRelationship {

    final static String C = "MagLengthRelationship";

    public abstract double getMedianMag(double length);

    public double getMedianMag(double length, double rake) {
      setRake(rake);
      return getMedianMag(length);
    }

    public abstract double getMagStdDev(double length);

    public double getMagStdDev(double length, double rake) {
      setRake(rake);
      return getMagStdDev(length);
    }

    public abstract double getMedianLength(double mag);

    public double getMedianLength(double mag, double rake) {
      setRake(rake);
      return getMedianLength(mag);
    }

    public abstract double getMedianLengthStdDev(double mag);

    public double getMedianLengthStdDev(double mag, double rake) {
      setRake(rake);
      return getMedianLengthStdDev(mag);
    }



}
