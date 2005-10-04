package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;
import java.awt.event.*;
import java.util.ArrayList;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.param.estimate.*;
import org.opensha.param.editor.*;
import org.opensha.param.editor.estimate.*;
import org.opensha.refFaultParamDb.gui.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.gui.LabeledBoxPanel;

/**
 * <p>Title: SequenceInformation.java </p>
 * <p>Description: This GUI allows user to view/add information relating to
 * event sequences </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditSequence extends LabeledBoxPanel implements ActionListener,
    ParameterChangeListener {

  private JButton addAnotherSequenceButton = new JButton("Add Another Sequence");
  private JButton sequenceWeightsButton = new JButton("Assign Weights to Sequences");


  // TITLE
  private final static String TITLE = "Add Sequence";

  // various parameter names
  private final static String SEQUENCE_NAME_PARAM_NAME = "Sequence Name";
  private final static String SEQUENCE_PROB_PARAM_NAME = "Sequence Prob.";
  private final static String COMMENTS_PARAM_NAME = "Comments";
  private final static String MISSED_EVENTS_PROB_PARAM_NAME = "Probability of missed events";
  private final static String EVENTS_PARAM_NAME = "Events in Sequence";

  // constants for making missed events prob parameters
  private final static String BEFORE = "Before";
  private final static String BETWEEN = "Between";
  private final static String AFTER = "After";
  private final static double MISSED_EVENT_PROB_MIN=0.0;
  private final static double MISSED_EVENT_PROB_MAX=1.0;

  // Sequence Prob constraints
  private final static double SEQUENCE_PROB_MIN = 0;
  private final static double SEQUENCE_PROB_MAX = 1;

  // various parameter types
  private StringParameter sequenceNameParam;
  private DoubleParameter sequenceProbParam;
  private StringParameter commentsParam;
  private StringListParameter eventsParam;
  private ParameterList missedEventsProbParamList;

  // various parameter editors
  private StringParameterEditor sequenceNameParamEditor;
  private DoubleParameterEditor sequenceProbParamEditor;
  private CommentsParameterEditor commentsParamEditor;
  private ConstrainedStringListParameterEditor eventsParamEditor;
  private ParameterListEditor missedEventsProbParamEditor;

  public AddEditSequence() {
    try {
      this.setLayout(GUI_Utils.gridBagLayout);
      // add Parameters and editors
      initParamsAndEditors();
      // add the action listeners to the button
      addActionListeners();
      // set the title
      this.setTitle(TITLE);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void initParamsAndEditors() throws Exception {

   // sequence name parameter
   sequenceNameParam = new StringParameter(this.SEQUENCE_NAME_PARAM_NAME);
   sequenceNameParamEditor = new StringParameterEditor(sequenceNameParam);

   // comments param
   commentsParam = new StringParameter(this.COMMENTS_PARAM_NAME);
   commentsParamEditor = new CommentsParameterEditor(commentsParam);

   // sequence probability
   this.sequenceProbParam = new DoubleParameter(this.SEQUENCE_PROB_PARAM_NAME, SEQUENCE_PROB_MIN, SEQUENCE_PROB_MAX);
   sequenceProbParamEditor = new ConstrainedDoubleParameterEditor(sequenceProbParam);

   // select events in this sequence
   ArrayList eventList = getAvailableEvents();
   this.eventsParam = new StringListParameter(this.EVENTS_PARAM_NAME, eventList);
   eventsParam.addParameterChangeListener(this);
   eventsParamEditor = new ConstrainedStringListParameterEditor(eventsParam);

   // missed events probability editor
   constructMissedEventsProbEditor();

   // add the parameter editors to the GUI componenets
    addEditorstoGUI();
  }


  /**
   * If user selects/deselects an event in missed events list, then add/remove to the
   * missed events prob. editor
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    if(event.getParameterName().equalsIgnoreCase(this.EVENTS_PARAM_NAME))
      constructMissedEventsProbEditor();
  }

  /**
   * construct the missed event param editor based on selected events in the sequence
   */
  private void constructMissedEventsProbEditor() {
    if(missedEventsProbParamEditor!=null)
      this.remove(missedEventsProbParamEditor); // remove this from the splitpane
    ArrayList selectedEvents = (ArrayList)eventsParam.getValue();
    missedEventsProbParamList = new ParameterList();
    int numEvents = 0;
    ArrayList paramNames=null;
    if(selectedEvents!=null) {
      numEvents = selectedEvents.size();
      paramNames = getNamesForMissedEventProbs(selectedEvents);
      DoubleParameter probParameter;
      String paramName;
      double eachProb = 1.0/(numEvents+1);
      // create the missed events prob parameters (they are equal to number of events in sequence)
      for(int i=0; i<=numEvents; ++i)  {
        probParameter = new DoubleParameter((String)paramNames.get(i), this.MISSED_EVENT_PROB_MIN,
                                            this.MISSED_EVENT_PROB_MAX, new Double(eachProb));
        missedEventsProbParamList.addParameter(probParameter);
      }
    }

    missedEventsProbParamEditor  = new ParameterListEditor(missedEventsProbParamList);
    missedEventsProbParamEditor.setTitle(MISSED_EVENTS_PROB_PARAM_NAME);
    this.add(missedEventsProbParamEditor, new GridBagConstraints(0, 4, 1, 3, 0.0, 0.0
           ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
    this.validate();
    this.repaint();
  }


  /**
   * Get the parameter name strings for missed event prob based on selected events
   *
   * @param selectEvents
   * @return
   */
  public static ArrayList getNamesForMissedEventProbs(ArrayList selectedEvents) {
    // create the missed events prob parameters (they are equal to number of events in sequence)
    int numEvents = selectedEvents.size();
    ArrayList names = new ArrayList();
    for(int i=0; i<numEvents; ++i)  {
      if(i==0) names.add(BEFORE+" "+selectedEvents.get(i));
      else names.add(BETWEEN+" " +selectedEvents.get(i-1)+" & "+selectedEvents.get(i));
    }
    // probability after the last event
    if(numEvents>0) {
      int i=numEvents-1;
      names.add(AFTER + " " + selectedEvents.get(i));
    }
    return names;
  }

  /**
   * Add the parameter editors to the GUI
   */
  private void addEditorstoGUI() {
    int yPos=0;
    // sequence name
    this.add(sequenceNameParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    //sequence prob
    add(sequenceProbParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    // comments
    add(commentsParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

    // events for this site
    add(eventsParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

    // add another sequence
   add(this.addAnotherSequenceButton,  new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
           ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    //add sequence weights
   add(this.sequenceWeightsButton,  new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
           ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2,2,2,2), 0, 0));


 }


  /**
   * add the action listeners to the buttons
   */
  private void addActionListeners() {
    addAnotherSequenceButton.addActionListener(this);
    this.sequenceWeightsButton.addActionListener(this);
  }


  public void actionPerformed(ActionEvent event) {

  }


 /**
  * Get a list of available events.
  *  THIS IS JUST A FAKE IMPLEMENTATION. IT SHOULD GET THIS FROM THE DATABASE.
  * @return
  */
 private ArrayList getAvailableEvents() {
   ArrayList eventNamesList = new ArrayList();
   eventNamesList.add("Event 1");
   eventNamesList.add("Event 2");
   eventNamesList.add("Event 3");
   /*eventNamesList.add("Event 4");
   eventNamesList.add("Event 5");
   eventNamesList.add("Event 6");*/
   return eventNamesList;
 }




  //static initializer for setting look & feel
  static {
    String osName = System.getProperty("os.name");
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }

}