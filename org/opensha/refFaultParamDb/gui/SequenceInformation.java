package org.opensha.refFaultParamDb.gui;

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

/**
 * <p>Title: SequenceInformation.java </p>
 * <p>Description: This GUI allows user to view/add information relating to
 * event sequences </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SequenceInformation extends JFrame implements ActionListener,
    ParameterChangeListener {
  private JPanel mainPanel = new JPanel();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JPanel sequenceParamsPanel = new JPanel();
  private JSplitPane eventSplitPane = new JSplitPane();
  private JButton okButton = new JButton();
  private JButton cancelButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  // TITLE
  private final static String TITLE = "Sequence Information";

  // various parameter names
  private final static String SEQUENCE_NUM_PARAM_NAME = "Sequence Number";
  private final static String SEQUENCE_NAME_PARAM_NAME = "Sequence Name";
  private final static String SEQUENCE_PROB_PARAM_NAME = "Sequence Prob.";
  private final static String COMMENTS_PARAM_NAME = "Comments";
  private final static String REFERENCES_PARAM_NAME = "References";
  private final static String MISSED_EVENTS_PROB_PARAM_NAME = "Probability of missed events";
  private final static String EVENTS_PARAM_NAME = "Events in Sequence";

  // constants for making missed events prob parameters
  private final static String BEFORE = "Before";
  private final static String BETWEEN = "Between";
  private final static double MISSED_EVENT_PROB_MIN=0.0;
  private final static double MISSED_EVENT_PROB_MAX=1.0;

  // Sequence Prob constraints
  private final static double SEQUENCE_PROB_MIN = 0;
  private final static double SEQUENCE_PROB_MAX = 1;

  // various parameter types
  private StringParameter sequenceNumParam;
  private StringParameter sequenceNameParam;
  private DoubleParameter sequenceProbParam;
  private StringParameter commentsParam;
  private StringListParameter eventsParam;
  private ParameterList missedEventsProbParamList;

  // various parameter editors
  private ConstrainedStringParameterEditor sequenceNumParamEditor;
  private StringParameterEditor sequenceNameParamEditor;
  private DoubleParameterEditor sequenceProbParamEditor;
  private CommentsParameterEditor commentsParamEditor;
  private ConstrainedStringListParameterEditor eventsParamEditor;
  private ParameterListEditor missedEventsProbParamEditor;

  // width and height for this window
  private final static int WIDTH = 700;
  private final static int HEIGHT = 700;

  // number of sequences
  private int numSequences;

  public SequenceInformation(int numSequences) {
    this.numSequences = numSequences;
    try {
      // intiliaze he GUI components
      jbInit();
      // add Parameters and editors
      initParamsAndEditors();
      // add the action listeners to the button
      addActionListeners();
      // set the title
      this.setTitle(TITLE);
      setSize(WIDTH, HEIGHT);
      show();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void initParamsAndEditors() throws Exception {
    // fill all the events from 1 to number of sequences
   ArrayList sequenceNumList = new ArrayList();
   for(int i=1; i<=numSequences; ++i) sequenceNumList.add(new String("Seq "+i));
   sequenceNumParam = new StringParameter(this.SEQUENCE_NUM_PARAM_NAME, sequenceNumList, (String)sequenceNumList.get(0));
   this.sequenceNumParamEditor = new ConstrainedStringParameterEditor(sequenceNumParam);

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
      eventSplitPane.remove(missedEventsProbParamEditor); // remove this from the splitpane
    ArrayList selectedEvents = (ArrayList)eventsParam.getValue();
    missedEventsProbParamList = new ParameterList();
    int numEvents = 0;
    if(selectedEvents!=null) numEvents = selectedEvents.size();
    DoubleParameter probParameter;
    String paramName;
    // create the missed events prob parameters (they are equal to number of events in sequence)
    for(int i=0; i<numEvents; ++i)  {
      if(i==0) paramName = BEFORE+" "+selectedEvents.get(i);
      else paramName = BETWEEN+" " +selectedEvents.get(i-1)+" & "+selectedEvents.get(i);
      probParameter = new DoubleParameter(paramName, this.MISSED_EVENT_PROB_MIN,
                                          this.MISSED_EVENT_PROB_MAX, new Double(1.0/numEvents));
      missedEventsProbParamList.addParameter(probParameter);
    }
    missedEventsProbParamEditor  = new ParameterListEditor(missedEventsProbParamList);
    missedEventsProbParamEditor.setTitle(MISSED_EVENTS_PROB_PARAM_NAME);
    eventSplitPane.add(missedEventsProbParamEditor, JSplitPane.RIGHT);
    eventSplitPane.setDividerLocation(300);
  }

  /**
   * Add the parameter editors to the GUI
   */
  private void addEditorstoGUI() {
    eventSplitPane.add(eventsParamEditor, JSplitPane.LEFT);
    int yPos=0;
    sequenceParamsPanel.add(sequenceNumParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    sequenceParamsPanel.add(sequenceNameParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    sequenceParamsPanel.add(sequenceProbParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    sequenceParamsPanel.add(commentsParamEditor,  new GridBagConstraints(1, 0, 1, 3, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

  }


  /**
   * This function is called when a button is clicked on this screen
   *
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
   /* else if(event.getSource()==this.okButton ||
      event.getSource()==this.cancelButton) this.di
    */
  }


  /**
   * add the action listeners to the buttons
   */
  private void addActionListeners() {
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);
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


  public static void main(String[] args) {
    SequenceInformation sequenceInformation = new SequenceInformation(5);
  }


  // initialize the GUI components
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    mainPanel.setLayout(gridBagLayout2);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    okButton.setText("OK");
    cancelButton.setText("Cancel");
    sequenceParamsPanel.setLayout(gridBagLayout1);
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 4, 0, 3), 360, 414));
    mainSplitPane.add(sequenceParamsPanel, JSplitPane.LEFT);
    mainSplitPane.add(eventSplitPane, JSplitPane.RIGHT);
    mainPanel.add(okButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 190, 11, 0), 32, 5));
    mainPanel.add(cancelButton,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 40, 11, 156), 14, 5));
    mainSplitPane.setDividerLocation(175);
    eventSplitPane.setDividerLocation(300);
  }
}