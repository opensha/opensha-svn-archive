package org.opensha.refFaultParamDb.gui.view;

import org.opensha.gui.LabeledBoxPanel;
import java.awt.LayoutManager;
import java.awt.*;
import javax.swing.JButton;
import org.opensha.data.estimate.LogNormalEstimate;
import javax.swing.JPanel;
import org.opensha.data.estimate.NormalEstimate;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.param.StringParameter;
import org.opensha.refFaultParamDb.gui.CommentsParameterEditor;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;

/**
 * <p>Title: ViewSlipRate.java </p>
 * <p>Description: View Slip Rate Information for a  site for a specific time period </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewSlipRate extends LabeledBoxPanel {
  private final static String SLIP_RATE_TITLE = "Slip Rate";
  // edit buttons
  private JButton editSlipRateButton = new JButton("Edit");

  public ViewSlipRate() {
    super(GUI_Utils.gridBagLayout);
    try {
      viewSlipRateForTimePeriod();
      setTitle(this.SLIP_RATE_TITLE);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * display the slip Rate info for the selected time period
   */
  private void viewSlipRateForTimePeriod() throws Exception {

    // Slip Rate Estimate
    LogNormalEstimate slipRateEstimate = new LogNormalEstimate(1.5, 0.25);
    JPanel slipRateEstimatePanel = GUI_Utils.getPanel(new InfoLabel(slipRateEstimate),
                                            "Slip Rate Estimate(mm/yr)");

    // Aseismic slip rate estimate
    NormalEstimate aSiemsicSlipEstimate = new NormalEstimate(0.7, 0.5);
    JPanel aseismicPanel = GUI_Utils.getPanel(new InfoLabel(aSiemsicSlipEstimate),
                                    "Aseismic Slip Factor(0-1, 1=all aseismic)");

    // comments
    String comments = "Perinent comments will be displayed here";
    StringParameter commentsParam = new StringParameter("Slip Rate Comments",
        comments);
    CommentsParameterEditor commentsPanel = new CommentsParameterEditor(
        commentsParam);
    commentsPanel.setEnabled(false);

    // references
    ArrayList references = new ArrayList();
    references.add("Ref 1");
    references.add("Ref 2");
    JPanel referencesPanel = GUI_Utils.getPanel(new InfoLabel(references), "References");

    // add the slip rate info the panel
    int yPos = 0;
    add(this.editSlipRateButton,
        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0
                               , GridBagConstraints.EAST,
                               GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
                               0, 0));
    add(slipRateEstimatePanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));
    add(aseismicPanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));
    add(commentsPanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));
    add(referencesPanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(0, 0, 0, 0), 0, 0));

  }

}