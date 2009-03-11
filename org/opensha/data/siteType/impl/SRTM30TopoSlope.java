package org.opensha.data.siteType.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.opensha.calc.ArcsecondConverter;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.RectangularGeographicRegion;
import org.opensha.data.siteType.AbstractSiteData;
import org.opensha.data.siteType.SiteDataToXYZ;
import org.opensha.data.siteType.servlet.SiteDataServletAccessor;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.util.binFile.BinaryMesh2DCalculator;
import org.opensha.util.binFile.GeolocatedRectangularBinaryMesh2DCalculator;

public class SRTM30TopoSlope extends AbstractSiteData<Double> {
	
	public static final String NAME = "SRTM30 Topographic Slope";
	public static final String SHORT_NAME = "SRTM30_Slope";
	
	public static final double arcSecondSpacing = 30.0;
	// for 30 arc seconds this is 0.008333333333333333
	public static final double spacing = ArcsecondConverter.getDegrees(arcSecondSpacing);
	
	public static final int nx = 43200;
	public static final int ny = 18000;
	
	public static final double minLon = -180;
	public static final double minLat = -60;
	
	public static final String SERVLET_URL = "http://opensha.usc.edu:8080/OpenSHA/SiteData/SRTM30_Slope";
	
	private boolean useServlet;
	
	private GeographicRegion region;
	
	private RandomAccessFile file = null;
	private byte[] recordBuffer = null;
	private FloatBuffer floatBuff = null;
	
	private GeolocatedRectangularBinaryMesh2DCalculator calc = null;
	
	private SiteDataServletAccessor<Double> servlet = null;
	
	public SRTM30TopoSlope() throws IOException {
		this(null, true);
	}
	
	public SRTM30TopoSlope(String fileName) throws IOException {
		this(fileName, false);
	}
	
	private SRTM30TopoSlope(String fileName, boolean useServlet) throws IOException {
		this.useServlet = useServlet;
		if (useServlet) {
			servlet = new SiteDataServletAccessor<Double>(SERVLET_URL);
		} else {
			file = new RandomAccessFile(new File(fileName), "r");
			
			recordBuffer = new byte[4];
			ByteBuffer record = ByteBuffer.wrap(recordBuffer);
			record.order(ByteOrder.LITTLE_ENDIAN);
			floatBuff = record.asFloatBuffer();
		}
		
		calc = new GeolocatedRectangularBinaryMesh2DCalculator(
				BinaryMesh2DCalculator.TYPE_FLOAT, nx, ny, minLat, minLon, spacing);
		
		calc.setStartBottom(false);
		calc.setStartLeft(true);
		
		try {
			region = new RectangularGeographicRegion(-60, 90, -180, 180);
		} catch (RegionConstraintException e) {
			e.printStackTrace();
		}
	}

	public GeographicRegion getApplicableRegion() {
		return region;
	}

	public Location getClosestDataLocation(Location loc) throws IOException {
		return calc.calcClosestLocation(loc);
	}

	public String getMetadata() {
		return "Topographic slope derived from version 2 of STRM30 30 Arcsecond Digital" +
				"Elevation Model.";
	}

	public String getName() {
		return NAME;
	}

	public double getResolution() {
		return spacing;
	}

	public String getShortName() {
		return SHORT_NAME;
	}

	public String getType() {
		return TYPE_TOPOGRAPHIC_SLOPE;
	}

	public String getTypeFlag() {
		return TYPE_FLAG_MEASURED;
	}

	public Double getValue(Location loc) throws IOException {
		if (useServlet) {
			return servlet.getValue(loc);
		} else {
			long pos = calc.calcClosestLocationFileIndex(loc);
			
//			System.out.println("Seek pos: " + pos);
			
			if (pos < 0 || pos > calc.getMaxFilePos())
				return Double.NaN;
			
			file.seek(pos);
			file.read(recordBuffer);
			
			double val = floatBuff.get(0);
			
			return val;
		}
	}
	
	public ArrayList<Double> getValues(LocationList locs) throws IOException {
		if (useServlet) {
			return servlet.getValues(locs);
		} else {
			return super.getValues(locs);
		}
	}

	public boolean isValueValid(Double val) {
		return val != null && !Double.isNaN(val);
	}
	
	public static void main(String args[]) throws IOException, RegionConstraintException {
		SRTM30TopoSlope data = new SRTM30TopoSlope();
		
		System.out.println(data.getValue(new Location(34, -118)));
		System.out.println(data.getValue(new Location(32, -118)));
		
		EvenlyGriddedRectangularGeographicRegion region = new EvenlyGriddedRectangularGeographicRegion(32, 35, -121, -115, 0.02);
//		EvenlyGriddedRectangularGeographicRegion region = new EvenlyGriddedRectangularGeographicRegion(-60, 60, -180, 180, 1);
//		
		SiteDataToXYZ.writeXYZ(data, region, "/tmp/topo_slope.txt");
	}
}
