package org.opensha.commons.mapping.gmt.elements;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

public class PSXYPolygon extends PSXYElement {
	
	private ArrayList<PSXYPoint2D> points = new ArrayList<PSXYPoint2D>();;
	
	/**
	 * Constructor for a simple line
	 * 
	 * @param point1
	 * @param point2
	 */
	public PSXYPolygon(Point2D point1, Point2D point2) {
		points.add(new PSXYPoint2D(point1));
		points.add(new PSXYPoint2D(point2));
	}
	
	public PSXYPolygon(Location loc1, Location loc2) {
		points.add(new PSXYPoint2D(loc1.getLongitude(), loc1.getLatitude()));
		points.add(new PSXYPoint2D(loc2.getLongitude(), loc2.getLatitude()));
	}
	
	public PSXYPolygon(ArrayList<Point2D> points) {
		for (Point2D point : points) {
			this.points.add(new PSXYPoint2D(point));
		}
	}
	
	public PSXYPolygon(LocationList locs) {
		for (Location loc : locs) {
			points.add(new PSXYPoint2D(loc.getLongitude(), loc.getLatitude()));
		}
	}
	
	public PSXYPolygon() {
		
	}
	
	public ArrayList<PSXYPoint2D> getPoints() {
		return points;
	}
	
	public void addPoint(Point2D point) {
		points.add(new PSXYPoint2D(point));
	}
	
	/**
	 * Returns true if polygon has at least 2 points
	 * @return
	 */
	public boolean isValid() {
		return points != null && points.size() >= 2;
	}
	
	public int size() {
		return points.size();
	}
	
	/**
	 * Because Point2D.Double isn't serializable (doesn't have a no-arg constructor) we need to
	 * store points internally as this object.
	 * @author kevin
	 *
	 */
	private class PSXYPoint2D extends Point2D.Double implements Serializable {
		public PSXYPoint2D() {};
		public PSXYPoint2D(Point2D point) {
			super(point.getX(), point.getY());
		}
		public PSXYPoint2D(double x, double y) {
			super(x, y);
		}
	}
}
