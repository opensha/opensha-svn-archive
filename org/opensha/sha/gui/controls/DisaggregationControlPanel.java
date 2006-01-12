package org.opensha.sha.gui.controls;

import java.awt.*;
import javax.swing.*;

import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.param.event.*;
import java.awt.event.*;
import org.opensha.param.StringParameter;
import java.util.ArrayList;


/**
 * <p>Title: DisaggregationControlPanel</p>
 * <p>Description: This is control panel in which user can choose whether
 * to choose disaggregation or not. In addition, prob. can be input by the user</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta
 * @version 1.0
 */

public class DisaggregationControlPanel extends JFrame
    implements ParameterChangeFailListener, ParameterChangeListener{

  private final static String DISAGGREGATION_PROB_PARAM_NAME = "Disaggregation Prob";
  private final static String DISAGGREGATION_IML_PARAM_NAME = "Disaggregation IML";


  //Disaggregation Parameter
  private DoubleParameter disaggregationProbParam =
      new DoubleParameter(DISAGGREGATION_PROB_PARAM_NAME, 0, 1, new Double(.01));

  private DoubleParameter disaggregationIMLParam =
     new DoubleParameter(DISAGGREGATION_IML_PARAM_NAME, 0, 11, new Double(.1));

  private StringParameter disaggregationParameter ;

  private final static String DISAGGREGATION_PARAM_NAME = "Diasaggregate";

  public final static String NO_DISAGGREGATION = "No Disaggregation";
  public final static String DISAGGREGATE_USING_PROB = "Probability";
  public final static String DISAGGREGATE_USING_IML = "IML";

  //Shows the source disaggregation only if this parameter is selected
  private final static String SOURCE_DISAGGR_PARAM_NAME = "Show Source Disaggregation List";
  private BooleanParameter sourceDisaggregationParam = new BooleanParameter
      (SOURCE_DISAGGR_PARAM_NAME,new Boolean(false));

  private final static String NUM_SOURCE_PARAM_NAME = "Num Sources in List";
  private IntegerParameter numSourcesToShow = new IntegerParameter(NUM_SOURCE_PARAM_NAME,new Integer(100));


  //show the bin data only if this parameter is selected
  private final static String SHOW_DISAGGR_BIN_RATE_PARAM_NAME = "Show Disaggregation Bin Rate Data";
  private BooleanParameter binRateDisaggregationParam = new BooleanParameter
      (SHOW_DISAGGR_BIN_RATE_PARAM_NAME,new Boolean(false));



  //sets the Mag Range for Disaggregation calculation
  private static final String MIN_MAG_PARAM_NAME = "Min Mag (bin center)";
  private static final String NUM_MAG_PARAM_NAME = "Num Mag";
  private static final String DELTA_MAG_PARAM_NAME = "Delta Mag";
  private DoubleParameter minMagParam = new DoubleParameter(MIN_MAG_PARAM_NAME,0,10,new Double(5));
  private IntegerParameter numMagParam = new IntegerParameter(NUM_MAG_PARAM_NAME,new Integer(8));
  private DoubleParameter deltaMagParam = new DoubleParameter(DELTA_MAG_PARAM_NAME,new Double(0.5));

  //sets the Dist range for Disaggregation calculation
  private static final String MIN_DIST_PARAM_NAME = "Min Dist (bin center)";
  private static final String NUM_DIST_PARAM_NAME = "Num Dist";
  private static final String DELTA_DIST_PARAM_NAME = "Delta Dist";
  private DoubleParameter minDistParam = new DoubleParameter(MIN_DIST_PARAM_NAME,new Double(5));
  private IntegerParameter numDistParam = new IntegerParameter(NUM_DIST_PARAM_NAME,new Integer(11));
  private DoubleParameter deltaDistParam = new DoubleParameter(DELTA_DIST_PARAM_NAME,new Double(10));



  private ParameterListEditor paramListEditor;

  private boolean isDisaggregationSelected;


  // applet which called this control panel
  DisaggregationControlPanelAPI parent;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  public DisaggregationControlPanel(DisaggregationControlPanelAPI parent,
                                    Component parentComponent) {

    // set info strings for parameters
    minMagParam.setInfo("The center of the first magnitude bin (for histogram & mode calcs)");
    minDistParam.setInfo("The center of the first distance bin (for histogram & mode calcs)");

    numMagParam.setInfo("The number of magnitude bins (for histogram & mode calcs)");
    numDistParam.setInfo("The number of distance bins (for histogram & mode calcs)");

    deltaMagParam.setInfo("The width of magnitude bins (for histogram & mode calcs)");
    deltaDistParam.setInfo("The width of distance bins (for histogram & mode calcs)");

    sourceDisaggregationParam.setInfo("To show a list of sources in descending order"+
                                     " of their contribution to the hazard");

    numSourcesToShow.setInfo("The number of sources to show in the list");

    try {

      this.parent= parent;

      ArrayList disaggregateList = new ArrayList();
      disaggregateList.add(NO_DISAGGREGATION);
      disaggregateList.add(DISAGGREGATE_USING_PROB);
      disaggregateList.add(DISAGGREGATE_USING_IML);

      disaggregationParameter = new StringParameter(DISAGGREGATION_PARAM_NAME,disaggregateList,
          (String)disaggregateList.get(0));
      disaggregationParameter.addParameterChangeListener(this);
      disaggregationProbParam.addParameterChangeFailListener(this);
      disaggregationIMLParam.addParameterChangeFailListener(this);
      sourceDisaggregationParam.addParameterChangeListener(this);

      ParameterList paramList = new ParameterList();
      paramList.addParameter(disaggregationParameter);
      paramList.addParameter(disaggregationProbParam);
      paramList.addParameter(disaggregationIMLParam);
      paramList.addParameter(sourceDisaggregationParam);
      paramList.addParameter(numSourcesToShow);
      paramList.addParameter(binRateDisaggregationParam);
      paramList.addParameter(minMagParam);
      paramList.addParameter(numMagParam);
      paramList.addParameter(deltaMagParam);
      paramList.addParameter(minDistParam);
      paramList.addParameter(numDistParam);
      paramList.addParameter(deltaDistParam);


      paramListEditor = new ParameterListEditor(paramList);
      setParamsVisible((String)disaggregationParameter.getValue());
      jbInit();
      // show the window at center of the parent component
      this.setLocation(parentComponent.getX()+parentComponent.getWidth()/2,0);
      parent.setDisaggregationSelected(isDisaggregationSelected);

    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  // initialize the gui components
  private void jbInit() throws Exception {

    this.getContentPane().setLayout(gridBagLayout1);
    this.getContentPane().add(paramListEditor,
                              new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.NORTH, GridBagConstraints.BOTH,
        new Insets(2, 2, 2, 2), 0, 0));
    this.setTitle("Disaggregation Control Panel");
    paramListEditor.setTitle("Set Disaggregation Params");
    this.setSize(300,200);
  }


  /**
   *  Shown when a Constraint error is thrown on Disaggregation ParameterEditor
   * @param  e  Description of the Parameter
   */
  public void parameterChangeFailed( ParameterChangeFailEvent e ) {

    StringBuffer b = new StringBuffer();
    ParameterAPI param = ( ParameterAPI ) e.getSource();

    ParameterConstraintAPI constraint = param.getConstraint();
    String oldValueStr = e.getOldValue().toString();
    String badValueStr = e.getBadValue().toString();
    String name = param.getName();


    b.append( "The value ");
    b.append( badValueStr );
    b.append( " is not permitted for '");
    b.append( name );
    b.append( "'.\n" );
    b.append( "Resetting to ");
    b.append( oldValueStr );
    b.append( ". The constraints are: \n");
    b.append( constraint.toString() );

    JOptionPane.showMessageDialog(
        this, b.toString(),
        "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
        );

  }

  /**
   * shows the num sources to be shown for the disaggregation passed in
   * argument is true.
   * @param paramToShow boolean
   */
  private void showNumSourcesParam(boolean paramToShow){
    paramListEditor.getParameterEditor(NUM_SOURCE_PARAM_NAME).setVisible(paramToShow);
  }



  /**
   *
   * @param e ParameterChangeEvent
   */
  public void parameterChange (ParameterChangeEvent e){
    String paramName = e.getParameterName();
    if(paramName.equals(DISAGGREGATION_PARAM_NAME))
      setParamsVisible((String)disaggregationParameter.getValue());
    if(paramName.equals(SOURCE_DISAGGR_PARAM_NAME))
      showNumSourcesParam(((Boolean)sourceDisaggregationParam.getValue()).booleanValue());
  }


  /**
   * Returns the mininum Magnitude
   * @return double
   */
  public double getMinMag(){
    return ((Double)minMagParam.getValue()).doubleValue();
  }

  /**
   * Returns the number of magnitude intervals
   * @return double
   */
  public int getNumMag(){
    return ((Integer)numMagParam.getValue()).intValue();
  }

  /**
   * Returns the Mag range Discritization. It is evenly discretized.
   * @return double
   */
  public double getdeltaMag(){
    return ((Double)deltaMagParam.getValue()).doubleValue();
  }

  /**
   * Returns the minimum Distance
   * @return double
   */
  public double getMinDist(){
    return ((Double)minDistParam.getValue()).doubleValue();
  }

  /**
   * Returns the number of Distance intervals
   * @return double
   */
  public int getNumDist(){
    return ((Integer)numDistParam.getValue()).intValue();
  }

  /**
   * Returns the Distance range Discritization. It is evenly discretized.
   * @return double
   */
  public double getdeltaDist(){
    return ((Double)deltaDistParam.getValue()).doubleValue();
  }

  /**
   * Makes the parameters visible based on the choice of the user for Disaggregation
   */
  private void setParamsVisible(String paramValue){
    if(paramValue.equals(NO_DISAGGREGATION)){
      paramListEditor.getParameterEditor(DISAGGREGATION_PROB_PARAM_NAME).
          setVisible(false);
      paramListEditor.getParameterEditor(DISAGGREGATION_IML_PARAM_NAME).
          setVisible(false);
      isDisaggregationSelected = false;
      paramListEditor.getParameterEditor(MIN_MAG_PARAM_NAME).setVisible(false);
      paramListEditor.getParameterEditor(NUM_MAG_PARAM_NAME).setVisible(false);
      paramListEditor.getParameterEditor(DELTA_MAG_PARAM_NAME).setVisible(false);
      paramListEditor.getParameterEditor(MIN_DIST_PARAM_NAME).setVisible(false);
      paramListEditor.getParameterEditor(NUM_DIST_PARAM_NAME).setVisible(false);
      paramListEditor.getParameterEditor(DELTA_DIST_PARAM_NAME).setVisible(false);
      paramListEditor.getParameterEditor(SOURCE_DISAGGR_PARAM_NAME).setVisible(false);
      paramListEditor.getParameterEditor(SHOW_DISAGGR_BIN_RATE_PARAM_NAME).setVisible(false);
      showNumSourcesParam(false);
      this.setSize(300,200);
    }
    else{
      if (paramValue.equals(DISAGGREGATE_USING_PROB)) {
        paramListEditor.getParameterEditor(DISAGGREGATION_PROB_PARAM_NAME).
            setVisible(true);
        paramListEditor.getParameterEditor(DISAGGREGATION_IML_PARAM_NAME).
            setVisible(false);
        isDisaggregationSelected = true;
      }
      else if (paramValue.equals(DISAGGREGATE_USING_IML)) {
        paramListEditor.getParameterEditor(DISAGGREGATION_PROB_PARAM_NAME).
            setVisible(false);
        paramListEditor.getParameterEditor(DISAGGREGATION_IML_PARAM_NAME).
            setVisible(true);
        isDisaggregationSelected = true;
      }
      paramListEditor.getParameterEditor(MIN_MAG_PARAM_NAME).setVisible(true);
      paramListEditor.getParameterEditor(NUM_MAG_PARAM_NAME).setVisible(true);
      paramListEditor.getParameterEditor(DELTA_MAG_PARAM_NAME).setVisible(true);
      paramListEditor.getParameterEditor(MIN_DIST_PARAM_NAME).setVisible(true);
      paramListEditor.getParameterEditor(NUM_DIST_PARAM_NAME).setVisible(true);
      paramListEditor.getParameterEditor(DELTA_DIST_PARAM_NAME).setVisible(true);
      paramListEditor.getParameterEditor(SOURCE_DISAGGR_PARAM_NAME).setVisible(true);
      paramListEditor.getParameterEditor(SHOW_DISAGGR_BIN_RATE_PARAM_NAME).setVisible(true);
      showNumSourcesParam(((Boolean)sourceDisaggregationParam.getValue()).booleanValue());
      this.setSize(300,500);
    }
    this.repaint();
    this.validate();
    parent.setDisaggregationSelected(isDisaggregationSelected);
  }


  /**
   *
   * @return String : Returns on wht basis Diaggregation is being done either
   * using Probability or IML.
   */
  public String getDisaggregationParamValue(){
    return (String)disaggregationParameter.getValue();
  }


  /**
   * This function returns disaggregation prob value if disaggregation to be done
   * based on Probability else it returns IML value if disaggregation to be done
   * based on IML. If not disaggregation to be done , return -1.
   */
  public double getDisaggregationVal() {

    if(isDisaggregationSelected){
      String paramValue = getDisaggregationParamValue();
      if(paramValue.equals(DISAGGREGATE_USING_PROB))
        return ( (Double) disaggregationProbParam.getValue()).doubleValue();
      else if(paramValue.equals(DISAGGREGATE_USING_IML))
        return ( (Double) disaggregationIMLParam.getValue()).doubleValue();
    }
    return -1;
  }

  /**
   * Checks if Source Disaggregation needs to be calculated and shown in the window.
   * @return boolean
   */
  public boolean isSourceDisaggregationSelected(){
    if(isDisaggregationSelected)
      return ((Boolean)sourceDisaggregationParam.getValue()).booleanValue();
    return false;
  }

  /**
   * Returns the number of sources to show in Disaggregation.
   * @return int
   */
  public int getNumSourcesForDisagg(){
    if(isDisaggregationSelected)
      return ((Integer)numSourcesToShow.getValue()).intValue();
    return 0;
  }

  /**
   * Returns if Disaggregation Bin Rate Data is to be selected
   * @return boolean
   */
  public boolean isShowDisaggrBinDataSelected(){
    return ((Boolean)binRateDisaggregationParam.getValue()).booleanValue();
  }
}
