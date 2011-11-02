package org.opensha.refFaultParamDb.calc.sectionDists;

import org.opensha.sha.faultSurface.GriddedSurfaceInterface;

public interface SurfaceFilter {
	
	public double getCornerMidptFilterDist();
	
	public boolean isIncluded(GriddedSurfaceInterface surface, int row, int col);

}
