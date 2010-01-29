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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.RegionUtils.Color;

public class GriddedRegionTest {

	// octagonal region
	static GriddedRegion octRegion;

	@BeforeClass
	public static void setUp(){
		LocationList ll = new LocationList();
		ll.addLocation(new Location(25,-115));
		ll.addLocation(new Location(25,-110));
		ll.addLocation(new Location(30,-105));
		ll.addLocation(new Location(35,-105));
		ll.addLocation(new Location(40,-110));
		ll.addLocation(new Location(40,-115));
		ll.addLocation(new Location(35,-120));
		ll.addLocation(new Location(30,-120));
		octRegion  = new GriddedRegion(ll, null, 0.2, GriddedRegion.ANCHOR_0_0);
	}

	@Test
	public final void testGriddedRegionLocationLocationDoubleLocation() {
		
		fail("Not yet implemented: test of set spacing");
	}

	@Test
	public final void testGriddedRegionLocationListBorderTypeDoubleLocation() {
		
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

		//fail("Not yet implemented");
	}

	@Test
	public final void testGriddedRegionLocationDoubleDoubleLocation() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGriddedRegionLocationListDoubleDoubleLocation() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGriddedRegionGeographicRegionDoubleLocation() {
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
		GriddedRegion eggr = new GriddedRegion(
				RegionTest.circRegion, 0.5, null);
		Region gr = RegionTest.lgRectMercRegion;
		
		fail("Not yet implemented");
	}

	public static void main(String[] args) {
		
		
		GriddedRegion sreg = new GriddedRegion(
				RegionTest.lgRectMercRegion, 0.5, null);
		Region gr = sreg.subRegion(RegionTest.circRegion);
		RegionUtils.regionToKML(gr, "SubRegion_circRectSub", Color.RED);
		
		
		
		
		
		// The code below will generate kml files for visual verification that
		// GriddedRegions are being instantiated correctly. These files were
		// the basis of comparison when performing an overhaul of the region
		// package. See below for code to generate gridded regions prior to
		// package modification.

		GriddedRegion eggr;
		
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
