package org.opensha.commons.data;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.TreeSet;

import org.opensha.commons.util.DataUtils.MinMaxAveTracker;

public class Point2DToleranceSortedTreeSet extends TreeSet<Point2D> implements Point2DToleranceSortedList {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Point2DToleranceSortedTreeSet(Point2DComparator comparator) {
		super(comparator);
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#getTolerance()
	 */
	@Override
	public double getTolerance() {
		return ((Point2DComparator)comparator()).getTolerance();
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#setTolerance(double)
	 */
	@Override
	public void setTolerance(double newTolerance) {
		((Point2DComparator)comparator()).setTolerance(newTolerance);
	}

	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#add(java.awt.geom.Point2D)
	 */
	@Override
	public boolean add(Point2D e) {
		if (super.contains(e))
			super.remove(e);
		return super.add(e);
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#getMinY()
	 */
	@Override
	public double getMinY() {
		if (isEmpty())
			return Double.NaN;
		double minY = Double.POSITIVE_INFINITY;
		for (Point2D p : this)
			if (p.getY() < minY)
				minY = p.getY();
		return minY;
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#getMaxY()
	 */
	@Override
	public double getMaxY() {
		if (isEmpty())
			return Double.NaN;
		double maxY = Double.NEGATIVE_INFINITY;
		for (Point2D p : this)
			if (p.getY() > maxY)
				maxY = p.getY();
		return maxY;
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#getMinX()
	 */
	@Override
	public double getMinX() {
		return this.first().getX();
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#getMaxX()
	 */
	@Override
	public double getMaxX() {
		return this.last().getX();
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#get(int)
	 */
	@Override
	public Point2D get(int index) {
		if (index > size())
			throw new IndexOutOfBoundsException();
		int cnt = 0;
		for (Point2D p : this) {
			if (cnt == index)
				return p;
			cnt++;
		}
		return null; // unreachable
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#remove(int)
	 */
	@Override
	public boolean remove(int index) {
		if (index > size())
			throw new IndexOutOfBoundsException();
		int cnt = 0;
		for (Point2D p : this) {
			if (cnt == index)
				return remove(p);
			cnt++;
		}
		return false; // unreachable
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#get(double)
	 */
	@Override
	public Point2D get(double x) {
		Point2D findPoint = new Point2D.Double(x,0.0);
		for (Point2D p : this) {
			if(comparator().compare(p, findPoint) == 0)
				return p;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.commons.data.Point2DToleranceSortedList#getIndex(java.awt.geom.Point2D)
	 */
	@Override
	public int indexOf(Point2D findPoint) {
		int cnt = 0;
		for (Point2D p : this) {
			if(comparator().compare(p, findPoint) == 0)
				return cnt;
			cnt++;
		}
		return -1;
	}

	@Override
	public Point2DComparator getComparator() {
		return (Point2DComparator)comparator();
	}

}
