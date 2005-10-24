package org.opensha.refFaultParamDb.vo;

import org.opensha.sha.fault.FaultTrace;

/**
 * <p>Title: FaultSection2002.java </p>
 * <p>Description: Read the fault section 2002 database. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FaultSection2002 {
  private String faultId;
  private String sectionName;
  private String entryDate;
  private String faultModel;
  private String comments;
  private float aveLongTermSlipRate;
  private float aveDip;
  private float aveUpperSeisDepth;
  private float aveLowerSeisDepth;
  private String sectionId;
  private String nshm02Id;
  private FaultTrace faultTrace;

  public FaultSection2002() {
  }
  public float getAveDip() {
    return aveDip;
  }
  public float getAveLongTermSlipRate() {
    return aveLongTermSlipRate;
  }
  public float getAveLowerSeisDepth() {
    return aveLowerSeisDepth;
  }
  public float getAveUpperSeisDepth() {
    return aveUpperSeisDepth;
  }
  public String getComments() {
    return comments;
  }
  public String getEntryDate() {
    return entryDate;
  }
  public String getFaultId() {
    return faultId;
  }
  public String getFaultModel() {
    return faultModel;
  }
  public FaultTrace getFaultTrace() {
    return faultTrace;
  }
  public String getNshm02Id() {
    return nshm02Id;
  }
  public String getSectionId() {
    return sectionId;
  }
  public String getSectionName() {
    return sectionName;
  }
  public void setAveDip(float aveDip) {
    this.aveDip = aveDip;
  }
  public void setAveLongTermSlipRate(float aveLongTermSlipRate) {
    this.aveLongTermSlipRate = aveLongTermSlipRate;
  }
  public void setAveLowerSeisDepth(float aveLowerSeisDepth) {
    this.aveLowerSeisDepth = aveLowerSeisDepth;
  }
  public void setAveUpperSeisDepth(float aveUpperSeisDepth) {
    this.aveUpperSeisDepth = aveUpperSeisDepth;
  }
  public void setComments(String comments) {
    this.comments = comments;
  }
  public void setEntryDate(String entryDate) {
    this.entryDate = entryDate;
  }
  public void setFaultId(String faultId) {
    this.faultId = faultId;
  }
  public void setFaultModel(String faultModel) {
    this.faultModel = faultModel;
  }
  public void setFaultTrace(FaultTrace faultTrace) {
    this.faultTrace = faultTrace;
  }
  public void setNshm02Id(String nshm02Id) {
    this.nshm02Id = nshm02Id;
  }
  public void setSectionId(String sectionId) {
    this.sectionId = sectionId;
  }
  public void setSectionName(String sectionName) {
    this.sectionName = sectionName;
  }

}