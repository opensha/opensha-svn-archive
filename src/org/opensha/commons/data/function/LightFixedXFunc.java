package org.opensha.commons.data.function;

import java.awt.geom.Point2D;
import java.util.Arrays;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.exceptions.Point2DException;

/**
 * This is a lightweight array based DescretizedFunc instance that doesn't allow
 * changing the set of X values. It uses less memory than other DiscretizedFunc instances.
 * @author kevin
 *
 */
public class LightFixedXFunc extends AbstractDiscretizedFunc {
	
	private double[] xVals, yVals;
	
	public LightFixedXFunc(DiscretizedFunc func) {
		xVals = new double[func.getNum()];
		yVals = new double[xVals.length];
		
		for (int i=0; i<xVals.length; i++) {
			Point2D pt = func.get(i);
			xVals[i] = pt.getX();
			yVals[i] = pt.getY();
		}
	}
	
	/**
	 * Values must be sorted!
	 * @param xVals
	 * @param yVals
	 */
	public LightFixedXFunc(double[] xVals, double[] yVals) {
		this.xVals = xVals;
		this.yVals = yVals;
	}

	@Override
	public double getY(double x) {
		int ind = Arrays.binarySearch(xVals, x);
		return xVals[ind];
	}

	@Override
	public double getFirstInterpolatedX(double y) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private int getXIndexBefore(double x) {
		int ind = Arrays.binarySearch(xVals, x);
		if (ind < 0)
			return -ind-2;
		return ind-1;
	}

	@Override
	public double getInterpolatedY(double x) {
//		throw new UnsupportedOperationException("Not yet implemented");
		// finds the size of the point array
		int max=xVals.length;
		//if passed parameter(x value) is not within range then throw exception
		if(x>getX(max-1) || x<getX(0))
			throw new InvalidRangeException("x Value must be within the range: "+getX(0)+" and "+getX(max-1)
					+" (supplied x: "+x+")");
		//if x value is equal to the maximum value of all given X's then return the corresponding Y value
		if(x==getX(max-1))
			return getY(x);
		//finds the X values within which the the given x value lies
		int x1Ind = getXIndexBefore(x);
		if (x1Ind == -1)
			// this means that it matches at index 0
			return getY(0);
		int x2Ind = x1Ind+1;
		Point2D pt1 = get(x1Ind);
		Point2D pt2 = get(x2Ind);
		double x1 = pt1.getX();
		double y1 = pt1.getY();
		double x2 = pt2.getX();
		double y2 = pt2.getY();
		//using the linear interpolation equation finding the value of y for given x
		double y= ((y2-y1)*(x-x1))/(x2-x1) + y1;
		return y;
	}

	@Override
	public double getInterpolatedY_inLogXLogYDomain(double x) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public double getInterpolatedY_inLogYDomain(double x) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public double getFirstInterpolatedX_inLogXLogYDomain(double y) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int getXIndex(double x) {
		return Arrays.binarySearch(xVals, x);
	}

	@Override
	public int getIndex(Point2D point) {
		return Arrays.binarySearch(xVals, point.getX());
	}

	@Override
	public DiscretizedFunc deepClone() {
		double[] xVals = Arrays.copyOf(this.xVals, this.xVals.length);
		double[] yVals = Arrays.copyOf(this.yVals, this.yVals.length);
		return new LightFixedXFunc(xVals, yVals);
	}

	@Override
	public int getNum() {
		return xVals.length;
	}

	@Override
	public double getMinX() throws IndexOutOfBoundsException {
		return xVals[0];
	}

	@Override
	public double getMaxX() throws IndexOutOfBoundsException {
		return xVals[xVals.length-1];
	}

	@Override
	public double getMinY() throws IndexOutOfBoundsException {
		return StatUtils.min(yVals);
	}

	@Override
	public double getMaxY() throws IndexOutOfBoundsException {
		return StatUtils.max(yVals);
	}

	@Override
	public Point2D get(int index) {
		return new Point2D.Double(xVals[index], yVals[index]);
	}

	@Override
	public double getX(int index) throws IndexOutOfBoundsException {
		return xVals[index];
	}

	@Override
	public double getY(int index) throws IndexOutOfBoundsException {
		return yVals[index];
	}

	@Override
	public void set(Point2D point) throws Point2DException {
		int ind = getIndex(point);
		if (ind < 0)
			throw new UnsupportedOperationException("Can't add new points");
		yVals[ind] = point.getY();
	}

	@Override
	public void set(double x, double y) throws Point2DException {
		int ind = getXIndex(x);
		if (ind < 0)
			throw new UnsupportedOperationException("Can't add new points");
		yVals[ind] = y;
	}

	@Override
	public void set(int index, double Y) throws IndexOutOfBoundsException {
		yVals[index] = Y;
	}

	@Override
	public boolean hasPoint(Point2D point) {
		return getIndex(point) >= 0;
	}

	@Override
	public boolean hasPoint(double x, double y) {
		return hasPoint(new Point2D.Double(x, y));
	}

	@Override
	public String getMetadataString() {
		return null;
	}

}
