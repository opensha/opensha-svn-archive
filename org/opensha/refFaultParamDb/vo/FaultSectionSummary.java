/**
 * 
 */
package org.opensha.refFaultParamDb.vo;

/**
 * Returns id and name for fault section in the table
 * @author vipingupta
 *
 */
public class FaultSectionSummary {
	private int sectionId;
	private String sectionName;
	
	public FaultSectionSummary() { 
	}
	
	public FaultSectionSummary(int sectionId, String sectionName) {
		setSectionId(sectionId);
		setSectionName(sectionName);
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
}
