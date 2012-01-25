package org.opensha.sha.earthquake.observedEarthquake.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;

public class UCERF3_CatalogParser {
	
	public static ObsEqkRupList loadCatalog(File file) throws IOException {
		ObsEqkRupList rups = new ObsEqkRupList();
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		String line;
		String[] split;
		while (in.ready()) {
			line = in.readLine();
			line = line.trim();
			line = line.replaceAll("\t", " ");
			while (line.contains("  "))
				line = line.replaceAll("  ", " ");
			split = line.split(" ");
			// FORMAT:
			// 0-Year 1-month 2-day 3-hour 4-minute 5-second 6-lat 7-long 8-depth 9-mag
			// 10-magType 11-magSource 12-magErr 13-magRounding 14-EarthquakeID
			
			int year			= Integer.parseInt(split[0]);
			int month			= Integer.parseInt(split[1]);
			int date			= Integer.parseInt(split[2]);
			int hourOfDay		= Integer.parseInt(split[3]);
			int minute			= Integer.parseInt(split[4]);
			int second			= (int)(Double.parseDouble(split[5])); // round it
			double latitude		= Double.parseDouble(split[6]);
			double longitude	= Double.parseDouble(split[7]);
			double depth		= Double.parseDouble(split[8]);
			if (Double.isNaN(depth))
				depth = 0;
			double mag			= Double.parseDouble(split[9]);
			
			// not included, but can easily be if needed. we'd need to create a subclass of ObsEqkRupture
//			int magType			= Integer.parseInt(split[10]);
//			int magSource		= Integer.parseInt(split[11]);
//			double magError		= Double.parseDouble(split[12]);
//			double magRounding	= Double.parseDouble(split[13]);
			int eventID			= (int)Double.parseDouble(split[14]);
			
			GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT-0:00"));
			cal.set(year, month-1, date, hourOfDay, minute, second);
			
			Location hypoLoc = new Location(latitude, longitude, depth);
			
			rups.add(new ObsEqkRupture(eventID+"", cal.getTimeInMillis(), hypoLoc, mag));
		}
		
		return rups;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File file = new File("/home/kevin/OpenSHA/UCERF3/UCERF3CatalogVersion1.txt");
		for (ObsEqkRupture rup : loadCatalog(file)) {
			System.out.println(rup);
		}
	}

}
