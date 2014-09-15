package scratch.UCERF3.erf.ETAS;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;
import java.util.zip.ZipException;

import javax.swing.JOptionPane;

import org.dom4j.DocumentException;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.region.CaliforniaRegions.RELM_TESTING_GRIDDED;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.impl.ParameterListEditor;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.LocationParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.AbstractNthRupERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupOrigTimeComparator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.parsers.UCERF3_CatalogParser;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BPTAveragingTypeOptions;
import org.opensha.sha.earthquake.param.BPTAveragingTypeParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.FaultGridSpacingParam;
import org.opensha.sha.earthquake.param.HistoricOpenIntervalParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityOptions;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.GriddedSubsetSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.utils.GriddedSurfaceUtils;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.analysis.FaultSysSolutionERF_Calc;
import scratch.UCERF3.analysis.FaultSystemSolutionCalc;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools.EpicenterMapThread;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_ParameterList;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class ETAS_Simulator {
	
	public static boolean D=true; // debug flag
	private static boolean live_map = true;
	static boolean pause_for_events = false;
	
	/**
	 * This represents an ETAS simulation.  
	 * 
	 * This assume ER probabilities are constant up until 
	 * the next fault-system event (only works if fault system events occur every few years or
	 * less).
	 * 
	 * TODO:
	 * 
	 * 0) get rid of ETAS_EqkRupture.setParentID() and the parentID field within (since pointer to parent rupture exists)
	 * 1) define tests for all code elements
	 * 2) document all the code
	 * 
	 * 
	 * @param resultsDir directory where results will be writer
	 * @param griddedRegion
	 * @param obsEqkRuptureList
	 * @param includeSpontEvents
	 * @param includeIndirectTriggering - include secondary, tertiary, etc events
	 * @param includeEqkRates - whether or not to include the long-term rate of events in sampling aftershocks
	 * @param gridSeisDiscr - lat lon discretization of gridded seismicity (degrees)
	 * @param simulationName 
	 * @param randomSeed - set for reproducibility, or set null if new seed desired
	 * @throws IOException 

	 */
	public static void testETAS_Simulation(File resultsDir, AbstractNthRupERF erf,
			GriddedRegion griddedRegion, ETAS_EqkRupture scenarioRup, List<? extends ObsEqkRupture> histQkList, boolean includeSpontEvents,
			boolean includeIndirectTriggering, boolean includeEqkRates, double gridSeisDiscr, String simulationName,
			Long randomSeed, ETAS_ParameterList etasParams)
					throws IOException {
		testETAS_Simulation(resultsDir, erf, griddedRegion, scenarioRup,  histQkList, includeSpontEvents,
				includeIndirectTriggering, includeEqkRates, gridSeisDiscr, simulationName,
				randomSeed, null, null, null, etasParams);
	}
	
	/**
	 * This represents an ETAS simulation.  
	 * 
	 * This assume ER probabilities are constant up until 
	 * the next fault-system event (only works if fault system events occur every few years or
	 * less).
	 * 
	 * What is assumed about region grid spacing? TODO
	 * 
	 * 
	 * @param resultsDir directory where results will be writer
	 * @param griddedRegion
	 * @param obsEqkRuptureList
	 * @param includeSpontEvents
	 * @param includeIndirectTriggering - include secondary, tertiary, etc events
	 * @param includeEqkRates - whether or not to include the long-term rate of events in sampling aftershocks
	 * @param gridSeisDiscr - lat lon discretization of gridded seismicity (degrees)
	 * @param simulationName 
	 * @param randomSeed - set for reproducibility, or set null if new seed desired
	 * @param fractionSrcAtPointList - from pre-computed data file
	 * @param srcAtPointList - from pre-computed data file
	 * @throws IOException 

	 */
	public static void testETAS_Simulation(File resultsDir, AbstractNthRupERF erf,
			GriddedRegion griddedRegion, ETAS_EqkRupture scenarioRup, List<? extends ObsEqkRupture> histQkList, boolean includeSpontEvents,
			boolean includeIndirectTriggering, boolean includeEqkRates, double gridSeisDiscr, String simulationName,
			Long randomSeed, List<float[]> fractionSrcInCubeList, List<int[]> srcInCubeList, int[] inputIsCubeInsideFaultPolygon, 
			ETAS_ParameterList etasParams)
					throws IOException {
		
		int numFaultSysSources = 0;
		if(erf instanceof FaultSystemSolutionERF) {
			numFaultSysSources = ((FaultSystemSolutionERF)erf).getNumFaultSystemSources();
		}
		
		// TODO:
		// griddedRegion could come from erf.getSolution().getGridSourceProvider().getGriddedRegion(); can the two be different?
		// gridSeisDiscr could come from erf.getSolution().getGridSourceProvider().getGriddedRegion().getLatSpacing()
		// add testScenario as method argument (can be null)
		
		ETAS_Utils etas_utils;
		if(randomSeed != null)
			etas_utils = new ETAS_Utils(randomSeed);
		else
			etas_utils = new ETAS_Utils(System.currentTimeMillis());
		
		// this could be input value
		SeisDepthDistribution seisDepthDistribution = new SeisDepthDistribution(etas_utils);
		
		// directory for saving results
		if(!resultsDir.exists()) resultsDir.mkdir();
		
		// file for writing simulations info
		FileWriter info_fr = new FileWriter(new File(resultsDir, "infoString.txt"));	// TODO this is closed below; why the warning?
		FileWriter simulatedEventsFileWriter = new FileWriter(new File(resultsDir, "simulatedEvents.txt"));
		ETAS_SimAnalysisTools.writeEventHeaderToFile(simulatedEventsFileWriter);

		info_fr.write(simulationName+"\n");
		info_fr.write("\nrandomSeed="+etas_utils.getRandomSeed()+"\n");
		if(D) System.out.println("\nrandomSeed="+etas_utils.getRandomSeed());
		if(histQkList == null)
			info_fr.write("\nhistQkList.size()=null"+"\n");
		else
			info_fr.write("\nhistQkList.size()="+histQkList.size()+"\n");
		info_fr.write("includeSpontEvents="+includeSpontEvents+"\n");
		info_fr.write("includeIndirectTriggering="+includeIndirectTriggering+"\n");
		
		info_fr.write("\nERF Adjustable Paramteres:\n\n");
		for(Parameter param : erf.getAdjustableParameterList()) {
			info_fr.write("\t"+param.getName()+" = "+param.getValue()+"\n");
		}
		TimeSpan tsp = erf.getTimeSpan();
		String startTimeString = tsp.getStartTimeMonth()+"/"+tsp.getStartTimeDay()+"/"+tsp.getStartTimeYear()+"; hr="+tsp.getStartTimeHour()+"; min="+tsp.getStartTimeMinute()+"; sec="+tsp.getStartTimeSecond();
		info_fr.write("\nERF StartTime: "+startTimeString+"\n");
		info_fr.write("\nERF TimeSpan Duration: "+erf.getTimeSpan().getDuration()+" years\n");
		
		info_fr.write("\nETAS Paramteres:\n\n");
		if(D) System.out.println("\nETAS Paramteres:\n\n");
		for(Parameter param : etasParams) {
			info_fr.write("\t"+param.getName()+" = "+param.getValue()+"\n");
			if(D) System.out.println("\t"+param.getName()+" = "+param.getValue());
		}
		
		info_fr.flush();
		
		// Make the list of observed ruptures, plus scenario if that was included
		ArrayList<ETAS_EqkRupture> obsEqkRuptureList = new ArrayList<ETAS_EqkRupture>();
		
		if(histQkList != null) {
			int id=0;
			for(ObsEqkRupture qk : histQkList) {
				Location hyp = qk.getHypocenterLocation();
				if(griddedRegion.contains(hyp) && hyp.getDepth() < 24.0) {
					ETAS_EqkRupture etasRup = new ETAS_EqkRupture(qk);
					etasRup.setID(id);
					obsEqkRuptureList.add(etasRup);
					id+=1;
				}
			}
			System.out.println("histQkList.size()="+histQkList.size());
			System.out.println("obsEqkRuptureList.size()="+obsEqkRuptureList.size());
		}
		
		int scenarioRupID = -1;
		double numPrimaryAshockForScenario = 0;
		if(scenarioRup != null) {
			scenarioRupID = obsEqkRuptureList.size();
			scenarioRup.setID(scenarioRupID);
			obsEqkRuptureList.add(scenarioRup);		
			if(D) {
				System.out.println("Num locs on scenario rup surface: "+scenarioRup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface().size());
			}
		}
		
		

		// this will store the simulated aftershocks & spontaneous events (in order of occurrence) - ObsEqkRuptureList? (they're added in order anyway)
		ObsEqkRupOrigTimeComparator otComparator = new ObsEqkRupOrigTimeComparator();	// this will keep the event in order of origin time
		PriorityQueue<ETAS_EqkRupture>  simulatedRupsQueue = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);
		
		// this is for keeping track of aftershocks on the fault system
		ArrayList<Integer> nthFaultSysRupAftershocks = new ArrayList<Integer>();
		
		long simStartTimeMillis = erf.getTimeSpan().getStartTimeCalendar().getTimeInMillis();
		long simEndTimeMillis = erf.getTimeSpan().getEndTimeCalendar().getTimeInMillis();
		double simDuration = erf.getTimeSpan().getDuration();
		
		if(D) System.out.println("Updating forecast in testETAS_Simulation");
		// get the total rate over the duration of the forecast
		erf.updateForecast();	// do this to get annual rate over the entire forecast (used to sample spontaneous events)
		if(D) System.out.println("Done updating forecast in testETAS_Simulation");
		
		//Compute origTotRate; this calculation includes rates outside region, 
		// but this is dominated by gridded seis so shouldn't matter; using 
		// ERF_Calculator.getTotalRateInRegion(erf, griddedRegion, 0.0) takes way too long.
		if(D) System.out.println("Computing origTotRate");
		long st = System.currentTimeMillis();
		double origTotRate=0;
		for(ProbEqkSource src:erf) {
			for(ProbEqkRupture rup:src) {
				origTotRate += rup.getMeanAnnualRate(erf.getTimeSpan().getDuration());
			}
		}
		if (D) System.out.println("\torigTotRate="+(float)origTotRate+"; that took (sec): "+(float)(System.currentTimeMillis()-st)/1000f);
		info_fr.write("\nExpected mean annual rate over timeSpan (per year) = "+(float)origTotRate+"\n");
		
		// set to yearly probabilities for simulation forecast (in case input was not a 1-year forecast)
		erf.getTimeSpan().setDuration(1.0);	// TODO make duration expected time to next supra seis event?
		erf.updateForecast();
		
		
		if(D) System.out.println("Computing original spontaneousRupSampler & sourceRates[s]");
		st = System.currentTimeMillis();
		double sourceRates[] = new double[erf.getNumSources()];
		double duration = erf.getTimeSpan().getDuration();
		IntegerPDF_FunctionSampler spontaneousRupSampler = new IntegerPDF_FunctionSampler(erf.getTotNumRups());
		int nthRup=0;
		System.out.println("tot num:"+erf.getTotNumRups());
		for(int s=0;s<erf.getNumSources();s++) {
			ProbEqkSource src = erf.getSource(s);
			sourceRates[s] = src.computeTotalEquivMeanAnnualRate(duration);
			for(ProbEqkRupture rup:src) {
				if (nthRup >= spontaneousRupSampler.getNum())
					throw new RuntimeException("Weird...tot num="+erf.getTotNumRups()+", nth="+nthRup);
				spontaneousRupSampler.set(nthRup, rup.getMeanAnnualRate(duration));
				nthRup+=1;
			}
		}
		if(D) System.out.println("\tspontaneousRupSampler.calcSumOfY_Vals()="+(float)spontaneousRupSampler.calcSumOfY_Vals() +
				"; that took (sec): "+(float)(System.currentTimeMillis()-st)/1000f);
		
		
		if(D) System.out.println("Making ETAS_PrimaryEventSampler");
		st = System.currentTimeMillis();
		ETAS_PrimaryEventSampler etas_PrimEventSampler = new ETAS_PrimaryEventSampler(griddedRegion, erf, sourceRates, 
				gridSeisDiscr,null, etasParams.getApplyLongTermRates(), etas_utils, etasParams.get_q(), etasParams.get_d(), 
				etasParams.getImposeGR(), fractionSrcInCubeList, srcInCubeList, inputIsCubeInsideFaultPolygon);
		if(D) System.out.println("ETAS_PrimaryEventSampler creation took "+(float)(System.currentTimeMillis()-st)/60000f+ " min");
		info_fr.write("\nMaking ETAS_PrimaryEventSampler took "+(System.currentTimeMillis()-st)/60000+ " min");
		
		
//double[] sectRateArray = etas_PrimEventSampler.getCurrentRateOfEachSubsection();
//List<FaultSectionPrefData> dataList = ((FaultSystemSolutionERF)erf).getSolution().getRupSet().getFaultSectionDataList();
//System.out.println("Mojave S 9 Rate: "+sectRateArray[1846]+"\t"+dataList.get(1846).getName());
//System.out.println("Mojave N 4 Rate: "+sectRateArray[1836]+"\t"+dataList.get(1836).getName());

//System.out.println("Mojave S 9 Prob: "+etas_PrimEventSampler.tempGetSampleProbForAllCubesOnFaultSection(1846, scenarioRup)+"\t"+dataList.get(1846).getName());
//System.out.println("Mojave N 4 Prob: "+etas_PrimEventSampler.tempGetSampleProbForAllCubesOnFaultSection(1836, scenarioRup)+"\t"+dataList.get(1836).getName());

		// prevent supra-seis triggering on scenario surface if it's a FSS rupture
		if(scenarioRup != null && scenarioRup.getFSSIndex() >= 0)
			etas_PrimEventSampler.removeTriggeringOnSupraSeisRup(scenarioRup);

//System.out.println("Mojave S 9 MFD: \n"+etas_PrimEventSampler.tempGetSampleMFD_ForAllCubesOnFaultSection(1846, scenarioRup).getCumRateDistWithOffset().toString());
//System.out.println("Mojave S 9 MFD: \n"+etas_PrimEventSampler.tempGetSampleMFD_ForAllCubesOnFaultSection(1846, scenarioRup).toString());
//System.out.println("Mojave S 3 MFD: \n"+etas_PrimEventSampler.tempGetSampleMFD_ForAllCubesOnFaultSection(1840, scenarioRup).getCumRateDistWithOffset().toString());
//System.out.println("Mojave S 3 MFD: \n"+etas_PrimEventSampler.tempGetSampleMFD_ForAllCubesOnFaultSection(1840, scenarioRup).toString());
//System.out.println("\nMojave N 4 MFD: \n"+etas_PrimEventSampler.tempGetSampleMFD_ForAllCubesOnFaultSection(1836, scenarioRup).getCumRateDistWithOffset().toString());
//System.out.println("\nMojave N 4 MFD: \n"+etas_PrimEventSampler.tempGetSampleMFD_ForAllCubesOnFaultSection(1836, scenarioRup).toString());
//System.exit(-1);
		
		
		// Make list of primary aftershocks for given list of obs quakes 
		// (filling in origin time ID, parentID, and location on parent that does triggering, with the rest to be filled in later)
		if (D) System.out.println("Making primary aftershocks from input obsEqkRuptureList, size = "+obsEqkRuptureList.size());
		PriorityQueue<ETAS_EqkRupture>  eventsToProcess = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);	// not sure about the first field
		int testParID=0;	// this will be used to test IDs
		int eventID = obsEqkRuptureList.size();	// start IDs after input events
		for(ETAS_EqkRupture rup: obsEqkRuptureList) {
			int parID = rup.getID();
			if(parID != testParID) 
				throw new RuntimeException("problem with ID");
			long rupOT = rup.getOriginTime();
			double startDay = (double)(simStartTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
			// get a list of random primary event times, in units of days since main shock
//			double[] randomAftShockTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
			double[] randomAftShockTimes = etas_utils.getRandomEventTimes(etasParams.get_k(), etasParams.get_p(), rup.getMag(), ETAS_Utils.magMin_DEFAULT, etasParams.get_c(), startDay, endDay);
			if(rup.getID() == scenarioRupID)
				numPrimaryAshockForScenario = randomAftShockTimes.length;
			if(randomAftShockTimes.length>0) {
				for(int i=0; i<randomAftShockTimes.length;i++) {
					long ot = rupOT +  (long)(randomAftShockTimes[i]*(double)ProbabilityModelsCalc.MILLISEC_PER_DAY);	// convert to milliseconds
					ETAS_EqkRupture newRup = new ETAS_EqkRupture(rup, eventID, ot);
					newRup.setParentID(parID);
					newRup.setGeneration(1);
					newRup.setParentTriggerLoc(etas_utils.getRandomLocationOnRupSurface(rup.getRuptureSurface()));
					etas_PrimEventSampler.addParentLocToProcess(newRup.getParentTriggerLoc());
					eventsToProcess.add(newRup);
					eventID +=1;
				}
			}
			testParID += 1;				
		}
		if (D) System.out.println("The "+obsEqkRuptureList.size()+" input events produced "+eventsToProcess.size()+" primary aftershocks");
		info_fr.write("\nThe "+obsEqkRuptureList.size()+" input observed events produced "+eventsToProcess.size()+" primary aftershocks\n");

		
		// make the list of spontaneous events, filling in only event IDs and origin times for now
		if(includeSpontEvents) {
			if (D) System.out.println("Making spontaneous events and times of primary aftershocks...");
			double fractionNonTriggered=etasParams.getFractSpont();	// one minus branching ratio TODO fix this; this is not what branching ratio is
			double expectedNum = origTotRate*simDuration*fractionNonTriggered;
			int numSpontEvents = etas_utils.getPoissonRandomNumber(expectedNum);
			for(int r=0;r<numSpontEvents;r++) {
				ETAS_EqkRupture rup = new ETAS_EqkRupture();
				double ot = simStartTimeMillis+etas_utils.getRandomDouble()*(simEndTimeMillis-simStartTimeMillis);	// random time over time span
				rup.setOriginTime((long)ot);
				rup.setID(eventID);
				rup.setGeneration(0);
				eventsToProcess.add(rup);
				eventID += 1;
			}
			String spEvStringInfo = "Spontaneous Events:\n\n\tAssumed fraction non-triggered = "+fractionNonTriggered+
					"\n\texpectedNum="+expectedNum+"\n\tnumSampled="+numSpontEvents+"\n";
			if(D) System.out.println(spEvStringInfo);
			info_fr.write("\n"+spEvStringInfo);
		}

		// If scenarioRup != null, compute ExpectedPrimaryMFD if in debug mode; this takes time!
		List<EvenlyDiscretizedFunc> expectedPrimaryMFDsForScenarioList=null;
		boolean doit = true;
		if(D && scenarioRup !=null && doit) {
			if(D) System.out.println("Computing Scenario Diagnostics");
			long rupOT = scenarioRup.getOriginTime();
			double startDay = (double)(simStartTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
			double expNum = ETAS_Utils.getExpectedNumEvents(etasParams.get_k(), etasParams.get_p(), scenarioRup.getMag(), ETAS_Utils.magMin_DEFAULT, etasParams.get_c(), startDay, endDay);
			
			info_fr.write("\nExpected number of primary events for Scenario: "+expNum+"\n");
			info_fr.write("\nObserved number of primary events for Scenario: "+numPrimaryAshockForScenario+"\n");
			System.out.println("\nExpected number of primary events for Scenario: "+expNum);
			System.out.println("Observed number of primary events for Scenario: "+numPrimaryAshockForScenario+"\n");
			
			// compute expected MFD
			expectedPrimaryMFDsForScenarioList = ETAS_SimAnalysisTools.plotExpectedPrimaryMFD_ForRup("Scenario", new File(resultsDir,"scenarioExpPrimMFD").getAbsolutePath(), 
					etas_PrimEventSampler, scenarioRup, expNum);
// System.exit(-1);
			// Compute Primary Event Sampler Map
			etas_PrimEventSampler.plotSamplerMap(etas_PrimEventSampler.getAveSamplerForRupture(scenarioRup), "Primary Sampler for Scenario", "scenarioPrimaryMap", resultsDir);
			// Compute subsection trigger probability map
			String info = etas_PrimEventSampler.plotSubSectTriggerProbGivenRuptureAndReturnInfo(scenarioRup, resultsDir, 50);
			
			System.out.println(info);	
			info_fr.write(info+"\n");
		}
		
		if(D) {
			System.out.println("Testing the etas_PrimEventSampler");
			etas_PrimEventSampler.testRates();
//			etas_PrimEventSampler.testMagFreqDist();	// this is time consuming

		}

		CalcProgressBar progressBar;
		try {
			progressBar = new CalcProgressBar("Primary aftershocks to process", "junk");
			progressBar.showProgress(true);
		} catch (Throwable t) {
			// headless, don't show it
			progressBar = null;
		}
		
		if (D) System.out.println("Looping over eventsToProcess (initial num = "+eventsToProcess.size()+")...\n");
		if (D) System.out.println("\tFault system ruptures triggered (date\tmag\tname\tnthRup,src,rupInSrc,fltSysRup):");
		info_fr.write("\nFault system ruptures triggered (date\tmag\tname\tnthRup,src,rupInSrc,fltSysRup):\n");

		st = System.currentTimeMillis();
		
		int numSimulatedEvents = 0;
		
		EpicenterMapThread mapThread;
		if (D && live_map)
			mapThread = ETAS_SimAnalysisTools.plotUpdatingEpicenterMap(
				simulationName, null, simulatedRupsQueue, griddedRegion.getBorder());
		else
			mapThread = null;
		
		if (D && pause_for_events) {
			try {
				JOptionPane.showMessageDialog(null, "Continue", "Ready To Generate Events", JOptionPane.PLAIN_MESSAGE);
			} catch (HeadlessException e) {
				// do nothing if Headless
			}
		}
		
		while(eventsToProcess.size()>0) {
			
			if (progressBar != null) progressBar.updateProgress(numSimulatedEvents, eventsToProcess.size()+numSimulatedEvents);
			
			ETAS_EqkRupture rup = eventsToProcess.poll();	//Retrieves and removes the head of this queue, or returns null if this queue is empty.
			
			boolean succeededInSettingRupture=true;
			
			if(rup.getParentID() == -1)	{ // it's a spontaneous event TODO
//			if(rup.getParentRup() == null)	{ // it's a spontaneous event
				Location hypoLoc = null;
				ProbEqkRupture erf_rup;
				nthRup = spontaneousRupSampler.getRandomInt(etas_utils.getRandomDouble());	// sample from long-term model
				erf_rup = erf.getNthRupture(nthRup);
				LocationList surfPts = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				if(surfPts.size() == 1) {// point source
					Location ptLoc = surfPts.get(0);
					// FOLLOWING ASSUMES A GRID SPACING OF 0.1 FOR BACKGROUND SEIS; "0.99" is to keep it in cell
					hypoLoc = new Location(ptLoc.getLatitude()+(etas_utils.getRandomDouble()-0.5)*0.1*0.99,
							ptLoc.getLongitude()+(etas_utils.getRandomDouble()-0.5)*0.1*0.99,
							seisDepthDistribution.getRandomDepth());
				}
				else {
					int hypIndex = etas_utils.getRandomInt(surfPts.size()-1);	// choose random loc assuming uniform probability among points
					hypoLoc = surfPts.get(hypIndex);
				}
				rup.setAveRake(erf_rup.getAveRake());
				rup.setMag(erf_rup.getMag());
				rup.setRuptureSurface(erf_rup.getRuptureSurface());
				rup.setNthERF_Index(nthRup);
				rup.setHypocenterLocation(hypoLoc);
				int sourceIndex = erf.getSrcIndexForNthRup(nthRup);
				if (sourceIndex < numFaultSysSources)
					rup.setFSSIndex(((FaultSystemSolutionERF)erf).getFltSysRupIndexForNthRup(nthRup));
				else
					rup.setGridNodeIndex(sourceIndex - numFaultSysSources);
			}
			else {
				succeededInSettingRupture = etas_PrimEventSampler.setRandomPrimaryEvent(rup);
			}
			
			// break out if we failed to set the rupture
			if(!succeededInSettingRupture)
				continue;

			// add the rupture to the list
			simulatedRupsQueue.add(rup);	// this storage does not take much memory during the simulations
			numSimulatedEvents += 1;
			
			ETAS_SimAnalysisTools.writeEventToFile(simulatedEventsFileWriter, rup);
			
			long rupOT = rup.getOriginTime();
			
			// now sample primary aftershock times for this event
			if(includeIndirectTriggering) {
				int parID = rup.getID();	// rupture is now the parent
				int gen = rup.getGeneration()+1;
				double startDay = 0;	// starting at origin time since we're within the timespan
				double endDay = (double)(simEndTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
//				double[] eventTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
				double[] eventTimes = etas_utils.getRandomEventTimes(etasParams.get_k(), etasParams.get_p(), rup.getMag(), ETAS_Utils.magMin_DEFAULT, etasParams.get_c(), startDay, endDay);

				if(eventTimes.length>0) {
					for(int i=0; i<eventTimes.length;i++) {
						long ot = rupOT +  (long)(eventTimes[i]*(double)ProbabilityModelsCalc.MILLISEC_PER_DAY);
						ETAS_EqkRupture newRup = new ETAS_EqkRupture(rup, eventID, ot);
						newRup.setGeneration(gen);
						newRup.setParentID(parID);
						newRup.setParentTriggerLoc(etas_utils.getRandomLocationOnRupSurface(rup.getRuptureSurface()));
						etas_PrimEventSampler.addParentLocToProcess(newRup.getParentTriggerLoc());
						eventsToProcess.add(newRup);
						eventID +=1;
					}
				}		
			}
			
			
			// if it was a fault system rupture, need to update time span, rup rates, block, and samplers.
			nthRup = rup.getNthERF_Index();
			int srcIndex = erf.getSrcIndexForNthRup(nthRup);

			if(srcIndex<numFaultSysSources) {
				
				nthFaultSysRupAftershocks.add(nthRup);
				int fltSysRupIndex = ((FaultSystemSolutionERF)erf).getFltSysRupIndexForNthRup(nthRup);

				// set the start time for the time dependent calcs
				erf.getTimeSpan().setStartTimeInMillis(rupOT);	
				
				if(D) {
					Toolkit.getDefaultToolkit().beep();
					System.out.println("GOT A FAULT SYSTEM RUPTURE!");
				}
				
				TimeSpan ts = erf.getTimeSpan();
				String rupString = "\t"+ts.getStartTimeMonth()+"/"+ts.getStartTimeDay()+"/"+ts.getStartTimeYear()+"\tmag="+
						(float)rup.getMag()+"\t"+erf.getSource(srcIndex).getName()+
						"\n\tnthRup="+nthRup+", srcIndex="+srcIndex+", RupIndexInSource="+
						erf.getRupIndexInSourceForNthRup(nthRup)+", fltSysRupIndex="+fltSysRupIndex+"\tgen="+rup.getGeneration();
				if(D) System.out.println(rupString);
				info_fr.write(rupString+"\n");

				// set the date of last event for this rupture
				((FaultSystemSolutionERF)erf).setFltSystemSourceOccurranceTime(srcIndex, rupOT);

				// now update source rates for etas_PrimEventSampler & spontaneousRupSampler
				if(D) System.out.print("\tUpdating src rates for etas_PrimEventSampler & spontaneousRupSampler; ");
				Long st2 = System.currentTimeMillis();
				if(erf.getParameter(ProbabilityModelParam.NAME).getValue() != ProbabilityModelOptions.POISSON) {
					erf.updateForecast();
					for(int s=0;s<numFaultSysSources;s++) {
						ProbEqkSource src = erf.getSource(s);
						double oldRate = sourceRates[s];
						sourceRates[s] = src.computeTotalEquivMeanAnnualRate(duration);
						double newRate = sourceRates[s];
						// TEST THAT RATE CHANGED PROPERLY
						if(D) {
							if(s == erf.getSrcIndexForNthRup(nthRup)) {
								System.out.print("for rup that occurred, oldRate="+(float)oldRate+" & newRate = "+(float)newRate+"\n");			
							}
						}
						// update the spontaneous event sampler with new rupture rates
						for(int r=0 ; r<src.getNumRuptures(); r++) {
							ProbEqkRupture rupInSrc = src.getRupture(r);
							double rate = rupInSrc.getMeanAnnualRate(duration);
							spontaneousRupSampler.set(erf.getIndexN_ForSrcAndRupIndices(s, r), rate);
						}
					}
					// now update the ETAS sampler
					etas_PrimEventSampler.declareRateChange();
					// and prevent triggering on surface of what just ruptured
					etas_PrimEventSampler.removeTriggeringOnSupraSeisRup(rup);

				}
				String tempFileName = new File(resultsDir,"FltSysRup"+fltSysRupIndex+"_ExpPrimMFD").getAbsolutePath();
				if(D) {
					System.out.println("Sampler update took "+(System.currentTimeMillis()-st2)/1000+" secs");					
					System.out.println("Running plotExpectedPrimaryMFD_ForRup");
					double startDay = 0.0;	// from the moment it occurs
					double endDay = (double)(simEndTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
					double expNum = ETAS_Utils.getExpectedNumEvents(etasParams.get_k(), etasParams.get_p(), scenarioRup.getMag(), ETAS_Utils.magMin_DEFAULT, etasParams.get_c(), startDay, endDay);
					ETAS_SimAnalysisTools.plotExpectedPrimaryMFD_ForRup("Triggered Supra Seis Flt Sys Rup #"+fltSysRupIndex, tempFileName, etas_PrimEventSampler, erf.getNthRupture(nthRup), expNum);
				}
			}
		}
		
		if (progressBar != null) progressBar.showProgress(false);
		if (mapThread != null)
			mapThread.kill();

		if(D) System.out.println("\nLooping over events took "+(System.currentTimeMillis()-st)/1000+" secs");
		info_fr.write("\nLooping over events took "+(System.currentTimeMillis()-st)/1000+" secs\n");
		
		
		int[] numInEachGeneration = ETAS_SimAnalysisTools.getNumAftershocksForEachGeneration(simulatedRupsQueue, 10);
		String numInfo = "Total num ruptures: "+simulatedRupsQueue.size()+"\n";
		numInfo += "Num spontaneous: "+numInEachGeneration[0]+"\n";
		numInfo += "Num 1st Gen: "+numInEachGeneration[1]+"\n";
		numInfo += "Num 2nd Gen: "+numInEachGeneration[2]+"\n";
		numInfo += "Num 3rd Gen: "+numInEachGeneration[3]+"\n";
		numInfo += "Num 4th Gen: "+numInEachGeneration[4]+"\n";
		numInfo += "Num 5th Gen: "+numInEachGeneration[5]+"\n";
		numInfo += "Num 6th Gen: "+numInEachGeneration[6]+"\n";
		numInfo += "Num 7th Gen: "+numInEachGeneration[7]+"\n";
		numInfo += "Num 8th Gen: "+numInEachGeneration[8]+"\n";
		numInfo += "Num 9th Gen: "+numInEachGeneration[9]+"\n";
		numInfo += "Num 10th Gen: "+numInEachGeneration[10]+"\n";
		
		if(D) System.out.println(numInfo);
		info_fr.write(numInfo+"\n");


		if(D && scenarioRup !=null) {	// scenario rupture included
			int inputRupID = scenarioRup.getID();
			ETAS_SimAnalysisTools.plotEpicenterMap(simulationName, new File(resultsDir,"hypoMap.pdf").getAbsolutePath(), obsEqkRuptureList.get(0), simulatedRupsQueue, griddedRegion.getBorder());
			ETAS_SimAnalysisTools.plotDistDecayHistOfAshocksForRup(simulationName, new File(resultsDir,"distDecayForRup.pdf").getAbsolutePath(), 
					simulatedRupsQueue, etasParams.get_q(), etasParams.get_d(), inputRupID);
			ArrayList<IncrementalMagFreqDist> obsAshockMFDsForFirstEvent = ETAS_SimAnalysisTools.getAftershockMFDsForRup(simulatedRupsQueue, inputRupID, simulationName);
			obsAshockMFDsForFirstEvent.add((IncrementalMagFreqDist)expectedPrimaryMFDsForScenarioList.get(0));
			ETAS_SimAnalysisTools.plotMagFreqDistsForRup("AshocksOfScenarioMFD", resultsDir, obsAshockMFDsForFirstEvent);
			
			
			// write stats for first rup
			
			double expPrimNumAtMainMag = Double.NaN;
			double expPrimNumAtMainMagMinusOne = Double.NaN;
			if(expectedPrimaryMFDsForScenarioList.get(1) != null) {
				expPrimNumAtMainMag = expectedPrimaryMFDsForScenarioList.get(1).getInterpolatedY(scenarioRup.getMag());
				expPrimNumAtMainMagMinusOne = expectedPrimaryMFDsForScenarioList.get(1).getInterpolatedY(scenarioRup.getMag()-1.0);				
			}
			EvenlyDiscretizedFunc obsPrimCumMFD = obsAshockMFDsForFirstEvent.get(1).getCumRateDistWithOffset();
			double obsPrimNumAtMainMag = obsPrimCumMFD.getInterpolatedY(scenarioRup.getMag());
			double obsPrimNumAtMainMagMinusOne = obsPrimCumMFD.getInterpolatedY(scenarioRup.getMag()-1.0);
			EvenlyDiscretizedFunc obsAllCumMFD = obsAshockMFDsForFirstEvent.get(1).getCumRateDistWithOffset();
			double obsAllNumAtMainMag = obsAllCumMFD.getInterpolatedY(scenarioRup.getMag());
			double obsAllNumAtMainMagMinusOne = obsAllCumMFD.getInterpolatedY(scenarioRup.getMag()-1.0);
			String testEventStats="\nAftershock Stats for single input event:\n";
			testEventStats+="\tNum Primary Aftershocks at main shock mag("+(float)scenarioRup.getMag()+"):\n\t\tExpected="+expPrimNumAtMainMag+"\n\t\tObserved="+obsPrimNumAtMainMag+"\n";
			testEventStats+="\tNum Primary Aftershocks at one minus main-shock mag("+(float)(scenarioRup.getMag()-1.0)+"):\n\t\tExpected="+expPrimNumAtMainMagMinusOne+"\n\t\tObserved="+obsPrimNumAtMainMagMinusOne+"\n";
			testEventStats+="\tTotal Observed Num Aftershocks:\n\t\tAt main-shock mag = "+obsAllNumAtMainMag+"\n\t\tAt one minus main-shock mag = "+obsAllNumAtMainMagMinusOne+"\n";
			if(D) System.out.println(testEventStats);
			info_fr.write(testEventStats);

			
		} else if (D) {
			ETAS_SimAnalysisTools.plotEpicenterMap(simulationName, new File(resultsDir,"hypoMap.pdf").getAbsolutePath(), null, simulatedRupsQueue, griddedRegion.getBorder());
		}
		
		if(D) {
			ETAS_SimAnalysisTools.plotDistDecayHistForAshocks(simulationName, new File(resultsDir,"distDecay.pdf").getAbsolutePath(), simulatedRupsQueue, etasParams.get_q(), etasParams.get_d());
			ETAS_SimAnalysisTools.plotNumVsLogTimeSinceParent(simulationName, new File(resultsDir,"logTimeDecay.pdf").getAbsolutePath(), simulatedRupsQueue,
					etasParams.get_k(), etasParams.get_p(), etasParams.get_c());
			ETAS_SimAnalysisTools.plotNumVsTimeSinceParent(simulationName, new File(resultsDir,"timeDecay.pdf").getAbsolutePath(), simulatedRupsQueue,
					etasParams.get_k(), etasParams.get_p(), etasParams.get_c());
			ETAS_SimAnalysisTools.plotMagFreqDists(simulationName, resultsDir, simulatedRupsQueue);
		}
		
		info_fr.close();
		simulatedEventsFileWriter.close();

		ETAS_SimAnalysisTools.writeMemoryUse("Memory at end of simultation");
	}
	
	
	/**
	 * This utility finds the source index for the fault system rupture that has the given first and last subsection
	 * @param erf
	 * @param firstSectID
	 * @param secondSectID
	 */
	private static void writeInfoAboutSourceWithThisFirstAndLastSection(FaultSystemSolutionERF erf, int firstSectID, int secondSectID) {
		System.out.println("Looking for source...");
		for(int s=0; s<erf.getNumFaultSystemSources();s++) {
			FaultSystemRupSet rupSet = erf.getSolution().getRupSet();
			List<Integer> sectListForSrc = rupSet.getSectionsIndicesForRup(erf.getFltSysRupIndexForSource(s));
			boolean firstIsIt = rupSet.getFaultSectionData(sectListForSrc.get(0)).getSectionId() == firstSectID;
			boolean lastIsIt = rupSet.getFaultSectionData(sectListForSrc.get(sectListForSrc.size()-1)).getSectionId() == secondSectID;
			if(firstIsIt && lastIsIt) {
				System.out.println("SourceIndex="+s+"\tfssIndex="+erf.getFltSysRupIndexForSource(s)+"\t"+erf.getSource(s).getName());
				break; 
			}
			firstIsIt = rupSet.getFaultSectionData(sectListForSrc.get(0)).getSectionId() == secondSectID;
			lastIsIt = rupSet.getFaultSectionData(sectListForSrc.get(sectListForSrc.size()-1)).getSectionId() == firstSectID;
			if(firstIsIt && lastIsIt) {
				System.out.println("SourceIndex="+s+"\tfssIndex="+erf.getFltSysRupIndexForSource(s)+"\t"+erf.getSource(s).getName());
				break;
			}
		}
	}
	
	
	/**
	 * This builds a scenario rup for the given scenario.
	 * This returns null is scenario=null.
	 * TODO origin time should be passed in, as well as whether ERF elastic rebound is reset for scenario?
	 * @param scenario
	 * @param erf
	 * @return
	 */
	public static ETAS_EqkRupture buildScenarioRup(TestScenario scenario, FaultSystemSolutionERF_ETAS erf) {
		ETAS_EqkRupture scenarioRup=null;
		if(scenario != null) {
			scenarioRup = new ETAS_EqkRupture();
			Long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
			scenarioRup.setOriginTime(ot);	
			int fssIndex = scenario.fssIndex;
			if(fssIndex>=0) {
				int srcID = erf.getSrcIndexForFltSysRup(fssIndex);
				Preconditions.checkState(srcID >= 0, "Source not found for FSS index="+fssIndex);
				ProbEqkRupture rupFromERF = erf.getSource(srcID).getRupture(0);
				scenarioRup.setAveRake(rupFromERF.getAveRake());
				scenarioRup.setMag(rupFromERF.getMag());
				if (!Double.isNaN(scenario.mag))
					// override mag
					scenarioRup.setMag(scenario.mag);
				scenarioRup.setFSSIndex(fssIndex);
				scenarioRup.setRuptureSurface(rupFromERF.getRuptureSurface());
//	System.out.println(rupFromERF.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface().size());
//				CompoundSurface trimmedSurface = GriddedSurfaceUtils.trimEndsOfSurface(
//						(CompoundSurface)rupFromERF.getRuptureSurface(), 3, 3);
//				scenarioRup.setRuptureSurface(trimmedSurface);
//	System.out.println(trimmedSurface.getEvenlyDiscritizedListOfLocsOnSurface().size());
//	System.exit(-1);
//				scenarioRup.setRuptureSurface(rupFromERF.getRuptureSurface().getMoved(new LocationVector(60.0, 5.0, 0.0)));
//				System.out.println("test Mainshock: "+erf.getSource(srcID).getName()+"; mag="+scenarioRup.getMag());
				System.out.println("\tProbBeforeDateOfLastReset: "+erf.getSource(srcID).getRupture(0).getProbability());
				erf.setFltSystemSourceOccurranceTime(srcID, ot);
				erf.updateForecast();
				System.out.println("\tProbAfterDateOfLastReset: "+erf.getSource(srcID).getRupture(0).getProbability());
			} 
			else {
				scenarioRup.setAveRake(0.0);
				if (scenario == TestScenario.CUSTOM) {
					// prompt for loc/mag
					ParameterList params = new ParameterList();
					DoubleParameter magParam = new DoubleParameter("Magnitude", 2d, 8.89, (Double)scenario.mag);
					params.addParameter(magParam);
					LocationParameter locParam = new LocationParameter("Hpocenter Location", scenario.loc);
					params.addParameter(locParam);
					ParameterListEditor edit = new ParameterListEditor(params);
					
					// this will block until done
					JOptionPane.showMessageDialog(null, edit, "Custom Rupture", JOptionPane.PLAIN_MESSAGE);
					scenarioRup.setMag(magParam.getValue());
					scenarioRup.setPointSurface(locParam.getValue());
					System.out.println("Loaded custom scenario: M"+(float)scenarioRup.getMag()
							+", "+scenarioRup.getHypocenterLocation());
				} else {
					scenarioRup.setMag(scenario.mag);
					scenarioRup.setPointSurface(scenario.loc);
				}
			}	
		}
	return scenarioRup;
	}

	/**
	 * 
	 * 
	 * TODO:
	 * 
	 * @param scenario
	 * @param etasParams
	 * @param randomSeed
	 * @param simulationName
	 */
	public static void runTest(TestScenario scenario, ETAS_ParameterList etasParams, Long randomSeed, 
			String simulationName, ObsEqkRupList histQkList) {
		
		ETAS_SimAnalysisTools.writeMemoryUse("Memory at beginning of run");

		Long st = System.currentTimeMillis();

		FaultSystemSolutionERF_ETAS erf = getU3_ETAS_ERF();
		
		CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion = RELM_RegionUtils.getGriddedRegionInstance();

		ETAS_EqkRupture scenarioRup= buildScenarioRup(scenario, erf);
		
//		System.out.println("aveStrike="+scenarioRup.getRuptureSurface().getAveStrike());
//		for(Location loc:scenarioRup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface()) {
//			System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
//		}
//		System.exit(-1);
		
		boolean includeSpontEvents=true;
		boolean includeIndirectTriggering=true;
		boolean includeEqkRates = true;
		double gridSeisDiscr = 0.1;
		
		System.out.println("Starting testETAS_Simulation");
		try {
			String dirNameForSavingFiles = "U3_ETAS_"+simulationName+"/";
			File resultsDir = new File(dirNameForSavingFiles);
			testETAS_Simulation(resultsDir, erf, griddedRegion, scenarioRup, histQkList,  includeSpontEvents, 
					includeIndirectTriggering, includeEqkRates, gridSeisDiscr, simulationName, randomSeed, etasParams);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		float timeMin = (float)(System.currentTimeMillis()-st)/60000f;
		System.out.println("Total simulation took "+timeMin+" min");

	}

	
	
	
	public static ObsEqkRupList getHistCatalog() {
		File file = new File("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/data/ofr2013-1165_EarthquakeCat.txt");
		ObsEqkRupList histQkList=null;
		try {
			histQkList = UCERF3_CatalogParser.loadCatalog(file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return histQkList;
	}
	

	
	
	
	/**
	 * This is where on can put bug-producing cases for others to look at
	 * @throws IOException
	 */
	public static void runBugReproduce() throws IOException {
	}


	
	public static FaultSystemSolutionERF_ETAS getU3_ETAS_ERF() {
		
		// means solution ERF
		System.out.println("Starting ERF instantiation");
		
//		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolution fss;
		try {
			fss = FaultSystemIO.loadSol(new File(fileName));
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		return getU3_ETAS_ERF(fss);
	}
	
	public static FaultSystemSolutionERF_ETAS getU3_ETAS_ERF(FaultSystemSolution fss) {
		Long st = System.currentTimeMillis();
		FaultSystemSolutionERF_ETAS erf = new FaultSystemSolutionERF_ETAS(fss);
		
//		// Or for Reference branch ERF:
//		// U3.3 compuond file, assumed to be in data/scratch/InversionSolutions
//		// download it from here: http://opensha.usc.edu/ftp/kmilner/ucerf3/2013_05_10-ucerf3p3-production-10runs/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip
//		String fileName = "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip";
//		File invDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
//		File compoundFile = new File(invDir, fileName);
//		CompoundFaultSystemSolution fetcher;
//		FaultSystemSolutionERF erf  = null;
//		try {
//			fetcher = CompoundFaultSystemSolution.fromZipFile(compoundFile);
//			LogicTreeBranch ref = LogicTreeBranch.DEFAULT;
//			InversionFaultSystemSolution sol = fetcher.getSolution(ref);
//			erf = new FaultSystemSolutionERF(sol);
//		} catch (ZipException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		
		
		// set parameters
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
		erf.setParameter(BackgroundRupParam.NAME, BackgroundRupType.POINT);
		erf.setParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME, false);
		erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.U3_BPT);
		erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.MID_VALUES);
		BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE;
		erf.setParameter(BPTAveragingTypeParam.NAME, aveType);
		erf.setParameter(AleatoryMagAreaStdDevParam.NAME, 0.0);
		erf.getParameter(HistoricOpenIntervalParam.NAME).setValue(2014d-1875d);	
		erf.getTimeSpan().setStartTimeInMillis(Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR)+1);
		erf.getTimeSpan().setDuration(1);
		
		erf.updateForecast();
		
		float timeSec = (float)(System.currentTimeMillis()-st)/1000f;
		System.out.println("ERF instantiation took "+timeSec+" sec");

		return erf;
	}
	
	
	
	/**
	 * Test scenarios
	 *
	 */
	public enum TestScenario {
		
		MOJAVE("Mojave M7.05", 197792),		// provided by Kevin
		LANDERS("Landers", 246711),			// found by running: writeInfoAboutSourceWithThisFirstAndLastSection(erf, 243, 989);
		NORTHRIDGE("Northridge", 187455),	// found by running: writeInfoAboutSourceWithThisFirstAndLastSection(erf, 1409, 1413);
		LA_HABRA_6p2("La Habra 6.2", new Location(33.932,-117.917,4.8), 6.2),
		NEAR_MAACAMA("Near Maacama", new Location(39.79509, -123.56665-0.04, 7.54615), 7.0),
		ON_MAACAMA("On Maacama", new Location(39.79509, -123.56665, 7.54615), 7.0),
		ON_N_MOJAVE("On N Mojave", getMojaveTestLoc(0.0), 5.0),	// on N edge of the Mojave scenario
		NEAR_N_MOJAVE_3KM("On N Mojave", getMojaveTestLoc(3.0), 5.0),	// on N edge of the Mojave scenario
		CUSTOM("Custom (will prompt)", new Location(34, -118), 5d), // will prompt when built, these are just defaults
		NAPA("Napa 6.0", 93902, null, 6d), // supra-seismogenic rup that is a 6.3 in U3, overridden to be a 6.0
		PARKFIELD("Parkfield M6", 30473), // Parkfield M6 fault based rup
		BOMBAY_BEACH_M6("Bombay Beach M6", new Location(33.3183,-115.7283,5.8), 6.0); // Bombay Beach M6 in location of 2009 M4.8

				
		private String name;
		private int fssIndex;
		private Location loc;
		private double mag;
		private TestScenario(String name, int fssIndex) {
			this(name, fssIndex, null, Double.NaN);
		}
		
		private TestScenario(String name, Location loc, double mag) {
			this(name, -1, loc, mag);
		}
		
		private TestScenario(String name, int fssIndex, Location loc, double mag) {
			this.fssIndex = fssIndex;
			this.name = name;
			this.loc = loc;
			this.mag = mag;
			Preconditions.checkState(loc != null || fssIndex >= 0);
			if (fssIndex >= 0)
				Preconditions.checkState(loc == null);
			else
				Preconditions.checkState(loc != null);
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public int getFSS_Index() {return fssIndex;}
		
		public Location getLocation() {return loc;}
		
		public double getMagnitude() {return mag;}
	}

	
	public static Location getMojaveTestLoc(double horzDist) {
		Location loc = new Location(34.698495, -118.508948, 6.550000191);
		if(horzDist == 0.0)
			return loc;
		else {
			LocationVector vect = new LocationVector((295.037-270.0), horzDist, 0.0);
			return LocationUtils.location(loc, vect);			
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
//		Location testLoc = new Location(32.57336, -118.25770, 9.05759);
//		CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
//		System.out.println(griddedRegion.indexForLocation(testLoc));
//		System.out.println(griddedRegion.contains(testLoc));
		
//		for(int i=0;i<griddedRegion.getNodeCount();i++) {
//			Location loc = griddedRegion.getLocation(i);
//			System.out.println(loc.getLongitude()+"\t"+loc.getLatitude());
//		}
		
//		for(Location loc:griddedRegion.getBorder()) {
//			System.out.println(loc.getLongitude()+"\t"+loc.getLatitude());
//		}
//			
//
//		System.exit(-1);
		
		// temporary hack
		AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF = 2.55;
		
		ETAS_ParameterList params = new ETAS_ParameterList();
//		params.setApplyLongTermRates(false);
//		params.set_d_MinDist(2.0);
//		params.setImposeGR(false);
//		runTest(TestScenario.NEAR_MAACAMA, params, new Long(1407965202664l), "nearMaacama_1", null);
//		runTest(TestScenario.ON_MAACAMA, params, new Long(1407965202664l), "onMaacama_1", null);
		
//		runTest(TestScenario.ON_N_MOJAVE, params, new Long(1407965202664l), "OnN_Mojave_1", null);
//		runTest(TestScenario.NEAR_N_MOJAVE_3KM, params, new Long(1407965202664l), "NearN_Mojave_3KM_1", null);
//		runTest(TestScenario.LA_HABRA_6p2, params, null, "LaHabraTest_1", null);
//		runTest(null, params, null, "NoMainshockTest_1", null);
//		runTest(null, params, null, "HistCatalogTest_2", getHistCatalog());
//		runTest(TestScenario.NAPA, params, 1409022950070l, "Napa failure", null);
//		runTest(TestScenario.NAPA, params, 1409243011639l, "NapaEvent_noSpont_uniform_2", null);
		runTest(TestScenario.NAPA, params, 1409709441451l, "NapaEvent_maxLoss", null);
//		runTest(TestScenario.MOJAVE, params, new Long(14079652l), "MojaveEvent_noSpnont_1", null);	// aveStrike=295.0367915096109

		
//		runTest(TestScenario.PARKFIELD, params, new Long(14079652l), "ParkfieldTest_noSpnont_1", null);	// aveStrike=295.0367915096109
//		runTest(TestScenario.BOMBAY_BEACH_M6, params, new Long(14079652l), "BombayBeachTest_noSpnont_1", null);	// aveStrike=295.0367915096109


		// ************** OLD STUFF BELOW *********************
		
		
//		try {
//			runBugReproduce();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion = RELM_RegionUtils.getGriddedRegionInstance();
//		System.out.println(griddedRegion.getNumLocations());
//		
//		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED();
//		System.out.println(region.getNumLocations());
//		System.exit(-1);
		
		
		// test to make sure M>2.5 events included:
//		SummedMagFreqDist mfd = ERF_Calculator.getTotalMFD_ForERF(erf, 0.05, 8.95, 90, true);
//		GraphWindow graph = new GraphWindow(mfd, "Test ERF MFD"); 
		
		// make bulge plots:
//		try {
////			GMT_CA_Maps.plotBulgeFromFirstGenAftershocksMap(erf, "1stGenBulgePlotCorrected", "test bulge", "1stGenBulgePlotCorrectedDir", true);
//			GMT_CA_Maps.plotBulgeFromFirstGenAftershocksMap(erf, "1stGenBulgePlot", "test bulge", "1stGenBulgePlotCorrDir", false);
////			FaultBasedMapGen.plotBulgeFromFirstGenAftershocksMap((InversionFaultSystemSolution)erf.getSolution(), griddedRegion, null, "testBulge", true, true);
////			FaultBasedMapGen.plotBulgeForM6pt7_Map((InversionFaultSystemSolution)erf.getSolution(), griddedRegion, null, "testBulge", true, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.exit(0);
				
//		// examine bulge reduction scaling factors
//		SummedMagFreqDist[] subMFD_Array = FaultSystemSolutionCalc.getSubSeismNucleationMFD_inGridNotes((InversionFaultSystemSolution)erf.getSolution(), griddedRegion);
//		SummedMagFreqDist[] supraMFD_Array = FaultSystemSolutionCalc.getSupraSeismNucleationMFD_inGridNotes((InversionFaultSystemSolution)erf.getSolution(), griddedRegion);
//		
//		double min=Double.MAX_VALUE;
//		int minIndex=-1;
//		for(int i=0;i<subMFD_Array.length ;i++) {
//			if(subMFD_Array[i] != null) {
//				double scaleFactor = ETAS_Utils.getScalingFactorToImposeGR(supraMFD_Array[i], subMFD_Array[i]);
//				if(scaleFactor<min) {
//					min = scaleFactor;
//					minIndex=i;
//				}
//			}
//		}
//		System.out.println("maxIndex="+minIndex+"; max="+min);
//		double minFactor = ETAS_Utils.getScalingFactorToImposeGR(supraMFD_Array[5739], subMFD_Array[5739]);
//		System.out.println("Location for min scaleFactor: "+griddedRegion.getLocation(5739)+"/tminFactor="+minFactor);
//		EvenlyDiscretizedFunc testMFD = new EvenlyDiscretizedFunc(2.55, 8.95, 65);
//		EvenlyDiscretizedFunc testMFDcorr = new EvenlyDiscretizedFunc(2.55, 8.95, 65);
//		SummedMagFreqDist tempMFD = new SummedMagFreqDist(2.55, 8.95, 65);
//		SummedMagFreqDist tempMFDcorr = new SummedMagFreqDist(2.55, 8.95, 65);
//		tempMFD.addIncrementalMagFreqDist(supraMFD_Array[5739]);
//		tempMFD.addIncrementalMagFreqDist(subMFD_Array[5739]);
//		tempMFDcorr.addIncrementalMagFreqDist(supraMFD_Array[5739]);
//		tempMFDcorr.scale(minFactor);
//		tempMFDcorr.addIncrementalMagFreqDist(subMFD_Array[5739]);
//		for(int i=0;i<testMFD.getNum();i++) {
//			testMFD.set(i, tempMFD.getY(i)*Math.pow(10d, testMFD.getX(i)));
//			testMFDcorr.set(i, tempMFDcorr.getY(i)*Math.pow(10d, testMFD.getX(i)));
//		}
//		testMFD.setName("testMFD");
//		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
//		funcs.add(testMFDcorr);
//		funcs.add(testMFD);
//		GraphWindow graph = new GraphWindow(funcs, "Test GR Corr"+" "); 

	

		
		
//		double minDist=Double.MAX_VALUE;
//		int minDistIndex=-1;
//		for(FaultSectionPrefData fltData:erf.getSolution().getRupSet().getFaultSectionDataList()){
//			double dist = fltData.getStirlingGriddedSurface(1.0, false, true).getDistanceRup(ptSurf);
//			if(dist<minDist) {
//				minDist=dist;
//				minDistIndex=fltData.getSectionId();
//			}
//		}
//		System.out.println("minDist="+minDist+"; minDistIndex="+minDistIndex);
//		FaultSectionPrefData fltData = erf.getSolution().getRupSet().getFaultSectionDataList().get(minDistIndex);
//		System.out.println(fltData.getName());
//		System.out.println(fltData.getStirlingGriddedSurface(1.0, false, true));

	}
}
