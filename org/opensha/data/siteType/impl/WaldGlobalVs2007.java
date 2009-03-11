package org.opensha.data.siteType.impl;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.calc.ArcsecondConverter;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.RectangularGeographicRegion;
import org.opensha.data.siteType.AbstractSiteData;
import org.opensha.data.siteType.SiteDataToXYZ;
import org.opensha.data.siteType.servlet.SiteDataServletAccessor;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.param.ArbitrarilyDiscretizedFuncParameter;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ArbitrarilyDiscretizedFuncTableModel;
import org.opensha.param.editor.ParameterEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;

public class WaldGlobalVs2007 extends AbstractSiteData<Double> implements ParameterChangeListener {
	
	private static final boolean D = false;
	
	public static final String NAME = "Global Vs30 from Topographic Slope (Wald 2007)";
	public static final String SHORT_NAME = "Wald2007";
	
	private GeographicRegion region = RectangularGeographicRegion.createEntireGlobeRegion();
	
	public static final double arcSecondSpacing = 30.0;
	// for 30 arc seconds this is 0.008333333333333333
	public static final double spacing = ArcsecondConverter.getDegrees(arcSecondSpacing);
	
	private StringParameter coeffPresetParam;
	public static final String COEFF_SELECT_PARAM_NAME = "Region Type";
	public static final String COEFF_ACTIVE_NAME = "Active Tectonic";
	public static final String COEFF_STABLE_NAME = "Stable Continent";
	public static final String COEFF_CUSTOM_NAME = "Custom Coefficients";
	
	private ArbitrarilyDiscretizedFuncParameter coeffFuncParam;
	public static final String COEFF_FUNC_PARAM_NAME = "Topographic Slope Translation Coefficients";
	
	private final ArbitrarilyDiscretizedFunc activeFunc = createActiveCoefficients();
	private final ArbitrarilyDiscretizedFunc stableFunc = createStableCoefficients();
	private ArbitrarilyDiscretizedFunc customFunc = null;
	
	private SRTM30TopoSlope srtm30_Slope;
	
	private ArbitrarilyDiscretizedFunc coeffFunc;
	
	/**
	 * Creates function for active tectonic regions from Allen & Wald 2008
	 * @return
	 */
	public static ArbitrarilyDiscretizedFunc createActiveCoefficients() {
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		
		func.set(3e-4,		180);
		func.set(3.5e-3,	240);
		func.set(0.010,		300);
		func.set(0.018,		360);
		func.set(0.050,		490);
		func.set(0.10,		620);
		func.set(0.14,		760);
		
		return func;
	}
	
	/**
	 * Creates function for stable tectonic regions from Wald & Allen 2007
	 * @return
	 */
	public static ArbitrarilyDiscretizedFunc createStableCoefficients() {
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		
		func.set(2.0e-5,	180);
		func.set(2.0e-3,	240);
		func.set(4.0e-3,	300);
		func.set(7.2e-3,	360);
		func.set(0.013,		490);
		func.set(0.018,		620);
		func.set(0.025,		760);
		
		return func;
	}
	
	public WaldGlobalVs2007() throws IOException {
		srtm30_Slope = new SRTM30TopoSlope();
		
		ArrayList<String> coeffNames = new ArrayList<String>();
		
		coeffNames.add(COEFF_ACTIVE_NAME);
		coeffNames.add(COEFF_STABLE_NAME);
		coeffNames.add(COEFF_CUSTOM_NAME);
		
		coeffPresetParam = new StringParameter(COEFF_SELECT_PARAM_NAME, coeffNames, COEFF_ACTIVE_NAME);
		
		coeffFuncParam = new ArbitrarilyDiscretizedFuncParameter(COEFF_FUNC_PARAM_NAME, activeFunc.deepClone());
		coeffFuncParam.setNonEditable();
		
		coeffPresetParam.addParameterChangeListener(this);
		coeffFuncParam.addParameterChangeListener(this);
		
		coeffFunc = (ArbitrarilyDiscretizedFunc) coeffFuncParam.getValue();
		
		this.paramList.addParameter(minVs30Param);
		this.paramList.addParameter(maxVs30Param);
		this.paramList.addParameter(coeffPresetParam);
		this.paramList.addParameter(coeffFuncParam);
	}

	public GeographicRegion getApplicableRegion() {
		return region;
	}

	public Location getClosestDataLocation(Location loc) throws IOException {
		return srtm30_Slope.getClosestDataLocation(loc);
	}

	public String getMetadata() {
		// TODO implement this!
		return 	"Vs30 estimations from topographic slope, as described in:\n\n" +
				"Topographic Slope as a Proxy for Seismic Site-Conditions (Vs30) and Amplification Around the Globe\n" +
				"By Trevor I. Allen and David J. Wald\n" +
				"U.S. Geological Survey Open-File Report 2007-1357\n" +
				"\n" +
				"Digital Elevation model in use is SRTM30, 30 arc second";
	}

	public String getName() {
		return NAME;
	}

	public double getResolution() {
		return spacing;
	}

	public String getShortName() {
		return SHORT_NAME;
	}

	public String getType() {
		return TYPE_VS30;
	}

	public String getTypeFlag() {
		return TYPE_FLAG_INFERRED;
	}
	
	private double getVs30(double slope) {
//		System.out.println("old: " + slope);
//		slope = slope / 100d;
//		System.out.println("new: " + slope);
		double vs;
		if (slope <= coeffFunc.getMinX())
			vs = coeffFunc.getY(0);
		else if (slope >= coeffFunc.getMaxX())
			vs = coeffFunc.getY(coeffFunc.getNum()-1);
		else
			vs = coeffFunc.getInterpolatedY(slope);
		
		if (D) System.out.println("Translated slope of " + slope + " to Vs30 of " + vs);
		return vs;
	}

	public Double getValue(Location loc) throws IOException {
		Double slope = srtm30_Slope.getValue(loc);
		
		if (!srtm30_Slope.isValueValid(slope))
			return Double.NaN;
		
		double vs30 = getVs30(slope);
		
		return vs30;
	}
	
	@Override
	public ArrayList<Double> getValues(LocationList locs) throws IOException {
		ArrayList<Double> slopes = srtm30_Slope.getValues(locs);
		ArrayList<Double> vs30 = new ArrayList<Double>();
		
		for (int i=0; i<slopes.size(); i++) {
			vs30.add(getVs30(slopes.get(i)));
		}
		
		return vs30;
	}

	public boolean isValueValid(Double el) {
		return el > 0 && !el.isNaN();
	}

	public void parameterChange(ParameterChangeEvent event) {
		String paramName = event.getParameterName();
		if (D) System.out.println("WaldparameterChange start...");
		if (paramName == COEFF_SELECT_PARAM_NAME) {
			if (D) System.out.println("Coeff select changed...");
			String val = (String)coeffPresetParam.getValue();
			
			// if we're switching away from a custom function, we want to store that
			ArbitrarilyDiscretizedFunc oldFunc = (ArbitrarilyDiscretizedFunc)coeffFuncParam.getValue();
			if (!ArbitrarilyDiscretizedFuncTableModel.areFunctionPointsEqual(oldFunc, activeFunc)
					&& !ArbitrarilyDiscretizedFuncTableModel.areFunctionPointsEqual(oldFunc, stableFunc)) {
				// the function has been edited...store it
				customFunc = oldFunc;
			}
				
			if (val == COEFF_ACTIVE_NAME) {
				coeffFuncParam.setValue(activeFunc.deepClone());
			} else if (val == COEFF_STABLE_NAME) {
				coeffFuncParam.setValue(stableFunc.deepClone());
			} else if (val == COEFF_CUSTOM_NAME) {
				if (customFunc == null) {
					customFunc = activeFunc.deepClone();
				}
				coeffFuncParam.setValue(customFunc);
			}
			refreshParams();
		} else if (paramName == COEFF_FUNC_PARAM_NAME) {
			if (D) System.out.println("Coeff func changed...");
			coeffFunc = (ArbitrarilyDiscretizedFunc)coeffFuncParam.getValue();
			if (D) {
				for (int i=0; i<coeffFunc.getNum(); i++) {
					System.out.println("x: " + coeffFunc.getX(i) + ", y: " + coeffFunc.getY(i));
				}
			}
		}
		if (D) System.out.println("WaldparameterChange DONE");
	}
	
	private void refreshParams() {
		if (this.paramEdit == null)
			return;
		if (D) System.out.println("WaldRefreshParams start...");
		String val = (String)coeffPresetParam.getValue();
		ParameterEditor funcEditor = this.paramEdit.getParameterEditor(COEFF_FUNC_PARAM_NAME);
		funcEditor.setEnabled(val == COEFF_CUSTOM_NAME);
		if (D) System.out.println("WaldRefreshParams refreshing params...");
//		funcEditor.refreshParamEditor();
		paramEdit.refreshParamEditor();
		funcEditor.validate();
		paramEdit.validate();
//		paramEdit.refreshParamEditor();
		if (D) System.out.println("WaldRefreshParams DONE");
	}
	
	public void setCoeffFunction(ArbitrarilyDiscretizedFunc func) {
		String selected = (String)coeffPresetParam.getValue();
		if (!selected.equals(COEFF_CUSTOM_NAME)) {
			coeffPresetParam.setValue(COEFF_CUSTOM_NAME);
		}
		this.coeffFuncParam.setValue(func);
	}
	
	public ArbitrarilyDiscretizedFunc getCoeffFunctionClone() {
		return coeffFunc.deepClone();
	}
	
	public static void printMapping(ArbitrarilyDiscretizedFunc func) {
		for (int i=0; i<func.getNum(); i++) {
			System.out.println(func.getX(i) + "\t=>\t" + func.getY(i));
		}
	}

	@Override
	protected void initParamListEditor() {
		super.initParamListEditor();
		refreshParams();
	}
	
	public static void main(String args[]) throws IOException, RegionConstraintException {
		WaldGlobalVs2007 data = new WaldGlobalVs2007();
		
		System.out.println(data.getValue(new Location(34, -118)));
		System.out.println(data.getValue(new Location(34, -10)));
		
		EvenlyGriddedRectangularGeographicRegion region = new EvenlyGriddedRectangularGeographicRegion(32, 35, -121, -117, 0.01);
//		EvenlyGriddedRectangularGeographicRegion region = new EvenlyGriddedRectangularGeographicRegion(-60, 60, -180, 180, 1);
		
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		func.set(0.000032,	180);
		func.set(0.0022,	240);
		func.set(0.0063,	300);
		func.set(0.018,		360);
		func.set(0.05,		490);
		func.set(0.01,		620);
		func.set(0.138,		760);
		data.setCoeffFunction(func);
//		
		SiteDataToXYZ.writeXYZ(data, region, "/tmp/topo_vs30.txt");
		
		System.out.println(data.getCoeffFunctionClone());
		
		System.out.println("Active Tectonic:");
		printMapping(createActiveCoefficients());
		System.out.println("Stable Continent:");
		printMapping(createStableCoefficients());
	}
}
