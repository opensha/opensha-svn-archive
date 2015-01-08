package org.opensha.sha.cybershake.etas;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.cybershake.AbstractModProbConfig;
import org.opensha.sha.cybershake.calc.RuptureProbabilityModifier;
import org.opensha.sha.cybershake.calc.RuptureVariationProbabilityModifier;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.eew.ZeroProbMod;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2_FM2pt1;
import scratch.kevin.ucerf3.etas.ETAS_CatalogStats;
import scratch.kevin.ucerf3.etas.MPJ_ETAS_Simulator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;

public class ETASModProbConfig extends AbstractModProbConfig {
	
	public enum ETAS_CyberShake_Scenarios {
		PARKFIELD("Parkfield Scenario", 8, 30473),
		BOMBAY_BEACH_M6("Bombay Beach Pt Scenario", 9, new Location(33.31833333333334,-115.72833333333335,5.8), 6.0),
		BOMBAY_BEACH_BRAWLEY_FAULT_M6("Bombay Beach Scenario", -1, 238408),
		MOJAVE_S_POINT_M6("Mojave M6 Point Scenario", -1, new Location(34.42295,-117.80177,5.8), 6.0),
		TEST_BOMBAY_M6_SUBSET("Bombay Beach M6 Scenario 50%", -1),
		TEST_BOMBAY_M6_SUBSET_FIRST("Bombay Beach M6 Scenario First Half", -1),
		TEST_BOMBAY_M6_SUBSET_SECOND("Bombay Beach M6 Scenario Second Half", -1),
		TEST_NEGLIGABLE("Test Negligable Scenario", -1),
		MAPPED_UCERF2("UCERF2 Time Indep", 10),
		MAPPED_UCERF2_TIMEDEP("UCERF2 Time Dep, no ETAS", 11);
		
		private int probModelID;
		private String name;
		private int triggerRupIndex;
		private Location triggerLoc;
		private double triggerMag;
		private ETAS_CyberShake_Scenarios(String name, int probModelID) {
			this(name, probModelID, -1);
		}
		
		private ETAS_CyberShake_Scenarios(String name, int probModelID, int triggerRupIndex) {
			this(name, probModelID, triggerRupIndex, null, Double.NaN);
		}
		
		private ETAS_CyberShake_Scenarios(String name, int probModelID, Location triggerLoc, double triggerMag) {
			this(name, probModelID, -1, triggerLoc, triggerMag);
		}
		
		private ETAS_CyberShake_Scenarios(String name, int probModelID, int triggerRupIndex,
				Location triggerLoc, double triggerMag) {
			this.triggerRupIndex = triggerRupIndex;
			this.triggerLoc = triggerLoc;
			this.triggerMag = triggerMag;
			this.probModelID = probModelID;
			this.name = name;
		}
		
		public boolean isETAS() {
			return triggerRupIndex >= 0 || triggerLoc != null;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public int getProbModelID() {
			return probModelID;
		}

		public int getTriggerRupIndex() {
			return triggerRupIndex;
		}

		public Location getTriggerLoc() {
			return triggerLoc;
		}

		public double getTriggerMag() {
			return triggerMag;
		}
	}
	
	public enum ETAS_Cybershake_TimeSpans {
		ONE_YEAR("One Year", 1, 1d),
		ONE_WEEK("One Week", 4, 7d/365.25),
		ONE_DAY("One Day", 2, 1d/365.25);
		
		int timeSpanID;
		String name;
		private double years;
		private ETAS_Cybershake_TimeSpans(String name, int timeSpanID, double years) {
			this.timeSpanID = timeSpanID;
			this.name = name;
			this.years = years;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public int getTimeSpanID() {
			return timeSpanID;
		}
		
		public double getTimeYears() {
			return years;
		}
	}
	
	private static final boolean calc_by_add_spontaneous = true;
	// if >0, all hypos within this distance will be promoted, not just closest
	private static final double hypocenter_buffer_km = 10d;
	
	private ETAS_CyberShake_Scenarios scenario;
	private ETAS_Cybershake_TimeSpans timeSpan;
	
	private File[] catalogsDirs;
	
	private long ot;
	private long endTime;
	
	private FaultSystemSolution sol;
	private List<List<ETAS_EqkRupture>> catalogs;
	
	// mapping from UCERF2 <sourceID, rupID> to UCERF3 FSS Index
//	private Table<Integer, Integer, Integer> rupMappingTable;
	// mapping from FSS Index to CyberShake UCERF2 source/rup
	private Map<Integer, IDPairing> rupMappingTable;
	private Map<IDPairing, Integer> rupMappingReverseTable;
	
	private ERF ucerf2;
	private ERF modifiedUCERF2;
//	private FaultSystemSolutionERF timeDepNoETAS_ERF;
	/** map from original modified source to original source */
	private Map<Integer, Integer> u2SourceMappings;
	/** map from original source to modified source */
	private Map<Integer, Integer> u2ModSourceMappings;
	/** map from original source ID, to modRupID, origRupID */
	private Map<Integer, Map<Integer, Integer>> rupIndexMappings;
	/** map from original source ID, to origRupID, modRupID */
	private Map<Integer, Map<Integer, Integer>> rupIndexMappingsReversed;
	
	private Map<Integer, Map<Location, List<Integer>>> rvHypoLocations;
	private Map<IDPairing, Map<Integer, Location>> hypoLocationsByRV;
	
	private Map<IDPairing, Map<Double, List<Integer>>> rvProbs;
	private List<RVProbSortable> rvProbsSortable;
	
	private double[][] fractOccurances;
	
	private double normalizedTriggerRate = 0d;
	
	// if set to true, will bump up probability for all RVs equally instead of the closest hypocenters to
	// the etas rupture hypos
	private boolean triggerAllHyposEqually = false;

	public ETASModProbConfig(ETAS_CyberShake_Scenarios scenario, ETAS_Cybershake_TimeSpans timeSpan,
			FaultSystemSolution sol, File catalogsDir, File mappingsCSVFile)
			throws IOException {
		this(scenario, timeSpan, sol, new File[] {catalogsDir}, mappingsCSVFile);
	}

	public ETASModProbConfig(ETAS_CyberShake_Scenarios scenario, ETAS_Cybershake_TimeSpans timeSpan,
			FaultSystemSolution sol, File[] catalogsDirs, File mappingsCSVFile)
			throws IOException {
		super(scenario+" ("+timeSpan+")", scenario.probModelID, timeSpan.timeSpanID);
		
		this.catalogsDirs = catalogsDirs;
		
		this.sol = sol;
		ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		endTime = ot + Math.round(timeSpan.years*ProbabilityModelsCalc.MILLISEC_PER_YEAR);
		
		System.out.println("Start time: "+ot);
		System.out.println("End time: "+endTime);
		
		this.scenario = scenario;
		this.timeSpan = timeSpan;
		
		ucerf2 = MeanUCERF2_ToDB.createUCERF2ERF();
		if (scenario == ETAS_CyberShake_Scenarios.MAPPED_UCERF2_TIMEDEP)
			loadMappings(mappingsCSVFile, MeanUCERF2.PROB_MODEL_WGCEP_PREF_BLEND);
		else
			loadMappings(mappingsCSVFile);
		
		// TODO use time dep ERF probs?
//		timeDepNoETAS_ERF = new FaultSystemSolutionERF(sol);
//		timeDepNoETAS_ERF.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
//		timeDepNoETAS_ERF.setParameter(BackgroundRupParam.NAME, BackgroundRupType.POINT);
//		timeDepNoETAS_ERF.setParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME, false);
//		timeDepNoETAS_ERF.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.U3_BPT);
//		timeDepNoETAS_ERF.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.MID_VALUES);
//		BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE;
//		timeDepNoETAS_ERF.setParameter(BPTAveragingTypeParam.NAME, aveType);
//		timeDepNoETAS_ERF.setParameter(AleatoryMagAreaStdDevParam.NAME, 0.0);
//		timeDepNoETAS_ERF.getParameter(HistoricOpenIntervalParam.NAME).setValue(2014d-1875d);	
//		timeDepNoETAS_ERF.getTimeSpan().setStartTimeInMillis(ot+1);
//		timeDepNoETAS_ERF.getTimeSpan().setDuration(duration);
	}
	
	private void loadCatalogs(File[] catalogsDirs) throws IOException {
		catalogs = Lists.newArrayList();
		
		for (File catalogsDir : catalogsDirs) {
			if (!catalogsDir.isDirectory()) {
				Preconditions.checkState(catalogsDir.getName().toLowerCase().endsWith(".zip"), "Must be directory or zip file!");
				loadCatalogsZip(catalogsDir);
				continue;
			}
			catalogs = Lists.newArrayList();
			
			int fssCount = 0;
			
			// load in the catalogs
			System.out.println("Loading catalogs from: "+catalogsDir.getAbsolutePath());
			for (File subDir : catalogsDir.listFiles()) {
				if (!subDir.isDirectory() || !MPJ_ETAS_Simulator.isAlreadyDone(subDir))
					continue;
				if (scenario == ETAS_CyberShake_Scenarios.TEST_BOMBAY_M6_SUBSET && Math.random() < 0.5)
					continue;
				File catalogFile = new File(subDir, "simulatedEvents.txt");
				Preconditions.checkState(catalogFile.exists());
				
				List<ETAS_EqkRupture> catalog = ETAS_SimAnalysisTools.loadCatalog(catalogFile, 5d);
				catalog = filterCatalog(catalog);
				
				catalogs.add(catalog);
				fssCount += catalog.size();
			}
			System.out.println("Loaded "+catalogs.size()+" catalogs ("+fssCount+" fault rups)");
			Preconditions.checkState(!catalogs.isEmpty(), "Must load at least one catalog!");
		}
		
		if (scenario == ETAS_CyberShake_Scenarios.TEST_BOMBAY_M6_SUBSET_FIRST)
			catalogs = catalogs.subList(0, catalogs.size()/2);
		else if (scenario == ETAS_CyberShake_Scenarios.TEST_BOMBAY_M6_SUBSET_SECOND)
			catalogs = catalogs.subList(catalogs.size()/2, catalogs.size());
	}
	
	private void loadCatalogsZip(File zipFile) throws ZipException, IOException {
		int fssCount = 0;
		
		ZipFile zip = new ZipFile(zipFile);
		
		List<? extends ZipEntry> entries = Collections.list(zip.entries());
		// sort for constant ordering
		Collections.sort(entries, new Comparator<ZipEntry>() {

			@Override
			public int compare(ZipEntry o1, ZipEntry o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		for (ZipEntry entry : entries) {
			if (!entry.isDirectory())
				continue;
//			System.out.println(entry.getName());
			String subEntryName = entry.getName()+"simulatedEvents.txt";
			ZipEntry catEntry = zip.getEntry(subEntryName);
			String infoEntryName = entry.getName()+"infoString.txt";
			ZipEntry infoEntry = zip.getEntry(infoEntryName);
			if (catEntry == null || infoEntry == null)
				continue;
			
			if (scenario == ETAS_CyberShake_Scenarios.TEST_BOMBAY_M6_SUBSET && Math.random() < 0.5)
				continue;
			
			// make sure it's actually done
			BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(infoEntry)));
			
			boolean done = false;
			for (String line : CharStreams.readLines(reader)) {
				if (line.contains("Total num ruptures: ")) {
					done = true;
					break;
				}
			}
			if (!done)
				continue;
//			System.out.println("Loading "+catEntry.getName());
			
			List<ETAS_EqkRupture> catalog;
			try {
				catalog = ETAS_SimAnalysisTools.loadCatalog(zip.getInputStream(catEntry), 5d);
			} catch (Exception e) {
				continue;
			}
			catalog = filterCatalog(catalog);
			
			catalogs.add(catalog);
			fssCount += catalog.size();
		}
		
		System.out.println("Loaded "+catalogs.size()+" catalogs ("+fssCount+" fault rups) from "+zipFile.getAbsolutePath());
		Preconditions.checkState(!catalogs.isEmpty(), "Must load at least one catalog!");
	}
	
	private List<ETAS_EqkRupture> filterCatalog(List<ETAS_EqkRupture> catalog) {
		// cull ruptures after end time
		for (int i=catalog.size(); --i >= 0;) {
			long rupTime = catalog.get(i).getOriginTime();
			if (rupTime > endTime)
				catalog.remove(i);
			else
				break;
		}
		
		if (calc_by_add_spontaneous)
			// only spontaneous, we're adding to the long term rates
			catalog = ETAS_SimAnalysisTools.getChildrenFromCatalog(catalog, 0);
		
		// now only FSS ruptures
		for (int i=catalog.size(); --i >= 0;)
			if (catalog.get(i).getFSSIndex() < 0)
				catalog.remove(i);
		
		return catalog;
	}
	
	private void loadMappings(File mappingsCSVFile) throws IOException {
		loadMappings(mappingsCSVFile, UCERF2.PROB_MODEL_POISSON);
	}
	
	private void loadMappings(File mappingsCSVFile, String u2ProbModel) throws IOException {
		modifiedUCERF2 = new ModMeanUCERF2_FM2pt1();

		modifiedUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		modifiedUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, u2ProbModel);
		modifiedUCERF2.getTimeSpan().setDuration(1d);
		modifiedUCERF2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
		modifiedUCERF2.updateForecast();
		
		// now we need to map from modified UCERF2 to regular UCERF2

		Map<String, Integer> origU2SourceNames = Maps.newHashMap();
		for (int sourceID=0; sourceID<ucerf2.getNumSources(); sourceID++)
			origU2SourceNames.put(ucerf2.getSource(sourceID).getName(), sourceID);
		Map<String, Integer> origU2ModSourceNames = Maps.newHashMap();
		for (int sourceID=0; sourceID<modifiedUCERF2.getNumSources(); sourceID++)
			origU2ModSourceNames.put(modifiedUCERF2.getSource(sourceID).getName(), sourceID);
		
		// map from modified source to original source
		u2SourceMappings = Maps.newHashMap();
		for (int sourceID=0; sourceID<modifiedUCERF2.getNumSources(); sourceID++)
			u2SourceMappings.put(sourceID, origU2SourceNames.get(modifiedUCERF2.getSource(sourceID).getName()));

		u2ModSourceMappings = Maps.newHashMap();
		for (int sourceID=0; sourceID<ucerf2.getNumSources(); sourceID++)
			u2ModSourceMappings.put(sourceID, origU2ModSourceNames.get(ucerf2.getSource(sourceID).getName()));
		
		// map rupture IDs now
		// map from original source ID, to <modRupID, origRupID>
		rupIndexMappings = Maps.newHashMap();
		for (int modSourceID=0; modSourceID<modifiedUCERF2.getNumSources(); modSourceID++) {
			int origSourceID = u2SourceMappings.get(modSourceID);
			
			Map<Integer, Integer> rupIndexMapping = rupIndexMappings.get(origSourceID);
			if (rupIndexMapping == null) {
				rupIndexMapping = Maps.newHashMap();
				rupIndexMappings.put(origSourceID, rupIndexMapping);
			}
			
			ProbEqkSource modSource = modifiedUCERF2.getSource(modSourceID);
			ProbEqkSource origSource = ucerf2.getSource(origSourceID);
			Preconditions.checkState(modSource.getName().equals(origSource.getName()));
			for (int modRupID=0; modRupID<modSource.getNumRuptures(); modRupID++) {
				ProbEqkRupture modRup = modSource.getRupture(modRupID);
				double closestDist = Double.POSITIVE_INFINITY;
				int closestOrigID = -1;
				
				for (int origRupID=0; origRupID<origSource.getNumRuptures(); origRupID++) {
					ProbEqkRupture origRup = origSource.getRupture(origRupID);
//					System.out.println("Testing mags "+modRup.getMag()+" "+origRup.getMag());
					if ((float)modRup.getMag() != (float)origRup.getMag())
						// mag must match
						continue;
					// distance between top left points
					double dist1 = LocationUtils.linearDistanceFast(
							origRup.getRuptureSurface().getFirstLocOnUpperEdge(),
							modRup.getRuptureSurface().getFirstLocOnUpperEdge());
					double dist2 = LocationUtils.linearDistanceFast(
							origRup.getRuptureSurface().getLastLocOnUpperEdge(),
							modRup.getRuptureSurface().getFirstLocOnUpperEdge());
					double dist3 = LocationUtils.linearDistanceFast(
							origRup.getRuptureSurface().getFirstLocOnUpperEdge(),
							modRup.getRuptureSurface().getLastLocOnUpperEdge());
					double dist = Math.min(dist1, Math.min(dist2, dist3));
//					System.out.println("dist="+dist);
					if (dist < closestDist) {
						closestDist = dist;
						closestOrigID = origRupID;
					}
				}
				if (closestDist > 10d)
					continue;
//				Preconditions.checkState(closestDist < 30d, "No mapping for mod source "+modSourceID+" rup "+modRupID
//						+". Closest with mag match was "+closestDist+" km away");
				rupIndexMapping.put(modRupID, closestOrigID);
			}
		}
		// now do mapping reversed
		rupIndexMappingsReversed = Maps.newHashMap();
		for (Integer sourceID : rupIndexMappings.keySet()) {
			Map<Integer, Integer> rupMappings = rupIndexMappings.get(sourceID);
			Map<Integer, Integer> rupMappingsReversed = Maps.newHashMap();
			for (Integer modRupID : rupMappings.keySet())
				rupMappingsReversed.put(rupMappings.get(modRupID), modRupID);
			rupIndexMappingsReversed.put(sourceID, rupMappingsReversed);
		}
		
		CSVFile<String> csv = CSVFile.readFile(mappingsCSVFile, true);
		
		Map<Integer, List<IDPairing>> rupCandidates = Maps.newHashMap();
		
		HashSet<Integer> fssIndexes = new HashSet<Integer>();
		
		for (int r=1; r<csv.getNumRows(); r++) {
			List<String> row = csv.getLine(r);
			int fssIndex = Integer.parseInt(row.get(4));
			fssIndexes.add(fssIndex);
			int modSourceID = Integer.parseInt(row.get(0));
			int sourceID = u2SourceMappings.get(modSourceID);
			int modRupID = Integer.parseInt(row.get(1));
			if (!rupIndexMappings.get(sourceID).containsKey(modRupID)) {
				System.out.println("WARNING: skipping (no mapping) for mod "+modSourceID+", "+modRupID);
				continue;
			}
			int rupID = rupIndexMappings.get(sourceID).get(modRupID);
			String sourceName = row.get(2);
			double mag = Double.parseDouble(row.get(3));
			
			String erfSourceName = ucerf2.getSource(sourceID).getName();
			Preconditions.checkState(erfSourceName.equals(sourceName),
					"Source name mismatch for source "+sourceID+":\n\tFile: "+sourceName+"\n\tERF: "+erfSourceName);
			double erfMag;
			if (rupID >= ucerf2.getSource(sourceID).getNumRuptures())
				erfMag = 0;
			else
				erfMag = ucerf2.getSource(sourceID).getRupture(rupID).getMag();
			List<Double> erfMags = Lists.newArrayList();
			for (ProbEqkRupture rup : ucerf2.getSource(sourceID))
				erfMags.add(rup.getMag());
//			if ((float)mag != (float)erfMag || rupID >= erfMags.size()) {
//				System.out.println("Mag mismatch for source "+sourceID+" rup "+rupID+":\n\tFile: "
//						+mag+"\n\tERF: "+erfMag+"\n\tERF Mags: "+Joiner.on(",").join(erfMags));
//				// remap
////				double magTolerance = 0.05;
//				double smallestDelta = Double.POSITIVE_INFINITY;
//				double matchProb = 0;
//				int matchIndex = -1;
//				ProbEqkSource source = ucerf2.getSource(sourceID);
//				for (int erfRupID=0; erfRupID<source.getNumRuptures(); erfRupID++) {
//					ProbEqkRupture rup = source.getRupture(erfRupID);
//					double delta = Math.abs(mag - rup.getMag());
//					if (delta < smallestDelta || (delta == smallestDelta && rup.getProbability() > matchProb)) {
//						matchProb = rup.getProbability();
//						smallestDelta = delta;
//						matchIndex = erfRupID;
//					}
//				}
//				Preconditions.checkState(smallestDelta < 0.05, "Couldn't find a match within 0.05 mag units: "+smallestDelta);
//				rupID = matchIndex;
//			}
			Preconditions.checkState((float)mag == (float)erfMag, "Mag mismatch for source "+sourceID+" rup "+rupID+":\n\tFile: "
					+mag+"\n\tERF: "+erfMag+"\n\tERF Mags: "+Joiner.on(",").join(erfMags));
			
			List<IDPairing> candidates = rupCandidates.get(fssIndex);
			if (candidates == null) {
				candidates = Lists.newArrayList();
				rupCandidates.put(fssIndex, candidates);
			}
			candidates.add(new IDPairing(sourceID, rupID));
//			rupMappingTable.put(sourceID, rupID, fssIndex);
		}
		System.out.println("Loaded candidates for "+rupCandidates.size()+"/"+fssIndexes.size()+" FSS ruptures");
		
		rupMappingTable = Maps.newHashMap();
		rupMappingReverseTable = Maps.newHashMap();
		for (Integer fssIndex : rupCandidates.keySet()) {
			List<IDPairing> candidates = rupCandidates.get(fssIndex);
			// rate by mag, dist, prob
			double closestMag = Double.POSITIVE_INFINITY;
			double prevDist = Double.POSITIVE_INFINITY;
			double prevProb = 0d;
			IDPairing match = null;
			
			double origMag = sol.getRupSet().getMagForRup(fssIndex);
			CompoundSurface surf = (CompoundSurface) sol.getRupSet().getSurfaceForRupupture(fssIndex, 1d, false);
			List<? extends RuptureSurface> surfs = surf.getSurfaceList();
			Location fssFirstLoc = surfs.get(0).getFirstLocOnUpperEdge();
			Location fssLastLoc = surfs.get(surfs.size()-1).getFirstLocOnUpperEdge();
			
			for (IDPairing pair : candidates) {
				int sourceID = pair.getID1();
				int rupID = pair.getID2();
				
				ProbEqkRupture rup = ucerf2.getRupture(sourceID, rupID);
				double magDelta = Math.abs(rup.getMag() - origMag);
				double dist1 = LocationUtils.linearDistanceFast(
						fssFirstLoc,
						rup.getRuptureSurface().getFirstLocOnUpperEdge());
				double dist2 = LocationUtils.linearDistanceFast(
						fssLastLoc,
						rup.getRuptureSurface().getFirstLocOnUpperEdge());
				double dist3 = LocationUtils.linearDistanceFast(
						fssFirstLoc,
						rup.getRuptureSurface().getLastLocOnUpperEdge());
				double dist = Math.min(dist1, Math.min(dist2, dist3));
				double prob = rup.getProbability();
				if (magDelta < closestMag || (magDelta == closestMag &&
						(dist < prevDist || (dist == prevDist && prob > prevProb)))) {
					closestMag = magDelta;
					prevDist = dist;
					prevProb = prob;
					match = pair;
				}
			}
			Preconditions.checkNotNull(match);
			rupMappingTable.put(fssIndex, match);
//			Preconditions.checkState(!rupMappingReverseTable.containsKey(match));
			rupMappingReverseTable.put(match, fssIndex);
		}
	}
	
	private void loadHyposForETASRups() {
		rvHypoLocations = Maps.newHashMap();
		hypoLocationsByRV = Maps.newHashMap();
		
		System.out.println("Loading hypos");
		int ERFID = 35;
		int RUP_VAR_SCEN_ID = 4;
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			for (ETAS_EqkRupture rup : catalog) {
				int fssIndex = rup.getFSSIndex();
				
				if (rvHypoLocations.containsKey(fssIndex))
					// we've already loaded hypos
					continue;
				
				IDPairing pair = rupMappingTable.get(fssIndex);
				Preconditions.checkNotNull(pair, "No mapping for rupture that occurred: "+fssIndex);
				
				String sql = "SELECT Rup_Var_ID,Hypocenter_Lat,Hypocenter_Lon,Hypocenter_Depth FROM Rupture_Variations " +
						"WHERE ERF_ID=" + ERFID + " AND Rup_Var_Scenario_ID=" + RUP_VAR_SCEN_ID + " " +
						"AND Source_ID=" + pair.getID1() + " AND Rupture_ID=" + pair.getID2();
				
				Map<Location, List<Integer>> locsMap = Maps.newHashMap();
				rvHypoLocations.put(fssIndex, locsMap);
				Map<Integer, Location> hyposByRV = Maps.newHashMap();
				hypoLocationsByRV.put(pair, hyposByRV);
				
				try {
					ResultSet rs = db.selectData(sql);
					boolean success = rs.first();
					while (success) {
						int rvID = rs.getInt("Rup_Var_ID");
						double lat = rs.getDouble("Hypocenter_Lat");
						double lon = rs.getDouble("Hypocenter_Lon");
						double depth = rs.getDouble("Hypocenter_Depth");
						Location loc = new Location(lat, lon, depth);
						
						List<Integer> ids = locsMap.get(loc);
						if (ids == null) {
							ids = Lists.newArrayList();
							locsMap.put(loc, ids);
						}
						ids.add(rvID);
						hyposByRV.put(rvID, loc);

						success = rs.next();
					}
				} catch (SQLException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
				Preconditions.checkState(!locsMap.isEmpty());
			}
		}
		
		System.out.println("Done loading hypos");
	}
	
	public void setNormTriggerRate(double normalizedTriggerRate) {
		this.normalizedTriggerRate = normalizedTriggerRate;
		// clear all caches
		rvProbs = null;
		rvProbsSortable = null;
		mod = null;
	}
	
	private void loadRVProbs() {
		// loads in probabilities for rupture variations from the ETAS catalogs
		
		RuptureProbabilityModifier probMod = getRupProbModifier();
		
		double prob = 1d/catalogs.size();
		if (scenario == ETAS_CyberShake_Scenarios.TEST_NEGLIGABLE)
			// make the probability gain super small which should result if almost zero gain if implemented correctly
			prob = 1e-20;
		// TODO correctly deal with exceedence probs, as a rup can happen more than once in a catalog 
		MinMaxAveTracker rvTrack = new MinMaxAveTracker();
		
		double occurMult = 1d;
		if (normalizedTriggerRate > 0d) {
			// we're normalizing the rate of triggered events
			double actualTriggerCount = 0d;
			for (List<ETAS_EqkRupture> catalog : catalogs)
				actualTriggerCount += catalog.size();
			// this is the rate of triggered ruptures
			actualTriggerCount /= catalogs.size();
			
			occurMult = normalizedTriggerRate/actualTriggerCount;
		}
		
		// map from ID pairing to <rv ID, fractional num etas occurrences>
		Map<IDPairing, Map<Integer, Double>> rvOccurCountsMap = Maps.newHashMap();
		Map<IDPairing, List<Integer>> allRVsMap = Maps.newHashMap();
		
		fractOccurances = new double[ucerf2.getNumSources()][];
		
		double singleFractRate = 1d/(double)catalogs.size();
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			for (ETAS_EqkRupture rup : catalog) {
				Location hypo = rup.getHypocenterLocation();
				Preconditions.checkNotNull(hypo);
				IDPairing pair = rupMappingTable.get(rup.getFSSIndex());
				Preconditions.checkNotNull(pair);
				double[] sourceOccurances = fractOccurances[pair.getID1()];
				if (sourceOccurances == null) {
					sourceOccurances = new double[ucerf2.getNumRuptures(pair.getID1())];
					fractOccurances[pair.getID1()] = sourceOccurances;
				}
				sourceOccurances[pair.getID2()] += singleFractRate;
				Map<Location, List<Integer>> rvHypoLocs = rvHypoLocations.get(rup.getFSSIndex());
				
				List<Integer> allRVsList = Lists.newArrayList();
				for (List<Integer> ids : rvHypoLocs.values())
					allRVsList.addAll(ids);
				allRVsMap.put(pair, allRVsList);
				
				double minDist = Double.POSITIVE_INFINITY;
				Location closestLoc = null;
				List<Location> locsWithinBuffer = Lists.newArrayList();
				for (Location loc : rvHypoLocs.keySet()) {
					double dist = LocationUtils.linearDistanceFast(loc, hypo);
					Preconditions.checkState(!loc.equals(closestLoc), "Duplicate locations!");
					if (dist < minDist) {
						minDist = dist;
						closestLoc = loc;
					}
					if (dist <= hypocenter_buffer_km)
						locsWithinBuffer.add(loc);
				}
				Preconditions.checkNotNull(closestLoc);
				Preconditions.checkState(minDist < 1000d, "No hypo match with 1000 km (closest="+minDist+")");
				
//				double myProb = prob;
				List<Integer> toBePromoted;
				if (locsWithinBuffer.size() < 2) {
					// just do the closest
					toBePromoted = Lists.newArrayList(rvHypoLocs.get(closestLoc));
				} else {
					// include all hypocenters within the buffer
					toBePromoted = Lists.newArrayList();
					for (Location hypoLoc : locsWithinBuffer) {
						toBePromoted.addAll(rvHypoLocs.get(hypoLoc));
					}
				}
				rvTrack.addValue(toBePromoted.size());
				Preconditions.checkState(toBePromoted.size() >= 1,
						"Should be more than one ID for each hypo (size="+toBePromoted.size()+")");
				
				Map<Integer, Double> rvCounts = rvOccurCountsMap.get(pair);
				if (rvCounts == null) {
					rvCounts = Maps.newHashMap();
					rvOccurCountsMap.put(pair, rvCounts);
				}
				
				// each mapped rv gets a fractional occurance, adding up to one
				double fractionalOccur = occurMult/(double)toBePromoted.size();
				for (int rvIndex : toBePromoted) {
					Double count = rvCounts.get(rvIndex);
					if (count == null)
						count = 0d;
					count += fractionalOccur;
					rvCounts.put(rvIndex, count);
				}
				
//				Map<Double, List<Integer>> rupRVProbs = rvProbs.get(pair);
//				if (rupRVProbs == null) {
//					rupRVProbs = Maps.newHashMap();
//					rvProbs.put(pair, rupRVProbs);
//					
//					if (calc_by_add_spontaneous) {
//						// now add in the regular probability
//						double initialProb = probMod.getModifiedProb(pair.getID1(), pair.getID2(), 0d);
//						List<Integer> allRVIndexes = Lists.newArrayList();
//						for (List<Integer> indexes : rvHypoLocs.values())
//							allRVIndexes.addAll(indexes);
//						// TODO???
////						rupRVProbs.put(initialProb/(double)allRVIndexes.size(), allRVIndexes);
//						rupRVProbs.put(initialProb, allRVIndexes);
//					}
//				}
//				
//				while (!toBePromoted.isEmpty()) {
//					List<Integer> newToBePromoted = Lists.newArrayList();
//					List<Integer> prevIDs = rupRVProbs.get(myProb);
//					if (prevIDs == null) {
//						prevIDs = Lists.newArrayList();
//						rupRVProbs.put(myProb, prevIDs);
//					}
//					for (int newID : toBePromoted) {
//						int index = prevIDs.indexOf(newID);
//						if (index >= 0) {
//							// this hypo now has additional probability, remove from this level and add it next time in the loop
//							prevIDs.remove(index);
//							newToBePromoted.add(newID);
//						} else {
//							prevIDs.add(newID);
//						}
//					}
//					
//					if (prevIDs.isEmpty())
//						rupRVProbs.remove(myProb);
//					
//					myProb += prob;
//					toBePromoted = newToBePromoted;
//				}
			}
		}
		System.out.println("RV counts for hypos encountered: "+rvTrack);
		
		// now build rv probs
		rvProbs = Maps.newHashMap();
		rvProbsSortable = Lists.newArrayList();
		
		for (IDPairing pair : rvOccurCountsMap.keySet()) {
			Map<Integer, Double> rvCounts = rvOccurCountsMap.get(pair);
			Preconditions.checkNotNull(rvCounts);
			Preconditions.checkState(!rvCounts.isEmpty());
			
			List<Integer> allRVsList = allRVsMap.get(pair);
			int totNumRVs = allRVsList.size();
			Preconditions.checkState(totNumRVs > 0);
			
			Map<Integer, Double> rvProbMap = Maps.newHashMap();
			
			if (calc_by_add_spontaneous) {
				// add in all RVs at original probability
				double startingProbPer = rupProbMod.getModifiedProb(
						pair.getID1(), pair.getID2(), 0d)/(double)totNumRVs;
				for (Integer rvID : allRVsList)
					rvProbMap.put(rvID, startingProbPer);
			}
			
			// now add probability for each occurrence
			for (Integer rvID : rvCounts.keySet()) {
				double occur = rvCounts.get(rvID);
				double rvProb = occur * prob;
				if (rvProbMap.containsKey(rvID))
					rvProb += rvProbMap.get(rvID);
				rvProbMap.put(rvID, rvProb);
				Location hypocenter = hypoLocationsByRV.get(pair).get(rvID);
				double mag = ucerf2.getRupture(pair.getID1(), pair.getID2()).getMag();
				rvProbsSortable.add(new RVProbSortable(pair.getID1(), pair.getID2(), rvID, mag, hypocenter, occur, rvProb));
			}
			
			// now reverse the mapping
			Map<Double, List<Integer>> rvProbsToCombine = Maps.newHashMap();
			for (Integer rvID : rvProbMap.keySet()) {
				Double rvProb = rvProbMap.get(rvID);
				List<Integer> rvsAtProb = rvProbsToCombine.get(rvProb);
				if (rvsAtProb == null) {
					rvsAtProb = Lists.newArrayList();
					rvProbsToCombine.put(rvProb, rvsAtProb);
				}
				rvsAtProb.add(rvID);
			}
			
			// now sub the probabilities for each set of combined rvs, add to actual map
			Map<Double, List<Integer>> mergedProbs = Maps.newHashMap();
			for (Double individualProb : rvProbsToCombine.keySet()) {
				List<Integer> rvIndexes = rvProbsToCombine.get(individualProb);
				double groupProb = individualProb*(double)rvIndexes.size();
				mergedProbs.put(groupProb, rvIndexes);
			}
			rvProbs.put(pair, mergedProbs);
		}
	}
	
	public void setTriggerAllHyposEqually(boolean triggerAllHyposEqually) {
		this.triggerAllHyposEqually = triggerAllHyposEqually;
	}
	
	private RuptureProbabilityModifier rupProbMod;

	@Override
	public synchronized RuptureProbabilityModifier getRupProbModifier() {
		if (rupProbMod != null)
			return rupProbMod;
		if (calc_by_add_spontaneous || scenario == ETAS_CyberShake_Scenarios.MAPPED_UCERF2) {
			final double aftRateCorr = 1d; // include aftershocks
			final double duration = timeSpan.getTimeYears();
			rupProbMod = new RuptureProbabilityModifier() {
				
				@Override
				public double getModifiedProb(int sourceID, int rupID, double origProb) {
					Integer fssIndex = rupMappingReverseTable.get(new IDPairing(sourceID, rupID));
					if (fssIndex == null)
						return 0d;
					double rupRate = sol.getRateForRup(fssIndex);
					double durationAdjustedRate = rupRate * duration;
					if (triggerAllHyposEqually && fractOccurances != null && fractOccurances[sourceID] != null)
						// this means we're applying rate increases to the whole rupture not just specifi RVs
						durationAdjustedRate += fractOccurances[sourceID][rupID];
					double prob = 1-Math.exp(-aftRateCorr*durationAdjustedRate);
					return prob;
				}
			};
		} else {
			rupProbMod = new ZeroProbMod();
		}
		return rupProbMod;
	}
	
	public synchronized void setCatalogs(List<List<ETAS_EqkRupture>> catalogs) {
		this.catalogs = catalogs;
		this.rvHypoLocations = null;
		this.rvProbs = null;
	}
	
	public List<List<ETAS_EqkRupture>> getCatalogs() {
		return catalogs;
	}

	@Override
	public synchronized RuptureVariationProbabilityModifier getRupVarProbModifier() {
		if (scenario == ETAS_CyberShake_Scenarios.MAPPED_UCERF2
				|| scenario == ETAS_CyberShake_Scenarios.MAPPED_UCERF2_TIMEDEP || triggerAllHyposEqually)
			return null;
		try {
			if (catalogs == null) {
				System.out.println("Loading catalogs for "+scenario.name);
				loadCatalogs(catalogsDirs);
			}
			
			if (rvHypoLocations == null) {
				System.out.println("Loading Hypos for ETAS rups for "+scenario.name);
				loadHyposForETASRups();
			}

			if (rvProbs == null) {
				System.out.println("Loading RV probs for "+scenario.name);
				loadRVProbs();
				System.out.println("DONE loading RV probs for "+scenario.name);
			}
		} catch (IOException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		if (mod == null)
			mod = new RupProbMod();
		return mod;
	}
	
	private RupProbMod mod = null;
	private class RupProbMod implements RuptureVariationProbabilityModifier {

		@Override
		public Map<Double, List<Integer>> getVariationProbs(int sourceID,
				int rupID, double originalProb) {
			if (triggerAllHyposEqually)
				return null;
			return rvProbs.get(new IDPairing(sourceID, rupID));
		}
		
	}
	
	public Map<IDPairing, Map<Double, List<Integer>>> getRVProbs() {
		return rvProbs;
	}
	
	public Map<Integer, Map<Location, List<Integer>>> getHypoLocs() {
		return rvHypoLocations;
	}
	
	public Map<Integer, IDPairing> getRupMappingTable() {
		return rupMappingTable;
	}
	
	public ETAS_CyberShake_Scenarios getScenario() {
		return scenario;
	}
	
	public ETAS_Cybershake_TimeSpans getTimeSpan() {
		return timeSpan;
	}
	
	public Date getTimeSpanStart() {
		return new GregorianCalendar(2014, 0, 1).getTime();
	}
	
	public void writeTriggerMFD(File outputDir, String prefix) throws IOException {
		getRupVarProbModifier(); // make sure everything has been loaded
		IncrementalMagFreqDist incrMFD = new IncrementalMagFreqDist(6.05, 31, 0.1d);
		incrMFD.setName("Incremental MFD");
		
		double catRate = 1d/catalogs.size();
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			for (ETAS_EqkRupture rup : catalog) {
//				System.out.println("Mag: "+rup.getMag()+", "+incrMFD.getMinX()+" => "+incrMFD.getMaxX());
				incrMFD.add(incrMFD.getClosestXIndex(rup.getMag()), catRate);
			}
		}
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		EvenlyDiscretizedFunc cmlMFD = incrMFD.getCumRateDistWithOffset();
		cmlMFD.setName("Cumulative MFD");
		
		funcs.add(incrMFD);
//		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.BLUE));
		
		funcs.add(cmlMFD);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		PlotSpec spec = new PlotSpec(funcs, chars, scenario.name+" Scenario Supra Seis MFD",
				"Magnitude", timeSpan.name+" Rate");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		gp.setBackgroundColor(Color.WHITE);
		
		gp.setXLog(false);
		gp.setYLog(true);
		gp.setUserBounds(6d, 8.5d, 1e-5, 1e0);
		gp.drawGraphPanel(spec);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	List<RVProbSortable> getRVProbsSortable() {
		return rvProbsSortable;
	}
	
	public static class RVProbSortable implements Comparable<RVProbSortable> {
		
		private int sourceID, rupID;
		private List<Integer> rvIDs;
		private List<Location> hypocenters;
		private double mag;
		private double occurances;
		private double triggerRate;

		public RVProbSortable(int sourceID, int rupID, int rvID, double mag,
				Location hypocenter, double occurances, double triggerRate) {
			this.sourceID = sourceID;
			this.rupID = rupID;
			this.mag = mag;
			this.rvIDs = Lists.newArrayList(rvID);
			this.hypocenters = Lists.newArrayList(hypocenter);
			this.occurances = occurances;
			this.triggerRate = triggerRate;
		}

		public RVProbSortable(int sourceID, int rupID, double mag) {
			this.sourceID = sourceID;
			this.rupID = rupID;
			this.mag = mag;
			
			this.occurances = 0d;
			this.triggerRate = 0d;
			this.hypocenters = Lists.newArrayList();
			this.rvIDs = Lists.newArrayList();
		}

		@Override
		public int compareTo(RVProbSortable o) {
//			return Double.compare(o.triggerRate, triggerRate);
			return Double.compare(o.getTriggerMoRate(), getTriggerMoRate());
		}
		
		public void addRV(double occurances, double triggerRate, int rvID, Location hypocenter) {
			this.occurances += occurances;
			this.triggerRate += triggerRate;
			rvIDs.add(rvID);
			hypocenters.add(hypocenter);
		}
		
		public double getTriggerMoRate() {
			double moment = Math.pow(10, 1.5*(mag + 6d));
			return moment * triggerRate;
		}
		
		@Override
		public String toString() {
			return "Source: "+sourceID+", Rup: "+rupID+", RV: "+Joiner.on(",").join(rvIDs)+", Mag: "+mag
					+"\nHypocenter: "+Joiner.on(",").join(hypocenters)+"\noccur: "+occurances+", triggerRate: "+triggerRate;
		}

		public int getSourceID() {
			return sourceID;
		}

		public int getRupID() {
			return rupID;
		}

		public List<Integer> getRvIDs() {
			return rvIDs;
		}

		public List<Location> getHypocenters() {
			return hypocenters;
		}

		public double getOccurances() {
			return occurances;
		}

		public double getTriggerRate() {
			return triggerRate;
		}

		public double getMag() {
			return mag;
		}
		
	}
	
	ERF getCS_UCERF2_ERF() {
		return ucerf2;
	}
	
	public ERF getModERFforGMPE() {
		final double timeRateMultiplier = timeSpan.getTimeYears();
		return new AbstractERF() {
			
			@Override
			public String getName() {
				return scenario.name()+" ETAS MODIFIED UCERF2";
			}
			
			@Override
			public void updateForecast() {
				
			}
			
			@Override
			public ProbEqkSource getSource(int sourceID) {
				final ProbEqkSource orig = ucerf2.getSource(sourceID);
				final List<ProbEqkRupture> modRups = Lists.newArrayList();
				for (int rupID=0; rupID<orig.getNumRuptures(); rupID++) {
					double fractOccur;
					if (fractOccurances != null && fractOccurances[sourceID] != null)
						fractOccur = fractOccurances[sourceID][rupID];
					else
						fractOccur = 0d;
					final ProbEqkRupture origRup = orig.getRupture(rupID);
					double origProb = origRup.getProbability();
					double origRate = -Math.log(1d-origProb); // 1 year, don't need to divide by years
					// convert to the correct time span and modify for trigger rate
					double modRate = origRate*timeRateMultiplier + fractOccur;
					double modProb = 1-Math.exp(-modRate);
					modRups.add(new ProbEqkRupture(origRup.getMag(), origRup.getAveRake(),
							modProb, origRup.getRuptureSurface(), null));
				}
				ProbEqkSource mod = new ProbEqkSource() {
					
					@Override
					public RuptureSurface getSourceSurface() {
						return orig.getSourceSurface();
					}
					
					@Override
					public LocationList getAllSourceLocs() {
						return orig.getAllSourceLocs();
					}
					
					@Override
					public ProbEqkRupture getRupture(int nRupture) {
						return modRups.get(nRupture);
					}
					
					@Override
					public int getNumRuptures() {
						return orig.getNumRuptures();
					}
					
					@Override
					public double getMinDistance(Site site) {
						return orig.getMinDistance(site);
					}
				};
				return mod;
			}
			
			@Override
			public int getNumSources() {
				return ucerf2.getNumSources();
			}
		};
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
		ETAS_Cybershake_TimeSpans timeSpan = ETAS_Cybershake_TimeSpans.ONE_WEEK;
		FaultSystemSolution sol = FaultSystemIO.loadSol(new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/ucerf2_mapped_sol.zip"));
		ETASModProbConfig conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.PARKFIELD, timeSpan, sol,
				new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_09_02-parkfield-nospont/results.zip"),
				new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
//		ETASModProbConfig conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.BOMBAY_M6, timeSpan, sol,
//				new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_09_02-bombay_beach_m6-nospont/results.zip"),
//				new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
//		ETASModProbConfig conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.MAPPED_UCERF2, timeSpan, sol,
//				new File[0],
//				new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
		
		conf.writeTriggerMFD(new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mfds"),
				conf.scenario.name().toLowerCase()+"_trigger_mfd");
		ETAS_CatalogStats.calcNumWithMagAbove(conf.catalogs, 0d);
		
		System.exit(0);
	}

}
