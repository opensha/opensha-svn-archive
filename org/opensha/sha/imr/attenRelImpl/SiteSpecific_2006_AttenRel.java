package org.opensha.sha.imr.attenRelImpl;

import java.util.*;

import org.opensha.data.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.imr.*;

/**
 * <b>Title:</b> SiteSpecific_2006_AttenRel<p>
 *
 * <b>Description:</b> This implements the site effect models
 * developed by Bazzuro and Cornell(2004), Baturay and Stewart(2003), applied
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
 * @author     Edward H. Field
 * @created    july, 2004
 * @version    1.0
 */


public class SiteSpecific_2006_AttenRel
    extends AttenuationRelationship implements AttenuationRelationshipAPI,
    NamedObjectAPI {

  // debugging stuff:
  private final static String C = "SiteSpecific_2006_AttenRel";
  private final static boolean D = false;
  public final static String NAME = "Site Specfic AttenuationRelationship (2006)";
  public final static String SHORT_NAME = "SS2006";


  // warning constraint fields:
  protected final static Double VS30_WARN_MIN = new Double(50.0);
  protected final static Double VS30_WARN_MAX = new Double(760.0);

  // the Soft Soil Parameter
  private BooleanParameter softSoilParam = null;
  public final static String SOFT_SOIL_NAME = "Soft Soil Case";
  public final static String SOFT_SOIL_INFO =
      "Indicates that site is considered NEHRP E regardless of Vs30.\n\n" +
      "Conditions required are undrained shear strength < 24 kPa, " +
      "PI > 20, water content > 40%, and thickness of clay exceeds 3 m.";
  public final static Boolean SOFT_SOIL_DEFAULT = new Boolean(false);

  private AS_1997_AttenRel as_1997_attenRel;
  /**
   * The current set of coefficients based on the selected intensityMeasure
   */
  private SS_2006_AttenRelCoefficients coeffs = null;
  
  
  //Intercept param
  private DoubleParameter AF_InterceptParam;
  public final static String AF_INTERCEPT_PARAM_NAME = "AF Intercept";
  public final static String AF_INTERCEPT_PARAM_INFO = 
	  "Intercept of the median regression model for the ground response analyses";
  private DoubleConstraint AF_InterceptparamConstraint = new DoubleConstraint(-2,2);
  public final static double AF_INTERCEPT_PARAM_DEFAULT = 0;
  
  //Slope Param
  private DoubleParameter AF_SlopeParam;
  public final static String AF_SLOPE_PARAM_NAME = "AF Slope";
  public final static String AF_SLOPE_PARAM_INFO = 
	  "Slope of the median regression model for the ground response analyses";
  private DoubleConstraint AF_slopeParamConstraint = new DoubleConstraint(-1,1);
  public final static double AF_SLOPE_PARAM_DEFAULT = 0;
  
  //Additive refeerence acceleration param
  private DoubleParameter AF_AddRefAccParam;
  public final static String AF_ADDITIVE_REF_ACCELERATION_PARAM_NAME = "AF Add. Ref. Acceleration";
  public final static String AF_ADDITIVE_REF_ACCELERATION_PARAM_INFO = 
	  "Additive reference acceleration of the median regression model for the ground response " +
	  "analyses. This parameter improves the linear model fit for low Sa(rock) / PGA(rock)" +
	  "values and leads to more relaistic predictons than quadratic models";
  private DoubleConstraint AFaddRefAccParamConstraint = new DoubleConstraint(0,0.5);
  public final static double AF_ADDITIVE_REF_ACCERLATION_DEFAULT = 0.03;
  
  
  //Std. Dev AF param
  private DoubleParameter AF_StdDevParam;
  public final static String AF_STD_DEV_PARAM_NAME = "Std. Dev. AF";
  public final static String AF_STD_DEV_PARAM_INFO = 
	  "Standard Deviation of the amplification factor from the ground response analyses" +
	  " regression model";
  private DoubleConstraint AF_StdDevParamConstraint = new DoubleConstraint(0,1.0);
  public final static double AF_STD_DEV_DEFAULT = 0.3;
  
  //number of runs parameter
  private IntegerParameter numRunsParam;
  public final static String NUM_RUNS_PARAM_NAME = "Number of Runs";
  public final static String NUM_RUNS_PARAM_INFO = "Number of runs of the wave propagation"+
	  " simulation for the site";
  private IntegerConstraint numRunsConstraint = new IntegerConstraint(1,Integer.MAX_VALUE);
  public final static int NUM_RUNS_PARAM_DEFAULT = 1;

  
  //Site Effect correction to apply
  private StringParameter siteEffectCorrectionParam;
  public final static String SITE_EFFECT_PARAM_NAME = "Site Effect Model";
  public final static String SITE_EFFECT_PARAM_INFO = "Select which model to apply for" +
  		" site effect correction";
  private final static String BATURAY_STEWART_MODEL = "Baturay and Stewart (2003)";
  private final static String BAZZURO_CORNELL_MODEL = "Bazzuro and Cornell(2004)";
  
  /**
   *  Hashtable of coefficients for the supported intensityMeasures
   */
  protected Hashtable horzCoeffs = new Hashtable();

  // for issuing warnings:
  private transient ParameterChangeWarningListener warningListener = null;

  /**
   *  No-Arg constructor. This initializes several ParameterList objects.
   */
  public SiteSpecific_2006_AttenRel(ParameterChangeWarningListener warningListener) {

    super();

    this.warningListener = warningListener;

    as_1997_attenRel = new AS_1997_AttenRel(warningListener);
    // set the site type to rock
    as_1997_attenRel.getParameter(as_1997_attenRel.SITE_TYPE_NAME).setValue(
        as_1997_attenRel.SITE_TYPE_ROCK);
    // set the component to ave horz
    as_1997_attenRel.getParameter(as_1997_attenRel.COMPONENT_NAME).setValue(
        as_1997_attenRel.COMPONENT_AVE_HORZ);

    // overide local params with those in as_1997_attenRel
    this.sigmaTruncTypeParam = (StringParameter) as_1997_attenRel.getParameter(
        as_1997_attenRel.SIGMA_TRUNC_TYPE_NAME);
    this.sigmaTruncLevelParam = (DoubleParameter) as_1997_attenRel.getParameter(
        as_1997_attenRel.SIGMA_TRUNC_LEVEL_NAME);
    this.exceedProbParam = (DoubleParameter) as_1997_attenRel.getParameter(
        as_1997_attenRel.EXCEED_PROB_NAME);
    this.stdDevTypeParam = (StringParameter) as_1997_attenRel.getParameter(
        as_1997_attenRel.STD_DEV_TYPE_NAME);
    this.periodParam = (DoubleDiscreteParameter) as_1997_attenRel.getParameter(
        PERIOD_NAME);

    initCoefficients();
    initSupportedIntensityMeasureParams();
    initEqkRuptureParams();
    initPropagationEffectParams();
    initSiteParams();
    initOtherParams();

    initIndependentParamLists(); // Do this after the above
    /*
            TreeSet set = new TreeSet();
            Enumeration keys = horzCoeffs.keys(); // same as for vertCoeffs
            while ( keys.hasMoreElements() ) {
              SS_2006_AttenRelCoefficients c = ( SS_2006_AttenRelCoefficients ) horzCoeffs.get( keys.nextElement() );
              System.out.println(c.period+"\t"+c.b1+"\t"+c.vRef+"\t"+c.c+"\t"+c.b2+"\t"+c.tau+"\t"+c.e1+"\t"+c.e3);
            }
     */
  }

  /**
   * This does nothing, but is needed.
   */
  protected void setPropagationEffectParams() {

  }

  /**
   *  This sets the eqkRupture related parameters.
   *
   * @param  eqkRupture  The new eqkRupture value
   * @throws InvalidRangeException thrown if rake is out of bounds
   */
  public void setEqkRupture(EqkRupture eqkRupture) throws InvalidRangeException {
    this.as_1997_attenRel.setEqkRupture(eqkRupture);
    this.eqkRupture = eqkRupture;
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

    vs30Param.setValueIgnoreWarning(site.getParameter(VS30_NAME).getValue());
    softSoilParam.setValue(site.getParameter(SOFT_SOIL_NAME).getValue());
    this.site = site;
    // set the location in as_1997_attenRel
    as_1997_attenRel.setSiteLocation(site.getLocation());
  }

  /**
   * This function determines which set of coefficients in the HashMap
   * are to be used given the current intensityMeasure (im) Parameter.
   */
  protected void updateCoefficients() throws ParameterException {
    // Check that parameter exists
    if (im == null) {
      throw new ParameterException(C +
                                   ": updateCoefficients(): " +
                                   "The Intensity Measusre Parameter has not been set yet, unable to process."
          );
    }

    StringBuffer key = new StringBuffer(im.getName());
    if (im.getName().equalsIgnoreCase(SA_NAME)) {
      key.append("/" + periodParam.getValue());
    }
    if (horzCoeffs.containsKey(key.toString())) {
      coeffs = (SS_2006_AttenRelCoefficients) horzCoeffs.get(key.toString());
    }
    else {
      throw new ParameterException(C + ": setIntensityMeasureType(): " +
                                   "Unable to locate coefficients with key = " +
                                   key);
    }
  }

  /**
   * Calculates the mean
   * @return    The mean value
   */
  public double getMean() throws IMRException {

    double asRockSA, lnAF;

    // get AS-1997 SA for rock
     as_1997_attenRel.setIntensityMeasure(im);
    asRockSA = as_1997_attenRel.getMean();
    
    // get the amp factor
    double aVal = ((Double)AF_InterceptParam.getValue()).doubleValue();
    double bVal = ((Double)AF_SlopeParam.getValue()).doubleValue();
    double cVal = ((Double)AF_AddRefAccParam.getValue()).doubleValue();
    lnAF = aVal+bVal*Math.log(Math.exp(asRockSA)+cVal);   

    // return the result
    return lnAF + as_1997_attenRel.getMean();
  }


  /**
   * Returns the Std Dev.
   */
  public double getStdDev(){
	  
	  String stdDevType = stdDevTypeParam.getValue().toString();
	  if (stdDevType.equals(STD_DEV_TYPE_NONE)) { // "None (zero)"
		  return 0;
	  }
	  else {
		  
		  
		  String siteCorrectionModelUsed = (String)siteEffectCorrectionParam.getValue();
		  if(siteCorrectionModelUsed.equals(this.BAZZURO_CORNELL_MODEL))
			  return getStdDevForBC();
		  else{
			  float periodParamVal;
			  if(im.getName().equals(this.SA_NAME))
				  periodParamVal = (float)((Double) periodParam.getValue()).doubleValue();
			  else
				  periodParamVal = 0;
			  
			  if(periodParamVal < 0.75)
				  return getStdDevForBS();
			  else if(periodParamVal > 1.5)
				  return getStdDevForCS();
			  else{
				  //getting the Std Dev for Period of 0.75
				  periodParam.setValue(new Double(0.75));
				  double stdDev_BS = getStdDevForBS();
				  //getting the Std Dev. for period param 1.5
				  periodParam.setValue(new Double(1.5));
				  double stdDev_CS = getStdDevForCS();
				  //setting the period to period selected by the user
				  periodParam.setValue(new Double(periodParamVal));
				  //linear interpolation to get the Std Dev.
				  double stdDev = ((periodParamVal - 0.75)/(1.5 -0.75))*
				  (stdDev_CS - stdDev_BS) + stdDev_BS;
				  return stdDev;
			  }
		  }
	  }
  }
  
  
  /**
   * @return    The stdDev value for Bazzurro and Cornell (2004) Site Correction Model
   */
  private double getStdDevForBC(){
	  double bVal = ((Double)AF_SlopeParam.getValue()).doubleValue();
	  double stdDevAF = ((Double)this.AF_StdDevParam.getValue()).doubleValue();
	  as_1997_attenRel.setIntensityMeasure(im);
	  double asRockStdDev = as_1997_attenRel.getStdDev();
	  double stdDev = Math.pow(bVal+1, 2)*Math.pow(asRockStdDev, 2)+Math.pow(stdDevAF, 2);
	  return Math.sqrt(stdDev);
  }

  /**
   * @return    The stdDev value for Baturay and Stewart (2003) Site Correction Model
   */
  private double getStdDevForBS(){
	  double stdDevAF = ((Double)this.AF_StdDevParam.getValue()).doubleValue();
	  int numRuns = ((Integer)this.numRunsParam.getValue()).intValue();
	  double stdError = stdDevAF/(Math.sqrt(numRuns));
	  double stdDev_gNet;
	  double vs30 = ((Double)vs30Param.getValue()).doubleValue();
	  if(vs30 <= 180)
		  stdDev_gNet = 0.38;
	  else
		  stdDev_gNet = 0.56;
	  double stdDev = Math.pow(stdDev_gNet,2)+Math.pow(stdError, 2);
	  return Math.sqrt(stdDev);
  }

  /**
   * @return    The stdDev value for Choi and Stewart (2005) model
   */
  private double getStdDevForCS() throws IMRException {

      double vs30, sigmaV, sigmaAS;

      // get As-1997 stdDev
//      as_1997_attenRel.setIntensityMeasure(im);
//      sigmaAS = as_1997_attenRel.getStdDev();

      // set vs30 from the parameters
      if ( ( (Boolean) softSoilParam.getValue()).booleanValue()) {
        vs30 = 174;
      }
      else {
        try {
          vs30 = ( (Double) vs30Param.getValue()).doubleValue();
        }
        catch (NullPointerException e) {
          throw new IMRException(C + ": getMean(): " + ERR);
        }
      }

      // this is inefficient if the im has not been changed in any way
      updateCoefficients();

      // set sigmaV
      if (vs30 < 260) {
        sigmaV = coeffs.e1;
      }
      else if (vs30 < 360) {
        sigmaV = coeffs.e1 +
            ( (coeffs.e3 - coeffs.e1) / Math.log(360 / 260)) *
            Math.log(vs30 / 260);
      }
      else {
        sigmaV = coeffs.e3;
      }

      return Math.sqrt(sigmaV * sigmaV + coeffs.tau * coeffs.tau);
   
  }

  
  public void setParamDefaults() {

    //((ParameterAPI)this.iml).setValue( IML_DEFAULT );
    vs30Param.setValue(VS30_DEFAULT);
    softSoilParam.setValue(new Boolean(false));
    as_1997_attenRel.setParamDefaults();
    // re-set the site type to rock and component to ave horz
    as_1997_attenRel.getParameter(as_1997_attenRel.SITE_TYPE_NAME).setValue(
        as_1997_attenRel.SITE_TYPE_ROCK);
    as_1997_attenRel.getParameter(as_1997_attenRel.COMPONENT_NAME).setValue(
        as_1997_attenRel.COMPONENT_AVE_HORZ);
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
    ListIterator it = as_1997_attenRel.getMeanIndependentParamsIterator();
    String ignoreStr1 = as_1997_attenRel.SITE_TYPE_NAME;
    String ignoreStr2 = as_1997_attenRel.COMPONENT_NAME;
    while (it.hasNext()) {
      Parameter param = (Parameter) it.next();
      if (!ignoreStr1.equals(param.getName()) &&
          !ignoreStr2.equals(param.getName())) {
        meanIndependentParams.addParameter(param);
      }
    }
    meanIndependentParams.addParameter(vs30Param);
    meanIndependentParams.addParameter(softSoilParam);
    meanIndependentParams.addParameter(componentParam);

    // params that the stdDev depends upon
    stdDevIndependentParams.clear();
    stdDevIndependentParams.addParameter(vs30Param);
    stdDevIndependentParams.addParameter(softSoilParam);
    stdDevIndependentParams.addParameter(componentParam);
    it = as_1997_attenRel.getStdDevIndependentParamsIterator();
    while (it.hasNext()) {
      Parameter param = (Parameter) it.next();
      if (!ignoreStr1.equals(param.getName()) &&
          !ignoreStr2.equals(param.getName())) {
        stdDevIndependentParams.addParameter(param);
      }
    }

    // params that the exceed. prob. depends upon
    exceedProbIndependentParams.clear();
    exceedProbIndependentParams.addParameter(vs30Param);
    exceedProbIndependentParams.addParameter(softSoilParam);
    exceedProbIndependentParams.addParameter(componentParam);
    it = as_1997_attenRel.getExceedProbIndependentParamsIterator();
    while (it.hasNext()) {
      Parameter param = (Parameter) it.next();
      if (!ignoreStr1.equals(param.getName()) &&
          !ignoreStr2.equals(param.getName())) {
        exceedProbIndependentParams.addParameter(param);
      }
    }

    // params that the IML at exceed. prob. depends upon
    imlAtExceedProbIndependentParams.addParameterList(
        exceedProbIndependentParams);
    imlAtExceedProbIndependentParams.addParameter(exceedProbParam);

  }

  /**
   *  Creates the Vs30 site parameter and adds it to the siteParams list.
   *  Makes the parameters noneditable.
   */
  protected void initSiteParams() {

    // create vs30 Parameter:
    super.initSiteParams();

    // create and add the warning constraint:
    DoubleConstraint warn = new DoubleConstraint(VS30_WARN_MIN, VS30_WARN_MAX);
    warn.setNonEditable();
    vs30Param.setWarningConstraint(warn);
    vs30Param.addParameterChangeWarningListener(warningListener);
    vs30Param.setNonEditable();

    // make the Soft Soil parameter
    softSoilParam = new BooleanParameter(SOFT_SOIL_NAME, SOFT_SOIL_DEFAULT);
    softSoilParam.setInfo(SOFT_SOIL_INFO);
    
    //make the AF intercept paarameter
    AF_InterceptParam = new DoubleParameter(this.AF_INTERCEPT_PARAM_NAME,
    		AF_InterceptparamConstraint,this.AF_INTERCEPT_PARAM_DEFAULT);
    AF_InterceptParam.setInfo(this.AF_INTERCEPT_PARAM_INFO);
    
    //make the AF slope parameter
    this.AF_SlopeParam = new DoubleParameter(this.AF_SLOPE_PARAM_NAME,
    		this.AF_slopeParamConstraint,this.AF_SLOPE_PARAM_DEFAULT);
    AF_SlopeParam.setInfo(this.AF_SLOPE_PARAM_INFO);
    
    //make theb AF Additive Reference Paramerter
    this.AF_AddRefAccParam = new DoubleParameter(this.AF_ADDITIVE_REF_ACCELERATION_PARAM_NAME,
    		this.AFaddRefAccParamConstraint,this.AF_ADDITIVE_REF_ACCERLATION_DEFAULT);
    AF_AddRefAccParam.setInfo(this.AF_ADDITIVE_REF_ACCELERATION_PARAM_INFO);
    
    //make the AF Std. Dev.
    this.AF_StdDevParam = new DoubleParameter(this.AF_ADDITIVE_REF_ACCELERATION_PARAM_NAME,
    		this.AF_StdDevParamConstraint,this.AF_STD_DEV_DEFAULT);
    
    AF_StdDevParam.setInfo(this.AF_STD_DEV_PARAM_INFO);
    
    //make the number of runs parameter
    this.numRunsParam = new IntegerParameter(this.NUM_RUNS_PARAM_NAME,
    		this.numRunsConstraint,this.NUM_RUNS_PARAM_DEFAULT);
    numRunsParam.setInfo(this.NUM_RUNS_PARAM_INFO);
    
    //create the Site Effect correction parameter
    ArrayList siteEffectCorrectionModelList = new ArrayList();
    siteEffectCorrectionModelList.add(this.BATURAY_STEWART_MODEL);
    siteEffectCorrectionModelList.add(this.BAZZURO_CORNELL_MODEL);
    StringConstraint siteEffectCorrectionConstraint = new StringConstraint(siteEffectCorrectionModelList);
    this.siteEffectCorrectionParam = new StringParameter(this.SITE_EFFECT_PARAM_NAME,
    		siteEffectCorrectionConstraint,(String)siteEffectCorrectionModelList.get(0));
    siteEffectCorrectionParam.setInfo(this.SITE_EFFECT_PARAM_INFO);

    // add it to the siteParams list:
    siteParams.clear();
    siteParams.addParameter(siteEffectCorrectionParam);
    siteParams.addParameter(AF_InterceptParam);
    siteParams.addParameter(AF_SlopeParam);
    siteParams.addParameter(AF_AddRefAccParam);
    siteParams.addParameter(vs30Param);
    siteParams.addParameter(softSoilParam);

  }

  /**
   *  Creates the two Potential Earthquake parameters (magParam and
   *  fltTypeParam) and adds them to the eqkRuptureParams
   *  list. Makes the parameters noneditable.
   */
  protected void initEqkRuptureParams() {

    // Create magParam
    super.initEqkRuptureParams();

    eqkRuptureParams.clear();
    ListIterator it = as_1997_attenRel.getEqkRuptureParamsIterator();
    while (it.hasNext()) {
      eqkRuptureParams.addParameter( (Parameter) it.next());
    }
  }

  /**
   *  Creates the single Propagation Effect parameter and adds it to the
   *  propagationEffectParams list. Makes the parameters noneditable.
   */
  protected void initPropagationEffectParams() {
    propagationEffectParams.clear();
    ListIterator it = as_1997_attenRel.getPropagationEffectParamsIterator();
    while (it.hasNext()) {
      propagationEffectParams.addParameter( (Parameter) it.next());
    }

  }

  /**
   *  Creates the two supported IM parameters (PGA and SA), as well as the
   *  independenParameters of SA (periodParam and dampingParam) and adds
   *  them to the supportedIMParams list. Makes the parameters noneditable.
   */
  protected void initSupportedIntensityMeasureParams() {

    supportedIMParams.clear();
    Iterator it = as_1997_attenRel.getSupportedIntensityMeasuresIterator();
    while (it.hasNext()) {
      supportedIMParams.addParameter( (Parameter) it.next());
    }
  }

  /**
   *  Creates other Parameters that the mean or stdDev depends upon,
   *  such as the Component or StdDevType parameters.
   */
  protected void initOtherParams() {

    // init other params defined in parent class -- Don't need this
    // super.initOtherParams();

    // the Component Parameter (not supporting AS_1997's vertical)
    StringConstraint constraint = new StringConstraint();
    constraint.addString(COMPONENT_AVE_HORZ);
    constraint.setNonEditable();
    componentParam = new StringParameter(COMPONENT_NAME, constraint,
                                         COMPONENT_DEFAULT);
    componentParam.setInfo(COMPONENT_INFO);
    componentParam.setNonEditable();
    // add this to the list
    otherParams.clear();
    otherParams.addParameter(componentParam);
    Iterator it = as_1997_attenRel.getOtherParamsIterator();
    Parameter param;
    while (it.hasNext()) {
      param = (Parameter) it.next();
      if (!COMPONENT_NAME.equals(param.getName())) {
        otherParams.addParameter(param);
      }
    }
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

  /**
   *  This creates the hashtable of coefficients for the supported
   *  intensityMeasures (im).  The key is the im parameter name, plus the
   *  period value for SA (separated by "/").  For example, the key for SA
   *  at 1.0 second period is "SA/1.0".
   */
  protected void initCoefficients() {

    String S = C + ": initCoefficients():";
    if (D) {
      System.out.println(S + "Starting");
    }

    horzCoeffs.clear();

    SS_2006_AttenRelCoefficients coeff = new SS_2006_AttenRelCoefficients(
        PGA_NAME,
        0.0, -0.64, 418, -0.36, -0.14, 0.27, 0.44, 0.50);

    SS_2006_AttenRelCoefficients coeff0 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.01")).doubleValue(),
        0.01, -0.64, 418, -0.36, -0.14, 0.27, 0.44, 0.50);
    SS_2006_AttenRelCoefficients coeff1 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.02")).doubleValue(),
        0.02, -0.63, 490, -0.34, -0.12, 0.26, 0.45, 0.51);
    SS_2006_AttenRelCoefficients coeff2 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.03")).doubleValue(),
        0.03, -0.62, 324, -0.33, -0.11, 0.26, 0.46, 0.51);
    SS_2006_AttenRelCoefficients coeff3 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.04")).doubleValue(),
        0.04, -0.61, 233, -0.31, -0.11, 0.26, 0.47, 0.51);
    SS_2006_AttenRelCoefficients coeff4 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.05")).doubleValue(),
        0.05, -0.64, 192, -0.29, -0.11, 0.25, 0.47, 0.52);
    SS_2006_AttenRelCoefficients coeff5 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.06")).doubleValue(),
        0.06, -0.64, 181, -0.25, -0.11, 0.25, 0.48, 0.52);
    SS_2006_AttenRelCoefficients coeff6 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.075")).doubleValue(),
        0.075, -0.64, 196, -0.23, -0.11, 0.24, 0.48, 0.52);
    SS_2006_AttenRelCoefficients coeff7 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.09")).doubleValue(),
        0.09, -0.64, 239, -0.23, -0.12, 0.23, 0.49, 0.52);
    SS_2006_AttenRelCoefficients coeff8 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.10")).doubleValue(),
        0.10, -0.60, 257, -0.25, -0.13, 0.23, 0.49, 0.53);
    SS_2006_AttenRelCoefficients coeff9 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.12")).doubleValue(),
        0.12, -0.56, 299, -0.26, -0.14, 0.24, 0.49, 0.53);
    SS_2006_AttenRelCoefficients coeff10 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.15")).doubleValue(),
        0.15, -0.53, 357, -0.28, -0.18, 0.25, 0.49, 0.54);
    SS_2006_AttenRelCoefficients coeff11 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.17")).doubleValue(),
        0.17, -0.53, 406, -0.29, -0.19, 0.26, 0.48, 0.55);
    SS_2006_AttenRelCoefficients coeff12 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.20")).doubleValue(),
        0.20, -0.52, 453, -0.31, -0.19, 0.27, 0.47, 0.56);
    SS_2006_AttenRelCoefficients coeff13 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.24")).doubleValue(),
        0.24, -0.52, 493, -0.38, -0.16, 0.29, 0.47, 0.56);
    SS_2006_AttenRelCoefficients coeff14 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.30")).doubleValue(),
        0.30, -0.52, 532, -0.44, -0.14, 0.35, 0.46, 0.57);
    SS_2006_AttenRelCoefficients coeff15 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.36")).doubleValue(),
        0.36, -0.51, 535, -0.48, -0.11, 0.38, 0.46, 0.57);
    SS_2006_AttenRelCoefficients coeff16 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.40")).doubleValue(),
        0.40, -0.51, 535, -0.50, -0.10, 0.40, 0.46, 0.57);
    SS_2006_AttenRelCoefficients coeff17 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.46")).doubleValue(),
        0.46, -0.50, 535, -0.55, -0.08, 0.42, 0.45, 0.58);
    SS_2006_AttenRelCoefficients coeff18 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.50")).doubleValue(),
        0.50, -0.50, 535, -0.60, -0.06, 0.42, 0.45, 0.59);
    SS_2006_AttenRelCoefficients coeff19 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.60")).doubleValue(),
        0.60, -0.49, 535, -0.66, -0.03, 0.42, 0.44, 0.60);
    SS_2006_AttenRelCoefficients coeff20 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.75")).doubleValue(),
        0.75, -0.47, 535, -0.69, 0.00, 0.42, 0.44, 0.63);
    SS_2006_AttenRelCoefficients coeff21 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.85")).doubleValue(),
        0.85, -0.46, 535, -0.69, 0.00, 0.42, 0.44, 0.63);
    SS_2006_AttenRelCoefficients coeff22 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("1.00")).doubleValue(),
        1.00, -0.44, 535, -0.70, 0.00, 0.42, 0.44, 0.64);
    SS_2006_AttenRelCoefficients coeff23 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("1.50")).doubleValue(),
        1.50, -0.40, 535, -0.72, 0.00, 0.42, 0.44, 0.67);
    SS_2006_AttenRelCoefficients coeff24 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("2.00")).doubleValue(),
        2.00, -0.38, 535, -0.73, 0.00, 0.43, 0.44, 0.69);
    SS_2006_AttenRelCoefficients coeff25 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("3.00")).doubleValue(),
        3.00, -0.34, 535, -0.74, 0.00, 0.45, 0.44, 0.71);
    SS_2006_AttenRelCoefficients coeff26 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("4.00")).doubleValue(),
        4.00, -0.31, 535, -0.75, 0.00, 0.47, 0.44, 0.73);
    SS_2006_AttenRelCoefficients coeff27 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("5.00")).doubleValue(),
        5.00, -0.30, 535, -0.75, 0.00, 0.49, 0.44, 0.75);
    // add zero-period case; same as 0.01 sec.
    SS_2006_AttenRelCoefficients coeff28 = new SS_2006_AttenRelCoefficients(
        SA_NAME + "/" + (new Double("0.0")).doubleValue(),
        0.0, -0.64, 418, -0.36, -0.14, 0.27, 0.44, 0.50);

    horzCoeffs.put(coeff.getName(), coeff);
    horzCoeffs.put(coeff0.getName(), coeff0);
    horzCoeffs.put(coeff1.getName(), coeff1);
    horzCoeffs.put(coeff2.getName(), coeff2);
    horzCoeffs.put(coeff3.getName(), coeff3);
    horzCoeffs.put(coeff4.getName(), coeff4);
    horzCoeffs.put(coeff5.getName(), coeff5);
    horzCoeffs.put(coeff6.getName(), coeff6);
    horzCoeffs.put(coeff7.getName(), coeff7);
    horzCoeffs.put(coeff8.getName(), coeff8);
    horzCoeffs.put(coeff9.getName(), coeff9);

    horzCoeffs.put(coeff10.getName(), coeff10);
    horzCoeffs.put(coeff11.getName(), coeff11);
    horzCoeffs.put(coeff12.getName(), coeff12);
    horzCoeffs.put(coeff13.getName(), coeff13);
    horzCoeffs.put(coeff14.getName(), coeff14);
    horzCoeffs.put(coeff15.getName(), coeff15);
    horzCoeffs.put(coeff16.getName(), coeff16);
    horzCoeffs.put(coeff17.getName(), coeff17);
    horzCoeffs.put(coeff18.getName(), coeff18);
    horzCoeffs.put(coeff19.getName(), coeff19);

    horzCoeffs.put(coeff20.getName(), coeff20);
    horzCoeffs.put(coeff21.getName(), coeff21);
    horzCoeffs.put(coeff22.getName(), coeff22);
    horzCoeffs.put(coeff23.getName(), coeff23);
    horzCoeffs.put(coeff24.getName(), coeff24);
    horzCoeffs.put(coeff25.getName(), coeff25);
    horzCoeffs.put(coeff26.getName(), coeff26);
    horzCoeffs.put(coeff27.getName(), coeff27);
    horzCoeffs.put(coeff28.getName(), coeff28);
  }

  /**
   *  <b>Title:</b> SS_2006_AttenRelCoefficients<br>
   *  <b>Description:</b> This class encapsulates all the
   *  coefficients needed for the calculation.<br>
   *  <b>Copyright:</b> Copyright (c) 2001 <br>
   *  <b>Company:</b> <br>
   */

  class SS_2006_AttenRelCoefficients
      implements NamedObjectAPI {

    protected final static String C = "SS_2006_AttenRelCoefficients";
    protected final static boolean D = false;
    /** For serialization. */
    private static final long serialVersionUID = 1234567890987654399L;

    protected String name;
    protected double period = -1;
    protected double b1;
    protected double vRef;
    protected double c;
    protected double b2;
    protected double tau;
    protected double e1;
    protected double e3;

    /**
     *  Constructor for the SS_2006_AttenRelCoefficients object that sets all values at once
     *
     * @param  name  Description of the Parameter
     */
    public SS_2006_AttenRelCoefficients(String name, double period,
                                        double b1, double vRef, double c,
                                        double b2, double tau,
                                        double e1, double e3) {

      this.name = name;
      this.period = period;
      this.b1 = b1;
      this.vRef = vRef;
      this.c = c;
      this.b2 = b2;
      this.tau = tau;
      this.e1 = e1;
      this.e3 = e3;
    }

    /**
     *  Gets the name attribute
     *
     * @return    The name value
     */
    public String getName() {
      return name;
    }

    /**
     *  Debugging - prints out all cefficient names and values
     *
     * @return    Description of the Return Value
     */
    public String toString() {

      StringBuffer b = new StringBuffer();
      b.append(C);
      b.append("\n  Period = " + period);
      b.append("\n  b1 = " + b1);
      b.append("\n  vRef = " + vRef);
      b.append("\n  c = " + c);
      b.append("\n  b2 = " + b2);
      b.append("\n  tau = " + tau);
      b.append("\n  e1 = " + e1);
      b.append("\n e3 = " + e3);
      return b.toString();
    }
  }


}
