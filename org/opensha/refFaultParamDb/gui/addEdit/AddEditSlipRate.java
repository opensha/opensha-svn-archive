package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import java.util.ArrayList;
import org.opensha.param.editor.ParameterListEditor;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;


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

public class AddEditSlipRate extends JPanel{
  // SLIP RATE
  private final static String SLIP_RATE_PARAM_NAME="Slip Rate Estimate";
  private final static String SLIP_RATE_COMMENTS_PARAM_NAME="Slip Rate Comments";
  private final static String SLIP_RATE_REFERENCES_PARAM_NAME="Slip Rate References";
  private final static String SLIP_RATE_UNITS = "mm/yr";
  private final static double SLIP_RATE_MIN = 0;
  private final static double SLIP_RATE_MAX = Double.POSITIVE_INFINITY;

  // ASEISMIC SLIP FACTOR
  private final static String ASEISMIC_SLIP_FACTOR_PARAM_NAME="Aseismic Slip Factor Estimate";
  private final static double ASEISMIC_SLIP_FACTOR_MIN=0;
  private final static double ASEISMIC_SLIP_FACTOR_MAX=1;

  private StringListParameter slipRateReferencesParam;
 private EstimateParameter slipRateEstimateParam;
 private EstimateParameter aSeismicSlipFactorParam;
 private StringParameter slipRateCommentsParam;

 // parameter List editor
 private ParameterListEditor slipRateParameterListEditor;


// various buttons in this window
 private JButton addNewReferenceButton = new JButton("Add Reference");
 private JButton okButton = new JButton("OK");
 private JButton cancelButton = new JButton("Cancel");

 private final static String SLIP_RATE_PARAMS_TITLE = "Slip Rate Params";


  public AddEditSlipRate() {
    try {
      this.addSlipRateInfoParameters();
      this.setLayout(GUI_Utils.gridBagLayout);
      this.add(slipRateParameterListEditor,
               new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                      , GridBagConstraints.CENTER,
                                      GridBagConstraints.BOTH,
                                      new Insets(0, 0, 0, 0), 0, 0));
    }catch(Exception e) {
      e.printStackTrace();
    }

  }


  /**
   * Add the input parameters if the user provides the slip rate info
   */
  private void addSlipRateInfoParameters() throws Exception {
   ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
   this.slipRateEstimateParam = new EstimateParameter(this.SLIP_RATE_PARAM_NAME,
        SLIP_RATE_UNITS, SLIP_RATE_MIN, SLIP_RATE_MAX, allowedEstimates);
   // slipRateEstimateParamEditor = new ConstrainedEstimateParameterEditor(slipRateEstimateParam);
    this.aSeismicSlipFactorParam = new EstimateParameter(this.ASEISMIC_SLIP_FACTOR_PARAM_NAME,
        ASEISMIC_SLIP_FACTOR_MIN, ASEISMIC_SLIP_FACTOR_MAX, allowedEstimates);
    //aSeismicSlipFactorParamEditor = new ConstrainedEstimateParameterEditor(aSeismicSlipFactorParam);
    // references
    ArrayList availableReferences = getAvailableReferences();
    this.slipRateReferencesParam = new StringListParameter(this.SLIP_RATE_REFERENCES_PARAM_NAME, availableReferences);
    //slipRateReferencesParamEditor = new ConstrainedStringListParameterEditor(slipRateReferencesParam);

    slipRateCommentsParam = new StringParameter(this.SLIP_RATE_COMMENTS_PARAM_NAME);
    //slipRateCommentsParamEditor = new StringParameterEditor(slipRateCommentsParam);
    ParameterList paramList = new ParameterList();
    paramList.addParameter(slipRateEstimateParam);
    paramList.addParameter(aSeismicSlipFactorParam);
    paramList.addParameter(slipRateCommentsParam);
    paramList.addParameter(slipRateReferencesParam);
    slipRateParameterListEditor = new ParameterListEditor(paramList);
    slipRateParameterListEditor.setTitle(this.SLIP_RATE_PARAMS_TITLE);
   }

   /**
   * Get a list of available references.
   *  THIS IS JUST A FAKE IMPLEMENTATION. IT SHOULD GET THIS FROM THE DATABASE.
   * @return
   */
private ArrayList getAvailableReferences() {
  ArrayList referencesNamesList = new ArrayList();
  referencesNamesList.add("Reference 1");
  referencesNamesList.add("Reference 2");
  return referencesNamesList;

}


}