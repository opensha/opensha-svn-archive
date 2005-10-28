package org.opensha.refFaultParamDb.vo;

/**
 * <p>Title: CombinedDisplacementInfo.java </p>
 * <p>Description: This class saves the information if the user wants to enter
 * displacement info for a site.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CombinedDisplacementInfo {
  private EstimateInstances displacementEstimate;
  private EstimateInstances aSeismicSlipFactorEstimateForDisp;
  private String displacementComments;
  private double senseOfMotionRake=Double.NaN;
  private double measuredComponentRake=Double.NaN;

  public CombinedDisplacementInfo() {
  }
  public EstimateInstances getASeismicSlipFactorEstimateForDisp() {
    return aSeismicSlipFactorEstimateForDisp;
  }
  public String getDisplacementComments() {
    return displacementComments;
  }
  public EstimateInstances getDisplacementEstimate() {
    return displacementEstimate;
  }
  public void setDisplacementEstimate(EstimateInstances displacementEstimate) {
    this.displacementEstimate = displacementEstimate;
  }
  public void setDisplacementComments(String displacementComments) {
    this.displacementComments = displacementComments;
  }
  public void setASeismicSlipFactorEstimateForDisp(EstimateInstances aSeismicSlipFactorEstimateForDisp) {
    this.aSeismicSlipFactorEstimateForDisp = aSeismicSlipFactorEstimateForDisp;
  }
  public double getSenseOfMotionRake() {
    return this.senseOfMotionRake;
  }

  public double getMeasuredComponentRake() {
    return this.measuredComponentRake;
  }
  public void setMeasuredComponentRake(double measuredComponentRake) {
    this.measuredComponentRake = measuredComponentRake;
  }
  public void setSenseOfMotionRake(double senseOfMotionRake) {
    this.senseOfMotionRake = senseOfMotionRake;
  }
}