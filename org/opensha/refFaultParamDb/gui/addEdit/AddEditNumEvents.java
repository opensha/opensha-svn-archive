package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import java.util.ArrayList;
import org.opensha.param.editor.ParameterListEditor;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.gui.LabeledBoxPanel;
import org.opensha.param.editor.IntegerParameterEditor;
import org.opensha.param.editor.estimate.ConstrainedEstimateParameterEditor;
import org.opensha.refFaultParamDb.gui.CommentsParameterEditor;
import org.opensha.param.editor.ConstrainedStringListParameterEditor;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeEvent;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditNumEvents extends LabeledBoxPanel implements ParameterChangeListener {
  // Number of events parameter
  private final static String NUM_EVENTS_PARAM_NAME="Number of Events";
  private final static String MIN_EVENTS_PARAM_NAME="Min Events";
  private final static String MAX_EVENTS_PARAM_NAME="Max Events";
  private final static String NUM_EVENTS_COMMENTS_PARAM_NAME="Comments";
  private final static String NUM_EVENTS_REFERENCES_PARAM_NAME="Choose References";
  private final static String NUM_EVENTS_LIST_PARAM_NAME="# of Events";
  private final static String PROB_PARAM_NAME="Prob. this is correct # events";
  private final static int NUM_EVENTS_MIN=0;
  private final static int NUM_EVENTS_MAX=Integer.MAX_VALUE;


  // various parameters
  private StringListParameter numEventsReferencesParam;
  private IntegerParameter minEventsParam;
  private IntegerParameter maxEventsParam;
  private StringParameter numEventsCommentsParam;
  private StringParameter numEventsListParameter;
  private StringParameter probEventsParameter;

  // parameter editors
  private ConstrainedStringListParameterEditor numEventsReferencesParamEditor;
  private IntegerParameterEditor minEventsParamEditor;
  private IntegerParameterEditor maxEventsParamEditor;
  private CommentsParameterEditor numEventsCommentsParamEditor;
  private CommentsParameterEditor numEventsListParameterEditor;
  private CommentsParameterEditor probEventsParameterEditor;

  // various buttons in this window
  private JButton addNewReferenceButton = new JButton("Add Reference not currently in database");


  private final static String NUM_EVENTS_PARAMS_TITLE = "Num Events Params";

  public AddEditNumEvents() {
    try {
       this.setLayout(GUI_Utils.gridBagLayout);
       addNumEventsParameters();
       updateNumEventsList();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
  * Add the input parameters if user provides the events
  */
 private void addNumEventsParameters() throws Exception {

   // min number of events
   minEventsParam = new IntegerParameter(this.MIN_EVENTS_PARAM_NAME, NUM_EVENTS_MIN, NUM_EVENTS_MAX, new Integer(1));
   minEventsParam.addParameterChangeListener(this);
   minEventsParamEditor = new IntegerParameterEditor(minEventsParam);
   // max number of events
   maxEventsParam = new IntegerParameter(this.MIN_EVENTS_PARAM_NAME, NUM_EVENTS_MIN, NUM_EVENTS_MAX, new Integer(2));
   maxEventsParam.addParameterChangeListener(this);
   maxEventsParamEditor = new IntegerParameterEditor(maxEventsParam);

   // Textarea to show events list
   numEventsListParameter = new StringParameter(this.NUM_EVENTS_LIST_PARAM_NAME);
   numEventsListParameterEditor = new CommentsParameterEditor(numEventsListParameter);
   numEventsListParameterEditor.setEnabled(false);

   // text area where user can type the probs.
   probEventsParameter = new StringParameter(this.PROB_PARAM_NAME);
   probEventsParameterEditor = new CommentsParameterEditor(probEventsParameter);

   // references
   ArrayList availableReferences = getAvailableReferences();
   this.numEventsReferencesParam = new StringListParameter(this.NUM_EVENTS_REFERENCES_PARAM_NAME, availableReferences);
   numEventsReferencesParamEditor = new ConstrainedStringListParameterEditor(numEventsReferencesParam);

   // comments
   numEventsCommentsParam = new StringParameter(this.NUM_EVENTS_COMMENTS_PARAM_NAME);
   numEventsCommentsParamEditor = new CommentsParameterEditor(numEventsCommentsParam);

   // Add the editors to the panel
   int yPos=0;
   add(minEventsParamEditor, new GridBagConstraints( 0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
   add(maxEventsParamEditor, new GridBagConstraints( 0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
   add(numEventsListParameterEditor, new GridBagConstraints( 0, yPos, 1, 3, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
   add(probEventsParameterEditor, new GridBagConstraints( 1, yPos, 1, 3, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
   yPos+=3;
   add(numEventsReferencesParamEditor, new GridBagConstraints( 0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
   this.add(this.addNewReferenceButton,
            new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                  , GridBagConstraints.CENTER,
                                  GridBagConstraints.NONE,
                                  new Insets(0, 0, 0, 0), 0, 0));
   add(numEventsCommentsParamEditor, new GridBagConstraints( 0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));

   setTitle(this.NUM_EVENTS_PARAMS_TITLE);
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

/**
 *  This method is called whenever a min or max is changed so that num events list
 * is updated
 * @param event
 */
  public void parameterChange(ParameterChangeEvent event) {
    updateNumEventsList();
  }

  /**
   * Update the  num events list
   */
  private void updateNumEventsList() {
    int min = ((Integer)minEventsParam.getValue()).intValue();
    int max = ((Integer)maxEventsParam.getValue()).intValue();
    String text="";
    for(int i=min; i<=max; ++i) {
      text+=""+i+"\n";
    }
    this.numEventsListParameter.setValue(text);
    numEventsListParameterEditor.refreshParamEditor();
  }



}