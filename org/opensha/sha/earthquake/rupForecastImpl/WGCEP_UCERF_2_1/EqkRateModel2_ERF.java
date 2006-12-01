/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.calc.FaultMomentCalc;
import org.opensha.calc.MomentMagCalc;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_A_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Somerville_2006_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.TimeSpan;
import org.opensha.data.ValueWeight;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.exceptions.FaultException;
import org.opensha.param.*;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_GR_EqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Point2Vert_SS_FaultPoisSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data.A_FaultsFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data.B_FaultsFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data.SegRateConstraint;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data.SegmentRecurIntv;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SingleMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.surface.FrankelGriddedSurface;
import org.opensha.sha.surface.StirlingGriddedSurface;
import org.opensha.util.FileUtils;

/**
 * @author vipingupta
 *
 */
public class EqkRateModel2_ERF extends EqkRupForecast {
	//for Debug purposes
	private static String  C = new String("EqkRateModel2_ERF");
	private boolean D = true;
	
	// name of this ERF
	public final static String NAME = new String("WGCEP Eqk Rate Model 2 ERF");
	
//	ArrayList allSourceNames;
	
	private String CHAR_MAG_FREQ_DIST = "1";
	private String GR_MAG_FREQ_DIST = "2";
	private String FAULTING_STYLE_SS = "1";
	private String FAULTING_STYLE_R = "2";
	private String FAULTING_STYLE_N = "3";
	
	public final static double MIN_MAG = 5;
	public final static double MAX_MAG = 9;
	public final static double DELTA_MAG = 0.1;
	public final static int NUM_MAG = (int)Math.round((MAX_MAG-MIN_MAG)/DELTA_MAG) + 1;
	
	// public final static double B_FAULT_GR_MAG_LOWER = 6.5;
	public final static double BACKGROUND_MAG_LOWER = 5.0;
	
	
	public final static double BACK_SEIS_DEPTH = 5.0;
	
	// this is the moment rate taken from A, B, and C sources and put into background seismicity
	private double moRateFracToBackground;
	
	// various summed MFDs
	private SummedMagFreqDist bFaultCharSummedMFD, bFaultGR_SummedMFD, aFaultSummedMFD, cZoneSummedMFD;
	private GutenbergRichterMagFreqDist totBackgroundMFD;
	
	/*
	 * Static variables for input files
	 */
	//private final static String IN_FILE_PATH = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/frankel02_inputfiles/";
	private final static String IN_FILE_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/";
	
	private ArrayList allSources;
	
	// fault-model parameter stuff
	// public final static String FAULT_MODEL_NAME = new String ("Fault Model");
	// public final static String FAULT_MODEL_FRANKEL = new String ("Frankel's");
	// public final static String FAULT_MODEL_STIRLING = new String ("Stirling's");
	// make the fault-model parameter
	// ArrayList faultModelNamesStrings = new ArrayList();
	// StringParameter faultModelParam;
	
	// background seismicity inlcude/exclude
	public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
	public final static String BACK_SEIS_INCLUDE = new String ("Include");
	public final static String BACK_SEIS_EXCLUDE = new String ("Exclude");
	public final static String BACK_SEIS_ONLY = new String ("Only Background");
	// make the fault-model parameter
	private ArrayList backSeisOptionsStrings = new ArrayList();
	private StringParameter backSeisParam;
	
	//	 background seismicity 
	public final static String BACK_SEIS_RUP_NAME = new String ("Treat Background Seismicity As");
	public final static String BACK_SEIS_RUP_POINT = new String ("Point Sources");
	public final static String BACK_SEIS_RUP_FINITE = new String ("Finite Sources");
	// make the fault-model parameter
	private ArrayList backSeisRupStrings = new ArrayList();
	private StringParameter backSeisRupParam;
	
	// 
	public final static String BACK_SEIS_MAG_NAME = "Backgroud Seis Mmax";
	public final static Double BACK_SEIS_MAG_MIN = new Double(5.0);
	public final static Double BACK_SEIS_MAG_MAX = new Double(9.0);
	public final static Double BACK_SEIS_MAG_DEFAULT = new Double(7.0);
	private final static String BACK_SEIS_MAG_INFO = "Maximum Magnitude for background seismicity";
	private DoubleParameter backSeisMaxMagParam ;
	
	// For rupture offset lenth along fault parameter
	public final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
	private Double DEFAULT_RUP_OFFSET_VAL= new Double(10);
	private final static String RUP_OFFSET_PARAM_UNITS = "km";
	private final static String RUP_OFFSET_PARAM_INFO = "Length of offset for floating ruptures";
	public final static double RUP_OFFSET_PARAM_MIN = 1;
	public final static double RUP_OFFSET_PARAM_MAX = 100;
	private DoubleParameter rupOffset_Param;
	
	// rate for M>=5
	public final static String TOT_MAG_RATE_PARAM_NAME = "Total M�5 Rate";
	public final static Double TOT_MAG_RATE_MIN = new Double(2.0);
	public final static Double TOT_MAG_RATE_MAX = new Double(20.0);
	public final static Double TOT_MAG_RATE_DEFAULT = new Double(6.69);
	private final static String TOT_MAG_RATE_INFO = "Total rate of M�5 events in the RELM test region (e.g, 3.22 for no aftershocks, or 6.69 including aftershocks)";
	private DoubleParameter totalMagRateParam ;
	
	//choose mag area relationship
	private final static String MAG_AREA_RELS_PARAM_NAME = "Mag-Area Relationship";
	private final static String MAG_AREA_RELS_PARAM_INFO = "Mag-Area Relationship for computing mean mag from area or vice versa";
	private StringParameter magAreaRelParam;
	private ArrayList magAreaRelationships;
	
	// choose deformation model
	private final static String DEFORMATION_MODEL_PARAM_NAME = "Deformation Model";
	private final static String DEFORMATION_MODEL_PARAM_INFO = "D2.1 to D2.3 use Fault Model 2.1, and D2.4 to D2.6 use Fault Model 2.2";
	private StringParameter deformationModelsParam;
	private DeformationModelSummaryDB_DAO deformationModelSummaryDB_DAO = new DeformationModelSummaryDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList<DeformationModelSummary> deformationModelSummariesList;
	
	// aseismic factor interpolated
	public final static String ASEIS_INTER_PARAM_NAME = "Aseis Factor Reduces Area?";
	private final static String ASEIS_INTER_PARAM_INFO = "Otherwise it reduces slip rate";
	private BooleanParameter aseisFactorInterParam; 
	
	
	// constrain A-fault segment rates boolean
	public final static String CONSTRAIN_A_SEG_RATES_PARAM_NAME = "Constrain Segment Rates?";
	private final static String CONSTRAIN_A_SEG_RATES_PARAM_INFO = "Constrain A-fault segments rates (add equations to inversion)";
	private BooleanParameter constrainA_SegRatesParam; 
	
	// connect more B-Faults boolean
	public final static String CONNECT_B_FAULTS_PARAM_NAME = "Connect More B Faults?";
	private final static String CONNECT_B_FAULTS_PARAM_INFO = "Connect nearby B-Faults";
	private BooleanParameter connectMoreB_FaultsParam;

	//	 rupture model type
	public final static String RUP_MODEL_TYPE_NAME = "A-Fault Solution Type";
	public final static String RUP_MODEL_TYPE_INFO = "The type of solution to apply for all A-Fault Sources";
	public final static String UNSEGMENTED_A_FAULT_MODEL = "Unsegmented Model";
	private StringParameter rupModelParam;
	
	//	 A-fault slip-model type
	public final static String SLIP_MODEL_TYPE_NAME = "A-Fault Slip Model";
	public final static String SLIP_MODEL_TYPE_INFO = "Type of slip on a segment for each rupture on all A-Fault Sources";
	private StringParameter slipModelParam;
	
	// % char vs GR param
	private final static String CHAR_VS_GR_PARAM_NAME = "% Char vs GR";
	private final static Double CHAR_VS_GR_MIN = new Double(.0);
	private final static Double CHAR_VS_GR_MAX = new Double(100.0);
	private final static Double CHAR_VS_GR_DEFAULT = new Double(67.0);
	private final static String CHAR_VS_GR_INFO = "The % moment rate put into characteristic (vs GR) events on B-Faults (and A-Faults for un-segmented option)";
	private DoubleParameter percentCharVsGRParam; 
	
	// 
	private double[] totalRelativeGriddedRates;
	private EvenlyGriddedRELM_Region region = new EvenlyGriddedRELM_Region();
	
	
	// char mag sigma >=0 and <=1
	private final static String MAG_SIGMA_PARAM_NAME = "Mag Sigma";
	private final static Double MAG_SIGMA_MIN = new Double(0.0);
	private final static Double MAG_SIGMA_MAX = new Double(1.0);
	private final static Double MAG_SIGMA_DEFAULT = new Double(0.12);
	private final static String MAG_SIGMA_INFO = "Standard Deviation for characteristic MFD";
	private DoubleParameter magSigmaParam;
	
	// Char mag trunc level (units is number of sigmas) >=0 and <=6
	// Mag truncation level
	private final static String TRUNC_LEVEL_PARAM_NAME = "Truncation Level";
	private final static String TRUNC_LEVEL_PARAM_UNITS = "Number of sigmas";
	private final static Double TRUNC_LEVEL_MIN = new Double(0.0);
	private final static Double TRUNC_LEVEL_MAX = new Double(6.0);
	private final static Double TRUNC_LEVEL_DEFAULT = new Double(2.0);
	private final static String TRUNC_LEVEL_INFO = "This defines the last non-zero value on the characteristic MFD";
	private DoubleParameter truncLevelParam;
	
	public final static String B_FAULTS_B_VAL_PARAM_NAME = "B-Faults b-value";
	public final static String B_FAULTS_B_VAL_PARAM_INFO = "GR-distribution b-value to apply to B-Faults";
	public final static Double B_FAULT_GR_B_DEFAULT= new Double(0.8);
	public final static Double B_VAL_MIN = new Double(-1);
	public final static Double B_VAL_MAX = new Double(2);
	private DoubleParameter bFaultB_ValParam;
	
	public final static String REGION_B_VAL_PARAM_NAME = "Background Seis b-value";
	public final static String REGION_B_VAL_PARAM_INFO = "GR-distribution b-value to apply to the background seismicity";
	public final static Double REGIONAL_B_DEFAULT = new Double(0.9);
	// min and max same as for bFaultB_ValParam
	private DoubleParameter regionB_ValParam;
	
	// B-Fault Min Mag
	private final static String B_FAULTS_MIN_MAG = "B-Faults Min Mag";
	private final static Double B_FAULTS_MIN_MAG_DEFAULT = new Double(6.5);
	private final static String B_FAULTS_MIN_MAG_INFO = "Min Mag to apply to B-Faults GR-distribution";
	private DoubleParameter bFaultsMinMagParam;
	

	// whether to inlcude C-zones
	private final static String INCLUDE_C_ZONES  = "Include C Zones?";
	private final static Boolean INCLUDE_C_ZONES_DEFAULT = new Boolean(true);
	private BooleanParameter includeC_ZonesParam;
	
	// fraction to put into background
	public final static String A_AND_B_MO_RATE_REDUCTION_PARAM_NAME = "Fract MoRate to Background";
	public final static Double A_AND_B_MO_RATE_REDUCTION_MIN = new Double(0);
	public final static Double A_AND_B_MO_RATE_REDUCTION_MAX = new Double(1);
	public final static Double A_AND_B_MO_RATE_REDUCTION_DEFAULT = new Double(0.18);
	public final static String A_AND_B_MO_RATE_REDUCTION_INFO = "Fraction of Moment Rate to take from A & B Faults & C zones to put into background seismicity";
	private DoubleParameter moRateFracToBackgroundParam;
	
	// Mean Mag Correction
	private final static String MEAN_MAG_CORRECTION = "Mean Mag Correction";
	private final static Double MEAN_MAG_CORRECTION_MIN = new Double(-0.5);
	private final static Double MEAN_MAG_CORRECTION_MAX = new Double(0.5);
	private final static Double MEAN_MAG_CORRECTION_DEFAULT = new Double(0.0);
	private final static String MEAN_MAG_CORRECTION_INFO = "Increment added to mean mag as additional epistemic uncertainity";
	private DoubleParameter meanMagCorrectionParam;
	
	// set for background
	private final static String SET_FOR_BCK_PARAM_NAME = "Set for Background";
	private final static String SET_FOR_BCK_PARAM_FRAC_MO_RATE = "Frac Mo Rate From ABC sources";
	private final static String SET_FOR_BCK_PARAM_BCK_MAX_MAG = "Max Mag";
	private final static String SET_FOR_BCK_PARAM_INFO = "This sets background moment rate as fraction of A, B, and C sources, or sets the background max mag";
	private StringParameter setForBckParam;
	
	// A and B faults fetcher
	private A_FaultsFetcher aFaultsFetcher = new A_FaultsFetcher();
	private B_FaultsFetcher bFaultsFetcher  = new B_FaultsFetcher();
	private final static String B_CONNECT_MINIMAL = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/B_FaultConnectionsMinimum.txt";
	private final static String B_CONNECT_MODEL1 = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/B_FaultConnectionsF2.1.txt";
	private final static String B_CONNECT_MODEL2 = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/B_FaultConnectionsF2.2.txt";
	private final static String A_FAULT_SEGMENTS_MODEL1 = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/SegmentModelsF2.1.txt";
	private final static String A_FAULT_SEGMENTS_MODEL2 = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/SegmentModelsF2.2.txt";
	private ArrayList aFaultSources, bFaultSources;
	
	private B_FaultFixes bFaultFixes = new B_FaultFixes(); 
	
	/*
	 // fault file parameter for testing
	  public final static String FAULT_FILE_NAME = new String ("Fault File");
	  // make the fault-model parameter
	   ArrayList faultFileNamesStrings = new ArrayList();
	   StringParameter faultFileParam;
	   */
	
	/**
	 *
	 * No argument constructor
	 */
	public EqkRateModel2_ERF() {
		
		// create the timespan object with start time and duration in years
		timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
		timeSpan.addParameterChangeListener(this);
		timeSpan.setDuration(50);
		
		// create and add adj params to list
		initAdjParams();
		
		
		// add the change listener to parameters so that forecast can be updated
		// whenever any paramater changes
		//faultModelParam.addParameterChangeListener(this);
		rupOffset_Param.addParameterChangeListener(this);
		backSeisParam.addParameterChangeListener(this);
		backSeisRupParam.addParameterChangeListener(this);
		deformationModelsParam.addParameterChangeListener(this);
		connectMoreB_FaultsParam.addParameterChangeListener(this);
		setA_FaultFileName();
		setB_FaultConnectionsFileName();
	}
	
	
//	make the adjustable parameters & the list
	private void initAdjParams() {
		
		//faultModelNamesStrings.add(FAULT_MODEL_FRANKEL);
		// faultModelNamesStrings.add(FAULT_MODEL_STIRLING);
		//  faultModelParam = new StringParameter(FAULT_MODEL_NAME, faultModelNamesStrings,
		//     (String)faultModelNamesStrings.get(0));
		
		// background seismicity include/exclude  
		backSeisOptionsStrings.add(BACK_SEIS_EXCLUDE);
		backSeisOptionsStrings.add(BACK_SEIS_INCLUDE);
		backSeisOptionsStrings.add(BACK_SEIS_ONLY);
		backSeisParam = new StringParameter(BACK_SEIS_NAME, backSeisOptionsStrings,BACK_SEIS_INCLUDE);
		
		// backgroud treated as point sources/finite sources
		backSeisRupStrings.add(BACK_SEIS_RUP_POINT);
		backSeisRupStrings.add(BACK_SEIS_RUP_FINITE);
		backSeisRupParam = new StringParameter(BACK_SEIS_RUP_NAME, backSeisRupStrings,BACK_SEIS_RUP_POINT);
		
		// back seis Mmax
		backSeisMaxMagParam = new DoubleParameter(BACK_SEIS_MAG_NAME, BACK_SEIS_MAG_MIN, BACK_SEIS_MAG_MAX,
				BACK_SEIS_MAG_DEFAULT);
		backSeisMaxMagParam.setInfo(BACK_SEIS_MAG_INFO);
		
		// rup offset
		rupOffset_Param = new DoubleParameter(RUP_OFFSET_PARAM_NAME,RUP_OFFSET_PARAM_MIN,
				RUP_OFFSET_PARAM_MAX,RUP_OFFSET_PARAM_UNITS,DEFAULT_RUP_OFFSET_VAL);
		rupOffset_Param.setInfo(RUP_OFFSET_PARAM_INFO);
		
		// total M>-5 rate
		totalMagRateParam = new DoubleParameter(TOT_MAG_RATE_PARAM_NAME, TOT_MAG_RATE_MIN,
				TOT_MAG_RATE_MAX, TOT_MAG_RATE_DEFAULT);
		totalMagRateParam.setInfo(TOT_MAG_RATE_INFO);
		
		// % char vs GR param
		percentCharVsGRParam = new DoubleParameter(CHAR_VS_GR_PARAM_NAME, CHAR_VS_GR_MIN,
				CHAR_VS_GR_MAX, CHAR_VS_GR_DEFAULT);
		percentCharVsGRParam.setInfo(CHAR_VS_GR_INFO);
		
		// aseis factor param
		aseisFactorInterParam = new BooleanParameter(ASEIS_INTER_PARAM_NAME, new Boolean(true));
		aseisFactorInterParam.setInfo(ASEIS_INTER_PARAM_INFO);
		
		// constrainA_SegRatesParam
		constrainA_SegRatesParam = new BooleanParameter(CONSTRAIN_A_SEG_RATES_PARAM_NAME, new Boolean(true));
		constrainA_SegRatesParam.setInfo(CONSTRAIN_A_SEG_RATES_PARAM_INFO);
		
		// connect more B Faults
		connectMoreB_FaultsParam = new BooleanParameter(CONNECT_B_FAULTS_PARAM_NAME);
		connectMoreB_FaultsParam.setInfo(CONNECT_B_FAULTS_PARAM_INFO);
		
		//		 make objects of Mag Area Relationships
		magAreaRelationships = new ArrayList();
		magAreaRelationships.add(new Ellsworth_A_WG02_MagAreaRel() );
		magAreaRelationships.add(new Ellsworth_B_WG02_MagAreaRel());
		magAreaRelationships.add(new HanksBakun2002_MagAreaRel());
		magAreaRelationships.add(new Somerville_2006_MagAreaRel());
//		magAreaRelationships.add(new WC1994_MagAreaRelationship());
		// array List of Mag Area Rel names
		ArrayList magAreaNamesList = new ArrayList();
		for(int i=0; i<magAreaRelationships.size(); ++i)
			magAreaNamesList.add(((MagAreaRelationship)magAreaRelationships.get(i)).getName());
		magAreaRelParam = new StringParameter(MAG_AREA_RELS_PARAM_NAME, magAreaNamesList, (String)magAreaNamesList.get(1));
		magAreaRelParam.setInfo(MAG_AREA_RELS_PARAM_INFO);
		
		
		// deformation model param
		deformationModelSummariesList = this.deformationModelSummaryDB_DAO.getAllDeformationModels();
		// make a list of deformation model names
		ArrayList deformationModelNames = new ArrayList();
		for(int i=0; i<deformationModelSummariesList.size(); ++i) {
			deformationModelNames.add(((DeformationModelSummary)deformationModelSummariesList.get(i)).getDeformationModelName());
//			System.out.println(i+" "+
//			((DeformationModelSummary)deformationModelsList.get(i)).getDeformationModelName()+"  "+
//			((DeformationModelSummary)deformationModelsList.get(i)).getDeformationModelId());
		}
		deformationModelsParam = new StringParameter(DEFORMATION_MODEL_PARAM_NAME,deformationModelNames, (String)deformationModelNames.get(0) );
		deformationModelsParam.setInfo(DEFORMATION_MODEL_PARAM_INFO);
		
		
		// A-Fault model type
		ArrayList rupModels = new ArrayList();
		rupModels.add(A_FaultsFetcher.MIN_RATE_RUP_MODEL);
		rupModels.add(A_FaultsFetcher.MAX_RATE_RUP_MODEL);
		rupModels.add(A_FaultsFetcher.GEOL_INSIGHT_RUP_MODEL);
		rupModels.add(UNSEGMENTED_A_FAULT_MODEL);
		rupModelParam = new StringParameter(RUP_MODEL_TYPE_NAME, rupModels, A_FaultsFetcher.GEOL_INSIGHT_RUP_MODEL);
		rupModelParam.setInfo(RUP_MODEL_TYPE_INFO);
		
		// A-fault slip type
		slipModelParam = new StringParameter(SLIP_MODEL_TYPE_NAME, 
				A_FaultSegmentedSource.getSupportedSlipModels(), 
				A_FaultSegmentedSource.WG02_SLIP_MODEL);
		slipModelParam.setInfo(SLIP_MODEL_TYPE_INFO);
		
		// mag Sigma Param
		magSigmaParam = new DoubleParameter(MAG_SIGMA_PARAM_NAME, MAG_SIGMA_MIN, MAG_SIGMA_MAX,
				MAG_SIGMA_DEFAULT);
		magSigmaParam.setInfo(MAG_SIGMA_INFO);
		
		// trunc level
		truncLevelParam = new DoubleParameter(TRUNC_LEVEL_PARAM_NAME, TRUNC_LEVEL_MIN, TRUNC_LEVEL_MAX,
				TRUNC_LEVEL_PARAM_UNITS, TRUNC_LEVEL_DEFAULT);
		truncLevelParam.setInfo(TRUNC_LEVEL_INFO);
		
		bFaultB_ValParam = new DoubleParameter(B_FAULTS_B_VAL_PARAM_NAME, this.B_VAL_MIN, this.B_VAL_MAX, this.B_FAULT_GR_B_DEFAULT);
		bFaultB_ValParam.setInfo(B_FAULTS_B_VAL_PARAM_INFO);
		regionB_ValParam = new DoubleParameter(REGION_B_VAL_PARAM_NAME, this.B_VAL_MIN, this.B_VAL_MAX, this.REGIONAL_B_DEFAULT);
		regionB_ValParam.setInfo(REGION_B_VAL_PARAM_INFO);
		
		moRateFracToBackgroundParam = new DoubleParameter(A_AND_B_MO_RATE_REDUCTION_PARAM_NAME, 
				A_AND_B_MO_RATE_REDUCTION_MIN, A_AND_B_MO_RATE_REDUCTION_MAX, 
				A_AND_B_MO_RATE_REDUCTION_DEFAULT);
		moRateFracToBackgroundParam.setInfo(A_AND_B_MO_RATE_REDUCTION_INFO);
		
		
		// Mean Mag Correction
		meanMagCorrectionParam = new DoubleParameter(MEAN_MAG_CORRECTION, 
				MEAN_MAG_CORRECTION_MIN, MEAN_MAG_CORRECTION_MAX,
				MEAN_MAG_CORRECTION_DEFAULT);
		meanMagCorrectionParam.setInfo(MEAN_MAG_CORRECTION_INFO);
		
//		 B-Fault Min Mag
		bFaultsMinMagParam = new DoubleParameter(B_FAULTS_MIN_MAG, 5.0, 8.0, B_FAULTS_MIN_MAG_DEFAULT);
		bFaultsMinMagParam.setInfo(B_FAULTS_MIN_MAG_INFO);
		
		
		// whether to inlcude C-zones
		includeC_ZonesParam = new BooleanParameter(INCLUDE_C_ZONES, INCLUDE_C_ZONES_DEFAULT);
		
		// set for background
		ArrayList<String> options = new ArrayList<String>();
		options.add(SET_FOR_BCK_PARAM_FRAC_MO_RATE);
		options.add(SET_FOR_BCK_PARAM_BCK_MAX_MAG);
		setForBckParam = new StringParameter(SET_FOR_BCK_PARAM_NAME, options, SET_FOR_BCK_PARAM_BCK_MAX_MAG);
		setForBckParam.setInfo(SET_FOR_BCK_PARAM_INFO);
		setForBckParam.addParameterChangeListener(this);
			
		
		//	 add adjustable parameters to the list
//		adjustableParams.addParameter(faultModelParam);		not needed for now
//		adjustableParams.addParameter(rupOffset_Param);		not needed for now
		adjustableParams.addParameter(deformationModelsParam);
		adjustableParams.addParameter(aseisFactorInterParam);
		adjustableParams.addParameter(rupModelParam);
		adjustableParams.addParameter(slipModelParam);
		adjustableParams.addParameter(magAreaRelParam);
		adjustableParams.addParameter(magSigmaParam);
		adjustableParams.addParameter(truncLevelParam);
		adjustableParams.addParameter(constrainA_SegRatesParam);
		adjustableParams.addParameter(percentCharVsGRParam);
		adjustableParams.addParameter(bFaultB_ValParam);
		adjustableParams.addParameter(bFaultsMinMagParam);
		adjustableParams.addParameter(connectMoreB_FaultsParam);
//		adjustableParams.addParameter(backSeisParam);		not needed for now
		adjustableParams.addParameter(includeC_ZonesParam);
		adjustableParams.addParameter(meanMagCorrectionParam);
		adjustableParams.addParameter(totalMagRateParam);
		adjustableParams.addParameter(regionB_ValParam);
		adjustableParams.addParameter(setForBckParam);
		adjustableParams.addParameter(backSeisMaxMagParam);
		
		
		
	}
	
	
	/**
	 * Get the info on the selected deformation model
	 * @return
	 */
	private DeformationModelSummary getSelectedDeformationModelSummary() {
		String selectedDefModel  = (String) deformationModelsParam.getValue();
		for(int i=0; i<deformationModelSummariesList.size(); ++i) {
			DeformationModelSummary deformationModelSum = (DeformationModelSummary)deformationModelSummariesList.get(i);
			if(deformationModelSum.getDeformationModelName().equalsIgnoreCase(selectedDefModel)) {
				return deformationModelSum;
			}
		}
		return null;
	}
	
	/**
	 * Return list of objects of MagAreaRelationships which are supported by this model
	 * @return
	 */
	public ArrayList getMagAreaRelationships() {
		return this.magAreaRelationships;
	}
	
	
	/**
	 * Get the selected Mag Area relationship
	 * @return
	 */
	private MagAreaRelationship getMagAreaRelationship() {
		String magAreaRelName = (String)this.magAreaRelParam.getValue();
		// iterate over all Mag Area relationships to find the selected one
		for(int i=0; i<magAreaRelationships.size(); ++i) {
			MagAreaRelationship magAreaRel = (MagAreaRelationship)magAreaRelationships.get(i);
			if(magAreaRel.getName().equalsIgnoreCase(magAreaRelName))
				return magAreaRel;
		}
		
		return null;
	}
	
	
	
	/**
	 * This gets the total relative a-value rates for all locations inside the RELM test region
	 * (by total we mean including A, B, and C faults/zones).  Note that absolute rates in Karen's 
	 * files are not correct, so just compute relative rates such that the sum of all equals 1.0.
	 * This will be used for the background seismicity after rates from A, B, and C faults/zones
	 * are subtracted off.
	 */
	private void makeTotalRelativeGriddedRates() {
		
		double origTime = System.currentTimeMillis();
		
		// Read Karen's a-value data
		totalRelativeGriddedRates = new double[region.getNumGridLocs()];
		double totRateSum=0, rateSumInsideRegion = 0;
		String backSeisPath = "backSeisFiles/";
		try {
			// read the locations into a location list
			ArrayList latLines = FileUtils.loadFile(IN_FILE_PATH+backSeisPath+"latloc_v1.txt");
			ArrayList lonLines = FileUtils.loadFile(IN_FILE_PATH+backSeisPath+"lonloc_v1.txt");
			ArrayList backSeisRateLines = FileUtils.loadFile(IN_FILE_PATH+backSeisPath+"RatesRecommended_v1.txt");
			double lat, lon, rate;
			int index;
			//FileWriter fw = new FileWriter("KarenRates.txt");
			
			for(int i=0;i<latLines.size(); ++i) {
				String latLine = (String)latLines.get(i);
				String lonLine = (String)lonLines.get(i);
				StringTokenizer latTokenizer = new StringTokenizer(latLine);
				StringTokenizer lonTokenizer = new StringTokenizer(lonLine);
				String backSeisRateLine = (String)backSeisRateLines.get(i);
				StringTokenizer rateTokenizer = new StringTokenizer(backSeisRateLine);
				while(latTokenizer.hasMoreTokens()) {
					rate = Double.parseDouble(rateTokenizer.nextToken());
					totRateSum+=rate;
					// NOTE THAT ADDING 0.05 IN WHAT FOLLOWS IS APPOXIMATE: WE SHOULD REALLY 
					// AVE THE FOUR CORNER LOCS OR HAVE HER CHANGE THE BIN_CENTER LOCATIONS
					lat = Double.parseDouble(latTokenizer.nextToken()) + 0.05;
					lon = Double.parseDouble(lonTokenizer.nextToken()) + 0.05;
					// the following wrote the file out to check it visually; it looked good
					//fw.write((float)(lat-0.05)+"\t"+(float)(lon-0.05)+"\t"+rate+"\n");
					index = region.getNearestLocationIndex(new Location(lat,lon));
					if(index!=-1) {
						rateSumInsideRegion+=rate;
						//if(totalRelativeGriddedRates[index]!=0) System.out.println(lat+","+lon+","+index);
						totalRelativeGriddedRates[index]=rate;
						
					}
					
				}
			}
			//fw.close();
			
			double relativeSum = 0;
			// the following wrote the file out to check it visually; it looked good
			//FileWriter fw1 = new FileWriter("erfRates.txt");
			for(int i=0; i<totalRelativeGriddedRates.length; ++i) {
				//Location loc = this.region.getGridLocation(i);
				// the following wrote the file out to check it visually; it looked good
				//fw1.write((float)loc.getLatitude()+"\t"+(float)loc.getLongitude()+"\t"+totalRelativeGriddedRates[i]+"\n");
				totalRelativeGriddedRates[i]/=rateSumInsideRegion;
				relativeSum+=totalRelativeGriddedRates[i];
			}
			//fw1.close();
			
			
			if(D){
				System.out.println("makeGridSources(): time to read & sum rates (sec) = "+(System.currentTimeMillis()-origTime)/1e3);
				System.out.println(C+" makeGridSources(): Sum of all background rates="+totRateSum);
				System.out.println(C+" makeGridSources(): Sum of rates inside RELM region ="+rateSumInsideRegion);
				System.out.println(C+" makeGridSources(): Fractional rate inside RELM region ="+(float)rateSumInsideRegion/totRateSum);
				System.out.println(C+" makeGridSources(): relative sum in RELM region ="+relativeSum);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		/* String backSeisPath = "backSeisFiles/";
		 try {
		 // read the locations into a location list
		  backSeisLocList = new LocationList();
		  ArrayList latLines = FileUtils.loadFile(IN_FILE_PATH+backSeisPath+"latloc_v1.txt");
		  ArrayList lonLines = FileUtils.loadFile(IN_FILE_PATH+backSeisPath+"lonloc_v1.txt");
		  double lat, lon;
		  for(int i=0;i<latLines.size(); ++i) {
		  String latLine = (String)latLines.get(i);
		  String lonLine = (String)lonLines.get(i);
		  StringTokenizer latTokenizer = new StringTokenizer(latLine);
		  StringTokenizer lonTokenizer = new StringTokenizer(lonLine);
		  while(latTokenizer.hasMoreTokens()) {
		  lat = Double.parseDouble(latTokenizer.nextToken());
		  lon = Double.parseDouble(lonTokenizer.nextToken());
		  backSeisLocList.addLocation(new Location(lat,lon));
		  }
		  }
		  // free the memeory
		   latLines = null;
		   lonLines = null;
		   
		   if(D) {
		   System.out.println("makeGridSources(): time to read background seis loc file (sec) = "+(System.currentTimeMillis()-origTime)/1e3);
		   origTime = System.currentTimeMillis();
		   }
		   
		   // read the backgroud rates
		    backSeisRates = new double[backSeisLocList.size()];
		    ArrayList backSeisRateLines = FileUtils.loadFile(IN_FILE_PATH+backSeisPath+"RatesRecommended_v1.txt");
		    int index=0;
		    double ratesSum=0, rateSumInsideRELM = 0; // sum of all background rates
		    for(int i=0;i<backSeisRateLines.size(); ++i) {
		    String backSeisRateLine = (String)backSeisRateLines.get(i);
		    StringTokenizer rateTokenizer = new StringTokenizer(backSeisRateLine);
		    while(rateTokenizer.hasMoreTokens()) {
		    backSeisRates[index] = Double.parseDouble(rateTokenizer.nextToken());
		    ratesSum+=backSeisRates[index];
		    if(region.isLocationInside(backSeisLocList.getLocationAt(index)))
		    rateSumInsideRELM+=backSeisRates[index];
		    ++index;
		    }
		    }
		    
		    /* FileWriter fw = new FileWriter("backgroundRates.txt");
		     for(int i=0; i<backSeisRates.length; ++i) {
		     Location loc = this.backSeisLocList.getLocationAt(i);
		     fw.write(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+backSeisRates[i]+"\n");
		     }
		     fw.close();
		     
		     
		     if(D){
		     System.out.println("makeGridSources(): time to read & sum rates (sec) = "+(System.currentTimeMillis()-origTime)/1e3);
		     System.out.println(C+" makeGridSources(): Sum of all background rates="+ratesSum);
		     System.out.println(C+" makeGridSources(): Sum of rates inside RELM region ="+rateSumInsideRELM);
		     System.out.println(C+" makeGridSources(): Fractional rate inside RELM region ="+(float)rateSumInsideRELM/ratesSum);
		     }
		     
		     } catch(Exception e) {
		     e.printStackTrace();
		     }*/
		
	}
	
	
	/**
	 * This makes the sources for all faults:
	 */
	private void makeAllFaultSources() {
		
		
		
	}
	
	
	
	
	/**
	 * Returns the  ith earthquake source
	 *
	 * @param iSource : index of the source needed
	 */
	public ProbEqkSource getSource(int iSource) {
		
		return (ProbEqkSource) allSources.get(iSource);
	}
	
	/**
	 * Get the number of earthquake sources
	 *
	 * @return integer
	 */
	public int getNumSources(){
		return allSources.size();
	}
	
	/**
	 * Temproray test
	 *
	 */
	private void  makeBackgroundGridSources_TEST() {
		
		//MagAreaRelationship magAreaRel = this.getMagAreaRelationship();
		
		// get the total rate of M�5 events
		double rate = ((Double)this.totalMagRateParam.getValue()).doubleValue();
		double bValue = ((Double)this.regionB_ValParam.getValue()).doubleValue();
		double magMax = ((Double)meanMagCorrectionParam.getValue()).doubleValue();
		
		// now subtract the A, B, & C fault/zone rates
		//rate -= this.bFaultCharSummedMFD.getTotalIncrRate();
		//rate -= this.bFaultGR_SummedMFD.getTotalIncrRate();
		//	rate -= this.aFaultSummedMFD.getTotalIncrRate();
		double totMoRateBC = this.bFaultCharSummedMFD.getTotalMomentRate()+
				this.bFaultGR_SummedMFD.getTotalMomentRate()+this.cZoneSummedMFD.getTotalMomentRate();
		double totRateA = this.aFaultSummedMFD.getTotalIncrRate();
		double totBackRate = rate-totRateA;
		double totBackMoRate = totMoRateBC;
		totBackgroundMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		totBackgroundMFD.setAllButMagUpper(MIN_MAG, totBackMoRate, totBackRate, bValue, true);
	}

	
	private void  makeBackgroundGridSources() {
		
		//MagAreaRelationship magAreaRel = this.getMagAreaRelationship();
		
		// get the total rate of M�5 events
		double rate = ((Double)this.totalMagRateParam.getValue()).doubleValue();
		double bValue = ((Double)this.regionB_ValParam.getValue()).doubleValue();
		
		double totRateABC = this.aFaultSummedMFD.getTotalIncrRate()+this.bFaultCharSummedMFD.getTotalIncrRate()+
			this.bFaultGR_SummedMFD.getTotalIncrRate()+this.cZoneSummedMFD.getTotalIncrRate();
		System.out.println(this.aFaultSummedMFD.getTotalIncrRate()+","+this.bFaultCharSummedMFD.getTotalIncrRate()+
				","+this.bFaultGR_SummedMFD.getTotalIncrRate()+","+this.cZoneSummedMFD.getTotalIncrRate());
		double totBackRate = rate-totRateABC;
		
		totBackgroundMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);

		String backgroundTreatment = (String) setForBckParam.getValue();
		if(backgroundTreatment.equals(this.SET_FOR_BCK_PARAM_FRAC_MO_RATE)) {
			double totMoRateABC = this.aFaultSummedMFD.getTotalMomentRate()+this.bFaultCharSummedMFD.getTotalMomentRate()+
				this.bFaultGR_SummedMFD.getTotalMomentRate()+this.cZoneSummedMFD.getTotalMomentRate();
			double totBackMoRate = moRateFracToBackground*totMoRateABC/(1-moRateFracToBackground);
			System.out.println(moRateFracToBackground+","+totBackRate+","+bValue);
			if(moRateFracToBackground > 0)
				totBackgroundMFD.setAllButMagUpper(MIN_MAG, totBackMoRate, totBackRate, bValue, true);
		}
		else {
			double magMax = ((Double)backSeisMaxMagParam.getValue()).doubleValue(); 
			totBackgroundMFD.setAllButTotMoRate(MIN_MAG, magMax, totBackRate, bValue);
		}

		//
		
		// now subtract the A, B, & C fault/zone rates
		//rate -= this.bFaultCharSummedMFD.getTotalIncrRate();
		//rate -= this.bFaultGR_SummedMFD.getTotalIncrRate();
		//	rate -= this.aFaultSummedMFD.getTotalIncrRate();
/*
		totBackgroundMFD = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		IncrementalMagFreqDist cumTotBackgroundMFD = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		double backRate, targetRate, aRate, bRateGR, bRateChar, cRate, mag;
		for(int i=0; i<totBackgroundMFD.getNum(); i++) {
			mag = totBackgroundMFD.getX(i);
			if(mag <= magMax) {
				targetRate = targetRegionalMFD.getCumRate(mag);
				aRate = aFaultSummedMFD.getCumRate(mag);
				bRateGR = bFaultGR_SummedMFD.getCumRate(mag);
				bRateChar = bFaultCharSummedMFD.getCumRate(mag);
				cRate = cZoneSummedMFD.getCumRate(mag);
				backRate = targetRate - aRate - bRateGR - bRateChar - cRate;
				//if(mag==5) System.out.println(targetRate +" "+ aRate +" "+bRateGR +" "+bRateChar+" "+cRate+" "+backRate);
				if (backRate > 0) cumTotBackgroundMFD.set(mag,backRate);
			}
		}
		int i;
		for( i=0; i<totBackgroundMFD.getNum()-1; i++) {
			totBackgroundMFD.set(i, cumTotBackgroundMFD.getY(i) - cumTotBackgroundMFD.getY(i+1) );
		}
		totBackgroundMFD.set(i, cumTotBackgroundMFD.getY(i));
*/
	}
	
	private void makeC_ZoneSources() {
		this.cZoneSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		
		if(((Boolean)includeC_ZonesParam.getValue()).booleanValue()) {
 			
			String []names = { "Foothills Fault System", "Mohawk-Honey Lake Zone",
					"Northeastern California", "Western Nevada", "Eastern California Shear Zone N",
					"Eastern California Shear Zone S", "Imperial Valley", "San Gorgonio Knot"};
			
			double[] slipRates = { 0.05, 2.0, 4.0, 4.0, 5.0, 8.0, 14.0, 5.0}; // mm/yr
			double[] depthTop = { 0, 0, 0, 0, 0, 0, 0, 0}; // km
			double[] depthBottom = { 12, 15, 15, 15, 14, 15.5, 12.6, 18.3}; //km
			double[] strike = { 325, 335, 315, 315, 320, 320, 310, 290};
			double[] length = { 360, 88, 230, 245, 180, 88, 58, 100 }; // km
			double[] magLower = {6.0, 6.5, 6.5, 6.5, 6.5, 6.5, 6.5, 6.5}; 
			double[] magUpper = {7.0, 7.3, 7.3, 7.3, 7.3, 7.3, 7.3, 7.3};
			double bValue = 0.8;
			double moRate;
			for(int i=0; i<names.length; ++i) {
				moRate = FaultMomentCalc.getMoment((depthBottom[i]-depthTop[i])*length[i]*1e6, slipRates[i]/1000.0)*(1-moRateFracToBackground);
				GutenbergRichterMagFreqDist grMFD = new GutenbergRichterMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
				grMFD.setAllButTotCumRate(magLower[i], magUpper[i], moRate, bValue);
				cZoneSummedMFD.addIncrementalMagFreqDist(grMFD);
			}
		}
	}
	
	private void mkA_FaultSegmentedSources() {
		boolean constrainA_SegRates = ((Boolean)constrainA_SegRatesParam.getValue()).booleanValue();
		double magSigma  = ((Double) magSigmaParam.getValue()).doubleValue();
		double magTruncLevel = ((Double) truncLevelParam.getValue()).doubleValue();
		String rupModel = (String) rupModelParam.getValue();
		String slipModel = (String)slipModelParam.getValue();
		int deformationModelId =  getSelectedDeformationModelSummary().getDeformationModelId();
		boolean isAseisReducesArea = ((Boolean) aseisFactorInterParam.getValue()).booleanValue();
		double meanMagCorrection = ((Double)meanMagCorrectionParam.getValue()).doubleValue();
		// this gets a list of FaultSegmentData objects (one for each A fault)
		ArrayList aFaultSegmentData = aFaultsFetcher.getFaultSegmentDataList(deformationModelId, 
				isAseisReducesArea);
		aFaultSources = new ArrayList();
		aFaultSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		//System.out.println("************ Initial ******"+aFaultSummedMFD.toString());
		for(int i=0; i<aFaultSegmentData.size(); ++i) {
			FaultSegmentData segmentData = (FaultSegmentData) aFaultSegmentData.get(i);
			ValueWeight[] aPrioriRates = aFaultsFetcher.getAprioriRupRates(segmentData.getFaultName(), rupModel);
			A_FaultSegmentedSource aFaultSource = new A_FaultSegmentedSource(segmentData, 
					getMagAreaRelationship(), slipModel, aPrioriRates, magSigma, 
					magTruncLevel, moRateFracToBackground, meanMagCorrection, constrainA_SegRates);
			aFaultSources.add(aFaultSource);
			aFaultSummedMFD.addIncrementalMagFreqDist(aFaultSource.getTotalRupMFD());
			//System.out.println("************"+i+"******"+aFaultSummedMFD.toString());
		}
	}
	
	
	/**
	 * This is a quick fix.  We should really use our A_FaultFloatingSource since it has a lot of
	 * other capabilities.
	 *
	 */
	private void mkA_FaultUnsegmentedSources() {
		double magSigma  = ((Double) magSigmaParam.getValue()).doubleValue();
		double magTruncLevel = ((Double) truncLevelParam.getValue()).doubleValue();
		double fractCharVsGR= ((Double) percentCharVsGRParam.getValue()).doubleValue()/100.0;
		MagAreaRelationship magAreaRel = getMagAreaRelationship();
		int deformationModelId = getSelectedDeformationModelSummary().getDeformationModelId();
		boolean isAseisReducesArea = ((Boolean) aseisFactorInterParam.getValue()).booleanValue();
		double bValue = ((Double) bFaultB_ValParam.getValue()).doubleValue();
		double meanMagCorrection = ((Double)meanMagCorrectionParam.getValue()).doubleValue();
		double minMagGR = ((Double) bFaultsMinMagParam.getValue()).doubleValue();

		ArrayList aFaultSegmentData = aFaultsFetcher.getFaultSegmentDataList(deformationModelId, 
				isAseisReducesArea);
		aFaultSources = new ArrayList();
		aFaultSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		for(int i=0; i<aFaultSegmentData.size(); ++i) {
			FaultSegmentData segmentData = (FaultSegmentData) aFaultSegmentData.get(i);
			UnsegmentedSource source = new UnsegmentedSource( segmentData,  magAreaRel, 
					fractCharVsGR,  MIN_MAG, MAX_MAG, NUM_MAG, 
					magSigma, magTruncLevel, 
					minMagGR, bValue, moRateFracToBackground, Double.NaN, Double.NaN, meanMagCorrection);
			aFaultSources.add(source);
			aFaultSummedMFD.addIncrementalMagFreqDist(source.getMagFreqDist());   		
		}
	}
	
	private void mkB_FaultSources() {
		double magSigma  = ((Double)this.magSigmaParam.getValue()).doubleValue();
		double magTruncLevel = ((Double)this.truncLevelParam.getValue()).doubleValue();
		double fractCharVsGR= ((Double)this.percentCharVsGRParam.getValue()).doubleValue()/100.0;
		MagAreaRelationship magAreaRel = this.getMagAreaRelationship();
		int deformationModelId = this.getSelectedDeformationModelSummary().getDeformationModelId();
		boolean isAseisReducesArea = ((Boolean)this.aseisFactorInterParam.getValue()).booleanValue();
		double meanMagCorrection = ((Double)meanMagCorrectionParam.getValue()).doubleValue();
		double minMagGR = ((Double)this.bFaultsMinMagParam.getValue()).doubleValue();
		ArrayList bFaultSegmentData = this.bFaultsFetcher.getFaultSegmentDataList(deformationModelId, 
				isAseisReducesArea);
		double bValue = ((Double)this.bFaultB_ValParam.getValue()).doubleValue();
//		ArrayList B_faultCharMFDs = new ArrayList();
//		ArrayList B_faultGR_MFDs = new ArrayList();
		bFaultCharSummedMFD= new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		bFaultGR_SummedMFD= new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		bFaultSources = new ArrayList();
		double fixMag, fixRate;
		for(int i=0; i<bFaultSegmentData.size(); ++i) {
			FaultSegmentData segmentData = (FaultSegmentData)bFaultSegmentData.get(i);
			fixMag = bFaultFixes.getMag(segmentData.getFaultName());
			fixRate = bFaultFixes.getRate(segmentData.getFaultName());
			//if(!Double.isNaN(fixMag)) {
			//	System.out.println(segmentData.getFaultName()+","+fixMag+","+fixRate);
			//}
			UnsegmentedSource source = new UnsegmentedSource( segmentData,  magAreaRel, 
					fractCharVsGR,  MIN_MAG, MAX_MAG, NUM_MAG, 
					magSigma, magTruncLevel,minMagGR, 
					bValue, moRateFracToBackground, fixMag, fixRate, meanMagCorrection);
			bFaultSources.add(source);
			bFaultCharSummedMFD.addIncrementalMagFreqDist(source.getCharMagFreqDist());
			if(source.getGR_MagFreqDist() != null)  // will be null if char mag is lower than mag lower of GR
				bFaultGR_SummedMFD.addIncrementalMagFreqDist(source.getGR_MagFreqDist());
		}
	}
	
	
	/**
	 * Get a List of A_FaultSegmentedSorce objects. 
	 * Returns a list of  UnsegmentedFaultSource in case of unsegmented.
	 * 
	 * @return
	 */
	public ArrayList get_A_FaultSources() {
		return this.aFaultSources;
	}
	
	/**
	 * Get a List of b Fault sources (objects of UnsegmentedFaultSource)
	 * 
	 * @return
	 */
	public ArrayList get_B_FaultSources() {
		return this.bFaultSources;
	}
	
	
	
	/**
	 * Get the list of all earthquake sources.
	 *
	 * @return ArrayList of Prob Earthquake sources
	 */
	public ArrayList  getSourceList(){
		return allSources;
	}
	
	
	/**
	 * Return the name for this class
	 *
	 * @return : return the name for this class
	 */
	public String getName(){
		return NAME;
	}
	
	
	/**
	 * This returns an EvenlyDiscretizedFunc that is a cumulative 
	 * MFD for Karen Felzer's best-fit to the observed MFD (from Table 1 in her appendix)
	 * @return
	 */
	public EvenlyDiscretizedFunc getObsBestFitCumMFD(boolean includeAftershocks) {
		double rate;
		double bVal;
		// set b-value based on rate (guess whether aftershocks included)
		if(includeAftershocks)  {
			bVal = 1.0;
			rate = 6.69;
		}
		else {
			bVal = 0.8;
			rate = 3.22;
		}
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(this.MIN_MAG, this.NUM_MAG, this.DELTA_MAG,
				this.MIN_MAG, 8.0, 1.0, bVal);
		gr.scaleToCumRate(0,rate);
		EvenlyDiscretizedFunc func = gr.getCumRateDist();
		EvenlyDiscretizedFunc newFunc = new EvenlyDiscretizedFunc(MIN_MAG, 7.5, (int)Math.round((7.5-MIN_MAG)/DELTA_MAG)+1);
		for(int i=0; i<newFunc.getNum(); ++i) {
			newFunc.set(i, func.getY(i));
		}
		
		if(includeAftershocks)
			newFunc.setInfo("Cumulative MFD for Karen Felzer's best-fit to observed catalog including aftershocks (Rate(M�5)=7.1; b= 1.0)");
		else
			newFunc.setInfo("Cumulative MFD for Karen Felzer's best-fit to observed catalog excluding aftershocks (Rate(M�5)=3.1; b= 0.93)");
		
		return newFunc;
		/*EvenlyDiscretizedFunc obsBestFitCumMFD = new IncrementalMagFreqDist(this.MIN_MAG, this.NUM_MAG, this.DELTA_MAG);
		double[] incrRates = {5.68, 1.79, 0.57, 0.18, 0.06, 0.018};
		double sum=0;
		for(int i=5; i>=0; i--) {
			sum += incrRates[i];
			obsBestFitCumMFD.set(i, sum);
		}
		obsBestFitCumMFD.setInfo("Cumulative MFD for Karen Felzer's best-fit to observed catalog (from Table 1 in her appendix)");
		return obsBestFitCumMFD;*/
	}
	
	
	/**
	 * This returns an ArrayList of EvenlyDiscretizedFunc that have cumulative 
	 * MFD for Karen Felzer's observed MFD (from Table 6 in the doc she sent on Nov 2nd) and upper and lower confidence MFDs
	 * @return
	 */
	public ArrayList getObsCumMFD(boolean includeAftershocks) {
		EvenlyDiscretizedFunc obsCumMFD = new IncrementalMagFreqDist(5.0, 7.5, 6);
		EvenlyDiscretizedFunc obsCumLowMFD = new IncrementalMagFreqDist(5.0, 7.5, 6);
		EvenlyDiscretizedFunc obsCumHighMFD = new IncrementalMagFreqDist(5.0, 7.5, 6);
		double[] cumRatesWith = {6.69, 2.11, 0.66, 0.21, 0.06, 0.015};
		double[] cumRatesLowWith =  {6.69-2.7, 2.11-0.85, 0.66-0.27, 0.21-0.11, 0.06-0.045, 0.015-0.01};
		double[] cumRatesHighWith = {6.69+2.7, 2.11+0.85, 0.66+0.27, 0.21+0.11, 0.06+0.077, 0.015+0.026};
		double[] cumRates = {3.22, 1.27, 0.5, 0.19, 0.06, 0.015};
		double[] cumRatesLow =  {3.22-1,13, 1.27-0.45, 0.5-0.18, 0.19-0.1, 0.06-0.045, 0.015-0.01};
		double[] cumRatesHigh = {3.22+1,13, 1.27+0.45, 0.5+0.18, 0.19+0.1, 0.06+0.077, 0.015+0.026};
		
		if(includeAftershocks) {
			cumRates = cumRatesWith;
			cumRatesLow = cumRatesLowWith;
			cumRatesHigh = cumRatesHighWith;
		}
	
		for(int i=5; i>=0; i--) {
			obsCumMFD.set(i, cumRates[i]);
			obsCumLowMFD.set(i, cumRatesLow[i]);
			obsCumHighMFD.set(i, cumRatesHigh[i]);
		}
		if(includeAftershocks) {
			obsCumMFD.setInfo("Cumulative MFD for observed catalog including aftershocks (from Karen Felzer's Sept. 29, 2006 email)");
			obsCumLowMFD.setInfo("Lower 98% confidence of cumulative MFD for observed catalog including aftershocks (from Karen Felzer's Nov. 2, 2006 email)");
			obsCumHighMFD.setInfo("Upper 98% confidence of cumulative MFD for observed catalog including aftershocks (from Karen Felzer's Nov. 2, 2006 email)");
		}
		else {
			obsCumMFD.setInfo("Cumulative MFD for observed catalog excluding aftershocks (from Karen Felzer's Sept. 29, 2006 email)");
			obsCumLowMFD.setInfo("Lower 98% confidence of cumulative MFD for observed catalog excluding aftershocks (from Karen Felzer's Nov. 2, 2006 email)");
			obsCumHighMFD.setInfo("Upper 98% confidence of cumulative MFD for observed catalog excluding aftershocks (from Karen Felzer's Nov. 2, 2006 email)");
		}
		
		ArrayList obsCumList = new ArrayList();
		obsCumList.add(obsCumMFD);
		obsCumList.add(obsCumLowMFD);
		obsCumList.add(obsCumHighMFD);
		return obsCumList;
	}
	
	
	
	public IncrementalMagFreqDist getTotal_B_FaultsCharMFD() {
		return this.bFaultCharSummedMFD;
		//return new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
	}
	
	public IncrementalMagFreqDist getTotal_B_FaultsGR_MFD() {
		return this.bFaultGR_SummedMFD;
		//return new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
	} 
	
	public IncrementalMagFreqDist getTotal_A_FaultsMFD() {
		return this.aFaultSummedMFD;
	}
	
	public IncrementalMagFreqDist getTotal_BackgroundMFD() {
		return this.totBackgroundMFD;
		
	}
	
	public IncrementalMagFreqDist getTotal_C_ZoneMFD() {
		return this.cZoneSummedMFD;
		//return new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
	}
	
	public IncrementalMagFreqDist getTotalMFD() {
		SummedMagFreqDist totalMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		totalMFD.addIncrementalMagFreqDist(bFaultCharSummedMFD);
		totalMFD.addIncrementalMagFreqDist(bFaultGR_SummedMFD);
		totalMFD.addIncrementalMagFreqDist(aFaultSummedMFD);
		totalMFD.addIncrementalMagFreqDist(totBackgroundMFD);
		totalMFD.addIncrementalMagFreqDist(cZoneSummedMFD);
		return totalMFD;
	}
	
	
	/**
	 * update the forecast
	 **/
	
	public void updateForecast() {
		
		String backgroundTreatment = (String) setForBckParam.getValue();
		if(backgroundTreatment.equals(this.SET_FOR_BCK_PARAM_FRAC_MO_RATE))
			moRateFracToBackground = ((Double) moRateFracToBackgroundParam.getValue()).doubleValue();
		else
			moRateFracToBackground = 0;
		
		String rupModel = (String) rupModelParam.getValue();
		//System.out.println("Creating A Fault sources");
		long time1 = System.currentTimeMillis();
		if(rupModel.equalsIgnoreCase(UNSEGMENTED_A_FAULT_MODEL)) 
			mkA_FaultUnsegmentedSources();
		else 
			mkA_FaultSegmentedSources();
		long time2 = System.currentTimeMillis();
		//System.out.println("Creating B Fault sources. Time Spent in creating A Fault Sources="+(time2-time1)/1000+" sec");
		mkB_FaultSources();
		long time3 = System.currentTimeMillis();
		//System.out.println("Creating C Zone Fault sources. Time Spent in creating B Fault Sources="+(time3-time2)/1000+" sec");
//		Make C Zone MFD
		makeC_ZoneSources();
		long time4 = System.currentTimeMillis();
		//System.out.println("Creating Background sources. Time Spent in creating C Zone Sources="+(time4-time3)/1000+" sec");
		
		// makeTotalRelativeGriddedRates();
		makeBackgroundGridSources();
		long time5 = System.currentTimeMillis();
		//System.out.println("Done. Time Spent in creating background Sources="+(time5-time4)/1000+" sec");
	}
	
	
	/**
	 *  This is the main function of this interface. Any time a control
	 *  paramater or independent paramater is changed by the user in a GUI this
	 *  function is called, and a paramater change event is passed in.
	 *
	 *  This sets the flag to indicate that the sources need to be updated
	 *
	 * @param  event
	 */
	public void parameterChange(ParameterChangeEvent event) {
		super.parameterChange(event);
		String paramName = event.getParameterName();
		
		if(paramName.equalsIgnoreCase(SET_FOR_BCK_PARAM_NAME)) {
			String paramValue = (String)event.getNewValue();
			if(paramValue.equalsIgnoreCase(SET_FOR_BCK_PARAM_FRAC_MO_RATE)) { // if Fract Mo Rate needs to be set
				adjustableParams.removeParameter(backSeisMaxMagParam);
				adjustableParams.addParameter(this.moRateFracToBackgroundParam);
			} else if(paramValue.equalsIgnoreCase(SET_FOR_BCK_PARAM_BCK_MAX_MAG)){ // if Max Mag needs to be set
				adjustableParams.removeParameter(moRateFracToBackgroundParam);
				adjustableParams.addParameter(backSeisMaxMagParam);
			}
		} else if(paramName.equalsIgnoreCase(CONNECT_B_FAULTS_PARAM_NAME)) { // whether more B-Faults need to be connected
			setB_FaultConnectionsFileName();
		} else if(paramName.equalsIgnoreCase(DEFORMATION_MODEL_PARAM_NAME)) { // if deformation model changes, update the files to be read
			setA_FaultFileName();
			setB_FaultConnectionsFileName();	
		}
		
	}
	
	/**
	 * Set the B-Fault conenction filname based on whether B-Faults need to be connected and the chosen deformation model
	 *
	 */
	public void setB_FaultConnectionsFileName() {
		boolean isConnectMoreB_Faults = ((Boolean) connectMoreB_FaultsParam.getValue()).booleanValue();
		String fileName=null;
		if(!isConnectMoreB_Faults)  { // if we do not have to connect B-Fsults
			fileName = B_CONNECT_MINIMAL;
		} else { // if B-Faults need to be connected
			//find the deformation model
			DeformationModelSummary defModelSummary = getSelectedDeformationModelSummary();
			String faultModelName = defModelSummary.getFaultModel().getFaultModelName();
			// get the B-Fault filename based on selected fault model
			if(faultModelName.equalsIgnoreCase("F2.1")) fileName = B_CONNECT_MODEL1;
			else if((faultModelName.equalsIgnoreCase("F2.2"))) fileName = B_CONNECT_MODEL2;
			else throw new RuntimeException("Unsupported Fault Model");
		}
		bFaultsFetcher.setConnectionFileName(fileName, aFaultsFetcher);  // the aFaultsFetcher needed to find which are B faults
	}
	
	/**
	 * Set the A-Fault segments file name based on chosen deformation model
	 *
	 */
	public void setA_FaultFileName() {
//		find the deformation model
		String fileName=null;
		DeformationModelSummary defModelSummary = getSelectedDeformationModelSummary();
		String faultModelName = defModelSummary.getFaultModel().getFaultModelName();
		// get the A-Fault filename based on selected fault model
		if(faultModelName.equalsIgnoreCase("F2.1")) fileName = A_FAULT_SEGMENTS_MODEL1;
		else if((faultModelName.equalsIgnoreCase("F2.2"))) fileName = A_FAULT_SEGMENTS_MODEL2;
		else throw new RuntimeException("Unsupported Fault Model");
		this.aFaultsFetcher.setSegmentModelFileName(fileName);
	}
	
	/**
	 * This generates text to console that can be pasted into a Matlab *.m file and then
	 * executed in Matlab to compare the Java NNSL solutions to those in Matlab (script
	 * writes the max absolute difference).  Make sure the MATLAB_TEST in A_FaultSegmentedSource
	 * is set as true before running this.  Paste the results into a name.m file, put this where
	 * Matlab can find it, and then type "name" in Matlab.
	 * 
	 * Everything here compares well (except the case below).  That is, the max abs diff
	 * was generally less that 1e-10, got as low as 1e-7 on Elsinore, and 1e-5 on S. SAF.
	 * 
	 *  The following case did not converge in Matlab:
	 *
	 *  % Hanks & Bakun (2002) Mag-Area Rel.
	 *  % Min Rate Model
	 *  % WGCEP-2002 model (Dsr prop to Vs)
	 *  CASE_2_0_2
	 *  S._San_Andreas
	 *  
	 *  By dividing the right and left sides of the equation by 10, the Matlab solution 
	 *  converged with a max absolute-value difference from that here of 1.3243e-04
	 *  (the rates of 5 ruptures differed by 1e-4 when rounded to nearest 1e-4).
	 */
	private void makeMatlabNNLS_testScript() {
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		ArrayList rupModelOptions = ((StringConstraint)rupModelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		for(int imag=0; imag<magAreaOptions.size();imag++)
			for(int irup=0; irup<rupModelOptions.size();irup++)
				if(!((String)rupModelOptions.get(irup)).equals(UNSEGMENTED_A_FAULT_MODEL))
					for(int islip=0; islip<slipModelOptions.size();islip++) {
						magAreaRelParam.setValue(magAreaOptions.get(imag));
						rupModelParam.setValue(rupModelOptions.get(irup));
						slipModelParam.setValue(slipModelOptions.get(islip));
						System.out.println("% "+magAreaOptions.get(imag)+
								"\n% " + rupModelOptions.get(irup) +
								"\n% " + slipModelOptions.get(islip));
						System.out.println("display CASE_"+imag+"_"+irup+"_"+islip);
						mkA_FaultSegmentedSources();
					}
	}
	
	
	
	
	private void printMag6_5_discrepancies() {
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		ArrayList rupModelOptions = ((StringConstraint)rupModelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		double obVal = this.getObsBestFitCumMFD(true).getY(6.5);
		for(int imag=0; imag<magAreaOptions.size();imag++)
			for(int irup=0; irup<rupModelOptions.size();irup++)
					for(int islip=0; islip<slipModelOptions.size();islip++) {
						magAreaRelParam.setValue(magAreaOptions.get(imag));
						rupModelParam.setValue(rupModelOptions.get(irup));
						slipModelParam.setValue(slipModelOptions.get(islip));
						try {
							updateForecast();
						}catch(Exception e) {
							System.out.println(e.getMessage()+" , "+magAreaOptions.get(imag)+
									" , " + rupModelOptions.get(irup) +
									" , " + slipModelOptions.get(islip));
							continue;
						}
						// print out pred rate of M�6.5 and ratio with respect to obsBestFitCumMFD at same mag
						double predCumRate = getTotalMFD().getCumRate(6.5);
						
						System.out.println((float)(predCumRate/obVal)+" , "+(float)predCumRate+" , "+magAreaOptions.get(imag)+
								" , " + rupModelOptions.get(irup) +
								" , " + slipModelOptions.get(islip));
						//System.out.println("display CASE_"+imag+"_"+irup+"_"+islip);

					}
	}
	
	/**
	 * 
	 *
	 */
	private void findMinBulge() {
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		ArrayList rupModelOptions = ((StringConstraint)rupModelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		double obVal = this.getObsBestFitCumMFD(true).getY(6.5);
		double minRatio = 10, ratio;
		String str="", minStr="";
		try {
		FileWriter fw = new FileWriter("Bulge.txt");
		int imag=1;
		//for(int imag=0; imag<magAreaOptions.size();imag++)
			for(int irup=0; irup<rupModelOptions.size();irup++)
					for(int islip=0; islip<slipModelOptions.size();islip++) 
						for(double per=50.0; per<=80; per+=10) // % char vs GR
							for(double bVal1=0.8; bVal1<=1.2; bVal1+=0.1)  // b faults B val
								for(double bVal2=0.8; bVal2<=1.2; bVal2+=0.1) // bacgrd B val
									for(double frac=0.1; frac<0.3; frac+=0.05){ // moment rate reduction
										magAreaRelParam.setValue(magAreaOptions.get(imag));
										rupModelParam.setValue(rupModelOptions.get(irup));
										slipModelParam.setValue(slipModelOptions.get(islip));
										this.moRateFracToBackgroundParam.setValue(frac);
										this.bFaultB_ValParam.setValue(bVal1);
										this.percentCharVsGRParam.setValue(per);
										this.regionB_ValParam.setValue(bVal2);
										try {
											updateForecast();
										}catch(Exception e) {
											System.out.println(e.getMessage()+" , "+magAreaOptions.get(imag)+
													" , " + rupModelOptions.get(irup) +
													" , " + slipModelOptions.get(islip));
											continue;
										}
										// print out pred rate of M�6.5 and ratio with respect to obsBestFitCumMFD at same mag
										double predCumRate = getTotalMFD().getCumRate(6.5);
										ratio = (predCumRate/obVal);
										str = (float)(predCumRate/obVal)+" , "+(float)predCumRate+" , "+magAreaOptions.get(imag)+
											" , " + rupModelOptions.get(irup) +
											" , " + slipModelOptions.get(islip)+","+per+","+bVal1+","+
											bVal2+","+frac;
										System.out.println(str);
										fw.write(str+"\n");
										if(ratio<minRatio) {
											minRatio = ratio;
											minStr = str;
										}
										
					}
		fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println(minRatio+"------"+minStr);
	}
	
	
	
	/**
	 * Generate Excel sheet for each fault.
	 * Each sheet will have all Rup solution Types
	 * 
	 */
	public void generateExcelSheetForSegRecurIntv(String outputFileName) {
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		ArrayList rupModelOptions = ((StringConstraint)rupModelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		int numA_Faults = 8;	
//		 Create Excel Workbook and sheets if they do not exist already
		
		HSSFWorkbook wb  = new HSSFWorkbook();
		HSSFCellStyle cellStyle = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setColor(HSSFFont.COLOR_RED);
		cellStyle.setFont(font);
		
		// create sheets
		for(int i=0; i<numA_Faults; ++i) {
			wb.createSheet();
		}
		int currRow[] = new int[numA_Faults];
		for(int irup=0; irup<rupModelOptions.size();irup++) {
			int rupStartRow[] = new int[numA_Faults];
			for(int imag=0; imag<magAreaOptions.size();imag++) {
				if(!((String)rupModelOptions.get(irup)).equals(UNSEGMENTED_A_FAULT_MODEL))
					for(int islip=0; islip<slipModelOptions.size();islip++) {
			
						magAreaRelParam.setValue(magAreaOptions.get(imag));
						rupModelParam.setValue(rupModelOptions.get(irup));
						slipModelParam.setValue(slipModelOptions.get(islip));
						mkA_FaultSegmentedSources();
						
						// Write header for each Rup Solution Types
						if(imag==0 && islip==0) {
							// do for each fault
							for(int i=0; i<this.aFaultSources.size(); ++i) {
								 HSSFSheet sheet = wb.getSheetAt(i);
								 String sheetName = ((A_FaultSegmentedSource)aFaultSources.get(i)).getFaultSegmentData().getFaultName();
								 wb.setSheetName(i, sheetName);
								 //System.out.println(currRow[i]);
								 HSSFRow row = sheet.createRow((short)currRow[i]++);
								 // Write Rup solution Type
								 HSSFCell cell = row.createCell((short)0);
								 cell.setCellValue((String)rupModelOptions.get(irup));
								 cell.setCellStyle(cellStyle);
								 row = sheet.createRow((short)currRow[i]++);
								 int col=5;
								 
								 // Write All Mag Areas in appropriate columns
								 for(int j=0; j<magAreaOptions.size(); ++j, col+=(slipModelOptions.size()-1)) {
									 cell = row.createCell((short)col);
									 cell.setCellValue((String)magAreaOptions.get(j));
									 cell.setCellStyle(cellStyle);
								 }
								 // write the headers
								 row = sheet.createRow((short)currRow[i]++);
								 col=0;
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Segment_Name");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Mean Recur Intv");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Min Recur Intv");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Max Recur Intv");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Characteristic Model Recur Intv");
								 cell.setCellStyle(cellStyle);
								 for(int j=0; j<magAreaOptions.size(); ++j) {
									 for(int k=0; k<slipModelOptions.size(); ++k) {
										 String slipModel = (String)slipModelOptions.get(k);
										 if(!slipModel.equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) {
											 cell = row.createCell((short)col++);
											 cell.setCellValue(slipModel);
											 cell.setCellStyle(cellStyle);
										 }
									 }
								 }								 
								 // write Seg Names and mean Recur Intv
								 A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
								 rupStartRow[i] = currRow[i];
								 								 
								 for(int seg=0; seg<source.getFaultSegmentData().getNumSegments(); ++seg) {
									 row = sheet.createRow((short)currRow[i]++);
									 row.createCell((short)0).setCellValue(source.getFaultSegmentData().getSegmentName(seg));
									 ArrayList<SegRateConstraint> segRatesList = aFaultsFetcher.getSegRateConstraints(source.getFaultSegmentData().getFaultName(), seg);
									 if(segRatesList==null || segRatesList.size()==0) continue;
									 SegRateConstraint finalSegRateConstraint = SegRateConstraint.getWeightMean(segRatesList);
									 //System.out.println(seg+","+source.getFaultSegmentData().getSegmentName(seg));
									 row.createCell((short)1).setCellValue((int)Math.round(1/finalSegRateConstraint.getMean()));
									 row.createCell((short)2).setCellValue((int)Math.round(1/(finalSegRateConstraint.getMean()+2*finalSegRateConstraint.getStdDevToMean())));
									 row.createCell((short)3).setCellValue((int)Math.round(1/(finalSegRateConstraint.getMean()-2*finalSegRateConstraint.getStdDevToMean())));

									 //row.createCell((short)1).setCellValue(source.getS(rup));
								 }
							}
						}
						   
						   
						
						// write the rup Mag and rates
						for(int i=0; i<this.aFaultSources.size(); ++i) {
							 HSSFSheet sheet = wb.getSheetAt(i);
							 A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
							 int rateCol;
							 if(slipModelOptions.get(islip).equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) {
								 rateCol = 4;
							 } else rateCol = 4 + imag*(slipModelOptions.size()-1) + islip;
							 //rateCol = magCol + islip;
							 for(int seg=0; seg<source.getFaultSegmentData().getNumSegments(); ++seg) {
								 sheet.getRow(seg+rupStartRow[i]).createCell((short)rateCol).setCellValue((int)Math.round(source.getFinalSegRecurInt(seg)));
							 }
						}
					}
			}
			// 
			for(int i=0; i<wb.getNumberOfSheets(); ++i) {
				HSSFSheet sheet = wb.getSheetAt(i);
				sheet.createRow((short)currRow[i]++);
				sheet.createRow((short)currRow[i]++);
			}
			
		}
		try {
			FileOutputStream fileOut = new FileOutputStream(outputFileName);
			wb.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Generate Excel sheet for each fault.
	 * Each sheet will have all Rup solution Types
	 * 
	 */
	public void generateExcelSheetForSegSlipRate(String outputFileName) {
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		ArrayList rupModelOptions = ((StringConstraint)rupModelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		int numA_Faults = 8;	
//		 Create Excel Workbook and sheets if they do not exist already
		
		HSSFWorkbook wb  = new HSSFWorkbook();
		HSSFCellStyle cellStyle = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setColor(HSSFFont.COLOR_RED);
		cellStyle.setFont(font);
		
		// create sheets
		for(int i=0; i<numA_Faults; ++i) {
			wb.createSheet();
		}
		
		int currRow[] = new int[numA_Faults];
		for(int irup=0; irup<rupModelOptions.size();irup++) {
			int rupStartRow[] = new int[numA_Faults];
			for(int imag=0; imag<magAreaOptions.size();imag++) {
				if(!((String)rupModelOptions.get(irup)).equals(UNSEGMENTED_A_FAULT_MODEL))
					for(int islip=0; islip<slipModelOptions.size();islip++) {
			
						magAreaRelParam.setValue(magAreaOptions.get(imag));
						rupModelParam.setValue(rupModelOptions.get(irup));
						slipModelParam.setValue(slipModelOptions.get(islip));
						mkA_FaultSegmentedSources();
						
						// Write header for each Rup Solution Types
						if(imag==0 && islip==0) {
							// do for each fault
							for(int i=0; i<this.aFaultSources.size(); ++i) {
								 HSSFSheet sheet = wb.getSheetAt(i);
								 String sheetName = ((A_FaultSegmentedSource)aFaultSources.get(i)).getFaultSegmentData().getFaultName();
								 wb.setSheetName(i, sheetName);
								 //System.out.println(currRow[i]);
								 HSSFRow row = sheet.createRow((short)currRow[i]++);
								 // Write Rup solution Type
								 HSSFCell cell = row.createCell((short)0);
								 cell.setCellValue((String)rupModelOptions.get(irup));
								 cell.setCellStyle(cellStyle);
								 row = sheet.createRow((short)currRow[i]++);
								 int col=3;
								 
								 // Write All Mag Areas in appropriate columns
								 for(int j=0; j<magAreaOptions.size(); ++j, col+=(slipModelOptions.size()-1)) {
									 cell = row.createCell((short)col);
									 cell.setCellValue((String)magAreaOptions.get(j));
									 cell.setCellStyle(cellStyle);
								 }
								 // write the headers
								 row = sheet.createRow((short)currRow[i]++);
								 col=0;
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Segment_Name");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Original Slip Rate");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Characteristic Model Recur Intv");
								 cell.setCellStyle(cellStyle);
								 for(int j=0; j<magAreaOptions.size(); ++j) {
									 for(int k=0; k<slipModelOptions.size(); ++k) {
										 String slipModel = (String)slipModelOptions.get(k);
										 if(!slipModel.equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) {
											 cell = row.createCell((short)col++);
											 cell.setCellValue(slipModel);
											 cell.setCellStyle(cellStyle);
										 }
									 }
								 }								 
								 // write Seg Names and original Slip Rate
								 A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
								 rupStartRow[i] = currRow[i];
								 
								 for(int seg=0; seg<source.getFaultSegmentData().getNumSegments(); ++seg) {
									 row = sheet.createRow((short)currRow[i]++);
									 row.createCell((short)0).setCellValue(source.getFaultSegmentData().getSegmentName(seg));
									 //System.out.println(seg+","+source.getFaultSegmentData().getSegmentName(seg));
									 row.createCell((short)1).setCellValue(source.getFaultSegmentData().getSegmentSlipRate(seg)*1e3);
								 }
							}
						}
						   
						   
						
						// write the rup Mag and rates
						for(int i=0; i<this.aFaultSources.size(); ++i) {
							 HSSFSheet sheet = wb.getSheetAt(i);
							 A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
							 int rateCol;
							 if(slipModelOptions.get(islip).equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) {
								 rateCol = 2;
							 } else rateCol = 2 + imag*(slipModelOptions.size()-1) + islip;
							 //rateCol = magCol + islip;
							 for(int seg=0; seg<source.getFaultSegmentData().getNumSegments(); ++seg) {
								 sheet.getRow(seg+rupStartRow[i]).createCell((short)rateCol).setCellValue(source.getFinalSegSlipRate(seg)*1e3);
							 }
						}
					}
			}
			// 
			for(int i=0; i<wb.getNumberOfSheets(); ++i) {
				HSSFSheet sheet = wb.getSheetAt(i);
				sheet.createRow((short)currRow[i]++);
				sheet.createRow((short)currRow[i]++);
			}
			
		}
		try {
			FileOutputStream fileOut = new FileOutputStream(outputFileName);
			wb.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate Excel sheet for each fault.
	 * Each sheet will have all Rup solution Types
	 * 
	 */
	public void generateExcelSheetsForRupMagRates(String outputFileName) {
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		ArrayList rupModelOptions = ((StringConstraint)rupModelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		
		int numA_Faults = 8;	
//		 Create Excel Workbook and sheets if they do not exist already
		
		HSSFWorkbook wb  = new HSSFWorkbook();
		HSSFCellStyle cellStyle = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setColor(HSSFFont.COLOR_RED);
		cellStyle.setFont(font);
		//currRow = new int[aFaultSources.size()];
		// create sheets
		for(int i=0; i<numA_Faults; ++i) {
			wb.createSheet();
			//currRow[i]=0;
		}
		
		int currRow[] = new int[numA_Faults];
		for(int irup=0; irup<rupModelOptions.size();irup++) {
			int rupStartRow[] = new int[numA_Faults];
			for(int imag=0; imag<magAreaOptions.size();imag++) {
				//int numSlipModels = slipModelOptions.size();
				//double magRate[][] = new double[numSlipModels][2];
				if(!((String)rupModelOptions.get(irup)).equals(UNSEGMENTED_A_FAULT_MODEL))
					for(int islip=0; islip<slipModelOptions.size();islip++) {
			
						magAreaRelParam.setValue(magAreaOptions.get(imag));
						rupModelParam.setValue(rupModelOptions.get(irup));
						slipModelParam.setValue(slipModelOptions.get(islip));
						mkA_FaultSegmentedSources();
						
						// Write header for each Rup Solution Types
						if(imag==0 && islip==0) {
							// do for each fault
							for(int i=0; i<this.aFaultSources.size(); ++i) {
								 HSSFSheet sheet = wb.getSheetAt(i);
								 String sheetName = ((A_FaultSegmentedSource)aFaultSources.get(i)).getFaultSegmentData().getFaultName();
								 wb.setSheetName(i, sheetName);
								 //System.out.println(currRow[i]);
								 HSSFRow row = sheet.createRow((short)currRow[i]++);
								 // Write Rup solution Type
								 HSSFCell cell = row.createCell((short)0);
								 cell.setCellValue((String)rupModelOptions.get(irup));
								 cell.setCellStyle(cellStyle);
								 row = sheet.createRow((short)currRow[i]++);
								 int col=4;
								 
								 // Write All Mag Areas in appropriate columns
								 for(int j=0; j<magAreaOptions.size(); ++j, col+=slipModelOptions.size()) {
									 cell = row.createCell((short)col);
									 cell.setCellValue((String)magAreaOptions.get(j));
									 cell.setCellStyle(cellStyle);
								 }
								 // write the headers
								 row = sheet.createRow((short)currRow[i]++);
								 col=0;
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Rup_Name");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("A-Priori Rate");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Char Mag");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Char Rate");
								 cell.setCellStyle(cellStyle);
								 for(int j=0; j<magAreaOptions.size(); ++j) {
									 cell = row.createCell((short)col++);
									 cell.setCellValue("Mag");
									 cell.setCellStyle(cellStyle);
									 for(int k=0; k<slipModelOptions.size(); ++k) {
										 String slipModel = (String)slipModelOptions.get(k);
										 if(!slipModel.equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) {
											 cell = row.createCell((short)col++);
											 cell.setCellValue((String)slipModelOptions.get(k));
											 cell.setCellStyle(cellStyle);
										 }
									 }
								 }								 
								 // write Rup Names and Apriori Rates
								 A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
								 rupStartRow[i] = currRow[i];
								 for(int rup=0; rup<source.getNumRuptures(); ++rup) {
									 row = sheet.createRow((short)currRow[i]++);
									 row.createCell((short)0).setCellValue(source.getLongRupName(rup));
									 row.createCell((short)1).setCellValue(source.getAPrioriRupRate(rup));
								 }
								 // write totals
								 row = sheet.createRow((short)currRow[i]++);
								 row.createCell((short)0).setCellValue("Totals");
								 
								 // a priori rate total
								 cell = row.createCell((short)1);
								 String colStr="B";
								 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
								 cell.setCellFormula("SUM("+colStr+rupStartRow[i]+":"+colStr+(rupStartRow[i]+source.getNumRuptures()+")"));
								 
								 // Char rate total
								 cell = row.createCell((short)3);
								 colStr="D";
								 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
								 cell.setCellFormula("SUM("+colStr+rupStartRow[i]+":"+colStr+(rupStartRow[i]+source.getNumRuptures()+")"));

								 // totals for other rates
								 for(int k=0; k<slipModelOptions.size(); ++k) {
									 if(slipModelOptions.get(k).equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) continue;
									 for(int j=0; j<magAreaOptions.size(); ++j) {
										 int totCol = 4 + j*slipModelOptions.size()+k;
										 cell = row.createCell((short)totCol);
										 colStr=""+(char)('A'+totCol);
										 //System.out.println(colStr);
										 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
										 cell.setCellFormula("SUM("+colStr+rupStartRow[i]+":"+colStr+(rupStartRow[i]+source.getNumRuptures()+")"));

									 }
								 }
							}
						}
						   
						   
						
						// write the rup Mag and rates
						for(int i=0; i<this.aFaultSources.size(); ++i) {
							 HSSFSheet sheet = wb.getSheetAt(i);
							 A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
							 int magCol, rateCol;
							 if(slipModelOptions.get(islip).equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) {
								 magCol = 2; 
								 rateCol = 3;
							 } else {
								 magCol = 4 + imag*slipModelOptions.size();
								 rateCol = magCol + islip;
							 }
							 for(int rup=0; rup<source.getNumRuptures(); ++rup) {
								 sheet.getRow(rup+rupStartRow[i]).createCell((short)magCol).setCellValue(source.getRupMeanMag(rup));
								 sheet.getRow(rup+rupStartRow[i]).createCell((short)rateCol).setCellValue(source.getRupRate(rup));
							 }
						}
					}
			}
			// 
			for(int i=0; i<wb.getNumberOfSheets(); ++i) {
				HSSFSheet sheet = wb.getSheetAt(i);
				sheet.createRow((short)currRow[i]++);
				sheet.createRow((short)currRow[i]++);
			}
			
		}
		try {
			FileOutputStream fileOut = new FileOutputStream(outputFileName);
			wb.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// this is temporary for testing purposes
	public static void main(String[] args) {
		EqkRateModel2_ERF erRateModel2_ERF = new EqkRateModel2_ERF();
		//erRateModel2_ERF.findMinBulge();
		erRateModel2_ERF.generateExcelSheetsForRupMagRates("A_FaultRupRates_2_1.xls");
		erRateModel2_ERF.generateExcelSheetForSegRecurIntv("A_FaultSegRecurIntv_2_1.xls");
		//erRateModel2_ERF.printMag6_5_discrepancies();
		//erRateModel2_ERF.makeMatlabNNLS_testScript();
		//erRateModel2_ERF.makeTotalRelativeGriddedRates();
		
	}
}
