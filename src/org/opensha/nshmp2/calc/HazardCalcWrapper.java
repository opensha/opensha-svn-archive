package org.opensha.nshmp2.calc;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.LocationList;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardCalcWrapper {

	private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2";
	private static final String S = File.separator;

//	private BlockingQueue<HazardCalcResult> queue;
	
	HazardCalcWrapper(LocationList locs, Period period, String name) {
		
		// init result queue
		BlockingQueue<HazardCalcResult> queue = Queues.newLinkedBlockingQueue();
		
		// init result file
		File out = new File(OUT_DIR + S + name + S + period + S + "curves.csv");
		
		// init and start results writer
		HazardCalcWriter hcw = null;
		try {
			hcw = new HazardCalcWriter(queue, out, period);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		ExecutorService ex = Executors.newSingleThreadExecutor();
		ex.submit(hcw);
		
		ThreadedHazardCalc thc = null;
		try {
			thc = new ThreadedHazardCalc(locs, period, queue);
			thc.start();
		} catch (ExecutionException ee) {
			ee.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		
		// start() returns when all calculations are complete (i.e. all
		// CompletionService.take() calls have completed). Closing the result
		// writer flushes and closes open streams
		hcw.close();
		
		
		// in MPJ, does a Barrier return indicating a job is complete
		
		// a call to the writer FuterTask.get() will pick up any exceptions that
		// may have been thrown but I'm not sure how to monitor for them
		// during calculations.
		
		System.exit(0);

	}
	
//	private void initResultFile() {
//		String outDirName = OUT_DIR + S + name + S + per + S + "curves.csv";
//		File outDir = new File(outDirName);
//		outDir.mkdirs();
//		String curveFile = outDirName + "curves.csv";
//		toCSV(new File(curveFile), curveData);
//	}

	// one approach to determining if tasks are done is to wait for 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		new HazardCalcMgr2();
		
//		Set<Period> periods = EnumSet.of(Period.GM0P20, Period.GM1P00, Period.GM0P00);
		Set<Period> periods = EnumSet.of(Period.GM0P20);
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

//		tg = TestGrid.LOS_ANGELES;
//		gr = tg.grid();
//		locLists.add(gr.getNodeList());
//		names.add(tg.name());
//		
//		tg = TestGrid.SAN_FRANCISCO;
//		gr = tg.grid();
//		locLists.add(gr.getNodeList());
//		names.add(tg.name());
//
//		tg = TestGrid.SEATTLE;
//		gr = tg.grid();
//		locLists.add(gr.getNodeList());
//		names.add(tg.name());
//		
//		tg = TestGrid.MEMPHIS;
//		gr = tg.grid();
//		locLists.add(gr.getNodeList());
//		names.add(tg.name());
//		
//		tg = TestGrid.SALT_LAKE_CITY;
//		gr = tg.grid();
//		locLists.add(gr.getNodeList());
//		names.add(tg.name());
		
//		LocationList locList = new LocationList();
//		for (NEHRP_TestCity city : NEHRP_TestCity.values()) {
//			locList.add(city.location());
//		}
//		locLists.add(locList);
//		names.add("NEHRPcities");

		LocationList locList = new LocationList();
		locList.add(NEHRP_TestCity.MEMPHIS.location());
		locList.add(NEHRP_TestCity.MEMPHIS.location());
		locList.add(NEHRP_TestCity.MEMPHIS.location());
		locList.add(NEHRP_TestCity.MEMPHIS.location());
		locLists.add(locList);
		names.add("testMEMPhis");
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
				
				new HazardCalcWrapper(locLists.get(i), per, names.get(i));
				sw.stop();
				System.out.println("Finishing: " + names.get(i) + " " + per + 
					" " + sw.elapsedTime(TimeUnit.SECONDS));
			}
		}

	}

}
