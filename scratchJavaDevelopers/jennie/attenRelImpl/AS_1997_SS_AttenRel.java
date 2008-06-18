package scratchJavaDevelopers.jennie.attenRelImpl;
//package org.opensha.sha.imr.attenRelImpl;

import java.text.DecimalFormat;
import java.util.*;

import org.opensha.data.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.imr.*;
import org.opensha.sha.imr.attenRelImpl.*;
import org.opensha.sha.imr.attenRelImpl.AS_1997_AttenRel.AS_1997_AttenRelCoefficients;

/**
 * <b>Title:</b> AS_1997_SiteSpecific_AttenRel<p>
 *
 * <b>Description:</b> This applies a site specific site effect model
 * to the Abrahamson & Silva (1997) rock-site predictions. <p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>saParam - Response Spectral Acceleration
 * <LI>pgaParam - Peak Ground Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>distanceRupParam - closest distance to surface projection of fault
 * <LI>vs30Param - Average 30-meter shear-wave velocity at the site
 * <LI>softSoilParam - To overide Vs30 and apply NEHPR E (see INFO for details)
 * <LI>fltTypeParam - Style of faulting
 * <LI>isOnHangingWallParam - tells if site is directly over the rupture surface
 * <LI>componentParam - Component of shaking (only one)
 * <LI>stdDevTypeParam - The type of standard deviation
 * </UL><p>
 *
 * @author     Jennie Watson-Lamprey
 * @created    june, 2008
 * @version    1.0
 */


public class AS_1997_SS_AttenRel extends AS_1997_AttenRel {

  // debugging stuff:
  private final static String C = "AS_1997_SS_AttenRel";
  private final static boolean D = false;
  public final static String NAME = "Abrahamson and Silva (1997) Site Specific";
  public final static String SHORT_NAME = "AS1997SS";
  private static final long serialVersionUID = 1234567890987654355L;

  private double mag, dist;
  
  //Intercept param
  private DoubleParameter AF_InterceptParam;
  public final static String AF_INTERCEPT_PARAM_NAME = "AF Intercept";
  public final static String AF_INTERCEPT_PARAM_INFO = 
	  "Intercept of the median regression model for the ground response analyses";
  private DoubleConstraint AF_InterceptparamConstraint = new DoubleConstraint(-2,2);
  public final static double AF_INTERCEPT_PARAM_DEFAULT = 0;
  
  //Slope Param
  protected DoubleParameter AF_SlopeParam;
  public final static String AF_SLOPE_PARAM_NAME = "AF Slope";
  public final static String AF_SLOPE_PARAM_INFO = 
	  "Slope of the median regression model for the ground response analyses";
  private DoubleConstraint AF_slopeParamConstraint = new DoubleConstraint(-1,1);
  public final static double AF_SLOPE_PARAM_DEFAULT = 0;
  
  //Additive reference acceleration param
  protected DoubleParameter AF_AddRefAccParam;
  public final static String AF_ADDITIVE_REF_ACCELERATION_PARAM_NAME = "AF Add. Ref. Acceleration";
  public final static String AF_ADDITIVE_REF_ACCELERATION_PARAM_INFO = 
	  "Additive reference acceleration of the median regression model for the ground response " +
	  "analyses. This parameter improves the linear model fit for low Sa(rock) / PGA(rock)" +
	  "values and leads to more relaistic predictons than quadratic models";
  private DoubleConstraint AFaddRefAccParamConstraint = new DoubleConstraint(0,0.5);
  public final static double AF_ADDITIVE_REF_ACCERLATION_DEFAULT = 0.03;
  
  //Mag reference param
  protected DoubleParameter AF_MagParam;
  public final static String AF_MagPARAM_NAME = "AF Magnitude";
  public final static String AF_MagPARAM_INFO = 
	  "Slope of the regression for magnitude";
  private DoubleConstraint AFMagParamConstraint = new DoubleConstraint(-4,4);
  public final static double AF_MagParam_DEFAULT = 0.0;
  
  
  //Std. Dev AF param
  protected DoubleParameter AF_StdDevParam;
  public final static String AF_STD_DEV_PARAM_NAME = "Std. Dev. AF";
  public final static String AF_STD_DEV_PARAM_INFO = 
	  "Standard Deviation of the amplification factor from the ground response analyses" +
	  " regression model";
  private DoubleConstraint AF_StdDevParamConstraint = new DoubleConstraint(0,1.0);
  public final static double AF_STD_DEV_DEFAULT = 0.3;
  
   
  /**
   *  Hashtable of coefficients for the supported intensityMeasures
   */
// protected Hashtable horzCoeffs = new Hashtable();

  // for issuing warnings:
//  private transient ParameterChangeWarningListener warningListener = null;

   public AS_1997_SS_AttenRel(ParameterChangeWarningListener warningListener) {

    super(warningListener);

    initCoefficients();
    initSupportedIntensityMeasureParams();
    initEqkRuptureParams();
    initPropagationEffectParams();
    initSiteParams();		// do only in constructor
    initOtherParams();

    initIndependentParamLists(); // Do this after the above
  }
  




  /**
   *  This sets the site-related parameter (vs30Param) based on what is in
   *  the Site object passed in.  WarningExceptions are ingored
   *
   * @param  site             The new site value which contains a Vs30 Parameter
   * @throws ParameterException Thrown if the Site object doesn't contain a
   * Vs30 parameter
   */
  public void setSite(Site site) throws ParameterException {

	    this.site = site;
	  
	  AF_InterceptParam.setValue(site.getParameter(AF_INTERCEPT_PARAM_NAME).getValue());
	  AF_AddRefAccParam.setValue(site.getParameter(AF_ADDITIVE_REF_ACCELERATION_PARAM_NAME).getValue());
	  AF_SlopeParam.setValue(site.getParameter(AF_SLOPE_PARAM_NAME).getValue());
	  AF_MagParam.setValue(site.getParameter(AF_MagPARAM_NAME).getValue());
	  AF_StdDevParam.setValue(site.getParameter(AF_STD_DEV_PARAM_NAME).getValue());  

  }


  
  
  
  /**
   * Calculates the mean
   * @return    The mean value
   */
  public double getMean() throws IMRException {
	  
//	    double mag, dist, mean;
	    String fltType, isHW, component;
	    double lnAF;

	    try {
	      mag = ( (Double) magParam.getValue()).doubleValue();
	      dist = ( (Double) distanceRupParam.getValue()).doubleValue();
	      fltType = fltTypeParam.getValue().toString();
//	      siteType = siteTypeParam.getValue().toString();
	      isHW = isOnHangingWallParam.getValue().toString();
	      component = componentParam.getValue().toString();
	    }
	    catch (NullPointerException e) {
	        throw new IMRException(C + ": getMean(): " + ERR);
	      }

	      // check if distance is beyond the user specified max
	      if (dist > USER_MAX_DISTANCE) {
	        return VERY_SMALL_MEAN;
	      }

	      double F, f5, rockMeanPGA, rockMean;
	      int HW;

	      if (fltType.equals(FLT_TYPE_REVERSE)) {
	        F = 1.0;
	      }
	      else if (fltType.equals(FLT_TYPE_REV_OBL)) {
	        F = 0.5;
	      }
	      else {
	        F = 0.0;
	      }

	      if (isHW.equals(IS_ON_HANGING_WALL_TRUE)) {
	        HW = 1;
	      }
	      else {
	        HW = 0;
	      }

	      // Get PGA coefficients
	      if (componentParam.getValue().toString().equals(COMPONENT_AVE_HORZ)) {
	        coeff = (AS_1997_AttenRelCoefficients) horzCoeffs.get(PGA_NAME);
	        a2 = 0.512;
	        a4 = -0.144;
	        a13 = 0.17;
	        c1 = 6.4;
	        c5 = 0.03;
	        n = 2;
	      }
	      else {
	        coeff = (AS_1997_AttenRelCoefficients) vertCoeffs.get(PGA_NAME);
	        a2 = 0.909;
	        a4 = 0.275;
	        a13 = 0.06;
	        c1 = 6.4;
	        c5 = 0.3;
	        n = 3;
	      }

	      // Get mean rock PGA
	      rockMeanPGA = calcRockMean(mag, dist, F, HW);

	      // now set coefficients for the current im and component (inefficent if im=PGA)
	      updateCoefficients();

	      rockMean = calcRockMean(mag, dist, F, HW);
	      
	      lnAF = getAF(rockMean);
	      
	      return rockMean + lnAF;

	  
  }


    
    
  public double getAF(double asRockSA) {
    	
    double lnAF;
    	
    // get the amp factor
    double aVal = ((Double)AF_InterceptParam.getValue()).doubleValue();
    double bVal = ((Double)AF_SlopeParam.getValue()).doubleValue();
    double cVal = ((Double)AF_AddRefAccParam.getValue()).doubleValue();
    double mVal = ((Double)AF_MagParam.getValue()).doubleValue();
    lnAF = aVal+bVal*Math.log(Math.exp(asRockSA)+cVal)+mVal*mag;   

    // return the result
    return lnAF;
  }


  /**
   * Returns the Std Dev.
   */
  public double getStdDev(){
	  
	  return 0;
  }
  


 
  public void setParamDefaults() {
	  
	    magParam.setValue(MAG_DEFAULT);
	    fltTypeParam.setValue(FLT_TYPE_DEFAULT);
	    distanceRupParam.setValue(DISTANCE_RUP_DEFAULT);
	    saParam.setValue(SA_DEFAULT);
	    periodParam.setValue(PERIOD_DEFAULT);
	    dampingParam.setValue(DAMPING_DEFAULT);
	    pgaParam.setValue(PGA_DEFAULT);
	    componentParam.setValue(COMPONENT_DEFAULT);
	    stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);
	    isOnHangingWallParam.setValue(IS_ON_HANGING_WALL_DEFAULT);
	    setAFParamDefaults();
  }
  
  public void setAFParamDefaults() {
	    AF_AddRefAccParam.setValue(AF_ADDITIVE_REF_ACCERLATION_DEFAULT);
	    AF_InterceptParam.setValue(AF_INTERCEPT_PARAM_DEFAULT);
	    AF_SlopeParam.setValue(AF_SLOPE_PARAM_DEFAULT);
	    AF_MagParam.setValue(AF_MagParam_DEFAULT);
	    AF_StdDevParam.setValue(AF_STD_DEV_DEFAULT);
	  
  }
  

  /**
   * This creates the lists of independent parameters that the various dependent
   * parameters (mean, standard deviation, exceedance probability, and IML at
   * exceedance probability) depend upon. NOTE: these lists do not include anything
   * about the intensity-measure parameters or any of thier internal
   * independentParamaters.
   */
  
  
  protected void initIndependentParamLists() {

	    // params that the mean depends upon
	    meanIndependentParams.clear();
	    meanIndependentParams.addParameter(distanceRupParam);
//	    meanIndependentParams.addParameter(siteTypeParam);
	    meanIndependentParams.addParameter(magParam);
	    meanIndependentParams.addParameter(fltTypeParam);
	    meanIndependentParams.addParameter(isOnHangingWallParam);
//	    meanIndependentParams.addParameter(componentParam);
	    meanIndependentParams.addParameter(AF_AddRefAccParam);
	    meanIndependentParams.addParameter(AF_InterceptParam);
	    meanIndependentParams.addParameter(AF_SlopeParam);
	    meanIndependentParams.addParameter(AF_MagParam);
	    meanIndependentParams.addParameter(AF_StdDevParam);

	    // params that the stdDev depends upon
	    stdDevIndependentParams.clear();
	    stdDevIndependentParams.addParameter(stdDevTypeParam);
	    stdDevIndependentParams.addParameter(magParam);
//	    stdDevIndependentParams.addParameter(componentParam);
	    stdDevIndependentParams.addParameter(AF_AddRefAccParam);
	    stdDevIndependentParams.addParameter(AF_InterceptParam);
	    stdDevIndependentParams.addParameter(AF_SlopeParam);
	    stdDevIndependentParams.addParameter(AF_StdDevParam);

	    // params that the exceed. prob. depends upon
	    exceedProbIndependentParams.clear();
	    exceedProbIndependentParams.addParameter(distanceRupParam);
//	    exceedProbIndependentParams.addParameter(siteTypeParam);
	    exceedProbIndependentParams.addParameter(magParam);
	    exceedProbIndependentParams.addParameter(fltTypeParam);
	    exceedProbIndependentParams.addParameter(isOnHangingWallParam);
//	    exceedProbIndependentParams.addParameter(componentParam);
	    exceedProbIndependentParams.addParameter(stdDevTypeParam);
	    exceedProbIndependentParams.addParameter(this.sigmaTruncTypeParam);
	    exceedProbIndependentParams.addParameter(this.sigmaTruncLevelParam);
	    exceedProbIndependentParams.addParameter(AF_AddRefAccParam);
	    exceedProbIndependentParams.addParameter(AF_InterceptParam);
	    exceedProbIndependentParams.addParameter(AF_SlopeParam);
	    exceedProbIndependentParams.addParameter(AF_MagParam);
	    exceedProbIndependentParams.addParameter(AF_StdDevParam);

	    // params that the IML at exceed. prob. depends upon
	    imlAtExceedProbIndependentParams.addParameterList(
	        exceedProbIndependentParams);
	    imlAtExceedProbIndependentParams.addParameter(exceedProbParam);
	  }

  /**
   *  Create the site param and fill with AF parameters.
   */
  protected void initSiteParams() {
   
    //make the AF intercept parameter
    AF_InterceptParam = new DoubleParameter(this.AF_INTERCEPT_PARAM_NAME,
    		AF_InterceptparamConstraint,this.AF_INTERCEPT_PARAM_DEFAULT);
    AF_InterceptParam.setInfo(this.AF_INTERCEPT_PARAM_INFO);
    
    //make the AF slope parameter
    this.AF_SlopeParam = new DoubleParameter(this.AF_SLOPE_PARAM_NAME,
    		this.AF_slopeParamConstraint,this.AF_SLOPE_PARAM_DEFAULT);
    AF_SlopeParam.setInfo(this.AF_SLOPE_PARAM_INFO);
    
    //make theb AF Additive Reference Parameter
    this.AF_AddRefAccParam = new DoubleParameter(this.AF_ADDITIVE_REF_ACCELERATION_PARAM_NAME,
    		this.AFaddRefAccParamConstraint,this.AF_ADDITIVE_REF_ACCERLATION_DEFAULT);
    AF_AddRefAccParam.setInfo(this.AF_ADDITIVE_REF_ACCELERATION_PARAM_INFO);

    //make the AF Mag Parameter
    this.AF_MagParam = new DoubleParameter(this.AF_MagPARAM_NAME,
    		this.AFMagParamConstraint,this.AF_MagParam_DEFAULT);
    AF_MagParam.setInfo(this.AF_MagPARAM_INFO);
    
    //make the AF Std. Dev.
    this.AF_StdDevParam = new DoubleParameter(this.AF_STD_DEV_PARAM_NAME,
    		this.AF_StdDevParamConstraint,this.AF_STD_DEV_DEFAULT);
    
    AF_StdDevParam.setInfo(this.AF_STD_DEV_PARAM_INFO);
    
     // add it to the siteParams list:
    siteParams.clear();
    siteParams.addParameter(AF_AddRefAccParam);
    siteParams.addParameter(AF_InterceptParam);
    siteParams.addParameter(AF_SlopeParam);
    siteParams.addParameter(AF_MagParam);
    siteParams.addParameter(AF_StdDevParam);


  }




}
