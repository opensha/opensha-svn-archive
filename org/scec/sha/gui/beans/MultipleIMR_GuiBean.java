package org.scec.sha.gui.beans;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.sha.imr.*;

/**
 * <p>Title: MultipleIMR_GuiBean</p>
 * <p>Description: This class gives the user the option of selecting multiple
 * attenuationrelationships and their supported parameters parameters. This
 * enables the user to select from only those Attenuationrelationship which support
 * the selected IMT and its parameters</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created : 03 March,2004
 * @version 1.0
 */

public class MultipleIMR_GuiBean extends JPanel  implements ActionListener{

  //list of the supported AttenuationRelationships
  ArrayList attenRelsSupported;
  //number of the supported Attenuation Relatoinships
  int numSupportedAttenRels ;

  //Gui elements
  private JScrollPane jScrollPane1 = new JScrollPane();


  //name of the attenuationrelationship weights parameter
  public static final String wtsParamName = "Wgt-";
  public static final String attenRelParamsButtonName = "Set IMR Params";
  public static final String attenRelNonIdenticalParams = "Set IMT non-identical params";
  public static final String attenRelIdenticalParamsFrameTitle = "IMR's identical params";
  public static final String attenRelIdenticalParams = "Set IMR identical params";

  //Dynamic Gui elements array to generate the AttenRel components on the fly.
  private JCheckBox[] attenRelCheckBox;
  private JButton[] paramButtons;
  private DoubleParameter[] wtsParameter;
  private DoubleParameterEditor[] wtsParameterEditor;
  private ParameterList paramList[];
  private ParameterListEditor editor[];
  private JDialog imrParamsFrame[];

  private ParameterList otherParams= new ParameterList();
  private ParameterListEditor otherParamsEditor;
  private JDialog otherIMR_paramsFrame;
  private JButton setAllParamButtons = new JButton("Set All Params");
  private JPanel imrPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();

  /**
   *
   * @param supportedAttenRels
   */
  public MultipleIMR_GuiBean(ArrayList supportedAttenRels)
  {
    //initializing all the array of the GUI elements to be the number of the supported AtrtenuationRelationships.
    attenRelsSupported = supportedAttenRels;
    numSupportedAttenRels = attenRelsSupported.size();
    attenRelCheckBox = new JCheckBox[numSupportedAttenRels];
    paramButtons = new JButton[numSupportedAttenRels];
    wtsParameter = new DoubleParameter[numSupportedAttenRels];
    wtsParameterEditor = new DoubleParameterEditor[numSupportedAttenRels];
    paramList = new ParameterList[numSupportedAttenRels];
    editor = new ParameterListEditor[numSupportedAttenRels];
    imrParamsFrame = new JDialog[numSupportedAttenRels];
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * function to create the Gui elements
   * @throws Exception
   */
  void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    imrPanel.setLayout(gridBagLayout2);
    this.add(jScrollPane1,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 3, 5, 5), 377, 469));
    jScrollPane1.getViewport().add(imrPanel, null);
    for(int i=0;i<numSupportedAttenRels;++i){
      attenRelCheckBox[i] = new JCheckBox(((AttenuationRelationshipAPI)attenRelsSupported.get(i)).getName());
      attenRelCheckBox[i].addActionListener(this);
      wtsParameter[i] = new DoubleParameter(wtsParamName+i,new Double(1.0));
      wtsParameterEditor[i] = new DoubleParameterEditor(wtsParameter[i]);
      imrPanel.add(attenRelCheckBox[i],new GridBagConstraints(0, i, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
      imrPanel.add(wtsParameterEditor[i],new GridBagConstraints(1, i, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
      paramButtons[i] = new JButton(attenRelParamsButtonName);
      paramButtons[i].addActionListener(this);
      imrPanel.add(paramButtons[i],new GridBagConstraints(2, i, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
    }

    imrPanel.add(setAllParamButtons,new GridBagConstraints(0, numSupportedAttenRels, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 3, 5, 5), 0, 0));

    setIMR_Params();
    setAllParamButtons.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAllParamButtons_actionPerformed(e);
      }
    });
  }


  /**
   * Adds all the AttenuationRelationship related parameters to the parameter list for all the
   * supported AttenuationRelationship. Creates the subsequent editor for these parameterlists
   * and add them to the frame window.
   */
  private void setIMR_Params(){
    //Panel Parent
    Container parent = this;
    /*This loops over all the parent of this class until the parent is Frame(applet)
    this is required for the passing in the JDialog to keep the focus on the adjustable params
    frame*/
    while(!(parent instanceof JFrame) && parent != null)
      parent = parent.getParent();

    for(int i=0;i<numSupportedAttenRels;++i){
      paramList[i] = new ParameterList();
      ListIterator it =((AttenuationRelationshipAPI)attenRelsSupported.get(i)).getOtherParamsIterator();
      //iterating over all the Attenuation relationship parameters for the IMR.
      while(it.hasNext()){
        ParameterAPI tempParam  = (ParameterAPI)it.next();
        if(!tempParam.getName().equals(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME) &&
           !tempParam.getName().equals(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME))
          paramList[i].addParameter((ParameterAPI)it.next());
        //adding the other common parameters ( same for all attenuation relationship)
        // to the list of the other param list.
        else if(!otherParams.containsParameter(tempParam.getName()))
          otherParams.addParameter(tempParam);
      }

      //showing the parameter editors of the AttenRel in a window
      editor[i] = new ParameterListEditor(paramList[i]);
      editor[i].setTitle(attenRelNonIdenticalParams);
      imrParamsFrame[i] = new JDialog((JFrame)parent,((AttenuationRelationshipAPI)attenRelsSupported.get(i)).getName()+" Params");
      imrParamsFrame[i].setSize(230,300);
      imrParamsFrame[i].getContentPane().setLayout(new GridBagLayout());
      imrParamsFrame[i].getContentPane().add(editor[i],new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    }

    //creating the parameterList editor for the Other parameters editor and
    //putting this editor in a frame to be shown in the window
    otherParamsEditor = new ParameterListEditor(otherParams);
    otherParamsEditor.setTitle(attenRelIdenticalParams);
    otherIMR_paramsFrame = new JDialog((JFrame)parent,attenRelIdenticalParamsFrameTitle);
    otherIMR_paramsFrame.setSize(230,300);
    otherIMR_paramsFrame.getContentPane().setLayout(new GridBagLayout());
    otherIMR_paramsFrame.getContentPane().add(otherParamsEditor,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
  }


  /**
   * Method definition for the "Set All Identical Params".
   * This function will set each of the identical param value in the
   * selected AttenuationRelationship.
   * @param e
   */
  public void setAllParamButtons_actionPerformed(ActionEvent e){
    otherIMR_paramsFrame.pack();
    otherIMR_paramsFrame.show();
  }

  /**
   * This is a common function if any action is performed on the AttenuationRelationship
   * check box or its associated parameters button.
   * It checks what is the source of the action and depending on the source how will it
   * response to it.
   * @param e
   */
  public void actionPerformed(ActionEvent e){
    //checking if the source of the action was the button
    if(e.getSource() instanceof JButton){
      Object button = e.getSource();
      //if the source of the event was IMR param button then loop over all the buttons ot check which
      //is it actually.
      for(int i=0;i<numSupportedAttenRels;++i){
        if(button.equals(paramButtons[i])){
          //getting the AttenRel params from the AttenRel whose button was pressed
          imrParamsFrame[i].pack();
          imrParamsFrame[i].show();
        }
      }
    }
    //if the source of event is CheckBox then perform the action accordingly
    else if(e.getSource() instanceof JCheckBox){
      Object attenRelCheck = e.getSource();
      for(int i=0;i<numSupportedAttenRels;++i){
        if(attenRelCheck.equals(attenRelCheckBox[i])){
          if(attenRelCheckBox[i].isSelected()){
            paramButtons[i].setEnabled(false);
            wtsParameterEditor[i].setEnabled(false);
          }
        }
      }
    }
  }

  /**
   * Checks to see if the Intensity Measure is supported by the AttenuationRelationship.
   * If it is supported make its parameters and check box enabled and set the
   * parameters default values, else disable the choice of that AttenuationRelationship.
   */
  public void setIntensityMeasure(ParameterAPI param){

    //Iterating over all the supported AttenRels ot check if they support the selected IMT
    for(int i=0;i < numSupportedAttenRels;++i){
      AttenuationRelationship attenRel = (AttenuationRelationship)attenRelsSupported.get(i);
      if(attenRel.isIntensityMeasureSupported(param))
        attenRelCheckBox[i].setEnabled(false);
    }
  }
}