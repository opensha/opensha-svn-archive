package org.scec.calc.magScalingRelations;

//double check what's needed
import org.scec.util.FaultUtils;
import org.scec.exceptions.InvalidRangeException;
import org.scec.data.*;

/**
 * <b>Title:</b>MagScalingRelationship<br>
 *
 * <b>Description:  This is an abstract class that gives the median and standard
 * deviation of magnitude as a function of some scalar value (or the median and
 * standard deviation of the scalar value as a function of magnitude).  The values
 * can also be a function of rake</b>  <p>
 *
 * @author Edward H. Field
 * @version 1.0
 */

public abstract class MagScalingRelationship implements NamedObjectAPI  {

    final static String C = "MagScalingRelationship";

    /**
     * The rupture rake in degrees.  The default is Double.NaN
     */
    protected double rake = Double.NaN;

    public abstract double getMedianMag(double scale);

    public double getMedianMag(double scale, double rake) {
      setRake(rake);
      return getMedianMag(scale);
    }

    public abstract double getMagStdDev(double scale);

    public double getMagStdDev(double scale, double rake) {
      setRake(rake);
      return getMagStdDev(scale);
    }

    public abstract double getMedianScale(double mag);

    public double getScale(double mag, double rake) {
      setRake(rake);
      return getMedianScale(mag);
    }

    public abstract double getMedianScaleStdDev(double mag);

    public double getMedianScaleStdDev(double mag, double rake) {
      setRake(rake);
      return getMedianScaleStdDev(mag);
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
