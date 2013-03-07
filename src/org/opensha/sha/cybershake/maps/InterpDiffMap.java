package org.opensha.sha.cybershake.maps;

import java.awt.Color;

import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.util.cpt.CPT;

public class InterpDiffMap extends GMT_Map {
	
	public enum InterpDiffMapType {
		BASEMAP("basemap", null),
		INTERP_NOMARKS("interpolated", null),
		INTERP_MARKS("interpolated_marks", Color.WHITE),
		DIFF("diff", Color.BLACK),
		RATIO("ratio", Color.BLACK);
		
		private String prefix;
		private Color markerColor;
		
		private InterpDiffMapType(String prefix, Color markerColor) {
			this.prefix = prefix;
			this.markerColor = markerColor;
		}
		
		public String getPrefix() {
			return prefix;
		}
		
		public Color getMarkerColor() {
			return markerColor;
		}
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GeoDataSet scatter;
	private GMT_InterpolationSettings interpSettings;
	private InterpDiffMapType[] mapTypes;
	
	public InterpDiffMap(Region region, GeoDataSet baseMap, double basemapInc, CPT cpt,
			GeoDataSet scatter, GMT_InterpolationSettings interpSettings,
			InterpDiffMapType[] mapTypes) {
		super(region, baseMap, basemapInc, cpt);
		this.scatter = scatter;
		this.interpSettings = interpSettings;
		this.mapTypes = mapTypes;
	}

	public GeoDataSet getScatter() {
		return scatter;
	}

	public void setScatter(GeoDataSet scatter) {
		this.scatter = scatter;
	}

	public GMT_InterpolationSettings getInterpSettings() {
		return interpSettings;
	}

	public void setInterpSettings(GMT_InterpolationSettings interpSettings) {
		this.interpSettings = interpSettings;
	}

	public InterpDiffMapType[] getMapTypes() {
		return mapTypes;
	}

	public void setMapTypes(InterpDiffMapType[] mapTypes) {
		this.mapTypes = mapTypes;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
