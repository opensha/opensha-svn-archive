package org.opensha.sha.imr.attenRelImpl.test;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.opensha.commons.param.event.ParameterChangeWarningEvent;
import org.opensha.commons.param.event.ParameterChangeWarningListener;

public abstract class NGATest extends TestCase implements ParameterChangeWarningListener {
	
	public static double tolerance = 0.5;
	
	private String dir;
	
	public NGATest(String arg0, String dir) {
		super(arg0);
		this.dir = dir;
	}
	
	/**
	 * Tests a single file
	 * @param filePath
	 * @return discrepancy, or negative number for failure
	 */
	public abstract double doSingleFileTest(File file);
	
	public abstract String getLastFailMetadata();
	
	public abstract String getLastFailLine();
	
	public double compareResults(double valFromSHA,
			double targetVal){
		//comparing each value we obtained after doing the IMR calc with the target result
		//and making sure that values lies with the .01% range of the target values.
		//comparing if the values lies within the actual tolerence range of the target result
		double result = 0;
		if(targetVal!=0)
			result =(StrictMath.abs(valFromSHA-targetVal)/targetVal)*100;

		return result;
	}
	
	private ArrayList<File> getTestFiles() {
		File f = new File(dir);
		File[] fileList = f.listFiles();
		
		ArrayList<File> files = new ArrayList<File>();
		
		for(int i=0;i<fileList.length;++i) {

			String fileName = fileList[i].getName();

			if(fileName.contains("README") || fileName.contains("COEF")
					|| !(fileName.contains(".OUT") || fileName.contains(".TXT")))
				continue; // skip the README/COEF/Fortran files
			
			files.add(fileList[i]);
		}
		
		return files;
	}
	
	public void testAll() {
		double maxDisc = 0;
		for(File file : getTestFiles()) {
			double discrep = doSingleFileTest(file);
			assertTrue(discrep >= 0);
			if (discrep > maxDisc)
				maxDisc = discrep;
		}
		System.out.println("Maximum discrepancy: " + maxDisc);
	}
	
	public void runDiagnostics() throws Exception {
		this.setUp();
		double maxDisc = 0;
		String summary = "";
		for(File file : getTestFiles()) {
			double discrep = doSingleFileTest(file);
			if (discrep > maxDisc)
				maxDisc = discrep;
			
			if (discrep < 0) { // fail
				summary += "\n" + file.getName() + ": FAILED for line:";
				summary += "\n" + this.getLastFailLine();
			} else {	// good
				summary += "\n" + file.getName() + ": PASSED for discrepancey: " + discrep;
			}
		}
		System.out.println(summary);
		System.out.println("Maximum discrepancy: " + maxDisc);
	}
	
	protected double[] loadPeriods(String line) {
		StringTokenizer tok = new StringTokenizer(line);
		
		// skip the first 9
		for (int i=0; i<9; i++) {
			tok.nextToken();
		}
		
		String col = tok.nextToken();
		
		ArrayList<Double> periodList = new ArrayList<Double>();
		
		while (!col.contains("PGA")) {
			periodList.add(Double.parseDouble(col));
			
			col = tok.nextToken();
		}
		
		double periods[] = new double[periodList.size()];
		
		String str = "";
		
		for (int i=0; i<periodList.size(); i++) {
			periods[i] = periodList.get(i);
			str += " " + periods[i];
		}
		
		System.out.println("Periods:" + str);
		
		return periods;
	}
	
	public void parameterChangeWarning(ParameterChangeWarningEvent e){
		System.err.println("Parameter change warning!");
		System.err.flush();
		return;
	}
	
}
