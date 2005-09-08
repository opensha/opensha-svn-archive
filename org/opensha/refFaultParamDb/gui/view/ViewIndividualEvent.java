package org.opensha.refFaultParamDb.gui.view;

import javax.swing.*;
import java.awt.*;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import org.opensha.param.editor.*;
import org.opensha.param.editor.estimate.*;
import java.util.ArrayList;
import org.opensha.param.event.*;
import java.awt.event.*;
import org.opensha.refFaultParamDb.gui.*;
import org.opensha.gui.LabeledBoxPanel;
import org.opensha.refFaultParamDb.gui.addEdit.AddNewReference;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditIndividualEvent;

/**
 * <p>Title: AddEditIndividualEvent.java </p>
 * <p>Description: This GUI allows to view an event information: Event name,
 * event date estimate, slip estimate, whether diplacement shared with other events, references, comments </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewIndividualEvent extends JFrame implements ParameterChangeListener,
    ActionListener {
  private JPanel topPanel = new JPanel();
  private JSplitPane estimatesSplitPane = new JSplitPane();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JSplitPane detailedEventInfoSplitPane = new JSplitPane();
  private JButton closeButton = new JButton("Close");
  private JButton editButton  = new JButton("Edit Event");
  private JButton addButton  = new JButton("Add New Event");
  private JPanel eventSummaryPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  // various parameter names
  private final static String EVENT_NAME_PARAM_NAME = "Event Name";
  private final static String COMMENTS_PARAM_NAME = "Comments";
  private final static String REFERENCES_PARAM_NAME = "Choose References";
  private final static String DATE_ESTIMATE_PARAM_NAME = "Event Time Estimate";
  private final static String SLIP_ESTIMATE_PARAM_NAME = "Event Slip Estimate";
  private final static String SLIP_TITLE = "Event Slip";
  private final static String DISPLACEMENT_SHARED_PARAM_NAME = "Slip Shared With Other Events";
  private final static String SHARED_EVENT_PARAM_NAME = "Names of Events Sharing Slip";
  private final static String COMMENTS_REFERENCES_TITLE="Comments & References";

  //date estimate related constants
  private final static String TIME_ESTIMATE_UNITS="years";
  //slip rate constants
  private final static String SLIP_RATE_UNITS = "mm/yr";
  // diplacement parameter list editor title
  private final static String DISPLACEMENT_TITLE = "Shared Slip";
  private final static String TITLE = "View Event";

  // information displayed for selected event
  private StringParameter eventNameParam;
  private InfoLabel commentsLabel = new InfoLabel();
  private InfoLabel timeEstLabel = new InfoLabel();
  private InfoLabel slipEstLabel = new InfoLabel();
  private InfoLabel displacementSharedLabel = new InfoLabel();
  private InfoLabel sharedEventLabel = new InfoLabel();
  private InfoLabel referencesLabel = new InfoLabel();

  // various parameter editors
  private ConstrainedStringParameterEditor eventNameParamEditor;

  private final static int WIDTH = 600;
  private final static int HEIGHT = 700;

  public ViewIndividualEvent() {
    try {
      // initialize the GUI
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

  /**
   * Intialize the parameters and editors and add to the GUI
   */
  private void initParamsAndEditors() throws Exception {

    // event name parameter
    ArrayList eventNamesList = getEventNamesList();
    eventNameParam = new StringParameter(this.EVENT_NAME_PARAM_NAME, eventNamesList,
                                         (String)eventNamesList.get(0));
    eventNameParamEditor = new ConstrainedStringParameterEditor(eventNameParam);

    // add the parameter editors to the GUI componenets
    addEditorstoGUI();
  }





  /**
   * Get a list of all the event names
   * @return
   */
  private ArrayList getEventNamesList() {
    ArrayList eventList = new ArrayList();
    eventList.add("Test Event 1");
    eventList.add("Test Event 2");
    return eventList;
  }

  /**
   * Add the parameter editors to the GUI
   */
  private void addEditorstoGUI() {

    // event time estimate
    this.estimatesSplitPane.add(timeEstLabel, JSplitPane.LEFT);

    // event slip and whether slip is shared
    LabeledBoxPanel slipPanel = new LabeledBoxPanel(gridBagLayout1);
    slipPanel.setTitle(SLIP_TITLE);
    slipPanel.add(slipEstLabel,  new GridBagConstraints(0, 0, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    slipPanel.add(displacementSharedLabel,  new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    estimatesSplitPane.add(slipPanel, JSplitPane.RIGHT);

    // comments and references
    LabeledBoxPanel commentsReferencesPanel = new LabeledBoxPanel(gridBagLayout1);
    commentsReferencesPanel.setTitle(COMMENTS_REFERENCES_TITLE);
    this.detailedEventInfoSplitPane.add(commentsReferencesPanel, JSplitPane.RIGHT);
    commentsReferencesPanel.add(this.commentsLabel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    commentsReferencesPanel.add(this.referencesLabel,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

    // event name
    eventSummaryPanel.add(eventNameParamEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    eventSummaryPanel.add(this.editButton,  new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    eventSummaryPanel.add(this.addButton,  new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
  }


  /**
   * This function is called whenever a paramter is changed and we have
   * registered as listeners to that parameters
   *
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {

  }

  /**
   * Show the info according to event selected by the user
   * @param eventName
   */
  private void setEventInfo(String eventName) {
   /* TimeEstimate startTime = new TimeEstimate();
    startTime.setForKaUnits(new NormalEstimate(1000, 50), 1950);
    String comments = "Comments about this event";
    ArrayList references = new ArrayList();
    references.add("Ref 4");
    references.add("Ref 1");*/

  }

  /**
   * This function is called when a button is clicked on this screen
   *
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
     Object source = event.getSource();
     if(source==addButton || source==editButton) new AddEditIndividualEvent();
  }

  /**
   * add the action listeners to the buttons
   */
  private void addActionListeners() {
    addButton.addActionListener(this);
    editButton.addActionListener(this);
    closeButton.addActionListener(this);
  }

  public static void main(String args[]) {
    ViewIndividualEvent eventInfo = new ViewIndividualEvent();
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


  /**
   * initialize the GUI
   * @throws java.lang.Exception
   */
  private void jbInit() throws Exception {
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    topPanel.setLayout(gridBagLayout2);
    this.getContentPane().setLayout(borderLayout1);
    eventSummaryPanel.setLayout(gridBagLayout1);
    this.getContentPane().add(topPanel, BorderLayout.CENTER);
    topPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 3, 0, 2), 305, 423));
    topPanel.add(closeButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 147, 29, 0), 54, 7));
    mainSplitPane.add(detailedEventInfoSplitPane, JSplitPane.BOTTOM);
    detailedEventInfoSplitPane.add(estimatesSplitPane, JSplitPane.LEFT);
    mainSplitPane.add(eventSummaryPanel, JSplitPane.TOP);
    estimatesSplitPane.setDividerLocation(WIDTH/3);
    mainSplitPane.setDividerLocation(50);
    detailedEventInfoSplitPane.setDividerLocation(WIDTH*2/3);
  }
}
