package org.opensha.refFaultParamDb.calc.sectionDists;

import org.opensha.sha.faultSurface.GriddedSurface;

public interface SurfaceFilter {
	
	public double getCornerMidptFilterDist();
	
	public boolean isIncluded(GriddedSurface surface, int row, int col);

}
