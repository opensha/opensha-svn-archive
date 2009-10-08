package org.opensha.commons.gui.beans;

import org.opensha.commons.data.region.GriddedRegion;
import org.opensha.commons.data.region.Region;
import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.ParameterListEditor;

public class GriddedRegionGUIBean extends ParameterListEditor {
	
	// parameter names for min/max lat/lon and gridspacing
	private final static String MIN_LAT_PARAM_NAME = "Min Lat";
	private final static String MAX_LAT_PARAM_NAME = "Max Lat";
	private final static String MIN_LON_PARAM_NAME = "Min Lon";
	private final static String MAX_LON_PARAM_NAME = "Max Lon";
	private final static String GRIDSPACING_PARAM_NAME = "GridSpacing";
	
	private DoubleParameter minLatParam;
	private DoubleParameter maxLatParam;
	private DoubleParameter minLonParam;
	private DoubleParameter maxLonParam;
	private DoubleParameter gridSpacingParam;
	
	public GriddedRegionGUIBean() {
		this(32, 36, -121, -115, 0.1);
	}
	
	public GriddedRegionGUIBean(double minLat, double maxLat, double minLon, double maxLon, double gridSpacing) {
		DoubleConstraint latConst = new DoubleConstraint(-90d, 90d);
		DoubleConstraint lonConst = new DoubleConstraint(-180d, 180d);
		// make the min and max lat param
		minLatParam = new DoubleParameter(MIN_LAT_PARAM_NAME, latConst, minLat);
		maxLatParam = new DoubleParameter(MAX_LAT_PARAM_NAME, latConst, maxLat);
		// make the min and max lon param
		minLonParam = new DoubleParameter(MIN_LON_PARAM_NAME, lonConst, minLon);
		maxLonParam = new DoubleParameter(MAX_LON_PARAM_NAME, lonConst, maxLon);
		DoubleConstraint spacingConst = new DoubleConstraint(0.001, 5);
		gridSpacingParam = new DoubleParameter(GRIDSPACING_PARAM_NAME, spacingConst, gridSpacing);


		// add the params to the list
		this.parameterList = new ParameterList();
		parameterList.addParameter(minLatParam);
		parameterList.addParameter(maxLatParam);
		parameterList.addParameter(minLonParam);
		parameterList.addParameter(maxLonParam);
		parameterList.addParameter(gridSpacingParam);
		setTitle("Select Region");
		addParameters();
	}
	
	public double getMinLat() {
		return minLatParam.getValue();
	}
	
	public double getMaxLat() {
		return maxLatParam.getValue();
	}
	
	public double getMinLon() {
		return minLonParam.getValue();
	}
	
	public double getMaxLon() {
		return maxLonParam.getValue();
	}
	
	public double getGridSpacing() {
		return gridSpacingParam.getValue();
	}
	
	public void setFromGriddedRegion(GriddedRegion region) {
		setFromGeographicRegion(region);
		gridSpacingParam.setValue(region.getSpacing());
	}
	
	public void setFromGeographicRegion(Region region) {
		minLatParam.setValue(region.getMinLat());
		maxLatParam.setValue(region.getMaxLat());
		minLonParam.setValue(region.getMinLon());
		maxLonParam.setValue(region.getMaxLon());
	}

}
