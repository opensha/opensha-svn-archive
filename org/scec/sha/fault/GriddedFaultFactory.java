package org.scec.sha.fault;

import org.scec.sha.surface.GriddedSurfaceFactoryAPI;
import org.scec.sha.surface.GriddedSurfaceAPI;


/**
 * <b>Title:</b> GriddedFaultFactory<br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public abstract class GriddedFaultFactory implements GriddedSurfaceFactoryAPI {

    public abstract GriddedSurfaceAPI getGriddedSurface();

}
