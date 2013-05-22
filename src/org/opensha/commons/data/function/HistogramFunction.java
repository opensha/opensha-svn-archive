/**
 * 
 */
package org.opensha.commons.data.function;

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
}
