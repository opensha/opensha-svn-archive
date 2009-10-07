package org.opensha.sha.faultSurface;


/**
 * <b>Title:</b> EvenlyGriddedSurfaceAPI<p>
 * <b>Description:</b>
 *
 * This extends GriddedSurfaceAPI assuming the locations are in some way evenly
 * spaced. <p>
 *
 * @author
 * @created
 * @version    1.0
 */
public interface EvenlyGriddedSurfaceAPI extends GriddedSurfaceAPI {


    /**
     * returns the grid spacing
     *
     * @return
     */
    public  double getGridSpacing() ;


}
