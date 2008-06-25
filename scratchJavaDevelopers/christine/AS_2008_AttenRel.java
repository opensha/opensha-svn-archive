// package org.opensha.sha.imr.attenRelImpl;

 package scratchJavaDevelopers.christine;

 
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.opensha.data.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.imr.*;
import org.opensha.sha.param.*;
import org.opensha.sha.surface.*;
import org.opensha.util.FileUtils;

/**
 * <b>Title:</b> AS_2008_AttenRel<p>
 *
 * <b>Description:</b> This implements the Attenuation Relationship published by Abrahamson & Silva (2008, Earthquake Spectra, pre-print v06) <p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>pgvParam - Peak Ground Velocity
 * <LI>saParam - Response Spectral Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>fltTypeParam - Style of faulting
 * <LI>rupTopDepthParam - Depth to top of rupture
 * <LI>dipParam - Rupture surface dip
 * <LI>distanceRupParam - Closest distance to surface projection of fault
 * <LI>distanceRupXParam - Horizontal distance from top edge of rupture
 * <li>distRupMinusJB_OverRupParam - used as a proxy for hanging wall effect
 * <LI>vs30Param 
 * <li>depthTo1pt0kmPerSecParam
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
 * Verification :
 * </p>
 *
 * @author     Christine Goulet
 * @created    Jun., 2008
 * @version    1.0
 */


public class AS_2008_AttenRel
    extends AttenuationRelationship implements
    AttenuationRelationshipAPI,
    NamedObjectAPI, ParameterChangeListener {

  // Debugging stuff
  private final static String C = "AS_2008_CG_AttenRel";
  private final static boolean D = false;
  public final static String SHORT_NAME = "AS2008";
  private static final long serialVersionUID = 1234567890987654358L;


  // Name of IMR
  public final static String NAME = "Abrahamson & Silva (2008)";
  private final static String AS_2008_CoeffFile = "as_2008_coeff.txt";
  
  // Local variables declaration
  double[] per,VLIN,b,a1,a2,a8,a10,a12,a13,a14,a15,a16,a18,s1e,s2e,s1m,s2m,s3,s4,rho;
  
  double c1 = 6.75;
  double c4 = 4.5;
  double a3 = 0.265;
  double a4 = -0.265;
  double a5 = -0.398;
  double N = 1.18;
  double c = 1.88;
  double c2 = 50.0;
    
  private HashMap indexFromPerHashMap;

  private int iper;
  private double vs30, rJB, rRup, distRupMinusJB_OverRup, f_rv, f_nm, mag, depthTop, depthTo1pt0kmPerSec,dip;
  private String stdDevType, component;
  private boolean magSaturation;
  private boolean parameterChange;
  
  private PropagationEffect propagationEffect;

  // values for warning parameters
  protected final static Double MAG_WARN_MIN = new Double(4.0);
  protected final static Double MAG_WARN_MAX = new Double(8.5);
  protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_RUP_WARN_MAX = new Double(200.0);
  protected final static Double DISTANCE_MINUS_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_MINUS_WARN_MAX = new Double(50.0);
  protected final static Double VS30_WARN_MIN = new Double(150.0);
  protected final static Double VS30_WARN_MAX = new Double(1500.0);
  protected final static Double DEPTH_1pt0_WARN_MIN = new Double(0);
  protected final static Double DEPTH_1pt0_WARN_MAX = new Double(10);
  protected final static Double DIP_WARN_MIN = new Double(15);
  protected final static Double DIP_WARN_MAX = new Double(90);
  protected final static Double RUP_TOP_WARN_MIN = new Double(0);
  protected final static Double RUP_TOP_WARN_MAX = new Double(15);
  
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
  public AS_2008_AttenRel(ParameterChangeWarningListener warningListener) {

    super();
    
    this.warningListener = warningListener;
    readCoeffFile();
    initSupportedIntensityMeasureParams();
    indexFromPerHashMap = new HashMap();
    for (int i = 2; i < per.length; i++) {
      indexFromPerHashMap.put(new Double(per[i]), new Integer(i));
    }

    initEqkRuptureParams();
    initPropagationEffectParams();
    initSiteParams();
    initOtherParams();

    initIndependentParamLists(); // This must be called after the above
    initParameterEventListeners(); //add the change listeners to the parameters
    
    propagationEffect = new PropagationEffect();
    propagationEffect.fixDistanceJB(true); // this ensures that it's exatly zero over the discretized rupture surfaces
 
    }
  
  @SuppressWarnings("unchecked")
private void readCoeffFile(){
	  try{
		ArrayList<String> coeff= FileUtils.loadJarFile(AS_2008_CoeffFile);
		//reading the Period
		String perLine = coeff.get(0);
		ArrayList period = new ArrayList<Double>();
		StringTokenizer st = new StringTokenizer(perLine);
		int size = loadCoeffInArray(st,period);
		per = new double[size];
		this.createCoeffArray(period, per);
		period = null;
		
		//reading VLIN
		String VLINLine = coeff.get(1);
		ArrayList VLINList = new ArrayList<Double>();
		st = new StringTokenizer(VLINLine);
		size =loadCoeffInArray(st,VLINList);
		VLIN = new double[size];
		this.createCoeffArray(VLINList, VLIN);
		VLINList = null;
		
		//reading b
		String bLine = coeff.get(2);
		ArrayList bList = new ArrayList<Double>();
		st = new StringTokenizer(bLine);
		size =loadCoeffInArray(st,bList);
		b = new double[size];
		this.createCoeffArray(bList, b);
		bList = null;
		
		//reading a1
		String a1Line = coeff.get(3);
		ArrayList a1List = new ArrayList<Double>();
		st = new StringTokenizer(a1Line);
		size =loadCoeffInArray(st,a1List);
		a1 = new double[size];
		this.createCoeffArray(a1List, a1);
		a1List = null;
		
		//reading a2
		String a2Line = coeff.get(4);
		ArrayList a2List = new ArrayList<Double>();
		st = new StringTokenizer(a2Line);
		size =loadCoeffInArray(st,a2List);
		a2 = new double[size];
		this.createCoeffArray(a2List, a2);
		a2List = null;
		
		//reading a8
		String a8Line = coeff.get(5);
		ArrayList a8List = new ArrayList<Double>();
		st = new StringTokenizer(a8Line);
		size =loadCoeffInArray(st,a8List);
		a8 = new double[size];
		this.createCoeffArray(a8List, a8);
		a8List = null;
		
		//reading a10
		String a10Line = coeff.get(6);
		ArrayList a10List = new ArrayList<Double>();
		st = new StringTokenizer(a10Line);
		size =loadCoeffInArray(st,a10List);
		a10 = new double[size];
		this.createCoeffArray(a10List, a10);
		a10List = null;
		
		//reading a12
		String a12Line = coeff.get(7);
		ArrayList a12List = new ArrayList<Double>();
		st = new StringTokenizer(a12Line);
		size =loadCoeffInArray(st,a12List);
		a12 = new double[size];
		this.createCoeffArray(a12List, a12);
		a12List = null;
		
		//reading a13
		String a13Line = coeff.get(8);
		ArrayList a13List = new ArrayList<Double>();
		st = new StringTokenizer(a13Line);
		size =loadCoeffInArray(st,a13List);
		a13 = new double[size];
		this.createCoeffArray(a13List, a13);
		a13List = null;
		
		//reading a14
		String a14Line = coeff.get(9);
		ArrayList a14List = new ArrayList<Double>();
		st = new StringTokenizer(a14Line);
		size =loadCoeffInArray(st,a14List);
		a14 = new double[size];
		this.createCoeffArray(a14List, a14);
		a14List = null;
		
		//reading a15
		String a15Line = coeff.get(10);
		ArrayList a15List = new ArrayList<Double>();
		st = new StringTokenizer(a15Line);
		size =loadCoeffInArray(st,a15List);
		a15 = new double[size];
		this.createCoeffArray(a15List, a15);
		a15List = null;
		
		
		//reading a16
		String a16Line = coeff.get(11);
		ArrayList a16List = new ArrayList<Double>();
		st = new StringTokenizer(a16Line);
		size =loadCoeffInArray(st,a16List);
		a16 = new double[size];
		this.createCoeffArray(a16List, a16);
		a16List = null;
		
		//reading a18
		String a18Line = coeff.get(12);
		ArrayList a18List = new ArrayList<Double>();
		st = new StringTokenizer(a18Line);
		size =loadCoeffInArray(st,a18List);
		a18 = new double[size];
		this.createCoeffArray(a18List, a18);
		a18List = null;
		
		
		//reading s1e
		String s1eLine = coeff.get(13);
		ArrayList s1eList = new ArrayList<Double>();
		st = new StringTokenizer(s1eLine);
		size =loadCoeffInArray(st,s1eList);
		s1e = new double[size];
		this.createCoeffArray(s1eList, s1e);
		s1eList = null;
		
		//reading s2e
		String s2eLine = coeff.get(14);
		ArrayList s2eList = new ArrayList<Double>();
		st = new StringTokenizer(s2eLine);
		size =loadCoeffInArray(st,s2eList);
		s2e = new double[size];
		this.createCoeffArray(s2eList, s2e);
		s2eList = null;
		
		//reading s1m
		String s1mLine = coeff.get(15);
		ArrayList s1mList = new ArrayList<Double>();
		st = new StringTokenizer(s1mLine);
		size =loadCoeffInArray(st,s1mList);
		s1m = new double[size];
		this.createCoeffArray(s1mList, s1m);
		s1mList = null;
		
		//reading s2m
		String s2mLine = coeff.get(16);
		ArrayList s2mList = new ArrayList<Double>();
		st = new StringTokenizer(s2mLine);
		size =loadCoeffInArray(st,s2mList);
		s2m = new double[size];
		this.createCoeffArray(s2mList, s2m);
		s2mList = null;
		
		
		//reading s3
		String s3Line = coeff.get(17);
		ArrayList s3List = new ArrayList<Double>();
		st = new StringTokenizer(s3Line);
		size =loadCoeffInArray(st,s3List);
		s3 = new double[size];
		this.createCoeffArray(s3List, s3);
		s3List = null;
		
		
		//reading s4
		String s4Line = coeff.get(18);
		ArrayList s4List = new ArrayList<Double>();
		st = new StringTokenizer(s4Line);
		size =loadCoeffInArray(st,s4List);
		s4 = new double[size];
		this.createCoeffArray(s4List, s4);
		s4List = null;
		
		//reading rho
		String rho_sLine = coeff.get(19);
		ArrayList rho_sList = new ArrayList<Double>();
		st = new StringTokenizer(rho_sLine);
		size =loadCoeffInArray(st,rho_sList);
		rho = new double[size];
		this.createCoeffArray(rho_sList, rho);
		rho_sList = null;
		
		
	  }catch(IOException e){
		  System.out.println(AS_2008_CoeffFile+" file Not Found");
		  e.printStackTrace();
	  }
  }
  
  private int loadCoeffInArray(StringTokenizer st,ArrayList<Double> coeff){
	  st.nextToken();
	  while(st.hasMoreTokens())
		  coeff.add(Double.parseDouble(st.nextToken().trim()));
	  return coeff.size();
  }
  
  private void createCoeffArray(ArrayList<Double> coeff,double c[]){
	  for(int i=0;i<c.length;++i)
		  c[i] = coeff.get(i);
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
		  fltTypeParam.setValue(FLT_TYPE_REVERSE);
	  }
	  else if(rake >-150 && rake<-30) {
		  fltTypeParam.setValue(FLT_TYPE_NORMAL);
	  }
	  else { // strike slip
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
    depthTo1pt0kmPerSecParam.setValueIgnoreWarning(site.getParameter(this.DEPTH_1pt0_NAME).
                                      getValue());
    this.site = site;
    setPropagationEffectParams();

  }

  /**
   * This sets the two propagation-effect parameters (distanceRupParam and
   * distRupMinusJB_OverRupParam) based on the current site and eqkRupture.  
   */
  protected void setPropagationEffectParams() {

    if ( (this.site != null) && (this.eqkRupture != null)) {
   
    	propagationEffect.setAll(this.eqkRupture, this.site); // use this for efficiency
//    	System.out.println(propagationEffect.getParamValue(distanceRupParam.NAME));
    	distanceRupParam.setValueIgnoreWarning(propagationEffect.getParamValue(distanceRupParam.NAME)); // this sets rRup too
    	double dist_jb = ((Double)propagationEffect.getParamValue(DistanceJBParameter.NAME)).doubleValue();
    	double dRupMinusJB_OverRup = (rRup-dist_jb)/rRup;
    	distRupMinusJB_OverRupParam.setValueIgnoreWarning(dRupMinusJB_OverRup);
    }
  }

	double rX = rRup; // CG 20080624: need to add a new parameter rX somewhere else. Used as = to rRup to get going for now
	double rupWidth = 10; // CG 20080624: need to add a new parameter rX somewhere else. Used as = 10 to get going for now

  
  
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
      iper = ( (Integer) indexFromPerHashMap.get(periodParam.getValue())).intValue();
    }
    else if (im.getName().equalsIgnoreCase(PGV_NAME)) {
        iper = 1;
      }
    else if (im.getName().equalsIgnoreCase(PGA_NAME)) {
    		iper = 2;
    }
    parameterChange = true;
    intensityMeasureChanged = false;

  }

  /**
   * Calculates the mean of the exceedance probability distribution. <p>
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
	  // (must do this here because we get pga_rock below by passing in 1100 m/s)
	  if(Double.isNaN(depthTo1pt0kmPerSec)){
		  if(vs30 <= 2500)
			  depthTo1pt0kmPerSec = 2;
		  else
			  depthTo1pt0kmPerSec = 0;
	  }
	    
	  double pga_rock = Math.exp(getMean(2, 1100, rRup, rJB, rX, f_rv, f_nm, mag, dip,
			  rupWidth, depthTop, depthTo1pt0kmPerSec, magSaturation, 0));
	  
	  double mean = getMean(iper, vs30, rRup, rJB, rX, f_rv, f_nm, mag, dip, rupWidth,
			  depthTop, depthTo1pt0kmPerSec, magSaturation, pga_rock);
	  
// System.out.println(mean+"\t"+iper+"\t"+vs30+"\t"+rRup+"\t"+rJB+"\t"+f_rv+"\t"+f_nm+"\t"+mag+"\t"+dip+"\t"+depthTop+"\t"+depthTo1pt0kmPerSec+"\t"+magSaturation+"\t"+pga_rock);

// make sure SA does not exceed PGA if per < 0.2 (page 11 of pre-print)
	  if(iper < 3 || iper > 11 ) // not SA period between 0.02 and 0.15
		  return mean;
	  else {
		  double pga_mean = getMean(2, vs30, rRup, rJB, rX, f_rv, f_nm, mag, dip, rupWidth,
				  depthTop, depthTo1pt0kmPerSec, magSaturation, pga_rock); // mean for PGA
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
	  
	  // compute rJB
	  rJB = rRup - distRupMinusJB_OverRup*rRup;


	  // set default value of basin depth based on the final value of vs30
	  // (must do this here because we get pga_rock below by passing in 1100 m/s)
	  if(Double.isNaN(depthTo1pt0kmPerSec)){
		  if(vs30 <= 2500)
			  depthTo1pt0kmPerSec = 2;
		  else
			  depthTo1pt0kmPerSec = 0;
	  }

	  double pga_rock = Double.NaN;
	  if(vs30 < VLIN[iper]) 
		  pga_rock = Math.exp(getMean(2, 1100, rRup, rX, rJB, f_rv, f_nm, mag,dip, rupWidth, depthTop, depthTo1pt0kmPerSec, magSaturation, 0));
	  
	  component = (String)componentParam.getValue();
	  
	  double stdDev = getStdDev(iper, stdDevType, component, vs30, pga_rock);
	  
//System.out.println(stdDev+"\t"+iper+"\t"+stdDevType+"\t"+component+"\t"+vs30+"\t"+pga_rock);

	  return stdDev;
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
    componentParam.setValue(COMPONENT_DEFAULT);
    stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);
    depthTo1pt0kmPerSecParam.setValue(DEPTH_1pt0_DEFAULT);
    dipParam.setValue(DIP_DEFAULT);

    
    vs30 = ( (Double) vs30Param.getValue()).doubleValue(); 
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
    meanIndependentParams.addParameter(distanceRupParam);
    meanIndependentParams.addParameter(distRupMinusJB_OverRupParam);
    meanIndependentParams.addParameter(vs30Param);
    meanIndependentParams.addParameter(depthTo1pt0kmPerSecParam);
    meanIndependentParams.addParameter(magParam);
    meanIndependentParams.addParameter(fltTypeParam);
    meanIndependentParams.addParameter(rupTopDepthParam);
    meanIndependentParams.addParameter(dipParam);
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
    DoubleConstraint warn2 = new DoubleConstraint(DEPTH_1pt0_WARN_MIN,
                                                  DEPTH_1pt0_WARN_MAX);
    warn2.setNonEditable();
    depthTo1pt0kmPerSecParam.setWarningConstraint(warn2);
    depthTo1pt0kmPerSecParam.addParameterChangeWarningListener(warningListener);
    depthTo1pt0kmPerSecParam.setNonEditable();

    siteParams.clear();
    siteParams.addParameter(vs30Param);
    siteParams.addParameter(depthTo1pt0kmPerSecParam);

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
    for (int i = 2; i < per.length; i++) {
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
    constraint.addString(COMPONENT_GMRotI50);
    
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
   * @param depthTo1pt0kmPerSec
   * @param magSaturation
   * @param pga_rock
   * @return
   */
  //@param rX
  
  
  public double getMean(int iper, double vs30, double rRup, 
                            double distJB, double rX, double f_rv,
                            double f_nm, double mag, double dip, double rupWidth, double depthTop,
                            double depthTo1pt0kmPerSec,
                            boolean magSaturation, double pga_rock) {


    double rR, v1, vs30Star, f1, f4, f5, f6, f8, f10;
    
    //"Base model": f1 term, dependent on magnitude and distance
    rR=Math.sqrt(Math.pow(rRup,2)+Math.pow(c4,2));
    
    if(mag<=c1)
    		f1 = a1[iper]+a4*(mag-c1)+a8[iper]*Math.pow(8.5-mag,2)+(a2[iper]+a3*(mag-c1))*Math.log(rR);
    else
   			f1 = a1[iper]+a5*(mag-c1)+a8[iper]*Math.pow(8.5-mag,2)+(a2[iper]+a3*(mag-c1))*Math.log(rR);

    //"Site response model": f5_pga1100 term and required computation for v1 and vs30Star
    //Vs30 dependent term v1
    if(per[iper]<=0.5)
    	v1=1500;
    else if(per[iper] > 0.5 && per[iper] <=1.0)
    	v1=Math.exp(8.0-0.795*Math.log(per[iper]/0.21));
    else if(per[iper] > 1.0 && per[iper] <2.0)
    	v1=Math.exp(6.76-0.297*Math.log(per[iper]));
    else 
    	v1 = 700;
    	    	// CG 20080624 Need to add that for IM=PGV, v1=862 m/s

    //Vs30 dependent term vs30Star
    if(vs30<v1)
    	vs30Star = vs30;
    else
    	vs30Star = v1;
    
    //f5_pga1100
    if (vs30<VLIN[iper])
    	f5 = a10[iper]*Math.log(vs30Star/VLIN[iper])-b[iper]*Math.log(pga_rock+c*Math.pow(vs30Star/VLIN[iper],N));
    else		
    	f5 = (a10[iper]+b[iper]*N)*Math.log(vs30Star / VLIN[iper]);
    
    //"Hanging wall model": f4 term and required computation of T1, T2, T3, T4 and T5
    double T1, T2, T3, T4, T5;
    
    if (distJB<30)
    	T1=1-distJB/30;
    else
    	T1=0;
    
    if (rX<=rupWidth*Math.cos(Math.toRadians(dip)))
    	T2 = 0.5+(rX/2*rupWidth*Math.cos(Math.toRadians(dip)));
    else
    	T2 = 1;
    	
    if (rX>=depthTop)
    	T3 = 1;
    else
    	T3 = rX/depthTop;
    
   if(mag<=6)
	   T4 = 0;
   else if(mag>=7)
	   T4 = 1;
   else
	   T4 = mag - 6;
   
   if(dip>=70)
	   T5 = 1-(dip-70)/20;
   else
	   T5 = 1;
	   
   // f4 term
   f4 = a14[iper]*T1*T2*T3*T4*T5;
    
   // "Depth to top of rupture model": f6 term
   if(depthTop<10)
	   f6 = a16[iper]*depthTop/10;
   else
	   f6 = a16[iper];

   // "Large distance model": f8 term and required T6 computation
   double T6;

   if(mag<5.5)
	   T6 = 1;
   else if(mag>6.5)
	   T6 = 0;
   else
	   T6 = 6.5-mag;

   if(rRup<100)
	   f8 = 0;
   else
	   f8=a18[iper]*(rRup - 100)*T6;
   
   // "Soil depth model": f10 term and required z1Hat, a21, e2 and a22 computation
   double z1Hat, e2, a21test, a21, a22;
   
   if(vs30<180)
	   z1Hat = Math.exp(6.745);
   else if(vs30>=180 && vs30<=500)
	   z1Hat = Math.exp(6.745-1.35*Math.log(vs30/180));
   else if(vs30>500 && vs30<1000)
	   z1Hat = Math.exp(5.394-4.48*Math.log(vs30/180));
   else
	   z1Hat = 1; //Math.exp(0)=1;
   
   if(per[iper]>=0.35 && per[iper]<=2 && vs30<=1000)
		e2 = -0.25*Math.log(vs30/1000)*Math.log(per[iper]/0.35);
	else if(per[iper]>=2 && vs30<=1000)
		e2 = -0.25*Math.log(vs30/1000)*Math.log(2.0/0.35);
	else
		e2 = 0; //	if(per[iper]<0.35 || vs30>1000) 

   a21test = (a10[iper] + b[iper]*N)*Math.log(vs30Star/Math.min(v1, 1000))+e2*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
   if(a21test<0)
	   a21=-(a10[iper] + b[iper]*N)*Math.log(vs30Star/Math.min(v1, 1000))/Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
	else
		a21 = e2;

   if(per[iper]<2)
	   a22 = 0;
   else
	   a22 = 0.0625*(per[iper]-2);
   
   if(depthTo1pt0kmPerSec>=200)
	   f10 = a21*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2)) + a22*Math.log(depthTo1pt0kmPerSec/200);
   else
	   f10 = a21*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
	   
   
   // "Constant displacement model" : Td
   double Td;
   		Td=Math.pow(-1.25+0.3*mag,10);
   		
   // "Mean" 
   		double cgMean;
   		//if(per[iper]<Td)
   			cgMean = f1 + a12[iper]*f_rv +a13[iper]*f_nm + f5 + f4 + f6 + f8 + f10; 
   		//else
   			//cgMean = 
   		
   		return cgMean;
   		
   
  }

 /**
  * 
  * @param iper
  * @param stdDevType
  * @param component
  * @return
  */
  public double getStdDev(int iper, String stdDevType, String component, double vs30, double rock_pga) {

	  if (stdDevType.equals(STD_DEV_TYPE_NONE))
		  return 0.0;
	  else {

		  // get tau - inter-event term
		  double tau =0;

		  // compute intra-event sigma
		  double sigma = 0;

		  // compute total sigma
		  double sigma_total = Math.sqrt(tau*tau + sigma*sigma);

		  	  return sigma_total;	
//			  return Double.NaN;   // just in case invalid stdDev given			  

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
		  distRupMinusJB_OverRup = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(VS30_NAME)) {
		  vs30 = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(DEPTH_1pt0_NAME)) {
		  if(val == null)
			  depthTo1pt0kmPerSec = Double.NaN;  // can't set the defauly here because vs30 could still change
		  else
			  depthTo1pt0kmPerSec = ( (Double) val).doubleValue();
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
    depthTo1pt0kmPerSecParam.removeParameterChangeListener(this);
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
    depthTo1pt0kmPerSecParam.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    fltTypeParam.addParameterChangeListener(this);
    rupTopDepthParam.addParameterChangeListener(this);
    stdDevTypeParam.addParameterChangeListener(this);
    periodParam.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);
  }

  /**
   * 
   * @throws MalformedURLException if returned URL is not a valid URL.
   * @returns the URL to the AttenuationRelationship document on the Web.
   */
  public URL getAttenuationRelationshipURL() throws MalformedURLException{
	  return new URL("http://www.opensha.org/documentation/modelsImplemented/attenRel/AS_2008.html");
  }   
  
  /**
   * This tests DistJB numerical precision with respect to the f_hngR term.  Looks OK now.
   * @param args
   */
  public static void main(String[] args) {

	  Location loc1 = new Location(-0.1, 0.0, 0);
	  Location loc2 = new Location(+0.1, 0.0, 0);
	  FaultTrace faultTrace = new FaultTrace("test");
	  faultTrace.addLocation(loc1);
	  faultTrace.addLocation(loc2);	  
	  StirlingGriddedSurface surface = new StirlingGriddedSurface(faultTrace, 45.0,0,10,1);
	  EqkRupture rup = new EqkRupture();
	  rup.setMag(7);
	  rup.setAveRake(90);
	  rup.setRuptureSurface(surface);
	  
	  AS_2008_AttenRel attenRel = new AS_2008_AttenRel(null);
	  attenRel.setParamDefaults();
	  attenRel.setIntensityMeasure("PGA");
	  attenRel.setEqkRupture(rup);
	  
	  Site site = new Site();
	  site.addParameter(attenRel.getParameter(attenRel.VS30_NAME));
	  site.addParameter(attenRel.getParameter(attenRel.DEPTH_1pt0_NAME));
	  
	  Location loc;
	  for(double dist=-0.3; dist<=0.3; dist+=0.01) {
		  loc = new Location(0,dist);
		  site.setLocation(loc);
		  attenRel.setSite(site);
//		  System.out.print((float)dist+"\t");
		  attenRel.getMean();
	  }
	  
  }
  
}
