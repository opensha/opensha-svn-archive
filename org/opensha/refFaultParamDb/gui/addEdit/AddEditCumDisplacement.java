package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import java.util.ArrayList;
import org.opensha.param.editor.ParameterListEditor;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;

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

public class AddEditCumDisplacement extends JPanel{
  // ASEISMICE SLIP FACTOR
  private final static String ASEISMIC_SLIP_FACTOR_PARAM_NAME="Aseismic Slip Factor Estimate";
  private final static double ASEISMIC_SLIP_FACTOR_MIN=0;
  private final static double ASEISMIC_SLIP_FACTOR_MAX=1;

   // CUMULATIVE DISPLACEMENT
  private final static String CUMULATIVE_DISPLACEMENT_PARAM_NAME="Cumulative Displacement Estimate";
  private final static String CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME="Cumulative Displacement Comments";
  private final static String CUMULATIVE_DISPLACEMENT_REFERENCES_PARAM_NAME="Cumulative Displacement References";
  private final static String CUMULATIVE_DISPLACEMENT_UNITS = "mm";
  private final static double CUMULATIVE_DISPLACEMENT_MIN = 0;
  private final static double CUMULATIVE_DISPLACEMENT_MAX = Double.POSITIVE_INFINITY;


 private StringListParameter cumDisplacementReferencesParam;
 private EstimateParameter aSeismicSlipFactorParam;
 private EstimateParameter cumDisplacementParam;
 private StringParameter displacementCommentsParam;

 // parameter List editor
 private ParameterListEditor cumDisplacementParameterListEditor;

// various buttons in this window
 private JButton addNewReferenceButton = new JButton("Add Reference");
 private JButton okButton = new JButton("OK");
 private JButton cancelButton = new JButton("Cancel");
 private final static String CUM_DISPLACEMENT_PARAMS_TITLE = "Cumulative Displacement Params";

 /**
  * Add Cum displacement parameters
  */
 public AddEditCumDisplacement() {
   try {
     addCumulativeDisplacementParameters();
     this.setLayout(GUI_Utils.gridBagLayout);
     this.add(cumDisplacementParameterListEditor,
              new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                     , GridBagConstraints.CENTER,
                                     GridBagConstraints.BOTH,
                                     new Insets(0, 0, 0, 0), 0, 0));
   }catch(Exception e) {
     e.printStackTrace();
   }
  }

  /**
   * Add the input parameters if user provides the cumulative displacement
   */
  private void addCumulativeDisplacementParameters() throws Exception {
    ArrayList allowedEstimates = EstimateConstraint.
        createConstraintForPositiveDoubleValues();
    this.cumDisplacementParam = new EstimateParameter(this.
        CUMULATIVE_DISPLACEMENT_PARAM_NAME,
        CUMULATIVE_DISPLACEMENT_UNITS, CUMULATIVE_DISPLACEMENT_MIN,
        CUMULATIVE_DISPLACEMENT_MAX, allowedEstimates);
    // cumDisplacementParamEditor = new ConstrainedEstimateParameterEditor(cumDisplacementParam);
    displacementCommentsParam = new StringParameter(this.
        CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME);
    //displacementCommentsParamEditor = new StringParameterEditor(displacementCommentsParam);
    // references
    ArrayList availableReferences = getAvailableReferences();
    this.cumDisplacementReferencesParam = new StringListParameter(this.
        CUMULATIVE_DISPLACEMENT_REFERENCES_PARAM_NAME, availableReferences);
    //cumDisplacementReferencesParamEditor = new ConstrainedStringListParameterEditor(cumDisplacementReferencesParam);
    ParameterList paramList = new ParameterList();
    paramList.addParameter(cumDisplacementParam);
    paramList.addParameter(aSeismicSlipFactorParam);
    paramList.addParameter(displacementCommentsParam);
    paramList.addParameter(cumDisplacementReferencesParam);
    this.cumDisplacementParameterListEditor = new ParameterListEditor(paramList);
    cumDisplacementParameterListEditor.setTitle(this.
                                                CUM_DISPLACEMENT_PARAMS_TITLE);
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