package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.A_Faults;

import org.opensha.calc.BPT_DistCalc;


/**
 * <p>Title: WG02_QkProbCalc </p>
 * <p>Description: 
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date July, 2007
 * @version 1.0
 */


public class WG02_QkProbCalc {
	
	//for Debug purposes
	private static String C = new String("WG02_QkProbCalc");
	private final static boolean D = false;
	
	//name for this classs
	protected String NAME = "WG02_QkProbCalc";
		
	private double[] segProb, rupProb;
	
	
	/*
	 * For segment specific alpha.  Note that segRate could be computed from rupRate and rupInSeg,
	 * but we don't do that in case this is called multiple times with the same values (e.g., in 
	 * a simulation).  It is up to the user do make sure segRate is consistent with rupRate and rupInSeg.
	 */
	public void computeProbs(double[] segRate, double[] rupRate, double[] segMoRate, 
			double[] segAlpha, double[] segTimeSinceLast, double duration, double[][] rupInSeg) {
		
		computeSegProbs(segRate, segAlpha, segTimeSinceLast, duration);
		computeRupProbs(segRate, rupRate, segMoRate, rupInSeg);
	}


	/*
	 * For constant alpha.    Note that segRate could be computed from rupRate and rupInSeg,
	 * but we don't do that in case this is called multiple times with the same values (e.g., in 
	 * a simulation).  It is up to the user do make sure segRate is consistent with rupRate and rupInSeg.
	 */
	public void computeProbs(double[] segRate, double[] rupRate, double[] segMoRate, 
			double segAlpha, double[] segTimeSinceLast, double duration, double[][] rupInSeg) {
		
		computeSegProbs(segRate, segAlpha, segTimeSinceLast, duration);
		computeRupProbs(segRate, rupRate, segMoRate, rupInSeg);
	}

	
	private void computeRupProbs(double[] segRate, double[] rupRate, double[] segMoRate, double[][] rupInSeg) {
		
		int num_seg = segRate.length;
		int num_rup = rupRate.length;

		rupProb = new double[num_rup];
		for(int rup=0; rup<num_rup; rup++) {
			// compute sum of segMoRates for segs in rupture
			double totMoRate = 0;
			for(int seg=0; seg < num_seg; seg++)
				totMoRate += segMoRate[seg]*rupInSeg[seg][rup];
			// now compute sum
			double sum = 0;
			for(int seg=0; seg < num_seg; seg++)
				sum += segProb[seg]*segMoRate[seg]*rupInSeg[seg][rup]/segRate[seg];
			rupProb[rup] = rupRate[rup]*sum/totMoRate;
		}				
	}

	/*
	 * compute seg probs
	 */
	private void computeSegProbs(double[] segRate, double[] segAlpha, double[] segTimeSinceLast, double duration) {
		int num_seg = segRate.length;
		segProb = new double[num_seg];
		for(int seg=0; seg < num_seg; seg++)
			segProb[seg] = BPT_DistCalc.getCondProb(segTimeSinceLast[seg], segRate[seg], segAlpha[seg], duration);
	}

	/*
	 * compute seg probs
	 */
	private void computeSegProbs(double[] segRate, double segAlpha, double[] segTimeSinceLast, double duration) {
		int num_seg = segRate.length;
		segProb = new double[num_seg];
		for(int seg=0; seg < num_seg; seg++)
			segProb[seg] = BPT_DistCalc.getCondProb(segTimeSinceLast[seg], segRate[seg], segAlpha, duration);
	}
	
	public double[] getSegProbs() {return segProb;}

	public double[] getRupProbs() {return rupProb;}
	
	

	
	/*
	 * This tests results against those obtained from the WGCEP-2002 Fortran code (single branch 
	 * for SAF done by Ned Field in Feb of 2006; see his "Neds0206TestOutput.txt" file).  Results
	 * are within 0.5%.

	 */
	public static void main(String args[]) {
		double[] segRate = {0.00466746464,0.00432087015,0.004199435,0.004199435};
		double[] rupRate = {0.00145604357,0.000706832856,0.,0.,0.000505269971,0.,0.00109066791,0.,0.000402616395,0.00270615076};
		double[] segMoRate  = {4.74714853E+24,5.62020641E+24,1.51106804E+25,1.06885024E+25};
		double[] segAlpha = {0.5,0.5,0.5,0.5};
		double alpha = 0.5;
		double[] segTimeSinceLast = {96, 96, 96, 96};
		double duration = 30;
		double[][] rupInSeg = {
				// 1,2,3,4,5,6,7,8,9,10
				{1,0,0,0,1,0,0,1,0,1}, // seg 1
				{0,1,0,0,1,1,0,1,1,1}, // seg 2
				{0,0,1,0,0,1,1,1,1,1}, // seg 3
				{0,0,0,1,0,0,1,0,1,1} // seg 4
		};
		// test values
		double[] testRupProb = {0.0405939706,0.017191546,0.,0.,0.0131122563,0.,0.0250523612,0.,0.00934537873,0.0644722432};
		double[] testSegProb = {0.130127236,0.105091952,0.0964599401,0.0964599401};

		WG02_QkProbCalc calc = new WG02_QkProbCalc();
		// try with both alpha and segAlpha
		calc.computeProbs(segRate,rupRate,segMoRate,alpha,segTimeSinceLast,duration,rupInSeg);
		double[] rupProb = calc.getRupProbs();
		// write out test results 
		/*
		System.out.println("Test rup fractional differences:");
		for(int rup=0; rup<10;rup++)
			if(rupProb[rup] == 0)
				System.out.println("rup"+rup+": "+(float)(rupProb[rup]-testRupProb[rup]));
			else
				System.out.println("rup"+rup+": "+(float)((rupProb[rup]-testRupProb[rup])/testRupProb[rup]));
		*/
		
	}
}

