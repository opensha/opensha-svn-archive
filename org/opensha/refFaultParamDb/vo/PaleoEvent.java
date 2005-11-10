package org.opensha.refFaultParamDb.vo;

import java.util.ArrayList;
import org.opensha.refFaultParamDb.data.TimeAPI;

/**
 * <p>Title: PaleoEvent.java </p>
 * <p>Description: This class holds the information existing in the database
 * for Paleo Events </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PaleoEvent {
  private String eventName;
  private int siteId;
  private String siteEntryDate;
  private String contributorName;
  private TimeAPI eventTime;
  private int displacementEstId;
  private EstimateInstances displacementEst;
  private String entryDate;
  private String comments;
  private ArrayList referenceList;
  private int eventId;
  private boolean displacementShared;
  private double senseOfMotionRake=Double.NaN;
  private String senseOfMotionQual;
  private double measuredComponentRake=Double.NaN;
  private String measuredComponentQual;

  public PaleoEvent() {
  }

  public void setDisplacementShared(boolean displacementShared) {
    this.displacementShared = displacementShared;
  }

  public boolean isDisplacementShared() {
    return this.displacementShared;
  }

  public int getEventId() {
    return this.eventId;
  }

  public void setEventId(int eventId) {
    this.eventId = eventId;
  }
  public String getSiteEntryDate() {
    return this.siteEntryDate;
  }
  public void setSiteEntryDate(String siteEntryDate) {
    this.siteEntryDate = siteEntryDate;
  }
  public String getComments() {
    return comments;
  }
  public String getContributorName() {
    return contributorName;
  }
  public int getDisplacementEstId() {
    return displacementEstId;
  }
  public String getEntryDate() {
    return entryDate;
  }
  public String getEventName() {
    return eventName;
  }
  public TimeAPI getEventTime() {
    return eventTime;
  }
  public ArrayList getReferenceList() {
    return this.referenceList;
  }
  public int getSiteId() {
    return siteId;
  }
  public void setSiteId(int siteId) {
    this.siteId = siteId;
  }
  public void setReferenceList(ArrayList referenceList) {
    this.referenceList = referenceList;
  }
  public void setEventTime(TimeAPI eventTime) {
    this.eventTime = eventTime;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  public void setEntryDate(String entryDate) {
    this.entryDate = entryDate;
  }
  public void setDisplacementEstId(int displacementEst) {
    this.displacementEstId = displacementEst;
  }
  public void setContributorName(String contributorName) {
    this.contributorName = contributorName;
  }
  public void setComments(String comments) {
    this.comments = comments;
  }
  public EstimateInstances getDisplacementEst() {
    return displacementEst;
  }
  public void setDisplacementEst(EstimateInstances displacementEst) {
    this.displacementEst = displacementEst;
  }
  public  String getSenseOfMotionQual() {
    return this.senseOfMotionQual;
  }
  public String getMeasuredComponentQual() {
    return this.measuredComponentQual;
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
  public double getMeasuredComponentRake() {
    return measuredComponentRake;
  }
  public double getSenseOfMotionRake() {
    return senseOfMotionRake;
  }
}