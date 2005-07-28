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
   private final static String CHOOSE_ESTIMATE_PARAM_NAME = "Estimate Type";

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
    * Min/Max  values that can be set into Normal/LogNormal estimate
    * These are used for testing purposes. These  parameters may be removed
    * when we deploy this.
    */
   private DoubleParameter minNormalEstimateParam;
   private final static String NORMAL_MIN_X_PARAM_NAME="Normal Estimate MinX";
   private final static Double DEFAULT_NORMAL_MIN_X_PARAM_VAL=new Double(Double.NEGATIVE_INFINITY);
   private DoubleParameter minLogNormalEstimateParam;
   private final static String LOGNORMAL_MIN_X_PARAM_NAME="LogNormal Estimate MinX";
   private final static Double DEFAULT_LOGNORMAL_MIN_X_PARAM_VAL=new Double(0);
   private final static DoubleConstraint LOGNORMAL_MIN_X_PARAM_CONSTRAINT = new DoubleConstraint(0, Double.POSITIVE_INFINITY);
   private DoubleParameter maxEstimateParam ;
   private final static String ESTIMATE_MAX_X_PARAM_NAME="Estimate MaxX";
   private final static Double DEFAULT_MAX_ESTIMATE_X_PARAM_VAL=new Double(Double.POSITIVE_INFINITY);




   /**
    * Log Base param for log normal distribution
    */
   private StringParameter logBaseParam;
   private final static String LOG_BASE_PARAM_NAME="Log Base";
   private final static String LOG_BASE_10_NAME="10";
   private final static String NATURAL_LOG_NAME="E";

 // for X and Y vlaues for Discrete Value estimate and Min/Max/Preferred Estimate
   private ArbitrarilyDiscretizedFuncParameter arbitrarilyDiscFuncParam;
   private final static String XY_PARAM_NAME = "XY Values";

   private EvenlyDiscretizedFuncParameter evenlyDiscFuncParam;
   private final static String PDF_PARAM_NAME = "PDF Vals";

   //private JButton setEstimateButton ;
   private JButton viewEstimateButton;

   /**
    * Min, Max, Preferred
    */
   private DoubleParameter minX_Param;
   private final static String MIN_X_PARAM_NAME="Min X";
   private final static Double DEFAULT_MIN_X_PARAM_VAL=new Double(1);
   private DoubleParameter maxX_Param ;
   private final static String MAX_X_PARAM_NAME="Max X";
   private final static Double DEFAULT_MAX_X_PARAM_VAL=new Double(10);
   private DoubleParameter prefferedX_Param ;
   private final static String PREF_X_PARAM_NAME="Preffered X";
   private final static Double DEFAULT_PREFERRED_X_PARAM_VAL=new Double(10);
   private DoubleParameter minProbParam;
   private final static String MIN_PROB_PARAM_NAME="Min Prob";
   private final static Double DEFAULT_MIN_PROB_PARAM_VAL=new Double(0.25);
   private DoubleParameter maxProbParam ;
   private final static String MAX_PROB_PARAM_NAME="Max Prob";
   private final static Double DEFAULT_MAX_PROB_PARAM_VAL=new Double(0.25);
   private DoubleParameter prefferedProbParam ;
   private final static String PREF_PROB_PARAM_NAME="Preffered Prob";
   private final static Double DEFAULT_PREFERRED_PROB_PARAM_VAL=new Double(0.5);
   private ParameterListEditor xValsParamListEditor;
   private ParameterListEditor probValsParamListEditor;
   // title of Parameter List Editor
   public static final String X_TITLE = new String("X Values");
   // title of Parameter List Editor
   public static final String PROB_TITLE = new String("Probability");

   private JTextArea estimateInfo;

   private final static String PDF_DISCRETE_ESTIMATE_INFO = "PDF and Discrete Values will be normalized";



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
    add(this.editor,new GridBagConstraints( 0, 0, 2, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    //add(setEstimateButton,new GridBagConstraints( 0, 1, 0, 1, 1.0, 0.0
     //   , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
     add(xValsParamListEditor,new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    add(probValsParamListEditor,new GridBagConstraints( 1, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
     add(viewEstimateButton,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    add(this.estimateInfo,new GridBagConstraints( 0, 3, 2, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    this.setEstimateInfo(PDF_DISCRETE_ESTIMATE_INFO);
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

    arbitrarilyDiscFuncParam = new ArbitrarilyDiscretizedFuncParameter(XY_PARAM_NAME, new ArbitrarilyDiscretizedFunc());
    evenlyDiscFuncParam = new EvenlyDiscretizedFuncParameter(PDF_PARAM_NAME, new EvenlyDiscretizedFunc(1.0,2.0,2));
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
   logBaseParam = new StringParameter(this.LOG_BASE_PARAM_NAME,logBases,(String)logBases.get(0));



   /**
    * Min/Max  values that can be set into Normal/LogNormal estimate
    * These are used for testing purposes. These  parameters may be removed
    * when we deploy this.
    */
   minNormalEstimateParam = new DoubleParameter(NORMAL_MIN_X_PARAM_NAME,
                                                DEFAULT_NORMAL_MIN_X_PARAM_VAL);
   minLogNormalEstimateParam = new DoubleParameter(LOGNORMAL_MIN_X_PARAM_NAME,
       LOGNORMAL_MIN_X_PARAM_CONSTRAINT, DEFAULT_LOGNORMAL_MIN_X_PARAM_VAL);
   maxEstimateParam = new DoubleParameter(ESTIMATE_MAX_X_PARAM_NAME,
                                          DEFAULT_MAX_ESTIMATE_X_PARAM_VAL);

   // put all the parameters in the parameter list
   parameterList = new ParameterList();
   parameterList.addParameter(chooseEstimateParam);
   parameterList.addParameter(this.meanParam);
   parameterList.addParameter(this.stdDevParam);
   parameterList.addParameter(this.linearMedianParam);
   parameterList.addParameter(this.logBaseParam);
   parameterList.addParameter(this.arbitrarilyDiscFuncParam);
   parameterList.addParameter(evenlyDiscFuncParam);
   parameterList.addParameter(minNormalEstimateParam);
   parameterList.addParameter(minLogNormalEstimateParam);
   parameterList.addParameter(maxEstimateParam);
   this.editor = new ParameterListEditor(parameterList);
   editor.setTitle(estimateParam.getName());

   // parameters for min/max/preferred user choice
  minX_Param = new DoubleParameter(MIN_X_PARAM_NAME, DEFAULT_MIN_X_PARAM_VAL);
  maxX_Param = new DoubleParameter(MAX_X_PARAM_NAME, DEFAULT_MAX_X_PARAM_VAL);
  prefferedX_Param = new DoubleParameter(PREF_X_PARAM_NAME, DEFAULT_PREFERRED_X_PARAM_VAL);

  ParameterList xValsParamList = new ParameterList();
  xValsParamList.addParameter(minX_Param);
  xValsParamList.addParameter(maxX_Param);
  xValsParamList.addParameter(prefferedX_Param);
  xValsParamListEditor = new ParameterListEditor(xValsParamList);
  xValsParamListEditor.setTitle(this.X_TITLE);


  minProbParam = new DoubleParameter(MIN_PROB_PARAM_NAME,DEFAULT_MIN_PROB_PARAM_VAL);
  maxProbParam = new DoubleParameter(MAX_PROB_PARAM_NAME,DEFAULT_MAX_PROB_PARAM_VAL);
  prefferedProbParam = new DoubleParameter(PREF_PROB_PARAM_NAME,DEFAULT_PREFERRED_PROB_PARAM_VAL);
  ParameterList probParamList = new ParameterList();
  probParamList.addParameter(minProbParam);
  probParamList.addParameter(maxProbParam);
  probParamList.addParameter(prefferedProbParam);
  probValsParamListEditor = new ParameterListEditor(probParamList);
  probValsParamListEditor.setTitle(this.PROB_TITLE);


   // to view the info for various estimates
   estimateInfo = new JTextArea();
   estimateInfo.setRows(5);
   estimateInfo.setForeground(this.FORE_COLOR);
   estimateInfo.setBackground(this.STRING_BACK_COLOR);
   estimateInfo.setEditable(false);
   //setEstimateButton = new JButton("Set Estimate");
   viewEstimateButton = new JButton("View Estimate");
   //setEstimateButton.addActionListener(this);
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
    else if(estimateName.equalsIgnoreCase(IntegerEstimate.NAME) ||
            estimateName.equalsIgnoreCase(DiscreteValueEstimate.NAME))
      setParamsForXY_Estimate();
    else if(estimateName.equalsIgnoreCase(FractileListEstimate.NAME))
      setParamsForFractileListEstimate();
    else if(estimateName.equalsIgnoreCase(PDF_Estimate.NAME))
      setParamsForPDF_Estimate();
  }

  private void setParamsForFractileListEstimate() {
    editor.setParameterVisible(CHOOSE_ESTIMATE_PARAM_NAME, true);
    editor.setParameterVisible(MEAN_PARAM_NAME, false);
    editor.setParameterVisible(STD_DEV_PARAM_NAME, false);
    editor.setParameterVisible(LINEAR_MEDIAN_PARAM_NAME, false);
    editor.setParameterVisible(LOG_BASE_PARAM_NAME, false);
    editor.setParameterVisible(PDF_PARAM_NAME, false);
    editor.setParameterVisible(XY_PARAM_NAME, false);
    editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, false);
    editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, false);
    editor.setParameterVisible(ESTIMATE_MAX_X_PARAM_NAME, false);
    xValsParamListEditor.setVisible(true);
    probValsParamListEditor.setVisible(true);

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
   editor.setParameterVisible(PDF_PARAM_NAME, false);
   editor.setParameterVisible(XY_PARAM_NAME, false);
   editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, true);
   editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, false);
   editor.setParameterVisible(ESTIMATE_MAX_X_PARAM_NAME, true);
   xValsParamListEditor.setVisible(false);
   probValsParamListEditor.setVisible(false);
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
    editor.setParameterVisible(PDF_PARAM_NAME, false);
    editor.setParameterVisible(XY_PARAM_NAME, false);
    editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, false);
    editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, true);
    editor.setParameterVisible(ESTIMATE_MAX_X_PARAM_NAME, true);
    xValsParamListEditor.setVisible(false);
    probValsParamListEditor.setVisible(false);
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
   editor.setParameterVisible(PDF_PARAM_NAME, true);
   editor.setParameterVisible(XY_PARAM_NAME, false);
   editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, false);
   editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, false);
   editor.setParameterVisible(ESTIMATE_MAX_X_PARAM_NAME, false);
   xValsParamListEditor.setVisible(false);
   probValsParamListEditor.setVisible(false);
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
   editor.setParameterVisible(PDF_PARAM_NAME, false);
   editor.setParameterVisible(XY_PARAM_NAME, true);
   editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, false);
   editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, false);
   editor.setParameterVisible(ESTIMATE_MAX_X_PARAM_NAME, false);
   xValsParamListEditor.setVisible(false);
   probValsParamListEditor.setVisible(false);
 }

 public void actionPerformed(ActionEvent e) {
   if  (e.getSource()==viewEstimateButton) {
     setEstimateInParameter();
     viewEstimate();
   }
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
   double minX = ((Double)this.minNormalEstimateParam.getValue()).doubleValue();
   double maxX = ((Double)this.maxEstimateParam.getValue()).doubleValue();
   NormalEstimate estimate = new NormalEstimate(mean, stdDev);
   this.estimateParam.setValue(estimate);
   estimate.setMinMaxX(minX, maxX);
 }

 private void setLogNormalEstimate() {
   double linearMedian = ((Double)linearMedianParam.getValue()).doubleValue();
   double stdDev = ((Double)stdDevParam.getValue()).doubleValue();
   LogNormalEstimate estimate = new LogNormalEstimate(linearMedian, stdDev);
   if(this.logBaseParam.getValue().equals(this.LOG_BASE_10_NAME))
     estimate.setIsBase10(true);
   else   estimate.setIsBase10(false);
   this.estimateParam.setValue(estimate);
   double minX = ((Double)this.minLogNormalEstimateParam.getValue()).doubleValue();
   double maxX = ((Double)this.maxEstimateParam.getValue()).doubleValue();
   estimate.setMinMaxX(minX, maxX);
 }

 private void setDiscreteValueEstimate() {
   DiscreteValueEstimate estimate = new DiscreteValueEstimate((ArbitrarilyDiscretizedFunc)this.arbitrarilyDiscFuncParam.getValue(), false);
   this.estimateParam.setValue(estimate);
 }

 private void setIntegerEstimate() {
   IntegerEstimate estimate = new IntegerEstimate((ArbitrarilyDiscretizedFunc)this.arbitrarilyDiscFuncParam.getValue(), false);
   this.estimateParam.setValue(estimate);
 }

 private void setPDF_Estimate() {
   PDF_Estimate estimate = new PDF_Estimate((EvenlyDiscretizedFunc)this.evenlyDiscFuncParam.getValue(), false);
   estimateParam.setValue(estimate);
 }

 private void setFractileListEstimate() {
   ArbDiscrEmpiricalDistFunc empiricalFunc = new ArbDiscrEmpiricalDistFunc();
   empiricalFunc.set(((Double)this.minX_Param.getValue()).doubleValue(), ((Double)this.minProbParam.getValue()).doubleValue());
   empiricalFunc.set(((Double)this.maxX_Param.getValue()).doubleValue(), ((Double)this.maxProbParam.getValue()).doubleValue());
   empiricalFunc.set(((Double)this.prefferedX_Param.getValue()).doubleValue(), ((Double)this.prefferedProbParam.getValue()).doubleValue());
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


 private void setEstimateInfo(String info) {
   this.estimateInfo.setEditable(true);
   estimateInfo.setText(info);
   estimateInfo.setEditable(false);
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
    frame.getContentPane().add(estimateParameterEditor, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    frame.pack();
    frame.show();
  }

}