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
public class GriddedRegionDataSet extends AbstractGeographicDataSet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GriddedRegion region;
	private LocationList nodeList;
	private HashMap<Location, Double> map;
	
	public GriddedRegionDataSet(GriddedRegion region, boolean latitudeX) {
		super(latitudeX);
		this.region = region;
		nodeList = region.getNodeList();
		map = new HashMap<Location, Double>();
	}

	@Override
	public int size() {
		return region.getNodeCount();
	}

	@Override
	public void set(Location loc, double value) {
		if (!contains(loc))
			throw new InvalidRangeException("point must exist in the gridded region!");
		map.put(loc, value);
	}

	@Override
	public double get(Location loc) {
		return map.get(loc);
	}

	@Override
	public int indexOf(Location loc) {
		return nodeList.indexOf(loc);
	}

	@Override
	public Location getLocation(int index) {
		return nodeList.get(index);
	}

	@Override
	public boolean contains(Location loc) {
		return nodeList.contains(loc);
	}

	@Override
	public Object clone() {
		GriddedRegionDataSet data = new GriddedRegionDataSet(region, isLatitudeX());
		
		for (int i=0; i<size(); i++) {
			data.set(getLocation(i), get(i));
		}
		
		return data;
	}

	@Override
	public LocationList getLocationList() {
		return nodeList;
	}

}
