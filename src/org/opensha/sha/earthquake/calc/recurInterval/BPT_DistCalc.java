/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.earthquake.calc.recurInterval;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;


/**
 * <b>Title:</b> BPT_DistCalc.java <p>
 * <b>Description:</p>.
 <p>
 *
 * @author Edward Field
 * @created    July, 2007
 * @version 1.0
 */

public final class BPT_DistCalc extends EqkProbDistCalc implements ParameterChangeListener {
	
	// this defines how closely to 0 the denominator of the conditional probability calculation can get
	// about ??? greater than double precision accuracy (check by trial and error over aperiodicity from 0.1 to 1.0)
	final static double SAFE_ONE_MINUS_CDF = 1e-13;
	final static double MIN_NORM_DURATION = 0.01;
	
	double safeTimeSinceLast=Double.NaN;
	
	public BPT_DistCalc() {
		NAME = "BPT";
		super.initAdjParams();
	}
	
	
	
	
	/*
	 * This computes the PDF and then the cdf from the pdf using 
	 * Trapezoidal integration. 
	 */
	protected void computeDistributions() {
		
		// make these null
		integratedCDF = null;
		integratedOneMinusCDF = null;

		pdf = new EvenlyDiscretizedFunc(0,numPoints,deltaX);
		cdf = new EvenlyDiscretizedFunc(0,numPoints,deltaX);
		// set first y-values to zero
		pdf.set(0,0);
		cdf.set(0,0);
		
		double temp1 = mean/(2.*Math.PI*(aperiodicity*aperiodicity));
		double temp2 = 2.*mean*(aperiodicity*aperiodicity);
		double t,pd,cd=0;
		for(int i=1; i< pdf.getNum(); i++) { // skip first point because it's NaN
			t = cdf.getX(i);
			pd = Math.sqrt(temp1/(t*t*t)) * Math.exp(-(t-mean)*(t-mean)/(temp2*t));
			if(Double.isNaN(pd)){
				pd=0;
				System.out.println("pd=0 for i="+i);
			}
			cd += deltaX*(pd+pdf.getY(i-1))/2;  // Trapizoidal integration
			pdf.set(i,pd);
			cdf.set(i,cd);
		}
		computeSafeTimeSinceLastCutoff();
		upToDate = true;
	}

	

	/**
	 * This computed the conditional probability using Trapezoidal integration (slightly more
	 * accurrate that the WGCEP-2002 code, which this method is modeled after). Although this method 
	 * is static (doesn't require instantiation), it is less efficient than the non-static version 
	 * here (it is also very slightly less accurate because the other interpolates the cdf). 
	 * Note also that if timeSinceLast/mean > aperiodicity*10, timeSinceLast is changed to equal
	 * mean*aperiodicity*10 (to avoid numerical problems at high timeSinceLast). 
	 * @param timeSinceLast - time since last event
	 * @param rate - average rate of events
	 * @param alpha - coefficient of variation (technically corrrect??)
	 * @param duration - forecast duration
	 * @return
	 */
	public static double getCondProb(double mean, double aperiodicity, double timeSinceLast, double duration) {
		
		double step = 0.001;
		double cdf=0, pdf, pdf_last=0, t, temp1, temp2, x, cBPT1=0, cBPT2;
		int i, i1, i2;
		
		// avoid numerical problems when too far out on tails
		if ( timeSinceLast/mean > aperiodicity*10 )
			x = 10.*aperiodicity*mean;
		else
			x = timeSinceLast;
		
		// find index of the two points in time
		i1 = Math.round((float)((x/mean)/step));
		i2 = Math.round((float)(((x+duration)/mean)/step));
		
		temp1 = 1/(2.*Math.PI*(aperiodicity*aperiodicity));
		temp2 = 2.*(aperiodicity*aperiodicity)*1;
		t = step*1.;
		for(i=1; i<=i2; i++) {
			pdf = Math.sqrt(temp1/(t*t*t)) * Math.exp(-(t-1)*(t-1) / (temp2*t) );
			
			cdf += step*(pdf+pdf_last)/2;
			if ( i == i1 ) cBPT1 = cdf;
/*
			if ( i == i1 || i == i2) {
				System.out.println("time = "+t);
			}
			System.out.println(i+"\t"+t+"\t"+pdf+"\t"+cdf);
*/
			t += step;
			pdf_last=pdf;
		}
		cBPT2 = cdf;
		
		if ( cBPT1 >= 1.0 )
			return Double.NaN;
		else
			return (cBPT2-cBPT1)/( 1.-cBPT1);
		
	}	
	
	
	/**
	 * This is a version of the parent method getCondProb(*) that avoids numerical artifacts
	 * at high timeSinceLast (cdf gets too close to 1 and therefore we have division by 
	 * nearly zero).
	 * If timeSinceLast+duration is greater than safeTimeSinceLast, then the conditional probability at 
	 * safeTimeSinceLast-duration is returned (i.e., cond prob becomes constant near safeTimeSinceLast).
	 * The safe values have been visually verified for aperiodicity values of 0.1 to 1.0 (using the GUI).
	 * @param timeSinceLast
	 * @param duration
	 * @return
	 */
	public double getSafeCondProb(double timeSinceLast, double duration) {
		this.duration=duration;
		if(!upToDate) computeDistributions();
		
		double result=Double.NaN;
		
		if(duration/mean >= MIN_NORM_DURATION) {
//			System.out.println("good");
			if(timeSinceLast+duration <= safeTimeSinceLast) {
				result = getCondProb(timeSinceLast, duration);
			}
			else {
				if(safeTimeSinceLast-duration<0)
					return 1.0; // very long duration
				double condProbAtSafeTime = getCondProb(safeTimeSinceLast-duration, duration);
				double condProbAtInfTime = 1-Math.exp(-duration/(aperiodicity*aperiodicity*mean*2)); // based on Equation 24 of Matthews et al. (2002).
				// linear interpolate assuming inf time is mean*10
				result = condProbAtSafeTime + (condProbAtInfTime-condProbAtSafeTime)*(timeSinceLast-(safeTimeSinceLast-duration))/(10*mean-(safeTimeSinceLast-duration));
			}			
		}
		else {
//			System.out.println("bad");
			double condProbForMinNormDur = getSafeCondProb(timeSinceLast, MIN_NORM_DURATION*mean);
			result = condProbForMinNormDur*duration/(MIN_NORM_DURATION*mean);
			
		}
		
		 this.duration=duration;	// can't set this first thing or it screws the above up
		 return result;

		// test:
//		double result = (p2-p1)/(1.0-p1);;
//		double p1_alt = cdf.getInterpolatedY(timeSinceLast);
//		double p2_alt = cdf.getInterpolatedY(timeSinceLast+duration/10d);
//		
//		double result_alt = 10*(p2_alt-p1_alt)/(1.0-p1_alt);;
//		System.out.println(timeSinceLast+"\t"+(p2-p1)+"\t"+(1.0-p1)+"\t"+result+"\t"+result_alt+"\t"+(float)(result_alt/result));


		
//		if(timeSinceLast+duration <= safeTimeSinceLast) {
//			return getCondProb(timeSinceLast, duration);
//		}
//		else {
//			double condProbAtSafeTime = getCondProb(safeTimeSinceLast-duration, duration);
//			double condProbAtInfTime = 1-Math.exp(-duration/(aperiodicity*aperiodicity*mean*2)); // based on Equation 24 of Matthews et al. (2002).
//			// linear interpolate assuming inf time is mean*10
//			return condProbAtSafeTime + (condProbAtInfTime-condProbAtSafeTime)*(timeSinceLast-(safeTimeSinceLast-duration))/(10*mean-(safeTimeSinceLast-duration));
//		}
		
//		double newTimeSinceLast=timeSinceLast;
//		if(timeSinceLast+duration > safeTimeSinceLast) {
//			newTimeSinceLast = safeTimeSinceLast-duration-cdf.getDelta();
//		}
//		
//		if(newTimeSinceLast<0) // if safeTimeSinceLast is less than duration, it must be a very long duration compared to recurrence interval
//			return 1.0;
////			throw new RuntimeException(this.mean+"\t"+timeSinceLast+"\t"+newTimeSinceLast+"\t"+safeTimeSinceLast+"\t"+duration);
//		
//		double p1 = cdf.getInterpolatedY(newTimeSinceLast);
//		double p2 = cdf.getInterpolatedY(newTimeSinceLast+duration);
//		double denom = 1.0-p1;
//		return (p2-p1)/denom;
	}	
	
	
	public EvenlyDiscretizedFunc getCondProbFunc() {
		if(duration==0)
			throw new RuntimeException("duration has not been set");
		if(!upToDate) computeDistributions();
		int numPts = numPoints - (int)(duration/deltaX+1);
		EvenlyDiscretizedFunc condFunc = new EvenlyDiscretizedFunc(0.0, numPts , deltaX);
		for(int i=0;i<condFunc.getNum();i++) {
			condFunc.set(i,getSafeCondProb(condFunc.getX(i), duration));
		}
		condFunc.setName(NAME+" Safe Conditional Probability Function");
		condFunc.setInfo(adjustableParams.toString()+"\n"+"safeTimeSinceLast="+safeTimeSinceLast);
		return condFunc;
	}
	
	/**
	 * This computes the probability of an event over the specified duration for the case where the 
	 * date of last event is unknown (looping over all possible values), but where the historic open 
	 * interval is applied (the latter defaults to zero if never set).
	 * 
	 * If (histOpenInterval>safeTimeSinceLast), this returns condProbFunc.getY(safeTimeSinceLast) 
	 * because values are constant above.
	 * @return
	 */
	public double getSafeCondProbForUnknownTimeSinceLastEvent() {
		if(!upToDate) computeDistributions();

		if(histOpenInterval>=safeTimeSinceLast) {
			// we're in the range where cond prob is constant, so any wted ave must equal the constant value
			return this.getSafeCondProb(safeTimeSinceLast,duration);
		}
		
		if(integratedCDF==null) 
			makeIntegratedCDFs();
		double lastTime = histOpenInterval+duration;
		if(lastTime>integratedCDF.getMaxX())
			lastTime=integratedCDF.getMaxX();
		double numer = duration - (integratedCDF.getInterpolatedY(lastTime)-integratedCDF.getInterpolatedY(histOpenInterval));
		double denom = (integratedOneMinusCDF.getY(numPoints-1)-integratedOneMinusCDF.getInterpolatedY(histOpenInterval));
		double result = numer/denom;
		
		if(numer >1e13) {
			return result;
		}
		else {
			result=0;
			double normDenom=0;
			EvenlyDiscretizedFunc condProbFunc = getCondProbFunc();
			int firstIndex = condProbFunc.getClosestXIndex(histOpenInterval);
			int indexOfSafeTime = condProbFunc.getClosestXIndex(safeTimeSinceLast);	// need to use closest because condProbFunc has fewer points than CDF (so safeTimeSinceLast can exceed the x-axis range)
	
//			if(firstIndex>=indexOfSafeTime) {
//				// we're in the range where cond prob is constant, so avoid numerical errors and just return the following
//				return condProbFunc.getY(indexOfSafeTime);
//			}
			
			for(int i=firstIndex;i<condProbFunc.getNum();i++) {
//			for(int i=firstIndex;i<=indexOfSafeTime;i++) {
				double probOfTimeSince = (1-cdf.getY(i));
				if(i==firstIndex)
					probOfTimeSince *= ((cdf.getX(i)+deltaX/2.0) - histOpenInterval)/deltaX;	// fraction of first bin
				normDenom+=probOfTimeSince; 
				result+= condProbFunc.getY(i)*probOfTimeSince;
			}
			result /= normDenom;	// normalize properly
			
			if(result>1) result=1;
			
			return result;
		}
		
//		System.out.println(Math.log10(duration/mean)+"\t"+Math.log10(histOpenInterval/mean)+"\t"+numer+"\t"+denom+"\t"+result);
		
		
		
		
//		// OLD VERSION
//		double result=0;
//		double normDenom=0;
//		EvenlyDiscretizedFunc condProbFunc = getCondProbFunc();
//		int firstIndex = condProbFunc.getClosestXIndex(histOpenInterval);
//		int indexOfSafeTime = condProbFunc.getClosestXIndex(safeTimeSinceLast);	// need to use closest because condProbFunc has fewer points than CDF (so safeTimeSinceLast can exceed the x-axis range)
//
//		//		if(histOpenInterval>safeTimeSinceLast) {
//		if(firstIndex>=indexOfSafeTime) {
//			// we're in the range where cond prob is constant, so avoid numerical errors and just return the following
//			return condProbFunc.getY(indexOfSafeTime);
//		}
////		for(int i=firstIndex;i<condProbFunc.getNum();i++) {
//		for(int i=firstIndex;i<=indexOfSafeTime;i++) {
//			double probOfTimeSince = (1-cdf.getY(i));
//			if(i==firstIndex)
//				probOfTimeSince *= ((cdf.getX(i)+deltaX/2.0) - histOpenInterval)/deltaX;	// fraction of first bin
//			normDenom+=probOfTimeSince; 
//			result+= condProbFunc.getY(i)*probOfTimeSince;
//		}
//		result /= normDenom;	// normalize properly
//		
//		if(result>1) result=1;
//		
//		return result;
	}

	
	/**
	 * This returns the maximum value of timeSinceLast (as discretized in the x-axis of the cdf) that is  
	 * numerically safe (to avoid division by zero in the conditional probability calculations, where the
	 * denominator is 1-cdf). This returns Double.Nan if no x-axis values are safe (not even the first ones).  
	 * The threshold for safe values was found by trial and error and checked for aperiodicity values between 
	 * 0.1 and 1.0 (using the GUI).
	 * @return
	 */
	public double getSafeTimeSinceLastCutoff() {
		if(!upToDate) computeDistributions();
		return safeTimeSinceLast;
	}
	
	/**
	 * This finds the largest x-axis value such that (1.0-cdf.getY(x)) >= SAFE_ONE_MINUS_CDF
	 * (not too close to zero, as this is the denominator of the conditional probability calculation)
	 */
	private void computeSafeTimeSinceLastCutoff() {
		safeTimeSinceLast = Double.NaN;
		for(int x=0;x<cdf.getNum();x++) {
			if(1.0-cdf.getY(x) < SAFE_ONE_MINUS_CDF) {	// when cdf gets too close to 1, keep last safeTimeSinceLast
				break;
			}
			else {
				safeTimeSinceLast = cdf.getX(x);
			}
		}
		
		if(Double.isNaN(safeTimeSinceLast)) {
			throw new RuntimeException ("CDF never gets close to 1.0; need to increase numPoints?");
		}
		
//		System.out.println("safeTimeSinceLast="+safeTimeSinceLast);
	}
	
	
	public void testSafeCalcs() {
		
		double[] durations = {1,0.1,0.01,0.001,0.0001};
		double[] aperiodicities = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
		
		System.out.println("fractDiff\tsafeProb\tprob\taper\ttimeSince\tdur\tsafeDist");

		for(double aper: aperiodicities) {
			for(double dur:durations) {
				setAll(1.0, aper, 0.01, 1000,dur);
				double safeDist = getSafeTimeSinceLastCutoff();
				EvenlyDiscretizedFunc safeCondProbFunc = getCondProbFunc();
				EvenlyDiscretizedFunc condProbFunc = super.getCondProbFunc();
				for(int i=0;i<safeCondProbFunc.getNum();i++) {
					double timeSince = safeCondProbFunc.getX(i);
					if(timeSince<safeDist) {
						double safeProb = safeCondProbFunc.getY(i);
						double prob = condProbFunc.getY(i);
						double fractDiff;
						if(prob<1e-16 && safeProb<1e-16) {
							fractDiff=0.0;
						}
						else {
							fractDiff = Math.abs((safeProb-prob)/safeProb);
						}
						if(fractDiff > 0.001) {
							System.out.println(fractDiff+"\t"+safeProb+"\t"+prob+"\t"+aper+"\t"+timeSince+"\t"+dur+"\t"+safeDist);
						}
					}
				}
			}			
		}
	}

	
	/**
	 *  Main method for running tests.  
	 *  Test1 compares the static getCondProb(*) method against values obtained directly from the WGCEP-2002 
	 *  code; all are within 0.3%.
	 *  Test2 campares the non static getCondProb(*) method against the static; all are within 0.5%.  The 
	 *  differences is that the non-static is slightly more accurate due to interpolation of the CDF
	 *  (exact same values are obtained otherwise; see commented out code).
	 *  Test3 is the non-static used more efficiently (exact same values as from non-static above); this
	 *  is about a factor of two faster.
	 *  Test4 examines what happens if delta is changed to 0.01 in the non-static method (also about
	 *  a factor of two faster).
	 */
	public static void main(String args[]) {
		
		BPT_DistCalc calcBPT = new BPT_DistCalc();
		
		calcBPT.testSafeCalcs();
		System.exit(0);

		// test data from WGCEP-2002 code run (single branch for SAF) done by Ned Field
		// in Feb of 2006 (see his "Neds0206TestOutput.txt" file).

		double timeSinceLast = 96;
		double nYr = 30;
		double alph = 0.5;
		double[] rate = {0.00466746464,0.00432087015,0.004199435,0.004199435};
		double[] prob = {0.130127236,0.105091952,0.0964599401,0.0964599401};
/*
		// this is a test of a problematic case (which let to a bug identification).  This case
		// shows a 4% difference between the static and non-static methods due to interpolation
		double timeSinceLast = 0.247;
		double nYr = 0.0107;
		double alph = 0.5;
		double[] rate = {1};
		double[] prob = {8.3067856E-4}; // this is the value given by the static method
		double timeSinceLast = 115;
//		double nYr = 5;
//		double alph = 0.5;
//		double[] rate = {0.002149};
//		double[] prob = {8.3067856E-4}; // this is the value given by the static method
*/

		// Test1
		double[] static_prob = new double[rate.length];
		double p;
		System.out.println("Test1: static-method comparison with probs from WG02 code");
		for(int i=0;i<rate.length;i++) {
			p = getCondProb(1/rate[i],alph, timeSinceLast, nYr);
			System.out.println("Test1 (static): prob="+(float)p+"; ratio="+(float)(p/prob[i]));
			static_prob[i]=p;
		}

		
		BPT_DistCalc calc = new BPT_DistCalc();
		
		// Test2
		double[] nonStatic_prob = new double[rate.length];

		System.out.println("Test2: non-static method compared to static");
		for(int i=0;i<rate.length;i++) {
			calc.setAll((1/rate[i]),alph);
			p = calc.getCondProb(timeSinceLast,nYr);
			System.out.println("Test2: prob="+(float)p+"; ratio="+(float)(p/static_prob[i]));
			nonStatic_prob[i]=p;
		}

		/*
		// Test3
		System.out.println("Test3: non-static method used efficiently compared to non-static");
		calc.setAll(1,alph);
		for(int i=0;i<rate.length;i++) {
			p = calc.getCondProb(timeSinceLast*rate[i],nYr*rate[i]);
			System.out.println("Test3: prob="+(float)p+"; ratio="+(float)(p/nonStatic_prob[i]));
		}
		
		
		// Speed tests
		// First the static method
		long milSec0 = System.currentTimeMillis();
		int numCalcs = 10000;
		for(int i=0; i< numCalcs; i++)
			p = getCondProb(1/rate[0],alph,timeSinceLast,nYr);
		double time = (double)(System.currentTimeMillis()-milSec0)/1000;
		System.out.println("Speed Test for static method = "+(float)time+" sec");
		// now the faster way
		milSec0 = System.currentTimeMillis();
		calc.setAll(1,alph);
		for(int i=0; i< numCalcs; i++)
			p = calc.getCondProb(timeSinceLast*rate[0],nYr*rate[0]);
		double time2 = (double)(System.currentTimeMillis()-milSec0)/1000;
		System.out.println("Speed Test for non-static used efficiently = "+(float)time2+" sec");
		System.out.println("Ratio of non-static used efficiently to static = "+(float)(time2/time));
		
		
		// test the delta=0.01 case
		System.out.println("Test4: comparison of non-static and non static w/ delta=0.01");
		for(int i=0;i<rate.length;i++) {
			double mri = 1/rate[i];
			int num = (int)(10*alph*mri/0.01);
			calc.setAll(mri,alph,0.01,num);
			p = calc.getCondProb(timeSinceLast,nYr);
			System.out.println("Test4 (delta=0.01): ="+(float)p+"; ratio="+(float)(p/nonStatic_prob[i]));
		}

		// Another Speed test
		milSec0 = System.currentTimeMillis();
		calc.setAll(1,alph);
		for(int i=0; i< numCalcs; i++)
			p = calc.getCondProb(timeSinceLast*rate[0],nYr*rate[0]);
		double time3 = (double)(System.currentTimeMillis()-milSec0)/1000;
		System.out.println("Speed Test for deltaX = 0.01 & non static used effieicintly = "+(float)time3+" sec");
		System.out.println("Ratio of compute time above versus static  = "+(float)(time3/time));
		*/
	}
	

}

