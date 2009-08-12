package org.opensha.commons.data.region;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

public class GeographicRegionTest {
	
	// don't need to test Dateline spaning and pole-wrapping cases as they've
	// been declared as unsupported in docs.
	
	// too short location list
	private LocationList shorty;
	
	// large rect region
	private GeographicRegion rectRegion1;
	
	// large rect region initialized with great circle borders
	private GeographicRegion rectRegion2;

	// large rect region with repeating start point
	private GeographicRegion rectRegion3;
	
	
	
	@Before
	public void setUp() throws Exception {
		
		shorty = new LocationList();
		shorty.addLocation(new Location(35,-125));
		shorty.addLocation(new Location(35,-90));
		
		LocationList ll = new LocationList();
		ll.addLocation(new Location(35,-125));
		ll.addLocation(new Location(35,-90));
		ll.addLocation(new Location(45,-90));
		ll.addLocation(new Location(45,-125));
		rectRegion1 = new GeographicRegion(ll, BorderType.MERCATOR_LINEAR);
		
		rectRegion2 = new GeographicRegion(ll, BorderType.GREAT_CIRCLE);
		
		ll.addLocation(new Location(35,-125));
		rectRegion3 = new GeographicRegion(ll, BorderType.MERCATOR_LINEAR);
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGeographicRegionDoubleDoubleDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGeographicRegionLocationListBorderType() {
		// too short a LocationList
		try {
			GeographicRegion gr = new GeographicRegion(shorty, null);
			fail("IllegalArgumentException not caught");
		} catch (IllegalArgumentException iae) {}
		// defaulting to MERCATOR_LINEAR border
		GeographicRegion gr = new GeographicRegion(shorty, null);
	}

	@Test
	public final void testGeographicRegionLocationDouble() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGeographicRegionLocationListDouble() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGeographicRegionGeographicRegion() {
		fail("Not yet implemented");
	}

	@Test
	public final void testIsLocationInside() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetMinLat() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetMaxLat() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetMinLon() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetMaxLon() {
		fail("Not yet implemented");
	}

}
