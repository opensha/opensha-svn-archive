package org.opensha.refFaultParamDb.gui.addEdit.paleoSite;


import java.util.ArrayList;

import org.opensha.commons.param.StringListParameter;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.editor.ConstrainedStringListParameterEditor;

import java.awt.*;
import org.opensha.refFaultParamDb.gui.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.refFaultParamDb.gui.event.DbAdditionListener;

import org.opensha.refFaultParamDb.gui.event.DbAdditionSuccessEvent;

/**
 * <p>Title: AddNewTimeSpan</p>
 * <p>Description:  This class allows the user to add new Timespn for a given Site.</p>
 * @author Vipin Gupta
 * @version 1.0
 */

public class AddEditTimeSpan extends JPanel  {
  // start time estimate param
  private final static String START_TIME_PARAM_NAME="Start Time";
  // end time estimate param
  private final static String END_TIME_PARAM_NAME="End Time";
  private final static String TIMESPAN_COMMENTS_PARAM_NAME="Dating Methodology";
  private final static String TIMESPAN_COMMENTS_DEFAULT="Summary of dating techniques and dated features";

  private final static String TITLE = "Add Time Span";

  // various parameters
   private StringParameter timeSpanCommentsParam;

   // parameter editors
   private CommentsParameterEditor timeSpanCommentsParamEditor;


  // time gui bean
  private TimeGuiBean startTimeBean;
  private TimeGuiBean endTimeBean;
  private JSplitPane timSpanSplitPane = new JSplitPane();
  private JPanel commentsPanel = new JPanel();

  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  public AddEditTimeSpan() {
    try {
      jbInit();
      addTimeEstimateParametersAndEditors();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    this.setVisible(true);
  }

  /**
   * Set values in the editor
   * @param startTime
   * @param endTime
   * @param comments
   */
  public AddEditTimeSpan(TimeAPI startTime, TimeAPI endTime,
                         String comments) {
    this();
    setValuesInParameters(startTime, endTime, comments);
  }

  private void setValuesInParameters(TimeAPI startTime, TimeAPI endTime,
                         String comments) {
    startTimeBean.setTime(startTime);
    endTimeBean.setTime(endTime);
    timeSpanCommentsParam.setValue(comments);
    timeSpanCommentsParamEditor.refreshParamEditor();
  }

  public void setNowYearVal(int nowYearVal) {
    endTimeBean.setNowYearVal(nowYearVal);
  }


  /**
  * Add the start and end time estimate parameters
  */
 private void addTimeEstimateParametersAndEditors() throws Exception{
   // start time estimate
   startTimeBean = new TimeGuiBean(this.START_TIME_PARAM_NAME, false);
   //end time estimate
   endTimeBean = new TimeGuiBean(this.END_TIME_PARAM_NAME, true);
   timSpanSplitPane.add(startTimeBean, JSplitPane.LEFT);
   timSpanSplitPane.add(endTimeBean, JSplitPane.RIGHT);
   timSpanSplitPane.setDividerLocation(325);

   // timespan comments
   timeSpanCommentsParam = new StringParameter(TIMESPAN_COMMENTS_PARAM_NAME);
   timeSpanCommentsParamEditor = new CommentsParameterEditor(timeSpanCommentsParam);
   commentsPanel.add(timeSpanCommentsParamEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
 }




  private void jbInit() throws Exception {
    setLayout(gridBagLayout1);
    this.setMinimumSize(new Dimension(0, 0));
    add(timSpanSplitPane,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 0, 4), 374, 432));
    this.add(commentsPanel,  new GridBagConstraints(0, 1, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 3, 3, 0), 256, 90));
    timSpanSplitPane.setOrientation(timSpanSplitPane.HORIZONTAL_SPLIT);
    commentsPanel.setLayout(gridBagLayout1);
  }



  /**
   * Get the start time for this time span
   * @return
   */
  public TimeAPI getStartTime() {
    TimeAPI startTime = this.startTimeBean.getSelectedTime();
    setDatingComments(startTime);
    return startTime;
  }

  /**
   * Set references and dating comments
   * @param timeAPI
   * @throws java.lang.RuntimeException
   */
  private void setDatingComments(TimeAPI timeAPI) throws
      RuntimeException {
    timeAPI.setDatingComments((String)this.timeSpanCommentsParam.getValue());
  }

  /**
   * Get the end time for this time span
   * @return
   */
  public TimeAPI getEndTime() {
    TimeAPI endTime = this.endTimeBean.getSelectedTime();
    setDatingComments(endTime);
    return endTime;
  }

  /**
   * Get the comments about this timespan
   * @return
   */
  public String getTimeSpanComments() {
    return (String)timeSpanCommentsParam.getValue();
  }

}
