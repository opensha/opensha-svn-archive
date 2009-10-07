package org.opensha.commons.data.region;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensha.commons.data.region.RegionUtils.Color;

public class EvenlyGriddedGeographicRegionTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testEvenlyGriddedGeographicRegionLocationLocationDouble() {
		
		fail("Not yet implemented: test of set spacing");
	}

	@Test
	public final void testEvenlyGriddedGeographicRegionLocationListBorderTypeDouble() {
		fail("Not yet implemented");
	}

	@Test
	public final void testEvenlyGriddedGeographicRegionLocationDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	public final void testEvenlyGriddedGeographicRegionLocationListDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	public final void testEvenlyGriddedGeographicRegionGeographicRegionDouble() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetMinGridLat() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetMaxGridLat() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetMinGridLon() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetMaxGridLon() {
		fail("Not yet implemented");
	}

	@Test
	public final void testSubRegion() {
		EvenlyGriddedGeographicRegion eggr = new EvenlyGriddedGeographicRegion(
				GeographicRegionTest.circRegion, 0.5, null);
		Region gr = GeographicRegionTest.lgRectMercRegion;
		
		fail("Not yet implemented");
	}

	public static void main(String[] args) {
		
		
		EvenlyGriddedGeographicRegion sreg = new EvenlyGriddedGeographicRegion(
				GeographicRegionTest.lgRectMercRegion, 0.5, null);
		Region gr = sreg.subRegion(GeographicRegionTest.circRegion);
		RegionUtils.regionToKML(gr, "SubRegion_circRectSub", Color.RED);
		
		
		
		
		
		// The code below will generate kml files for visual verification that
		// GriddedRegions are being instantiated correctly. These files were
		// the basis of comparison when performing an overhaul of the region
		// package. See below for code to generate gridded regions prior to
		// package modification.

		EvenlyGriddedGeographicRegion eggr;
		
		// nocal
		eggr = new CaliforniaRegions.RELM_NOCAL_GRIDDED();
		RegionUtils.regionToKML(eggr, "ver_NoCal_new", Color.ORANGE);
		// relm
		eggr = new CaliforniaRegions.RELM_GRIDDED();
		RegionUtils.regionToKML(eggr, "ver_RELM_new", Color.ORANGE);
		// relm_testing
		eggr = new CaliforniaRegions.RELM_TESTING_GRIDDED();
		RegionUtils.regionToKML(eggr, "ver_RELM_testing_new", Color.ORANGE);
		// socal
		eggr = new CaliforniaRegions.RELM_SOCAL_GRIDDED();
		RegionUtils.regionToKML(eggr, "ver_SoCal_new", Color.ORANGE);
		// wg02
		eggr = new CaliforniaRegions.WG02_GRIDDED();
		RegionUtils.regionToKML(eggr, "ver_WG02_new", Color.ORANGE);
		// wg07
		eggr = new CaliforniaRegions.WG07_GRIDDED();
		RegionUtils.regionToKML(eggr, "ver_WG07_new", Color.ORANGE);
		// relm_collect
		eggr = new CaliforniaRegions.RELM_COLLECTION_GRIDDED();
		RegionUtils.regionToKML(eggr, "ver_RELM_collect_new", Color.ORANGE);
		
		
		// The code below initializes and outputs kml for the now
		// deprecated/deleted gridded region constructors. To rerun, one can
		// check out svn Revision 5832 or earlier and copy 
		// RegionUtils.regionToKML() and accessory methods to the project.
		//
		// eggr = new EvenlyGriddedNoCalRegion();
		// RegionUtils.regionToKML(eggr, "ver_NoCal_old", Color.BLUE);
		// relm
		// eggr = new EvenlyGriddedRELM_Region();
		// RegionUtils.regionToKML(eggr, "ver_RELM_old", Color.BLUE);
		// relm_testing
		// eggr = new EvenlyGriddedRELM_TestingRegion();
		// RegionUtils.regionToKML(eggr, "ver_RELM_testing_old", Color.BLUE);
		// socal
		// eggr = new EvenlyGriddedSoCalRegion();
		// RegionUtils.regionToKML(eggr, "ver_SoCal_old", Color.BLUE);
		// wg02
		// eggr = new EvenlyGriddedWG02_Region();
		// RegionUtils.regionToKML(eggr, "ver_WG02_old", Color.BLUE);
		// wg07
		// eggr = new EvenlyGriddedWG07_LA_Box_Region();
		// RegionUtils.regionToKML(eggr, "ver_WG07_old", Color.BLUE);
		// relm_collect
		// eggr = new RELM_CollectionRegion();
		// RegionUtils.regionToKML(eggr, "ver_RELM_collect_old", Color.BLUE);
	}
}
