package org.opensha.refFaultParamDb.gui.addEdit;

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

/**
 * <p>Title: AddEditIndividualEvent.java </p>
 * <p>Description: This GUI allows to add an event information: Event name,
 * event date estimate, slip estimate, whether diplacement shared with other events, references, comments </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditIndividualEvent extends JFrame implements ParameterChangeListener,
    ActionListener {
  private JPanel topPanel = new JPanel();
  private JSplitPane estimatesSplitPane = new JSplitPane();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JSplitPane detailedEventInfoSplitPane = new JSplitPane();
  private JButton okButton = new JButton();
  private JButton cancelButton = new JButton();
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
  private final static double TIME_ESTIMATE_MIN=0;
  private final static double TIME_ESTIMATE_MAX=Double.MAX_VALUE;
  private final static String TIME_ESTIMATE_UNITS="years";

  // add new reference button
  private JButton addNewReferenceButton = new JButton("Add Reference");
  private final static String addNewReferenceToolTipText = "Add Reference not currently in database";

  //slip rate constants
  private final static String SLIP_RATE_UNITS = "mm/yr";
  private final static double SLIP_RATE_MIN = 0;
  private final static double SLIP_RATE_MAX = Double.POSITIVE_INFINITY;

  // diplacement parameter list editor title
  private final static String DISPLACEMENT_TITLE = "Shared Slip";
  private final static String TITLE = "Add/Edit Event";

  // various parameter types
  private StringParameter eventNameParam;
  private StringParameter commentsParam;
  private EstimateParameter dateEstParam;
  private EstimateParameter slipEstParam;
  private BooleanParameter displacementSharedParam;
  private StringListParameter sharedEventParam;
  private StringListParameter referencesParam;

  // various parameter editors
  private StringParameterEditor eventNameParamEditor;
  private CommentsParameterEditor commentsParamEditor;
  private ConstrainedEstimateParameterEditor dateEstParamEditor;
  private ConstrainedEstimateParameterEditor slipEstParamEditor;
  private ParameterListEditor displacementParamListEditor;
  private ConstrainedStringListParameterEditor referencesParamEditor;

  private final static int WIDTH = 600;
  private final static int HEIGHT = 700;


  public AddEditIndividualEvent() {
    try {
      // initialize the GUI
      jbInit();
      // add Parameters and editors
      initParamsAndEditors();
      // add the action listeners to the button
      addActionListeners();
      // set the title
      this.setTitle(TITLE);
      // Show/Hide the editor to enter the name of event with which dispalcement is shared
      setSharedEventVisible(((Boolean)this.displacementSharedParam.getValue()).booleanValue());
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
    eventNameParam = new StringParameter(this.EVENT_NAME_PARAM_NAME);
    eventNameParamEditor = new StringParameterEditor(eventNameParam);

    // comments param
    commentsParam = new StringParameter(this.COMMENTS_PARAM_NAME);
    commentsParamEditor = new CommentsParameterEditor(commentsParam);

    // references param
    referencesParam = new StringListParameter(this.REFERENCES_PARAM_NAME, this.getAvailableReferences());
    referencesParamEditor = new ConstrainedStringListParameterEditor(referencesParam);

    // date param
    ArrayList dateAllowedEstList = EstimateConstraint.createConstraintForDateEstimates();
    dateEstParam = new EstimateParameter(this.DATE_ESTIMATE_PARAM_NAME,
             this.TIME_ESTIMATE_UNITS, this.TIME_ESTIMATE_MIN, this.TIME_ESTIMATE_MAX,
             dateAllowedEstList);
    this.dateEstParamEditor = new ConstrainedEstimateParameterEditor(dateEstParam,true,false);

    // slip rate param
    ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
    this.slipEstParam = new EstimateParameter(this.SLIP_ESTIMATE_PARAM_NAME,
      SLIP_RATE_UNITS, SLIP_RATE_MIN, SLIP_RATE_MAX, allowedEstimates);
    slipEstParamEditor = new ConstrainedEstimateParameterEditor(slipEstParam, true,false);

    // whether displacement is shared with other events
    this.displacementSharedParam = new BooleanParameter(this.DISPLACEMENT_SHARED_PARAM_NAME, new Boolean(false));
    displacementSharedParam.addParameterChangeListener(this);

    // event name parameter with which dispalcement is shared(only if displacement is shared)
    ArrayList eventNamesList = getEventNamesList();
    this.sharedEventParam = new StringListParameter(SHARED_EVENT_PARAM_NAME, eventNamesList);
    ParameterList paramList  = new ParameterList();
    paramList.addParameter(displacementSharedParam);
    paramList.addParameter(sharedEventParam);
    displacementParamListEditor = new ParameterListEditor(paramList);
    displacementParamListEditor.setTitle(DISPLACEMENT_TITLE);

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
    this.estimatesSplitPane.add(dateEstParamEditor, JSplitPane.LEFT);

    // event slip and whether slip is shared
    LabeledBoxPanel slipPanel = new LabeledBoxPanel(gridBagLayout1);
    slipPanel.setTitle(SLIP_TITLE);
    slipPanel.add(slipEstParamEditor,  new GridBagConstraints(0, 0, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    slipPanel.add(displacementParamListEditor,  new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    estimatesSplitPane.add(slipPanel, JSplitPane.RIGHT);

    // comments and references
    LabeledBoxPanel commentsReferencesPanel = new LabeledBoxPanel(gridBagLayout1);
    commentsReferencesPanel.setTitle(COMMENTS_REFERENCES_TITLE);
    this.detailedEventInfoSplitPane.add(commentsReferencesPanel, JSplitPane.RIGHT);
    commentsReferencesPanel.add(this.commentsParamEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    commentsReferencesPanel.add(this.referencesParamEditor,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    commentsReferencesPanel.add(this.addNewReferenceButton,  new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
                 ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    // event name
    eventSummaryPanel.add(eventNameParamEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   }


  /**
   * This function is called whenever a paramter is changed and we have
   * registered as listeners to that parameters
   *
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    if(event.getParameterName().equalsIgnoreCase(this.DISPLACEMENT_SHARED_PARAM_NAME))
      setSharedEventVisible(((Boolean)event.getNewValue()).booleanValue());
  }

  /**
   * Show/Hide the editor to enter the name of event with which dispalcement is shared
   *
   * @param isVisible
   */
  private void setSharedEventVisible(boolean isVisible) {
    this.displacementParamListEditor.setParameterVisible(this.SHARED_EVENT_PARAM_NAME, isVisible);
  }

  /**
   * This function is called when a button is clicked on this screen
   *
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    if(event.getSource() == addNewReferenceButton) new AddNewReference();
  }

  /**
   * add the action listeners to the buttons
   */
  private void addActionListeners() {
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);
    this.addNewReferenceButton.setToolTipText(this.addNewReferenceToolTipText);
    addNewReferenceButton.addActionListener(this);
  }

  public static void main(String args[]) {
    AddEditIndividualEvent eventInfo = new AddEditIndividualEvent();
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
    cancelButton.setText("Cancel");
    okButton.setText("OK");
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    topPanel.setLayout(gridBagLayout2);
    this.getContentPane().setLayout(borderLayout1);
    eventSummaryPanel.setLayout(gridBagLayout1);
    this.getContentPane().add(topPanel, BorderLayout.CENTER);
    topPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 3, 0, 2), 305, 423));
    topPanel.add(okButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 147, 29, 0), 54, 7));
    topPanel.add(cancelButton,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 23, 29, 211), 36, 7));
    mainSplitPane.add(detailedEventInfoSplitPane, JSplitPane.BOTTOM);
    detailedEventInfoSplitPane.add(estimatesSplitPane, JSplitPane.LEFT);
    mainSplitPane.add(eventSummaryPanel, JSplitPane.TOP);
    estimatesSplitPane.setDividerLocation(WIDTH/3);
    mainSplitPane.setDividerLocation(50);
    detailedEventInfoSplitPane.setDividerLocation(WIDTH*2/3);
  }
}