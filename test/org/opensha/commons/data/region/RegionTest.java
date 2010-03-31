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

package org.opensha.commons.data.region;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;

public class RegionTest {
	
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
	static LocationList octRegionList;
	// small rect region (regionLocLoc)
	static Region smRectRegion1;
	// small rect region (regionLocLoc)
	static Region smRectRegion2;
	// small rect region (regionLocLoc)
	static Region smRectRegion3;
	// large rect region (regionLocList)
	static Region lgRectRegion;
	// large rect region (regionLocListBorderType)
	static Region lgRectMercRegion;
	// large rect region (regionLocListBorderType)
	static Region lgRectGCRegion;
	// buffered region (regionLocListDouble)
	static Region buffRegion;
	// circular region (regionLocDouble)
	static Region circRegion;
	// small circular region
	static Region smCircRegion;
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
	// interior region (smRectRegion3 added to lgRectRegion)
	static Region interiorRegion;
	// multi interior region
	static Region multiInteriorRegion;
	
	@BeforeClass
	public static void setUp() {
		octRegionList = new LocationList();
		octRegionList.addLocation(new Location(25,-115));
		octRegionList.addLocation(new Location(25,-110));
		octRegionList.addLocation(new Location(30,-105));
		octRegionList.addLocation(new Location(35,-105));
		octRegionList.addLocation(new Location(40,-110));
		octRegionList.addLocation(new Location(40,-115));
		octRegionList.addLocation(new Location(35,-120));
		octRegionList.addLocation(new Location(30,-120));
		octRegion  = new Region(octRegionList, null);
		
		Location a = new Location(39,-117);
		Location b = new Location(41,-113);
		smRectRegion1 = new Region(a,b);
		
		// offset from smRectRegion1; for testing interior overlap
		a = new Location(40,-116);
		b = new Location(42,-112);
		smRectRegion2 = new Region(a,b);

		LocationList ll = new LocationList();
		ll.addLocation(new Location(40,-116));
		ll.addLocation(new Location(40,-112));
		ll.addLocation(new Location(42,-112));
		ll.addLocation(new Location(42,-116));
		smRectRegion3 = new Region(ll, BorderType.MERCATOR_LINEAR);

		a = new Location(35,-125);
		b = new Location(45,-105);
		lgRectRegion = new Region(a,b);
		
		ll = new LocationList();
		ll.addLocation(new Location(35,-125));
		ll.addLocation(new Location(35,-105));
		ll.addLocation(new Location(45,-105));
		ll.addLocation(new Location(45,-125));
		lgRectMercRegion = new Region(ll, BorderType.MERCATOR_LINEAR);
		lgRectGCRegion = new Region(ll, BorderType.GREAT_CIRCLE);

		Location center = new Location(35, -125);
		circRegion = new Region(center, 400);
		Location smCenter = new Location(43, -110);
		smCircRegion = new Region(smCenter, 100);
		
		ll = new LocationList();
		ll.addLocation(new Location(35,-125));
		ll.addLocation(new Location(42,-119));
		ll.addLocation(new Location(40,-113));
		ll.addLocation(new Location(45,-105));
		buffRegion = new Region(ll,100);
		
		// unions and intersections
		circLgRectIntersect = Region.intersect(lgRectMercRegion, circRegion);
		circLgRectUnion = Region.union(lgRectMercRegion, circRegion);
		smRectLgRectIntersect = Region.intersect(lgRectMercRegion, smRectRegion1);
		smRectLgRectUnion = Region.union(lgRectMercRegion, smRectRegion1);
		circSmRectIntersect = Region.intersect(circRegion, smRectRegion1);
		circSmRectUnion = Region.intersect(circRegion, smRectRegion1);
		
		// interior
		interiorRegion = new Region(lgRectRegion);
		interiorRegion.addInterior(smRectRegion3);
		interiorRegion.addInterior(smCircRegion);
//		interiorRegion2 = new Region(lgRectRegion); TODO clean
//		interiorRegion2.addInterior(smRectRegion3);
		
	}

	@Test
	public final void testRegionLocationLocation() {
		
		// initialization tests
		Location L1 = new Location(32,112);
		Location L2 = new Location(32,118);
		Location L3 = new Location(34,118);
		try {
			Region r = new Region(L1,L2);
			fail("Same lat values not caught");
		} catch (IllegalArgumentException iae) {}
		try {
			Region r = new Region(L2,L3);
			fail("Same lon values not caught");
		} catch (IllegalArgumentException iae) {}
		try {
			L1 = null;
			L2 = null;
			Region r = new Region(L1,L2);
			fail("Null argument not caught");
		} catch (NullPointerException npe) {}
		
		// region creation tests
		LocationList ll1 = smRectRegion1.getBorder();
		LocationList ll2 = createLocList(regionLocLocDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		
		// test that addition of additional N and E offset for insidedness
		// testing is not applied to borders at 90N and 180E
		Location L4 = new Location(80,170);
		Location L5 = new Location(90,170);
		Location L6 = new Location(90,180);
		Location L7 = new Location(80,180);
		Region r1 = new Region(L4, L6);
		LocationList locList1 = new LocationList();
		locList1.addLocation(L4);
		locList1.addLocation(L5);
		locList1.addLocation(L6);
		locList1.addLocation(L7);
		Region r2 = new Region(locList1, BorderType.MERCATOR_LINEAR);
		assertTrue(r1.equals(r2));
		
		// and is applied to other borders
		Location L8 = new Location(80,170);
		Location L9 = new Location(89,170);
		Location L10 = new Location(89,179);
		Location L11 = new Location(80,179);
		Region r3 = new Region(L8, L10);
		LocationList locList2 = new LocationList();
		locList2.addLocation(L8);
		locList2.addLocation(L9);
		locList2.addLocation(L10);
		locList2.addLocation(L11);
		Region r4 = new Region(locList2, BorderType.MERCATOR_LINEAR);
		assertTrue(!r3.equals(r4));
		
		// test serialization
		try {
			// write it
			File objPersist = new File("test_serilaize.obj");
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(objPersist));
	        out.writeObject(octRegion);
	        out.close();
	        // read it
	        ObjectInputStream in = new ObjectInputStream(
					new FileInputStream(objPersist));
	        Region r_in = (Region) in.readObject();
	        in.close();
	        assertTrue(octRegion.equals(r_in));
	        objPersist.delete();
		} catch (IOException ioe) {
			fail("Serialization Failed: " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			fail("Deserialization Failed: " + cnfe.getMessage());
		}
	}

	@Test
	public final void testRegionLocationListBorderType() {
		// null args
		LocationList ll = new LocationList();
		try {
			ll = null;
			Region r = new Region(
					ll, BorderType.MERCATOR_LINEAR);
			fail("Null argument not caught");
		} catch (NullPointerException npe) {}
		
		// too short location list
		ll = new LocationList();
		ll.addLocation(new Location(35,-125));
		ll.addLocation(new Location(35,-75));
		try {
			Region r = new Region(ll, null);
			fail("Location list too short  not caught");
		} catch (IllegalArgumentException iae) {}
		
		// check that start point repeated at end of list is removed
		ll.addLocation(new Location(45,-75));
		ll.addLocation(new Location(35,-125));
		Region rectRegionStartRepeat = new Region(ll, null);
		assertTrue("Repeated start point not clipped",
				rectRegionStartRepeat.getBorder().size() == 3);

		// no-area location list
		ll = new LocationList();
		ll.addLocation(new Location(35,-125));
		ll.addLocation(new Location(35,-124));
		ll.addLocation(new Location(35,-123));
		try {
			Region r = new Region(ll, null);
			fail("Empty Region not caught");
		} catch (IllegalArgumentException iae) {}
			
		// non-singular location list
		ll = new LocationList();
		ll.addLocation(new Location(35,-125));
		ll.addLocation(new Location(35,-124));
		ll.addLocation(new Location(36,-125));
		ll.addLocation(new Location(36,-124));
		try {
			Region r = new Region(ll, null);
			fail("Non-singular Region not caught");
		} catch (IllegalArgumentException iae) {}
				
		// region creation test
		LocationList ll1 = lgRectMercRegion.getBorder();
		LocationList ll2 = createLocList(regionLocListMercatorDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		
		ll1 = lgRectGCRegion.getBorder();
		ll2 = createLocList(regionLocListGreatCircleDat);
		assertTrue(ll1.compareTo(ll2) == 0);
	}

	@Test
	public final void testRegionLocationDouble() {
		Location L1 = new Location(0,0,0);
		try {
			L1 = null;
			Region gr = new Region(L1, 50);
			fail("Null argument not caught");
		} catch (NullPointerException npe) {}
		try {
			L1 = new Location(0,0,0);
			Region gr = new Region(L1, 1001);
			fail("Radius too high not caught");
		} catch (IllegalArgumentException iae) {}
		try {
			L1 = new Location(0,0,0);
			Region gr = new Region(L1, 0);
			fail("Radius too low not caught");
		} catch (IllegalArgumentException iae) {}
		
		// region creation test
		LocationList ll1 = circRegion.getBorder();
		LocationList ll2 = createLocList(regionCircularDat);
		assertTrue(ll1.compareTo(ll2) == 0);
	}

	@Test
	public final void testRegionLocationListDouble() {
		LocationList ll = new LocationList();
		try {
			Region gr = new Region(ll, 50);
			fail("Empty location list not caught");
		} catch (IllegalArgumentException iae) {}
		ll.addLocation(new Location(0,0,0));
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
		LocationList ll1 = buffRegion.getBorder();
		LocationList ll2 = createLocList(regionBufferDat);
		assertTrue(ll1.compareTo(ll2) == 0);
	}

	@Test
	public final void testRegionRegion() {
		circRegion.setName("Cicle Region");
		Region newCircle = new Region(circRegion);
		assertTrue(newCircle.equals(circRegion)); // just tests areas
		// also test transfer of name and border data
		assertTrue(newCircle.getName().equals(circRegion.getName()));
		assertTrue(newCircle.getBorder().compareTo(
				circRegion.getBorder()) == 0);
		// test that interior gets transferred
		Region newInterior = new Region(interiorRegion);
		assertTrue(newInterior.equals(interiorRegion)); // just tests areas
		assertTrue(newInterior.getBorder().compareTo(
				interiorRegion.getBorder()) == 0);
		// test that locList interiors match
		List<LocationList> newInteriors = newInterior.getInteriors();
		List<LocationList> interiors = interiorRegion.getInteriors();
		for (int i=0; i<newInteriors.size(); i++) {
			assertTrue(newInteriors.get(i).compareTo(
					interiors.get(i)) == 0);
		}
		
		// null case
		try {
			Region r1 = null;
			Region r2 = new Region(r1);
			fail("Null argument not caught");
		} catch (NullPointerException npe) {}
		
		// re-test serialization to check region with interior
		try {
			// write it
			File objPersist = new File("test_serilaize.obj");
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(objPersist));
	        out.writeObject(interiorRegion);
	        out.close();
	        // read it
	        ObjectInputStream in = new ObjectInputStream(
					new FileInputStream(objPersist));
	        Region r_in = (Region) in.readObject();
	        in.close();
	        assertTrue(interiorRegion.equals(r_in));
	        objPersist.delete();
		} catch (IOException ioe) {
			fail("Serialization Failed: " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			fail("Deserialization Failed: " + cnfe.getMessage());
		}
		
	}
	
	@Test
	public final void testContainsLocation() {
		
		// insidedness testing is largely unnecessary as we can assume
		// java.awt.Area handles contains correctly. We do want to check
		// that great circle borders are being created correctly and
		// test that here.
				
		Location containsLoc1 = new Location(35.1,-115); // bottom edge
		Location containsLoc2 = new Location(45.1,-115); // top edge
		
		// mercator
		assertTrue(lgRectMercRegion.contains(containsLoc1));
		assertTrue(!lgRectMercRegion.contains(containsLoc2));
		// great circle
		assertTrue(!lgRectGCRegion.contains(containsLoc1));
		assertTrue(lgRectGCRegion.contains(containsLoc2));

		// also need to test that the small offset added to 'rectangular'
		// regions leads to inclusion of points that fall on the north and
		// east borders; also check points on the south and west to be safe
		Region rectRegionLocLoc = new Region(
				new Location(35,-105), new Location(45,-125));
		Location containsEloc = new Location(40,-105);
		Location containsNloc = new Location(45,-115);
		Location containsSloc = new Location(35,-115);
		Location containsWloc = new Location(40,-125);
		
		assertTrue(rectRegionLocLoc.contains(containsEloc));
		assertTrue(rectRegionLocLoc.contains(containsNloc));
		assertTrue(rectRegionLocLoc.contains(containsSloc));
		assertTrue(rectRegionLocLoc.contains(containsWloc));

		assertTrue(!lgRectMercRegion.contains(containsEloc));
		assertTrue(!lgRectMercRegion.contains(containsNloc));
		assertTrue(lgRectMercRegion.contains(containsSloc));
		assertTrue(lgRectMercRegion.contains(containsWloc));
		
		//fail()
	}
	
	@Test
	public final void testContainsRegion() {
		assertTrue(lgRectMercRegion.contains(smRectRegion1));
		assertTrue(!circRegion.contains(smRectRegion1));
		assertTrue(!lgRectMercRegion.contains(circRegion));
	}

	@Test
	public final void testIsRectangular() {
		assertTrue("N/A, covered by Area.isRectangular()", true);
	}
	
	@Test
	public final void testAddInterior() {
		//exception - null arg
		try {
			octRegion.addInterior(null);
			fail("Null argument not caught");
		} catch (Exception e) {}
		//exception - supplied has interior (non-singular)
		try {
			octRegion.addInterior(interiorRegion);
			fail("Illegal argument not caught");
		} catch (Exception e) {}
		//exception - contains: supplied exceeds existing
		try {
			smRectRegion1.addInterior(lgRectMercRegion);
			fail("Illegal argument not caught");
		} catch (Exception e) {}
		//exception - contains: supplied overlaps existing
		try {
			lgRectMercRegion.addInterior(circRegion);
			fail("Illegal argument not caught");
		} catch (Exception e) {}
		//exception - supplied overlaps an existing interior
		try {
			interiorRegion.addInterior(smRectRegion2);
			fail("Illegal argument not caught");
		} catch (Exception e) {}
		
		
		// test that interior area was set by checking insidedness of
		// rectangular interior.
		assertTrue(!interiorRegion.contains(new Location(41, -114)));
		// N edge - should include
		assertTrue(interiorRegion.contains(new Location(42, -114)));
		assertTrue(!interiorRegion.contains(new Location(41.9999, -114)));
		// E edge - should include
		assertTrue(interiorRegion.contains(new Location(41, -112)));
		assertTrue(!interiorRegion.contains(new Location(41, -112.0001)));
		// S edge - should NOT include
		assertTrue(!interiorRegion.contains(new Location(40, -114)));
		assertTrue(interiorRegion.contains(new Location(39.9999, -114)));
		// W edge - should NOT include
		assertTrue(!interiorRegion.contains(new Location(41, -116)));
		assertTrue(interiorRegion.contains(new Location(41, -116.0001)));
		// center of small circle
		assertTrue(!interiorRegion.contains(new Location(43, -110)));
		// check some other points that should still be inside
		assertTrue(interiorRegion.contains(new Location(42, -115)));
		assertTrue(interiorRegion.contains(new Location(40, -112)));
		assertTrue(interiorRegion.contains(new Location(38, -115)));
		assertTrue(interiorRegion.contains(new Location(40, -118)));

	}
	
	// TODO compareTo should be replaced with equals()
	@Test
	public final void testGetInteriors() {
		assertTrue(octRegion.getInteriors() == null);
		assertTrue(interiorRegion.getInteriors() != null);
		assertTrue(interiorRegion.getInteriors().get(0).compareTo(
				smRectRegion3.getBorder()) == 0);
		assertTrue(interiorRegion.getInteriors().get(1).compareTo(
				smCircRegion.getBorder()) == 0);

		// test immutability of List
		try {
			interiorRegion.getInteriors().add(new LocationList());
			fail("UnsupportedOperationExcaeption not caught");
		} catch (Exception e) {}
		
		fail("Not yet implemented: immutability of interior borders");
	}
	
	@Test
	public final void testGetBorder() {
		// test border is correct
		// TODO use equals when implemented
		assertTrue(octRegionList.compareTo(octRegion.getBorder()) == 0);
		
		fail("Not yet implemented: immutability of outer border");
	}

	@Test
	public final void testEquals() {
		assertTrue("N/A, covered by Area.equals(Area)", true);
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
	public final void testDistanceToLocation() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetName() {
		assertTrue(octRegion.getName().equals("Unnamed Region"));
	}

	@Test
	public final void testSetName() {
		octRegion.setName("Oct Region");
		assertTrue(octRegion.getName().equals("Oct Region"));
		octRegion.setName("Unnamed Region");
		assertTrue(octRegion.getName().equals("Unnamed Region"));
	}

	@Test
	public final void testToString() {
		// test that strig rep of circle is correct
		assertTrue(circRegion.toString().equals(
				"Region\n\tMinimum Lat: 31.40272\n\tMinimum Lon: -129.38866" +
				"\n\tMaximum Lat: 38.59728\n\tMaximum Lon: -120.61135"));
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
		//exceptions
		try {
			Region.intersect(null, interiorRegion);
			fail("Null argument not caught");
		} catch (Exception e) {}
		try {
			Region.intersect(interiorRegion, null);
			fail("Illegal argument not caught");
		} catch (Exception e) {}
		
		LocationList ll1, ll2;
		// partial overlap
		ll1 = circLgRectIntersect.getBorder();
		ll2 = createLocList(regionCircRectIntersectDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		// full overlap - this could be tested by matching a statically 
		// defined region using getRegionOutline(), however, Area operations
		// have a tendency to change the winding direction of border in which
		// case LocatonList.compareTo(LocationList) will fail, even though
		// the borders polygons are the same
		ll1 = smRectLgRectIntersect.getBorder();
		ll2 = createLocList(regionSmRectLgRectIntersectDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		
		// no overlap
		assertTrue(circSmRectIntersect == null);
	}
	
	@Test
	public final void testUnion() {
		//exceptions
		try {
			Region.union(null, interiorRegion);
			fail("Null argument not caught");
		} catch (Exception e) {}
		try {
			Region.union(interiorRegion, null);
			fail("Illegal argument not caught");
		} catch (Exception e) {}

		LocationList ll1, ll2;
		// partial overlap
		ll1 = circLgRectUnion.getBorder();
		ll2 = createLocList(regionCircRectUnionDat);
		assertTrue(ll1.compareTo(ll2) == 0);
		// full overlap - this could be tested by matching a statically 
		// defined region using getRegionOutline(), however, Area operations
		// have a tendency to change the winding direction of border in which
		// case LocatonList.compareTo(LocationList) will fail, even though
		// the borders polygons are the same
		ll1 = smRectLgRectUnion.getBorder();
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
		
		RegionTest.setUp();

		// The code below was used to create KML files for visual verification
		// of regions. The border vertices were then culled from the KML and 
		// are stored in arrays (below) for use in this test class
		
		// RECT
		RegionUtils.regionToKML(smRectRegion1, "RegionLocLoc", Color.ORANGE);
		
//		// LOCATION LIST border - mercator and great circle
//		RegionUtils.regionToKML(lgRectMercRegion, "RegionLocListMercator", Color.ORANGE);
//		RegionUtils.regionToKML(lgRectGCRegion, "RegionLocListGreatCircle", Color.ORANGE);
//
//		// CIRCLE
//		RegionUtils.regionToKML(circRegion, "RegionLocDouble", Color.ORANGE);
//		
//		// BUFFER
//		RegionUtils.regionToKML(buffRegion,"RegionLocListDouble",Color.ORANGE);
//
//		// CIRCLE-RECT INTERSECT and UNION
//		RegionUtils.regionToKML(circLgRectIntersect,"RegionCircleRectIntersect",Color.ORANGE);
//		RegionUtils.regionToKML(circLgRectUnion,"RegionCircleRectUnion",Color.ORANGE);
//		RegionUtils.regionToKML(smRectLgRectIntersect,"RegionSmRectLgRectIntersect",Color.ORANGE); 
//		RegionUtils.regionToKML(smRectLgRectUnion,"RegionSmRectLgRectUnion",Color.ORANGE);
//		
//		// INTERIOR REGION
//		RegionUtils.regionToKML(interiorRegion,"RegionInterior",Color.ORANGE);
		
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
	// close kml polygons but not needed internally for the region
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
