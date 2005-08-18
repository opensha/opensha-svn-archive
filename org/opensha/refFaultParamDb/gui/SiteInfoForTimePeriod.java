package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import org.opensha.param.editor.*;
import org.opensha.param.editor.estimate.*;
import java.util.ArrayList;
import org.opensha.param.event.*;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;
import java.awt.event.*;

/**
 * <p>Title: SiteInfoForTimePeriod.java </p>
 * <p>Description: This window allows a user to add/view/update the slip/events/displacement
 * date for a particular time period </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SiteInfoForTimePeriod extends JPanel implements ParameterChangeListener,
    ActionListener {

  private final static String AVAILABLE_INFO_PARAM_NAME="I have info on";
  private final static String DATED_FEATURE_COMMENTS_PARAM_NAME="Description of Dated Features";


// SLIP RATE
  private final static String SLIP_RATE_PARAM_NAME="Slip Rate Estimate";
  private final static String SLIP_RATE_COMMENTS_PARAM_NAME="Slip Rate Comments";
  private final static String SLIP_RATE_REFERENCES_PARAM_NAME="Slip Rate References";
  private final static String SLIP_RATE_UNITS = "mm/yr";
  private final static double SLIP_RATE_MIN = 0;
  private final static double SLIP_RATE_MAX = Double.POSITIVE_INFINITY;

  // ASEISMICE SLIP FACTOR
  private final static String ASEISMIC_SLIP_FACTOR_PARAM_NAME="Aseismic Slip Factor Estimate";
  private final static double ASEISMIC_SLIP_FACTOR_MIN=0;
  private final static double ASEISMIC_SLIP_FACTOR_MAX=1;

   // CUMULATIVE DISPLACEMENT
  private final static String CUMULATIVE_DISPLACEMENT_PARAM_NAME="Total Displacement Estimate";
  private final static String CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME="Cumulative Displacement Comments";
  private final static String CUMULATIVE_DISPLACEMENT_REFERENCES_PARAM_NAME="Cumulative Displacement References";
  private final static String CUMULATIVE_DISPLACEMENT_UNITS = "mm/yr";
  private final static double CUMULATIVE_DISPLACEMENT_MIN = 0;
  private final static double CUMULATIVE_DISPLACEMENT_MAX = Double.POSITIVE_INFINITY;

  // Number of events parameter
  private final static String NUM_EVENTS_PARAM_NAME="Number of Events";
  private final static String NUM_EVENTS_REFERENCES_PARAM_NAME="Num of Events References";
  private final static double NUM_EVENTS_MIN=0;
  private final static double NUM_EVENTS_MAX=Integer.MAX_VALUE;

  // number of individual parameter
  private final static String NUM_INDIVIDUAL_EVENTS_PARAM_NAME = "Num Individual Events:";
  private final static String NUM_SEQUENCES_PARAM_NAME = "Num Sequences:";

  // various types of information that user can provide
  private final static String SLIP_RATE_INFO = "Slip Rate";
  private final static String CUMULATIVE_DISPLACEMENT_INFO = "Cumulative Displacement";
  private final static String EVENTS_INFO = "Events";
  private final static String SLIP_RATE_AND_EVENTS_INFO = "Slip Rate and Number of Events in same Time Period";
  private final static String CUMULATIVE_DISPLACEMENT_AND_EVENTS_INFO = "Cumulative Displacement and Number of Events in same Time Period";
  private final static String INDIVIDUAL_EVENTS_INFO = "Individual Events";


  private final static String TITLE = "Site Info for Time Period";
  private final static String NUM_EVENTS_PARAMS_TITLE = "Num Events Params";
  private final static String CUM_DISPLACEMENT_PARAMS_TITLE = "Cumulative Displacement Params";
  private final static String SLIP_RATE_PARAMS_TITLE = "Slip Rate Params";
  private final static String INDIVIDUAL_EVENTS_PARAMS_TITLE="Individual Events Params";

  // various parameters for this window
  private StringParameter availableInfoParam;
  private StringParameter datedFeatureCommentsParam;
  private StringListParameter slipRateReferencesParam;
  private StringListParameter cumDisplacementReferencesParam;
  private StringListParameter numEventsReferencesParam;
  private EstimateParameter slipRateEstimateParam;
  private EstimateParameter aSeismicSlipFactorParam;
  private StringParameter slipRateCommentsParam;
  private EstimateParameter cumDisplacementParam;
  private StringParameter displacementCommentsParam;
  private EstimateParameter numEventsParam;
  private IntegerParameter numIndividualEventsParam;
  private IntegerParameter numSequencesParam;


  // various parameter Editors for this window
  private ConstrainedStringParameterEditor availableInfoParamEditor;
  private StringParameterEditor datedFeatureCommentsParamEditor;
 /* private ConstrainedStringListParameterEditor slipRateReferencesParamEditor;
  private ConstrainedStringListParameterEditor cumDisplacementReferencesParamEditor;
  private ConstrainedStringListParameterEditor numEventsReferencesParamEditor;
  private ConstrainedEstimateParameterEditor slipRateEstimateParamEditor;
  private ConstrainedEstimateParameterEditor aSeismicSlipFactorParamEditor;
  private StringParameterEditor slipRateCommentsParamEditor;
  private ConstrainedEstimateParameterEditor cumDisplacementParamEditor;
  private StringParameterEditor displacementCommentsParamEditor;
  private ConstrainedEstimateParameterEditor numEventsParamEditor;*/

  // parameter List editor
  private ParameterListEditor slipRateParameterListEditor;
  private ParameterListEditor cumDisplacementParameterListEditor;
  private ParameterListEditor numEventsParameterListEditor;
  private ParameterListEditor individualEventsParameterListEditor;


  // various buttons in this window
  private JButton addNewReferenceButton = new JButton("Add Reference");
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");
  private JButton perEventInfoButton = new JButton("Per Event Info");
  private JButton perSequenceInfoButton = new JButton("Per Sequence Info");



  public SiteInfoForTimePeriod() {
    try {
      // initialize the parameters and the editors
      initParametersAndEditors();
      // add the editors to this window
      jbInit();
      //add action listeners on the buttons
      addActionListeners();
      //set the  parameters according to available info
      this.setParameters((String)availableInfoParam.getValue());
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
  * Whenever user presses a button on this window, this function is called
  * @param event
  */
 public void actionPerformed(ActionEvent event) {
   // if it is "Add New Site" request, pop up another window to fill the new site type
    Object source = event.getSource();
    if(source==this.addNewReferenceButton)
       new AddNewReference();
     else if (source == this.perEventInfoButton)
       new PerEventInformation(((Integer)numIndividualEventsParam.getValue()).intValue());
     else if(source == this.perSequenceInfoButton)
       new SequenceInformation(((Integer)numSequencesParam.getValue()).intValue());
 }

  // add action listeners on the buttons in this window
  private void addActionListeners() {
     this.addNewReferenceButton.addActionListener(this);
     this.perEventInfoButton.addActionListener(this);
     this.perSequenceInfoButton.addActionListener(this);
  }

  /**
   * intialize the parameters and the editors
   */
  private void initParametersAndEditors() throws Exception {
    // add common parameters which are needed irrespective of what information is
    // provided by the user.
    addCommonParameters();
    //add parameters for slip rate info
    addSlipRateInfoParameters();
    // add parameters for cumulative displacement
    addCumulativeDisplacementParameters();
    // add parameters for Num Events info
    addNumEventsParameters();
    // add parametrs for individual events info
    this.addIndividualEventsParameters();

  }

  private void jbInit() {
    Container contentPane = this;
    contentPane.setLayout(new GridBagLayout());
    int yPos=0;
    // dated feature comments
    contentPane.add(datedFeatureCommentsParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    // what type of info is provided by the user
    contentPane.add(availableInfoParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
     // references
   /*contentPane.add(slipRateReferencesParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   // add  new reference  button
   contentPane.add(addNewReferenceButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // Slip Rate Estimate
   contentPane.add(slipRateEstimateParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // Aseismic Slip Factor estimate
   contentPane.add(aSeismicSlipFactorParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // Slip Rate Comments
   contentPane.add(slipRateCommentsParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   // Cumulative diplacement
   contentPane.add(cumDisplacementParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // Cumulative diplacement references
   contentPane.add(this.cumDisplacementReferencesParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // displacement comments
   contentPane.add(displacementCommentsParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   // num events estimate
   contentPane.add(numEventsParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // num events references
   contentPane.add(this.numEventsReferencesParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0)); */

   // Slip Rate params
   contentPane.add(this.slipRateParameterListEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   // cum displacement params
   contentPane.add(this.cumDisplacementParameterListEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   // num events params
   contentPane.add(this.numEventsParameterListEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 0.5
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   // individual events params
   contentPane.add(this.individualEventsParameterListEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

   // enter info for individual events
   contentPane.add(this.perEventInfoButton,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 0.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // enter info for sequences
   contentPane.add(this.perSequenceInfoButton,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 0.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

   // add new reference
   contentPane.add(this.addNewReferenceButton,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 0.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    // ok button
   contentPane.add(okButton,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 0.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // cancel button
   contentPane.add(cancelButton,  new GridBagConstraints(1, yPos, 1, 1, 1.0, 0.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
  }

  /**
   * Add common parameters which are needed irrespective of whatever information
   * is provided by the user
   */
  private void addCommonParameters() throws Exception {
    // availablle info for this site
    ArrayList availableInfoList = getAvailableInfoList();
    availableInfoParam = new StringParameter(AVAILABLE_INFO_PARAM_NAME, availableInfoList,
                                             (String)availableInfoList.get(0));
    availableInfoParamEditor = new ConstrainedStringParameterEditor(availableInfoParam);
    availableInfoParam.addParameterChangeListener(this);

    // dated feature comments
    datedFeatureCommentsParam = new StringParameter(this.DATED_FEATURE_COMMENTS_PARAM_NAME);
    datedFeatureCommentsParamEditor = new StringParameterEditor(datedFeatureCommentsParam);

  }


  /**
  *  make the parameters visible/invisible when user selects the info
  *  he can provide
  *
  * @param event
  */
 public void parameterChange(ParameterChangeEvent event) {
   if(event.getParameterName().equalsIgnoreCase(this.AVAILABLE_INFO_PARAM_NAME))
     setParameters((String)availableInfoParam.getValue());
 }

 /**
  * make the parameters visible/invisible when user selects the info
  * he can provide
  *
  * @param info
  */
 private void setParameters(String info) {

   // show parameters for slip rate only
   if(info.equalsIgnoreCase(this.SLIP_RATE_INFO)) {
     setParamsVisibleForCumDisplacementInfo(false);
     setParamsVisibleForEventsInfo(false);
     setParamsVisibleForIndividualEvents(false);
     setParamsVisibleForSlipRateInfo(true);
   }
    // show parameters just for cumulative displacement
   else if(info.equalsIgnoreCase(this.CUMULATIVE_DISPLACEMENT_INFO)) {
     setParamsVisibleForSlipRateInfo(false);
     setParamsVisibleForEventsInfo(false);
     setParamsVisibleForIndividualEvents(false);
     setParamsVisibleForCumDisplacementInfo(true);
   }
   // show parameters for events info only
   else if(info.equalsIgnoreCase(this.EVENTS_INFO)) {
     setParamsVisibleForSlipRateInfo(false);
    setParamsVisibleForCumDisplacementInfo(false);
    setParamsVisibleForIndividualEvents(false);
    setParamsVisibleForEventsInfo(true);
   }
   // show parameters for slip rate and events info
   else if(info.equalsIgnoreCase(this.SLIP_RATE_AND_EVENTS_INFO)) {
     setParamsVisibleForCumDisplacementInfo(false);
     setParamsVisibleForIndividualEvents(false);
     setParamsVisibleForSlipRateInfo(true);
     setParamsVisibleForEventsInfo(true);
   }
   // show parameters for cumulative displacement and event info
   else if(info.equalsIgnoreCase(this.CUMULATIVE_DISPLACEMENT_AND_EVENTS_INFO)) {
     setParamsVisibleForSlipRateInfo(false);
     setParamsVisibleForIndividualEvents(false);
     setParamsVisibleForCumDisplacementInfo(true);
     setParamsVisibleForEventsInfo(true);
   }
   // show parameters for individual events
   else if(info.equalsIgnoreCase(this.INDIVIDUAL_EVENTS_INFO)) {
     setParamsVisibleForSlipRateInfo(false);
     setParamsVisibleForCumDisplacementInfo(false);
     setParamsVisibleForEventsInfo(false);
     setParamsVisibleForIndividualEvents(true);
   }

 }

 /**
  * Set the parameters visible/invisible when user is providing just Slip Rate info
  */
 private void setParamsVisibleForSlipRateInfo(boolean isVisible) {
   this.slipRateParameterListEditor.setVisible(isVisible);
   /*this.slipRateCommentsParamEditor.setVisible(isVisible);
   this.aSeismicSlipFactorParamEditor.setVisible(isVisible);
   this.slipRateEstimateParamEditor.setVisible(isVisible);
   this.slipRateReferencesParamEditor.setVisible(isVisible);*/
 }

 /**
  *  Set the parameters visible/invisible when user is providing just Cumulative
  *  displacement info
  */
 private void setParamsVisibleForCumDisplacementInfo(boolean isVisible) {
   this.cumDisplacementParameterListEditor.setVisible(isVisible);
   /*this.cumDisplacementParamEditor.setVisible(isVisible);
   this.aSeismicSlipFactorParamEditor.setVisible(isVisible);
   this.displacementCommentsParamEditor.setVisible(isVisible);
   this.cumDisplacementReferencesParamEditor.setVisible(isVisible);*/
 }

 /**
 *  Set the parameters visible/invisible when user is providing just Number of Events
 *   info
 */
 private void setParamsVisibleForEventsInfo(boolean isVisible) {
   this.numEventsParameterListEditor.setVisible(isVisible);
   /*this.numEventsParamEditor.setVisible(isVisible);
   this.numEventsReferencesParamEditor.setVisible(isVisible);*/
 }

 /**
  * Set the parameters visible/invisible related to individual events
  *
  * @param isVisible
  */
 private void setParamsVisibleForIndividualEvents(boolean isVisible) {
   this.individualEventsParameterListEditor.setVisible(isVisible);
   this.perEventInfoButton.setVisible(isVisible);
   this.perSequenceInfoButton.setVisible(isVisible);
 }



 /**
  * Add the input parameters if the user provides the slip rate info
  */
 private void addSlipRateInfoParameters() throws Exception {
  ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
  this.slipRateEstimateParam = new EstimateParameter(this.SLIP_RATE_PARAM_NAME,
       SLIP_RATE_UNITS, SLIP_RATE_MIN, SLIP_RATE_MAX, allowedEstimates);
  // slipRateEstimateParamEditor = new ConstrainedEstimateParameterEditor(slipRateEstimateParam);
   this.aSeismicSlipFactorParam = new EstimateParameter(this.ASEISMIC_SLIP_FACTOR_PARAM_NAME,
       ASEISMIC_SLIP_FACTOR_MIN, ASEISMIC_SLIP_FACTOR_MAX, allowedEstimates);
   //aSeismicSlipFactorParamEditor = new ConstrainedEstimateParameterEditor(aSeismicSlipFactorParam);
   // references
   ArrayList availableReferences = getAvailableReferences();
   this.slipRateReferencesParam = new StringListParameter(this.SLIP_RATE_REFERENCES_PARAM_NAME, availableReferences);
   //slipRateReferencesParamEditor = new ConstrainedStringListParameterEditor(slipRateReferencesParam);

   slipRateCommentsParam = new StringParameter(this.SLIP_RATE_COMMENTS_PARAM_NAME);
   //slipRateCommentsParamEditor = new StringParameterEditor(slipRateCommentsParam);
   ParameterList paramList = new ParameterList();
   paramList.addParameter(slipRateEstimateParam);
   paramList.addParameter(aSeismicSlipFactorParam);
   paramList.addParameter(slipRateReferencesParam);
   paramList.addParameter(slipRateCommentsParam);
   slipRateParameterListEditor = new ParameterListEditor(paramList);
   slipRateParameterListEditor.setTitle(this.SLIP_RATE_PARAMS_TITLE);
  }

 /**
  * Add the input parameters if user provides the cumulative displacement
  */
 private void addCumulativeDisplacementParameters() throws Exception {
   ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
   this.cumDisplacementParam = new EstimateParameter(this.CUMULATIVE_DISPLACEMENT_PARAM_NAME,
       CUMULATIVE_DISPLACEMENT_UNITS, CUMULATIVE_DISPLACEMENT_MIN, CUMULATIVE_DISPLACEMENT_MAX, allowedEstimates);
  // cumDisplacementParamEditor = new ConstrainedEstimateParameterEditor(cumDisplacementParam);
   displacementCommentsParam = new StringParameter(this.CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME);
   //displacementCommentsParamEditor = new StringParameterEditor(displacementCommentsParam);
   // references
   ArrayList availableReferences = getAvailableReferences();
   this.cumDisplacementReferencesParam = new StringListParameter(this.CUMULATIVE_DISPLACEMENT_REFERENCES_PARAM_NAME, availableReferences);
   //cumDisplacementReferencesParamEditor = new ConstrainedStringListParameterEditor(cumDisplacementReferencesParam);
   ParameterList paramList = new ParameterList();
   paramList.addParameter(cumDisplacementParam);
   paramList.addParameter(aSeismicSlipFactorParam);
   paramList.addParameter(cumDisplacementReferencesParam);
   paramList.addParameter(displacementCommentsParam);
   this.cumDisplacementParameterListEditor = new ParameterListEditor(paramList);
   cumDisplacementParameterListEditor.setTitle(this.CUM_DISPLACEMENT_PARAMS_TITLE);

 }

 /**
  * Add the input parameters if user provides the events
  */
 private void addNumEventsParameters() throws Exception {
   ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveIntValues();
   this.numEventsParam = new EstimateParameter(this.NUM_EVENTS_PARAM_NAME,
                                               NUM_EVENTS_MIN, NUM_EVENTS_MAX, allowedEstimates);
  // numEventsParamEditor  = new ConstrainedEstimateParameterEditor(numEventsParam);
   // references
  ArrayList availableReferences = getAvailableReferences();
  this.numEventsReferencesParam = new StringListParameter(this.NUM_EVENTS_REFERENCES_PARAM_NAME, availableReferences);
 // numEventsReferencesParamEditor = new ConstrainedStringListParameterEditor(numEventsReferencesParam);
  ParameterList paramList = new ParameterList();
  paramList.addParameter(numEventsParam);
  paramList.addParameter(numEventsReferencesParam);
  this.numEventsParameterListEditor = new ParameterListEditor(paramList);
  numEventsParameterListEditor.setTitle(this.NUM_EVENTS_PARAMS_TITLE);
 }

 /**
  * Add the parameters associated with individual events
  *
  * @throws java.lang.Exception
  */
 private void addIndividualEventsParameters() throws Exception {
   this.numIndividualEventsParam = new IntegerParameter(this.NUM_INDIVIDUAL_EVENTS_PARAM_NAME, 0, Integer.MAX_VALUE, new Integer(1));
   this.numSequencesParam = new IntegerParameter(this.NUM_SEQUENCES_PARAM_NAME, 0, Integer.MAX_VALUE, new Integer(1));
   ParameterList paramList = new ParameterList();
   paramList.addParameter(numIndividualEventsParam);
   paramList.addParameter(numSequencesParam);
   this.individualEventsParameterListEditor = new ParameterListEditor(paramList);
   individualEventsParameterListEditor.setTitle(this.INDIVIDUAL_EVENTS_PARAMS_TITLE);
 }

 /**
  * Get the available information list
  *
  * @return
  */
 private ArrayList getAvailableInfoList() {
    ArrayList availableInfoList = new ArrayList();
    availableInfoList.add(SLIP_RATE_INFO);
    availableInfoList.add(CUMULATIVE_DISPLACEMENT_INFO);
    availableInfoList.add(EVENTS_INFO);
    availableInfoList.add(SLIP_RATE_AND_EVENTS_INFO);
    availableInfoList.add(CUMULATIVE_DISPLACEMENT_AND_EVENTS_INFO);
    availableInfoList.add(this.INDIVIDUAL_EVENTS_INFO);
    return availableInfoList;
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



  public static void main(String[] args) {
    SiteInfoForTimePeriod siteInfoForTimePeriod = new SiteInfoForTimePeriod();
   /* this.setTitle(TITLE);
      this.pack();
      this.show(); */

  }
}