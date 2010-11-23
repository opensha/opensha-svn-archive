package org.opensha.commons.util;

import static org.junit.Assert.*;
import static org.opensha.commons.util.DataUtils.*;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

	@Test
	public void testGetPercentDiff() {
		
		//fail("Not yet implemented");
	}

}
