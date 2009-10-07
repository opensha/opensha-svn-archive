package org.opensha.commons.mapping.gmt.elements;

import org.opensha.commons.data.region.Region;
import org.opensha.commons.mapping.gmt.GMT_Map;

public enum TopographicSlopeFile {
	CA_THREE		(3, "calTopoInten03.grd", GMT_Map.ca_topo_region),
	CA_SIX			(6, "calTopoInten06.grd", GMT_Map.ca_topo_region),
	CA_EIGHTEEN		(18, "calTopoInten18.grd", GMT_Map.ca_topo_region),
	CA_THIRTY 		(30, "calTopoInten30.grd", GMT_Map.ca_topo_region),
	SRTM_30_PLUS	(30, "srtm30_plus_v5.0_inten.grd", Region.getGlobalRegion());
	
	private final int resolution;
	private final String fileName;
	private final Region region;
	TopographicSlopeFile(int resolution, String fileName, Region region) {
		this.resolution = resolution;
		this.fileName = fileName;
		this.region = region;
	}
	
	public int resolution() { return resolution; }
	public String fileName() { return fileName; }
	public Region region() { return region; }
}
