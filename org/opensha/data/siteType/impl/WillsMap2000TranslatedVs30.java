package org.opensha.data.siteType.impl;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.siteType.AbstractSiteData;
import org.opensha.exceptions.InvalidRangeException;
import org.opensha.sha.util.SiteTranslator;

public class WillsMap2000TranslatedVs30 extends AbstractSiteData<Double> {
	
	public static final String NAME = WillsMap2000.NAME + " - Translated to Vs30";
	public static final String SHORT_NAME = WillsMap2000.SHORT_NAME + "_Vs30";
	
	WillsMap2000 map;
	
	public WillsMap2000TranslatedVs30() {
		this(null, true);
	}
	
	public WillsMap2000TranslatedVs30(String fileName) {
		this(fileName, false);
	}
	
	private WillsMap2000TranslatedVs30(String fileName, boolean useServlet) {
		if (useServlet)
			map = new WillsMap2000(useServlet);
		else
			map = new WillsMap2000(fileName);
	}

	public GeographicRegion getApplicableRegion() {
		return map.getApplicableRegion();
	}

	public Location getClosestDataLocation(Location loc) throws IOException {
		return map.getClosestDataLocation(loc);
	}

	public String getMetadata() {
		String meta = map.getMetadata();
		meta += "\n\nTranslated to Vs30 values with the following table:\n" + SiteTranslator.getWillsVs30TranslationString();
		return null;
	}

	public String getName() {
		return NAME;
	}

	public double getResolution() {
		return map.getResolution();
	}

	public String getShortName() {
		return SHORT_NAME;
	}

	public String getDataType() {
		return TYPE_VS30;
	}

	public String getDataMeasurementType() {
		return map.getDataMeasurementType();
	}

	public Double getValue(Location loc) throws IOException {
		String wills = map.getValue(loc);
		return SiteTranslator.getVS30FromWillsClass(wills);
	}
	
	public ArrayList<Double> getValues(LocationList locs) throws IOException {
		ArrayList<String> willsVals = map.getValues(locs);
		ArrayList<Double> vsVals = new ArrayList<Double>();
		
		for (String wills : willsVals) {
			vsVals.add(SiteTranslator.getVS30FromWillsClass(wills));
		}
		return vsVals;
	}

	public boolean isValueValid(Double val) {
		return val != null && !val.isNaN() && val > 0;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InvalidRangeException 
	 */
	public static void main(String[] args) throws InvalidRangeException, IOException {
		WillsMap2000TranslatedVs30 data = new WillsMap2000TranslatedVs30();
		
		System.out.println(data.getValue(new Location(34, -118)));
	}

}
