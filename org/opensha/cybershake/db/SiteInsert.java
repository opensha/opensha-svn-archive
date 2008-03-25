package org.opensha.cybershake.db;

public class SiteInsert {
	double lat;
	double lon;
	String name;
	String short_name;
	
	public SiteInsert(double lat, double lon, String name, String short_name) {
		this.lat = lat;
		this.lon = lon;
		this.name = name;
		this.short_name = short_name;
	}
	
	public String toString() {
		return "Lat: " + lat + "\tLon: " + lon + "\tName: " + name + "\tABBR: " + short_name;
	}
}