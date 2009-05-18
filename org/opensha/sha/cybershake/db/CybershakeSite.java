package org.opensha.sha.cybershake.db;

import org.opensha.data.Location;

public class CybershakeSite {
	
	public static final int TYPE_POI = 1;
	public static final int TYPE_PRECARIOUS_ROCK = 2;
	public static final int TYPE_BROADBAND_STATION = 3;
	public static final int TYPE_TEST_SITE = 4;
	public static final int TYPE_GRID_20_KM = 5;
	public static final int TYPE_GRID_10_KM = 6;
	public static final int TYPE_GRID_05_KM = 7;
	
	public int id;
	public double lat;
	public double lon;
	public String name;
	public String short_name;
	public int type_id;
	
	public CybershakeSite(int id, double lat, double lon, String name, String short_name, int type_id) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.name = name;
		this.short_name = short_name;
		this.type_id = type_id;
	}
	
	public CybershakeSite(double lat, double lon, String name, String short_name) {
		this(-1, lat, lon, name, short_name, -1);
	}
	
	public Location createLocation() {
		return new Location(lat, lon);
	}
	
	public String toString() {
		String str = "";
		if (id >= 0)
			str += "ID: " + id + "\t";
		
		str += "Lat: " + lat + "\tLon: " + lon + "\tName: " + name + "\tABBR: " + short_name;
		
		if (type_id >= 0)
			str += "\tType ID: " + type_id;
		
		return str;
	}
	
	public String getFormattedName() {
		if (name.equals(short_name))
			return name;
		else
			return name + " (" + short_name + ")";
	}
}