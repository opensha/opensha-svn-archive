/**
 * 
 */
package org.opensha.commons.data.function;

import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.sha.earthquake.calc.recurInterval.BPT_DistCalc;

/**
 * This class sets the tolerance high so that it can be used to construct histograms.
 * 
 * It also provides some methods relevant to a histogram.
 * 
 * @author field
 *
 */
public class HistogramFunction extends EvenlyDiscretizedFunc {

	/**
	 * @param min
	 * @param num
	 * @param delta
	 */
	public HistogramFunction(double min, int num, double delta) {
		super(min, num, delta);
		this.setTolerance(getDelta());
	}

	/**
	 * @param min
	 * @param max
	 * @param num
	 */
	public HistogramFunction(double min, double max, int num) {
		super(min, max, num);
		this.setTolerance(getDelta());
	}

	/**
	 * This normalized the function so that the sum of Y valaues is 1.0
	 */
	public void normalizeBySumOfY_Vals() {
		scale(1.0/calcSumOfY_Vals());
	}
	
	/**
	 * This returns the cumulative distribution function (total number less than and equal to each x-axis value)
	 * @return
	 */
	public HistogramFunction getCumulativeDistFunction() {
		HistogramFunction cumHist = new HistogramFunction(getMinX(), getNum(), getDelta());
		double sum=0;
		for(int i=0;i<getNum();i++) {
			sum+=getY(i);
			cumHist.set(i,sum);
		}
		return cumHist;
	}
	
	/**
	 * This returns the x-axis value corresponding to the maximum y-axis value
	 * @return
	 */
	public double getMode() {
		double mode=Double.NaN, max=-1;;
		for(int i=0;i<getNum();i++) {
			if(getY(i)>max) {
				max = getY(i);
				mode = getX(i);
			}
		}
		return mode;
	}
	
	public double computeMean() {
		double sum = calcSumOfY_Vals();
		double mean = 0;
		for(int i=0;i<getNum();i++) {
			mean+=getX(i)*getY(i)/sum;
		}
		return mean;
	}
	
	
	public double computeStdDev() {
		double sum = calcSumOfY_Vals();
		double mean = computeMean();
		double var = 0;
		for(int i=0;i<getNum();i++) {
			var+=(getX(i)-mean)*(getX(i)-mean)*getY(i)/sum;
		}
		return Math.sqrt(var);
	}
	
	public double computeCOV() {
		return computeStdDev()/computeMean();
	}
	
	
	// test of compute methods
	public static void main(String[] args) {
		BPT_DistCalc bpt_calc = new BPT_DistCalc();
		bpt_calc.setAll(110, 0.25, 1, 600);
		EvenlyDiscretizedFunc func = bpt_calc.getPDF();
		GraphWindow graph = new GraphWindow(func, "Test BPT"); 
		HistogramFunction hist = new HistogramFunction(func.getMinX(),func.getMaxX(), func.getNum());
		for(int i=0;i<hist.getNum();i++)
			hist.set(i, func.getY(i));
		System.out.println("mean="+hist.computeMean());
		System.out.println("std="+hist.computeStdDev());
		System.out.println("cov="+hist.computeCOV());

	}

}
