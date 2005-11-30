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


  private ParameterListEditor paramListEditor;

  private boolean isDisaggregationSelected;


  // applet which called this control panel
  DisaggregationControlPanelAPI parent;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  public DisaggregationControlPanel(DisaggregationControlPanelAPI parent,
                                    Component parentComponent) {
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

      ParameterList paramList = new ParameterList();
      paramList.addParameter(disaggregationParameter);
      paramList.addParameter(disaggregationProbParam);
      paramList.addParameter(disaggregationIMLParam);

      paramListEditor = new ParameterListEditor(paramList);
      setParamsVisible((String)disaggregationParameter.getValue());
      jbInit();
      // show the window at center of the parent component
      this.setLocation(parentComponent.getX()+parentComponent.getWidth()/2,
                     parentComponent.getY()+parentComponent.getHeight()/2);
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
   *
   * @param e ParameterChangeEvent
   */
  public void parameterChange (ParameterChangeEvent e){
    String paramName = e.getParameterName();
    if(paramName.equals(DISAGGREGATION_PARAM_NAME))
      setParamsVisible((String)disaggregationParameter.getValue());
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
    }
    else if(paramValue.equals(DISAGGREGATE_USING_PROB)){
      paramListEditor.getParameterEditor(DISAGGREGATION_PROB_PARAM_NAME).
          setVisible(true);
      paramListEditor.getParameterEditor(DISAGGREGATION_IML_PARAM_NAME).
          setVisible(false);
      isDisaggregationSelected = true;
    }
    else if(paramValue.equals(DISAGGREGATE_USING_IML)){
      paramListEditor.getParameterEditor(DISAGGREGATION_PROB_PARAM_NAME).
          setVisible(false);
      paramListEditor.getParameterEditor(DISAGGREGATION_IML_PARAM_NAME).
          setVisible(true);
      isDisaggregationSelected = true;
    }

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




}
