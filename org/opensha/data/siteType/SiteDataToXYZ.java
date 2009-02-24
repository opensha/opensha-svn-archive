package org.opensha.data.siteType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;

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
