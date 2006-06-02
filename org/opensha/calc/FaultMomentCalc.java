package org.opensha.calc;

/**
 * <p>Title: FaultMomentCalc </p>
 * <p>Description: This is a utility to calculate moment (in SI units: Newton-Meters) for
 * given fault information</p>
 *
 * @author Ned Field
 * @created    Dec 2, 2002
 * @version 1.0
 */

public final class FaultMomentCalc {

 /**
  * This function calculates the moment (SI units) for the given fault area and average slip,
  * assuming a shear modulus of 3e10 N-m.  Note that this also computes moment rate
  * if slip rate is given rather than slip.
  * @param area: the fault area (in square Meters)
  * @param slip: the ave slip (in Meters)
  * @returns Moment (in Newton-Meters) or moment rate if slip-rate given.
  */
  public static double getMoment(double area, double slip) {
    return 3e10*slip*area;
  }
  
  /**
   * This function calculates slip for a given fault area and moment, assuming a shear modulus of 3e10 N-m.
   * This also calculates slip rate if moment rate is given
   * 
   * @param area: the fault area (in square Meters)
   * @param moment:(in Newton-Meters) or moment rate 
   * @returns Slip (in meters) or slip rate if moment-rate is given
   */
  public static double getSlip(double area, double moment) {
	  return moment/(area*3e10);
  }

}
