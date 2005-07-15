package org.opensha.refFaultParamDb.vo;
import java.util.Date;

/**
 * <p>Title: EstimateType.java </p>
 * <p>Description: Various estimate types in types available</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EstimateType {
  private int estimateTypeId;
  private String estimateName;
  private Date effectiveDate;

  public EstimateType() {
  }

  public EstimateType(int estimateTypeId, String estimateName, Date effectiveDate) {
    setEstimateTypeId(estimateTypeId);
    setEstimateName(estimateName);
    setEffectiveDate(effectiveDate);
  }

  public Date getEffectiveDate() {
    return effectiveDate;
  }

  public void setEffectiveDate(Date effectiveDate) {
    this.effectiveDate = effectiveDate;
  }
  public String getEstimateName() {
    return estimateName;
  }
  public void setEstimateName(String estimateName) {
    this.estimateName = estimateName;
  }
  public int getEstimateTypeId() {
    return estimateTypeId;
  }
  public void setEstimateTypeId(int estimateTypeId) {
    this.estimateTypeId = estimateTypeId;
  }
}