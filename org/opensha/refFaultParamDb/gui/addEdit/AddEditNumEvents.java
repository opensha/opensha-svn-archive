package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import java.util.ArrayList;
import org.opensha.param.editor.ParameterListEditor;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditNumEvents extends JPanel {
  // Number of events parameter
  private final static String NUM_EVENTS_PARAM_NAME="Number of Events";
  private final static String NUM_EVENTS_REFERENCES_PARAM_NAME="Num of Events References";
  private final static double NUM_EVENTS_MIN=0;
  private final static double NUM_EVENTS_MAX=Integer.MAX_VALUE;

  private StringListParameter numEventsReferencesParam;
  private EstimateParameter numEventsParam;

  // parameter List editor
  private ParameterListEditor numEventsParameterListEditor;

// various buttons in this window
  private JButton addNewReferenceButton = new JButton("Add Reference");
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");

  private final static String NUM_EVENTS_PARAMS_TITLE = "Num Events Params";

  public AddEditNumEvents() {
    try {
      addNumEventsParameters();
      this.setLayout(GUI_Utils.gridBagLayout);
      this.add(numEventsParameterListEditor, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
  * Add the input parameters if user provides the events
  */
 private void addNumEventsParameters() throws Exception {
   ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveIntValues();
   this.numEventsParam = new EstimateParameter(this.NUM_EVENTS_PARAM_NAME,
                                               NUM_EVENTS_MIN, NUM_EVENTS_MAX, allowedEstimates);
  // numEventsParamEditor  = new ConstrainedEstimateParameterEditor(numEventsParam);
   // references
  ArrayList availableReferences = getAvailableReferences();
  this.numEventsReferencesParam = new StringListParameter(this.NUM_EVENTS_REFERENCES_PARAM_NAME, availableReferences);
 // numEventsReferencesParamEditor = new ConstrainedStringListParameterEditor(numEventsReferencesParam);
  ParameterList paramList = new ParameterList();
  paramList.addParameter(numEventsParam);
  paramList.addParameter(numEventsReferencesParam);
  this.numEventsParameterListEditor = new ParameterListEditor(paramList);
  numEventsParameterListEditor.setTitle(this.NUM_EVENTS_PARAMS_TITLE);
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