package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import java.awt.*;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import org.opensha.param.editor.*;
import org.opensha.param.editor.estimate.*;
import java.util.ArrayList;
import org.opensha.param.event.*;
import ch.randelshofer.quaqua.QuaquaManager;
import java.awt.event.*;

/**
 * <p>Title: PerEventInformation.java </p>
 * <p>Description: This GUI allows to view the per event information: Event name,
 * event date estimate, slip estimate, whether diplacement shared with other events, references, comments </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PerEventInformation extends JFrame implements ParameterChangeListener, ActionListener {
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
  private final static String EVENT_NUM_PARAM_NAME = "Event Number";
  private final static String EVENT_NAME_PARAM_NAME = "Event Name";
  private final static String COMMENTS_PARAM_NAME = "Comments";
  private final static String REFERENCES_PARAM_NAME = "References";
  private final static String DATE_ESTIMATE_PARAM_NAME = "Event Time Estimate";
  private final static String SLIP_ESTIMATE_PARAM_NAME = "Event Slip Estimate";
  private final static String DISPLACEMENT_SHARED_PARAM_NAME = "Slip Shared With Other Events";
  private final static String SHARED_EVENT_PARAM_NAME = "Names of Events Sharing Slip";

  //date estimate related constants
  private final static double TIME_ESTIMATE_MIN=0;
  private final static double TIME_ESTIMATE_MAX=Double.MAX_VALUE;
  private final static String TIME_ESTIMATE_UNITS="years";

  //slip rate constants
  private final static String SLIP_RATE_UNITS = "mm/yr";
  private final static double SLIP_RATE_MIN = 0;
  private final static double SLIP_RATE_MAX = Double.POSITIVE_INFINITY;

  // diplacement parameter list editor title
  private final static String DISPLACEMENT_TITLE = "Shared Slip";
  private final static String TITLE = "Per Event Information";

  // various parameter types
  private StringParameter eventNumParam;
  private StringParameter eventNameParam;
  private StringParameter commentsParam;
  private EstimateParameter dateEstParam;
  private EstimateParameter slipEstParam;
  private BooleanParameter displacementSharedParam;
  private StringListParameter sharedEventParam;

  // various parameter editors
  private ConstrainedStringParameterEditor eventNumParamEditor;
  private StringParameterEditor eventNameParamEditor;
  private CommentsParameterEditor commentsParamEditor;
  private ConstrainedEstimateParameterEditor dateEstParamEditor;
  private ConstrainedEstimateParameterEditor slipEstParamEditor;
  private ParameterListEditor displacementParamListEditor;

  private final static int WIDTH = 700;
  private final static int HEIGHT = 700;

  // events for this GUI
  private int numberOfEvents;

  public PerEventInformation(int numEvents) {
    this.numberOfEvents = numEvents;
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

    // fill all the events from 1 to number of events
    ArrayList eventNumList = new ArrayList();
    for(int i=1; i<=numberOfEvents; ++i) eventNumList.add(new String("Ev "+i));
    eventNumParam = new StringParameter(this.EVENT_NUM_PARAM_NAME, eventNumList, (String)eventNumList.get(0));
    eventNumParam.addParameterChangeListener(this);
    this.eventNumParamEditor = new ConstrainedStringParameterEditor(eventNumParam);

    // event name parameter
    eventNameParam = new StringParameter(this.EVENT_NAME_PARAM_NAME);
    eventNameParamEditor = new StringParameterEditor(eventNameParam);

    // comments param
    commentsParam = new StringParameter(this.COMMENTS_PARAM_NAME);
    commentsParamEditor = new CommentsParameterEditor(commentsParam);


    // date param
    ArrayList dateAllowedEstList = EstimateConstraint.createConstraintForDateEstimates();
    dateEstParam = new EstimateParameter(this.DATE_ESTIMATE_PARAM_NAME,
             this.TIME_ESTIMATE_UNITS, this.TIME_ESTIMATE_MIN, this.TIME_ESTIMATE_MAX,
             dateAllowedEstList);
    this.dateEstParamEditor = new ConstrainedEstimateParameterEditor(dateEstParam,true);

    // slip rate param
    ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
    this.slipEstParam = new EstimateParameter(this.SLIP_ESTIMATE_PARAM_NAME,
      SLIP_RATE_UNITS, SLIP_RATE_MIN, SLIP_RATE_MAX, allowedEstimates);
    slipEstParamEditor = new ConstrainedEstimateParameterEditor(slipEstParam, true);

    // whether displacement is shared with other events
    this.displacementSharedParam = new BooleanParameter(this.DISPLACEMENT_SHARED_PARAM_NAME, new Boolean(false));
    displacementSharedParam.addParameterChangeListener(this);
    // event name parameter with which dispalcement is shared(only if displacement is shared)
    this.sharedEventParam = new StringListParameter(SHARED_EVENT_PARAM_NAME, eventNumList);
    ParameterList paramList  = new ParameterList();
    paramList.addParameter(displacementSharedParam);
    paramList.addParameter(sharedEventParam);
    displacementParamListEditor = new ParameterListEditor(paramList);
    displacementParamListEditor.setTitle(DISPLACEMENT_TITLE);

    // add the parameter editors to the GUI componenets
    addEditorstoGUI();
  }

  /**
   * Add the parameter editors to the GUI
   */
  private void addEditorstoGUI() {
    this.estimatesSplitPane.add(dateEstParamEditor, JSplitPane.LEFT);
    estimatesSplitPane.add(slipEstParamEditor, JSplitPane.RIGHT);
    this.detailedEventInfoSplitPane.add(displacementParamListEditor, JSplitPane.RIGHT);
    int yPos=0;
    eventSummaryPanel.add(eventNumParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    eventSummaryPanel.add(eventNameParamEditor,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    eventSummaryPanel.add(commentsParamEditor,  new GridBagConstraints(1, 0, 1, 2, 1.0, 1.0
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

  }

  /**
   * add the action listeners to the buttons
   */
  private void addActionListeners() {
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);
  }

  public static void main(String args[]) {
    PerEventInformation eventInfo = new PerEventInformation(5);
  }

  //static initializer for setting look & feel
  static {
    String osName = System.getProperty("os.name");
    try {
      if(osName.startsWith("Mac OS"))
        UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
      else
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
    cancelButton.setActionCommand("jButton2");
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
    mainSplitPane.setDividerLocation(125);
    detailedEventInfoSplitPane.setDividerLocation(WIDTH*2/3);
  }
}