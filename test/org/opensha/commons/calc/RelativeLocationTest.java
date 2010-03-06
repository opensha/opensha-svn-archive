/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.commons.calc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opensha.commons.calc.RelativeLocation.*;

import org.junit.Test;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

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
	//     vd: Vincenty distance (most accurate, provided for comparison)
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

	//        fdtl           dtl
	// d321   34.472999888   34.425229936
	// d231   34.472999888  -34.425229936
	// d432   47.859144611  -47.851004687
	// d413   30.170948729   30.205855981

	// deltas - based on what decimal place known values above were clipped
	double ldD    = 0.001;			// long-distance
	double sdD    = 0.0000001;		// short-distance
	double angleD = 0.000000001;	// angle
	double azrD   = 0.000000001;	// azimuth-rad
	double azdD   = 0.00001;		// azimuth-deg
	double dtlD   = 0.000000001;	// dist to line
	
	
	
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
		assertEquals(  6474.888, surfaceDistanceFast(L5,L1), ldD);
		assertEquals(  6493.824, surfaceDistanceFast(L2,L5), ldD);
		assertEquals( 13707.303, surfaceDistanceFast(L4,L6), ldD);
		assertEquals( 13735.216, surfaceDistanceFast(L6,L3), ldD);
		assertEquals(43.6090864, surfaceDistanceFast(L1,L2), sdD);
		assertEquals(48.2790921, surfaceDistanceFast(L1,L3), sdD);
		assertEquals(69.3146382, surfaceDistanceFast(L1,L4), sdD);
		assertEquals(60.6200022, surfaceDistanceFast(L2,L3), sdD);
		assertEquals(48.2952403, surfaceDistanceFast(L4,L2), sdD);
		assertEquals(43.7518956, surfaceDistanceFast(L4,L3), sdD);
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
		assertEquals( 34.425229936, distanceToLine(L3,L2,L1), dtlD);
		assertEquals(-34.425229936, distanceToLine(L2,L3,L1), dtlD);
		assertEquals(-47.851004687, distanceToLine(L4,L3,L2), dtlD);
		assertEquals( 30.205855981, distanceToLine(L4,L1,L3), dtlD);
	}
	
	@Test
	public final void testGetApproxHorzDistToLine() {
		assertEquals(34.472999888, getApproxHorzDistToLine(L3,L2,L1), dtlD);
		assertEquals(34.472999888, getApproxHorzDistToLine(L2,L3,L1), dtlD);
		assertEquals(47.859144611, getApproxHorzDistToLine(L4,L3,L2), dtlD);
		assertEquals(30.170948729, getApproxHorzDistToLine(L4,L1,L3), dtlD);
	}
	
	@Test
	public final void testIsPole() {
		Location sp = new Location(-89.999999999999, 0);
		Location np = new Location( 89.999999999999, 0);
		Location ll = new Location(22,150);
		assertTrue(isPole(sp));
		assertTrue(isPole(np));
		assertTrue(!isPole(ll));
	}
	
	/**
	 * Test value generation along with various speed comparisons 
	 * provided below.
	 * @param args
	 */
	public static void main(String[] args) {
		
		// shared convenience fields
		Location L1, L2, L3, L4, L5, L6;
		int numIter = 1000000;
		
		
		
		// ==========================================================
		//     VALUE GENERATION
		// ==========================================================
		
		L1 = new Location(32.6, 20.4);
		L2 = new Location(32.4, 20);
		L3 = new Location(32.2, 20.6);
		L4 = new Location(32, 20.2);
		
		L5 = new Location(90, 0);
		L6 = new Location(-90, 0);
		
		//     vd			sd			fsd			angle		az-rad		az-deg
		// d51  6393.578 km	 6382.596	 6474.888	1.001818991	3.141592654	180.0
		// d25  6415.757 km	 6404.835	 6493.824	1.005309649	0.0			  0.0
		// d46 13543.818 km	13565.796	13707.303	2.129301687	3.141592654	180.0
		// d63 13565.996 km	13588.035	13735.216	2.132792346	0.0			  0.0
		
		// d12 43.645957 km	43.6090311	43.6090864  0.006844919 4.179125015 239.44623
		// d13 48.183337 km	48.2790582	48.2790921	0.007577932	2.741190313 157.05864
		// d14 69.150258 km	69.3145862	69.3146382	0.010879690 3.417161139 195.78891
		// d23 60.706703 km	60.6198752	60.6200022	0.009514959	1.943625801 111.36156
		// d42 48.198212 km	48.2952067	48.2952403	0.007580467	5.883856933	337.12017
		// d43 43.787840 km	43.7518411	43.7518956	0.006867335	1.035735858  59.34329
		
		//        fdtl           dtl
		// d321   34.472999888   34.425229936
		// d231   34.472999888  -34.425229936
		// d432   47.859144611  -47.851004687
		// d413   30.170948729   30.205855981
		
		Location p1 = L4;
		Location p2 = L1;
		Location p3 = L3;
		System.out.println(surfaceDistance(p1, p2));
		System.out.println(surfaceDistanceFast(p1, p2));
		System.out.println(angle(p1,p2));
		System.out.println(azimuthRad(p1, p2));
		System.out.println(azimuth(p1, p2));
		System.out.println(getApproxHorzDistToLine(p1, p2, p3));
		System.out.println(distanceToLine(p1, p2, p3));
		
		
		
		
		// ==========================================================
		//    Distance to Line Methods
		//
		//    Summary: the highly accurate Haversine based formula is 
		//             much slower (up to 20x), but does not work
		//			   accross dateline and does not indicate
		//			   sidedness
		// ==========================================================
		
		L1 = new Location(32,-116);
		L2 = new Location(37,-115);
		L3 = new Location(34,-114);
		
		System.out.println("\nSPEED TEST -- distanceToLine()\n");
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			for (int j=0; j<numIter; j++) {
				double d = RelativeLocation.getApproxHorzDistToLine(L1,L2,L3);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" AHDTL: " + T);
		}
		
		System.out.println("");
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			for (int j=0; j<numIter; j++) {
				double d = RelativeLocation.distanceToLine(L1,L2,L3);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println("   DTL: " + T);
		}

		
		
		
		// ==========================================================
		//    Surface Distance Methods
		//
		//    Summary: Accurate, Haversine based methods of distance
		//             calculation have beeen shown to be much faster
		//             than existing methods (e.g. getHorzDistance).
		//             1M repeat runs showed the following comp
		//             times:
		//                
		//             HD   getHorizDistance()			1158 ms
		//             AHD  getApproxHorzDistance()		820  ms
		//             SD   surfaceDistance()			255  ms
		//             SDF  surfaceDistanceFast()		3    ms
		// ==========================================================

		// long pair ~9K km : discrepancies > 100km
		// L1 = new Location(20,-10);
		// L2 = new Location(-20,60);
		
		// mid pair ~250 km : discrepancies in 10s of meters
		// L1 = new Location(32.1,-117.2);
		// L2 = new Location(33.8, -115.4);
		
		// short pair : negligible discrepancy in values
		L1 = new Location(32.132,-117.21);
		L2 = new Location(32.306, -117.105);
		
		System.out.println("getHorzDistance(): " + 
				getHorzDistance(L1, L2));
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			double surfDist;
			for (int j=0; j<numIter; j++) {
				surfDist = getHorzDistance(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" HD: " + T);
		}
		
		System.out.println("getApproxHorzDistance(): " + 
				getApproxHorzDistance(L1, L2));
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			double surfDist;
			for (int j=0; j<numIter; j++) {
				surfDist = getApproxHorzDistance(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" AD: " + T);
		}

		System.out.println("surfaceDistance(): " + 
				surfaceDistance(L1, L2));
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			double surfDist;
			for (int j=0; j<numIter; j++) {
				surfDist = surfaceDistance(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" SD: " + T);
		}
		
		System.out.println("surfaceDistanceFast(): " + 
				surfaceDistanceFast(L1, L2));
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			double surfDist;
			for (int j=0; j<numIter; j++) {
				surfDist = surfaceDistanceFast(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println("SDF: " + T);
		}

		
		

		// ==========================================================
		//    Linear Distance Methods
		//
		//    Summary: Accurate, Haversine based methods of distance
		//             calculation have beeen shown to be much faster
		//             than existing methods (e.g. getHorzDistance).
		//             1M repeat runs showed the following comp
		//             times:
		//                
		//             TD   getTotalDistanceOLD()		1190 ms
		//             LD   getLinearDistance()			221  ms
		//             LDF  getLinearDistanceFast()		3    ms
		// ==========================================================

		// mid pair ~250 km : discrepancies in 10s of meters
		L1 = new Location(32.1,-117.2);
		L2 = new Location(33.8, -115.4);
		
		// short pair : negligible discrepancy in values
		// L1 = new Location(32.132,-117.21);
		// L2 = new Location(32.306, -117.105);
		
		System.out.println("getTotalDistance(): " + 
				getTotalDistance(L1, L2));
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			double dist;
			for (int j=0; j<numIter; j++) {
				dist = getTotalDistance(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" TD: " + T);
		}

		System.out.println("linearDistance(): " + 
				linearDistance(L1, L2));
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			double dist;
			for (int j=0; j<numIter; j++) {
				dist = linearDistance(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" SD: " + T);
		}
		
		System.out.println("linearDistanceFast(): " + 
				linearDistanceFast(L1, L2));
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			double dist;
			for (int j=0; j<numIter; j++) {
				dist = linearDistanceFast(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println("SDF: " + T);
		}

		
		
		// ==========================================================
		//    Azimuth Methods
		//
		//    Summary: New, spherical geometry azimuth methods are
		//			   faster than existing methods.
		//             1M repeat runs showed the following comp
		//             times:
		//                
		//             gA   getAzimuthOLD()		1240 ms
		//              A   azimuth()			348  ms
		// ==========================================================

		L1 = new Location(32, -117);
		L2 = new Location(33, -115);
		
		System.out.println("getAzimuth(): " + 
				getAzimuth(L1, L2));
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			double dist;
			for (int j=0; j<numIter; j++) {
				dist = getAzimuth(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" gA: " + T);
		}

		System.out.println("azimuth(): " + 
				azimuth(L1, L2));
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			double dist;
			for (int j=0; j<numIter; j++) {
				dist = azimuth(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println("  A: " + T);
		}

		
		
		
		// ==========================================================
		//    The following code may be used to explore how old and
		//	  new distance caclulation methods compare and how
		//	  results cary with distance

		// commented values are accurate distances computed 
		// using the Vincenty formula
		
		Location L1a = new Location(20,-10); // 8818.496 km
		Location L1b = new Location(-20,60);
		
		Location L2a = new Location(90,10); // 4461.118 km
		Location L2b = new Location(50,80);

		Location L3a = new Location(-80,-30); // 3824.063 km
		Location L3b = new Location(-50,20);
		
		Location L4a = new Location(-42,178); // 560.148 km
		Location L4b = new Location(-38,-178);

		Location L5a = new Location(5,-90); // 784.028 km
		Location L5b = new Location(0,-85);

		Location L6a = new Location(70,-40); // 1148.942 km
		Location L6b = new Location(80,-50);

		Location L7a = new Location(-30,80); // 1497.148 km
		Location L7b = new Location(-20,90);
		
		Location L8a = new Location(70,70); // 234.662 km
		Location L8b = new Location(72,72);

		Location L9a = new Location(-20,120); // 305.532 km
		Location L9b = new Location(-18,122);
		
		// LocationList llL1 = createLocList(L1a,L1b,0.2);
		// LocationList llL2 = createLocList(L2a,L2b,0.2);
		// LocationList llL3 = createLocList(L3a,L3b,0.2);
		// LocationList llL4 = createLocList(L4a,L4b,356); // spans prime meridian
		LocationList llL5 = createLocList(L5a,L5b,0.05);
		// LocationList llL6 = createLocList(L6a,L6b,0.05);
		// LocationList llL7 = createLocList(L7a,L7b,0.05);
		// LocationList llL8 = createLocList(L8a,L8b,0.001);
		// LocationList llL9 = createLocList(L9a,L9b,0.001);
		
		LocationList LLtoUse = llL5;
		Location startPt = LLtoUse.getLocationAt(0);
		for (int i = 1; i < LLtoUse.size(); i++) {
			Location endPt = LLtoUse.getLocationAt(i);
			double surfDist = surfaceDistance(startPt, endPt);
			double fastSurfDist = surfaceDistanceFast(startPt, endPt);
			double delta1 = fastSurfDist - surfDist;
			double horizDist = getHorzDistance(startPt, endPt);
			double approxDist = getApproxHorzDistance(startPt, endPt);
			double delta2 = approxDist - horizDist;
			double delta3 = fastSurfDist - approxDist;
			String s = String.format(
					"sd: %03.4f  sdf: %03.4f  d: %03.4f  " + 
					"hd: %03.4f  ad: %03.4f  d: %03.4f  Df: %03.4f",
					surfDist, fastSurfDist, delta1,
					horizDist, approxDist, delta2, delta3);
			System.out.println(s);
		}
	
	}
	
	// utility method to create a locationlist between two points; points
	// are discretized in longitude using 'lonInterval'; latitude intervals 
	// are whatever they need to be to get to L2
	//
	// this is used in 'main' when exploring variations between distance
	// calculators
	private static LocationList createLocList(
			Location L1, Location L2, double lonInterval) {
		int numPoints = (int) Math.floor(Math.abs(
				L2.getLongitude() - L1.getLongitude()) / lonInterval);
		double dLat = (L2.getLatitude() - L1.getLatitude()) / numPoints;
		double dLon = (L1.getLongitude() - L2.getLongitude() < 0) ? 
				lonInterval : -lonInterval;
		LocationList ll = new LocationList();
		double lat = L1.getLatitude();
		double lon = L1.getLongitude();
		for(int i=0; i<=numPoints; i++) {
			//System.out.println(lat + " " + lon);
			ll.addLocation(new Location(lat,lon));
			lat += dLat;
			lon += dLon;
		}
		return ll;
	}


}
