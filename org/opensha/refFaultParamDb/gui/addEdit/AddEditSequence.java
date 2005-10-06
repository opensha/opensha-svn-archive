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
import org.opensha.refFaultParamDb.vo.EventSequence;
import java.util.Iterator;
import org.opensha.refFaultParamDb.dao.db.PaleoEventDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.vo.PaleoEvent;

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
  private final static String COMMENTS_PARAM_NAME = "Comments";
  private final static String MISSED_EVENTS_PROB_PARAM_NAME = "Probability of missed events";
  private final static String EVENTS_PARAM_NAME = "Events in Sequence";
  private final static String SEQUENCE_PROB_PARAM_NAME = "Prob of occurence of Seq ";
  private final static String SEQUENCE_PROB_TITLE = "Sequence Probabilities";

  // parameter default values
  private final static String SEQUENCE_NAME_PARAM_DEFAULT = "Enter Sequence Name";
  private final static String COMMENTS_PARAM_DEFAULT = "Enter Comments";

  // messages to be shown to user
  private final static String MSG_MISSING_SEQUENCE_NAME="Please enter sequence name";
  private final static String MSG_MISSING_EVENT_NAMES="Please select atleast 1 event in this sequence";
  private final static String MSG_NEED_TO_SAVE_CURRRENT_SEQ = "Do you want to save current sequence?";
  private final static String MSG_NO_EVENT="Cannot add sequence as no event exists for this site in database";

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
  private StringParameter commentsParam;
  private StringListParameter eventsParam;
  private ParameterList missedEventsProbParamList;
  private ParameterList sequenceProbParamList;

  // various parameter editors
  private StringParameterEditor sequenceNameParamEditor;
  private CommentsParameterEditor commentsParamEditor;
  private ConstrainedStringListParameterEditor eventsParamEditor;
  private ParameterListEditor missedEventsProbParamEditor;
  private ParameterListEditor sequenceProbEditor;

  private ArrayList sequenceList = new ArrayList();
  private PaleoEventDB_DAO paleoEventDAO = new PaleoEventDB_DAO(DB_AccessAPI.dbConnection);
  private int siteId;
  private String siteEntryDate;


  public AddEditSequence(int siteId, String siteEntryDate) {
      this.siteId = siteId;
      this.siteEntryDate = siteEntryDate;
      this.setLayout(GUI_Utils.gridBagLayout);
      // add Parameters and editors
      initParamsAndEditors();
      // add the action listeners to the button
      addActionListeners();
      // set the title
      this.setTitle(TITLE);
  }

  private void initParamsAndEditors()  {

   // sequence name parameter
   try {
     sequenceNameParam = new StringParameter(this.SEQUENCE_NAME_PARAM_NAME,
                                             SEQUENCE_NAME_PARAM_DEFAULT);
     sequenceNameParamEditor = new StringParameterEditor(sequenceNameParam);

     // comments param
     commentsParam = new StringParameter(this.COMMENTS_PARAM_NAME,
                                         this.COMMENTS_PARAM_DEFAULT);
     commentsParamEditor = new CommentsParameterEditor(commentsParam);
   }catch(Exception e) {
     e.printStackTrace();
   }

   // select events in this sequence
   ArrayList eventList = getAvailableEvents();
   if(eventList==null || eventList.size()==0)
     throw new RuntimeException(MSG_NO_EVENT);
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
    Object source = event.getSource();
    try {
      if (source == this.addAnotherSequenceButton) {
        saveCurrentSequence();
        this.removeAll(); // remove parameters for current sequence
        initParamsAndEditors(); // add parameters so that user can emter another sequence
      }
      else if (source == this.sequenceWeightsButton) {
        int option = JOptionPane.showConfirmDialog(this,
            MSG_NEED_TO_SAVE_CURRRENT_SEQ, this.TITLE,
            JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
          saveCurrentSequence();
        this.removeAll(); // remove all the parameters
        addProbParams(); // add params so that user can enter probability for each sequence
        }
    } catch(Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  /**
   * Add the parameters so that user can enter probability for each sequence
   */
  private void addProbParams() {
    sequenceProbParamList = new ParameterList();
    int numSequences = this.sequenceList.size();
    Double seqProbDefault = new Double(1.0/numSequences);
    for(int i=0; i<numSequences; ++i) {
      // sequence probability
      DoubleParameter sequenceProbParam = new DoubleParameter(this.SEQUENCE_PROB_PARAM_NAME+i,
          SEQUENCE_PROB_MIN, SEQUENCE_PROB_MAX, seqProbDefault);
      sequenceProbParamList.addParameter(sequenceProbParam);
    }
    this.sequenceProbEditor = new ParameterListEditor(sequenceProbParamList);
    sequenceProbEditor.setTitle(SEQUENCE_PROB_TITLE);
    this.add(sequenceProbEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    this.validate();
    this.repaint();
  }


  /**
   * Save the current sequence in the sequence list
   */
  private void saveCurrentSequence() {
    EventSequence sequence = new EventSequence();
    String sequenceName = ((String)this.sequenceNameParam.getValue()).trim();
    // check that user entered sequence name
    if(sequenceName.equalsIgnoreCase("") ||
       sequenceName.equalsIgnoreCase(this.SEQUENCE_NAME_PARAM_DEFAULT))
      throw new RuntimeException(MSG_MISSING_SEQUENCE_NAME);
    // check that user picked atleast 1 event for the sequence
    ArrayList selectedEvents = (ArrayList)eventsParam.getValue();
    if(selectedEvents == null || selectedEvents.size()==0)
      throw new RuntimeException(MSG_MISSING_EVENT_NAMES);
    // ietrator over all the missed events prob parameters
    Iterator paramsIterator = missedEventsProbParamList.getParametersIterator();
    ArrayList missingProbs = new ArrayList();
    double missedProbs[] = new double[selectedEvents.size()+1];
    int i=0;
    while(paramsIterator.hasNext()) {
      missedProbs[i++] = ((Double)((ParameterAPI)paramsIterator.next()).getValue()).doubleValue();
    }
    sequence.setComments((String)this.commentsParam.getValue());
    sequence.setEventsParam(selectedEvents);
    sequence.setSequenceName(sequenceName);
    sequence.setMissedEventsProbList(missedProbs);
    sequence.setSiteId(this.siteId);
    sequence.setSiteEntryDate(this.siteEntryDate);
    // add the sequence to the list
    this.sequenceList.add(sequence);
  }


 /**
  * Get a list of available events.
  * @return
  */
 private ArrayList getAvailableEvents() {
   ArrayList eventsInfoList = this.paleoEventDAO.getAllEvents(this.siteId);
   ArrayList eventNamesList = new ArrayList();
   for(int i=0; i<eventsInfoList.size(); ++i)
     eventNamesList.add(((PaleoEvent)eventsInfoList.get(i)).getEventName());
   return eventNamesList;
 }

 /**
  * Return a ArrayList where each element is  a EventSequence object
  * @return
  */
 public ArrayList getAllSequences() {
   // set the sequence probabilities
   Iterator it = this.sequenceProbParamList.getParametersIterator();
   int i=0;
   while(it.hasNext()) {
     DoubleParameter param =  (DoubleParameter)it.next();
     ((EventSequence)this.sequenceList.get(i)).setSequenceProb(((Double)param.getValue()).doubleValue());
     ++i;
   }
   return sequenceList;
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