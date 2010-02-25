package org.opensha.sha.imr.attenRelImpl.peer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.sha.calc.HazardCurveCalculator;

/**
 * Administration class for PEER test cases. This class provides several methods
 * to run all or a subset of the test cases as well as reults comparison tools.
 *
 * @author Peter Powers
 * @version $Id:$
 */
public class TestAdmin {

	private static final String PEER_DIR = "tmp/PEER_TESTS/";
	private static final String PEER_FILE_SUFFIX = "-PGA_OpenSHA.txt";
	
	static {
		File peerDir = new File(PEER_DIR);
		peerDir.mkdirs();
	}
	
	/**
	 * Tester.
	 * @param args
	 */
	public static void main(String[] args) {
//		runTests(95,98);
//		runShortTests();
		//runLongTests();
	}
	
	/**
	 * Run a single test.
	 * @param test
	 */
	public static void runTest(PeerTest test) {
		ArrayList<PeerTest> testList = new ArrayList<PeerTest>();
		testList.add(test);
		TestAdmin ta = new TestAdmin();
		ta.submit(testList);
	}

	/**
	 * Run all 106 tests.
	 */
	public static void runTests() {
		ArrayList<PeerTest> masterList = TestConfig.getSetOneDecriptors();
		TestAdmin ta = new TestAdmin();
		ta.submit(masterList);
	}
	
	/**
	 * Run a range of tests. See TestConfig to match indices to tests.
	 * @param min test index (inclusive)
	 * @param max test index (inclusive)
	 */
	public static void runTests(int min, int max) {
		ArrayList<PeerTest> testList = new ArrayList<PeerTest>();
		addRangeToList(testList, min, max);
		TestAdmin ta = new TestAdmin();
		ta.submit(testList);
	}
	
	/**
	 * Runs all the short tests: those taking a minute or less (most take under
	 * a second to run); 91 in all. Takes ~2min on an 8-core processor.
	 */
	public static void runShortTests() {
		ArrayList<PeerTest> testList = new ArrayList<PeerTest>();
		addRangeToList(testList, 0, 20);
		addRangeToList(testList, 28, 76);
		addRangeToList(testList, 84, 105);
		TestAdmin ta = new TestAdmin();
		ta.submit(testList);
	}
	
	/**
	 * Runs all the long tests: 14 tests (cases 4 and 9b) take ~20min each.
	 * Takes ~40min on an 8-core processor.
	 */
	public static void runLongTests() {
		ArrayList<PeerTest> testList = new ArrayList<PeerTest>();
		addRangeToList(testList, 21, 27);
		addRangeToList(testList, 77, 83);
		TestAdmin ta = new TestAdmin();
		ta.submit(testList);
	}
	
	private static void addRangeToList(ArrayList<PeerTest> list, int min, int max) {
		ArrayList<PeerTest> masterList = TestConfig.getSetOneDecriptors();
		for (int i=min; i<=max; i++) {
			list.add(masterList.get(i));
		}
	}

	private ArrayList<Future<?>> futures;
	private SimpleDateFormat sdf = new SimpleDateFormat();
	
	private void submit(List<PeerTest> tests) {
		try {

			int numProc = Runtime.getRuntime().availableProcessors();
			ExecutorService ex = Executors.newFixedThreadPool(numProc);

			String start = sdf.format(new Date(System.currentTimeMillis()));
			System.out.println("Running PEER test cases...");
			System.out.println("Processors: " + numProc);
			System.out.println("Start Time: " + start);
	
			futures = new ArrayList<Future<?>>();
			for (PeerTest pt : tests) {
				futures.add(ex.submit(new TestRunner(pt)));
			}

			// all tests on a single processor machine should
			// not take more than 12 hours
			ex.shutdown();
			ex.awaitTermination(12, TimeUnit.HOURS);
			
			String end = sdf.format(new Date(System.currentTimeMillis()));
			System.out.println("  End Time: " + end);
			System.exit(0);
			
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	private class TestRunner implements Runnable {
		
		private PeerTest test;

		TestRunner(PeerTest test) {
			this.test = test;
		}

		public void run() {
			
			long start = System.currentTimeMillis();
			System.out.println("  Starting: " + test);
			
			try {
				TestConfig tc = new TestConfig(test);
				
				HazardCurveCalculator calc = new HazardCurveCalculator();
				calc.setMaxSourceDistance(TestConfig.MAX_DISTANCE);
				calc.setIncludeMagDistCutoff(false);
				DiscretizedFuncAPI adf = calc.getHazardCurve(
						tc.getFunction(),
						tc.getSite(),
						tc.getIMR(),
						tc.getERF());
				
				adf = TestConfig.functionFromLogX(adf);
				
				BufferedWriter br = new BufferedWriter(new FileWriter(
						PEER_DIR + test + PEER_FILE_SUFFIX));
				for (int j = 0; j < adf.getNum(); ++j) {
					br.write(adf.get(j).getX() + "\t"
							+ adf.get(j).getY() + "\n");
				}
				br.close();

			} catch (Exception e) {
				System.out.println("    FAILED: " + test);
				e.printStackTrace();
			}
			long end = System.currentTimeMillis();
			int dtm = (int) ((end - start) / 1000 / 60);
			int dts = (int) ((end - start) / 1000 % 60);
			System.out.println(
					"  Finished: " + test + " " + dtm + "m " + dts + "s");
		}
	}
	
	public static void compareResults(File dir1, File dir2) {
		
		String[] filenames = dir1.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(PEER_FILE_SUFFIX)) ? true : false;
			}
		});
		
	}
	
	private static String fileSummary(File f1, File f2) {
		try {
			BufferedReader br1 = new BufferedReader(new FileReader(f1));
			
			BufferedReader br2 = new BufferedReader(new FileReader(f2));
			
		
			br1.close();
			br2.close();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return "shizzler";
	}
	
}
