package org.opensha.sha.earthquake.calc.recurInterval;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.IntegerParameter;
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

public final class ExponentialDistCalc extends EqkProbDistCalc implements ParameterChangeListener {
	
	
	
	public ExponentialDistCalc() {
		NAME = "Exponential";
		super.initAdjParams();
		mkAdjParamList();
	}
	
	public void setAll(double mean, double deltaX, int numPoints) {
		this.mean=mean;
		this.deltaX=deltaX;;
		this.numPoints=numPoints;
		upToDate=false;
	}

	
	/**
	 * 
	 * @param mean
	 * @param timeSinceLast
	 * @param duration
	 * @param deltaX
	 * @param numPoints
	 */
	public void setAll(double mean, double deltaX, int numPoints, double duration) {
		this.mean=mean;
		this.deltaX=deltaX;;
		this.numPoints=numPoints;
		this.duration = duration;
		upToDate=false;
	}
	
	/**
	 * For this case deltaX defaults to 0.001*mean and numPoints is aperiodicity*10/deltaX+1
	 * @param mean
	 * @param timeSinceLast
	 * @param duration
	 */
	public void setAll(double mean) {
		this.mean=mean;
		this.deltaX = DELTA_X_DEFAULT*mean;
		this.numPoints = (int)Math.round(aperiodicity*10*mean/deltaX)+1;
		upToDate=false;
	}
	
	
	/*
	 * This computes the PDF and then the cdf from the pdf using 
	 * Trapezoidal integration. 
	 */
	protected void computeDistributions() {
		
		pdf = new EvenlyDiscretizedFunc(0,numPoints,deltaX);
		cdf = new EvenlyDiscretizedFunc(0,numPoints,deltaX);
		
		double rate= 1/mean;
		double t;
		
		for(int i=0; i< pdf.getNum(); i++) { // skip first point because it's NaN
			t = pdf.getX(i);
			pdf.set(i,rate*Math.exp(-t*rate));
			cdf.set(i,1-Math.exp(-t*rate));
		}
		upToDate = true;
	}

	



	/**
	 *
	 */
	private void mkAdjParamList() {
		adjustableParams = new ParameterList();
		adjustableParams.addParameter(meanParam);
		adjustableParams.addParameter(durationParam);
		adjustableParams.addParameter(deltaX_Param);
		adjustableParams.addParameter(numPointsParam);
		
		setAll(DEFAULT_MEAN_PARAM_VAL.doubleValue(), DEFAULT_DELTAX_PARAM_VAL.doubleValue(), DEFAULT_NUMPOINTS_PARAM_VAL.intValue(),
				DEFAULT_DURATION_PARAM_VAL.doubleValue());
	}
	
	
	/**
	 * Set the primitive types whenever a parameter changes
	 */
	public void parameterChange(ParameterChangeEvent event) {
		String paramName = event.getParameterName();
		if(paramName.equalsIgnoreCase(MEAN_PARAM_NAME)) this.mean = ((Double) meanParam.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(DURATION_PARAM_NAME)) this.duration = ((Double) durationParam.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(DELTA_X_PARAM_NAME)) this.deltaX = ((Double) deltaX_Param.getValue()).doubleValue();
		else if(paramName.equalsIgnoreCase(NUM_POINTS_PARAM_NAME)) this.numPoints = ((Integer) numPointsParam.getValue()).intValue();
		this.upToDate = false;
	}
	
	/**
	 *  Main method for running tests.
	 */
	public static void main(String args[]) {
		
	}
}

