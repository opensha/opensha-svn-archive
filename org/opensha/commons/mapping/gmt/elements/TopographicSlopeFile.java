package org.opensha.commons.mapping.gmt.elements;

import org.opensha.commons.data.region.RectangularGeographicRegion;
import org.opensha.commons.exceptions.RegionConstraintException;
import org.opensha.commons.mapping.gmt.GMT_Map;

public enum TopographicSlopeFile {
	CA_THREE		(3, "calTopoInten03.grd", GMT_Map.ca_topo_region),
	CA_SIX			(6, "calTopoInten06.grd", GMT_Map.ca_topo_region),
	CA_EIGHTEEN		(18, "calTopoInten18.grd", GMT_Map.ca_topo_region),
	CA_THIRTY 		(30, "calTopoInten30.grd", GMT_Map.ca_topo_region);
	
	private final int resolution;
	private final String fileName;
	private final RectangularGeographicRegion region;
	TopographicSlopeFile(int resolution, String fileName, RectangularGeographicRegion region) {
		this.resolution = resolution;
		this.fileName = fileName;
		this.region = region;
	}
	
	public int resolution() { return resolution; }
	public String fileName() { return fileName; }
	public RectangularGeographicRegion region() { return region; }
}
