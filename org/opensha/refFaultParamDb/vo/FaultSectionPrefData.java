/**
 * 
 */
package org.opensha.refFaultParamDb.vo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.opensha.data.Location;
import org.opensha.metadata.XMLSaveable;
import org.opensha.sha.fault.EqualLengthSubSectionsTrace;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.fault.SimpleFaultData;

/**
 * This class saves the preferred values (rather than the estimate) from the FaultSectionData
 * @author vipingupta
 *
 */
public class FaultSectionPrefData  implements java.io.Serializable, XMLSaveable {
	
	public static final String XML_METADATA_NAME = "FaultSectionPrefData";
	
	private int sectionId=-1;
	private String sectionName;
	private String shortName;
	private double aveLongTermSlipRate;
	private double slipRateStdDev;
	private double aveDip;
	private double aveRake;
	private double aveUpperDepth;
	private double aveLowerDepth;
	private double aseismicSlipFactor;
	private FaultTrace faultTrace;
	private float dipDirection;
	
	public String getShortName() {
		return this.shortName;
	}
	 
	public void setFaultSectionPrefData(FaultSectionPrefData faultSectionPrefData) {
		sectionId = faultSectionPrefData.getSectionId();
		sectionName= faultSectionPrefData.getSectionName();
		shortName= faultSectionPrefData.getShortName();
		aveLongTermSlipRate= faultSectionPrefData.getAveLongTermSlipRate();
		slipRateStdDev=faultSectionPrefData.getSlipRateStdDev();
		aveDip= faultSectionPrefData.getAveDip();
		aveRake= faultSectionPrefData.getAveRake();
		aveUpperDepth= faultSectionPrefData.getAveUpperDepth();
		aveLowerDepth= faultSectionPrefData.getAveLowerDepth();
		aseismicSlipFactor= faultSectionPrefData.getAseismicSlipFactor();
		faultTrace= faultSectionPrefData.getFaultTrace();
		dipDirection= faultSectionPrefData.getDipDirection();
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public double getAseismicSlipFactor() {
		return aseismicSlipFactor;
	}
	public void setAseismicSlipFactor(double aseismicSlipFactor) {
		this.aseismicSlipFactor = aseismicSlipFactor;
	}
	public double getAveDip() {
		return aveDip;
	}
	public void setAveDip(double aveDip) {
		this.aveDip = aveDip;
	}
	public double getAveLongTermSlipRate() {
		return aveLongTermSlipRate;
	}
	public void setAveLongTermSlipRate(double aveLongTermSlipRate) {
		this.aveLongTermSlipRate = aveLongTermSlipRate;
	}
	public double getAveLowerDepth() {
		return aveLowerDepth;
	}
	public void setAveLowerDepth(double aveLowerDepth) {
		this.aveLowerDepth = aveLowerDepth;
	}
	public double getAveRake() {
		return aveRake;
	}
	public void setAveRake(double aveRake) {
		this.aveRake = aveRake;
	}
	public double getAveUpperDepth() {
		return aveUpperDepth;
	}
	public void setAveUpperDepth(double aveUpperDepth) {
		this.aveUpperDepth = aveUpperDepth;
	}
	public float getDipDirection() {
		return dipDirection;
	}
	
	public void setDipDirection(float dipDirection) {
		this.dipDirection = dipDirection;
	}
	public FaultTrace getFaultTrace() {
		return faultTrace;
	}
	public void setFaultTrace(FaultTrace faultTrace) {
		this.faultTrace = faultTrace;
	}
	public int getSectionId() {
		return sectionId;
	}
	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	public double getLength() {
		return this.faultTrace.getTraceLength();
	}
	public double getDownDipWidth() {
		return (getAveLowerDepth()-getAveUpperDepth())/Math.sin(getAveDip()*Math.PI/ 180);
	}
	
	/**
	 * Get a list of all sub sections
	 * 
	 * @param maxSubSectionLen
	 * @return
	 */
	public ArrayList getSubSectionsList(double maxSubSectionLen) {
		EqualLengthSubSectionsTrace equalLengthSubsTrace = new EqualLengthSubSectionsTrace(this.faultTrace, maxSubSectionLen);
		int numSubSections = equalLengthSubsTrace.getNumSubSections();
		ArrayList<FaultSectionPrefData> subSectionList = new ArrayList<FaultSectionPrefData>();
		for(int i=0; i<numSubSections; ++i) {
			FaultSectionPrefData subSection = new FaultSectionPrefData();
			subSection.setFaultSectionPrefData(this);
			subSection.setFaultTrace(equalLengthSubsTrace.getSubSectionTrace(i));
			subSection.setSectionId(this.sectionId*1000+i);
			subSectionList.add(subSection);
			subSection.setSectionName(this.sectionName+" Subsection:"+(i+1));
		}
		return subSectionList;
	}

	public double getSlipRateStdDev() {
		return slipRateStdDev;
	}

	public void setSlipRateStdDev(double slipRateStdDev) {
		this.slipRateStdDev = slipRateStdDev;
	}
	
	/**
	 * Make simple fault data.  This reduces the lower seis depth by the aseismicSlipFactor if aseisReducesArea is true
	 *
	 * @param faultSection
	 * @return
	 */
	public SimpleFaultData getSimpleFaultData(boolean aseisReducesArea) {
		if(!aseisReducesArea) {
			SimpleFaultData simpleFaultData = new SimpleFaultData(getAveDip(), getAveLowerDepth(), getAveUpperDepth(), getFaultTrace());
			return simpleFaultData;
		}
		else {
			//adjust the upper & lower seis depth according the aseis factor
			double depthToReduce = aseismicSlipFactor*(getAveLowerDepth() - getAveUpperDepth());
			double lowerDepth = getAveLowerDepth()-depthToReduce/2.0;
			double upperDepth = getAveUpperDepth() + depthToReduce/2.0;
			//System.out.println(depthToReduce+","+lowerDepth+","+upperDepth);
			SimpleFaultData simpleFaultData = new SimpleFaultData(getAveDip(), lowerDepth, upperDepth, getFaultTrace());
			return simpleFaultData;
			
		}
	}

	public Element toXMLMetadata(Element root) {
		
		Element el = root.addElement(XML_METADATA_NAME);
		el.addAttribute("sectionId", this.getSectionId() + "");
		el.addAttribute("sectionName", this.getSectionName());
		el.addAttribute("shortName", this.getShortName());
		el.addAttribute("aveLongTermSlipRate", this.getAveLongTermSlipRate() + "");
		el.addAttribute("slipRateStdDev", this.getSlipRateStdDev() + "");
		el.addAttribute("aveDip", this.getAveDip() + "");
		el.addAttribute("aveRake", this.getAveRake() + "");
		el.addAttribute("aveUpperDepth", this.getAveUpperDepth() + "");
		el.addAttribute("aveLowerDepth", this.getAveLowerDepth() + "");
		el.addAttribute("aseismicSlipFactor", this.getAseismicSlipFactor() + "");
		el.addAttribute("dipDirection", this.getDipDirection() + "");
		
		FaultTrace trace = this.getFaultTrace();
		
		Element traceEl = el.addElement("FaultTrace");
		traceEl.addAttribute("name", trace.getName());
		
		for (int j=0; j<trace.getNumLocations(); j++) {
			Location loc = trace.getLocationAt(j);
			Element locEl = traceEl.addElement("Location");
			
			locEl.addAttribute("lat", loc.getLatitude() + "");
			locEl.addAttribute("lon", loc.getLongitude() + "");
		}
		return root;
	}
	
	public static FaultSectionPrefData fromXMLMetadata(Element el) throws InvocationTargetException {
		
		int sectionId = Integer.parseInt(el.attributeValue("sectionId"));
		String sectionName = el.attributeValue("sectionName");
		String shortName = el.attributeValue("shortName");
		double aveLongTermSlipRate = Double.parseDouble(el.attributeValue("aveLongTermSlipRate"));
		double aveDip = Double.parseDouble(el.attributeValue("aveDip"));
		double aveRake = Double.parseDouble(el.attributeValue("aveRake"));
		double aveUpperDepth = Double.parseDouble(el.attributeValue("aveUpperDepth"));
		double aveLowerDepth = Double.parseDouble(el.attributeValue("aveLowerDepth"));
		double aseismicSlipFactor = Double.parseDouble(el.attributeValue("aseismicSlipFactor"));
		float dipDirection = Float.parseFloat(el.attributeValue("dipDirection"));

		Element traceEl = el.element("FaultTrace");

		String traceName = traceEl.attributeValue("name");
		
		FaultTrace trace = new FaultTrace(traceName);
		
		Iterator<Element> traceIt = traceEl.elementIterator();
		while (traceIt.hasNext()) {
			Element locEl = traceIt.next();
			
			double lat = Double.parseDouble(locEl.attributeValue("lat"));
			double lon = Double.parseDouble(locEl.attributeValue("lon"));
			
			trace.addLocation(new Location(lat, lon));
		}
		
		FaultSectionPrefData data = new FaultSectionPrefData();
		data.setSectionId(sectionId);
		data.setSectionName(sectionName);
		data.setShortName(shortName);
		data.setAveLongTermSlipRate(aveLongTermSlipRate);
		data.setAveDip(aveDip);
		data.setAveRake(aveRake);
		data.setAveUpperDepth(aveUpperDepth);
		data.setAveLowerDepth(aveLowerDepth);
		data.setAseismicSlipFactor(aseismicSlipFactor);
		data.setDipDirection(dipDirection);
		data.setFaultTrace(trace);
		
		return data;
	}

}
