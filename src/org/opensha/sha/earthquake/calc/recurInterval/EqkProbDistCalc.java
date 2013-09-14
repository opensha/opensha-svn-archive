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

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.IntegerParameter;


/**
 * <b>Title:</b> EqkProbDistCalc.java <p>
 * <b>Description:</p>.
 <p>
 *
 * @author Edward Field
 * @created    July, 2007
 * @version 1.0
 */

public abstract class EqkProbDistCalc implements ParameterChangeListener {
	
	protected EvenlyDiscretizedFunc pdf, cdf;
	protected double mean, aperiodicity, deltaX, duration, histOpenInterval;
	protected int numPoints;
	public static final double DELTA_X_DEFAULT = 0.001;
	protected boolean upToDate=false;
	protected  String NAME;
	
	// Parameter names
	protected final static String MEAN_PARAM_NAME= "Mean";
	protected final static String APERIODICITY_PARAM_NAME = "Aperiodicity";
	protected final static String DURATION_PARAM_NAME = "Duration";
	protected final static String DELTA_X_PARAM_NAME = "Delta X";
	protected final static String NUM_POINTS_PARAM_NAME = "Num Points";
	protected final static String HIST_OPEN_INTERVAL_PARAM_NAME = "Historic Open Interval";
	
	// Parameter Infos
	protected final static String MEAN_PARAM_INFO= "Mean";
	protected final static String APERIODICITY_PARAM_INFO = "Aperiodicity";
	protected final static String DURATION_PARAM_INFO = "Duration";
	protected final static String DELTA_X_PARAM_INFO = "Delta X";
	protected final static String NUM_POINTS_PARAM_INFO = "Num Points";
	protected final static String HIST_OPEN_INTERVAL_PARAM_INFO = "Historic time interval over which event has not occurred";
	
	// default param values
	protected final static Double DEFAULT_MEAN_PARAM_VAL = new Double(100);
	protected final static Double DEFAULT_APERIODICITY_PARAM_VAL = new Double(0.5);
	protected final static Double DEFAULT_DURATION_PARAM_VAL = new Double(30);
	protected final static Double DEFAULT_DELTAX_PARAM_VAL = new Double(1);
	protected final static Integer DEFAULT_NUMPOINTS_PARAM_VAL = new Integer(500);
	protected final static Double DEFAULT_HIST_OPEN_INTERVAL_PARAM_VAL = new Double(0.0);
	
	// various adjustable params
	protected DoubleParameter meanParam, aperiodicityParam, durationParam, deltaX_Param, histOpenIntParam;
	protected IntegerParameter numPointsParam;
	
	// adjustable parameter list
	protected ParameterList adjustableParams;

	/*
	 * 
	 */
	abstract void computeDistributions();
	
	public EvenlyDiscretizedFunc getCDF() {
		if(!upToDate) computeDistributions();
		cdf.setName(NAME+" CDF");
		cdf.setInfo(adjustableParams.toString());
		return cdf;
	}

	public EvenlyDiscretizedFunc getPDF() {
		if(!upToDate) computeDistributions();
		pdf.setName(NAME+" PDF");
		pdf.setInfo(adjustableParams.toString());
getCondProbForUnknownTimeSinceLastEvent();
		return pdf;
	}

	public EvenlyDiscretizedFunc getHazFunc() {
		if(!upToDate) computeDistributions();
		EvenlyDiscretizedFunc hazFunc = new EvenlyDiscretizedFunc(0, pdf.getMaxX(), pdf.getNum());
		double haz;
		for(int i=0;i<hazFunc.getNum();i++) {
			haz = pdf.getY(i)/(1.0-cdf.getY(i));
			hazFunc.set(i,haz);
		}
		hazFunc.setName(NAME+" Hazard Function");
		hazFunc.setInfo(adjustableParams.toString());
		return hazFunc;
	}
	
	/*
	 * This gives a function of the probability of an event occurring between time T
	 * (on the x-axis) and T+duration, conditioned that it has not occurred before T.
	 * TODO THIS NEEDS TO BE TESTED
	 */
	public EvenlyDiscretizedFunc getCondProbFunc() {
		if(duration==0)
			throw new RuntimeException("duration has not been set");
		if(!upToDate) computeDistributions();
		int numPts = numPoints - (int)(duration/deltaX+1);
		EvenlyDiscretizedFunc condFunc = new EvenlyDiscretizedFunc(0.0, numPts , deltaX);
		for(int i=0;i<condFunc.getNum();i++) {
			condFunc.set(i,getCondProb(condFunc.getX(i), duration));
		}
		condFunc.setName(NAME+" Conditional Probability Function");
		condFunc.setInfo(adjustableParams.toString());
		return condFunc;
	}
	
	public EvenlyDiscretizedFunc getCondProbFunc(double durationYears) {
//		durationParam.setValue(durationYears);
		duration = durationYears;
		return getCondProbFunc();
	}

	

	/**
	 * This is a non-static version that is slightly more accurate (due to
	 * interpolation of the cdf function), although it requires instantiation of the class to
	 * access (and stores information internally). The commented out bit of code gives the non 
	 * interpolated result which is exactly the same as what comes from the static version.
	 * This does not check for numerical errors at high timeSinceLast (look for a getSafeCondProb(*)
	 * version of this method in subclasses.
	 * @param timeSinceLast
	 * @param duration
	 * @return
	 */
	public double getCondProb(double timeSinceLast, double duration) {
		if(!upToDate) computeDistributions();
		
		double p1 = cdf.getInterpolatedY(timeSinceLast);
		double p2 = cdf.getInterpolatedY(timeSinceLast+duration);
//		System.out.println("t1 and t2:\t"+timeSinceLast+"\t"+(timeSinceLast+duration));		
//		System.out.println("p1 and p2:\t"+p1+"\t"+p2);
		return (p2-p1)/(1.0-p1);

		
		// non interpolated alternative that gives the same result as the static methods:
/*
		int pt1 = (int)Math.round(timeSinceLast/deltaX);
		int pt2 = (int)Math.round((timeSinceLast+duration)/deltaX);
		double prob = (cdf.getY(pt2)-cdf.getY(pt1))/(1.0-cdf.getY(pt1));
//		System.out.println("pt1 and pt2:\t"+pt1+"\t"+pt2+"\t"+cdf.getX(pt1)+"\t"+cdf.getX(pt2));
//		System.out.println(cdf.getX(pt1)+"\t"+cdf.getX(pt2)+"\t"+cdf.getY(pt1)+"\t"+cdf.getY(pt2));
		return prob;
*/
		
	}	

	/**
	 * Initialize adjustable parameters
	 *
	 */
	protected void initAdjParams() {
	
		meanParam =  new  DoubleParameter(MEAN_PARAM_NAME, Double.MIN_VALUE, Double.MAX_VALUE, DEFAULT_MEAN_PARAM_VAL);
		meanParam.setInfo(MEAN_PARAM_INFO);
		meanParam.addParameterChangeListener(this);
		aperiodicityParam  = new DoubleParameter(APERIODICITY_PARAM_NAME, Double.MIN_VALUE, Double.MAX_VALUE, DEFAULT_APERIODICITY_PARAM_VAL);
		aperiodicityParam.setInfo(APERIODICITY_PARAM_INFO);
		aperiodicityParam.addParameterChangeListener(this);
		durationParam = new  DoubleParameter(DURATION_PARAM_NAME, Double.MIN_VALUE, Double.MAX_VALUE, DEFAULT_DURATION_PARAM_VAL);
		durationParam.setInfo(DURATION_PARAM_INFO);
		durationParam.addParameterChangeListener(this);
		deltaX_Param = new  DoubleParameter(DELTA_X_PARAM_NAME, Double.MIN_VALUE, Double.MAX_VALUE, DEFAULT_DELTAX_PARAM_VAL);
		deltaX_Param.setInfo(DELTA_X_PARAM_INFO);
		deltaX_Param.addParameterChangeListener(this);
		numPointsParam = new  IntegerParameter(NUM_POINTS_PARAM_NAME, Integer.MIN_VALUE, Integer.MAX_VALUE, DEFAULT_NUMPOINTS_PARAM_VAL);;
		numPointsParam.setInfo(NUM_POINTS_PARAM_INFO);
		numPointsParam.addParameterChangeListener(this);
		histOpenIntParam = new  DoubleParameter(HIST_OPEN_INTERVAL_PARAM_NAME, 0, Double.MAX_VALUE, DEFAULT_HIST_OPEN_INTERVAL_PARAM_VAL);
		histOpenIntParam.setInfo(HIST_OPEN_INTERVAL_PARAM_INFO);
		histOpenIntParam.addParameterChangeListener(this);

		adjustableParams = new ParameterList();
		adjustableParams.addParameter(meanParam);
		adjustableParams.addParameter(aperiodicityParam);
		adjustableParams.addParameter(durationParam);
		adjustableParams.addParameter(deltaX_Param);
		adjustableParams.addParameter(numPointsParam);
		adjustableParams.addParameter(histOpenIntParam);

		setAll(DEFAULT_MEAN_PARAM_VAL.doubleValue(), DEFAULT_APERIODICITY_PARAM_VAL.doubleValue(),
				DEFAULT_DELTAX_PARAM_VAL.doubleValue(), DEFAULT_NUMPOINTS_PARAM_VAL.intValue(),
				DEFAULT_DURATION_PARAM_VAL.doubleValue(), DEFAULT_HIST_OPEN_INTERVAL_PARAM_VAL.doubleValue());

	}
	
	/**
	 * Get adjustable params
	 * 
	 * @return
	 */
	public ParameterList getAdjParams() {
		return this.adjustableParams;
	}
	
	
	/**
	 * Get the name 
	 * @return
	 */
	public String getName() {
		return this.NAME;
	}
	
	
	/**
	 * This sets the values of the distribution by finding those that are best fit to the function passed in.
	 * The best-fit values are found via a grid search.
	 * @param dist - the distribution to be fit (should be a true pdf such that integration (sum of y values multiplied by deltaX) equals 1)
	 * @param minMean
	 * @param maxMean
	 * @param numMean
	 * @param minAper
	 * @param maxAper
	 * @param numAper
	 */
	public void fitToThisFunction(EvenlyDiscretizedFunc dist, double minMean, double maxMean,
			int numMean, double minAper, double maxAper,int numAper) {
		
		deltaX_Param.setValue(dist.getDelta()/2);	// increase discretization here just to be safe
		numPointsParam.setValue(dist.getNum()*2+1);	// vals start from zero whereas passed in histograms might start at delta/2
		double bestMean=0;
		double bestAper=0;
		double best_rms=Double.MAX_VALUE;
		double deltaMean=(maxMean-minMean)/(numMean-1);
		double deltaAper=(maxAper-minAper)/(numAper-1);
		for(int m=0;m<numMean;m++) {
			for(int c=0;c<numAper;c++) {
				meanParam.setValue(minMean+m*deltaMean);
				aperiodicityParam.setValue(minAper+c*deltaAper);
				EvenlyDiscretizedFunc pdf = this.getPDF();
				double rms=0;
				for(int i=0;i<dist.getNum()-1;i++) {
					double diff=(dist.getY(i)-pdf.getInterpolatedY(dist.getX(i)));
					rms += diff*diff;
				}
				if(rms<best_rms) {
					bestMean=mean;
					bestAper=aperiodicity;
					best_rms=rms;
				}
			}
		}
		meanParam.setValue(bestMean);
		aperiodicityParam.setValue(bestAper);
//		System.out.println(this.NAME+" best fit mean and aper: "+mean+"\t"+aperiodicity);
	}
	
	public double getMean() {return mean;}
	
	public double  getAperiodicity() {return aperiodicity;}
	
	
	/**
	 * This computes the probability of an event over the specified duration for the case where the 
	 * date of last event is unknown, but where the historic open interval is applied (which defaults 
	 * to zero if never set).
	 * @return
	 */
	public double getCondProbForUnknownTimeSinceLastEvent() {
		double result=0;
		double sum=0;
		EvenlyDiscretizedFunc condProbFunc = getCondProbFunc();
		int firstIndex = condProbFunc.getClosestXIndex(histOpenInterval);
		for(int i=firstIndex;i<condProbFunc.getNum();i++) {
			double probOfTimeSince = (1-cdf.getY(i))/mean;
			sum+=probOfTimeSince; 
			result+= condProbFunc.getY(i)*probOfTimeSince;
		}
		result /= sum;	// normalize properly
		
		
// TEST OF RESULT
		// this is another way of computing the same thing
		int lastIndex = condProbFunc.getClosestXIndex(histOpenInterval+duration);
		double sumCDF=0;
		for(int i=firstIndex;i<=lastIndex;i++) {
			sumCDF += cdf.getY(i)*cdf.getDelta();
		}
		double result2= (duration-sumCDF)/mean;
//		System.out.println("\ncdf.getDelta()="+cdf.getDelta());
		
		
		double sumCDF2=0;
		int numIndicesForDuration = (int)Math.round(duration/cdf.getDelta());
		for(int i=firstIndex;i<cdf.getNum()-numIndicesForDuration;i++) {
			sumCDF2 += (cdf.getY(i+numIndicesForDuration)-cdf.getY(i))*cdf.getDelta();
		}
		double result3 = sumCDF2/mean;
		

		double poisProb1orMore = 1-Math.exp(-duration/mean);
		double poisProbOf1 = (duration/mean)*Math.exp(-duration/mean);
		System.out.println("\nsum="+(float)sum);
		System.out.println("CondProbForUnknownDateOfLast="+(float)result+
				"\ttestCalc="+(float)result2+" ("+(float)(result/result2)+")"+
				"\ttestCalc2="+(float)result3 +" ("+(float)(result/result3)+")"+
				"\tduration/mean="+(duration/mean)+
				"\tpoisProb1orMore="+(float)poisProb1orMore+
				"\tpoisProbOf1="+(float)poisProbOf1);
// END TEST OF RESULT
		
		
		return result;
	}
	
	
	public double OLDgetCondProbForUnknownDateOfLastEvent() {
		
		
		double result=0;
		double result2=0;
		double sum=0;
		double test=0;
		EvenlyDiscretizedFunc cpFunc = getCondProbFunc();
		EvenlyDiscretizedFunc hazFunc = getHazFunc();
		for(int i=0;i<cpFunc.getNum();i++) {
			sum+=(1-cdf.getY(i))/mean; 
			if(cdf.getX(i)<=duration) test+=cdf.getY(i);
		}
		test /= sum;
		for(int i=0;i<cpFunc.getNum();i++) {
			result+= cpFunc.getY(i)*(1-cdf.getY(i))/(mean*sum);
			result2+= hazFunc.getY(i)*(1-cdf.getY(i))/(mean*sum);
		}
		double poisProb1orMore = 1-Math.exp(-duration/mean);
		double poisProbOf1 = (duration/mean)*Math.exp(-duration/mean);
		double test2 = (duration-test)/mean;
		System.out.println("\ntestSum="+(float)sum);
		System.out.println("CondProbForUnknownDateOfLast="+(float)result+"\tduration/mean="+(duration/mean)+
				"\tpoisProb1orMore="+(float)poisProb1orMore+"\tpoisProbOf1="+(float)poisProbOf1+"\ttest="+(float)test2);
		System.out.println("AveHazFuncForUnknownDateOfLast="+(float)result2+"\t1/mean="+(1.0/mean));
		return result;
	}

	
	/**
	 * Method to set several parameter values
	 * @param mean
	 * @param aperiodicity
	 * @param deltaX
	 * @param numPoints
	 */
	public void setAll(double mean, double aperiodicity, double deltaX, int numPoints) {
		this.mean=mean;
		this.aperiodicity=aperiodicity;
		this.deltaX=deltaX;;
		this.numPoints=numPoints;
		upToDate=false;
	}

	
	/**
	 * Method to set several parameter values
	 * @param mean
	 * @param aperiodicity
	 * @param deltaX
	 * @param numPoints
	 * @param duration
	 */
	public void setAll(double mean, double aperiodicity, double deltaX, int numPoints, double duration) {
		this.mean=mean;
		this.aperiodicity=aperiodicity;
		this.deltaX=deltaX;;
		this.numPoints=numPoints;
		this.duration = duration;
		upToDate=false;
	}
	
	/**
	 * Method to set several parameter values
	 * @param mean
	 * @param aperiodicity
	 * @param deltaX
	 * @param numPoints
	 * @param duration
	 */
	public void setAll(double mean, double aperiodicity, double deltaX, int numPoints, double duration, double histOpenInterval) {
		this.mean=mean;
		this.aperiodicity=aperiodicity;
		this.deltaX=deltaX;;
		this.numPoints=numPoints;
		this.duration = duration;
		this.histOpenInterval = histOpenInterval;
		upToDate=false;
	}

	
	/**
	 * For this case deltaX defaults to 0.001*mean and numPoints is aperiodicity*10/deltaX+1
	 * @param mean
	 * @param aperiodicity
	 */
	public void setAll(double mean, double aperiodicity) {
		this.mean=mean;
		this.aperiodicity=aperiodicity;
		this.deltaX = DELTA_X_DEFAULT*mean;
		this.numPoints = (int)Math.round(aperiodicity*10*mean/deltaX)+1;
		upToDate=false;
	}
	
	
	
	/**
	 * Set the primitive types whenever a parameter changes
	 */
	public void parameterChange(ParameterChangeEvent event) {
		String paramName = event.getParameterName();
		if(paramName.equalsIgnoreCase(MEAN_PARAM_NAME)) this.mean = ((Double) meanParam.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(APERIODICITY_PARAM_NAME)) this.aperiodicity = ((Double) aperiodicityParam.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(DURATION_PARAM_NAME)) this.duration = ((Double) durationParam.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(DELTA_X_PARAM_NAME)) this.deltaX = ((Double) deltaX_Param.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(NUM_POINTS_PARAM_NAME)) this.numPoints = ((Integer) numPointsParam.getValue()).intValue();
		else if(paramName.equalsIgnoreCase(HIST_OPEN_INTERVAL_PARAM_NAME)) this.histOpenInterval = ((Double) histOpenIntParam.getValue()).intValue();
		this.upToDate = false;
	}


}

