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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditSlipRate;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.opensha.data.estimate.Estimate;

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
  private final static String SLIP_RATE_PANEL_TITLE = "Slip Rate Estimate(mm/yr)";
  private final static String ASEISMIC_PANEL_TITLE = "Aseismic Slip Factor(0-1, 1=all aseismic)";

  private final static String SLIP_RATE = "Slip Rate";
  private final static String ASEISMIC_SLIP_FACTOR = "Aseismic Slip Factor";
  private final static String PROB = "Probability";

  // various labels to provide the information
  private InfoLabel slipRateEstimateLabel = new InfoLabel();
  private InfoLabel aSesimicSlipFactorLabel = new InfoLabel();
  private StringParameter commentsParam = new StringParameter("Slip Rate Comments");
  private CommentsParameterEditor commentsParameterEditor;

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
   * Set the info about the slip rate, aseismic slip factor, comments and references
   *
   * @param slipRateEstimate
   * @param aSeismicSlipFactorEstimate
   * @param comments
   * @param references
   */
  public void setInfo(Estimate slipRateEstimate, Estimate aSeismicSlipFactorEstimate,
                      String comments) {
    slipRateEstimateLabel.setTextAsHTML(slipRateEstimate, SLIP_RATE, PROB);
    aSesimicSlipFactorLabel.setTextAsHTML(aSeismicSlipFactorEstimate, this.ASEISMIC_SLIP_FACTOR, PROB);
    commentsParam.setValue(comments);
    commentsParameterEditor.refreshParamEditor();
  }

  /**
   * display the slip Rate info for the selected time period
   */
  private void viewSlipRateForTimePeriod() throws Exception {
    JPanel slipRateEstimatePanel = GUI_Utils.getPanel(slipRateEstimateLabel,
                                            SLIP_RATE_PANEL_TITLE);

    JPanel aseismicPanel = GUI_Utils.getPanel(aSesimicSlipFactorLabel,
                                    ASEISMIC_PANEL_TITLE);
    commentsParameterEditor = new CommentsParameterEditor(commentsParam);
    commentsParameterEditor.setEnabled(false);

    // add the slip rate info the panel
    int yPos = 0;
    add(slipRateEstimatePanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));
    add(aseismicPanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));
    add(commentsParameterEditor, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));
  }
}