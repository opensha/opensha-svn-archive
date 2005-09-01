package org.opensha.refFaultParamDb.data;
import org.opensha.data.estimate.Estimate;
/**
 * <p>Title: TimeEstimate.java </p>
 * <p>Description: Allows the user to specify a time estimate. This estimate
 * can be a start time estimate or an end time estimate in a time span.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TimeEstimate implements TimeAPI {
  private Estimate estimate;

  public TimeEstimate() {
  }

}