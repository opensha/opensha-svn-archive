package org.opensha.sha.imr.attenRelImpl;

import java.util.*;

import org.opensha.data.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.imr.*;
import org.opensha.sha.param.*;
import org.opensha.sha.surface.*;

/**
 * <b>Title:</b> CB_2005_prelim_AttenRel<p>
 *
 * <b>Description:</b> This implements the Attenuation Relationship
 * developed by Campbell & Bozorgnia (2005) <p>
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


public class CB_2005_prelim_AttenRel
    extends AttenuationRelationship implements
    AttenuationRelationshipAPI,
    NamedObjectAPI, ParameterChangeListener {

  // Debugging stuff
  private final static String C = "CB_2005_prelim_AttenRel";
  private final static boolean D = false;

  // Name of IMR
  public final static String NAME = "Campbell & Bozorgnia (2005 prelim)";

  // coefficients:
  private static double[] per = {
      0, 0.2, 1};

  private static double[] c0_EPRI = {
      1.387, -0.394, -3.117};
  private static double[] c0_PEN = {
      1.481, -0.112, -3.109};
  private static double[] c1_EPRI = {
       -0.019, 0.461, 0.553};
  private static double[] c1_PEN = {
       -0.052, 0.400, 0.550};
  private static double[] c2_EPRI = {
       -0.439, -0.650, -0.622};
  private static double[] c2_PEN = {
       -0.378, -0.575, -0.586};
  private static double[] c2prime_EPRI = {
       -0.385, -0.650, -0.622};
  private static double[] c2prime_PEN = {
       -0.353, -0.575, -0.586};
  private static double[] c3_EPRI = {
       -2.570, -2.050, -2.246};
  private static double[] c3_PEN = {
       -2.480, -1.914, -2.199};
  private static double[] c4_EPRI = {
      0.224, 0.123, 0.212};
  private static double[] c4_PEN = {
      0.218, 0.117, 0.207};
  private static double[] c5_EPRI = {
      6.07, 8.57, 3.17};
  private static double[] c5_PEN = {
      6.41, 9.48, 3.21};
  private static double[] c6_EPRI = {
      0.419, 0.368, 0.355};
  private static double[] c6_PEN = {
      0.409, 0.333, 0.359};
  private static double[] c7_EPRI = {
       -0.107, -0.035, -0.150};
  private static double[] c7_PEN = {
       -0.088, -0.007, -0.142};
  private static double[] c8_EPRI = {
      0.742, 0.798, 0.828};
  private static double[] c8_PEN = {
      0.687, 0.668, 0.825};
  private static double[] c9_EPRI = {
      0.988, 1.305, 1.429};
  private static double[] c9_PEN = {
      1.034, 2.155, 1.579};
  private static double[] c10_EPRI = {
      0.046, 0.071, 0.146};
  private static double[] c10_PEN = {
      0.029, 0.047, 0.137};
  private static double[] k1_EPRI = {
      1035, 1229, 444};
  private static double[] k1_PEN = {
      865, 748, 401};
  private static double[] k2_EPRI = {
       -1.140, -1.479, -1.700};
  private static double[] k2_PEN = {
       -1.186, -2.188, -1.955};
  private static double[] k3_EPRI = {
      1.346, 1.312, 1.179};
  private static double[] k3_PEN = {
      1.346, 1.312, 1.179};
  private static double[] k4_EPRI = {
      1.838, 1.856, 1.929};
  private static double[] k4_PEN = {
      1.838, 1.856, 1.929};
  private static double[] sigma_EPRI = {
      0.475, 0.524, 0.568};
  private static double[] sigma_PEN = {
      0.473, 0.522, 0.568};
  private static double[] tau_EPRI = {
      0.269, 0.341, 0.353};
  private static double[] tau_PEN = {
      0.250, 0.310, 0.352};
  private static double[] sigma_ln_EPRI = {
      0.546, 0.625, 0.669};
  private static double[] sigma_ln_PEN = {
      0.535, 0.607, 0.668};

  private HashMap indexFromPerHashMap;

  private int iper;
  private double vs30, rjb, rRup, distRupJB_Fraction, rake, mag, depthTop,
      depthTo2pt5kmPerSec;
  private String stdDevType, nonLinearAmpModel;
  private boolean magSaturation;
  private boolean parameterChange;

  // ?????????????????????????????????????
  protected final static Double MAG_WARN_MIN = new Double(4.5);
  protected final static Double MAG_WARN_MAX = new Double(8.5);
  protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_RUP_WARN_MAX = new Double(200.0);
  protected final static Double VS30_WARN_MIN = new Double(120.0);
  protected final static Double VS30_WARN_MAX = new Double(2000.0);
  protected final static Double DEPTH_2pt5_WARN_MIN = new Double(0);
  protected final static Double DEPTH_2pt5_WARN_MAX = new Double(7000);

  /**
   * nonLinearAmpModelParam, a StringParameter that represents the type of
   * nonlinearity model.
   */
  protected StringParameter nonLinearAmpModelParam = null;
  public final static String NONLIN_MODEL_TYPE_NAME = "Nonlinear Model";
  public final static String NONLIN_MODEL_TYPE_INFO =
      "Type of nonlinear amplification model";
  public final static String NONLIN_MODEL_TYPE_PEN = "PEN";
  public final static String NONLIN_MODEL_TYPE_EPRI = "EPRI";
  public final static String NONLIN_MODEL_TYPE_DEFAULT = NONLIN_MODEL_TYPE_PEN;

  /**
   * magSaturationParam, a BooleanParameter that represents the type of
   * mag saturation model.
   */
  protected BooleanParameter magSaturationParam = null;
  public final static String MAG_SATURATION_NAME = "Mag Saturation";
  public final static String MAG_SATURATION_INFO =
      "Applies the magnitude-saturation option (c-prime rather than c coeff)";
  public final static Boolean MAG_SATURATION_DEFAULT = new Boolean(false);

  /**
   * The DistanceRupParameter, closest distance to fault surface.
   */
  private DistanceRupParameter distanceRupParam = null;
  private final static Double DISTANCE_RUP_DEFAULT = new Double(0);

  /**
   * Joyner-Boore Distance parameter, used as a proxy for computing their
   * hanging-wall term from a site and eqkRupture.
   */
  private DistRupMinusJB_OverRupParameter distRupMinusJB_OverRupParam = null;
  private final static Double DISTANCE_RUP_MINUS_JB_DEFAULT = new Double(0);
  // No waring constraint needed for this



  // for issuing warnings:
  private transient ParameterChangeWarningListener warningListener = null;

  /**
   *  This initializes several ParameterList objects.
   */
  public CB_2005_prelim_AttenRel(ParameterChangeWarningListener warningListener) {

    super();

    this.warningListener = warningListener;

    initSupportedIntensityMeasureParams();
    indexFromPerHashMap = new HashMap();
    for (int i = 0; i < per.length; i++) {
      indexFromPerHashMap.put(new Double(per[i]), new Integer(i));
    }

    initEqkRuptureParams();
    initPropagationEffectParams();
    initSiteParams();
    initOtherParams();

    initIndependentParamLists(); // This must be called after the above
    initPameterListeners(); //add the change listeners to the parameters

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
    rakeParam.setValue(eqkRupture.getAveRake());
    GriddedSurfaceAPI surface = eqkRupture.getRuptureSurface();
    double depth = surface.getLocation(0, 0).getDepth();
    rupTopDepthParam.setValue(depth);

//    setFaultTypeFromRake(eqkRupture.getAveRake());
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
    depthTo2pt5kmPerSecParam.setValueIgnoreWarning(site.getParameter(this.DEPTH_2pt5_NAME).
                                      getValue());
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

      distanceRupParam.setValue(eqkRupture, site);
      distRupMinusJB_OverRupParam.setValue(eqkRupture, site);

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

    if (im.getName().equalsIgnoreCase(SA_NAME)) {
      iper = ( (Integer) indexFromPerHashMap.get(periodParam.getValue())).
          intValue();
    }
    else {
      iper = 0;
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
    if (rRup > USER_MAX_DISTANCE) {
      return VERY_SMALL_MEAN;
    }

    if (nonLinearAmpModel.equals(this.NONLIN_MODEL_TYPE_EPRI)) {
      double pgar = Math.exp(getMean_EPRI(0, 1100, rRup, distRupJB_Fraction,
                                          rake, mag,
                                          depthTop, depthTo2pt5kmPerSec,
                                          magSaturation,
                                          0));
      return getMean_EPRI(iper, vs30, rRup, distRupJB_Fraction, rake, mag,
                          depthTop, depthTo2pt5kmPerSec, magSaturation, pgar);
    }
    else {
      double pgar = Math.exp(getMean_PEN(0, 1100, rRup, distRupJB_Fraction,
                                         rake, mag,
                                         depthTop, depthTo2pt5kmPerSec,
                                         magSaturation,
                                         0));
      return getMean_PEN(iper, vs30, rRup, distRupJB_Fraction, rake, mag,
                         depthTop, depthTo2pt5kmPerSec, magSaturation, pgar);

    }
  }

  /**
   * @return    The stdDev value
   */
  public double getStdDev() {
    if (intensityMeasureChanged) {
      setCoeffIndex();
    }

    if (nonLinearAmpModel.equals(this.NONLIN_MODEL_TYPE_EPRI)) {
      return getStdDev_EPRI(iper, stdDevType);
    }
    else {
      return getStdDev_PEN(iper, stdDevType);
    }
  }

  /**
   * Allows the user to set the default parameter values for the selected Attenuation
   * Relationship.
   */
  public void setParamDefaults() {

    vs30Param.setValue(VS30_DEFAULT);
    magParam.setValue(MAG_DEFAULT);
    rakeParam.setValue(RAKE_DEFAULT);

    nonLinearAmpModelParam.setValue(this.NONLIN_MODEL_TYPE_DEFAULT);
    rupTopDepthParam.setValue(RUP_TOP_DEFAULT);
    distanceRupParam.setValue(DISTANCE_RUP_DEFAULT);
    distRupMinusJB_OverRupParam.setValue(this.DISTANCE_RUP_MINUS_JB_DEFAULT);
    saParam.setValue(SA_DEFAULT);
    periodParam.setValue(PERIOD_DEFAULT);
    dampingParam.setValue(DAMPING_DEFAULT);
    pgaParam.setValue(PGA_DEFAULT);
    componentParam.setValue(COMPONENT_DEFAULT);
    stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);
    magSaturationParam.setValue(MAG_SATURATION_DEFAULT);
    depthTo2pt5kmPerSecParam.setValue(this.DEPTH_2pt5_DEFAULT);

    vs30 = ( (Double) vs30Param.getValue()).doubleValue();
    distRupJB_Fraction = ( (Double) distRupMinusJB_OverRupParam.getValue()).
        doubleValue();
    rRup = ( (Double) distanceRupParam.getValue()).doubleValue();
    nonLinearAmpModel = (String) nonLinearAmpModelParam.getValue();
    rake = ( (Double) rakeParam.getValue()).doubleValue();
    mag = ( (Double) magParam.getValue()).doubleValue();
    magSaturation = ( (Boolean) magSaturationParam.getValue()).booleanValue();
    depthTop = ( (Double) rupTopDepthParam.getValue()).doubleValue();
    stdDevType = (String) stdDevTypeParam.getValue();
    depthTo2pt5kmPerSec = ( (Double) depthTo2pt5kmPerSecParam.getValue()).
        doubleValue();
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
    meanIndependentParams.addParameter(distRupMinusJB_OverRupParam);
    meanIndependentParams.addParameter(vs30Param);
    meanIndependentParams.addParameter(depthTo2pt5kmPerSecParam);
    meanIndependentParams.addParameter(magParam);
    meanIndependentParams.addParameter(rakeParam);
    meanIndependentParams.addParameter(rupTopDepthParam);
    meanIndependentParams.addParameter(componentParam);
    meanIndependentParams.addParameter(magSaturationParam);
    meanIndependentParams.addParameter(nonLinearAmpModelParam);

    // params that the stdDev depends upon
    stdDevIndependentParams.clear();
    stdDevIndependentParams.addParameter(stdDevTypeParam);
    stdDevIndependentParams.addParameter(componentParam);
    stdDevIndependentParams.addParameter(nonLinearAmpModelParam);

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

    // create and add the warning constraint:
    DoubleConstraint warn2 = new DoubleConstraint(DEPTH_2pt5_WARN_MIN,
                                                  DEPTH_2pt5_WARN_MAX);
    warn2.setNonEditable();
    depthTo2pt5kmPerSecParam.setWarningConstraint(warn2);
    depthTo2pt5kmPerSecParam.addParameterChangeWarningListener(warningListener);
    depthTo2pt5kmPerSecParam.setNonEditable();

    siteParams.clear();
    siteParams.addParameter(vs30Param);
    siteParams.addParameter(depthTo2pt5kmPerSecParam);

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

    eqkRuptureParams.clear();
    eqkRuptureParams.addParameter(magParam);
    eqkRuptureParams.addParameter(rakeParam);
    eqkRuptureParams.addParameter(rupTopDepthParam);
  }

  /**
   *  Creates the Propagation Effect parameters and adds them to the
   *  propagationEffectParams list. Makes the parameters noneditable.
   */
  protected void initPropagationEffectParams() {

    distanceRupParam = new DistanceRupParameter();
    distanceRupParam.addParameterChangeWarningListener(warningListener);
    DoubleConstraint warn = new DoubleConstraint(DISTANCE_RUP_WARN_MIN,
                                                 DISTANCE_RUP_WARN_MAX);
    warn.setNonEditable();
    distanceRupParam.setWarningConstraint(warn);
    distanceRupParam.setNonEditable();

    //create distRupMinusJB_OverRupParam
    distRupMinusJB_OverRupParam = new DistRupMinusJB_OverRupParameter();
    distRupMinusJB_OverRupParam.setNonEditable();

    propagationEffectParams.addParameter(distanceRupParam);
    propagationEffectParams.addParameter(distRupMinusJB_OverRupParam);

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
    for (int i = 0; i < per.length; i++) {
      periodConstraint.addDouble(new Double(per[i]));
    }
    periodConstraint.setNonEditable();
    periodParam = new DoubleDiscreteParameter(PERIOD_NAME, periodConstraint,
                                              PERIOD_UNITS, null);
    periodParam.setInfo(PERIOD_INFO);
    periodParam.setNonEditable();

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

    // Put parameters in the supportedIMParams list:
    supportedIMParams.clear();
    supportedIMParams.addParameter(saParam);
    supportedIMParams.addParameter(pgaParam);

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

    // make nonlinear model parameter
    StringConstraint nonLinConstr = new StringConstraint();
    nonLinConstr.addString(NONLIN_MODEL_TYPE_PEN);
    nonLinConstr.addString(NONLIN_MODEL_TYPE_EPRI);
    nonLinearAmpModelParam = new StringParameter(NONLIN_MODEL_TYPE_NAME,
                                                 nonLinConstr,
                                                 NONLIN_MODEL_TYPE_DEFAULT);
    nonLinearAmpModelParam.setInfo(NONLIN_MODEL_TYPE_INFO);
    nonLinearAmpModelParam.setNonEditable();

    // make magSaturationParam
    magSaturationParam = new BooleanParameter(MAG_SATURATION_NAME,
                                              MAG_SATURATION_DEFAULT);
    magSaturationParam.setInfo(MAG_SATURATION_INFO);
    magSaturationParam.setNonEditable();

    // add these to the list
    otherParams.addParameter(componentParam);
    otherParams.addParameter(stdDevTypeParam);
    otherParams.addParameter(nonLinearAmpModelParam);
    otherParams.addParameter(magSaturationParam);

  }

  /**
   * get the name of this IMR
   *
   * @returns the name of this IMR
   */
  public String getName() {
    return NAME;
  }

  public double getMean_PEN(int iper, double vs30, double rRup,
                            double distRupJB_Fraction,
                            double rake, double mag, double depthTop,
                            double depthTo2pt5kmPerSec,
                            boolean magSaturation, double pgar) {

    double c = 1.88;
    double n = 1.18;

/*
    System.out.println("iper=" + iper +", vs30="+ vs30 +", rRup="+ rRup +", (rRup-rJB)/rRup="+
                       distRupJB_Fraction+", rake="+ rake+", mag="+ mag+", depthTop="+
                       depthTop+", depthTo2pt5kmPerSec="+ depthTo2pt5kmPerSec+", magSaturation=" +
                       magSaturation +", pgar="+ pgar+", nonLinModel=PEN");
 */

    double rjb, Frv, Fn, SR, Ff, F1;
    rjb = rRup - distRupJB_Fraction * rRup;

    //       Mechanism
    if (rake > 30 && rake < 150) {
      Frv = 1.0;
    }
    else {
      Frv = 0.0;
    }

    if (rake < -30 && rake > -150) {
      Fn = 1.0;
    }
    else {
      Fn = 0.0;
    }

    if (depthTop < 1) {
      Ff = depthTop;
    }
    else {
      Ff = 1;
    }

    //Mag dependence
    if (mag <= 6.5) {
      F1 = c0_PEN[iper] + c1_PEN[iper] * mag;
    }
    else {
      if (magSaturation) {
        F1 = c0_PEN[iper] + c1_PEN[iper] * mag + c2prime_PEN[iper] * (mag - 6.5);
      }
      else {
        F1 = c0_PEN[iper] + c1_PEN[iper] * mag + c2_PEN[iper] * (mag - 6.5);
      }
    }

    //Distance dependence
    double F2 = (c3_PEN[iper] + c4_PEN[iper] * mag) *
        Math.log(Math.sqrt(rRup * rRup + c5_PEN[iper] * c5_PEN[iper]));

    //Faulting Style dependence
    double F3 = c6_PEN[iper] * Frv * Ff + c7_PEN[iper] * Fn;

    //Hanging Wall dependence
    double FhwM, FhwR, FhwH;
    if (rjb == 0) {
      FhwR = 1;
    }
    else {
      FhwR = distRupJB_Fraction;
    }

    if (mag <= 6) {
      FhwM = 0;
    }
    else if (mag < 6.5) {
      FhwM = (mag - 6) / 0.5;
    }
    else {
      FhwM = 1;
    }

    if (depthTop >= 20) {
      FhwH = 0;
    }
    else {
      FhwH = (20 - depthTop) / 20;
    }

    double F4 = c8_PEN[iper] * Frv * FhwR * FhwM * FhwH;

    //Shallow site condition dependence
    double F5;
    if (vs30 < k1_PEN[iper]) {
      F5 = c9_PEN[iper] * Math.log(vs30 / k1_PEN[iper]) +
          k2_PEN[iper] *
          (Math.log(pgar + c * Math.pow((vs30 / k1_PEN[iper]), n)) -
          Math.log(pgar + c));
    }
    else {
      F5 = (c9_PEN[iper] + k2_PEN[iper] * n) * Math.log(vs30 / k1_PEN[iper]);
    }

    //Sediment depth dependence
    double F6;
    if (depthTo2pt5kmPerSec <= 1) {
      F6 = c10_PEN[iper] * (depthTo2pt5kmPerSec - 1);
    }
    else if (depthTo2pt5kmPerSec <= 3) {
      F6 = 0;
    }
    else {
      F6 = k3_PEN[iper] * (Math.exp( -10) - Math.exp( -3.333 * depthTo2pt5kmPerSec)) +
           k4_PEN[iper] * (Math.exp( -0.75) - Math.exp( -0.25 * depthTo2pt5kmPerSec));
    }
/*
double temp = Math.exp(F1 + F2 + F3 + F4 + F5 + F6);
System.out.println("F1="+(float)F1 +", F2="+ (float)F2 +", F3="+ (float)F3 +
                       ", F4="+ (float)F4 +", F5="+ (float)F5 +", F6="+ (float)F6+
        "meanIML="+(float)temp);
 */
    return (F1 + F2 + F3 + F4 + F5 + F6);
  }

  public double getMean_EPRI(int iper, double vs30, double rRup,
                             double distRupJB_Fraction,
                             double rake, double mag, double depthTop,
                             double depthTo2pt5kmPerSec,
                             boolean magSaturation, double pgar) {

    double c = 1.38;
    double n = 1.30;

    double rjb, Frv, Fn, SR, Ff, F1;
    rjb = rRup - distRupJB_Fraction * rRup;

    //       Mechanism
    if (rake > 30 && rake < 150) {
      Frv = 1.0;
    }
    else {
      Frv = 0.0;
    }

    if (rake < -30 && rake > -150) {
      Fn = 1.0;
    }
    else {
      Fn = 0.0;
    }

    if (depthTop < 1) {
      Ff = depthTop;
    }
    else {
      Ff = 1;
    }

    //Mag dependence
    if (mag <= 6.5) {
      F1 = c0_EPRI[iper] + c1_EPRI[iper] * mag;
    }
    else {
      if (magSaturation) {
        F1 = c0_EPRI[iper] + c1_EPRI[iper] * mag + c2prime_EPRI[iper] * (mag - 6.5);
      }
      else {
        F1 = c0_EPRI[iper] + c1_EPRI[iper] * mag + c2_EPRI[iper] * (mag - 6.5);
      }
    }

    //Distance dependence
    double F2 = (c3_EPRI[iper] + c4_EPRI[iper] * mag) *
        Math.log(Math.sqrt(rRup * rRup + c5_EPRI[iper] * c5_EPRI[iper]));

    //Faulting Style dependence
    double F3 = c6_EPRI[iper] * Frv * Ff + c7_EPRI[iper] * Fn;

    //Hanging Wall dependence
    double FhwM, FhwR, FhwH;
    if (rjb == 0) {
      FhwR = 1;
    }
    else {
      FhwR = distRupJB_Fraction;
    }

    if (mag <= 6) {
      FhwM = 0;
    }
    else if (mag < 6.5) {
      FhwM = (mag - 6) / 0.5;
    }
    else {
      FhwM = 1;
    }

    if (depthTop >= 20) {
      FhwH = 0;
    }
    else {
      FhwH = (20 - depthTop) / 20;
    }

    double F4 = c8_EPRI[iper] * Frv * FhwR * FhwM * FhwH;

    //Shallow site condition dependence
    double F5;
    if (vs30 < k1_EPRI[iper]) {
      F5 = c9_EPRI[iper] * Math.log(vs30 / k1_EPRI[iper]) +
                k2_EPRI[iper] *
                (Math.log(pgar + c * Math.pow((vs30 / k1_EPRI[iper]), n)) -
                 Math.log(pgar + c));    }
    else {
      F5 = (c9_EPRI[iper] + k2_EPRI[iper] * n) * Math.log(vs30 / k1_EPRI[iper]);
    }

    //Sediment depth dependence
    double F6;
    if (depthTo2pt5kmPerSec <= 1) {
      F6 = c10_EPRI[iper] * (depthTo2pt5kmPerSec - 1);
    }
    else if (depthTo2pt5kmPerSec <= 3) {
      F6 = 0;
    }
    else {
      F6 = k3_EPRI[iper] * (Math.exp( -10) - Math.exp( -3.333 * depthTo2pt5kmPerSec)) +
          k4_EPRI[iper] * (Math.exp( -0.75) - Math.exp( -0.25 * depthTo2pt5kmPerSec));
    }

    return (F1 + F2 + F3 + F4 + F5 + F6);
  }

  public double getStdDev_EPRI(int iper, String stdDevType) {

    double s = sigma_EPRI[iper];
    double t = tau_EPRI[iper];

    if (stdDevType.equals(STD_DEV_TYPE_NONE)) {
      return 0;
    }
    else if (stdDevType.equals(STD_DEV_TYPE_INTRA)) {
      return t;
    }
    else if (stdDevType.equals(STD_DEV_TYPE_INTER)) {
      return s;
    }
    else { // it's total sigma
      return Math.sqrt(t * t + s * s);
    }
  }

  public double getStdDev_PEN(int iper, String stdDevType) {

    double s = sigma_PEN[iper];
    double t = tau_PEN[iper];

    if (stdDevType.equals(STD_DEV_TYPE_NONE)) {
      return 0;
    }
    else if (stdDevType.equals(STD_DEV_TYPE_INTRA)) {
      return t;
    }
    else if (stdDevType.equals(STD_DEV_TYPE_INTER)) {
      return s;
    }
    else { // it's total sigma
      return Math.sqrt(t * t + s * s);
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
    if (pName.equals(DistanceRupParameter.NAME)) {
      rRup = ( (Double) val).doubleValue();
    }
    else if (pName.equals(DistRupMinusJB_OverRupParameter.NAME)) {
      distRupJB_Fraction = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.VS30_NAME)) {
      vs30 = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.DEPTH_2pt5_NAME)) {
      depthTo2pt5kmPerSec = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.MAG_NAME)) {
      mag = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.RAKE_NAME)) {
      rake = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.NONLIN_MODEL_TYPE_NAME)) {
      nonLinearAmpModel = (String) val;
    }
    else if (pName.equals(this.MAG_SATURATION_NAME)) {
      magSaturation = ( (Boolean) val).booleanValue();
    }
    else if (pName.equals(this.RUP_TOP_NAME)) {
      depthTop = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.STD_DEV_TYPE_NAME)) {
      stdDevType = (String) val;
    }
    else if (pName.equals(this.PERIOD_NAME) && intensityMeasureChanged) {
      setCoeffIndex();
    }
  }

  /**
   * Adds the parameter change listeners. This allows to listen to when-ever the
   * parameter is changed.
   */
  private void initPameterListeners() {

    distanceRupParam.addParameterChangeListener(this);
    distRupMinusJB_OverRupParam.addParameterChangeListener(this);
    vs30Param.addParameterChangeListener(this);
    depthTo2pt5kmPerSecParam.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);
    nonLinearAmpModelParam.addParameterChangeListener(this);
    rupTopDepthParam.addParameterChangeListener(this);
    magSaturationParam.addParameterChangeListener(this);
    stdDevTypeParam.addParameterChangeListener(this);
    periodParam.addParameterChangeListener(this);
  }

}
