package scratch.kevin.simulators.momRateVariation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.dom4j.DocumentException;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.EventRecord;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class UCERF3ComparisonAnalysis {
	
	private static List<EQSIM_Event> loadCatalogAsFakeSimEvents(FaultSystemSolution sol, Region region,
			File eventFile) throws IOException {
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
		
		long startTime = Long.MIN_VALUE;
		
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
			
			if (startTime == Long.MIN_VALUE)
				startTime = epoch;
			
			long millis = epoch - startTime;
			double secs = (double)millis / 1000d;
			
			EventRecord rec = new EventRecord(null);
			rec.setTime(secs);
			
			double moment = MagUtils.magToMoment(rupSet.getMagForRup(fssIndex));
			
			rec.setMoment(moment);
			
			EQSIM_Event e = new EQSIM_Event(rec);
			
			events.add(e);
		}
		
		read.close();
		
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
			Preconditions.checkState(events.get(0).getTime() == 0d, "Bad start time: %s", events.get(0).getTime());
			
			for (EQSIM_Event e : events)
				stitched.add(e.cloneNewTime(timeSecs+e.getTime(), id++));
			
			timeSecs = stitched.get(stitched.size()-1).getTime();
		}
		
		return stitched;
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
		File fssFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip");
		File mainDir = new File("/home/kevin/Simulators/time_series/ucerf3_compare");
		
		String dirPrefix = "10000yr";
		int threads = 8;
		File outputDir = new File(mainDir, dirPrefix+"_runs_combined");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		Region region = new CaliforniaRegions.RELM_SOCAL();
		
		FaultSystemSolution sol = FaultSystemIO.loadSol(fssFile);
		
		List<List<EQSIM_Event>> eventsList = Lists.newArrayList();
		
		int[] windowLens = { 10, 25, 50, 75, 100, 150, 200 };
		
		for (int i=0; i<threads; i++) {
			File runDir = new File(mainDir, dirPrefix+"_run"+i);
			List<EQSIM_Event> events = loadCatalogAsFakeSimEvents(
					sol, region, new File(runDir, "sampledEventsData.txt"));
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
		}
		
		// now stitch
		List<EQSIM_Event> stitched = stitch(eventsList);
		double calcDuration = stitched.get(stitched.size()-1).getTimeInYears() - stitched.get(0).getTimeInYears();
		System.out.println("Loaded "+stitched.size()+" stitched UCERF3 events, duration: "+calcDuration+" years");
		
		File tsPlot = new File(outputDir, "ts_plot.png");
		SimulatorMomRateVarCalc.plotMomRateVar(stitched, windowLens, "Fake UCERF3 Stitched", 0,
				(int)calcDuration, true, false, tsPlot);
		
		for (int windowLen : windowLens) {
			File outputFile = new File(outputDir, "ucerf3_"+outputDir.getName()+"_"+windowLen+"yr.bin");
			SimulatorMomRateVarCalc.writeMomRateTimeSeries(windowLen, stitched, outputFile);
		}
	}

}
