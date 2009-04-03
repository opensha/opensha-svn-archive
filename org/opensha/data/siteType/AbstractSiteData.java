package org.opensha.data.siteType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.siteType.impl.CVM2BasinDepth;
import org.opensha.data.siteType.impl.CVM4BasinDepth;
import org.opensha.data.siteType.impl.USGSBayAreaBasinDepth;
import org.opensha.data.siteType.impl.WillsMap2000;
import org.opensha.data.siteType.impl.WillsMap2006;
import org.opensha.param.DoubleParameter;
import org.opensha.param.Parameter;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.editor.ParameterListEditor;

/**
 * Abstract implementation of SiteDataAPI. It handles some of the basic methods, such as
 * getValues, and getAnnnotatedValue(s). It also creates default parameters for convenience.
 * 
 * @author Kevin Milner
 *
 * @param <Element>
 */
public abstract class AbstractSiteData<Element> implements SiteDataAPI<Element> {
	
	protected ParameterList paramList;
	protected ParameterListEditor paramEdit = null;
	
	public static final String PARAM_MIN_BASIN_DEPTH_DOUBLE_NAME = "Minimum Basin Depth";
	public static final Double PARAM_MIN_BASIN_DEPTH_DOUBLE_MIN = 0.0d;
	public static final Double PARAM_MIN_BASIN_DEPTH_DOUBLE_MAX = 100000d;
	public static final Double PARAM_MIN_BASIN_DEPTH_DOUBLE_DEFAULT = PARAM_MIN_BASIN_DEPTH_DOUBLE_MIN;
	/**
	 * Parameter for specifying minimum basin depth
	 */
	protected DoubleParameter minBasinDoubleParam = new DoubleParameter(PARAM_MIN_BASIN_DEPTH_DOUBLE_NAME,
			PARAM_MIN_BASIN_DEPTH_DOUBLE_MIN, PARAM_MIN_BASIN_DEPTH_DOUBLE_MAX, PARAM_MIN_BASIN_DEPTH_DOUBLE_DEFAULT);
	
	public static final String PARAM_MAX_BASIN_DEPTH_DOUBLE_NAME = "Maximum Basin Depth";
	public static final Double PARAM_MAX_BASIN_DEPTH_DOUBLE_MIN = 0.0d;
	public static final Double PARAM_MAX_BASIN_DEPTH_DOUBLE_MAX = 100000d;
	public static final Double PARAM_MAX_BASIN_DEPTH_DOUBLE_DEFAULT = PARAM_MAX_BASIN_DEPTH_DOUBLE_MAX;
	/**
	 * Parameter for specifying maximum basin depth
	 */
	protected DoubleParameter maxBasinDoubleParam = new DoubleParameter(PARAM_MAX_BASIN_DEPTH_DOUBLE_NAME,
			PARAM_MAX_BASIN_DEPTH_DOUBLE_MIN, PARAM_MAX_BASIN_DEPTH_DOUBLE_MAX, PARAM_MAX_BASIN_DEPTH_DOUBLE_DEFAULT);
	
	public static final String PARAM_MIN_VS30_NAME = "Minimum Vs30";
	public static final Double PARAM_MIN_VS30_MIN = 0.0d;
	public static final Double PARAM_MIN_VS30_MAX = 100000d;
	public static final Double PARAM_MIN_VS30_DEFAULT = PARAM_MIN_VS30_MIN;
	/**
	 * Parameter for specifying minimum Vs30
	 */
	protected DoubleParameter minVs30Param = new DoubleParameter(PARAM_MIN_VS30_NAME,
			PARAM_MIN_VS30_MIN, PARAM_MIN_VS30_MAX, PARAM_MIN_VS30_DEFAULT);
	
	public static final String PARAM_MAX_VS30_NAME = "Maximum Vs30";
	public static final Double PARAM_MAX_VS30_MIN = 0.0d;
	public static final Double PARAM_MAX_VS30_MAX = 5000d;
	public static final Double PARAM_MAX_VS30_DEFAULT = PARAM_MAX_VS30_MAX;
	/**
	 * Parameter for specifying maximum Vs30
	 */
	protected DoubleParameter maxVs30Param = new DoubleParameter(PARAM_MAX_VS30_NAME,
			PARAM_MAX_VS30_MIN, PARAM_MAX_VS30_MAX, PARAM_MAX_VS30_DEFAULT);
	
	public AbstractSiteData() {
		paramList = new ParameterList();
	}
	
	public SiteDataValue<Element> getAnnotatedValue(Location loc) throws IOException {
		Element val = this.getValue(loc);
		return new SiteDataValue<Element>(this.getDataType(), this.getDataMeasurementType(), val, this.getName());
	}
	
	public SiteDataValueList<Element> getAnnotatedValues(LocationList locs) throws IOException {
		ArrayList<Element> vals = this.getValues(locs);
		return new SiteDataValueList<Element>(this.getDataType(), this.getDataMeasurementType(), vals, this.getName());
	}

	/**
	 * Returns a list of the values at each location.
	 * 
	 * This should be overridden if there is a more efficient way of accessing the data,
	 * like through a servlet where you can request all of the values at once.
	 */
	public ArrayList<Element> getValues(LocationList locs) throws IOException {
		ArrayList<Element> vals = new ArrayList<Element>();
		
		for (Location loc : locs) {
			vals.add(this.getValue(loc));
		}
		
		return vals;
	}
	
	public boolean hasDataForLocation(Location loc, boolean checkValid) {
		if (this.getApplicableRegion().isLocationInside(loc)) {
			if (checkValid) {
				try {
					Element val = this.getValue(loc);
					return this.isValueValid(val);
				} catch (IOException e) {
					return false;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	public ParameterList getAdjustableParameterList() {
		return paramList;
	}
	
	protected void initParamListEditor() {
		paramEdit = new ParameterListEditor(paramList);
		
		if (paramList.size() == 0)
			paramEdit.setTitle("No Adjustable Parameters");
		else
			paramEdit.setTitle("Adjustable Parameters");
	}
	
	public ParameterListEditor getParameterListEditor() {
		if (paramEdit == null) {
			initParamListEditor();
		} else {
			paramEdit.refreshParamEditor();
		}
		
		return paramEdit;
	}
	
	/**
	 * Helper method for checking if the basin depth is within the min/max
	 * 
	 * @param val
	 * @return
	 */
	protected Double certifyMinMaxBasinDepth(Double val) {
		Double min = (Double) minBasinDoubleParam.getValue();
		Double max = (Double) maxBasinDoubleParam.getValue();
		
		if (min != null && val < min)
			return min;
		if (max != null && val > max)
			return max;
		return val;
	}
	
	/**
	 * Helper method for checking if the Vs30 is within the min/max
	 * 
	 * @param val
	 * @return
	 */
	protected Double certifyMinMaxVs30(Double val) {
		Double min = (Double) minVs30Param.getValue();
		Double max = (Double) maxVs30Param.getValue();
		
		if (min != null && val < min)
			return min;
		if (max != null && val > max)
			return max;
		return val;
	}
	
	/**
	 * If your SiteData provider has data that needs to be saved in order to be recreated from
	 * XML, then override this method.
	 * 
	 * @param paramsEl
	 * @return
	 */
	protected org.dom4j.Element addXMLParameters(org.dom4j.Element paramsEl) {
		return paramsEl;
	}
	
	public org.dom4j.Element toXMLMetadata(org.dom4j.Element root) {
		return this.toXMLMetadata(root, -1);
	}

	public org.dom4j.Element toXMLMetadata(org.dom4j.Element root, int cacheSize) {
		org.dom4j.Element el = root.addElement(XML_METADATA_NAME);
		el.addAttribute("Name", getName());
		el.addAttribute("Ref_ClassName", this.getClass().getName());
		el.addAttribute("CacheSize", cacheSize + "");
		
		org.dom4j.Element paramsEl = el.addElement("DataParameters");
		addXMLParameters(paramsEl);
		
		Iterator<ParameterAPI> paramIt = this.paramList.iterator();
		org.dom4j.Element paramsElement = el.addElement(Parameter.XML_GROUP_METADATA_NAME);
		while (paramIt.hasNext()) {
			ParameterAPI param = paramIt.next();
			paramsElement = param.toXMLMetadata(paramsElement);
		}
		
		return root;
	}
	
	public static SiteDataAPI<?> fromXMLMetadata(org.dom4j.Element dataElem) throws IOException {
		String name = dataElem.attributeValue("Name");
		int cacheSize = Integer.parseInt(dataElem.attributeValue("CacheSize"));
		
		org.dom4j.Element paramsEl = dataElem.element("DataParameters");
		
		SiteDataAPI<?> provider;
		if (name.equals(CVM2BasinDepth.NAME)) {
			provider = CVM2BasinDepth.fromXMLParams(paramsEl);
		} else if (name.equals(CVM4BasinDepth.NAME)) {
			provider = CVM4BasinDepth.fromXMLParams(paramsEl);
		} else if (name.equals(WillsMap2000.NAME)) {
			provider = WillsMap2000.fromXMLParams(paramsEl);
		} else if (name.equals(WillsMap2006.NAME)) {
			provider = WillsMap2006.fromXMLParams(paramsEl);
		} else if (name.equals(USGSBayAreaBasinDepth.NAME)) {
			provider = USGSBayAreaBasinDepth.fromXMLParams(paramsEl);
		} else {
			throw new RuntimeException("Cannot load Site Data Provider '" + name + "' from XML!");
		}
		
		// add params
//		System.out.println("Setting params...");
		org.dom4j.Element paramsElement = dataElem.element(Parameter.XML_GROUP_METADATA_NAME);
		for (ParameterAPI param : provider.getAdjustableParameterList()) {
//			System.out.println("Setting param " + param.getName());
			Iterator<org.dom4j.Element> it = paramsElement.elementIterator();
			while (it.hasNext()) {
				org.dom4j.Element el = it.next();
				if (param.getName().equals(el.attribute("name").getValue())) {
//					System.out.println("Found a match!");
					if (param.setValueFromXMLMetadata(el)) {
//						System.out.println("Parameter set successfully!");
					} else {
						System.out.println("Parameter could not be set from XML!");
						System.out.println("It is possible that the parameter type doesn't yet support loading from XML");
					}
				}
			}
		}
		
		if (cacheSize > 0) {
			return new CachedSiteDataWrapper(provider);
		}
		
		return provider;
	}
	
}
