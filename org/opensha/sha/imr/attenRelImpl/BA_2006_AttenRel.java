package org.opensha.sha.imr.attenRelImpl;

import java.util.*;

import org.opensha.data.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.imr.*;
import org.opensha.sha.param.*;
import org.opensha.util.FaultUtils;

/**
 * <b>Title:</b> BA_2006_AttenRel<p>
 *
 * <b>Description:</b> This implements the Attenuation Relationship
 * developed by Boore (2005) <p>
 *
 * Supported Intensity-Measure Parameters:  BELOW NEEDS TO BE UPDATED<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>saParam - Response Spectral Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>distanceRupParam - closest distance to surface projection of fault
 * <LI>siteTypeParam - "Rock/Shallow-Soil" versus "Deep-Soil"
 * <LI>fltTypeParam - Style of faulting
 * <LI>isOnHangingWallParam - tells if site is directly over the rupture surface
 * <LI>componentParam - Component of shaking (only one)
 * <LI>stdDevTypeParam - The type of standard deviation
 * </UL><p>
 *
 * @author     Edward H. Field
 * @created    April, 2002
 * @version    1.0
 */


public class BA_2006_AttenRel
    extends AttenuationRelationship implements
    AttenuationRelationshipAPI,
    NamedObjectAPI, ParameterChangeListener {

  // Debugging stuff
  private final static String C = "BA_2006_AttenRel";
  private final static boolean D = false;
  public final static String SHORT_NAME = "Boore2006";


  // Name of IMR
  public final static String NAME = "Boore & Atkinson (2006)";

  // coefficients:
  // note that index 0 is for PGA4nl (rock-PGA for computing amp factor),
  //index 1 is for PGV and index 2 is for PGA
  double[] period = {-2,-1,0,0.1,0.2,1,2,3};
  double[] e01 = {-0.96402,4.4555,-0.99856,-0.37456,-0.02226,-1.1229,-1.91959,-2.463};
  double[] e02 = {-0.96402,4.51767,-0.96172,-0.33081,0.00076,-1.08836,-1.84131,-2.37551};
  double[] e03 = {-0.96402,4.09492,-1.21708,-0.54534,-0.17985,-1.43789,-2.27626,-2.85859};
  double[] e04 = {-0.96402,4.48774,-0.97485,-0.39063,0.01211,-1.05239,-1.97819,-2.55923};
  double[] e05 = {0.29795,0.45269,0.39881,0.21476,0.46512,0.71081,0.7488,0.78104};
  double[] e06 = {-0.20341,-0.10949,-0.11032,-0.13716,-0.15204,-0.19137,-0.30555,-0.41815};
  double[] e07 = {0,0,0,0,0,0,0.33411,0.72716};
  double[] e08 = {0,0,0,0,0,0,0,0};
  double[] mh = {7,8.5,7,7,7,	7,7,7};
  double[] c01 = {-0.55,-0.8477,-0.7258,-0.7264,-0.6368,-0.8908,-0.8771,	-0.8209};
  double[] c02	={0,0.1152,0.1289,0.1297,0.04354,0.109,0.1002,0.06827};
  double[] c03	={0.01151,-0.00622,-0.01151,-0.01359,-0.00967,-0.00322,-0.00258,-0.00196};
  double[] c04	={0,0,0,0,0,0,0,0};
  double[] mref ={6,4.5,4.5,4.5,4.5,4.5,4.5,4.5};
  double[] rref	={5,5,5,	5,5,5,5,5};
  double[] h =	{3,4.4,3.2,3.8,4,4.6,	4.7,4.8};
  double[] blin		={-0.6,-0.36,-0.25,-0.31,-0.7,-0.73,-0.74};
  double[] vref	=	{760	,760,760,760,760,760,760};
  double[] b1	=	{-0.5,-0.64,-0.6,-0.52,-0.44,-0.38,-0.34};
  double[] b2	=	{-0.06,-0.14,-0.13,-0.19,0,0,0};
  double[] v1	=	{180	,180,180,180,180,180,180};
  double[] v2	=	{300,300,300,300,300,300,300};
  double[] pga_low=	{0.06,0.06,0.06,0.06,0.06,0.06,0.06};
  double[] sig1	=	{0.518,0.507,0.532,0.525,0.578,0.587,0.571};
  double[] sig2u	=	{0.288,0.267,0.332,0.286,0.306,0.398,0.414};
  double[] sigtu	=	{0.592,0.571,0.626,0.596,0.654,0.709,0.705};
  double[] sig2m	=	{0.26,0.262,0.334,0.288,0.29,0.389,0.403};
  double[] sigtm	=	{0.58,0.569,0.629,0.599,0.647,0.705,0.7};

  private HashMap indexFromPerHashMap;

  private int iper;
  private double vs30, rjb, mag;
  private String stdDevType;
  private boolean parameterChange;

  // ?????????????????????????????????????
  protected final static Double MAG_WARN_MIN = new Double(4.5);
  protected final static Double MAG_WARN_MAX = new Double(8.5);
  protected final static Double DISTANCE_JB_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_JB_WARN_MAX = new Double(200.0);
  protected final static Double VS30_WARN_MIN = new Double(120.0);
  protected final static Double VS30_WARN_MAX = new Double(2000.0);

  
  // style of faulting options
  public final static String FLT_TYPE_UNKNOWN = "Unknown";
  public final static String FLT_TYPE_STRIKE_SLIP = "Strike-Slip";
  public final static String FLT_TYPE_REVERSE = "Reverse";
  public final static String FLT_TYPE_NORMAL = "Normal";
  public final static String FLT_TYPE_DEFAULT = "Unknown";

  
  
  /**
   * The DistanceRupParameter, closest distance to fault surface.
   */
  private DistanceJBParameter distanceJBParam = null;
  private final static Double DISTANCE_JB_DEFAULT = new Double(0);

  // for issuing warnings:
  private transient ParameterChangeWarningListener warningListener = null;

  /**
   *  This initializes several ParameterList objects.
   */
  public BA_2006_AttenRel(ParameterChangeWarningListener
                                    warningListener) {

    super();

    this.warningListener = warningListener;

    initSupportedIntensityMeasureParams();
    indexFromPerHashMap = new HashMap();
    for (int i = 3; i < period.length ; i++) {
      indexFromPerHashMap.put(new Double(period[i]), new Integer(i));
    }

    initEqkRuptureParams();
    initPropagationEffectParams();
    initSiteParams();
    initOtherParams();

    initIndependentParamLists(); // This must be called after the above
    initParameterEventListeners(); //add the change listeners to the parameters

  }

  /**
   *  This sets the eqkRupture related parameters (magParam
   *  and fltTypeParam) based on the eqkRupture passed in.
   *  The internally held eqkRupture object is also set as that
   *  passed in.  Warning constrains are ingored.
   *
   * @param  eqkRupture  The new eqkRupture value
   * @throws InvalidRangeException thrown if rake is out of bounds
   */
  public void setEqkRupture(EqkRupture eqkRupture) throws InvalidRangeException {

    magParam.setValueIgnoreWarning(new Double(eqkRupture.getMag()));
    setFaultTypeFromRake(eqkRupture.getAveRake());
    this.eqkRupture = eqkRupture;
    setPropagationEffectParams();

  }

  /**
   *  This sets the site-related parameter (siteTypeParam) based on what is in
   *  the Site object passed in (the Site object must have a parameter with
   *  the same name as that in siteTypeParam).  This also sets the internally held
   *  Site object as that passed in.
   *
   * @param  site             The new site object
   * @throws ParameterException Thrown if the Site object doesn't contain a
   * Vs30 parameter
   */
  public void setSite(Site site) throws ParameterException {

    vs30Param.setValue(site.getParameter(this.VS30_NAME).getValue());
    this.site = site;
    setPropagationEffectParams();

  }

  /**
   * This sets the two propagation-effect parameters (distanceRupParam and
   * isOnHangingWallParam) based on the current site and eqkRupture.  The
   * hanging-wall term is rake independent (i.e., it can apply to strike-slip or
   * normal faults as well as reverse and thrust).  However, it is turned off if
   * the dip is greater than 70 degrees.  It is also turned off for point sources
   * regardless of the dip.  These specifications were determined from a series of
   * discussions between Ned Field, Norm Abrahamson, and Ken Campbell.
   */
  protected void setPropagationEffectParams() {

    if ( (this.site != null) && (this.eqkRupture != null)) {

      distanceJBParam.setValue(eqkRupture, site);

    }
  }

  /**
   * This function returns the array index for the coeffs corresponding to the chosen IMT
   */
  protected void setCoeffIndex() throws ParameterException {

    // Check that parameter exists
    if (im == null) {
      throw new ParameterException(C +
                                   ": updateCoefficients(): " +
                                   "The Intensity Measusre Parameter has not been set yet, unable to process."
          );
    }

    if (im.getName().equalsIgnoreCase(PGV_NAME)) {
      iper = 1;
    }
    else if (im.getName().equalsIgnoreCase(PGA_NAME)) {
      iper = 2;
    }
    else {
      iper = ( (Integer) indexFromPerHashMap.get(periodParam.getValue())).
          intValue();
    }

    parameterChange = true;
    intensityMeasureChanged = false;

  }

  /**
   * Calculates the mean of the exceedence probability distribution. <p>
   * @return    The mean value
   */
  public double getMean() {
    if (intensityMeasureChanged) {
      setCoeffIndex();
    }

    // check if distance is beyond the user specified max
    if (rjb > USER_MAX_DISTANCE) {
      return VERY_SMALL_MEAN;
    }
    if (parameterChange) {
      // remember that pga4nl term uses coeff index 0
      double pga4nl = Math.exp(getMean(0, vs30, rjb, mag, 0.0));
      return getMean(iper, vs30, rjb, mag, pga4nl);
    }
    return 0;
  }

  /**
   * @return    The stdDev value
   */
  public double getStdDev() {
    if (intensityMeasureChanged) {
      setCoeffIndex();
    }

    return getStdDev(iper, stdDevType);
  }

  /**
   * Determines the style of faulting from the rake angle (which
   * comes from the eqkRupture object) and fills in the
   * value of the fltTypeParam.  Options are "Reverse" if 150>rake>30,
   * "Strike-Slip" if rake is within 30 degrees of 0 or 180, and "Unkown"
   * otherwise (which means normal-faulting events are assigned as "Unkown";
   * confirmed by David Boore via email as being correct).
   *
   * @param rake                      in degrees
   * @throws InvalidRangeException    If not valid rake angle
   */
  protected void setFaultTypeFromRake(double rake) throws InvalidRangeException {
    FaultUtils.assertValidRake(rake);
    if (Math.abs(Math.sin(rake * Math.PI / 180)) <= 0.5) {
      fltTypeParam.setValue(FLT_TYPE_STRIKE_SLIP); // 0.5 = sin(30)
    }
    else if (rake >= 30 && rake <= 150) {
      fltTypeParam.setValue(FLT_TYPE_REVERSE);
    }
    else if (rake >= -150 && rake <= -30) {
        fltTypeParam.setValue(FLT_TYPE_NORMAL);
      }
    else {
      fltTypeParam.setValue(FLT_TYPE_UNKNOWN);
    }
  } 
  
  
  
  /**
   * Allows the user to set the default parameter values for the selected Attenuation
   * Relationship.
   */
  public void setParamDefaults() {

    vs30Param.setValue(VS30_DEFAULT);
    magParam.setValue(MAG_DEFAULT);
    distanceJBParam.setValue(DISTANCE_JB_DEFAULT);
    fltTypeParam.setValue(FLT_TYPE_DEFAULT);
    saParam.setValue(SA_DEFAULT);
    periodParam.setValue(PERIOD_DEFAULT);
    dampingParam.setValue(DAMPING_DEFAULT);
    pgaParam.setValue(PGA_DEFAULT);
    pgvParam.setValue(PGV_DEFAULT);
    stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);

    vs30 = ( (Double) vs30Param.getValue()).doubleValue();
    rjb = ( (Double) distanceJBParam.getValue()).doubleValue();
    mag = ( (Double) magParam.getValue()).doubleValue();
    stdDevType = (String) stdDevTypeParam.getValue();
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
    meanIndependentParams.addParameter(distanceJBParam);
    meanIndependentParams.addParameter(vs30Param);
    meanIndependentParams.addParameter(magParam);
    meanIndependentParams.addParameter(fltTypeParam);
    meanIndependentParams.addParameter(componentParam);

    // params that the stdDev depends upon
    stdDevIndependentParams.clear();
    stdDevIndependentParams.addParameter(stdDevTypeParam);
    stdDevIndependentParams.addParameter(componentParam);

    // params that the exceed. prob. depends upon
    exceedProbIndependentParams.clear();
    exceedProbIndependentParams.addParameterList(meanIndependentParams);
    
    exceedProbIndependentParams.addParameter(stdDevTypeParam);
    exceedProbIndependentParams.addParameter(sigmaTruncTypeParam);
    exceedProbIndependentParams.addParameter(sigmaTruncLevelParam);

    // params that the IML at exceed. prob. depends upon
    imlAtExceedProbIndependentParams.addParameterList(
        exceedProbIndependentParams);
    imlAtExceedProbIndependentParams.addParameter(exceedProbParam);
  }

  /**
   * This sets the site and eqkRupture, and the related parameters,
   *  from the propEffect object passed in. Warning constrains are ingored.
   * @param propEffect
   * @throws ParameterException Thrown if the Site object doesn't contain a
   * Vs30 parameter
   * @throws InvalidRangeException thrown if rake is out of bounds
   */
  public void setPropagationEffect(PropagationEffect propEffect) throws
      InvalidRangeException, ParameterException {

    this.site = propEffect.getSite();
    this.eqkRupture = propEffect.getEqkRupture();

    vs30Param.setValueIgnoreWarning(site.getParameter(VS30_NAME).getValue());

    magParam.setValueIgnoreWarning(new Double(eqkRupture.getMag()));
    setFaultTypeFromRake(eqkRupture.getAveRake());

    propEffect.setParamValue(distanceJBParam);
  }
  
  
  
  
  /**
   *  Creates the Site-Type parameter and adds it to the siteParams list.
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

    siteParams.clear();
    siteParams.addParameter(vs30Param);
  }

  /**
   *  Creates the two Potential Earthquake parameters (magParam and
   *  fltTypeParam) and adds them to the eqkRuptureParams
   *  list. Makes the parameters noneditable.
   */
  protected void initEqkRuptureParams() {

    // Create magParam & other common EqkRup-related params
    super.initEqkRuptureParams();

    //  Create and add warning constraint to magParam:
    DoubleConstraint warn = new DoubleConstraint(MAG_WARN_MIN, MAG_WARN_MAX);
    warn.setNonEditable();
    magParam.setWarningConstraint(warn);
    magParam.addParameterChangeWarningListener(warningListener);
    magParam.setNonEditable();

    StringConstraint constraint = new StringConstraint();
    constraint.addString(FLT_TYPE_UNKNOWN);
    constraint.addString(FLT_TYPE_STRIKE_SLIP);
    constraint.addString(FLT_TYPE_NORMAL);
    constraint.addString(FLT_TYPE_REVERSE);
    constraint.setNonEditable();
    fltTypeParam = new StringParameter(FLT_TYPE_NAME, constraint, null);
    fltTypeParam.setInfo(FLT_TYPE_INFO);
    fltTypeParam.setNonEditable();

    eqkRuptureParams.clear();
    eqkRuptureParams.addParameter(magParam);
    eqkRuptureParams.addParameter(fltTypeParam);
  }

  /**
   *  Creates the Propagation Effect parameters and adds them to the
   *  propagationEffectParams list. Makes the parameters noneditable.
   */
  protected void initPropagationEffectParams() {

    distanceJBParam = new DistanceJBParameter();
    distanceJBParam.addParameterChangeWarningListener(warningListener);
    DoubleConstraint warn = new DoubleConstraint(DISTANCE_JB_WARN_MIN,
                                                 DISTANCE_JB_WARN_MAX);
    warn.setNonEditable();
    distanceJBParam.setWarningConstraint(warn);
    distanceJBParam.setNonEditable();

    propagationEffectParams.addParameter(distanceJBParam);

  }

  /**
   *  Creates the two supported IM parameters (PGA and SA), as well as the
   *  independenParameters of SA (periodParam and dampingParam) and adds
   *  them to the supportedIMParams list. Makes the parameters noneditable.
   */
  protected void initSupportedIntensityMeasureParams() {

    // Create saParam (& its dampingParam) and pgaParam:
    super.initSupportedIntensityMeasureParams();

    // Create saParam's "Period" independent parameter:
    DoubleDiscreteConstraint periodConstraint = new DoubleDiscreteConstraint();
    for (int i = 3; i < period.length; i++) {
      periodConstraint.addDouble(new Double(period[i]));
    }
    periodConstraint.setNonEditable();
    periodParam = new DoubleDiscreteParameter(PERIOD_NAME, periodConstraint,
                                              PERIOD_UNITS, null);
    periodParam.setInfo(PERIOD_INFO);
    periodParam.setNonEditable();

    //Create PGV Parameter (pgvParam):
    DoubleConstraint pgvConstraint = new DoubleConstraint(PGV_MIN, PGV_MAX);
    pgvConstraint.setNonEditable();
    pgvParam = new WarningDoubleParameter(PGV_NAME, pgvConstraint, PGV_UNITS);
    pgvParam.setInfo(PGV_INFO);
    DoubleConstraint warn = new DoubleConstraint(PGV_WARN_MIN, PGV_WARN_MAX);
    warn.setNonEditable();
    pgvParam.setWarningConstraint(warn);
    pgvParam.setNonEditable();

    // Set damping constraint as non editable since no other options exist
    dampingConstraint.setNonEditable();

    // Add SA's independent parameters:
    saParam.addIndependentParameter(dampingParam);
    saParam.addIndependentParameter(periodParam);

    // Now Make the parameter noneditable:
    saParam.setNonEditable();

    // Add the warning listeners:
    saParam.addParameterChangeWarningListener(warningListener);
    pgaParam.addParameterChangeWarningListener(warningListener);
    pgvParam.addParameterChangeWarningListener(warningListener);

    // Put parameters in the supportedIMParams list:
    supportedIMParams.clear();
    supportedIMParams.addParameter(saParam);
    supportedIMParams.addParameter(pgaParam);
    supportedIMParams.addParameter(pgvParam);

  }

  /**
   *  Creates other Parameters that the mean or stdDev depends upon,
   *  such as the Component or StdDevType parameters.
   */
  protected void initOtherParams() {

    // init other params defined in parent class
    super.initOtherParams();

    // the Component Parameter
    StringConstraint constraint = new StringConstraint();
    constraint.addString(COMPONENT_AVE_HORZ);
    constraint.setNonEditable();
    componentParam = new StringParameter(COMPONENT_NAME, constraint,
                                         COMPONENT_DEFAULT);
    componentParam.setInfo(COMPONENT_INFO);
    componentParam.setNonEditable();

    // the stdDevType Parameter
    StringConstraint stdDevTypeConstraint = new StringConstraint();
    stdDevTypeConstraint.addString(STD_DEV_TYPE_TOTAL);
    stdDevTypeConstraint.addString(STD_DEV_TYPE_NONE);
    stdDevTypeConstraint.addString(STD_DEV_TYPE_INTER);
    stdDevTypeConstraint.addString(STD_DEV_TYPE_INTRA);
    stdDevTypeConstraint.setNonEditable();
    stdDevTypeParam = new StringParameter(STD_DEV_TYPE_NAME,
                                          stdDevTypeConstraint,
                                          STD_DEV_TYPE_DEFAULT);
    stdDevTypeParam.setInfo(STD_DEV_TYPE_INFO);
    stdDevTypeParam.setNonEditable();

    // add these to the list
    otherParams.addParameter(componentParam);
    otherParams.addParameter(stdDevTypeParam);

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

  public double getMean(int iper, double vs30, double rjb, double mag,
                        double pga4nl) {

    // remember that pga4ln term uses coeff index 0
    double Fm, Fd, Fs;
    int U =0, S=0, N=0, R=0;
    String fltType = (String)fltTypeParam.getValue();
    if(fltType.equals(FLT_TYPE_UNKNOWN))
    	  U=1;
    else if(fltType.equals(FLT_TYPE_NORMAL))
    	 N=1;
    else if(fltType.equals(FLT_TYPE_STRIKE_SLIP))
    	 S=1;
    else
    	 R=1;
    if (mag <= mh[iper]) {
      Fm = e01[iper]*U + e02[iper] *S +
          e03[iper]*N+e04[iper]*R+e05[iper]* (mag - mh[iper]) +e06[iper] * Math.pow( (mag - mh[iper]), 2);
    }
    else {
      Fm = e01[iper]*U +e02[iper]*S+e03[iper]*N+ e04[iper]*R+
      	e07[iper] * (mag - mh[iper])+ e08[iper] *Math.pow((mag - mh[iper]),2);
    }

    double r = Math.sqrt(rjb * rjb + h[iper] * h[iper]);
    Fd = (c01[iper] + c02[iper] * (mag - mref[iper]))
        * Math.log(r / rref[iper]) + (c03[iper] + c04[iper] * (mag - mref[iper]))* (r - rref[iper]);

   	//if mean for iper then make Fs to be zero
    if(iper == 0)
    		Fs =0;
    else{
	    double bnl = 0;
	    if (vs30 <= v1[iper]) {
	      bnl = b1[iper];
	    }
	    else if (vs30 <= v2[iper] && vs30 > v1[iper]) {
	      bnl = (b1[iper] - b2[iper]) * Math.log(vs30 / v2[iper]) /
	          Math.log(v1[iper] / v2[iper]) + b2[iper];
	    }
	    else if (vs30 <= vref[iper] && vs30 > v2[iper]) {
	      bnl = b2[iper] * Math.log(vs30 / vref[iper]) /
	          Math.log(v2[iper] / vref[iper]);
	    }
	    else if (vs30 > vref[iper]) {
	      bnl = 0.0;
	    }   
    		if (pga4nl <= 0.06) {
    			Fs = blin[iper] * Math.log(vs30 / vref[iper]) + bnl * Math.log(0.06 / 0.1);
    		}	
    		else {
    			Fs = blin[iper] * Math.log(vs30 / vref[iper]) +
    			bnl * Math.log(pga4nl / 0.1);
    		}
    }
    return (Fm + Fd + Fs);
  }

  public double getStdDev(int iper, String stdDevType) {

    if (stdDevType.equals(STD_DEV_TYPE_NONE)) {
      return 0;
    }
    else if (stdDevType.equals(STD_DEV_TYPE_INTRA)) {
      return sig2u[iper];
    }
    else if (stdDevType.equals(STD_DEV_TYPE_INTER)) {
      return sig1[iper];
    }
    else { // it's total sigma
      return Math.sqrt(sig1[iper] * sig1[iper] + sig2u[iper] * sig2u[iper]);
    }
  }

  /**
   * This listens for parameter changes and updates the primitive parameters accordingly
   * @param e ParameterChangeEvent
   */
  public void parameterChange(ParameterChangeEvent e) {

    String pName = e.getParameterName();
    Object val = e.getNewValue();
    parameterChange = true;
    if (pName.equals(DistanceJBParameter.NAME)) {
      rjb = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.VS30_NAME)) {
      vs30 = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.MAG_NAME)) {
      mag = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.STD_DEV_TYPE_NAME)) {
      stdDevType = (String) val;
    }
    else if (pName.equals(this.PERIOD_NAME) && intensityMeasureChanged) {
      setCoeffIndex();
    }
  }

  /**
   * Allows to reset the change listeners on the parameters
   */
  public void resetParameterEventListeners(){
    distanceJBParam.removeParameterChangeListener(this);
    vs30Param.removeParameterChangeListener(this);
    magParam.removeParameterChangeListener(this);
    stdDevTypeParam.removeParameterChangeListener(this);
    periodParam.removeParameterChangeListener(this);

    this.initParameterEventListeners();
  }

  /**
   * Adds the parameter change listeners. This allows to listen to when-ever the
   * parameter is changed.
   */
  protected void initParameterEventListeners() {

    distanceJBParam.addParameterChangeListener(this);
    vs30Param.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    stdDevTypeParam.addParameterChangeListener(this);
    periodParam.addParameterChangeListener(this);
  }

}
