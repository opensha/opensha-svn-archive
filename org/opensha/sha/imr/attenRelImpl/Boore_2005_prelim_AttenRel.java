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
import org.opensha.calc.RelativeLocation;

/**
 * <b>Title:</b> Boore_2005_prelim_AttenRel<p>
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


public class Boore_2005_prelim_AttenRel
    extends AttenuationRelationship implements
    AttenuationRelationshipAPI,
    NamedObjectAPI, ParameterChangeListener {

  // Debugging stuff
  private final static String C = "Boore_2005_prelim_AttenRel";
  private final static boolean D = false;

  // Name of IMR
  public final static String NAME = "Boore (2005 prelim)";

  // coefficients:
  // note that index 0 is for PGV, and that the last index (6) is for his pga4nl term (rock-PGA for computing amp factor)
  double[] period = { -1, 0, 0.1, 0.2, 1, 3, -2 };
  double[] e01 = { 4.73642, -0.92027, -0.36995, 0.01688, -1.00316, -2.27419, -0.96409 };
  double[] e02 = { 0.46374, 0.28115, 0.06284, 0.19353, 0.70367, 0.78311, 0.29795 };
  double[] e03 = { -0.1324, -0.21409, -0.24453, -0.26457, -0.25927, -0.46006, -0.20341 };
  double[] e04 = { 0, 0, 0, 0, 0.30832, 0.9588, 0 };
  double[] mh = { 8.5, 7, 7, 7, 7, 7, 7 };
  double[] c01 = { -0.7468, -0.5641, -0.5404, -0.6379, -0.7478, -0.7986, -0.55 };
  double[] c02 = { 0, 0, 0, 0, 0, 0, 0 };
  double[] c03 = { -0.00622, -0.01151, -0.01359, -0.00967, -0.00322, -0.00196, -0.01151 };
  double[] mref = { 6, 6, 6, 6, 6, 6, 6 };
  double[] rref = { 5, 5, 5, 5, 5, 5, 5 };
  double[] h = { 4.4, 3.2, 3.4, 4.6, 4, 5, 3 };
  double[] blin = { -0.6, -0.36, -0.25, -0.31, -0.7, -0.74, 0 };
  double[] vref = { 760, 760, 760, 760, 760, 760, 760 };
  double[] b1 = { -0.5, -0.64, -0.6, -0.52, -0.44, -0.34, 0 };
  double[] b2 = { -0.06, -0.14, -0.13, -0.19, 0, 0, 0 };
  double[] v1 = { 180, 180, 180, 180, 180, 180, 180 };
  double[] v2 = { 300, 300, 300, 300, 300, 300, 300 };
  double[] pga_low = { 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06 };
  double[] sig1 = { 0.49, 0.49, 0.523, 0.523, 0.571, 0.562, -999 };
  double[] sig2 = { 0.26, 0.253, 0.311, 0.235, 0.311, 0.433, -999 };
  //double[] sigt = { 0.555, 0.553, 0.608, 0.573, 0.649, 0.709, 0.241 };

  private static final double MAX_MAG = 8.5;
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


  /**
   * The DistanceRupParameter, closest distance to fault surface.
   */
  private DistanceRupParameter distanceJBParam = null;
  private final static Double DISTANCE_JB_DEFAULT = new Double(0);


  // for issuing warnings:
  private transient ParameterChangeWarningListener warningListener = null;

  /**
   *  This initializes several ParameterList objects.
   */
  public Boore_2005_prelim_AttenRel(ParameterChangeWarningListener warningListener) {

    super();

    this.warningListener = warningListener;

    initSupportedIntensityMeasureParams();
    indexFromPerHashMap = new HashMap();
    for (int i = 1; i < period.length-1; i++) {
      indexFromPerHashMap.put(new Double(period[i]), new Integer(i));
    }

    initEqkRuptureParams();
    initPropagationEffectParams();
    initSiteParams();
    initOtherParams();

    initIndependentParamLists(); // This must be called after the above
    initPameterListeners();//add the change listeners to the parameters

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

    if(im.getName().equalsIgnoreCase(PGV_NAME))
      iper = 0;
    else if (im.getName().equalsIgnoreCase(PGA_NAME))
      iper = 1;
    else
      iper = ( (Integer) indexFromPerHashMap.get(periodParam.getValue())).intValue();

    parameterChange = true;
    intensityMeasureChanged = false;

  }

  /**
   * Calculates the mean of the exceedence probability distribution. <p>
   * @return    The mean value
   */
  public double getMean() {
    if(intensityMeasureChanged)
      setCoeffIndex();

    // check if distance is beyond the user specified max
    if (rjb > USER_MAX_DISTANCE) {
      return VERY_SMALL_MEAN;
    }
    if(parameterChange){
      // remember that pga4nl term uses coeff index 6
      double pga4nl = getMean(6,vs30,rjb,mag,0.0);
      return getMean(iper, vs30, rjb, mag,pga4nl);
    }
    return 0;
  }




  /**
   * @return    The stdDev value
   */
  public double getStdDev() {
    if(intensityMeasureChanged)
      setCoeffIndex();

    return getStdDev(iper, stdDevType);
   }

  /**
   * Allows the user to set the default parameter values for the selected Attenuation
   * Relationship.
   */
  public void setParamDefaults() {

    vs30Param.setValue(VS30_DEFAULT);
    magParam.setValue(MAG_DEFAULT);
    distanceJBParam.setValue(DISTANCE_JB_DEFAULT);
    saParam.setValue(SA_DEFAULT);
    periodParam.setValue(PERIOD_DEFAULT);
    dampingParam.setValue(DAMPING_DEFAULT);
    pgaParam.setValue(PGA_DEFAULT);
    pgvParam.setValue(PGV_DEFAULT);
    stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);

    vs30 = ((Double)vs30Param.getValue()).doubleValue();
    rjb = ((Double)distanceJBParam.getValue()).doubleValue();
    mag = ((Double)magParam.getValue()).doubleValue();
    stdDevType = (String)stdDevTypeParam.getValue();
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
  }

  /**
   *  Creates the Propagation Effect parameters and adds them to the
   *  propagationEffectParams list. Makes the parameters noneditable.
   */
  protected void initPropagationEffectParams() {

    distanceJBParam = new DistanceRupParameter();
    distanceJBParam.addParameterChangeWarningListener(warningListener);
    DoubleConstraint warn = new DoubleConstraint(DISTANCE_JB_WARN_MIN,
                                                 DISTANCE_JB_WARN_MAX);
    warn.setNonEditable();
    distanceJBParam.setWarningConstraint(warn);
    distanceJBParam.setNonEditable();

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
    for (int i = 1; i < period.length-1; i++) {
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




  public double getMean(int iper, double vs30, double rjb, double mag, double pga4nl) {

    // remember that pga4ln term uses coeff index 6
    double Fm,Fd,Fs;
    if(mag <= MAX_MAG)
      Fm=e01[iper] + e02[iper]*(mag - MAX_MAG) + e03[iper]*Math.pow((mag - MAX_MAG),2);
    else
      Fm = e01[iper] + e04[iper]*(mag - MAX_MAG);

    double r = Math.sqrt(rjb*rjb+h[iper]*h[iper]);
    Fd = c01[iper]*Math.log(r/rref[iper]) + c02[iper]*(mag-mref[iper])
        * Math.log(r/rref[iper]) + c03[iper]*(r - rref[iper]);

    double bnl=0;
    if(vs30 <= v1[iper])
      bnl = b1[iper];
    else if(vs30 <= v2[iper] && vs30 >v1[iper])
      bnl = (b1[iper]-b2[iper])*Math.log(vs30/v2[iper])/Math.log(v1[iper]/v2[iper]) + b2[iper];
    else if(vs30 <= vref[iper] && vs30 > v2[iper])
      bnl = b2[iper]*Math.log(vs30/vref[iper])/Math.log(v2[iper]/vref[iper]);
    else if(vs30 > vref[iper])
      bnl = 0.0;

    if(pga4nl <= 0.06)
      Fs = blin[iper]*Math.log(vs30/vref[iper]) + bnl*Math.log(0.06/0.1);
    else
      Fs = blin[iper]*Math.log(vs30/vref[iper]) + bnl*Math.log(pga4nl/0.1) ;
    return (Fm + Fd +Fs);
  }


  public double getStdDev(int iper, String stdDevType) {

     if (stdDevType.equals(STD_DEV_TYPE_NONE))
      return 0;
    else if (stdDevType.equals(STD_DEV_TYPE_INTRA))
      return sig2[iper];
    else if (stdDevType.equals(STD_DEV_TYPE_INTER))
        return sig1[iper];
    else // it's total sigma
      return Math.sqrt(sig1[iper]*sig1[iper] + sig2[iper]*sig2[iper]);
  }


  /**
   * This listens for parameter changes and updates the primitive parameters accordingly
   * @param e ParameterChangeEvent
   */
  public void parameterChange(ParameterChangeEvent e) {


    String pName = e.getParameterName();
    Object val = e.getNewValue();
    parameterChange = true;
    if (pName.equals(DistanceJBParameter.NAME))
      rjb = ( (Double) val).doubleValue();
    else if (pName.equals(this.VS30_NAME))
      vs30 = ( (Double) val).doubleValue();
    else if (pName.equals(this.MAG_NAME))
      mag = ( (Double) val).doubleValue();
    else if (pName.equals(this.STD_DEV_TYPE_NAME))
      stdDevType = (String) val;
    else if(pName.equals(this.PERIOD_NAME) && intensityMeasureChanged)
      setCoeffIndex();
  }

  /**
   * Adds the parameter change listeners. This allows to listen to when-ever the
   * parameter is changed.
   */
  private void initPameterListeners() {

    distanceJBParam.addParameterChangeListener(this);
    vs30Param.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    stdDevTypeParam.addParameterChangeListener(this);
    periodParam.addParameterChangeListener(this);
  }

}
