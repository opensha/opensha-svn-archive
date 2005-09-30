package org.opensha.refFaultParamDb.vo;

import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.data.estimate.Estimate;
import java.util.ArrayList;

/**
 * <p>Title: CombinedEventsInfo.java </p>
 * <p>Description: Put the combined events info for a particular site into the database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CombinedEventsInfo {
  private int siteId;
  private String siteEntryDate;
  private TimeAPI startTime;
  private TimeAPI endTime;
  private EstimateInstances displacementEstimate;
  private EstimateInstances slipRateEstimate;
  private EstimateInstances numEventsEstimate;
  private EstimateInstances aSeismicSlipFactorEstimate;
  private String slipRateComments;
  private String numEventsComments;
  private String displacementComments;
  private String datedFeatureComments;
  private ArrayList shortCitationList;
  private String entryDate;
  private int infoId;
  private String contributorName;

  /*
   * Various  set/get methods
   */
  public String getEntryDate() {
    return this.entryDate;
  }
  public void setEntryDate(String entryDate) {
    this.entryDate = entryDate;
  }
  public void setContributorName(String contributorName) {
    this.contributorName = contributorName;
  }
  public String getContributorName() {
    return this.contributorName;
  }
  public int getInfoId() {
    return this.infoId;
  }
  public void setInfoId(int infoId) {
    this.infoId = infoId;
  }

  public ArrayList getShortCitationList() {
    return this.shortCitationList;
  }
  public void setShortCitationList(ArrayList shortCitationList) {
    this.shortCitationList = shortCitationList;
  }
  public EstimateInstances getASeismicSlipFactorEstimate() {
    return aSeismicSlipFactorEstimate;
  }
  public String getDatedFeatureComments() {
    return datedFeatureComments;
  }
  public String getDisplacementComments() {
    return displacementComments;
  }
  public EstimateInstances getDisplacementEstimate() {
    return displacementEstimate;
  }
  public TimeAPI getEndTime() {
    return endTime;
  }
  public String getNumEventsComments() {
    return numEventsComments;
  }
  public EstimateInstances getNumEventsEstimate() {
    return numEventsEstimate;
  }
  public String getSiteEntryDate() {
    return siteEntryDate;
  }
  public int getSiteId() {
    return siteId;
  }
  public String getSlipRateComments() {
    return slipRateComments;
  }
  public EstimateInstances getSlipRateEstimate() {
    return slipRateEstimate;
  }
  public TimeAPI getStartTime() {
    return startTime;
  }
  public void setStartTime(TimeAPI startTime) {
    this.startTime = startTime;
  }
  public void setSlipRateEstimate(EstimateInstances slipRateEstimate) {
    this.slipRateEstimate = slipRateEstimate;
  }
  public void setSlipRateComments(String slipRateComments) {
    this.slipRateComments = slipRateComments;
  }
  public void setSiteEntryDate(String siteEntryDate) {
    this.siteEntryDate = siteEntryDate;
  }
  public void setNumEventsEstimate(EstimateInstances numEventsEstimate) {
    this.numEventsEstimate = numEventsEstimate;
  }
  public void setNumEventsComments(String numEventsComments) {
    this.numEventsComments = numEventsComments;
  }
  public void setEndTime(TimeAPI endTime) {
    this.endTime = endTime;
  }
  public void setDisplacementEstimate(EstimateInstances displacementEstimate) {
    this.displacementEstimate = displacementEstimate;
  }
  public void setDisplacementComments(String displacementComments) {
    this.displacementComments = displacementComments;
  }
  public void setDatedFeatureComments(String datedFeatureComments) {
    this.datedFeatureComments = datedFeatureComments;
  }
  public void setASeismicSlipFactorEstimate(EstimateInstances aSeismicSlipFactorEstimate) {
    this.aSeismicSlipFactorEstimate = aSeismicSlipFactorEstimate;
  }
  public void setSiteId(int siteId) {
    this.siteId = siteId;
  }

}