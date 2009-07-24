package org.opensha.commons.data.region;

/**
 * A border type is required for the initialization of some 
 * <code>GeographicRegion</code>s. 
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 * @see GeographicRegion
 */
public enum BorderType {

	/** 
	 * Defines a {@link GeographicRegion} border as following a straight
	 * line in a Mercator projection
	 */
	MERCATOR_LINEAR,
	
	/**
	 * Defines a {@link GeographicRegion} border as following a great circle.
	 */
	GREAT_CIRCLE;
	
}
