package org.opensha.sha.earthquake.calc;

import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.param.DoubleParameter;
import org.opensha.param.IntegerParameter;
import org.opensha.param.ParameterList;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;


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
	protected double mean, aperiodicity, deltaX, duration;
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
	
	// Parameter Infos
	protected final static String MEAN_PARAM_INFO= "Mean";
	protected final static String APERIODICITY_PARAM_INFO = "Aperiodicity";
	protected final static String DURATION_PARAM_INFO = "Duration";
	protected final static String DELTA_X_PARAM_INFO = "Delta X";
	protected final static String NUM_POINTS_PARAM_INFO = "Num Points";
	
	// default param values
	protected final static Double DEFAULT_MEAN_PARAM_VAL = new Double(100);
	protected final static Double DEFAULT_APERIODICITY_PARAM_VAL = new Double(0.5);
	protected final static Double DEFAULT_DURATION_PARAM_VAL = new Double(30);
	protected final static Double DEFAULT_DELTAX_PARAM_VAL = new Double(1);
	protected final static Integer DEFAULT_NUMPOINTS_PARAM_VAL = new Integer(500);
	
	// various adjustable params
	protected DoubleParameter meanParam, aperiodicityParam, durationParam, deltaX_Param;
	protected IntegerParameter numPointsParam;
	
	// adjustable parameter list
	protected ParameterList adjustableParams;

	/*
	 * 
	 */
	abstract void computeDistributions();
	
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
}

