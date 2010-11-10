package org.opensha.commons.eq.cat.util;

import static org.opensha.commons.eq.cat.util.DataType.DEPTH;
import static org.opensha.commons.eq.cat.util.DataType.LATITUDE;
import static org.opensha.commons.eq.cat.util.DataType.LONGITUDE;
import static org.opensha.commons.eq.cat.util.DataType.MAGNITUDE;

import static org.opensha.commons.geo.GeoTools.*;
import static org.opensha.commons.eq.cat.CatTools.*;

import org.opensha.commons.eq.cat.filters.CatalogBrush;

/**
 * Values used to identify bounds on the <code>double</code> valued
 * <code>DataType</code>s: [LONGITUDE, LATITUDE, DEPTH, MAGNITUDE]. These keys
 * provide convenient access to tha absolute limit of each value and may be used to
 * identify upper and lower bounds when brushing catalog data.
 * 
 * @author Peter Powers
 * @version $Id:$
 * @see CatalogBrush
 */
public enum DataBound {

	MIN,
	MAX;
	
//	/** Minimum catalog latitude identifier. */
//	MIN_LAT(LATITUDE, LAT_MIN),
//	/** Maximum catalog latitude identifier. */
//	MAX_LAT(LATITUDE, LAT_MAX),
//	/** Minimum catalog longitude identifier. */
//	MIN_LON(LONGITUDE, LON_MIN),
//	/** Maximum catalog longitude identifier. */
//	MAX_LON(LONGITUDE, LON_MAX),
//	/** Minimum catalog depth identifier. */
//	MIN_DEPTH(DEPTH, DEPTH_MIN),
//	/** Maximum catalog depth identifier. */
//	MAX_DEPTH(DEPTH, DEPTH_MAX),
//	/** Minimum catalog magnitude identifier. */
//	MIN_MAG(MAGNITUDE, MAG_MIN),
//	/** Maximum catalog magnitude identifier. */
//	MAX_MAG(MAGNITUDE, MAG_MAX);
//
//	private DataType parent;
//	private double limit;
//
//	/*
//	 * Parent can be initialized via constructor, however complement is self
//	 * referential and can only be set after initialization
//	 */
//	private DataBound(DataType parent, double limit) {
//		this.parent = parent;
//		this.limit = limit;
//	}
//
//	/**
//	 * Returns the parent <code>DataType</code> for this bound (e.g. the parent
//	 * of <code>MIN_LAT</code> is <code>DataType.LATITUDE</code>)
//	 * 
//	 * @return the parent <code>DataType</code>
//	 */
//	public DataType parent() {
//		return parent;
//	}
//	
//	/**
//	 * Returns the extreme value for this bound (e.g. the extreme limit of
//	 * <code>MIN_LAT</code> is 90.0).
//	 * @return the absolut limit 
//	 */
//	public double extreme() {
//		return extreme;
//	}
//
//	/**
//	 * Returns the complement of this bound (e.g. the complement of
//	 * <code>MIN_LAT</code> is <code>MAX_LAT</code>.
//	 * 
//	 * @return the bound complement
//	 */
//	public DataBound complement() {
//		switch (this) {
//			case MAX_LAT:
//				return MIN_LAT;
//			case MIN_LAT:
//				return MAX_LAT;
//			case MAX_LON:
//				return MIN_LON;
//			case MIN_LON:
//				return MAX_LON;
//			case MAX_DEPTH:
//				return MIN_DEPTH;
//			case MIN_DEPTH:
//				return MAX_DEPTH;
//			case MAX_MAG:
//				return MIN_MAG;
//			case MIN_MAG:
//				return MAX_MAG;
//			default:
//				return null;
//		}
//	}
//
//	/**
//	 * Returns the minimum <code>DataBound</code> identifier for the requested
//	 * <code>type</code>.
//	 * 
//	 * @param type requested
//	 * @return the minimum bound identifier
//	 */
//	public DataBound minBound(DataType type) {
//		switch (type) {
//			case LATITUDE:
//				return MIN_LAT;
//			case LONGITUDE:
//				return MIN_LON;
//			case DEPTH:
//				return MIN_DEPTH;
//			case MAGNITUDE:
//				return MIN_MAG;
//			default:
//				return null;
//		}
//	}
//
//	/**
//	 * Returns the maximum <code>DataBound</code> identifier for the requested
//	 * <code>type</code>.
//	 * 
//	 * @param type requested
//	 * @return the maximum bound identifier
//	 */
//	public DataBound maxBound(DataType type) {
//		switch (type) {
//			case LATITUDE:
//				return MAX_LAT;
//			case LONGITUDE:
//				return MAX_LON;
//			case DEPTH:
//				return MAX_DEPTH;
//			case MAGNITUDE:
//				return MAX_MAG;
//			default:
//				return null;
//		}
//	}

}
