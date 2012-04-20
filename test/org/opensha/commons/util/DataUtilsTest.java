package org.opensha.commons.util;

import static org.junit.Assert.*;
import static org.opensha.commons.util.DataUtils.*;
import static org.opensha.commons.util.DataUtils.Direction.ASCENDING;
import static org.opensha.commons.util.DataUtils.Direction.DESCENDING;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.opensha.commons.data.function.DefaultXY_DataSet;

import com.google.common.primitives.Doubles;

@SuppressWarnings("javadoc")
public class DataUtilsTest {

	private double[] dd = {-10, Double.NaN, 0.0, 10};
	
	@Test (expected = NullPointerException.class)
	public final void testValidateArrayNPE() { validate(null, -10, 10); }

	@Test (expected = IllegalArgumentException.class)
	public final void testValidateArray1() { validate(dd, -9, 10); }

	@Test (expected = IllegalArgumentException.class)
	public final void testValidateArray2() { validate(dd, -10, 9); }
	
	
	@Test (expected = IllegalArgumentException.class)
	public final void testValidate1() { validate(-11, -10, 10); }

	@Test (expected = IllegalArgumentException.class)
	public final void testValidate2() { validate(11, -10, 10); }

	
	@Test (expected = NullPointerException.class)
	public final void testArraySelectNPE1() { arraySelect(null, new int[0]); }
	
	@Test (expected = NullPointerException.class)
	public final void testArraySelectNPE2() { arraySelect(new int[0], null); }
	
	@Test (expected = IllegalArgumentException.class)
	public final void testArraySelectIAE1() { arraySelect(new Object(), new int[0]); }

	@Test (expected = IllegalArgumentException.class)
	public final void testArraySelectIAE2() { arraySelect(new double[0], new int[0]); }

	@Test (expected = IndexOutOfBoundsException.class)
	public final void testArraySelectIOOB1() { arraySelect(new double[10], new int[] {-2}); }
	
	@Test (expected = IndexOutOfBoundsException.class)
	public final void testArraySelectIOOB2() { arraySelect(new double[10], new int[] {10}); }
	
	@Test
	public final void testArraySelect() {
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
	public final void testNNhistNPE() { nearestNeighborHist(nnDat_null, 0, 2); }
	@Test (expected = IllegalArgumentException.class)
	public final void testNNhistIAE() { nearestNeighborHist(nnDat_empty, 0, 2); }
	@Test (expected = IllegalArgumentException.class)
	public final void testNNhistIAE2() { nearestNeighborHist(nnDat, 0, -1); }
	@Test (expected = IllegalArgumentException.class)
	public final void testNNhistIAE3() { nearestNeighborHist(nnDat, 21, -1); }
	@Test
	public final void testNNhistNull() {
		assertNull(nearestNeighborHist(nnDat, 19, 3));
		assertNull(nearestNeighborHist(nnDat, 18, 4));
	}
	@Test
	public final void testNNhist() {
		double delta = 0.0001;
		
		DefaultXY_DataSet xy = nearestNeighborHist(nnDat, 1, 3);
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
	
	
	// new set of utils methods
	
	private double[] utility = {1,2,3,4,5};	

	@Test
	public final void testScale() {
		double[] dat = Arrays.copyOf(utility, utility.length);
		DataUtils.scale(5, dat);
		assertArrayEquals(new double[] {5,10,15,20,25}, dat, 0.0);
	}
	
	@Test(expected=NullPointerException.class)
	public final void testTransformNPE() {
		DataUtils.scale(5, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public final void testTransformIAE() {
		DataUtils.scale(5);
	}
	
	@Test
	public final void testAdd() {
		double[] dat = Arrays.copyOf(utility, utility.length);
		DataUtils.add(5, dat);
		assertArrayEquals(new double[] {6,7,8,9,10}, dat, 0.0);
	}

	@Test
	public final void testAbs() {
		double[] dat = Arrays.copyOf(utility, utility.length);
		DataUtils.scale(-1, dat);
		DataUtils.abs(dat);
		assertArrayEquals(new double[] {1,2,3,4,5}, dat, 0.0);
	}
	
	
	
	@Test(expected=NullPointerException.class)
	public final void testDiffNPE1() {
		DataUtils.diff(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public final void testDiffIAE() {
		DataUtils.diff(new double[0]);
	}

	@Test
	public final void testDiff() {
		double[] dat = Arrays.copyOf(utility, utility.length);
		double[] diff = DataUtils.diff(dat);
		
		// test size of returned array
		assertTrue(diff.length == (dat.length - 1));
		
		// test all values = 1
		assertTrue(Doubles.min(diff) == 1 && Doubles.max(diff) == 1);
		
		double[] d1 = {1,2,3,3,4,7};
		double[] diff1 = DataUtils.diff(d1);
		assertArrayEquals(new double[] {1,1,0,1,3}, diff1, 0);
		
		double[] d2 = {-3,8,0,3,4,2,5,5,1};
		double[] diff2 = DataUtils.diff(d2);
		assertArrayEquals(new double[] {11,-8,3,1,-2,3,0,-4}, diff2, 0);
		
	}
	
	@Test
	public final void testFlip() {
		double[] actual = {-2, -1, 0, 1, 2};
		double[] expect = Arrays.copyOf(actual, actual.length);
		Collections.reverse(Doubles.asList(expect));
		assertArrayEquals(expect, DataUtils.flip(actual), 0);
	}	

	
	@Test
	public final void testIsMonotonic() {
		
		double[] d1 = {-2, -1, 0, 1, 2};
		double[] d2 = {-2, -1, 0, 0, 1, 2};
		double[] d3 = {0, 1, 2, 2, 1, 0};
		
		double[] d4 = Arrays.copyOf(d1, d1.length);
		Collections.reverse(Doubles.asList(d4));
		double[] d5 = Arrays.copyOf(d2, d2.length);
		Collections.reverse(Doubles.asList(d5));
		double[] d6 = DataUtils.add(2, DataUtils.flip(d3));
		
		
		assertTrue(DataUtils.isMonotonic(ASCENDING, false, d1));
		assertTrue(DataUtils.isMonotonic(ASCENDING, true, d1));
		assertTrue(DataUtils.isMonotonic(ASCENDING, true, d2));
		assertFalse(DataUtils.isMonotonic(ASCENDING, false, d2));
		assertFalse(DataUtils.isMonotonic(ASCENDING, false, d3));
		assertFalse(DataUtils.isMonotonic(ASCENDING, true, d3));
		
		assertTrue(DataUtils.isMonotonic(DESCENDING, false, d4));
		assertTrue(DataUtils.isMonotonic(DESCENDING, true, d4));
		assertTrue(DataUtils.isMonotonic(DESCENDING, true, d5));
		assertFalse(DataUtils.isMonotonic(DESCENDING, false, d5));
		assertFalse(DataUtils.isMonotonic(DESCENDING, false, d6));
		assertFalse(DataUtils.isMonotonic(DESCENDING, true, d6));
	}
	
	
	@Test
	public final void testPercentDiff() {
		assertEquals(2.0, DataUtils.getPercentDiff(98, 100), 0.0);
		assertEquals(2.0, DataUtils.getPercentDiff(102, 100), 0.0);
		assertEquals(Double.POSITIVE_INFINITY,DataUtils.getPercentDiff(1, 0), 0.0);
		assertEquals(0.0, DataUtils.getPercentDiff(0, 0), 0.0);
		assertEquals(Double.NaN, DataUtils.getPercentDiff(Double.NaN, 0), 0.0);
		assertEquals(Double.NaN, DataUtils.getPercentDiff(0, Double.NaN), 0.0);
	}
	
	
	
	
//	public static void main(String[] args) {
//		// Function speed test
//		// prob needs memory increase
//
//		int size = 100000000;
//		Stopwatch sw = new Stopwatch();
//		sw.start();
//		double[] d1 = new double[size];
//		double[] d2 = new double[size];
//		for (int i = 0; i < size; i++) {
//			d1[i] = Math.random();
//			d2[i] = Math.random();
//		}
//		sw.stop();
//		System.out.println(sw.elapsedMillis());
//
//		sw.reset().start();
//		scale2(12.57, d2);
//		sw.stop();
//		System.out.println(sw.elapsedMillis());
//		sw.reset().start();
//		DataUtils.scale(12.57, d2);
//		sw.stop();
//		System.out.println(sw.elapsedMillis());
//		System.out.println(d1[3]);
//	}
//
//	public static double[] scale2(double scale, double... array) {
//		checkNotNull(array);
//		checkArgument(array.length > 0);
//		for (int i = 0; i < array.length; i++) {
//			array[i] = array[i] * scale;
//		}
//		return array;
//	}


}
