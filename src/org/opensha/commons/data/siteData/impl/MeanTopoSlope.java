package org.opensha.commons.data.siteData.impl;

import java.io.IOException;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.commons.data.region.Region;
import org.opensha.commons.data.siteData.AbstractSiteData;
import org.opensha.commons.data.siteData.SiteDataAPI;
import org.opensha.commons.data.siteData.util.MeanTopoSlopeCalculator;
import org.opensha.commons.param.DoubleParameter;

public class MeanTopoSlope extends AbstractSiteData<Double> {
	
	SiteDataAPI<Double> topoSlopeProvider = null;
	MeanTopoSlopeCalculator calc = null;
	
	public static final String PARAM_RADIUS_NAME = "Single Location Radius (KM)";
	public static final Double PARAM_RADIUS_MIN = 10d;
	public static final Double PARAM_RADIUS_MAX = 10000d;
	public static final Double PARAM_RADIUS_DEFAULT = 300d;
	public static final String PARAM_RADIUS_INFO = "The radius in KM around single Location requests that should " +
			"be used to determine mean slope";
	private DoubleParameter radiusParam = new DoubleParameter(PARAM_RADIUS_NAME, PARAM_RADIUS_MIN, 
			PARAM_RADIUS_MAX, PARAM_RADIUS_DEFAULT);
	
	public static final String PARAM_SPACING_NAME = "Single Location Grid Spacing (degrees)";
	public static final Double PARAM_SPACING_MIN = 0.001;
	public static final Double PARAM_SPACING_MAX = 1d;
	public static final Double PARAM_SPACING_DEFAULT = 0.1d;
	public static final String PARAM_SPACING_INFO = "The degree spacing that should be used for the circular region " +
			"around single site requests";
	private DoubleParameter spacingParam = new DoubleParameter(PARAM_SPACING_NAME, PARAM_SPACING_MIN, 
			PARAM_SPACING_MAX, PARAM_SPACING_DEFAULT);
	
	public MeanTopoSlope() throws IOException {
		this(new SRTM30PlusTopoSlope());
	}
	
	public MeanTopoSlope(SiteDataAPI<Double> topoSlopeProvider) {
		this.topoSlopeProvider = topoSlopeProvider;
		calc = new MeanTopoSlopeCalculator(topoSlopeProvider);
		
		radiusParam.setInfo(PARAM_RADIUS_INFO);
		spacingParam.setInfo(PARAM_SPACING_INFO);
		
		this.paramList.addParameter(radiusParam);
		this.paramList.addParameter(spacingParam);
	}

	public Region getApplicableRegion() {
		return topoSlopeProvider.getApplicableRegion();
	}

	public Location getClosestDataLocation(Location loc) throws IOException {
		return topoSlopeProvider.getClosestDataLocation(loc);
	}

	public String getDataMeasurementType() {
		return topoSlopeProvider.getDataMeasurementType();
	}

	public String getDataType() {
		return topoSlopeProvider.getDataType();
	}

	public String getMetadata() {
		String meta  = "Topographic slope averaged over a region from the following dataset:\n\n";
		meta += topoSlopeProvider.getMetadata();
		
		return meta;
	}

	public String getName() {
		return "Regional Mean of " + topoSlopeProvider.getName();
	}

	public double getResolution() {
		return topoSlopeProvider.getResolution();
	}

	public String getShortName() {
		return "Mean" + topoSlopeProvider.getShortName();
	}

	public Double getValue(Location loc) throws IOException {
		double radius = (Double)radiusParam.getValue();
		double gridSpacing = (Double)spacingParam.getValue();
		return calc.getMeanSlope(loc, radius, gridSpacing);
	}
	
	public Double getValue(EvenlyGriddedGeographicRegion region) throws IOException {
		return calc.getMeanSlope(region);
	}

	public boolean isValueValid(Double el) {
		return !el.isNaN();
	}

}
