package org.opensha.commons.calc;
import static org.opensha.commons.calc.RelativeLocation.*;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opensha.commons.data.Location;

//@SuppressWarnings("all")
public class RelativeLocationTest {
	
	// short-range, small-angle test points
	Location L1 = new Location(32.6, 20.4);
	Location L2 = new Location(32.4, 20);
	Location L3 = new Location(32.2, 20.6);
	Location L4 = new Location(32, 20.2);
	
	// polar and long-distance, large-angle test points
	Location L5 = new Location(90, 0);
	Location L6 = new Location(-90, 0);
	
	// Expected results from methods in this class were computed using the
	// class methods and compared to the results provided by one or more
	// reputable online calculators.
	
	//    p2p: L1 to L2 eg
	//     vd: Vincenty distance (very accurate, provided for comparison)
	//     sd: expected values of surfaceDistance()
	//    fsd: expected values of fastSurfaceDistance()
	//  angle: in radians between points
	// az-rad: azimuth in radians from L1 to L2
	// az-deg: azimuth in degrees from L1 to L2
	
	
	// p2p	vd (km)		sd			fsd			angle		az-rad		az-deg
	// ---	---------	---------	---------	-----------	-----------	---------
	// d51	 6393.578	 6382.596	 6474.888	1.001818991	3.141592654	180.0
	// d25	 6415.757	 6404.835	 6493.824	1.005309649	0.0			  0.0
	// d46	13543.818	13565.796	13707.303	2.129301687	3.141592654	180.0
	// d63	13565.996	13588.035	13735.216	2.132792346	0.0			  0.0
	
	// d12	43.645957	43.6090311	43.6090864  0.006844919 4.179125015 239.44623
	// d13	48.183337	48.2790582	48.2790921	0.007577932	2.741190313 157.05864
	// d14	69.150258	69.3145862	69.3146382	0.010879690 3.417161139 195.78891
	// d23	60.706703	60.6198752	60.6200022	0.009514959	1.943625801 111.36156
	// d42	48.198212	48.2952067	48.2952403	0.007580467	5.883856933	337.12017
	// d43	43.787840	43.7518411	43.7518956	0.006867335	1.035735858  59.34329



	// deltas
	double ldD    = 0.001;			// long-distance
	double sdD    = 0.0000001;		// short-distance
	double angleD = 0.000000001;	// angle
	double azrD   = 0.000000001;	// azimuth-rad
	double azdD   = 0.00001;		// azimuth-deg
	
	
	
//	@Before
//	public void setUp() throws Exception {
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}

	@Test
	public final void testAngle() {
		assertEquals(1.001818991, angle(L5,L1), angleD);
		assertEquals(1.005309649, angle(L2,L5), angleD);
		assertEquals(2.129301687, angle(L4,L6), angleD);
		assertEquals(2.132792346, angle(L6,L3), angleD);
		assertEquals(0.006844919, angle(L1,L2), angleD);
		assertEquals(0.007577932, angle(L1,L3), angleD);
		assertEquals(0.010879690, angle(L1,L4), angleD);
		assertEquals(0.009514959, angle(L2,L3), angleD);
		assertEquals(0.007580467, angle(L4,L2), angleD);
		assertEquals(0.006867335, angle(L4,L3), angleD);
	}

	@Test
	public final void testSurfaceDistance() {
		assertEquals(  6382.596, surfaceDistance(L5,L1), ldD);
		assertEquals(  6404.835, surfaceDistance(L2,L5), ldD);
		assertEquals( 13565.796, surfaceDistance(L4,L6), ldD);
		assertEquals( 13588.035, surfaceDistance(L6,L3), ldD);
		assertEquals(43.6090311, surfaceDistance(L1,L2), sdD);
		assertEquals(48.2790582, surfaceDistance(L1,L3), sdD);
		assertEquals(69.3145862, surfaceDistance(L1,L4), sdD);
		assertEquals(60.6198752, surfaceDistance(L2,L3), sdD);
		assertEquals(48.2952067, surfaceDistance(L4,L2), sdD);
		assertEquals(43.7518411, surfaceDistance(L4,L3), sdD);
	}

	@Test
	public final void testFastSurfaceDistance() {
		assertEquals(  6474.888, fastSurfaceDistance(L5,L1), ldD);
		assertEquals(  6493.824, fastSurfaceDistance(L2,L5), ldD);
		assertEquals( 13707.303, fastSurfaceDistance(L4,L6), ldD);
		assertEquals( 13735.216, fastSurfaceDistance(L6,L3), ldD);
		assertEquals(43.6090864, fastSurfaceDistance(L1,L2), sdD);
		assertEquals(48.2790921, fastSurfaceDistance(L1,L3), sdD);
		assertEquals(69.3146382, fastSurfaceDistance(L1,L4), sdD);
		assertEquals(60.6200022, fastSurfaceDistance(L2,L3), sdD);
		assertEquals(48.2952403, fastSurfaceDistance(L4,L2), sdD);
		assertEquals(43.7518956, fastSurfaceDistance(L4,L3), sdD);
	}

	@Test
	public final void testLinearDistance() {
		double delta = 0.000000001;
		
		// small angles
		Location L1 = new Location(20.0, 20.0, 2);
		Location L2 = new Location(20.1, 20.1, 2);
		Location L3 = new Location(20.1, 20.1, 17);
		double sd12 = surfaceDistance(L1,L2);	// 15.256270609
		double ld12 = linearDistance(L1,L2);	// 15.251477684
		double ld13 = linearDistance(L1,L3);	// 21.378955649
		
		assertTrue("Linear distance should be shorter", ld12 < sd12);
		assertTrue("ld12 should be shorter than ld13", ld13 > ld12);
		assertEquals(15.251477684, ld12, delta);
		assertEquals(21.378955649, ld13, delta);

		// large angles
		Location L4 = new Location( 45.0, -20.0, 2);
		Location L5 = new Location(-40.0, 20.0, 17);
		Location L6 = new Location(-50.0, 20.0, 17);
		double ld45 = linearDistance(L4,L5);	// 9172.814801278
		double ld46 = linearDistance(L4,L6);	// 9828.453361410
		
		assertEquals(9172.814801278, ld45, delta);
		assertEquals(9828.453361410, ld46, delta);
	}

	@Test
	public final void testAzimuth() {
		assertEquals(    180.0, azimuth(L5,L1), azdD);
		assertEquals(      0.0, azimuth(L2,L5), azdD);
		assertEquals(    180.0, azimuth(L4,L6), azdD);
		assertEquals(      0.0, azimuth(L6,L3), azdD);
		assertEquals(239.44623, azimuth(L1,L2), azdD);
		assertEquals(157.05864, azimuth(L1,L3), azdD);
		assertEquals(195.78891, azimuth(L1,L4), azdD);
		assertEquals(111.36156, azimuth(L2,L3), azdD);
		assertEquals(337.12017, azimuth(L4,L2), azdD);
		assertEquals( 59.34329, azimuth(L4,L3), azdD);
	}

	@Test
	public final void testAzimuthRad() {
		assertEquals(3.141592654, azimuthRad(L5,L1), azrD);
		assertEquals(0.0        , azimuthRad(L2,L5), azrD);
		assertEquals(3.141592654, azimuthRad(L4,L6), azrD);
		assertEquals(0.0        , azimuthRad(L6,L3), azrD);
		assertEquals(4.179125015, azimuthRad(L1,L2), azrD);
		assertEquals(2.741190313, azimuthRad(L1,L3), azrD);
		assertEquals(3.417161139, azimuthRad(L1,L4), azrD);
		assertEquals(1.943625801, azimuthRad(L2,L3), azrD);
		assertEquals(5.883856933, azimuthRad(L4,L2), azrD);
		assertEquals(1.035735858, azimuthRad(L4,L3), azrD);
	}

	@Test
	public final void testLocation() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testDistanceToLine() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testGetApproxHorzDistToLine() {
		fail("Not yet implemented");
	}
	
	

}
