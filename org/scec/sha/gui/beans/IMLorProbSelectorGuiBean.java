package org.scec.sha.gui.beans;

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
  public final static String IML_AT_PROB="IML@Prob";
  public final static String PROB_AT_IML="Prob@IML";
  public final static String PROBABILITY="Probability";
  public final static String MAP_TYPE = "Map Type";
  private final static String IML="IML";
  private final static String MAP_INFO="Set What To Plot";
  private final static Double MIN_PROB=new Double(0);
  private final static Double MAX_PROB=new Double(1);
  private final static Double DEFAULT_PROB= new Double(.5);
  private final static Double DEFAULT_IML = new Double(.1);

  private StringParameter imlProb;

  //double parameters for inutting the values for the iml or prob.
  private DoubleParameter prob = new DoubleParameter(PROBABILITY,MIN_PROB,MAX_PROB,DEFAULT_PROB);
  private DoubleParameter iml = new DoubleParameter(IML,DEFAULT_IML);

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
    imlProb.addParameterChangeListener(this);
    parameterList= new ParameterList();
    parameterList.addParameter(imlProb);
    parameterList.addParameter(prob);
    parameterList.addParameter(iml);
    addParameters();
    this.setTitle(MAP_INFO);
    setParams(imlProb.getValue().toString());
  }

  /**
   * this function selects either the IML or Prob. to be entered by the user.
   * So, we update the site object as well.
   *
   * @param e
   */
  public void parameterChange(ParameterChangeEvent e) {
    String name = e.getParameterName();
    // if user changes the map type desired
    if(name.equalsIgnoreCase(this.MAP_TYPE)) {
      // make the IML@Prob visible or Prob@IML as visible
      setParams(parameterList.getParameter(MAP_TYPE).getValue().toString());
    }
  }

  /**
   * Make the IML@Prob or Prob@IML as visible, invisible based on map type selected
   * @param mapType
   */
  private void setParams(String mapType) {
    if(mapType.equalsIgnoreCase(IML_AT_PROB)) { // if IML@prob is selected
      this.setParameterVisible(IML,false);
      this.setParameterVisible(PROBABILITY, true);
    } else { // if Prob@IML is selected
      this.setParameterVisible(PROBABILITY,false);
      this.setParameterVisible(IML, true);
    }
  }

  /**
   *
   * @return the double value for the iml or prob, depending on the MapType
   * selected by the user.
   */
 public double getIML_Prob(){
   if(parameterList.getParameter(MAP_TYPE).getValue().toString().equalsIgnoreCase(IML_AT_PROB))
     return ((Double)prob.getValue()).doubleValue();
   else return ((Double)iml.getValue()).doubleValue();
 }

 /**
  * returns whether IML@Prob is selcted or Prob@IML
  * @return
  */
 public String getSelectedOption() {
   return this.imlProb.getValue().toString();
 }
}
