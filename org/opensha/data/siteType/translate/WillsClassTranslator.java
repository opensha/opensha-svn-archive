package org.opensha.data.siteType.translate;

import java.util.HashMap;

public class WillsClassTranslator {
	
	public final static String WILLS_B = "B";
	public final static String WILLS_BC = "BC";
	public final static String WILLS_C = "C";
	public final static String WILLS_CD = "CD";
	public final static String WILLS_D = "D";
	public final static String WILLS_DE = "DE";
	public final static String WILLS_E = "E";
	
	public final static HashMap<String, Double> wills_vs30_map = new HashMap<String, Double>();
	
	static {
		wills_vs30_map.put(WILLS_B,		1000d);
		wills_vs30_map.put(WILLS_BC,	760d);
		wills_vs30_map.put(WILLS_C,		560d);
		wills_vs30_map.put(WILLS_CD,	360d);
		wills_vs30_map.put(WILLS_D,		270d);
		wills_vs30_map.put(WILLS_DE,	180d);
		wills_vs30_map.put(WILLS_E,		Double.NaN);
	}
	
	public static double getVS30(String wills) {
		return wills_vs30_map.get(wills);
	}
	
	public static String getWillsClass(double vs30) {
		
		// TODO: figure out what to do with the in-between ranges!
		// This isn't quite clear: http://www.opensha.org/documentation/glossary/WillsSiteClass.html
		
		// handle the main ranges
		if (vs30 > 760)
			return WILLS_B;
		else if (vs30 > 360)
			return WILLS_C;
		else if (vs30 > 180)
			return WILLS_D;
		
		return WILLS_E;
	}
}
