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

package org.opensha.commons.util;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.primitives.Ints;

/**
 * This class provides various data processing utilities.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class DataUtils {

	// no instantiation
	private DataUtils() {}

	/**
	 * Validates the domain of a <code>double</code> data set. Method verifies
	 * that data values all fall within a specified minimum and maximum range
	 * (inclusive). Empty arrays are ignored. If <code>min</code> is
	 * <code>Double.NaN</code>, no lower limit is imposed; the same holds true
	 * for <code>max</code>. <code>Double.NaN</code> values in <code>data</code>
	 * will validate.
	 * 
	 * @param data to validate
	 * @param min minimum range value
	 * @param max maximum range value
	 * @throws NullPointerException if <code>data</code> is <code>null</code>
	 * @throws IllegalArgumentException if any <code>data</code> value is out of
	 *         range
	 */
	public final static void validate(double[] data, double min, double max)
			throws IllegalArgumentException, NullPointerException {
		checkNotNull(data, "Supplied data array is null");
		double value;
		for (int i = 0; i < data.length; i++) {
			value = data[i];
			if (value > max || value < min) {
				throw new IllegalArgumentException("Data value (" + value
					+ ") at position " + (i + 1) + " is out of range.");
			}
		}
	}
		
	/**
	 * Verifies that a <code>double</code> data value falls within a specified
	 * minimum and maximum range (inclusive). If <code>min</code> is 
	 * <code>Double.NaN</code>, no lower limit is imposed; the same holds true
	 * for <code>max</code>. A value of <code>Double.NaN</code> will always
	 * validate.
	 * 
	 * @param value to check
	 * @param min minimum range value
	 * @param max minimum range value
	 * @throws IllegalArgumentException if value is out of range
	 */
	public final static void validate(double value, double min, double max) {
		if (value > max || value < min) {
			throw new IllegalArgumentException("Value (" + value
				+ ") is out of range.");
		}
	}

	/**
	 * Creates a new array from the values in a source array at the specified
	 * indices. Returned array is of same type as source.
	 * 
	 * @param array array source
	 * @param indices index values of items to select
	 * @return a new array of values at indices in source
	 * @throws NullPointerException if <code>array</code> or
	 *         <code>indices</code> are <code>null</code>
	 * @throws IllegalArgumentException if data object is not an array or if
	 *         data array is empty
	 * @throws IndexOutOfBoundsException if any indices are out of range
	 */
	public static Object arraySelect(Object array, int[] indices) {
		checkNotNull(array, "Supplied data array is null");
		checkNotNull(indices, "Supplied index array is null");
		checkArgument(array.getClass().isArray(),
			"Data object supplied is not an array");
		int arraySize = Array.getLength(array);
		checkArgument(arraySize != 0, "Supplied data array is empty");

		// validate indices
		for (int i = 0; i < indices.length; i++) {
			checkPositionIndex(indices[i], arraySize, "Supplied index");
		}

		Class<? extends Object> srcClass = array.getClass().getComponentType();
		Object out = Array.newInstance(srcClass, indices.length);
		for (int i = 0; i < indices.length; i++) {
			Array.set(out, i, Array.get(array, indices[i]));
		}
		return out;
	}

	/**
	 * Sorts the supplied data array in place and returns an <code>int[]</code>
	 * array of the original indices of the data values. For example, if the
	 * supplied array is [3, 1, 8], the supplied array will be sorted to [1, 3,
	 * 8] and the array [2, 1, 3] will be returned.
	 * 
	 * @param data array to sort
	 * @return the inidices of the unsorted array values
	 * @throws NullPointerException if source array is <code>null</code>
	 */
	public static int[] indexAndSort(final double[] data) {
		checkNotNull(data, "Source array is null");
		List<Integer> indices = Ints.asList(new int[data.length]);
		for (int i = 0; i < indices.size(); i++) {
			indices.set(i, i);
		}
		Collections.sort(indices, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				double d1 = data[i1];
				double d2 = data[i2];
				return (d1 < d2) ? -1 : (d1 == d2) ? 0 : 1;
			}
		});
		Arrays.sort(data);
		return Ints.toArray(indices);
	}

	/**
	 * Returns the percent difference between two values.
	 * TODO test,edit
	 */
//	public static double getPercentDiff2(double test, double target) {
//		return Math.abs((test - target) / target) * 100.0;
//
//
//		// double result = 0;
//		// if (targetVal != 0)
//		// result = (Math.abs(testVal - targetVal) / targetVal) * 100d;
//		//
//		// return result;
//	}
	
	public static double getPercentDiff(double testVal, double targetVal) {
		double result = 0;
		if (targetVal != 0)
			result = (Math.abs(testVal - targetVal) / targetVal) * 100d;

		return result;
	}
// TODO clean
	public static void main(String[] args) {
//		System.out.println(getPercentDiff1(10,10) + " " + getPercentDiff2(10,10));
//		System.out.println(getPercentDiff1(10.0000000000001,10) + " " + getPercentDiff2(10.0000000000001,10));
//		System.out.println(getPercentDiff1(10,10.0000000000001) + " " + getPercentDiff2(10,10.0000000000001));
//		System.out.println(getPercentDiff1(10,0) + " " + getPercentDiff2(10.0,0.0));
//		System.out.println(getPercentDiff1(0,10) + " " + getPercentDiff2(0,10));
//		System.out.println(getPercentDiff1(Double.NaN,10) + " " + getPercentDiff2(Double.NaN,10));
//		System.out.println(getPercentDiff1(10,Double.NaN) + " " + getPercentDiff2(10,Double.NaN));
//		
//		double d1 = 10.0;
//		double d2 = 0.0;
//		try {
//			double dd = Math.abs((d1 - d2) / d2) * 100.0;
//			System.out.println(dd);
//		} catch (ArithmeticException e) {
//			System.out.println("hi");
//			//return (test == 0.0) ? 0.0 : 100.0;
//		}
//		System.out.println(Math.abs((10d - 0d) / 0d) * 100.0);
		
		//getPercentDiff(0.)
		
	}
	

	// TODO test
	/**
	 * Class for tracking the minimum and maximum values of a set of data.
	 */
	public static class MinMaxAveTracker {
		private double min = Double.POSITIVE_INFINITY;
		private double max = Double.NEGATIVE_INFINITY;
		private double tot = 0;
		private int num = 0;

		/**
		 * Add a new value to the tracker. Min/Max/Average will be updated.
		 * 
		 * @param val value to be added
		 */
		public void addValue(double val) {
			if (val < min) min = val;
			if (val > max) max = val;
			tot += val;
			num++;
		}

		/**
		 * Returns the minimum value that has been added to this tracker, or positive infinity if
		 * no values have been added.
		 * 
		 * @return minimum value
		 */
		public double getMin() {
			return min;
		}

		/**
		 * Returns the maximum value that has been added to this tracker, or negative infinity if
		 * no values have been added.
		 * 
		 * @return maximum value
		 */
		public double getMax() {
			return max;
		}

		/**
		 * Computes the average of all values that have been added to this tracker.
		 * 
		 * @return the average of all values that have been added to this tracker.
		 */
		public double getAverage() {
			return tot / (double) num;
		}

		/**
		 * 
		 * @return total number of values added to this tracker.
		 */
		public int getNum() {
			return num;
		}

		@Override
		public String toString() {
			return "min: " + min + ", max: " + max + ", avg: " + getAverage();
		}
	}

}
