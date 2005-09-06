package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.JFrame;

import java.util.ArrayList;

import org.opensha.param.estimate.*;
import javax.swing.JSplitPane;
import java.awt.*;
import javax.swing.JPanel;
import org.opensha.refFaultParamDb.gui.*;

/**
 * <p>Title: AddNewTimeSpan</p>
 * <p>Description:  This class allows the user to add new Timespn for a given Site.</p>
 * @author Vipin Gupta
 * @version 1.0
 */

public class AddEditTimeSpan extends JPanel {
  // start time estimate param
  private final static String START_TIME_PARAM_NAME="Start Time";

  // end time estimate param
  private final static String END_TIME_PARAM_NAME="End Time";

  private final static String TITLE = "Add Time Span";

  // time gui bean
  private TimeGuiBean startTimeBean;
  private TimeGuiBean endTimeBean;
  private JSplitPane timSpanSplitPane = new JSplitPane();

  private BorderLayout borderLayout1 = new BorderLayout();

  public AddEditTimeSpan() {
    try {
      addTimeEstimateParametersAndEditors();
      jbInit();

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    this.setVisible(true);
  }

  /**
  * Add the start and end time estimate parameters
  */
 private void addTimeEstimateParametersAndEditors() {
   // create constraint of allowed estimate types
   //ArrayList startDateEstimatesList =  EstimateConstraint.createConstraintForDateEstimates();
   // start time estimate
   startTimeBean = new TimeGuiBean(this.START_TIME_PARAM_NAME);
   //end time estimate
   endTimeBean = new TimeGuiBean(this.END_TIME_PARAM_NAME);
   timSpanSplitPane.add(startTimeBean, JSplitPane.LEFT);
   timSpanSplitPane.add(endTimeBean, JSplitPane.RIGHT);
   timSpanSplitPane.setDividerLocation(220);
 }

  private void jbInit() throws Exception {
    setLayout(borderLayout1);
    add(timSpanSplitPane, java.awt.BorderLayout.CENTER);
    timSpanSplitPane.setOrientation(timSpanSplitPane.HORIZONTAL_SPLIT);
  }

}
