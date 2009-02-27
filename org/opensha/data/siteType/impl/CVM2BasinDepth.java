package org.opensha.data.siteType.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.siteType.AbstractSiteData;
import org.opensha.data.siteType.SiteDataToXYZ;
import org.opensha.data.siteType.servlet.SiteDataServletAccessor;
import org.opensha.util.binFile.BinaryMesh2DCalculator;
import org.opensha.util.binFile.GeolocatedRectangularBinaryMesh2DCalculator;

public class CVM2BasinDepth extends AbstractSiteData<Double> {
	
	public static final String NAME = "SCEC Community Velocity Model Version 2 Basin Depth";
	public static final String SHORT_NAME = "CVM2";
	
	public static final String FILE_NAME = "data/siteData/CVM2/depth_2.5.bin";
	
	private boolean useServlet;
	
	private SiteDataServletAccessor<Double> servlet = null;
	
	public static final String SERVLET_URL = "http://opensha.usc.edu:8080/OpenSHA/SiteData/CVM2";
	
	private GeolocatedRectangularBinaryMesh2DCalculator calc = null;
	
	private RandomAccessFile file = null;
	
	private byte[] recordBuffer = null;
	private FloatBuffer floatBuff = null;
	
	public static final double minLat = 32;
	public static final double minLon = -121;
	
	private static final int nx = 701;
	private static final int ny = 401;
	
	private static final long MAX_FILE_POS = (nx-1) * (ny-1) * 4;
	
	public static final double gridSpacing = 0.01;
	
	public CVM2BasinDepth() {
		super();
		useServlet = true;
		initCommon();
		
		servlet = new SiteDataServletAccessor<Double>(SERVLET_URL);
	}
	
	public CVM2BasinDepth(String fileName) throws IOException {
		super();
		useServlet = false;
		initCommon();
		
		file = new RandomAccessFile(fileName, "r");
		
		calc.setStartBottom(true);
		calc.setStartLeft(true);
		
		recordBuffer = new byte[4];
		ByteBuffer record = ByteBuffer.wrap(recordBuffer);
		record.order(ByteOrder.LITTLE_ENDIAN);
		
		floatBuff = record.asFloatBuffer();
	}
	
	private void initCommon() {
		calc = new GeolocatedRectangularBinaryMesh2DCalculator(
				BinaryMesh2DCalculator.TYPE_FLOAT, nx, ny, minLat, minLon, gridSpacing);
	}

	public GeographicRegion getApplicableRegion() {
		return calc.getApplicableRegion();
	}

	public Location getClosestDataLocation(Location loc) throws IOException {
		if (useServlet)
			return servlet.getClosestLocation(loc);
		else
			return calc.calcClosestLocation(loc);
	}
	
	public String getName() {
		return NAME;
	}

	public double getResolution() {
		return gridSpacing;
	}

	public String getShortName() {
		return SHORT_NAME;
	}
	
	public String getMetadata() {
		return "Depth to Vs = 2.5 KM/sec, extracted from version 2 of the SCEC Community Velocity Model, " +
				"part of the SCEC Phase III report.\n\n" +
				"It has a grid spacing of " + gridSpacing + " degrees, and was converted to binary for fast " +
				"I/O February 2009.";
	}

	public String getType() {
		return TYPE_DEPTH_TO_2_5;
	}

	public String getTypeFlag() {
		return TYPE_FLAG_MEASURED;
	}

	public Double getValue(Location loc) throws IOException {
		if (useServlet) {
			return servlet.getValue(loc);
		} else {
			long pos = calc.calcClosestLocationFileIndex(loc);
			
			if (pos > MAX_FILE_POS || pos < 0)
				return Double.NaN;
			
			file.seek(pos);
			file.read(recordBuffer);
			
			// this is in meters
			double val = floatBuff.get(0);
			
			// convert to KM
			Double dobVal = (double)val / 1000d;
			return dobVal;
		}
	}
	
	public ArrayList<Double> getValues(LocationList locs) throws IOException {
		if (useServlet) {
			return servlet.getValues(locs);
		} else {
			return super.getValues(locs);
		}
	}

	public boolean isValueValid(Double el) {
		return el >=0 && !Double.isNaN(el);
	}
	
	public static void main(String[] args) throws IOException {
//		CVM2BasinDepth map = new CVM2BasinDepth(FILE_NAME);
		CVM2BasinDepth map = new CVM2BasinDepth();
		SiteDataToXYZ.writeXYZ(map, 0.01, "/tmp/basin.txt");
		
		LocationList locs = new LocationList();
		locs.addLocation(new Location(34.01920, -118.28800));
		locs.addLocation(new Location(34.91920, -118.3200));
		locs.addLocation(new Location(34.781920, -118.88600));
		locs.addLocation(new Location(34.21920, -118.38600));
		locs.addLocation(new Location(34.61920, -118.18600));
		
		ArrayList<Double> vals = map.getValues(locs);
		for (double val : vals)
			System.out.println(val);
	}

}
