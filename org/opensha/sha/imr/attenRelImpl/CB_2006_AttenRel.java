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
 * developed by Campbell & Bozorgnia (2006, http://peer.berkeley.edu/lifelines/nga_docs/nov_13_06/Campbell-Bozorgnia_NGA_11-13-06.html) <p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>pgvParam - Peak Ground Velocity
 * <LI>pgdParam - Peak Ground Displacement
 * <LI>saParam - Response Spectral Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>fltTypeParam - Style of faulting
 * <LI>rupTopDepthParam - depth to top of rupture
 * <LI>dipParam - rupture surface dip
 * <LI>distanceRupParam - closest distance to surface projection of fault
 * <li>distRupMinusJB_OverRupParam - used as a proxy for hanging wall effect
 * <LI>vs30Param 
 * <li>depthTo2pt5kmPerSecParam
 * <LI>componentParam - Component of shaking
 * <LI>stdDevTypeParam - The type of standard deviation
 * <li>
 * </UL></p>
 * <p>
 * NOTES: distRupMinusJB_OverRupParam is used rather than distancJBParameter because the latter 
 * should not be held constant when distanceRupParameter is changed (e.g., in the 
 * AttenuationRelationshipApplet).  This includes the stipulation that the mean of 0.2-sec SA should 
 * not be less than that of PGA (the latter being given if so).
 * <p>
 * Verification :This model has been tested with the data provided by Campbell in his NGA report.
 * I ran the our AttenuationRelationship application and input the parameters as given in Campbell's
 * NGA report Table 3. I ran all the test cases with X-axis being the "Individual Value".
 * Then I spot check the values (manually) that I got from OpenSHA with that given in Campbell's report.
 * </p>
 *
 * @author     Ned Field & Nitin Gupta
 * @created    Nov., 2006
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
  //Index 0 is PGD, index 1 is PGV  and index 2 is PGA, rest all are for SA periods
  // index        0  1  2  3    4    5    6    7    8    9   10
  double[] per = {-2,-1,0,0.01,0.02,0.03,0.05,0.075,0.1,0.15,0.2,0.25,0.3,0.4,0.5,0.75,1,1.5,2,3,4,5,7.5,10};
  double[] c0 = {-5.27,0.954,-1.715,-1.715,-1.68,-1.552,-1.209,-0.657,-0.314,-0.133,-0.486,-0.89,-1.171,-1.466,-2.569,-4.844,-6.406,-8.692,-9.701,-10.556,-11.212,-11.684,-12.505,-13.087};
  double[] c1 ={1.6,0.696,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.656,0.972,1.196,1.513,1.6,1.6,1.6,1.6,1.6,1.6};
  double[] c2={-0.07,-0.309,-0.53,-0.53,-0.53,-0.53,-0.53,-0.53,-0.53,-0.53,-0.446,-0.362,-0.294,-0.186,-0.304,-0.578,-0.772,-1.046,-0.978,-0.638,-0.316,-0.07,-0.07,-0.07};
  double[] c3={0,-0.019,-0.262,-0.262,-0.262,-0.262,-0.267,-0.302,-0.324,-0.339,-0.398,-0.458,-0.511,-0.592,-0.536,-0.406,-0.314,-0.185,-0.236,-0.491,-0.77,-0.986,-0.656,-0.422};
  double[] c4 ={-2,-2.016,-2.118,-2.118,-2.123,-2.145,-2.199,-2.277,-2.318,-2.309,-2.22,-2.146,-2.095,-2.066,-2.041,-2,-2,-2,-2,-2,-2,-2,-2,-2};
  double[] c5={0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17,0.17};
  double[] c6={4,4,5.6,5.6,5.6,5.6,5.74,7.09,8.05,8.79,7.6,6.58,6.04,5.3,4.73,4,4,4,4,4,4,4,4,4};
  double[] c7={0,0.245,0.28,0.28,0.28,0.28,0.28,0.28,0.28,0.28,0.28,0.28,0.28,0.28,0.28,0.28,0.255,0.161,0.094,0,0,0,0,0};
  double[] c8={0,0,-0.12,-0.12,-0.12,-0.12,-0.12,-0.12,-0.099,-0.048,-0.012,0,0,0,0,0,0,0,0,0,0,0,0,0};
  double[] c9={0,0.358,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.49,0.371,0.154,0,0,0,0};
  double[] c10={-0.82,1.694,1.058,1.058,1.102,1.174,1.272,1.438,1.604,1.928,2.194,2.351,2.46,2.587,2.544,2.133,1.571,0.406,-0.456,-0.82,-0.82,-0.82,-0.82,-0.82};
  double[] c11={0.3,0.092,0.04,0.04,0.04,0.04,0.04,0.04,0.04,0.04,0.04,0.04,0.04,0.04,0.04,0.077,0.15,0.253,0.3,0.3,0.3,0.3,0.3,0.3};
  double[] c12={1,1,0.61,0.61,0.61,0.61,0.61,0.61,0.61,0.61,0.61,0.61,0.61,0.61,0.883,1,1,1,1,1,1,1,1,1};
  double[] k1={400,400,865,865,865,908,1054,1086,1032,878,748,654,587,503,457,410,400,400,400,400,400,400,400,400};
  double[] k2={0,-1.955,-1.186,-1.186,-1.219,-1.273,-1.346,-1.471,-1.624,-1.931,-2.188,-2.381,-2.518,-2.657,-2.669,-2.401,-1.955,-1.025,-0.299,0,0,0,0,0};
  double[] k3={2.744,1.929,1.839,1.839,1.84,1.841,1.843,1.845,1.847,1.852,1.856,1.861,1.865,1.874,1.883,1.906,1.929,1.974,2.019,2.11,2.2,2.291,2.517,2.744};
  double[] s_lny={0.667,0.484,0.478,0.478,0.48,0.489,0.51,0.52,0.531,0.532,0.534,0.534,0.544,0.541,0.55,0.568,0.568,0.564,0.571,0.558,0.576,0.601,0.628,0.667};
  double[] t_lny={0.485,0.203,0.219,0.219,0.219,0.235,0.258,0.292,0.286,0.28,0.249,0.24,0.215,0.217,0.214,0.227,0.255,0.296,0.296,0.326,0.297,0.359,0.428,0.485};
  double[] c_lny={0.309,0.206,0.181,0.181,0.181,0.181,0.179,0.177,0.19,0.201,0.207,0.208,0.216,0.224,0.223,0.236,0.24,0.237,0.241,0.244,0.252,0.253,0.288,0.309};
  double[] rho_s={0.174,0.691,1,1,0.999,0.989,0.963,0.922,0.898,0.89,0.871,0.852,0.831,0.785,0.735,0.628,0.534,0.411,0.331,0.289,0.261,0.2,0.174,0.174};
  double[] rho_t={0.29,0.538,1,1,0.994,0.979,0.927,0.88,0.871,0.885,0.913,0.873,0.848,0.756,0.631,0.442,0.29,0.29,0.29,0.29,0.29,0.29,0.29,0.29};
  private HashMap indexFromPerHashMap;

  private int iper;
  private double vs30, rJB, rRup, distRupMinusJB_OverRup, f_rv, f_nm, mag, depthTop, depthTo2pt5kmPerSec,dip;
  private String stdDevType, component;
  private boolean magSaturation;
  private boolean parameterChange;

  // values for warning parameters
  protected final static Double MAG_WARN_MIN = new Double(4.0);
  protected final static Double MAG_WARN_MAX = new Double(8.5);
  protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_RUP_WARN_MAX = new Double(200.0);
  protected final static Double DISTANCE_MINUS_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_MINUS_WARN_MAX = new Double(50.0);
  protected final static Double VS30_WARN_MIN = new Double(180.0);
  protected final static Double VS30_WARN_MAX = new Double(1500.0);
  protected final static Double DEPTH_2pt5_WARN_MIN = new Double(0);
  protected final static Double DEPTH_2pt5_WARN_MAX = new Double(6);
  protected final static Double DIP_WARN_MIN = new Double(15);
  protected final static Double DIP_WARN_MAX = new Double(90);
  protected final static Double RUP_TOP_WARN_MIN = new Double(0);
  protected final static Double RUP_TOP_WARN_MAX = new Double(20);
  
  // style of faulting options
  public final static String FLT_TYPE_STRIKE_SLIP = "Strike-Slip";
  public final static String FLT_TYPE_REVERSE = "Reverse";
  public final static String FLT_TYPE_NORMAL = "Normal";
  public final static String FLT_TYPE_DEFAULT = FLT_TYPE_STRIKE_SLIP;

  // change component default from that of parent
  String COMPONENT_DEFAULT = this.COMPONENT_GMRotI50;


  /**
   * The DistanceRupParameter, closest distance to fault surface.
   */
  private DistanceRupParameter distanceRupParam = null;
  private final static Double DISTANCE_RUP_DEFAULT = new Double(0);

  /**
   * this is used to compute JB Distance value, used as a proxy for computing their
   * hanging-wall term.
   */
  private DistRupMinusJB_OverRupParameter distRupMinusJB_OverRupParam = null;
  private final static Double DISTANCE_RUP_MINUS_DEFAULT = new Double(0);


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
    for (int i = 3; i < per.length; i++) {
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
   *  passed in.  Warning constraints are ingored.
   *
   * @param  eqkRupture  The new eqkRupture value
   * @throws InvalidRangeException thrown if rake is out of bounds
   */
  public void setEqkRupture(EqkRupture eqkRupture) throws InvalidRangeException {
	  
	  magParam.setValueIgnoreWarning(new Double(eqkRupture.getMag()));
	  
	  double rake = eqkRupture.getAveRake();
	  if(rake >30 && rake <150) {
		  f_rv = 1;
		  f_nm = 0;
		  fltTypeParam.setValue(FLT_TYPE_REVERSE);
	  }
	  else if(rake >-150 && rake<-30) {
		  f_nm = 1;	
		  f_rv = 0;
		  fltTypeParam.setValue(FLT_TYPE_NORMAL);
	  }
	  else { // strike slip
		  f_rv = 0;
		  f_nm=0;
		  fltTypeParam.setValue(FLT_TYPE_STRIKE_SLIP);
	  }
	  
	  EvenlyGriddedSurfaceAPI surface = eqkRupture.getRuptureSurface();
	  double depth = surface.getLocation(0, 0).getDepth();
	  rupTopDepthParam.setValue(depth);
	  
	  dipParam.setValue(surface.getAveDip());
	  
//	  setFaultTypeFromRake(eqkRupture.getAveRake());
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
    else if (im.getName().equalsIgnoreCase(PGV_NAME)) {
        iper = 1;
      }
    else if (im.getName().equalsIgnoreCase(PGA_NAME)) {
    		iper = 2;
    }
    else if (im.getName().equalsIgnoreCase(PGD_NAME)) {
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
	  
	  
	  // check if distance is beyond the user specified max
	  if (rRup > USER_MAX_DISTANCE) {
		  return VERY_SMALL_MEAN;
	  }
	  
	  
	  if (intensityMeasureChanged) {
		  setCoeffIndex();  // intensityMeasureChanged is set to false in this method
	  }
	  
	  // compute rJB
	  rJB = rRup - distRupMinusJB_OverRup*rRup;
	  
	  // set default value of basin depth based on the final value of vs30
	  if(Double.isNaN(depthTo2pt5kmPerSec)){
		  if(vs30 <= 2500)
			  depthTo2pt5kmPerSec = 2;
		  else
			  depthTo2pt5kmPerSec = 0;
	  }
	  
	  
	  double pgar = Math.exp(getMean(2, 1100, rRup, rJB, f_rv, f_nm, mag,
			  depthTop, depthTo2pt5kmPerSec, magSaturation, 0));
	  
	  double mean = getMean(iper, vs30, rRup, rJB, f_rv, f_nm, mag,
			  depthTop, depthTo2pt5kmPerSec, magSaturation, pgar);
	  
	  if(iper < 4 && iper > 9 ) // not SA period between 0.02 and 0.1
		  return mean;
	  else { // make sure 0.2-sec SA mean is not less than that of PGA (bottom of pg 11 of their report)
		  double pga_mean = getMean(2, vs30, rRup, rJB, f_rv, f_nm, mag,
				  depthTop, depthTo2pt5kmPerSec, magSaturation, pgar); // mean for PGA
		  return Math.max(mean,pga_mean);
	  }
  }



  /**
   * @return    The stdDev value
   */
  public double getStdDev() {
    if (intensityMeasureChanged) {
      setCoeffIndex();  // intensityMeasureChanged is set to false in this method
    }
    return getStdDev(iper, stdDevType, component);
  }

  /**
   * Allows the user to set the default parameter values for the selected Attenuation
   * Relationship.
   */
  public void setParamDefaults() {

    vs30Param.setValue(VS30_DEFAULT);
    magParam.setValue(MAG_DEFAULT);
    fltTypeParam.setValue(FLT_TYPE_DEFAULT);
    rupTopDepthParam.setValue(RUP_TOP_DEFAULT);
    distanceRupParam.setValue(DISTANCE_RUP_DEFAULT);
    distRupMinusJB_OverRupParam.setValue(DISTANCE_RUP_MINUS_DEFAULT);
    saParam.setValue(SA_DEFAULT);
    periodParam.setValue(PERIOD_DEFAULT);
    dampingParam.setValue(DAMPING_DEFAULT);
    pgaParam.setValue(PGA_DEFAULT);
    pgvParam.setValue(PGV_DEFAULT);
    pgdParam.setValue(PGD_DEFAULT);
    componentParam.setValue(COMPONENT_DEFAULT);
    stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);
    depthTo2pt5kmPerSecParam.setValue(DEPTH_2pt5_DEFAULT);
    dipParam.setValue(DIP_DEFAULT);
    
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
    meanIndependentParams.addParameter(fltTypeParam);
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
    
    DoubleConstraint warn1 = new DoubleConstraint(DIP_MIN, DIP_MAX);
    warn.setNonEditable();
    dipParam.setWarningConstraint(warn1);
    dipParam.addParameterChangeWarningListener(warningListener);
    dipParam.setNonEditable();

    DoubleConstraint warn2 = new DoubleConstraint(RUP_TOP_MIN, RUP_TOP_MAX);
    warn.setNonEditable();
    rupTopDepthParam.setWarningConstraint(warn2);
    rupTopDepthParam.addParameterChangeWarningListener(warningListener);
    rupTopDepthParam.setNonEditable();
    
    StringConstraint constraint = new StringConstraint();
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
    eqkRuptureParams.addParameter(dipParam);
    eqkRuptureParams.addParameter(rupTopDepthParam);
  }

  /**
   *  Creates the Propagation Effect parameters and adds them to the
   *  propagationEffectParams list. Makes the parameters noneditable.
   */
  protected void initPropagationEffectParams() {

    distanceRupParam = new DistanceRupParameter();
    DoubleConstraint warn = new DoubleConstraint(DISTANCE_RUP_WARN_MIN,
                                                 DISTANCE_RUP_WARN_MAX);
    warn.setNonEditable();
    distanceRupParam.setWarningConstraint(warn);
    distanceRupParam.addParameterChangeWarningListener(warningListener);

    distanceRupParam.setNonEditable();

    //create distRupMinusJB_OverRupParam
    distRupMinusJB_OverRupParam = new DistRupMinusJB_OverRupParameter();
    DoubleConstraint warnJB = new DoubleConstraint(DISTANCE_MINUS_WARN_MIN, DISTANCE_MINUS_WARN_MAX);
    distRupMinusJB_OverRupParam.addParameterChangeWarningListener(warningListener);
    warn.setNonEditable();
    distRupMinusJB_OverRupParam.setWarningConstraint(warnJB);
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
    for (int i = 3; i < per.length; i++) {
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

    //  Create PGV Parameter (pgvParam):
    DoubleConstraint pgvConstraint = new DoubleConstraint(PGV_MIN, PGV_MAX);
    pgvConstraint.setNonEditable();
    pgvParam = new WarningDoubleParameter(PGV_NAME, pgvConstraint, PGV_UNITS);
    pgvParam.setInfo(PGV_INFO);
    DoubleConstraint warn = new DoubleConstraint(PGV_WARN_MIN, PGV_WARN_MAX);
    warn.setNonEditable();
    pgvParam.setWarningConstraint(warn);
    pgvParam.setNonEditable();
    
    //  Create PGD Parameter (pgdParam):
    DoubleConstraint pgdConstraint = new DoubleConstraint(PGD_MIN, PGD_MAX);
    pgdConstraint.setNonEditable();
    pgdParam = new WarningDoubleParameter(PGD_NAME, pgdConstraint, PGD_UNITS);
    pgdParam.setInfo(PGD_INFO);
    DoubleConstraint pgdWarn = new DoubleConstraint(PGD_WARN_MIN, PGD_WARN_MAX);
    pgdWarn.setNonEditable();
    pgdParam.setWarningConstraint(pgdWarn);
    pgdParam.setNonEditable();
    
    // Add the warning listeners:
    saParam.addParameterChangeWarningListener(warningListener);
    pgaParam.addParameterChangeWarningListener(warningListener);
    pgvParam.addParameterChangeWarningListener(warningListener);
    pgdParam.addParameterChangeWarningListener(warningListener);
    

    
    // Put parameters in the supportedIMParams list:
    supportedIMParams.clear();
    supportedIMParams.addParameter(saParam);
    supportedIMParams.addParameter(pgaParam);
    supportedIMParams.addParameter(pgvParam);
    supportedIMParams.addParameter(pgdParam);
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
    constraint.addString(COMPONENT_GMRotI50);
    constraint.addString(COMPONENT_RANDOM_HORZ);
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

  
  /**
   * 
   * @param iper
   * @param vs30
   * @param rRup
   * @param distJB
   * @param f_rv
   * @param f_nm
   * @param mag
   * @param depthTop
   * @param depthTo2pt5kmPerSec
   * @param magSaturation
   * @param pgar
   * @return
   */
  public double getMean(int iper, double vs30, double rRup,
                            double distJB,double f_rv,
                            double f_nm, double mag, double depthTop,
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

    // fault-style term
    fflt = c7[iper]*f_rv*ffltz+c8[iper]*f_nm;
    
    //hanging wall effects
    double fhngr;
    if(distJB == 0)
    	 fhngr = 1;
    else if(depthTop < 1)
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
    else if(depthTo2pt5kmPerSec <=3)
    	 fsed = 0;
    else
    	 fsed = c12[iper]*k3[iper]*Math.exp(-0.75)*(1-Math.exp(-0.25*(depthTo2pt5kmPerSec-3)));
    

    return fmag+fdis+fflt+fhng+fsite+fsed;
  }

 /**
  * 
  * @param iper
  * @param stdDevType
  * @param component
  * @return
  */
  public double getStdDev(int iper, String stdDevType, String component) {
	  
	  double s = s_lny[iper];
	  double t = t_lny[iper];
	  double c = c_lny[iper];
	  
	  // set k for random versus ave horz
	  double k;
	  if(component.equals(COMPONENT_GMRotI50))
		  k =0;
	  else if (component.equals(COMPONENT_RANDOM_HORZ))
		  k=1;
	  else
		  k = Double.NaN; // just in case invalid component given
	  
	  if (stdDevType.equals(STD_DEV_TYPE_TOTAL))
		  return Math.sqrt(t*t + s*s + k*c*c);
	  else if (stdDevType.equals(STD_DEV_TYPE_INTRA))
		  return Math.sqrt(s*s + k*c*c);
	  else if (stdDevType.equals(STD_DEV_TYPE_INTER))
		  return Math.sqrt(t*t + k*c*c);
	  else if (stdDevType.equals(STD_DEV_TYPE_NONE))
		  return 0;
	  else
		  return Double.NaN;   // just in case invalid stdDev given
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
		  distRupMinusJB_OverRup = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(VS30_NAME)) {
		  vs30 = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(DEPTH_2pt5_NAME)) {
		  if(val == null)
			  depthTo2pt5kmPerSec = Double.NaN;  // can't set the defauly here because vs30 could still change
		  else
			  depthTo2pt5kmPerSec = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(MAG_NAME)) {
		  mag = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(FLT_TYPE_NAME)) {
		  String fltType = (String)fltTypeParam.getValue();
		  if (fltType.equals(FLT_TYPE_NORMAL)) {
			  f_rv = 0 ;
			  f_nm = 1;
		  }
		  else if (fltType.equals(FLT_TYPE_REVERSE)) {
			  f_rv = 1;
			  f_nm = 0;
		  }
		  else {
			  f_rv =0 ;
			  f_nm = 0;
		  }
	  }
	  else if (pName.equals(RUP_TOP_NAME)) {
		  depthTop = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(STD_DEV_TYPE_NAME)) {
		  stdDevType = (String) val;
	  }
	  else if (pName.equals(DIP_NAME)) {
		  dip = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(COMPONENT_NAME)) {
		  component = (String)componentParam.getValue();
	  }
	  else if (pName.equals(PERIOD_NAME)) {
		  intensityMeasureChanged = true;
	  }
  }

  /**
   * Allows to reset the change listeners on the parameters
   */
  public void resetParameterEventListeners(){
    distanceRupParam.removeParameterChangeListener(this);
    distRupMinusJB_OverRupParam.removeParameterChangeListener(this);
    vs30Param.removeParameterChangeListener(this);
    depthTo2pt5kmPerSecParam.removeParameterChangeListener(this);
    magParam.removeParameterChangeListener(this);
    fltTypeParam.removeParameterChangeListener(this);
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
    distRupMinusJB_OverRupParam.addParameterChangeListener(this);
    vs30Param.addParameterChangeListener(this);
    depthTo2pt5kmPerSecParam.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    fltTypeParam.addParameterChangeListener(this);
    rupTopDepthParam.addParameterChangeListener(this);
    stdDevTypeParam.addParameterChangeListener(this);
    periodParam.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);
  }

}
