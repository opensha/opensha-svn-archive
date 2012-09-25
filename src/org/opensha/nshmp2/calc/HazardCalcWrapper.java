package org.opensha.nshmp2.calc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.LocationList;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;
import org.opensha.nshmp2.util.Utils;

import com.google.common.collect.Lists;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
@Deprecated
public class HazardCalcWrapper {

//	private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2";
//	private static final String OUT_DIR = "/Volumes/Scratch/nshmp-opensha-trunk";
	private static final String S = File.separator;

	HazardCalcWrapper(LocationList locs, Period period, HazardResultWriter writer) {
	
		// init result file
//		File out = new File(OUT_DIR + S + name + S + period + S + "curves.csv");
		
		ThreadedHazardCalc thc = null;
		
		try {
			thc = new ThreadedHazardCalc(locs, period, writer);
			thc.calculate(null);
		} catch (ExecutionException ee) {
			ee.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
//	HazardCalcWrapper(TestGrid grid, Period period, File out) {
//		this(grid.grid().getNodeList(), period, out);
//	}
	
//	HazardCalcWrapper(File config) {
//		try {
//			HazardCalcConfig hcConfig = new HazardCalcConfig(config);
//			this(hcConfig.grid, hcConfig.period, hc.name);
//	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		Set<Period> periods = EnumSet.of(Period.GM0P20, Period.GM1P00, Period.GM0P00);
		Set<Period> periods = EnumSet.of(Period.GM0P00);
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
//		
//		LocationList locList = new LocationList();
//		for (NEHRP_TestCity city : NEHRP_TestCity.values()) {
//			locList.add(city.location());
//		}
//		locLists.add(locList);
//		names.add("NEHRPcities");
//
//		locList = new LocationList();
//		locList.add(NEHRP_TestCity.LOS_ANGELES.location());
//		locLists.add(locList);
//		names.add("testLA2");

		
//		Stopwatch sw = new Stopwatch();
//		
//		for (Period per : periods) {
//			for (int i=0; i<locLists.size(); i++) {
//				System.out.println("Starting: " + names.get(i) + " " + per);
//				sw.reset().start();
//				
//				new HazardCalcWrapper(locLists.get(i), per, names.get(i));
//				sw.stop();
//				System.out.println("Finishing: " + names.get(i) + " " + per + 
//					" " + sw.elapsedTime(TimeUnit.SECONDS));
//			}
//		}
		
		
		try {
			URL propsURL = (args.length > 0) ? 
				new File(args[0]).toURI().toURL() : 
				Utils.getResource("/calc.properties");
			
			HazardCalcConfig config = new HazardCalcConfig(propsURL);
			
			String name = config.name;
			TestGrid grid = config.grid;
			Period period = config.period;
			ERF_ID erfID = config.erfID;
			boolean epiUnc = config.epiUnc;
			String outDir = config.outDir;
			boolean singleFile = config.singleFile;
			
			System.out.println(name);
			System.out.println(grid);
			System.out.println(period);
			System.out.println(erfID);
			System.out.println(epiUnc);
			System.out.println(outDir);
			System.out.println(singleFile);
			
			
			String outPath = outDir + S + name + S + grid + S + period + S;
			File localOutFile = new File(outPath + "curves.csv");
			File mpjOutDir = new File(outPath);
			
			HazardResultWriter writer = null;
			try {
				writer = singleFile ? 
					new HazardResultWriterLocal(localOutFile, period) :
					new HazardResultWriterMPJ(mpjOutDir);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			new HazardCalcWrapper(grid.grid().getNodeList(), period, writer);
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}


	}

}
