package org.opensha.commons.data.region;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.math.util.MathUtils;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

/**
 * This wrapper class contains a number of California regions commonly and 
 * historically used in seismic hazard analysis.
 * 
 * @author Peter Powers
 * @version $Id:$
 * @see GeographicRegion
 * @see EvenlyGriddedGeographicRegion
 */
public class CaliforniaRegions {
	
	// TODO each should probably implement 'named' interface
	// TODO RELM_NOCAL/SOCAL minimally used; revisit; clean
	
	private static Location anchor = new Location(0,0);
	
	/** 
	 * Gridded region used in the Regional Earthquake Likelihood 
	 * Models (RELM) project. Grid spacing is 0.1&deg;.
	 */
	public static final class RELM_GRIDDED extends 
			EvenlyGriddedGeographicRegion {
		/** New instance of region. */
		public RELM_GRIDDED() {
			super(readCoords("RELM.coords"), 
					BorderType.MERCATOR_LINEAR, 0.1, anchor);
		}
	}

	/** 
	 * A simplified representation of the RELM gridded region. 
	 */
	public static final class RELM_TESTING extends GeographicRegion {
		/** New instance of region. */
		public RELM_TESTING() {
			super(readCoords("RELM_testing.coords"), 
					BorderType.MERCATOR_LINEAR);
		}
	}
			
	/** 
	 * A simplified representation of the RELM gridded region.
	 * Grid spacing is 0.1&deg;.
	 */
	public static final class RELM_TESTING_GRIDDED extends 
			EvenlyGriddedGeographicRegion {
		/** New instance of region. */
		public RELM_TESTING_GRIDDED() {
			super(readCoords("RELM_testing.coords"), 
					BorderType.MERCATOR_LINEAR, 0.1, anchor);
		}
	}

	/** 
	 * Expanded RELM region used to capture large external events.
	 */
	public static final class RELM_COLLECTION extends GeographicRegion {
		/** New instance of region. */
		public RELM_COLLECTION() {
			super(readCoords("RELM_collection.coords"), 
					BorderType.MERCATOR_LINEAR);
		}
	}

	/** 
	 * Expanded gridded RELM region used to capture large external events.
	 * Grid spacing is 0.1&deg;.
	 */
	public static final class RELM_COLLECTION_GRIDDED extends 
			EvenlyGriddedGeographicRegion {
		/** New instance of region. */
		public RELM_COLLECTION_GRIDDED() {
			super(readCoords("RELM_collection.coords"), 
					BorderType.MERCATOR_LINEAR, 0.1, anchor);
		}
	}

	/** 
	 * Northern half of the RELM region.
	 */
	public static final class RELM_NOCAL extends GeographicRegion {
		/** New instance of region. */
		public RELM_NOCAL() {
			super(readCoords("RELM_NoCal.coords"), 
					BorderType.MERCATOR_LINEAR);
		}
	}

	/** 
	 * Northern half of the gridded RELM region. Grid spacing is 0.1&deg;.
	 */
	public static final class RELM_NOCAL_GRIDDED extends 
			EvenlyGriddedGeographicRegion {
		/** New instance of region. */
		public RELM_NOCAL_GRIDDED() {
			super(readCoords("RELM_NoCal.coords"), 
					BorderType.MERCATOR_LINEAR, 0.1, anchor);
		}
	}

	/** 
	 * Southern half of the RELM region.
	 */
	public static final class RELM_SOCAL extends GeographicRegion {
		/** New instance of region. */
		public RELM_SOCAL() {
			super(readCoords("RELM_SoCal.coords"), 
					BorderType.MERCATOR_LINEAR);
		}
	}

	/** 
	 * Southern half of the gridded RELM region. Grid spacing is 0.1&deg;.
	 */
	public static final class RELM_SOCAL_GRIDDED extends 
			EvenlyGriddedGeographicRegion {
		/** New instance of region. */
		public RELM_SOCAL_GRIDDED() {
			super(readCoords("RELM_SoCal.coords"), 
					BorderType.MERCATOR_LINEAR, 0.1, anchor);
		}
	}

	/** 
	 * A gridded, box-shaped central California region used in the 2002  
	 * Working Group on California Earthquake Probabilities (WGCEP).
	 * Grid spacing is 0.1&deg;.
	 */
	public static final class WG02_GRIDDED extends 
			EvenlyGriddedGeographicRegion {
		/** New instance of region. */
		public WG02_GRIDDED() {
			super(readCoords("WG02.coords"), 
					BorderType.MERCATOR_LINEAR, 0.1, anchor);
		}
	}
				
	/** 
	 * A gridded, box-shaped region centered on Los Angeles (with the same 
	 * dimensions as that for the 2002 Working Group) used by the 2007 Working 
	 * Group on California Earthquake Probabilities (WGCEP). Grid spacing
	 * is 0.1&deg;.
	 * 
	 * TODO this may not be necessary; no references
	 */
	public static final class WG07_GRIDDED extends 
			EvenlyGriddedGeographicRegion {
		/** New instance of region. */
		public WG07_GRIDDED() {
			super(readCoords("WG07.coords"), 
					BorderType.MERCATOR_LINEAR, 0.1,
					new Location(34,-118));
		}
	}

	/*
	 * Reads coordinate pairs from a file. Each line of the file should have
	 * a comma-delimited lat-ln pair e.g. 41.23,-117.89
	 */
	private static LocationList readCoords(String filename) {
		BufferedReader br;
		try {
			File f = new File(CaliforniaRegions.class.getResource(
					"/resources/data/region/" + filename).toURI());
			br = new BufferedReader(new FileReader(f));
			LocationList ll = new LocationList();
			String[] vals;
	        String s;
	        while ((s = br.readLine()) != null) {
	        	vals = s.trim().split(",");
	        	double lat = Double.valueOf(vals[0]);
	        	double lon = Double.valueOf(vals[1]);
	        	Location loc = new Location(lat, lon);
	        	ll.addLocation(loc);
	        }
	        br.close();
	        return ll;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
