package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.JPanel;
import org.opensha.param.StringParameter;
import org.opensha.param.DoubleParameter;
import java.util.ArrayList;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.editor.DoubleParameterEditor;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.param.event.ParameterChangeEvent;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.HashMap;

/**
 * <p>Title: SenseOfMotion_MeasuredCompPanel.java </p>
 * <p>Description: this panel can be added to various GUI componenets where
 * we need Sense of Motion and Measured Component of Slip Parameters.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SenseOfMotion_MeasuredCompPanel extends JPanel implements ParameterChangeListener {
  private final static String SOM_PARAM_NAME = "Sense of Motion";
  private final static String SOM_RAKE_PARAM_NAME = "Rake for Sense of Motion";
  private final static String MEASURED_COMP_PARAM_NAME = "Measured Component";
  private final static String RAKE_MEASURED_COMP_PARAM_NAME = "Rake for Measured Component";
  private final static String RAKE = "Rake";
  private final static String UNKNOWN  = "Unknown";

  private StringParameter somParam; // Sense of motion pick list
  private DoubleParameter somRakeParam; // sense of motion rake
  private StringParameter measuredCompParam; // measured component pick list
  private DoubleParameter measuredCompRakeParam; // measured component rake

  // parameter editors
  private ConstrainedStringParameterEditor somParamEditor;
  private DoubleParameterEditor somRakeParamEditor;
  private ConstrainedStringParameterEditor measuredCompParamEditor;
  private DoubleParameterEditor measuredCompRakeParamEditor;

  public SenseOfMotion_MeasuredCompPanel() {
    this.setLayout(GUI_Utils.gridBagLayout);
    initParamListAndEditor();
    addEditorsToGUI();
    setSOM_RakeParamVisibility();
    this.setMeasuredCompRakeVisibility();
  }

  private void initParamListAndEditor() {
    try {
      // sense of motion
      ArrayList allowedSOMs = getAllowedSOMs();
      somParam = new StringParameter(SOM_PARAM_NAME, allowedSOMs,
                                     (String) allowedSOMs.get(0));
      somParam.addParameterChangeListener(this);
      somParamEditor = new ConstrainedStringParameterEditor(somParam);
      // rake for sense of motion
      somRakeParam = new DoubleParameter(SOM_RAKE_PARAM_NAME);
      somRakeParamEditor = new DoubleParameterEditor(somRakeParam);
      // measured component
      ArrayList allowedMeasuredComps = getAllowedMeasuredComponents();
      measuredCompParam = new StringParameter(MEASURED_COMP_PARAM_NAME,
                                              allowedMeasuredComps,
                                              (String) allowedMeasuredComps.get(
          0));
      measuredCompParam.addParameterChangeListener(this);
      measuredCompParamEditor = new ConstrainedStringParameterEditor(measuredCompParam);
      // rake for measured component
      measuredCompRakeParam = new DoubleParameter(RAKE_MEASURED_COMP_PARAM_NAME);
      measuredCompRakeParamEditor = new DoubleParameterEditor(measuredCompRakeParam);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void addEditorsToGUI() {
    int yPos=0;
    this.add(somParamEditor,new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.add(somRakeParamEditor,new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.add(measuredCompParamEditor,new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.add(measuredCompRakeParamEditor,new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Get the allowed measured components
   * @return
   */
  private ArrayList getAllowedMeasuredComponents() {
    ArrayList measuredComps = new ArrayList();
    measuredComps.add(UNKNOWN);
    measuredComps.add(RAKE);
    measuredComps.add("Vertical");
    measuredComps.add("Horizontal,Trace-Parallel");
    measuredComps.add("Horizontal,Trace-NORMAL");
    return measuredComps;
  }

  /**
   * Get the allowed values for Sense of Motion
   * @return
   */
  private ArrayList getAllowedSOMs() {
    ArrayList somList = new ArrayList();
    somList.add(UNKNOWN);
    somList.add(RAKE);
    somList.add("R");
    somList.add("N");
    somList.add("RL");
    somList.add("LL");
    somList.add("RL-N");
    somList.add("LL-N");
    somList.add("RL-R");
    somList.add("LL-R");
    somList.add("N-RL");
    somList.add("N-LL");
    somList.add("R-RL");
    somList.add("R-LL");
    return somList;
  }

  /**
   * Called when a parameter changes and this class is listening to that parameter
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    String paramName = event.getParameterName();
    // if user selects rake for sense of motion, then show the parameter so that  user can type rake value
    if(paramName.equalsIgnoreCase(this.SOM_PARAM_NAME)) setSOM_RakeParamVisibility();
    else if(paramName.equalsIgnoreCase(MEASURED_COMP_PARAM_NAME)) setMeasuredCompRakeVisibility();
  }

  /**
   * Allow user to enter rake value if user wishes to.
   * If a qualitative value is choses, show the corresponding rake value
   */
  private void setSOM_RakeParamVisibility() {
    String value = (String)this.somParam.getValue();
    if(value.equalsIgnoreCase(this.RAKE)) this.somRakeParamEditor.setVisible(true);
    else somRakeParamEditor.setVisible(false);
  }

  /**
   * Allow user to enter rake value if user wishes to.
   * If a qualitative value is choses, show the corresponding rake value
   */
  private void setMeasuredCompRakeVisibility() {
    String value = (String)this.measuredCompParam.getValue();
    if(value.equalsIgnoreCase(this.RAKE)) this.measuredCompRakeParamEditor.setVisible(true);
    else measuredCompRakeParamEditor.setVisible(false);
  }

  /**
   * Get the measured component rake
   * If it not Rake, Double.Nan is returned
   * @return
   */
  public double getMeasuredCompRake() {
    String value = (String)this.measuredCompParam.getValue();
    if(!value.equalsIgnoreCase(RAKE)) return Double.NaN;
    else return ((Double)this.measuredCompRakeParam.getValue()).doubleValue();
  }

  /**
  * Get the measured component qualitative value
  * If it Unknown or if rake is provided, null is returned
  * @return
  */
 public String getMeasuredCompQual() {
   String value = (String)this.measuredCompParam.getValue();
   if(value.equalsIgnoreCase(RAKE) || value.equalsIgnoreCase(UNKNOWN)) return null;
   return value;
 }


  /**
   * Get the Sense of Motion rake
   * If it is not rake, Double.Nan is returned
   * @return
   */
  public double getSenseOfMotionRake() {
    String value = (String)this.somParam.getValue();
    if(!value.equalsIgnoreCase(RAKE)) return Double.NaN;
    else return ((Double)this.somRakeParam.getValue()).doubleValue();
  }

  /**
  * Get the Sense of Motion qualitative value
  * If it Unknown or if rake is provided, null is returned
  * @return
  */
 public String getSenseOfMotionQual() {
   String value = (String)this.somParam.getValue();
   if(value.equalsIgnoreCase(RAKE) || value.equalsIgnoreCase(UNKNOWN)) return null;
   return value;
 }
}