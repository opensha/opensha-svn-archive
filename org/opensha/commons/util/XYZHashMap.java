package org.opensha.commons.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * HashMap that loads and stores the values in a Generic Mapping Tools stle XYZ files.
 * 
 * @author kevin
 *
 */
public class XYZHashMap extends HashMap<String, Double> {

	public XYZHashMap(String xyzFile) throws FileNotFoundException, IOException {
		super();
		
		ArrayList<String> lines = FileUtils.loadFile(xyzFile);
		
		for (String line : lines) {
			line = line.trim();
			if (line.length() < 2)
				continue;
			StringTokenizer tok = new StringTokenizer(line);
			double lat = Double.parseDouble(tok.nextToken());
			double lon = Double.parseDouble(tok.nextToken());
			double val = Double.parseDouble(tok.nextToken());
			
			this.put(lat, lon, val);
		}
	}
	
	public double get(double lat, double lon) {
		String key = this.keyGen(lat, lon);
		return this.get(key);
	}
	
	public void put(double lat, double lon, double val) {
		String key = this.keyGen(lat, lon);
		
		this.put(key, val);
	}
	
	private String keyGen(double lat, double lon) {
		return lat + "_" + lon;
	}
}
