package org.opensha.sha.calc.IM_EventSet.v03.gui;

import java.util.ArrayList;

import javax.swing.BoxLayout;

import org.opensha.commons.data.siteData.SiteDataAPI;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.sha.util.SiteTranslator;

public class AddSiteDataPanel extends ParameterListEditor {
	
	public static ArrayList<String> siteDataTypes;
	
	static {
		siteDataTypes = new ArrayList<String>();
		siteDataTypes.add(SiteDataAPI.TYPE_VS30);
		siteDataTypes.add(SiteDataAPI.TYPE_WILLS_CLASS);
		siteDataTypes.add(SiteDataAPI.TYPE_DEPTH_TO_2_5);
		siteDataTypes.add(SiteDataAPI.TYPE_DEPTH_TO_1_0);
	}
	
	StringParameter typeSelector;
	StringParameter measSelector;
	StringParameter valEntry;
	
	public AddSiteDataPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		typeSelector = new StringParameter("Site Data Type", siteDataTypes, siteDataTypes.get(0));
		
		ArrayList<String> measTypes = new ArrayList<String>();
		measTypes.add(SiteDataAPI.TYPE_FLAG_INFERRED);
		measTypes.add(SiteDataAPI.TYPE_FLAG_MEASURED);
		
		measSelector = new StringParameter("Site Data Measurement Type", measTypes, measTypes.get(0));
		
		valEntry = new StringParameter("Value");
		
		ParameterList paramList = new ParameterList();
		
		paramList.addParameter(typeSelector);
		paramList.addParameter(measSelector);
		paramList.addParameter(valEntry);
		
		this.setTitle("New Site Data Value");
		this.setParameterList(paramList);
	}
	
	public SiteDataValue<?> getValue() {
		String type = typeSelector.getValue();
		String measType = measSelector.getValue();
		String valStr = valEntry.getValue();
		if (valStr == null || valStr.length() == 0)
			throw new RuntimeException("No value was entered!");
		return getValue(type, measType, valStr);
	}
	
	public static SiteDataValue<?> getValue(String type, String measType, String valStr) {
		valStr = valStr.trim();
		Object val;
		
		if (type.equals(SiteDataAPI.TYPE_WILLS_CLASS)) {
			if (!SiteTranslator.wills_vs30_map.containsKey(valStr))
				throw new RuntimeException("'" + valStr + "' is not a valid Wills Site Class!");
			val = valStr;
		} else {
			// it's a double that we need to parse
			try {
				val = Double.parseDouble(valStr);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("'" + valStr + "' cannot be parsed into a numerical value!");
			}
		}
		return new SiteDataValue(type, measType, val);
	}

}
