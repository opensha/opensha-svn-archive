package org.opensha.commons.data.xyz;

import java.util.ArrayList;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;

public class ArrayBasedGeoDataSet extends AbstractGeoDataSet {
	
	private Location[] locs;
	private double[] vals;
	
	LocationList locList = new LocationList();
	
	int highestSet = -1;

	public ArrayBasedGeoDataSet(boolean latitudeX, int size) {
		super(latitudeX);
		locs = new Location[size];
		vals = new double[size];
		
		for (int i=0; i<size; i++)
			locList.add(null);
	}
	
	public void set(int index, Location loc, double value) {
		locs[index] = loc;
		vals[index] = value;
		locList.set(index, loc);
	}

	@Override
	public void set(Location loc, double value) {
		int index = indexOf(loc);
		if (index >= 0) {
			locs[index] = loc;
			locList.set(index, loc);
			vals[index] = value;
		} else {
			highestSet++;
			locs[highestSet] = loc;
			vals[highestSet] = value;
		}
	}

	@Override
	public double get(Location loc) {
		return vals[indexOf(loc)];
	}

	@Override
	public int indexOf(Location loc) {
		for (int i=0; i<locs.length; i++)
			if (locs[i] != null && locs[i].equals(loc))
				return i;
		return -1;
	}

	@Override
	public Location getLocation(int index) {
		return locs[index];
	}

	@Override
	public boolean contains(Location loc) {
		return indexOf(loc)>=0;
	}

	@Override
	public LocationList getLocationList() {
		return locList;
	}

	@Override
	public GeoDataSet copy() {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return locs.length;
	}

	@Override
	public void set(int index, double z) {
		vals[index] = z;
	}

	@Override
	public double get(int index) {
		return vals[index];
	}

}
