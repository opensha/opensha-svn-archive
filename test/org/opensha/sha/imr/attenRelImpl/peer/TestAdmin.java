package org.opensha.sha.imr.attenRelImpl.peer;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.gui.infoTools.ExceptionWindow;
import org.opensha.sha.imr.attenRelImpl.peer.TestConfig.PeerTest;

public class TestAdmin {

	private static final String PEER_DIR = "tmp/PEER_TESTS/";
	
	static {
		File peerDir = new File(PEER_DIR);
		peerDir.mkdirs();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestAdmin ta = new TestAdmin();
		ArrayList<PeerTest> masterList = TestConfig.getSetOneDecriptors();
//		ta.runTests(masterList);
		
//		System.out.println("NumTests: " + masterList.size());
		ArrayList<PeerTest> testList = new ArrayList<PeerTest>();
		int dec = 20;
		testList.add(masterList.get(dec + 0));
		testList.add(masterList.get(dec + 1));
		testList.add(masterList.get(dec + 2));
		testList.add(masterList.get(dec + 3));
		testList.add(masterList.get(dec + 4));
		testList.add(masterList.get(dec + 5));
		testList.add(masterList.get(dec + 6));
		testList.add(masterList.get(dec + 7));
		testList.add(masterList.get(dec + 8));
		testList.add(masterList.get(dec + 9));
		ta.runTests(testList);
		
//		int i=0;
//		for (PeerTest pt : masterList) {
//			System.out.println(i++ + " " + pt);
//		}
	}
	
	public void runtTest(PeerTest test) {
		ArrayList<PeerTest> tdl = new ArrayList<PeerTest>();
		tdl.add(test);
		runTests(tdl);
	}

	private Thread[] threads;
	private SimpleDateFormat sdf = new SimpleDateFormat();
	
	
	public void runTests(List<PeerTest> tests) {
		//String start = sdf.format(new Date(System.currentTimeMillis()));
		
//		int numProc = Runtime.getRuntime().availableProcessors();
//		int numThreads = (numProc > 1) ? numProc - 1 : 1;
//		threads = new Thread[numThreads];
//		
//		System.out.println("Running PEER test cases...");
//		System.out.println("    Processors: " + numProc);
//		System.out.println("   Max Threads: " + numThreads);
//		System.out.println("    Start Time: " + start);
		
//		for (PeerTest pt : tests) {
		for (int i=0; i<tests.size(); i++) {
			PeerTest pt = tests.get(i);
//			int idx = pollThreads();
//			while (idx == -1) {
//				try {
//					System.out.println("        -- sleeping");
//					Thread.sleep(2000);
//				} catch (InterruptedException ie) {
//					ie.printStackTrace();
//				}
//				idx = pollThreads();
//			}
			
			Thread t = new Thread(new TestRunner(pt));
			System.out.println("      Starting: " + i + " " + pt);
			t.start();
			
//			try {
//				t.join();				
//			} catch (InterruptedException ie) {
//				ie.printStackTrace();
//			}
			
//			try {
//				t.join(2000);
//			} catch (InterruptedException ie) {
//				ie.printStackTrace();
//			}
//			System.out.println("      Starting: " + pt);
//			System.out.println("        thread: " + idx);
//			System.out.println("            at: " + 
//					sdf.format(new Date(System.currentTimeMillis())));
//			threads[idx] = new Thread(new TestRunner(pt));
//			threads[idx].start();
		}

		//String end = sdf.format(new Date(System.currentTimeMillis()));
//		
//		// final poll
//		System.out.println("Finishing...");
//		while (!finished()) {
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException ie) {
//				ie.printStackTrace();
//			}
//		}
//		
		//System.out.println("      End Time: " + end);
	}
	
	// returns -1 if no Thread space available
//	private int pollThreads() {
//		System.out.println("        -- polling");
//		for (int i=0; i<threads.length; i++) {
//			if (threads[i] == null || !threads[i].isAlive()) {
//				return i;
//			}
//		}
//		return -1;
//	}
	
	
//	private boolean finished() {
//		for (int i=0; i<threads.length; i++) {
//			if (threads[i].isAlive()) return false;
//		}
//		return true;
//	}

	class TestRunner implements Runnable {
		
		private PeerTest test;

		TestRunner(PeerTest test) {
			this.test = test;
		}


		public void run() {
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
				
				FileWriter peerFile = new FileWriter(
						PEER_DIR + test + "-PGA_OpenSHA.txt");
				for (int j = 0; j < adf.getNum(); ++j)
					peerFile.write(adf.get(j).getX() + "\t"
							+ adf.get(j).getY() + "\n");
				peerFile.close();

			} catch (Exception e) {
				System.out.println("Failed: " + test);
				e.printStackTrace();
			}
		}
	}
	
	
	private static String getMeta(TestConfig tc) {
		
		StringBuffer sb = new StringBuffer();
		sb.append("IMR params: \n");
		sb.append(tc.getIMR().getAllParamMetadata() + "\n");
		sb.append("Site params: \n");
		sb.append(tc.getSite() + "\n");
		sb.append(tc.getSite().getParameterListMetadataString("\n") + "\n");
		sb.append("IMT params: \n");
		sb.append(tc.getIMR().getIntensityMeasure().getMetadataString() + "\n");
		sb.append("ERF params: \n");
		sb.append(tc.getERF().getAdjustableParameterList().getParameterListMetadataString("\n") + "\n");
		sb.append("TimeSpan params: \n");
		sb.append("Duration: " + tc.getERF().getTimeSpan().getDuration() + "\n");
		sb.append("Units: " + tc.getERF().getTimeSpan().getDurationUnits() + "\n");
		return sb.toString();
	}

//	private void runTests() {
//		TestConfig config = new TestConfig();
//		
//		//if (this.runAllPeerTestsCP.runAllPEER_TestCases()) {
//			try {
//				//progressCheckBox.setSelected(false);
//				//String peerDirName = "PEER_TESTS/";
//				// creating the peer directory in which we put all the peer
//				// related files
//				//File peerDir = new File(peerDirName);
//				//if (!peerDir.isDirectory()) { // if main directory does not
//												// exist
//				//	boolean success = (new File(peerDirName)).mkdir();
//				//}
//				
//				// output dir
//				String peerDirName = "PEER_TESTS/";
//				File peerDir = new File(peerDirName);
//				peerDir.mkdirs();
//				
//				//ArrayList<TestDescriptor> testCases =
//				//	config.getSetOTwoDecriptors();
//				ArrayList<TestDescriptor> testCases = 
//					config.getSetOneDecriptors();
//
//				/*
//				 * if(epistemicControlPanel == null) epistemicControlPanel =
//				 * new ERF_EpistemicListControlPanel(this,this);
//				 * epistemicControlPanel.setCustomFractileValue(05);
//				 * epistemicControlPanel.setVisible(false);
//				 */
//				// System.out.println("size="+testCases.size());
//				
//				// NOTE this was only needed for Set2 Case 5
//				// that was an epistemic list erf
//				//setAverageSelected(true);
//				
//				/*
//				 * size=106 for Set 1 Case1: 0-6 Case2: 7-13 Case3: 14-20
//				 * Case4: 21-27 Case5 28-34 Case6: 35-41 Case7: 42-48
//				 * Case8a: 49-55 Case8b: 56-62 Case8c: 63-69 Case9a: 70-76
//				 * Case9b: 77-83 Case9c: 84-90 Case10: 91-95 Case11: 96-99
//				 * Case12: 100-106
//				 * 
//				 * DOING ALL TAKES ~24 HOURS?
//				 */
//				for (int i = 0; i < testCases.size(); ++i) {
//					// for(int i=35 ;i < 35; ++i){
//					System.out.println("Working on # " + (i + 1) + " of "
//							+ testCases.size());
//
//					// first do PGA
//					config.setTest(testCases.get(i));
//					calculate();
//
//					FileWriter peerFile = new FileWriter(peerDirName
//							+ (String) testCases.get(i)
//							+ "-PGA_OpenSHA.txt");
//					DiscretizedFuncAPI func = (DiscretizedFuncAPI) functionList.get(0);
//					for (int j = 0; j < func.getNum(); ++j)
//						peerFile.write(func.get(j).getX() + "\t"
//								+ func.get(j).getY() + "\n");
//					peerFile.close();
//					clearPlot();
//
//					// now do SA
//					/*
//					 * imtGuiBean.getParameterList().getParameter(IMT_GuiBean
//					 * .IMT_PARAM_NAME).setValue(SA_Param.NAME);
//					 * imtGuiBean.getParameterList
//					 * ().getParameter(PeriodParam.NAME).setValue(new
//					 * Double(1.0)); addButton(); peerFile = new
//					 * FileWriter(peerDirName
//					 * +(String)testCasesTwo.get(i)+"-1secSA_OpenSHA.dat");
//					 * for(int j=0; j<totalProbFuncs.get(0).getNum();++j)
//					 * peerFile
//					 * .write(totalProbFuncs.get(0).get(j).getX()+" "
//					 * +totalProbFuncs.get(0).get(j).getY()+"\n");
//					 * peerFile.close(); this.clearPlot(true);
//					 */
//
//				}
//				System.exit(101);
//				// peerResultsFile.close();
//			} catch (Exception ee) {
//				ExceptionWindow bugWindow = new ExceptionWindow(this, ee,
//						getParametersInfoAsString());
//				bugWindow.setVisible(true);
//				bugWindow.pack();
//			}
//		//}
//
//	}
}
