package org.opensha.calc;

//The following are needed only for the tests
import java.text.DecimalFormat;
import org.opensha.data.function.EvenlyDiscretizedFunc;


/**
 * <b>Title:</b> BPT_DistCalc.java <p>
 * <b>Description:</p>.
 <p>
 *
 * @author Edward Field
 * @created    July, 2007
 * @version 1.0
 */

public final class BPT_DistCalc {
	
	EvenlyDiscretizedFunc pdf, cdf, hazFunc;
	double alpha;
	public final double DELTA_DEFAULT = 0.001;
	
	
	/*
	 * delta is the discretization of the x-axis
	 */
	public BPT_DistCalc(double alpha, double delta){
		this.alpha = alpha;
		makeFunctions(delta);
	}
	
	/*
	 * This applies the default delta
	 */
	public BPT_DistCalc(double alpha){
		this.alpha = alpha;
		makeFunctions(DELTA_DEFAULT);
	}
	
	/*
	 * no arg constructor
	 */
	public BPT_DistCalc(){
		this.alpha = Double.NaN;
	}
	
	
	public void setAlpha(double alpha) {
		this.alpha = alpha;
		makeFunctions(DELTA_DEFAULT);
	}

	/*
	 * delta is the discretization of the x-axis
	 */
	public void setAlphaAndDelta(double alpha, double delta) {
		this.alpha = alpha;
		makeFunctions(delta);
	}

	/*
	 * delta is the discretization of the x-axis
	 */
	public void setDelta(double delta) {
		makeFunctions(delta);
	}
	
	public double getAlpha() {return alpha;}
	
	/*
	 * The discretization of the x-axis
	 */
	public double getDelta() {return cdf.getDelta();}

	/**
	 * This makes pdf and cdf functions for mu = 1.0 & the current alpha value.
	 * The max x-axis value is alpha*10
	 *
	 */
	private void makeFunctions(double delta) {
		
		// make sure alpha has been set
		if(Double.isNaN(alpha))
			throw new RuntimeException("Error in BPT_DistCalc: alpha is NaN");
		
		int num = Math.round((float)(alpha*10/delta));
		pdf = new EvenlyDiscretizedFunc( (delta/2),  num,  delta);
		cdf = new EvenlyDiscretizedFunc( 0,  num+1,  delta);
		
		double temp1 = 1.0/(2.*Math.PI*(alpha*alpha));
		double temp2 = 2.*(alpha*alpha);
		double t,pd,cd=0;
		for(int i=1; i< pdf.getNum(); i++) { // skip first point because it's NaN
			t = cdf.getX(i);
			pd = Math.sqrt(temp1/(t*t*t)) * Math.exp(-(t-1)*(t-1)/(temp2*t));
			cd += pd*delta;
			pdf.set(i,pd);
			cdf.set(i+1,cd);
		}
		
		// these are for the test of the getCDF and getPDF methods (in the main method) 
		/*
		System.out.println("pdf.getMinX="+pdf.getMinX()+"; pdf.getMaxX="+pdf.getMaxX());
		System.out.println("pdf.getY(1000)="+pdf.getY(1000));
		System.out.println("cdf.getMinX="+cdf.getMinX()+"; cdf.getMaxX="+cdf.getMaxX());
		System.out.println("cdf.getY(1000)="+cdf.getY(1000));
		*/

		//System.out.println("maxCDF="+cdf.getY(cdf.getNum()-1));
		//System.out.println("num="+cdf.getNum());
		
//		for(int i=0;i<cdf.getNum();i+=50)
//		System.out.println(pdf.getX(i)+"\t"+pdf.getY(i));
	}
	
	
	/*
	 * This gets the CDF for the alpha (and delta) already set
	 */
	public EvenlyDiscretizedFunc getCDF(double rate) {
		EvenlyDiscretizedFunc newCDF = new EvenlyDiscretizedFunc(cdf.getMinX()/rate, cdf.getMaxX()/rate, cdf.getNum());
		for(int i=0;i<newCDF.getNum();i++) newCDF.set(i,cdf.getY(i));
		// Add Info ?????
		return newCDF;
	}

	/*
	 * This gets the PDF for the alpha (and delta) already set
	 */
	public EvenlyDiscretizedFunc getPDF(double rate) {
		EvenlyDiscretizedFunc newPDF = new EvenlyDiscretizedFunc(pdf.getMinX()/rate, pdf.getMaxX()/rate, pdf.getNum());
		for(int i=0;i<newPDF.getNum();i++) newPDF.set(i,pdf.getY(i));
		// Add Info ?????
		return newPDF;
	}

	
	/*
	 * This gets the hazard function for the alpha (and delta) already set.  THIS NEEDS VERIFICATION
	 */
	public EvenlyDiscretizedFunc getHazFunc(double rate) {
		EvenlyDiscretizedFunc hazFunc = new EvenlyDiscretizedFunc(pdf.getMinX()/rate, pdf.getMaxX()/rate, pdf.getNum());
		double haz;
		for(int i=0;i<hazFunc.getNum();i++) {
			haz = pdf.getY(i)/(1.0-cdf.getInterpolatedY(pdf.getX(i)));
			hazFunc.set(i,haz);
		}
		// Add Info ?????
		return hazFunc;
	}
	
	/*
	 * This gives the probability of an event occurring between time T
	 * (on the x-axis) and T+duration, conditioned that it has not occurred before T
	 * (for the set alpha and delta).  THIS NEEDS TO BE TESTED
	 */
	public EvenlyDiscretizedFunc getCondProbFunc(double rate, double duration) {
		double delta = cdf.getDelta()/rate;
		int num = (int)((cdf.getMaxX()/rate - duration)/delta) - 1;
		EvenlyDiscretizedFunc condFunc = new EvenlyDiscretizedFunc(0.0, num , delta);
		for(int i=0;i<condFunc.getNum();i++) {
			condFunc.set(i,getCondProb(condFunc.getX(i), rate, duration));
		}
		// Add Info ?????
		return condFunc;
	}
	

	/**
	 * This computed the conditional probability exactly as done in the WGCEP (2002) code.  
	 * Although this method is static (doesn't require instantiation), it is less efficient
	 * when performing numerous calculations (?).
	 * @param lapseTime - time since last event
	 * @param rate - average rate of events
	 * @param alpha - coefficient of variation (technically corrrect??)
	 * @param duration - forecast duration
	 * @return
	 */
	public static double getCondProb(double lapseTime, double rate,double alpha, double duration) {
		
		double mu1 = 1.0;
		double step = 0.001;
		double cdf, pdf, t, temp1, temp2, x, cBPT1, cBPT2;
		int i, i1, i2;
		double mu = 1/rate;
		
		// avoid numerical problems when too far out on tails
		if ( lapseTime*rate > alpha*10 )
			x = 10.*alpha/rate;
		else
			x = lapseTime;
		
		// find index of the two points in time
		i1 = Math.round((float)((x/mu)/step));
		i2 = Math.round((float)(((x+duration)/mu)/step));
		
		temp1 = mu1/(2.*Math.PI*(alpha*alpha));
		temp2 = 2.*(alpha*alpha)*mu1;
		t = step*3.;
		cdf = 0;
		cBPT1 = 0;
		for(i=3; i<=i2; i++) {
			pdf = Math.sqrt(temp1/(t*t*t)) * Math.exp(-(t-mu1)*(t-mu1) / (temp2*t) );
			cdf = cdf + pdf*step;
			if ( i == i1 ) cBPT1 = cdf;
			t += step;
		}
		cBPT2 = cdf;
		
		if ( cBPT1 >= 1.0 )
			return Double.NaN;
		else
			return (cBPT2-cBPT1)/( 1.-cBPT1);
		
	}
	
	
	public double getCondProb(double lapseTime, double rate, double duration) {
		double t1 = lapseTime*rate;
		double t2 = (lapseTime+duration)*rate;
		double p1 = cdf.getInterpolatedY(t1);
		if(p1 >= 1.0) return Double.NaN;
		
		double p2 = cdf.getInterpolatedY(t2);
		return (p2-p1)/(1.0-p1);
	}
	
	/**
	 *  Main method for running tests.  
	 *  Test1 compares the static getCondProb(*) method against values from the WGCEP-2002 
	 *  code; all are within 0.5%.
	 *  Test2 campares the other (non static) getCondProb(*)  method against values from the WGCEP-2002 
	 *  code; all are within 0.4%.  The systematic bias is due to what I believe is improved
	 *  bin centering in this version.
	 *  The static method takes about 2 times longer.
	 *  Test3 examines what happens if delta is changed to 0.01 (discrepancies are now up to 2.5%),
	 *  although it is faster by a factor of 8.
	 */
	public static void main(String args[]) {
		
		// test data from WGCEP-2002 code run (single branch for SAF) done by Ned Field
		// in Feb of 2006 (see his "Neds0206TestOutput.txt" file).
		double elapseTime = 96;
		double nYr = 30;
		double alph = 0.5;
		double[] rate = {0.00466746464,0.00432087015,0.004199435,0.004199435};
		double[] prob = {0.130127236,0.105091952,0.0964599401,0.0964599401};
		
		// Test1 (static method based on WGCEP-2002 code)
		double p;
		for(int i=0;i<rate.length;i++) {
			p = getCondProb(elapseTime,rate[i],alph,nYr);
			System.out.println("Test1 (static): ="+(float)p+"; ratio="+(float)(p/prob[i]));
		}
		
		// Test2 (faster method based on pre-computed & saved function)
		BPT_DistCalc calc = new BPT_DistCalc(0.5);
		for(int i=0;i<rate.length;i++) {
			p = calc.getCondProb(elapseTime,rate[i],nYr);
			System.out.println("Test2 (other): ="+(float)p+"; ratio="+(float)(p/prob[i]));
		}
		
		// Speed tests
		// First the static method based on WGCEP-2002 code
		long milSec0 = System.currentTimeMillis();
		int numCalcs = 10000;
		for(int i=0; i< numCalcs; i++)
			p = getCondProb(elapseTime,rate[0],alph,nYr);
		double time = (double)(System.currentTimeMillis()-milSec0)/1000;
		System.out.println("Speed Test for static = "+(float)time+" sec");
		// now the faster method based on pre-computed & saved function
		milSec0 = System.currentTimeMillis();
		for(int i=0; i< numCalcs; i++)
			p = calc.getCondProb(elapseTime,rate[0],nYr);
		double time2 = (double)(System.currentTimeMillis()-milSec0)/1000;
		System.out.println("Speed Test for other = "+(float)time2+" sec");
		System.out.println("Ratio of static to other = "+(float)(time/time2));
		
		
		// test the delta=0.01 case
		calc.setDelta(0.01);
		for(int i=0;i<rate.length;i++) {
			p = calc.getCondProb(elapseTime,rate[i],nYr);
			System.out.println("Test3 (delta=0.01): ="+(float)p+"; ratio="+(float)(p/prob[i]));
		}
		// Speed tests
		milSec0 = System.currentTimeMillis();
		for(int i=0; i< numCalcs; i++)
			p = calc.getCondProb(elapseTime,rate[0],nYr);
		double time3 = (double)(System.currentTimeMillis()-milSec0)/1000;
		System.out.println("Speed Test for 0.01 delta (non static) = "+(float)time3+" sec");
		System.out.println("Ratio of compute time for default delta vs 0.01 delta  = "+(float)(time2/time3));


		
		// test the returned discretized functions
		// (need to un-comment println statements in makeFunctions method for the test comparison
		/*
		calc.setDelta(calc.DELTA_DEFAULT);
		
		EvenlyDiscretizedFunc func = calc.getPDF(0.01);
		System.out.println("PDF: func.getMinX="+func.getMinX()+"; func.getMaxX="+func.getMaxX());
		System.out.println("PDF: func.getY(1000)="+func.getY(1000));

		func = calc.getCDF(0.01);
		System.out.println("CDF: func.getMinX="+func.getMinX()+"; func.getMaxX="+func.getMaxX());
		System.out.println("CDF: func.getY(1000)="+func.getY(1000));
		
		EvenlyDiscretizedFunc func = calc.getCondProbFunc(0.01, 30);
		EvenlyDiscretizedFunc func = calc.getHazFunc(0.01);

		*/
	}
}

