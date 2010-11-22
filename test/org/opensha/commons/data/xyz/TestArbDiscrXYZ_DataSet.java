package org.opensha.commons.data.xyz;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestArbDiscrXYZ_DataSet {
	
	private static final double xThresh = 0.000001d;
	
	private static double maxI = 89;

	@Before
	public void setUp() throws Exception {
	}
	
	protected XYZ_DataSetAPI createEmpty() {
		return new ArbDiscrXYZ_DataSet();
	}
	
	@Test
	public void testSetDuplicate() {
		XYZ_DataSetAPI xyz = createEmpty();
		
		assertEquals("initial size should be 0", 0, xyz.size());
		
		xyz.set(0d, 0d, 5d);
		assertEquals(1, xyz.size());
		
		xyz.set(0.1d, 0d, 5d);
		assertEquals(2, xyz.size());
		
		xyz.set(0.1d, 0d, 7d);
		assertEquals(2, xyz.size());
		
		assertEquals("index 0 not set correctly", 5d, xyz.get(0), xThresh);
		assertEquals("replace doesn't work", 7d, xyz.get(1), xThresh);
	}
	
	private XYZ_DataSetAPI getTestData() {
		return getTestData(0d);
	}
	
	private XYZ_DataSetAPI getTestData(double iAdd) {
		XYZ_DataSetAPI xyz = createEmpty();
		
		for (double i=0; i<=maxI; i++) {
			double realI = i+iAdd;
			xyz.set(realI, -realI, realI);
		}
		
		return xyz;
	}
	
	@Test
	public void testGetPoint() {
		XYZ_DataSetAPI xyz = getTestData();
		
		for (int i=0; i<maxI; i++) {
			assertEquals("get x by index doesn't work", (double)i, xyz.getPoint(i).getX(), xThresh);
			assertEquals("get y by index doesn't work", (double)-i, xyz.getPoint(i).getY(), xThresh);
			assertEquals("get z by index doesn't work", (double)i, xyz.get(i), xThresh);
			
			assertEquals("indexOf doesn't work", i, xyz.indexOf((double)i, (double)-i));
		}
	}
	
	@Test
	public void testSetAll() {
		XYZ_DataSetAPI xyz = getTestData();
		
		int origSize = xyz.size();
		
		xyz.setAll(getTestData());
		assertEquals("set all still added duplicates", origSize, xyz.size());
		
		XYZ_DataSetAPI diffValsDataSet = getTestData();
		XYZ_DataSetMath.add(diffValsDataSet, 0.1d);
		xyz.setAll(diffValsDataSet);
		assertEquals("set all still added duplicate locs with diff values", origSize, xyz.size());
		
		XYZ_DataSetAPI diffPtsDataSet = getTestData(0.1);
		xyz.setAll(diffPtsDataSet);
		assertEquals("set all didn't add new values", origSize*2, xyz.size());
	}
	
	@Test
	public void testClone() {
		XYZ_DataSetAPI xyz = getTestData();
		XYZ_DataSetAPI cloned = (XYZ_DataSetAPI)xyz.clone();
		
		assertEquals("cloned size incorrect", xyz.size(), cloned.size());
		
		for (int i=0; i<xyz.size(); i++) {
			assertEquals("cloned points not equal", xyz.getPoint(i), cloned.getPoint(i));
			assertEquals("cloned values not equal", xyz.get(i), cloned.get(i), xThresh);
		}
		
		// change the cloned values
		XYZ_DataSetMath.add(cloned, 0.1);
		
		for (int i=0; i<xyz.size(); i++) {
			assertTrue("cloned operations are affecting original", xyz.get(i) != cloned.get(i));
		}
	}
	
	@Test
	public void testMinMax() {
		XYZ_DataSetAPI xyz = getTestData();
		
		assertEquals("x min is wrong", 0d, xyz.getMinX(), xThresh);
		assertEquals("x max is wrong", maxI, xyz.getMaxX(), xThresh);
		assertEquals("y min is wrong", -maxI, xyz.getMinY(), xThresh);
		assertEquals("y max is wrong", 0d, xyz.getMaxY(), xThresh);
		assertEquals("z min is wrong", 0d, xyz.getMinZ(), xThresh);
		assertEquals("z max is wrong", maxI, xyz.getMaxZ(), xThresh);
	}

}
