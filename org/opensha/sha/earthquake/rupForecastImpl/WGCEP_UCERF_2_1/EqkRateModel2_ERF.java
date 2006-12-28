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
import java.util.Iterator;
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
import org.opensha.sha.magdist.*;
import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.surface.FrankelGriddedSurface;
import org.opensha.sha.surface.StirlingGriddedSurface;
import org.opensha.util.FileUtils;

import sun.tools.tree.ThisExpression;

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
	private double totMoRateReduction;
	
	// various summed MFDs
	private SummedMagFreqDist bFaultCharSummedMFD, bFaultGR_SummedMFD, aFaultSummedMFD, cZoneSummedMFD;
	private IncrementalMagFreqDist totBackgroundMFD;
	private ArrayList<IncrementalMagFreqDist>  cZonesMFD_List;
	
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
	public final static String TOT_MAG_RATE_PARAM_NAME = "Total M³5 Rate";
	public final static Double TOT_MAG_RATE_MIN = new Double(2.0);
	public final static Double TOT_MAG_RATE_MAX = new Double(20.0);
	public final static Double TOT_MAG_RATE_DEFAULT = new Double(3.22);
	private final static String TOT_MAG_RATE_INFO = "Total rate of M³5 events in the RELM test region (e.g, 3.22 for no aftershocks, or 6.69 including aftershocks)";
	private DoubleParameter totalMagRateParam ;
	
	// Aftershock/Foreshock Fraction
	public final static String AFTERSHOCK_FRACTION_PARAM_NAME = "Aftershock/Foreshock Fraction";
	public final static Double AFTERSHOCK_FRACTION_MIN = new Double(0.0);
	public final static Double AFTERSHOCK_FRACTION_MAX = new Double(1.0);
	public final static Double AFTERSHOCK_FRACTION_DEFAULT = new Double(0.09);
	private final static String AFTERSHOCK_FRACTION_INFO = "Fraction of moment rate released in foreshocks and aftershocks";
	private DoubleParameter aftershockFractionParam ;

	// Coupling Coefficient
	public final static String COUPLING_COEFF_PARAM_NAME = "Coupling Coefficient";
	public final static Double COUPLING_COEFF_MIN = new Double(0.0);
	public final static Double COUPLING_COEFF_MAX = new Double(1.0);
	public final static Double COUPLING_COEFF_DEFAULT = new Double(0.85);
	private final static String COUPLING_COEFF_INFO = "Fraction of moment rate on A & B faults released via seismogenic processes (e.g., excluding afterslip)";
	private DoubleParameter couplingCoeffParam ;
	
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
	
/*	
	// constrain A-fault segment rates boolean
	public final static String CONSTRAIN_A_SEG_RATES_PARAM_NAME = "Constrain Segment Rates?";
	private final static String CONSTRAIN_A_SEG_RATES_PARAM_INFO = "Constrain A-fault segments rates (add equations to inversion)";
	private BooleanParameter constrainA_SegRatesParam; 
*/
	// relative a-priori weights
	private final static String REL_A_PRIORI_WT_PARAM_NAME = "Wt On A-Priori Rates";
	private final static Double REL_A_PRIORI_WT_PARAM_MIN = new Double(Double.MIN_VALUE);
	private final static Double REL_A_PRIORI_WT_PARAM_MAX = new Double(Double.MAX_VALUE);
	private final static Double REL_A_PRIORI_WT_PARAM_DEFAULT = new Double(1);
	private final static String REL_A_PRIORI_WT_PARAM_INFO = "Relative to that put on the sement slip rates";
	private DoubleParameter relativeA_PrioriWeightParam; 

	// relative segment rate weights
	private final static String REL_SEG_RATE_WT_PARAM_NAME = "Wt On Segment Rates";
	private final static Double REL_SEG_RATE_WT_PARAM_MIN = new Double(0.0);
	private final static Double REL_SEG_RATE_WT_PARAM_MAX = new Double(Double.MAX_VALUE);
	private final static Double REL_SEG_RATE_WT_PARAM_DEFAULT = new Double(1.0);
	private final static String REL_SEG_RATE_WT_PARAM_INFO = "Relative to that put on the sement slip rates";
	private DoubleParameter relativeSegRateWeightParam; 

	// connect more B-Faults boolean
	public final static String CONNECT_B_FAULTS_PARAM_NAME = "Connect More B Faults?";
	private final static String CONNECT_B_FAULTS_PARAM_INFO = "Connect nearby B-Faults";
	private BooleanParameter connectMoreB_FaultsParam;

	//	 rupture model type
	public final static String RUP_MODEL_TYPE_NAME = "A-Fault Solution Type";
	public final static String RUP_MODEL_TYPE_INFO = "The type of solution to apply for all A-Fault Sources";
	public final static String UNSEGMENTED_A_FAULT_MODEL = "Unsegmented Model";
	public final static String SEGMENTED_A_FAULT_MODEL = "Segmented Model";
	private StringParameter rupModelParam;
	
	//	 rupture model type
	public final static String SEGMENTED_RUP_MODEL_TYPE_NAME = "Segmented A-Fault Solution Types";
	public final static String SEGMENTED_RUP_MODEL_TYPE_INFO = "To set the a-prior solution for each type-A Fault Source";
	private ParameterListParameter segmentedRupModelParam;
	
	// preserve minimum A-fault rates param
	public final static String PRESERVE_MIN_A_FAULT_RATE_PARAM_NAME = "Preserve Min A Fault Rates?";
	private final static String PRESERVE_MIN_A_FAULT_RATE_PARAM_INFO = "This will prevent rates from being lower than the minimum in the a-priori model";
	private BooleanParameter preserveMinAFaultRateParam;

	// weighted inversion param
	public final static String WEIGHTED_INVERSION_PARAM_NAME = "Weighted Inversion?";
	private final static String WEIGHTED_INVERSION_PARAM_INFO = "Use segment rate and slip rate uncertainties to weight the inversion";
	private BooleanParameter weightedInversionParam;
	
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
	
	public final static String BACK_SEIS_B_VAL_PARAM_NAME = "Background Seis b-value";
	public final static String BACK_SEIS_B_VAL_PARAM_INFO = "GR-distribution b-value to apply to the background seismicity";
	public final static Double BACK_SEIS_B_DEFAULT = new Double(0.9);
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
	public final static String ABC_MO_RATE_REDUCTION_PARAM_NAME = "Fract MoRate to Background";
	public final static Double ABC_MO_RATE_REDUCTION_MIN = new Double(0);
	public final static Double ABC_MO_RATE_REDUCTION_MAX = new Double(1);
	public final static Double ABC_MO_RATE_REDUCTION_DEFAULT = new Double(0.1);
	public final static String ABC_MO_RATE_REDUCTION_INFO = "Fraction of Moment Rate to take from A & B Faults & C zones to put into background seismicity";
	private DoubleParameter moRateFracToBackgroundParam;
	
	// Mean Mag Correction
	private final static String MEAN_MAG_CORRECTION = "Mean Mag Correction";
	private final static Double MEAN_MAG_CORRECTION_MIN = new Double(-0.5);
	private final static Double MEAN_MAG_CORRECTION_MAX = new Double(0.5);
	private final static Double MEAN_MAG_CORRECTION_DEFAULT = new Double(0.0);
	private final static String MEAN_MAG_CORRECTION_INFO = "Increment added to mean mag as additional epistemic uncertainity";
	private DoubleParameter meanMagCorrectionParam;
	
	// set for background
	private final static String SET_FOR_BCK_PARAM_NAME = "MFD for Background";
	private final static String SET_FOR_BCK_PARAM_FRAC_MO_RATE_TR_GR = "Trunc. GR (Mmax from fraction)";
	private final static String SET_FOR_BCK_PARAM_FRAC_MO_RATE_TA_GR = "Tapered GR (Mcorner from fraction)";
	private final static String SET_FOR_BCK_PARAM_BCK_MAX_MAG = "Trunc. GR (w/ set Mmax)";
	private final static String SET_FOR_BCK_PARAM_NSHMP02 = "NSHMP02 MFD";
	private final static String SET_FOR_BCK_PARAM_INFO = "This specifies the type of magnitude-frequency dist. to use for the background";
	private StringParameter setForBckParam;
	
	// A and B faults fetcher
	private A_FaultsFetcher aFaultsFetcher = new A_FaultsFetcher();
	private B_FaultsFetcher bFaultsFetcher  = new B_FaultsFetcher();

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
		rupModelParam.addParameterChangeListener(this);
		connectMoreB_FaultsParam.addParameterChangeListener(this);
		updateFetchersBasedonDefModels();
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
		
		// Aftershock/Foreshock fraction
		aftershockFractionParam = new DoubleParameter(AFTERSHOCK_FRACTION_PARAM_NAME, AFTERSHOCK_FRACTION_MIN,
				AFTERSHOCK_FRACTION_MAX, AFTERSHOCK_FRACTION_DEFAULT);
		aftershockFractionParam.setInfo(AFTERSHOCK_FRACTION_INFO);

		// Coupling coeff
		couplingCoeffParam = new DoubleParameter(COUPLING_COEFF_PARAM_NAME, COUPLING_COEFF_MIN,
				COUPLING_COEFF_MAX, COUPLING_COEFF_DEFAULT);
		couplingCoeffParam.setInfo(COUPLING_COEFF_INFO);

		
		// % char vs GR param
		percentCharVsGRParam = new DoubleParameter(CHAR_VS_GR_PARAM_NAME, CHAR_VS_GR_MIN,
				CHAR_VS_GR_MAX, CHAR_VS_GR_DEFAULT);
		percentCharVsGRParam.setInfo(CHAR_VS_GR_INFO);
		
		// aseis factor param
		aseisFactorInterParam = new BooleanParameter(ASEIS_INTER_PARAM_NAME, new Boolean(true));
		aseisFactorInterParam.setInfo(ASEIS_INTER_PARAM_INFO);
		
		// relativeA_PrioriWeightParam
		relativeA_PrioriWeightParam = new DoubleParameter(REL_A_PRIORI_WT_PARAM_NAME, REL_A_PRIORI_WT_PARAM_MIN,
				REL_A_PRIORI_WT_PARAM_MAX, REL_A_PRIORI_WT_PARAM_DEFAULT);
		relativeA_PrioriWeightParam.setInfo(REL_A_PRIORI_WT_PARAM_INFO);
		
		//
		relativeSegRateWeightParam = new DoubleParameter(REL_SEG_RATE_WT_PARAM_NAME, REL_SEG_RATE_WT_PARAM_MIN,
				REL_SEG_RATE_WT_PARAM_MAX, REL_SEG_RATE_WT_PARAM_DEFAULT);
		relativeSegRateWeightParam.setInfo(REL_SEG_RATE_WT_PARAM_INFO);
/*		
		// constrainA_SegRatesParam
		constrainA_SegRatesParam = new BooleanParameter(CONSTRAIN_A_SEG_RATES_PARAM_NAME, new Boolean(true));
		constrainA_SegRatesParam.setInfo(CONSTRAIN_A_SEG_RATES_PARAM_INFO);
*/
		
		
		// preserveMinAFaultRateParam
		preserveMinAFaultRateParam = new BooleanParameter(PRESERVE_MIN_A_FAULT_RATE_PARAM_NAME, true);
		preserveMinAFaultRateParam.setInfo(PRESERVE_MIN_A_FAULT_RATE_PARAM_INFO);
		
		// weightedInversionParam
		weightedInversionParam = new BooleanParameter(WEIGHTED_INVERSION_PARAM_NAME, true);
		weightedInversionParam.setInfo(WEIGHTED_INVERSION_PARAM_INFO);
		
		// connect more B Faults
		connectMoreB_FaultsParam = new BooleanParameter(CONNECT_B_FAULTS_PARAM_NAME, true);
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
		ArrayList<String> rupModels = new ArrayList<String>();
		rupModels.add(SEGMENTED_A_FAULT_MODEL);
		rupModels.add(UNSEGMENTED_A_FAULT_MODEL);
		rupModelParam = new StringParameter(RUP_MODEL_TYPE_NAME, rupModels, rupModels.get(0));
		rupModelParam.setInfo(RUP_MODEL_TYPE_INFO);
		
		// segmented fault model param
		makeSegmentedA_FaultParam();
		
		// A-fault slip type
		slipModelParam = new StringParameter(SLIP_MODEL_TYPE_NAME, 
				A_FaultSegmentedSource.getSupportedSlipModels(), 
				A_FaultSegmentedSource.TAPERED_SLIP_MODEL);
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
		regionB_ValParam = new DoubleParameter(BACK_SEIS_B_VAL_PARAM_NAME, this.B_VAL_MIN, this.B_VAL_MAX, this.BACK_SEIS_B_DEFAULT);
		regionB_ValParam.setInfo(BACK_SEIS_B_VAL_PARAM_INFO);
		
		moRateFracToBackgroundParam = new DoubleParameter(ABC_MO_RATE_REDUCTION_PARAM_NAME, 
				ABC_MO_RATE_REDUCTION_MIN, ABC_MO_RATE_REDUCTION_MAX, 
				ABC_MO_RATE_REDUCTION_DEFAULT);
		moRateFracToBackgroundParam.setInfo(ABC_MO_RATE_REDUCTION_INFO);
		
		
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
		options.add(SET_FOR_BCK_PARAM_FRAC_MO_RATE_TR_GR);
		options.add(SET_FOR_BCK_PARAM_FRAC_MO_RATE_TA_GR);
		options.add(SET_FOR_BCK_PARAM_BCK_MAX_MAG);
		options.add(SET_FOR_BCK_PARAM_NSHMP02);
		setForBckParam = new StringParameter(SET_FOR_BCK_PARAM_NAME, options, 
				SET_FOR_BCK_PARAM_FRAC_MO_RATE_TR_GR);
		setForBckParam.setInfo(SET_FOR_BCK_PARAM_INFO);
		setForBckParam.addParameterChangeListener(this);
		// put parameters in the parameter List object	
		createParamList();
	
	}
	
	/**
	 * Put parameters in theParameterList
	 */
	private void createParamList() {
		adjustableParams = new ParameterList();
		//	 add adjustable parameters to the list
//		adjustableParams.addParameter(faultModelParam);		not needed for now
//		adjustableParams.addParameter(rupOffset_Param);		not needed for now
		adjustableParams.addParameter(deformationModelsParam);
		adjustableParams.addParameter(aftershockFractionParam);
		adjustableParams.addParameter(couplingCoeffParam);
		adjustableParams.addParameter(aseisFactorInterParam);
		adjustableParams.addParameter(rupModelParam);
		String rupModel = (String)rupModelParam.getValue();
		if(rupModel.equalsIgnoreCase(SEGMENTED_A_FAULT_MODEL)) adjustableParams.addParameter(segmentedRupModelParam);
		if(rupModel.equalsIgnoreCase(SEGMENTED_A_FAULT_MODEL)) adjustableParams.addParameter(slipModelParam);
		if(rupModel.equalsIgnoreCase(SEGMENTED_A_FAULT_MODEL)) adjustableParams.addParameter(relativeA_PrioriWeightParam);
		if(rupModel.equalsIgnoreCase(SEGMENTED_A_FAULT_MODEL)) adjustableParams.addParameter(relativeSegRateWeightParam);
		if(rupModel.equalsIgnoreCase(SEGMENTED_A_FAULT_MODEL)) adjustableParams.addParameter(weightedInversionParam);
		if(rupModel.equalsIgnoreCase(SEGMENTED_A_FAULT_MODEL)) adjustableParams.addParameter(preserveMinAFaultRateParam);
		adjustableParams.addParameter(magAreaRelParam);
		adjustableParams.addParameter(magSigmaParam);
		adjustableParams.addParameter(truncLevelParam);
		adjustableParams.addParameter(percentCharVsGRParam);
		adjustableParams.addParameter(bFaultB_ValParam);
		adjustableParams.addParameter(bFaultsMinMagParam);
		adjustableParams.addParameter(connectMoreB_FaultsParam);
//		adjustableParams.addParameter(backSeisParam);		not needed for now
		adjustableParams.addParameter(includeC_ZonesParam);
		adjustableParams.addParameter(meanMagCorrectionParam);
		adjustableParams.addParameter(totalMagRateParam);
		adjustableParams.addParameter(moRateFracToBackgroundParam);
		adjustableParams.addParameter(setForBckParam);
		String setForBackground = (String)setForBckParam.getValue();
		if(setForBackground.equalsIgnoreCase(SET_FOR_BCK_PARAM_FRAC_MO_RATE_TR_GR) || setForBackground.equalsIgnoreCase(SET_FOR_BCK_PARAM_FRAC_MO_RATE_TA_GR)) 
			adjustableParams.addParameter(regionB_ValParam);
		else if(setForBackground.equalsIgnoreCase(SET_FOR_BCK_PARAM_BCK_MAX_MAG)) {
			adjustableParams.addParameter(regionB_ValParam);
			adjustableParams.addParameter(backSeisMaxMagParam);
		}
		// the else case (SET_FOR_BCK_PARAM_NSHMP02) adds nothing here
	}
	
	/**
	 * Rup Model options for each A-fault in case of segmented 
	 *
	 */
	private void makeSegmentedA_FaultParam() {
		ParameterList paramList = new ParameterList();
		ArrayList<String> faultNames = aFaultsFetcher.getAllFaultNames();
		for(int i=0; i<faultNames.size(); ++i) {
			ArrayList<String> supportedRupModels = aFaultsFetcher.getRupModels(faultNames.get(i));
			StringParameter rupModelParam = new StringParameter(faultNames.get(i), supportedRupModels, supportedRupModels.get(0));
			paramList.addParameter(rupModelParam);
		}
		segmentedRupModelParam = new ParameterListParameter(this.SEGMENTED_RUP_MODEL_TYPE_NAME, paramList);
		segmentedRupModelParam.setInfo(this.SEGMENTED_RUP_MODEL_TYPE_INFO);
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
		
		// get the total rate of M³5 events
		double rate = ((Double) totalMagRateParam.getValue()).doubleValue();
		double bValue = ((Double) regionB_ValParam.getValue()).doubleValue();
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
		((GutenbergRichterMagFreqDist)totBackgroundMFD).setAllButMagUpper(MIN_MAG, totBackMoRate, totBackRate, bValue, true);
	}

	
	private void  makeBackgroundGridSources() {
		
		// get the total rate of M³5 events & b-value
		double rate = ((Double)totalMagRateParam.getValue()).doubleValue();
		double bValue = ((Double)regionB_ValParam.getValue()).doubleValue();
		
		double totRateABC = aFaultSummedMFD.getTotalIncrRate()+bFaultCharSummedMFD.getTotalIncrRate()+
		bFaultGR_SummedMFD.getTotalIncrRate()+cZoneSummedMFD.getTotalIncrRate();
		
		double totBackRate = rate-totRateABC;
		
		String backgroundTreatment = (String) setForBckParam.getValue();
		if(backgroundTreatment.equals(SET_FOR_BCK_PARAM_FRAC_MO_RATE_TR_GR) ||
				backgroundTreatment.equals(SET_FOR_BCK_PARAM_FRAC_MO_RATE_TA_GR)) {
			double totMoRateABC = aFaultSummedMFD.getTotalMomentRate()+bFaultCharSummedMFD.getTotalMomentRate()+
			bFaultGR_SummedMFD.getTotalMomentRate()+cZoneSummedMFD.getTotalMomentRate();
			//restore the original, total moment rate:
			totMoRateABC /= (1-totMoRateReduction);
			// now get background component:
			double moRateFracToBackground = ((Double)moRateFracToBackgroundParam.getValue()).doubleValue();
			double totBackMoRate = totMoRateABC*moRateFracToBackground;
			if(backgroundTreatment.equals(SET_FOR_BCK_PARAM_FRAC_MO_RATE_TR_GR)) {
				totBackgroundMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
				if(moRateFracToBackground > 0)
					((GutenbergRichterMagFreqDist) totBackgroundMFD).setAllButMagUpper(MIN_MAG, totBackMoRate, totBackRate, bValue, true);
					// NOTE that momentRate is not exactly conserved here due to mag discretization
			}
			else {
				totBackgroundMFD = new TaperedGR_MagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
				if(moRateFracToBackground > 0)
					((TaperedGR_MagFreqDist) totBackgroundMFD).setAllButCornerMag(MIN_MAG, totBackMoRate, totBackRate, bValue);
			}
//			System.out.println(totBackMoRate+", "+totBackRate+", "+bValue+", "+totBackgroundMFD.getTotalMomentRate());
		}
		else if(backgroundTreatment.equals(SET_FOR_BCK_PARAM_BCK_MAX_MAG)) {
			double magMax = ((Double)backSeisMaxMagParam.getValue()).doubleValue();
			totBackgroundMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
			((GutenbergRichterMagFreqDist) totBackgroundMFD).setAllButTotMoRate(MIN_MAG, magMax, totBackRate, bValue);
		}
		else { // the SET_FOR_BCK_PARAM_NSHMP02 case
			totBackgroundMFD = getNSHMP02_CAmap_MFD();
			totBackgroundMFD.scaleToCumRate(5.0,totBackRate);
		}
		
// System.out.println(totBackgroundMFD.getTotalMomentRate()+","+totBackgroundMFD.getTotalIncrRate());

		
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
	
	/**
	 * This returns the total MFD for the two NSHMP02 CAmap? background seis input files
	 * @return
	 */
	private IncrementalMagFreqDist getNSHMP02_CAmap_MFD() {
		IncrementalMagFreqDist nshmp02_CAmap_MFD = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		nshmp02_CAmap_MFD.set(5.0, 0.16698005174155264);
		nshmp02_CAmap_MFD.set(5.1, 0.3058680092770254);
		nshmp02_CAmap_MFD.set(5.2, 0.2544099288559251);
		nshmp02_CAmap_MFD.set(5.3, 0.2116089618311663);
		nshmp02_CAmap_MFD.set(5.4, 0.17600866809181212);
		nshmp02_CAmap_MFD.set(5.5, 0.14639763351880405);
		nshmp02_CAmap_MFD.set(5.6, 0.12176824773610705);
		nshmp02_CAmap_MFD.set(5.7, 0.10128241693754861);
		nshmp02_CAmap_MFD.set(5.8, 0.08419511268253424);
		nshmp02_CAmap_MFD.set(5.9, 0.06998251205740466);
		nshmp02_CAmap_MFD.set(6.0, 0.05808698785127969);
		nshmp02_CAmap_MFD.set(6.1, 0.048192721778350184);
		nshmp02_CAmap_MFD.set(6.2, 0.03995083223814626);
		nshmp02_CAmap_MFD.set(6.3, 0.03304396747741058);
		nshmp02_CAmap_MFD.set(6.4, 0.027317668655642918);
		nshmp02_CAmap_MFD.set(6.5, 0.021448389512954837);
		nshmp02_CAmap_MFD.set(6.6, 0.01655280428196882);
		nshmp02_CAmap_MFD.set(6.7, 0.013495202518441145);
		nshmp02_CAmap_MFD.set(6.8, 0.010890259946258175);
		nshmp02_CAmap_MFD.set(6.9, 0.008774086217621335);
		nshmp02_CAmap_MFD.set(7.0, 0.0039333546797175895);
		return nshmp02_CAmap_MFD;
	}
	
	private void makeC_ZoneSources() {
		cZoneSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		cZonesMFD_List = new ArrayList<IncrementalMagFreqDist> ();
		if(((Boolean)includeC_ZonesParam.getValue()).booleanValue()) {
 			
			String []names = { "Foothills Fault System", "Mohawk-Honey Lake Zone",
					"Northeastern California", "Western Nevada", "Eastern California Shear Zone N",
					"Eastern California Shear Zone S", "Imperial Valley", "San Gorgonio Knot"};
			
			double[] slipRates = { 0.1, 4.0, 8.0, 8.0, 7.0, 10.0, 14.0, 2.0}; // mm/yr
			double[] depthTop = { 0, 0, 0, 0, 0, 0, 0, 0}; // km
			double[] depthBottom = { 12, 15, 15, 15, 14, 15.5, 12.6, 18.3}; //km
			double[] strike = { 325, 335, 315, 315, 320, 320, 310, 290};
			double[] length = { 360, 88, 230, 245, 180, 88, 58, 100 }; // km
			double[] magLower = {6.0, 6.5, 6.5, 6.5, 6.5, 6.5, 6.5, 6.5}; 
			double[] magUpper = {7.0, 7.3, 7.3, 7.3, 7.6, 7.6, 7.3, 7.3};
			double bValue = 0.8;
			double moRate, slipRate;
			for(int i=0; i<names.length; ++i) {
				// reduce slip rate by total moment rate reduction
				slipRate = (1-totMoRateReduction)*slipRates[i]/1000.0;
				moRate = FaultMomentCalc.getMoment((depthBottom[i]-depthTop[i])*length[i]*1e6, slipRate)*(1-totMoRateReduction);
				GutenbergRichterMagFreqDist grMFD = new GutenbergRichterMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
				grMFD.setAllButTotCumRate(magLower[i], magUpper[i], moRate, bValue);
				grMFD.setName(names[i]);
				cZonesMFD_List.add(grMFD);
				cZoneSummedMFD.addIncrementalMagFreqDist(grMFD);
			}
		}
	}
	
	private void mkA_FaultSegmentedSources() {
		double relativeA_PrioriWeight = ((Double)relativeA_PrioriWeightParam.getValue()).doubleValue();
		double relativeSegRateWeight = ((Double)relativeSegRateWeightParam.getValue()).doubleValue();
		double magSigma  = ((Double) magSigmaParam.getValue()).doubleValue();
		double magTruncLevel = ((Double) truncLevelParam.getValue()).doubleValue();
		ParameterList rupModels = (ParameterList) this.segmentedRupModelParam.getValue();
		String slipModel = (String)slipModelParam.getValue();
		boolean isAseisReducesArea = ((Boolean) aseisFactorInterParam.getValue()).booleanValue();
		double meanMagCorrection = ((Double)meanMagCorrectionParam.getValue()).doubleValue();
		boolean preserveMinAFaultRate = ((Boolean) preserveMinAFaultRateParam.getValue()).booleanValue();
		boolean wtedInversion = ((Boolean) weightedInversionParam.getValue()).booleanValue();
		// this gets a list of FaultSegmentData objects (one for each A fault, and for the deformation model previously set)
		ArrayList aFaultSegmentData = aFaultsFetcher.getFaultSegmentDataList(isAseisReducesArea);
		aFaultSources = new ArrayList();
		aFaultSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		//System.out.println("************ Initial ******"+aFaultSummedMFD.toString());
		for(int i=0; i<aFaultSegmentData.size(); ++i) {
			FaultSegmentData segmentData = (FaultSegmentData) aFaultSegmentData.get(i);
			ValueWeight[] aPrioriRates = aFaultsFetcher.getAprioriRupRates(segmentData.getFaultName(), (String)rupModels.getValue(segmentData.getFaultName()));
			A_FaultSegmentedSource aFaultSource = new A_FaultSegmentedSource(segmentData, 
					getMagAreaRelationship(), slipModel, aPrioriRates, magSigma, 
					magTruncLevel, totMoRateReduction, meanMagCorrection,preserveMinAFaultRate, 
					wtedInversion, relativeSegRateWeight, relativeA_PrioriWeight);
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
		boolean isAseisReducesArea = ((Boolean) aseisFactorInterParam.getValue()).booleanValue();
		double bValue = ((Double) bFaultB_ValParam.getValue()).doubleValue();
		double meanMagCorrection = ((Double)meanMagCorrectionParam.getValue()).doubleValue();
		double minMagGR = ((Double) bFaultsMinMagParam.getValue()).doubleValue();
//		 this gets a list of FaultSegmentData objects (one for each A fault, and for the deformation model previously set)	
		ArrayList aFaultSegmentData = aFaultsFetcher.getFaultSegmentDataList(isAseisReducesArea);
		aFaultSources = new ArrayList();
		aFaultSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		for(int i=0; i<aFaultSegmentData.size(); ++i) {
			FaultSegmentData segmentData = (FaultSegmentData) aFaultSegmentData.get(i);
			UnsegmentedSource source = new UnsegmentedSource( segmentData,  magAreaRel, 
					fractCharVsGR,  MIN_MAG, MAX_MAG, NUM_MAG, magSigma, magTruncLevel, 
					minMagGR, bValue, totMoRateReduction, Double.NaN, Double.NaN, meanMagCorrection);
			aFaultSources.add(source);
			aFaultSummedMFD.addIncrementalMagFreqDist(source.getMagFreqDist());   		
		}
	}
	
	private void mkB_FaultSources() {
		double magSigma  = ((Double) magSigmaParam.getValue()).doubleValue();
		double magTruncLevel = ((Double) truncLevelParam.getValue()).doubleValue();
		double fractCharVsGR= ((Double) percentCharVsGRParam.getValue()).doubleValue()/100.0;
		MagAreaRelationship magAreaRel = getMagAreaRelationship();
		boolean isAseisReducesArea = ((Boolean) aseisFactorInterParam.getValue()).booleanValue();
		double meanMagCorrection = ((Double)meanMagCorrectionParam.getValue()).doubleValue();
		double minMagGR = ((Double) bFaultsMinMagParam.getValue()).doubleValue();
		double bValue = ((Double) bFaultB_ValParam.getValue()).doubleValue();
//		 this gets a list of FaultSegmentData objects (one for each B fault, and for the deformation model previously set)
		ArrayList bFaultSegmentData = bFaultsFetcher.getFaultSegmentDataList(isAseisReducesArea);
//		ArrayList B_faultCharMFDs = new ArrayList();
//		ArrayList B_faultGR_MFDs = new ArrayList();
		bFaultCharSummedMFD= new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		bFaultGR_SummedMFD= new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		bFaultSources = new ArrayList();
		double fixMag, fixRate;
		try{
			//FileWriter fw1 = new FileWriter("B_Char_Temp.txt");
			//FileWriter fw2 = new FileWriter("B_GR_Temp.txt");
			for(int i=0; i<bFaultSegmentData.size(); ++i) {
				FaultSegmentData segmentData = (FaultSegmentData)bFaultSegmentData.get(i);
				fixMag = bFaultFixes.getMag(segmentData.getFaultName());
				fixRate = bFaultFixes.getRate(segmentData.getFaultName());
				//if(!Double.isNaN(fixMag)) {
				//	System.out.println(segmentData.getFaultName()+","+fixMag+","+fixRate);
				//}
				UnsegmentedSource source = new UnsegmentedSource( segmentData,  magAreaRel, 
						fractCharVsGR,  MIN_MAG, MAX_MAG, NUM_MAG, magSigma, magTruncLevel,minMagGR, 
						bValue, totMoRateReduction, fixMag, fixRate, meanMagCorrection);
				bFaultSources.add(source);
				IncrementalMagFreqDist charMagFreqDist = source.getCharMagFreqDist();
				//fw1.write(segmentData.getFaultName()+";"+(float)charMagFreqDist.getCumRate(6.5)+"\n");
				bFaultCharSummedMFD.addIncrementalMagFreqDist(charMagFreqDist);
				IncrementalMagFreqDist grMagFreqDist = source.getGR_MagFreqDist();
				if(source.getGR_MagFreqDist() != null)  {// will be null if char mag is lower than mag lower of GR
					bFaultGR_SummedMFD.addIncrementalMagFreqDist(grMagFreqDist);
					//fw2.write(segmentData.getFaultName()+";"+(float)grMagFreqDist.getCumRate(6.5)+"\n");
				}
			}
			//fw1.close();
			//fw2.close();
		}catch(Exception e) {
			e.printStackTrace();
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
			newFunc.setInfo("Cumulative MFD for Karen Felzer's best-fit to observed catalog including aftershocks (Rate(M³5)=6.69; b= 1.0)");
		else
			newFunc.setInfo("Cumulative MFD for Karen Felzer's best-fit to observed catalog excluding aftershocks (Rate(M³5)=3.22; b= 0.8)");
		
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
	public ArrayList<EvenlyDiscretizedFunc> getObsCumMFD(boolean includeAftershocks) {
		EvenlyDiscretizedFunc obsCumMFD = new IncrementalMagFreqDist(5.0, 7.5, 6);
		EvenlyDiscretizedFunc obsCumLowMFD = new IncrementalMagFreqDist(5.0, 7.5, 6);
		EvenlyDiscretizedFunc obsCumHighMFD = new IncrementalMagFreqDist(5.0, 7.5, 6);
		double[] cumRatesWith = {6.69, 2.11, 0.66, 0.21, 0.06, 0.015};
		double[] cumRatesLowWith =  {6.69-2.7, 2.11-0.85, 0.66-0.27, 0.21-0.11, 0.06-0.045, 0.015-0.01};
		double[] cumRatesHighWith = {6.69+2.7, 2.11+0.85, 0.66+0.27, 0.21+0.11, 0.06+0.077, 0.015+0.026};
		double[] cumRates = {3.22, 1.27, 0.5, 0.19, 0.06, 0.015};
		double[] cumRatesLow =  {3.22-1.13, 1.27-0.45, 0.5-0.18, 0.19-0.1, 0.06-0.045, 0.015-0.01};
		double[] cumRatesHigh = {3.22+1.13, 1.27+0.45, 0.5+0.18, 0.19+0.1, 0.06+0.077, 0.015+0.026};
		
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
		
		/*
		 * Convert into incremental and find the moment rate
		 * 
		 IncrementalMagFreqDist mfd = new IncrementalMagFreqDist(5, 7.5, 6);
		for(int i=0; i<mfd.getNum(); ++i) {
			if(i==mfd.getNum()-1)
				mfd.set(i, obsCumMFD.getInterpolatedY(mfd.getX(i)));
			else 	
				mfd.set(i, obsCumMFD.getInterpolatedY(mfd.getX(i)) - obsCumMFD.getInterpolatedY(mfd.getX(i+1)));
		}
		System.out.println(mfd.toString());
		System.out.println("IncludeAftershocks="+includeAftershocks+", moRate="+mfd.getTotalMomentRate());
		
		*/
		
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
	
	/**
	 * Return a list of MFDs, one for each C zone
	 * @return
	 */
	public ArrayList<IncrementalMagFreqDist> getC_ZoneMFD_List() {
		return this.cZonesMFD_List;
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
		double totToKeep = 1;
		// compute total moment rate reduction for A/B faults (fraction to reduce by)
		// 1st remove that which goes to the background
		totToKeep *= 1.0-((Double) moRateFracToBackgroundParam.getValue()).doubleValue();
		// now remove that which goes to aseismicity
		totToKeep *= ((Double)couplingCoeffParam.getValue()).doubleValue();
		// finally, remove that which goes to aftershocks
		totToKeep *= 1-((Double) aftershockFractionParam.getValue()).doubleValue();
		totMoRateReduction = 1.0-totToKeep;
		
// System.out.println("totMoRateReduction="+totMoRateReduction);
		
		String rupModel = (String) rupModelParam.getValue();
		
		//System.out.println("Creating A Fault sources");
		if(rupModel.equalsIgnoreCase(UNSEGMENTED_A_FAULT_MODEL)) 
			mkA_FaultUnsegmentedSources();
		else 
			mkA_FaultSegmentedSources();

		//System.out.println("Creating B Fault sources");
		mkB_FaultSources();

		//System.out.println("Creating C Zone Fault sources");
		makeC_ZoneSources();

		//System.out.println("Creating Background sources");
		makeBackgroundGridSources();
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
		
		if(paramName.equalsIgnoreCase(SET_FOR_BCK_PARAM_NAME) || paramName.equalsIgnoreCase(RUP_MODEL_TYPE_NAME)) {
			createParamList();
		} else if(paramName.equalsIgnoreCase(CONNECT_B_FAULTS_PARAM_NAME)) { // whether more B-Faults need to be connected
			bFaultsFetcher.setDeformationModel( ((Boolean) connectMoreB_FaultsParam.getValue()).booleanValue(), 
					getSelectedDeformationModelSummary(), aFaultsFetcher);
			//bFaultsFetcher.test_writeFileAfterCombiningB_Faults();
		} else if(paramName.equalsIgnoreCase(DEFORMATION_MODEL_PARAM_NAME)) { // if deformation model changes, update the files to be read
			updateFetchersBasedonDefModels();
			//bFaultsFetcher.test_writeFileAfterCombiningB_Faults();
		} 
	}

	/**
	 * Update fetchers based on selected deformation model
	 *
	 */
	private void updateFetchersBasedonDefModels() {
		aFaultsFetcher.setDeformationModel(getSelectedDeformationModelSummary());
		bFaultsFetcher.setDeformationModel( ((Boolean) connectMoreB_FaultsParam.getValue()).booleanValue(), 
				getSelectedDeformationModelSummary(), aFaultsFetcher);
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
//		throw new RuntimeException ("Method unsupported exception");
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
//		ArrayList rupModelOptions = ((StringConstraint)rupModelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		for(int imag=0; imag<magAreaOptions.size();imag++)
//			for(int irup=0; irup<rupModelOptions.size();irup++)
					for(int islip=0; islip<slipModelOptions.size();islip++) {
						magAreaRelParam.setValue(magAreaOptions.get(imag));
//						rupModelParam.setValue(rupModelOptions.get(irup));
						slipModelParam.setValue(slipModelOptions.get(islip));
						System.out.println("% "+magAreaOptions.get(imag)+
//								"\n% " + rupModelOptions.get(irup) +
								"\n% " + slipModelOptions.get(islip));
						System.out.println("display CASE_"+imag+"_"+islip);
//						System.out.println("display CASE_"+imag+"_"+irup+"_"+islip);
						mkA_FaultSegmentedSources();
					}
	}
	
	
	
	
	private void printMag6_5_discrepancies() {
		throw new RuntimeException ("Method unsupported exception");
		/*ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
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
						// print out pred rate of M³6.5 and ratio with respect to obsBestFitCumMFD at same mag
						double predCumRate = getTotalMFD().getCumRate(6.5);
						
						System.out.println((float)(predCumRate/obVal)+" , "+(float)predCumRate+" , "+magAreaOptions.get(imag)+
								" , " + rupModelOptions.get(irup) +
								" , " + slipModelOptions.get(islip));
						//System.out.println("display CASE_"+imag+"_"+irup+"_"+islip);

					}*/
	}
	
	/**
	 * 
	 *
	 */
	private void findMinBulge() {
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		double obVal = this.getObsBestFitCumMFD(true).getY(6.5);
		double minRatio = 10, ratio;
		String str="", minStr="";
		try {
		FileWriter fw = new FileWriter("Bulge.txt");
		int imag=1;
		//for(int imag=0; imag<magAreaOptions.size();imag++)
		String[] models = {"Geological Insight", "Min Rate", "Max Rate"};
			for(int irup=0; irup<1;irup++) {
				Iterator it = this.segmentedRupModelParam.getParametersIterator();
				while(it.hasNext()) { // set the specfiied rup model in each A fault
					StringParameter param = (StringParameter)it.next();
					ArrayList<String> allowedVals = param.getAllowedStrings();
					param.setValue(allowedVals.get(irup));
				}
					for(int islip=2; islip<slipModelOptions.size();islip++) 
						for(double per=0.0; per<=80; per+=10) // % char vs GR
							for(double bVal1=0.8; bVal1<=1.2; bVal1+=0.1)  // b faults B val
								for(double bVal2=0.8; bVal2<=1.2; bVal2+=0.1) // bacgrd B val
									for(double frac=0.1; frac<0.3; frac+=0.05){ // moment rate reduction
										magAreaRelParam.setValue(magAreaOptions.get(imag));
										
										slipModelParam.setValue(slipModelOptions.get(islip));
										this.moRateFracToBackgroundParam.setValue(frac);
										this.bFaultB_ValParam.setValue(bVal1);
										this.percentCharVsGRParam.setValue(per);
										this.regionB_ValParam.setValue(bVal2);
										try {
											updateForecast();
										}catch(Exception e) {
											System.out.println(e.getMessage()+" , "+magAreaOptions.get(imag)+
													" , " + models[irup] +
													" , " + slipModelOptions.get(islip));
											continue;
										}
										// print out pred rate of M³6.5 and ratio with respect to obsBestFitCumMFD at same mag
										double predCumRate = getTotalMFD().getCumRate(6.5);
										ratio = (predCumRate/obVal);
										str = (float)(predCumRate/obVal)+" , "+(float)predCumRate+" , "+magAreaOptions.get(imag)+
											" , " + models[irup] +
											" , " + slipModelOptions.get(islip)+","+per+","+bVal1+","+
											bVal2+","+frac;
										System.out.println(str);
										fw.write(str+"\n");
										if(ratio<minRatio) {
											minRatio = ratio;
											minStr = str;
										}
										
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
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		int numA_Faults = this.aFaultsFetcher.getAllFaultNames().size();	
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
		String[] models = {"Geological Insight", "Min Rate", "Max Rate"};
		for(int irup=0; irup<3;irup++) { // various rupture model types
			int rupStartRow[] = new int[numA_Faults];
			Iterator it = this.segmentedRupModelParam.getParametersIterator();
			while(it.hasNext()) { // set the specfiied rup model in each A fault
				StringParameter param = (StringParameter)it.next();
				ArrayList<String> allowedVals = param.getAllowedStrings();
				param.setValue(allowedVals.get(irup));
			}
			
			for(int imag=0; imag<magAreaOptions.size();imag++) {
					for(int islip=0; islip<slipModelOptions.size();islip++) {
			
						magAreaRelParam.setValue(magAreaOptions.get(imag));
						slipModelParam.setValue(slipModelOptions.get(islip));
						
						
						this.updateForecast();
						
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
								 cell.setCellValue(models[irup]);
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
									 double recurIntv = source.getFaultSegmentData().getRecurInterval(seg);
									 if(Double.isNaN(recurIntv)) continue;
									 double recurIntvStdDev = source.getFaultSegmentData().getRecurIntervalSigma(seg);
									 //System.out.println(seg+","+source.getFaultSegmentData().getSegmentName(seg));
									 row.createCell((short)1).setCellValue((int)Math.round(recurIntv));
									 row.createCell((short)2).setCellValue((int)Math.round(recurIntv-2*recurIntvStdDev));
									 row.createCell((short)3).setCellValue((int)Math.round(recurIntv+2*recurIntvStdDev));

									 //row.createCell((short)1).setCellValue(source.getS(rup));
								 }
							}
						}
						   
						   
						
						// 
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
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		int numA_Faults = this.aFaultsFetcher.getAllFaultNames().size();	
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
		String[] models = {"Geological Insight", "Min Rate", "Max Rate"};
		int currRow[] = new int[numA_Faults];
		for(int irup=0; irup<3;irup++) {
			int rupStartRow[] = new int[numA_Faults];
			
			Iterator it = this.segmentedRupModelParam.getParametersIterator();
			while(it.hasNext()) { // set the specfiied rup model in each A fault
				StringParameter param = (StringParameter)it.next();
				ArrayList<String> allowedVals = param.getAllowedStrings();
				param.setValue(allowedVals.get(irup));
			}
			
			for(int imag=0; imag<magAreaOptions.size();imag++) {
				
					for(int islip=0; islip<slipModelOptions.size();islip++) {
			
						magAreaRelParam.setValue(magAreaOptions.get(imag));
						slipModelParam.setValue(slipModelOptions.get(islip));
						this.updateForecast();
						
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
								 cell.setCellValue(models[irup]);
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
								 cell.setCellValue("Original Slip Rate");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Min Slip Rate");
								 cell.setCellStyle(cellStyle);
								 cell = row.createCell((short)col++);
								 cell.setCellValue("Max Slip Rate");
								 cell.setCellStyle(cellStyle);

								 cell = row.createCell((short)col++);
								 cell.setCellValue("Characteristic Model Slip Rate");
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
									 double slipRate = source.getFaultSegmentData().getSegmentSlipRate(seg);
									 double stdDev = source.getFaultSegmentData().getSegSlipRateStdDev(seg);
									 //System.out.println(seg+","+source.getFaultSegmentData().getSegmentName(seg));
									 row.createCell((short)1).setCellValue(slipRate*1e3);
									 row.createCell((short)2).setCellValue((slipRate-2*stdDev)*1e3);
									 row.createCell((short)3).setCellValue((slipRate+2*stdDev)*1e3);
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
		
		System.out.println(outputFileName);
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		
		int numA_Faults = this.aFaultsFetcher.getAllFaultNames().size();	
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
		HSSFSheet genPredErrSheet = wb.createSheet(); // Sheet for displaying the General Prediction error
		wb.setSheetName(wb.getNumberOfSheets()-1, "Gen. Pred. Err");
		HSSFSheet totalRatesSheet = wb.createSheet(); // Sheet for displaying the Total Rates
		wb.setSheetName(wb.getNumberOfSheets()-1, "Total Rates");
		String[] models = {"Geological Insight", "Min Rate", "Max Rate"};
		int currRow[] = new int[numA_Faults];
		for(int irup=0; irup<3;irup++) {
			int rupStartRow[] = new int[numA_Faults];
			
			Iterator it = this.segmentedRupModelParam.getParametersIterator();
			while(it.hasNext()) { // set the specfiied rup model in each A fault
				StringParameter param = (StringParameter)it.next();
				ArrayList<String> allowedVals = param.getAllowedStrings();
				param.setValue(allowedVals.get(irup));
			}
			
			for(int imag=0; imag<magAreaOptions.size();imag++) {
				//int numSlipModels = slipModelOptions.size();
				//double magRate[][] = new double[numSlipModels][2];
				for(int islip=0; islip<slipModelOptions.size();islip++) {
			
						magAreaRelParam.setValue(magAreaOptions.get(imag));
						
						slipModelParam.setValue(slipModelOptions.get(islip));
						this.updateForecast();
						this.genPredErrAndTotRateSheet( genPredErrSheet, totalRatesSheet, imag, islip,irup, cellStyle);
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
								 cell.setCellValue(models[irup]);
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
								 int totRow1=currRow[i];
								 int totRow2 = 2*totRow1+2;
								 int totRow3 = 3*totRow1+4;
								 int ratioRowIndex1 = totRow3+2;
								 int ratioRowIndex2 = ratioRowIndex1+1;
								 int ratioRowIndex3 = ratioRowIndex1+2;
								 HSSFRow ratioRow1=null, ratioRow2=null, ratioRow3=null;
								 
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
								 if(irup==0){
									 ratioRow1 = sheet.createRow((short)ratioRowIndex1);
									 ratioRow2 = sheet.createRow((short)ratioRowIndex2);
									 ratioRow3 = sheet.createRow((short)ratioRowIndex3);				 
									 ratioRow1.createCell((short)0).setCellValue("min/geol");	
									 ratioRow2.createCell((short)0).setCellValue("max/geol");	
									 ratioRow3.createCell((short)0).setCellValue("max/min");	
									 // a priori rate ratio
									 colStr="B";
									 cell = ratioRow1.createCell((short)1);
									 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
									 cell.setCellFormula(colStr+totRow2+"/"+colStr+totRow1);
									 cell = ratioRow2.createCell((short)1);
									 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
									 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow1);
									 cell = ratioRow3.createCell((short)1);
									 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
									 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow2);
									 // Char rate ratio
									 colStr="D";
									 cell = ratioRow1.createCell((short)3);
									 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
									 cell.setCellFormula(colStr+totRow2+"/"+colStr+totRow1);
									 cell = ratioRow2.createCell((short)3);
									 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
									 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow1);
									 cell = ratioRow3.createCell((short)3);
									 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
									 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow2);
								 }
								 
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
										 if(irup==0){
											 cell = ratioRow1.createCell((short)totCol);
											 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
											 cell.setCellFormula(colStr+totRow2+"/"+colStr+totRow1);
											 cell = ratioRow2.createCell((short)totCol);
											 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
											 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow1);
											 cell = ratioRow3.createCell((short)totCol);
											 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
											 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow2);
										 }
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
			for(int i=0; i<(wb.getNumberOfSheets()-2); ++i) { // do not do for Gen Pred Error and Tot rates
				HSSFSheet sheet = wb.getSheetAt(i);
				sheet.createRow((short)currRow[i]++);
				sheet.createRow((short)currRow[i]++);
			}
			
		}
		try {
			HSSFSheet metadataSheet = wb.createSheet(); // Sheet for displaying the Total Rates
			wb.setSheetName(wb.getNumberOfSheets()-1, "Metadata");
			Iterator it = this.adjustableParams.getParametersIterator();
			int row = 0;
			while(it.hasNext()) {
				ParameterAPI param = (ParameterAPI)it.next();
				if(param.getName().equals(MAG_AREA_RELS_PARAM_NAME) || param.getName().equals(SLIP_MODEL_TYPE_NAME) ||
						param.getName().equals(RUP_MODEL_TYPE_NAME)) continue;
				metadataSheet.createRow(row++).createCell((short)0).setCellValue(param.getMetadataString());
			}
			
			FileOutputStream fileOut = new FileOutputStream(outputFileName);
			wb.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate the excel sheet to save the general prediction error
	 * @param sheet
	 */
	private void genPredErrAndTotRateSheet(HSSFSheet predErrSheet, HSSFSheet totRateSheet, int imag, int islip, int irup, HSSFCellStyle cellStyle) {
		
		ArrayList magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		ArrayList slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		int numA_Faults = this.aFaultsFetcher.getAllFaultNames().size();	
		//Create Excel Workbook and sheets if they do not exist already
		double totRate=0;
		
		//currRow = new int[aFaultSources.size()];
		String[] models = {"Geological Insight", "Min Rate", "Max Rate"};
		// Write header for each Rup Solution Types
		int currRow  = irup*(numA_Faults+6);
		int faultNamesStartRow = currRow+3;
		if(imag==0 && islip==0) { // Write the headers and fault names for the first time	
			HSSFRow row1 = predErrSheet.createRow((short)currRow);
			HSSFRow row2 = totRateSheet.createRow((short)currRow++);
			// Write Rup solution Type
			HSSFCell cell1 = row1.createCell((short)0);
			HSSFCell cell2 = row2.createCell((short)0);
			cell1.setCellValue(models[irup]);
			cell2.setCellValue(models[irup]);
			cell1.setCellStyle(cellStyle);
			cell2.setCellStyle(cellStyle);
			row1 = predErrSheet.createRow((short)currRow);
			row2 = totRateSheet.createRow((short)currRow++);
			int col=2;
			// Write All Mag Areas in appropriate columns
			for(int j=0; j<magAreaOptions.size(); ++j, col+=(slipModelOptions.size()-1)/* Not to be done for Char */) {
				cell1 = row1.createCell((short)col);
				cell1.setCellValue((String)magAreaOptions.get(j));
				cell1.setCellStyle(cellStyle);
				cell2 = row2.createCell((short)col);
				cell2.setCellValue((String)magAreaOptions.get(j));
				cell2.setCellStyle(cellStyle);
			}
			// write the headers
			row1 = predErrSheet.createRow((short)currRow);
			row2 = totRateSheet.createRow((short)currRow++);
			col=0;
			cell1 = row1.createCell((short)col);
			cell2 = row2.createCell((short)col++);
			cell1.setCellValue("Fault Name");
			cell2.setCellValue("Total Rate");
			cell1.setCellStyle(cellStyle);
			cell2.setCellStyle(cellStyle);
			cell1 = row1.createCell((short)col);
			cell2 = row2.createCell((short)col++);
			cell1.setCellValue("Characteristic");
			cell2.setCellValue("Characteristic");
			cell1.setCellStyle(cellStyle);
			cell2.setCellStyle(cellStyle);
			for(int j=0; j<magAreaOptions.size(); ++j) {
				for(int k=0; k<slipModelOptions.size(); ++k) {
					String slipModel = (String)slipModelOptions.get(k);
					if(!slipModel.equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) {
						cell1 = row1.createCell((short)col);
						cell1.setCellValue((String)slipModelOptions.get(k));
						cell1.setCellStyle(cellStyle);
						cell2 = row2.createCell((short)col++);
						cell2.setCellValue((String)slipModelOptions.get(k));
						cell2.setCellStyle(cellStyle);
					}
				}
			}	
			
			// write Source Names
			for(int iSource=0; iSource<aFaultSources.size(); ++iSource) {
				row1 = predErrSheet.createRow((short)(faultNamesStartRow+iSource));
				A_FaultSegmentedSource src = (A_FaultSegmentedSource)aFaultSources.get(iSource);
				row1.createCell((short)0).setCellValue(src.getFaultSegmentData().getFaultName());
			}
			
			totRateSheet.createRow(faultNamesStartRow).createCell((short)0).setCellValue("Total Rate");
			
			currRow+=aFaultSources.size();
			// write totals
			row1 = predErrSheet.createRow((short)currRow++);
			row1.createCell((short)0).setCellValue("Totals");
			cell1 = row1.createCell((short)1); // totals for Char
			String colStr="B";
			cell1.setCellType(HSSFCell.CELL_TYPE_FORMULA);
			cell1.setCellFormula("SUM("+colStr+faultNamesStartRow+":"+colStr+(faultNamesStartRow+numA_Faults+")"));							
			// totals for other rates
			int totalOptions = (slipModelOptions.size()-1)*magAreaOptions.size()+1;
			
			for(int k=0; k<totalOptions; ++k) {
				cell1 = row1.createCell((short)(k+1));
				colStr=""+(char)('B'+k);
				//System.out.println(colStr);
				cell1.setCellType(HSSFCell.CELL_TYPE_FORMULA);
				cell1.setCellFormula("SUM("+colStr+faultNamesStartRow+":"+colStr+(faultNamesStartRow+numA_Faults+")"));
			}
		}
		
		
		int col;
		if(slipModelOptions.get(islip).equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL))  col = 1; 
		else col = 1 + (imag*(slipModelOptions.size()-1))+islip;				
		// write the Gen. Pred. Error
		for(int i=0; i<this.aFaultSources.size(); ++i) {
			A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
			for(int rupIndex=0; rupIndex<source.getNumRuptures(); ++rupIndex) totRate+=source.getRupRate(rupIndex);

			predErrSheet.getRow(i+faultNamesStartRow).createCell((short)col).setCellValue(source.getGeneralizedPredictionError());
		}			
		totRateSheet.getRow(faultNamesStartRow).createCell((short)col).setCellValue(totRate);
	}
	
	/**
	 * This generates some excel spreadsheet test results.  I should save the default parameters
	 *
	 */
	public void mkExcelSheetTests() {
		
		// Test 1 versus 2 - turn preserved min rates off in latter
		// Test #a versus #b - moment removed versus none removed, respectively
		
		
		// generate file for default parameters
		generateExcelSheetsForRupMagRates("Test1a_A_FaultRupRates_2_1.xls");
		
		// turn off preserve min rate (assuming default was true!!)
		preserveMinAFaultRateParam.setValue(false);
		generateExcelSheetsForRupMagRates("Test2a_A_FaultRupRates_2_1.xls");
		// turn it back on
		preserveMinAFaultRateParam.setValue(true);
		
		// change the moment removed to zero
		moRateFracToBackgroundParam.setValue(0.0);
		// now remove that which goes to aseismicity
		couplingCoeffParam.setValue(1.0);
		// finally, remove that which goes to aftershocks
		aftershockFractionParam.setValue(0.0);
		generateExcelSheetsForRupMagRates("Test1b_A_FaultRupRates_2_1.xls");
		
		// turn off preserve min rate
		preserveMinAFaultRateParam.setValue(false);
		generateExcelSheetsForRupMagRates("Test2b_A_FaultRupRates_2_1.xls");
		// turn it back on
		preserveMinAFaultRateParam.setValue(true);



	}
	
	// this is temporary for testing purposes
	public static void main(String[] args) {
		EqkRateModel2_ERF erRateModel2_ERF = new EqkRateModel2_ERF();
		//erRateModel2_ERF.findMinBulge();
		//erRateModel2_ERF.findMinBulge();
		//erRateModel2_ERF.generateExcelSheetsForRupMagRates("A_FaultRupRates_2_1.xls");
		//erRateModel2_ERF.generateExcelSheetForSegRecurIntv("A_FaultSegRecurIntv_2_1.xls");
		//erRateModel2_ERF.printMag6_5_discrepancies();
		//erRateModel2_ERF.makeMatlabNNLS_testScript();
		//erRateModel2_ERF.makeTotalRelativeGriddedRates();
		erRateModel2_ERF.mkExcelSheetTests();
		
	}
}
