package org.scec.sha.fault;

import org.scec.sha.surface.GriddedSurfaceFactoryAPI;
import org.scec.sha.surface.GriddedSurfaceAPI;
import org.scec.sha.fault.FaultTrace;

/**
 * <b>Title:</b> FrankelGriddedFaultFactory<br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public class FrankelGriddedFaultFactory extends SimpleGriddedFaultFactory {

    public FrankelGriddedFaultFactory() { super(); }
    public FrankelGriddedFaultFactory(
        FaultTrace faultTrace,
        Double aveDip,
        Double upperSeismogenicDepth,
        Double lowerSeismogenicDepth,
        Double gridSpacing
    ){ super(faultTrace, aveDip, upperSeismogenicDepth, lowerSeismogenicDepth, gridSpacing); }


    public GriddedSurfaceAPI getGriddedSurface(){ return null; }
}
