package util;

import org.opensha.commons.geo.Location;

/**
 * List of 34 city sites in regions of the United States of greatest seismic
 * risk as specified in the 2009 edition of the <a
 * href="http://www.fema.gov/library/viewRecord.do?id=4103" target=_blank">NEHRP
 * Recommended Seismic Provisions</a>
 * 
 * @author Peter Powers
 * @version $Id:$
 */
@SuppressWarnings("all")
public enum NEHRP_TestCity {

	// SoCal
	LOS_ANGELES(34.05,-118.25),
	CENTURY_CITY(34.05,-118.40),
	NORTHRIDGE(34.20,-118.55),
	LONG_BEACH(33.80,-118.20),
	IRVINE(33.65,-117.80),
	RIVERSIDE(33.95,-117.40),
	SAN_BERNARDINO(34.10,-117.30),
	SAN_LUIS_OBISPO(35.30,-120.65),
	SAN_DIEGO(32.70,-117.15),
	SANTA_BARBARA(34.45,-119.70),
	VENTURA(34.30,-119.30),
	
	OAKLAND(37.80,-122.25),
	CONCORD(37.95,-122.00),
	MONTEREY(36.60,-121.90),
	SACRAMENTO(38.60,-121.50),
	SAN_FRANCISCO(37.75,-122.40),
	SAN_MATEO(37.55,-122.30),
	SAN_JOSE(37.35,-121.90),
	SANTA_CRUZ(36.95,-122.05),
	VALLEJO(38.10,-122.25),
	SANTA_ROSA(38.45,-122.70),
	
	SEATTLE(47.60,-122.30),
	TACOMA(47.25,-122.45),
	EVERETT(48.00,-122.20),
	PORTLAND(45.50,-122.65),
	
	SALT_LAKE_CITY(40.75,-111.90),
	BOISE(43.60,-116.20),
	RENO(39.55,-119.80),
	LAS_VEGAS(36.20,-115.15),
	
	ST_LOUIS(38.60,-90.20),
	MEMPHIS(35.15,-90.05),
	CHARLESTON(32.80,-79.95),
	CHICAGO(41.85,-87.65),
	NEW_YORK(40.75,-74.00);
	
	private Location loc;
	private NEHRP_TestCity(double lat, double lon) {
		loc = new Location(lat, lon);
	}
	
	/**
	 * Returns the geographic <code>Location</code> of the city.
	 * @return the <code>Location</code> of the city
	 */
	public Location location() {
		return loc;
	}
	
}
