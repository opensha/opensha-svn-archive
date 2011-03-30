package org.opensha.commons.util;

import static org.junit.Assert.*;
import static org.opensha.commons.util.DataUtils.*;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.function.XY_DataSet;

public class DataUtilsTest {

	private double[] dd = {-10, Double.NaN, 0.0, 10};

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@Before
	public void setUp() throws Exception {}

	
	@Test (expected = NullPointerException.class)
	public void testValidateArrayNPE() { validate(null, -10, 10); }

	@Test (expected = IllegalArgumentException.class)
	public void testValidateArray1() { validate(dd, -9, 10); }

	@Test (expected = IllegalArgumentException.class)
	public void testValidateArray2() { validate(dd, -10, 9); }
	
	
	@Test (expected = IllegalArgumentException.class)
	public void testValidate1() { validate(-11, -10, 10); }

	@Test (expected = IllegalArgumentException.class)
	public void testValidate2() { validate(11, -10, 10); }

	
	@Test (expected = NullPointerException.class)
	public void testArraySelectNPE1() { arraySelect(null, new int[0]); }
	
	@Test (expected = NullPointerException.class)
	public void testArraySelectNPE2() { arraySelect(new int[0], null); }
	
	@Test (expected = IllegalArgumentException.class)
	public void testArraySelectIAE1() { arraySelect(new Object(), new int[0]); }

	@Test (expected = IllegalArgumentException.class)
	public void testArraySelectIAE2() { arraySelect(new double[0], new int[0]); }

	@Test (expected = IndexOutOfBoundsException.class)
	public void testArraySelectIOOB1() { arraySelect(new double[10], new int[] {-2}); }
	
	@Test (expected = IndexOutOfBoundsException.class)
	public void testArraySelectIOOB2() { arraySelect(new double[10], new int[] {10}); }
	
	@Test
	public void testArraySelect() {
		int[] result = new int[] { -10, 5, 17, 2010 };
		int[] array = new int[] { -4, -10, 9, -20, 5, 17, 3000, 2010 };
		int[] idx = new int[] { 1, 4, 5, 7};
		assertTrue(Arrays.equals(
			(int[]) DataUtils.arraySelect(array, idx), result));
	}

	// length = 20
	private double[] nnDat = {2,3,3,8,8,8,9,4,10,17,18,18,5,7,12,20,11,3,2,6};
	private double[] nnDat_null = null;
	private double[] nnDat_empty = {};
	private double[] nnDat_repeat = {2,2,3,3,3,3,3,3,4,4};
	private double[] nnDat_repeatSmall = {3,3,3,3};
	
	@Test (expected = NullPointerException.class)
	public void testNNhistNPE() { nearestNeighborHist(nnDat_null, 0, 2); }
	@Test (expected = IllegalArgumentException.class)
	public void testNNhistIAE() { nearestNeighborHist(nnDat_empty, 0, 2); }
	@Test (expected = IllegalArgumentException.class)
	public void testNNhistIAE2() { nearestNeighborHist(nnDat, 0, -1); }
	@Test (expected = IllegalArgumentException.class)
	public void testNNhistIAE3() { nearestNeighborHist(nnDat, 21, -1); }
	@Test
	public void testNNhistNull() {
		assertNull(nearestNeighborHist(nnDat, 19, 3));
		assertNull(nearestNeighborHist(nnDat, 18, 4));
	}
	@Test
	public void testNNhist() {
		double delta = 0.0001;
		
		XY_DataSet xy = nearestNeighborHist(nnDat, 1, 3);
		assertTrue(xy.getNum() == 6);
		assertEquals(1.5, xy.getY(0), delta);
		assertEquals(3.0, xy.getY(1), delta);
		assertEquals(1.0, xy.getY(2), delta);
		assertEquals(3.0, xy.getY(3), delta);
		assertEquals(1.0, xy.getY(4), delta);
		assertEquals(0.4286, xy.getY(5), delta);
		assertEquals(2.0, xy.getX(0), delta);
		assertEquals(3.0, xy.getX(1), delta);
		assertEquals(6.0, xy.getX(2), delta);
		assertEquals(8.0, xy.getX(3), delta);
		assertEquals(10.0, xy.getX(4), delta);
		assertEquals(17.0, xy.getX(5), delta);
		
		xy = nearestNeighborHist(nnDat, 1, 4);
		assertTrue(xy.getNum() == 5);
		assertEquals(2.0, xy.getY(0), delta);
		assertEquals(1.3333, xy.getY(1), delta);
		assertEquals(2.0, xy.getY(2), delta);
		assertEquals(1.0, xy.getY(3), delta);
		assertEquals(0.5, xy.getY(4), delta);
		assertEquals(2.5, xy.getX(0), delta);
		assertEquals(4.5, xy.getX(1), delta);
		assertEquals(8.0, xy.getX(2), delta);
		assertEquals(10.5, xy.getX(3), delta);
		assertEquals(18.0, xy.getX(4), delta);
		
		// testing repeat values that would have infinite bin values; results
		// in shorter output data set
		xy = nearestNeighborHist(nnDat_repeat, 1, 4);
		assertTrue(xy.getNum() == 1);
		
		// testing small array of repeat values that would have infinite bin
		// values and results in an empty output data set
		xy = nearestNeighborHist(nnDat_repeatSmall, 3, 4);
		assertNull(xy);
	}
	
	
	@Test
	public void testGetPercentDiff() {
		// TODO
		//fail("Not yet implemented");
	}

}
