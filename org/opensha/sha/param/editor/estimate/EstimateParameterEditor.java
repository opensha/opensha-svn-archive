package org.opensha.sha.param.editor.estimate;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.border.*;
import java.lang.RuntimeException;

import org.opensha.param.editor.*;
import org.opensha.param.*;
import org.opensha.exceptions.*;
import org.opensha.param.event.*;
import org.opensha.param.estimate.EstimateParameter;
import org.opensha.param.estimate.EstimateConstraint;
import org.opensha.sha.magdist.*;
import org.opensha.data.estimate.*;

/**
 * <p>Title: EstimateParameterEditor.java </p>
 * <p>Description: This is the Estimate Parameter Editor. All estimates listed
 * in the constraint of the EstimateParameter are listed as choices, and below
 * are shown the associated independent parameters than need to be filled in to
 * make the desired estimates.
 </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vipin Gupta, Nitin Gupta
 * @date July 19, 2005
 * @version 1.0
 */

public class EstimateParameterEditor  extends ParameterEditor
    implements ParameterChangeListener,
    ParameterChangeFailListener {

  private EstimateParameter estimateParam;
  // name of the estimate
   private String name;
   /**
    * Paramter List for holding all parameters
    */
   private ParameterList parameterList;

   /**
    * ParameterListEditor for holding parameters
    */
   private ParameterListEditor editor;

    // title of Parameter List Editor
   public static final String ESTIMATE_TITLE = new String("Estimates");

   private StringParameter chooseEstimateParam;
   private final static String CHOOSE_ESTIMATE = "Choose Estimate";

   /**
    * Mean parameter for Normal distributioon
    */
   private DoubleParameter meanParam = new DoubleParameter("Mean");
   /**
    * Std Dev parameter for normal/lognormal distribution
    */
   private DoubleParameter stdDevParam = new DoubleParameter("Std Dev");
   /**
    * Linear Median parameter for lognormal distribution
    */
   private DoubleParameter linearMedianParam = new DoubleParameter("Linear Median");

   /**
    * Log Base param for log normal distribution
    */
   private StringParameter logBaseParam;
   private final static String LOG_BASE_PARAM_NAME="Log Base";
   private final static String LOG_BASE_10_NAME="10";
   private final static String NATURAL_LOG_NAME="E";

   /**
    * Min,max, num for PDF
    */
   private DoubleParameter minParam = new DoubleParameter("Min");
   private DoubleParameter maxParam = new DoubleParameter("Max");
   private DoubleParameter numParam = new DoubleParameter("Num");
   private JTextArea xyVals = new JTextArea();



  public EstimateParameterEditor() {
  }

  //constructor taking the Parameter as the input argument
   public EstimateParameterEditor(ParameterAPI model){
     super(model);
     setParameter(model);
  }

  public void setParameter(ParameterAPI param)  {
    String S = C + ": Constructor(): ";
    if ( D ) System.out.println( S + "Starting:" );
      // remove the previous editor
    //removeAll();
    estimateParam = (EstimateParameter) param;
    // make the params editor
    initParamListAndEditor();
    add(this.editor,new GridBagConstraints( 0, 0, 0, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(this.xyVals,new GridBagConstraints( 0, 1, 0, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    editor = new ParameterListEditor(parameterList);
    editor.setTitle(ESTIMATE_TITLE);

    // All done
    if ( D ) System.out.println( S + "Ending:" );
  }

  /**
   *
   */
  protected void initParamListAndEditor()  {

    // Starting
    String S = C + ": initControlsParamListAndEditor(): ";
    if ( D ) System.out.println( S + "Starting:" );

      // list of available estimates
    chooseEstimateParam = new StringParameter(CHOOSE_ESTIMATE,
                                              ((EstimateConstraint)estimateParam.getConstraint()).getAllowedEstimateList());

    chooseEstimateParam.addParameterChangeListener(this);
    // log choices for log normal distribution
    ArrayList logBases = new ArrayList();
    logBases.add(this.LOG_BASE_10_NAME);
    logBases.add(this.NATURAL_LOG_NAME);
    logBaseParam = new StringParameter(this.LOG_BASE_PARAM_NAME,logBases);

    // put all the parameters in the parameter list
    parameterList = new ParameterList();
    parameterList.addParameter(chooseEstimateParam);
    parameterList.addParameter(this.meanParam);
    parameterList.addParameter(this.stdDevParam);
    parameterList.addParameter(this.linearMedianParam);
    parameterList.addParameter(this.logBaseParam);
    parameterList.addParameter(this.minParam);
    parameterList.addParameter(this.maxParam);
    parameterList.addParameter(this.numParam);
    this.editor = new ParameterListEditor(parameterList);
  }

  public void parameterChangeFailed(ParameterChangeFailEvent event) {
    throw new RuntimeException("Unsupported method");
  }


  /**
   * Make the parameters visible/invisible based on selected estimate
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    // based on user selection of estimates, make the parameters visible/invisible
    if(event.getParameterName().equalsIgnoreCase(CHOOSE_ESTIMATE))
      setEstimateParams((String)chooseEstimateParam.getValue());
  }

  // make the params visible/invisible based on selected estimate type
  private void setEstimateParams(String estimateName) {
    if(estimateName.equalsIgnoreCase(NormalEstimate.NAME))
      setParamsForNormalEstimate();
    else if(estimateName.equalsIgnoreCase(LogNormalEstimate.NAME))
      setParamsForLogNormalEstimate();
    else if(estimateName.equalsIgnoreCase(FractileListEstimate.NAME) ||
            estimateName.equalsIgnoreCase(IntegerEstimate.NAME) ||
            estimateName.equalsIgnoreCase(DiscreteValueEstimate.NAME))
      setParamsForXY_Estimate();
    else if(estimateName.equalsIgnoreCase(PDF_Estimate.NAME))
      setParamsForPDF_Estimate();
  }

  /**
   * Set the params visible for normal estimate
   */
  private void setParamsForNormalEstimate() {
   editor.setParameterVisible(chooseEstimateParam.getName(), true);
   editor.setParameterVisible(meanParam.getName(), true);
   editor.setParameterVisible(stdDevParam.getName(), true);
   editor.setParameterVisible(linearMedianParam.getName(), false);
   editor.setParameterVisible(logBaseParam.getName(), false);
   editor.setParameterVisible(minParam.getName(), false);
   editor.setParameterVisible(maxParam.getName(), false);
   editor.setParameterVisible(numParam.getName(), false);
   this.xyVals.setVisible(false);
  }

  /**
   * Set the params visible for lognormal estimate
   */
  private void setParamsForLogNormalEstimate() {
    editor.setParameterVisible(chooseEstimateParam.getName(), true);
    editor.setParameterVisible(meanParam.getName(), false);
    editor.setParameterVisible(stdDevParam.getName(), true);
    editor.setParameterVisible(linearMedianParam.getName(), true);
    editor.setParameterVisible(logBaseParam.getName(), false);
    editor.setParameterVisible(minParam.getName(), false);
    editor.setParameterVisible(maxParam.getName(), false);
    editor.setParameterVisible(numParam.getName(), false);
    this.xyVals.setVisible(false);
  }

  /**
  * Set the params visible for PDF  estimate
  */
 private void setParamsForPDF_Estimate() {
   editor.setParameterVisible(chooseEstimateParam.getName(), true);
   editor.setParameterVisible(meanParam.getName(), false);
   editor.setParameterVisible(stdDevParam.getName(), false);
   editor.setParameterVisible(linearMedianParam.getName(), false);
   editor.setParameterVisible(logBaseParam.getName(), false);
   editor.setParameterVisible(minParam.getName(), true);
   editor.setParameterVisible(maxParam.getName(), true);
   editor.setParameterVisible(numParam.getName(), true);
   this.xyVals.setVisible(true);
 }

 /**
  * Set the params visible for FractileList, DiscreteValue and Integer  estimate
  */
 private void setParamsForXY_Estimate() {
   editor.setParameterVisible(chooseEstimateParam.getName(), true);
   editor.setParameterVisible(meanParam.getName(), false);
   editor.setParameterVisible(stdDevParam.getName(), false);
   editor.setParameterVisible(linearMedianParam.getName(), false);
   editor.setParameterVisible(logBaseParam.getName(), false);
   editor.setParameterVisible(minParam.getName(), false);
   editor.setParameterVisible(maxParam.getName(), false);
   editor.setParameterVisible(numParam.getName(), false);
   this.xyVals.setVisible(true);
 }


  public static void main(String args[]) {
    JFrame frame = new JFrame();
    frame.getContentPane().setLayout(new GridBagLayout());
    EstimateParameter estimateParam = new org.opensha.param.estimate.DepthEstParameter("Depth");
    EstimateParameterEditor estimateParameterEditor = new EstimateParameterEditor(estimateParam);
    frame.getContentPane().add(estimateParameterEditor, new GridBagConstraints( 0, 0, 0, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    frame.pack();
    frame.show();
  }

}