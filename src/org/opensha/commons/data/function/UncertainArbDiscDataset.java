package org.opensha.commons.data.function;

import org.jfree.data.Range;

import com.google.common.base.Preconditions;

/**
 * Represents an uncertain discretized function which has both an upper and lower bound. Can be used in
 * conjunction with PlotLineType.SHADED_UNCERTAIN to show shaded uncertainty bounds, or plotted normally.
 * 
 * @author kevin
 *
 */
public class UncertainArbDiscDataset extends UnmodifiableDiscrFunc {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private UnmodifiableDiscrFunc lowerFunc;
	private UnmodifiableDiscrFunc upperFunc;
	
	public UncertainArbDiscDataset(DiscretizedFunc meanFunc, DiscretizedFunc lowerFunc, DiscretizedFunc upperFunc) {
		super(meanFunc);
		this.lowerFunc = new UnmodifiableDiscrFunc(lowerFunc);
		this.upperFunc = new UnmodifiableDiscrFunc(upperFunc);
		
		Preconditions.checkArgument(meanFunc.getNum() == lowerFunc.getNum(), "Lower func not same length as mean");
		Preconditions.checkArgument(meanFunc.getNum() == upperFunc.getNum(), "Upper func not same length as mean");
		
		for (int i=0; i<getNum(); i++) {
			double x = meanFunc.getX(i);
			double y = meanFunc.getY(i);
			
			Preconditions.checkArgument((float)x == lowerFunc.getX(i), "X inconsistent in lower func");
			Preconditions.checkArgument((float)x == upperFunc.getX(i), "X inconsistent in lower func");
			Preconditions.checkArgument((float)y >= lowerFunc.getY(i), "Lower func must be <= mean func");
			Preconditions.checkArgument((float)y <= upperFunc.getY(i), "Upper func must be >= mean func");
		}
	}
	
	public Range getYRange(int index) {
		return new Range(lowerFunc.getY(index), upperFunc.getY(index));
	}
	
	public Range getYRange(double x) {
		return getYRange(getXIndex(x));
	}
	
	public double getUpperY(int index) {
		return upperFunc.getY(index);
	}
	
	public double getUpperY(double x) {
		return getUpperY(getXIndex(x));
	}
	
	public double getLowerY(int index) {
		return lowerFunc.getY(index);
	}

	public double getLowerY(double x) {
		return getLowerY(getXIndex(x));
	}
	
}
