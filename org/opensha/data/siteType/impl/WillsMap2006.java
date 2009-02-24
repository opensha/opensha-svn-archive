package org.opensha.data.siteType.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.siteType.SiteDataAPI;
import org.opensha.data.siteType.SiteDataToXYZ;
import org.opensha.data.siteType.servlet.SiteDataServletAccessor;
import org.opensha.util.binFile.BinaryMesh2DCalculator;
import org.opensha.util.binFile.GeolocatedRectangularBinaryMesh2DCalculator;

public class WillsMap2006 implements SiteDataAPI<Double> {
	
	public static final int nx = 49867;
	public static final int ny = 44016;
	
	public static final double spacing = 0.00021967246502752;
	
	public static final double minLon = -124.52997177169;
	public static final double minLat = 32.441345502265;
	
	public static final String NAME = "CGS/Wills Site Classification Map (2006)";
	public static final String SHORT_NAME = "Wills2006";
	
	public static final String SERVER_BIN_FILE = "/export/opensha/data/siteData/wills2006.bin";
	public static final String DEBUG_BIN_FILE = "/home/kevin/OpenSHA/siteClass/out.bin";
	
	public static final String SERVLET_URL = "http://opensha.usc.edu:8080/OpenSHA/SiteData/Wills2006";
	
	private RandomAccessFile file = null;
	private byte[] recordBuffer = null;
	private ShortBuffer shortBuff = null;
	
	private GeolocatedRectangularBinaryMesh2DCalculator calc = null;
	
	private boolean useServlet;
	
	private SiteDataServletAccessor<Double> servlet = null;
	
	public WillsMap2006() throws IOException {
		this.useServlet = true;
		
		initCommon();
		
		servlet = new SiteDataServletAccessor<Double>(SERVLET_URL);
	}
	
	private GeographicRegion applicableRegion;
	
	public WillsMap2006(File dataFile) throws IOException {
		this.useServlet = false;
		
		initCommon();
		
		file = new RandomAccessFile(dataFile, "r");
		
		recordBuffer = new byte[2];
		ByteBuffer record = ByteBuffer.wrap(recordBuffer);
		record.order(ByteOrder.LITTLE_ENDIAN);
		shortBuff = record.asShortBuffer();
	}
	
	private void initCommon() {
		calc = new GeolocatedRectangularBinaryMesh2DCalculator(
				BinaryMesh2DCalculator.TYPE_SHORT,nx, ny, minLat, minLon, spacing);
		
		calc.setStartBottom(false);
		calc.setStartLeft(true);
		
		applicableRegion = calc.getApplicableRegion();
	}

	public GeographicRegion getApplicableRegion() {
		return applicableRegion;
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
		return spacing;
	}

	public String getType() {
		return SiteDataAPI.TYPE_VS30;
	}

	public Double getValue(Location loc) throws IOException {
		if (useServlet) {
			return servlet.getValue(loc);
		} else {
			long pos = calc.calcClosestLocationFileIndex(loc);
			
			if (pos < 0 || pos > calc.getMaxFilePos())
				return Double.NaN;
			
			file.seek(pos);
			file.read(recordBuffer);
			
			int val = shortBuff.get(0);
			
			if (val <= 0)
				return Double.NaN;
			
			return (double)val;
		}
	}

	public ArrayList<Double> getValues(LocationList locs) throws IOException {
		if (useServlet) {
			return servlet.getValues(locs);
		} else {
			ArrayList<Double> vals = new ArrayList<Double>();
			
			for (Location loc : locs) {
				vals.add(this.getValue(loc));
			}
			
			return vals;
		}
	}
	
	public boolean isValueValid(Double val) {
		return val.equals(Double.NaN);
	}
	
	public static void main(String[] args) throws IOException {
		
		WillsMap2006 map = new WillsMap2006();
		SiteDataToXYZ.writeXYZ(map, 0.02, "/tmp/wills.txt");
		
//		SiteDataServletAccessor<Double> serv = new SiteDataServletAccessor<Double>(SERVLET_URL);
//		
//		LocationList locs = new LocationList();
//		locs.addLocation(new Location(34.01920, -118.28800));
//		locs.addLocation(new Location(34.91920, -118.3200));
//		locs.addLocation(new Location(34.781920, -118.88600));
//		locs.addLocation(new Location(34.21920, -118.38600));
//		locs.addLocation(new Location(34.61920, -118.18600));
//		
//		ArrayList<Double> vals = map.getValues(locs);
//		for (double val : vals)
//			System.out.println(val);
	}

}
