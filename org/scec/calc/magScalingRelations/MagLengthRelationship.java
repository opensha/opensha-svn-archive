package org.scec.calc.magScalingRelations;

//double check what's needed
import org.scec.util.FaultUtils;
import org.scec.exceptions.InvalidRangeException;
import org.scec.data.*;

/**
 * <b>Title:</b>MagLengthRelationship<br>
 *
 * <b>Description:  This is an abstract class that gives the mean and standard
 * deviation of magnitude as a function of length (km-squared) or visa versa.  The
 * values can also be a function of rake</b>  <p>
 *
 * @author Edward H. Field
 * @version 1.0
 */

public abstract class MagLengthRelationship extends MagScalingRelationship {

    final static String C = "MagLengthRelationship";

    public abstract double getMeanMag(double length);

    public double getMeanMag(double length, double rake) {
      setRake(rake);
      getMeanMag(length);
    }

    public abstract double getMagStdDev(double length);

    public double getMagStdDev(double length, double rake) {
      setRake(rake);
      getMagStdDev(length);
    }

    public abstract double getMeanLength(double mag);

    public double getMeanLength(double mag, double rake) {
      setRake(rake);
      getMeanLength(mag);
    }

    public abstract double getMeanLengthStdDev(double mag);

    public double getMeanLengthStdDev(double mag, double rake) {
      setRake(rake);
      getMeanLengthStdDev(mag);
    }



}
