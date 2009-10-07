package org.opensha.commons.data.region;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.RegionUtils.Color;

public class GeographicRegionTest {
	
	// TODO need to test immutability of border

	// Notes:
	// ===============================================================
	// Don't need to test Dateline-spanning and pole-wrapping cases as
	// they've been declared as unsupported in docs.
	//
	// The 'region creation' parts of the constructor tests were built by 
	// creating a kml output file to visually verify correct border creation.
	// The vertices were then culled from the KML file and are stored in 
	// static arrays at the end of this file as.
	// ===============================================================
	
	// octagonal region
	static Region octRegion;
	// small rect region (regionLocLoc)
	static Region smRectRegion;
	// large rect region (regionLocListBorderType)
	static Region lgRectMercRegion;
	// large rect region (regionLocListBorderType)
	static Region lgRectGCRegion;
	// buffered region (regionLocListDouble)
	static Region buffRegion;
	// circular region (regionLocDouble)
	static Region circRegion;
	// cicle-lgRect intersect
	static Region circLgRectIntersect;
	// cicle-lgRect union
	static Region circLgRectUnion;
	// smRect-lgRect intersect
	static Region smRectLgRectIntersect;
	// smRect-lgRect union
	static Region smRectLgRectUnion;
	// circle-smRect intersect
	static Region circSmRectIntersect;
	// circle-smRect union
	static Region circSmRectUnion;
	
	
	// static initializer is used for this test class because main(), which
	// is used to generate kml files for visual verification of results and
	// and as coordinate data source for tests
	static {
		LocationList ll = new LocationList();
		ll.addLocation(new Location(25,-115));
		ll.addLocation(new Location(25,-110));
		ll.addLocation(new Location(30,-105));
		ll.addLocation(new Location(35,-105));
		ll.addLocation(new Location(40,-110));
		ll.addLocation(new Location(40,-115));
		ll.addLocation(new Location(35,-120));
		ll.addLocation(new Location(30,-120));
		octRegion  = new Region(ll, null);
		
		Location a = new Location(39,-117);
		Location b = new Location(41,-113);
		smRectRegion = new Region(a,b);

		ll = new LocationList();
		ll.addLocation(new Location(35,-125));
		ll.addLocation(new Location(35,-105));
		ll.addLocation(new Location(45,-105));
		ll.addLocation(new Location(45,-125));
		lgRectMercRegion = new Region(ll, BorderType.MERCATOR_LINEAR);
		lgRectGCRegion = new Region(ll, BorderType.GREAT_CIRCLE);

		Location center = new Location(35, -125);
		circRegion = new Region(center, 400);
		
		ll = new LocationList();
		ll.addLocation(new Location(35,-125));
		ll.addLocation(new Location(42,-119));
		ll.addLocation(new Location(40,-113));
		ll.addLocation(new Location(45,-105));
		buffRegion = new Region(ll,100);
		
		// unions and intersections
		circLgRectIntersect = Region.intersect(lgRectMercRegion, circRegion);
		circLgRectUnion = Region.union(lgRectMercRegion, circRegion);
		smRectLgRectIntersect = Region.intersect(lgRectMercRegion, smRectRegion);
		smRectLgRectUnion = Region.union(lgRectMercRegion, smRectRegion);
		circSmRectIntersect = Region.intersect(circRegion, smRectRegion);
		circSmRectUnion = Region.intersect(circRegion, smRectRegion);
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGeographicRegionLocationLocation() {
		
		// initialization tests
		Location L1 = new Location(32,112);
		Location L2 = new Location(32,118);
		Location L3 = new Location(34,118);
		try {
			Region gr = new Region(L1,L2);
			fail("Same lat values not caught");
		} catch (IllegalArgumentException iae) {}
		try {
			Region gr = new Region(L2,L3);
			fail("Same lon values not caught");
		} catch (IllegalArgumentException iae) {}
		try {
			L1 = null;
			L2 = null;
			Region gr = new Region(L1,L2);
			fail("Null argument not caught");
		} catch (NullPointerException npe) {}
		
		// region creation tests
		LocationList ll1 = smRectRegion.getRegionOutline();
		LocationList ll2 = createLocList(regionLocLocDat);
		assertTrue(ll1.compareTo(ll2) == 0);
	}

	@Test
	public final void testGeographicRegionLocationListBorderType() {
		// null args
		LocationList ll = new LocationList();
		try {
			ll = null;
			Region gr = new Region(
					ll, BorderType.MERCATOR_LINEAR);
			fail("Null argument not caught");
		} catch (NullPointerException npe) {}
		
		// too short location list
		ll = new LocationList();
		ll.addLocation(new Location(35,-125));
		ll.addLocation(new Location(35,-75));
		try {
			Region gr = new Region(ll, null);
			fail("Location list too short  not caught");
		} catch (IllegalArgumentException iae) {}
		
		// check that start point repeated at end of list is removed
		ll.addLocation(new Location(45,-75));
		ll.addLocation(new Location(35,-125));
		Region rectRegionStartRepeat = new Region(ll, null);
		assertTrue("Repeated start point not clipped",
				rectRegionStartRepeat.getRegionOutline().size() == 3);
		
		// region creation test
		LocationList ll1 = lgRectMercRegion.getRegionOutline();
		LocationList ll2 = createLocList(regionLocListMercatorDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		
		ll1 = lgRectGCRegion.getRegionOutline();
		ll2 = createLocList(regionLocListGreatCircleDat);
		assertTrue(ll1.compareTo(ll2) == 0);
	}

	@Test
	public final void testGeographicRegionLocationDouble() {
		Location L1 = new Location();
		try {
			L1 = null;
			Region gr = new Region(L1, 50);
			fail("Null argument not caught");
		} catch (NullPointerException npe) {}
		try {
			L1 = new Location();
			Region gr = new Region(L1, 1001);
			fail("Radius too high not caught");
		} catch (IllegalArgumentException iae) {}
		try {
			L1 = new Location();
			Region gr = new Region(L1, 0);
			fail("Radius too low not caught");
		} catch (IllegalArgumentException iae) {}
		
		// region creation test
		LocationList ll1 = circRegion.getRegionOutline();
		LocationList ll2 = createLocList(regionCircularDat);
		assertTrue(ll1.compareTo(ll2) == 0);
	}

	@Test
	public final void testGeographicRegionLocationListDouble() {
		LocationList ll = new LocationList();
		try {
			Region gr = new Region(ll, 50);
			fail("Empty location list not caught");
		} catch (IllegalArgumentException iae) {}
		ll.addLocation(new Location());
		try {
			Region gr = new Region(ll, 501);
			fail("Buffer too high not caught");
		} catch (IllegalArgumentException iae) {}
		try {
			Region gr = new Region(ll, 0);
			fail("Buffer too low not caught");
		} catch (IllegalArgumentException iae) {}
		ll = null;
		try {
			Region gr = new Region(ll, 50);
			fail("Null argument not caught");
		} catch (NullPointerException npe) {}

		// region creation test
		LocationList ll1 = buffRegion.getRegionOutline();
		LocationList ll2 = createLocList(regionBufferDat);
		assertTrue(ll1.compareTo(ll2) == 0);
	}

	@Test
	public final void testGeographicRegionGeographicRegion() {
		assertTrue("No test needed", true);
	}

	@Test
	public final void testIsLocationInside() {
		
		// insidedness testing is largely unnecessary as we can assume
		// java.awt.Area handles contains correctly. We do want to check
		// that great circle borders are being created correctly and
		// test that here.
				
		Location containsLoc1 = new Location(35.1,-115); // bottom edge
		Location containsLoc2 = new Location(45.1,-115); // top edge
		
		// mercator
		assertTrue(lgRectMercRegion.isLocationInside(containsLoc1));
		assertTrue(!lgRectMercRegion.isLocationInside(containsLoc2));
		// great circle
		assertTrue(!lgRectGCRegion.isLocationInside(containsLoc1));
		assertTrue(lgRectGCRegion.isLocationInside(containsLoc2));

		// also need to test that the small offset added to 'rectangular'
		// regions leads to inclusion of points that fall on the north and
		// east borders; also check points on the south and west to be safe
		Region rectRegionLocLoc = new Region(
				new Location(35,-105), new Location(45,-125));
		Location containsEloc = new Location(40,-105);
		Location containsNloc = new Location(45,-115);
		Location containsSloc = new Location(35,-115);
		Location containsWloc = new Location(40,-125);
		
		assertTrue(rectRegionLocLoc.isLocationInside(containsEloc));
		assertTrue(rectRegionLocLoc.isLocationInside(containsNloc));
		assertTrue(rectRegionLocLoc.isLocationInside(containsSloc));
		assertTrue(rectRegionLocLoc.isLocationInside(containsWloc));

		assertTrue(!lgRectMercRegion.isLocationInside(containsEloc));
		assertTrue(!lgRectMercRegion.isLocationInside(containsNloc));
		assertTrue(lgRectMercRegion.isLocationInside(containsSloc));
		assertTrue(lgRectMercRegion.isLocationInside(containsWloc));
	}

	@Test
	public final void testGetMinLat() {
		assertEquals(25, octRegion.getMinLat(), 0);
	}

	@Test
	public final void testGetMaxLat() {
		assertEquals(40, octRegion.getMaxLat(), 0);
	}

	@Test
	public final void testGetMinLon() {
		assertEquals(-120, octRegion.getMinLon(), 0);
	}

	@Test
	public final void testGetMaxLon() {
		assertEquals(-105, octRegion.getMaxLon(), 0);
	}

	@Test
	public final void testGetGlobalRegion() {
		Region global = Region.getGlobalRegion();
		assertEquals(180, global.getMaxLon(), 0);
		assertEquals(-180, global.getMinLon(), 0);
		assertEquals(90, global.getMaxLat(), 0);
		assertEquals(-90, global.getMinLat(), 0);
	}

	@Test
	public final void testIntersect() {
		LocationList ll1, ll2;
		// partial overlap
		ll1 = circLgRectIntersect.getRegionOutline();
		ll2 = createLocList(regionCircRectIntersectDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		// full overlap - this could be tested by matching a statically 
		// defined region using getRegionOutline(), however, Area operations
		// have a tendency to change the winding direction of border in which
		// case LocatonList.compareTo(LocationList) will fail, even though
		// the borders polygons are the same
		ll1 = smRectLgRectIntersect.getRegionOutline();
		ll2 = createLocList(regionSmRectLgRectIntersectDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		// no overlap
		assertTrue(circSmRectIntersect == null);
	}
	
	@Test
	public final void testUnion() {
		LocationList ll1, ll2;
		// partial overlap
		ll1 = circLgRectUnion.getRegionOutline();
		ll2 = createLocList(regionCircRectUnionDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		// full overlap - this could be tested by matching a statically 
		// defined region using getRegionOutline(), however, Area operations
		// have a tendency to change the winding direction of border in which
		// case LocatonList.compareTo(LocationList) will fail, even though
		// the borders polygons are the same
		ll1 = smRectLgRectUnion.getRegionOutline();
		ll2 = createLocList(regionSmRectLgRectUnionDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		// no overlap
		assertTrue(circSmRectUnion == null);
}

	// utility method to create LocationList from data arrays
	private static LocationList createLocList(double[] data) {
		LocationList locList = new LocationList();
		for (int i=0; i<data.length; i+=3) {
			Location loc = new Location(data[i+1], data[i], data[i+2]);
			locList.addLocation(loc);
		}
		return locList;
	}

	public static void main(String[] args) {
		// The code below was used to create KML files for visual verification
		// of regions. The border vertices were then culled from the KML and 
		// are stored in arrays (below) for use in this test class
		Region gr;
		
		// RECT
		RegionUtils.regionToKML(smRectRegion, "RegionLocLoc", Color.ORANGE);
		
		// LOCATION LIST border - mercator and great circle
		RegionUtils.regionToKML(lgRectMercRegion, "RegionLocListMercator", Color.ORANGE);
		RegionUtils.regionToKML(lgRectGCRegion, "RegionLocListGreatCircle", Color.ORANGE);

		// CIRCLE
		RegionUtils.regionToKML(circRegion, "RegionLocDouble", Color.ORANGE);
		
		// BUFFER
		RegionUtils.regionToKML(buffRegion,"RegionLocListDouble",Color.ORANGE);

		// CIRCLE-RECT INTERSECT and UNION
		RegionUtils.regionToKML(circLgRectIntersect,"RegionCircleRectIntersect",Color.ORANGE);
		RegionUtils.regionToKML(circLgRectUnion,"RegionCircleRectUnion",Color.ORANGE);
		RegionUtils.regionToKML(smRectLgRectIntersect,"RegionSmRectLgRectIntersect",Color.ORANGE); 
		RegionUtils.regionToKML(smRectLgRectUnion,"RegionSmRectLgRectUnion",Color.ORANGE);
	}
	
	/* debugging utility method to read Area coordinates */
	private static void readArea(Area area) {
		PathIterator pi = area.getPathIterator(null);
		double[] vertex = new double[6];
		while (!pi.isDone()) {
			pi.currentSegment(vertex);
			System.out.println("AreaCoord: " + vertex[1] + " " + vertex[0]);
			pi.next();
		}
	}
	
	// note: always strip the last set of coordinates; they are required to
	// close the kml polygons but noot needed internally for the region
	// class to define a border shape
	private static double[] regionLocLocDat = new double[] {
		-117.0,39.0,0.0,
		-112.99999,39.0,0.0,
		-112.99999,41.00001,0.0,
		-117.0,41.00001,0.0};
	
	private static double[] regionLocListMercatorDat = new double[] {
		-125.0,35.0,0.0,
		-105.0,35.0,0.0,
		-105.0,45.0,0.0,
		-125.0,45.0,0.0};
	
	private static double[] regionLocListGreatCircleDat = new double[] {
		-125.0,45.0,0.0,
		-125.0,44.10068,0.0,
		-125.0,43.20136,0.0,
		-125.0,42.30204,0.0,
		-125.0,41.40272,0.0,
		-125.0,40.5034,0.0,
		-125.0,39.60408,0.0,
		-125.0,38.70476,0.0,
		-125.0,37.80544,0.0,
		-125.0,36.90611,0.0,
		-125.0,36.00679,0.0,
		-125.0,35.10747,0.0,
		-125.0,35.0,0.0,
		-123.90654,35.08559,0.0,
		-122.81091,35.16135,0.0,
		-121.71337,35.22722,0.0,
		-120.61419,35.28318,0.0,
		-119.51362,35.32918,0.0,
		-118.41193,35.36519,0.0,
		-117.3094,35.3912,0.0,
		-116.20629,35.40719,0.0,
		-115.10288,35.41314,0.0,
		-113.99945,35.40906,0.0,
		-112.89626,35.39494,0.0,
		-111.7936,35.3708,0.0,
		-110.69173,35.33665,0.0,
		-109.59094,35.29251,0.0,
		-108.49147,35.23841,0.0,
		-107.39361,35.17438,0.0,
		-106.29761,35.10047,0.0,
		-105.20372,35.0167,0.0,
		-105.0,35.0,0.0,
		-105.0,35.89932,0.0,
		-105.0,36.79864,0.0,
		-105.0,37.69796,0.0,
		-105.0,38.59728,0.0,
		-105.0,39.4966,0.0,
		-105.0,40.39592,0.0,
		-105.0,41.29524,0.0,
		-105.0,42.19456,0.0,
		-105.0,43.09389,0.0,
		-105.0,43.99321,0.0,
		-105.0,44.89253,0.0,
		-105.0,45.0,0.0,
		-106.26441,45.1043,0.0,
		-107.53314,45.1946,0.0,
		-108.80559,45.27081,0.0,
		-110.08114,45.33286,0.0,
		-111.35917,45.38068,0.0,
		-112.63904,45.41422,0.0,
		-113.92011,45.43346,0.0,
		-115.20172,45.43837,0.0,
		-116.48323,45.42895,0.0,
		-117.76399,45.40521,0.0,
		-119.04335,45.36716,0.0,
		-120.32067,45.31486,0.0,
		-121.59531,45.24835,0.0,
		-122.86665,45.16769,0.0,
		-124.13409,45.07297,0.0};
	
	private static double[] regionCircularDat = new double[] {
		-125.0,38.59728,0.0,
		-124.20188,38.54006,0.0,
		-123.43155,38.37041,0.0,
		-122.71545,38.09424,0.0,
		-122.0774,37.72108,0.0,
		-121.53764,37.26362,0.0,
		-121.11214,36.73712,0.0,
		-120.81225,36.15877,0.0,
		-120.64461,35.54704,0.0,
		-120.61135,34.92099,0.0,
		-120.71044,34.29968,0.0,
		-120.93605,33.70161,0.0,
		-121.27912,33.14421,0.0,
		-121.72776,32.64345,0.0,
		-122.26778,32.21343,0.0,
		-122.88308,31.8661,0.0,
		-123.5561,31.61102,0.0,
		-124.26819,31.45515,0.0,
		-125.0,31.40272,0.0,
		-125.73181,31.45515,0.0,
		-126.4439,31.61102,0.0,
		-127.11692,31.8661,0.0,
		-127.73222,32.21343,0.0,
		-128.27224,32.64345,0.0,
		-128.72088,33.14421,0.0,
		-129.06395,33.70161,0.0,
		-129.28956,34.29968,0.0,
		-129.38865,34.92099,0.0,
		-129.35539,35.54704,0.0,
		-129.18775,36.15877,0.0,
		-128.88786,36.73712,0.0,
		-128.46236,37.26362,0.0,
		-127.9226,37.72108,0.0,
		-127.28455,38.09424,0.0,
		-126.56845,38.37041,0.0,
		-125.79812,38.54006,0.0};
	
	private static double[] regionBufferDat = new double[] {
		-125.18862,34.1142,0.0,
		-125.37168,34.15435,0.0,
		-125.54379,34.21995,0.0,
		-125.69983,34.30907,0.0,
		-125.83512,34.41906,0.0,
		-125.94558,34.54667,0.0,
		-126.02777,34.68808,0.0,
		-126.07909,34.83906,0.0,
		-126.09782,34.99506,0.0,
		-126.08322,35.15136,0.0,
		-126.03552,35.30319,0.0,
		-125.95603,35.44592,0.0,
		-125.93395,35.47209,0.0,
		-125.93627,35.47328,0.0,
		-120.05746,42.44155,0.0,
		-120.05545,42.44485,0.0,
		-120.04683,42.45415,0.0,
		-119.99009,42.52141,0.0,
		-119.98637,42.51942,0.0,
		-119.93553,42.57429,0.0,
		-119.7864,42.68625,0.0,
		-119.6126,42.77722,0.0,
		-119.4195,42.84433,0.0,
		-119.21312,42.88546,0.0,
		-119.0,42.89932,0.0,
		-118.78688,42.88546,0.0,
		-118.5805,42.84433,0.0,
		-118.54485,42.83194,0.0,
		-118.5438,42.83388,0.0,
		-113.18982,41.04478,0.0,
		-105.85659,45.66514,0.0,
		-105.82748,45.68596,0.0,
		-105.80865,45.69534,0.0,
		-105.78448,45.71057,0.0,
		-105.78223,45.70851,0.0,
		-105.64471,45.77704,0.0,
		-105.44153,45.84424,0.0,
		-105.22434,45.88544,0.0,
		-105.0,45.89932,0.0,
		-104.77566,45.88544,0.0,
		-104.55847,45.84424,0.0,
		-104.35529,45.77704,0.0,
		-104.17252,45.68596,0.0,
		-104.01581,45.57387,0.0,
		-103.8899,45.44431,0.0,
		-103.7985,45.30131,0.0,
		-103.74417,45.1493,0.0,
		-103.72827,44.99294,0.0,
		-103.75099,44.83701,0.0,
		-103.81132,44.68623,0.0,
		-103.90718,44.5451,0.0,
		-104.03546,44.41784,0.0,
		-104.19218,44.30821,0.0,
		-104.23696,44.28617,0.0,
		-104.23481,44.2842,0.0,
		-112.20786,39.33894,0.0,
		-112.21034,39.34098,0.0,
		-112.2529,39.30867,0.0,
		-112.4196,39.21971,0.0,
		-112.60336,39.15423,0.0,
		-112.79874,39.11417,0.0,
		-113.0,39.10068,0.0,
		-113.20126,39.11417,0.0,
		-113.39664,39.15423,0.0,
		-113.50274,39.19204,0.0,
		-113.5046,39.18908,0.0,
		-118.55087,40.86722,0.0,
		-124.07458,34.51961,0.0,
		-124.07684,34.52077,0.0,
		-124.16488,34.41906,0.0,
		-124.30017,34.30907,0.0,
		-124.45621,34.21995,0.0,
		-124.62832,34.15435,0.0,
		-124.81138,34.1142,0.0,
		-125.0,34.10068,0.0};
	
	private static double[] regionCircRectIntersectDat = new double[] {
		-125.0,35.0,0.0,
		-125.0,38.59728,0.0,
		-124.20188,38.54006,0.0,
		-123.43155,38.37041,0.0,
		-122.71545,38.09424,0.0,
		-122.0774,37.72108,0.0,
		-121.53764,37.26362,0.0,
		-121.11214,36.73712,0.0,
		-120.81225,36.15877,0.0,
		-120.64461,35.54704,0.0,
		-120.61555,35.0,0.0};

	private static double[] regionCircRectUnionDat = new double[] {
		-125.73181,31.45515,0.0,
		-126.4439,31.61102,0.0,
		-127.11692,31.8661,0.0,
		-127.73222,32.21343,0.0,
		-128.27225,32.64345,0.0,
		-128.72089,33.14421,0.0,
		-129.06395,33.70161,0.0,
		-129.28957,34.29968,0.0,
		-129.38866,34.92099,0.0,
		-129.35539,35.54704,0.0,
		-129.18774,36.15877,0.0,
		-128.88786,36.73712,0.0,
		-128.46236,37.26362,0.0,
		-127.9226,37.72108,0.0,
		-127.28455,38.09424,0.0,
		-126.56845,38.37041,0.0,
		-125.79812,38.54006,0.0,
		-125.0,38.59728,0.0,
		-125.0,45.0,0.0,
		-105.0,45.0,0.0,
		-105.0,35.0,0.0,
		-120.61555,35.0,0.0,
		-120.61135,34.92099,0.0,
		-120.71044,34.29968,0.0,
		-120.93605,33.70161,0.0,
		-121.27912,33.14421,0.0,
		-121.72776,32.64345,0.0,
		-122.26778,32.21343,0.0,
		-122.88308,31.8661,0.0,
		-123.5561,31.61102,0.0,
		-124.26819,31.45515,0.0,
		-125.0,31.40272,0.0};
	
	private static double[] regionSmRectLgRectIntersectDat = new double[] {
		-117.0,39.0,0.0,
		-117.0,41.00001,0.0,
		-112.99999,41.00001,0.0,
		-112.99999,39.0,0.0};
	
	private static double[] regionSmRectLgRectUnionDat = new double[] {
		-125.0,35.0,0.0,
		-125.0,45.0,0.0,
		-105.0,45.0,0.0,
		-105.0,35.0,0.0};
}
