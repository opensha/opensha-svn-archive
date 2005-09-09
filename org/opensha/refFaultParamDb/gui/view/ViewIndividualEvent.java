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
import org.opensha.refFaultParamDb.data.TimeEstimate;
import org.opensha.data.estimate.NormalEstimate;
import org.opensha.data.estimate.LogNormalEstimate;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.data.estimate.Estimate;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;

/**
 * <p>Title: AddEditIndividualEvent.java </p>
 * <p>Description: This GUI allows to view an event information: Event name,
 * event date estimate, slip estimate, whether diplacement shared with other events, references, comments </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewIndividualEvent extends LabeledBoxPanel implements ParameterChangeListener,
    ActionListener {
  private JButton closeButton = new JButton("Close");
  private JButton editButton  = new JButton("Edit Event");
  private JButton addButton  = new JButton("Add New Event");

  // various parameter names
  private final static String EVENT_NAME_PARAM_NAME = "Event Name";
  private final static String COMMENTS_PARAM_NAME = "Comments";
  private final static String REFERENCES_PARAM_NAME = "References";
  private final static String TIME_ESTIMATE_PARAM_NAME = "Event Time Estimate";
  private final static String SLIP_ESTIMATE_PARAM_NAME = "Event Slip Estimate";
  private final static String DISPLACEMENT_SHARED_PARAM_NAME = "Slip Shared With Other Events";
  private final static String SHARED_EVENT_PARAM_NAME = "Names of Events Sharing Slip";
  private final static String TITLE = "Individual Events";

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


  public ViewIndividualEvent() {
    try {
      this.setLayout(GUI_Utils.gridBagLayout);
      // add Parameters and editors
      initParamsAndEditors();
      // add the action listeners to the button
      addActionListeners();
      // set event info according to selected event
      this.setEventInfo((String)eventNameParam.getValue());
      // set the title
      this.setTitle(TITLE);
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
    eventNameParam.addParameterChangeListener(this);
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
   * Add all the event information to theGUI
   */
  private void addEditorstoGUI() {
    int yPos=0;

    add(eventNameParamEditor ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(GUI_Utils.getPanel(timeEstLabel,TIME_ESTIMATE_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(GUI_Utils.getPanel(slipEstLabel,SLIP_ESTIMATE_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(GUI_Utils.getPanel(displacementSharedLabel,DISPLACEMENT_SHARED_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(GUI_Utils.getPanel(sharedEventLabel,SHARED_EVENT_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(GUI_Utils.getPanel(commentsLabel,COMMENTS_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    add(GUI_Utils.getPanel(referencesLabel,REFERENCES_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
  }


  /**
   * This function is called whenever a paramter is changed and we have
   * registered as listeners to that parameters
   *
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    this.setEventInfo((String)eventNameParam.getValue());
  }

  /**
   * Show the info according to event selected by the user
   *
   * @param eventName
   */
  private void setEventInfo(String eventName) {
    // just set some fake implementation right now
    // event time estimate
    TimeEstimate startTime = new TimeEstimate();
    startTime.setForKaUnits(new NormalEstimate(1000, 50), 1950);
    // comments
    String comments = "Comments about this event";
    // references
    ArrayList references = new ArrayList();
    references.add("Ref 4");
    references.add("Ref 1");
    // Slip Rate Estimate
    LogNormalEstimate slipRateEstimate = new LogNormalEstimate(1.5, 0.25);
    // displacement is shared or not
    String displacement = "Shared";
    // events with which displacement is shared
    ArrayList eventsList = new ArrayList();
    eventsList.add("Event 10");
    eventsList.add("Event 11");
    updateLabels(startTime, slipRateEstimate, comments, references, displacement,
                 eventsList);
  }

  /**
   * Update the labels to view information about the events
   * @param eventTime
   * @param slipEstimate
   * @param comments
   * @param references
   * @param displacement
   * @param sharingEvents
   */
  private void updateLabels(TimeAPI eventTime, Estimate slipEstimate, String comments,
                            ArrayList references, String displacement, ArrayList sharingEvents) {
    commentsLabel.setTextAsHTML(comments);
    timeEstLabel.setTextAsHTML(eventTime);
    slipEstLabel.setTextAsHTML(slipEstimate);
    displacementSharedLabel.setTextAsHTML(displacement);
    sharedEventLabel.setTextAsHTML(sharingEvents);
    referencesLabel.setTextAsHTML(references);

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



}
