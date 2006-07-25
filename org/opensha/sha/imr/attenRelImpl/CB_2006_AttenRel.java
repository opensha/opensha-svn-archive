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
 * <b>Title:</b> CB_2006_AttenRel<p>
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


public class CB_2006_AttenRel
    extends AttenuationRelationship implements
    AttenuationRelationshipAPI,
    NamedObjectAPI, ParameterChangeListener {

  // Debugging stuff
  private final static String C = "CB_2006_AttenRel";
  private final static boolean D = false;
  public final static String SHORT_NAME = "CB2006";


  // Name of IMR
  public final static String NAME = "Campbell & Bozorgnia (2006)";

  // coefficients:
  //Index 0 is PGV and index 1 is PGA as well as for SA @0.1sec, rest all are for SA periods
  private double[] per ={	-1,0.01,0.02,0.03,0.05,0.075,0.1,0.15,0.2,0.25,0.3,0.4,0.5,0.75,1,1.5,	2,3,	4,5,	7.5,10};
  private double[]c0 ={
		  0.954,-1.715	,-1.68,	-1.552,	-1.209,	-0.657,	-0.314,	-0.133,	-0.486,	-0.89,	-1.171,	-1.466,	-2.569,	-4.844,	-6.406,	-8.692,
		  -9.701,	-10.556,	-11.212,	-11.684,	-12.505,	-13.087
  };
  private double[] c1	={
		  0.696,0.5,0.5,0.5,	0.5,	0.5,	0.5,	0.5,	0.5,	0.5,	0.5,	0.5,	0.656,	0.972,	1.196,	1.513,	1.6,	1.6,	1.6,	1.6,	1.6,	1.6
		  };
  private double[] c2 = {
		  -0.309,-0.53,	-0.53,	-0.53,	-0.53,	-0.53,	-0.53,	-0.53,	-0.446,	-0.362,	-0.294,	-0.186,	-0.304,	-0.578,	-0.772,	-1.046,
		  -0.978,	-0.638,	-0.316,	-0.07,	-0.07,	-0.07
  };
  private double[] c3 = {
		  -0.019,-0.262,	-0.262,	-0.262,	-0.267,	-0.302,	-0.324,	-0.339,	-0.398,	-0.458,	-0.511,	-0.592,	-0.536,	-0.406,	-0.314,	-0.185,
		  -0.236,	-0.491,	-0.77,	-0.986,	-0.656,	-0.422
  };
  private double[] c4 =	{
		  -2.016,-2.118,	-2.123,	-2.145,	-2.199,	-2.277,	-2.318,	-2.309,	-2.22,	-2.146,	-2.095,	-2.066,	-2.041,
		  -2	,-2,	-2,	-2,	-2,	-2,	-2,	-2,	-2
  };
  private double[] c5 ={
		  0.17,0.17,	0.17,	0.17,	0.17,	0.17,	0.17,	0.17,	0.17,	0.17,	0.17,	0.17,	0.17,	0.17,
		  0.17,	0.17,	0.17,	0.17,	0.17,	0.17,	0.17,	0.17	
  };
  private double[] c6 ={
		  4,5.6,	5.6,	5.6,	5.74,	7.09,	8.05,	8.79,	7.6,	6.58,	6.04,	5.3,	4.73,	4,	4,	4,	4,	4,	4,	4,	4,	4
  };
  private double[] c7 ={
		  0.245,0.28,	0.28,	0.28,	0.28,	0.28,	0.28,	0.28,	0.28,	0.28,	0.28,	0.28,	0.28,	0.28,	0.255,
		  0.161,	0.094,	0,	0,	0,	0,	0
  };
  private double[] c8 ={
		  0,-0.12,	-0.12,	-0.12,	-0.12,	-0.12,	-0.099,	-0.048,	-0.012,	0,	
		  0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0
  };
  private double[] c9 ={
		  0.358,0.49,	0.49	,0.49,	0.49,	0.49,	0.49,	0.49,	0.49,	0.49,	0.49,	0.49,	0.49,	0.49,	0.49,
		  0.49,	0.371,	0.154,	0,	0,	0,	0
  };
  private double[] c10={
		  1.694,1.058,	1.102,	1.174,	1.272,	1.438,	1.604,	1.928,	2.194,	2.351,	2.46	,2.587,	2.544,	2.133,	1.571,
		  0.406,	-0.456,	-0.82,	-0.82,	-0.82,	-0.82,	-0.82
  };
  private double[] c11={
		  0.092,0.04,	0.04,	0.04,	0.04,	0.04,	0.04,	0.04,	0.04,	0.04,	0.04,	0.04,	0.04,	0.077,
		  0.15,	0.253,	0.3,	0.3,	0.3,	0.3,	0.3	,0.3
  };
  private double[] c12={
		  1,0.61,	0.61,	0.61,	0.61,	0.61,	0.61,	0.61,	0.61,	0.61,	0.61,	0.61,	0.883,
		  1	,1	,1	,1	,1	,1	,1	,1	,1	
  };
  private double[] k1 ={
			401,865,865,	908,	1054,	1086,	1032,	878,	748,	654,	587,	503,	457,	410,	401,	402,	402,	402,	402,	402,	402,	402
  };
  private double[] k2	= {
		  -1.955,-1.186,	-1.219,	-1.273,	-1.346,	-1.471,	-1.624,	-1.931,	-2.188,	-2.381,	-2.518,	-2.657,	-2.669,	-2.401,	-1.955,
		  -1.025,	-0.299,	0.004,	0.004,	0.004,	0.004,	0.004
  };
  private double[] k3={
		  1.929,1.839,	1.84,	1.841,	1.843,	1.845,	1.847,	1.852,	1.856,	1.861,	1.865,	1.874,	1.883,	1.906,
		  1.929,	1.974,	2.019,	2.11	,2.2,	2.291,	2.517,	2.744
  };
  private double[] sig={
		  0.484,0.478,	0.48,	0.489,	0.51	,0.52,	0.531,	0.532,	0.534,	0.534,0.544,	0.541,	0.55,	0.568,	0.568,
		  0.564,	0.571,	0.558,	0.576,	0.601,	0.628,	0.667
  };
  private double[] tau={
		  0.203,0.219,	0.219,	0.235,	0.258,	0.292,	0.286,	0.28,	0.249,	0.24,	0.215,	0.217,	0.214,	0.227,	0.255,
		  0.296, 0.296,	0.326,	0.297,	0.359,	0.428,	0.485
  };
  private double[] sigt={
		  0.525,0.526, 0.528,	0.543,	0.572,	0.596,	0.603,	0.601,	0.589,	0.585,	0.585,	0.583,	0.59,	0.612,	0.623,	0.637,	0.643,	0.646,	0.648,	0.7,	0.76,
		  	0.825
  };
  private HashMap indexFromPerHashMap;

  private int iper;
  private double vs30, rJB, rRup, rake, mag, depthTop,
      depthTo2pt5kmPerSec,dip;
  private String stdDevType;
  private boolean magSaturation;
  private boolean parameterChange;

  // ?????????????????????????????????????
  protected final static Double MAG_WARN_MIN = new Double(4.0);
  protected final static Double MAG_WARN_MAX = new Double(8.5);
  protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_RUP_WARN_MAX = new Double(200.0);
  protected final static Double DISTANCE_JB_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_JB_WARN_MAX = new Double(200.0);
  protected final static Double VS30_WARN_MIN = new Double(120.0);
  protected final static Double VS30_WARN_MAX = new Double(2000.0);
  protected final static Double DEPTH_2pt5_WARN_MIN = new Double(0);
  protected final static Double DEPTH_2pt5_WARN_MAX = new Double(6);




  /**
   * The DistanceRupParameter, closest distance to fault surface.
   */
  private DistanceRupParameter distanceRupParam = null;
  private final static Double DISTANCE_RUP_DEFAULT = new Double(0);

  /**
   * Joyner-Boore Distance parameter, used as a proxy for computing their
   * hanging-wall term from a site and eqkRupture.
   */
  private DistanceJBParameter distanceJBParam = null;
  private final static Double DISTANCE_JB_DEFAULT = new Double(0);

  // No waring constraint needed for this



  // for issuing warnings:
  private transient ParameterChangeWarningListener warningListener = null;

  /**
   *  This initializes several ParameterList objects.
   */
  public CB_2006_AttenRel(ParameterChangeWarningListener warningListener) {

    super();

    this.warningListener = warningListener;

    initSupportedIntensityMeasureParams();
    indexFromPerHashMap = new HashMap();
    for (int i = 1; i < per.length; i++) {
      indexFromPerHashMap.put(new Double(per[i]), new Integer(i));
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
    double depth = surface.getLocation(0, 0).getDepth();
    rupTopDepthParam.setValue(depth);
    dipParam.setValue(surface.getAveDip());

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

    if (im.getName().equalsIgnoreCase(SA_NAME)) {
      iper = ( (Integer) indexFromPerHashMap.get(periodParam.getValue())).
          intValue();
    }
    else if (im.getName().equalsIgnoreCase(PGV_NAME)) {
        iper = 0;
      }
    else if (im.getName().equalsIgnoreCase(PGA_NAME)) {
    		iper = 1;
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

    // do the following here in case vs30 changed after null value set
    if(Double.isNaN(depthTo2pt5kmPerSec)){
      if(vs30 <= 2500)
        depthTo2pt5kmPerSec = 1;
      else
        depthTo2pt5kmPerSec = 0;
    }

    
      double pgar = Math.exp(getMean(0, 1100, rRup, rJB,
                                         rake, mag,
                                         depthTop, depthTo2pt5kmPerSec,
                                         magSaturation,
                                         0));
      return getMean(iper, vs30, rRup, rJB, rake, mag,
                         depthTop, depthTo2pt5kmPerSec, magSaturation, pgar);

    
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

    rupTopDepthParam.setValue(RUP_TOP_DEFAULT);
    distanceRupParam.setValue(DISTANCE_RUP_DEFAULT);
    distanceJBParam.setValue(this.DISTANCE_JB_DEFAULT);
    saParam.setValue(SA_DEFAULT);
    periodParam.setValue(PERIOD_DEFAULT);
    dampingParam.setValue(DAMPING_DEFAULT);
    pgaParam.setValue(PGA_DEFAULT);
    componentParam.setValue(COMPONENT_DEFAULT);
    stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);
    depthTo2pt5kmPerSecParam.setValue(this.DEPTH_2pt5_DEFAULT);
    dipParam.setValue(DIP_DEFAULT);
    vs30 = ( (Double) vs30Param.getValue()).doubleValue();
    rJB = ( (Double) distanceJBParam.getValue()).
        doubleValue();
    rRup = ( (Double) distanceRupParam.getValue()).doubleValue();
    rake = ( (Double) rakeParam.getValue()).doubleValue();
    mag = ( (Double) magParam.getValue()).doubleValue();
    depthTop = ( (Double) rupTopDepthParam.getValue()).doubleValue();
    stdDevType = (String) stdDevTypeParam.getValue();
    depthTo2pt5kmPerSec = ( (Double) depthTo2pt5kmPerSecParam.getValue()).
        doubleValue();
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
    meanIndependentParams.addParameter(distanceJBParam);
    meanIndependentParams.addParameter(vs30Param);
    meanIndependentParams.addParameter(depthTo2pt5kmPerSecParam);
    meanIndependentParams.addParameter(magParam);
    meanIndependentParams.addParameter(rakeParam);
    meanIndependentParams.addParameter(rupTopDepthParam);
    meanIndependentParams.addParameter(dipParam);
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
    eqkRuptureParams.addParameter(dipParam);
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

    //create distanceJBParam
    distanceJBParam = new DistanceJBParameter();
    distanceJBParam.addParameterChangeWarningListener(warningListener);
    DoubleConstraint warnJB = new DoubleConstraint(DISTANCE_JB_WARN_MIN,
            DISTANCE_JB_WARN_MAX);
    warn.setNonEditable();
    distanceJBParam.setWarningConstraint(warnJB);
    distanceJBParam.setNonEditable();
    
    propagationEffectParams.addParameter(distanceRupParam);
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
    for (int i = 1; i < per.length; i++) {
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

  
  
  public double getMean(int iper, double vs30, double rRup,
                            double distJB,
                            double rake, double mag, double depthTop,
                            double depthTo2pt5kmPerSec,
                            boolean magSaturation, double pgar) {


    double fmag,fdis,fflt,fhng,fsite,fsed;
    double n = 1.88;
    double c = 1.18;
    
    //modeling depence on magnitude
    if(mag<= 5.5)
    		fmag = c0[iper]+c1[iper]*mag;
    else if(mag > 5.5  && mag <=6.5)
    	   fmag = c0[iper]+c1[iper]*mag+c2[iper]*(mag-5.5);
    else
    	  fmag  = c0[iper]+c1[iper]*mag+c2[iper]*(mag-5.5)+c3[iper]*(mag - 6.5);
    
    //source to site distance
    fdis = (c4[iper]+c5[iper]*mag)*Math.log(Math.sqrt(Math.pow(rRup,2)+Math.pow(c6[iper],2)));
    
    //style of faulting
    double ffltz; //getting the depth top or also called Ztor in Campbell's paper
    if(depthTop <1)
    	  ffltz = depthTop;
    else
    	 ffltz = 1;

    double Frv; // indicator variable representing reverse and reverse-oblique faulting
    if(rake >30 && rake <150)
    	 Frv = 1;
    else
    	 Frv = 0;
    double Fnm ;//indicator varible representing normal and normal-oblique faulting
    if(rake >-150 && rake<-30)
    	  Fnm = 1;
    else
    	 Fnm=0;
    fflt = c7[iper]*Frv*ffltz+c8[iper]*Fnm;
    
    //hanging wall effects
    double fhngr;
    if(distJB == 0)
    	 fhngr = 1;
    else if(distJB >0 && depthTop < 1)
    	 fhngr = (Math.max(rRup,Math.sqrt(Math.pow(distJB,2)+1)) - distJB)/
    	 	Math.max(rRup,Math.sqrt(Math.pow(distJB,2)+1));
    else
    	 fhngr = (rRup-distJB)/rRup;
    
    double fhngm;
    if(mag<=6.0)
    	  fhngm =0;
    else if(mag>6.0 && mag<6.5)
    	  fhngm = 2*(mag -6);
    else
    	 fhngm= 1;
    
    double fhngz;
    if(depthTop >=20)
    	  fhngz =0;
    else
    	 fhngz = (20-depthTop)/20;
    
    double fhngd;
    if(dip <= 70)
    	 fhngd =1;
    else
    	 fhngd = (90-dip)/20; 
    
    fhng = c9[iper]*fhngr*fhngm*fhngz*fhngd;
    
    
    //modelling dependence on linear and non-linear site conditions
    if(vs30< k1[iper])
    	 fsite = c10[iper]*Math.log(vs30/k1[iper])+k2[iper]*(Math.log(pgar+c*Math.pow(vs30/k1[iper],n)) -
    			 Math.log(pgar+c));
    else
    	 fsite = (c10[iper]+k2[iper]*n)*Math.log(vs30/k1[iper]);
    
    //modelling depence on shallow sediments effects and 3-D basin effects
    if(depthTo2pt5kmPerSec<1)
    	 fsed = c11[iper]*(depthTo2pt5kmPerSec -1);
    else if(depthTo2pt5kmPerSec>=1 && depthTo2pt5kmPerSec <=3)
    	 fsed = 0;
    else
    	 fsed = c12[iper]*k3[iper]*Math.exp(-0.75)*(1-Math.exp(-0.25*(depthTo2pt5kmPerSec-3)));
    

    return fmag+fdis+fflt+fhng+fsite+fsed;
  }

 
  public double getStdDev(int iper, String stdDevType) {

    double s = sig[iper];
    double t = tau[iper];

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
      rJB = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.VS30_NAME)) {
      vs30 = ( (Double) val).doubleValue();
    }
    else if (pName.equals(this.DEPTH_2pt5_NAME)) {
      if(val == null)
        depthTo2pt5kmPerSec = Double.NaN;
      else
        depthTo2pt5kmPerSec = ( (Double) val).doubleValue();
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
    else if (pName.equals(this.DIP_NAME)) {
        dip = ( (Double) val).doubleValue();
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
    distanceJBParam.removeParameterChangeListener(this);
    vs30Param.removeParameterChangeListener(this);
    depthTo2pt5kmPerSecParam.removeParameterChangeListener(this);
    magParam.removeParameterChangeListener(this);
    rakeParam.removeParameterChangeListener(this);
    rupTopDepthParam.removeParameterChangeListener(this);
    dipParam.removeParameterChangeListener(this);
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
    distanceJBParam.addParameterChangeListener(this);
    vs30Param.addParameterChangeListener(this);
    depthTo2pt5kmPerSecParam.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);
    rupTopDepthParam.addParameterChangeListener(this);
    stdDevTypeParam.addParameterChangeListener(this);
    periodParam.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);
  }

}
