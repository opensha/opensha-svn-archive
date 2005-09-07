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

public class ViewSlipRate extends LabeledBoxPanel implements ActionListener {
  private final static String SLIP_RATE_TITLE = "Slip Rate";
  // edit buttons
  private JButton editSlipRateButton = new JButton("Edit");
  private final static String EDIT_TITLE = "Edit Slip Rate";

  // various labels to provide the information
  private InfoLabel slipRateEstimateLabel = new InfoLabel();
  private InfoLabel aSesimicSlipFactorLabel = new InfoLabel();
  private InfoLabel referencesLabel = new InfoLabel();
  private StringParameter commentsParam = new StringParameter("Slip Rate Comments");
  private CommentsParameterEditor commentsParameterEditor;

  public ViewSlipRate() {
    super(GUI_Utils.gridBagLayout);
    try {
      viewSlipRateForTimePeriod();
      setTitle(this.SLIP_RATE_TITLE);
      editSlipRateButton.addActionListener(this);
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
                      String comments, ArrayList references) {
    slipRateEstimateLabel.setTextAsHTML(slipRateEstimate);
    aSesimicSlipFactorLabel.setTextAsHTML(aSeismicSlipFactorEstimate);
    referencesLabel.setTextAsHTML(references);
    commentsParam.setValue(comments);
    commentsParameterEditor.refreshParamEditor();
  }

  /**
   * display the slip Rate info for the selected time period
   */
  private void viewSlipRateForTimePeriod() throws Exception {

    JPanel slipRateEstimatePanel = GUI_Utils.getPanel(slipRateEstimateLabel,
                                            "Slip Rate Estimate(mm/yr)");
    JPanel aseismicPanel = GUI_Utils.getPanel(aSesimicSlipFactorLabel,
                                    "Aseismic Slip Factor(0-1, 1=all aseismic)");
    commentsParameterEditor = new CommentsParameterEditor(commentsParam);
    commentsParameterEditor.setEnabled(false);
    JPanel referencesPanel = GUI_Utils.getPanel(referencesLabel, "References");

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
    add(commentsParameterEditor, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));
    add(referencesPanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(0, 0, 0, 0), 0, 0));

  }

  /**
   * This function is called when edit button is clicked
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    JFrame frame= new JFrame(EDIT_TITLE);
    AddEditSlipRate addEditSlipRate =  new AddEditSlipRate();
    Container contentPane = frame.getContentPane();
    contentPane.setLayout(GUI_Utils.gridBagLayout);
    contentPane.add(addEditSlipRate, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));
    frame.setSize(300,750);
    frame.show();
  }

}