package org.scec.calc.magScalingRelations;

//double check what's needed
import org.scec.util.FaultUtils;
import org.scec.exceptions.InvalidRangeException;
import org.scec.data.*;

/**
 * <b>Title:</b>MagAreaRelationship<br>
 *
 * <b>Description:  This is an abstract class that gives the mean and standard
 * deviation of magnitude as a function of area (km-squared) or visa versa.  The
 * values can also be a function of rake</b>  <p>
 *
 * @author Edward H. Field
 * @version 1.0
 */

public abstract class MagAreaRelationship extends MagScalingRelationship {

    final static String C = "MagAreaRelationship";

    public abstract double getMeanMag(double area);

    public double getMeanMag(double area, double rake) {
      setRake(rake);
      return getMeanMag(area);
    }

    public abstract double getMagStdDev(double area);

    public double getMagStdDev(double area, double rake) {
      setRake(rake);
      return getMagStdDev(area);
    }

    public abstract double getMeanArea(double mag);

    public double getMeanArea(double mag, double rake) {
      setRake(rake);
      return getMeanArea(mag);
    }

    public abstract double getMeanAreaStdDev(double mag);

    public double getMeanAreaStdDev(double mag, double rake) {
      setRake(rake);
      return getMeanAreaStdDev(mag);
    }



}
