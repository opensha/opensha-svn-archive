package scratchJavaDevelopers.bbradley;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.opensha.commons.data.NamedObjectAPI;
import org.opensha.commons.data.Site;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.DoubleDiscreteConstraint;
import org.opensha.commons.param.DoubleDiscreteParameter;
import org.opensha.commons.param.StringConstraint;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.WarningDoubleParameter;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.commons.util.FaultUtils;




import org.opensha.sha.earthquake.*;
import org.opensha.sha.imr.*;
import org.opensha.sha.imr.param.EqkRuptureParams.FaultTypeParam;
import org.opensha.sha.imr.param.EqkRuptureParams.MagParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.DampingParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.ComponentParam;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.param.*;

/**
 * <b>Title:</b> McVerryetal_2000_AttenRel<p>
 *
 * <b>Description:</b> This implements the Attenuation Relationship published by McVerry et al (2000,
 * "Crustal and subduction zone attenuation relations for New Zealand Earthquakes", Proc 12th World 
 * conference on earthquake engineering </b>
 * A more complete description of the attenuation relation can be found at McVerry et al (2006, "New 
 * Zealand Acceleration Response Spectrum Attenuation Relations for Crustal and Subduction Zone 
 * Earthquakes", <it> Bulletin of the New Zealand Society of Earthquake Engineering <it> Vol 39. No. 4 pp1-58)
 * 
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>saParam - Response Spectral Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>distanceRupParam - closest distance to fault surface  
 * <LI>siteTypeParam - "A-Strong Rock", "B-Rock", "C-Shallow Soil", "D-Deep or soft soil"
 * <LI>fltTypeParam - Style of faulting
 * <LI>componentParam - Component of shaking (either geometric mean or largest horizontal)
 * <LI>stdDevTypeParam - The type of standard deviation
 * </UL></p>
 * 
 *<p>
 *
 * Verification - Not performed as at 1 June 2009
 * 
 *</p>
 *
 *
 * @author     Brendon A. Bradley
 * @created    June, 2009
 * @version    1.0
 */


public class McVerryetal_2000_AttenRel
    extends AttenuationRelationship implements
    ScalarIntensityMeasureRelationshipAPI,
    NamedObjectAPI, ParameterChangeListener {

  // Debugging stuff
  private final static String C = "McVerryetal_2000_AttenRel";
  private final static boolean D = false;
  public final static String SHORT_NAME = "McVerryetal2000";
  private static final long serialVersionUID = 1234567890987654353L;

  // Name of IMR
  public final static String NAME = "McVerry et al (2000)";
  
  // URL Info String
  private final static String URL_INFO_STRING = "http://www.opensha.org/documentation/modelsImplemented/attenRel/McVerryetal_2000.html";

  // coefficients: 
  //Note unlike the NGA equations period=-1 here gives the 'primed' coefficients
  double[] period= {    -1.0,      0.0,    0.075,      0.1,      0.2,      0.3,      0.4,      0.5,     0.75,      1.0,      1.5,      2.0,      3.0};
  double[] C1=     { 0.28815,   0.1813,  1.36561,  1.77717,  1.39535,  0.44591,  0.01645,  0.14826, -0.21246, -0.10451, -0.48665, -0.77433, -1.30916}; 
  double[] C3AS=   {     0.0,      0.0,     0.03,    0.028,  -0.0138,   -0.036,  -0.0518,  -0.0635,  -0.0862,   -0.102,    -0.12,    -0.12,  -0.1726}; 
  double C4AS=   -0.144;
  double[] C5=     {-0.00967, -0.00846, -0.00889, -0.00837,  -0.0094, -0.00987, -0.00923, -0.00823, -0.00738, -0.00588,  -0.0063,  -0.0063, -0.00553}; 
  double C6AS=   0.17;
  double[] C8=     {-0.70494, -0.75519, -0.94568, -1.01852, -0.78199, -0.56098, -0.51281, -0.56716, -0.55384, -0.65892, -0.58222, -0.58222, -0.57009}; 
  double[] C10AS=  {     5.6,      5.6,     5.58,      5.5,      5.1,      4.8,     4.52,      4.3,      3.9,      3.7,     3.55,     3.55,      3.5}; 
  double[]C11=     { 8.68354,  8.10697,  8.68782,  9.37929, 10.61479,  9.40776,  8.50343,  8.46463,  7.30176,  7.08727,  6.93264,  6.64496,  5.05488}; 
  double C12y=   1.414;
  double[] C13y=   {     0.0,      0.0,      0.0,  -0.0011,  -0.0027,  -0.0036,  -0.0043,  -0.0048,  -0.0057,  -0.0064,  -0.0073,  -0.0073,  -0.0089}; 
  double[] C15=    {  -2.552,   -2.552,   -2.707,   -2.655,   -2.528,   -2.454,   -2.401,    -2.36,   -2.286,   -2.234,    -2.16,    -2.16,   -2.033}; 
  double[] C17=    {-2.56727, -2.48795, -2.54215, -2.60945, -2.70851, -2.47668, -2.36895,  -2.4063, -2.26512, -2.27668, -2.28347, -2.28347,  -2.0305}; 
  double C18y=   1.7818;
  double C19y=   0.554;
  double[] C20=    {  0.0155,  0.01622,   0.0185,   0.0174,  0.01542,  0.01278,  0.01426,  0.01287,   0.0108,  0.00946,  0.00788,  0.00788, -0.00265}; 
  double[] C24=    {-0.50962, -0.41369, -0.48652, -0.61973, -0.67672, -0.59339, -0.30579, -0.24839, -0.01298,  0.06672, -0.02289, -0.02289, -0.20537}; 
  double[] C29=    { 0.30206,  0.44307,  0.31139,  0.34059,  0.37235,  0.56648,  0.69911,  0.63188,  0.51577,  0.34048,  0.12468,  0.12468,  0.14593}; 
  double[] C30AS=  {   -0.23,    -0.23,    -0.28,    -0.28,   -0.245,   -0.195,    -0.16,   -0.121,    -0.05,      0.0,     0.04,     0.04,     0.04}; 
  double C32=    -0.2;
  double[] C33AS=  {    0.26,     0.26,     0.26,     0.26,     0.26,    0.198,    0.154,    0.119,    0.057,    0.013,   -0.049,   -0.049,   -0.156}; 
  double[] C43=    {-0.31769, -0.29648, -0.29648, -0.43854, -0.29906, -0.05184,  0.20301,  0.37026,  0.73517,  0.87764,  0.75438,  0.75438,  0.61545}; 
  double[] C46=    {-0.03279, -0.03301, -0.03452, -0.03595, -0.03853, -0.03604, -0.03364,  -0.0326, -0.02877, -0.02561, -0.02034, -0.02034, -0.01673}; 
  double[] sigma6= {  0.4865,   0.5281,   0.5398,   0.5703,   0.5505,   0.5627,    0.568,   0.5562,   0.5629,   0.5394,   0.5394,   0.5701}; 
  double[] sigSlope={-0.1261,   -0.097,  -0.0673,  -0.0243,  -0.0861,  -0.1405,  -0.1444,  -0.0932,  -0.0749,  -0.0056,  -0.0056,   0.0934};
  double[] tau=    {  0.2687,   0.3217,   0.3088,	0.2726,	  0.2112,   0.2005,   0.1476,   0.1794,   0.2053,   0.2411,   0.2411,   0.2406};
  
  
  /*double a1 = 0.03;  // g
  double pgalow=0.06; // g
  double a2 = 0.09; // g
  double v1 = 180; // m/s
  double v2 = 300; // m/s
  double v_ref = 760; // m/s
  double m_ref = 4.5;
  double r_ref = 1; //km
  */
  private HashMap indexFromPerHashMap;

  private int iper;
  private double rRup, mag;
  private String stdDevType, fltType, component;
  private boolean parameterChange;
  
  private PropagationEffect propagationEffect;

  protected final static Double MAG_WARN_MIN = new Double(5);
  protected final static Double MAG_WARN_MAX = new Double(7.5);
  protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_RUP_WARN_MAX = new Double(400.0);
  
  /**
   * Site Type Parameter ("Rock/Shallow-Soil" versus "Deep-Soil")
   */
  private StringParameter siteTypeParam = null;
  public final static String SITE_TYPE_NAME = "McVerryetal Site Type";
  // no units
  public final static String SITE_TYPE_INFO = "Geological conditions at the site";
  public final static String SITE_TYPE_A = "A-Strong-Rock";
  public final static String SITE_TYPE_B = "B-Rock";
  public final static String SITE_TYPE_C = "C-Shallow-Soil";
  public final static String SITE_TYPE_D = "D-Soft-or-Deep-Soil";
  public final static String SITE_TYPE_DEFAULT = SITE_TYPE_C;

  
  // style of faulting options
  public final static String FLT_TYPE_STRIKE_SLIP = "Strike-Slip";
  public final static String FLT_TYPE_REVERSE = "Reverse";
  public final static String FLT_TYPE_REVERSE_OBLIQUE = "Oblique-Reverse";
  public final static String FLT_TYPE_NORMAL = "Normal";
  public final static String FLT_TYPE_INTERFACE = "Subduction-Interface";
  public final static String FLT_TYPE_DEEP_SLAB = "Subduction-Deep-Slab";

  // change component default from that of parent
  //Currently the above coefficients are for the (unrotated) geometric mean - will include larger horizontal later
  public final static String COMPONENT_GEOMEAN = ComponentParam.COMPONENT_RANDOM_HORZ;
  //public final static String COMPONENT_LARGERHORIZ = COMPONENT_LARGER;
 
  // for issuing warnings:
  private transient ParameterChangeWarningListener warningListener = null;

  /**
   *  This initializes several ParameterList objects.
   */
  public McVerryetal_2000_AttenRel(ParameterChangeWarningListener
                                    warningListener) {

    super();

    this.warningListener = warningListener;

    initSupportedIntensityMeasureParams();
    indexFromPerHashMap = new HashMap();
    for (int i = 1; i < period.length ; i++) {
      indexFromPerHashMap.put(new Double(period[i]), new Integer(i));
    }

    initEqkRuptureParams();
    initPropagationEffectParams();
    initSiteParams();
    initOtherParams();

    initIndependentParamLists(); // This must be called after the above
    initParameterEventListeners(); //add the change listeners to the parameters

    propagationEffect = new PropagationEffect();
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
   */
  public void setSite(Site site) throws ParameterException {

	    siteTypeParam.setValue((String)site.getParameter(SITE_TYPE_NAME).getValue());
	    this.site = site;
	    setPropagationEffectParams();

  }

  /**
   * This sets the  propagation-effect parameter (distanceRupParam) based on the current site and eqkRupture.  
   */
  protected void setPropagationEffectParams() {

    if ( (this.site != null) && (this.eqkRupture != null)) {
   
    	propagationEffect.setAll(this.eqkRupture, this.site); // use this for efficiency
    	distanceRupParam.setValueIgnoreWarning(propagationEffect.getParamValue(distanceRupParam.NAME)); // this sets rRup too
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

    iper = ( (Integer) indexFromPerHashMap.get(saPeriodParam.getValue())).
       intValue();

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
		  setCoeffIndex(); // intensityMeasureChanged is set to false in this method
	  }
	  
	  // remember that pga4nl term uses coeff index 0
	  double pga = Math.exp(getMean(0, siteTypeParam, rRup, mag, fltType));
	  double pga_prime = Math.exp(getMean(1, siteTypeParam, rRup, mag, fltType));
	  double sa_prime = Math.exp(getMean(iper, siteTypeParam, rRup, mag, fltType));
	  return (sa_prime*pga/pga_prime);
  }

  /**
   * @return    The stdDev value
   */
  public double getStdDev() {
    if (intensityMeasureChanged) {
      setCoeffIndex();// intensityMeasureChanged is set to false in this method
    }
    return getStdDev(iper, stdDevType);
  }

  /**
   * Determines the style of faulting from the rake angle.  Their report is not explicit,
   * so these ranges come from an email that told us to decide, but that within 30-degrees
   * of horz for SS was how the NGA data were defined.
   *
   * @param rake                      in degrees
   * @throws InvalidRangeException    If not valid rake angle
   */
  protected void setFaultTypeFromRake(double rake) throws InvalidRangeException {
	  if (rake<=33 && rake>=-33)
		  fltTypeParam.setValue(FLT_TYPE_STRIKE_SLIP);
	  else if (rake<=-147 || rake>=147)
		  fltTypeParam.setValue(FLT_TYPE_STRIKE_SLIP);
	  else if (rake > 33 && rake < 66)
		  fltTypeParam.setValue(FLT_TYPE_REVERSE_OBLIQUE);
	  else if (rake >123 && rake <147)
		  fltTypeParam.setValue(FLT_TYPE_REVERSE_OBLIQUE);
	  else if (rake > 66 && rake <123)
		  fltTypeParam.setValue(FLT_TYPE_REVERSE);
	  else if (rake > -147 && rake < -33)
		  fltTypeParam.setValue(FLT_TYPE_NORMAL);
	  // NEED ADDITIONAL LINES HERE FOR THE SUBDUCTION SOURCE TYPES
  } 
  
  
  
  /**
   * Allows the user to set the default parameter values for the selected Attenuation
   * Relationship.
   */
  public void setParamDefaults() {

	siteTypeParam.setValue(SITE_TYPE_DEFAULT);
    magParam.setValueAsDefault();
    distanceRupParam.setValueAsDefault();
    fltTypeParam.setValueAsDefault();
    saParam.setValueAsDefault();
    saPeriodParam.setValueAsDefault();
    saDampingParam.setValueAsDefault();
    pgaParam.setValueAsDefault();
    stdDevTypeParam.setValueAsDefault();

    mag = ( (Double) magParam.getValue()).doubleValue();
    fltType = (String) fltTypeParam.getValue();
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
    meanIndependentParams.addParameter(distanceRupParam);
    meanIndependentParams.addParameter(siteTypeParam);
    meanIndependentParams.addParameter(magParam);
    meanIndependentParams.addParameter(fltTypeParam);
    meanIndependentParams.addParameter(componentParam);

    // params that the stdDev depends upon
    stdDevIndependentParams.clear();
    stdDevIndependentParams.addParameterList(meanIndependentParams);
    stdDevIndependentParams.addParameter(stdDevTypeParam);

    // params that the exceed. prob. depends upon
    exceedProbIndependentParams.clear();
    exceedProbIndependentParams.addParameterList(stdDevIndependentParams);
    exceedProbIndependentParams.addParameter(sigmaTruncTypeParam);
    exceedProbIndependentParams.addParameter(sigmaTruncLevelParam);

    // params that the IML at exceed. prob. depends upon
    imlAtExceedProbIndependentParams.addParameterList(
        exceedProbIndependentParams);
    imlAtExceedProbIndependentParams.addParameter(exceedProbParam);
  }

  protected void initSiteParams() {
	    
	    StringConstraint siteConstraint = new StringConstraint();
	    siteConstraint.addString(SITE_TYPE_A);
	    siteConstraint.addString(SITE_TYPE_B);
	    siteConstraint.addString(SITE_TYPE_C);
	    siteConstraint.addString(SITE_TYPE_D);
	    siteConstraint.setNonEditable();
	    siteTypeParam = new StringParameter(SITE_TYPE_NAME, siteConstraint, null);
	    siteTypeParam.setInfo(SITE_TYPE_INFO);
	    siteTypeParam.setNonEditable();

	    siteParams.clear();
	    siteParams.addParameter(siteTypeParam);    
	  }

	  /**
	   *  Creates the two Potential Earthquake parameters (magParam and
	   *  fltTypeParam) and adds them to the eqkRuptureParams
	   *  list. Makes the parameters noneditable.
	   */
	  protected void initEqkRuptureParams() {

		magParam = new MagParam(MAG_WARN_MIN, MAG_WARN_MAX);
	    
	    StringConstraint constraint = new StringConstraint();
	    constraint.addString(FLT_TYPE_STRIKE_SLIP);
	    constraint.addString(FLT_TYPE_REVERSE);
	    constraint.addString(FLT_TYPE_REVERSE_OBLIQUE);
	    constraint.addString(FLT_TYPE_NORMAL);
	    constraint.addString(FLT_TYPE_INTERFACE);
	    constraint.addString(FLT_TYPE_DEEP_SLAB);
	    constraint.setNonEditable();
	    fltTypeParam = new FaultTypeParam(constraint,FLT_TYPE_STRIKE_SLIP);

	    eqkRuptureParams.clear();
	    eqkRuptureParams.addParameter(magParam);
	    eqkRuptureParams.addParameter(fltTypeParam);
	  }

	  /**
	   *  Creates the Propagation Effect parameters and adds them to the
	   *  propagationEffectParams list. Makes the parameters noneditable.
	   */
	  protected void initPropagationEffectParams() {

	    distanceRupParam = new DistanceRupParameter(0.0);
	    DoubleConstraint warn = new DoubleConstraint(DISTANCE_RUP_WARN_MIN,
	                                                 DISTANCE_RUP_WARN_MAX);
	    warn.setNonEditable();
	    distanceRupParam.setWarningConstraint(warn);
	    distanceRupParam.addParameterChangeWarningListener(warningListener);

	    distanceRupParam.setNonEditable();
	    
	    propagationEffectParams.addParameter(distanceRupParam);
	  }

	  /**
	   *  Creates the two supported IM parameters (PGA and SA), as well as the
	   *  independenParameters of SA (periodParam and dampingParam) and adds
	   *  them to the supportedIMParams list. Makes the parameters noneditable.
	   */
	  protected void initSupportedIntensityMeasureParams() {

	    // Create saParam:
	    DoubleDiscreteConstraint periodConstraint = new DoubleDiscreteConstraint();
	    for (int i = 2; i < period.length; i++) {
	      periodConstraint.addDouble(new Double(period[i]));
	    }
	    periodConstraint.setNonEditable();
		saPeriodParam = new PeriodParam(periodConstraint);
		saDampingParam = new DampingParam();
		saParam = new SA_Param(saPeriodParam, saDampingParam);
		saParam.setNonEditable();

		//  Create PGA Parameter (pgaParam):
		pgaParam = new PGA_Param();
		pgaParam.setNonEditable();
		    
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
	    constraint.addString(ComponentParam.COMPONENT_RANDOM_HORZ);
	    componentParam = new ComponentParam(constraint,ComponentParam.COMPONENT_RANDOM_HORZ);
	    
	    // the stdDevType Parameter
	    StringConstraint stdDevTypeConstraint = new StringConstraint();
	    stdDevTypeConstraint.addString(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
	    stdDevTypeConstraint.addString(StdDevTypeParam.STD_DEV_TYPE_NONE);
	    stdDevTypeConstraint.addString(StdDevTypeParam.STD_DEV_TYPE_INTER);
	    stdDevTypeConstraint.addString(StdDevTypeParam.STD_DEV_TYPE_INTRA);
	    stdDevTypeConstraint.setNonEditable();
	    stdDevTypeParam = new StdDevTypeParam(stdDevTypeConstraint);

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

  public double getMean(int iper, StringParameter siteTypeParam, double rRup, double mag,
                        String fltType) {

    // initialise dummy variables
    double CN=0.0, CR=0.0, SI=0.0, DS=0.0, deltaC=0.0, deltaD=0.0;
    String siteType = siteTypeParam.getValue().toString();
    double lnSA_AB,lnSA_CD;
    double rVol=0.0, hc=0.0;  // need to change value of rVol and Hc 
    
    //allocate dummy fault variables
    if(fltType.equals(FLT_TYPE_NORMAL)) {
    	 CN=-1.0;
    	 CR=0.0;
    }
    else if(fltType.equals(FLT_TYPE_REVERSE)) {
    	 CN=0.0;
         CR=1.0;
    }
    else if(fltType.equals(FLT_TYPE_REVERSE_OBLIQUE)) {
    	 CN=0.0;
         CR=0.5;
    }
    else if(fltType.equals(FLT_TYPE_INTERFACE)) {
   	     SI=1.0;
    }
    else if(fltType.equals(FLT_TYPE_DEEP_SLAB)) {
  	     DS=1.0;
    }
    
    //allocate dummy site variables
    if(siteType.equals(SITE_TYPE_C)) {
    	deltaC=1.0;
    }
    else if(siteType.equals(SITE_TYPE_C)) {
	   	deltaD=1.0;
	}

    //Crustal attenuation relation
    lnSA_AB=C1[iper]+C4AS*(mag-6.)+C3AS[iper]*Math.pow(8.5-mag,2)+C5[iper]*rRup+(C8[iper]+C6AS*(mag-6.))*Math.log(Math.sqrt(Math.pow(rRup,2.)+Math.pow(C10AS[iper],2.)))+C46[iper]*rVol+C32*CN+C33AS[iper]*CR;
    
    //Subduction attenuation relation
    //lnSA_AB=C11[iper]+(C12y+(C15[iper]-C17[iper])*C19y)*(mag-6)+C13y[iper]*Math.pow(10-mag,3.)+C17[iper]*Math.log(rRup+C18y*Math.exp(C19y*mag))+C20[iper]*hc+C24[iper]*SI+C46[iper]*rVol*(1-DS);
    
    //site terms
    lnSA_CD=lnSA_AB+C29[iper]*deltaC+(C30AS[iper]*Math.log(Math.exp(lnSA_AB)+0.03)+C43[iper])*deltaD;
    double lnSA = lnSA_CD;
    
    return lnSA;
  }

  public double getStdDev(int iper, String stdDevType) {
	  
	if(stdDevType.equals(StdDevTypeParam.STD_DEV_TYPE_NONE)) {
		return 0.0;
	}
	else if (stdDevType.equals(StdDevTypeParam.STD_DEV_TYPE_INTER)) {
		return (tau[iper]);
	}
	else {   
		double sigmaIntra;
		if (mag <=5.0) {
			sigmaIntra=sigma6[iper]-sigSlope[iper];
		}
		else if (mag >=7.0) {
			sigmaIntra=sigma6[iper]+sigSlope[iper];
		}
		else {
			sigmaIntra=sigma6[iper]+sigSlope[iper]*(mag-6.);
		}
		
		if (stdDevType.equals(StdDevTypeParam.STD_DEV_TYPE_INTRA)) {
			return sigmaIntra;
		}
		else if (stdDevType.equals(StdDevTypeParam.STD_DEV_TYPE_TOTAL)) {
			double sigmaTotal = Math.sqrt(Math.pow(sigmaIntra,2.)+Math.pow(tau[iper],2.));
			return (sigmaTotal);
		}
		else { 
			return Double.NaN;
		}
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
	  //else if (pName.equals(Vs30_Param.NAME)) {
		//  vs30 = ( (Double) val).doubleValue();
	  //}
	  else if (pName.equals(magParam.NAME)) {
		  mag = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(StdDevTypeParam.NAME)) {
		  stdDevType = (String) val;
	  }
	  else if (pName.equals(ComponentParam.NAME)) {
		  component = (String)componentParam.getValue();
	  }
	  else if (pName.equals(PeriodParam.NAME)) {
		  intensityMeasureChanged = true;
	  }
  }
  /**
   * Allows to reset the change listeners on the parameters
   */
  public void resetParameterEventListeners(){
    distanceRupParam.removeParameterChangeListener(this);
    siteTypeParam.removeParameterChangeListener(this);
    magParam.removeParameterChangeListener(this);
    fltTypeParam.removeParameterChangeListener(this);
    stdDevTypeParam.removeParameterChangeListener(this);
    saPeriodParam.removeParameterChangeListener(this);

    this.initParameterEventListeners();
  }

  /**
   * Adds the parameter change listeners. This allows to listen to when-ever the
   * parameter is changed.
   */
  protected void initParameterEventListeners() {

    distanceRupParam.addParameterChangeListener(this);
    siteTypeParam.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    fltTypeParam.addParameterChangeListener(this);
    stdDevTypeParam.addParameterChangeListener(this);
    saPeriodParam.addParameterChangeListener(this);
  }

  
  /**
   * This provides a URL where more info on this model can be obtained
   * @throws MalformedURLException if returned URL is not a valid URL.
   * @returns the URL to the AttenuationRelationship document on the Web.
   */
  public URL getInfoURL() throws MalformedURLException{
	  return new URL("http://www.opensha.org/documentation/modelsImplemented/attenRel/McVerryetal_2000.html");
  }
}