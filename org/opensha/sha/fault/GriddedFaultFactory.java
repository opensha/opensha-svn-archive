package org.opensha.sha.fault;

import org.opensha.sha.surface.EvenlyGriddedSurfaceFactoryAPI;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;

// Fix - Needs more comments

/**
 * <b>Title:</b> GriddedFaultFactory<p>
 * <b>Description:</b> <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public abstract class GriddedFaultFactory implements EvenlyGriddedSurfaceFactoryAPI {

    public abstract EvenlyGriddedSurfaceAPI getGriddedSurface();

}
