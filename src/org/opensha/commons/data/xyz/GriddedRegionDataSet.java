package org.opensha.commons.data.xyz;

import java.awt.geom.Point2D;
import java.util.HashMap;

import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;

/**
 * This is a Geohgraphic Dataset on a regular grid, as defined by a GriddedRegion. Points
 * not in the given GriddedRegion cannot be set.
 * 
 * @author kevin
 *
 */
public class GriddedRegionDataSet implements GeographicDataSetAPI {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GriddedRegion region;
	private LocationList nodeList;
	private HashMap<Location, Double> map;
	
	private boolean latitudeX;
	
	public GriddedRegionDataSet(GriddedRegion region, boolean latitudeX) {
		this.region = region;
		nodeList = region.getNodeList();
		this.latitudeX = latitudeX;
		map = new HashMap<Location, Double>();
	}

	@Override
	public double getMinX() {
		if (latitudeX)
			return region.getMinGridLat();
		else
			return region.getMinGridLon();
	}

	@Override
	public double getMaxX() {
		if (latitudeX)
			return region.getMaxGridLat();
		else
			return region.getMaxGridLon();
	}

	@Override
	public double getMinY() {
		if (latitudeX)
			return region.getMinGridLon();
		else
			return region.getMinGridLat();
	}

	@Override
	public double getMaxY() {
		if (latitudeX)
			return region.getMaxGridLon();
		else
			return region.getMaxGridLat();
	}
	
	private MinMaxAveTracker getZTracker() {
		MinMaxAveTracker tracker = new MinMaxAveTracker();
		
		for (double val : map.values()) {
			tracker.addValue(val);
		}
		
		return tracker;
	}

	@Override
	public double getMinZ() {
		return getZTracker().getMin();
	}

	@Override
	public double getMaxZ() {
		return getZTracker().getMax();
	}
	
	private Location ptToLoc(Point2D point) {
		if (latitudeX)
			return new Location(point.getX(), point.getY());
		else
			return new Location(point.getY(), point.getX());
	}

	private Point2D locToPoint(Location loc) {
		if (latitudeX)
			return new Point2D.Double(loc.getLatitude(), loc.getLongitude());
		else
			return new Point2D.Double(loc.getLongitude(), loc.getLatitude());
	}

	@Override
	public void set(Point2D point, double z) {
		set(ptToLoc(point), z);
	}

	@Override
	public void set(double x, double y, double z) {
		set(new Point2D.Double(x, y), z);
	}

	@Override
	public void set(int index, double z) {
		set(getLocation(index), z);
	}

	@Override
	public double get(Point2D point) {
		return get(ptToLoc(point));
	}

	@Override
	public double get(double x, double y) {
		return get(new Point2D.Double(x, y));
	}

	@Override
	public double get(int index) {
		return get(getLocation(index));
	}

	@Override
	public Point2D getPoint(int index) {
		return locToPoint(getLocation(index));
	}

	@Override
	public int indexOf(Point2D point) {
		return indexOf(ptToLoc(point));
	}

	@Override
	public boolean contains(Point2D point) {
		return contains(ptToLoc(point));
	}

	@Override
	public boolean contains(double x, double y) {
		return contains(new Point2D.Double(x, y));
	}

	@Override
	public int size() {
		return region.getNodeCount();
	}

	@Override
	public void setAll(XYZ_DataSetAPI dataset) {
		for (int i=0; i<dataset.size(); i++) {
			set(dataset.getPoint(i), dataset.get(i));
		}
	}

	@Override
	public boolean isLatitudeX() {
		return latitudeX;
	}

	@Override
	public void set(Location loc, double value) {
		if (!contains(loc))
			throw new InvalidRangeException("point must be within range");
		map.put(loc, value);
	}

	@Override
	public double get(Location loc) {
		return map.get(loc);
	}

	@Override
	public int indexOf(Location loc) {
		return nodeList.indexOf(loc);
	}

	@Override
	public Location getLocation(int index) {
		return nodeList.get(index);
	}

	@Override
	public boolean contains(Location loc) {
		return nodeList.contains(loc);
	}

	@Override
	public Object clone() {
		GriddedRegionDataSet data = new GriddedRegionDataSet(region, latitudeX);
		
		for (int i=0; i<size(); i++) {
			data.set(getLocation(i), get(i));
		}
		
		return data;
	}

	@Override
	public int indexOf(double x, double y) {
		return indexOf(new Point2D.Double(x, y));
	}

	@Override
	public void setLatitudeX(boolean latitudeX) {
		this.latitudeX = latitudeX;
	}

}
