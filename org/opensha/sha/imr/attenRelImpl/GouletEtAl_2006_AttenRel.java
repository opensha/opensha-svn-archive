package org.opensha.sha.imr.attenRelImpl;

import org.opensha.param.event.ParameterChangeWarningListener;

public class GouletEtAl_2006_AttenRel extends BC_2004_AttenRel {

	
	
   public final static String NAME = "Goulet Et. Al. (2006)";
   public final static String SHORT_NAME = "GouletEtAl2006";
   private static final long serialVersionUID = 1234567890987654364L;

	
  public GouletEtAl_2006_AttenRel(ParameterChangeWarningListener warningListener) {
	  super(warningListener);
  }
  
  /**
   * Returns the Std Dev.
   */
  public double getStdDev(){
	  
	  String stdDevType = stdDevTypeParam.getValue().toString();
	  if (stdDevType.equals(STD_DEV_TYPE_NONE)) { // "None (zero)"
		  return 0;
	  }
	  updateCoefficients();
	  return getStdDevForGoulet();
  }
  
  
  /**
   * @return    The stdDev value for Goulet (2006) Site Correction Model
   */
  private double getStdDevForGoulet(){
	  double bVal = ((Double)AF_SlopeParam.getValue()).doubleValue();
	  double cVal = ((Double)this.AF_AddRefAccParam.getValue()).doubleValue();
	  double stdDevAF = ((Double)this.AF_StdDevParam.getValue()).doubleValue();
	  double tau = coeffs.tau;
	  as_1997_attenRel.setIntensityMeasure(im);
	  double asRockMean = as_1997_attenRel.getMean();
	  double asRockStdDev = as_1997_attenRel.getStdDev();
	  double stdDev = Math.pow((bVal*asRockMean)/(asRockMean+cVal)+1, 2)*
	                  (Math.pow(asRockStdDev,2)-Math.pow(tau, 2))+Math.pow(stdDevAF,2)+Math.pow(tau,2);
	  return Math.sqrt(stdDev);
  }

  
  /**
   * get the name of this IMR
   *
   * @returns the name of this IMR
   */
  public String getName() {
    return NAME;
  }

  /**
   * Returns the Short Name of each AttenuationRelationship
   * @return String
   */
  public String getShortName() {
    return SHORT_NAME;
  }
  
}
