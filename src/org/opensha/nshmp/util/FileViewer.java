package org.opensha.nshmp.util;

import org.opensha.nshmp.sha.io.NEHRP_Record;

public class FileViewer {
	private static float lat;
	private static float lon;
	private static float vals[];
	private static String fileName = "/Users/emartinez/Desktop/rndFiles/2003-PRVI-Retrofit-10-050-a.rnd";
	private static String TAB = "\t";
	public static void main (String args[]) {
		NEHRP_Record rec = new NEHRP_Record();
		for ( int i = 1; i < 16267; ++i ) {
			rec.getRecord(fileName, i);
			lat = rec.getLatitude();
			lon = rec.getLongitude();
			vals = rec.getPeriods();
			System.out.println("" + i + "" + TAB + "" + lat + "" + TAB + "" + lon + "" + TAB + vals[0] + "" + TAB + "" + vals[1]);
		}
	}
}
