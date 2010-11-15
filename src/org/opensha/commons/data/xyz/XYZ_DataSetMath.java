package org.opensha.commons.data.xyz;

import java.awt.geom.Point2D;

/**
 * This class is used to perform common math operations on and between XYZ datasets.
 * 
 * @author kevin
 *
 */
public class XYZ_DataSetMath {
	
	private static boolean isGeo(XYZ_DataSetAPI map1, XYZ_DataSetAPI map2) {
		if (map1 instanceof GeographicDataSetAPI && map2 instanceof GeographicDataSetAPI)
			return true;
		return false;
	}
	
	/**
	 * Returns new <code>GeographicDataSetAPI</code> that represents the values in the
	 * two given maps added together.
	 * @param map1
	 * @param map2
	 * @return
	 */
	public static XYZ_DataSetAPI add(XYZ_DataSetAPI map1, XYZ_DataSetAPI map2) {
		if (isGeo(map1, map2))
			return GeographicDataSetMath.add((GeographicDataSetAPI)map1, (GeographicDataSetAPI)map2);
		ArbDiscrXYZ_DataSet sum = new ArbDiscrXYZ_DataSet();
		
		for (int i=0; i<map1.size(); i++) {
			Point2D point = map1.getPoint(i);
			double val1 = map1.get(i);
			int map2Index = map2.indexOf(point);
			if (map2Index >= 0) {
				double val2 = map2.get(map2Index);
				sum.set(point, val1 + val2);
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
	public static XYZ_DataSetAPI subtract(XYZ_DataSetAPI minuend, XYZ_DataSetAPI subtrahend) {
		if (isGeo(minuend, subtrahend))
			return GeographicDataSetMath.subtract((GeographicDataSetAPI)minuend, (GeographicDataSetAPI)subtrahend);
		ArbDiscrXYZ_DataSet difference = new ArbDiscrXYZ_DataSet();
		
		for (int i=0; i<minuend.size(); i++) {
			Point2D point = minuend.getPoint(i);
			double val1 = minuend.get(i);
			int map2Index = subtrahend.indexOf(point);
			if (map2Index >= 0) {
				double val2 = subtrahend.get(map2Index);
				difference.set(point, val1 - val2);
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
	public static XYZ_DataSetAPI multiply(XYZ_DataSetAPI map1, XYZ_DataSetAPI map2) {
		if (isGeo(map1, map2))
			return GeographicDataSetMath.multiply((GeographicDataSetAPI)map1, (GeographicDataSetAPI)map2);
		ArbDiscrXYZ_DataSet product = new ArbDiscrXYZ_DataSet();
		
		for (int i=0; i<map1.size(); i++) {
			Point2D point = map1.getPoint(i);
			double val1 = map1.get(i);
			int map2Index = map2.indexOf(point);
			if (map2Index >= 0) {
				double val2 = map2.get(map2Index);
				product.set(point, val1 * val2);
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
	public static XYZ_DataSetAPI divide(XYZ_DataSetAPI dividend, XYZ_DataSetAPI divisor) {
		if (isGeo(dividend, divisor))
			return GeographicDataSetMath.divide((GeographicDataSetAPI)dividend, (GeographicDataSetAPI)divisor);
		ArbDiscrXYZ_DataSet quotient = new ArbDiscrXYZ_DataSet();
		
		for (int i=0; i<dividend.size(); i++) {
			Point2D point = dividend.getPoint(i);
			double val1 = dividend.get(i);
			int map2Index = divisor.indexOf(point);
			if (map2Index >= 0) {
				double val2 = divisor.get(map2Index);
				quotient.set(point, val1 / val2);
			}
		}
		
		return quotient;
	}
	
	/**
	 * Takes the absolute value of the given map. Changes are done in place, nothing is returned.
	 * 
	 * @param map
	 */
	public static void abs(XYZ_DataSetAPI map) {
		for (int i=0; i<map.size(); i++) {
			map.set(i, Math.abs(map.get(i)));
		}
	}
	
	/**
	 * Takes the natural log of the given map. Changes are done in place, nothing is returned.
	 * 
	 * @param map
	 */
	public static void log(XYZ_DataSetAPI map) {
		for (int i=0; i<map.size(); i++) {
			map.set(i, Math.log(map.get(i)));
		}
	}
	
	/**
	 * Takes the natural log base 10 of the given map. Changes are done in place, nothing is returned.
	 * 
	 * @param map
	 */
	public static void log10(XYZ_DataSetAPI map) {
		for (int i=0; i<map.size(); i++) {
			map.set(i, Math.log10(map.get(i)));
		}
	}
	
	/**
	 * Euler's number e raised to the power of each value in the given map. Changes are done in place, nothing is returned.
	 * 
	 * @param map
	 */
	public static void exp(XYZ_DataSetAPI map) {
		for (int i=0; i<map.size(); i++) {
			map.set(i, Math.exp(map.get(i)));
		}
	}
	
	/**
	 * Each value in the map is raised to the given power. Changes are done in place, nothing is returned.
	 * 
	 * @param map
	 */
	public static void pow(XYZ_DataSetAPI map, double pow) {
		for (int i=0; i<map.size(); i++) {
			map.set(i, Math.pow(map.get(i), pow));
		}
	}
	
	/**
	 * Each value in the map is scaled by the given scalar. Changes are done in place, nothing is returned.
	 * 
	 * @param map
	 */
	public static void scale(XYZ_DataSetAPI map, double scalar) {
		for (int i=0; i<map.size(); i++) {
			map.set(i, map.get(i) * scalar);
		}
	}
	
	/**
	 * The given value is added to each value in the map. Changes are done in place, nothing is returned.
	 * 
	 * @param map
	 */
	public static void add(XYZ_DataSetAPI map, double value) {
		for (int i=0; i<map.size(); i++) {
			map.set(i, map.get(i) + value);
		}
	}

}
