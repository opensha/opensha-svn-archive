package org.opensha.util.binFile;

import org.opensha.data.Location;
import org.opensha.data.region.RectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;

public class GeolocatedRectangularBinaryMesh2DCalculator extends
		BinaryMesh2DCalculator {
	
	private double minLat;
	private double maxLat;
	private double minLon;
	private double maxLon;
	private double gridSpacing;
	
	private boolean startBottom = true;
	private boolean startLeft = true;

	public GeolocatedRectangularBinaryMesh2DCalculator(int numType, int nx, int ny,
			double minLat, double minLon, double gridSpacing) {
		super(numType, nx, ny);
		
		this.minLat = minLat;
		this.minLon = minLon;
		this.maxLat = minLat + gridSpacing * ny;
		this.maxLon = minLon + gridSpacing * nx;
		this.gridSpacing = gridSpacing;
	}
	
	public long[] calcClosestLocationIndices(Location loc) {
		return calcClosestLocationIndices(loc.getLatitude(), loc.getLongitude());
	}
	
	public long[] calcClosestLocationIndices(double lat, double lon) {
		long x = calcX(lon);
		long y = calcY(lat);
		
		long pt[] = { x, y };
		
		return pt;
	}
	
	public long calcClosestLocationIndex(Location loc) {
		return calcClosestLocationIndex(loc.getLatitude(), loc.getLongitude());
	}
	
	public long calcClosestLocationIndex(double lat, double lon) {
		long pt[] = calcClosestLocationIndices(lat, lon);
		
		return this.calcMeshIndex(pt[0], pt[1]);
	}
	
	public long calcClosestLocationFileIndex(Location loc) {
		return calcClosestLocationFileIndex(loc.getLatitude(), loc.getLongitude());
	}
	
	public long calcClosestLocationFileIndex(double lat, double lon) {
		long pt[] = calcClosestLocationIndices(lat, lon);
		
		return this.calcFileIndex(pt[0], pt[1]);
	}
	
	public Location getLocationForPoint(long x, long y) {
		double lat = maxLat - y * gridSpacing;
		double lon = minLon + x * gridSpacing;
		
		return new Location(lat, lon);
	}
	
	public Location calcClosestLocation(Location loc) {
		return calcClosestLocation(loc.getLatitude(), loc.getLongitude());
	}
	
	public Location calcClosestLocation(double lat, double lon) {
		long pt[] = calcClosestLocationIndices(lat, lon);
		
		return getLocationForPoint(pt[0], pt[1]);
	}
	
	private long calcX(double lon) {
		if (startLeft)
			return (long)((lon - minLon) / gridSpacing + 0.5);
		else
			return (long)((maxLon - lon) / gridSpacing + 0.5);
	}
	
	private long calcY(double lat) {
		if (startBottom)
			return (long)((lat - minLat) / gridSpacing + 0.5);
		else
			return (long)((maxLat - lat) / gridSpacing + 0.5);
	}

	public double getMinLat() {
		return minLat;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public double getMinLon() {
		return minLon;
	}

	public double getMaxLon() {
		return maxLon;
	}

	public double getGridSpacing() {
		return gridSpacing;
	}

	public boolean isStartBottom() {
		return startBottom;
	}

	public void setStartBottom(boolean startBottom) {
		this.startBottom = startBottom;
	}

	public boolean isStartLeft() {
		return startLeft;
	}

	public void setStartLeft(boolean startLeft) {
		this.startLeft = startLeft;
	}
	
	public RectangularGeographicRegion getApplicableRegion() {
		try {
			return new RectangularGeographicRegion(minLat, maxLat, minLon, maxLon);
		} catch (RegionConstraintException e) {
			e.printStackTrace();
			return null;
		}
	}

}
