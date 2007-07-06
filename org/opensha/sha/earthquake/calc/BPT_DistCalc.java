package org.opensha.sha.earthquake.calc;

import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.param.DoubleParameter;
import org.opensha.param.IntegerParameter;
import org.opensha.param.ParameterList;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;


/**
 * <b>Title:</b> BPT_DistCalc.java <p>
 * <b>Description:</p>.
 <p>
 *
 * @author Edward Field
 * @created    July, 2007
 * @version 1.0
 */

public final class BPT_DistCalc implements ParameterChangeListener {
	
	private EvenlyDiscretizedFunc pdf, cdf;
	private double mean, aperiodicity, deltaX, duration;
	private int numPoints;
	public static final double DELTA_X_DEFAULT = 0.001;
	private boolean upToDate=false;
	private final String NAME = "BPT";
	
	// Parameter names
	private final static String MEAN_PARAM_NAME= "Mean";
	private final static String APERIODICITY_PARAM_NAME = "Aperiodicity";
	private final static String DURATION_PARAM_NAME = "Duration";
	private final static String DELTA_X_PARAM_NAME = "Delta X";
	private final static String NUM_POINTS_PARAM_NAME = "Num Points";
	
	// Parameter Infos
	private final static String MEAN_PARAM_INFO= "Mean";
	private final static String APERIODICITY_PARAM_INFO = "Aperiodicity";
	private final static String DURATION_PARAM_INFO = "Duration";
	private final static String DELTA_X_PARAM_INFO = "Delta X";
	private final static String NUM_POINTS_PARAM_INFO = "Num Points";
	
	// various adjustable params
	private DoubleParameter meanParam, aperiodicityParam, durationParam, deltaX_Param;
	private IntegerParameter numPointsParam;
	
	// adjustable parameter list
	private ParameterList adjustableParams;

	public void setAll(double mean, double aperiodicity, double deltaX, int numPoints) {
		this.mean=mean;
		this.aperiodicity=aperiodicity;
		this.deltaX=deltaX;;
		this.numPoints=numPoints;
		upToDate=false;
	}

	
	/**
	 * 
	 * @param mean
	 * @param aperiodicity
	 * @param timeSinceLast
	 * @param duration
	 * @param deltaX
	 * @param numPoints
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
	 * For this case deltaX defaults to 0.001*mean and numPoints is aperiodicity*10/deltaX+1
	 * @param mean
	 * @param aperiodicity
	 * @param timeSinceLast
	 * @param duration
	 */
	public void setAll(double mean, double aperiodicity) {
		this.mean=mean;
		this.aperiodicity=aperiodicity;
		this.deltaX = DELTA_X_DEFAULT*mean;
		this.numPoints = (int)Math.round(aperiodicity*10*mean/deltaX)+1;
		upToDate=false;
	}
	
	
	/*
	 * This computes the PDF and then the cdf from the pdf using 
	 * Trapezoidal integration. 
	 */
	private void computeDistributions() {
		
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
		upToDate = true;
	}

	
	public EvenlyDiscretizedFunc getCDF() {
		if(!upToDate) computeDistributions();
		return cdf;
	}


	public EvenlyDiscretizedFunc getPDF() {
		if(!upToDate) computeDistributions();
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
		return hazFunc;
	}
	
	/*
	 * This gives a function of the probability of an event occurring between time T
	 * (on the x-axis) and T+duration, conditioned that it has not occurred before T.
	 * THIS NEEDS TO BE TESTED
	 */
	public EvenlyDiscretizedFunc getCondProbFunc() {
		if(duration==0)
			throw new RuntimeException("duration has not been set");
		if(!upToDate) computeDistributions();
		int numPts = numPoints - (int)(duration/deltaX);
		EvenlyDiscretizedFunc condFunc = new EvenlyDiscretizedFunc(0.0, numPts , deltaX);
		for(int i=0;i<condFunc.getNum();i++) {
			condFunc.set(i,getCondProb(condFunc.getX(i), duration));
		}
		return condFunc;
	}
	

	/**
	 * This is a non-static version that is slightly more accurate (due to
	 * interpolation of the cdf function), although it requires instantiation of the class to
	 * access (and stores information internally). The commented out bit of code gives the non 
	 * interpolated result which is exactly the same as what comes from the static version.
	 * @param timeSinceLast
	 * @param duration
	 * @return
	 */
	public double getCondProb(double timeSinceLast, double duration) {
		if(!upToDate) computeDistributions();
		double p1 = cdf.getInterpolatedY(timeSinceLast);
		double p2 = cdf.getInterpolatedY(timeSinceLast+duration);
		return (p2-p1)/(1.0-p1);
		
		// non interpolated alt:
		/*
		int pt1 = (int)Math.round(timeSinceLast/deltaX) + 1;
		int pt2 = (int)Math.round((timeSinceLast+duration)/deltaX) + 1;
		return (cdf.getY(pt2)-cdf.getY(pt1))/(1.0-cdf.getY(pt1));
		*/
	}	


	/**
	 * This computed the conditional probability using Trapezoidal integration (slightly more
	 * accurrate that the WGCEP-2002 code, which this method is modeled after). Although this method 
	 * is static (doesn't require instantiation), it is less efficient than the non-static version 
	 * here (it is also very slightly less accurate because the other interpolates the cdf).  
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
		i1 = Math.round((float)((x/mean)/step))+1;
		i2 = Math.round((float)(((x+duration)/mean)/step))+1;
		
		temp1 = 1/(2.*Math.PI*(aperiodicity*aperiodicity));
		temp2 = 2.*(aperiodicity*aperiodicity)*1;
		t = step*1.;
		for(i=1; i<=i2; i++) {
			pdf = Math.sqrt(temp1/(t*t*t)) * Math.exp(-(t-1)*(t-1) / (temp2*t) );
			cdf += step*(pdf+pdf_last)/2;
			if ( i == i1 ) cBPT1 = cdf;
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
	 * Initialize adjustable parameters
	 *
	 */
	private void initAdjParams() {
	
		/* make double parameters for the following (except the last is an IntParam); all
		   must be greater than zero:
		 Mean
		 Aperiodicity
		 Duration
		 Delta X
		 Num Points
		 
		 Ned will edit the names and info
		 
		 Listen to each of these and set the local/primative accordingly when it changes & set
		 the upToDate = false
		 
		 Add a getAdjParams method so these can be put in a GUI
		 */
		
		meanParam =  new  DoubleParameter(MEAN_PARAM_NAME, Double.MIN_VALUE, Double.MAX_VALUE);
		meanParam.setInfo(MEAN_PARAM_INFO);
		meanParam.addParameterChangeListener(this);
		aperiodicityParam  = new DoubleParameter(APERIODICITY_PARAM_NAME, Double.MIN_VALUE, Double.MAX_VALUE);
		aperiodicityParam.setInfo(APERIODICITY_PARAM_INFO);
		aperiodicityParam.addParameterChangeListener(this);
		durationParam = new  DoubleParameter(DURATION_PARAM_NAME, Double.MIN_VALUE, Double.MAX_VALUE);
		durationParam.setInfo(DURATION_PARAM_INFO);
		durationParam.addParameterChangeListener(this);
		deltaX_Param = new  DoubleParameter(DELTA_X_PARAM_NAME, Double.MIN_VALUE, Double.MAX_VALUE);
		deltaX_Param.setInfo(DELTA_X_PARAM_INFO);
		deltaX_Param.addParameterChangeListener(this);
		numPointsParam = new  IntegerParameter(NUM_POINTS_PARAM_NAME, Integer.MIN_VALUE, Integer.MAX_VALUE);;
		numPointsParam.setInfo(NUM_POINTS_PARAM_INFO);
		numPointsParam.addParameterChangeListener(this);
		
		adjustableParams = new ParameterList();
		adjustableParams.addParameter(meanParam);
		adjustableParams.addParameter(aperiodicityParam);
		adjustableParams.addParameter(durationParam);
		adjustableParams.addParameter(deltaX_Param);
		adjustableParams.addParameter(numPointsParam);
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
	 * Set the primitive types whenever a parameter changes
	 */
	public void parameterChange(ParameterChangeEvent event) {
		String paramName = event.getParameterName();
		if(paramName.equalsIgnoreCase(MEAN_PARAM_NAME)) this.mean = ((Double) meanParam.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(APERIODICITY_PARAM_NAME)) this.aperiodicity = ((Double) aperiodicityParam.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(DURATION_PARAM_NAME)) this.duration = ((Double) durationParam.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(DELTA_X_PARAM_NAME)) this.deltaX = ((Double) deltaX_Param.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(NUM_POINTS_PARAM_NAME)) this.numPoints = ((Integer) deltaX_Param.getValue()).intValue();
		this.upToDate = false;
	}
	
	/**
	 *  Main method for running tests.  
	 *  Test1 compares the static getCondProb(*) method against values obtained directly from the WGCEP-2002 
	 *  code; all are within 0.3%.
	 *  Test2 campares the non static getCondProb(*) method against the static; all are within 0.4%.  The 
	 *  differences is that the non-static is slightly more accurate due to interpolation of the CDF
	 *  (exact same values are obtained otherwise; see commented out code).
	 *  Test3 is the non-static used more efficiently (exact same values as from non-static above); this
	 *  is about a factor of two faster.
	 *  Test4 examines what happens if delta is changed to 0.01 in the non-static method (also about
	 *  a factor of two faster).
	 */
	public static void main(String args[]) {
		
		// test data from WGCEP-2002 code run (single branch for SAF) done by Ned Field
		// in Feb of 2006 (see his "Neds0206TestOutput.txt" file).
		double timeSinceLast = 96;
		double nYr = 30;
		double alph = 0.5;
		double[] rate = {0.00466746464,0.00432087015,0.004199435,0.004199435};
		double[] prob = {0.130127236,0.105091952,0.0964599401,0.0964599401};
		
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
	}
	
	/**
	 * Get the name 
	 * @return
	 */
	public String getName() {
		return this.NAME;
	}
}

