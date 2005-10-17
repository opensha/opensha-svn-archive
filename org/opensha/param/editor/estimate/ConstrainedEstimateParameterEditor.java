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
   /**
    * Std Dev parameter for normal/lognormal distribution
    */
   private DoubleParameter stdDevParam;
   private final static String STD_DEV_PARAM_NAME="Std Dev";

   /**
    * Linear Median parameter for lognormal distribution
    */
   private DoubleParameter linearMedianParam;
   private final static String LINEAR_MEDIAN_PARAM_NAME="Linear Median";

   /**
    * Min/Max  values that can be set into Normal/LogNormal estimate
    * These are used for testing purposes. These  parameters may be removed
    * when we deploy this.
    */
   private DoubleParameter minNormalEstimateParam;
   private final static String NORMAL_MIN_X_PARAM_NAME="Normal Left Truncation Point";
   private final static Double DEFAULT_NORMAL_MIN_X_PARAM_VAL=new Double(Double.NEGATIVE_INFINITY);
   private DoubleParameter minLogNormalEstimateParam;
   private final static String LOGNORMAL_MIN_X_PARAM_NAME="LogNormal Left Truncation Point";
   private final static Double DEFAULT_LOGNORMAL_MIN_X_PARAM_VAL=new Double(0);
   private final static DoubleConstraint LOGNORMAL_MIN_X_PARAM_CONSTRAINT = new DoubleConstraint(0, Double.POSITIVE_INFINITY);
   private DoubleParameter maxNormalEstimateParam ;
   private final static String NORMAL_MAX_X_PARAM_NAME="Normal Right Truncation Point";
   private final static Double DEFAULT_NORMAL_MAX_X_PARAM_VAL=new Double(Double.POSITIVE_INFINITY);
   private DoubleParameter maxLogNormalEstimateParam ;
   private final static String LOGNORMAL_MAX_X_PARAM_NAME="LogNormal Right Truncation Point";
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
   private final static String XY_PARAM_NAME = "Values";

   private EvenlyDiscretizedFuncParameter evenlyDiscFuncParam;
   private final static String PDF_PARAM_NAME = "PDF Vals";

   //private JButton setEstimateButton ;
   private JButton viewEstimateButton;

   /**
    * Min, Max, Preferred
    */
   private DoubleParameter minX_Param;
   private final static String MIN_X_PARAM_NAME="Min";
   private DoubleParameter maxX_Param ;
   private final static String MAX_X_PARAM_NAME="Max";
   private DoubleParameter prefferedX_Param ;
   private final static String PREF_X_PARAM_NAME="Preferred";
   private DoubleParameter minProbParam;
   private final static String MIN_PROB_PARAM_NAME="Min";
   private DoubleParameter maxProbParam ;
   private final static String MAX_PROB_PARAM_NAME="Max";
   private DoubleParameter prefferedProbParam ;
   private final static String PREF_PROB_PARAM_NAME="Preferred";
   private ParameterListEditor xValsParamListEditor;
   private ParameterListEditor probValsParamListEditor;




   // title of Parameter List Editor
   public static final String X_TITLE = new String("Values");
   // title of Parameter List Editor
   public static final String PROB_TITLE = new String("Probability this value is correct");

   private final static String MIN_CONSTRAINT_LABEL="Min Value:";
   private final static String MAX_CONSTRAINT_LABEL="Max Value:";

   // size of the editor
   protected final static Dimension WIGET_PANEL_DIM = new Dimension( 140, 300 );

   /* this editor will be shown only as a button. On button click, a new window
    appears showing all the parameters */
   private JButton button;

   private JFrame frame;

   /* Whether to show this editor as a button.
      1. If this is set as true, a pop-up window appears on button click
      2. If this is set as false, button is not shown for pop-up window.
    */
   private boolean showEditorAsPanel = false;

   private EstimateConstraint estimateConstraint;

   /**
    * Whether to show the min/max cut off parameters for normal/log normal estimates
    */
   private boolean showMinMaxTruncationParams=true;
   private final static String MSG_IS_NORMAL_OK =
       "It seems that Normal(Gaussian) distribution can be used here instead of Min/Max/Preferred. \n"+
       "Do you want to use Normal(Gaussian) ?";
   private final static String MSG_VALUE_MISSING_SUFFIX = " value is missing ";

   public ConstrainedEstimateParameterEditor() {
   }

    public ConstrainedEstimateParameterEditor(ParameterAPI model) {
      this(model, false, true);
    }

   //constructor taking the Parameter as the input argument
   public ConstrainedEstimateParameterEditor(ParameterAPI model,
                                             boolean showEditorAsPanel,
                                             boolean showMinMaxTruncationParams){

     this.showEditorAsPanel = showEditorAsPanel;
     this.showMinMaxTruncationParams = showMinMaxTruncationParams;
     try {
       this.jbInit();
       setParameter(model);
     }catch(Exception e) {
       e.printStackTrace();
     }
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

    Container container;
    if (!this.showEditorAsPanel) { // show editor as a button. In this case
                                   // parameters are shown in a pop up window on button click
      frame = new JFrame();
      frame.setTitle(this.estimateParam.getName());
      button = new JButton(this.estimateParam.getName());
      button.addActionListener(this);
      this.add(this.button,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
      container = frame.getContentPane();
      container.setLayout(GBL);
    } else { // add all the parameters in the current panel
      container = this;
    }

    /*container.add(addParamatersToPanel(), new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
                                                 , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
     */
    container.add(this.editor,new GridBagConstraints( 0, 0, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    container.add(xValsParamListEditor,new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    container.add(probValsParamListEditor,new GridBagConstraints( 1, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    container.add(viewEstimateButton,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 ) );
    //container.add(this.estimateInfo,new GridBagConstraints( 0, 3, 2, 1, 1.0, 0.0
    //    , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );

    setEstimateParams((String)chooseEstimateParam.getValue());
    this.refreshParamEditor();
    // All done
    if ( D ) System.out.println( S + "Ending:" );
  }


  protected void jbInit() {
    this.setMinimumSize(new Dimension(0, 0));
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
    this.updateUI();
    if(frame!=null)
      frame.repaint();
  }

  /**
   * Initialize the parameters and editors
   */
  protected void initParamListAndEditor()  {

    // Starting
    String S = C + ": initControlsParamListAndEditor(): ";
    if ( D ) System.out.println( S + "Starting:" );


    meanParam = new DoubleParameter(MEAN_PARAM_NAME);
    stdDevParam = new DoubleParameter(STD_DEV_PARAM_NAME);
    linearMedianParam = new DoubleParameter(LINEAR_MEDIAN_PARAM_NAME);

    ArbitrarilyDiscretizedFunc arbitraryDiscretizedFunc = new ArbitrarilyDiscretizedFunc();
    arbitraryDiscretizedFunc.setXAxisName("Exact Value");
    arbitraryDiscretizedFunc.setYAxisName("Probability this is correct");
    arbitrarilyDiscFuncParam = new ArbitrarilyDiscretizedFuncParameter(XY_PARAM_NAME, arbitraryDiscretizedFunc);

    EvenlyDiscretizedFunc evenlyDiscretizedFunc = new EvenlyDiscretizedFunc(1.0,4.0,7);
    evenlyDiscretizedFunc.setXAxisName("Value");
    evenlyDiscretizedFunc.setYAxisName("Probability");
    evenlyDiscFuncParam = new EvenlyDiscretizedFuncParameter(PDF_PARAM_NAME, evenlyDiscretizedFunc);
    // list of available estimates
    estimateConstraint = (EstimateConstraint)estimateParam.getConstraint();
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

    // put all the parameters in the parameter list
    parameterList = new ParameterList();
    parameterList.addParameter(chooseEstimateParam);
    parameterList.addParameter(this.meanParam);
    parameterList.addParameter(this.stdDevParam);
    parameterList.addParameter(this.linearMedianParam);
    parameterList.addParameter(this.logBaseParam);
    parameterList.addParameter(this.arbitrarilyDiscFuncParam);
    parameterList.addParameter(evenlyDiscFuncParam);

    if(this.showMinMaxTruncationParams) // whether to show min/max truncation params
      addMinMaxTruncationParams(estimateConstraint);

    this.editor = new ParameterListEditor(parameterList);

   // show the units and estimate param name as the editor title
   String units = estimateParam.getUnits();
   String title;
   if(units!=null && !units.equalsIgnoreCase("")) title = estimateParam.getName()+"("+units+")";
   else title = estimateParam.getName();
   editor.setTitle(title);

   // parameters for min/max/preferred user choice
  minX_Param = new DoubleParameter(MIN_X_PARAM_NAME);

  maxX_Param = new DoubleParameter(MAX_X_PARAM_NAME);
  prefferedX_Param = new DoubleParameter(PREF_X_PARAM_NAME);

  ParameterList xValsParamList = new ParameterList();
  xValsParamList.addParameter(minX_Param);
  xValsParamList.addParameter(maxX_Param);
  xValsParamList.addParameter(prefferedX_Param);
  xValsParamListEditor = new ParameterListEditor(xValsParamList);
  xValsParamListEditor.setTitle(this.X_TITLE);


  minProbParam = new DoubleParameter(MIN_PROB_PARAM_NAME);
  maxProbParam = new DoubleParameter(MAX_PROB_PARAM_NAME);
  prefferedProbParam = new DoubleParameter(PREF_PROB_PARAM_NAME);
  ParameterList probParamList = new ParameterList();
  probParamList.addParameter(minProbParam);
  probParamList.addParameter(maxProbParam);
  probParamList.addParameter(prefferedProbParam);
  probValsParamListEditor = new ParameterListEditor(probParamList);
  probValsParamListEditor.setTitle(this.PROB_TITLE);

   //setEstimateButton = new JButton("Set Estimate");
   viewEstimateButton = new JButton("Plot Estimate");
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

 /**
  * Add the parameters  so that user can enter min/max truncation values
  * @param estimateConstraint
  * @throws ParameterException
  * @throws ConstraintException
  */
  private void addMinMaxTruncationParams(EstimateConstraint estimateConstraint) throws
      ParameterException, ConstraintException {
    /**
     * Min/Max  values that can be set into Normal/LogNormal estimate
     * These are used for testing purposes. These  parameters may be removed
     * when we deploy this.
     */
    minNormalEstimateParam = new DoubleParameter(NORMAL_MIN_X_PARAM_NAME,
                                                 estimateConstraint.getMin());
    minLogNormalEstimateParam = new DoubleParameter(LOGNORMAL_MIN_X_PARAM_NAME,
        LOGNORMAL_MIN_X_PARAM_CONSTRAINT, estimateConstraint.getMin());
    maxNormalEstimateParam = new DoubleParameter(NORMAL_MAX_X_PARAM_NAME,
                                           estimateConstraint.getMax());
    maxLogNormalEstimateParam = new DoubleParameter(LOGNORMAL_MAX_X_PARAM_NAME,
                                                  estimateConstraint.getMax());
    parameterList.addParameter(minNormalEstimateParam);
    parameterList.addParameter(minLogNormalEstimateParam);
    parameterList.addParameter(maxNormalEstimateParam);
    parameterList.addParameter(maxLogNormalEstimateParam);
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
    if(event.getParameterName().equalsIgnoreCase(CHOOSE_ESTIMATE_PARAM_NAME)) {
      setEstimateParams( (String) chooseEstimateParam.getValue());
      this.refreshParamEditor();
    }
  }



  // make the params visible/invisible based on selected estimate type
  private void setEstimateParams(String estimateName) {

    // For NORMAL estimate
    if(estimateName.equalsIgnoreCase(NormalEstimate.NAME)) {
      setParamsForNormalEstimate();
    }
    // for LOGNORMAL Estimate
    else if(estimateName.equalsIgnoreCase(LogNormalEstimate.NAME)) {
      setParamsForLogNormalEstimate();
    }
    // for Integer Estimate and DiscretValueEstimate
    else if(estimateName.equalsIgnoreCase(IntegerEstimate.NAME) ||
            estimateName.equalsIgnoreCase(DiscreteValueEstimate.NAME))
      setParamsForXY_Estimate();
    // for Fractile List Estimate
    else if(estimateName.equalsIgnoreCase(FractileListEstimate.NAME))
      setParamsForFractileListEstimate();
    // For PDF Estimate
    else if(estimateName.equalsIgnoreCase(PDF_Estimate.NAME))
      setParamsForPDF_Estimate();

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
     if(this.showMinMaxTruncationParams) {
       editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, false);
       editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, false);
       editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, false);
       editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, false);
     }
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
   if(this.showMinMaxTruncationParams) {
     editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, true);
     editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, false);
     editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, true);
     editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, false);
   }
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
    if(this.showMinMaxTruncationParams) {
      editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, false);
      editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, true);
      editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, false);
      editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, true);
    }
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
   if(this.showMinMaxTruncationParams) {
     editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, false);
     editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, false);
     editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, false);
     editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, false);
   }
   xValsParamListEditor.setVisible(false);
   probValsParamListEditor.setVisible(false);
   viewEstimateButton.setVisible(true);
 }

 /**
  * It enables/disables the editor according to whether user is allowed to
  * fill in the values.
  */
 public void setEnabled(boolean isEnabled) {
   this.editor.setEnabled(isEnabled);
   this.xValsParamListEditor.setEnabled(isEnabled);
   this.probValsParamListEditor.setEnabled(isEnabled);

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
   if(this.showMinMaxTruncationParams) {
     editor.setParameterVisible(NORMAL_MIN_X_PARAM_NAME, false);
     editor.setParameterVisible(LOGNORMAL_MIN_X_PARAM_NAME, false);
     editor.setParameterVisible(NORMAL_MAX_X_PARAM_NAME, false);
     editor.setParameterVisible(LOGNORMAL_MAX_X_PARAM_NAME, false);
   }
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

   estimate.setUnits(estimateParam.getUnits());
   this.estimateParam.setValue(estimate);
 }

 /**
  * Set the estimate paramter value to be normal estimate
  */
 private Estimate setNormalEstimate() {
   // first check that user typed in the mean value
   Double meanParamVal = (Double)meanParam.getValue();
   if(meanParamVal==null)
     throw new RuntimeException(meanParam.getName()+MSG_VALUE_MISSING_SUFFIX);
   double mean = meanParamVal.doubleValue();
   // check that usr entered the standard deviation value
   Double stdDevVal = (Double)stdDevParam.getValue();
   if(stdDevVal==null)
      throw new RuntimeException(stdDevParam.getName()+MSG_VALUE_MISSING_SUFFIX);
   double stdDev = stdDevVal.doubleValue();
   // create Normal(Gaussian) estimate
   NormalEstimate estimate = new NormalEstimate(mean, stdDev);
   if(this.showMinMaxTruncationParams) {
     double minX = ( (Double)this.minNormalEstimateParam.getValue()).
         doubleValue();
     double maxX = ( (Double)this.maxNormalEstimateParam.getValue()).
         doubleValue();
     estimate.setMinMaxX(minX, maxX);
   } else {
     estimate.setMinMaxX(estimateConstraint.getMin().doubleValue(),
                         estimateConstraint.getMax().doubleValue());
   }
   return estimate;
 }




 /**
  * Set the estimate paramter value to be lognormal estimate
  */
 private Estimate setLogNormalEstimate() {
   // check that user entered linear median value
   Double linearMedianVal = (Double)linearMedianParam.getValue();
   if(linearMedianVal==null)
     throw new RuntimeException(linearMedianParam.getName()+MSG_VALUE_MISSING_SUFFIX);
   double linearMedian = linearMedianVal.doubleValue();
   // check that user entered std dev value
   Double stdDevVal = (Double)stdDevParam.getValue();
   if(stdDevVal==null)
     throw new RuntimeException(stdDevParam.getName()+MSG_VALUE_MISSING_SUFFIX);
   double stdDev =stdDevVal.doubleValue();
   // create instance of log normal estimate
   LogNormalEstimate estimate = new LogNormalEstimate(linearMedian, stdDev);
   if(this.logBaseParam.getValue().equals(this.LOG_BASE_10_NAME))
     estimate.setIsBase10(true);
   else   estimate.setIsBase10(false);

   if(this.showMinMaxTruncationParams) {
     double minX = ( (Double)this.minLogNormalEstimateParam.getValue()).
         doubleValue();
     double maxX = ( (Double)this.maxLogNormalEstimateParam.getValue()).
         doubleValue();
     estimate.setMinMaxX(minX, maxX);
   }else {
     estimate.setMinMaxX(estimateConstraint.getMin().doubleValue(),
                         estimateConstraint.getMax().doubleValue());
   }

   return estimate;
 }




 /**
  * Set the estimate paramter value to be discrete vlaue estimate
  */
 private Estimate setDiscreteValueEstimate() {
   ArbitrarilyDiscretizedFunc val = (ArbitrarilyDiscretizedFunc)this.arbitrarilyDiscFuncParam.getValue();
   if(val.getNum()==0) throw new RuntimeException(arbitrarilyDiscFuncParam.getName()+
         this.MSG_VALUE_MISSING_SUFFIX);
   DiscreteValueEstimate estimate = new DiscreteValueEstimate(val, true);
   return estimate;
 }




 /**
  * Set the estimate paramter value to be integer estimate
  */
 private Estimate setIntegerEstimate() {
   ArbitrarilyDiscretizedFunc val = (ArbitrarilyDiscretizedFunc)this.arbitrarilyDiscFuncParam.getValue();
   if(val.getNum()==0)
      throw new RuntimeException(arbitrarilyDiscFuncParam.getName()+this.MSG_VALUE_MISSING_SUFFIX);
   IntegerEstimate estimate = new IntegerEstimate(val, true);
   return estimate;
 }

 /**
  * Set the estimate paramter value to be Pdf estimate
  */
 private Estimate setPDF_Estimate() {
   PDF_Estimate estimate = new PDF_Estimate((EvenlyDiscretizedFunc)this.evenlyDiscFuncParam.getValue(), true);
   return estimate;
 }



 /**
   * Set the estimate paramter value to be min/max/preferred estimate
   */
 private Estimate setFractileListEstimate() {
   ArbDiscrEmpiricalDistFunc empiricalFunc = new ArbDiscrEmpiricalDistFunc();
   double minX, maxX, prefX, minProb, maxProb, prefProb;
   minX = getValueForParameter(minX_Param);
   maxX = getValueForParameter(maxX_Param);
   prefX = getValueForParameter(prefferedX_Param);
   // check that user typed in atleast one of minX, maxX or prefX
   if(Double.isNaN(minX) && Double.isNaN(maxX) && Double.isNaN(prefX)) {
     throw new RuntimeException("Enter atleast one of "+minX_Param.getName()+","+
                                maxX_Param.getName()+" or "+prefferedX_Param.getName());
   }
   minProb = getValueForParameter(minProbParam);
   maxProb = getValueForParameter(maxProbParam);
   prefProb = getValueForParameter(prefferedProbParam);
   // check that if user entered minX, then minProb was also entered
   checkValidVals(minX, minProb, minX_Param.getName(), minProbParam.getName());
   // check that if user entered maxX, then maxProb was also entered
   checkValidVals(maxX, maxProb, maxX_Param.getName(), maxProbParam.getName());
   // check that if user entered prefX, then prefProb was also entered
   checkValidVals(prefX, prefProb, prefferedX_Param.getName(), prefferedProbParam.getName());

   if(Double.isNaN(minX)) {
     minX = Double.NEGATIVE_INFINITY;
     minProb = 0;
   }
   if(Double.isNaN(maxX)) {
     maxX = Double.POSITIVE_INFINITY;
     maxProb = 0;
   }
   if(Double.isNaN(prefX)) {
     prefX = (minX+maxX)/2;
     prefProb = 0;
   }

   // if min/max/pref is symmetric, ask the user whether normal distribution can be used
   if(!(Double.isNaN(minX) || Double.isNaN(maxX) || Double.isNaN(prefX) ||
        Double.isNaN(minProb) || Double.isNaN(maxProb) || Double.isNaN(prefProb))) { //
     if((minX>prefX) || (minX>maxX) || (prefX>maxX))
       throw new RuntimeException(prefferedX_Param.getName()+" should be greater than/equal to "+minX_Param.getName()+
                                  "\n"+maxX_Param.getName()+" should be greater than/equal to "+prefferedX_Param.getName());
     double diffX1 = prefX - minX;
     double diffX2 = maxX - prefX;
     double diffProb1 = prefProb - minProb;
     double diffProb2 = prefProb - maxProb;
     if(diffX1==diffX2 && diffProb1==diffProb2 &&
        chooseEstimateParam.getAllowedStrings().contains(NormalEstimate.NAME)) { // it is symmetric
        // ask the user if normal distribution can be used
        int option = JOptionPane.showConfirmDialog(this, MSG_IS_NORMAL_OK);
        if(option==JOptionPane.OK_OPTION) {
          this.chooseEstimateParam.setValue(NormalEstimate.NAME);
          throw new RuntimeException("Changing to Normal Estimate");
        }
     }
   }
   empiricalFunc.set(minX, minProb);
   empiricalFunc.set(maxX, maxProb);
   empiricalFunc.set(prefX, prefProb);
   FractileListEstimate estimate = new FractileListEstimate(empiricalFunc);
   return estimate;
 }

 /**
  * Check that if usr entered X value, then prob value is also entered.
  * Similarly, a check is also made that if prob is entered, x val must be entered as well.
  * If any check fails RuntimException is thrown
  * @param xVal
  * @param probVal
  * @param xParamName
  * @param probParamName
  */
 private void checkValidVals(double xVal, double probVal, String xParamName,
                             String probParamName) {
   if(Double.isNaN(xVal) && !Double.isNaN(probVal))
     throw new RuntimeException("If " + xParamName +" is empty, "+probParamName+" is not allowed");
   if(!Double.isNaN(xVal) && Double.isNaN(probVal))
     throw new RuntimeException(probParamName+this.MSG_VALUE_MISSING_SUFFIX);
 }

 private double getValueForParameter(Parameter param) {
   Object val = param.getValue();
   if(val==null) return Double.NaN;
   else return ((Double)val).doubleValue();
 }



  private void copyFunction(DiscretizedFunc funcFrom, DiscretizedFunc funcTo) {
    int numVals = funcFrom.getNum();
    for(int i=0; i < numVals; ++i) funcTo.set(funcFrom.getX(i), funcFrom.getY(i));
  }

  //static initializer for setting look & feel
   static {
     String osName = System.getProperty("os.name");
     try {
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
    ConstrainedEstimateParameterEditor estimateParameterEditor = new ConstrainedEstimateParameterEditor(estimateParam,true,true);
    frame.getContentPane().add(estimateParameterEditor, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    frame.pack();
    frame.show();
  }

}