package org.opensha.commons.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testValidateDoubleArrayDoubleDouble() {
		//fail("Not yet implemented");
	}

	@Test
	public void testValidateDoubleDoubleDouble() {
		//fail("Not yet implemented");
	}

	@Test
	public void testArraySelect() {
		try {
			DataUtils.arraySelect(null, null);
			fail("Null array not caught");
		} catch (NullPointerException e) {}
		try {
			DataUtils.arraySelect(new Object(), null);
			fail("Non-array not caught");
		} catch (IllegalArgumentException e) {}
		try {
			DataUtils.arraySelect(new double[0], null);
			fail("Empty array not caught");
		} catch (IllegalArgumentException e) {}
		try {
			DataUtils.arraySelect(new double[1], null);
			fail("Null index array not caught");
		} catch (NullPointerException e) {}
		try {
			DataUtils.arraySelect(new double[10], new int[] {-2 });
			fail("Index out of bounds caught");
		} catch (ArrayIndexOutOfBoundsException e) {}
		try {
			DataUtils.arraySelect(new double[10], new int[] { 10 });
			fail("Index out of bounds caught");
		} catch (ArrayIndexOutOfBoundsException e) {}
		int[] result = new int[] { -10, 5, 17, 2010 };
		int[] array = new int[] { -4, -10, 9, -20, 5, 17, 3000, 2010 };
		int[] idx = new int[] { 1, 4, 5, 7};
		assertTrue(Arrays.equals(
			(int[]) DataUtils.arraySelect(array, idx), result));
	}

	@Test
	public void testGetPercentDiff() {
		//fail("Not yet implemented");
	}

}
