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
 * <b>Description:</b> This implements the Attenuation Relationship published by Abrahamson & Silva (see reference below) <p>
 * <b>Reference:</b> Abrahamson, N. and Silva, W. (2008) "Summary of the Abrahamson & Silva NGA Ground-Motion Relations", Earthquake Spectra, 24(1) , pp. 67-97 <p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>pgvParam - Peak Ground Velocity
 * <LI>saParam - Response Spectral Acceleration
 * </UL>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - Moment magnitude
 * <LI>fltTypeParam - Style of faulting (SS, REV, NORM)
 * <LI>rupTopDepthParam - Depth to top of rupture (km)
 * <LI>rupWidth - Down-dip rupture width (km)
 * <LI>dipParam - Rupture surface dip (degrees)
 * <LI>distanceRupParam - Closest distance to surface projection of fault (km)
 * <LI>vs30Param - Average shear wave velocity of top 30 m of soil/rock (m/s)
 * <li>depthTo1pt0kmPerSecParam - Depth to shear wave velocity Vs=1.0km/s (km)
 * <LI>componentParam - Component of shaking - Only GMRotI50 is supported
 * <LI>stdDevTypeParam - The type of standard deviation
 * <LI>rX - Horizontal distance from top edge of rupture
 * <LI>rJB - Joyner-Boore distance
 * </UL>
 * <p>
 * Verification : Not done as of June 28 2008
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
  
  /**
   * Depth 1.0 km/sec Parameter, reserved for representing the depth to where
   * shear-wave velocity = 1.0 km/sec ("Z1.0 (m)" in PEER's 2008 NGA flat file);
   */
   protected WarningDoubleParameter depthTo1pt0kmPerSecParam = new WarningDoubleParameter(DEPTH_1pt0_NAME, DEPTH_1pt0_DEFAULT);
   public final static String DEPTH_1pt0_NAME = "Depth to Vs = 1.0 km/sec (m)";
   public final static String DEPTH_1pt0_UNITS = "m";
   public final static String DEPTH_1pt0_INFO =
   "The depth (m) to where shear-wave velocity = 1.0 km/sec";
   public final static Double DEPTH_1pt0_DEFAULT = new Double("1.0");
   protected final static Double DEPTH_1pt0_MIN = new Double(0.0);
   protected final static Double DEPTH_1pt0_MAX = new Double(30000.0);
 
  /**
   * Down-dip width of fault rupture plane
   */
   protected WarningDoubleParameter rupWidthParam = new WarningDoubleParameter(RUP_WIDTH_NAME, RUP_WIDTH_DEFAULT);
   public final static String RUP_WIDTH_NAME = "FaultRup. Down-Dip Width (km)";
   public final static String RUP_WIDTH_UNITS = "km";
   public final static String RUP_WIDTH_INFO =
   "Fault down-dip rupture width (km)";
   protected final static Double RUP_WIDTH_MIN = new Double(1.0);
   protected final static Double RUP_WIDTH_MAX = new Double(100.0);
 
  /**
   * Horizontal distance to top edge of fault rupture used in Abrahamson & Silva and Chiou & Youngs
   */
   protected WarningDoubleParameter distanceXParam = new WarningDoubleParameter(DISTANCE_X_NAME, DISTANCE_X_DEFAULT);
   public final static String DISTANCE_X_NAME = "DistanceX (km)";
   public final static String DISTANCE_X_UNITS = "km";
   public final static String DISTANCE_X_INFO =
   "Horizontal distance to top edge of rupture, measured ppd to strike (km)";
   protected final static Double DISTANCE_X_MIN = new Double(0.0);
   protected final static Double DISTANCE_X_MAX = new Double(200.0);
 
   /**
   * Joyner-Boore distance
   */
   protected WarningDoubleParameter distanceJBParam = new WarningDoubleParameter(DISTANCE_JB_NAME, DISTANCE_JB_DEFAULT);
   public final static String DISTANCE_JB_NAME = "DistanceJB (km)";
   public final static String DISTANCE_JB_UNITS = "km";
   public final static String DISTANCE_JB_INFO =
   "Joyner-Boore Distance (km)";
   protected final static Double DISTANCE_JB_MIN = new Double(0.0);
   protected final static Double DISTANCE_JB_MAX = new Double(200.0);
  
  // Name of IMR
  public final static String NAME = "Abrahamson & Silva (2008)";
  private final static String AS_2008_CoeffFile = "scratchJavaDevelopers/christine/as_2008_coeff.txt";
  
  // Local variables declaration
  double[] per,VLIN,b,a1,a2,a8,a10,a12,a13,a14,a15,a16,a18,s1e,s2e,s1m,s2m,s3,s4,rho;
  
  double c1 = 6.75;
  double c4 = 4.5;
  double a3 = 0.265;
  double a4 = -0.231;
  double a5 = -0.398;
  double N = 1.18;
  double c = 1.88;
  double c2 = 50.0;
  double sigmaamp=0.3;
    
  private HashMap indexFromPerHashMap;

  private int iper;
  private double vs30, rRup, f_rv, f_nm, hw, vsm, mag, depthTop, dip;
  private String stdDevType, component;
  private boolean parameterChange;
  
  private PropagationEffect propagationEffect;

  // values for warning parameters
  protected final static Double MAG_WARN_MIN = new Double(5.0);
  protected final static Double MAG_WARN_MAX = new Double(8.5);
  protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_RUP_WARN_MAX = new Double(200.0);
  protected final static Double DISTANCE_JB_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_JB_WARN_MAX = new Double(200.0);
  protected final static Double DISTANCE_X_WARN_MIN = new Double(0.0);
  protected final static Double DISTANCE_X_WARN_MAX = new Double(200.0);
  protected final static Double VS30_WARN_MIN = new Double(150.0);
  protected final static Double VS30_WARN_MAX = new Double(1500.0);
  protected final static Double DEPTH_1pt0_WARN_MIN = new Double(0);
  protected final static Double DEPTH_1pt0_WARN_MAX = new Double(10000);
  protected final static Double DIP_WARN_MIN = new Double(15);
  protected final static Double DIP_WARN_MAX = new Double(90);
  protected final static Double RUP_TOP_WARN_MIN = new Double(0);
  protected final static Double RUP_TOP_WARN_MAX = new Double(15);
  protected final static Double RUP_WIDTH_WARN_MIN = new Double(5);
  protected final static Double RUP_WIDTH_WARN_MAX = new Double(100);
    
  // style of faulting options
  public final static String FLT_TYPE_STRIKE_SLIP = "Strike-Slip";
  public final static String FLT_TYPE_REVERSE = "Reverse";
  public final static String FLT_TYPE_NORMAL = "Normal";
  public final static String FLT_TYPE_DEFAULT = FLT_TYPE_STRIKE_SLIP;

  // HWFlag of site (hangingwall side or footwall)
  protected StringParameter flagHWParam = new StringParameter(HW_FLAG_NAME);
  public final static String HW_FLAG_NAME = "Flag for site location.";
  public final static String HW_FLAG_INFO = 
	  "Select appropriate location of site relative to dip of fault.";
  public final static String HW_FLAG_HW = "Hanging Wall Side";
  public final static String HW_FLAG_FW = "Foot Wall Side";
  public final static String HW_FLAG_DEFAULT = HW_FLAG_HW;

  // VSFlag vs30 (measured or estimated)
  protected StringParameter flagVSParam = new StringParameter(VS_FLAG_NAME);
  public final static String VS_FLAG_NAME = "Flag for Vs30 value.";
  public final static String VS_FLAG_INFO = 
	  "Select how Vs30 was obtained.";
  public final static String VS_FLAG_M = "Measured";
  public final static String VS_FLAG_E = "Estimated";
  public final static String VS_FLAG_DEFAULT = VS_FLAG_E;
  
  
  // change component default from that of parent
  String COMPONENT_DEFAULT = this.COMPONENT_GMRotI50;

  /**
   * The DistanceRupParameter, closest distance to fault surface.
   */
  private DistanceRupParameter distanceRupParam = null;
  private final static Double DISTANCE_RUP_DEFAULT = new Double(0);
  private final static Double DISTANCE_JB_DEFAULT = new Double(DISTANCE_RUP_DEFAULT);
  private final static Double DISTANCE_X_DEFAULT = new Double(DISTANCE_RUP_DEFAULT);
  protected final static Double RUP_WIDTH_DEFAULT = new Double(10.0);

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
 
//per,VLIN,b,a1,a2,a8,a10,a12,a13,a14,a15,a16,a18,s1e,s2e,s1m,s2m,s3,s4,rho;

//    System.out.print("\nper"); for(int i=0; i<per.length;i++) System.out.print("\t"+per[i]);
//    System.out.print("\nVLIN"); for(int i=0; i<per.length;i++) System.out.print("\t"+VLIN[i]);
//    System.out.print("\nb"); for(int i=0; i<per.length;i++) System.out.print("\t"+b[i]);
//    System.out.print("\na1"); for(int i=0; i<per.length;i++) System.out.print("\t"+a1[i]);
//    System.out.print("\na2"); for(int i=0; i<per.length;i++) System.out.print("\t"+a2[i]);
//    System.out.print("\na8"); for(int i=0; i<per.length;i++) System.out.print("\t"+a8[i]);
//    System.out.print("\na10"); for(int i=0; i<per.length;i++) System.out.print("\t"+a10[i]);
//    System.out.print("\na12"); for(int i=0; i<per.length;i++) System.out.print("\t"+a12[i]);
//    System.out.print("\na13"); for(int i=0; i<per.length;i++) System.out.print("\t"+a13[i]);
//    System.out.print("\na14"); for(int i=0; i<per.length;i++) System.out.print("\t"+a14[i]);
//    System.out.print("\na15"); for(int i=0; i<per.length;i++) System.out.print("\t"+a15[i]);
//    System.out.print("\na16"); for(int i=0; i<per.length;i++) System.out.print("\t"+a16[i]);
//    System.out.print("\na18"); for(int i=0; i<per.length;i++) System.out.print("\t"+a18[i]);
//    System.out.print("\ns1e"); for(int i=0; i<per.length;i++) System.out.print("\t"+s1e[i]);
//    System.out.print("\ns2e"); for(int i=0; i<per.length;i++) System.out.print("\t"+s2e[i]);
//    System.out.print("\ns1m"); for(int i=0; i<per.length;i++) System.out.print("\t"+s1m[i]);
//    System.out.print("\ns2m"); for(int i=0; i<per.length;i++) System.out.print("\t"+s2m[i]);
//    System.out.print("\ns3"); for(int i=0; i<per.length;i++) System.out.print("\t"+s3[i]);
//    System.out.print("\ns4"); for(int i=0; i<per.length;i++) System.out.print("\t"+s4[i]);
//    System.out.print("\nrho"); for(int i=0; i<per.length;i++) System.out.print("\t"+rho[i]);
    
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
   *  passed in.  Warning constraints are ignored.
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
////    	System.out.println(propagationEffect.getParamValue(distanceRupParam.NAME));
//    	distanceRupParam.setValueIgnoreWarning(propagationEffect.getParamValue(distanceRupParam.NAME)); // this sets rRup too
//    	double dist_jb = ((Double)propagationEffect.getParamValue(DistanceJBParameter.NAME)).doubleValue();
//    	double dRupMinusJB_OverRup = (rRup-dist_jb)/rRup;
//    	distRupMinusJB_OverRupParam.setValueIgnoreWarning(dRupMinusJB_OverRup);
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
      iper = ( (Integer) indexFromPerHashMap.get(periodParam.getValue())).intValue();
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
	  
      double rX = (Double)distanceXParam.getValue();
	  double rJB = (Double)distanceJBParam.getValue();
	  double rupWidth = (Double)rupWidthParam.getValue();
	  double depthTo1pt0kmPerSec = (Double)depthTo1pt0kmPerSecParam.getValue();

	  // Returns the index of the period just below Td (Eq. 21)
	  int iTd= searchTdIndex(mag);
	  System.out.println("Inside getMean, mag= "+ mag+ " iTd= "+iTd);

	double pga_rock = Math.exp(getMean(1,0, 1100, rRup, rJB, rX, f_rv, f_nm, mag, dip,
		  rupWidth, depthTop, depthTo1pt0kmPerSec,hw,  0, 0, 0));
	
	  System.out.println("Inside getMean, pga_rock= "+ pga_rock);
	
	double medSa1100BeforeTdMinus = Math.exp(getMean(iper,iTd, 1100, rRup, rJB, rX, f_rv, f_nm, mag, dip,
		  rupWidth, depthTop, depthTo1pt0kmPerSec,hw,  pga_rock, 0, 0));

	double medSa1100BeforeTdPlus = Math.exp(getMean(iper,iTd+1, 1100, rRup, rJB, rX, f_rv, f_nm, mag, dip,
			  rupWidth, depthTop, depthTo1pt0kmPerSec,hw,  pga_rock, 0, 0));
	  
	System.out.println("Inside getMean, MedSa1100BeforeTd _Minus "+ medSa1100BeforeTdMinus +"\t _Plus "+medSa1100BeforeTdPlus);

	double mean = getMean(iper,0, vs30, rRup, rJB, rX, f_rv, f_nm, mag, dip, rupWidth,
			  depthTop, depthTo1pt0kmPerSec, hw, pga_rock, medSa1100BeforeTdMinus, medSa1100BeforeTdPlus);
	  
//System.out.println("Line 590, pga_r "+ pga_rock +"\t mean "+mean+"\t iper " +iper+"\t vs30 "+vs30+"\t rRup "+rRup+"\t rJB "+rJB+"\t frv "+f_rv+"\t fnm "+f_nm+"\t mag "+mag+"\t dip "+dip+"\t ztop "+depthTop+"\t z10 "+depthTo1pt0kmPerSec);
//System.out.println("Line 600, iTd " +iTd + " MedSa1100BeforeTd _Minus "+ medSa1100BeforeTdMinus +"\t _Plus "+medSa1100BeforeTdPlus);
return mean; 



//// TODO CG: This was in CB. Need to check if it applies to AS. If it is kept, remove the "return mean" from above. 
////	  make sure SA does not exceed PGA if per < 0.2 (page 11 of pre-print CB)
//	  if(iper < 3 || iper > 11 ) // not SA period between 0.02 and 0.15
//		  return mean;
//	  else {
//		  double pga_mean = getMean(1, vs30, rRup, rJB, rX, f_rv, f_nm, mag, dip, rupWidth,
//				  depthTop, depthTo1pt0kmPerSec, hw, pga_rock); // mean for PGA
//		  return Math.max(mean,pga_mean);
//	  }
  	}


  /**
   * @return    The stdDev value
   */
  public double getStdDev() {
	  if (intensityMeasureChanged) {
		  setCoeffIndex();  // intensityMeasureChanged is set to false in this method
	  }
	  
      double rX = (Double)distanceXParam.getValue();
	  double rJB = (Double)distanceJBParam.getValue();
	  double rupWidth = (Double)rupWidthParam.getValue();
	  double depthTo1pt0kmPerSec = (Double)depthTo1pt0kmPerSecParam.getValue();

//	  // set default value of basin depth based on the final value of vs30
//	  // (must do this here because we get pga_rock below by passing in 1100 m/s)
//	  if(Double.isNaN(depthTo1pt0kmPerSec)){
//		  if(vs30 <= 2500)
//			  depthTo1pt0kmPerSec = 2;
//		  else
//			  depthTo1pt0kmPerSec = 0;
//	  }

	  double pga_rock = Double.NaN;

	  pga_rock = Math.exp(getMean(1, 0, 1100, rRup, rJB, rX, f_rv, f_nm, mag,dip, rupWidth, depthTop, depthTo1pt0kmPerSec, hw, 0,0,0));
		 // System.out.println("Line 628, pga_rock"+"\t"+pga_rock);	  
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
    flagVSParam.setValue(VS_FLAG_DEFAULT);
    magParam.setValue(MAG_DEFAULT);
    fltTypeParam.setValue(FLT_TYPE_DEFAULT);
    flagHWParam.setValue(HW_FLAG_DEFAULT);
    rupTopDepthParam.setValue(RUP_TOP_DEFAULT);
    distanceRupParam.setValue(DISTANCE_RUP_DEFAULT);
    distanceJBParam.setValue(DISTANCE_JB_DEFAULT);
    saParam.setValue(SA_DEFAULT);
    periodParam.setValue(PERIOD_DEFAULT);
    dampingParam.setValue(DAMPING_DEFAULT);
    pgaParam.setValue(PGA_DEFAULT);
    pgvParam.setValue(PGV_DEFAULT);
    componentParam.setValue(COMPONENT_DEFAULT);
    stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);
    depthTo1pt0kmPerSecParam.setValue(DEPTH_1pt0_DEFAULT);
    dipParam.setValue(DIP_DEFAULT);
    rupWidthParam.setValue(RUP_WIDTH_DEFAULT);
    
    
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
    meanIndependentParams.addParameter(magParam);
    meanIndependentParams.addParameter(distanceRupParam);
    meanIndependentParams.addParameter(distanceJBParam);
    meanIndependentParams.addParameter(distanceXParam);
    meanIndependentParams.addParameter(fltTypeParam);
    meanIndependentParams.addParameter(flagHWParam);
    meanIndependentParams.addParameter(rupTopDepthParam);
    meanIndependentParams.addParameter(rupWidthParam);
    meanIndependentParams.addParameter(dipParam);
    meanIndependentParams.addParameter(vs30Param);
    meanIndependentParams.addParameter(flagVSParam);
    meanIndependentParams.addParameter(depthTo1pt0kmPerSecParam);
    meanIndependentParams.addParameter(componentParam);

    // params that the stdDev depends upon
    stdDevIndependentParams.clear();
    stdDevIndependentParams.addParameterList(meanIndependentParams);
    stdDevIndependentParams.addParameter(stdDevTypeParam);
//    meanIndependentParams.addParameter(flagVSParam);


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

    StringConstraint constraintHW = new StringConstraint();
    constraintHW.addString(HW_FLAG_HW);
    constraintHW.addString(HW_FLAG_FW);
    constraintHW.setNonEditable();
    flagHWParam = new StringParameter(HW_FLAG_NAME, constraintHW, null);
    flagHWParam.setInfo(HW_FLAG_INFO);
    flagHWParam.setNonEditable();

    StringConstraint constraintVS = new StringConstraint();
    constraintVS.addString(VS_FLAG_M);
    constraintVS.addString(VS_FLAG_E);
    constraintVS.setNonEditable();
    flagVSParam = new StringParameter(VS_FLAG_NAME, constraintVS, null);
    flagVSParam.setInfo(VS_FLAG_INFO);
    flagVSParam.setNonEditable();
    
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
    
    propagationEffectParams.addParameter(distanceRupParam);

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

//  // TODO check with Ned if this does what I want. goulet 20080715
//  public int searchTdIndex (double Td) {
// 		double[] TestTd = new double[21];
// 		int indexForTd = 23;
// 		for(int i=2;i<23;++i){
//		  TestTd[i] = Math.abs(Td-per[i]);
// 		}
// 		for(int i=2;i<22;++i){
//	  		  if(TestTd[i+1]<=TestTd[i]) {
//		  		   indexForTd = i;
//	  		  } else {
//	  			   indexForTd = i;
//	  		  }
// 		}
// 		return indexForTd;
//  	}
   
  
  /**
   * 
   * @param iper
   * @param vs30
   * @param rRup
   * @param rJB
   * @param rX
   * @param f_rv
   * @param f_nm
   * @param mag
   * @param depthTop
   * @param depthTo1pt0kmPerSec
   * @param pga_rock
   * @return
   */
  //@param rX
  
  //TODO check this
  public int searchTdIndex (double mag) {
		//double[] TestTd = new double[23];
		int iTd = 22;
		double Td=Math.pow(10,-1.25+0.3*mag );
		for(int i=2;i<22;++i){
		  if (Td>= per[i] && Td< per[i+1] ) {
			    iTd = i;
		  }
		}
		//System.out.println("Inside searchTdIndex \t"+iTd +"mag \t" +mag);
		return iTd;
	}


   
  public double getMean(int iper, int iTd, double vs30, double rRup, 
                            double rJB, double rX, double f_rv,
                            double f_nm, double mag, double dip, 
                            double rupWidth, double depthTop,
                            double depthTo1pt0kmPerSec, double hw,
                            double pga_rock, double medSa1100BeforeTdMinus, double medSa1100BeforeTdPlus) {

    double rR, v1, vs30Star, f1, f4, f5, f6, f8, f10, amp1100;
    
    if(iTd>0 && iTd <23){
    	iper=iTd;
 //   } else if(iTd>=23){
    }
//	System.out.println("iTd "+ iTd + "\t iper "+ iper );
    
    //"Base model": f1 term (Eq. 2), dependent on magnitude and distance
    rR=Math.sqrt(Math.pow(rRup,2)+Math.pow(c4,2)); // Eq. 3
    
    if(mag<=c1) {
    		f1 = a1[iper]+a4*(mag-c1)+a8[iper]*Math.pow(8.5-mag,2)+(a2[iper]+a3*(mag-c1))*Math.log(rR);
    } else {
   			f1 = a1[iper]+a5*(mag-c1)+a8[iper]*Math.pow(8.5-mag,2)+(a2[iper]+a3*(mag-c1))*Math.log(rR);
    }
    //"Site response model": f5_pga1100 (Eq. 5) term and required computation for v1 and vs30Star
    
    //Vs30 dependent term v1 (Eq. 6)
    if(per[iper]<=0.5 && per[iper]>-1.0) {
    	v1=1500;
    } else if(per[iper] > 0.5 && per[iper] <=1.0) {
    	v1=Math.exp(8.0-0.795*Math.log(per[iper]/0.21));
    } else if(per[iper] > 1.0 && per[iper] <2.0) {
    	v1=Math.exp(6.76-0.297*Math.log(per[iper]));
    } else if(per[iper]>=2.0) {
    	v1 = 700;
    } else { 
    	v1=862;
    }
    // amp1100 is an intermediate parameter. It is used in Norm's spreadsheet (nga_Sa_v19a.xls) 
    // and it simplifies the computations. it is called "site ampwrt VLIN for VS30=1100" in the spreadsheet.
    amp1100 = (a10[iper] +b[iper]*N)*Math.log(Math.min(v1,1100));
 
    //Vs30 dependent term vs30Star (Eq. 5)
    if(vs30<v1) {
    	vs30Star = vs30;
    } else {
    	vs30Star = v1;
    }
    //f5_pga1100 (Eq. 4)
    if (vs30<VLIN[iper]) {
    	f5 = a10[iper]*Math.log(vs30Star/VLIN[iper])-b[iper]*Math.log(pga_rock+c)+b[iper]*Math.log(pga_rock+c*Math.pow(vs30Star/VLIN[iper],N));
    } else {		
    	f5 = (a10[iper]+b[iper]*N)*Math.log(vs30Star / VLIN[iper]);
    }
    //"Hanging wall model": f4 (Eq. 7) term and required computation of T1, T2, T3, T4 and T5 (Eqs. 8-12)
    if(hw>0){
    double T1, T2, T3, T4, T5;
    
    if (rJB<30) {
    	T1=1-rJB/30;
    } else {
    	T1=0;
    }
    if (rX<=rupWidth*Math.cos(Math.toRadians(dip))) {
    	T2 = 0.5+(rX/2*rupWidth*Math.cos(Math.toRadians(dip)));
    } else {
    	T2 = 1;
    }	
    if (rX>=depthTop) {
    	T3 = 1;
    } else {
    	T3 = rX/depthTop;
    }
   if(mag<=6) {
	   T4 = 0;
   } else if(mag>=7) {
	   T4 = 1;
   } else {
	   T4 = mag - 6;
   }
   
   if(dip>=70) {
	   T5 = 1-(dip-70)/20;
   } else {
	   T5 = 1;
   }   
   // f4 term: CG 20080628 Note this should be optimized so that f4 is only computed for HW=1. 
   //Need to change the if structure a bit to do so, but it works now.
 	   f4 = a14[iper]*T1*T2*T3*T4*T5;
    } else {
	   f4=0;
   }
   // "Depth to top of rupture model": f6 term (eq. 13)
   if(depthTop<10) {
	   f6 = a16[iper]*depthTop/10;
   } else {
	   f6 = a16[iper];
   }
   // "Large distance model": f8 term (Eq. 14) and required T6 computation (Eq. 15)
   double T6;

   if(mag<5.5) {
	   T6 = 1;
   } else if(mag>6.5) {
	   T6 = 0.5;
   } else {
	   T6 = 0.5*(6.5-mag) +0.5;
   }
   
   if(rRup<100) {
	   f8 = 0;
   } else {
	   f8=a18[iper]*(rRup - 100)*T6;
   }
   
   // "Soil depth model": f10 term (eq. 16) and required z1Hat, a21, e2 and a22 computation (eqs. 17-20)
   double z1Hat, e2, a21test, a21, a22;
   
   // Eq. 17
   if(vs30<180) {
	   z1Hat = Math.exp(6.745);
   } else if(vs30>=180 && vs30<=500) {
	   z1Hat = Math.exp(6.745-1.35*Math.log(vs30/180));
   } else {
	   z1Hat = Math.exp(5.394-4.48*Math.log(vs30/500));
   }
   // Eq. 19
   if(per[iper]<0.35 || vs30>1000) {
	   e2=0;
   } else if(per[iper]>=0.35 && per[iper]<2) {
	   e2 = -0.25*Math.log(vs30/1000)*Math.log(per[iper]/0.35);
   } else {// if(per[iper]>=2)
		e2 = -0.25*Math.log(vs30/1000)*Math.log(2.0/0.35);
   }
   
   // Eq. 18
   a21test = (a10[iper] + b[iper]*N)*Math.log(vs30Star/Math.min(v1, 1000))+e2*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));

   if(vs30>=1000){
	   a21=0;
   } else if(a21test<0) {
	   a21=-(a10[iper] + b[iper]*N)*Math.log(vs30Star/Math.min(v1, 1000))/Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
   } else {
		a21 = e2;
   }
   
   // Eq. 20
   if(per[iper]<2){
	   a22 = 0;
   } else {
	   a22 = 0.0625*(per[iper]-2);
   }
   
   // Eq. 16
   if(depthTo1pt0kmPerSec>=200){
	   f10 = a21*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2)) + a22*Math.log(depthTo1pt0kmPerSec/200);
   } else {
	   f10 = a21*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
   }
   
//   double cgtest=Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
	  // "Constant displacement model" : Td (Eq. 21)
   		double Td=Math.pow(10,-1.25+0.3*mag);
//   		double cgMean;
   		if(iTd>0 && iTd<23){
   		
   		double medSa1100BeforeTd =  f1 + a12[iper]*f_rv +a13[iper]*f_nm  + f4 + f6 + amp1100 +f8;

		double medSa1100WithTd = medSa1100BeforeTd;

//		System.out.println("Line 1166, iTD " +iTd+" medSa1100BeforeTdMinus "+medSa1100BeforeTdMinus +" medSa1100BeforeTdPlus "+medSa1100BeforeTdPlus);
//		System.out.println("Line 1167, per[iTD] " +per[iTd]+" per[iTd+1] "+per[iTd+1]);

 		double medSa1100AtTd= Math.exp(Math.log(medSa1100BeforeTdPlus/medSa1100BeforeTdMinus)/Math.log(per[iTd])/per[iTd+1]*Math.log(Td/per[iTd])+Math.log(medSa1100BeforeTdMinus));
//		System.out.println("medSa1100BeforeTdMinus "+medSa1100BeforeTdMinus +" medSa1100BeforeTdPlus "+medSa1100BeforeTdPlus +" medSa100AtTd "+ medSa1100AtTd);
   		
		   // If iTd is between 1 and 22 and per[iper]>per[iTd], then getMean returns medSa1100BeforeTd (Minus or Plus)
 		if(per[iper]>=Td){
 				medSa1100WithTd = medSa1100AtTd*Math.pow(Td/per[iper],2);
// 	  			double cgMean = medSa1100WithTd*Math.exp(-amp1100 + f10 +f5); 
 	  			double cgMean = Math.log(medSa1100WithTd)-amp1100 + f10 +f5; 
 	  			System.out.println("cgMean Case 3, medSa1100WithTd, per[iTd]= "+per[iTd]+" per[iper]= "+per[iper]);
 			}
   		}
   		
   // "Compute Mean"  - which is actually the median! Eq. 1 and 22
			// TODO add flag for aftershock and term in equation below

		// The following logic is consisten with Norm's spreadsheet, nga_Sa_v19a.xls
   // If iTd=0 AND per[iper} <0.0001 AND (vs30=1100), then return pga_rock
   // Else, then return Savs30
		double cgMean;
		if(iTd<1 && per[iper]<0.001 && vs30>=1100 && vs30<=1100){
  			cgMean = f1 + a12[iper]*f_rv +a13[iper]*f_nm  + f4 + f6 + amp1100 +f8; 
  			System.out.println("cgMean Case 1,pga_rock, per[iTd]= "+per[iTd]+" per[iper]= "+per[iper]);
   		} else {
  			cgMean = f1 + a12[iper]*f_rv +a13[iper]*f_nm  +f5 + f4 + f6 + f8 + f10; 
  			System.out.println("cgMean Case 2,Sa, per[iTd]= "+per[iTd]+" per[iper]= "+per[iper]);
  		}
   			//System.out.println("pga_rock "+ pga_rock +"\t per "+ per[iper] +"\t f1 "+f1+"\t f4 "+f4+"\t f5 "+f5+"\t f6 "+f6+"\t f8 "+f8+"\t f10 "+f10+"\t a21 "+a21+"\t a22 "+a22+"\t z1hat "+z1Hat+"\t v1 "+v1);
   			
   		return cgMean;
   }

 /**
  * 
  * @param iper
  * @param stdDevType
  * @param component
  * @return
  */
  public double getStdDev(int iper, String stdDevType, String component, double vs30, double pga_rock) {

	  if (stdDevType.equals(STD_DEV_TYPE_NONE))
		  return 0.0;
	  else {
		  // Compute sigma0 (eq. 27), tau0 (eq. 28) and dterm (eq. 26) 
		  double  dterm, s1, s1PGA, s2, s2PGA,  sigma0, sigma0PGA, tau0, tau0PGA, sigmaB, sigmaBPGA, tauB, tauBPGA, sigma, tau;  
		  
		  // dterm (eq. 26)
		  if(vs30>=VLIN[iper])
			  dterm=0;
		  else
			  dterm=(-b[iper]*pga_rock/(pga_rock+c))+1/(pga_rock+c*Math.pow(vs30/VLIN[iper],N));

		  // Define appropriate s1 and s2 values depending on how Vs30 was obtained (measured or estimated)
		  if(vsm>0){
			  s1=s1m[iper];
			  s1PGA=s1m[2];
			  s2=s2m[iper];				
		  	  s2PGA=s2m[2];}
		  else {
			  s1=s1e[iper];
			  s1PGA=s1e[2];
			  s2=s2e[iper];
		      s2PGA=s2e[2];}
		  
		  // Compute sigma0 (eq. 27)
		  if(mag<5){
			  sigma0=s1;
		  	  sigma0PGA=s1PGA;}
		  else if(mag>7){
			  sigma0=s2;
			  sigma0PGA=s2PGA;}
		  else{
			  sigma0=s1+0.5*(s2-s1)*(mag-5);
			  sigma0PGA=s1PGA+0.5*(s2PGA-s1PGA)*(mag-5);}
		  
		  // Compute tau0 (eq. 28)
		  if(mag<5){
			  tau0=s3[iper];
			  tau0PGA=s3[2];}
		  else if(mag>7){
			  tau0=s4[iper];
			  tau0PGA=s4[2];}
		  else{
			  tau0=s3[iper]+0.5*(s4[iper]-s3[iper])*(mag-5);
			  tau0PGA=s3[2]+0.5*(s4[2]-s3[2])*(mag-5);}

		  // Compute sigmaB  (eq. 23)
		      sigmaB=Math.sqrt(Math.pow(sigma0,2)-Math.pow(sigmaamp,2));
			  sigmaBPGA=Math.sqrt(Math.pow(sigma0PGA,2)-Math.pow(sigmaamp,2));

		  // Compute tauB
			  tauB=tau0;
			  tauBPGA=tau0PGA;

	      // compute intra-event sigma
			  sigma = Math.pow(sigma0,2)+Math.pow(sigmaamp,2)+Math.pow(dterm,2)*Math.pow(sigmaBPGA,2)+2*dterm*sigmaB*sigmaBPGA*rho[iper];

		  // get tau - inter-event term
			  tau = Math.pow(tau0,2)+Math.pow(dterm,2)*Math.pow(tauBPGA,2)+2*dterm*tauB*tauBPGA*rho[iper];

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
	  else if (pName.equals(VS30_NAME)) {
		  vs30 = ( (Double) val).doubleValue();
	  }
	  else if (pName.equals(DEPTH_1pt0_NAME)) {
		  double depthTo1pt0kmPerSec = (Double)depthTo1pt0kmPerSecParam.getValue();
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
	  else if (pName.equals(HW_FLAG_NAME)) {
		  String flagHW = (String)flagHWParam.getValue();
		  if (flagHW.equals(HW_FLAG_HW)) {
			  hw = 1 ;
		  }
		  else if (flagHW.equals(HW_FLAG_FW)) {
			  hw = 0;
		  }
	  }
	  else if (pName.equals(VS_FLAG_NAME)) {
		  String flagVS = (String)flagVSParam.getValue();
		  if (flagVS.equals(VS_FLAG_E)) {
			  vsm = 0 ;
		  }
		  else if (flagVS.equals(VS_FLAG_M)) {
			  vsm = 1;
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
    distanceJBParam.removeParameterChangeListener(this);
    distanceXParam.removeParameterChangeListener(this);
    vs30Param.removeParameterChangeListener(this);
    depthTo1pt0kmPerSecParam.removeParameterChangeListener(this);
    magParam.removeParameterChangeListener(this);
    fltTypeParam.removeParameterChangeListener(this);
    rupTopDepthParam.removeParameterChangeListener(this);
    rupWidthParam.removeParameterChangeListener(this);
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
    distanceXParam.addParameterChangeListener(this);
    vs30Param.addParameterChangeListener(this);
    depthTo1pt0kmPerSecParam.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    fltTypeParam.addParameterChangeListener(this);
    rupTopDepthParam.addParameterChangeListener(this);
    rupWidthParam.addParameterChangeListener(this);
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
   * CG: This comment was for CB 2008. Need to check with Ned what's the reason behind it:
   * This tests rJB numerical precision with respect to the f_hngR term.  Looks OK now.
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
