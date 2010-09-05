package org.opensha.commons.data.function;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.dom4j.Element;
import org.opensha.commons.exceptions.Point2DException;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;

public class XY_DataSet extends AbstractXY_DataSet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Point2D> points;
	private MinMaxAveTracker xTracker;
	private MinMaxAveTracker yTracker;
	
	public XY_DataSet() {
		points = new ArrayList<Point2D>();
		resetTrackers();
	}
	
	private void resetTrackers() {
		xTracker = new MinMaxAveTracker();
		yTracker = new MinMaxAveTracker();
		for (Point2D pt : points) {
			xTracker.addValue(pt.getX());
			yTracker.addValue(pt.getY());
		}
	}

	@Override
	public XY_DataSetAPI deepClone() {
		XY_DataSet xy = new XY_DataSet();
		for (Point2D pt : points) {
			xy.set(pt);
		}
		xy.setName(getName());
		xy.setInfo(getInfo());
		xy.setXAxisName(getXAxisName());
		xy.setYAxisName(getYAxisName());
		return xy;
	}

	@Override
	public boolean equals(XY_DataSetAPI function) {
		if( !getName().equals(function.getName() )  ) return false;

		if( !getInfo().equals(function.getInfo() )  ) return false;
		return true;
	}

	@Override
	public Point2D get(int index) {
		return points.get(index);
	}

	@Override
	public double getMaxX() {
		return xTracker.getMax();
	}

	@Override
	public double getMaxY() {
		return yTracker.getMax();
	}

	@Override
	public String getMetadataString() {
		String meta = ClassUtils.getClassNameWithoutPackage(this.getClass()) + "\n";
		meta += "name: " + getName() + "\n";
		meta += "info: " + getInfo() + "\n";
		meta += "values:" + "\n";
		for (Point2D pt : points) {
			meta += "\t"+(float)pt.getX() + ", " + (float)pt.getY();
		}
		return null;
	}

	@Override
	public double getMinX() {
		return xTracker.getMin();
	}

	@Override
	public double getMinY() {
		return yTracker.getMin();
	}

	@Override
	public int getNum() {
		return points.size();
	}

	@Override
	public Iterator<Point2D> getPointsIterator() {
		return points.iterator();
	}

	@Override
	public double getX(int index) {
		return get(index).getX();
	}

	@Override
	public double getY(int index) {
		return get(index).getY();
	}

	@Override
	public boolean hasPoint(Point2D point) {
		return points.contains(point);
	}

	@Override
	public boolean hasPoint(double x, double y) {
		return hasPoint(new Point2D.Double(x, y));
	}

	@Override
	public void set(Point2D point) throws Point2DException {
		points.add(point);
	}

	@Override
	public void set(double x, double y) throws Point2DException {
		set(new Point2D.Double(x, y));
	}

	@Override
	public void set(int index, double y) {
		Point2D point = get(index);
		if (point != null)
			point.setLocation(point.getX(), y);
		else
			throw new IndexOutOfBoundsException();
	}

	@Override
	public Element toXMLMetadata(Element root) {
		throw new RuntimeException("not supported");
	}

}
