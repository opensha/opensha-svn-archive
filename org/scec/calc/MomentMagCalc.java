package org.scec.calc;

/**
 * <p>Title: MomentMagCalc </p>
 * <p>Description: This is a utility to calculate moment for a given magnitude or vice versa</p>
 *
 * @author Vipin Gupta
 * @created    August 7, 2002
 * @version 1.0
 */

public final class MomentMagCalc {

 public static double getMoment(double mag) {
    return (Math.pow(10,1.5*mag+9.05));
 }

 /**
  * This function calculates the magnitude for the given moment
  * @param moment : Moment for which magnitude needs to be calculated
  * @returns magnitude for the given moment
  */
 public static double getMag(double moment) {
   return (Math.log(moment)/Math.log(10)-9.05)/1.5;
 }

}