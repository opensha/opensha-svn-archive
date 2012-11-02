package org.opensha.commons.data.xyz;

import java.util.HashMap;

import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;

/**
 * This is a Geohgraphic Dataset on a regular grid, as defined by a GriddedRegion. Points
 * not in the given GriddedRegion cannot be set.
 * 
 * @author kevin
 *
 */
public class GriddedGeoDataSet extends AbstractGeoDataSet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GriddedRegion region;
	double[] values;
	
	public GriddedGeoDataSet(GriddedRegion region, boolean latitudeX) {
		super(latitudeX);
		this.region = region;
		values = new double[region.getNodeCount()];
	}

	@Override
	public int size() {
		return region.getNodeCount();
	}

	@Override
	public void set(Location loc, double value) {
		int index = indexOf(loc);
		if (index < 0)
			throw new InvalidRangeException("point must exist in the gridded region!");
		values[index] = value;
	}

	@Override
	public double get(Location loc) {
		return values[indexOf(loc)];
	}

	@Override
	public int indexOf(Location loc) {
		return region.indexForLocation(loc);
	}

	@Override
	public Location getLocation(int index) {
		return region.getLocation(index);
	}

	@Override
	public boolean contains(Location loc) {
		return indexOf(loc) >= 0;
	}

	@Override
	public Object clone() {
		return copy();
	}
	
	@Override
	public GriddedGeoDataSet copy() {
		GriddedGeoDataSet data = new GriddedGeoDataSet(region, isLatitudeX());
		
		for (int i=0; i<size(); i++) {
			data.set(getLocation(i), get(i));
		}
		
		return data;
	}

	@Override
	public LocationList getLocationList() {
		return region.getNodeList();
	}
	
	public GriddedRegion getRegion() {
		return region;
	}

}
