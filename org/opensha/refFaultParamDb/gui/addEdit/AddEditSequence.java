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

/**
 * <p>Title: SequenceInformation.java </p>
 * <p>Description: This GUI allows user to view/add information relating to
 * event sequences </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditSequence extends JFrame implements ActionListener,
    ParameterChangeListener {
  private JPanel mainPanel = new JPanel();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JPanel sequenceParamsPanel = new JPanel();
  private JSplitPane eventSplitPane = new JSplitPane();
  private JButton okButton = new JButton();
  private JButton cancelButton = new JButton();

  private BorderLayout borderLayout1 = new BorderLayout();

  // TITLE
  private final static String TITLE = "Add/Edit Sequence";

  // various parameter names
  private final static String SEQUENCE_NAME_PARAM_NAME = "Sequence Name";
  private final static String SEQUENCE_PROB_PARAM_NAME = "Sequence Prob.";
  private final static String COMMENTS_PARAM_NAME = "Comments";
  private final static String REFERENCES_PARAM_NAME = "Choose References";
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
  private StringListParameter referencesParam;
  private StringParameter commentsParam;
  private StringListParameter eventsParam;
  private ParameterList missedEventsProbParamList;

  // various parameter editors
  private StringParameterEditor sequenceNameParamEditor;
  private DoubleParameterEditor sequenceProbParamEditor;
  private ConstrainedStringListParameterEditor referencesParamEditor;
  private CommentsParameterEditor commentsParamEditor;
  private ConstrainedStringListParameterEditor eventsParamEditor;
  private ParameterListEditor missedEventsProbParamEditor;

  // add new reference button
  private JButton addNewReferenceButton = new JButton("Add Reference");
  private final static String addNewReferenceToolTipText = "Add Reference not currently in database";


  // width and height for this window
  private final static int WIDTH = 700;
  private final static int HEIGHT = 650;

  public AddEditSequence() {
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
      this.setLocationRelativeTo(null);
      show();
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

   // references param
   referencesParam = new StringListParameter(this.REFERENCES_PARAM_NAME, getAvailableReferences());
   this.referencesParamEditor = new ConstrainedStringListParameterEditor(referencesParam);

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
    eventSplitPane.add(missedEventsProbParamEditor, JSplitPane.RIGHT);
    eventSplitPane.setDividerLocation(300);
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
    eventSplitPane.add(eventsParamEditor, JSplitPane.LEFT);
    int yPos=0;
    // sequence name
    sequenceParamsPanel.add(sequenceNameParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    //sequence prob
    sequenceParamsPanel.add(sequenceProbParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    // comments
    sequenceParamsPanel.add(commentsParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    // references
    sequenceParamsPanel.add(this.referencesParamEditor,  new GridBagConstraints(1, 0, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    // add new reference
    sequenceParamsPanel.add(this.addNewReferenceButton,  new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

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
    if(event.getSource() == addNewReferenceButton) new AddNewReference();
  }


  /**
   * add the action listeners to the buttons
   */
  private void addActionListeners() {
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);
    addNewReferenceButton.addActionListener(this);
    addNewReferenceButton.setToolTipText(this.addNewReferenceToolTipText);
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
    AddEditSequence sequenceInformation = new AddEditSequence();
  }


  // initialize the GUI components
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    mainPanel.setLayout(GUI_Utils.gridBagLayout);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    okButton.setText("OK");
    cancelButton.setText("Cancel");
    sequenceParamsPanel.setLayout(GUI_Utils.gridBagLayout);
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 4, 0, 3), 360, 414));
    mainSplitPane.add(sequenceParamsPanel, JSplitPane.TOP);
    mainSplitPane.add(eventSplitPane, JSplitPane.BOTTOM);
    mainPanel.add(okButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 190, 11, 0), 32, 5));
    mainPanel.add(cancelButton,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 40, 11, 156), 14, 5));
    mainSplitPane.setDividerLocation(260);
    eventSplitPane.setDividerLocation(300);
  }
}