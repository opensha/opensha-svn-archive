package org.scec.sha.gui.beans;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.param.editor.*;
import org.scec.sha.imr.*;
import org.scec.sha.gui.infoTools.AttenuationRelationshipsInstance;

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

public class MultipleIMR_GuiBean extends JPanel  implements IMR_GuiBeanAPI,
    ActionListener,ItemListener,ParameterChangeListener,
    ParameterChangeWarningListener, ParameterChangeFailListener{


  private static final String C = "MultipleIMR_GuiBean";
  private static final boolean D = false;

  //list of the supported AttenuationRelationships
  ArrayList attenRelsSupported;

  //number of the supported Attenuation Relatoinships
  int numSupportedAttenRels ;

  //Gui elements
  private JScrollPane jScrollPane1 = new JScrollPane();


  //instance of the class to create the objects of the AttenuationRelationships dynamically.
  AttenuationRelationshipsInstance attenRelInstances = new AttenuationRelationshipsInstance();


  //keeps the indexing for the Attenuation Relationship for which any event is generated.
  private int indexOfAttenRel;

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
  // this flag is needed else messages are shown twice on focus lost
  private boolean inParameterChangeWarning = false;

  private JPanel imrPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();

  //Instance of the application using this Gui Bean.
  AttenuationRelationshipSiteParamsRegionAPI application;

  /**
   *
   * @param api : Instance of the application using this GUI bean.
   */
  public MultipleIMR_GuiBean(AttenuationRelationshipSiteParamsRegionAPI api)
  {
    application = api;
    //initializing all the array of the GUI elements to be the number of the supported AtrtenuationRelationships.
    attenRelsSupported = attenRelInstances.createIMRClassInstance(this);
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
      attenRelCheckBox[i].addItemListener(this);
      wtsParameter[i] = new DoubleParameter(wtsParamName+i,new Double(1.0));
      wtsParameterEditor[i] = new DoubleParameterEditor(wtsParameter[i]);
      imrPanel.add(attenRelCheckBox[i],new GridBagConstraints(0, i, 1, 1, 1.0, 1.0
            ,GridBagConstraints.WEST, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
      imrPanel.add(wtsParameterEditor[i],new GridBagConstraints(1, i, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
      paramButtons[i] = new JButton(attenRelParamsButtonName);
      paramButtons[i].addActionListener(this);
      imrPanel.add(paramButtons[i],new GridBagConstraints(2, i, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
    }

    imrPanel.add(setAllParamButtons,new GridBagConstraints(0, numSupportedAttenRels, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.CENTER, new Insets(4, 3, 5, 5), 0, 0));

    setIMR_Params();
    setAllParamButtons.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAllParamButtons_actionPerformed(e);
      }
    });
  }


  /**
   *
   * @returns the Object for the supported Attenuation Relationships in a ArrayList
   */
  public ArrayList getSupportedAttenuationRelationships(){
    return attenRelsSupported;
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
          paramList[i].addParameter(tempParam);
        //adding the other common parameters ( same for all attenuation relationship)
        // to the list of the other param list.
        else if(!otherParams.containsParameter(tempParam.getName()))
          otherParams.addParameter(tempParam);
        //adding the change listener events to the parameters
        tempParam.addParameterChangeFailListener(this);
        tempParam.addParameterChangeListener(this);
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

    // set the trunc level based on trunc type
    String value = (String)otherParams.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).getValue();
    toggleSigmaLevelBasedOnTypeValue(value);
  }


  /**
   * Method definition for the "Set All Identical Params".
   * This function will set each of the identical param value in the
   * selected AttenuationRelationship.
   * @param e
   */
  public void setAllParamButtons_actionPerformed(ActionEvent e){
    indexOfAttenRel = 0;
    otherIMR_paramsFrame.pack();
    otherIMR_paramsFrame.show();
  }

  /**
   * This is a common function if any action is performed on the AttenuationRelationship
   * associated parameters button.
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
          indexOfAttenRel = i;
          //getting the AttenRel params from the AttenRel whose button was pressed
          imrParamsFrame[i].pack();
          imrParamsFrame[i].show();
        }
      }
    }
  }

  /**
   * This is a common function if any action is performed on the AttenuationRelationship
   * check boxes.
   * It checks what is the source of the action and depending on the source how will it
   * response to it.
   * @param e
   */
  public void itemStateChanged(ItemEvent e) {
    //if the source of event is CheckBox then perform the action accordingly
    if(e.getSource() instanceof JCheckBox){
      Object attenRelCheck = e.getSource();
      System.out.println("Inside the AttenRel checkBox action performed");
      for(int i=0;i<numSupportedAttenRels;++i){
        if(attenRelCheck.equals(attenRelCheckBox[i])){
          if(attenRelCheckBox[i].isSelected()){
            paramButtons[i].setEnabled(true);
            wtsParameterEditor[i].setVisible(true);
          }
          else{
            paramButtons[i].setEnabled(false);
            wtsParameterEditor[i].setVisible(false);
          }
        }
      }
      application.setGriddedRegionSiteParams();
    }
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

     String name1 = event.getParameterName();

     // if Truncation type changes
     if( name1.equals(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME) ){  // special case hardcoded. Not the best way to do it, but need framework to handle it.
       String value = event.getNewValue().toString();
       toggleSigmaLevelBasedOnTypeValue(value);
     }
   }



   /**
    * sigma level is visible or not
    * @param value
    */
   private void toggleSigmaLevelBasedOnTypeValue(String value){
     if( value.equalsIgnoreCase("none") ) {
       if(D) System.out.println("Value = " + value + ", need to set value param off.");
       otherParamsEditor.setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, false );
     }
     else{
       if(D) System.out.println("Value = " + value + ", need to set value param on.");
       otherParamsEditor.setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, true );
     }
   }


   /**
    * Shown when a Constraint error is thrown on a ParameterEditor
    *
    * @param  e  Description of the Parameter
    */
   public void parameterChangeFailed( ParameterChangeFailEvent e ) {

     String S = C + " : parameterChangeWarning(): ";


     StringBuffer b = new StringBuffer();

     ParameterAPI param = ( ParameterAPI ) e.getSource();


     ParameterConstraintAPI constraint = param.getConstraint();
     String oldValueStr = e.getOldValue().toString();
     String badValueStr = e.getBadValue().toString();
     String name = param.getName();

     // only show messages for visible site parameters
     AttenuationRelationshipAPI imr = null ;
     //currently I am handling the situation if this event occurs due to some
     //non-identical params for each AttenRel.
     //We might have to think of the situation if event occurs to the identical
     //params for AttenRel's, them I will have to iterate over all the selected AttenRel.
     if(indexOfAttenRel !=0)
       imr = (AttenuationRelationshipAPI)attenRelsSupported.get(indexOfAttenRel);
     ListIterator it = imr.getSiteParamsIterator();
     boolean found = false;
     // see whether this parameter exists in site param list for this IMR
     while(it.hasNext() && !found)
       if(((ParameterAPI)it.next()).getName().equalsIgnoreCase(name))
         found = true;

     // if this parameter for which failure was issued does not exist in
     // site parameter list, then do not show the message box
     if(!found)  return;



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
    *  Function that must be implemented by all Listeners for
    *  ParameterChangeWarnEvents.
    *
    * @param  event  The Event which triggered this function call
    */
   public void parameterChangeWarning( ParameterChangeWarningEvent e ){

     String S = C + " : parameterChangeWarning(): ";
     WarningParameterAPI param = e.getWarningParameter();

     //check if this parameter exists in the site param list of this IMR
     // if it does not then set its value using ignore warningAttenuationRelationshipAPI imr ;
     // only show messages for visible site parameters
     AttenuationRelationshipAPI imr =null;
     //currently I am handling the situation if this event occurs due to some
     //non-identical params for each AttenRel.
     //We might have to think of the situation if event occurs to the identical
     //params for AttenRel's, them I will have to iterate over all the selected AttenRel.
     if(indexOfAttenRel !=0)
       imr = (AttenuationRelationshipAPI)attenRelsSupported.get(indexOfAttenRel);
     ListIterator it = imr.getSiteParamsIterator();
     boolean found = false;
     while(it.hasNext() && !found)
       if(param.getName().equalsIgnoreCase(((ParameterAPI)it.next()).getName()))
         found = true;
     if(!found) {
       param.setValueIgnoreWarning(e.getNewValue());
       return;
     }


     // if it is already processing a warning, then return
     if(inParameterChangeWarning) return;
     inParameterChangeWarning = true;

     StringBuffer b = new StringBuffer();

     try{
       Double min = (Double)param.getWarningMin();
       Double max = (Double)param.getWarningMax();

       String name = param.getName();

       b.append( "You have exceeded the recommended range for ");
       b.append( name );
       b.append( ": (" );
       b.append( min.toString() );

       b.append( " to " );
       b.append( max.toString() );
       b.append( ")\n" );
       b.append( "Click Yes to accept the new value: " );
       b.append( e.getNewValue().toString() );
     }
     catch( Exception ee){

       String name = param.getName();

       b.append( "You have exceeded the recommended range for: \n");
       b.append( name + '\n' );
       b.append( "Click Yes to accept the new value: " );
       b.append( e.getNewValue().toString() );
       b.append( name );
     }


     int result = 0;

     result = JOptionPane.showConfirmDialog( this, b.toString(),
         "Exceeded Recommended Values", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

     if(D) System.out.println(S + "You choose" + result);

     switch (result) {
       case JOptionPane.YES_OPTION:
         if(D) System.out.println(S + "You choose yes, changing value to " + e.getNewValue().toString() );
         param.setValueIgnoreWarning( e.getNewValue());
         break;
       case JOptionPane.NO_OPTION:
         if(D) System.out.println(S + "You choose no, keeping value = " + e.getOldValue().toString() );
         param.setValueIgnoreWarning( e.getOldValue() );
         break;
       default:
         param.setValueIgnoreWarning( e.getOldValue() );
       if(D) System.out.println(S + "Not sure what you choose, not changing value.");
       break;
     }
     inParameterChangeWarning = false;
     if(D) System.out.println(S + "Ending");
   }

   /**
    * Whether to show the warning messages or not
    * In some cases, we may not want to show warning messages.
    * Presently it is being used in HazardCurveApplet
    * @param show
    */
   public void showWarningMessages(boolean show){
     inParameterChangeWarning = !show;
   }


   /**
    * Selects the AttenuationRelationship models only if they are
    * supported by the selected IMT.
    * @param attenRelName : AttenRel name which needs to be
    * selected to do the calculation.
    * It makes only one AttenRel selected at a time
    */
   public void setIMR_Selected(String attenRelName){
     for(int i=0;i < numSupportedAttenRels;++i){
       if(attenRelCheckBox[i].getName().equals(attenRelName)){
         if(attenRelCheckBox[i].isEnabled() && !attenRelCheckBox[i].isSelected()){
           attenRelCheckBox[i].setSelected(true);
         }
       }
     }
   }

   /**
    *
    * @returns the selected IMRs instances in the ArrayList
    */
   public ArrayList getSelectedIMRs(){

     ArrayList selectedIMRs = new ArrayList();
     for(int i=0;i < numSupportedAttenRels;++i)
       if(attenRelCheckBox[i].isEnabled() && attenRelCheckBox[i].isSelected())
         selectedIMRs.add(attenRelsSupported.get(i));
     return selectedIMRs;
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
      if(!attenRel.isIntensityMeasureSupported(param)){
        attenRelCheckBox[i].setSelected(false);
        attenRelCheckBox[i].setEnabled(false);
      }
      else{
        attenRelCheckBox[i].setSelected(true);
        attenRelCheckBox[i].setEnabled(true);
      }
    }
  }
}