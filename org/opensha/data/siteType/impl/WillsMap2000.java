package org.opensha.data.siteType.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.RectangularGeographicRegion;
import org.opensha.data.siteType.SiteDataAPI;
import org.opensha.data.siteType.SiteDataToXYZ;
import org.opensha.data.siteType.servlet.SiteDataServletAccessor;
import org.opensha.data.siteType.translate.WillsClassTranslator;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.gui.servlets.siteEffect.WillsSiteClass;

public class WillsMap2000 implements SiteDataAPI<String> {
	
	public static final String NAME = "CGS/Wills Site Classification Map (2000)";
	public static final String SHORT_NAME = "Wills2000";
	
	public static final double minLat = 31.4;
	public static final double maxLat = 41.983;
	public static final double minLon = -124.45;
	public static final double maxLon = -114;
	
	// approximate...
	public static final double spacing = 0.01667;
	
	private GeographicRegion applicableRegion;
	
	private String fileName = WillsSiteClass.WILLS_FILE;
	
	private boolean useServlet;
	
	public static final String SERVLET_URL = "http://opensha.usc.edu:8080/OpenSHA/SiteData/Wills2006";
	
	SiteDataServletAccessor<String> servlet = null;
	
	public WillsMap2000() {
		this(true);
	}
	
	public WillsMap2000(String fileName) {
		this(false);
		this.fileName= fileName;
	}
	
	public WillsMap2000(boolean useServlet) {
		this.useServlet = useServlet;
		try {
			applicableRegion = new RectangularGeographicRegion(minLat, maxLat, minLon, maxLon);
		} catch (RegionConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (useServlet)
			servlet = new SiteDataServletAccessor<String>(SERVLET_URL);
	}

	public GeographicRegion getApplicableRegion() {
		return applicableRegion;
	}

	public Location getClosestDataLocation(Location loc) throws IOException {
		if (useServlet)
			return servlet.getClosestLocation(loc);
		LocationList locs = new LocationList();
		locs.addLocation(loc);
		WillsSiteClass wills = new WillsSiteClass(locs, WillsSiteClass.WILLS_FILE);
		wills.getWillsSiteClass();
		return wills.getLastLocation();
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
		return TYPE_WILLS_CLASS;
	}

	public String getValue(Location loc) throws IOException {
		if (useServlet)
			return servlet.getValue(loc);
		LocationList locs = new LocationList();
		locs.addLocation(loc);
		return getValues(locs).get(0);
	}

	public ArrayList<String> getValues(LocationList locs) throws IOException {
		if (useServlet)
			return servlet.getValues(locs);
		WillsSiteClass wills = new WillsSiteClass(locs, fileName);
		return wills.getWillsSiteClass();
	}

	public boolean isValueValid(String val) {
		Set<String> keys = WillsClassTranslator.wills_vs30_map.keySet();
		return keys.contains(val);
	}
	
	public static void main(String[] args) throws IOException {
		
		WillsMap2000 map = new WillsMap2000();
		SiteDataToXYZ.writeXYZ(map, 0.02, "/tmp/wills2000.txt");
		
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
