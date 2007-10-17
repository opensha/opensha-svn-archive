/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.MeanUCERF;


import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EventObject;
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
	public final static int NUM_MAG = (int)Math.round((MAX_MAG-MIN_MAG)/DELTA_MAG) + 1;

	// public final static double B_FAULT_GR_MAG_LOWER = 6.5;
	public final static double BACKGROUND_MAG_LOWER = UCERF2.BACKGROUND_MAG_LOWER;
	
	// Fault Grid Spacing
	public final static double GRID_SPACING = UCERF2.GRID_SPACING;

	public final static double BACK_SEIS_DEPTH = UCERF2.BACK_SEIS_DEPTH;

	// various summed MFDs
	private SummedMagFreqDist bFaultSummedMFD, aFaultSummedMFD, cZoneSummedMFD, nonCA_B_FaultsSummedMFD;
	private IncrementalMagFreqDist totBackgroundMFD;

	/*
	 * Static variables for input files
	 */
	//private final static String IN_FILE_PATH = "/opt/install/apache-tomcat-5.5.20/webapps/OpenSHA/WEB-INF/dataFiles/frankel02_inputfiles/";
	private final static String IN_FILE_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/";
	private final static String NON_CA_SOURCES_FILENAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/data/NearCA_NSHMP/NonCA_Faults.txt";
	
	private ArrayList allSources;

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
	public final static String BACK_SEIS_RUP_FINITE = new String ("Finite Sources");
	private ArrayList backSeisRupStrings = new ArrayList();
	private StringParameter backSeisRupParam;

	// For rupture offset lenth along fault parameter
	public final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
	private Double DEFAULT_RUP_OFFSET_VAL= new Double(5);
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

	private ArrayList aFaultSourceGenerators; 
	private ArrayList<UnsegmentedSource> bFaultSources;
	private ArrayList<ProbEqkSource> nonCA_bFaultSources;
	

	private NSHMP_GridSourceGenerator nshmp_gridSrcGen = new NSHMP_GridSourceGenerator();
	private UCERF2 ucerf2 = new UCERF2();

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

		// backgroud treated as point sources/finite sources
		backSeisRupStrings.add(BACK_SEIS_RUP_POINT);
		backSeisRupStrings.add(BACK_SEIS_RUP_FINITE);
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
		if(iSource<allSources.size()) // everything but the grid sources
			return (ProbEqkSource) allSources.get(iSource);
		else {
			boolean bulgeReduction = true;
			boolean maxMagGrid = true;
			return nshmp_gridSrcGen.getGriddedSource(iSource - allSources.size(), true, timeSpan.getDuration(), bulgeReduction, maxMagGrid);
		}
	}

	/**
	 * Get the number of earthquake sources
	 *
	 * @return integer
	 */
	public int getNumSources(){
		return allSources.size() + nshmp_gridSrcGen.getNumSources();
	}
	

	/**
	 * Get the list of all earthquake sources.
	 *
	 * @return ArrayList of Prob Earthquake sources
	 */
	public ArrayList  getSourceList(){
		ArrayList sourceList = new ArrayList();
		sourceList.addAll(allSources);
		boolean bulgeReduction = true;
		boolean maxMagGrid = true;
		sourceList.addAll(this.nshmp_gridSrcGen.getAllGriddedSources(true, timeSpan.getDuration(), bulgeReduction, maxMagGrid));
		return sourceList;
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
		if(this.parameterChangeFlag) getB_FaultSources();
		parameterChangeFlag = false;
	}


	
	/**
	 * 
	 */
	private void getB_FaultSources() {
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
		
		ArrayList<FaultSegmentData> faultSegDataList = bFaultsFetcher.getB_FaultsCommonConnOpts();
		for(int i=0; i<faultSegDataList.size(); ++i) {
			bFaultSources.add(new UnsegmentedSource(faultSegDataList.get(i), empiricalModel,  rupOffset,  0.5, empiricalModelWt, duration));
		}
		
		faultSegDataList  = bFaultsFetcher.getB_FaultsCommonNoConnOpts();
		for(int i=0; i<faultSegDataList.size(); ++i) {
			if(faultSegDataList.get(i).getFaultName().equalsIgnoreCase("Mendocino")) continue;
			bFaultSources.add(new UnsegmentedSource(faultSegDataList.get(i), empiricalModel,  rupOffset,  1.0, empiricalModelWt, duration));
		}		
		
		faultSegDataList  = bFaultsFetcher.getB_FaultsUniqueToF2_1ConnOpts();
		for(int i=0; i<faultSegDataList.size(); ++i) {
			bFaultSources.add(new UnsegmentedSource(faultSegDataList.get(i), empiricalModel,  rupOffset,  0.25, empiricalModelWt, duration));
		}
		
		
		faultSegDataList  = bFaultsFetcher.getB_FaultsUniqueToF2_1NoConnOpts();
		for(int i=0; i<faultSegDataList.size(); ++i) {
			bFaultSources.add(new UnsegmentedSource(faultSegDataList.get(i), empiricalModel,  rupOffset,  0.5, empiricalModelWt, duration));
		}
		
		faultSegDataList  = bFaultsFetcher.getB_FaultsUniqueToF2_2ConnOpts();
		for(int i=0; i<faultSegDataList.size(); ++i) {
			bFaultSources.add(new UnsegmentedSource(faultSegDataList.get(i), empiricalModel,  rupOffset,  0.25, empiricalModelWt, duration));
		}
		
		faultSegDataList  = bFaultsFetcher.getB_FaultsUniqueToF2_2NoConnOpts();
		for(int i=0; i<faultSegDataList.size(); ++i) {
			bFaultSources.add(new UnsegmentedSource(faultSegDataList.get(i), empiricalModel,  rupOffset,  0.5, empiricalModelWt, duration));
		}
		
		faultSegDataList  = bFaultsFetcher.getB_FaultsCommonWithUniqueConnOpts();
		for(int i=0; i<faultSegDataList.size(); ++i) {
			bFaultSources.add(new UnsegmentedSource(faultSegDataList.get(i), empiricalModel,  rupOffset,  0.75, empiricalModelWt, duration));
		}
		
		// Now calculate the B-Faults total MFD
		bFaultSummedMFD= new SummedMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		
		double mag, rate;
		for(int srcIndex=0; srcIndex<bFaultSources.size(); ++srcIndex) {
			UnsegmentedSource source = bFaultSources.get(srcIndex);
			System.out.println(source.getName());
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
	 * Creates the timespan object based on if it is time dependent or time independent model.
	 */
	private void setTimespanParameter() {
		if (this.probModelParam.getValue().equals(PROB_MODEL_BPT)) {
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
		System.out.println(meanUCERF2.getTotal_B_FaultsMFD().getCumRateDistWithOffset());
	}
}