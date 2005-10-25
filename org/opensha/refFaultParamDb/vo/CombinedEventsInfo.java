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
  private String datedFeatureComments;
  private ArrayList referenceList;
  private String entryDate;
  private int infoId;
  private String contributorName;
  private CombinedSlipRateInfo combinedSlipRateInfo;
  private CombinedDisplacementInfo combinedDisplacementInfo;
  private CombinedNumEventsInfo combinedNumEventsInfo;

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

  public ArrayList getReferenceList() {
    return this.referenceList;
  }
  public void setReferenceList(ArrayList referenceList) {
    this.referenceList = referenceList;
  }

  public String getDatedFeatureComments() {
    return datedFeatureComments;
  }

  public TimeAPI getEndTime() {
    return endTime;
  }

  public String getSiteEntryDate() {
    return siteEntryDate;
  }
  public int getSiteId() {
    return siteId;
  }

  public TimeAPI getStartTime() {
    return startTime;
  }
  public void setStartTime(TimeAPI startTime) {
    this.startTime = startTime;
  }

  public void setSiteEntryDate(String siteEntryDate) {
    this.siteEntryDate = siteEntryDate;
  }

  public void setEndTime(TimeAPI endTime) {
    this.endTime = endTime;
  }

  public void setDatedFeatureComments(String datedFeatureComments) {
    this.datedFeatureComments = datedFeatureComments;
  }
  public void setSiteId(int siteId) {
    this.siteId = siteId;
  }
  public CombinedDisplacementInfo getCombinedDisplacementInfo() {
    return combinedDisplacementInfo;
  }
  public CombinedNumEventsInfo getCombinedNumEventsInfo() {
    return combinedNumEventsInfo;
  }
  public CombinedSlipRateInfo getCombinedSlipRateInfo() {
    return combinedSlipRateInfo;
  }
  public void setCombinedDisplacementInfo(CombinedDisplacementInfo combinedDisplacementInfo) {
    this.combinedDisplacementInfo = combinedDisplacementInfo;
  }
  public void setCombinedNumEventsInfo(CombinedNumEventsInfo combinedNumEventsInfo) {
    this.combinedNumEventsInfo = combinedNumEventsInfo;
  }
  public void setCombinedSlipRateInfo(CombinedSlipRateInfo combinedSlipRateInfo) {
    this.combinedSlipRateInfo = combinedSlipRateInfo;
  }

}