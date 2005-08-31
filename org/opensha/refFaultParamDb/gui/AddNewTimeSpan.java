package org.opensha.refFaultParamDb.gui;

import javax.swing.JFrame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.util.ArrayList;
import org.opensha.param.estimate.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddNewTimeSpan extends JFrame {
  // start time estimate param
  private final static String START_TIME_PARAM_NAME="Start Time";

  // end time estimate param
  private final static String END_TIME_PARAM_NAME="End Time";

  // time gui bean
  private TimeGuiBean startTimeBean;
  private TimeGuiBean endTimeBean;

  public AddNewTimeSpan() {

  }

  /**
  * Add the start and end time estimate parameters
  */
 private void addTimeEstimateParametersAndEditors() {
   // create constraint of allowed estimate types
   ArrayList startDateEstimatesList =  EstimateConstraint.createConstraintForDateEstimates();
   // start time estimate
   startTimeBean = new TimeGuiBean(this.START_TIME_PARAM_NAME);
   //end time estimate
   endTimeBean = new TimeGuiBean(this.END_TIME_PARAM_NAME);
 }

}