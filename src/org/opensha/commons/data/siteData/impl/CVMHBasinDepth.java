package org.opensha.commons.data.siteData.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.opensha.commons.data.siteData.AbstractCVMBasinDepth;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.ServerPrefUtils;

public class CVMHBasinDepth extends AbstractCVMBasinDepth {
	
	public static final String NAME = "SCEC/Harvard Community Velocity Model Version 11.9.0 Basin Depth"; // TODO
	public static final String SHORT_NAME = "CVMH";
	
	// CVM4 region
	public static final double minLat = 31;
	public static final double minLon = -121;
	private static final int nx = 1701;
	private static final int ny = 1101;
	
//	public static final double minLat = 30.96;
//	public static final double minLon = -120.85;
//	private static final int nx = 1501;
//	private static final int ny = 1129;
	
	private static final double grid_spacing = 0.005;
	
	public static final String DEPTH_2_5_FILE = "src/resources/data/site/CVMH/depth_2.5_first.bin";
	public static final String DEPTH_1_0_FILE = "src/resources/data/site/CVMH/depth_1.0_first.bin";
	
	public static final String SERVLET_2_5_URL = ServerPrefUtils.SERVER_PREFS.getServletBaseURL() + "SiteData/CVMH_2_5";
	public static final String SERVLET_1_0_URL = ServerPrefUtils.SERVER_PREFS.getServletBaseURL() + "SiteData/CVMH_1_0";
	
	public CVMHBasinDepth(String type) throws IOException {
		this(type, null, true);
	}

	public CVMHBasinDepth(String type, File dataFile, boolean useServlet) throws IOException {
		super(nx, ny, minLat, minLon, grid_spacing, true, true, type,
				dataFile, useServlet);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getShortName() {
		return SHORT_NAME;
	}

	@Override
	public String getDataMeasurementType() {
		return TYPE_FLAG_INFERRED;
	}

	@Override
	public String getMetadata() {
		return getDataType()+", CVMH 11.9.0 extracted with UCVM 12.2.0 on May 21 2012 by Patrick Small";
	}

	@Override
	protected File getDefaultFile(String type) {
		if (type.equals(TYPE_DEPTH_TO_1_0))
			return new File(DEPTH_1_0_FILE);
		return new File(DEPTH_2_5_FILE);
	}

	@Override
	protected String getServletURL(String type) {
		if (type.equals(TYPE_DEPTH_TO_1_0))
			return SERVLET_1_0_URL;
		return SERVLET_2_5_URL;
	}
	
	public static void main(String[] args) throws IOException {
		CVMHBasinDepth cvmh = new CVMHBasinDepth(TYPE_DEPTH_TO_2_5, null, false);
		
		System.out.println(cvmh.getApplicableRegion());
		FileWriter fw = new FileWriter(new File("/tmp/cvmh_grid_locs.txt"));
		for (long pos=0; pos<=cvmh.maxFilePos; pos+=4) {
			Double val = cvmh.getValue(pos);
			long x = cvmh.calc.calcFileX(pos);
			long y = cvmh.calc.calcFileY(pos);
			Location loc = cvmh.calc.getLocationForPoint(x, y);
//			System.out.println(loc.getLatitude() + ", " + loc.getLongitude() + ": " + val);
			fw.write((float)loc.getLatitude()+"\t"+(float)loc.getLongitude()+"\t"+val.floatValue()+"\n");
		}
		fw.close();
	}

}
