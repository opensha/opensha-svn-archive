/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.MeanUCERF;


import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;

import java.util.StringTokenizer;



import org.opensha.calc.FaultMomentCalc;
import org.opensha.calc.MomentMagCalc;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.*;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.TimeSpan;
import org.opensha.data.ValueWeight;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.DiscretizedFuncList;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.data.region.EvenlyGriddedWG02_Region;
import org.opensha.data.region.GeographicRegion;
import org.opensha.exceptions.FaultException;
import org.opensha.param.*;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.B_FaultFixes;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.EmpiricalModel;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.A_Faults.A_FaultSegmentedSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis.GenerateTestExcelSheets;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis.ParamOptions;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.A_FaultsFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.B_FaultsFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.EventRates;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.NonCA_FaultsFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.SegmentTimeDepData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.UCERF1MfdReader;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.gui.A_FaultsMFD_Plotter;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.griddedSeis.NSHMP_GridSourceGenerator;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.magdist.*;
import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.surface.FrankelGriddedSurface;
import org.opensha.sha.surface.StirlingGriddedSurface;
import org.opensha.util.FileUtils;


/**
 * @author 
 *
 */
public class MeanUCERF2 extends EqkRupForecast {
	//for Debug purposes
	private static String  C = new String("MeanUCERF2");
	private boolean D = true;

	// name of this ERF
	public final static String NAME = new String("UCERF2 - Single Branch");

//	ArrayList allSourceNames;

	public final static double MIN_MAG = UCERF2.MIN_MAG;
	public final static double MAX_MAG = UCERF2.MAX_MAG;
	public final static double DELTA_MAG = UCERF2.DELTA_MAG;
	public final static int NUM_MAG = UCERF2.NUM_MAG;

	// public final static double B_FAULT_GR_MAG_LOWER = 6.5;
	public final static double BACKGROUND_MAG_LOWER = UCERF2.BACKGROUND_MAG_LOWER;
	
	// Fault Grid Spacing
	public final static double GRID_SPACING = UCERF2.GRID_SPACING;

	public final static double BACK_SEIS_DEPTH = UCERF2.BACK_SEIS_DEPTH;

	// various summed MFDs
	private SummedMagFreqDist bFaultSummedMFD, aFaultSummedMFD;
	private IncrementalMagFreqDist totBackgroundMFD, cZoneSummedMFD, nonCA_B_FaultsSummedMFD;

	// background seismicity inlcude/exclude param
	public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
	public final static String BACK_SEIS_INCLUDE = new String ("Include");
	public final static String BACK_SEIS_EXCLUDE = new String ("Exclude");
	public final static String BACK_SEIS_ONLY = new String ("Only Background");
	public final static String BACK_SEIS_INFO = new String ("Background includes C Zones here");
	private ArrayList backSeisOptionsStrings = new ArrayList();
	private StringParameter backSeisParam;

	// background seismicity treated as param
	public final static String BACK_SEIS_RUP_NAME = new String ("Treat Background Seismicity As");
	public final static String BACK_SEIS_RUP_POINT = new String ("Point Sources");
	//public final static String BACK_SEIS_RUP_FINITE = new String ("Finite Sources");
	private ArrayList backSeisRupStrings = new ArrayList();
	private StringParameter backSeisRupParam;

	// For rupture offset lenth along fault parameter
	public final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
	private Double DEFAULT_RUP_OFFSET_VAL= new Double(10);
	private final static String RUP_OFFSET_PARAM_UNITS = "km";
	private final static String RUP_OFFSET_PARAM_INFO = "Length of offset for floating ruptures";
	public final static double RUP_OFFSET_PARAM_MIN = 1;
	public final static double RUP_OFFSET_PARAM_MAX = 100;
	private DoubleParameter rupOffsetParam;

	
	// Probability Model Param
	public final static String PROB_MODEL_PARAM_NAME = "Probability Model";
	private final static String PROB_MODEL_PARAM_INFO = "Probability Model for Time Dependence";
	public final static String PROB_MODEL_POISSON = "Poisson";
	public final static String PROB_MODEL_BPT = "BPT";
	public final static String PROB_MODEL_EMPIRICAL = "Empirical";
	public final static String PROB_MODEL_WGCEP_PREF_BLEND = "WGCEP Preferred Blend";
	public final static String PROB_MODEL_DEFAULT = PROB_MODEL_WGCEP_PREF_BLEND;
	private StringParameter probModelParam;

	
	// Time duration
	private final static double DURATION_DEFAULT = 30;
	private final static double DURATION_MIN = 1;
	private final static double DURATION_MAX = 100;

	//start time
	private final static int START_TIME_DEFAULT = 2007;
	private final static int START_TIME_MIN = 2007;
	private final static int START_TIME_MAX = 2107;

	// 
	private EvenlyGriddedRELM_Region region = new EvenlyGriddedRELM_Region();

	private EmpiricalModel empiricalModel = new EmpiricalModel();

	
	private ArrayList<UnsegmentedSource> bFaultSources;
	private ArrayList<UnsegmentedSource> aFaultUnsegmentedSources;
	private ArrayList<FaultRuptureSource> aFaultSegmentedSources;
	private ArrayList<ProbEqkSource> allSources;
	private ArrayList<ProbEqkSource> backgroundSources; // includes C-Zones as well
	
	private ArrayList<String> aFaultsBranchParamNames; // parameters that are adjusted for A_Faults
	private ArrayList<ParamOptions> aFaultsBranchParamValues; // paramter values and their weights for A_Faults
	private int lastParamIndex;
	
	
	private HashMap<String, SummedMagFreqDist> sourceMFDMapping;
	private HashMap<String, Double> sourceRakeMapping;
	private HashMap<String, StirlingGriddedSurface> sourceGriddedSurfaceMapping;

	private NSHMP_GridSourceGenerator nshmp_gridSrcGen = new NSHMP_GridSourceGenerator();
	private UCERF2 ucerf2 = new UCERF2();
	private DeformationModelSummaryDB_DAO defModelSummaryDAO = new DeformationModelSummaryDB_DAO(DB_AccessAPI.dbConnection);


	/**
	 *
	 * No argument constructor
	 */
	public MeanUCERF2() {

		// create and add adj params
		initAdjParams();

		// set param defaults
		setParamDefaults();

		// put parameters in the parameter List object	
		createParamList();

//		create the timespan parameter, to allow the user to set the timespan to be
		//time independent or time dependent.
		setTimespanParameter();

		// add the change listener to parameters so that forecast can be updated
		// whenever any paramater changes
		//faultModelParam.addParameterChangeListener(this);
		rupOffsetParam.addParameterChangeListener(this);
		backSeisParam.addParameterChangeListener(this);
		backSeisRupParam.addParameterChangeListener(this);
		this.probModelParam.addParameterChangeListener(this);
		this.parameterChangeFlag = true;
		ucerf2.updateForecast();
	}

	/**
	 * This intializes the adjustable parameters
	 */
	private void initAdjParams() {

		// NOTE THAT VALUES SET IN THE CONSTRUCTORS ARE OVER RIDDEN BY CALLING THE setParamDefaults()
		// NETHOD AT THE END

		// background seismicity include/exclude  
		backSeisOptionsStrings.add(BACK_SEIS_EXCLUDE);
		backSeisOptionsStrings.add(BACK_SEIS_INCLUDE);
		backSeisOptionsStrings.add(BACK_SEIS_ONLY);
		backSeisParam = new StringParameter(BACK_SEIS_NAME, backSeisOptionsStrings,BACK_SEIS_INCLUDE);
		backSeisParam.setInfo(BACK_SEIS_INFO);
		
		// backgroud treated as point sources/finite sources
		backSeisRupStrings.add(BACK_SEIS_RUP_POINT);
		//backSeisRupStrings.add(BACK_SEIS_RUP_FINITE);
		backSeisRupParam = new StringParameter(BACK_SEIS_RUP_NAME, backSeisRupStrings,BACK_SEIS_RUP_POINT);


		// rup offset
		rupOffsetParam = new DoubleParameter(RUP_OFFSET_PARAM_NAME,RUP_OFFSET_PARAM_MIN,
				RUP_OFFSET_PARAM_MAX,RUP_OFFSET_PARAM_UNITS,DEFAULT_RUP_OFFSET_VAL);
		rupOffsetParam.setInfo(RUP_OFFSET_PARAM_INFO);


		// Probability Model Param
		ArrayList<String> probModelOptions = new ArrayList<String>();
		probModelOptions.add(this.PROB_MODEL_WGCEP_PREF_BLEND);
		probModelOptions.add(PROB_MODEL_POISSON);
		probModelOptions.add(PROB_MODEL_BPT);
		probModelOptions.add(PROB_MODEL_EMPIRICAL);
		probModelParam = new StringParameter(PROB_MODEL_PARAM_NAME, probModelOptions, PROB_MODEL_DEFAULT);
		probModelParam.setInfo(PROB_MODEL_PARAM_INFO);
	}


	// Set default value for parameters
	public void setParamDefaults() {
		backSeisParam.setValue(BACK_SEIS_INCLUDE);
		// backgroud treated as point sources/finite soource
		backSeisRupParam.setValue(BACK_SEIS_RUP_POINT);
		// rup offset
		rupOffsetParam.setValue(DEFAULT_RUP_OFFSET_VAL);
		probModelParam.setValue(PROB_MODEL_DEFAULT);

	}

	/**
	 * Put parameters in theParameterList
	 */
	private void createParamList() {
		adjustableParams = new ParameterList();
		adjustableParams.addParameter(rupOffsetParam);		
		adjustableParams.addParameter(backSeisParam);		
		adjustableParams.addParameter(backSeisRupParam);		
		adjustableParams.addParameter(probModelParam);		
	}


	/**
	 * Returns the  ith earthquake source
	 *
	 * @param iSource : index of the source needed
	 */
	public ProbEqkSource getSource(int iSource) {
		return allSources.get(iSource);
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


	public IncrementalMagFreqDist getTotal_B_FaultsMFD() {
		return this.bFaultSummedMFD;
	}

	public IncrementalMagFreqDist getTotal_NonCA_B_FaultsMFD() {
		return this.nonCA_B_FaultsSummedMFD;
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
		totalMFD.addIncrementalMagFreqDist(bFaultSummedMFD);
		totalMFD.addIncrementalMagFreqDist(aFaultSummedMFD);
		totalMFD.addIncrementalMagFreqDist(totBackgroundMFD);
		totalMFD.addIncrementalMagFreqDist(cZoneSummedMFD);
		totalMFD.addIncrementalMagFreqDist(nonCA_B_FaultsSummedMFD);
		return totalMFD;
	}

	/**
	 * update the forecast
	 **/

	public void updateForecast() {
		if(this.parameterChangeFlag)  {
			String backSeis = (String)backSeisParam.getValue();
			allSources = new ArrayList<ProbEqkSource>();
			if(cZoneSummedMFD == null) cZoneSummedMFD = ucerf2.getTotal_C_ZoneMFD();
			if(totBackgroundMFD==null) totBackgroundMFD = ucerf2.getTotal_BackgroundMFD();
			if(nonCA_B_FaultsSummedMFD==null) nonCA_B_FaultsSummedMFD = ucerf2.getTotal_NonCA_B_FaultsMFD();

			// if only background is not selected
			if(!backSeis.equalsIgnoreCase(BACK_SEIS_ONLY)) {
				mkA_FaultSources();
				allSources.addAll(this.aFaultSegmentedSources);
				allSources.addAll(this.aFaultUnsegmentedSources);
			
				mkB_FaultSources();
				allSources.addAll(this.bFaultSources);
				
				allSources.addAll(ucerf2.getNonCA_B_FaultSources());
			}
			if(backSeis.equalsIgnoreCase(BACK_SEIS_INCLUDE)) {
				if(backgroundSources ==null);
					backgroundSources = (this.nshmp_gridSrcGen.getAllGriddedSources(true, timeSpan.getDuration(), true, true));
				allSources.addAll(backgroundSources);
			}
			
		}
		parameterChangeFlag = false;
	}
	
	/**
	 * Make A_Fault Sources
	 *
	 */
	private void mkA_FaultSources() {
		
		// DO For Segmented sources
		fillAdjustableParamsForA_Faults();
		sourceMFDMapping = new HashMap<String, SummedMagFreqDist>();
		sourceRakeMapping = new  HashMap<String, Double> ();
		sourceGriddedSurfaceMapping = new HashMap<String, StirlingGriddedSurface>();
		findBranches(0,1);
		aFaultSegmentedSources = new ArrayList<FaultRuptureSource>();
		double duration = timeSpan.getDuration();
		aFaultSummedMFD = new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG); 
		// iterate over all rupture sources
		Iterator<String> it = sourceMFDMapping.keySet().iterator();
		while(it.hasNext()) {
			String name = it.next();
			aFaultSummedMFD.addIncrementalMagFreqDist(sourceMFDMapping.get(name));
			FaultRuptureSource faultRupSrc = new FaultRuptureSource(sourceMFDMapping.get(name), 
					sourceGriddedSurfaceMapping.get(name),
					sourceRakeMapping.get(name),
					duration);
			faultRupSrc.setName(name);
			//System.out.println("*******"+name+"\n"+sourceMFDMapping.get(name));
			aFaultSegmentedSources.add(faultRupSrc);
		}
		
		// make unsegmnted A-Fault Sources
		mkUnsegmentedA_FaultSources();
		
		
		
	}

	private void mkUnsegmentedA_FaultSources() {
		double rupOffset = ((Double)this.rupOffsetParam.getValue()).doubleValue();
		double empiricalModelWt=0.0;
		double duration = this.timeSpan.getDuration();
		String probModel = (String)this.probModelParam.getValue();
		if(probModel.equals(PROB_MODEL_BPT) || probModel.equals(PROB_MODEL_POISSON) ) empiricalModelWt = 0;
		else if(probModel.equals(PROB_MODEL_EMPIRICAL)) empiricalModelWt = 1;
		else if(probModel.equals(PROB_MODEL_WGCEP_PREF_BLEND)) empiricalModelWt = 0.3;

		// DO for unsegmented sources
		aFaultUnsegmentedSources = new ArrayList<UnsegmentedSource>();
		A_FaultsFetcher aFaultsFetcher = ucerf2.getA_FaultsFetcher();
		// get deformation model summaries
		DeformationModelSummary defModelSummary2_1 = defModelSummaryDAO.getDeformationModel("D2.1");
		DeformationModelSummary defModelSummary2_2 = defModelSummaryDAO.getDeformationModel("D2.2");
		DeformationModelSummary defModelSummary2_3 = defModelSummaryDAO.getDeformationModel("D2.3");
		
		double wt = 0.5;
		aFaultsFetcher.setDeformationModel(defModelSummary2_1, true);
		ArrayList<FaultSegmentData> faultSegmentList = aFaultsFetcher.getFaultSegmentDataList(true);
		ArrayList<Double> moRateList = new ArrayList<Double>();
		for(int i=0; i<faultSegmentList.size(); ++i)
			moRateList.add(wt*faultSegmentList.get(i).getTotalMomentRate());
		wt = 0.2;
		aFaultsFetcher.setDeformationModel(defModelSummary2_2, true);
		faultSegmentList = aFaultsFetcher.getFaultSegmentDataList(true);
		for(int i=0; i<faultSegmentList.size(); ++i) {
			double newMoRate = moRateList.get(i) + wt*faultSegmentList.get(i).getTotalMomentRate();
			moRateList.set(i, newMoRate);
		}
		wt = 0.3;
		aFaultsFetcher.setDeformationModel(defModelSummary2_3, true);
		faultSegmentList = aFaultsFetcher.getFaultSegmentDataList(true);
	
		for(int i=0; i<faultSegmentList.size(); ++i) {
			double newMoRate = moRateList.get(i) + wt*faultSegmentList.get(i).getTotalMomentRate();
			moRateList.set(i, newMoRate);
			UnsegmentedSource unsegmentedSource = new UnsegmentedSource(faultSegmentList.get(i),  
					empiricalModel, rupOffset, 0.0, 0.0,  1, empiricalModelWt,  duration, moRateList.get(i), 0);
			aFaultUnsegmentedSources.add(unsegmentedSource);
			//			System.out.println(source.getName());
			int numRups = unsegmentedSource.getNumRuptures();
			double mag, rate;
			for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
				ProbEqkRupture rup = unsegmentedSource.getRupture(rupIndex);
				mag = rup.getMag();
				rate = rup.getMeanAnnualRate(duration);
				aFaultSummedMFD.add(mag, 0.1*rate); // apply weight of unsegmented model
			}
		}
		
	}
	
	
	/**
	 * Calculate MFDs
	 * 
	 * @param paramIndex
	 * @param weight
	 */
	private void findBranches(int paramIndex, double weight) {
		ParamOptions options = this.aFaultsBranchParamValues.get(paramIndex);
		String paramName = this.aFaultsBranchParamNames.get(paramIndex);
		int numValues = options.getNumValues();
		for(int i=0; i<numValues; ++i) {
			double newWt;
			if(ucerf2.getAdjustableParameterList().containsParameter(paramName)) {
				ucerf2.getParameter(paramName).setValue(options.getValue(i));	
				newWt = weight * options.getWeight(i);
			} else {
				if(i==0) newWt=weight;
				else return;
			}
			if(paramIndex==lastParamIndex) { // if it is last paramter in list, make A_Faults
				mkA_FaultSegmentedSourceGenerators(newWt);
			} else { // recursion 
				findBranches(paramIndex+1, newWt);
			}
		}
	}
	
	
	private void mkA_FaultSegmentedSourceGenerators(double weight) {
		//System.out.println(weight);
		double relativeA_PrioriWeight = ((Double)ucerf2.getParameter(UCERF2.REL_A_PRIORI_WT_PARAM_NAME).getValue()).doubleValue();
		double relativeSegRateWeight = UCERF2.REL_SEG_RATE_WT_PARAM_DEFAULT;
		double magSigma  = UCERF2.MAG_SIGMA_DEFAULT;
		double magTruncLevel = UCERF2.TRUNC_LEVEL_DEFAULT;
		boolean isAseisReducesArea = true;
		double meanMagCorrection = UCERF2.MEAN_MAG_CORRECTION_DEFAULT;
		boolean wtedInversion = true;
		ParameterList rupModels = (ParameterList) (this.ucerf2.getParameter(UCERF2.SEGMENTED_RUP_MODEL_TYPE_NAME).getValue());

		
		A_FaultsFetcher aFaultsFetcher = ucerf2.getA_FaultsFetcher();
		DeformationModelSummary defModelSummary = defModelSummaryDAO.getDeformationModel((String)ucerf2.getParameter(UCERF2.DEFORMATION_MODEL_PARAM_NAME).getValue());
		aFaultsFetcher.setDeformationModel(defModelSummary, false);
		
		// this gets a list of FaultSegmentData objects (one for each A fault, and for the deformation model previously set)
		ArrayList aFaultSegmentData = aFaultsFetcher.getFaultSegmentDataList(isAseisReducesArea);

		double duration = timeSpan.getDuration();
		double startYear = Double.NaN, aperiodicity = Double.NaN;
		boolean isSegDependentAperiodicity = false;
		String probModel = (String)ucerf2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).getValue();
		
		if(probModel.equals(PROB_MODEL_BPT)) { // for time dependence
			startYear = this.timeSpan.getStartTimeYear();
			isSegDependentAperiodicity = false;
			aperiodicity = ((Double)ucerf2.getParameter(UCERF2.APERIODICITY_PARAM_NAME).getValue()).doubleValue();
		}
		
		ParameterAPI param = ucerf2.getParameter(UCERF2.REL_A_PRIORI_WT_PARAM_NAME);
		
		double minA_FaultRate1, minA_FaultRate2;
		if(((Double)param.getValue()).doubleValue()==1e10) {
			minA_FaultRate1 = 0.0;
			minA_FaultRate2 = 0.0;	
		} else {
			minA_FaultRate1 = UCERF2.MIN_A_FAULT_RATE_1_DEFAULT;
			minA_FaultRate2 = UCERF2.MIN_A_FAULT_RATE_2_DEFAULT;	
		}
		
		String slipModel = A_FaultSegmentedSourceGenerator.TAPERED_SLIP_MODEL;
		double totMoRateReduction = 0.1;
		
		for(int i=0; i<aFaultSegmentData.size(); ++i) {
			FaultSegmentData segmentData = (FaultSegmentData) aFaultSegmentData.get(i);
			ValueWeight[] aPrioriRates = aFaultsFetcher.getAprioriRupRates(segmentData.getFaultName(), (String)rupModels.getValue(segmentData.getFaultName()));

			// set the min-rate constraint and correct bogus, indicator rates in aPrioriRates
			double minRates[] = new double[aPrioriRates.length];
			double minRateFrac1 = minA_FaultRate1; // for unknown ruptures
			double minRateFrac2 = minA_FaultRate2; // for unlikely ruptures
			double minRate = Double.MAX_VALUE;
			for(int rup=0; rup<aPrioriRates.length; rup++) // find minimum, ignoring values less than zero which are indicators
				if(aPrioriRates[rup].getValue() < minRate && aPrioriRates[rup].getValue() >= 0) minRate = aPrioriRates[rup].getValue();
			for(int rup=0; rup<aPrioriRates.length; rup++) {
				double rate = aPrioriRates[rup].getValue();
				if(rate >= 0) minRates[rup] = minRate*minRateFrac1; // treat it as unknowns
				else if (rate == -1) {
					minRates[rup] = minRate*minRateFrac1;
					aPrioriRates[rup].setValue(0.0);   // over ride bogus indicator value with zero
				}
				else if (rate == -2) {
					minRates[rup] = minRate*minRateFrac2;
					aPrioriRates[rup].setValue(0.0);   // over ride bogus indicator value with zero
				}
				else 
					throw new RuntimeException("Problem with a-priori rates for fault "+segmentData.getFaultName());
//				System.out.println(rup+"  "+(float)minRates[rup]+"  "+segmentData.getFaultName());
			}

			A_FaultSegmentedSourceGenerator aFaultSourceGenerator = new A_FaultSegmentedSourceGenerator(segmentData, 
					ucerf2.getMagAreaRelationship(), slipModel, aPrioriRates, magSigma, 
					magTruncLevel, totMoRateReduction, meanMagCorrection,minRates, 
					wtedInversion, relativeSegRateWeight, relativeA_PrioriWeight);
			ArrayList<FaultRuptureSource> sources = new ArrayList<FaultRuptureSource>();
			if(probModel.equals(PROB_MODEL_POISSON)) // time Independent
				sources.addAll(aFaultSourceGenerator.getTimeIndependentSources(duration));
			else if(probModel.equals(PROB_MODEL_BPT)) 
				sources.addAll(aFaultSourceGenerator.getTimeDependentSources(duration, startYear, aperiodicity, isSegDependentAperiodicity));
			 else // Empirical Model
				sources.addAll(aFaultSourceGenerator.getTimeDepEmpiricalSources(duration, empiricalModel));
			
			String faultName = segmentData.getFaultName();
			for(int srcIndex=0; srcIndex<sources.size(); ++srcIndex) {
				FaultRuptureSource source  = sources.get(srcIndex);
				String key = faultName +";"+source.getName();
				if(!sourceMFDMapping.containsKey(key)) {
					sourceMFDMapping.put(key, new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG));
					sourceRakeMapping.put(key, aFaultSourceGenerator.getAveRake(srcIndex));
					this.sourceGriddedSurfaceMapping.put(key, aFaultSourceGenerator.getCombinedGriddedSurface(srcIndex));
				}
				SummedMagFreqDist mfd = sourceMFDMapping.get(key);
				int numRups = source.getNumRuptures();
				for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
					ProbEqkRupture rupture = source.getRupture(rupIndex);
					mfd.add(rupture.getMag(), rupture.getMeanAnnualRate(duration)*weight);
				}
			}
		}
	}
	
	
	/**
	 * Paramters that are adjusted in the runs
	 *
	 */
	private void fillAdjustableParamsForA_Faults() {
		if(ucerf2.getAdjustableParameterList().containsParameter(UCERF2.SEG_DEP_APERIODICITY_PARAM_NAME))
			ucerf2.getParameter(UCERF2.SEG_DEP_APERIODICITY_PARAM_NAME).setValue(new Boolean(false));
		this.aFaultsBranchParamNames = new ArrayList<String>();
		this.aFaultsBranchParamValues = new ArrayList<ParamOptions>();
		
		// Deformation model
		aFaultsBranchParamNames.add(UCERF2.DEFORMATION_MODEL_PARAM_NAME);
		ParamOptions options = new ParamOptions();
		options.addValueWeight("D2.1", 0.5);
		options.addValueWeight("D2.2", 0.2);
		options.addValueWeight("D2.3", 0.3);
		aFaultsBranchParamValues.add(options);
		
		// Mag Area Rel
		aFaultsBranchParamNames.add(UCERF2.MAG_AREA_RELS_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(Ellsworth_B_WG02_MagAreaRel.NAME, 0.5);
		options.addValueWeight(HanksBakun2002_MagAreaRel.NAME, 0.5);
		aFaultsBranchParamValues.add(options);
		
		// A-Fault solution type
		aFaultsBranchParamNames.add(UCERF2.RUP_MODEL_TYPE_NAME);
		options = new ParamOptions();
		options.addValueWeight(UCERF2.SEGMENTED_A_FAULT_MODEL, 0.9);
		aFaultsBranchParamValues.add(options);
		
		// Apriori wt param
		aFaultsBranchParamNames.add(UCERF2.REL_A_PRIORI_WT_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Double(1e-4), 0.5);
		options.addValueWeight(new Double(1e10), 0.5);
		aFaultsBranchParamValues.add(options);
		
		
		// Prob Model
		
		aFaultsBranchParamNames.add(UCERF2.PROB_MODEL_PARAM_NAME);
		options = new ParamOptions();
		// see the option chosen for Prob Model
		String probModel = (String)this.probModelParam.getValue();
		if(probModel.equals(PROB_MODEL_BPT)){
			options.addValueWeight(UCERF2.PROB_MODEL_BPT, 1.0);
		} else if (probModel.equals(PROB_MODEL_POISSON) ) {
			options.addValueWeight(UCERF2.PROB_MODEL_POISSON, 1.0);
		}
		else if(probModel.equals(PROB_MODEL_EMPIRICAL)) {
			options.addValueWeight(UCERF2.PROB_MODEL_EMPIRICAL, 1.0);
		}
		else if(probModel.equals(PROB_MODEL_WGCEP_PREF_BLEND)) {
			options.addValueWeight(UCERF2.PROB_MODEL_BPT, 0.7);
			options.addValueWeight(UCERF2.PROB_MODEL_EMPIRICAL, 0.3);
		}
		aFaultsBranchParamValues.add(options);
		
		//	BPT parameter setting
		aFaultsBranchParamNames.add(UCERF2.APERIODICITY_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Double(0.3), 0.2);
		options.addValueWeight(new Double(0.5), 0.5);
		options.addValueWeight(new Double(0.7), 0.3);
		aFaultsBranchParamValues.add(options);
		
		lastParamIndex = aFaultsBranchParamNames.size()-1;
	}


	
	/**
	 * Make B-Faults sources and caluculate B-Faults Total Summed MFD
	 */
	private void mkB_FaultSources() {
		A_FaultsFetcher aFaultsFetcher = ucerf2.getA_FaultsFetcher();
		B_FaultsFetcherForMeanUCERF bFaultsFetcher = new B_FaultsFetcherForMeanUCERF(aFaultsFetcher, true);
		bFaultSources = new ArrayList<UnsegmentedSource> ();
		double rupOffset = ((Double)this.rupOffsetParam.getValue()).doubleValue();
		double empiricalModelWt=0.0;
		
		String probModel = (String)this.probModelParam.getValue();
		if(probModel.equals(PROB_MODEL_BPT) || probModel.equals(PROB_MODEL_POISSON) ) empiricalModelWt = 0;
		else if(probModel.equals(PROB_MODEL_EMPIRICAL)) empiricalModelWt = 1;
		else if(probModel.equals(PROB_MODEL_WGCEP_PREF_BLEND)) empiricalModelWt = 0.3;
		
		double duration = this.timeSpan.getDuration();
		double wt = 0.5;
		
		ArrayList<FaultSegmentData> faultSegDataList = bFaultsFetcher.getB_FaultsCommonConnOpts();
		addToB_FaultSources(rupOffset, empiricalModelWt, duration, wt, faultSegDataList);
		
		wt=1.0;
		faultSegDataList  = bFaultsFetcher.getB_FaultsCommonNoConnOpts();
		addToB_FaultSources(rupOffset, empiricalModelWt, duration, wt, faultSegDataList);
		
		wt=0.25;
		faultSegDataList  = bFaultsFetcher.getB_FaultsUniqueToF2_1ConnOpts();
		addToB_FaultSources(rupOffset, empiricalModelWt, duration, wt, faultSegDataList);
		
		wt=0.5;
		faultSegDataList  = bFaultsFetcher.getB_FaultsUniqueToF2_1NoConnOpts();
		addToB_FaultSources(rupOffset, empiricalModelWt, duration, wt, faultSegDataList);
		
		wt=0.25;
		faultSegDataList  = bFaultsFetcher.getB_FaultsUniqueToF2_2ConnOpts();
		addToB_FaultSources(rupOffset, empiricalModelWt, duration, wt, faultSegDataList);
		
		wt=0.5;
		faultSegDataList  = bFaultsFetcher.getB_FaultsUniqueToF2_2NoConnOpts();
		addToB_FaultSources(rupOffset, empiricalModelWt, duration, wt, faultSegDataList);
		
		wt=0.75;
		faultSegDataList  = bFaultsFetcher.getB_FaultsCommonWithUniqueConnOpts();
		addToB_FaultSources(rupOffset, empiricalModelWt, duration, wt, faultSegDataList);
		
		// Now calculate the B-Faults total MFD
		bFaultSummedMFD= new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		
		double mag, rate;
		for(int srcIndex=0; srcIndex<bFaultSources.size(); ++srcIndex) {
			UnsegmentedSource source = bFaultSources.get(srcIndex);
			//System.out.println(source.getName());
			int numRups = source.getNumRuptures();
			for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
				ProbEqkRupture rup = source.getRupture(rupIndex);
				mag = rup.getMag();
				rate = rup.getMeanAnnualRate(duration);
				bFaultSummedMFD.add(mag, rate);
			}
		}
	}

	/**
	 * MAe sources from FaultSegmentData List and to bFaultList
	 * @param rupOffset
	 * @param empiricalModelWt
	 * @param duration
	 * @param wt
	 * @param faultSegDataList
	 */
	private void addToB_FaultSources(double rupOffset, double empiricalModelWt, double duration, double wt, ArrayList<FaultSegmentData> faultSegDataList) {
		for(int i=0; i<faultSegDataList.size(); ++i) {
			if(faultSegDataList.get(i).getFaultName().equalsIgnoreCase("Mendocino")) continue;
			//System.out.println(faultSegDataList.get(i).getFaultName()+"\t"+wt);
			bFaultSources.add(new UnsegmentedSource(faultSegDataList.get(i), 
					empiricalModel,  rupOffset,  wt, 
					empiricalModelWt, duration));
		}
	}
	

	/**
	 * Creates the timespan object based on if it is time dependent or time independent model.
	 */
	private void setTimespanParameter() {
		if (this.probModelParam.getValue().equals(PROB_MODEL_BPT) ||
				probModelParam.getValue().equals(PROB_MODEL_WGCEP_PREF_BLEND)) {
			// create the time-dep timespan object with start time and duration in years
			timeSpan = new TimeSpan(TimeSpan.YEARS, TimeSpan.YEARS);
			// set duration
			timeSpan.setDuractionConstraint(DURATION_MIN, DURATION_MAX);
			timeSpan.setDuration(DURATION_DEFAULT);
			// set the start year 
			timeSpan.setStartTimeConstraint(TimeSpan.START_YEAR, START_TIME_MIN, START_TIME_MAX);
			timeSpan.setStartTime(START_TIME_DEFAULT);

			timeSpan.addParameterChangeListener(this);
		}
		else {
			// create the time-ind timespan object with start time and duration in years
			timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
			timeSpan.setDuration(DURATION_DEFAULT);
			timeSpan.addParameterChangeListener(this);

		}
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
		if(paramName.equalsIgnoreCase(RUP_OFFSET_PARAM_NAME)) {
			
		} else if(paramName.equalsIgnoreCase(PROB_MODEL_PARAM_NAME)) {
			createParamList();
			setTimespanParameter();
			timeSpanChange(new EventObject(timeSpan));
		} else if (paramName.equalsIgnoreCase(BACK_SEIS_NAME)) {
			
		} else if(paramName.equalsIgnoreCase(BACK_SEIS_RUP_NAME)) { 

		} 
		parameterChangeFlag = true;
	}


	/**
	 *  
	 *
	 * @param  event  The Event which triggered this function call
	 */
	public void timeSpanChange(EventObject event) {
		parameterChangeFlag = true;
	}
	



	// this is temporary for testing purposes
	public static void main(String[] args) {
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(MeanUCERF2.PROB_MODEL_PARAM_NAME, MeanUCERF2.PROB_MODEL_POISSON);
		meanUCERF2.updateForecast();
		System.out.println(meanUCERF2.getTotal_A_FaultsMFD().getCumRateDistWithOffset());
		
		System.out.println(meanUCERF2.getTotal_B_FaultsMFD().getCumRateDistWithOffset());
		System.out.println(meanUCERF2.getTotal_C_ZoneMFD().getCumRateDistWithOffset());
		System.out.println(meanUCERF2.getTotal_NonCA_B_FaultsMFD().getCumRateDistWithOffset());
		System.out.println(meanUCERF2.getTotal_BackgroundMFD().getCumRateDistWithOffset());
		System.out.println(meanUCERF2.getTotalMFD().getCumRateDistWithOffset());
	}
}