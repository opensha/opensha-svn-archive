package org.opensha.refFaultParamDb.gui;

import java.awt.*;
import javax.swing.*;
import org.opensha.refFaultParamDb.data.TimeEstimate;
import org.opensha.gui.LabeledBoxPanel;
import org.opensha.refFaultParamDb.data.ExactTime;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.data.estimate.NormalEstimate;

/**
 * <p>Title: AddEditSiteInfoForTimePeriod</p>
 *
 * <p>Description: This class allows the user to edit the Site info. for a given
 * time period.</p>
 * @author Vipin Gupta
 * @since Sept 01,2005
 * @version 1.0
 */
public class AddEditSiteInfoForTimePeriod
    extends JFrame {

  private JSplitPane siteInfoSplitPane = new JSplitPane();
  private BorderLayout borderLayout1 = new BorderLayout();

  public AddEditSiteInfoForTimePeriod() {
    try {
      jbInit();
      addTimeSpanInfo();// add start and end time estimates
      addSiteInfoForTimePeriod();// add the info for the selected time period
      siteInfoSplitPane.setDividerLocation(225);
      this.pack();
      this.setVisible(true);
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    siteInfoSplitPane.setPreferredSize(new Dimension(590, 550));
    this.getContentPane().add(siteInfoSplitPane, java.awt.BorderLayout.CENTER);
    siteInfoSplitPane.setDividerLocation(210);
  }

  /**
   * Add the start and end time estimate parameters
   */
  private void addTimeSpanInfo() {
    ExactTime startTime = new ExactTime(246, 1, 15, 10, 56, 21, TimeAPI.BC);
    TimeEstimate endTime =  new TimeEstimate();
    endTime.setForKaUnits(new NormalEstimate(1000, 50), 1950);
    String comments = "Dating features comments and techniques will go here";
    // timeSpan panel which will conatin start time and end time
    LabeledBoxPanel timeSpanPanel = new ViewTimeSpan(startTime, endTime, comments);
    siteInfoSplitPane.add(timeSpanPanel, JSplitPane.LEFT);
  }

  /**
   * display the info for the selected time period
   */
  private void addSiteInfoForTimePeriod() {
    SiteInfoForTimePeriod siteInfoForTimePeriod = new SiteInfoForTimePeriod();
    siteInfoSplitPane.add(siteInfoForTimePeriod, JSplitPane.RIGHT);
  }


}
