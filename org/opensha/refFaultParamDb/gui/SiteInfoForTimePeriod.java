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
  private final static String REFERENCES_PARAM_NAME="References";

// SLIP RATE
  private final static String SLIP_RATE_PARAM_NAME="Slip Rate Estimate";
  private final static String SLIP_RATE_COMMENTS_PARAM_NAME="Slip Rate Comments";
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
  private final static String CUMULATIVE_DISPLACEMENT_UNITS = "mm/yr";
  private final static double CUMULATIVE_DISPLACEMENT_MIN = 0;
  private final static double CUMULATIVE_DISPLACEMENT_MAX = Double.POSITIVE_INFINITY;

  // Number of events parameter
  private final static String NUM_EVENTS_PARAM_NAME="Number of Events";
  private final static double NUM_EVENTS_MIN=0;
  private final static double NUM_EVENTS_MAX=Integer.MAX_VALUE;

  // various types of information that user can provide
  private final static String SLIP_RATE_INFO = "Slip Rate";
  private final static String CUMULATIVE_DISPLACEMENT_INFO = "Cumulative Displacement";
  private final static String EVENTS_INFO = "Events";
  private final static String SLIP_RATE_AND_EVENTS_INFO = "Slip Rate and Number of Events in same Time Period";
  private final static String CUMULATIVE_DISPLACEMENT_AND_EVENTS_INFO = "Cumulative Displacement and Number of Events in same Time Period";
   private final static String INDIVIDUAL_EVENTS_INFO = "Individual Events";


  private final static String TITLE = "Site Info for Time Period";

  // various parameters for this window
  private StringParameter availableInfoParam;
  private StringParameter datedFeatureCommentsParam;
  private StringParameter referencesParam;
  private EstimateParameter slipRateEstimateParam;
  private EstimateParameter aSeismicSlipFactorParam;
  private StringParameter slipRateCommentsParam;
  private EstimateParameter cumDisplacementParam;
  private StringParameter displacementCommentsParam;
  private EstimateParameter numEventsParam;


  // various parameter Editorts for this window
  private ConstrainedStringParameterEditor availableInfoParamEditor;
  private StringParameterEditor datedFeatureCommentsParamEditor;
  private ConstrainedStringParameterEditor referencesParamEditor;
  private ConstrainedEstimateParameterEditor slipRateEstimateParamEditor;
  private ConstrainedEstimateParameterEditor aSeismicSlipFactorParamEditor;
  private StringParameterEditor slipRateCommentsParamEditor;
  private ConstrainedEstimateParameterEditor cumDisplacementParamEditor;
  private StringParameterEditor displacementCommentsParamEditor;
  private ConstrainedEstimateParameterEditor numEventsParamEditor;


  // various buttons in this window
  private JButton addNewReferenceButton = new JButton("Add Reference");
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");



  public SiteInfoForTimePeriod() {
    try {
      // initialize the parameters and the editors
      initParametersAndEditors();
      // add the editors to this window
      jbInit();
      //add action listeners on the buttons
      addActionListeners();
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
    if(event.getSource()==this.addNewReferenceButton)
       new AddNewReference();
 }


  // add action listeners on the buttons in this window
  private void addActionListeners() {
     this.addNewReferenceButton.addActionListener(this);
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
    // add parameters for events
    addEventsParameters();

  }

  private void jbInit() {
    Container contentPane = this;
    contentPane.setLayout(new GridBagLayout());
    int yPos=0;
    // dated feature comments
    contentPane.add(datedFeatureCommentsParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    // what type of info is provided by the user
    contentPane.add(availableInfoParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
     // references
   contentPane.add(referencesParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
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
   // displacement comments
   contentPane.add(displacementCommentsParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   // num events estimate
   contentPane.add(numEventsParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // ok button
   contentPane.add(okButton,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
   // cancel button
   contentPane.add(cancelButton,  new GridBagConstraints(1, yPos, 1, 1, 1.0, 1.0
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

    // dated feature comments
    datedFeatureCommentsParam = new StringParameter(this.DATED_FEATURE_COMMENTS_PARAM_NAME);
    datedFeatureCommentsParamEditor = new StringParameterEditor(datedFeatureCommentsParam);

    // availablle info for this site
    ArrayList availableReferences = getAvailableReferences();
    referencesParam = new StringParameter(this.REFERENCES_PARAM_NAME, availableReferences,
                                          (String)availableReferences.get(0));
    referencesParamEditor = new ConstrainedStringParameterEditor(referencesParam);
    availableInfoParam.addParameterChangeListener(this);

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
     setParamsVisibleForSlipRateInfo(true);
   }
    // show parameters just for cumulative displacement
   else if(info.equalsIgnoreCase(this.CUMULATIVE_DISPLACEMENT_INFO)) {
     setParamsVisibleForSlipRateInfo(false);
     setParamsVisibleForEventsInfo(false);
     setParamsVisibleForCumDisplacementInfo(true);
   }
   // show parameters for events info only
   else if(info.equalsIgnoreCase(this.EVENTS_INFO)) {
     setParamsVisibleForSlipRateInfo(false);
    setParamsVisibleForCumDisplacementInfo(false);
    setParamsVisibleForEventsInfo(true);
   }
   // show parameters for slip rate and events info
   else if(info.equalsIgnoreCase(this.SLIP_RATE_AND_EVENTS_INFO)) {
     setParamsVisibleForCumDisplacementInfo(false);
     setParamsVisibleForSlipRateInfo(true);
     setParamsVisibleForEventsInfo(true);
   }
   // show parameters for cumulative displacement and event info
   else if(info.equalsIgnoreCase(this.CUMULATIVE_DISPLACEMENT_AND_EVENTS_INFO)) {
     setParamsVisibleForSlipRateInfo(false);
     setParamsVisibleForCumDisplacementInfo(true);
     setParamsVisibleForEventsInfo(true);
   }
 }

 /**
  * Set the parameters visible/invisible when user is providing just Slip Rate info
  */
 private void setParamsVisibleForSlipRateInfo(boolean isVisible) {
   this.slipRateCommentsParamEditor.setVisible(isVisible);
   this.aSeismicSlipFactorParamEditor.setVisible(isVisible);
   this.slipRateEstimateParamEditor.setVisible(isVisible);
 }

 /**
  *  Set the parameters visible/invisible when user is providing just Cumulative
  *  displacement info
  */
 private void setParamsVisibleForCumDisplacementInfo(boolean isVisible) {
   this.cumDisplacementParamEditor.setVisible(isVisible);
   this.aSeismicSlipFactorParamEditor.setVisible(isVisible);
   this.displacementCommentsParamEditor.setVisible(isVisible);
 }

 /**
 *  Set the parameters visible/invisible when user is providing just Events
 *   info
 */
 private void setParamsVisibleForEventsInfo(boolean isVisible) {
   this.numEventsParamEditor.setVisible(isVisible);
 }



 /**
  * Add the input parameters if the user provides the slip rate info
  */
 private void addSlipRateInfoParameters() throws Exception {
  ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
  this.slipRateEstimateParam = new EstimateParameter(this.SLIP_RATE_PARAM_NAME,
       SLIP_RATE_UNITS, SLIP_RATE_MIN, SLIP_RATE_MAX, allowedEstimates);
   slipRateEstimateParamEditor = new ConstrainedEstimateParameterEditor(slipRateEstimateParam);
   this.aSeismicSlipFactorParam = new EstimateParameter(this.ASEISMIC_SLIP_FACTOR_PARAM_NAME,
       ASEISMIC_SLIP_FACTOR_MIN, ASEISMIC_SLIP_FACTOR_MAX, allowedEstimates);
   aSeismicSlipFactorParamEditor = new ConstrainedEstimateParameterEditor(aSeismicSlipFactorParam);
   slipRateCommentsParam = new StringParameter(this.SLIP_RATE_COMMENTS_PARAM_NAME);
   slipRateCommentsParamEditor = new StringParameterEditor(slipRateCommentsParam);
 }

 /**
  * Add the input parameters if user provides the cumulative displacement
  */
 private void addCumulativeDisplacementParameters() throws Exception {
   ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
   this.cumDisplacementParam = new EstimateParameter(this.CUMULATIVE_DISPLACEMENT_PARAM_NAME,
       CUMULATIVE_DISPLACEMENT_UNITS, CUMULATIVE_DISPLACEMENT_MIN, CUMULATIVE_DISPLACEMENT_MAX, allowedEstimates);
   cumDisplacementParamEditor = new ConstrainedEstimateParameterEditor(cumDisplacementParam);
   displacementCommentsParam = new StringParameter(this.CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME);
   displacementCommentsParamEditor = new StringParameterEditor(displacementCommentsParam);
 }

 /**
  * Add the input parameters if user provides the events
  */
 private void addEventsParameters() throws Exception {
   ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveIntValues();
   this.numEventsParam = new EstimateParameter(this.NUM_EVENTS_PARAM_NAME,
                                               NUM_EVENTS_MIN, NUM_EVENTS_MAX, allowedEstimates);
   numEventsParamEditor  = new ConstrainedEstimateParameterEditor(numEventsParam);
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