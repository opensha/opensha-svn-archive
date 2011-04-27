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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.stat.StatUtils;
import org.opensha.commons.data.function.XY_DataSet;

import com.google.common.primitives.Ints;

/**
 * This class provides various data processing utilities.
 * 
 * @author Peter Powers
 * @version $Id$
 */
public class DataUtils {

    private static Random random;

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
     * Creates an array of random <code>double</code> values. Method re-seeds
     * the random generator with the current time on each call.
     * @param length of output array
     * @return the array of random <code>double</code>s
     */
    public static double[] randomValues(int length) {
    	if (random == null) random = new Random();
        double[] sequence = new double[length];
        random.setSeed(System.currentTimeMillis());
        for (int i=0; i<length; i++) {
            sequence[i] = random.nextFloat();
        }
        return sequence;
    }

	/**
	 * Nearest neighbor binning algorithm after Silverman, B. W. (1986),
	 * <em>Density Estimation for Statistics and Data Analysis</em>, Chapman
	 * &amp; Hall, New York. This method is a density estimator that uses
	 * variable width binning with a fixed sample size per bin that better
	 * reflects the distribution of the underlying data. It is particularly
	 * useful when workgin with power-law distributed data. Bin widths are
	 * computed as the difference between the last values in adjacent bins. In
	 * the case of the 1st bin, the supplied origin is taken as the "last value"
	 * of the previous bin. Bin positions are set from the median value in each
	 * bin. Note that the supplied <code>data</code> is not modified; this
	 * method uses a copy internally. In most cases, data will be fairly
	 * continuous in X, however, for small <code>size</code>s it's possible to
	 * have bins of identical values such that corresponding bin value is
	 * Infinity. Such values are not included in the resultant data set.
	 * 
	 * @param data to be binned
	 * @param origin for binning
	 * @param size of each bin
	 * @return an <code>XY_DataSet</code> of the binned distribution or
	 *         <code>null</code> if the binned distribution is empty
	 * @throws NullPointerException if the supplied <code>data</code> is
	 *         <code>null</code>
	 * @throws IllegalArgumentException if supplied <code>data</code> is empty,
	 *         the bin <code>size</code> is &lt;1, or the <code>origin</code> is
	 *         greater than all <code>data</code> values
	 */
	public static XY_DataSet nearestNeighborHist(double[] data, double origin,
			int size) {
		checkNotNull(data, "Supplied data is null");
		checkArgument(data.length > 0, "Supplied data is empty");
		checkArgument(size > 0, "Bin size can't be less than 1");
		double[] localData = Arrays.copyOf(data, data.length);
		Arrays.sort(localData);
		int startIdx = Arrays.binarySearch(localData, origin);
		checkArgument(startIdx < localData.length,
			"Origin is greater than all data values");
		startIdx = (startIdx > 0) ? startIdx : -startIdx - 1;
		// for multipe identical values, binary search may not return
		// the lowest index so walk down
		while (startIdx > 0 && origin == localData[startIdx-1]) startIdx--;
		// trim data
		localData = Arrays.copyOfRange(localData, startIdx, localData.length);
		int binCount = (int) Math.floor(localData.length / size);
		// bail on an empty distribution
		if (binCount == 0) return null;
		List<Double> x = new ArrayList<Double>();
		List<Double> y = new ArrayList<Double>();
		double binLo, binHi, binDelta;
		for (int i = 0; i < binCount; i++) {
			int datIdx = i * size;
			binLo = (i == 0) ? origin : localData[datIdx-1];
			binHi = localData[datIdx + size - 1];
			binDelta = binHi - binLo;
			// bail on intervals of identical values
			if (binDelta == 0) continue;
			y.add(size / (binHi - binLo));
			x.add(StatUtils.percentile(localData, datIdx, size, 50.0));
		}
		// bail on empty distribution
		return (x.isEmpty()) ? null : new XY_DataSet(x, y);
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
		
		System.out.println(StatUtils.percentile(new double[]{5,3,3,5,3}, 50.0));
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
