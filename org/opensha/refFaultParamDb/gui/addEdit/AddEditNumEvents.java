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
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.param.editor.ArbitrarilyDiscretizedFuncParameterEditor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.data.estimate.IntegerEstimate;

/**
 * <p>Title: AddEditNumEvents.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditNumEvents extends LabeledBoxPanel implements ParameterChangeListener {
  // Number of events parameter
  private final static String NUM_EVENTS_PARAM_NAME="Number of Events";
  private final static String MIN_EVENTS_PARAM_NAME="Min # of Events";
  private final static String MAX_EVENTS_PARAM_NAME="Max # of Events";
  private final static String NUM_EVENTS_COMMENTS_PARAM_NAME="Comments";
  private final static String NUM_EVENTS_LIST_HEADER="# of Events";
  private final static String PROB_HEADER="Prob. this is correct # events";
  private final static String EVENT_PROB_PARAM_NAME= "Events Prob";
  private final static int NUM_EVENTS_MIN=0;
  private final static int NUM_EVENTS_MAX=Integer.MAX_VALUE;
  private final static String NUM_EVENTS_UNITS=" ";


  // various parameters
  private IntegerParameter minEventsParam;
  private IntegerParameter maxEventsParam;
  private StringParameter numEventsCommentsParam;
  private ArbitrarilyDiscretizedFuncParameter eventsProbParameter;

  // parameter editors
  private IntegerParameterEditor minEventsParamEditor;
  private IntegerParameterEditor maxEventsParamEditor;
  private CommentsParameterEditor numEventsCommentsParamEditor;
  private ArbitrarilyDiscretizedFuncParameterEditor eventsProbParameterEditor;

  private ArbitrarilyDiscretizedFunc eventProbs = new ArbitrarilyDiscretizedFunc();
  private final static String NUM_EVENTS_PARAMS_TITLE = "Num Events Params";

  public AddEditNumEvents() {
    try {
       this.setLayout(GUI_Utils.gridBagLayout);
       addNumEventsParameters();
       this.setMinimumSize(new Dimension(0, 0));
    }catch(Exception e) {
      e.printStackTrace();
    }
  }




  /**
  * Add the input parameters if user provides the events
  */
 private void addNumEventsParameters() throws Exception {

   // min number of events
   minEventsParam = new IntegerParameter(this.MIN_EVENTS_PARAM_NAME, NUM_EVENTS_MIN, NUM_EVENTS_MAX);
   minEventsParam.addParameterChangeListener(this);
   minEventsParamEditor = new IntegerParameterEditor(minEventsParam);
   // max number of events
   maxEventsParam = new IntegerParameter(this.MAX_EVENTS_PARAM_NAME, NUM_EVENTS_MIN, NUM_EVENTS_MAX);
   maxEventsParam.addParameterChangeListener(this);
   maxEventsParamEditor = new IntegerParameterEditor(maxEventsParam);

   // parameter to show events list
   eventProbs.setXAxisName(this.NUM_EVENTS_LIST_HEADER);
   eventProbs.setYAxisName(this.PROB_HEADER);
   eventsProbParameter = new ArbitrarilyDiscretizedFuncParameter(EVENT_PROB_PARAM_NAME,eventProbs);
   eventsProbParameterEditor = new ArbitrarilyDiscretizedFuncParameterEditor(eventsProbParameter);
   eventsProbParameterEditor.setXEnabled(false); // user cannot type in the X values

   // comments
   numEventsCommentsParam = new StringParameter(this.NUM_EVENTS_COMMENTS_PARAM_NAME);
   numEventsCommentsParamEditor = new CommentsParameterEditor(numEventsCommentsParam);

   // Add the editors to the panel
   int yPos=0;
   add(minEventsParamEditor, new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
   add(maxEventsParamEditor, new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
   add(eventsProbParameterEditor, new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
   add(numEventsCommentsParamEditor, new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));

   setTitle(this.NUM_EVENTS_PARAMS_TITLE);
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
   Integer minVal = (Integer)minEventsParam.getValue();
   if(minVal==null) {
     JOptionPane.showMessageDialog(this, minEventsParam.getName() + " is missing");
     return;
   }
   int min = minVal.intValue();
   Integer maxVal = (Integer)maxEventsParam.getValue();
   if(maxVal==null) {
    JOptionPane.showMessageDialog(this, maxEventsParam.getName() + " is missing");
    return;
  }
   int max = maxVal.intValue();
   String text="";
   eventProbs.clear();
   for(int i=min; i<=max; ++i) {
     eventProbs.set((double)i,0.0);
   }
   eventsProbParameter.setValue(eventProbs);
   eventsProbParameterEditor.refreshParamEditor();
  }

  /**
   * Get the comments for num events estimate
   *
   * @return
   */
  public String getNumEventsComments() {
    return (String)this.numEventsCommentsParam.getValue();
  }

  /**
   * Get the num events estimate
   *
   * @return
   */
  public EstimateInstances getNumEventsEstimate() {
     ArbitrarilyDiscretizedFunc eventProb = (ArbitrarilyDiscretizedFunc)this.eventsProbParameter.getValue();
     IntegerEstimate numEventsEstimate = new IntegerEstimate(eventProb,false);
     return new EstimateInstances(numEventsEstimate, this.NUM_EVENTS_UNITS);
   }

}