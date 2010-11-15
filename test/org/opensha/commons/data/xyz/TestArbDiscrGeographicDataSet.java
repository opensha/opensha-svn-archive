package org.opensha.commons.data.xyz;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;

import org.junit.Before;
import org.junit.Test;
import org.opensha.commons.geo.Location;

public class TestArbDiscrGeographicDataSet extends TestArbDiscrXYZ_DataSet {

	@Before
	public void setUp() throws Exception {
	}

	@Override
	protected XYZ_DataSetAPI createEmpty() {
		return createEmpty(true);
	}
	
	protected XYZ_DataSetAPI createEmpty(boolean latitudeX) {
		return new ArbDiscrGeographicDataSet(latitudeX);
	}
	
	protected ArbDiscrGeographicDataSet createTestData(boolean latitudeX) {
		return createTestData(latitudeX, 0d);
	}
	
	protected static ArbDiscrGeographicDataSet createTestData(boolean latitudeX, double add) {
		ArbDiscrGeographicDataSet data = new ArbDiscrGeographicDataSet(latitudeX);
		
		double lat = -90;
		double lon = -180;
		
		int i=0;
		while (lat+add <= 90 && lon+add <= 180) {
			data.set(new Location(lat+add, lon+add), i++);
			
			lat += 0.5;
			lon += 1.0;
		}
		
		return data;
	}
	
	private static void verifySingleLatX(ArbDiscrGeographicDataSet data) {
		for (int i=0; i<data.size(); i++) {
			Point2D pt = data.getPoint(i);
			Location loc = data.getLocation(i);
			
			if (data.isLatitudeX()) {
				assertEquals("LatX == true, but x != lat", loc.getLatitude(), pt.getX(), 0d);
				assertEquals("LatX == true, but y != lon", loc.getLongitude(), pt.getY(), 0d);
			} else {
				assertEquals("LatX == false, but y != lat", loc.getLatitude(), pt.getY(), 0d);
				assertEquals("LatX == false, but x != lon", loc.getLongitude(), pt.getX(), 0d);
			}
		}
	}
	
	private static void verifyLatX(ArbDiscrGeographicDataSet data1, ArbDiscrGeographicDataSet data2) {
		assertEquals("sizes not equal with different latX", data1.size(), data2.size());
		
		verifySingleLatX(data1);
		verifySingleLatX(data2);
		
		boolean opposite = data1.isLatitudeX() != data2.isLatitudeX();
		
		for (int i=0; i<data1.size(); i++) {
			Point2D pt1 = data1.getPoint(i);
			Point2D pt2 = data2.getPoint(i);
			Location loc1 = data1.getLocation(i);
			Location loc2 = data2.getLocation(i);
			assertEquals("locs not equal", loc1, loc2);
			
			if (opposite) {
				assertEquals("x1 != y2 when opposite!", pt1.getX(), pt2.getY(), 0d);
				assertEquals("y1 != x2 when opposite!", pt1.getY(), pt2.getX(), 0d);
			} else {
				assertEquals("not opposite, but also not equal!", pt1, pt2);
			}
		}
	}
	
	@Test
	public void testLatitudeX() {
		ArbDiscrGeographicDataSet data1 = createTestData(true);
		assertTrue("LatitudeX not set correctly in constructor", data1.isLatitudeX());
		ArbDiscrGeographicDataSet data2 = createTestData(false);
		assertFalse("LatitudeX not set correctly in constructor", data2.isLatitudeX());
		
		verifyLatX(data1, data2);
		
		data1.setLatitudeX(false);
		assertFalse("LatitudeX not set correctly via method", data1.isLatitudeX());
		verifyLatX(data1, data2);
		
		data2.setLatitudeX(true);
		assertTrue("LatitudeX not set correctly via methodr", data2.isLatitudeX());
		verifyLatX(data1, data2);
		
		data1.setLatitudeX(true);
		assertTrue("LatitudeX not set correctly via method", data1.isLatitudeX());
		verifyLatX(data1, data2);
	}
	
	@Test
	public void testGet() {
		ArbDiscrGeographicDataSet data = createTestData(true);
		
		for (int i=0; i<data.size(); i++) {
			Point2D pt = data.getPoint(i);
			Location loc = data.getLocation(i);
			
			assertEquals("get not equal with pt vs loc", data.get(pt), data.get(loc), 0d);
			assertEquals("get not equal with loc vs ind", data.get(i), data.get(loc), 0d);
		}
	}
	
	@Test
	public void testSetDuplicateLocs() {
		ArbDiscrGeographicDataSet xyz = createTestData(true);
		
		int origSize = xyz.size();
		
		xyz.setAll(createTestData(true));
		assertEquals("set all still added duplicates", origSize, xyz.size());
		
		ArbDiscrGeographicDataSet diffValsDataSet = createTestData(true);
		XYZ_DataSetMath.add(diffValsDataSet, 0.1d);
		xyz.setAll(diffValsDataSet);
		assertEquals("set all still added duplicate locs with diff values", origSize, xyz.size());
		
		ArbDiscrGeographicDataSet diffPtsDataSet = createTestData(true, 0.1);
		xyz.setAll(diffPtsDataSet);
		assertEquals("set all didn't add new values", origSize+diffPtsDataSet.size(), xyz.size());
	}
	

}
