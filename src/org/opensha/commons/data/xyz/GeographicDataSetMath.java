package org.opensha.commons.data.xyz;

import org.opensha.commons.geo.Location;

/**
 * This class does math for Geographic datasets. Comparisons are done in Locations instead of Point2D's.
 * 
 * @author kevin
 *
 */
public class GeographicDataSetMath extends XYZ_DataSetMath {
	
	/**
	 * Returns new <code>GeographicDataSetAPI</code> that represents the values in the
	 * two given maps added together.
	 * @param map1
	 * @param map2
	 * @return
	 */
	public static GeographicDataSetAPI add(GeographicDataSetAPI map1, GeographicDataSetAPI map2) {
		ArbDiscrGeographicDataSet sum = new ArbDiscrGeographicDataSet(map1.isLatitudeX());
		
		for (int i=0; i<map1.size(); i++) {
			Location loc = map1.getLocation(i);
			double val1 = map1.get(i);
			int map2Index = map2.indexOf(loc);
			if (map2Index >= 0) {
				double val2 = map2.get(map2Index);
				sum.set(loc, val1 + val2);
			}
		}
		return sum;
	}
	
	/**
	 * Returns new <code>GeographicDataSetAPI</code> that represents the values in the
	 * minuend map minus the values in the subtrahend map.
	 * @param map1
	 * @param map2
	 * @return
	 */
	public static GeographicDataSetAPI subtract(GeographicDataSetAPI minuend, GeographicDataSetAPI subtrahend) {
		ArbDiscrGeographicDataSet difference = new ArbDiscrGeographicDataSet(minuend.isLatitudeX());
		
		for (int i=0; i<minuend.size(); i++) {
			Location loc = minuend.getLocation(i);
			double val1 = minuend.get(i);
			int map2Index = subtrahend.indexOf(loc);
			if (map2Index >= 0) {
				double val2 = subtrahend.get(map2Index);
				difference.set(loc, val1 - val2);
			}
		}
		
		return difference;
	}
	
	/**
	 * Returns new <code>GeographicDataSetAPI</code> that represents the values in the
	 * two given maps multiplied together.
	 * @param map1
	 * @param map2
	 * @return
	 */
	public static GeographicDataSetAPI multiply(GeographicDataSetAPI map1, GeographicDataSetAPI map2) {
		ArbDiscrGeographicDataSet product = new ArbDiscrGeographicDataSet(map1.isLatitudeX());
		
		for (int i=0; i<map1.size(); i++) {
			Location loc = map1.getLocation(i);
			double val1 = map1.get(i);
			int map2Index = map2.indexOf(loc);
			if (map2Index >= 0) {
				double val2 = map2.get(map2Index);
				product.set(loc, val1 * val2);
			}
		}
		return product;
	}
	
	/**
	 * Returns new <code>GeographicDataSetAPI</code> that represents the values in the
	 * minuend map minus the values in the subtrahend map.
	 * @param map1
	 * @param map2
	 * @return
	 */
	public static GeographicDataSetAPI divide(GeographicDataSetAPI dividend, GeographicDataSetAPI divisor) {
		ArbDiscrGeographicDataSet quotient = new ArbDiscrGeographicDataSet(dividend.isLatitudeX());
		
		for (int i=0; i<dividend.size(); i++) {
			Location loc = dividend.getLocation(i);
			double val1 = dividend.get(i);
			int map2Index = divisor.indexOf(loc);
			if (map2Index >= 0) {
				double val2 = divisor.get(map2Index);
				quotient.set(loc, val1 / val2);
			}
		}
		
		return quotient;
	}

}
