package scratch.kevin.simulators.momRateVariation;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.dom4j.DocumentException;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityOptions;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.EventRecord;
import org.opensha.sha.simulators.RectangularElement;
import org.opensha.sha.simulators.Vertex;
import org.opensha.sha.simulators.iden.ElementMagRangeDescription;
import org.opensha.sha.simulators.iden.RuptureIdentifier;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.IDPairing;
import scratch.kevin.markov.EmpiricalMarkovChain;
import scratch.kevin.simulators.MarkovChainBuilder;
import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.SynchIdens;
import scratch.kevin.simulators.SynchIdens.SynchFaults;
import scratch.kevin.simulators.catBuild.RandomCatalogBuilder;
import scratch.kevin.simulators.dists.RandomDistType;
import scratch.kevin.simulators.synch.RecurrencePlotGen;
import scratch.kevin.simulators.synch.SynchParamCalculator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UCERF3ComparisonAnalysis {
	
	private static Map<Integer, RectangularElement> loadElements(FaultSystemRupSet rupSet) {
		Map<Integer, RectangularElement> elems = Maps.newHashMap();
		
		for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList()) {
			Vertex[] vertices = new Vertex[4];
			
			StirlingGriddedSurface surf = sect.getStirlingGriddedSurface(1d);
			FaultTrace upper = surf.getEvenlyDiscritizedUpperEdge();
			FaultTrace lower = surf.getEvenlyDiscritizedLowerEdge();
			
			vertices[0] = new Vertex(upper.first());
			vertices[1] = new Vertex(upper.last());
			vertices[2] = new Vertex(lower.last());
			vertices[3] = new Vertex(lower.first());
			
			RectangularElement elem = new RectangularElement(sect.getSectionId(), vertices, sect.getSectionName(),
					sect.getParentSectionId(), sect.getParentSectionId(), 0, 0, sect.getOrigAveSlipRate(),
					sect.getAseismicSlipFactor(), null, false);
			
			elems.put(sect.getSectionId(), elem);
		}
		
		return elems;
	}
	
	private static class UCERF3EventRecord extends EventRecord {
		
		private List<RectangularElement> rupElems;

		public UCERF3EventRecord(Map<Integer, RectangularElement> elems, FaultSystemRupSet rupSet, int rupIndex, double time) {
			super(null);
			
			setID(rupIndex);
			setMagnitude(rupSet.getMagForRup(rupIndex));
			setMoment(MagUtils.magToMoment(rupSet.getMagForRup(rupIndex)));
			setTime(time);
			
			rupElems = Lists.newArrayList();
			for (int sectID : rupSet.getSectionsIndicesForRup(rupIndex))
				rupElems.add(elems.get(sectID));
			
			int[] elemIDs = new int[rupElems.size()];
			for (int i=0; i<rupElems.size(); i++)
				elemIDs[i] = rupElems.get(i).getID();
			
			setElementIDsAndSlips(elemIDs, null);
		}

		@Override
		public List<RectangularElement> getRectangularElements() {
			return rupElems;
		}
		
	}
	
	private static List<RuptureIdentifier> buildUCERF3_EquivIdens(List<RuptureIdentifier> idens,
			List<RectangularElement> origElems, Map<Integer, RectangularElement> ucerf3ElemsMap,
			FaultSystemRupSet rupSet) {
		List<RuptureIdentifier> ucerf3Idens = Lists.newArrayList();
		
		Map<Integer, RectangularElement> origElemsMap = Maps.newHashMap();
		for (RectangularElement elem : origElems)
			origElemsMap.put(elem.getID(), elem);
		
		for (RuptureIdentifier iden : idens) {
			Preconditions.checkState(iden instanceof ElementMagRangeDescription,
					"Cannot convert iden of type: %s", ClassUtils.getClassNameWithoutPackage(iden.getClass()));
			ElementMagRangeDescription elemIden = (ElementMagRangeDescription)iden;
			List<Integer> mappedIDs = Lists.newArrayList();
			for (int origID : elemIden.getElementIDs()) {
				RectangularElement elem = origElemsMap.get(origID);
				Preconditions.checkNotNull(elem);
				// find closest match
				Location center = elem.getCenterLocation();
				double minDist = Double.POSITIVE_INFINITY;
				RectangularElement closest = null;
				for (RectangularElement u3Elem : ucerf3ElemsMap.values()) {
					if (u3Elem.getName().toLowerCase().contains("stepover"))
						// we don't want stepovers
						continue;
					if (rupSet.getFaultSectionData(u3Elem.getID()).getDateOfLastEvent() == Long.MIN_VALUE)
						// only sections with open intervals
						continue;
					Location u3Center = u3Elem.getCenterLocation();
					double dist = LocationUtils.horzDistanceFast(center, u3Center);
					if (dist < minDist) {
						minDist = dist;
						closest = u3Elem;
					}
				}
				System.out.println("Matched "+elem.getName()+" to "+closest.getName()+", dist="+minDist+" km");
				mappedIDs.add(closest.getID());
			}
			ucerf3Idens.add(new ElementMagRangeDescription("U3 "+iden.getName(), mappedIDs,
					elemIden.getMinMag(), elemIden.getMaxMag()));
		}
		
		return ucerf3Idens;
	}
	
	private static List<EQSIM_Event> loadCatalogAsFakeSimEvents(FaultSystemSolution sol, Region region,
			File eventFile, Map<Integer, RectangularElement> elems, int startYear) throws IOException {
		List<EQSIM_Event> events = Lists.newArrayList();
		
		FaultSystemRupSet rupSet = sol.getRupSet();
		
		HashSet<Integer> sectIndexesInRegion;
		if (region == null) {
			sectIndexesInRegion = null;
		} else {
			sectIndexesInRegion = new HashSet<Integer>();
			for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList()) {
				for (Location loc : sect.getFaultTrace()) {
					if (region.contains(loc)) {
						sectIndexesInRegion.add(sect.getSectionId());
						break;
					}
				}
			}
		}
		
		BufferedReader read = new BufferedReader(new FileReader(eventFile));
		
		String line;
		
		long startTime = new GregorianCalendar(startYear, 0, 1).getTimeInMillis();
		
		while ((line = read.readLine()) != null) {
			if (line.contains("nthRupIndex"))
				continue; //header
			line = line.trim();
			
			// nthRupIndex	fssRupIndex	year	epoch	normRI	mag	nthCatalog	timeToNextInYrs	utilizedPaleoSite
			
			String[] split = line.split("\t");
			int fssIndex = Integer.parseInt(split[1]);
			long epoch = Long.parseLong(split[3]);
			
			if (sectIndexesInRegion != null) {
				boolean inside = false;
				for (int index : rupSet.getSectionsIndicesForRup(fssIndex)) {
					if (sectIndexesInRegion.contains(index)) {
						inside = true;
						break;
					}
				}
				if (!inside)
					continue;
			}
			
//			if (startTime == Long.MIN_VALUE)
//				startTime = epoch;
			
			long millis = epoch - startTime;
			Preconditions.checkState(millis >= 0l);
			double secs = (double)millis / 1000d;
			
			EventRecord rec = new UCERF3EventRecord(elems, rupSet, fssIndex, secs);
			
			EQSIM_Event e = new EQSIM_Event(rec);
			
			events.add(e);
		}
		
		read.close();
		
		return events;
	}
	
	private static List<EQSIM_Event> calcPoissonCatalog(FaultSystemSolution sol,
			Map<Integer, RectangularElement> elems, double duration) {
		
		FaultSystemRupSet rupSet = sol.getRupSet();
		
		List<EQSIM_Event> events = Lists.newArrayList();
		
		double expectedRate = 0d;
		for (int fssIndex=0; fssIndex<sol.getRupSet().getNumRuptures(); fssIndex++) {
			double rate = sol.getRateForRup(fssIndex);
			expectedRate += rate;
			
			if (rate == 0d)
				continue;
			
			double expectation = rate*duration;
			PoissonDistribution poisson = new PoissonDistribution(expectation);
			int occurances = poisson.sample();
			
			for (int i=0; i<occurances; i++) {
				double timeYears = Math.random()*duration;
				double timeSecs = timeYears*General_EQSIM_Tools.SECONDS_PER_YEAR;
				
				events.add(new EQSIM_Event(new UCERF3EventRecord(elems, rupSet, fssIndex, timeSecs)));
			}
		}
		
		double actualRate = (double)events.size()/duration;
		
		System.out.println("Poisson catalog has "+events.size()+" events in "+duration+" years");
		System.out.println("\tExpected rate: "+expectedRate);
		System.out.println("\tActual rate: "+actualRate);
		
		Collections.sort(events);
		
		return events;
	}
	
	/**
	 * Chooses a random time between events
	 * @param events
	 * @return
	 */
	private static double getRandomTimeBetween(List<EQSIM_Event> events) {
		// random int between 1 and size-1
		int randIndex = new Random().nextInt(events.size()-1)+1;
		double t0 = events.get(randIndex-1).getTime();
		double t1 = events.get(randIndex).getTime();
		
		double delta = t1 - t0;
		Preconditions.checkState(delta >= 0);
		
		return delta;
	}
	
	private static List<EQSIM_Event> stitch(List<List<EQSIM_Event>> eventsList) {
		List<EQSIM_Event> stitched = Lists.newArrayList();
		
		double timeSecs = 0;
		
		int id = 0;
		
		for (int i=0; i<eventsList.size(); i++) {
			// move forward in time one random recurrence interval then stitch in new catalog
			if (i > 0)
				timeSecs += getRandomTimeBetween(stitched);
			
			List<EQSIM_Event> events = eventsList.get(i);
//			Preconditions.checkState(events.get(0).getTime() == 0d, "Bad start time: %s", events.get(0).getTime());
			
			for (EQSIM_Event e : events)
				stitched.add(e.cloneNewTime(timeSecs+e.getTime(), id++));
			
			timeSecs = stitched.get(stitched.size()-1).getTime();
		}
		
		return stitched;
	}
	
	/**
	 * This will generate fake events to establish the open interval on each fault under consideration when
	 * building a markov chain.
	 * 
	 * @param rupSet
	 * @param u3Idens
	 * @param elems
	 * @return
	 */
	private static List<EQSIM_Event> getFakePreEvents(FaultSystemRupSet rupSet, List<RuptureIdentifier> u3Idens,
			Map<Integer, RectangularElement> elems, int startYear) {
		List<EQSIM_Event> events = Lists.newArrayList();
		
		long startTime = new GregorianCalendar(startYear, 0, 1).getTimeInMillis();
		
		for (RuptureIdentifier u3Iden : u3Idens) {
			Preconditions.checkState(u3Iden instanceof ElementMagRangeDescription);
			ElementMagRangeDescription iden = (ElementMagRangeDescription)u3Iden;
			int sectIndex = iden.getElementIDs().get(0);
			FaultSectionPrefData sect = rupSet.getFaultSectionData(sectIndex);
			Preconditions.checkState(sect.getDateOfLastEvent() > Long.MIN_VALUE, "No open interval for: %s", sect.getName());
			
			long millis = sect.getDateOfLastEvent() - startTime;
			Preconditions.checkState(millis < 0l);
			double secs = (double)millis / 1000d;
			
			int fssIndex = -1;
			double mag = Double.POSITIVE_INFINITY;
			// find smallest rupture over min magnitude
			for (int r : rupSet.getRupturesForSection(sectIndex)) {
				double myMag = rupSet.getMagForRup(r);
				if (myMag > iden.getMinMag() && myMag < mag) {
					mag = myMag;
					fssIndex = r;
				}
			}
			Preconditions.checkState(fssIndex >= 0);
			
			EventRecord rec = new UCERF3EventRecord(elems, rupSet, fssIndex, secs);
			
			EQSIM_Event e = new EQSIM_Event(rec);
			
			events.add(e);
		}
		
		Collections.sort(events);
		
		return events;
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
		File fssFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip");
		
//		File mainDir = new File("/home/kevin/Simulators/time_series/ucerf3_compare/2015_07_30-MID_VALUES");
//		MagDependentAperiodicityOptions cov = MagDependentAperiodicityOptions.MID_VALUES;
//		int[] batches = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		
//		File mainDir = new File("/home/kevin/Simulators/time_series/ucerf3_compare/2015_08_05-HIGH_VALUES");
//		MagDependentAperiodicityOptions cov = MagDependentAperiodicityOptions.HIGH_VALUES;
//		int[] batches = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		
		File mainDir = new File("/home/kevin/Simulators/time_series/ucerf3_compare/2015_08_05-LOW_VALUES");
		MagDependentAperiodicityOptions cov = MagDependentAperiodicityOptions.LOW_VALUES;
		int[] batches = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		
		int startYear = 2014;
		
		String dirPrefix = "10000yr";
		int threads = 8;
		File outputDir = new File(mainDir, dirPrefix+"_runs_combined");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		Region region = new CaliforniaRegions.RELM_SOCAL();
		
		FaultSystemSolution sol = FaultSystemIO.loadSol(fssFile);
		
		List<List<EQSIM_Event>> eventsList = Lists.newArrayList();
		List<Double> catalogLengths = Lists.newArrayList();
		
		int[] windowLens = { 10, 25, 50, 75, 100, 150, 200 };
//		int[] windowLens = { 25 };
		
		boolean doSynch = false;
		boolean doRecurrence = true;
		boolean doRecurrencePoisson = true;
		
		Map<Integer, RectangularElement> elems = loadElements(sol.getRupSet());
		
		for (int batch : batches) {
			File batchDir;
			if (batch < 0)
				batchDir = mainDir;
			else
				batchDir = new File(mainDir, "batch"+batch);
			for (int i=0; i<threads; i++) {
				File runDir = new File(batchDir, dirPrefix+"_run"+i);
				List<EQSIM_Event> events = loadCatalogAsFakeSimEvents(
						sol, region, new File(runDir, "sampledEventsData.txt"), elems, startYear);
				double calcDuration = events.get(events.size()-1).getTimeInYears() - events.get(0).getTimeInYears();
				System.out.println("Loaded "+events.size()+" fake UCERF3 events, duration: "+calcDuration+" years");
				
				File tsPlot = new File(runDir, "ts_plot.png");
				SimulatorMomRateVarCalc.plotMomRateVar(events, windowLens, "Fake UCERF3", 0,
						(int)calcDuration, true, false, tsPlot);
				
				for (int windowLen : windowLens) {
					File outputFile = new File(runDir, "ucerf3_"+runDir.getName()+"_"+windowLen+"yr.bin");
					SimulatorMomRateVarCalc.writeMomRateTimeSeries(windowLen, events, outputFile);
				}
				
				eventsList.add(events);
				catalogLengths.add(calcDuration);
			}
		}
		
		// now stitch
		List<EQSIM_Event> stitched = stitch(eventsList);
		double calcDuration = stitched.get(stitched.size()-1).getTimeInYears() - stitched.get(0).getTimeInYears();
		System.out.println("Loaded "+stitched.size()+" stitched UCERF3 events, duration: "+calcDuration+" years");
		System.out.println("\tStitched Rate: "+(stitched.size()/calcDuration));
		
		File tsPlot = new File(outputDir, "ts_plot.png");
		SimulatorMomRateVarCalc.plotMomRateVar(stitched, windowLens, "Fake UCERF3 Stitched", 0,
				(int)calcDuration, true, false, tsPlot);
		
		for (int windowLen : windowLens) {
			File outputFile = new File(outputDir, "ucerf3_"+outputDir.getName()+"_"+windowLen+"yr.bin");
			SimulatorMomRateVarCalc.writeMomRateTimeSeries(windowLen, stitched, outputFile);
		}
		
		// now synch analysis
		List<SynchFaults[]> faultSets = Lists.newArrayList();
		
		faultSets.add(new SynchFaults[] {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAF_CARRIZO});
		faultSets.add(new SynchFaults[] {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_CARRIZO, SynchFaults.SAF_COACHELLA,
				SynchFaults.SAF_CHOLAME, SynchFaults.SAN_JACINTO});
		
		int numCatalogs = 3;
		
		List<EQSIM_Event> poissonEvents = null;
		if (doRecurrence && doRecurrencePoisson)
			poissonEvents = calcPoissonCatalog(sol, elems, 30000d);
		
		for (SynchFaults[] faults : faultSets) {
			List<RuptureIdentifier> origIdens = SynchIdens.getIndividualFaults(7, 10d, faults);
			List<RectangularElement> origElems = SimAnalysisCatLoader.loadGeomOnly();
			List<RuptureIdentifier> rupIdens = buildUCERF3_EquivIdens(origIdens, origElems, elems, sol.getRupSet());
			double distSpacing = 10d;
			boolean normalize = true;
			
			if (doRecurrence) {
				File recurDir = new File("/home/kevin/Simulators/recurrence_plots");
				Preconditions.checkState(recurDir.exists() || recurDir.mkdir());
				
				if (poissonEvents != null) {
					File poissonDir = new File(recurDir, "ucerf3_poisson");
					Preconditions.checkState(poissonDir.exists() || poissonDir.mkdir());
					
					poissonDir = new File(poissonDir, Joiner.on("_").join(Lists.newArrayList(faults)));
					Preconditions.checkState(poissonDir.exists() || poissonDir.mkdir());
					
					List<int[]> fullPath = MarkovChainBuilder.getStatesPath(distSpacing, poissonEvents, rupIdens, 0);
					
					RecurrencePlotGen.plotRecurrence(poissonDir, fullPath, distSpacing, normalize);
				}
				
				File recurSubDir = new File(recurDir, "ucerf3_"+cov.name());
				Preconditions.checkState(recurSubDir.exists() || recurSubDir.mkdir());
				
				recurSubDir = new File(recurSubDir, Joiner.on("_").join(Lists.newArrayList(faults)));
				Preconditions.checkState(recurSubDir.exists() || recurSubDir.mkdir());
				
				List<EQSIM_Event> preEvents = getFakePreEvents(sol.getRupSet(), rupIdens, elems, startYear);
				double oiBefore = preEvents.get(preEvents.size()-1).getTimeInYears();
				Preconditions.checkState(oiBefore < 0);
				int skipStates = (int)(-oiBefore/distSpacing + 0.5);
				System.out.println("Fake event OI established at "+oiBefore+" years, skipping "+skipStates+" states");
				
				// sort individual catalogs by length
				List<List<EQSIM_Event>> sortedLists = ComparablePairing.getSortedData(catalogLengths, eventsList);
				// reverse to get from longest to shortest
				Collections.reverse(sortedLists);
				if (numCatalogs > sortedLists.size())
					numCatalogs = sortedLists.size();
				
				for (int i=0; i<numCatalogs; i++) {
					List<EQSIM_Event> catalog = Lists.newArrayList(eventsList.get(i));
					double origCatLen = catalog.get(catalog.size()-1).getTimeInYears() - catalog.get(0).getTimeInYears();
					// add in pre events so that it starts with a fully fleshed out open interval on each fault
					catalog.addAll(0, preEvents);
					double newCatLen = catalog.get(catalog.size()-1).getTimeInYears() - catalog.get(0).getTimeInYears();
					double addedYears = newCatLen - origCatLen;
					int approxAddedStates = (int)(addedYears/distSpacing + 0.5);
					System.out.println("Catalog "+i+" lenght: "+origCatLen);
					System.out.println("Length with pre events: "+newCatLen+", added "
							+addedYears+" ("+approxAddedStates+" states)");
					
					List<int[]> fullPath = MarkovChainBuilder.getStatesPath(distSpacing, catalog, rupIdens, 0);
					fullPath = fullPath.subList(skipStates, fullPath.size());
					
					File mySubDir = new File(recurSubDir, "catalog"+i);
					if (mySubDir.exists())
						FileUtils.deleteRecursive(mySubDir);
					Preconditions.checkState(mySubDir.mkdir());
					
					RecurrencePlotGen.plotRecurrence(mySubDir, fullPath, distSpacing, normalize);
				}
			}
		}
		
//		if (doSynch) {
//			File synchDir = new File(outputDir, "synch");
//			Preconditions.checkState(synchDir.exists() || synchDir.mkdir());
//			
//			File markovPlotsDir = new File(synchDir, "markov_plots");
//			if (!markovPlotsDir.exists())
//				markovPlotsDir.mkdir();
//			
//			SynchParamCalculator.plotMarkovNumTransHist(chain, rupIdens, markovPlotsDir);
//
//			// ccdfs/acdfs
//			File ccdfDir = new File(synchDir, "ccdfs");
//			if (!ccdfDir.exists())
//				ccdfDir.mkdir();
//
//			System.out.println("Generating CCDFs...");
//			Map<IDPairing, HistogramFunction[]> catDensFuncs =
//					PeriodicityPlotter.plotACDF_CCDFs(ccdfDir, stitched, rupIdens,
//							null, null, 2000d, distSpacing);
//
//			int lagMax = 30;
//
//			System.out.println("Calculating Synch Params");
//			// write synch CSV
//			File synchCSVFile = new File(synchDir, "synch_params.csv");
//			SynchParamCalculator.writeSynchParamsTable(synchCSVFile, rupIdens, chain, catDensFuncs, lagMax);
//			
//			SynchParamCalculator.writeSynchVsProbTable(new File(synchDir, "synch_compare_prob_gain.csv"),
//					stitched, rupIdens, chain);
////			writeSynchVsProbTable(new File(writeDir, "synch_compare_prob_gain_excl_corup.csv"),
////					myEvents, rupIdens, chain, false);
//
//			// now write std devs
//			//		System.out.println("Calculating Synch Std Dev/Biases");
//			//		writeSynchParamsStdDev(writeDir, myEvents, rupIdens, chain, new int[] {0}, 100, distSpacing);
//
//			// now do std devs for each lag
//			//		System.out.println("Calculating Synch Lag Std Devs/Biases");
//			//		writeSynchParamsStdDev(writeDir, myEvents, rupIdens, chain, rangeInclusive(-20, 20), 100, distSpacing);
//			
//			// write inter event time dists
//			List<Color> colors = SynchIdens.getStandardColors();
//			File distsDir = new File(synchDir, "inter_event_dists");
//			if (!distsDir.exists())
//				distsDir.mkdir();
//			PeriodicityPlotter.plotPeriodsAndEvents(stitched, false, false, distsDir,
//					rupIdens, colors, false);
//		}
	}

}
