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
  // various labels to provide the information
  private InfoLabel displacementEstimateLabel = new InfoLabel();
  private InfoLabel aSesimicSlipFactorLabel = new InfoLabel();
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
   * Set the info about the displacement, aseismic slip factor, comments and references
   *
   * @param displacementEstimate
   * @param aSeismicSlipFactorEstimate
   * @param comments
   * @param references
   */
  public void setInfo(Estimate displacementEstimate, Estimate aSeismicSlipFactorEstimate,
                      String comments) {
    displacementEstimateLabel.setTextAsHTML(displacementEstimate);
    aSesimicSlipFactorLabel.setTextAsHTML(aSeismicSlipFactorEstimate);
    commentsParam.setValue(comments);
    commentsParameterEditor.refreshParamEditor();
  }

  /**
   * display the slip Rate info for the selected time period
   */
  private void viewDisplacementForTimePeriod() throws Exception {

    JPanel displacementEstimatePanel = GUI_Utils.getPanel(displacementEstimateLabel,
                                            "Displacement Estimate(m)");
    JPanel aseismicPanel = GUI_Utils.getPanel(aSesimicSlipFactorLabel,
                                    "Aseismic Slip Factor(0-1, 1=all aseismic)");
    commentsParameterEditor = new CommentsParameterEditor(commentsParam);
    commentsParameterEditor.setEnabled(false);

    // add the displacement info the panel
    int yPos = 0;
    add(displacementEstimatePanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
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