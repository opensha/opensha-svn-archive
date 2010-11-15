package org.opensha.commons.data.xyz;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;

/**
 * This class represents an arbitrarily discretized geographic dataset. It is backed by Locations
 * (in a HashMap). This should be used for scattered XYZ data or maps where it is impractical or
 * unnecessary to use the evenly discretized version.
 * 
 * @author kevin
 *
 */
public class ArbDiscrGeographicDataSet implements GeographicDataSetAPI {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean latitudeX;
	
	// we need a separate list of points (vs just using map for everything) to ensure order
	private LocationList points;
	// mapping of poitns to values
	private HashMap<Location, Double> map;
	
	public ArbDiscrGeographicDataSet(boolean latitudeX) {
		this.latitudeX = latitudeX;
		points = new LocationList();
		map = new HashMap<Location, Double>();
	}

	@Override
	public boolean isLatitudeX() {
		return latitudeX;
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
	public void set(Location loc, double value) {
		if (!points.contains(loc))
			points.add(loc);
		map.put(loc, value);
	}

	@Override
	public double get(Location loc) {
		return map.get(loc);
	}

	@Override
	public int indexOf(Location loc) {
		return points.indexOf(loc);
	}

	@Override
	public Location getLocation(int index) {
		return points.get(index);
	}

	@Override
	public boolean contains(Location loc) {
		return points.contains(loc);
	}
	
	public static ArbDiscrGeographicDataSet loadXYZFile(String fileName, boolean latitudeX)
	throws FileNotFoundException, IOException {
		ArrayList<String> lines = FileUtils.loadFile(fileName);
		
		ArbDiscrGeographicDataSet xyz = new ArbDiscrGeographicDataSet(latitudeX);
		
		for (String line : lines) {
			if (line.startsWith("#"))
				continue;
			if (line.length() < 2)
				continue;
			StringTokenizer tok = new StringTokenizer(line);
			if (tok.countTokens() < 3)
				continue;
			
			double lat, lon;
			
			if (latitudeX) {
				lat = Double.parseDouble(tok.nextToken());
				lon = Double.parseDouble(tok.nextToken());
			} else {
				lon = Double.parseDouble(tok.nextToken());
				lat = Double.parseDouble(tok.nextToken());
			}
			double val = Double.parseDouble(tok.nextToken());
			
			xyz.set(new Location(lat, lon), val);
		}
		
		return xyz;
	}
	
	public static void writeXYZFile(XYZ_DataSetAPI xyz, String fileName) throws IOException {
		ArbDiscrXYZ_DataSet.writeXYZFile(xyz, fileName);
	}

	@Override
	public Object clone() {
		ArbDiscrGeographicDataSet data = new ArbDiscrGeographicDataSet(isLatitudeX());
		
		for (int i=0; i<size(); i++) {
			data.set(getPoint(i), get(i));
		}
		
		return data;
	}

	@Override
	public double getMinX() {
		if (latitudeX)
			return LocationList.calcMinLat(points);
		else
			return LocationList.calcMinLon(points);
	}

	@Override
	public double getMaxX() {
		if (latitudeX)
			return LocationList.calcMaxLat(points);
		else
			return LocationList.calcMaxLon(points);
	}

	@Override
	public double getMinY() {
		if (latitudeX)
			return LocationList.calcMinLon(points);
		else
			return LocationList.calcMinLat(points);
	}

	@Override
	public double getMaxY() {
		if (latitudeX)
			return LocationList.calcMaxLon(points);
		else
			return LocationList.calcMaxLat(points);
	}
	
	private MinMaxAveTracker getZTracker() {
		MinMaxAveTracker tracker = new MinMaxAveTracker();
		for (double value : map.values()) {
			tracker.addValue(value);
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

	@Override
	public void set(Point2D point, double z) {
		set(ptToLoc(point), z);
	}

	@Override
	public void set(double x, double y, double z) {
		set(ptToLoc(new Point2D.Double(x, y)), z);
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
		return get(ptToLoc(new Point2D.Double(x, y)));
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
	public int indexOf(double x, double y) {
		return indexOf(new Point2D.Double(x, y));
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
		return points.size();
	}

	@Override
	public void setAll(XYZ_DataSetAPI dataset) {
		if (dataset instanceof GeographicDataSetAPI) {
			GeographicDataSetAPI geo = (GeographicDataSetAPI)dataset;
			for (int i=0; i<dataset.size(); i++)
				set(geo.getLocation(i), geo.get(i));
		} else {
			for (int i=0; i<dataset.size(); i++)
				set(dataset.getPoint(i), dataset.get(i));
		}
	}

	@Override
	public void setLatitudeX(boolean latitudeX) {
		this.latitudeX = latitudeX;
	}

}
