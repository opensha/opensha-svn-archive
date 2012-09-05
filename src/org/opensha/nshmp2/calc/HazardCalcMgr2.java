package org.opensha.nshmp2.calc;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionService;
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
import org.opensha.nshmp2.erf.WUS_ERF;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.NSHMP_Utils;
import org.opensha.nshmp2.util.Period;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * Class manages NSHMP hazard calculations. Farms out HazardCalcs to threads and
 * aggregates results.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardCalcMgr2 {

	private static final String OUT_DIR = "/Volumes/Scratch/nshmp-cf";
	private static final String S = File.separator;
	private static Logger log;
	private static final Level LEVEL;
	private static final boolean loggable;
	private List<List<String>> curveData;

	private Period per;
	private String name;
//	private String dataName = "SF_BOX";
//	private String dataName = "LA_BOX";
//	private String dataName = "cybershake";

	static {
		LEVEL = Level.INFO;
		// NSHMP_Utils logger is set to WARNING; probably want to use prefs
		log = NSHMP_Utils.logger();
		log.setLevel(LEVEL);
		for (Handler h : NSHMP_Utils.logger().getHandlers()) {
			h.setLevel(LEVEL);
		}
		loggable = log.isLoggable(LEVEL);
	}

	HazardCalcMgr2(LocationList locs, Period per, String name) {
		this.name = name;
		this.per = per;
		try {
			// init thread mgr
			int numProc = Runtime.getRuntime().availableProcessors();
			ExecutorService ex = Executors.newFixedThreadPool(numProc);
			CompletionService<HazardCalcResult> ecs = new ExecutorCompletionService<HazardCalcResult>(
				ex);

			// init erf
			WUS_ERF erf = new WUS_ERF();
			erf.updateForecast();

			// init results; requires that period be set
			initResults();

			for (Location loc : locs) {
				Site site = new Site(loc);
				
				HazardCalc2 hc = HazardCalc2.create(erf, site, per);
				ecs.submit(hc);
			}
			ex.shutdown();

			// process results as they come in
			System.out.println("Jobs submitted: " + locs.size());
			for (int i = 0; i < locs.size(); i++) {
				HazardCalcResult result = ecs.take().get();
				if (i % 10 == 0) System.out.println("Jobs completed: " + i);
				addResult(result.location(), result.curve());
			}

			// write data
			writeFiles();
			
			// necessary ??
			ex.awaitTermination(48, TimeUnit.HOURS);

			//System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addResult(Location loc, DiscretizedFunc f) {

		// // compute RTGM -- not in this branch; enable in trunk
		// if (p == Period.GM0P20 || p == Period.GM1P00) {
		// RTGM.Frequency freq = per.equals(Period.GM0P20)
		// ? RTGM.Frequency.SA_0P20 : RTGM.Frequency.SA_1P00;
		// RTGM rtgm = RTGM.create(f, freq, 0.8);
		// double val = rtgm.get();
		// }

		List<String> curveDat = Lists.newArrayList();
		curveDat.add(Double.toString(loc.getLatitude()));
		curveDat.add(Double.toString(loc.getLongitude()));
		for (Point2D p : f) {
			curveDat.add(Double.toString(p.getY()));
		}
		curveData.add(curveDat);
	}

	private void initResults() {
		curveData = Lists.newArrayList();
		List<String> curveHeader = Lists.newArrayList();
		curveHeader.add("lat");
		curveHeader.add("lon");
		for (Double d : per.getIMLs()) {
			curveHeader.add(d.toString());
		}
		curveData.add(curveHeader);
	}

	private void writeFiles() {
		String outDirName = OUT_DIR + S + name + S + per + S;
		File outDir = new File(outDirName);
		outDir.mkdirs();
		String curveFile = outDirName + "curves.csv";
		toCSV(new File(curveFile), curveData);
	}

	private static void toCSV(File file, List<List<String>> content) {
		if (file.exists()) file.delete();
		Joiner joiner = Joiner.on(',').useForNull(" ");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file, true));
			for (List<String> lineDat : content) {
				String line = joiner.join(lineDat);
				pw.println(line);
			}
			pw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void main(String[] args) {
//		new HazardCalcMgr2();
		
		Set<Period> periods = EnumSet.of(Period.GM0P00, Period.GM0P20, Period.GM1P00);
		List<LocationList> locLists = Lists.newArrayList();
		List<String> names = Lists.newArrayList();
		TestGrid tg = null;
		GriddedRegion gr = null;
		
//		gr = new CaliforniaRegions.WG02_GRIDDED();
//		locLists.add(gr.getNodeList());
//		names.add("SF_BOX");
		
//		gr = new CaliforniaRegions.WG07_GRIDDED();
//		locLists.add(gr.getNodeList());
//		names.add("LA_BOX");

//		tg = TestGrids.LOS_ANGELES;
//		gr = tg.grid();
//		locLists.add(gr.getNodeList());
//		names.add(tg.name());
//		
//		tg = TestGrids.SAN_FRANCISCO;
//		gr = tg.grid();
//		locLists.add(gr.getNodeList());
//		names.add(tg.name());

		tg = TestGrid.SEATTLE;
		gr = tg.grid();
		locLists.add(gr.getNodeList());
		names.add(tg.name());
		
		tg = TestGrid.MEMPHIS;
		gr = tg.grid();
		locLists.add(gr.getNodeList());
		names.add(tg.name());
		
		tg = TestGrid.SALT_LAKE_CITY;
		gr = tg.grid();
		locLists.add(gr.getNodeList());
		names.add(tg.name());
		
//		LocationList locList = new LocationList();
//		for (NEHRP_TestCity city : NEHRP_TestCity.values()) {
//			locList.add(city.location());
//		}
//		locLists.add(locList);
//		names.add("NEHRPcities");

//		LocationList locList = new LocationList();
//		locList.add(NEHRP_TestCity.LOS_ANGELES.location());
//		locLists.add(locList);
//		names.add("testLA1");
//
//		locList = new LocationList();
//		locList.add(NEHRP_TestCity.LOS_ANGELES.location());
//		locLists.add(locList);
//		names.add("testLA2");

		
		Stopwatch sw = new Stopwatch();
		
		for (Period per : periods) {
			for (int i=0; i<locLists.size(); i++) {
				System.out.println("Starting: " + names.get(i) + " " + per);
				sw.reset().start();
				new HazardCalcMgr2(locLists.get(i), per, names.get(i));
				sw.stop();
				System.out.println("Finishing: " + names.get(i) + " " + per + 
					" " + sw.elapsedTime(TimeUnit.MINUTES));
			}
		}
	}
	
	
//	// Location(s) of interest
//	LocationList locs = new LocationList();
//	
//	// NEHRP test cities
//	for (NEHRP_TestCity city : NEHRP_TestCity.values()) {
//		locs.add(city. location());
//	}
//	
//	// cybershake region
//	GriddedRegion gr = new CaliforniaRegions.WG02_GRIDDED();
//	for (Location loc : gr) {
//		locs.add(loc);
//	}
//	
//	locs.add(NEHRP_TestCity.LOS_ANGELES.location());
//	locs.add(NEHRP_TestCity.RIVERSIDE.location());
//	locs.add(NEHRP_TestCity.CENTURY_CITY.location());
//	locs.add(NEHRP_TestCity.BOISE.location());
//
//	locs.add(NEHRP_TestCity.LOS_ANGELES.shiftedLocation());
//	locs.add(NEHRP_TestCity.CENTURY_CITY.shiftedLocation());
//	locs.add(NEHRP_TestCity.NORTHRIDGE.shiftedLocation());
//	locs.add(NEHRP_TestCity.LONG_BEACH.shiftedLocation());
//	locs.add(NEHRP_TestCity.IRVINE.shiftedLocation());
//	locs.add(NEHRP_TestCity.RIVERSIDE.shiftedLocation());
//	locs.add(NEHRP_TestCity.SAN_BERNARDINO.shiftedLocation());
//	locs.add(NEHRP_TestCity.SAN_LUIS_OBISPO.shiftedLocation());


}
