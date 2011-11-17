package org.opensha.sha.earthquake.rupForecastImpl.nshmp;

import static org.opensha.sha.nshmp.SourceRegion.*;
import static org.opensha.sha.nshmp.SourceType.*;

import java.util.EnumSet;

import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.source.Sources;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultType;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech;
import org.opensha.sha.nshmp.MultiSourceERF;
import org.opensha.sha.nshmp.SourceRegion;
import org.opensha.sha.nshmp.SourceType;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class WUS_ERF extends MultiSourceERF {

	private WUS_ERF() {
		super("NSHMP West");
	}

	// want to get param group/list for each source type and add

	private void initParams() {

	}

	private static final String NSHMP_FAULT_TYPE_FILTER_PARAM_NAME = "Fault Type Filter";
	private static final String NSHMP_FOCAL_MECH_FILTER_PARAM_NAME = "Focal Mech Filter";

	private void initFaultParams() {

		EnumParameter<FaultType> faultTypeFilter = new EnumParameter<FaultType>(
			"Fault Type Filter", EnumSet.allOf(FaultType.class), null, "All");
		faultTypeFilter.addParameterChangeListener(this);

		EnumParameter<FocalMech> focalMechFilter = new EnumParameter<FocalMech>(
			"Focal Mech Filter", EnumSet.allOf(FocalMech.class), null, "All");
		focalMechFilter.addParameterChangeListener(this);

		
	}

	private void init() {
//		addERFs(GRIDDED, Forecasts.getGridList(WUS));
//		addERFs(FAULT, Sources.getFaultList(WUS));
		addERFs(FAULT, Sources.getFaultList(CA));
//		addERFs(SUBDUCTION, Forecasts.getSubductionList(CASC));
	}
	
	public static void main(String[] args) {
		WUS_ERF wus = new WUS_ERF();
		wus.init();
		wus.updateForecast();
		
		System.out.println(wus.getNumSources());
	}

}
