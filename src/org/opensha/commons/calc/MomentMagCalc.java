package org.opensha.commons.calc;

/**
 * <p>Title: MomentMagCalc </p>
 * <p>Description: This is a utility to calculate moment (in SI units: Newton-Meters) for a given magnitude or vice versa</p>
 *
 * @author Vipin Gupta
 * @created    August 7, 2002
 * @version 1.0
 */

public final class MomentMagCalc {

 /**
  * This function calculates the moment for the given magnitude
  * @param mag: Magnitude
  * @returns Moment in Newton-Meters
  */
 public static double getMoment(double mag) {
    return (Math.pow(10,1.5*mag+9.05));
 }

 /**
  * This function calculates the magnitude for the given moment
  * @param moment : Moment in Newton-Meters
  * @returns magnitude for the given moment
  */
 public static double getMag(double moment) {
   return (Math.log(moment)/Math.log(10)-9.05)/1.5;
 }

}
