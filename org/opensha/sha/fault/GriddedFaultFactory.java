package org.opensha.sha.fault;

import org.opensha.sha.surface.GriddedSurfaceFactoryAPI;
import org.opensha.sha.surface.GriddedSurfaceAPI;

// Fix - Needs more comments

/**
 * <b>Title:</b> GriddedFaultFactory<p>
 * <b>Description:</b> <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public abstract class GriddedFaultFactory implements GriddedSurfaceFactoryAPI {

    public abstract GriddedSurfaceAPI getGriddedSurface();

}
