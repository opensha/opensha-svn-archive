package org.opensha.sha.earthquake.recurInterval;

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

public final class LognormalDistCalc extends EqkProbDistCalc implements ParameterChangeListener {
	 
	
	
	public LognormalDistCalc() {
		NAME = "Lognormal";
		super.initAdjParams();
		mkAdjParamList();
	}
	
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
	protected void computeDistributions() {
		
		pdf = new EvenlyDiscretizedFunc(0,numPoints,deltaX);
		cdf = new EvenlyDiscretizedFunc(0,numPoints,deltaX);
		// set first y-values to zero
		pdf.set(0,0);
		cdf.set(0,0);
		
		// convert mean and aperiodicity to mu and sigma
		double sigma = Math.sqrt(Math.log(aperiodicity*aperiodicity+1));
		double mu = Math.log(mean/Math.exp(sigma*sigma/2));
		
		double temp1 = sigma*Math.sqrt(2.0*Math.PI);
		double temp2 = 2.0*sigma*sigma;
		double t,pd,cd=0;
		for(int i=1; i< pdf.getNum(); i++) { // skip first point because it's NaN
			t = cdf.getX(i);
			pd = Math.exp(-(Math.log(t)-mu)*(Math.log(t)-mu)/temp2)/(temp1*t);
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

	



	/**
	 *
	 */
	private void mkAdjParamList() {
		adjustableParams = new ParameterList();
		adjustableParams.addParameter(meanParam);
		adjustableParams.addParameter(aperiodicityParam);
		adjustableParams.addParameter(durationParam);
		adjustableParams.addParameter(deltaX_Param);
		adjustableParams.addParameter(numPointsParam);
		
		setAll(DEFAULT_MEAN_PARAM_VAL.doubleValue(), DEFAULT_APERIODICITY_PARAM_VAL.doubleValue(),
				DEFAULT_DELTAX_PARAM_VAL.doubleValue(), DEFAULT_NUMPOINTS_PARAM_VAL.intValue(),
				DEFAULT_DURATION_PARAM_VAL.doubleValue());
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
		this.upToDate = false;
	}
	
	/**
	 *  Main method for running tests.
	 */
	public static void main(String args[]) {
		
	}
}

