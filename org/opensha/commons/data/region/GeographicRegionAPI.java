package org.opensha.commons.data.region;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.metadata.XMLSaveable;

import java.util.ListIterator;

/**
 * <p>Title: GeographicRegionAPI</p>
 *
 * <p>Description: This interface defines the methods that user can utilize while
 * working with any kind of GeographicRegion</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public interface GeographicRegionAPI extends XMLSaveable {

    /**
     * This method checks whether the given location is inside the region by
     * converting the region outline into a cartesion-coordinate-system polygon, with
     * straight-line segment, and using the definition of insidedness given in the
     * java.awt Shape interface:<p>
     * A point is considered inside if an only if:
     * <UL>
     * <LI>it lies completely inside the boundary or
     * <LI>it lies exactly on the boundary and the space immediately adjacent
     * to the point in the increasing X direction is entirely inside the boundary or
     * <LI>it lies exactly on a horizontal boundary segment and the space immediately
     * adjacent to the point in the increasing Y direction is inside the boundary.
     * </UL><p>

     *
     * @param location
     * @return
     */
    public boolean isLocationInside(Location location);



    /**
     *
     * @returns maxLat
     */
    public double getMaxLat();

    /**
     *
     * @return minLat
     */
    public double getMinLat();

    /**
     *
     * @return minLon
     */
    public double getMaxLon();

    /**
     *
     * @return maxLon
     */
    public double getMinLon();

    /**
     *
     * @return the LocationList size
     */
    public int getNumRegionOutlineLocations();

    /**
     *
     * @returns the ListIterator to the LocationList
     */
    public ListIterator getRegionOutlineIterator();


    /**
     *
     * @return the List of Locations (a polygon representing the outline of the region)
     */
    public LocationList getRegionOutline();





    /**
     * This computes the minimum horizonatal distance (km) from the location the
     * region outline.  Zero is returned if the given location is inside the polygon.
     * This distance is approximate in that it uses the RelativeLocation.getApproxHorzDistToLine(*)
     * method to compute the distance to each line segment in the region outline.
     * @return
     */
    public double getMinHorzDistToRegion(Location loc) ;
    
    /**
     * Returns true if the region is rectangular;
     * @return
     */
    public boolean isRectangular();
}
