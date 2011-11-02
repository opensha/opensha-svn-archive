/**
 * 
 */
package org.opensha.sha.faultSurface;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;

/**
 * This class represents an evenly gridded surface composed of four Locations.
 * 
 * @author field
 */
public class FourPointEvenlyGriddedSurface extends EvenlyGriddedSurface {

	// for debugging
	private final static boolean D = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The constructs the surface from the Locations given (counter clockwise 
	 * when looking at surface from positive side).  This computes gridSpacingAlong and 
	 * gridSpacingDown as the average of the two calculable distances for each.
	 * @param upperLeft
	 * @param lowerLeft
	 * @param lowerRight
	 * @param upperRight
	 */
	public FourPointEvenlyGriddedSurface(Location upperLeft,  Location lowerLeft, 
										 Location lowerRight, Location upperRight) {
		setNumRowsAndNumCols(2, 2);
		
		set(0, 0, upperLeft);
		set(0, 1, upperRight);
		set(1, 0, lowerLeft);
		set(1, 1, lowerRight);
		
		gridSpacingAlong = (LocationUtils.linearDistanceFast(getLocation(0, 0), getLocation(0, 1)) +
							LocationUtils.linearDistanceFast(getLocation(1, 0), getLocation(1, 1)))/2;
		gridSpacingDown = (LocationUtils.linearDistanceFast(getLocation(0, 0), getLocation(1, 0))+
						   LocationUtils.linearDistanceFast(getLocation(0, 1), getLocation(1, 1)))/2;

		if(gridSpacingAlong == gridSpacingDown)
			sameGridSpacing = true;
		else
			sameGridSpacing = false;
	}

	@Override
	public double getAveDip() {
		throw new RuntimeException("Method not yet implemented");
	}

	@Override
	public double getAveDipDirection() {
		throw new RuntimeException("Method not yet implemented");
	}

	@Override
	public double getAveRupTopDepth() {
		return (get(0,0).getDepth()+get(0,1).getDepth())/2;
	}

	@Override
	public double getAveStrike() {
		return getUpperEdge().getAveStrike();
	}

	@Override
	public LocationList getPerimeter() {
		LocationList perim = new LocationList();
		perim.add(getLocation(0,0));
		perim.add(getLocation(0,1));
		perim.add(getLocation(1,1));
		perim.add(getLocation(1,0));
		perim.add(getLocation(0,0));  // to close the polygon
		return perim;
	}

	@Override
	public FaultTrace getUpperEdge() {
		FaultTrace trace = new FaultTrace(null);
		trace.add(getLocation(0,0));
		trace.add(getLocation(0,1));
		return trace;
	}

}
