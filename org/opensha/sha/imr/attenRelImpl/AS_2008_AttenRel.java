package org.opensha.sha.imr.attenRelImpl;


import java.io.File;
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
 * <LI>vs30Param - Average shear wave velocity of top 30 m of soil/rock (m/s)
 * <LI>depthTo1pt0kmPerSecParam - Depth to shear wave velocity Vs=1.0km/s (m)
 * <LI>flagVSParam - how Vs30 was obtained (measured of estimated)
 * <LI>flagAS - flag for aftershocks
 * <LI>distanceRupParam - Closest distance to surface projection of fault (km)
 * <LI>distRupMinusJB_OverRupParam =  - used as a proxy for hanging wall effect;
 * <LI>distanceXParam - Horz dist to inf extension of top edge of rupture; neg values are on the foot wall;
 * <LI>componentParam - Component of shaking - Only GMRotI50 is supported
 * <LI>stdDevTypeParam - The type of standard deviation
 * </UL>
 * <p>
 * Verification : Not done as of June 28 2008
 * </p>
 *
 * @author     Christine Goulet & Ned Field
 * @created    2008
 * @version    2.0, Feb 2009
 */


public class AS_2008_AttenRel
extends AttenuationRelationship implements
AttenuationRelationshipAPI,
NamedObjectAPI, ParameterChangeListener {

	// Name of IMR
	public final static String NAME = "Abrahamson & Silva (2008) - Unverified!!";
	private final static String AS_2008_CoeffFile = "org" + File.separator + "opensha" + File.separator + "sha"
			+ File.separator + "imr" + File.separator + "attenRelImpl" + File.separator + "as_2008_coeff.txt";

	// Debugging stuff
	private final static String C = "AS_2008_CG_AttenRel";
	private final static boolean D = false;
	public final static String SHORT_NAME = "AS2008";
	private static final long serialVersionUID = 1234567890987654358L;

	// style of faulting param options
	public final static String FLT_TYPE_STRIKE_SLIP = "Strike-Slip";
	public final static String FLT_TYPE_REVERSE = "Reverse";
	public final static String FLT_TYPE_NORMAL = "Normal";
	public final static String FLT_TYPE_DEFAULT = FLT_TYPE_STRIKE_SLIP;

	/**
	 * Down-dip width of fault rupture param
	 */
	protected WarningDoubleParameter rupWidthParam;
	public final static String RUP_WIDTH_NAME = "Down-Dip Width";
	public final static String RUP_WIDTH_UNITS = "km";
	public final static String RUP_WIDTH_INFO = "Fault down-dip rupture width (km).";
	public final static Double RUP_WIDTH_MIN = new Double(0.1);
	public final static Double RUP_WIDTH_MAX = new Double(100.0);
	public final static Double RUP_WIDTH_DEFAULT = new Double(10.0);

	/**
	 * Depth 1.0 km/sec Parameter, reserved for representing the depth to where
	 * shear-wave velocity = 1.0 km/sec ("Z1.0 (m)" in PEER's 2008 NGA flat file);
	 */
	protected WarningDoubleParameter depthTo1pt0kmPerSecParam;
	public final static String DEPTH_1pt0_NAME = "Depth to Vs = 1.0 km/sec (m).";
	public final static String DEPTH_1pt0_UNITS = "m";
	public final static String DEPTH_1pt0_INFO = "The depth to where shear-wave velocity = 1.0 km/sec";
	public final static Double DEPTH_1pt0_DEFAULT = new Double("1000.0");
	protected final static Double DEPTH_1pt0_MIN = new Double(0.0);
	protected final static Double DEPTH_1pt0_MAX = new Double(30000.0);

	// VSFlag vs30 (measured or estimated)
	protected StringParameter flagVSParam;
	public final static String VS_FLAG_NAME = "Flag for Vs30 value.";
	public final static String VS_FLAG_INFO = 
		"Select how Vs30 was obtained.";
	public final static String VS_FLAG_M = "Measured";
	public final static String VS_FLAG_E = "Estimated";
	public final static String VS_FLAG_DEFAULT = VS_FLAG_E;

	// ASFlag (Atfershock or mainshock)
	protected StringParameter flagASParam;
	public final static String AS_FLAG_NAME = "Flag for aftershock.";
	public final static String AS_FLAG_INFO = 
		"Select aftershock for aftershock or mainshock for mainshocks, foreshock or swarms.";
	public final static String AS_FLAG_AS = "Aftershock";
	public final static String AS_FLAG_MS = "Mainshock";
	public final static String AS_FLAG_DEFAULT = AS_FLAG_MS;

	/**
	 * The DistanceRupParameter, closest distance to fault surface.
	 */
	private DistanceRupParameter distanceRupParam;
	private final static Double DISTANCE_RUP_DEFAULT = new Double(0.0);

	/**
	 * this is used to compute JB Distance value, used as a proxy for computing their
	 * hanging-wall term.
	 */
	private DistRupMinusJB_OverRupParameter distRupMinusJB_OverRupParam = null;
	public final static Double DISTANCE_RUP_MINUS_DEFAULT = new Double(0.0);
	
	/**
	 * Horizontal distance to top edge of fault rupture
	 */
	protected DistanceX_Parameter distanceXParam;
	private final static Double DISTANCE_X_DEFAULT = new Double(DISTANCE_RUP_DEFAULT);


	// change component default from that of parent
	String COMPONENT_DEFAULT = this.COMPONENT_GMRotI50;


	// primitive form of parameters
	private int iper;
	double mag, f_rv, f_nm, depthTop, rupWidth, dip, f_as;
	double vs30, vsm, depthTo1pt0kmPerSec;
	private double rRup, distRupMinusJB_OverRup, rX;
	private String component, stdDevType;

	private boolean parameterChange;

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

	private HashMap indexFromPerHashMap;

	private PropagationEffect propagationEffect;

	// values for warning parameters
	protected final static Double MAG_WARN_MIN = new Double(4.0);
	protected final static Double MAG_WARN_MAX = new Double(8.5);
	protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
	protected final static Double DISTANCE_RUP_WARN_MAX = new Double(200.0);
	protected final static Double DISTANCE_JB_WARN_MIN = new Double(0.0);
	protected final static Double DISTANCE_JB_WARN_MAX = new Double(200.0);
	protected final static Double DISTANCE_MINUS_WARN_MIN = new Double(0.0);
	protected final static Double DISTANCE_MINUS_WARN_MAX = new Double(50.0);
	protected final static Double DISTANCE_X_WARN_MIN = new Double(-300.0);
	protected final static Double DISTANCE_X_WARN_MAX = new Double(300.0);
	protected final static Double VS30_WARN_MIN = new Double(150.0);
	protected final static Double VS30_WARN_MAX = new Double(1500.0);
	protected final static Double DEPTH_1pt0_WARN_MIN = new Double(0.0);
	protected final static Double DEPTH_1pt0_WARN_MAX = new Double(10000);
	protected final static Double DIP_WARN_MIN = new Double(15.0);
	protected final static Double DIP_WARN_MAX = new Double(90.0);
	protected final static Double RUP_TOP_WARN_MIN = new Double(0.0);
	protected final static Double RUP_TOP_WARN_MAX = new Double(15.0);
	protected final static Double RUP_WIDTH_WARN_MIN = new Double(0.1);
	protected final static Double RUP_WIDTH_WARN_MAX = new Double(100.0);


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
		
		// do this to set the primitive types for each parameter;
		setParamDefaults();
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
		
		double rupWidth = surface.getSurfaceWidth();
		if(rupWidth>=1)
			rupWidthParam.setValue(surface.getSurfaceWidth());
		else
			rupWidthParam.setValue(1.0);

		dipParam.setValue(surface.getAveDip());

//		setFaultTypeFromRake(eqkRupture.getAveRake());
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
		flagVSParam.setValue(site.getParameter(VS_FLAG_NAME).getValue());

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
			
	    	distanceRupParam.setValueIgnoreWarning(propagationEffect.getParamValue(distanceRupParam.NAME)); // this sets rRup too
	    	double dist_jb = ((Double)propagationEffect.getParamValue(DistanceJBParameter.NAME)).doubleValue();
	    	distRupMinusJB_OverRupParam.setValueIgnoreWarning((rRup-dist_jb)/rRup);
	    	
			flagASParam.setValue(site.getParameter(AS_FLAG_NAME).getValue());
	    	this.distanceXParam.setValue(this.eqkRupture, this.site);
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
		// CG: Below used for validation for OUT files from Ken 
		// to differentiate MS (f_as=0) from AS (f_as=1)
		//		double f_as=1.0;
		
		// check if distance is beyond the user specified max
		if (rRup > USER_MAX_DISTANCE) {
			return VERY_SMALL_MEAN;
		}

		if (intensityMeasureChanged) {
			setCoeffIndex();  // intensityMeasureChanged is set to false in this method
		}

		double rJB = rRup - distRupMinusJB_OverRup*rRup;
		
		// CG: Below used for validation for OUT files from Ken 
		// Because rJB in OpenSHA is computed based on Rup and distRupMinusJB_OverRup
		// but Ken's files specify rJB directly. 
		// The following if statements take care of the two problems encountered
		//		if (rJB==9.0){
		//				rJB=10.0;
		//		}	else if (rJB==4.5){
		//				rJB=5.0;
		//		}

		// Returns the index of the period just below Td (Eq. 21)
		double Td=Math.pow(10,-1.25+0.3*mag );
		int iTd= searchTdIndex(Td);

		double pga_rock = Math.exp(getMean(1,0, 1100.0, rRup, rJB, f_as, rX, f_rv, f_nm, mag, dip,
				rupWidth, depthTop, depthTo1pt0kmPerSec,  0.0, 0.0, 0.0));


		double medSa1100WithTdMinus = Math.exp(getMean(iTd,0 , 1100.0, rRup, rJB, f_as, rX, f_rv, f_nm, mag, dip,
				rupWidth, depthTop, depthTo1pt0kmPerSec,  pga_rock, 0.0, 0.0));

		double medSa1100WithTdPlus = Math.exp(getMean(iTd+1,0 , 1100.0, rRup, rJB, f_as, rX, f_rv, f_nm, mag, dip,
				rupWidth, depthTop, depthTo1pt0kmPerSec,  pga_rock, 0.0, 0.0));
//System.out.println("From getMean, pga_rock = "+pga_rock+" Tdminus = "+per[iTd]+", meanSa1100TdMinus= "+ medSa1100WithTdMinus +", Tdplus = "+per[iTd+1]+", meanSa1100TdPlus= "+ medSa1100WithTdPlus);

		double f5 = getf5(iper, vs30, pga_rock);
//System.out.println("From getf5, f5 = "+f5);

		double f10 = getf10(iper, vs30, mag, depthTo1pt0kmPerSec);
//System.out.println("From getf10, f10 = "+f10);

		double mean = 0.0;
		if(per[iper]<Td || (Td>=10.0 && iTd==22)) {
		mean = (getMean(iper,0, vs30, rRup, rJB, f_as, rX, f_rv, f_nm, mag, dip, rupWidth,
				depthTop, depthTo1pt0kmPerSec, pga_rock,0, 0))+f10;
//System.out.println("From getMean, if(per<Td), mean = "+ Math.exp(mean));

		} else {
			double medSa1100AtTd0 = Math.exp(Math.log(medSa1100WithTdPlus/medSa1100WithTdMinus)/Math.log(per[iTd+1]/per[iTd])*Math.log(Math.pow(10,-1.25+0.3*mag)/per[iTd]) + Math.log(medSa1100WithTdMinus));
			double mean1100AtTd = (medSa1100AtTd0)*Math.pow(Math.pow(10,-1.25+0.3*mag)/per[iper],2);
			double f51100 = getf5(iper, 1100.0, pga_rock);
			f5 = getf5(iper, vs30, pga_rock);
//System.out.println("From getf5, f51100 = "+f51100+", f5="+f5);
			f10 = getf10(iper, vs30, mag, depthTo1pt0kmPerSec);
//System.out.println("Inside getMean, f10 = "+f10);

			mean = (Math.log(mean1100AtTd) -f51100+f5+f10);
//System.out.println("Inside getMean pga_rock=" +pga_rock+", mean1100atTd= " + mean1100AtTd + ", mean = "+mean);
		}

		return mean; 
	}


	/**
	 * @return    The stdDev value
	 */
	public double getStdDev() {
		// CG: Below used for validation for OUT files from Ken 
		// to differentiate MS (f_as=0) from AS (f_as=1)
		//		double f_as=1.0;
		
		if (intensityMeasureChanged) {
			setCoeffIndex();  // intensityMeasureChanged is set to false in this method
		}
		
		double rJB = rRup - distRupMinusJB_OverRup*rRup;

		// CG: Below used for validation for OUT files from Ken 
		// Because rJB in OpenSHA is computed based on Rup and distRupMinusJB_OverRup
		// but Ken's files specify rJB directly. 
		// The following if statements take care of the two problems encountered
		//		if (rJB==9.0){
		//				rJB=10.0;
		//		}	else if (rJB==4.5){
		//				rJB=5.0;
		//		}
		
		double pga_rock = Double.NaN;

		pga_rock = Math.exp(getMean(1,0, 1100.0, rRup, rJB, f_as, rX, f_rv, f_nm, mag, dip,
				rupWidth, depthTop, depthTo1pt0kmPerSec,  0.0, 0.0, 0.0));

		double stdDev = getStdDev(iper, stdDevType, component, vs30, pga_rock, vsm);

		return stdDev;
	}

	/**
	 * Allows the user to set the default parameter values for the selected Attenuation
	 * Relationship.
	 */
	public void setParamDefaults() {

		flagASParam.setValue(AS_FLAG_DEFAULT);
	
		magParam.setValue(MAG_DEFAULT);
		fltTypeParam.setValue(FLT_TYPE_DEFAULT);
		rupTopDepthParam.setValue(RUP_TOP_DEFAULT);
		rupWidthParam.setValue(RUP_WIDTH_DEFAULT);
		dipParam.setValue(DIP_DEFAULT);
		
		vs30Param.setValue(VS30_DEFAULT);
		depthTo1pt0kmPerSecParam.setValue(DEPTH_1pt0_DEFAULT);
		flagVSParam.setValue(VS_FLAG_DEFAULT);

		distanceRupParam.setValue(DISTANCE_RUP_DEFAULT);
	    distRupMinusJB_OverRupParam.setValue(DISTANCE_RUP_MINUS_DEFAULT);
		distanceXParam.setValue(DISTANCE_X_DEFAULT);

		saParam.setValue(SA_DEFAULT);	
		periodParam.setValue(PERIOD_DEFAULT);
		dampingParam.setValue(DAMPING_DEFAULT);
		pgaParam.setValue(PGA_DEFAULT);
		pgvParam.setValue(PGV_DEFAULT);
		componentParam.setValue(COMPONENT_DEFAULT);
		stdDevTypeParam.setValue(STD_DEV_TYPE_DEFAULT);
		
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
		meanIndependentParams.addParameter(flagASParam);
		meanIndependentParams.addParameter(magParam);
		meanIndependentParams.addParameter(fltTypeParam);
		meanIndependentParams.addParameter(rupTopDepthParam);
		meanIndependentParams.addParameter(rupWidthParam);
		meanIndependentParams.addParameter(dipParam);
		meanIndependentParams.addParameter(vs30Param);
		meanIndependentParams.addParameter(depthTo1pt0kmPerSecParam);
//		meanIndependentParams.addParameter(flagVSParam);
		meanIndependentParams.addParameter(distanceRupParam);
		meanIndependentParams.addParameter(distRupMinusJB_OverRupParam);
		meanIndependentParams.addParameter(distanceXParam);

		meanIndependentParams.addParameter(componentParam);

		// params that the stdDev depends upon
		stdDevIndependentParams.clear();
		stdDevIndependentParams.addParameterList(meanIndependentParams);
		stdDevIndependentParams.addParameter(stdDevTypeParam);
		meanIndependentParams.addParameter(flagVSParam);


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
		depthTo1pt0kmPerSecParam = new WarningDoubleParameter(DEPTH_1pt0_NAME, DEPTH_1pt0_DEFAULT);
		DoubleConstraint warn2 = new DoubleConstraint(DEPTH_1pt0_WARN_MIN,
				DEPTH_1pt0_WARN_MAX);
		warn2.setNonEditable();
		depthTo1pt0kmPerSecParam.setWarningConstraint(warn2);
		depthTo1pt0kmPerSecParam.addParameterChangeWarningListener(warningListener);
		depthTo1pt0kmPerSecParam.setNonEditable();

		StringConstraint constraintVS = new StringConstraint();
		constraintVS.addString(VS_FLAG_M);
		constraintVS.addString(VS_FLAG_E);
		constraintVS.setNonEditable();
		flagVSParam = new StringParameter(VS_FLAG_NAME, constraintVS, null);
		flagVSParam.setInfo(VS_FLAG_INFO);
		flagVSParam.setNonEditable();

		StringConstraint constraintAS = new StringConstraint();
		constraintAS.addString(AS_FLAG_AS);
		constraintAS.addString(AS_FLAG_MS);
		constraintAS.setNonEditable();
		flagASParam = new StringParameter(AS_FLAG_NAME, constraintAS, null);
		flagASParam.setInfo(AS_FLAG_INFO);
		flagASParam.setNonEditable();

		
		
		siteParams.clear();
		siteParams.addParameter(vs30Param);
		siteParams.addParameter(depthTo1pt0kmPerSecParam);
		siteParams.addParameter(flagVSParam);
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

		// now create the params unique to this relationship
		DoubleConstraint rupWidthConstraint = new DoubleConstraint(RUP_WIDTH_MIN, RUP_WIDTH_MAX);
		rupWidthConstraint.setNonEditable();
		rupWidthParam = new WarningDoubleParameter(RUP_WIDTH_NAME, rupWidthConstraint, RUP_WIDTH_DEFAULT);
		rupWidthParam.setInfo(RUP_WIDTH_INFO);
		DoubleConstraint warnRupWidthConstraint = new DoubleConstraint(RUP_WIDTH_WARN_MIN, RUP_WIDTH_WARN_MAX);
		warnRupWidthConstraint.setNonEditable();
		rupWidthParam.setWarningConstraint(warnRupWidthConstraint);
		

		eqkRuptureParams.clear();
		eqkRuptureParams.addParameter(magParam);
		eqkRuptureParams.addParameter(fltTypeParam);
		eqkRuptureParams.addParameter(dipParam);
		eqkRuptureParams.addParameter(rupTopDepthParam);
		eqkRuptureParams.addParameter(rupWidthParam);
	}

	/**
	 *  Creates the Propagation Effect parameters and adds them to the
	 *  propagationEffectParams list. Makes the parameters noneditable.
	 */
	protected void initPropagationEffectParams() {

		distanceRupParam = new DistanceRupParameter();
		DoubleConstraint warn = new DoubleConstraint(DISTANCE_RUP_WARN_MIN, DISTANCE_RUP_WARN_MAX);
		warn.setNonEditable();
		distanceRupParam.setWarningConstraint(warn);
		distanceRupParam.addParameterChangeWarningListener(warningListener);
		distanceRupParam.setNonEditable();

		//create distRupMinusJB_OverRupParam
		distRupMinusJB_OverRupParam = new DistRupMinusJB_OverRupParameter();
		DoubleConstraint warnJB = new DoubleConstraint(DISTANCE_MINUS_WARN_MIN, DISTANCE_MINUS_WARN_MAX);
		warnJB.setNonEditable();
		distRupMinusJB_OverRupParam.setWarningConstraint(warnJB);
		distRupMinusJB_OverRupParam.addParameterChangeWarningListener(warningListener);
		distRupMinusJB_OverRupParam.setNonEditable();
		
		
		distanceXParam = new DistanceX_Parameter();
		DoubleConstraint warnDX = new DoubleConstraint(DISTANCE_X_WARN_MIN, DISTANCE_X_WARN_MAX);
		warnDX.setNonEditable();
		distanceXParam.setWarningConstraint(warnDX);
		distanceXParam.addParameterChangeWarningListener(warningListener);
		distanceXParam.setNonEditable();

		propagationEffectParams.addParameter(distanceRupParam);
		propagationEffectParams.addParameter(distRupMinusJB_OverRupParam);
		propagationEffectParams.addParameter(distanceXParam);

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
				null);
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
				null);
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

	public int searchTdIndex (double Td) {
		//double[] TestTd = new double[23];
		int iTd = 22;
		for(int i=2;i<=22;++i){
			if (Td>= per[i] && Td< per[i+1] ) {
				iTd = i;
		}
		}
//		System.out.println("Inside searchTdIndex iTd = "+iTd +", Td = "+Td+", mag \t" +mag);
		
		return iTd;
	}

	public double getf5(int iper, double vs30, double pga_rock) {
		double vs30Star, v1, f5;
		//"Site response model": f5_pga1100 (Eq. 5) term and required computation for v1 and vs30Star
		//Vs30 dependent term v1 (Eq. 6)
		if(per[iper]==-1.0) {
			v1 = 862.0;
		} else if(per[iper]<=0.5 && per[iper]>-1.0) {
			v1=1500.0;
		} else if(per[iper] > 0.5 && per[iper] <=1.0) {
			v1=Math.exp(8.0-0.795*Math.log(per[iper]/0.21));
		} else if(per[iper] > 1.0 && per[iper] <2.0) {
			v1=Math.exp(6.76-0.297*Math.log(per[iper]));
		} else { 
			v1 = 700.0;
		}

		
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
	return f5;
	}
	
	public double getf10(int iper, double vs30, double mag, double depthTo1pt0kmPerSec) {
	
	// "Soil depth model": f10 term (eq. 16) and required z1Hat, a21, e2 and a22 computation (eqs. 17-20)
	double z1Hat, e2, a21test, a21, a22, f10;
	// Requires V1 and Vs30 star from f5
	double vs30Star, v1;
	//"Site response model": f5_pga1100 (Eq. 5) term and required computation for v1 and vs30Star
	//Vs30 dependent term v1 (Eq. 6)
	if(per[iper]==-1.0) {
		v1 = 862.0;
	} else if(per[iper]<=0.5 && per[iper]>-1.0) {
		v1=1500.0;
	} else if(per[iper] > 0.5 && per[iper] <=1.0) {
		v1=Math.exp(8.0-0.795*Math.log(per[iper]/0.21));
	} else if(per[iper] > 1.0 && per[iper] <2.0) {
		v1=Math.exp(6.76-0.297*Math.log(per[iper]));
	} else { 
		v1 = 700.0;
	}
//Vs30 dependent term vs30Star (Eq. 5)
	if(vs30<v1) {
		vs30Star = vs30;
	} else {
		vs30Star = v1;
	}
	
	// Eq. 17
	if(vs30<180.0) {
		z1Hat = Math.exp(6.745);
	} else if(vs30>=180.0 && vs30<=500.0) {
		z1Hat = Math.exp(6.745-1.35*Math.log(vs30/180.0));
	} else {
		z1Hat = Math.exp(5.394-4.48*Math.log(vs30/500.0));
	}
	
	// Eq. 19
	if((per[iper]<0.35 && per[iper]>-1.0) || vs30>1000.0) {
//	if(per[iper]<0.35 || vs30>1000.0) {
		e2=0.0;
	} else if(per[iper]>=0.35 && per[iper]<2.0) {
		e2 = -0.25*Math.log(vs30/1000)*Math.log(per[iper]/0.35);
	} else if(per[iper]==-1.0) {
		e2 = -0.25*Math.log(vs30/1000)*Math.log(1.0/0.35);
	} else {// if per[iper]>2.0
		e2 = -0.25*Math.log(vs30/1000)*Math.log(2.0/0.35);
	}
	

	// Eq. 18
//	if(per[iper]==-1.0) {
//	   a21test = (a10[16] + b[16]*N)*Math.log(vs30Star/Math.min(v1, 1000.0))+e2*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
//	} else {
	   a21test = (a10[iper] + b[iper]*N)*Math.log(vs30Star/Math.min(v1, 1000.0))+e2*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
//	}
		
	if(vs30>=1000.0){
		a21=0.0;
	} else if(a21test<0.0 && vs30<1000.0) {
		a21=-(a10[iper] + b[iper]*N)*Math.log(vs30Star/Math.min(v1, 1000.0))/Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
//	} else if(per[iper]==-1.0){
//		int tempiper=0;
//		a21=-(a10[tempiper] + b[tempiper]*N)*Math.log(vs30Star/Math.min(v1, 1000.0))/Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
//		System.out.println("Inside getf10, iper="+iper+" per[iper]="+per[iper]+" per[16]=" +per[16]+" a10="+a10[tempiper]+" b[iper]="+b[tempiper]+" N="+ N + " vs30Star="+ vs30Star +" v1="+v1+" c2="+c2);
	}	else {
		a21 = e2;
	}

	
	// Eq. 20
	if(per[iper]<2.0){
		a22 = 0.0;
	} else {
		a22 = 0.0625*(per[iper]-2.0);
	}

	// Eq. 16
	if(depthTo1pt0kmPerSec>=200){
		f10 = a21*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2)) + a22*Math.log(depthTo1pt0kmPerSec/200.0);
	} else {
		f10 = a21*Math.log((depthTo1pt0kmPerSec+c2)/(z1Hat+c2));
	}
	
//System.out.println("Inside getf10, iper="+iper+" per[iper]="+per[iper]+" per[16]=" +per[16]+" z1hat="+z1Hat+" a21test="+a21test+" a21 "+ a21 + " a22 "+ a22 +" e2="+e2+" f10 "+f10);
	
return f10;
}
	


	public double getMean(int iper, int iTd, double vs30, double rRup, 
			double rJB, double f_as, double rX, double f_rv,
			double f_nm, double mag, double dip, 
			double rupWidth, double depthTop,
			double depthTo1pt0kmPerSec,
			double pga_rock, double medSa1100WithTdMinus, double medSa1100WithTdPlus) {

		double rR, v1, vs30Star, f1, f4, f5, f6, f8;

		double hw = 0.0;
			   f4=0.0;

		if(rX>=0.0){
			hw = 1.0;
		}
		
		//"Base model": f1 term (Eq. 2), dependent on magnitude and distance
		rR=Math.sqrt(Math.pow(rRup,2)+Math.pow(c4,2)); // Eq. 3

		if(mag<=c1) {
			f1 = a1[iper]+a4*(mag-c1)+a8[iper]*Math.pow(8.5-mag,2)+(a2[iper]+a3*(mag-c1))*Math.log(rR);
		} else {
			f1 = a1[iper]+a5*(mag-c1)+a8[iper]*Math.pow(8.5-mag,2)+(a2[iper]+a3*(mag-c1))*Math.log(rR);
		}
//		System.out.println("a1= "+ a1[iper] +" a4= "+a4 +" mag= "+ mag +" c1="+c1+" a8= "+a8[iper]+" a2="+a2[iper]+" a3="+a3+" Rrup="+rRup+" R="+rR );

		//"Site response model": f5_pga1100 (Eq. 5) term and required computation for v1 and vs30Star
		//Vs30 dependent term v1 (Eq. 6)
		if(per[iper]==-1.0) {
			v1 = 862.0;
		} else if(per[iper]<=0.5 && per[iper]>-1.0) {
			v1=1500.0;
		} else if(per[iper] > 0.5 && per[iper] <=1.0) {
			v1=Math.exp(8.0-0.795*Math.log(per[iper]/0.21));
		} else if(per[iper] > 1.0 && per[iper] <2.0) {
			v1=Math.exp(6.76-0.297*Math.log(per[iper]));
		} else { 
			v1 = 700.0;
		}
		
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

//System.out.println("Inside Eqn getMean, per="+per[iper]+" hw="+hw+" f5=" +f5+" v1=" +v1+" vs30Star=" +vs30Star);

		
		//"Hanging wall model": f4 (Eq. 7) term and required computation of T1, T2, T3, T4 and T5 (Eqs. 8-12)
		if(hw==1.0){
			double T1, T2, T3, T4, T5;

			//T1 (Eq. 8)
			if (rJB<30.0) {
				T1=1.0-rJB/30.0;
			} else {
				T1=0.0;
			}
			
			//T2 (Eq. 9) - rewritten 2009-01-29 to be consistent with ES paper
			double rXtest = rupWidth*Math.cos(Math.toRadians(dip));
		    if (rX>rXtest || dip==90.0) {
				T2 = 1.0;
			} else {
				T2 = 0.5 + rX / (2.0*rXtest);
			}

			//T3 (Eq. 10)
			if (rX>=depthTop) {
				T3 = 1.0;
			} else {
				T3 = rX/depthTop;
			}

			//T4 (Eq. 11)
			if(mag<=6.0) {
				T4 = 0.0;
			} else if(mag>=7.0) {
				T4 = 1.0;
			} else {
				T4 = mag - 6.0;
			}

			//T5 (Eq. 12)
			if(dip>=70.0) {
				T5 = 1.0-(dip-70.0)/20.0;
			} else {
				T5 = 1.0;
			}   
			f4 = a14[iper]*T1*T2*T3*T4*T5;
//			System.out.println("Inside Eqn getMean, f4=" +f4+" T1=" +T1+" T2=" +T2+" T3=" +T3+" T4=" +T4);
		} 
		
		
		// "Depth to top of rupture model": f6 term (eq. 13)
		if(depthTop<10.0) {
			f6 = a16[iper]*depthTop/10.0;
		} else {
			f6 = a16[iper];
		}
		// "Large distance model": f8 term (Eq. 14) and required T6 computation (Eq. 15)
		double T6;

		if(mag<5.5) {
			T6 = 1.0;
		} else if(mag>6.5) {
			T6 = 0.5;
		} else {
			T6 = 0.5*(6.5-mag) +0.5;
		}

		if(rRup<100) {
			f8 = 0.0;
		} else {
			f8=a18[iper]*(rRup - 100.0)*T6;
		}

//System.out.println("Inside Eqn getMean, per="+per[iper]+" rJB="+rJB+" hw="+hw+" f1=" +f1+" f4=" +f4+" f5=" +f5+" f6=" +f6+" f8=" +f8);

		double cgMean;
			cgMean = f1 + a12[iper]*f_rv +a13[iper]*f_nm +a15[iper]*f_as + f4 + f5 + f6 +f8; 

		return cgMean;
	}

	/**
	 * 
	 * @param iper
	 * @param stdDevType
	 * @param component
	 * @return
	 */
	public double getStdDev(int iper, String stdDevType, String component, double vs30, double pga_rock, double vsm) {

		if (stdDevType.equals(STD_DEV_TYPE_NONE))
			return 0.0;
		else {
			// Compute sigma0 (eq. 27), tau0 (eq. 28) and dterm (eq. 26) 
			//NOTE: I created variables with the PGA suffix because it's easier to read the equations below (CGoulet)
			double  v1, vs30Star, dterm, s1, s1PGA, s2, s2PGA,  sigma0, sigma0PGA, tau0, tau0PGA, sigmaB, sigmaBPGA, tauB, tauBPGA, sigma, tau;  

			//"Site response model": f5_pga1100 (Eq. 5) term and required computation for v1 and vs30Star
			//Vs30 dependent term v1 (Eq. 6)
			if(per[iper]==-1.0) {
				v1 = 862.0;
			} else if(per[iper]<=0.5 && per[iper]>-1.0) {
				v1=1500.0;
			} else if(per[iper] > 0.5 && per[iper] <=1.0) {
				v1=Math.exp(8.0-0.795*Math.log(per[iper]/0.21));
			} else if(per[iper] > 1.0 && per[iper] <2.0) {
				v1=Math.exp(6.76-0.297*Math.log(per[iper]));
			} else { 
				v1 = 700.0;
			}
			
			//Vs30 dependent term vs30Star (Eq. 5)
			if(vs30<v1) {
				vs30Star = vs30;
			} else {
				vs30Star = v1;
			}
//TODO
			// sugmaamp=0.3 for all periods as per page 81. below equation 23
			double sigmaamp=0.3;
			
			// dterm (eq. 26) 
			//** The published ES version has errors in this equation. Per Norm (2008-08-15, personal communication)
			//			1) the test against VLIN should be made with vs30, but the computation with vs30Star 
			//			2) The (-b*pga_rock) is to multiply both terms of the equations
			dterm=0.0;
			if(vs30<VLIN[iper]){
				dterm=b[iper]*pga_rock*(-1.0/(pga_rock+c)+1.0/(pga_rock+c*Math.pow(vs30Star/VLIN[iper],N)));
			}
	
			// Define appropriate s1 and s2 values depending on how Vs30 was obtained
			// (measured or estimated), using the vsm flag defined above which is input in the GUI
			if(vsm==1.0) {
				s1PGA=s1m[1];
				s2PGA=s2m[1];
				s1=s1m[iper];
				s2=s2m[iper];				
			} else  {
				s1PGA=s1e[1];
				s2PGA=s2e[1];
				s1=s1e[iper];
				s2=s2e[iper];
			}

			// Compute sigma0 (Eq. 27)
			if(mag<5.0){
				sigma0=s1;
				sigma0PGA=s1PGA;
			} else if(mag>7.0){
				sigma0=s2;
				sigma0PGA=s2PGA;
			} else {
				sigma0=s1+0.5*(s2-s1)*(mag-5.0);
				sigma0PGA=s1PGA+0.5*(s2PGA-s1PGA)*(mag-5.0);
			}

			// Compute sigmaB  (Eq. 23)
			sigmaB=Math.sqrt(Math.pow(sigma0,2)-Math.pow(sigmaamp,2));
			sigmaBPGA=Math.sqrt(Math.pow(sigma0PGA,2)-Math.pow(sigmaamp,2));
			
			// Compute tau0 (Eq. 28)
			if(mag<5.0){
				tau0=s3[iper];
				tau0PGA=s3[1];
			} else if(mag>7.0){
				tau0=s4[iper];
				tau0PGA=s4[1];
			} else {
				tau0=s3[iper]+0.5*(s4[iper]-s3[iper])*(mag-5.0);
				tau0PGA=s3[1]+0.5*(s4[1]-s3[1])*(mag-5.0);
			}

			// Compute tauB (In text p. 81)
			tauB=tau0;
			tauBPGA=tau0PGA;

			// compute intra-event sigma (Eq. 24) 
			//** The published ES version has errors in this equation. Per Norm (2008-08-15, personal communication):
			//   1) use sigmaB instead of sigma0 in the first term.
			sigma = Math.sqrt(Math.pow(sigmaB,2)+Math.pow(sigmaamp,2)+Math.pow(dterm,2)*Math.pow(sigmaBPGA,2)+2.0*dterm*sigmaB*sigmaBPGA*rho[iper]);
			
			// get tau - inter-event term (Eq. 25)
			tau = Math.sqrt(Math.pow(tau0,2)+Math.pow(dterm,2)*Math.pow(tauBPGA,2)+2.0*dterm*tauB*tauBPGA*rho[iper]);

//System.out.println("PGArock="+pga_rock+" vsm="+vsm+" dterm="+ dterm + " sigma="+sigma+" tau="+tau);
//System.out.println("test PGA index, a1 at index 1="+a1[1]+" at index 2="+a1[2]);
			// compute total sigma
			double sigma_total = Math.sqrt(tau*tau + sigma*sigma);

//			System.out.println("pga_rock="+ pga_rock +"\t t0="+tau0+"\t sB="+sigmaB+"\t sBPGA="+sigmaBPGA+"\t s="+sigma+"\t t="+tau+"\t s_tot="+sigma_total);

			// return appropriate value
			if (stdDevType.equals(STD_DEV_TYPE_TOTAL))
				return sigma_total;
			else if (stdDevType.equals(STD_DEV_TYPE_INTRA))
				return sigma;
			else if (stdDevType.equals(STD_DEV_TYPE_INTER))
				return tau;
			else
				return Double.NaN;   // just in case invalid stdDev given			  

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
		
//		System.out.println(pName+"\t"+val);
		
		if (pName.equals(MAG_NAME)) {
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
		else if (pName.equals(RUP_WIDTH_NAME)) {
			rupWidth = ( (Double) val).doubleValue();
		}
		else if (pName.equals(DIP_NAME)) {
			dip = ( (Double) val).doubleValue();
		}
		else if (pName.equals(VS30_NAME)) {
			vs30 = ( (Double) val).doubleValue();
		}
		else if (pName.equals(DEPTH_1pt0_NAME)) {
			depthTo1pt0kmPerSec = (Double)depthTo1pt0kmPerSecParam.getValue();
			if(val == null)
				depthTo1pt0kmPerSec = Double.NaN;  // can't set the default here because vs30 could still change
			else
				depthTo1pt0kmPerSec = ( (Double) val).doubleValue();
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
		else if (pName.equals(AS_FLAG_NAME)) {
			String flagAS = (String)flagASParam.getValue();
			if (flagAS.equals(AS_FLAG_AS)) {
				f_as=0.0 ;
			}
			else if (flagAS.equals(AS_FLAG_MS)) {
				f_as=1.0;
			}
		}

		else if (pName.equals(DistanceRupParameter.NAME)) {
			rRup = ( (Double) val).doubleValue();
		}
		else if (pName.equals(DistRupMinusJB_OverRupParameter.NAME)) {
			distRupMinusJB_OverRup = ( (Double) val).doubleValue();
		}
		else if (pName.equals(distanceXParam.getName())) {
			 rX= ( (Double) val).doubleValue();
		}

		else if (pName.equals(STD_DEV_TYPE_NAME)) {
			stdDevType = (String) val;
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
		
		magParam.removeParameterChangeListener(this);
		fltTypeParam.removeParameterChangeListener(this);
		rupTopDepthParam.removeParameterChangeListener(this);
		rupWidthParam.removeParameterChangeListener(this);
		dipParam.removeParameterChangeListener(this);
		vs30Param.removeParameterChangeListener(this);
		depthTo1pt0kmPerSecParam.removeParameterChangeListener(this);
		flagVSParam.removeParameterChangeListener(this);
		flagASParam.removeParameterChangeListener(this);
		distanceRupParam.removeParameterChangeListener(this);
		distRupMinusJB_OverRupParam.removeParameterChangeListener(this);
		distanceXParam.removeParameterChangeListener(this);
		stdDevTypeParam.removeParameterChangeListener(this);
		componentParam.removeParameterChangeListener(this);
		periodParam.removeParameterChangeListener(this);


		this.initParameterEventListeners();
	}

	/**
	 * Adds the parameter change listeners. This allows to listen to when-ever the
	 * parameter is changed.
	 */
	protected void initParameterEventListeners() {

		magParam.addParameterChangeListener(this);
		fltTypeParam.addParameterChangeListener(this);
		rupTopDepthParam.addParameterChangeListener(this);
		rupWidthParam.addParameterChangeListener(this);
		dipParam.addParameterChangeListener(this);
		
		vs30Param.addParameterChangeListener(this);
		depthTo1pt0kmPerSecParam.addParameterChangeListener(this);
		flagVSParam.addParameterChangeListener(this);
		flagASParam.addParameterChangeListener(this);

		distanceRupParam.addParameterChangeListener(this);
		distRupMinusJB_OverRupParam.addParameterChangeListener(this);
		distanceXParam.addParameterChangeListener(this);
		
		stdDevTypeParam.addParameterChangeListener(this);
		componentParam.addParameterChangeListener(this);
		periodParam.addParameterChangeListener(this);
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
		site.addParameter(attenRel.getParameter(attenRel.VS_FLAG_NAME));

		Location loc;
		for(double dist=-0.3; dist<=0.3; dist+=0.01) {
			loc = new Location(0,dist);
			site.setLocation(loc);
			attenRel.setSite(site);
//			System.out.print((float)dist+"\t");
			attenRel.getMean();
		}

	}

}
