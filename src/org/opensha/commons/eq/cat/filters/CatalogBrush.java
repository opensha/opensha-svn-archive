package org.opensha.commons.eq.cat.filters;

import static org.opensha.commons.eq.cat.util.DataType.*;
import static org.opensha.commons.eq.cat.CatTools.*;
import static org.opensha.commons.geo.GeoTools.*;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opensha.commons.eq.cat.MutableCatalog;
import org.opensha.commons.eq.cat.util.DataBound;
import org.opensha.commons.eq.cat.util.DataType;
import org.opensha.commons.geo.GeoTools;
import org.opensha.commons.util.DataUtils;

/**
 * Add comments here
 * 
 * memory overhead
 * creates sorted copies of data
 * All methods in this class fail silently
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class CatalogBrush {

	private MutableCatalog catalog;
	private EnumSet<DataType> brushables = EnumSet.of(
		TIME, LATITUDE, LONGITUDE, DEPTH, MAGNITUDE);
	
	private Map<DataType, double[]> sortedSources;
	private Map<DataType, int[]> sourceIndices;
	private Map<DataType, Integer> minCarets; // inclusive
	private Map<DataType, Integer> maxCarets; // inclusive
	private Map<DataType, Double> minValues;
	private Map<DataType, Double> maxValues;
	
	/**
	 * Initializes a new data brush with the supplied catalog.
	 * @param catalog
	 */
	public CatalogBrush(MutableCatalog catalog) {
		this.catalog = catalog;
		brushables.retainAll(catalog.getDataTypes());
		initMaps();
	}
	
	/**
	 * Adjusts the catalog selection. Returns whether the requested change had
	 * an effect. If the supplied brush <code>key</code> is not applicable
	 * for the associated catalog (i.e. catalog does not contain data
	 * corresponding to the <code>key</code>), or the supplied value is out of
	 * range for the key or the key's compelement, no catalog selection change
	 * occurs and this method returns <code>false</code>.
	 * @param type of data to adjust
	 * @param key to adjust
	 * @param value for key
	 * @return <code>true</code> if catalog selection has changed, 
	 * <code>false</code> otherwise
	 */
	public boolean adjust(DataType type, Key key, double value) {
		if (!brushables.contains(type)) return false;
		if (!validateValue(type, value)) return false;
		if (key == Key.MIN) {
			return adjustMin(type, value);
		} else {
			return adjustMax(type, value);
		}
		
		// determine index
//		int idx = Arrays.binarySearch(sortedSources.get(type), value);
//		
//		// need to prevent brush overlap
//		if (key == Key.MIN) {
//			
//		}
//		
//		
//		return false;
	}
	
	// binary search is inclusive (min) and exclusive (max)
	// when carat is at min or max, we want to avoid specifying 0 length of array to search which always returns -1
	
	// caratMin can never == caratMax and vice versa
	
	private boolean adjustMin(DataType type, double value) {
		double currentMin = minValues.get(type);
		double catalogMin = catalog.minForType(type);
		
		double[] source = sortedSources.get(type);
		int minCaret = minCarets.get(type);
		int maxCaret = maxCarets.get(type);
		
		int idx = adjustMin(source, minCaret, maxCaret, value);
		if (idx == -1) return false;
		
//		int idx = Arrays.binarySearch(source, value);
//		if (idx == minCaret) return false;
//		// adjust values outside source array limits
//		idx = (idx == -1) ? 0 : (idx < -1) ? -idx - 1 : idx;
//		// prevent min caret from equalling or passing max caret 
//		idx = (idx >= maxCaret) ? maxCaret - 1 : idx;
//		// adjust values down so that all equal to value are included
//		while (source[idx]-1 == value) {
//			idx--;
//		}
		
//		minCarets
//			
//		} else if
//		int newCarat = 
//		
//		
//		if (value == currentMin) {
//			return false;
//		} else if (value <= catalogMin) {
//			minCarats.put(type, 0);
//			// TODO update selection;
//			return true;
//		} else {
//			idx = Arrays.binarySearch(sortedSources.get(type), 0, minCarats.get(type)+1, value);
//			
//		} else {
//			idx = Arrays.binarySearch(sortedSources.get(type), minCarats.get(type), maxCarats.get(type), value);
//		}
//		//if 
//		//int idx = Arrays.binarySearch(sortedSources.get(type), value);
//		
		return false;
	}
	
	private static int adjustMin(double[] data, int minCaret, int maxCaret, double value) {
		int idx = Arrays.binarySearch(data, value);
		if (idx == minCaret) return -1;
		// adjust values outside source array limits
		idx = (idx == -1) ? 0 : (idx < -1) ? -idx - 1 : idx;
		// prevent min caret from equalling or passing max caret 
		idx = (idx >= maxCaret) ? maxCaret - 1 : idx;
		// adjust values down so that all equal to value are included
		while (data[idx-1] == value) {
			idx--;
		}
		return idx;
	}

	private static int adjustMax(double[] data, int minCaret, int maxCaret, double value) {
		int idx = Arrays.binarySearch(data, value);
		if (idx == maxCaret) return -1;
		// adjust values outside source array limits
		idx = (idx == -1) ? 0 : (idx < -1) ? -idx - 2 : idx;
		// prevent max caret from equalling or passing min caret 
		idx = (idx <= minCaret) ? minCaret + 1 : idx;
		// adjust values up so that all equal to value are included
		while (data[idx+1] == value) {
			idx++;
		}
		return idx;
	}

	private boolean adjustMax(DataType type, double value) {
		return false;
	}
	
	
	public boolean adjust(DataBound key, long value) {
		return false;
	}
	
	public boolean[] selectionMask() {
		return null;
	}
	
	public static void main(String[] args) {
		double[] M = {0.3,0.7,1.1,1.1,1.1,1.1,1.8,2.2,2.7,2.7,2.7,2.7,2.8,3.1,3.6,4.5,4.5,4.5,4.5,4.5,4.5,4.5,4.5,5.1,6.7,7.2};
		int cMin = 0;
		int cMax = 25;
		System.out.println(adjustMax(M,cMin,cMax, 2.71));
	}
	
	/**
	 * Returns the indices of currently selected events.
	 * @return the selected catalog event indices
	 */
	public int[] selection() {
		return null;
	}
	
	/* initialize all data tracking maps */
	private void initMaps() {
		for (DataType type : brushables) {
			// retainAll in constructor already filtered out
			// data type missing from catalog
			double[] source = (double[]) catalog.copyData(type);
			sourceIndices.put(type, DataUtils.indexAndSort(source));
			sortedSources.put(type, source);
			minCarets.put(type, 0);
			maxCarets.put(type, catalog.size()-1);
			minValues.put(type, catalog.minForType(type));
			maxValues.put(type, catalog.maxForType(type));
		}
	}
	
	/* determine whether a supplied value is allowed */
	private boolean validateValue(DataType type, double value) {
		try {
			switch (type) {
				case LATITUDE: validateLat(value);
				case LONGITUDE: validateLon(value);
				case DEPTH: validateDepth(value);
				case MAGNITUDE: validateMag(value);
			}
		} catch (IllegalArgumentException iae) {
			return false;
		}
		return true;
	}

	/**
	 * Key used to identify whether DataBrush should adjust a minimum or a
	 * maximum value.
	 */
	public enum Key {

		/** Minimum value identifier. */
		MIN,
		/** Maximum value identifier. */
		MAX;
	}

}
