package org.scec.sha.gui;

import javax.swing.*;
import java.util.*;

import org.scec.param.editor.*;
import org.scec.param.*;
import org.scec.param.event.*;

/**
 * <p>Title: IMLorProbSelectorGuiBean</p>
 * <p>Description: This class provides with the ListEditor for the user to make the
 * selection for the Map Type user wants to generate</p>
 * @author: Nitin Gupta & Vipin Gupta
 * @created March 12,2003
 * @version 1.0
 */

public class IMLorProbSelectorGuiBean extends ParameterListEditor implements
    ParameterChangeListener{


  //definition of the class final static variables
  private final static String IML_AT_PROB="IML@Prob";
  private final static String PROB_AT_IML="Prob@IML";
  private final static String PROBABILITY="Probability";
  private final static String MAP_TYPE = "Map Type";
  private final static String IML="IML";
  private final static String MAP_INFO="Set Map Params";
  private final static Double MIN_PROB=new Double(0);
  private final static Double MAX_PROB=new Double(1);

  private StringParameter imlProb;

  //double parameters for inutting the values for the iml or prob.
  private DoubleParameter prob = new DoubleParameter(PROBABILITY,MIN_PROB,MAX_PROB);
  private DoubleParameter iml = new DoubleParameter(IML);

  /**
   * class constructor
   */
  public IMLorProbSelectorGuiBean() {

    // Build package names search path
    searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();

    //combo Box that provides the user to choose either the IML@prob or vis-a-versa
    Vector imlProbVector=new Vector();

    imlProbVector.add(IML_AT_PROB);
    imlProbVector.add(PROB_AT_IML);
    imlProb = new StringParameter(MAP_TYPE,imlProbVector,imlProbVector.get(0).toString());
    parameterList= new ParameterList();
    parameterList.addParameter(imlProb);
    parameterList.addParameter(prob);
    parameterList.addParameter(iml);
    addParameters();
    this.setTitle(MAP_INFO);
  }

  /**
   * this function selects either the IML or Prob. to be entered by the user.
   * So, we update the site object as well.
   *
   * @param e
   */
  public void parameterChange(ParameterChangeEvent e) {
    if(parameterList.getParameter(MAP_TYPE).getValue().toString().equalsIgnoreCase(IML_AT_PROB))
      this.setParameterVisible(IML,false);
    else if(parameterList.getParameter(MAP_TYPE).getValue().toString().equalsIgnoreCase(PROB_AT_IML))
       this.setParameterVisible(PROBABILITY,false);
  }

  /**
   *
   * @return the double value for the iml or prob, depending on the MapType
   * selected by the user.
   */
 public double getIMLProb(){
   if(parameterList.getParameter(MAP_TYPE).getValue().toString().equalsIgnoreCase(IML_AT_PROB))
     return ((Double)iml.getValue()).doubleValue();
   else
     return ((Double)prob.getValue()).doubleValue();
 }
}