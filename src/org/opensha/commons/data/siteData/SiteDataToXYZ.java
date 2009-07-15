package org.opensha.commons.data.siteData;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.EvenlyGriddedGeographicRegion;

/**
 * This class takes a SiteDataAPI object and writes it's data to a GMT XYZ file for a given region.
 * If no region is given, the applicable region for the data provider is used.
 * 
 * @author Kevin Milner
 *
 */
public class SiteDataToXYZ {
	
	public static void writeXYZ(SiteDataAPI<?> data, EvenlyGriddedGeographicRegion region,
			String fileName) throws IOException {
		writeXYZ(data, region, fileName, true);
	}
	
	public static void writeXYZ(SiteDataAPI<?> data, double gridSpacing,
			String fileName) throws IOException {
		EvenlyGriddedGeographicRegion region =
				new EvenlyGriddedGeographicRegion(data.getApplicableRegion().getRegionOutline(), gridSpacing);
		writeXYZ(data, region, fileName, true);
	}
	
	public static void writeXYZ(SiteDataAPI<?> data, EvenlyGriddedGeographicRegion region,
			String fileName, boolean latFirst) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		LocationList locs = region.getGridLocationsList();
		ArrayList<?> vals = data.getValues(locs);
		for (int i=0; i<locs.size(); i++) {
			Location loc = locs.getLocationAt(i);
			String str;
			if (latFirst)
				str = loc.getLatitude() + "\t" + loc.getLongitude() + "\t";
			else
				str = loc.getLongitude() + "\t" + loc.getLatitude() + "\t";
			
			str += vals.get(i).toString();
			
			fw.write(str + "\n");
		}
		
		fw.close();
	}

}
