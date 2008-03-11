/**
 * 
 */
package org.opensha.refFaultParamDb.vo;

/**
 * @author vipingupta
 *
 */
public class DeformationModelSummary  implements java.io.Serializable {
	private int deformationModelId;
	private String deformationModelName;
	private FaultModelSummary faultModel;
	private Contributor contributor;
	
	public Contributor getContributor() {
		return contributor;
	}
	public void setContributor(Contributor contributor) {
		this.contributor = contributor;
	}
	public int getDeformationModelId() {
		return deformationModelId;
	}
	public void setDeformationModelId(int deformationModelId) {
		this.deformationModelId = deformationModelId;
	}
	public String getDeformationModelName() {
		return deformationModelName;
	}
	public void setDeformationModelName(String deformationModelName) {
		this.deformationModelName = deformationModelName;
	}
	public FaultModelSummary getFaultModel() {
		return faultModel;
	}
	public void setFaultModel(FaultModelSummary faultModel) {
		this.faultModel = faultModel;
	}
	
}
