package org.opensha.refFaultParamDb.gui.view;

import org.opensha.gui.LabeledBoxPanel;
import java.awt.LayoutManager;
import javax.swing.JButton;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditCumDisplacement;
import org.opensha.param.StringParameter;
import org.opensha.refFaultParamDb.gui.CommentsParameterEditor;
import org.opensha.data.estimate.Estimate;
import javax.swing.JPanel;
import org.opensha.refFaultParamDb.vo.CombinedDisplacementInfo;

/**
 * <p>Title: ViewCumDisplacement.java </p>
 * <p>Description: View cumulative displacement for a site for a time period </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewCumDisplacement extends LabeledBoxPanel  {
  private final static String DISPLACEMENT_TITLE = "Displacement";
  private final static String DISPLACEMENT_PANEL_TITLE = "Displacement Estimate(m)";
  private final String ASEISMIC_SLIP_PANEL_TITLE = "Aseismic Slip Factor(0-1, 1=all aseismic)";
  private final static String MEASURED_COMP_SLIP_TITLE = "Measured Component of Slip";
  private final static String SENSE_OF_MOTION_TITLE="Sense of Motion";
  private final static String DISPLACEMENT = "Displacement";
  private final static String ASEISMIC_SLIP_FACTOR = "Aseismic Slip Factor";
  private final static String PROB = "Prob this is correct value";
  private final static String RAKE = "Rake";
  private final static String QUALITATIVE = "Qualitative";

// various labels to provide the information
  private InfoLabel displacementEstimateLabel = new InfoLabel();
  private InfoLabel aSesimicSlipFactorLabel = new InfoLabel();
  private InfoLabel senseOfMotionRakeLabel = new InfoLabel();
  private InfoLabel senseOfMotionQualLabel = new InfoLabel();
  private InfoLabel measuredCompQualLabel = new InfoLabel();
  private StringParameter commentsParam = new StringParameter("Displacement Comments");
  private CommentsParameterEditor commentsParameterEditor;

  public ViewCumDisplacement() {
    super(GUI_Utils.gridBagLayout);
    try {
      viewDisplacementForTimePeriod();
      setTitle(this.DISPLACEMENT_TITLE);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Show info about cumulative displacement
   *
   * @param combinedDisplacementInfo
   */
  public void setInfo(CombinedDisplacementInfo combinedDisplacementInfo) {
    if(combinedDisplacementInfo ==null) setInfo(null, null, null, Double.NaN, null, null);
    else setInfo(combinedDisplacementInfo.getDisplacementEstimate().getEstimate(),
                                 combinedDisplacementInfo.getASeismicSlipFactorEstimateForDisp().getEstimate(),
                                 combinedDisplacementInfo.getDisplacementComments(),
                                 combinedDisplacementInfo.getSenseOfMotionRake(),
                                 combinedDisplacementInfo.getSenseOfMotionQual(),
                                 combinedDisplacementInfo.getMeasuredComponentQual()
                                 );
  }
  /**
   * Set the info about the displacement, aseismic slip factor, comments and references
   *
   * @param displacementEstimate
   * @param aSeismicSlipFactorEstimate
   * @param comments
   * @param references
   */
  private void setInfo(Estimate displacementEstimate, Estimate aSeismicSlipFactorEstimate,
                      String comments, double rakeForSenseOfMotion, String senseOfMotionQual,
                      String measuredSlipQual) {
    displacementEstimateLabel.setTextAsHTML(displacementEstimate, DISPLACEMENT, PROB);
    aSesimicSlipFactorLabel.setTextAsHTML(aSeismicSlipFactorEstimate, this.ASEISMIC_SLIP_FACTOR, PROB);
    commentsParam.setValue(comments);
    commentsParameterEditor.refreshParamEditor();
    this.measuredCompQualLabel.setTextAsHTML(this.QUALITATIVE, measuredSlipQual);
// check whether sense of motion is available
    if(Double.isNaN(rakeForSenseOfMotion))
      senseOfMotionRakeLabel.setTextAsHTML(this.RAKE, null);
    else this.senseOfMotionRakeLabel.setTextAsHTML(this.RAKE,  GUI_Utils.decimalFormat.format(rakeForSenseOfMotion));
    this.senseOfMotionQualLabel.setTextAsHTML(this.QUALITATIVE, senseOfMotionQual);
  }

  /**
   * display the slip Rate info for the selected time period
   */
  private void viewDisplacementForTimePeriod() throws Exception {

    JPanel displacementEstimatePanel = GUI_Utils.getPanel(displacementEstimateLabel,
                                            DISPLACEMENT_PANEL_TITLE);
    JPanel aseismicPanel = GUI_Utils.getPanel(aSesimicSlipFactorLabel,
                                    ASEISMIC_SLIP_PANEL_TITLE);
    JPanel senseOfMotionPanel = GUI_Utils.getPanel(this.SENSE_OF_MOTION_TITLE);
    JPanel measuredSlipCompPanel = GUI_Utils.getPanel(this.MEASURED_COMP_SLIP_TITLE);

    // sense of motion panel
    senseOfMotionPanel.add(this.senseOfMotionRakeLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));
    senseOfMotionPanel.add(this.senseOfMotionQualLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));
    // measured component of slip panel
    measuredSlipCompPanel.add(this.measuredCompQualLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));

    commentsParameterEditor = new CommentsParameterEditor(commentsParam);
    commentsParameterEditor.setEnabled(false);

    // add the displacement info the panel
    int yPos = 0;
    add(displacementEstimatePanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));
    add(measuredSlipCompPanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));
    add(senseOfMotionPanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
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