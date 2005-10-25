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

   public CombinedSlipRateInfo() {
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
  public void setASeismicSlipFactorEstimateForSlip(EstimateInstances aSeismicSlipFactorEstimateForSlip) {
    this.aSeismicSlipFactorEstimateForSlip = aSeismicSlipFactorEstimateForSlip;
  }
  public void setSlipRateComments(String slipRateComments) {
    this.slipRateComments = slipRateComments;
  }
  public void setSlipRateEstimate(EstimateInstances slipRateEstimate) {
    this.slipRateEstimate = slipRateEstimate;
  }


}