package org.opensha.nshmp2.tmp;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.RegionUtils;

import com.google.common.primitives.Doubles;

/**
 * Test grids for map based comparisons of OpenSHA and NSHMP fortran codes.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public enum TestGrid {

	// @formatter:off
	NATIONAL(
		new double[] {24.6, 50.0},
		new double[] {-125.0, -65.0 }),
	CA(
		new double[] {31.5, 43.0},
		new double[] {-125.4, -113.1}),
	CA_RELM(
		new CaliforniaRegions.RELM_TESTING_GRIDDED()),
	LOS_ANGELES(
		new double[] {35.15,34.23,32.94,33.86},
		new double[] {-119.07,-116.70,-117.42,-119.80}),
	LOS_ANGELES_BIG(
		new double[] {32.0,35.0},
		new double[] {-120.0,-115.0}),
	SAN_FRANCISCO(
		new double[] {37.19,36.43,38.23,39.02},
		new double[] {-120.61,-122.09,-123.61,-122.08}),
	SEATTLE(
		new double[] {46.5,48.5},
		new double[] {-123.5,-121.5}),
	SALT_LAKE_CITY(
		new double[] {39.5,42.0},
		new double[] {-113.0,-111.0}),
	MEMPHIS(
		new double[] {34.0,36.5},
		new double[] {-91.5,-89.0}),
	MEMPHIS_BIG(
		new double[] {33.0,39.0},
		new double[] {-93.0,-88.0}),
	TEST( // small 6 node test grid
		new double[] {33.0, 33.1},
		new double[] {-117.9, -118.1});
	// @formatter:on

	private static final double BOUNDS_OFFSET = 0.0;
	private double[] lats, lons;
	private GriddedRegion grid;
	
	private TestGrid(double[] lats, double[] lons) {
		this.lats = lats;
		this.lons = lons;
	}
	
	private TestGrid(GriddedRegion grid) {
		lats = new double[] {grid.getMinLat(), grid.getMaxLat()};
		lons = new double[] {grid.getMinLon(), grid.getMaxLon()};
		this.grid = grid;
	}

	/**
	 * Initialize and return the associated gridded region.
	 * 
	 * @return the grid
	 */
	public GriddedRegion grid() {
		if (grid != null) return grid;
		if (lats.length == 2) {
			return new GriddedRegion(new Location(lats[0], lons[0]),
				new Location(lats[1], lons[1]), 0.1, GriddedRegion.ANCHOR_0_0);
		}
		LocationList locs = new LocationList();
		for (int i = 0; i < lats.length; i++) {
			locs.add(new Location(lats[i], lons[i]));
		}
		return new GriddedRegion(locs, BorderType.MERCATOR_LINEAR, 0.1,
			GriddedRegion.ANCHOR_0_0);
	}

	/**
	 * Returns a bounds array of [minLat, minLon, maxLat, maxLon] that is
	 * encloses the grid. Use for setting gmt map extents.
	 * @return a bounds array
	 */
	public double[] bounds() {
		// @formatter:off
		return new double[] { 
			Doubles.min(lats) - BOUNDS_OFFSET,
			Doubles.max(lats) + BOUNDS_OFFSET,
			Doubles.min(lons) - BOUNDS_OFFSET,
			Doubles.max(lons) + BOUNDS_OFFSET};
		// @formatter:on
	}

	public static void main(String[] args) {
		for (TestGrid tg : TestGrid.values()) {
			RegionUtils.regionToKML(tg.grid(), "TEST GRID " + tg, Color.ORANGE);
		}
	}
	
	public static Set<TestGrid> getLocals() {
		return EnumSet.of(LOS_ANGELES, SAN_FRANCISCO, SEATTLE, SALT_LAKE_CITY,
			MEMPHIS);
	}

}
