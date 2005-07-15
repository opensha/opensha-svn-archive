package org.opensha.refFaultParamDb.vo;

/**
 * <p>Title: FaultModel.java </p>
 * <p>Description: Various fault models available</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FaultModel {
  private int faultModelId;
  private String faultModelName;
  private Contributor contributor;

  public FaultModel() {
  }

  public FaultModel(int faultModelId, String faultModelName, Contributor contributor) {
   setFaultModelId(faultModelId);
   setContributor(contributor);
   setFaultModelName(faultModelName);
 }

 public FaultModel(String faultModelName, Contributor contributor) {
  setContributor(contributor);
  setFaultModelName(faultModelName);
}


  public Contributor getContributor() {
    return contributor;
  }
  public int getFaultModelId() {
    return faultModelId;
  }
  public String getFaultModelName() {
    return faultModelName;
  }
  public void setContributor(Contributor contributor) {
    this.contributor = contributor;
  }
  public void setFaultModelId(int faultModelId) {
    this.faultModelId = faultModelId;
  }
  public void setFaultModelName(String faultModelName) {
    this.faultModelName = faultModelName;
  }


}