package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import java.util.ArrayList;
import org.opensha.param.editor.ParameterListEditor;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.gui.LabeledBoxPanel;
import org.opensha.param.editor.ConstrainedStringListParameterEditor;
import org.opensha.param.editor.estimate.ConstrainedEstimateParameterEditor;
import org.opensha.refFaultParamDb.gui.CommentsParameterEditor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.data.estimate.Estimate;


/**
 * <p>Title: AddEditSlipRateForTimePeriod.java </p>
 * <p>Description: This Panel allows user to edit/add slip rate information
 * for a time span. User can enter slip rate,asisimic slip factor, comments
 * and references. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditSlipRate extends LabeledBoxPanel  {
  // SLIP RATE
  private final static String SLIP_RATE_PARAM_NAME="Slip Rate Estimate";
  private final static String SLIP_RATE="Slip Rate";
  private final static String SLIP_RATE_COMMENTS_PARAM_NAME="Slip Rate Comments";
  private final static String SLIP_RATE_REFERENCES_PARAM_NAME="Choose References";
  private final static String SLIP_RATE_UNITS = "mm/yr";
  private final static double SLIP_RATE_MIN = 0;
  private final static double SLIP_RATE_MAX = Double.POSITIVE_INFINITY;

  // ASEISMIC SLIP FACTOR
  private final static String ASEISMIC_SLIP_FACTOR_PARAM_NAME="Aseismic Slip Factor Estimate(0-1, 1=all aseismic)";
  private final static String ASEISMIC_SLIP_FACTOR="Aseismic Slip Factor";
  private final static double ASEISMIC_SLIP_FACTOR_MIN=0;
  private final static double ASEISMIC_SLIP_FACTOR_MAX=1;
  private final static String ASEISMIC_SLIP_FACTOR_UNITS = " ";

  // parameters
  private EstimateParameter slipRateEstimateParam;
  private EstimateParameter aSeismicSlipFactorParam;
  private StringParameter slipRateCommentsParam;

  // parameter editors
  private ConstrainedEstimateParameterEditor slipRateEstimateParamEditor;
  private ConstrainedEstimateParameterEditor aSeismicSlipFactorParamEditor;
  private CommentsParameterEditor slipRateCommentsParamEditor;

  private final static String SLIP_RATE_PARAMS_TITLE = "Slip Rate Params";


  public AddEditSlipRate() {
    try {
      this.setLayout(GUI_Utils.gridBagLayout);
      this.addSlipRateInfoParameters();
      this.setMinimumSize(new Dimension(0, 0));
    }catch(Exception e) {
      e.printStackTrace();
    }

  }



  /**
   * Add the input parameters if the user provides the slip rate info
   */
  private void addSlipRateInfoParameters() throws Exception {
    // slip rate estimate
   ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
   this.slipRateEstimateParam = new EstimateParameter(this.SLIP_RATE_PARAM_NAME,
        SLIP_RATE_UNITS, SLIP_RATE_MIN, SLIP_RATE_MAX, allowedEstimates);
    slipRateEstimateParamEditor = new ConstrainedEstimateParameterEditor(slipRateEstimateParam, true, true, SLIP_RATE);
    //aseismic slip factor
    this.aSeismicSlipFactorParam = new EstimateParameter(this.ASEISMIC_SLIP_FACTOR_PARAM_NAME,
        ASEISMIC_SLIP_FACTOR_MIN, ASEISMIC_SLIP_FACTOR_MAX, allowedEstimates);
    aSeismicSlipFactorParamEditor = new ConstrainedEstimateParameterEditor(aSeismicSlipFactorParam, true, true, ASEISMIC_SLIP_FACTOR);
    // slip rate comments
    slipRateCommentsParam = new StringParameter(this.SLIP_RATE_COMMENTS_PARAM_NAME);
    slipRateCommentsParamEditor = new CommentsParameterEditor(slipRateCommentsParam);

    int yPos=0;
    this.add(slipRateEstimateParamEditor,
             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(aSeismicSlipFactorParamEditor,
             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

    this.add(slipRateCommentsParamEditor,
             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));


    setTitle(this.SLIP_RATE_PARAMS_TITLE);
   }


   /**
    * Get the slip rate estimate along with units
    * @return
    */
   public EstimateInstances getSlipRateEstimate() {
     this.slipRateEstimateParamEditor.setEstimateInParameter();
     return new EstimateInstances((Estimate)this.slipRateEstimateParam.getValue(),
                                  this.SLIP_RATE_UNITS);
   }

   /**
    * Get aseismic slip factor estimate along with units
    * @return
    */
   public EstimateInstances getAseismicEstimate() {
     this.aSeismicSlipFactorParamEditor.setEstimateInParameter();
     return new EstimateInstances((Estimate)this.aSeismicSlipFactorParam.getValue(),
                                 ASEISMIC_SLIP_FACTOR_UNITS);
   }

   /**
    * Return the slip rate comments
    * @return
    */
   public String getSlipRateComments() {
     return (String)this.slipRateCommentsParam.getValue();
   }

}