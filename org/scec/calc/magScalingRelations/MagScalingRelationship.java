package org.scec.calc.magScalingRelations;

//double check what's needed
import org.scec.util.FaultUtils;
import org.scec.exceptions.InvalidRangeException;
import org.scec.data.*;

/**
 * <b>Title:</b>MagScalingRelationship<br>
 *
 * <b>Description:  This is an abstract class that gives the mean and standard
 * deviation of magnitude as a function of some scalar value (or the mean and
 * standard deviation of the scalar value as a function of magnitude).  The values
 * can also be a function of rake</b>  <p>
 *
 * @author Edward H. Field
 * @version 1.0
 */

public abstract class MagScalingRelationship implements NamedObjectAPI  {

    final static String C = "MagScalingRelationship";

    /**
     * The rupture rake.  The default is Double.NaN
     */
    protected double rake = Double.NaN;

    public abstract double getMeanMag(double scale);

    public double getMeanMag(double scale, double rake) {
      setRake(rake);
      return getMeanMag(scale);
    }

    public abstract double getMagStdDev(double scale);

    public double getMagStdDev(double scale, double rake) {
      setRake(rake);
      return getMagStdDev(scale);
    }

    public abstract double getMeanScale(double mag);

    public double getScale(double mag, double rake) {
      setRake(rake);
      return getMeanScale(mag);
    }

    public abstract double getMeanScaleStdDev(double mag);

    public double getMeanScaleStdDev(double mag, double rake) {
      setRake(rake);
      return getMeanScaleStdDev(mag);
    }

    public void setRake(double rake) {
      FaultUtils.assertValidRake(rake);
      this.rake = rake;
    }



    /**
     * Returns the name of the object
     *
     */
    public String getName() {
      return C;
    }
}
