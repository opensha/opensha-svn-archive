package org.scec.sha.gui.beans;


import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;


import javax.swing.*;
import java.awt.*;

import org.scec.param.ParameterAPI;
import org.scec.param.StringParameter;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.editor.ConstrainedStringParameterEditor;
import org.scec.sha.earthquake.EqkRupture;
import org.scec.data.Location;
import org.scec.param.editor.ParameterEditor;

/**
 * <p>Title: Eqk Rupture Selector GuiBean</p>
 * <p>Description: This class will show ERF and its parameters. It will
 * also allow the user to select a particular rupture for scenario maps </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class EqkRupSelectorGuiBean extends JPanel implements ParameterChangeListener
{


  /**
   * Name of the class
   */
  protected final static String C = "EqkRupSelectorGuiBean";
  // for debug purpose
  protected final static boolean D = false;

  //parameter to select earthquake rupture either from ERF or create your own rupture
  public final static String RUPTURE_SELECTOR_PARAM_NAME = "Select method of getting eqk rupture";
  private StringParameter ruptureSelectorParam;
  private final static String RUPTURE_SELECTOR_PARAM_INFO = "Toggles between methods to allow user "+
      "to allow defining their own rupture or select one from the existing ERF's ";
  public final static String RUPTURE_FROM_EXISTING_ERF = "Select Eqk rupture from ERF models";
  public final static String CREATE_RUPTURE = "Create Rupture";
  private ConstrainedStringParameterEditor ruptureSelectorParamEditor;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  //Gui elements
  private JScrollPane eqkRuptureParameterScrollPane = new JScrollPane();
  private JPanel rupturePanel = new JPanel();


  //defines he classes that implement we use in this GUI bean to get the
  private EqkRupSelectorGuiBeanAPI eqkRupturePanelFromERF = null;
  private EqkRupSelectorGuiBeanAPI eqkRupturePanelFromRupture = null;
  private EqkRupSelectorGuiBeanAPI eqkRupturePanel = null;

  //checks to see if user has choosen to create his own eqk rupture or getting
  //already existing one from the ERF list.
  private boolean isUserCreatedEqkRupture = false;

  //supported ERF classes
  private ArrayList supportedERF_Classes;

  /**
  * Constructor : It accepts the classNames of the ERFs to be shown in the editor
  * @param erfClassNames
  */
 public EqkRupSelectorGuiBean(ArrayList erfClassNames) throws InvocationTargetException{
   supportedERF_Classes = erfClassNames;
   isUserCreatedEqkRupture = false;
   eqkRupturePanelFromERF = new EqkRuptureFromERFSelectorPanel(erfClassNames);
   eqkRupturePanel = eqkRupturePanelFromERF;
   try {
     jbInit();
   }
   catch(Exception e) {
     e.printStackTrace();
   }
   toggleRuptureSelectionMethods();
 }

  public EqkRupSelectorGuiBean() throws InvocationTargetException{

    eqkRupturePanelFromRupture = new EqkRuptureCreationPanel();
    eqkRupturePanel = eqkRupturePanelFromRupture;
    isUserCreatedEqkRupture = true;
    try {
      jbInit();
          }
    catch(Exception e) {
      e.printStackTrace();
    }
    toggleRuptureSelectionMethods();
  }


  private void jbInit() throws Exception {
    ArrayList ruptureSelectionMethodList = new ArrayList();
    ruptureSelectionMethodList.add(RUPTURE_FROM_EXISTING_ERF);
    ruptureSelectionMethodList.add(CREATE_RUPTURE);
    ruptureSelectorParam = new StringParameter(RUPTURE_SELECTOR_PARAM_NAME,ruptureSelectionMethodList,
        (String)ruptureSelectionMethodList.get(0));
    ruptureSelectorParam.setInfo(RUPTURE_SELECTOR_PARAM_INFO);
    ruptureSelectorParam.addParameterChangeListener(this);
    ruptureSelectorParamEditor = new ConstrainedStringParameterEditor(ruptureSelectorParam);
    this.setLayout(gridBagLayout1);
    rupturePanel.setLayout(gridBagLayout2);
    this.setMinimumSize(new Dimension(0, 0));
    rupturePanel.setMinimumSize(new Dimension(0, 0));
    this.add(eqkRuptureParameterScrollPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 3, 5, 5), 0, 0));
    eqkRuptureParameterScrollPane.getViewport().add(rupturePanel, null);
    rupturePanel.add(ruptureSelectorParamEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
    eqkRuptureParameterScrollPane.validate();
    eqkRuptureParameterScrollPane.repaint();
  }

  /**
   *  This is the main function of this interface. Any time a control
   *  paramater or independent paramater is changed by the user in a GUI this
   *  function is called, and a paramater change event is passed in. This
   *  function then determines what to do with the information ie. show some
   *  paramaters, set some as invisible, basically control the paramater
   *  lists.
   *
   * @param  event
   */
  public void parameterChange( ParameterChangeEvent event ) {

    String S = C + ": parameterChange(): ";
    if ( D )
      System.out.println( "\n" + S + "starting: " );

    String name1 = event.getParameterName();

    // if ERF selected by the user  changes
    if( name1.equals(RUPTURE_SELECTOR_PARAM_NAME) ){
      String paramVal  = (String)event.getNewValue();
      if(paramVal.equals(RUPTURE_FROM_EXISTING_ERF))
        isUserCreatedEqkRupture = false;
      else if(paramVal.equals(CREATE_RUPTURE))
        isUserCreatedEqkRupture = true;
      //based on user selection toggles between allowing user to select rupture
      //from already existing ERF model
      try{
        toggleRuptureSelectionMethods();
      }catch(InvocationTargetException e){
        throw new RuntimeException(e.getMessage());
      }
    }
  }


  /**
   * Toggles between the visible panel for selecting the rupture from existing ERF
   * and
   * allowing the user to create his own rupture.
   */
  private void toggleRuptureSelectionMethods() throws InvocationTargetException{
    rupturePanel.remove((JPanel)eqkRupturePanel);
    if(!isUserCreatedEqkRupture){
       //if user has chosen to select eqk rupture from already existing ERF model
      if(eqkRupturePanelFromERF == null)
        eqkRupturePanelFromERF = new EqkRuptureFromERFSelectorPanel(supportedERF_Classes);
      eqkRupturePanel = eqkRupturePanelFromERF;
    }
    else{ //if user has chosen to create his own rupture.
      //if user has chosen to select eqk rupture from already existing ERF model
      if(eqkRupturePanelFromRupture == null)
        eqkRupturePanelFromRupture = new EqkRuptureCreationPanel();
      eqkRupturePanel = eqkRupturePanelFromRupture;
    }

    rupturePanel.add((JPanel)eqkRupturePanel.getEqkRuptureSelectorPanel(),  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));

    this.validate();
    this.repaint();
  }

  /**
    *
    * @returns the Hypocenter Location if selected else return null
    */
   public Location getHypocenterLocation(){
     return eqkRupturePanel.getHypocenterLocation();
  }

  /**
   *
   * @returns the EqkRupture Object
   */
  public EqkRupture getRupture(){
    return eqkRupturePanel.getRupture();
  }


  /**
   *
   * @returns the timespan Metadata for the selected Rupture.
   * If no timespan exists for the rupture then it returns the Message:
   * "No Timespan exists for the selected Rupture".
   */
  public String getTimespanMetadataString(){
    return eqkRupturePanel.getTimespanMetadataString();
  }


  /**
   *
   * @returns the Metadata String of parameters that constitute the making of this
   * ERF_RupSelectorGUI  bean.
   */
  public String getParameterListMetadataString(){
    return eqkRupturePanel.getParameterListMetadataString();
  }

  /**
   *
   * @param paramName
   * @returns the parameter from list of visible parameters in the rupture selector/creator GUI.
   */
  public ParameterAPI getParameter(String paramName){
    if(paramName.equals(this.RUPTURE_SELECTOR_PARAM_NAME))
      return ruptureSelectorParam;
    else{
        return eqkRupturePanel.getParameter(paramName);
    }
  }

  /**
   *
   * @param paramName
   * @returns the ParameterEditor associated with paramName
   */
  public ParameterEditor getParameterEditor(String paramName){
    if(paramName.equals(this.RUPTURE_SELECTOR_PARAM_NAME))
     return this.ruptureSelectorParamEditor;
   else{
       return eqkRupturePanel.getParameterEditor(paramName);
    }
  }

  /**
   *
   * @returns the instance to the Selected mode of Rupture calculator.
   * If user has chosen to get the Eqk rupture from ERF model,then the
   * returned  EqkRupSelectorGuiBeanAPI will the instance of EqkRuptureFromERFSelectorPanel.
   * Else if the user to selected to create his own rupture then returned API will
   * be the instance of EqkRuptureCreationPanel.
   */
  public EqkRupSelectorGuiBeanAPI getEqkRuptureSelectorPanel(){
    return eqkRupturePanel;
  }


}
