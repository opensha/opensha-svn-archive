package org.opensha.commons.mapping.gmt.elements;

import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.commons.mapping.gmt.GMT_Map;

public enum TopographicSlopeFile {
	CA_THREE		(3, "calTopoInten03.grd", GMT_Map.ca_topo_region),
	CA_SIX			(6, "calTopoInten06.grd", GMT_Map.ca_topo_region),
	CA_EIGHTEEN		(18, "calTopoInten18.grd", GMT_Map.ca_topo_region),
	CA_THIRTY 		(30, "calTopoInten30.grd", GMT_Map.ca_topo_region),
	SRTM_30_PLUS	(30, "srtm30_plus_v5.0_inten.grd", GeographicRegion.getGlobalRegion());
	
	private final int resolution;
	private final String fileName;
	private final GeographicRegion region;
	TopographicSlopeFile(int resolution, String fileName, GeographicRegion region) {
		this.resolution = resolution;
		this.fileName = fileName;
		this.region = region;
	}
	
	public int resolution() { return resolution; }
	public String fileName() { return fileName; }
	public GeographicRegion region() { return region; }
}
