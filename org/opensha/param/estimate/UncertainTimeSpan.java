package org.opensha.param.estimate;
import org.opensha.data.estimate.Estimate;


/**
 * <p>Title: UncertainTimeSpan.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class UncertainTimeSpan {
  private DateEstParameter startDateEst;
  private DateEstParameter endDateEst;

  public UncertainTimeSpan() {
  }

  public void set(DateEstParameter startDateEst,
                  DateEstParameter endDateEst) {
    this.startDateEst = startDateEst;
    this.endDateEst = endDateEst;
  }

  public void set(DateEstParameter startDateEst,
                  Estimate durationEst) {
    /**@todo Implement this org.opensha.param.estimate.UncertainTimeSpan method*/
    throw new java.lang.UnsupportedOperationException("Method set() not yet implemented.");
  }


}
