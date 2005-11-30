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
import org.opensha.param.estimate.EstimateParameter;
import org.opensha.param.editor.estimate.ConstrainedEstimateParameterEditor;
import org.opensha.param.estimate.EstimateConstraint;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.opensha.data.estimate.NormalEstimate;
import org.opensha.data.estimate.DiscreteValueEstimate;
import org.opensha.data.estimate.MinMaxPrefEstimate;
import org.opensha.data.estimate.PDF_Estimate;
import org.opensha.data.estimate.Estimate;
import org.opensha.refFaultParamDb.vo.EstimateInstances;

/**
 * <p>Title: SenseOfMotion_MeasuredCompPanel.java </p>
 * <p>Description: this panel can be added to various GUI componenets where
 * we need Sense of Motion Parameters.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SenseOfMotionPanel extends JPanel implements ParameterChangeListener, ActionListener {
  private final static String SOM_PARAM_NAME = "Sense of Motion";
  private final static String SOM_RAKE_PARAM_NAME = "Rake";
  private final static String QUAL_PARAM_NAME = "Qualitative";
  private final static String QUANTITATIVE = "Quantitative (Rake)";
  private final static String QUALITATIVE = "Qualitative";
  private final static String UNKNOWN  = "Unknown";
  private final static String BOTH = "Both";
  private final static double RAKE_MIN = -180.0;
  private final static double RAKE_MAX = 180.0;
  private final static String RAKE_CONVENTION = "We use rake as defined by Aki & Richards, 1980; p.106:\n"+
      "Rake is the angle, in degrees, between the strike and the slip direction, measured contra-clockwise from above.\n"+
      "Rake may take values from [-180,180]:\n"+
      "- rake is 0 for pure left lateral strike slip;\n"+
      "- rake is 180 or -180 for pure right lateral strike slip;\n"+
      "- rake is -90 for pure normal dip slip;\n"+
      "- rake is 90 for pure reverse dip slip;\n"+
      "- rake is in range [0 , 90] for reverse sinistral fault;\n"+
      "- rake is in [90, 180] for reverse dextral faults\n"+
      "- rake is in [-180, -90] for normal dextral faults\n"+
      "- rake is in [-90, 0] for normal sinistral fault\n";


  private StringParameter somParam; // Sense of motion pick list
  private EstimateParameter somRakeEstParam; // sense of motion rake
  private StringParameter somQualParam; //sense of motion qualitative param

  // parameter editors
  private ConstrainedStringParameterEditor somParamEditor;
  private ConstrainedStringParameterEditor somQualParamEditor;
  private ConstrainedEstimateParameterEditor somRakeEstParamEditor;
  private JButton rakeConventionButton = new JButton("Rake Conventions");

  public SenseOfMotionPanel() {
    this.setLayout(GUI_Utils.gridBagLayout);
    initParamListAndEditor();
    addEditorsToGUI();
    rakeConventionButton.addActionListener(this);
    setSOM_RakeParamVisibility();
  }

  private void initParamListAndEditor() {
    try {
      // sense of motion
      ArrayList allowedSOMs = getAllowedSOMs();
      somParam = new StringParameter(SOM_PARAM_NAME, allowedSOMs,
                                     (String) allowedSOMs.get(0));
      somParam.addParameterChangeListener(this);
      somParamEditor = new ConstrainedStringParameterEditor(somParam);
      // qualitative sense of motion
      ArrayList qualitativeSOMs = getAllowedQualitativeSOMs();
      somQualParam = new StringParameter(QUAL_PARAM_NAME, qualitativeSOMs,
                                     (String) qualitativeSOMs.get(0));
      somQualParamEditor = new ConstrainedStringParameterEditor(somQualParam);
      // rake for sense of motion
      ArrayList allowedEstimates = getAllowedEstimatesForRake();
      somRakeEstParam = new EstimateParameter(SOM_RAKE_PARAM_NAME, RAKE_MIN, RAKE_MAX, allowedEstimates);
      somRakeEstParamEditor = new ConstrainedEstimateParameterEditor(somRakeEstParam, true, false, SOM_RAKE_PARAM_NAME);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  private ArrayList getAllowedEstimatesForRake() {
    ArrayList allowedEstimateTypes = new ArrayList();
    allowedEstimateTypes.add(NormalEstimate.NAME);
    allowedEstimateTypes.add(DiscreteValueEstimate.NAME);
    allowedEstimateTypes.add(MinMaxPrefEstimate.NAME);
    allowedEstimateTypes.add(PDF_Estimate.NAME);
    return allowedEstimateTypes;
  }

  private void addEditorsToGUI() {
    int yPos=0;
    this.add(somParamEditor,new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.add(somQualParamEditor,new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.add(rakeConventionButton,new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.add(somRakeEstParamEditor,new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
  }

  private ArrayList getAllowedQualitativeSOMs() {
    ArrayList somQualList = new ArrayList();
    somQualList.add("R");
    somQualList.add("N");
    somQualList.add("RL");
    somQualList.add("LL");
    somQualList.add("RL-N");
    somQualList.add("LL-N");
    somQualList.add("RL-R");
    somQualList.add("LL-R");
    somQualList.add("N-RL");
    somQualList.add("N-LL");
    somQualList.add("R-RL");
    somQualList.add("R-LL");
    return somQualList;

  }
  /**
   * Get the allowed values for Sense of Motion
   * @return
   */
  private ArrayList getAllowedSOMs() {
    ArrayList somList = new ArrayList();
    somList.add(UNKNOWN);
    somList.add(QUALITATIVE);
    somList.add(this.QUANTITATIVE);
    somList.add(BOTH);
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
  }

  /**
   * Allow user to enter rake value if user wishes to.
   * If a qualitative value is choses, show the corresponding rake value
   */
  private void setSOM_RakeParamVisibility() {
    String value = (String)this.somParam.getValue();
    somRakeEstParamEditor.setVisible(false);
    somQualParamEditor.setVisible(false);
    rakeConventionButton.setVisible(false);
    if(value.equalsIgnoreCase(this.QUANTITATIVE) || value.equalsIgnoreCase(BOTH)) {
      this.somRakeEstParamEditor.setVisible(true);
      rakeConventionButton.setVisible(true);
    }
    if(value.equalsIgnoreCase(this.QUALITATIVE) || value.equalsIgnoreCase(BOTH))
      this.somQualParamEditor.setVisible(true);
  }

  /**
   * Get the Sense of Motion rake
   * If it is not rake, Double.Nan is returned
   * @return
   */
  public EstimateInstances getSenseOfMotionRake() {
    String value = (String)this.somParam.getValue();
    if(!value.equalsIgnoreCase(QUANTITATIVE)) return null;
    else {
      this.somRakeEstParamEditor.setEstimateInParameter();
      Estimate rakeEst =  (Estimate)this.somRakeEstParam.getValue();
      return new EstimateInstances(rakeEst,"");
    }
  }

  /**
  * Get the Sense of Motion qualitative value
  * If it Unknown or if rake is provided, null is returned
  * @return
  */
 public String getSenseOfMotionQual() {
   String value = (String)this.somParam.getValue();
   if(value.equalsIgnoreCase(QUANTITATIVE) || value.equalsIgnoreCase(UNKNOWN)) return null;
   return value;
 }

 public void actionPerformed(ActionEvent actionEvent) {
   Object source = actionEvent.getSource();
   if(source==this.rakeConventionButton)
     JOptionPane.showMessageDialog(this, RAKE_CONVENTION);
 }
}