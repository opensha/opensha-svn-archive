package org.opensha.commons.data.xyz;

import org.opensha.commons.geo.Location;

public interface GeographicDataSetAPI extends XYZ_DataSetAPI {
	
	public boolean isLatitudeX();
	
	public void set(Location loc, double value);
	
	public double get(Location loc);
	
	public int indexOf(Location loc);
	
	public Location getLocation(int index);
	
	public boolean contains(Location loc);

}
