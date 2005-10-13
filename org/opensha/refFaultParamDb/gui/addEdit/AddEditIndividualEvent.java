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
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.PaleoEventDB_DAO;
import org.opensha.refFaultParamDb.vo.PaleoEvent;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.data.estimate.Estimate;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.gui.event.DbAdditionFrame;
import org.opensha.refFaultParamDb.gui.event.DbAdditionListener;
import org.opensha.exceptions.*;
import org.opensha.refFaultParamDb.gui.event.DbAdditionSuccessEvent;

/**
 * <p>Title: AddEditIndividualEvent.java </p>
 * <p>Description: This GUI allows to add an event information: Event name,
 * event date estimate, slip estimate, whether diplacement shared with other events, references, comments </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditIndividualEvent extends DbAdditionFrame implements ParameterChangeListener,
    ActionListener, DbAdditionListener {
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
  private final static String EVENT_NAME_PARAM_DEFAULT = "Enter Event Name";
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
  private final static String MSG_NO_EVENT_EXIST_TO_SHARE_DISPLACEMENT =
      "No other event exists in database for this site. So, displacement cannot be shared";
  private final static String MSG_EVENT_NAME_MISSING = "Please enter event name";
  private final static String MSG_REFERENCE_MISSING = "Choose atleast 1 reference";
  private final static String MSG_SHARED_EVENTS_MISSING = "Choose atleast 1 event to share the displacement";
  private final static String MSG_EVENTS_DO_NOT_SHARE_DISPLACEMENT=
      "The selected event set for shared displacement is invalid.\nThese events do not share same displacement";
  private final static String MSG_PALEO_EVENT_ADD_SUCCESS = "Paleo Event added successfully to the database";
//slip rate constants
  private final static String SLIP_RATE_UNITS = "meters";
  private final static double SLIP_RATE_MIN = 0;
  private final static double SLIP_RATE_MAX = Double.POSITIVE_INFINITY;

  // diplacement parameter list editor title
  private final static String DISPLACEMENT_TITLE = "Shared Slip";
  private final static String TITLE = "Add Event";

  // various parameter types
  private StringParameter eventNameParam;
  private StringParameter commentsParam;
  private TimeGuiBean eventTimeEst = new TimeGuiBean(DATE_ESTIMATE_PARAM_NAME);
  private EstimateParameter slipEstParam;
  private BooleanParameter displacementSharedParam;
  private StringListParameter sharedEventParam;
  private StringListParameter referencesParam;

  // various parameter editors
  private StringParameterEditor eventNameParamEditor;
  private CommentsParameterEditor commentsParamEditor;
  private ConstrainedEstimateParameterEditor slipEstParamEditor;
  private ParameterListEditor displacementParamListEditor;
  private ConstrainedStringListParameterEditor referencesParamEditor;

  private final static int WIDTH = 600;
  private final static int HEIGHT = 700;

  // references DAO
  private ReferenceDB_DAO referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
  // paleo event DAO
  private PaleoEventDB_DAO paleoEventDAO = new PaleoEventDB_DAO(DB_AccessAPI.dbConnection);
  private ArrayList paleoEvents; // saves a list of all paleo events for this site
  private int siteId; // site id for which this paleo event will be added
  private String siteEntryDate; // site entry dat for which paleo event is to be added
  private AddNewReference addNewReference;
  private LabeledBoxPanel commentsReferencesPanel;
  public AddEditIndividualEvent(int siteId, String siteEntryDate) {
    try {
      this.siteId = siteId;
      this.siteEntryDate = siteEntryDate;
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
    eventNameParam = new StringParameter(this.EVENT_NAME_PARAM_NAME, EVENT_NAME_PARAM_DEFAULT);
    eventNameParamEditor = new StringParameterEditor(eventNameParam);

    // comments param
    commentsParam = new StringParameter(this.COMMENTS_PARAM_NAME);
    commentsParamEditor = new CommentsParameterEditor(commentsParam);

    // date param
    ArrayList dateAllowedEstList = EstimateConstraint.createConstraintForDateEstimates();

    // slip rate param
    ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
    this.slipEstParam = new EstimateParameter(this.SLIP_ESTIMATE_PARAM_NAME,
      SLIP_RATE_UNITS, SLIP_RATE_MIN, SLIP_RATE_MAX, allowedEstimates);
    slipEstParamEditor = new ConstrainedEstimateParameterEditor(slipEstParam, true,false);

    // whether displacement is shared with other events
    this.displacementSharedParam = new BooleanParameter(this.DISPLACEMENT_SHARED_PARAM_NAME, new Boolean(false));
    displacementSharedParam.addParameterChangeListener(this);
    ParameterList paramList  = new ParameterList();
    paramList.addParameter(displacementSharedParam);

    // event name parameter with which dispalcement is shared(only if displacement is shared)
    ArrayList eventNamesList = getEventNamesList();
    if(eventNamesList!=null && eventNamesList.size()>0) {
      this.sharedEventParam = new StringListParameter(SHARED_EVENT_PARAM_NAME,
          eventNamesList);
      paramList.addParameter(sharedEventParam);
    }
    displacementParamListEditor = new ParameterListEditor(paramList);
    displacementParamListEditor.setTitle(DISPLACEMENT_TITLE);

    // add the parameter editors to the GUI componenets
    addEditorstoGUI();

    makeReferencesParamAndEditor();
  }

  private void makeReferencesParamAndEditor() throws ConstraintException {
    if(referencesParamEditor!=null) commentsReferencesPanel.remove(referencesParamEditor);
    // references param
    referencesParam = new StringListParameter(this.REFERENCES_PARAM_NAME, this.getAvailableReferences());
    referencesParamEditor = new ConstrainedStringListParameterEditor(referencesParam);
    commentsReferencesPanel.add(this.referencesParamEditor,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
  }


  /**
  * Get a list of available references.
  * @return
  */
 private ArrayList getAvailableReferences() {
   return referenceDAO.getAllShortCitations();
 }


  /**
   * Get a list of all the event names
   * @return
   */
  private ArrayList getEventNamesList() {
    paleoEvents = paleoEventDAO.getAllEvents(siteId);
    ArrayList eventNames = new ArrayList();
    for(int i=0; i<paleoEvents.size(); ++i) {
      eventNames.add(((PaleoEvent)paleoEvents.get(i)).getEventName());
    }
    return eventNames;
  }

  /**
   * Add the parameter editors to the GUI
   */
  private void addEditorstoGUI() {

    // event time estimate
    this.estimatesSplitPane.add(eventTimeEst, JSplitPane.LEFT);

    // event slip and whether slip is shared
    LabeledBoxPanel slipPanel = new LabeledBoxPanel(gridBagLayout1);
    slipPanel.setTitle(SLIP_TITLE);
    slipPanel.add(slipEstParamEditor,  new GridBagConstraints(0, 0, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    slipPanel.add(displacementParamListEditor,  new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    estimatesSplitPane.add(slipPanel, JSplitPane.RIGHT);

    // comments and references
    commentsReferencesPanel = new LabeledBoxPanel(gridBagLayout1);
    commentsReferencesPanel.setTitle(COMMENTS_REFERENCES_TITLE);
    this.detailedEventInfoSplitPane.add(commentsReferencesPanel, JSplitPane.RIGHT);
    commentsReferencesPanel.add(this.commentsParamEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
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
    if(this.paleoEvents!=null && paleoEvents.size()>0)
      this.displacementParamListEditor.setParameterVisible(this.SHARED_EVENT_PARAM_NAME, isVisible);
    else if(isVisible) {
      displacementSharedParam.removeParameterChangeListener(this);
      this.displacementSharedParam.setValue(new Boolean(false));
      this.displacementParamListEditor.getParameterEditor(DISPLACEMENT_SHARED_PARAM_NAME).refreshParamEditor();
      displacementSharedParam.addParameterChangeListener(this);
      JOptionPane.showMessageDialog(this,MSG_NO_EVENT_EXIST_TO_SHARE_DISPLACEMENT);
    }
  }

  /**
   * This function is called when a button is clicked on this screen
   *
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource() ;
    if(source == addNewReferenceButton)  {
      addNewReference  = new AddNewReference();
      addNewReference.addDbAdditionSuccessListener(this);
    }
    else if(source == okButton) {
      try {
        addEventToDatabase();
      }catch(InsertException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
      }
    }
    else if(source == cancelButton) this.dispose();
  }

  /**
   * Add event to the database
   */
  private void addEventToDatabase() {
    PaleoEvent paleoEvent = new PaleoEvent();
    // make sure that user entered event name
    String eventName = (String)this.eventNameParam.getValue();
    if(eventName.trim().equalsIgnoreCase("") ||
       eventName.trim().equalsIgnoreCase(this.EVENT_NAME_PARAM_DEFAULT)) {
      JOptionPane.showMessageDialog(this, MSG_EVENT_NAME_MISSING);
      return;
    }
    paleoEvent.setEventName(eventName);
    // make sure that user choose a reference
    ArrayList reference = (ArrayList)this.referencesParam.getValue();
    if(reference==null || reference.size()==0) {
      JOptionPane.showMessageDialog(this, MSG_REFERENCE_MISSING);
      return;
    }
    paleoEvent.setShortCitationsList(reference);

    // if displacement is shared, make sure that user selects atleast 1 event
    boolean isDispShared = ((Boolean)this.displacementSharedParam.getValue()).booleanValue();
    ArrayList sharedEventNames=null;
    paleoEvent.setDisplacementShared(isDispShared);
    if(isDispShared) {
      sharedEventNames = (ArrayList)this.sharedEventParam.getValue();
      if(sharedEventNames==null || sharedEventNames.size()==0) {
        JOptionPane.showMessageDialog(this, MSG_SHARED_EVENTS_MISSING);
        return;
      } // now check that user has selected valid events to share displacement
      else{
        int dispEstId = paleoEventDAO.checkSameDisplacement(sharedEventNames);
        if(dispEstId<=0) {
          JOptionPane.showMessageDialog(this,
                                        MSG_EVENTS_DO_NOT_SHARE_DISPLACEMENT);
          return;
        } else paleoEvent.setDisplacementEstId(dispEstId);
      }
    } else { // if displacement is not shared, set displacement estimate in the paleo-event
      this.slipEstParamEditor.setEstimateInParameter();
      paleoEvent.setDisplacementEst(
          new EstimateInstances((Estimate)this.slipEstParam.getValue(), this.SLIP_RATE_UNITS));
    }
    // set other properties of the paleo event
    paleoEvent.setComments((String)this.commentsParam.getValue());
    paleoEvent.setSiteId(this.siteId);
    paleoEvent.setSiteEntryDate(this.siteEntryDate);
    TimeAPI eventTime = this.eventTimeEst.getSelectedTime();
    eventTime.setDatingComments(paleoEvent.getComments());
    eventTime.setReferencesList(paleoEvent.getShortCitationsList());
    paleoEvent.setEventTime(eventTime);
    this.paleoEventDAO.addPaleoevent(paleoEvent);
    JOptionPane.showMessageDialog(this, MSG_PALEO_EVENT_ADD_SUCCESS);
    this.sendEventToListeners(paleoEvent);
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

  public void dbAdditionSuccessful(DbAdditionSuccessEvent event) {
    Object source = event.getSource();
    if(source == this.addNewReference) {
      makeReferencesParamAndEditor();
      this.commentsReferencesPanel.updateUI();
    }
  }
}