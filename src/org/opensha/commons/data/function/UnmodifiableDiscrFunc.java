package org.opensha.commons.data.function;

import java.awt.geom.Point2D;

import org.opensha.commons.exceptions.Point2DException;
import org.opensha.commons.util.ClassUtils;

public class UnmodifiableDiscrFunc extends AbstractDiscretizedFunc {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DiscretizedFunc func;
	
	public UnmodifiableDiscrFunc(DiscretizedFunc func) {
		this.func = func.deepClone();
	}

	@Override
	public double getY(double x) {
		return func.getY(x);
	}

	@Override
	public double getFirstInterpolatedX(double y) {
		return func.getFirstInterpolatedX(y);
	}

	@Override
	public double getInterpolatedY(double x) {
		return func.getInterpolatedY(x);
	}

	@Override
	public double getInterpolatedY_inLogXLogYDomain(double x) {
		return func.getInterpolatedY_inLogXLogYDomain(x);
	}

	@Override
	public double getInterpolatedY_inLogYDomain(double x) {
		return func.getInterpolatedY_inLogYDomain(x);
	}

	@Override
	public double getFirstInterpolatedX_inLogXLogYDomain(double y) {
		return func.getFirstInterpolatedX_inLogXLogYDomain(y);
	}

	@Override
	public int getXIndex(double x) {
		return func.getXIndex(x);
	}

	@Override
	public int getIndex(Point2D point) {
		return func.getIndex(point);
	}

	@Override
	public DiscretizedFunc deepClone() {
		return func.deepClone();
	}

	@Override
	public int size() {
		return func.size();
	}

	@Override
	public double getMinX() throws IndexOutOfBoundsException {
		return func.getMinX();
	}

	@Override
	public double getMaxX() throws IndexOutOfBoundsException {
		return func.getMaxX();
	}

	@Override
	public double getMinY() throws IndexOutOfBoundsException {
		return func.getMinY();
	}

	@Override
	public double getMaxY() throws IndexOutOfBoundsException {
		return func.getMaxY();
	}

	@Override
	public Point2D get(int index) {
		return func.get(index);
	}

	@Override
	public double getX(int index) throws IndexOutOfBoundsException {
		return func.getX(index);
	}

	@Override
	public double getY(int index) throws IndexOutOfBoundsException {
		return func.getY(index);
	}

	@Override
	public void set(Point2D point) throws Point2DException {
		setFail();
	}

	@Override
	public void set(double x, double y) throws Point2DException {
		setFail();
	}

	@Override
	public void set(int index, double Y) throws IndexOutOfBoundsException {
		setFail();
	}
	
	private void setFail() {
		throw new UnsupportedOperationException("cannot modify an "+ClassUtils.getClassNameWithoutPackage(getClass()));
	}

	@Override
	public String getMetadataString() {
		return func.getMetadataString();
	}

	@Override
	public String toString() {
		return func.toString();
	}

}
