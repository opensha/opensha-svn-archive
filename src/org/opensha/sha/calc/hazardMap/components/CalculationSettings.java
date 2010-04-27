package org.opensha.sha.calc.hazardMap.components;

import org.dom4j.Element;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
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
	
	private ArbitrarilyDiscretizedFunc xValues;
	private double maxSourceDistance;
	private boolean calcInLogSpace = true;
	private boolean serializeERF = true;
	
	public CalculationSettings(ArbitrarilyDiscretizedFunc xValues, double maxSourceDistance) {
		this.xValues = xValues;
		this.maxSourceDistance = maxSourceDistance;
	}

	public ArbitrarilyDiscretizedFunc getXValues() {
		return xValues;
	}

	public void setXValues(ArbitrarilyDiscretizedFunc xValues) {
		this.xValues = xValues;
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
		calcEl = xValues.toXMLMetadata(calcEl);
		
		return root;
	}
	
	public static CalculationSettings fromXMLMetadata(Element calcEl) {
		double maxSourceDistance = Double.parseDouble(calcEl.attributeValue("maxSourceDistance"));
		boolean calcInLogSpace = Boolean.parseBoolean(calcEl.attributeValue("calcInLogSpace"));
		boolean serializeERF = Boolean.parseBoolean(calcEl.attributeValue("serializeERF"));
		Element funcElem = calcEl.element(ArbitrarilyDiscretizedFunc.XML_METADATA_NAME);
		ArbitrarilyDiscretizedFunc xValues = ArbitrarilyDiscretizedFunc.fromXMLMetadata(funcElem);
		
		CalculationSettings calcSettings = new CalculationSettings(xValues, maxSourceDistance);
		calcSettings.setCalcInLogSpace(calcInLogSpace);
		calcSettings.setSerializeERF(serializeERF);
		
		return calcSettings;
	}

}
