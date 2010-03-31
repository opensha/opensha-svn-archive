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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;

import com.lowagie.text.Anchor;


public class GriddedRegionTest {

	// TODO these tests ought to be revisited when Java 6 is adopted. Currently
	// contains for nodes on south and east borders is failing dus to upconversion
	// of float to double in GeneralPath to Area
	
	// octagonal region
	static GriddedRegion octRegion;

	
	// TODO the octRegionNodeCount will need to be updated when south/east  
	// boundary issue is resolved
	static int octRegionNodeCount = 700;

	@BeforeClass
	public static void setUp(){
		RegionTest.setUp();
		LocationList ll = new LocationList();
		ll.add(new Location(25,-115));
		ll.add(new Location(25,-110));
		ll.add(new Location(30,-105));
		ll.add(new Location(35,-105));
		ll.add(new Location(40,-110));
		ll.add(new Location(40,-115));
		ll.add(new Location(35,-120));
		ll.add(new Location(30,-120));
		octRegion  = new GriddedRegion(ll, null, 0.5, GriddedRegion.ANCHOR_0_0);
	}

	// Only minimal tests are required for GriddedRegion. All constructors
	// call super() so the bulk of constructor argument error checking is
	// handled by RegionTest. Only private method initGrid() nees to be tested.
	
	@Test
	public final void testGriddedRegionLocationLocationDoubleLocation() {
		Location l1 = new Location(10,10);
		Location l2 = new Location(10.1,10.1);
		Location l3 = new Location(15,15);
		GriddedRegion gr;
		
		// test spacing range
		try {
			gr = new GriddedRegion(l1,l3,-3,null);
			fail("Spacing less than 0 not caught");
		} catch (IllegalArgumentException e) {}
		try {
			gr = new GriddedRegion(l1,l3,0,null);
			fail("Spacing of 0 not caught");
		} catch (IllegalArgumentException e) {}
		try {
			gr = new GriddedRegion(l1,l3,6,null);
			fail("Spacing greater than 5 not caught");
		} catch (IllegalArgumentException e) {}
		
		// test anchor setting by examining nodes
		// TODO the values are currently inset 
		gr = new GriddedRegion(l2, l3, 1, null);
		Location loc = gr.locationForIndex(0);
		assertTrue(loc.getLatitude() == 11.1);
		assertTrue(loc.getLongitude() == 11.1);
		gr = new GriddedRegion(l2, l3, 1, GriddedRegion.ANCHOR_0_0);
		loc = gr.locationForIndex(0);
		assertTrue(loc.getLatitude() % 1 == 0);
		assertTrue(loc.getLongitude() % 1 == 0);
		gr = new GriddedRegion(l2, l3, 1, new Location(0.65, 0.65));
		loc = gr.locationForIndex(0);
		assertTrue(loc.getLatitude() == 10.65);
		assertTrue(loc.getLongitude() == 10.65);
		
		fail("Not yet implemented: test that region includes nodes on borders");
	}

	@Test
	public final void testGriddedRegionLocationListBorderTypeDoubleLocation() {

		assertTrue("Covered by Region tests and first constructor", true);

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
	        GriddedRegion gr_in = (GriddedRegion) in.readObject();
	        in.close();
	        assertTrue(octRegion.equals(gr_in));
	        
	        System.out.println(gr_in.getNodeCount());
	        objPersist.delete();
		} catch (IOException ioe) {
			fail("Serialization Failed: " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			fail("Deserialization Failed: " + cnfe.getMessage());
		}

	}

	@Test
	public void testGriddedRegionLocationDoubleDoubleLocation() {
		assertTrue("Covered by Region tests and first constructor", true);
	}

	@Test
	public void testGriddedRegionLocationListDoubleDoubleLocation() {
		assertTrue("Covered by Region tests and first constructor", true);
	}

	@Test
	public void testGriddedRegionRegionDoubleLocation() {
		assertTrue("Covered by Region tests and first constructor", true);
	}

	@Test
	public void testGetSpacing() {
		assertTrue(octRegion.getSpacing() == 0.5);
	}

	@Test
	public void testGetNodeCount() {
		// TODO the octRegionNodeCount will need to be updated when south/east  
		// boundary issue is resolved
		//System.out.println(octRegion.getNodeCount());
		assertTrue(octRegion.getNodeCount() == octRegionNodeCount);
	}

	@Test
	public void testIsEmpty() {
		assertTrue(!octRegion.isEmpty());
		Location l1 = new Location(0.2, 0.2);
		Location l2 = new Location(0.3, 0.3);
		GriddedRegion gr = new GriddedRegion(l1,l2,1,GriddedRegion.ANCHOR_0_0);
		assertTrue(gr.isEmpty());
	}

	@Test
	public void testEqualsGriddedRegion() {
		// compare two differently constructed gridded regions
		// need to take into account offset for rectangular region
		Location l1 = new Location(0, 0);
		Location l2 = new Location(0, 5.00001);
		Location l3 = new Location(5.00001, 5.00001);
		Location l4 = new Location(5.00001, 0);
		Location l5 = new Location(5, 5);
		Location anchor = new Location (0.6, 0.6);
		LocationList ll = new LocationList();
		ll.add(l1);
		ll.add(l2);
		ll.add(l3);
		ll.add(l4);
		GriddedRegion gr1 = new GriddedRegion(l1, l5, 0.1, anchor);
		GriddedRegion gr2 = new GriddedRegion(ll, null, 0.1, anchor);
		assertTrue(gr1.equals(gr2));
	}

	@Test
	public void testSubRegion() {
		RegionTest.setUp();
		GriddedRegion gr1 = new GriddedRegion(RegionTest.circRegion, 0.5, null);
		
		// test no overlap returns null
		GriddedRegion gr3 = gr1.subRegion(RegionTest.smRectRegion1);
		assertTrue(gr3 == null);
		
		// test an intersection that yields an ampty region
		Location l1 = new Location(27.2, -112.2);
		Location l2 = new Location(27.3, -112.1);
		Region r1 = new Region(l1,l2);
		GriddedRegion gr5 = octRegion.subRegion(r1);
		assertTrue(gr5.isEmpty());
		
		// test sub region that shoul dhave one node -- the center of octRegion
		Location l3 = new Location(32.4, -112.6);
		Location l4 = new Location(32.6, -112.4);
		Region r2 = new Region(l3,l4);
		GriddedRegion gr6 = octRegion.subRegion(r2);
		assertTrue(gr6.indexForLocation(new Location(32.5, -112.5)) == 0);
	}

	@Test
	public void testSetInterior() {
		Location l1 = new Location(0, 0);
		Location l2 = new Location(5, 5);
		GriddedRegion gr = new GriddedRegion(l1, l2, 0.1, null);
		try {
			octRegion.addInterior(gr);
			fail("Unsupported Operation not caught");
		} catch (UnsupportedOperationException uoe) {}
	}

	@Test
	public void testIterator() {
		int i = 0;
		for (Location loc : octRegion) {
			assertTrue(octRegion.contains(loc));
			i += 1;
		}
		assertTrue(i == octRegionNodeCount);
	}

	@Test
	public void testGetNodeList() {
		assertTrue(octRegion.getNodeList().size() == octRegionNodeCount);
	}

	@Test
	public void testLocationForIndex() {
		Location l1 = new Location(10,10);
		Location l2 = new Location(15,15);
		GriddedRegion gr1 = new GriddedRegion(l1,l2,1,null);
		Location loc0 = gr1.locationForIndex(0);
		assertTrue(loc0.equals(new Location(10,10)));
		Location l3 = new Location(10.1,10.1);
		Location l4 = new Location(15,15);
		GriddedRegion gr2 = new GriddedRegion(l3,l4,1,null);
		loc0 = gr2.locationForIndex(0);
		assertTrue(loc0.equals(new Location(11.1,11.1)));
	}

	@Test
	public void testIndexForLocation() {
		Location l1 = new Location(10,10);
		Location l2 = new Location(15,15);
		Location l3 = new Location(10.9, 10.9);
		Location l4 = new Location(11, 11);
		GriddedRegion gr = new GriddedRegion(l1,l2,1,null);
		Location result = gr.locationForIndex(gr.indexForLocation(l3));
		assertTrue(result.equals(l4));
	}

	@Test
	public final void testGetMinGridLat() {
		assertTrue(octRegion.getMinGridLat() == 25.0);
	}

	@Test
	public final void testGetMaxGridLat() {
		assertTrue(octRegion.getMaxGridLat() == 40.0);
	}

	@Test
	public final void testGetMinGridLon() {
		assertTrue(octRegion.getMinGridLon() == -120.0);
	}

	@Test
	public final void testGetMaxGridLon() {
		assertTrue(octRegion.getMaxGridLon() == -105.0);
	}

	
	
	
	public static void main(String[] args) {
		
		RegionTest.setUp();

		// SMALL RECT - includes N and E border nodes due to added offset
		GriddedRegion GR = new GriddedRegion(
				RegionTest.smRectRegion2, 0.2, null);
		RegionUtils.regionToKML(
				GR,
				"GriddedRegionLocLoc", 
				Color.ORANGE);

		// INTERIOR - created with lg loc loc rect and small loc loc rect 2
		GriddedRegion interiorGR = new GriddedRegion(
				RegionTest.interiorRegion, 1, null);
		RegionUtils.regionToKML(
				interiorGR,
				"GriddedRegionInterior", 
				Color.ORANGE);
				
		
		// =================================================================
		// The code below will generate kml files for visual verification that
		// GriddedRegions are being instantiated correctly. These files were
		// the basis of comparison when performing an overhaul of the region
		// package. See below for code to generate gridded regions prior to
		// package modification.
//
//		GriddedRegion eggr;
//		
//		// nocal
//		eggr = new CaliforniaRegions.RELM_NOCAL_GRIDDED();
//		RegionUtils.regionToKML(eggr, "ver_NoCal_new", Color.ORANGE);
//		// relm
//		eggr = new CaliforniaRegions.RELM_GRIDDED();
//		RegionUtils.regionToKML(eggr, "ver_RELM_new", Color.ORANGE);
//		// relm_testing
//		eggr = new CaliforniaRegions.RELM_TESTING_GRIDDED();
//		RegionUtils.regionToKML(eggr, "ver_RELM_testing_new", Color.ORANGE);
//		// socal
//		eggr = new CaliforniaRegions.RELM_SOCAL_GRIDDED();
//		RegionUtils.regionToKML(eggr, "ver_SoCal_new", Color.ORANGE);
//		// wg02
//		eggr = new CaliforniaRegions.WG02_GRIDDED();
//		RegionUtils.regionToKML(eggr, "ver_WG02_new", Color.ORANGE);
//		// wg07
//		eggr = new CaliforniaRegions.WG07_GRIDDED();
//		RegionUtils.regionToKML(eggr, "ver_WG07_new", Color.ORANGE);
//		// relm_collect
//		eggr = new CaliforniaRegions.RELM_COLLECTION_GRIDDED();
//		RegionUtils.regionToKML(eggr, "ver_RELM_collect_new", Color.ORANGE);
		
		// =================================================================
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
