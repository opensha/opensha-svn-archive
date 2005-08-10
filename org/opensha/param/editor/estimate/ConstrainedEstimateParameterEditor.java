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
import org.opensha.data.estimate.date.*;
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

public class ConstrainedEstimateParameterEditor  extends ParameterEditor
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
   private DoubleParameter maxNormalEstimateParam ;
   private final static String NORMAL_MAX_X_PARAM_NAME="Normal Estimate MaxX";
   private final static Double DEFAULT_NORMAL_MAX_X_PARAM_VAL=new Double(Double.POSITIVE_INFINITY);
   private DoubleParameter maxLogNormalEstimateParam ;
   private final static String LOGNORMAL_MAX_X_PARAM_NAME="LogNormal Estimate MaxX";
   private final static Double DEFAULT_LOGNORMAL_MAX_X_PARAM_VAL=new Double(Double.POSITIVE_INFINITY);

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

   // parameters for Date Estimate
   private StringParameter datingTechParam;
   private final static String DATING_TECH_PARAM_NAME="Dating Technique";
   private BooleanParameter isDateCorrectedParam;
   private final static String IS_DATE_CORRECTED_PARAM_NAME="Is Date Corrected";
   private StringParameter yearUnitsParam;
   private final static String YEAR_UNITS_PARAM_NAME="Year Units";
   private StringParameter yearBeforeParam;
   private final static String YEAR_BEFORE_PARAM_NAME="Years Before";

   // title of Parameter List Editor
   public static final String X_TITLE = new String("X Values");
   // title of Parameter List Editor
   public static final String PROB_TITLE = new String("Probability");

   private JTextArea estimateInfo;

   private final static String PDF_DISCRETE_ESTIMATE_INFO = "PDF and Discrete Values will be normalized";
   //private JLabel minConstraintLabel;
   private final static String MIN_CONSTRAINT_LABEL="Min Value:";
  // private JLabel maxConstraintLabel;
   private final static String MAX_CONSTRAINT_LABEL="Max Value:";

   /* this editor will be shown only as a button. On button click, a new window
    appears showing all the parameters */
   private JButton button;

   private JFrame frame;


   public ConstrainedEstimateParameterEditor() {
   }

   //constructor taking the Parameter as the input argument
   public ConstrainedEstimateParameterEditor(ParameterAPI model){
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
    button = new JButton(this.estimateParam.getName());
    button.addActionListener(this);
    this.add(this.button,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

    // add editor to the frame. This frame is visible when user clicks on button created above
    frame = new JFrame();
    frame.setTitle(this.estimateParam.getName());
    frame.getContentPane().setLayout(GBL);
    frame.getContentPane().add(this.editor,new GridBagConstraints( 0, 0, 2, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    frame.getContentPane().add(xValsParamListEditor,new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    frame.getContentPane().add(probValsParamListEditor,new GridBagConstraints( 1, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    frame.getContentPane().add(viewEstimateButton,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    frame.getContentPane().add(this.estimateInfo,new GridBagConstraints( 0, 3, 2, 1, 1.0, 0.0
                                                  , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    this.setEstimateInfo(editor.getToolTipText()+"\n"+PDF_DISCRETE_ESTIMATE_INFO);
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
    frame.repaint();
  }

  /**
   * Initialize the parameters and editors
   */
  protected void initParamListAndEditor()  {

    // Starting
    String S = C + ": initControlsParamListAndEditor(): ";
    if ( D ) System.out.println( S + "Starting:" );


    meanParam = new DoubleParameter(MEAN_PARAM_NAME, DEFAULT_MEAN_PARAM_VAL);
    stdDevParam = new DoubleParameter(STD_DEV_PARAM_NAME, DEFAULT_STD_DEV_PARAM_VAL);
    linearMedianParam = new DoubleParameter(LINEAR_MEDIAN_PARAM_NAME, DEFAULT_LINEAR_MEDIAN_PARAM_VAL);

    arbitrarilyDiscFuncParam = new ArbitrarilyDiscretizedFuncParameter(XY_PARAM_NAME, new ArbitrarilyDiscretizedFunc());
    evenlyDiscFuncParam = new EvenlyDiscretizedFuncParameter(PDF_PARAM_NAME, new EvenlyDiscretizedFunc(1.0,4.0,7));
    // list of available estimates
    EstimateConstraint estimateConstraint = (EstimateConstraint)estimateParam.getConstraint();
    ArrayList allowedEstimatesList = estimateConstraint.getAllowedEstimateList();
    chooseEstimateParam = new StringParameter(CHOOSE_ESTIMATE_PARAM_NAME,
                                              allowedEstimatesList,
                                              (String) allowedEstimatesList.get(0));

    chooseEstimateParam.addParameterChangeListener(this);

    // log choices for log normal distribution
    ArrayList logBases = new ArrayList();
    logBases.add(this.NATURAL_LOG_NAME);
    logBases.add(this.LOG_BASE_10_NAME);
    logBaseParam = new StringParameter(this.LOG_BASE_PARAM_NAME,logBases,(String)logBases.get(0));


    /**
     * Parameters for Date Estimate [dating technique, isCorrected, units(ka/ma),
     *  years before today/1950]
     */
    ArrayList datingTechniques = new ArrayList();
    datingTechniques.add(DateEstimateAPI.CARBON14_TECHNIQUE);
    datingTechParam = new StringParameter(DATING_TECH_PARAM_NAME,
        datingTechniques, (String)datingTechniques.get(0));

    isDateCorrectedParam = new BooleanParameter(IS_DATE_CORRECTED_PARAM_NAME);

    ArrayList yearUnitsList = new ArrayList();
    yearUnitsList.add(DateEstimateAPI.KA);
    yearUnitsList.add(DateEstimateAPI.MA);
    yearUnitsParam = new StringParameter(YEAR_UNITS_PARAM_NAME,
        yearUnitsList, (String)yearUnitsList.get(0));

    ArrayList yearBeforeList = new ArrayList();
    yearBeforeList.add(DateEstimateAPI.YEAR1950);
    yearBeforeList.add(DateEstimateAPI.TODAY);
    yearBeforeParam = new StringParameter(YEAR_BEFORE_PARAM_NAME,
        yearBeforeList, (String)yearBeforeList.get(0));

   /**
    * Min/Max  values that can be set into Normal/LogNormal estimate
    * These are used for testing purposes. These  parameters may be removed
    * when we deploy this.
    */
   minNormalEstimateParam = new DoubleParameter(NORMAL_MIN_X_PARAM_NAME,
                                                DEFAULT_NORMAL_MIN_X_PARAM_VAL);
   minLogNormalEstimateParam = new DoubleParameter(LOGNORMAL_MIN_X_PARAM_NAME,
       LOGNORMAL_MIN_X_PARAM_CONSTRAINT, DEFAULT_LOGNORMAL_MIN_X_PARAM_VAL);
   maxNormalEstimateParam = new DoubleParameter(NORMAL_MAX_X_PARAM_NAME,
                                          DEFAULT_NORMAL_MAX_X_PARAM_VAL);
    maxLogNormalEstimateParam = new DoubleParameter(LOGNORMAL_MAX_X_PARAM_NAME,
                                                 DEFAULT_LOGNORMAL_MAX_X_PARAM_VAL);



   // put all the parameters in the parameter list
   parameterList = new ParameterList();
   parameterList.addParameter(chooseEstimateParam);
   parameterList.addParameter(this.meanParam);
   parameterList.addParameter(this.stdDevParam);
   parameterList.addParameter(this.linearMedianParam);
   parameterList.addParameter(this.logBaseParam);
   parameterList.addParameter(datingTechParam);
   parameterList.addParameter(isDateCorrectedParam);
   parameterList.addParameter(yearUnitsParam);
   parameterList.addParameter(yearBeforeParam);
   parameterList.addParameter(this.arbitrarilyDiscFuncParam);
   parameterList.addParameter(evenlyDiscFuncParam);
   parameterList.addParameter(minNormalEstimateParam);
   parameterList.addParameter(minLogNormalEstimateParam);
   parameterList.addParameter(maxNormalEstimateParam);
   parameterList.addParameter(maxLogNormalEstimateParam);

   this.editor = new ParameterListEditor(parameterList);

   // show the units and estimate param name as the editor title
   String units = estimateParam.getUnits();
   String title;
   if(units!=null && !units.equalsIgnoreCase("")) title = estimateParam.getName()+"("+units+")";
   else title = estimateParam.getName();
   editor.setTitle(title);

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

   double constraintMin = estimateConstraint.getMin().doubleValue();
   double constraintMax = estimateConstraint.getMax().doubleValue();
   String constraintMinText = this.MIN_CONSTRAINT_LABEL+constraintMin;
   String constraintMaxText = this.MAX_CONSTRAINT_LABEL+constraintMax;
   //minConstraintLabel= new JLabel(this.MIN_CONSTRAINT_LABEL+constraintMin);
   //maxConstraintLabel= new JLabel(this.MAX_CONSTRAINT_LABEL+constraintMax);
   //editor.setToolTipText(minConstraintLabel.getText()+","+maxConstraintLabel.getText());
   editor.setToolTipText(this.estimateParam.getInfo()+"::"+constraintMinText+","+constraintMaxText);
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


    // if it is a date estimate, show the additional parameters
    if(this.isDateEstimate(estimateName)) setParamsForDateEstimate(true);
    else setParamsForDateEstimate(false);


    // For NORMAL estimate
    if(estimateName.equalsIgnoreCase(NormalEstimate.NAME) ||
       estimateName.equalsIgnoreCase(NormalDateEstimate.NAME))
      setParamsForNormalEstimate();
    // for LOGNORMAL Estimate
    else if(estimateName.equalsIgnoreCase(LogNormalEstimate.NAME)||
            estimateName.equalsIgnoreCase(LogNormalDateEstimate.NAME))
      setParamsForLogNormalEstimate();
    // for Integer Estimate and DiscretValueEstimate
    else if(estimateName.equalsIgnoreCase(IntegerEstimate.NAME) ||
            estimateName.equalsIgnoreCase(DiscreteValueEstimate.NAME) ||
            estimateName.equalsIgnoreCase(DiscreteValueDateEstimate.NAME))
      setParamsForXY_Estimate();
    // for Fractile List Estimate
    else if(estimateName.equalsIgnoreCase(FractileListEstimate.NAME)||
            estimateName.equalsIgnoreCase(FractileListDateEstimate.NAME))
      setParamsForFractileListEstimate();
    // For PDF Estimate
    else if(estimateName.equalsIgnoreCase(PDF_Estimate.NAME) ||
            estimateName.equalsIgnoreCase(PDF_DateEstimate.NAME))
      setParamsForPDF_Estimate();

  }


  /**
   * Finds whether some type of date estimate has been selected by the user
   *
   * @param estimateName
   * @return
   */
  private boolean isDateEstimate(String estimateName) {
    if(estimateName.equalsIgnoreCase(NormalDateEstimate.NAME) ||
      estimateName.equalsIgnoreCase(LogNormalDateEstimate.NAME) ||
      estimateName.equalsIgnoreCase(FractileListDateEstimate.NAME) ||
      estimateName.equalsIgnoreCase(DiscreteValueDateEstimate.NAME) ||
      estimateName.equalsIgnoreCase(PDF_DateEstimate.NAME))
     return true;
   return false;
  }

  private void setParamsForDateEstimate(boolean isVisible) {
    editor.setParameterVisible(DATING_TECH_PARAM_NAME, isVisible);
    editor.setParameterVisible(IS_DATE_CORRECTED_PARAM_NAME, isVisible);
    editor.setParameterVisible(YEAR_UNITS_PARAM_NAME, isVisible);
    editor.setParameterVisible(YEAR_BEFORE_PARAM_NAME, isVisible);
  }


  /**
   * make the parameters visible/invisible for min/max/preferred estimate
   */
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
    editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, false);
    editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, false);
    xValsParamListEditor.setVisible(true);
    probValsParamListEditor.setVisible(true);
    viewEstimateButton.setVisible(false);
  }

  /**
   * Set the params visible/invisible for normal estimate
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
   editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, true);
    editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, false);
   xValsParamListEditor.setVisible(false);
   probValsParamListEditor.setVisible(false);
   viewEstimateButton.setVisible(true);
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
    editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, false);
    editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, true);
    xValsParamListEditor.setVisible(false);
    probValsParamListEditor.setVisible(false);
    viewEstimateButton.setVisible(true);
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
   editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, false);
    editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, false);
   xValsParamListEditor.setVisible(false);
   probValsParamListEditor.setVisible(false);
   viewEstimateButton.setVisible(true);
 }

 /**
  * Set the params visible for DiscreteValue and Integer  estimate
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
   editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, false);
   editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, false);
   xValsParamListEditor.setVisible(false);
   probValsParamListEditor.setVisible(false);
   viewEstimateButton.setVisible(true);
 }

 public void actionPerformed(ActionEvent e) {
   if  (e.getSource()==viewEstimateButton) {
     try {
       setEstimateInParameter();
       viewEstimate();
     }catch(Exception ex) {
       JOptionPane.showMessageDialog(this,ex.getMessage());
     }
   } else if (e.getSource()==this.button) {
     frame.pack();
     this.frame.show();
   }
 }

 /**
  * Open a Jfreechart window to view the estimate
  */
 private void viewEstimate() {
   Estimate estimate = (Estimate)this.estimateParam.getValue();
   if(estimate!=null) {
     EstimateViewer estimateViewer = new EstimateViewer(estimate);
   }
 }


 /**
  * Set the estimate value inside the estimateParameter
  */
 public void setEstimateInParameter() {
   Estimate estimate = null;
   String estimateName=(String)this.chooseEstimateParam.getValue();

   if (estimateName.equalsIgnoreCase(NormalEstimate.NAME))
     estimate = setNormalEstimate();
   else if (estimateName.equalsIgnoreCase(LogNormalEstimate.NAME))
     estimate = setLogNormalEstimate();
   else if (estimateName.equalsIgnoreCase(FractileListEstimate.NAME))
     estimate = setFractileListEstimate();
   else if (estimateName.equalsIgnoreCase(IntegerEstimate.NAME))
     estimate = setIntegerEstimate();
   else if (estimateName.equalsIgnoreCase(DiscreteValueEstimate.NAME))
     estimate = setDiscreteValueEstimate();
   else if (estimateName.equalsIgnoreCase(PDF_Estimate.NAME))
     estimate = setPDF_Estimate();

     // for Date estimates
   if (estimateName.equalsIgnoreCase(NormalDateEstimate.NAME))
     estimate = setNormalDateEstimate();
   else if (estimateName.equalsIgnoreCase(LogNormalDateEstimate.NAME))
     estimate = setLogNormalDateEstimate();
   else if (estimateName.equalsIgnoreCase(FractileListDateEstimate.NAME))
     estimate = setFractileListDateEstimate();
   else if (estimateName.equalsIgnoreCase(DiscreteValueDateEstimate.NAME))
     estimate = setDiscreteValueDateEstimate();
   else if (estimateName.equalsIgnoreCase(PDF_DateEstimate.NAME))
     estimate = setPDF_DateEstimate();

   estimate.setUnits(estimateParam.getUnits());
   this.estimateParam.setValue(estimate);
 }

 /**
  * Set the estimate paramter value to be normal estimate
  */
 private Estimate setNormalEstimate() {
   double mean = ((Double)meanParam.getValue()).doubleValue();
   double stdDev = ((Double)stdDevParam.getValue()).doubleValue();
   double minX = ((Double)this.minNormalEstimateParam.getValue()).doubleValue();
   double maxX = ((Double)this.maxNormalEstimateParam.getValue()).doubleValue();
   NormalEstimate estimate = new NormalEstimate(mean, stdDev);
   estimate.setMinMaxX(minX, maxX);
   return estimate;
 }

 /**
  * Set the estimate paramter value to be normal date estimate
  */
 private Estimate setNormalDateEstimate() {
   double mean = ((Double)meanParam.getValue()).doubleValue();
   double stdDev = ((Double)stdDevParam.getValue()).doubleValue();
   double minX = ((Double)this.minNormalEstimateParam.getValue()).doubleValue();
   double maxX = ((Double)this.maxNormalEstimateParam.getValue()).doubleValue();
   NormalDateEstimate estimate = new NormalDateEstimate(mean, stdDev);
   estimate.setMinMaxX(minX, maxX);
   return estimate;
 }


 /**
  * Set the estimate paramter value to be lognormal estimate
  */
 private Estimate setLogNormalEstimate() {
   double linearMedian = ((Double)linearMedianParam.getValue()).doubleValue();
   double stdDev = ((Double)stdDevParam.getValue()).doubleValue();
   LogNormalEstimate estimate = new LogNormalEstimate(linearMedian, stdDev);
   if(this.logBaseParam.getValue().equals(this.LOG_BASE_10_NAME))
     estimate.setIsBase10(true);
   else   estimate.setIsBase10(false);
   double minX = ((Double)this.minLogNormalEstimateParam.getValue()).doubleValue();
   double maxX = ((Double)this.maxLogNormalEstimateParam.getValue()).doubleValue();
   estimate.setMinMaxX(minX, maxX);
   return estimate;
 }

 /**
  * Set the estimate paramter value to be lognormal date estimate
  */
 private Estimate setLogNormalDateEstimate() {
   double linearMedian = ((Double)linearMedianParam.getValue()).doubleValue();
   double stdDev = ((Double)stdDevParam.getValue()).doubleValue();
   LogNormalDateEstimate estimate = new LogNormalDateEstimate(linearMedian, stdDev);
   if(this.logBaseParam.getValue().equals(this.LOG_BASE_10_NAME))
     estimate.setIsBase10(true);
   else   estimate.setIsBase10(false);
   double minX = ((Double)this.minLogNormalEstimateParam.getValue()).doubleValue();
   double maxX = ((Double)this.maxLogNormalEstimateParam.getValue()).doubleValue();
   estimate.setMinMaxX(minX, maxX);
   return estimate;
 }


 /**
  * Set the estimate paramter value to be discrete vlaue estimate
  */
 private Estimate setDiscreteValueEstimate() {
   DiscreteValueEstimate estimate = new DiscreteValueEstimate((ArbitrarilyDiscretizedFunc)this.arbitrarilyDiscFuncParam.getValue(), false);
   return estimate;
 }

 /**
  * Set the estimate paramter value to be discrete value Date estimate
  */
 private Estimate setDiscreteValueDateEstimate() {
   DiscreteValueDateEstimate estimate = new DiscreteValueDateEstimate((ArbitrarilyDiscretizedFunc)this.arbitrarilyDiscFuncParam.getValue(), false);
   return estimate;
 }


 /**
  * Set the estimate paramter value to be integer estimate
  */
 private Estimate setIntegerEstimate() {
   IntegerEstimate estimate = new IntegerEstimate((ArbitrarilyDiscretizedFunc)this.arbitrarilyDiscFuncParam.getValue(), false);
   return estimate;
 }

 /**
  * Set the estimate paramter value to be Pdf estimate
  */
 private Estimate setPDF_Estimate() {
   PDF_Estimate estimate = new PDF_Estimate((EvenlyDiscretizedFunc)this.evenlyDiscFuncParam.getValue(), false);
   return estimate;
 }

 /**
  * Set the estimate paramter value to be Pdf Date estimate
  */
 private Estimate setPDF_DateEstimate() {
   PDF_DateEstimate estimate = new PDF_DateEstimate((EvenlyDiscretizedFunc)this.evenlyDiscFuncParam.getValue(), false);
   return estimate;
 }


 /**
   * Set the estimate paramter value to be min/max/preferred estimate
   */
 private Estimate setFractileListEstimate() {
   ArbDiscrEmpiricalDistFunc empiricalFunc = new ArbDiscrEmpiricalDistFunc();
   empiricalFunc.set(((Double)this.minX_Param.getValue()).doubleValue(), ((Double)this.minProbParam.getValue()).doubleValue());
   empiricalFunc.set(((Double)this.maxX_Param.getValue()).doubleValue(), ((Double)this.maxProbParam.getValue()).doubleValue());
   empiricalFunc.set(((Double)this.prefferedX_Param.getValue()).doubleValue(), ((Double)this.prefferedProbParam.getValue()).doubleValue());
   FractileListEstimate estimate = new FractileListEstimate(empiricalFunc);
   return estimate;
 }

 /**
  * Set the estimate paramter value to be min/max/preferred date estimate
  */
 private Estimate setFractileListDateEstimate() {
   ArbDiscrEmpiricalDistFunc empiricalFunc = new ArbDiscrEmpiricalDistFunc();
   empiricalFunc.set( ( (Double)this.minX_Param.getValue()).doubleValue(),
                     ( (Double)this.minProbParam.getValue()).doubleValue());
   empiricalFunc.set( ( (Double)this.maxX_Param.getValue()).doubleValue(),
                     ( (Double)this.maxProbParam.getValue()).doubleValue());
   empiricalFunc.set( ( (Double)this.prefferedX_Param.getValue()).doubleValue(),
                     ( (Double)this.prefferedProbParam.getValue()).doubleValue());
   FractileListDateEstimate estimate = new FractileListDateEstimate(empiricalFunc);
   return estimate;
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
    * Show the general info about the estimates to the user
    * @param info
    */
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
    ConstrainedEstimateParameterEditor estimateParameterEditor = new ConstrainedEstimateParameterEditor(estimateParam);
    frame.getContentPane().add(estimateParameterEditor, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    frame.pack();
    frame.show();
  }

}