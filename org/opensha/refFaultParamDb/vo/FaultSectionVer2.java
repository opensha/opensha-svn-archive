package org.opensha.refFaultParamDb.vo;

import org.opensha.sha.fault.FaultTrace;

/**
 * <p>Title: FaultSectionVer2.java </p>
 * <p>Description: Fault Section information saved in the database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FaultSectionVer2 {

  private int sectionId=-1;
  private String sectionName;
  private String faultName;
  private EstimateInstances aveLongTermSlipRateEst;
  private EstimateInstances aveDipEst;
  private EstimateInstances aveRakeEst;
  private EstimateInstances aveUpperDepthEst;
  private EstimateInstances aveLowerDepthEst;
  private EstimateInstances aseismicSlipFactorEst;
  private String entryDate;
  private String comments="";
  private FaultTrace faultTrace;
  private float dipDirection;


  public FaultSectionVer2() {
  }
  public EstimateInstances getAseismicSlipFactorEst() {
    return aseismicSlipFactorEst;
  }
  public EstimateInstances getAveDipEst() {
    return aveDipEst;
  }
  public EstimateInstances getAveLongTermSlipRateEst() {
    return aveLongTermSlipRateEst;
  }
  public EstimateInstances getAveLowerDepthEst() {
    return aveLowerDepthEst;
  }
  public EstimateInstances getAveRakeEst() {
    return aveRakeEst;
  }
  public EstimateInstances getAveUpperDepthEst() {
    return aveUpperDepthEst;
  }
  public String getComments() {
    return comments;
  }
  public float getDipDirection() {
    return dipDirection;
  }
  public String getEntryDate() {
    return entryDate;
  }
  public String getFaultName() {
    return faultName;
  }
  public FaultTrace getFaultTrace() {
    return faultTrace;
  }
  public int getSectionId() {
    return sectionId;
  }
  public String getSectionName() {
    return sectionName;
  }

  public void setSectionName(String sectionName) {
    this.sectionName = sectionName;
  }
  public void setSectionId(int sectionId) {
    this.sectionId = sectionId;
  }
  public void setFaultTrace(FaultTrace faultTrace) {
    this.faultTrace = faultTrace;
  }
  public void setFaultName(String faultName) {
    this.faultName = faultName;
  }
  public void setEntryDate(String entryDate) {
    this.entryDate = entryDate;
  }
  public void setDipDirection(float dipDirection) {
    this.dipDirection = dipDirection;
  }
  public void setComments(String comments) {
    this.comments = comments;
  }
  public void setAveUpperDepthEst(EstimateInstances aveUpperDepthEst) {
    this.aveUpperDepthEst = aveUpperDepthEst;
  }
  public void setAveRakeEst(EstimateInstances aveRakeEst) {
    this.aveRakeEst = aveRakeEst;
  }
  public void setAveLowerDepthEst(EstimateInstances aveLowerDepthEst) {
    this.aveLowerDepthEst = aveLowerDepthEst;
  }
  public void setAveLongTermSlipRateEst(EstimateInstances aveLongTermSlipRateEst) {
    this.aveLongTermSlipRateEst = aveLongTermSlipRateEst;
  }
  public void setAveDipEst(EstimateInstances aveDipEst) {
    this.aveDipEst = aveDipEst;
  }
  public void setAseismicSlipFactorEst(EstimateInstances aseismicSlipFactorEst) {
    this.aseismicSlipFactorEst = aseismicSlipFactorEst;
  }

}