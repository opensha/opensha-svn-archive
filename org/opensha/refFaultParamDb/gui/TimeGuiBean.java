package org.opensha.refFaultParamDb.gui;
import javax.swing.JPanel;
import org.opensha.param.estimate.*;
import org.opensha.param.editor.*;
import org.opensha.param.editor.estimate.*;
import org.opensha.param.*;
import java.util.ArrayList;
import java.awt.*;
import org.opensha.param.event.*;

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

public class TimeGuiBean extends JPanel implements ParameterChangeListener {
  private final static String TIME_OPTIONS_PARAM_NAME = "Time Available";
  private final static String ESTIMATE = "Estimate";
  private final static String EXACT="Exact";
  private final static String ESTIMATE_PARAMETER_NAME = "Estimate";


  // GUI bean to provide the exact time
  private ExactTimeGuiBean exactTimeGuiBean;

  // various parameters
  private EstimateParameter estimateParameter;
  private StringParameter timeOptionsParam;

  // parameter editors
  private ConstrainedEstimateParameterEditor estimateParamEditor;
  private ConstrainedStringParameterEditor timeOptionsParamEditor;

  public TimeGuiBean() {
    // intialize the parameters and editors
    initParamListAndEditors();
    // add the editors this panel
    addEditorsToPanel();
    setParametersVisible();
  }



  // intialize the various parameters and editors
  private void initParamListAndEditors() {
    // user can choose to provide exact time or a time estimate
    ArrayList availableTimeOptions = getAvailableTimeOptions();
    timeOptionsParam = new StringParameter(TIME_OPTIONS_PARAM_NAME, availableTimeOptions,
                                           (String)availableTimeOptions.get(0));
    timeOptionsParam.addParameterChangeListener(this);
    timeOptionsParamEditor = new ConstrainedStringParameterEditor(timeOptionsParam);
    // GUI bean so that user can provide exact time
    exactTimeGuiBean = new ExactTimeGuiBean();
    // param and editor to allow user to fill the time estimate values
    ArrayList allowedDateEstimates  = EstimateConstraint.createConstraintForDateEstimates();
    estimateParameter = new EstimateParameter(ESTIMATE_PARAMETER_NAME, 0,
                                              Double.MAX_VALUE, allowedDateEstimates);
    estimateParamEditor = new ConstrainedEstimateParameterEditor(estimateParameter,true);
  }


  /**
   * Add the editors the panel
   */
  private void addEditorsToPanel() {
    setLayout(new GridBagLayout());
    int yPos=0;
    add(this.timeOptionsParamEditor,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 0.0
         ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(this.exactTimeGuiBean, new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
         ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(this.estimateParamEditor, new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
         ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
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
  }

  /**
   * Set the parameters for exact time visible/invisible based on user selection
   * @param isVisible
   */
  private void setParametersVisibleForEstimateTime(boolean isVisible) {
    this.estimateParamEditor.setVisible(isVisible);
  }


}