package org.opensha.commons.data.xyz;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.commons.geo.Location;
import org.opensha.commons.util.FileUtils;

public class ArbDiscrGeographicDataSet extends ArbDiscrXYZ_DataSet implements GeographicDataSetAPI {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean latitudeX;
	
	public ArbDiscrGeographicDataSet(boolean latitudeX) {
		super();
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
		super.set(locToPoint(loc), value);
	}

	@Override
	public double get(Location loc) {
		return super.get(locToPoint(loc));
	}

	@Override
	public int indexOf(Location loc) {
		return super.indexOf(locToPoint(loc));
	}

	@Override
	public Location getLocation(int index) {
		return ptToLoc(getPoint(index));
	}

	@Override
	public boolean contains(Location loc) {
		return contains(locToPoint(loc));
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

	@Override
	public Object clone() {
		ArbDiscrGeographicDataSet data = new ArbDiscrGeographicDataSet(isLatitudeX());
		
		for (int i=0; i<size(); i++) {
			data.set(getPoint(i), get(i));
		}
		
		return data;
	}

}
