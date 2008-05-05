package org.opensha.cybershake.db;

public class SiteInsert {
	int id;
	double lat;
	double lon;
	String name;
	String short_name;
	
	public SiteInsert(int id, double lat, double lon, String name, String short_name) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.name = name;
		this.short_name = short_name;
	}
	
	public SiteInsert(double lat, double lon, String name, String short_name) {
		this(-1, lat, lon, name, short_name);
	}
	
	public String toString() {
		if (id > 0)
			return "ID: " + id + " Lat: " + lat + "\tLon: " + lon + "\tName: " + name + "\tABBR: " + short_name;
		else
			return "Lat: " + lat + "\tLon: " + lon + "\tName: " + name + "\tABBR: " + short_name;
	}
}