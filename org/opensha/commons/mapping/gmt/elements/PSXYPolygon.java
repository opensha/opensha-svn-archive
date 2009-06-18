package org.opensha.commons.mapping.gmt.elements;

import java.io.Serializable;
import java.util.ArrayList;

import org.opensha.commons.data.DataPoint2D;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

public class PSXYPolygon extends PSXYElement {
	
	private ArrayList<DataPoint2D> points = new ArrayList<DataPoint2D>();;
	
	/**
	 * Constructor for a simple line
	 * 
	 * @param point1
	 * @param point2
	 */
	public PSXYPolygon(DataPoint2D point1, DataPoint2D point2) {
		points.add(point1);
		points.add(point2);
	}
	
	public PSXYPolygon(Location loc1, Location loc2) {
		points.add(new DataPoint2D(loc1.getLongitude(), loc1.getLatitude()));
		points.add(new DataPoint2D(loc2.getLongitude(), loc2.getLatitude()));
	}
	
	public PSXYPolygon(ArrayList<DataPoint2D> points) {
		this.points = points;
	}
	
	public PSXYPolygon(LocationList locs) {
		for (Location loc : locs) {
			points.add(new DataPoint2D(loc.getLongitude(), loc.getLatitude()));
		}
	}
	
	public PSXYPolygon() {
		
	}
	
	public ArrayList<DataPoint2D> getPoints() {
		return points;
	}
	
	public void addPoint(DataPoint2D point) {
		points.add(point);
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
}
