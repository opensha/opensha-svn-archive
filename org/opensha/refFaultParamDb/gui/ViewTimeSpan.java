package org.opensha.refFaultParamDb.gui;

import org.opensha.gui.LabeledBoxPanel;
import org.opensha.refFaultParamDb.data.TimeAPI;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.param.StringParameter;
import org.opensha.gui.TitledBorderPanel;
import javax.swing.JPanel;

/**
 * <p>Title: ViewTimeSpan.java </p>
 * <p>Description: This class can be used for viewing timespan for site.It will contain
 * start/end time and dated feature comments</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewTimeSpan extends LabeledBoxPanel {

  // start time header
  private final static String START_TIME_PARAM_NAME="Start Time";
  // end time header
  private final static String END_TIME_PARAM_NAME="End Time";
  // timespan header
  private final static String TIME_SPAN_PARAM_NAME="TimeSpan";
  // dated feature comments
  private final static String DATED_FEATURE_COMMENTS_PARAM_NAME="Description of Timespan";
  // dating comments params
  StringParameter datedFeatureCommentsParam;
  CommentsParameterEditor datedFeatureCommentsParamEditor;

  /**
   *
   * @param startTime Start Time
   * @param endTime End Time
   * @param datingComments - dating feature comments
   */
  public ViewTimeSpan(TimeAPI startTime, TimeAPI endTime, String datingComments) {

    setLayout(new GridBagLayout());
    setTitle(this.TIME_SPAN_PARAM_NAME);
    // start time
    JPanel startTimePanel = new TitledBorderPanel(this.START_TIME_PARAM_NAME);
    startTimePanel.setLayout(new GridBagLayout());
    startTimePanel.add(new InfoLabel(startTime),  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

    //end time
    JPanel endTimePanel = new TitledBorderPanel(END_TIME_PARAM_NAME);
    endTimePanel.setLayout(new GridBagLayout());
    endTimePanel.add(new InfoLabel(endTime),  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

    // dating techniques
    try {
      // dated feature comments
      datedFeatureCommentsParam = new StringParameter(this.DATED_FEATURE_COMMENTS_PARAM_NAME, datingComments);
      datedFeatureCommentsParamEditor = new CommentsParameterEditor(datedFeatureCommentsParam);
      datedFeatureCommentsParamEditor.setEnabled(false);
    }catch(Exception e) {
      e.printStackTrace();
    }

    // add start time, end time and comments to the GUI
    int yPos = 0;
    add(startTimePanel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(endTimePanel,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(datedFeatureCommentsParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

  }

}