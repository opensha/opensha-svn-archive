package org.opensha.nshmp2.calc;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.erf.NSHMP2008;
import org.opensha.nshmp2.erf.WUS_ERF;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.NSHMP_Utils;
import org.opensha.nshmp2.util.Period;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * Class manages multithreaded NSHMP hazard calculations. Farms out
 * {@code HazardCalc}s to locally available cores and pipes results to a
 * supplied {@code Queue}.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class ThreadedHazardCalc {

//	private static final String OUT_DIR = "/Volumes/Scratch/nshmp-opensha-trunk2";
//	private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2";
//	private static final String S = File.separator;
//	private static Logger log;
//	private static final Level LEVEL;
//	private static final boolean loggable;
//	private List<List<String>> curveData;
	
//	private BlockingQueue<HazardCalcResult> queue;

//	private Period per;
//	private String name;

//	static {
//		LEVEL = Level.INFO;
//		// NSHMP_Utils logger is set to WARNING; probably want to use prefs
//		log = NSHMP_Utils.logger();
//		log.setLevel(LEVEL);
//		for (Handler h : NSHMP_Utils.logger().getHandlers()) {
//			h.setLevel(LEVEL);
//		}
//		loggable = log.isLoggable(LEVEL);
//	}

	private LocationList locs;
	private Period period;
	private Queue<HazardCalcResult> queue;
	
	/*
	 * Initializes a new threaded hazard calculation that pipes results to the
	 * suplied queue.
	 */
	ThreadedHazardCalc(LocationList locs, Period period,
		Queue<HazardCalcResult> queue) {
		this.locs = locs;
		this.period = period;
		this.queue = queue;
	}
	
	void start() throws InterruptedException, ExecutionException {
		// init thread mgr
		int numProc = Runtime.getRuntime().availableProcessors();
		ExecutorService ex = Executors.newFixedThreadPool(numProc);
		CompletionService<HazardCalcResult> ecs = 
				new ExecutorCompletionService<HazardCalcResult>(ex);

		// init erf
		NSHMP2008 erf = NSHMP2008.create();
//			NSHMP2008 erf = NSHMP2008.createSingleSource("CEUS.2007all8.AB.in");
		erf.updateForecast();
		System.out.println(erf);

		for (Location loc : locs) {
			Site site = new Site(loc);
			HazardCalc2 hc = HazardCalc2.create(erf, site, period);
			ecs.submit(hc);
		}
		ex.shutdown();
		System.out.println("Jobs submitted: " + locs.size());

		// process results as they come in
		for (int i = 0; i < locs.size(); i++) {
			queue.add(ecs.take().get());
			if (i % 10 == 0) System.out.println("Jobs completed: " + i);
		}
		
		// necessary ??
		ex.awaitTermination(24, TimeUnit.HOURS);
			
	}

//	private void addResult(Location loc, DiscretizedFunc f) {
//
//		// // compute RTGM -- not in this branch; enable in trunk
//		// if (p == Period.GM0P20 || p == Period.GM1P00) {
//		// RTGM.Frequency freq = per.equals(Period.GM0P20)
//		// ? RTGM.Frequency.SA_0P20 : RTGM.Frequency.SA_1P00;
//		// RTGM rtgm = RTGM.create(f, freq, 0.8);
//		// double val = rtgm.get();
//		// }
//
//		List<String> curveDat = Lists.newArrayList();
//		curveDat.add(Double.toString(loc.getLatitude()));
//		curveDat.add(Double.toString(loc.getLongitude()));
//		for (Point2D p : f) {
//			curveDat.add(Double.toString(p.getY()));
//		}
//		curveData.add(curveDat);
//	}

//	private void initResults() {
//		curveData = Lists.newArrayList();
//		List<String> curveHeader = Lists.newArrayList();
//		curveHeader.add("lat");
//		curveHeader.add("lon");
//		for (Double d : per.getIMLs()) {
//			curveHeader.add(d.toString());
//		}
//		curveData.add(curveHeader);
//	}

//	private void writeFiles() {
//		String outDirName = OUT_DIR + S + name + S + per + S;
//		File outDir = new File(outDirName);
//		outDir.mkdirs();
//		String curveFile = outDirName + "curves.csv";
//		toCSV(new File(curveFile), curveData);
//	}

//	private static void toCSV(File file, List<List<String>> content) {
//		if (file.exists()) file.delete();
//		Joiner joiner = Joiner.on(',').useForNull(" ");
//		try {
//			PrintWriter pw = new PrintWriter(new FileWriter(file, true));
//			for (List<String> lineDat : content) {
//				String line = joiner.join(lineDat);
//				pw.println(line);
//			}
//			pw.close();
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//	}


}
