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
  private String entryDte;
  private String comments;
  private ArrayList shortCitationsList;

  public PaleoEvent() {
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
  public String getEntryDte() {
    return entryDte;
  }
  public String getEventName() {
    return eventName;
  }
  public TimeAPI getEventTime() {
    return eventTime;
  }
  public ArrayList getShortCitationsList() {
    return shortCitationsList;
  }
  public int getSiteId() {
    return siteId;
  }
  public void setSiteId(int siteId) {
    this.siteId = siteId;
  }
  public void setShortCitationsList(ArrayList shortCitationsList) {
    this.shortCitationsList = shortCitationsList;
  }
  public void setEventTime(TimeAPI eventTime) {
    this.eventTime = eventTime;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  public void setEntryDte(String entryDte) {
    this.entryDte = entryDte;
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


}