package org.opensha.refFaultParamDb.data;

import java.util.ArrayList;

/**
 * <p>Title: TimeAPI.java </p>
 * <p>Description: API for specifying the times. It is used for specifying the
 * event time as well as start time (or an end  time) for timeSpan in a site</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TimeAPI {
  // constant values for AD/BC
  public final static String AD = "AD";
  public final static String BC = "BC";

  private ArrayList referencesList;
  private String datingComments;
  public String getDatingComments() {
    return datingComments;
  }
  public ArrayList getReferencesList() {
    return referencesList;
  }
  public void setDatingComments(String datingComments) {
    this.datingComments = datingComments;
  }
  public void setReferencesList(ArrayList referencesList) {
    this.referencesList = referencesList;
  }
}