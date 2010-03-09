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

package org.opensha.commons.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.Location;

public class LocationTests {

	Location location;

	@Before
	public void setUp() throws Exception {
		location = new Location(10,10,10);
	}

	@Test
	public void testEqualsLocation() {
		Location loc =  new Location(10,10,10);
		assertTrue(location.equalsLocation(loc));

		loc =  new Location(10,20,10);
		assertTrue(!location.equalsLocation(loc));

		loc =  new Location(20,10,10);
		assertTrue(!location.equalsLocation(loc));

		loc =  new Location(10,10,20);
		assertTrue(!location.equalsLocation(loc));
	}

	@Test
	public void testGetDepth() {
		assertEquals(location.getDepth(), 10.0, 0);
	}

	@Test
	public void testGetLatitude() {
		assertEquals(location.getLatitude(), 10.0, 0);
	}

	@Test
	public void testGetLongitude() {
		assertEquals(location.getLongitude(), 10.0, 0);
	}

	@Test
	public void testSetDepth() {
		double depth1 =   15.0;
		location.setDepth(depth1);
		double doubleRet = location.getDepth();
		assertTrue( doubleRet == 15.0 );

		location.setDepth( 10.0 );
		doubleRet = location.getDepth();
		assertTrue( doubleRet == 10.0 );
	}

//	@Test
//	public void testSetLatitude() {
//		double latitude1 =   15.0   /** @todo fill in non-null value */;
//		double doubleRet;
//		try {
//			location.setLatitude(latitude1);
//			doubleRet = location.getLatitude();
//			assertTrue( doubleRet == 15.0 );
//
//			location.setLatitude( 10.0 );
//			doubleRet = location.getLatitude();
//			assertTrue( doubleRet == 10.0 );
//		}
//		catch(Exception e) {
//			System.err.println("Exception thrown:  "+e);
//		}
//
//
//		latitude1 = -95.1;
//		try {
//			location.setLatitude(latitude1);
//		}
//		catch(Exception e) {
//			doubleRet = location.getLatitude();
//			assertTrue( doubleRet == 10.0 );
//			//System.err.println("Successfully caught Exception thrown:  " + e);
//		}
//
//
//
//		latitude1= 95.1;
//		try {
//			location.setLatitude(latitude1);
//
//		}
//		catch(Exception e) {
//			doubleRet = location.getLatitude();
//			assertTrue( doubleRet == 10.0 );
//			//System.err.println("Successfully caught Exception thrown:  " + e);
//		}
//	}

//	@Test
//	public void testSetLongitude() {
//		double longitude1= 15.0 ;
//		double doubleRet;
//		try {
//			location.setLongitude(longitude1);
//			doubleRet = location.getLongitude();
//			assertTrue( doubleRet == 15.0 );
//
//			location.setLongitude( 10.0 );
//			doubleRet = location.getLongitude();
//			assertTrue( doubleRet == 10.0 );
//		}
//		catch(Exception e) {
//			System.err.println("Exception thrown:  "+e);
//		}
//
//
//		longitude1 = -181.1;
//		try {
//			location.setLongitude(longitude1);
//		}
//		catch(Exception e) {
//			doubleRet = location.getLongitude();
//			assertTrue( doubleRet == 10.0 );
//			//System.err.println("Successfully caught Exception thrown:  " + e);
//		}
//
//		longitude1 = 181.1;
//		try {
//			location.setLongitude(longitude1);
//		}
//		catch(Exception e) {
//			doubleRet = location.getLongitude();
//			assertTrue( doubleRet == 10.0 );
//			//System.err.println("Successfully caught Exception thrown:  " + e);
//		}
//	}

//	@Test
//	public void testToString() {
//
//		location.setDepth( 10 );
//		location.setLatitude( 10 );
//		location.setLongitude( 10 );
//		String stringRet = location.toString();
//		//System.out.println( "Printing out toString(): " + stringRet );
//
//		String testStr = new String();
//		testStr = "" + location.getLatitude() + "," + location.getLongitude() +
//		"," + location.getDepth();
//		assertEquals(stringRet, testStr);
//
//	}

//	@Test
//	public void testEquals() {
//
//		location.setDepth( 10 );
//		location.setLatitude( 10 );
//		location.setLongitude( 10 );
//
//		Location location1 = new Location();
//		location1.setDepth( 10 );
//		location1.setLatitude( 10 );
//		location1.setLongitude( 10 );
//
//		boolean booleanRet = location.equals(location1);
//		assertTrue( booleanRet );
//
//		location1.setDepth( 11 );
//		booleanRet = location.equals(location1);
//		assertTrue( !booleanRet );
//
//		location1.setDepth( 10 );
//		booleanRet = location.equals(location1);
//		assertTrue( booleanRet );
//
//		location1.setLatitude( 11 );
//		booleanRet = location.equals(location1);
//		assertTrue( !booleanRet );
//
//		location1.setLatitude( 10 );
//		booleanRet = location.equals(location1);
//		assertTrue( booleanRet );
//
//		location1.setLongitude( 11 );
//		booleanRet = location.equals(location1);
//		assertTrue( !booleanRet );
//
//		location1.setLongitude( 10 );
//		booleanRet = location.equals(location1);
//		assertTrue( booleanRet );
//	}

//	@Test
//	public void testClone() {
//
//		Object objectRet = location.copy();
//		boolean booleanRet = location.equals(objectRet);
//		assertTrue( booleanRet );
//
//		Location location1 = (Location)objectRet;
//
//		location1.setDepth( 11 );
//		booleanRet = location.equals(location1);
//		assertTrue( !booleanRet );
//
//		location1.setDepth( 10 );
//		booleanRet = location.equals(location1);
//		assertTrue( booleanRet );
//
//		location1.setLatitude( 11 );
//		booleanRet = location.equals(location1);
//		assertTrue( !booleanRet );
//
//		location1.setLatitude( 10 );
//		booleanRet = location.equals(location1);
//		assertTrue( booleanRet );
//
//		location1.setLongitude( 11 );
//		booleanRet = location.equals(location1);
//		assertTrue( !booleanRet );
//
//		location1.setLongitude( 10 );
//		booleanRet = location.equals(location1);
//		assertTrue( booleanRet );
//	}
}
