/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

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
import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.exceptions.FaultException;
import org.opensha.param.BooleanParameter;
import org.opensha.param.DoubleParameter;
import org.opensha.param.StringParameter;
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
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.fetchers.A_FaultsFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.fetchers.B_FaultsFetcher;
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

//	  ArrayList allSourceNames;

	  private String CHAR_MAG_FREQ_DIST = "1";
	  private String GR_MAG_FREQ_DIST = "2";
	  private String FAULTING_STYLE_SS = "1";
	  private String FAULTING_STYLE_R = "2";
	  private String FAULTING_STYLE_N = "3";
	  
	  public final static double MIN_MAG = 5;
	  public final static double MAX_MAG = 9;
	  public final static double DELTA_MAG = 0.1;
	  public final static int NUM_MAG = (int)Math.round((MAX_MAG-MIN_MAG)/DELTA_MAG) + 1;
	  public final static double B_FAULT_GR_B_VALUE = 1.0;
	  public final static double B_FAULT_GR_MAG_LOWER = 6.5;
	  public final static double BACKGROUND_MAG_LOWER = 5.0;
	  public final static double BACKGROUND_B_VALUE = 1.0;

	  public final static double BACK_SEIS_DEPTH = 5.0;
	  
	  // various summed MFDs
	  private SummedMagFreqDist bFaultCharSummedMFD, bFaultGR_SummedMFD, aFaultSummedMFD, cZoneSummedMFD;
	  private GutenbergRichterMagFreqDist totBackgroundMFD;

	  /*
	   * Static variables for input files
	   */
	  //private final static String IN_FILE_PATH = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/frankel02_inputfiles/";
	  private final static String IN_FILE_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF2/";


	  /**
	   * Vectors for holding the various sources, separated by type
	   */
	  private ArrayList typeA_FaultSources;
	  private ArrayList typeB_FaultCharSources;
	  private ArrayList typeB_FaultGR_Sources;
	  
	  private ArrayList allSources;

	  private MagAreaRelationship magAreaRel;

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
	  public final static String BACK_SEIS_MAG_NAME = "Backgroud Seismicity Mmax";
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
	  private final static String TOT_MAG_RATE_PARAM_NAME = "Total M>=5 Rate";
	  public final static Double TOT_MAG_RATE_MIN = new Double(2.0);
	  public final static Double TOT_MAG_RATE_MAX = new Double(20.0);
	  public final static Double TOT_MAG_RATE_DEFAULT = new Double(7.5);
	  private final static String TOT_MAG_RATE_INFO = "Total regional rate of M>=5 events in the RELM test region (set as 4.05 for no aftershocks, or 7.5 including aftershocks)";
	  private DoubleParameter totalMagRateParam ;
	  
	  //choose mag area relationship
	  private final static String MAG_AREA_RELS_PARAM_NAME = "Mag-Area Relationship";
	  private final static String MAG_AREA_RELS_PARAM_INFO = "Mag-Area Relationship for mean mag of characteristic events";
	  private StringParameter magAreaRelParam;
	  private ArrayList magAreaRelationships;
	  
	  // choose deformation model
	  private final static String DEFORMATION_MODEL_PARAM_NAME = "Deformation Model";
	  private StringParameter deformationModelsParam;
	  private DeformationModelSummaryDB_DAO deformationModelSummaryDB_DAO = new DeformationModelSummaryDB_DAO(DB_AccessAPI.dbConnection);
	  private ArrayList deformationModelsList;
	  
	  // aseismic factor interpolated
	  public final static String ASEIS_INTER_PARAM_NAME = "Aseis Factor reduces section area";
	  private final static String ASEIS_INTER_PARAM_INFO = "Otherwise it reduces section slip rate";
	  private BooleanParameter aseisFactorInterParam; 
	 
	 
	  //	 rupture model type
	  private final static String RUP_MODEL_TYPE = "A Fault Model Type";
	  private final static String UNSEGMENTED_A_FAULT_MODEL = "Unsegmented A-Fault Model";
	  private StringParameter rupModelParam;
	 
	  // % char vs GR param
	  private final static String CHAR_VS_GR_PARAM_NAME = "% Char vs GR";
	  private final static Double CHAR_VS_GR_MIN = new Double(.0);
	  private final static Double CHAR_VS_GR_MAX = new Double(100.0);
	  private final static Double CHAR_VS_GR_DEFAULT = new Double(50.0);
	  private final static String CHAR_VS_GR_INFO = "The % moment rate put into characteristic (vs GR) events on B Faults (and A faults for un-segmented option)";
	  private DoubleParameter percentCharVsGRParam; 
	  
	  // 
	  private double[] totalRelativeGriddedRates;
	  private EvenlyGriddedRELM_Region region = new EvenlyGriddedRELM_Region();
	  
	  
	  // char mag sigma >=0 and <=1
	  private final static String MAG_SIGMA_PARAM_NAME = "Mag Sigma";
	  private final static Double MAG_SIGMA_MIN = new Double(0.0);
	  private final static Double MAG_SIGMA_MAX = new Double(1.0);
	  private final static Double MAG_SIGMA_DEFAULT = new Double(0.12);
	  private final static String MAG_SIGMA_INFO = "Standard Deviation for characteristic events";
	  private DoubleParameter magSigmaParam;
	  
	  // Char mag trunc level (units is number of sigmas) >=0 and <=6
	 // Mag truncation level
	  private final static String TRUNC_LEVEL_PARAM_NAME = "Truncation Level";
	  private final static Double TRUNC_LEVEL_MIN = new Double(0.0);
	  private final static Double TRUNC_LEVEL_MAX = new Double(6.0);
	  private final static Double TRUNC_LEVEL_DEFAULT = new Double(2.0);
	  private final static String TRUNC_LEVEL_INFO = "Truncation Level (Number of sigmas)";
	  private DoubleParameter truncLevelParam;
	  
	  // A and B faults fetcher
	  private A_FaultsFetcher aFaultsFetcher = new A_FaultsFetcher();
	  private B_FaultsFetcher bFaultsFetcher = new B_FaultsFetcher();
	  private  ArrayList bFaultSegmentData;
	  private double[] b_FaultMeanCharMag, b_FaultTotRate;
	  private ArrayList aFaultSources;
	  
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

	  }
	  

//	 make the adjustable parameters & the list
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
	    this.totalMagRateParam = new DoubleParameter(TOT_MAG_RATE_PARAM_NAME, TOT_MAG_RATE_MIN,
	    		TOT_MAG_RATE_MAX, TOT_MAG_RATE_DEFAULT);
	    totalMagRateParam.setInfo(TOT_MAG_RATE_INFO);
	    
	    // % char vs GR param
	    percentCharVsGRParam = new DoubleParameter(CHAR_VS_GR_PARAM_NAME, CHAR_VS_GR_MIN,
	    		CHAR_VS_GR_MAX, CHAR_VS_GR_DEFAULT);
	    totalMagRateParam.setInfo(CHAR_VS_GR_INFO);
	    
	    // aseis factor param
	    aseisFactorInterParam = new BooleanParameter(ASEIS_INTER_PARAM_NAME, new Boolean(true));
		aseisFactorInterParam.setInfo(ASEIS_INTER_PARAM_INFO);
		
		//		 make objects of Mag Area Relationships
		 magAreaRelationships = new ArrayList();
		magAreaRelationships.add(new Ellsworth_A_WG02_MagAreaRel() );
		magAreaRelationships.add(new Ellsworth_B_WG02_MagAreaRel());
		magAreaRelationships.add(new HanksBakun2002_MagAreaRel());
		magAreaRelationships.add(new Somerville_2006_MagAreaRel());
		magAreaRelationships.add(new WC1994_MagAreaRelationship());
		// array List of Mag Area Rel names
		ArrayList magAreaNamesList = new ArrayList();
		for(int i=0; i<magAreaRelationships.size(); ++i)
			magAreaNamesList.add(((MagAreaRelationship)magAreaRelationships.get(i)).getName());
		magAreaRelParam = new StringParameter(MAG_AREA_RELS_PARAM_NAME, magAreaNamesList, (String)magAreaNamesList.get(0));
		magAreaRelParam.setInfo(MAG_AREA_RELS_PARAM_INFO);
		
		
	    // deformation model param
	    deformationModelsList = this.deformationModelSummaryDB_DAO.getAllDeformationModels();
		// make a list of deformation model names
		ArrayList deformationModelNames = new ArrayList();
		for(int i=0; i<deformationModelsList.size(); ++i) {
			deformationModelNames.add(((DeformationModelSummary)deformationModelsList.get(i)).getDeformationModelName());
		}
		deformationModelsParam = new StringParameter(DEFORMATION_MODEL_PARAM_NAME,deformationModelNames, (String)deformationModelNames.get(0) );

		// A-Fault model type
		ArrayList rupModels = new ArrayList();
		rupModels.add(A_FaultsFetcher.MIN_RATE_RUP_MODEL);
		rupModels.add(A_FaultsFetcher.MAX_RATE_RUP_MODEL);
		rupModels.add(A_FaultsFetcher.GEOL_INSIGHT_RUP_MODEL);
		rupModels.add(UNSEGMENTED_A_FAULT_MODEL);
		rupModelParam = new StringParameter(RUP_MODEL_TYPE, rupModels, (String)rupModels.get(0));

		
		// mag Sigma Param
		magSigmaParam = new DoubleParameter(MAG_SIGMA_PARAM_NAME, MAG_SIGMA_MIN, MAG_SIGMA_MAX,
				MAG_SIGMA_DEFAULT);
		magSigmaParam.setInfo(MAG_SIGMA_INFO);
		
		// trunc level
		truncLevelParam = new DoubleParameter(TRUNC_LEVEL_PARAM_NAME, TRUNC_LEVEL_MIN, TRUNC_LEVEL_MAX,
				TRUNC_LEVEL_DEFAULT);
		truncLevelParam.setInfo(TRUNC_LEVEL_INFO);
		
	    //	 add adjustable parameters to the list
	    // adjustableParams.addParameter(faultModelParam);
	    adjustableParams.addParameter(rupOffset_Param);
	    adjustableParams.addParameter(totalMagRateParam);
	    adjustableParams.addParameter(deformationModelsParam);
	    adjustableParams.addParameter(magAreaRelParam);
	    adjustableParams.addParameter(aseisFactorInterParam);
	    adjustableParams.addParameter(rupModelParam);
	    adjustableParams.addParameter(percentCharVsGRParam);
	    adjustableParams.addParameter(magSigmaParam);
	    adjustableParams.addParameter(truncLevelParam);
	    adjustableParams.addParameter(backSeisParam);
	    adjustableParams.addParameter(backSeisMaxMagParam);
	  }
	  
		
		/**
		 * Get the Id of the selected deformation model
		 * @return
		 */
		private int getSelectedDeformationModelId() {
			String selectedDefModel  = (String)this.deformationModelsParam.getValue();
			for(int i=0; i<this.deformationModelsList.size(); ++i) {
				DeformationModelSummary deformationModel = (DeformationModelSummary)deformationModelsList.get(i);
				if(deformationModel.getDeformationModelName().equalsIgnoreCase(selectedDefModel)) {
					return deformationModel.getDeformationModelId();
				}
			}
			return -1;
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
	    
	    
	    private void  makeBackgroundGridSources() {
	    	
	    	//MagAreaRelationship magAreaRel = this.getMagAreaRelationship();
	    	
	    	// get the total rate of M³5 events
	    	double rate = ((Double)this.totalMagRateParam.getValue()).doubleValue();
	    	double magMax = ((Double)backSeisMaxMagParam.getValue()).doubleValue();
	    	
	    	// now subtract the A, B, & C fault/zone rates
	    	rate -= this.bFaultCharSummedMFD.getTotalIncrRate();
	    	rate -= this.bFaultGR_SummedMFD.getTotalIncrRate();
	    	rate -= this.aFaultSummedMFD.getTotalIncrRate();
	    	
	    	totBackgroundMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG,
                    BACKGROUND_MAG_LOWER, magMax, 1, BACKGROUND_B_VALUE);
	    	totBackgroundMFD.scaleToCumRate(0, rate);
	    }
	    
	    private void makeC_ZoneSources() {
	    	this.cZoneSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
	    }
	    
	    private void mkA_FaultSegmentedSources() {
	    	double magSigma  = ((Double)this.magSigmaParam.getValue()).doubleValue();
	    	double magTruncLevel = ((Double)this.truncLevelParam.getValue()).doubleValue();
	    	String rupModel = (String)this.rupModelParam.getValue();
	    	int deformationModelId = this.getSelectedDeformationModelId();
	    	boolean isAseisReducesArea = ((Boolean)this.aseisFactorInterParam.getValue()).booleanValue();
	    	ArrayList aFaultSegmentData = this.aFaultsFetcher.getFaultSegmentDataList(deformationModelId, 
	    			isAseisReducesArea);
	    	aFaultSources = new ArrayList();
	    	aFaultSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
	    	for(int i=0; i<aFaultSegmentData.size(); ++i) {
	    		FaultSegmentData segmentData = (FaultSegmentData) aFaultSegmentData.get(i);
	    		ValueWeight[] aPrioriRates = aFaultsFetcher.getAprioriRupRates(segmentData.getFaultName(), rupModel);
	    		A_FaultSegmentedSource aFaultSource = new A_FaultSegmentedSource(segmentData, aPrioriRates, 
	    				magSigma, magTruncLevel);
	    		aFaultSources.add(aFaultSource);
	    		aFaultSummedMFD.addIncrementalMagFreqDist(aFaultSource.getTotalRupMFD());
	    	}
	    }
	    
	    
	    /**
	     * This is a quick fix.  We should really use our A_FaultFloatingSource since it has a lot of
	     * other capabilities.
	     *
	     */
	    private void mkA_FaultUnsegmentedSources() {
	    	double magSigma  = ((Double)this.magSigmaParam.getValue()).doubleValue();
	    	double magTruncLevel = ((Double)this.truncLevelParam.getValue()).doubleValue();
	    	double fractCharVsGR= ((Double)this.percentCharVsGRParam.getValue()).doubleValue()/100.0;
	    	MagAreaRelationship magAreaRel = this.getMagAreaRelationship();
	    	int deformationModelId = this.getSelectedDeformationModelId();
	    	boolean isAseisReducesArea = ((Boolean)this.aseisFactorInterParam.getValue()).booleanValue();
	    	ArrayList aFaultSegmentData = this.aFaultsFetcher.getFaultSegmentDataList(deformationModelId, 
	    			isAseisReducesArea);
	    	double meanCharMag, moRate;
	    	aFaultSources = null;
	    	aFaultSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
	    	for(int i=0; i<aFaultSegmentData.size(); ++i) {
	    		FaultSegmentData segmentData = (FaultSegmentData)aFaultSegmentData.get(i);
	    		meanCharMag = magAreaRel.getMedianMag(segmentData.getTotalArea()/1e6);  // this area is reduced by aseis if appropriate
	    		meanCharMag = Math.round(meanCharMag/DELTA_MAG) * DELTA_MAG;
	    		moRate = segmentData.getTotalMomentRate();
	    		
	    		
	    		// only apply char if mag <= lower RG mag 
	    		if(meanCharMag <= B_FAULT_GR_MAG_LOWER) {
	    			GaussianMagFreqDist charMFD = new GaussianMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG, 
		    				meanCharMag, magSigma, moRate, magTruncLevel, 2);
		    		aFaultSummedMFD.addIncrementalMagFreqDist(charMFD);
	    		}
	    		else {
//	    			make char dist 
	    			GaussianMagFreqDist charMFD = new GaussianMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG, 
		    				meanCharMag, magSigma, moRate*fractCharVsGR, magTruncLevel, 2);
		    		aFaultSummedMFD.addIncrementalMagFreqDist(charMFD);
		    		GutenbergRichterMagFreqDist grMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG,
	                        B_FAULT_GR_MAG_LOWER, meanCharMag, moRate*(1-fractCharVsGR), B_FAULT_GR_B_VALUE);
		    		aFaultSummedMFD.addIncrementalMagFreqDist(grMFD);
	    		}
	    		
 	    	}
	    }
	    
	    private void mkB_FaultSources() {
	    	double magSigma  = ((Double)this.magSigmaParam.getValue()).doubleValue();
	    	double magTruncLevel = ((Double)this.truncLevelParam.getValue()).doubleValue();
	    	double fractCharVsGR= ((Double)this.percentCharVsGRParam.getValue()).doubleValue()/100.0;
	    	MagAreaRelationship magAreaRel = this.getMagAreaRelationship();
	    	int deformationModelId = this.getSelectedDeformationModelId();
	    	boolean isAseisReducesArea = ((Boolean)this.aseisFactorInterParam.getValue()).booleanValue();
	    	 bFaultSegmentData = this.bFaultsFetcher.getFaultSegmentDataList(deformationModelId, 
	    			isAseisReducesArea);
	    	double meanCharMag, moRate;
//	    	ArrayList B_faultCharMFDs = new ArrayList();
//	    	ArrayList B_faultGR_MFDs = new ArrayList();
	    	bFaultCharSummedMFD= new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
	    	bFaultGR_SummedMFD= new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
	    	b_FaultMeanCharMag = new double[bFaultSegmentData.size()];
	    	b_FaultTotRate = new double[bFaultSegmentData.size()];
	    	for(int i=0; i<bFaultSegmentData.size(); ++i) {
	    		FaultSegmentData segmentData = (FaultSegmentData)bFaultSegmentData.get(i);
	    		meanCharMag = magAreaRel.getMedianMag(segmentData.getTotalArea()/1e6);  // this area is reduced by aseis if appropriate
	    		meanCharMag = Math.round(meanCharMag/DELTA_MAG) * DELTA_MAG;
	    		moRate = segmentData.getTotalMomentRate();
	    		// only apply char if mag <= lower RG mag 
	    		if(meanCharMag <= B_FAULT_GR_MAG_LOWER) {
	    			GaussianMagFreqDist charMFD = new GaussianMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG, 
	    					meanCharMag, magSigma, moRate, magTruncLevel, 2);
		    		bFaultCharSummedMFD.addIncrementalMagFreqDist(charMFD);
		    		b_FaultTotRate[i]=charMFD.getTotalIncrRate();
	    		}
	    		else {
//	    			make char dist 
	    			GaussianMagFreqDist charMFD = new GaussianMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG, 
	    					meanCharMag, magSigma, moRate*fractCharVsGR, magTruncLevel, 2);
	    			bFaultCharSummedMFD.addIncrementalMagFreqDist(charMFD);
//	    			B_faultCharMFDs.add(charMFD);
	    			// make GR dist
	    			GutenbergRichterMagFreqDist grMFD = new GutenbergRichterMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG,
	    					B_FAULT_GR_MAG_LOWER, meanCharMag, moRate*(1-fractCharVsGR), B_FAULT_GR_B_VALUE);
	    			bFaultGR_SummedMFD.addIncrementalMagFreqDist(grMFD);
//	    			B_faultGR_MFDs.add(grMFD);
	    			b_FaultTotRate[i]=charMFD.getTotalIncrRate()+grMFD.getTotalIncrRate();
	    		}
	    		b_FaultMeanCharMag[i] = meanCharMag;
 	    	}
	    }
	    
	    
	    /**
	     * Get a List of aFaultSegmentData objects. It returns null in case of unsegmented model
	     * 
	     * @return
	     */
	    public ArrayList get_A_FaultSources() {
	    	return this.aFaultSources;
	    }
	    
	    /**
	     * Return an  Arraylist of FaultSegmentData objects for Type-B faults
	     * 
	     * NOTE - this is temporary until we have a B_FaultSource object implemented
	     * @return
	     */
	    public ArrayList getB_FaultSegmentDataList() {
	    	return bFaultSegmentData;
	    }
	    
	    /**
	     * Get mean Char mag for B Fault
	     * 
	     * NOTE - this is temporary until we have a B_FaultSource object implemented
	     * 
	     * @param index
	     * @return
	     */
	    public double get_B_FaultMag(int index) {
	    	return b_FaultMeanCharMag[index];
	    }
	    
	    /**
	     * Get Total Rate for B-type Fault
	     * 
	     * NOTE - this is temporary until we have a B_FaultSource object implemented
	     * @param index
	     * @return
	     */
	    public double get_B_FaultTotRate(int index) {
	    	return b_FaultTotRate[index];
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

	   
	   
	   public IncrementalMagFreqDist getTotal_B_FaultsCharMFD() {
		   return this.bFaultCharSummedMFD;
	   }
	   
	   public IncrementalMagFreqDist getTotal_B_FaultsGR_MFD() {
		   return this.bFaultGR_SummedMFD;  
	   } 
	   
	   public IncrementalMagFreqDist getTotal_A_FaultsMFD() {
		   return this.aFaultSummedMFD;
	   }
	   
	   public IncrementalMagFreqDist getTotal_BackgroundMFD() {
		   return this.totBackgroundMFD;
	   }
	   
	   public IncrementalMagFreqDist getTotal_C_ZoneMFD() {
		   return this.cZoneSummedMFD;
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
		 String rupModel = (String)this.rupModelParam.getValue();
		 System.out.println("Creating A Fault sources");
		 long time1 = System.currentTimeMillis();
		 if(rupModel.equalsIgnoreCase(UNSEGMENTED_A_FAULT_MODEL)) 
			 mkA_FaultUnsegmentedSources();
		 else 
			 mkA_FaultSegmentedSources();
		 long time2 = System.currentTimeMillis();
		 System.out.println("Creating B Fault sources. Time Spent in creating A Fault Sources="+(time2-time1)/1000+" sec");
		 this.mkB_FaultSources();
		 long time3 = System.currentTimeMillis();
		 System.out.println("Creating Background sources. Time Spent in creating B Fault Sources="+(time3-time2)/1000+" sec");
		 // makeTotalRelativeGriddedRates();
		 makeBackgroundGridSources();
		 long time4 = System.currentTimeMillis();
		 System.out.println("Creating C Zone Fault sources. Time Spent in creating background Sources="+(time4-time3)/1000+" sec");
		 // Make C Zone MFD
		 makeC_ZoneSources();
		 long time5 = System.currentTimeMillis();
		 System.out.println("Done. Time Spent in creating C Zone Sources="+(time5-time4)/1000+" sec");
  
		   /* OLD STUFF BELOW
	     // make sure something has changed
	     if(parameterChangeFlag) {

	       // get value of background seismicity paramter
	       String backSeis = (String) backSeisParam.getValue();

	       allSources = new ArrayList();

	       if (backSeis.equalsIgnoreCase(BACK_SEIS_INCLUDE)) {
	         makeAllFaultSources();
	         makeBackgroundGridSources();
	         // now create the allSources list:
	         allSources.addAll(typeA_FaultSources);
	         allSources.addAll(typeB_FaultCharSources);
	         allSources.addAll(typeB_FaultGR_Sources);
//	         allSources.addAll(this.backgrSeisSources);

	       }
	       else if (backSeis.equalsIgnoreCase(BACK_SEIS_EXCLUDE)) {
	         // now create the allSources list:
	         makeAllFaultSources();
	         allSources.addAll(typeA_FaultSources);
	         allSources.addAll(typeB_FaultCharSources);
	         allSources.addAll(typeB_FaultGR_Sources);
	       }
	       else {// only background sources
	    	   makeBackgroundGridSources();
	        // now create the allSources list:
	        allSources.addAll(this.backgrSeisSources);
	       }

	       parameterChangeFlag = false;
	     }
	     */

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

	     /**
	      * If change is made to the Back Seis param then
	      * remove/add the backSeisParam from the list of
	      * adjustable parameters and send that event to
	      * listening class for the changes in the
	      * parameter list.
	      */
	     if(paramName.equals(BACK_SEIS_NAME)){
	       String paramValue = (String)event.getNewValue();
	       if(paramValue.equals(BACK_SEIS_EXCLUDE)){
	         if(adjustableParams.containsParameter(backSeisRupParam))
	           adjustableParams.removeParameter(backSeisRupParam);
	         if(adjustableParams.containsParameter(this.backSeisMaxMagParam))
		           adjustableParams.removeParameter(backSeisMaxMagParam);
	       }
	       else{
	         //only add the parameter in the parameter list if it does not already exists
	         if(!adjustableParams.containsParameter(backSeisRupParam)){
	           adjustableParams.addParameter(backSeisRupParam);
	         if(!adjustableParams.containsParameter(this.backSeisMaxMagParam))
		           adjustableParams.addParameter(backSeisMaxMagParam);
	         }
	       }

	     }
	  }


	   // this is temporary for testing purposes
	   public static void main(String[] args) {
		   EqkRateModel2_ERF erRateModel2_ERF = new EqkRateModel2_ERF();
		   erRateModel2_ERF.makeTotalRelativeGriddedRates();
	   
	  }
}
