package org.opensha.calc.magScalingRelations.magScalingRelImpl;

import org.opensha.calc.magScalingRelations.*;

/**
 * <b>Title:</b>Shaw_2007_MagAreaRel<br>
 *
 * <b>Description:</b>  .<p>
 *
 * @author Edward H. Field
 * @version 1.0
 */

public class Shaw_2007_MagAreaRel extends MagAreaRelationship {

    final static String C = "Shaw_2007_MagAreaRel";
    public final static String NAME = "Shaw (2007)";

    /**
     * Computes the median magnitude from rupture area.
     * @param area in km
     * @return median magnitude
     */
    public double getMedianMag(double area){
    	double alpha=6;
    	double h=15;
    	double numer= Math.max(1,Math.sqrt(area/(h*h)));
    	double denom= (1 + Math.max(1,(area/(alpha*h*h))))/2;
    	return  3.98 + Math.log(area)*lnToLog + 0.667*Math.log(numer/denom)*lnToLog;
    }

    /**
     * Gives the standard deviation for magnitude
     * @return standard deviation
     */
    public double getMagStdDev(){ return Double.NaN;}

    /**
     * Computes the median rupture area from magnitude
     * @param mag - moment magnitude
     * @return median area in km
     */
    public double getMedianArea(double mag){
          return Double.NaN;
   }

    /**
     * This returns NaN because the value is not available
     * @return standard deviation
     */
    public double getAreaStdDev() {return  Double.NaN;}

    /**
     * Returns the name of the object
     *
     */
    public String getName() {
      return NAME;
    }
}

