package org.opensha.sha.calc.hazardMap.components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;
import org.opensha.commons.data.function.AbstractDiscretizedFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.metadata.XMLSaveable;

/**
 * The class contains basic hazard map parameters such as X values for curve calculation,
 * max source cutoff distance, wether the ERF should be serialized before it's distributed
 * to compute nodes, etc.
 * 
 * @author kevin
 *
 */
public class CalculationSettings implements XMLSaveable {
	
	public static final String XML_METADATA_NAME = "CalculationSettings";
	
//	private ArbitrarilyDiscretizedFunc xValues;
	private HashMap<String, DiscretizedFunc> imtXValMap;
	private double maxSourceDistance;
	private boolean calcInLogSpace = true;
	private boolean serializeERF = true;
	
	public CalculationSettings(DiscretizedFunc xValues, double maxSourceDistance) {
		this(new HashMap<String, DiscretizedFunc>(), maxSourceDistance);
		imtXValMap.put(null, xValues);
		this.maxSourceDistance = maxSourceDistance;
	}
	
	public CalculationSettings(HashMap<String, DiscretizedFunc> imtXValMap, double maxSourceDistance) {
		this.imtXValMap = imtXValMap;
		this.maxSourceDistance = maxSourceDistance;
	}

	public DiscretizedFunc getXValues(String imt) {
		if (imt == null || imtXValMap.size() == 1)
			return imtXValMap.get(imtXValMap.keySet().iterator().next());
		return imtXValMap.get(imt);
	}
	
	public void setXValues(DiscretizedFunc xValues) {
		imtXValMap.clear();
		if (xValues != null)
			imtXValMap.put(null, xValues);
	}

	public void setXValues(String imt, DiscretizedFunc xValues) {
		imtXValMap.put(imt, xValues);
	}
	
	public Map<String, DiscretizedFunc> getXValsMap() {
		return imtXValMap;
	}

	public double getMaxSourceDistance() {
		return maxSourceDistance;
	}

	public void setMaxSourceDistance(double maxSourceDistance) {
		this.maxSourceDistance = maxSourceDistance;
	}
	
	public void setCalcInLogSpace(boolean calcInLogSpace) {
		this.calcInLogSpace = calcInLogSpace;
	}
	
	public boolean isCalcInLogSpace() {
		return calcInLogSpace;
	}
	
	public void setSerializeERF(boolean serializeERF) {
		this.serializeERF = serializeERF;
	}
	
	public boolean isSerializeERF() {
		return serializeERF;
	}

	public Element toXMLMetadata(Element root) {
		Element calcEl = root.addElement(XML_METADATA_NAME);
		
		calcEl.addAttribute("maxSourceDistance", maxSourceDistance + "");
		calcEl.addAttribute("calcInLogSpace", calcInLogSpace + "");
		calcEl.addAttribute("serializeERF", serializeERF + "");
		for (String imt : imtXValMap.keySet()) {
			DiscretizedFunc xValues = imtXValMap.get(imt);
			if (imt == null)
				xValues.setXAxisName("");
			else
				xValues.setXAxisName(imt);
			calcEl = xValues.toXMLMetadata(calcEl);
		}
		
		return root;
	}
	
	public static CalculationSettings fromXMLMetadata(Element calcEl) {
		double maxSourceDistance = Double.parseDouble(calcEl.attributeValue("maxSourceDistance"));
		boolean calcInLogSpace = Boolean.parseBoolean(calcEl.attributeValue("calcInLogSpace"));
		boolean serializeERF = Boolean.parseBoolean(calcEl.attributeValue("serializeERF"));
		
		Iterator<Element> funcElIt = calcEl.elementIterator(ArbitrarilyDiscretizedFunc.XML_METADATA_NAME);
		HashMap<String, DiscretizedFunc> imtXValMap = new HashMap<String, DiscretizedFunc>();
		while (funcElIt.hasNext()) {
			Element funcEl = funcElIt.next();
			ArbitrarilyDiscretizedFunc xValues;
			AbstractDiscretizedFunc func = ArbitrarilyDiscretizedFunc.fromXMLMetadata(funcEl);
			if (func instanceof ArbitrarilyDiscretizedFunc)
				xValues = (ArbitrarilyDiscretizedFunc)func;
			else
				xValues = new ArbitrarilyDiscretizedFunc(func);
			imtXValMap.put(xValues.getXAxisName(), xValues);
		}
		
		CalculationSettings calcSettings;
		if (imtXValMap.size() == 1)
			calcSettings = new CalculationSettings(
					imtXValMap.get(imtXValMap.keySet().iterator().next()), maxSourceDistance);
		else
			calcSettings = new CalculationSettings(imtXValMap, maxSourceDistance);
		
		calcSettings.setCalcInLogSpace(calcInLogSpace);
		calcSettings.setSerializeERF(serializeERF);
		
		return calcSettings;
	}

}
