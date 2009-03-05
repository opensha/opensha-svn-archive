package org.opensha.data.siteType.impl;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.RectangularGeographicRegion;
import org.opensha.data.siteType.AbstractSiteData;
import org.opensha.data.siteType.servlet.SiteDataServletAccessor;
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
	
	private boolean useServlet;
	private GeographicRegion region = RectangularGeographicRegion.createEntireGlobeRegion();
	
	public static final double arcSecondSpacing = 30.0;
	private static final double arcSecondRadians = arcSecondSpacing * StrictMath.PI / 648000d;
	// for 30 arc seconds this is 0.008333333333333333
	public static final double spacing = StrictMath.toDegrees(arcSecondRadians);
	
	private SiteDataServletAccessor<Double> servlet = null;
	
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
	
	public static ArbitrarilyDiscretizedFunc createActiveCoefficients() {
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		
		func.set(1.0e-4,	180);
		func.set(2.2e-3,	240);
		func.set(6.3e-3,	300);
		func.set(0.018,		360);
		func.set(0.050,		490);
		func.set(0.10,		620);
		func.set(0.138,		760);
		
		return func;
	}
	
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
	
	public WaldGlobalVs2007() {
		this(null, true);
	}
	
	public WaldGlobalVs2007(String fileName) {
		this(fileName, false);
	}
	
	private WaldGlobalVs2007(String fileName, boolean useServlet) {
		this.useServlet = useServlet;
		if (useServlet) {
			// TODO: implement this!
		} else {
			// TODO: implement this!
		}
		
		ArrayList<String> coeffNames = new ArrayList<String>();
		
		coeffNames.add(COEFF_ACTIVE_NAME);
		coeffNames.add(COEFF_STABLE_NAME);
		coeffNames.add(COEFF_CUSTOM_NAME);
		
		coeffPresetParam = new StringParameter(COEFF_SELECT_PARAM_NAME, coeffNames, COEFF_ACTIVE_NAME);
		
		coeffFuncParam = new ArbitrarilyDiscretizedFuncParameter(COEFF_FUNC_PARAM_NAME, activeFunc.deepClone());
		coeffFuncParam.setNonEditable();
		
		coeffPresetParam.addParameterChangeListener(this);
		coeffFuncParam.addParameterChangeListener(this);
		
		this.paramList.addParameter(minVs30Param);
		this.paramList.addParameter(maxVs30Param);
		this.paramList.addParameter(coeffPresetParam);
		this.paramList.addParameter(coeffFuncParam);
	}

	public GeographicRegion getApplicableRegion() {
		return region;
	}

	public Location getClosestDataLocation(Location loc) throws IOException {
		if (useServlet)
			return servlet.getClosestLocation(loc);
		else
			// TODO: implement this!
			return null;
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

	public Double getValue(Location loc) throws IOException {
		// TODO Auto-generated method stub
		return null;
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
			ArbitrarilyDiscretizedFunc func = (ArbitrarilyDiscretizedFunc)coeffFuncParam.getValue();
			if (D) {
				for (int i=0; i<func.getNum(); i++) {
					System.out.println("x: " + func.getX(i) + ", y: " + func.getY(i));
				}
			}
		}
		if (D) System.out.println("WaldparameterChange DONE");
	}
	
	private void refreshParams() {
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

	@Override
	protected void initParamListEditor() {
		super.initParamListEditor();
		refreshParams();
	}
}
