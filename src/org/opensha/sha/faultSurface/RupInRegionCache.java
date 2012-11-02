package org.opensha.sha.faultSurface;

import java.io.Serializable;

import org.opensha.commons.geo.Region;
import org.opensha.sha.earthquake.EqkRupture;

public interface RupInRegionCache extends Serializable {
	
	public boolean isRupInRegion(EqkRupture rup, int srcIndex, int rupIndex, Region region);

}
