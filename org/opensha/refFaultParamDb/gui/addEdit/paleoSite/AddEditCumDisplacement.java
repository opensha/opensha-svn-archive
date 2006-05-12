package org.opensha.refFaultParamDb.gui.addEdit.paleoSite;

import javax.swing.*;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import java.util.ArrayList;
import org.opensha.param.editor.ParameterListEditor;
import java.awt.*;

import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.param.editor.estimate.ConstrainedEstimateParameterEditor;
import org.opensha.param.editor.ConstrainedStringListParameterEditor;
import org.opensha.refFaultParamDb.gui.CommentsParameterEditor;
import org.opensha.gui.LabeledBoxPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.data.estimate.Estimate;
import org.opensha.refFaultParamDb.vo.CombinedDisplacementInfo;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeEvent;

/**
 * <p>Title: AddEditCumDisplacement.java </p>
 * <p>Description: This panel allows the user to enter cumulative displacement
 * information for a time period. The information entered is cum displacmeent estimate,
 * aseismic slip factor estimate, references and comments</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditCumDisplacement extends LabeledBoxPanel implements ParameterChangeListener {
  // whether  Aseismic Slip Factor is Known/Unknown
  private final static String ASEISMIC_AVAILABLE_PARAM_NAME="Asiesmic Slip Factor";
  private final static String KNOWN = "Known";
  private final static String UNKNOWN = "Unknown";

  // ASEISMIC SLIP FACTOR
  private final static String ASEISMIC_SLIP_FACTOR_PARAM_NAME="Aseismic Slip Factor Estimate(0-1, 1=all aseismic)";
  private final static String ASEISMIC_SLIP_FACTOR_="Aseismic Slip Factor";
  private final static double ASEISMIC_SLIP_FACTOR_MIN=0;
  private final static double ASEISMIC_SLIP_FACTOR_MAX=1;
  public final static String ASEISMIC_SLIP_FACTOR_UNITS=" ";

   // CUMULATIVE DISPLACEMENT
  private final static String CUMULATIVE_DISPLACEMENT_PARAM_NAME="Cumulative Displacement Estimate";
  private final static String CUMULATIVE_DISPLACEMENT="Cumulative Disp";
  private final static String CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME="Cumulative Displacement Comments";
  public final static String CUMULATIVE_DISPLACEMENT_UNITS = "m";
  private final static double CUMULATIVE_DISPLACEMENT_MIN = 0;
  private final static double CUMULATIVE_DISPLACEMENT_MAX = Double.POSITIVE_INFINITY;

  // various parameters
  private EstimateParameter aSeismicSlipFactorParam;
  private EstimateParameter cumDisplacementParam;
  private StringParameter displacementCommentsParam;
  private StringParameter aseismicAvailableParam;

  // parameter editors
  private ConstrainedEstimateParameterEditor aSeismicSlipFactorParamEditor;
  private ConstrainedEstimateParameterEditor cumDisplacementParamEditor;
  private ConstrainedStringParameterEditor aseismicAvailableParamEditor;
  private CommentsParameterEditor displacementCommentsParamEditor;
  private SenseOfMotionPanel senseOfMotionPanel;
  private MeasuredCompPanel measuredCompPanel;

  // various buttons in this window
  private final static String CUM_DISPLACEMENT_PARAMS_TITLE = "Cumulative Displacement Params";

  /**
   * Add Cum displacement parameters
   */
  public AddEditCumDisplacement() {
   try {
     senseOfMotionPanel = new SenseOfMotionPanel();
     measuredCompPanel = new MeasuredCompPanel();
     setLayout(GUI_Utils.gridBagLayout);
     addCumulativeDisplacementParameters();
     setAseismicEditorVisibility();
     this.setMinimumSize(new Dimension(0, 0));

   }catch(Exception e) {
     e.printStackTrace();
   }
  }

  /**
   * Set the values in the editor
   * @param combinedDisplacementInfo
   */
  public AddEditCumDisplacement(CombinedDisplacementInfo combinedDisplacementInfo) {
    this();
    if(combinedDisplacementInfo!=null)
      setValuesInParameters(combinedDisplacementInfo);
  }


  private void setValuesInParameters(CombinedDisplacementInfo combinedDisplacementInfo) {
    // set cumulative displacement
    cumDisplacementParam.setValue(combinedDisplacementInfo.getDisplacementEstimate().getEstimate());
    cumDisplacementParamEditor.refreshParamEditor();

    // set aseismic slip factor
    EstimateInstances aseismicSlipFactor  = combinedDisplacementInfo.getASeismicSlipFactorEstimateForDisp();
    if(aseismicSlipFactor==null) { // if aseismic slip factor is known
      aseismicAvailableParam.setValue(this.UNKNOWN);
    } else { // aseismic slip factor is known
      aseismicAvailableParam.setValue(this.KNOWN);
      aSeismicSlipFactorParam.setValue(aseismicSlipFactor.getEstimate());
      aSeismicSlipFactorParamEditor.refreshParamEditor();
    }
    aseismicAvailableParamEditor.refreshParamEditor();

    // set comments
    this.displacementCommentsParam.setValue(combinedDisplacementInfo.getDisplacementComments());
    displacementCommentsParamEditor.refreshParamEditor();

    // measured component of slip
    measuredCompPanel.setMeasuredCompVal(combinedDisplacementInfo.getMeasuredComponentQual());

    // sense of motion
    EstimateInstances somQuan = combinedDisplacementInfo.getSenseOfMotionRake();
    Estimate rake = null;
    if(somQuan!=null) rake = somQuan.getEstimate();
    senseOfMotionPanel.setSenseOfMotion(combinedDisplacementInfo.getSenseOfMotionQual(),
                                        rake);

  }


  /**
   * Add the input parameters if user provides the cumulative displacement
   */
  private void addCumulativeDisplacementParameters() throws Exception {

    // cumulative displacement estimate
    ArrayList allowedEstimates = EstimateConstraint.
        createConstraintForPositiveDoubleValues();
    this.cumDisplacementParam = new EstimateParameter(this.
        CUMULATIVE_DISPLACEMENT_PARAM_NAME,
        CUMULATIVE_DISPLACEMENT_UNITS, CUMULATIVE_DISPLACEMENT_MIN,
        CUMULATIVE_DISPLACEMENT_MAX, allowedEstimates);
    cumDisplacementParamEditor = new ConstrainedEstimateParameterEditor(cumDisplacementParam,true);
    // whether aseismic slip is available or not
    ArrayList allowedVals = new ArrayList();
    allowedVals.add(this.KNOWN);
    allowedVals.add(this.UNKNOWN);
    aseismicAvailableParam = new StringParameter(ASEISMIC_AVAILABLE_PARAM_NAME, allowedVals,
                                                 (String)allowedVals.get(0));
    aseismicAvailableParam.addParameterChangeListener(this);
    aseismicAvailableParamEditor = new ConstrainedStringParameterEditor(aseismicAvailableParam);
    //aseismic slip factor
    this.aSeismicSlipFactorParam = new EstimateParameter(this.ASEISMIC_SLIP_FACTOR_PARAM_NAME,
        ASEISMIC_SLIP_FACTOR_UNITS, ASEISMIC_SLIP_FACTOR_MIN, ASEISMIC_SLIP_FACTOR_MAX, allowedEstimates);
    aSeismicSlipFactorParamEditor = new ConstrainedEstimateParameterEditor(aSeismicSlipFactorParam, true);
    // comments parameter editor
    displacementCommentsParam = new StringParameter(this.
        CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME);
    displacementCommentsParamEditor = new CommentsParameterEditor(displacementCommentsParam);


    int yPos=0;
    this.add(this.measuredCompPanel,
             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

    this.add(cumDisplacementParamEditor,
             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(aseismicAvailableParamEditor,
             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

    this.add(aSeismicSlipFactorParamEditor,
             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));


    this.add(senseOfMotionPanel,
             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

    this.add(displacementCommentsParamEditor,
             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));


    setTitle(this.CUM_DISPLACEMENT_PARAMS_TITLE);
  }

 public CombinedDisplacementInfo getCombinedDisplacementInfo() {
   CombinedDisplacementInfo combinedDisplacementInfo = new CombinedDisplacementInfo();
   combinedDisplacementInfo.setDisplacementComments(getDisplacementComments());
   combinedDisplacementInfo.setASeismicSlipFactorEstimateForDisp(getAseismicEstimate());
   combinedDisplacementInfo.setDisplacementEstimate(getDisplacementEstimate());
   combinedDisplacementInfo.setMeasuredComponentQual(this.measuredCompPanel.getMeasuredComp());
   combinedDisplacementInfo.setSenseOfMotionRake(this.senseOfMotionPanel.getSenseOfMotionRake());
   combinedDisplacementInfo.setSenseOfMotionQual(this.senseOfMotionPanel.getSenseOfMotionQual());
   return combinedDisplacementInfo;
 }


 public void parameterChange(ParameterChangeEvent event) {
   if(event.getParameterName().equalsIgnoreCase(this.ASEISMIC_AVAILABLE_PARAM_NAME))
     setAseismicEditorVisibility();
 }

 /**
  * Show/Hide the aseismic slip factor editor
  */
 private void setAseismicEditorVisibility() {
   String aseismicSlipFactorAvailability = (String)aseismicAvailableParam.getValue();
   if(aseismicSlipFactorAvailability.equalsIgnoreCase(this.KNOWN))
     this.aSeismicSlipFactorParamEditor.setVisible(true);
   else this.aSeismicSlipFactorParamEditor.setVisible(false);
 }

  /**
   * Get the displacement estimate
   * @return
   */
  private EstimateInstances getDisplacementEstimate() {
    this.cumDisplacementParamEditor.setEstimateInParameter();
    return new EstimateInstances((Estimate)cumDisplacementParam.getValue(),
                                 CUMULATIVE_DISPLACEMENT_UNITS);

  }

  /**
   * Get aseismic slip factor estimate
   * @return
   */
  private EstimateInstances getAseismicEstimate() {
    String aseismicSlipFactorAvailability = (String)aseismicAvailableParam.getValue();
    if(aseismicSlipFactorAvailability.equalsIgnoreCase(this.UNKNOWN)) return null;
    this.aSeismicSlipFactorParamEditor.setEstimateInParameter();
    return new EstimateInstances((Estimate)this.aSeismicSlipFactorParam.getValue(),
                                 ASEISMIC_SLIP_FACTOR_UNITS);
  }

  /**
   * Get the displacement comments
   * @return
   */
  private String getDisplacementComments() {
    return (String)this.displacementCommentsParam.getValue();
  }

}