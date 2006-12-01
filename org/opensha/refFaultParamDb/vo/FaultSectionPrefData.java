/**
 * 
 */
package org.opensha.refFaultParamDb.vo;

import java.util.ArrayList;

import org.opensha.sha.fault.EqualLengthSubSectionsTrace;
import org.opensha.sha.fault.FaultTrace;

/**
 * This class saves th preferred values (rather than the estimate) from the FaultSectionData
 * @author vipingupta
 *
 */
public class FaultSectionPrefData {
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

}
