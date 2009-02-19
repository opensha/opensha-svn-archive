package org.opensha.data.siteType.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.siteType.SiteDataAPI;
import org.opensha.data.siteType.SiteDataToXYZ;
import org.opensha.data.siteType.servlet.SiteDataServletAccessor;
import org.opensha.util.binFile.BinaryMesh2DCalculator;
import org.opensha.util.binFile.GeolocatedRectangularBinaryMesh2DCalculator;

public class CVM4BasinDepth implements SiteDataAPI<Double> {
	
	public static final String NAME = "SCEC Community Velocity Model Basin Depth";
	public static final String SHORT_NAME = "CVM4";
	
	public static final double minLat = 31;
	public static final double minLon = -121;
	
	private static final int nx = 1701;
	private static final int ny = 1101;
	
	private static final long MAX_FILE_POS = (nx-1) * (ny-1) * 4;
	
	public static final double gridSpacing = 0.005;
	
	public static final String DEPTH_2_5_FILE = "data/siteType/CVM4/depth_2.5.bin";
	public static final String DEPTH_1_0_FILE = "data/siteType/CVM4/depth_1.0.bin";
	
	public static final String SERVLET_2_5_URL = "http://opensha.usc.edu:8080/OpenSHA/SiteData/CVM4_2_5";
	public static final String SERVLET_1_0_URL = "http://opensha.usc.edu:8080/OpenSHA/SiteData/CVM4_1_0";
	
	private RandomAccessFile file = null;
	private GeolocatedRectangularBinaryMesh2DCalculator calc = null;
	
	private byte[] recordBuffer = null;
	private FloatBuffer floatBuff = null;
	
	private boolean useServlet;
	
	private String type;
	
	public CVM4BasinDepth(String type, boolean useServlet) throws IOException {
		this(type, null, useServlet);
	}
	
	public CVM4BasinDepth(String type, File dataFile, boolean useServlet) throws IOException {
		this.useServlet = useServlet;
		this.type = type;
		
		calc = new GeolocatedRectangularBinaryMesh2DCalculator(
				BinaryMesh2DCalculator.TYPE_FLOAT, nx, ny, minLat, minLon, gridSpacing);
		
		if (useServlet) {
			
		} else {
			if (dataFile == null) {
				if (type.equals(TYPE_DEPTH_TO_1_0))
					dataFile = new File(DEPTH_1_0_FILE);
				else
					dataFile = new File(DEPTH_2_5_FILE);
			}
			
			file = new RandomAccessFile(dataFile, "r");
			
			calc.setStartBottom(true);
			calc.setStartLeft(true);
			
			recordBuffer = new byte[4];
			ByteBuffer record = ByteBuffer.wrap(recordBuffer);
			record.order(ByteOrder.LITTLE_ENDIAN);
			
			floatBuff = record.asFloatBuffer();
		}
	}

	public GeographicRegion getApplicableRegion() {
		return calc.getApplicableRegion();
	}

	public Location getClosestDataLocation(Location loc) {
		return calc.calcClosestLocation(loc);
	}

	public String getName() {
		return NAME;
	}
	
	public String getShortName() {
		return SHORT_NAME;
	}

	public double getResolution() {
		return gridSpacing;
	}

	public String getType() {
		return type;
	}

	public Double getValue(Location loc) throws IOException {
		if (useServlet) {
			throw new RuntimeException("Servlet not implemented yet!");
		} else {
			long pos = calc.calcClosestLocationFileIndex(loc);
			
			if (pos > MAX_FILE_POS || pos < 0)
				return Double.NaN;
			
			file.seek(pos);
			file.read(recordBuffer);
			
			// this is in meters
			double val = floatBuff.get(0);
			
			// convert to KM
			return val / 1000d;
		}
	}

	public ArrayList<Double> getValues(LocationList locs) throws IOException {
		ArrayList<Double> vals = new ArrayList<Double>();
		
		for (int i=0; i<locs.size(); i++) {
			vals.add(this.getValue(locs.getLocationAt(i)));
		}
		
		return vals;
	}

	public boolean isValueValid(Double el) {
		return el.equals(Double.NaN);
	}
	
	public static void main(String[] args) throws IOException {
//		CVM4BasinDepth map = new CVM4BasinDepth(TYPE_DEPTH_TO_1_0, false);
//		SiteDataToXYZ.writeXYZ(map, 0.02, "/tmp/basin.txt");
		
		SiteDataServletAccessor<Double> serv = new SiteDataServletAccessor<Double>(SERVLET_2_5_URL);
		LocationList locs = new LocationList();
		locs.addLocation(new Location(34.01920, -118.28800));
		locs.addLocation(new Location(34.91920, -118.3200));
		locs.addLocation(new Location(34.781920, -118.88600));
		locs.addLocation(new Location(34.21920, -118.38600));
		locs.addLocation(new Location(34.61920, -118.18600));
		
		ArrayList<Double> vals = serv.getValues(locs);
		for (double val : vals)
			System.out.println(val);
	}

}
