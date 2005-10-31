package org.opensha.refFaultParamDb.vo;

/**
 * <p>Title: CombinedSlipRateInfo.java </p>
 * <p>Description: This class saves all the information if the user wants
 * to contribute slip rate info for a site.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CombinedSlipRateInfo {
  private EstimateInstances slipRateEstimate;
  private EstimateInstances aSeismicSlipFactorEstimateForSlip;
  private String slipRateComments;
  private double senseOfMotionRake=Double.NaN;
  private String senseOfMotionQual;
  private double measuredComponentRake=Double.NaN;
  private String measuredComponentQual;

  public CombinedSlipRateInfo() {
  }
  public  String getSenseOfMotionQual() {
    return this.senseOfMotionQual;
  }
  public String getMeasuredComponentQual() {
    return this.measuredComponentQual;
  }
  public EstimateInstances getASeismicSlipFactorEstimateForSlip() {
    return aSeismicSlipFactorEstimateForSlip;
  }
  public String getSlipRateComments() {
    return slipRateComments;
  }
  public EstimateInstances getSlipRateEstimate() {
    return slipRateEstimate;
  }
  public double getSenseOfMotionRake() {
    return this.senseOfMotionRake;
  }
  public double getMeasuredComponentRake() {
    return this.measuredComponentRake;
  }
  public void setASeismicSlipFactorEstimateForSlip(EstimateInstances aSeismicSlipFactorEstimateForSlip) {
    this.aSeismicSlipFactorEstimateForSlip = aSeismicSlipFactorEstimateForSlip;
  }
  public void setSlipRateComments(String slipRateComments) {
    this.slipRateComments = slipRateComments;
  }
  public void setSlipRateEstimate(EstimateInstances slipRateEstimate) {
    this.slipRateEstimate = slipRateEstimate;
  }
  public void setMeasuredComponentRake(double measuredComponentRake) {
    this.measuredComponentRake = measuredComponentRake;
  }
  public void setSenseOfMotionRake(double senseOfMotionRake) {
    this.senseOfMotionRake = senseOfMotionRake;
  }
  public void setMeasuredComponentQual(String measuredComponentQual) {
    this.measuredComponentQual = measuredComponentQual;
  }
  public void setSenseOfMotionQual(String senseOfMotionQual) {
    this.senseOfMotionQual = senseOfMotionQual;
  }
}