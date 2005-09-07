package org.opensha.refFaultParamDb.gui.addEdit;


import java.util.ArrayList;

import org.opensha.param.estimate.*;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.*;
import javax.swing.*;
import org.opensha.param.StringParameter;
import org.opensha.param.StringListParameter;
import org.opensha.param.editor.ConstrainedStringListParameterEditor;

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
  private final static String TIMESPAN_COMMENTS_PARAM_NAME="Dating Methodology";
  private final static String TIMESPAN_COMMENTS_DEFAULT="Summary of dating techniques and dated features";
  private final static String TIMESPAN_REFERENCES_PARAM_NAME="Choose References";
  private final static String ADD_REFERENCE_TEXT="Add Reference not currently in database";


  private final static String TITLE = "Add Time Span";

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
  private JButton addNewReferenceButton = new JButton();
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
    addNewReferenceButton.setText(ADD_REFERENCE_TEXT);
    add(timSpanSplitPane,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 0, 4), 374, 432));
    this.add(commentsPanel,  new GridBagConstraints(0, 1, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 3, 3, 0), 256, 90));
    this.add(referencesPanel,  new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 7, 0, 4), 270, 56));
    this.add(addNewReferenceButton,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 12, 3, 15), 191, 7));
    timSpanSplitPane.setOrientation(timSpanSplitPane.HORIZONTAL_SPLIT);
    commentsPanel.setLayout(gridBagLayout1);
    referencesPanel.setLayout(gridBagLayout1);
  }

  /**
   * Get a list of available references.
   *  THIS IS JUST A FAKE IMPLEMENTATION. IT SHOULD GET THIS FROM THE DATABASE.
   * @return
   */
  private ArrayList getAvailableReferences() {
    ArrayList referencesNamesList = new ArrayList();
    referencesNamesList.add("Reference 1");
    referencesNamesList.add("Reference 2");
    return referencesNamesList;

  }


}
