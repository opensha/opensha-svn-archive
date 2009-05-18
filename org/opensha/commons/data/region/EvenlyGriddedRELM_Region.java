package org.opensha.commons.data.region;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

import scratchJavaDevelopers.vipin.relm.CreateRELM_GriddedRegion;
import org.opensha.util.FileUtils;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import scratchJavaDevelopers.vipin.relm.*;

/**
 * <p>Title: EvenlyGriddedRELM_Region.java </p>
 * <p>Description: Creates a region specified by the RELM file. It has  hardcoded
 * location list which forms the outline of the polygon for the region</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EvenlyGriddedRELM_Region extends EvenlyGriddedGeographicRegion {
  protected final static double GRID_SPACING = 0.1;

  public EvenlyGriddedRELM_Region() {
    /**
     * Location list for RELM region
     */
    LocationList locList = getLocationList();
    // make polygon from the location list
    createEvenlyGriddedGeographicRegion(locList, GRID_SPACING);
  }

  /**
   * Location list which formas the outline of the ploygon for RELM region
   * This was obtained by reading RELM output file and finding min/max Longitude. then, Ned edited the file to minimize the number of points
   */
  protected LocationList getLocationList() {
    LocationList locList = new LocationList();
    
    locList.addLocation(new Location(31.5,-117.2));
    locList.addLocation(new Location(31.6, -117.4));
    locList.addLocation(new Location(31.7, -117.6));
    locList.addLocation(new Location(31.8, -117.8));
    locList.addLocation(new Location(31.9, -117.9));
    locList.addLocation(new Location(32.0, -118.0));
    locList.addLocation(new Location(32.1, -118.0));
    locList.addLocation(new Location(32.2, -118.1));
    locList.addLocation(new Location(32.3, -118.2));
    locList.addLocation(new Location(32.4, -118.2));
    locList.addLocation(new Location(32.5, -118.3));
    locList.addLocation(new Location(32.6, -118.3));
    locList.addLocation(new Location(32.7, -118.4));
    locList.addLocation(new Location(32.8, -118.5));
    locList.addLocation(new Location(32.9, -118.8));
    locList.addLocation(new Location(33.0, -119.1));
    locList.addLocation(new Location(33.1, -119.4));
    locList.addLocation(new Location(33.2, -119.7));
    locList.addLocation(new Location(33.3, -120.0));
    locList.addLocation(new Location(33.4, -120.3));
    locList.addLocation(new Location(33.5, -120.6));
    locList.addLocation(new Location(33.6, -120.9));
    locList.addLocation(new Location(33.7, -121.1));
    locList.addLocation(new Location(33.8, -121.2));
    locList.addLocation(new Location(33.9, -121.3));
    locList.addLocation(new Location(34.0, -121.4));
    locList.addLocation(new Location(34.1, -121.5));
    locList.addLocation(new Location(34.2, -121.6));
    locList.addLocation(new Location(34.3, -121.7));
    locList.addLocation(new Location(34.4, -121.8));
    locList.addLocation(new Location(34.5, -121.8));
    locList.addLocation(new Location(34.6, -121.9));
    locList.addLocation(new Location(34.7, -121.9));
    locList.addLocation(new Location(34.8, -122.0));
    locList.addLocation(new Location(34.9, -122.1));
    locList.addLocation(new Location(35.0, -122.1));
    locList.addLocation(new Location(35.1, -122.2));
    locList.addLocation(new Location(35.2, -122.3));
    locList.addLocation(new Location(35.3, -122.3));
    locList.addLocation(new Location(35.4, -122.4));
    locList.addLocation(new Location(35.5, -122.4));
    locList.addLocation(new Location(35.6, -122.5));
    locList.addLocation(new Location(35.7, -122.6));
    locList.addLocation(new Location(35.8, -122.6));
    locList.addLocation(new Location(35.9, -122.7));
    locList.addLocation(new Location(36.0, -122.8));
    locList.addLocation(new Location(36.1, -122.8));
    locList.addLocation(new Location(36.2, -122.9));
    locList.addLocation(new Location(36.3, -123.0));
    locList.addLocation(new Location(36.4, -123.0));
    locList.addLocation(new Location(36.5, -123.1));
    locList.addLocation(new Location(36.6, -123.1));
    locList.addLocation(new Location(36.7, -123.2));
    locList.addLocation(new Location(36.8, -123.3));
    locList.addLocation(new Location(36.9, -123.3));
    locList.addLocation(new Location(37.0, -123.4));
    locList.addLocation(new Location(37.1, -123.5));
    locList.addLocation(new Location(37.2, -123.5));
    locList.addLocation(new Location(37.3, -123.6));
    locList.addLocation(new Location(37.4, -123.6));
    locList.addLocation(new Location(37.5, -123.7));
    locList.addLocation(new Location(37.6, -123.8));
    locList.addLocation(new Location(37.7, -123.8));
    locList.addLocation(new Location(37.8, -123.9));
    locList.addLocation(new Location(37.9, -124.0));
    locList.addLocation(new Location(38.0, -124.0));
    locList.addLocation(new Location(38.1, -124.1));
    locList.addLocation(new Location(38.2, -124.2));
    locList.addLocation(new Location(38.3, -124.2));
    locList.addLocation(new Location(38.4, -124.3));
    locList.addLocation(new Location(38.5, -124.3));
    locList.addLocation(new Location(38.6, -124.4));
    locList.addLocation(new Location(38.7, -124.5));
    locList.addLocation(new Location(38.8, -124.5));
    locList.addLocation(new Location(38.9, -124.6));
    locList.addLocation(new Location(39.0, -124.7));
    locList.addLocation(new Location(39.1, -124.7));
    locList.addLocation(new Location(39.2, -124.8));
    locList.addLocation(new Location(39.3, -124.9));
    locList.addLocation(new Location(39.4, -124.9));
    locList.addLocation(new Location(39.5, -125.0));
    locList.addLocation(new Location(39.6, -125.0));
    locList.addLocation(new Location(39.7, -125.1));
    locList.addLocation(new Location(39.8, -125.2));
    locList.addLocation(new Location(39.9, -125.2));
    locList.addLocation(new Location(40.0, -125.3));
    locList.addLocation(new Location(40.1, -125.4));
    locList.addLocation(new Location(40.2, -125.4));
    locList.addLocation(new Location(40.3, -125.4));
    locList.addLocation(new Location(40.4, -125.4));
    locList.addLocation(new Location(40.5, -125.4));
    locList.addLocation(new Location(40.6, -125.4));
    locList.addLocation(new Location(40.7, -125.4));
    locList.addLocation(new Location(40.8, -125.4));
    locList.addLocation(new Location(40.9, -125.4));
    locList.addLocation(new Location(41.0, -125.4));
    locList.addLocation(new Location(41.1, -125.4));
    locList.addLocation(new Location(41.2, -125.3));
    locList.addLocation(new Location(41.3, -125.3));
    locList.addLocation(new Location(41.4, -125.3));
    locList.addLocation(new Location(41.5, -125.3));
    locList.addLocation(new Location(41.6, -125.3));
    locList.addLocation(new Location(41.7, -125.3));
    locList.addLocation(new Location(41.8, -125.3));
    locList.addLocation(new Location(41.9, -125.3));
    locList.addLocation(new Location(42.0, -125.3));
    locList.addLocation(new Location(42.1, -125.3));
    locList.addLocation(new Location(42.2, -125.3));
    locList.addLocation(new Location(42.3, -125.3));
    locList.addLocation(new Location(42.4, -125.3));
    locList.addLocation(new Location(42.5, -125.2));
    locList.addLocation(new Location(42.6, -125.2));
    locList.addLocation(new Location(42.7, -125.2));
    locList.addLocation(new Location(42.8, -125.2));
    locList.addLocation(new Location(42.9, -125.2));
    locList.addLocation(new Location(43.0, -125.2));
    locList.addLocation(new Location(43.0, -119.0));
    locList.addLocation(new Location(42.9, -119.0));
    locList.addLocation(new Location(42.8, -119.0));
    locList.addLocation(new Location(42.7, -119.0));
    locList.addLocation(new Location(42.6, -119.0));
    locList.addLocation(new Location(42.5, -119.0));
    locList.addLocation(new Location(42.4, -119.0));
    locList.addLocation(new Location(42.3, -119.0));
    locList.addLocation(new Location(42.2, -119.0));
    locList.addLocation(new Location(42.1, -119.0));
    locList.addLocation(new Location(42.0, -119.0));
    locList.addLocation(new Location(41.9, -119.0));
    locList.addLocation(new Location(41.8, -119.0));
    locList.addLocation(new Location(41.7, -119.0));
    locList.addLocation(new Location(41.6, -119.0));
    locList.addLocation(new Location(41.5, -119.0));
    locList.addLocation(new Location(41.4, -119.0));
    locList.addLocation(new Location(41.3, -119.0));
    locList.addLocation(new Location(41.2, -119.0));
    locList.addLocation(new Location(41.1, -119.0));
    locList.addLocation(new Location(41.0, -119.0));
    locList.addLocation(new Location(40.9, -119.0));
    locList.addLocation(new Location(40.8, -119.0));
    locList.addLocation(new Location(40.7, -119.0));
    locList.addLocation(new Location(40.6, -119.0));
    locList.addLocation(new Location(40.5, -119.0));
    locList.addLocation(new Location(40.4, -119.0));
    locList.addLocation(new Location(40.3, -119.0));
    locList.addLocation(new Location(40.2, -119.0));
    locList.addLocation(new Location(40.1, -119.0));
    locList.addLocation(new Location(40.0, -119.0));
    locList.addLocation(new Location(39.9, -119.0));
    locList.addLocation(new Location(39.8, -119.0));
    locList.addLocation(new Location(39.7, -119.0));
    locList.addLocation(new Location(39.6, -119.0));
    locList.addLocation(new Location(39.5, -119.0));
    locList.addLocation(new Location(39.4, -118.9));
    locList.addLocation(new Location(39.3, -118.8));
    locList.addLocation(new Location(39.2, -118.7));
    locList.addLocation(new Location(39.1, -118.5));
    locList.addLocation(new Location(39.0, -118.4));
    locList.addLocation(new Location(38.9, -118.3));
    locList.addLocation(new Location(38.8, -118.1));
    locList.addLocation(new Location(38.7, -118.0));
    locList.addLocation(new Location(38.6, -117.9));
    locList.addLocation(new Location(38.5, -117.7));
    locList.addLocation(new Location(38.4, -117.6));
    locList.addLocation(new Location(38.3, -117.4));
    locList.addLocation(new Location(38.2, -117.3));
    locList.addLocation(new Location(38.1, -117.2));
    locList.addLocation(new Location(38.0, -117.0));
    locList.addLocation(new Location(37.9, -116.9));
    locList.addLocation(new Location(37.8, -116.8));
    locList.addLocation(new Location(37.7, -116.6));
    locList.addLocation(new Location(37.6, -116.5));
    locList.addLocation(new Location(37.5, -116.4));
    locList.addLocation(new Location(37.4, -116.2));
    locList.addLocation(new Location(37.3, -116.1));
    locList.addLocation(new Location(37.2, -116.0));
    locList.addLocation(new Location(37.1, -115.8));
    locList.addLocation(new Location(37.0, -115.7));
    locList.addLocation(new Location(36.9, -115.6));
    locList.addLocation(new Location(36.8, -115.4));
    locList.addLocation(new Location(36.7, -115.3));
    locList.addLocation(new Location(36.6, -115.1));
    locList.addLocation(new Location(36.5, -115.0));
    locList.addLocation(new Location(36.4, -114.9));
    locList.addLocation(new Location(36.3, -114.7));
    locList.addLocation(new Location(36.2, -114.6));
    locList.addLocation(new Location(36.1, -114.5));
    locList.addLocation(new Location(36.0, -114.3));
    locList.addLocation(new Location(35.9, -114.2));
    locList.addLocation(new Location(35.8, -114.1));
    locList.addLocation(new Location(35.7, -114.0));
    locList.addLocation(new Location(35.6, -113.9));
    locList.addLocation(new Location(35.5, -113.8));
    locList.addLocation(new Location(35.4, -113.8));
    locList.addLocation(new Location(35.3, -113.7));
    locList.addLocation(new Location(35.2, -113.6));
    locList.addLocation(new Location(35.1, -113.6));
    locList.addLocation(new Location(35.0, -113.5));
    locList.addLocation(new Location(34.9, -113.5));
    locList.addLocation(new Location(34.8, -113.4));
    locList.addLocation(new Location(34.7, -113.3));
    locList.addLocation(new Location(34.6, -113.3));
    locList.addLocation(new Location(34.5, -113.2));
    locList.addLocation(new Location(34.4, -113.1));
    locList.addLocation(new Location(34.3, -113.1));
    locList.addLocation(new Location(34.2, -113.1));
    locList.addLocation(new Location(34.1, -113.1));
    locList.addLocation(new Location(34.0, -113.2));
    locList.addLocation(new Location(33.9, -113.2));
    locList.addLocation(new Location(33.8, -113.2));
    locList.addLocation(new Location(33.7, -113.3));
    locList.addLocation(new Location(33.6, -113.3));
    locList.addLocation(new Location(33.5, -113.3));
    locList.addLocation(new Location(33.4, -113.3));
    locList.addLocation(new Location(33.3, -113.4));
    locList.addLocation(new Location(33.2, -113.4));
    locList.addLocation(new Location(33.1, -113.4));
    locList.addLocation(new Location(33.0, -113.5));
    locList.addLocation(new Location(32.9, -113.5));
    locList.addLocation(new Location(32.8, -113.5));
    locList.addLocation(new Location(32.7, -113.5));
    locList.addLocation(new Location(32.6, -113.5));
    locList.addLocation(new Location(32.5, -113.6));
    locList.addLocation(new Location(32.4, -113.6));
    locList.addLocation(new Location(32.3, -113.6));
    locList.addLocation(new Location(32.2, -113.6));
    locList.addLocation(new Location(32.1, -113.7));
    locList.addLocation(new Location(32.0, -113.9));
    locList.addLocation(new Location(31.9, -114.1));
    locList.addLocation(new Location(31.8, -114.2));
    locList.addLocation(new Location(31.7, -114.4));
    locList.addLocation(new Location(31.6, -115.2));
    locList.addLocation(new Location(31.5, -116.4));
    return locList;
  }
  
  public static void main(String []args) {
	  EvenlyGriddedRELM_Region relmCollectionRegion = new EvenlyGriddedRELM_Region();
		try {
			FileWriter fw = new FileWriter("EvenlyGriddedRELM_Region.txt");
			for(int i=0; i<relmCollectionRegion.getNumGridLocs(); ++i) {
				Location loc = relmCollectionRegion.getGridLocation(i);
				fw.write((float)loc.getLatitude()+","+(float)loc.getLongitude()+"\n");
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}