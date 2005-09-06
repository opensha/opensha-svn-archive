package org.opensha.refFaultParamDb.gui.view;

import org.opensha.gui.LabeledBoxPanel;
import org.opensha.refFaultParamDb.data.TimeAPI;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.param.StringParameter;
import org.opensha.gui.TitledBorderPanel;
import javax.swing.JPanel;
import org.opensha.refFaultParamDb.gui.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditNumEvents;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditTimeSpan;
import java.util.ArrayList;

/**
 * <p>Title: ViewTimeSpan.java </p>
 * <p>Description: This class can be used for viewing timespan for site.It will contain
 * start/end time and dated feature comments</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewTimeSpan extends LabeledBoxPanel implements ActionListener{

  // start time header
  private final static String START_TIME_PARAM_NAME="Start Time:";
  // end time header
  private final static String END_TIME_PARAM_NAME="End Time:";
  // timespan header
  private final static String TIME_SPAN_PARAM_NAME="TimeSpan";
  // dated feature comments
  private final static String DATED_FEATURE_COMMENTS_PARAM_NAME="Dating Methodology";
  // dating comments params
  StringParameter datedFeatureCommentsParam = new StringParameter(this.DATED_FEATURE_COMMENTS_PARAM_NAME);
  CommentsParameterEditor datedFeatureCommentsParamEditor;
  // edit button
  private JButton editTimeSpanButton = new JButton("Edit");
  private final static String EDIT_TITLE = "Edit Timespan";
  private InfoLabel startTimeLabel = new InfoLabel();
  private InfoLabel endTimeLabel = new InfoLabel();
  private InfoLabel referencesLabel = new InfoLabel();

  /**
   *
   * @param startTime Start Time
   * @param endTime End Time
   * @param datingComments - dating feature comments
   */
  public ViewTimeSpan() {

    setLayout(GUI_Utils.gridBagLayout);
    setTitle(this.TIME_SPAN_PARAM_NAME);
    // start time
    JPanel startTimePanel = new TitledBorderPanel(this.START_TIME_PARAM_NAME);
    startTimePanel.setLayout(GUI_Utils.gridBagLayout);
    startTimePanel.add(startTimeLabel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

    //end time
    JPanel endTimePanel = new TitledBorderPanel(END_TIME_PARAM_NAME);
    endTimePanel.setLayout(GUI_Utils.gridBagLayout);
    endTimePanel.add(endTimeLabel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

    // dating techniques
    try {
      // dated feature comments
      datedFeatureCommentsParamEditor = new CommentsParameterEditor(datedFeatureCommentsParam);
      datedFeatureCommentsParamEditor.setEnabled(false);
    }catch(Exception e) {
      e.printStackTrace();
    }

    JPanel referencesPanel = GUI_Utils.getPanel(referencesLabel, "References");

    // add start time, end time and comments to the GUI
    int yPos = 0;
    add(this.editTimeSpanButton,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0
        ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    add(startTimePanel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(endTimePanel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(datedFeatureCommentsParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(referencesPanel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    editTimeSpanButton.addActionListener(this);
  }

  /**
   * Set the info about the start and end time and dating comments based on selected
   * site and timespan
   *
   * @param startTime
   * @param endTime
   * @param datingComments
   */
  public void setTimeSpan(TimeAPI startTime, TimeAPI endTime, String datingComments,
                          ArrayList references) {
    this.startTimeLabel.setTextAsHTML(startTime);
    this.endTimeLabel.setTextAsHTML(endTime);
    this.referencesLabel.setTextAsHTML(references);
    this.datedFeatureCommentsParam.setValue(datingComments);
    this.datedFeatureCommentsParamEditor.refreshParamEditor();
  }

  /**
 * This function is called when edit button is clicked
 * @param event
 */
public void actionPerformed(ActionEvent event) {
  JFrame frame= new JFrame(EDIT_TITLE);
  AddEditTimeSpan addEditTimespan =  new AddEditTimeSpan();
  Container contentPane = frame.getContentPane();
  contentPane.setLayout(GUI_Utils.gridBagLayout);
  contentPane.add(addEditTimespan, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
      , GridBagConstraints.CENTER,
      GridBagConstraints.BOTH,
      new Insets(0, 0, 0, 0), 0, 0));
  frame.pack();
  frame.setSize(500,500);
  frame.show();
}


}