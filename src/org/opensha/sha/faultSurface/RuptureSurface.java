
package org.opensha.sha.faultSurface;

import org.opensha.commons.data.Site;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;

/**
 * This interface defines a rupture surfaces. This does not specify how a rupture 
 * surface is to be represented (in order to maintan flexibility), but rather 
 * specifies what information a rupture surface needs to provide (see method 
 * descriptions for details).
 * @author field
 *
 */
public interface RuptureSurface {
	
	/**
	 * This returns rupture distance (kms to closest point on the 
	 * rupture surface), assuming the site has zero depth (for numerical 
	 * expediency).
	 * @return 
	 */
	public double getDistanceRup(Site site);
	
	/**
	 * This returns rupture distance (kms to closest point on the 
	 * rupture surface), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return 
	 */
	public double getDistanceRup(Location loc);

	/**
	 * This returns distance JB (shortest horz distance in km to surface projection 
	 * of rupture), assuming the site has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	public double getDistanceJB(Site site);

	/**
	 * This returns distance JB (shortest horz distance in km to surface projection 
	 * of rupture), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	public double getDistanceJB(Location loc);

	/**
	 * This returns "distance seis" (shortest distance in km to point on rupture 
	 * deeper than 3 km), assuming the site has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	public double getDistanceSeis(Site site);

	/**
	 * This returns "distance seis" (shortest distance in km to point on rupture 
	 * deeper than 3 km), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	public double getDistanceSeis(Location loc);


	/**
	 * This returns distance X (the shortest distance in km to the rupture 
	 * trace extended to infinity), where values >= 0 are on the hanging wall
	 * and values < 0 are on the foot wall.  The site is assumed to be at zero
	 * depth (for numerical expediency).
	 * @return
	 */
	public double getDistanceX(Site site);

	/**
	 * This returns distance X (the shortest distance in km to the rupture 
	 * trace extended to infinity), where values >= 0 are on the hanging wall
	 * and values < 0 are on the foot wall.  The location is assumed to be at zero
	 * depth (for numerical expediency).
	 * @return
	 */
	public double getDistanceX(Location loc);

	/**
	 * Average depth (km) to top of rupture (always a positive number)
	 * @return
	 */
	public double getAveRupTopDepth();
	
	/**
	 * Average dip (degrees) of rupture surface
	 * @return
	 */
	public double getAveDip();
	
	/**
	 * Average down-dip width (km) of rupture surface
	 * @return
	 */
	public double getAveRupWidth();
	
	/**
	 * Average dip direction (degrees) of rupture surface
	 * @return
	 */
	public double getAveDipDirection();
	
	/**
	 * This returns the top trace of the rupture (where the 
	 * locations are not necessarily equally spaced)
	 * @return
	 */
	public FaultTrace getRuptureTrace();
	
	/**
	 * This returns the first location on the fault trace
	 * @return
	 */
	public Location getFirstLocOnTrace();
	
	/**
	 * This returns the last location on the fault trace
	 * @return
	 */
	public Location getLastLocOnTrace();
	
	/**
	 * The is returns the fraction of this rupture surface 
	 * that's inside the given region.
	 * @param region
	 * @return
	 */
	public double getFractionOfSurfaceInRegion(Region region);
	
	
	/**
	 * This returns a list of the inidices from the given griddedRegion that
	 * are within the surface projection of this rupture surface.
	 * @param gridRegion
	 * @return
	 */
	public int[] getGriddedSurfaceIndicesInsideSurface(GriddedRegion griddedRegion);
	
	/**
	 * This returns the grid spacing used to define the discretization 
	 * used in what's returned by the methods here that contain "Discretized"
	 * in their names.
	 * @return
	 */
	public double getGridSpacing();
	
	/**
	 * This returns a list of locations that are evenly spread (at least 
	 * approximately) over the rupture surface, with a spacing given by
	 * what's returned by the getGridSpacing() method.  Further details 
	 * are specified by the implementing class.  These locations should
	 * be ordered as one reads the words on a page in a book.
	 * @return
	 */
	public LocationList getDiscritizedListOfLocationsOnSurface();
	
	/**
	 * This returns a list of locations that are evenly spread along the
	 * rupture trace.  Further details are specified by the implementing 
	 * class.  These locations should be ordered along the fault following
	 * the Aki and Richards convention.
	 * @return
	 */
	public LocationList getDiscritizedListOfLocationsOnTrace();
	
	

}
