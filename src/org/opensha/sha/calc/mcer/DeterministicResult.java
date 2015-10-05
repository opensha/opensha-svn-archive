package org.opensha.sha.calc.mcer;

import java.io.Serializable;

import org.dom4j.Element;
import org.opensha.commons.metadata.XMLSaveable;

public class DeterministicResult implements XMLSaveable, Serializable {
	
	public static final String XML_METADATA_NAME = "DeterministicResult";
	
	private int sourceID;
	private int rupID;
	private double mag;
	private String sourceName;
	private double val;
	
	public DeterministicResult(int sourceID, int rupID, double mag,
			String sourceName, double val) {
		super();
		this.sourceID = sourceID;
		this.rupID = rupID;
		this.mag = mag;
		this.sourceName = sourceName;
		this.val = val;
	}

	public int getSourceID() {
		return sourceID;
	}

	public int getRupID() {
		return rupID;
	}

	public double getMag() {
		return mag;
	}

	public String getSourceName() {
		return sourceName;
	}

	public double getVal() {
		return val;
	}
	
	public void setVal(double val) {
		this.val = val;
	}
	
	@Override
	public String toString() {
		return "DetResult: "+val+", src("+getSourceID()+","+getRupID()+"): "+getSourceName()+" (M="+(float)getMag()+")";
	}

	@Override
	public Element toXMLMetadata(Element root) {
		Element el = root.addElement(XML_METADATA_NAME);
		
		el.addAttribute("sourceID", sourceID+"");
		el.addAttribute("rupID", rupID+"");
		el.addAttribute("mag", mag+"");
		el.addAttribute("sourceName", sourceName);
		el.addAttribute("val", val+"");
		
		return root;
	}
	
	public static DeterministicResult fromXMLMetadata(Element determEl) {
		int sourceID = Integer.parseInt(determEl.attributeValue("sourceID"));
		int rupID = Integer.parseInt(determEl.attributeValue("rupID"));
		double mag = Double.parseDouble(determEl.attributeValue("mag"));
		String sourceName = determEl.attributeValue("sourceName");
		double val = Double.parseDouble(determEl.attributeValue("val"));
		
		return new DeterministicResult(sourceID, rupID, mag, sourceName, val);
	}

	@Override
	protected Object clone() {
		return new DeterministicResult(sourceID, rupID, mag, sourceName, val);
	}

}
