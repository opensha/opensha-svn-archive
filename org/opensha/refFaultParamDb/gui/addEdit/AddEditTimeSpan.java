package org.opensha.refFaultParamDb.gui.addEdit;


import java.util.ArrayList;

import org.opensha.param.estimate.*;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.*;
import javax.swing.*;
import org.opensha.param.StringParameter;
import org.opensha.param.StringListParameter;
import org.opensha.param.editor.ConstrainedStringListParameterEditor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.data.TimeAPI;

/**
 * <p>Title: AddNewTimeSpan</p>
 * <p>Description:  This class allows the user to add new Timespn for a given Site.</p>
 * @author Vipin Gupta
 * @version 1.0
 */

public class AddEditTimeSpan extends JPanel implements ActionListener {
  // start time estimate param
  private final static String START_TIME_PARAM_NAME="Start Time";
  // end time estimate param
  private final static String END_TIME_PARAM_NAME="End Time";
  private final static String TIMESPAN_COMMENTS_PARAM_NAME="Dating Methodology";
  private final static String TIMESPAN_COMMENTS_DEFAULT="Summary of dating techniques and dated features";
  private final static String TIMESPAN_REFERENCES_PARAM_NAME="Choose References";
  private final static String ADD_REFERENCE_TEXT="Add Reference";
  private final static String addNewReferenceToolTipText = "Add Reference not currently in database";

  private final static String TITLE = "Add Time Span";
  private final static String MSG_REF_MISSING = "Select atleast 1 reference for the timespan";

  // various parameters
   private StringParameter timeSpanCommentsParam;
   private StringListParameter timeSpanReferencesParam;

   // parameter editors
   private CommentsParameterEditor timeSpanCommentsParamEditor;
   private ConstrainedStringListParameterEditor timeSpanReferencesParamEditor;


  // time gui bean
  private TimeGuiBean startTimeBean;
  private TimeGuiBean endTimeBean;
  private JSplitPane timSpanSplitPane = new JSplitPane();
  private JPanel commentsPanel = new JPanel();
  private JPanel referencesPanel = new JPanel();
  private JButton addNewReferenceButton = new JButton(ADD_REFERENCE_TEXT);
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  // references DAO
  private ReferenceDB_DAO referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);

  public AddEditTimeSpan() {
    try {
      jbInit();
      addTimeEstimateParametersAndEditors();
      addNewReferenceButton.addActionListener(this);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    this.setVisible(true);
  }

  /**
  * When user chooses to add a new reference
  * @param event
  */
 public void actionPerformed(ActionEvent event) {
   if(event.getSource() == addNewReferenceButton) new AddNewReference();
 }


  /**
  * Add the start and end time estimate parameters
  */
 private void addTimeEstimateParametersAndEditors() throws Exception{
   // start time estimate
   startTimeBean = new TimeGuiBean(this.START_TIME_PARAM_NAME);
   //end time estimate
   endTimeBean = new TimeGuiBean(this.END_TIME_PARAM_NAME);
   timSpanSplitPane.add(startTimeBean, JSplitPane.LEFT);
   timSpanSplitPane.add(endTimeBean, JSplitPane.RIGHT);
   timSpanSplitPane.setDividerLocation(220);

   // timespan comments
   timeSpanCommentsParam = new StringParameter(TIMESPAN_COMMENTS_PARAM_NAME);
   timeSpanCommentsParamEditor = new CommentsParameterEditor(timeSpanCommentsParam);
   commentsPanel.add(timeSpanCommentsParamEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

   // references
   ArrayList availableReferences = getAvailableReferences();
   this.timeSpanReferencesParam = new StringListParameter(this.TIMESPAN_REFERENCES_PARAM_NAME, availableReferences);
   timeSpanReferencesParamEditor = new ConstrainedStringListParameterEditor(timeSpanReferencesParam);
   referencesPanel.add(timeSpanReferencesParamEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

 }


  private void jbInit() throws Exception {
    setLayout(gridBagLayout1);
    addNewReferenceButton.setToolTipText(this.addNewReferenceToolTipText);
    this.setMinimumSize(new Dimension(0, 0));
    add(timSpanSplitPane,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 0, 4), 374, 432));
    this.add(commentsPanel,  new GridBagConstraints(0, 1, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 3, 3, 0), 256, 90));
    this.add(referencesPanel,  new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 7, 0, 4), 270, 56));
    this.add(addNewReferenceButton,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 12, 3, 15), 0, 0));
    timSpanSplitPane.setOrientation(timSpanSplitPane.HORIZONTAL_SPLIT);
    commentsPanel.setLayout(gridBagLayout1);
    referencesPanel.setLayout(gridBagLayout1);
  }

  /**
   * Get a list of available references.
   * @return
   */
  private ArrayList getAvailableReferences() {
    return referenceDAO.getAllShortCitations();
  }

  /**
   * Get the start time for this time span
   * @return
   */
  public TimeAPI getStartTime() {
    TimeAPI startTime = this.startTimeBean.getSelectedTime();
    setReferencesAndDatingComments(startTime);
    return startTime;
  }

  /**
   * Set references and dating comments
   * @param timeAPI
   * @throws java.lang.RuntimeException
   */
  private void setReferencesAndDatingComments(TimeAPI timeAPI) throws
      RuntimeException {
    timeAPI.setDatingComments((String)this.timeSpanCommentsParam.getValue());
    ArrayList references = (ArrayList)this.timeSpanReferencesParam.getValue();
    if(references==null || references.size()==0)
      throw new RuntimeException(MSG_REF_MISSING);
    timeAPI.setReferencesList(references);
  }

  /**
   * Get the end time for this time span
   * @return
   */
  public TimeAPI getEndTime() {
    TimeAPI endTime = this.endTimeBean.getSelectedTime();
    setReferencesAndDatingComments(endTime);
    return endTime;
  }

  /**
   * Return a list of selected short citations
   *
   * @return
   */
  public ArrayList getTimeSpanShortCitationList() {
    return (ArrayList)timeSpanReferencesParam.getValue();
  }

  /**
   * Get the comments about this timespan
   * @return
   */
  public String getTimeSpanComments() {
    return (String)timeSpanCommentsParam.getValue();
  }
}
