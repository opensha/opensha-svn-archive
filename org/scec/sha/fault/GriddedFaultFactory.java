package org.scec.sha.fault;

import org.scec.sha.surface.GriddedSurfaceFactoryAPI;
import org.scec.sha.surface.GriddedSurfaceAPI;

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
