 package org.scec.sha.imr.attenRelImpl.calc;

/**
 * <p>Title: Wald_MMI_Calc </p>
 * <b>Description:</b> This computes MMI (from PGA and PGV) using the relationship given by
 * Wald et al. (1999, Earthquake Spectra).  The code is a modified version
 * of what Bruce Worden sent me (Ned) on 12/04/03.
 *
 * @author Ned Field
 * @created    May, 2004
 * @version 1.0
 */

public final class Wald_MMI_Calc {

  static double sma     =  3.6598;
  static double ba      = -1.6582;
  static double sma_low =  2.1987;
  static double ba_low  =  1;

  static double smv     =  3.4709;
  static double bv      =  2.3478;
  static double smv_low =  2.0951;
  static double bv_low  =  3.3991;

  /**
   *
   * @param pga - peak ground acceleration (g)
   * @param pgv - peak ground velocity (cm/sec)
   * @return
   */
  public static double getMMI(double pga, double pgv){
    String S = ".getMMI()";

    // Convert pga to gals as needed below
    pga *= 980.0;

    double a_scale, v_scale;
    double ammi; // Intensity from acceleration
    double vmmi; // Intensity from velocity

    ammi = (0.43429*Math.log(pga) * sma) + ba;
    if (ammi <= 5.0)
      ammi = (0.43429*Math.log(pga) * sma_low) + ba_low;

    vmmi = (0.43429*Math.log(pgv) * smv) + bv;
    if (vmmi <= 5.0)
      vmmi = (0.43429*Math.log(pgv) * smv_low) + bv_low;

    if (ammi < 1) ammi = 1;
    if (vmmi < 1) vmmi = 1;

    // use linear ramp between MMI 5 & 7 (ammi below and vmmi above, respectively)
    a_scale = (ammi - 5) / 2; // ramp
    if (a_scale > 1);
      a_scale = 1;
    if (a_scale < 0);
      a_scale = 0;

    a_scale = 1 - a_scale;

    v_scale = 1 - a_scale;

    double mmi = (a_scale * ammi) + (v_scale * vmmi);
    if (mmi < 1) mmi = 1 ;
    if (mmi > 10) mmi = 10;
//      return ((int) (mmi * 100)) / 100;
    return mmi;
    }
}