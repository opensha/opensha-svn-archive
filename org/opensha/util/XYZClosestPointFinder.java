package org.opensha.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class XYZClosestPointFinder {
	ArrayList<double[]> vals;
	
	public XYZClosestPointFinder(String fileName) throws FileNotFoundException, IOException {
		
		ArrayList<String> lines = null;
		lines = FileUtils.loadFile(fileName);
		
		vals = new ArrayList<double[]>();
		
		for (String line : lines) {
			line = line.trim();
			if (line.length() < 2)
				continue;
			StringTokenizer tok = new StringTokenizer(line);
			double lat = Double.parseDouble(tok.nextToken());
			double lon = Double.parseDouble(tok.nextToken());
			double val = Double.parseDouble(tok.nextToken());
			double doub[] = new double[3];
			doub[0] = lat;
			doub[1] = lon;
			doub[2] = val;
			vals.add(doub);
		}
	}
	
	public double getClosestVal(double lat, double lon) {
		return getClosestVal(lat, lon, Double.MAX_VALUE);
	}
	
	public double getClosestVal(double lat, double lon, double tolerance) {
		double closest = Double.MAX_VALUE;
		double closeVal = 0;
		
		for (double val[] : vals) {
			double dist = Math.pow(val[0] - lat, 2) + Math.pow(val[1] - lon, 2);
			if (dist < closest) {
				closest = dist;
				closeVal = val[2];
			}
		}
		
		if (closest < tolerance)
			return closeVal;
		else
			return Double.NaN;
	}
}