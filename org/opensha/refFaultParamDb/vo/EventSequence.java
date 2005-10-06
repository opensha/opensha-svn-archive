package org.opensha.refFaultParamDb.vo;

import java.util.ArrayList;

/**
 * <p>Title: EventSequence.java </p>
 * <p>Description: This class saves the information about a event sequence</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EventSequence {

  private String sequenceName;
  private double sequenceProb;
  private String comments;
  private ArrayList eventsParam;
  private double[] missedEventsProbs;
  private int siteId;
  private String siteEntryDate;

  public EventSequence() {
  }
  public String getComments() {
    return comments;
  }
  public ArrayList getEventsParam() {
    return eventsParam;
  }
  public double[] getMissedEventsProbs() {
    return missedEventsProbs;
  }
  public String getSequenceName() {
    return sequenceName;
  }
  public double getSequenceProb() {
    return sequenceProb;
  }
  public void setComments(String comments) {
    this.comments = comments;
  }
  public void setEventsParam(ArrayList eventsParam) {
    this.eventsParam = eventsParam;
  }
  public void setMissedEventsProbList(double[] missedEventsProbs) {
    this.missedEventsProbs = missedEventsProbs;
  }
  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }
  public void setSequenceProb(double sequenceProb) {
    this.sequenceProb = sequenceProb;
  }
  public String getSiteEntryDate() {
    return siteEntryDate;
  }
  public int getSiteId() {
    return siteId;
  }
  public void setSiteEntryDate(String siteEntryDate) {
    this.siteEntryDate = siteEntryDate;
  }
  public void setSiteId(int siteId) {
    this.siteId = siteId;
  }


}