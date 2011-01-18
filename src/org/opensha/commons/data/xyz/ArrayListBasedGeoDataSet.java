package org.opensha.commons.data.xyz;

import java.util.ArrayList;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;

public class ArrayListBasedGeoDataSet extends AbstractGeoDataSet {
	
	private LocationList locs;
	private ArrayList<Double> vals;

	public ArrayListBasedGeoDataSet(boolean latitudeX) {
		super(latitudeX);
		locs = new LocationList();
		vals = new ArrayList<Double>();
	}

	@Override
	public void set(Location loc, double value) {
//		if (con)
		int index = locs.indexOf(loc);
		if (index < 0) {
			locs.add(loc);
			vals.add(value);
		} else {
			locs.set(index, loc);
			vals.set(index, value);
		}
	}

	@Override
	public double get(Location loc) {
		return vals.get(indexOf(loc));
	}

	@Override
	public int indexOf(Location loc) {
		return locs.indexOf(loc);
	}

	@Override
	public Location getLocation(int index) {
		return locs.get(index);
	}

	@Override
	public boolean contains(Location loc) {
		return locs.contains(loc);
	}

	@Override
	public LocationList getLocationList() {
		return locs;
	}

	@Override
	public GeoDataSet copy() {
		ArrayListBasedGeoDataSet data = new ArrayListBasedGeoDataSet(isLatitudeX());
		for (int i=0; i<size(); i++)
			data.set(locs.get(i), vals.get(i));
		return null;
	}

	@Override
	public int size() {
		return locs.size();
	}

	@Override
	public void set(int index, double z) {
		vals.set(index, z);
	}

	@Override
	public double get(int index) {
		return vals.get(index);
	}

}
