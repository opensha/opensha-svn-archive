package org.opensha.refFaultParamDb.gui;
import javax.swing.JPanel;
import org.opensha.param.estimate.*;
import org.opensha.param.editor.*;
import org.opensha.param.editor.estimate.*;
import org.opensha.param.*;
import java.util.ArrayList;
import java.awt.*;
import org.opensha.param.event.*;
import org.opensha.gui.LabeledBoxPanel;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;

/**
 * <p>Title: TimeGuiBean.java </p>
 *
 * <p>Description: This GUI bean displays the GUI to the user so that
 * usr can enter the time estimate/exact time for a event. This GUI bean
 * can also be used to specify a start/end time for a timespan </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TimeGuiBean extends LabeledBoxPanel implements ParameterChangeListener {
  // parameters for Date Estimate
  private StringParameter yearUnitsParam;
  private final static String YEAR_UNITS_PARAM_NAME="Year Units";
  private final static String CALENDAR_YEAR = "Calendar Year";
  private final static String ZERO_YEAR_PARAM_NAME = "Zero Year";
  private IntegerParameter zeroYearParam;
  private final static String CALENDAR_ERA_PARAM_NAME="Era";
  private final static String AD = "AD";
  private final static String BC = "BC";
  private final static String KA = "ka";
  private final static Integer YEAR1950 = new Integer(1950);
  private StringParameter eraParam;

  // editors
  ConstrainedStringParameterEditor yearUnitsParamEditor;
  IntegerParameterEditor zeroYearParamEditor;
  ConstrainedStringParameterEditor eraParamEditor;

  private final static String TIME_OPTIONS_PARAM_NAME = "Type of Time";
  private final static String ESTIMATE = "Estimate";
  private final static String EXACT="Exact";


  // GUI bean to provide the exact time
  private ExactTimeGuiBean exactTimeGuiBean;

  // various parameters
  private EstimateParameter estimateParameter;
  private StringParameter timeOptionsParam;

  // parameter editors
  private ConstrainedEstimateParameterEditor estimateParamEditor;
  private ConstrainedStringParameterEditor timeOptionsParamEditor;

  public TimeGuiBean(String title) {
    try {
      this.title = title;
      // intialize the parameters and editors
      initParamListAndEditors();
      // add the editors this panel
      addEditorsToPanel();
      setParametersVisible();
      setTitle(title);
      setDateParamsVisibleBasedOnUnits();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }



  // intialize the various parameters and editors
  private void initParamListAndEditors() throws Exception {
    // user can choose to provide exact time or a time estimate
    ArrayList availableTimeOptions = getAvailableTimeOptions();
    timeOptionsParam = new StringParameter(TIME_OPTIONS_PARAM_NAME, availableTimeOptions,
                                           (String)availableTimeOptions.get(0));
    timeOptionsParam.addParameterChangeListener(this);
    timeOptionsParamEditor = new ConstrainedStringParameterEditor(timeOptionsParam);
    // GUI bean so that user can provide exact time
    exactTimeGuiBean = new ExactTimeGuiBean(EXACT+" "+title);
    // param and editor to allow user to fill the time estimate values
    ArrayList allowedDateEstimates  = EstimateConstraint.createConstraintForDateEstimates();
    estimateParameter = new EstimateParameter(ESTIMATE+" "+title, 0,
                                              Double.MAX_VALUE, allowedDateEstimates);
    estimateParamEditor = new ConstrainedEstimateParameterEditor(estimateParameter,true,false);
    /**
    * Parameters for Date Estimate [ isCorrected, units(ka/calendar year),
    *  era, 0th year (in case it is ka)]
    */

   // whether user wants to enter ka or calendar year
   ArrayList yearUnitsList = new ArrayList();
   yearUnitsList.add(CALENDAR_YEAR);
   yearUnitsList.add(KA);
   yearUnitsParam = new StringParameter(YEAR_UNITS_PARAM_NAME,
       yearUnitsList, (String)yearUnitsList.get(0));
   yearUnitsParam.addParameterChangeListener(this);
   yearUnitsParamEditor = new ConstrainedStringParameterEditor(yearUnitsParam);

   // Add ERAs
   ArrayList eras = new ArrayList();
   eras.add(AD);
   eras.add(BC);
   this.eraParam = new StringParameter(this.CALENDAR_ERA_PARAM_NAME, eras, (String)eras.get(0));
   eraParamEditor = new ConstrainedStringParameterEditor(eraParam);

   // ZERO year param
   this.zeroYearParam = new IntegerParameter(this.ZERO_YEAR_PARAM_NAME, 0, Integer.MAX_VALUE, AD, YEAR1950);
   zeroYearParamEditor = new IntegerParameterEditor(zeroYearParam);

  }


  /**
   * Add the editors the panel
   */
  private void addEditorsToPanel() {
    setLayout(GUI_Utils.gridBagLayout);
    int yPos=0;
    add(this.timeOptionsParamEditor,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(this.yearUnitsParamEditor,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(this.eraParamEditor,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(this.zeroYearParamEditor,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(this.exactTimeGuiBean, new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(this.estimateParamEditor, new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
  }

  /**
   * What time options are available to the user. USer can provide an estimate
   * as well as an exact time
   * @return
   */
  private ArrayList getAvailableTimeOptions() {
    ArrayList availableTimes = new ArrayList();
    availableTimes.add(ESTIMATE);
    availableTimes.add(EXACT);
    return availableTimes;
  }

  /**
   * This function is called whenever a parameter changes and this class
   * has registered to listen to that event.
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    String paramName = event.getParameterName();
    if(paramName.equalsIgnoreCase(this.TIME_OPTIONS_PARAM_NAME)) {
      setParametersVisible();
    }else if(event.getParameterName().equalsIgnoreCase(this.YEAR_UNITS_PARAM_NAME)) {
       // change date params based on whether user wants to enter calendar date or ka
       setDateParamsVisibleBasedOnUnits();
    }
  }

  // change date params based on whether user wants to enter calendar date or ka
  private void setDateParamsVisibleBasedOnUnits() {
    String yearUnitsVal = (String)this.yearUnitsParam.getValue();
     if(yearUnitsVal.equalsIgnoreCase(this.CALENDAR_YEAR)) { //if user wants to enter calendar date
       this.zeroYearParamEditor.setVisible(false);
       this.eraParamEditor.setVisible( true);
     }else if(yearUnitsVal.equalsIgnoreCase(KA)) { // if user wants to enter ka years
      this.zeroYearParamEditor.setVisible(true);
       this.eraParamEditor.setVisible(false);
     }
  }


  /**
   * Set the exact/estimate
   * @param isVisible
   */
  private void setParametersVisible() {
    String timeOptionChosen = (String)timeOptionsParam.getValue();
    if(timeOptionChosen.equalsIgnoreCase(this.EXACT)) {
      setParametersVisibleForExactTime(true);
      setParametersVisibleForEstimateTime(false);
    } else if(timeOptionChosen.equalsIgnoreCase(this.ESTIMATE)) {
      setParametersVisibleForEstimateTime(true);
      setParametersVisibleForExactTime(false);
    }
  }

  /**
   * Set the parameters for exact time visible/invisible based on user selection
   * @param isVisible
   */
  private void setParametersVisibleForExactTime(boolean isVisible) {
    this.exactTimeGuiBean.setVisible(isVisible);
    this.yearUnitsParamEditor.setVisible(isVisible);
  }

  /**
   * Set the parameters for exact time visible/invisible based on user selection
   * @param isVisible
   */
  private void setParametersVisibleForEstimateTime(boolean isVisible) {
    this.estimateParamEditor.setVisible(isVisible);
    this.yearUnitsParamEditor.setVisible(isVisible);
  }


}
