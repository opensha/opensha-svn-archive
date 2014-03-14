package scratch.UCERF3.erf.ETAS;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.zip.ZipException;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.FileParameter;
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
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class ETAS_Simulator {
	
	
	/**
	 * This represents an ETAS simulation.  This assume ER probabilities are constant up until 
	 * the next fault-system event (only works if fault system events occur every few years or
	 * less).
	 * @param griddedRegion
	 * @param obsEqkRuptureList
	 * @param includeSpontEvents
	 * @param includeIndirectTriggering - include secondary, tertiary, etc events
	 * @param includeEqkRates - whether or not to include the long-term rate of events in sampling aftershocks
	 * @param gridSeisDiscr - lat lon discretization of gridded seismicity (degrees)

	 */
	public static void testETAS_Simulation(FaultSystemSolutionERF erf, GriddedRegion griddedRegion, ArrayList<ObsEqkRupture> obsEqkRuptureList, 
			boolean includeSpontEvents, boolean includeIndirectTriggering, boolean includeEqkRates, double gridSeisDiscr) {
		
		FaultSystemRupSet fltSysRupSet = erf.getSolution().getRupSet();
		
		// this will store the aftershocks & spontaneous events (in order of occurrence) - ObsEqkRuptureList? (they're added in order anyway)
		ObsEqkRupOrigTimeComparator otComparator = new ObsEqkRupOrigTimeComparator();	// this will keep the event in order of origin time
		PriorityQueue<ETAS_EqkRupture>  simulatedRupsQueue = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);
		
		ArrayList<Double> normalizedRupRecurIntervals = new ArrayList<Double>();
		
		// this is for keeping track of aftershocks on the fault system
		ArrayList<Integer> nthFaultSysRupAftershocks = new ArrayList<Integer>();
		
		long simStartTime = erf.getTimeSpan().getStartTimeCalendar().getTimeInMillis();
		long simEndTime = erf.getTimeSpan().getEndTimeCalendar().getTimeInMillis();
		double simDuration = erf.getTimeSpan().getDuration();
		
		System.out.println("Updating forecast (twice)");
		// get the total rate over the duration of the forecast
		erf.updateForecast();	// do this to get annual rate over the entire forecast (used to sample spontaneous events)
		
		// fill in totalRate, longTermRateOfNthRups, magOfNthRups, and longTermSlipRateForSectArray
		double origTotRate=0;
		IntegerPDF_FunctionSampler spontaneousRupSampler = new IntegerPDF_FunctionSampler(erf.getTotNumRups());
//		double[] longTermRateOfNthRups = new double[erf.getTotNumRups()];
//		double[] magOfNthRups = new double[erf.getTotNumRups()];
//		double[] longTermSlipRateForSectArray = new double[numSections];
		int nthRup=0;
		for(ProbEqkSource src:erf) {
			for(ProbEqkRupture rup:src) {
				double rate = rup.getMeanAnnualRate(erf.getTimeSpan().getDuration());
//				longTermRateOfNthRups[nthRup] = rate;
//				magOfNthRups[nthRup] = rup.getMag();
				origTotRate += rate;
				spontaneousRupSampler.set(nthRup, rate);
				if(erf.getSrcIndexForNthRup(nthRup)<erf.getNumFaultSystemSources()) {
					// slip rates
					int fltSysIndex = erf.getFltSysRupIndexForNthRup(nthRup);
					List<Integer> sectIndices = fltSysRupSet.getSectionsIndicesForRup(fltSysIndex);
					double slips[];
					if(fltSysRupSet instanceof InversionFaultSystemRupSet) {
						slips = ((InversionFaultSystemRupSet) fltSysRupSet).getSlipOnSectionsForRup(erf.getFltSysRupIndexForNthRup(nthRup));
					}
					else {	// apply ave to all sections
						double mag = fltSysRupSet.getMagForRup(erf.getFltSysRupIndexForNthRup(nthRup));
						double area = fltSysRupSet.getAreaForRup(erf.getFltSysRupIndexForNthRup(nthRup));
						double aveSlip = FaultMomentCalc.getSlip(area, MagUtils.magToMoment(mag));
						slips = new double[sectIndices.size()];
						for(int i=0;i<slips.length;i++)
							slips[i]=aveSlip;
					}
					for(int s=0;s<sectIndices.size();s++) {
						int sectID = sectIndices.get(s);
//						longTermSlipRateForSectArray[sectID] += rate*slips[s];
					}					
				}
				nthRup+=1;
			}
		}
		System.out.println("origTotRate="+origTotRate);
		
		
		
		// set to yearly probabilities for simulation forecast (in case input was not a 1-year forecast)
		erf.getTimeSpan().setDuration(1.0);	// annualize
		erf.updateForecast();
		
		
		ETAS_Utils etas_utils = new ETAS_Utils();

		// Make list of primary events for given list of obs quakes 
		// (filling in origin time ID, and parentID, with the rest to be filled in later)
		System.out.println("Making primary aftershocks from input obsEqkRuptureList, size = "+obsEqkRuptureList.size());
		PriorityQueue<ETAS_EqkRupture>  eventsToProcess = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);	// not sure about the first field
		HashMap<Integer,ObsEqkRupture> mainshockHashMap = new HashMap<Integer,ObsEqkRupture>(); // this stores the active mainshocks
		HashMap<Integer,Integer> mainshockNumToProcess = new HashMap<Integer,Integer>();	// this keeps track of how many more aftershocks a mainshock needs to generate
		int parID=0;	// this will be used to assign an id to the given events
		int eventID = obsEqkRuptureList.size();	// start IDs after input events
		for(ObsEqkRupture rup: obsEqkRuptureList) {
			long rupOT = rup.getOriginTime();
			double startDay = (double)(simStartTime-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTime-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
			// get a list of random primary event times
			double[] randomAftShockTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
			if(randomAftShockTimes.length>0) {
				for(int i=0; i<randomAftShockTimes.length;i++) {
					long ot = rupOT +  (long)(randomAftShockTimes[i]*(double)ProbabilityModelsCalc.MILLISEC_PER_DAY);	// convert to milliseconds
					ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID,ot);
					newRup.setGeneration(1);
					eventsToProcess.add(newRup);
					eventID +=1;
				}
				mainshockHashMap.put(parID, rup);
				mainshockNumToProcess.put(parID,randomAftShockTimes.length);
				parID += 1;				
			}
		}
		System.out.println("the "+obsEqkRuptureList.size()+" input events produced "+eventsToProcess.size()+" events");
		
		
		// make the list of spontaneous events, filling in only event IDs and origin times for now
		if(includeSpontEvents) {
			double fractionNonTriggered=0.5;	// really need to solve for this value
			double expectedNum = origTotRate*simDuration*fractionNonTriggered;
			System.out.println("expected num spontaneous: "+expectedNum+
					";\tfractionNonTriggered="+fractionNonTriggered+"; origTotRate="+origTotRate+"; origDuration="+simDuration);
			int numSpontEvents = etas_utils.getPoissonRandomNumber(expectedNum);
			System.out.println("Making spontaneous events (times and event IDs only) - "+numSpontEvents+" were sampled");

			for(int r=0;r<numSpontEvents;r++) {
				ETAS_EqkRupture rup = new ETAS_EqkRupture();
				double ot = simStartTime+Math.random()*(simEndTime-simStartTime);	// random time over time span
				rup.setOriginTime((long)ot);
				rup.setID(eventID);
				rup.setParentID(-1);		// parent is long-term model
				rup.setGeneration(0);
				eventsToProcess.add(rup);
				eventID += 1;
			}
		}
		
		Region regionForRates = new Region(griddedRegion.getBorder(),BorderType.MERCATOR_LINEAR);


		System.out.println("\nMaking ETAS_PrimaryEventSampler");
		long st = System.currentTimeMillis();
		// first make array of rates for each source
		double sourceRates[] = new double[erf.getNumSources()];
		double duration = erf.getTimeSpan().getDuration();
		for(int s=0;s<erf.getNumSources();s++)
			sourceRates[s] = erf.getSource(s).computeTotalEquivMeanAnnualRate(duration);

		// this is not yet used for anything
		String testFileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/testBinaryFile";

		ETAS_PrimaryEventSampler etas_PrimEventSampler = new ETAS_PrimaryEventSampler(regionForRates, erf, sourceRates, 
				gridSeisDiscr,null, includeEqkRates);
		System.out.println("that took "+(System.currentTimeMillis()-st)/1000+ " sec");
		
		double etasDistDecay = etas_PrimEventSampler.getDistDecay();
		double etasMinDist = etas_PrimEventSampler.getMinDist();
		double maxDepthKm = etas_PrimEventSampler.getMaxDepth();
		
		System.out.println("Testing the etas_PrimEventSampler");
		etas_PrimEventSampler.testRates();
		// time consuming:
//		etas_PrimEventSampler.testMagFreqDist();


//		System.out.println("sleeping for 10 secs");
//		try {
//			Thread.sleep(10000L);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// this will store the active aftershock samplers (which give the index of an event inside the erf_RatesInSpace)
//		HashMap<Integer,IntegerPDF_FunctionSampler> aftershockSamplerMap = new HashMap<Integer,IntegerPDF_FunctionSampler>();
//		IntegerPDF_FunctionSampler firstAftershockSampler=null;

		
		CalcProgressBar progressBar = new CalcProgressBar("Primary aftershocks to process", "junk");
		progressBar.showProgress(true);
		
		System.out.println("Looping over eventsToProcess (initial num = "+eventsToProcess.size()+")\n");
		st = System.currentTimeMillis();
		
		while(eventsToProcess.size()>0) {
			
			progressBar.updateProgress(simulatedRupsQueue.size(), eventsToProcess.size()+simulatedRupsQueue.size());
			
			
			ETAS_EqkRupture rup = eventsToProcess.poll();	//Retrieves and removes the head of this queue, or returns null if this queue is empty.
			
			parID = rup.getParentID();
			int numToProcess=-1;
			
			
			// the following samples an nth rup and populates the hypo loc.
			if(parID == -1)	{ // it's a spontaneous event
				Location hypoLoc = null;
				ProbEqkRupture erf_rup;
				nthRup = spontaneousRupSampler.getRandomInt();	// sample from long-term model
				erf_rup = erf.getNthRupture(nthRup);
				LocationList surfPts = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				if(surfPts.size() == 1) {// point source
					Location ptLoc = surfPts.get(0);
					// FOLLOWING ASSUMES A GRID SPACING OF 0.1 FOR BACKGROUND SEIS, AND UNIFORM DIST OF DEPTHS UP TO maxDepthKm
					// "0.99" is to keep it in cell
					hypoLoc = new Location(ptLoc.getLatitude()+(Math.random()-0.5)*0.1*0.99,
							ptLoc.getLongitude()+(Math.random()-0.5)*0.1*0.99,
							Math.random()*maxDepthKm*0.999);
				}
				else {
					int hypIndex = (int)(Math.random()*(double)surfPts.size());	// choose random loc; randomize more if point source!
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
				etas_PrimEventSampler.setRandomPrimaryEvent(mainshock, rup);
//etas_PrimEventSampler.getTriggerProbOfEachSource( mainshock);
//System.exit(0);
				numToProcess -= 1;	// decrement num to process
			}

			// add the rupture to the list
			simulatedRupsQueue.add(rup);
				
			// this isn't working:
// 			progressBar.setProgressMessage((float)rup.getMag()+"\t");
			
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
			
			long rupOT = rup.getOriginTime();
			
			// now sample primary aftershock times for this event
			if(includeIndirectTriggering) {
				parID = rup.getID();	// rupture is now the parent
				int gen = rup.getGeneration()+1;
				double startDay = 0;	// starting at origin time since we're within the timespan
				double endDay = (double)(simEndTime-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
				double[] eventTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
				if(eventTimes.length>0) {
					for(int i=0; i<eventTimes.length;i++) {
						long ot = rupOT +  (long)(eventTimes[i]*(double)ProbabilityModelsCalc.MILLISEC_PER_DAY);
						ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID, ot);
						newRup.setGeneration(gen);
						eventsToProcess.add(newRup);
						eventID +=1;
					}
					mainshockHashMap.put(parID, rup);
					mainshockNumToProcess.put(parID,eventTimes.length);				
				}		
			}
			
			
			// if it was a fault system rupture, need to update time span, rup rates, block, and samplers.
			nthRup = rup.getNthERF_Index();
			int src = erf.getSrcIndexForNthRup(nthRup);

			if(src<erf.getNumFaultSystemSources()) {
				
				nthFaultSysRupAftershocks.add(nthRup);
				
				Toolkit.getDefaultToolkit().beep();
				System.out.println("GOT A FAULT SYSTEM RUPTURE!");
				System.out.println("nthRup="+"mag="+rup.getMag()+";  "+erf.getSource(src).getName());

				// set the start time for the time dependent calcs
				erf.getTimeSpan().setStartTimeInMillis(rupOT);	
				
				// set the date of last event and slip for this rupture
				erf.setFltSystemSourceOccurranceTime(src, rupOT);

				// now update source rates for etas_PrimEventSampler
				System.out.println("Updating src rates for etas_PrimEventSampler");
				if(erf.getParameter(ProbabilityModelParam.NAME).getValue() != ProbabilityModelOptions.POISSON) {
					erf.updateForecast();
					for(int s=0;s<erf.getNumFaultSystemSources();s++) {
						double oldRate = sourceRates[s];
						sourceRates[s] = erf.getSource(s).computeTotalEquivMeanAnnualRate(duration);
						double newRate = sourceRates[s];
						// TEST THAT RATE CHANGED PROPERLY
						if(s == erf.getSrcIndexForNthRup(nthRup)) {
							System.out.println("\tFor rup that occurred: old rate = "+oldRate+"\tnew rate = "+newRate);			
						}
					}
					// now update the sampler
					etas_PrimEventSampler.declareRateChange();						
				}
				System.out.println("Done with fault system rupture updates");
			}
		}
		
		progressBar.showProgress(false);

		System.out.println("Looping over events took "+(System.currentTimeMillis()-st)/1000+" secs");

		System.out.println("Fault System Aftershocks:\n");
		for(Integer n : nthFaultSysRupAftershocks) {
			int s=erf.getSrcIndexForNthRup(n);
			System.out.println("\t"+n+"\t"+s+"\t"+erf.getRupIndexInSourceForNthRup(n)+"\t"+erf.getFltSysRupIndexForNthRup(n)+
					"\tmag="+erf.getNthRupture(n).getMag()+"\t"+erf.getSource(s).getName());

		}

		String dirName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/";
		
//		ETAS_SimAnalysisTools.writeDataToFile("testRightHere.txt", simulatedRupsQueue);

		if(obsEqkRuptureList.size()==1) {	// assume the one event is some big test event (e.g., Landers)
			ETAS_SimAnalysisTools.plotEpicenterMap("", dirName+"hypoMap.pdf", obsEqkRuptureList.get(0), simulatedRupsQueue, griddedRegion.getBorder());
			ETAS_SimAnalysisTools.plotDistDecayHistForAshocks("", dirName+"distDecay.pdf", simulatedRupsQueue, obsEqkRuptureList.get(0), etasDistDecay, etasMinDist);
			ETAS_SimAnalysisTools.plotNumVsLogTime("", dirName+"logTimeDecay.pdf", simulatedRupsQueue, obsEqkRuptureList.get(0));
			ETAS_SimAnalysisTools.plotNumVsTime("", dirName+"timeDecay.pdf", simulatedRupsQueue, obsEqkRuptureList.get(0));
		}
		else {
			ETAS_SimAnalysisTools.plotEpicenterMap("test", dirName+"hypoMap.pdf", null, simulatedRupsQueue, griddedRegion.getBorder());
			ETAS_SimAnalysisTools.plotDistDecayHistForAshocks("test", null, simulatedRupsQueue, null, etasDistDecay, etasMinDist);
		}
		ETAS_SimAnalysisTools.plotMagFreqDists("", dirName+"aftMFD.pdf", simulatedRupsQueue);
		
		System.out.println("Total num ruptures: "+simulatedRupsQueue.size());

	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
//		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(fileName);

		// Reference branch ERF:
		// U3.3 compuond file, assumed to be in data/scratch/InversionSolutions
		// download it from here: http://opensha.usc.edu/ftp/kmilner/ucerf3/2013_05_10-ucerf3p3-production-10runs/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip
		String fileName = "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip";
		File invDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File compoundFile = new File(invDir, fileName);
		CompoundFaultSystemSolution fetcher;
		FaultSystemSolutionERF erf  = null;
		try {
			fetcher = CompoundFaultSystemSolution.fromZipFile(compoundFile);
			LogicTreeBranch ref = LogicTreeBranch.DEFAULT;
			InversionFaultSystemSolution sol = fetcher.getSolution(ref);
			erf = new FaultSystemSolutionERF(sol);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// set parameters
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
		erf.setParameter(BackgroundRupParam.NAME, BackgroundRupType.POINT);
		erf.setParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME, false);
		erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.U3_BPT);
		erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.MID_VALUES);
//		BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RI_AVE_TIME_SINCE;
		BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE;
//		BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RATE_AVE_NORM_TIME_SINCE;
		erf.setParameter(BPTAveragingTypeParam.NAME, aveType);
		erf.setParameter(AleatoryMagAreaStdDevParam.NAME, 0.0);
		erf.getParameter(HistoricOpenIntervalParam.NAME).setValue(2014d-1850d);	
		erf.getTimeSpan().setDuration(1);
		erf.getTimeSpan().setStartTime(2014);
		
		erf.updateForecast();
		
		// test to make sure M>2.5 events included:
		SummedMagFreqDist mfd = ERF_Calculator.getTotalMFD_ForERF(erf, 0.05, 8.95, 90, true);
		GraphWindow graph = new GraphWindow(mfd, "Test ERF MFD"); 
		
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		
		// get the rupture index of a Landers rupture
//		int nthRup = erf.getIndexN_ForSrcAndRupIndices(4755, 0);
//		ProbEqkRupture landers = erf.getSource(4755).getRupture(0);
		
		ObsEqkRupture landersObs = new ObsEqkRupture();
//		landersObs.setAveRake(landers.getAveRake());
//		landersObs.setMag(landers.getMag());
//		landersObs.setRuptureSurface(landers.getRuptureSurface());
		
		landersObs.setAveRake(0.0);
		landersObs.setMag(6);
		Location ptSurf = new Location(34.30,-116.5,7.0);	//
		landersObs.setPointSurface(ptSurf);
		Long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		landersObs.setOriginTime(ot);	

//		System.out.println("main shock: s=4755, r=0, nthRup="+nthRup+"mag="+landersObs.getMag()+
//				"; src name: " +erf.getSource(4755).getName());
		
		ArrayList<ObsEqkRupture> obsEqkRuptureList = new ArrayList<ObsEqkRupture>();
		obsEqkRuptureList.add(landersObs);
		
//		erf.setRuptureOccurrenceTimePred(nthRup, 0);
		
		boolean includeSpontEvents=false;
		boolean includeIndirectTriggering=false;
		boolean includeEqkRates = false;
		double gridSeisDiscr = 0.1;
		
		testETAS_Simulation(erf, griddedRegion, obsEqkRuptureList,  includeSpontEvents, includeIndirectTriggering, includeEqkRates, gridSeisDiscr);

	}

}
