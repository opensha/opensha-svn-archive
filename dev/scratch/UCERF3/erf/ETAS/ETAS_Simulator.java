package scratch.UCERF3.erf.ETAS;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.zip.ZipException;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupOrigTimeComparator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
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
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.analysis.FaultSystemSolutionCalc;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class ETAS_Simulator {
	
	public static boolean D=true; // debug flag
	
	
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
	 * @throws IOException 

	 */
	public static void testETAS_Simulation(File resultsDir, FaultSystemSolutionERF_ETAS erf,
			GriddedRegion griddedRegion, List<ObsEqkRupture> obsEqkRuptureList, boolean includeSpontEvents,
			boolean includeIndirectTriggering, boolean includeEqkRates, double gridSeisDiscr, String simulationName,
			Long randomSeed)
					throws IOException {
		testETAS_Simulation(resultsDir, erf, griddedRegion, obsEqkRuptureList, includeSpontEvents,
				includeIndirectTriggering, includeEqkRates, gridSeisDiscr, simulationName,
				randomSeed, null, null);
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
	 * @param fractionSrcAtPointList
	 * @param srcAtPointList
	 * @throws IOException 

	 */
	public static void testETAS_Simulation(File resultsDir, FaultSystemSolutionERF_ETAS erf,
			GriddedRegion griddedRegion, List<ObsEqkRupture> obsEqkRuptureList, boolean includeSpontEvents,
			boolean includeIndirectTriggering, boolean includeEqkRates, double gridSeisDiscr, String simulationName,
			Long randomSeed, List<float[]> fractionSrcAtPointList, List<int[]> srcAtPointList)
					throws IOException {
		
		ETAS_Utils etas_utils;
		if(randomSeed != null)
			etas_utils = new ETAS_Utils(randomSeed);
		else
			etas_utils = new ETAS_Utils(System.currentTimeMillis());
		
		SeisDepthDistribution seisDepthDistribution = new SeisDepthDistribution(etas_utils);
		
		// directory for saving results
		if(!resultsDir.exists()) resultsDir.mkdir();
		
		// file for writing simulations info
		FileWriter info_fr= new FileWriter(new File(resultsDir, "infoString.txt"));
		FileWriter simulatedEventsFileWriter = new FileWriter(new File(resultsDir, "simulatedEvents.txt"));


		info_fr.write(simulationName+"\n");
		info_fr.write("\nrandomSeed="+etas_utils.getRandomSeed()+"\n");
		info_fr.write("\nobsEqkRuptureList.size()="+obsEqkRuptureList.size()+"\n");
		info_fr.write("includeSpontEvents="+includeSpontEvents+"\n");
		info_fr.write("includeIndirectTriggering="+includeIndirectTriggering+"\n");
		info_fr.write("includeEqkRates="+includeEqkRates+"\n");
		
		info_fr.write("\nERF Adjustable Paramteres:\n\n");
		for(Parameter param : erf.getAdjustableParameterList()) {
			info_fr.write("\t"+param.getName()+" = "+param.getValue()+"\n");
		}
		TimeSpan tsp = erf.getTimeSpan();
		String startTimeString = tsp.getStartTimeMonth()+"/"+tsp.getStartTimeDay()+"/"+tsp.getStartTimeYear()+"; hr="+tsp.getStartTimeHour()+"; min="+tsp.getStartTimeMinute()+"; sec="+tsp.getStartTimeSecond();
		info_fr.write("\nERF StartTime: "+startTimeString+"\n");
		info_fr.write("\nERF TimeSpan Duration: "+erf.getTimeSpan().getDuration()+" years\n");
			
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
		
		//Compute origTotRate; this calculation include rates outside region, 
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
		for(int s=0;s<erf.getNumSources();s++) {
			ProbEqkSource src = erf.getSource(s);
			sourceRates[s] = src.computeTotalEquivMeanAnnualRate(duration);
			for(ProbEqkRupture rup:src) {
				spontaneousRupSampler.set(nthRup, rup.getMeanAnnualRate(duration));
				nthRup+=1;
			}
		}
		if(D) System.out.println("\tspontaneousRupSampler.calcSumOfY_Vals()="+(float)spontaneousRupSampler.calcSumOfY_Vals() +
				"; that took (sec): "+(float)(System.currentTimeMillis()-st)/1000f);
		
		// Make list of primary aftershocks for given list of obs quakes 
		// (filling in origin time ID, and parentID, with the rest to be filled in later)
		if (D) System.out.println("Making primary aftershocks from input obsEqkRuptureList, size = "+obsEqkRuptureList.size());
		PriorityQueue<ETAS_EqkRupture>  eventsToProcess = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);	// not sure about the first field
		HashMap<Integer,ObsEqkRupture> mainshockHashMap = new HashMap<Integer,ObsEqkRupture>(); // this stores the active main shocks
		HashMap<Integer,Integer> mainshockNumToProcess = new HashMap<Integer,Integer>();	// this keeps track of how many more aftershocks a main shock needs to generate
		int parID=0;	// this will be used to assign an id to the given events
		int eventID = obsEqkRuptureList.size();	// start IDs after input events
		for(ObsEqkRupture rup: obsEqkRuptureList) {
			long rupOT = rup.getOriginTime();
			double startDay = (double)(simStartTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
			// get a list of random primary event times, in units of days sin main shock
			double[] randomAftShockTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
			if(randomAftShockTimes.length>0) {
				for(int i=0; i<randomAftShockTimes.length;i++) {
					long ot = rupOT +  (long)(randomAftShockTimes[i]*(double)ProbabilityModelsCalc.MILLISEC_PER_DAY);	// convert to milliseconds
					ETAS_EqkRupture newRup = new ETAS_EqkRupture(rup, eventID, ot);
					newRup.setParentID(parID);
					newRup.setGeneration(1);
					eventsToProcess.add(newRup);
					eventID +=1;
				}
				mainshockHashMap.put(parID, rup);
				mainshockNumToProcess.put(parID,randomAftShockTimes.length);
				parID += 1;				
			}
		}
		if (D) System.out.println("The "+obsEqkRuptureList.size()+" input events produced "+eventsToProcess.size()+" primary aftershocks");
		info_fr.write("\nThe "+obsEqkRuptureList.size()+" input observed events produced "+eventsToProcess.size()+" primary aftershocks\n");

		
		// make the list of spontaneous events, filling in only event IDs and origin times for now
		if(includeSpontEvents) {
			if (D) System.out.println("Making spontaneous events and times of primary aftershocks...");
			double fractionNonTriggered=0.33;	// value for Jeanne; really need to solve for this value
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
		
		if(D) System.out.println("Making ETAS_PrimaryEventSampler");
		st = System.currentTimeMillis();

		ETAS_PrimaryEventSamplerTest1 etas_PrimEventSampler = new ETAS_PrimaryEventSamplerTest1(griddedRegion, erf, sourceRates, 
				gridSeisDiscr,null, includeEqkRates, etas_utils);
		if (fractionSrcAtPointList != null && srcAtPointList != null)
			etas_PrimEventSampler.setSrcAtPointCaches(fractionSrcAtPointList, srcAtPointList);
		if(D) System.out.println("ETAS_PrimaryEventSampler creation took "+(float)(System.currentTimeMillis()-st)/60000f+ " min");
		info_fr.write("\nMaking ETAS_PrimaryEventSampler took "+(System.currentTimeMillis()-st)/60000+ " min");

		// TODO this could be based on supraseis rupture (or rups with more than one point on surface)
		if(D && obsEqkRuptureList.size()==1) {	// assume the one event is some big test event (e.g., Landers)
			long rupOT = obsEqkRuptureList.get(0).getOriginTime();
			double startDay = (double)(simStartTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
			double expNum = ETAS_Utils.getDefaultExpectedNumEvents(obsEqkRuptureList.get(0).getMag(), startDay, endDay);
			ETAS_SimAnalysisTools.plotExpectedPrimaryMFD_ForRup("first event in obsEqkRuptureList", new File(resultsDir,"firstObsRupExpPrimMFD").getAbsolutePath(), 
					etas_PrimEventSampler, obsEqkRuptureList.get(0), expNum);
		}
		
		double etasDistDecay = etas_PrimEventSampler.getDistDecay();
		double etasMinDist = etas_PrimEventSampler.getMinDist();
		double maxDepthKm = etas_PrimEventSampler.getMaxDepth();
		
		info_fr.write("\n\netasDistDecay="+etasDistDecay+";\tetasMinDist="+etasMinDist+";\tmaxDepthKm="+maxDepthKm+"\n");

		if(D) {
			System.out.println("Testing the etas_PrimEventSampler");
			etas_PrimEventSampler.testRates();
		}
		// time consuming:
//		etas_PrimEventSampler.testMagFreqDist(); TODO what is this?


//		System.out.println("sleeping for 10 secs");
//		try {
//			Thread.sleep(10000L);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		CalcProgressBar progressBar;
		try {
			progressBar = new CalcProgressBar("Primary aftershocks to process", "junk");
			progressBar.showProgress(true);
		} catch (Exception e) {
			// headless, don't show it
			progressBar = null;
		}
		
		if (D) System.out.println("Looping over eventsToProcess (initial num = "+eventsToProcess.size()+")...\n");
		if (D) System.out.println("\tFault system ruptures triggered (date\tmag\tname\tnthRup,src,rupInSrc,fltSysRup):");
		info_fr.write("\nFault system ruptures triggered (date\tmag\tname\tnthRup,src,rupInSrc,fltSysRup):\n");

		st = System.currentTimeMillis();
		
		int numSimulatedEvents = 0;
		
		while(eventsToProcess.size()>0) {
			
			if (progressBar != null) progressBar.updateProgress(numSimulatedEvents, eventsToProcess.size()+numSimulatedEvents);
			
			ETAS_EqkRupture rup = eventsToProcess.poll();	//Retrieves and removes the head of this queue, or returns null if this queue is empty.
			
			parID = rup.getParentID();
			int numToProcess=-1;
			
			boolean succeededInSettingRupture=true;
			
			// the following samples an nth rup and populates the hypo loc.
			if(parID == -1)	{ // it's a spontaneous event
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
					int hypIndex = (int)(etas_utils.getRandomDouble()*(double)surfPts.size());	// choose random loc assuming uniform probability among points
					hypoLoc = surfPts.get(hypIndex);
				}
				
				rup.setAveRake(erf_rup.getAveRake());
				rup.setMag(erf_rup.getMag());
				RuptureSurface surf = erf_rup.getRuptureSurface();
				rup.setRuptureSurface(surf);
				rup.setNthERF_Index(nthRup);
				rup.setHypocenterLocation(hypoLoc);

			}
			else {
				numToProcess = mainshockNumToProcess.get(parID);	// this is the number of events the sampler has yet to process
				EqkRupture mainshock = mainshockHashMap.get(parID);
				succeededInSettingRupture = etas_PrimEventSampler.setRandomPrimaryEvent(mainshock, rup);
//etas_PrimEventSampler.getTriggerProbOfEachSource( mainshock);
//System.exit(0);
				numToProcess -= 1;	// decrement num to process
			}
			
			// update num to process or remove mainshock if this is zero
			if(parID != -1) {	// if not spontaneous
				if(numToProcess == 0) {
					mainshockNumToProcess.remove(parID);
					mainshockHashMap.remove(parID);
				}
				else {	// update the num to process
					mainshockNumToProcess.put(parID, numToProcess);
				}	
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
				parID = rup.getID();	// rupture is now the parent
				int gen = rup.getGeneration()+1;
				double startDay = 0;	// starting at origin time since we're within the timespan
				double endDay = (double)(simEndTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
				double[] eventTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
				if(eventTimes.length>0) {
					for(int i=0; i<eventTimes.length;i++) {
						long ot = rupOT +  (long)(eventTimes[i]*(double)ProbabilityModelsCalc.MILLISEC_PER_DAY);
						ETAS_EqkRupture newRup = new ETAS_EqkRupture(rup, eventID, ot);
						newRup.setGeneration(gen);
						newRup.setParentID(parID);
						eventsToProcess.add(newRup);
						eventID +=1;
					}
					mainshockHashMap.put(parID, rup);
					mainshockNumToProcess.put(parID,eventTimes.length);				
				}		
			}
			
			
			// if it was a fault system rupture, need to update time span, rup rates, block, and samplers.
			nthRup = rup.getNthERF_Index();
			int srcIndex = erf.getSrcIndexForNthRup(nthRup);

			if(srcIndex<erf.getNumFaultSystemSources()) {
				
				nthFaultSysRupAftershocks.add(nthRup);
				int fltSysRupIndex = erf.getFltSysRupIndexForNthRup(nthRup);

				// set the start time for the time dependent calcs
				erf.getTimeSpan().setStartTimeInMillis(rupOT);	
				
				Toolkit.getDefaultToolkit().beep();
				if(D) System.out.println("GOT A FAULT SYSTEM RUPTURE!");
				TimeSpan ts = erf.getTimeSpan();
				String rupString = "\t"+ts.getStartTimeMonth()+"/"+ts.getStartTimeDay()+"/"+ts.getStartTimeYear()+"\tmag="+(float)rup.getMag()+"\t"+erf.getSource(srcIndex).getName()
						+"\t"+nthRup+","+srcIndex+","+erf.getRupIndexInSourceForNthRup(nthRup)+","+fltSysRupIndex;
				if(D) System.out.println(rupString);
				info_fr.write(rupString+"\n");

				// set the date of last event for this rupture
				erf.setFltSystemSourceOccurranceTime(srcIndex, rupOT);

				// now update source rates for etas_PrimEventSampler
				if(D) System.out.print("\tUpdating src rates for etas_PrimEventSampler & spontaneousRupSampler; ");
				Long st2 = System.currentTimeMillis();
				if(erf.getParameter(ProbabilityModelParam.NAME).getValue() != ProbabilityModelOptions.POISSON) {
					erf.updateForecast();
					for(int s=0;s<erf.getNumFaultSystemSources();s++) {
						ProbEqkSource src = erf.getSource(s);
						double oldRate = sourceRates[s];
						sourceRates[s] = src.computeTotalEquivMeanAnnualRate(duration);
						double newRate = sourceRates[s];
						// TEST THAT RATE CHANGED PROPERLY
						if(s == erf.getSrcIndexForNthRup(nthRup)) {
							if(D)System.out.print("for rup that occurred, oldRate="+(float)oldRate+" & newRate = "+(float)newRate+"\n");			
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
				}
				String tempFileName = new File(resultsDir,"FltSysRup"+fltSysRupIndex+"_ExpPrimMFD").getAbsolutePath();
				if (D) ETAS_SimAnalysisTools.plotExpectedPrimaryMFD_ForRup("Triggered Supra Seis Flt Sys Rup #"+fltSysRupIndex, tempFileName, etas_PrimEventSampler, erf.getNthRupture(nthRup), Double.NaN);
				if (D) System.out.println("; sampler update took "+(System.currentTimeMillis()-st2)/1000+" secs");
			}
		}
		
		if (progressBar != null) progressBar.showProgress(false);

		if(D) System.out.println("\nLooping over events took "+(System.currentTimeMillis()-st)/1000+" secs");
		info_fr.write("\nLooping over events took "+(System.currentTimeMillis()-st)/1000+" secs\n");

//		ETAS_SimAnalysisTools.writeEventDataToFile("testRightHere.txt", simulatedRupsQueue);

		if(D && obsEqkRuptureList.size()==1) {	// assume the one event is some big test event (e.g., Landers)
			System.out.println("Doing ETAS_SimAnalysisTools.plotEpicenterMap...");
			ETAS_SimAnalysisTools.plotEpicenterMap("", new File(resultsDir,"hypoMap.pdf").getAbsolutePath(), obsEqkRuptureList.get(0), simulatedRupsQueue, griddedRegion.getBorder());
			System.out.println("Doing ETAS_SimAnalysisTools.plotDistDecayHistForAshocks...");
			ETAS_SimAnalysisTools.plotDistDecayHistForAshocks("", new File(resultsDir,"distDecay.pdf").getAbsolutePath(), simulatedRupsQueue, obsEqkRuptureList.get(0), etasDistDecay, etasMinDist);
			System.out.println("Doing ETAS_SimAnalysisTools.plotNumVsLogTime...");
			ETAS_SimAnalysisTools.plotNumVsLogTimePrimaryAftershocks("", new File(resultsDir,"logTimeDecay.pdf").getAbsolutePath(), simulatedRupsQueue);
			System.out.println("Doing ETAS_SimAnalysisTools.plotNumVsTime...");
			ETAS_SimAnalysisTools.plotNumVsTimePrimaryAftershocks("", new File(resultsDir,"timeDecay.pdf").getAbsolutePath(), simulatedRupsQueue);
		} else if (D) {
			System.out.println("Doing ETAS_SimAnalysisTools.plotEpicenterMap...");
			ETAS_SimAnalysisTools.plotEpicenterMap("test", new File(resultsDir,"hypoMap.pdf").getAbsolutePath(), null, simulatedRupsQueue, griddedRegion.getBorder());
			System.out.println("Doing ETAS_SimAnalysisTools.plotDistDecayHistForAshocks...");
			ETAS_SimAnalysisTools.plotDistDecayHistForAshocks("test", null, simulatedRupsQueue, null, etasDistDecay, etasMinDist);
		}
		if(D) System.out.println("Doing ETAS_SimAnalysisTools.plotMagFreqDists...");
		if(D) ETAS_SimAnalysisTools.plotMagFreqDists("", new File(resultsDir,"aftMFD.pdf").getAbsolutePath(), simulatedRupsQueue);
		
		if(D) System.out.println("Total num ruptures: "+simulatedRupsQueue.size());
		info_fr.write("Total num ruptures: "+simulatedRupsQueue.size()+"\n");
		
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
				System.out.println("Source "+s+"\t"+erf.getSource(s).getName());
				break;
			}
			firstIsIt = rupSet.getFaultSectionData(sectListForSrc.get(0)).getSectionId() == secondSectID;
			lastIsIt = rupSet.getFaultSectionData(sectListForSrc.get(sectListForSrc.size()-1)).getSectionId() == firstSectID;
			if(firstIsIt && lastIsIt) {
				System.out.println("Source "+s+"\t"+erf.getSource(s).getName());
				break;
			}
		}
	}
	
	
	
	public static void runMojaveTest() {
		
		System.out.println("Starting ERF instantiation");
		Long st = System.currentTimeMillis();
		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolutionERF_ETAS erf = new FaultSystemSolutionERF_ETAS(fileName);
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

		
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		
		ObsEqkRupture mainshockRup = new ObsEqkRupture();
		Long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		mainshockRup.setOriginTime(ot);	

		// Mojave M 7.05 rupture
		int fssRupID=197792;
		int srcID = erf.getSrcIndexForFltSysRup(fssRupID);

		ProbEqkRupture rupFromERF = erf.getSource(srcID).getRupture(0);
		mainshockRup.setAveRake(rupFromERF.getAveRake());
		mainshockRup.setMag(rupFromERF.getMag());
		mainshockRup.setRuptureSurface(rupFromERF.getRuptureSurface());
		System.out.println("test Mainshock: "+erf.getSource(srcID).getName());
		
		String simulationName = "Mojave7pt0_run5";
//		String simulationName = "Mojave7pt0_noER_noGRcorr_run2";
//		String simulationName = "Mojave7pt0_noER_run2";
		// This sets the rupture as having occurred in the ERF (to apply elastic rebound)
		erf.setFltSystemSourceOccurranceTime(srcID, ot);
		erf.updateForecast();
		
		ArrayList<ObsEqkRupture> obsEqkRuptureList = new ArrayList<ObsEqkRupture>();
		obsEqkRuptureList.add(mainshockRup);

		
		boolean includeSpontEvents=true;
		boolean includeIndirectTriggering=true;
		boolean includeEqkRates = true;
		double gridSeisDiscr = 0.1;
		
		System.out.println("Starting testETAS_Simulation");
		Long randomSeed = new Long(100);
		try {
			String dirNameForSavingFiles = "U3_ETAS_"+simulationName+"/";
			File resultsDir = new File(dirNameForSavingFiles);
			testETAS_Simulation(resultsDir, erf, griddedRegion, obsEqkRuptureList,  includeSpontEvents, 
					includeIndirectTriggering, includeEqkRates, gridSeisDiscr, simulationName, randomSeed);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		float timeMin = (float)(System.currentTimeMillis()-st)/60000f;
		System.out.println("Total simulation took "+timeMin+" min");

	}

	
	
	public static void runLandersTest() {
		
		System.out.println("Starting ERF instantiation");
		Long st = System.currentTimeMillis();
		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolutionERF_ETAS erf = new FaultSystemSolutionERF_ETAS(fileName);
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

		
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		
		ObsEqkRupture mainshockRup = new ObsEqkRupture();
		Long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		mainshockRup.setOriginTime(ot);	

		// Landers-like rupture; found using writeInfoAboutSourceWithThisFirstAndLastSection(erf, 243, 989)
		int srcID = 246139;	// Landers


		ProbEqkRupture rupFromERF = erf.getSource(srcID).getRupture(0);
		mainshockRup.setAveRake(rupFromERF.getAveRake());
		mainshockRup.setMag(rupFromERF.getMag());
		mainshockRup.setRuptureSurface(rupFromERF.getRuptureSurface());
		System.out.println("test Mainshock: "+erf.getSource(srcID).getName());
		
		String simulationName = "Landers_run1";
		// This sets the rupture as having occurred in the ERF (to apply elastic rebound)
		erf.setFltSystemSourceOccurranceTime(srcID, ot);
		erf.updateForecast();
		
		ArrayList<ObsEqkRupture> obsEqkRuptureList = new ArrayList<ObsEqkRupture>();
		obsEqkRuptureList.add(mainshockRup);

		
		boolean includeSpontEvents=true;
		boolean includeIndirectTriggering=true;
		boolean includeEqkRates = true;
		double gridSeisDiscr = 0.1;
		
		System.out.println("Starting testETAS_Simulation");
		try {
			String dirNameForSavingFiles = "U3_ETAS_"+simulationName+"/";
			File resultsDir = new File(dirNameForSavingFiles);
			testETAS_Simulation(resultsDir, erf, griddedRegion, obsEqkRuptureList,  includeSpontEvents, 
					includeIndirectTriggering, includeEqkRates, gridSeisDiscr, simulationName, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		float timeMin = (float)(System.currentTimeMillis()-st)/60000f;
		System.out.println("Total simulation took "+timeMin+" min");

	}
	
	public static void runNorthridgeTest() {
		
		System.out.println("Starting ERF instantiation");
		Long st = System.currentTimeMillis();
		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolutionERF_ETAS erf = new FaultSystemSolutionERF_ETAS(fileName);
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

		
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		
		ObsEqkRupture mainshockRup = new ObsEqkRupture();
		Long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		mainshockRup.setOriginTime(ot);	

		// Northridge-like rupture; found using writeInfoAboutSourceWithThisFirstAndLastSection(erf, 1409, 1413);	
		int srcID = 187124;	// Northridge


		ProbEqkRupture rupFromERF = erf.getSource(srcID).getRupture(0);
		mainshockRup.setAveRake(rupFromERF.getAveRake());
		mainshockRup.setMag(rupFromERF.getMag());
		mainshockRup.setRuptureSurface(rupFromERF.getRuptureSurface());
		System.out.println("test Mainshock: "+erf.getSource(srcID).getName());
		
		String simulationName = "Northridge_run1";
		// This sets the rupture as having occurred in the ERF (to apply elastic rebound)
		erf.setFltSystemSourceOccurranceTime(srcID, ot);
		erf.updateForecast();
		
		ArrayList<ObsEqkRupture> obsEqkRuptureList = new ArrayList<ObsEqkRupture>();
		obsEqkRuptureList.add(mainshockRup);

		
		boolean includeSpontEvents=true;
		boolean includeIndirectTriggering=true;
		boolean includeEqkRates = true;
		double gridSeisDiscr = 0.1;
		
		System.out.println("Starting testETAS_Simulation");
		try {
			String dirNameForSavingFiles = "U3_ETAS_"+simulationName+"/";
			File resultsDir = new File(dirNameForSavingFiles);
			testETAS_Simulation(resultsDir, erf, griddedRegion, obsEqkRuptureList,  includeSpontEvents, 
					includeIndirectTriggering, includeEqkRates, gridSeisDiscr, simulationName, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		float timeMin = (float)(System.currentTimeMillis()-st)/60000f;
		System.out.println("Total simulation took "+timeMin+" min");

	}

	
	
	public static void runLaHabraTest() {
		
		System.out.println("Starting ERF instantiation");
		Long st = System.currentTimeMillis();
		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolutionERF_ETAS erf = new FaultSystemSolutionERF_ETAS(fileName);
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

		
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		String simulationName = "LaHabra6pt2_run1";
		
		ObsEqkRupture mainshockRup = new ObsEqkRupture();
		Long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		mainshockRup.setOriginTime(ot);	

		// 3-29-14 M 5.1 La Habra Earthquake
		Location ptSurf = new Location(33.932,-117.917,4.8);	//
		double mag = 6.2;	// test bigger magnitude
		mainshockRup.setAveRake(0.0);
		mainshockRup.setMag(mag);
		mainshockRup.setPointSurface(ptSurf);
		
		ArrayList<ObsEqkRupture> obsEqkRuptureList = new ArrayList<ObsEqkRupture>();
		obsEqkRuptureList.add(mainshockRup);

		
		boolean includeSpontEvents=true;
		boolean includeIndirectTriggering=true;
		boolean includeEqkRates = true;
		double gridSeisDiscr = 0.1;
		
		System.out.println("Starting testETAS_Simulation");
		try {
			String dirNameForSavingFiles = "U3_ETAS_"+simulationName+"/";
			File resultsDir = new File(dirNameForSavingFiles);
			testETAS_Simulation(resultsDir, erf, griddedRegion, obsEqkRuptureList,  includeSpontEvents, 
					includeIndirectTriggering, includeEqkRates, gridSeisDiscr, simulationName, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		float timeMin = (float)(System.currentTimeMillis()-st)/60000f;
		System.out.println("Total simulation took "+timeMin+" min");

	}

	
	public static void runNoMainShockTest() {
		
		System.out.println("Starting ERF instantiation");
		Long st = System.currentTimeMillis();
		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolutionERF_ETAS erf = new FaultSystemSolutionERF_ETAS(fileName);
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

		
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		String simulationName = "NoMainShock_run1";
		
		ArrayList<ObsEqkRupture> obsEqkRuptureList = new ArrayList<ObsEqkRupture>();

		
		boolean includeSpontEvents=true;
		boolean includeIndirectTriggering=true;
		boolean includeEqkRates = true;
		double gridSeisDiscr = 0.1;
		
		System.out.println("Starting testETAS_Simulation");
		try {
			String dirNameForSavingFiles = "U3_ETAS_"+simulationName+"/";
			File resultsDir = new File(dirNameForSavingFiles);
			testETAS_Simulation(resultsDir, erf, griddedRegion, obsEqkRuptureList,  includeSpontEvents, 
					includeIndirectTriggering, includeEqkRates, gridSeisDiscr, simulationName, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		float timeMin = (float)(System.currentTimeMillis()-st)/60000f;
		System.out.println("Total simulation took "+timeMin+" min");

	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// temporary hack
		AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF = 2.55;
		
		
		
//		ETAS_Simulator.runLandersTest();
//		ETAS_Simulator.runNorthridgeTest();
		ETAS_Simulator.runMojaveTest();
//		ETAS_Simulator.runLaHabraTest();
//		ETAS_Simulator.runNoMainShockTest();
		
		
		
		// ************** OLD STUFF BELOW *********************
		
//		// For mean solution ERF
//		System.out.println("Starting ERF instantiation");
//		Long st = System.currentTimeMillis();
//		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
//		FaultSystemSolutionERF_ETAS erf = new FaultSystemSolutionERF_ETAS(fileName);

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
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
//		// set parameters
//		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
//		erf.setParameter(BackgroundRupParam.NAME, BackgroundRupType.POINT);
//		erf.setParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME, false);
//		erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.U3_BPT);
//		erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.MID_VALUES);
//		BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE;
//		erf.setParameter(BPTAveragingTypeParam.NAME, aveType);
//		erf.setParameter(AleatoryMagAreaStdDevParam.NAME, 0.0);
//		erf.getParameter(HistoricOpenIntervalParam.NAME).setValue(2014d-1875d);	
//		System.out.println("Setting timeSpan start");
//		erf.getTimeSpan().setStartTimeInMillis(Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR)+1);
//		System.out.println("Setting timeSpan duration");
//		erf.getTimeSpan().setDuration(1);
//		
//		erf.updateForecast();
//		
//		float timeSec = (float)(System.currentTimeMillis()-st)/1000f;
//		System.out.println("ERF instantiation took "+timeSec+" sec");
		
		
		
		
		// test to make sure M>2.5 events included:
//		SummedMagFreqDist mfd = ERF_Calculator.getTotalMFD_ForERF(erf, 0.05, 8.95, 90, true);
//		GraphWindow graph = new GraphWindow(mfd, "Test ERF MFD"); 
		
		
//		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();

		
		
		// make bulge plots:
//		try {
////			GMT_CA_Maps.plotBulgeFromFirstGenAftershocksMap(erf, "1stGenBulgePlotCorrected", "test bulge", "1stGenBulgePlotCorrectedDir", true);
//			GMT_CA_Maps.plotBulgeFromFirstGenAftershocksMap(erf, "1stGenBulgePlot", "test bulge", "1stGenBulgePlotCorrDir", false);
////			FaultBasedMapGen.plotBulgeFromFirstGenAftershocksMap((InversionFaultSystemSolution)erf.getSolution(), griddedRegion, null, "testBulge", true, true);
////			FaultBasedMapGen.plotBulgeForM6pt7_Map((InversionFaultSystemSolution)erf.getSolution(), griddedRegion, null, "testBulge", true, true);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
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

	

//		// this is a main shock to test (the rest is filled in by one of the options below)
//		ObsEqkRupture mainshockRup = new ObsEqkRupture();
//		Long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
//		mainshockRup.setOriginTime(ot);	
//		ArrayList<ObsEqkRupture> obsEqkRuptureList = new ArrayList<ObsEqkRupture>();
//		
//		obsEqkRuptureList.add(mainshockRup);

		
// ************* THIS SETS IT AS A FAULT-SYSTEM-SOLUTION RUPTURE FROM THE ERF ****************:
		
		// Landers-like rupture; found using writeInfoAboutSourceWithThisFirstAndLastSection(erf, 243, 989)
//		int srcID = 246139;	// Landers
		
		// Northridge-like rupture; found using writeInfoAboutSourceWithThisFirstAndLastSection(erf, 1409, 1413);	
//		int srcID = 187124;	// Northridge
		
		// Mojave M 7.05 rupture
//		int fssRupID=197792;
//		int srcID = erf.getSrcIndexForFltSysRup(fssRupID);

//		ProbEqkRupture rupFromERF = erf.getSource(srcID).getRupture(0);
//		mainshockRup.setAveRake(rupFromERF.getAveRake());
//		mainshockRup.setMag(rupFromERF.getMag());
//		mainshockRup.setRuptureSurface(rupFromERF.getRuptureSurface());
//		System.out.println("test Mainshock: "+erf.getSource(srcID).getName());
//		
//		// This sets the rupture as having occurred in the ERF (to apply elastic rebound)
//		System.out.println("Test Rate Before: "+erf.getSource(srcID).computeTotalEquivMeanAnnualRate(erf.getTimeSpan().getDuration()));
//		erf.setFltSystemSourceOccurranceTime(srcID, ot);
//		erf.updateForecast();
//		System.out.println("Test Rate After: "+erf.getSource(srcID).computeTotalEquivMeanAnnualRate(erf.getTimeSpan().getDuration()));
		
		
// ************* THIS SETS IT AS A POINT-SOURCE RUPTURE ****************:
		
		// 3-29-14 M 5.1 La Habra Earthquake
//		Location ptSurf = new Location(33.932,-117.917,4.8);	//
//		double mag = 6.2;	// test bigger magnitude

		// near Maacama to test most char MFD on fault sections
//		double mag = 7;
//		Location ptSurf = new Location(39.79509, -123.56665, 7.54615);	// right on point on surface
//		Location ptSurf = new Location(39.79509, -123.56665-0.04, 7.54615);	// not right on point on surface
		
//		mainshockRup.setAveRake(0.0);
//		mainshockRup.setMag(mag);
//		mainshockRup.setPointSurface(ptSurf);


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
////		System.out.println(fltData.getStirlingGriddedSurface(1.0, false, true));

		
// ************* DONE SETTING RUPTURE *********************
				
//		boolean includeSpontEvents=true;
//		boolean includeIndirectTriggering=true;
//		boolean includeEqkRates = true;
//		double gridSeisDiscr = 0.1;
//		
//		System.out.println("Starting testETAS_Simulation");
//		try {
//			testETAS_Simulation(erf, griddedRegion, obsEqkRuptureList,  includeSpontEvents, 
//					includeIndirectTriggering, includeEqkRates, gridSeisDiscr, "Mojave7pt0_run7");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		float timeMin = (float)(System.currentTimeMillis()-st)/60000f;
//		System.out.println("Total simulation took "+timeMin+" min");
	}
}
