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

public class ViewTimeSpan extends LabeledBoxPanel{

  // start time header
  private final static String START_TIME_PARAM_NAME="Start Time:";
  // end time header
  private final static String END_TIME_PARAM_NAME="End Time:";
  // timespan header
  private final static String TIME_SPAN_PARAM_NAME="TimeSpan";
  // dated feature comments
  private final static String DATED_FEATURE_COMMENTS_PARAM_NAME="Dating Methodology";
  // entry date
  private final static String ENTRY_DATE_PARAM_NAME="Entry Date";
  //contribbutor
  private final static String CONTRIBUTOR_PARAM_NAME="Contributor";
  private final static String REFERENCES_PANEL_TITLE = "References";
  // dating comments params
  private StringParameter datedFeatureCommentsParam = new StringParameter(this.DATED_FEATURE_COMMENTS_PARAM_NAME);
  private CommentsParameterEditor datedFeatureCommentsParamEditor;

  private InfoLabel startTimeLabel = new InfoLabel();
  private InfoLabel endTimeLabel = new InfoLabel();
  private InfoLabel referencesLabel = new InfoLabel();
  private InfoLabel entryDateLabel = new InfoLabel();
  private InfoLabel contributorLabel = new InfoLabel();

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


    JPanel referencesPanel = GUI_Utils.getPanel(referencesLabel, REFERENCES_PANEL_TITLE);

    // add start time, end time and comments to the GUI
    int yPos = 0;
    add(this.entryDateLabel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(this.contributorLabel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(startTimePanel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(endTimePanel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(datedFeatureCommentsParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(referencesPanel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
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
                          ArrayList references, String entryDate, String contributorName) {
    this.startTimeLabel.setTextAsHTML(startTime);
    this.endTimeLabel.setTextAsHTML(endTime);
    this.referencesLabel.setTextAsHTML(references);
    this.contributorLabel.setTextAsHTML(this.CONTRIBUTOR_PARAM_NAME, contributorName);
    this.entryDateLabel.setTextAsHTML(this.ENTRY_DATE_PARAM_NAME, entryDate);
    this.datedFeatureCommentsParam.setValue(datingComments);
    this.datedFeatureCommentsParamEditor.refreshParamEditor();
  }

}