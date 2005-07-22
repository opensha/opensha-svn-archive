package org.opensha.param.editor.estimate;

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
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.sha.gui.infoTools.EstimateViewer;
import org.opensha.param.estimate.*;
import ch.randelshofer.quaqua.QuaquaManager;

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
    ParameterChangeFailListener, ActionListener{

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
   private final static String CHOOSE_ESTIMATE_PARAM_NAME = "Choose Estimate";

   /**
    * Mean parameter for Normal distribution
    */
   private DoubleParameter meanParam;
   private final static String MEAN_PARAM_NAME="Mean";
   private final static Double DEFAULT_MEAN_PARAM_VAL=new Double(5);
   /**
    * Std Dev parameter for normal/lognormal distribution
    */
   private DoubleParameter stdDevParam;
   private final static String STD_DEV_PARAM_NAME="Std Dev";
   private final static Double DEFAULT_STD_DEV_PARAM_VAL=new Double(1);

   /**
    * Linear Median parameter for lognormal distribution
    */
   private DoubleParameter linearMedianParam;
   private final static String LINEAR_MEDIAN_PARAM_NAME="Linear Median";
   private final static Double DEFAULT_LINEAR_MEDIAN_PARAM_VAL=new Double(5);

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
   private DoubleParameter minParam ;
   private final static String MIN_PARAM_NAME="Min";
   private final static Double DEFAULT_MIN_PARAM_VAL=new Double(1);
   private DoubleParameter maxParam ;
   private final static String MAX_PARAM_NAME="Max";
   private final static Double DEFAULT_MAX_PARAM_VAL=new Double(10);
   private IntegerParameter numParam;
   private final static String NUM_PARAM_NAME="Num";
   private final static Integer DEFAULT_NUM_PARAM_VAL=new Integer(10);
   private DiscretizedFuncParameter xyValsParam;
   private final static String XY_PARAM_NAME = "XY Values";
   private JButton setEstimateButton ;
   private JButton viewEstimateButton;

   public EstimateParameterEditor() {
   }

   //constructor taking the Parameter as the input argument
   public EstimateParameterEditor(ParameterAPI model){
     super(model);
   }

  public void setParameter(ParameterAPI param)  {

    String S = C + ": Constructor(): ";
    if ( D ) System.out.println( S + "Starting:" );
      // remove the previous editor
    //removeAll();
    estimateParam = (EstimateParameter) param;
    // make the params editor
    initParamListAndEditor();
    this.setLayout(GBL);
    add(this.editor,new GridBagConstraints( 0, 0, 0, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(setEstimateButton,new GridBagConstraints( 0, 1, 0, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    add(viewEstimateButton,new GridBagConstraints( 0, 2, 0, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    setEstimateParams((String)chooseEstimateParam.getValue());
    this.refreshParamEditor();
    // All done
    if ( D ) System.out.println( S + "Ending:" );
  }

  protected void jbInit() {
     this.setLayout(GBL);
  }

  /**
   * Called when the parameter has changed independently from
   * the editor, such as with the ParameterWarningListener.
   * This function needs to be called to to update
   * the GUI component ( text field, picklist, etc. ) with
   * the new parameter value.
   */
  public void refreshParamEditor() {
    editor.refreshParamEditor();
    this.repaint();
  }


  /**
   *
   */
  protected void initParamListAndEditor()  {

    // Starting
    String S = C + ": initControlsParamListAndEditor(): ";
    if ( D ) System.out.println( S + "Starting:" );


    meanParam = new DoubleParameter(MEAN_PARAM_NAME, DEFAULT_MEAN_PARAM_VAL);
    stdDevParam = new DoubleParameter(STD_DEV_PARAM_NAME, DEFAULT_STD_DEV_PARAM_VAL);
    linearMedianParam = new DoubleParameter(LINEAR_MEDIAN_PARAM_NAME, DEFAULT_LINEAR_MEDIAN_PARAM_VAL);

    minParam = new DoubleParameter(MIN_PARAM_NAME, DEFAULT_MIN_PARAM_VAL);
    maxParam = new DoubleParameter(MAX_PARAM_NAME, DEFAULT_MAX_PARAM_VAL);
    numParam = new IntegerParameter(NUM_PARAM_NAME, DEFAULT_NUM_PARAM_VAL);
    xyValsParam = new DiscretizedFuncParameter(XY_PARAM_NAME);


   // list of available estimates
   ArrayList allowedEstimatesList = ((EstimateConstraint)estimateParam.getConstraint()).getAllowedEstimateList();
   chooseEstimateParam = new StringParameter(CHOOSE_ESTIMATE_PARAM_NAME,
                                             allowedEstimatesList,
                                            (String) allowedEstimatesList.get(0));

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
   parameterList.addParameter(this.xyValsParam);
   this.editor = new ParameterListEditor(parameterList);
   editor.setTitle(ESTIMATE_TITLE);

   setEstimateButton = new JButton("Set Estimate");
   viewEstimateButton = new JButton("View Estimate");
   setEstimateButton.addActionListener(this);
   viewEstimateButton.addActionListener(this);
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
    if(event.getParameterName().equalsIgnoreCase(CHOOSE_ESTIMATE_PARAM_NAME))
      setEstimateParams((String)chooseEstimateParam.getValue());
    this.refreshParamEditor();
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
   editor.setParameterVisible(CHOOSE_ESTIMATE_PARAM_NAME, true);
   editor.setParameterVisible(MEAN_PARAM_NAME, true);
   editor.setParameterVisible(STD_DEV_PARAM_NAME, true);
   editor.setParameterVisible(LINEAR_MEDIAN_PARAM_NAME, false);
   editor.setParameterVisible(LOG_BASE_PARAM_NAME, false);
   editor.setParameterVisible(MIN_PARAM_NAME, false);
   editor.setParameterVisible(MAX_PARAM_NAME, false);
   editor.setParameterVisible(NUM_PARAM_NAME, false);
   editor.setParameterVisible(XY_PARAM_NAME, false);
  }

  /**
   * Set the params visible for lognormal estimate
   */
  private void setParamsForLogNormalEstimate() {
    editor.setParameterVisible(CHOOSE_ESTIMATE_PARAM_NAME, true);
    editor.setParameterVisible(MEAN_PARAM_NAME, false);
    editor.setParameterVisible(STD_DEV_PARAM_NAME, true);
    editor.setParameterVisible(LINEAR_MEDIAN_PARAM_NAME, true);
    editor.setParameterVisible(LOG_BASE_PARAM_NAME, true);
    editor.setParameterVisible(MIN_PARAM_NAME, false);
    editor.setParameterVisible(MAX_PARAM_NAME, false);
    editor.setParameterVisible(NUM_PARAM_NAME, false);
    editor.setParameterVisible(XY_PARAM_NAME, false);

  }

  /**
  * Set the params visible for PDF  estimate
  */
 private void setParamsForPDF_Estimate() {
   editor.setParameterVisible(CHOOSE_ESTIMATE_PARAM_NAME, true);
   editor.setParameterVisible(MEAN_PARAM_NAME, false);
   editor.setParameterVisible(STD_DEV_PARAM_NAME, false);
   editor.setParameterVisible(LINEAR_MEDIAN_PARAM_NAME, false);
   editor.setParameterVisible(LOG_BASE_PARAM_NAME, false);
   editor.setParameterVisible(MIN_PARAM_NAME, true);
   editor.setParameterVisible(MAX_PARAM_NAME, true);
   editor.setParameterVisible(NUM_PARAM_NAME, true);
   editor.setParameterVisible(XY_PARAM_NAME, true);
 }

 /**
  * Set the params visible for FractileList, DiscreteValue and Integer  estimate
  */
 private void setParamsForXY_Estimate() {
   editor.setParameterVisible(CHOOSE_ESTIMATE_PARAM_NAME, true);
   editor.setParameterVisible(MEAN_PARAM_NAME, false);
   editor.setParameterVisible(STD_DEV_PARAM_NAME, false);
   editor.setParameterVisible(LINEAR_MEDIAN_PARAM_NAME, false);
   editor.setParameterVisible(LOG_BASE_PARAM_NAME, false);
   editor.setParameterVisible(MIN_PARAM_NAME, false);
   editor.setParameterVisible(MAX_PARAM_NAME, false);
   editor.setParameterVisible(NUM_PARAM_NAME, false);
   editor.setParameterVisible(XY_PARAM_NAME, true);
 }

 public void actionPerformed(ActionEvent e) {
   if(e.getSource()==setEstimateButton) setEstimateInParameter();
   else if  (e.getSource()==viewEstimateButton) viewEstimate();
 }


 private void viewEstimate() {
   EstimateViewer estimateViewer = new EstimateViewer((Estimate)this.estimateParam.getValue());
 }


 private void setEstimateInParameter() {
   String estimateName=(String)this.chooseEstimateParam.getValue();
   if(estimateName.equalsIgnoreCase(NormalEstimate.NAME))
     setNormalEstimate();
   else if(estimateName.equalsIgnoreCase(LogNormalEstimate.NAME))
     setLogNormalEstimate();
   else if(estimateName.equalsIgnoreCase(FractileListEstimate.NAME))
      setFractileListEstimate();
  else if(estimateName.equalsIgnoreCase(IntegerEstimate.NAME))
      setIntegerEstimate();
  else if(estimateName.equalsIgnoreCase(DiscreteValueEstimate.NAME))
     setDiscreteValueEstimate();
   else if(estimateName.equalsIgnoreCase(PDF_Estimate.NAME))
     setPDF_Estimate();

 }

 private void setNormalEstimate() {
   double mean = ((Double)meanParam.getValue()).doubleValue();
   double stdDev = ((Double)stdDevParam.getValue()).doubleValue();
   NormalEstimate estimate = new NormalEstimate(mean, stdDev);
   this.estimateParam.setValue(estimate);
 }

 private void setLogNormalEstimate() {
   double linearMedian = ((Double)linearMedianParam.getValue()).doubleValue();
   double stdDev = ((Double)stdDevParam.getValue()).doubleValue();
   LogNormalEstimate estimate = new LogNormalEstimate(linearMedian, stdDev);
   this.estimateParam.setValue(estimate);
 }

 private void setDiscreteValueEstimate() {
   DiscreteValueEstimate estimate = new DiscreteValueEstimate((ArbitrarilyDiscretizedFunc)this.xyValsParam.getValue(), false);
   this.estimateParam.setValue(estimate);
 }

 private void setIntegerEstimate() {
   IntegerEstimate estimate = new IntegerEstimate((ArbitrarilyDiscretizedFunc)this.xyValsParam.getValue(), false);
   this.estimateParam.setValue(estimate);
 }

 private void setPDF_Estimate() {
   double min = ((Double)minParam.getValue()).doubleValue();
   double max = ((Double)maxParam.getValue()).doubleValue();
   int num = ((Integer)numParam.getValue()).intValue();
   ArbitrarilyDiscretizedFunc func =(ArbitrarilyDiscretizedFunc)this.xyValsParam.getValue();
   EvenlyDiscretizedFunc evelyDiscretizedFunc = new EvenlyDiscretizedFunc(min,max,num);
   copyFunction(func, evelyDiscretizedFunc);
   PDF_Estimate estimate = new PDF_Estimate(evelyDiscretizedFunc, false);
   estimateParam.setValue(estimate);

 }

 private void setFractileListEstimate() {
   ArbitrarilyDiscretizedFunc func =(ArbitrarilyDiscretizedFunc)this.xyValsParam.getValue();
   ArbDiscrEmpiricalDistFunc empiricalFunc = new ArbDiscrEmpiricalDistFunc();
   copyFunction(func, empiricalFunc);
   FractileListEstimate estimate = new FractileListEstimate(empiricalFunc);
   estimateParam.setValue(estimate);
 }

  private void copyFunction(DiscretizedFunc funcFrom, DiscretizedFunc funcTo) {
    int numVals = funcFrom.getNum();
    for(int i=0; i < numVals; ++i) funcTo.set(funcFrom.getX(i), funcFrom.getY(i));
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
  * test the parameter editor
  * @param args
  */
  public static void main(String args[]) {
    JFrame frame = new JFrame();
    frame.getContentPane().setLayout(new GridBagLayout());
    EstimateParameter estimateParam = new EstimateParameter("Test", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, EstimateConstraint.createConstraintForAllEstimates());
    EstimateParameterEditor estimateParameterEditor = new EstimateParameterEditor(estimateParam);
    frame.getContentPane().add(estimateParameterEditor, new GridBagConstraints( 0, 0, 0, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    frame.pack();
    frame.show();
  }

}