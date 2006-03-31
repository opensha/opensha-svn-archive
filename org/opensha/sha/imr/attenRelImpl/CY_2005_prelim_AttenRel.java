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
 * <b>Title:</b> CY_2005_prelim_AttenRel<p>
 *
 * <b>Description:</b> This implements the Attenuation Relationship
 * developed by Chiou & Youngs (2005) <p>
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


public class CY_2005_prelim_AttenRel
    extends AttenuationRelationship implements
    AttenuationRelationshipAPI,
    NamedObjectAPI, ParameterChangeListener {

  // Debugging stuff
  private final static String C = "CY_2005_prelim_AttenRel";
  private final static boolean D = false;

  // Name of IMR
  public final static String NAME = "Chiou & Youngs (2005 prelim)";

  // coefficients:
  double[] period = {
      0.01, 0.029, 0.05, 0.075, 0.1, 0.15, 0.2, 0.3, 0.4, 0.5, 0.6, 0.75, 0.85,
      1, 1.5, 2, 3};
  double[] c1 = {
      0.4831, 0.5923, 0.8209, 1.1083, 1.2562, 1.3946, 1.3379, 1.1409, 0.9347,
      0.7252, 0.5265, 0.3121, 0.2299, 0.0531, -0.2847, -0.5664, -0.7916};
  double[] c2 = {
       -0.45, -0.45, -0.45, -0.45, -0.45, -0.45, -0.45, -0.45, -0.45, -0.45,
      -0.45, -0.45, -0.45, -0.45, -0.45, -0.45, -0.45};
  double[] cm = {
       -0.38, -0.38, -0.38, -0.38, -0.38, -0.38, -0.38, -0.38, -0.38, -0.38,
      -0.38, -0.38, -0.38, -0.38, -0.38, -0.38, -0.38};
  double[] mc = {
      5.00, 5.00, 5.00, 5.00, 5.00, 5.35, 5.60, 6.00, 6.20, 6.40, 6.55, 6.75,
      6.85, 7.00, 7.35, 7.60, 8.00};
  double[] c4 = {
       -1.05, -1.05, -1.05, -1.05, -1.05, -1.05, -1.05, -1.05, -1.05, -1.05,
      -1.05, -1.05, -1.05, -1.05, -1.05, -1.05, -1.05};
  double[] c5 = {
      0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35,
      0.35, 0.35, 0.35, 0.35, 0.35};
  double[] H = {
      1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5,
      1.5, 1.5};
  double[] Frv = {
      0.14, 0.14, 0.14, 0.139, 0.138, 0.137, 0.134, 0.127, 0.118, 0.108, 0.098,
      0.082, 0.073, 0.06, 0.029, 0.014, 0};
  double[] Ftor = {
      0.055, 0.055, 0.055, 0.055, 0.055, 0.055, 0.055, 0.055, 0.055, 0.055,
      0.054, 0.052, 0.049, 0.044, 0.026, 0.013, 0.003};
  double[] Fhw = {
      0.9, 0.9, 0.9, 0.9, 0.89, 0.87, 0.85, 0.81, 0.77, 0.725, 0.69, 0.65,
      0.625, 0.59, 0.49, 0.38, 0.15};
  double[] gamma = {
       -0.007134285, -0.007652814, -0.008450043, -0.00931877, -0.009461162,
      -0.008840156, -0.007485059, -0.006085877, -0.005305312, -0.004750315,
      -0.004177759, -0.003691066, -0.003520939, -0.003382255, -0.003092895,
      -0.002646036, -0.002516161};
  double[] phi1 = {
       -0.489, -0.479, -0.457, -0.425, -0.433, -0.425, -0.459, -0.537, -0.576,
      -0.620, -0.659, -0.703, -0.713, -0.764, -0.835, -0.885, -0.873};
  double[] phi2 = {
       -0.2640, -0.2810, -0.3145, -0.3158, -0.2915, -0.2215, -0.1709, -0.1181,
      -0.0870, -0.0688, -0.0567, -0.0424, -0.0343, -0.0247, -0.0065, -0.0013,
      0.0000};
  double[] phi3 = {
       -0.00409, -0.00400, -0.00387, -0.00384, -0.00402, -0.00489, -0.00635,
      -0.00837, -0.00978, -0.01070, -0.01117, -0.01162, -0.01182, -0.01200,
      -0.01231, -0.01238, -0.01240};
  double[] phi4 = {
      0.1003, 0.1106, 0.1326, 0.1646, 0.1896, 0.2192, 0.2230, 0.2001, 0.1720,
      0.1486, 0.1243, 0.0999, 0.0886, 0.0747, 0.0428, 0.0301, 0.0151};
  double[] tau = {
      0.331, 0.345, 0.374, 0.396, 0.391, 0.363, 0.351, 0.349, 0.394, 0.395,
      0.399, 0.396, 0.356, 0.355, 0.373, 0.422, 0.549};
  double[] sigma = {
      0.493, 0.501, 0.512, 0.526, 0.531, 0.533, 0.527, 0.540, 0.539, 0.551,
      0.560, 0.572, 0.580, 0.595, 0.612, 0.614, 0.575};
  // double[] total Sigma, 0.594, 0.608, 0.634, 0.658, 0.660, 0.645, 0.633, 0.642, 0.667, 0.678, 0.687, 0.696, 0.680, 0.693, 0.716, 0.745, 0.795
  protected final static Double PERIOD_DEFAULT = new Double(1.0);

  private HashMap indexFromPerHashMap;

  private int iper;
  private double vs30, rjb, rRup, distRupJB_Fraction, rake, dip, mag, depthTop;
  private String stdDevType;
  private boolean parameterChange;

  // ?????????????????????????????????????
  protected final static Double MAG_WARN_MIN = new Double(4.5);
  protected final static Double MAG_WARN_MAX = new Double(8.5);
  protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_RUP_WARN_MAX = new Double(200.0);
  protected final static Double VS30_WARN_MIN = new Double(120.0);
  protected final static Double VS30_WARN_MAX = new Double(2000.0);

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
  public CY_2005_prelim_AttenRel(ParameterChangeWarningListener warningListener) {

    super();

    this.warningListener = warningListener;

    initSupportedIntensityMeasureParams();
    indexFromPerHashMap = new HashMap();
    for (int i = 0; i < period.length; i++) {
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
    rakeParam.setValue(eqkRupture.getAveRake());
    EvenlyGriddedSurfaceAPI surface = eqkRupture.getRuptureSurface();
    dipParam.setValue(surface.getAveDip());
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
    if (parameterChange) {
      return getMean(iper, vs30, rRup, distRupJB_Fraction, dip, rake, mag,
                     depthTop);
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
   * Allows the user to set the default parameter values for the selected Attenuation
   * Relationship.
   */
  public void setParamDefaults() {

    vs30Param.setValue(VS30_DEFAULT);
    magParam.setValue(MAG_DEFAULT);
    rakeParam.setValue(RAKE_DEFAULT);
    dipParam.setValue(DIP_DEFAULT);
    rupTopDepthParam.setValue(RUP_TOP_DEFAULT);
    distanceRupParam.setValue(DISTANCE_RUP_DEFAULT);
    distRupMinusJB_OverRupParam.setValue(this.DISTANCE_RUP_MINUS_JB_DEFAULT);
    saParam.setValue(SA_DEFAULT);
    periodParam.setValue(PERIOD_DEFAULT);
    dampingParam.setValue(DAMPING_DEFAULT);
    pgaParam.setValue(PGA_DEFAULT);
    componentParam.setValue(COMPONENT_DEFAULT);
    stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);

    vs30 = ( (Double) vs30Param.getValue()).doubleValue();
    distRupJB_Fraction = ( (Double) distRupMinusJB_OverRupParam.getValue()).
        doubleValue();
    rRup = ( (Double) distanceRupParam.getValue()).doubleValue();
    rake = ( (Double) rakeParam.getValue()).doubleValue();
    mag = ( (Double) magParam.getValue()).doubleValue();
    depthTop = ( (Double) rupTopDepthParam.getValue()).doubleValue();
    stdDevType = (String) stdDevTypeParam.getValue();
    dip = ( (Double) dipParam.getValue()).doubleValue();
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
    meanIndependentParams.addParameter(magParam);
    meanIndependentParams.addParameter(rakeParam);
    meanIndependentParams.addParameter(dipParam);
    meanIndependentParams.addParameter(rupTopDepthParam);
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

    eqkRuptureParams.clear();
    eqkRuptureParams.addParameter(magParam);
    eqkRuptureParams.addParameter(dipParam);
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
    for (int i = 0; i < period.length; i++) {
      periodConstraint.addDouble(new Double(period[i]));
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

  private double getYref(int iper, double vs30, double rRup,
                         double distRupJB_Fraction,
                         double dip, double rake, double mag, double depthTop) {
    double rjb, SOF;
    rjb = rRup - distRupJB_Fraction * rRup;

    //       Mechanism
    if (rake > 30 && rake < 150) {
      SOF = 1.0;
    }
    else {
      SOF = 0.0;
    }
    double r = Math.sqrt(rRup * rRup + Math.pow(Math.exp(H[iper]), 2));
    double r1 = Math.min(r, 50);
    double r2 = Math.max(r / 50, 1);
    double hw = Math.pow(Math.cos(dip), 2) * (1 - (rjb / (rRup + .0001)));

    double yRef = c1[iper] + c2[iper] * (mag - 6) +
        cm[iper] * (Math.pow(Math.max(mc[iper] - mag, 0), 1.5)) +
        Frv[iper] * SOF +
        (c4[iper] + c5[iper] * (mag - 6)) * (Math.log(r1) + Math.log(r2) / 2) +
        gamma[iper] * rRup +
        Ftor[iper] * depthTop + Fhw[iper] * hw;
    return yRef;
  }

  private double getLnAmp(double vs30, double yRef) {
    return phi1[iper] * Math.log(vs30 / 1130) +
        phi2[iper] * Math.exp(phi3[iper] * (vs30 - 360)) *
        Math.log( (Math.exp(yRef) + phi4[iper]) / phi4[iper]);
  }


  public double getMean(int iper, double vs30, double rRup,
                        double distRupJB_Fraction,
                        double dip, double rake, double mag, double depthTop) {

    double yRef = getYref(iper, vs30, rRup, distRupJB_Fraction, dip, rake, mag,
                          depthTop);

    return yRef + getLnAmp(vs30,yRef);
  }


  public double getStdDev(int iper, String stdDevType) {

    if (stdDevType.equals(STD_DEV_TYPE_NONE)) {
      return 0;
    }
    else if (stdDevType.equals(STD_DEV_TYPE_INTRA)) {
      return tau[iper];
    }
    else if (stdDevType.equals(STD_DEV_TYPE_INTER)) {
      return sigma[iper];
    }
    else { // it's total sigma
      return Math.sqrt(tau[iper] * tau[iper] + sigma[iper] * sigma[iper]);
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
    else if (pName.equals(this.DIP_NAME)) {
      dip = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.MAG_NAME)) {
      mag = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.RAKE_NAME)) {
      rake = ( (Double) val).doubleValue();
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
   * Allows to reset the change listeners on the parameters
   */
  public void resetParameterEventListeners(){
    distanceRupParam.removeParameterChangeListener(this);
    distRupMinusJB_OverRupParam.removeParameterChangeListener(this);
    vs30Param.removeParameterChangeListener(this);
    dipParam.removeParameterChangeListener(this);
    magParam.removeParameterChangeListener(this);
    rakeParam.removeParameterChangeListener(this);
    rupTopDepthParam.removeParameterChangeListener(this);
    stdDevTypeParam.removeParameterChangeListener(this);
    periodParam.removeParameterChangeListener(this);
    this.initParameterEventListeners();
  }

  /**
   * Adds the parameter change listeners. This allows to listen to when-ever the
   * parameter is changed.
   */
  protected void initParameterEventListeners() {

    distanceRupParam.addParameterChangeListener(this);
    distRupMinusJB_OverRupParam.addParameterChangeListener(this);
    vs30Param.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);
    rupTopDepthParam.addParameterChangeListener(this);
    stdDevTypeParam.addParameterChangeListener(this);
    periodParam.addParameterChangeListener(this);
  }

}
