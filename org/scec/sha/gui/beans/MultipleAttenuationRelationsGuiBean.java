package org.scec.sha.gui.beans;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javax.swing.*;

import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.param.editor.*;
import org.scec.sha.imr.*;
import org.scec.sha.gui.infoTools.AttenuationRelationshipsInstance;

/**
 * <p>Title: MultipleIMR_GuiBean</p>
 * <p>Description: This class gives the user the option of selecting multiple
 * attenuationrelationships and their supported parameters. This
 * enables the user to select from only those Attenuationrelationship which support
 * the selected IMT and its parameters</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created : 03 March,2004
 * @version 1.0
 */

public class MultipleAttenuationRelationsGuiBean extends JPanel  implements
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

  //keeps the index of the last button pressed, so as to keep track of parameter list editor shown
  //So helps in corresponding to shown parameterList editor with the AttenuationRelation model.
  private int lastAttenRelButtonIndex;

  //name of the attenuationrelationship weights parameter
  public static final String wtsParamName = "Wgt-";
  public static final String attenRelParamsButtonName = "Set IMR Params";
  public static final String attenRelNonIdenticalParams = "Set IMT non-identical params";
  public static final String attenRelIdenticalParamsFrameTitle = "IMR's identical params";
  public static final String attenRelIdenticalParams = "Set IMR identical params";

  //Dynamic Gui elements array to generate the AttenRel components on the fly.
  private JCheckBox[] attenRelCheckBox;
  private JButton[] paramButtons;

  //AttenuationRelationship parameters and list declaration
  private DoubleParameter[] wtsParameter;
  private DoubleParameterEditor[] wtsParameterEditor;
  private ParameterList paramList[];
  private ParameterListEditor editor[];
  private JDialog imrParamsFrame[];

  /*private ParameterList otherParams= new ParameterList();
  private ParameterListEditor otherParamsEditor;
  private JDialog otherIMR_paramsFrame;
  private JButton setAllParamButtons = new JButton("Set All Params");*/
  // this flag is needed else messages are shown twice on focus lost
  private boolean inParameterChangeWarning = false;




  //IMT Parameter and List declaration
  private ParameterList imtParamList;
  private ParameterListEditor imtEditorParamListEditor;
  // IMT GUI Editor & Parameter names
  public final static String IMT_PARAM_NAME =  "IMT";
  // IMT Panel title
  public final static String IMT_EDITOR_TITLE =  "Set IMT";

  //stores the IMT Params for the choosen IMR
  private ArrayList imtParam;


  private JPanel imrPanel = new JPanel();
  private JPanel imrimtPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();


  //static string declaration
  private static String MULTIPLE_ATTEN_REL = "Show Multiple AttenRel";
  private static String SINGLE_ATTEN_REL = "Show Single AttenRel";
  // IMR GUI Editor & Parameter names
  public final static String IMR_PARAM_NAME = "IMR";
  public final static String IMR_EDITOR_TITLE =  "Set IMR";

  //toggle button between the multiple attenuation and single Attenuation relationship GUI.
  private JButton toggleButton = new JButton(MULTIPLE_ATTEN_REL);

  //Flag to keep check which if single multiple attenRel is being used.
  private boolean singleAttenRelSelected = true;

  //ParameterList and Editor declaration for the single AttenRels selection
  ParameterList singleAttenRelParamList = new ParameterList();
  ParameterListEditor singleAttenRelParamListEditor;



  //Instance of the application using this Gui Bean.
  AttenuationRelationshipSiteParamsRegionAPI application;


  /**
   *
   * @param api : Instance of the application using this GUI bean.
   */
  public MultipleAttenuationRelationsGuiBean(AttenuationRelationshipSiteParamsRegionAPI api)
  {
    application = api;
    //initializing all the array of the GUI elements to be the number of the supported AtrtenuationRelationships.
    attenRelsSupported = attenRelInstances.createIMRClassInstance(this);

    numSupportedAttenRels = attenRelsSupported.size();

    //setting the default parameters value for all the attenuationRelationship object.
    for(int i=0;i<numSupportedAttenRels;++i)
      ((AttenuationRelationshipAPI)attenRelsSupported.get(i)).setParamDefaults();

    //creates the IMT paameterList editor for the supported IMRs
    init_imtParamListAndEditor();

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
    imrimtPanel.setLayout(gridBagLayout2);
    imrPanel.setLayout(gridBagLayout3);
    this.setMinimumSize(new Dimension(0, 0));
    this.setPreferredSize(new Dimension(430, 539));
    imrimtPanel.setMinimumSize(new Dimension(0, 0));
    toggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toggleButton_actionPerformed(e);
      }
    });
    this.add(jScrollPane1,  new GridBagConstraints(0, 0, 0, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 3, 5, 5), 377, 469));
    jScrollPane1.getViewport().add(imrimtPanel, null);
    //adding the IMT parameter list editor as the first element in the AttenRel Selection panel
    imrimtPanel.add(imtEditorParamListEditor,new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 3, 5, 5), 0, 0));
    //adding the toogle button to the GUI, lets the user switch between the single and multiple AttenRels
    imrimtPanel.add(toggleButton,new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.EAST, new Insets(7, 200, 5, 5), 15, 5));
    //adding the panel to add the AttenRels and their parameter.
    imrimtPanel.add(imrPanel,new GridBagConstraints(0, 2, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    for(int i=0;i<numSupportedAttenRels;++i){
      attenRelCheckBox[i] = new JCheckBox(((AttenuationRelationshipAPI)attenRelsSupported.get(i)).getName());
      attenRelCheckBox[i].addItemListener(this);
      wtsParameter[i] = new DoubleParameter(wtsParamName+(i+1),new Double(1.0));
      wtsParameterEditor[i] = new DoubleParameterEditor(wtsParameter[i]);
      imrPanel.add(attenRelCheckBox[i],new GridBagConstraints(0, i+1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.WEST, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
      imrPanel.add(wtsParameterEditor[i],new GridBagConstraints(1, i+1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
      paramButtons[i] = new JButton(attenRelParamsButtonName);
      paramButtons[i].addActionListener(this);
      imrPanel.add(paramButtons[i],new GridBagConstraints(2, i+1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
    }

    /*imrPanel.add(setAllParamButtons,new GridBagConstraints(0, numSupportedAttenRels+1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.EAST, new Insets(7, 200, 5, 5), 15, 5));*/

    setIMR_Params();
    /*setAllParamButtons.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAllParamButtons_actionPerformed(e);
      }
    });*/
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

        /*if(!tempParam.getName().equals(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME) &&
           !tempParam.getName().equals(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME))*/
          paramList[i].addParameter(tempParam);
        //adding the other common parameters ( same for all attenuation relationship)
        // to the list of the other param list.
        /*else if(!otherParams.containsParameter(tempParam.getName()))
          otherParams.addParameter(tempParam);*/

        //adding the change listener events to the parameters
        tempParam.addParameterChangeFailListener(this);
        tempParam.addParameterChangeListener(this);
      }

      //showing the parameter editors of the AttenRel in a window
      editor[i] = new ParameterListEditor(paramList[i]);
      editor[i].setTitle(attenRelNonIdenticalParams);
      imrParamsFrame[i] = new JDialog((JFrame)parent,((AttenuationRelationshipAPI)attenRelsSupported.get(i)).getName()+" Params");
      imrParamsFrame[i].setSize(300,200);
      imrParamsFrame[i].getContentPane().setLayout(new GridBagLayout());
      imrParamsFrame[i].getContentPane().add(editor[i],new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
      String value = (String)paramList[i].getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).getValue();
      toggleSigmaLevelBasedOnTypeValue(value,i);
    }

    //creating the parameterList editor for the Other parameters editor and
    //putting this editor in a frame to be shown in the window
    /*otherParamsEditor = new ParameterListEditor(otherParams);
    otherParamsEditor.setTitle(attenRelIdenticalParams);
    otherIMR_paramsFrame = new JDialog((JFrame)parent,attenRelIdenticalParamsFrameTitle);
    otherIMR_paramsFrame.setSize(300,200);
    otherIMR_paramsFrame.getContentPane().setLayout(new GridBagLayout());
    otherIMR_paramsFrame.getContentPane().add(otherParamsEditor,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));*/

    // set the trunc level based on trunc type
  }


  /**
   * Method definition for the "Set All Identical Params".
   * This function will set each of the identical param value in the
   * selected AttenuationRelationship.
   * @param e
   */
  /*public void setAllParamButtons_actionPerformed(ActionEvent e){
    indexOfAttenRel = 0;
    //otherIMR_paramsFrame.pack();
    otherIMR_paramsFrame.show();
  }*/

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
          //keeps the track which was the last button pressed, which will correspond to the AttenRel model.
          lastAttenRelButtonIndex = i;

          //getting the AttenRel params from the AttenRel whose button was pressed
          //imrParamsFrame[i].pack();
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

     // if IMT selection then update
     if (name1.equalsIgnoreCase(this.IMT_PARAM_NAME)) {
       updateIMT((String)event.getNewValue());
       selectIMRsForChoosenIMT();
     }
     // if Truncation type changes
     else if( name1.equals(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME) ){
       // special case hardcoded. Not the best way to do it, but need framework to handle it.
       String value = event.getNewValue().toString();
       toggleSigmaLevelBasedOnTypeValue(value, lastAttenRelButtonIndex);
     }
     else if(name1.equals(AttenuationRelationship.PERIOD_NAME)){
       selectIMRsForChoosenIMT();
     }

   }


   /**
    *  Create a list of all the IMTs
    */
   private void init_imtParamListAndEditor() {

     imtParamList = new ParameterList();

     //vector to store all the IMT's supported by an IMR
     ArrayList imt=new ArrayList();
     imtParam = new ArrayList();
     for(int i=0;i<numSupportedAttenRels;++i){
       Iterator it = ((AttenuationRelationshipAPI)attenRelsSupported.get(i)).getSupportedIntensityMeasuresIterator();

       //loop over each IMT and get their independent parameters
       while ( it.hasNext() ) {
         DependentParameterAPI param = ( DependentParameterAPI ) it.next();

         //check to see if the IMT param already exists in the vector list,
         //if so the get that parameter, else create new instance of the imt
         //parameter.
         DoubleParameter param1;
         if(imt.contains(param.getName())){
           int index = imtParam.indexOf(param);
           param1 = (DoubleParameter)imtParam.get(index);
         }
         else{
           param1=new DoubleParameter(param.getName(),(Double)param.getValue());
           //add the dependent parameter only if it has not ben added before
           imtParam.add(param1);
           imt.add(param.getName());
         }

         // add all the independent parameters related to this IMT
         // NOTE: this will only work for DoubleDiscrete independent parameters; it's not general!
         // this also converts these DoubleDiscreteParameters to StringParameters
         ListIterator it2 = param.getIndependentParametersIterator();
         if(D) System.out.println("IMT is:"+param.getName());
         while ( it2.hasNext() ) {
           ParameterAPI param2 = (ParameterAPI ) it2.next();
           DoubleDiscreteConstraint values = ( DoubleDiscreteConstraint )param2.getConstraint();
           // add all the periods relating to the SA
           ArrayList allowedValues = values.getAllowedValues();

           //create the string parameter for the independent parameter with its
           //constarint being the indParamOptions.
           DoubleDiscreteParameter independentParam = new DoubleDiscreteParameter(param2.getName(),
               values, (Double)allowedValues.get(0));

           // added by Ned so the default period is 1.0 sec (this is a hack).
           if( ((String) independentParam.getName()).equals(AttenuationRelationship.PERIOD_NAME) )
             independentParam.setValue(new Double(1.0));

           /**
            * Checks to see if the independent parameter by this name already
            * exists in the dependent parameterlist, if so then add the new constraints
            * values to the old ones but without any duplicity. Then create the
            * new constraint for the independent parameter.
            */
           if(param1.containsIndependentParameter(independentParam.getName())){
             ArrayList paramVals = ((DoubleDiscreteConstraint)param1.getIndependentParameter(independentParam.getName()).getConstraint()).getAllowedValues();
             for(int j=0;j<allowedValues.size();++j)
               if(!paramVals.contains(allowedValues.get(j)))
                 paramVals.add(allowedValues.get(j));
             DoubleDiscreteConstraint constraint = new DoubleDiscreteConstraint(paramVals);
             independentParam.setConstraint(constraint);
           }
           else //add the independent parameter to the dependent param list
             param1.addIndependentParameter(independentParam);
         }
       }
     }
     // add the IMT paramter
     StringParameter imtParameter = new StringParameter (IMT_PARAM_NAME,imt,
         (String)imt.get(0));
     imtParameter.addParameterChangeListener(this);
     imtParamList.addParameter(imtParameter);

     /* gets the iterator for each supported IMT and iterates over all its indepenedent
     * parameters to add them to the common ArrayList to display in the IMT Panel
     **/

     Iterator it=imtParam.iterator();

     while(it.hasNext()){
       Iterator it1=((DependentParameterAPI)it.next()).getIndependentParametersIterator();
       while(it1.hasNext()){
         ParameterAPI tempParam = (ParameterAPI)it1.next();
         imtParamList.addParameter(tempParam);
         tempParam.addParameterChangeListener(this);
       }
     }

     // now make the editor based on the paramter list
     imtEditorParamListEditor = new ParameterListEditor(imtParamList);
     imtEditorParamListEditor.setTitle( this.IMT_EDITOR_TITLE );
     // update the current IMT
     updateIMT((String)imt.get(0));
   }


   /**
    *  Create a list of all the IMRs
    */
   private void init_imrParamListAndEditor() {



     //gets the arrayList of the IMR's supported by the choosen IM
     ArrayList supportedAttenRelsForSelectedIM = getAttenRelsSupportedForSelectedIM();

     // if we are entering this function for the first time, then make imr objects
     if(!singleAttenRelParamList.containsParameter(IMR_PARAM_NAME)) {
       singleAttenRelParamList = new ParameterList();
       ArrayList imrNamesVector=new ArrayList();
       Iterator it= supportedAttenRelsForSelectedIM.iterator();
       while(it.hasNext()){
         // make the IMR objects as needed to get the site params later
         AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI )it.next();
         imr.setParamDefaults();
         imrNamesVector.add(imr.getName());
         Iterator it1 = imr.getSiteParamsIterator();
         // add change fail listener to the site parameters for this IMR
         while(it1.hasNext()) {
           ParameterAPI param = (ParameterAPI)it1.next();
           param.addParameterChangeFailListener(this);
         }
       }

       // make the IMR selection paramter
       StringParameter selectIMR = new StringParameter(IMR_PARAM_NAME,
           imrNamesVector,(String)imrNamesVector.get(0));
       // listen to IMR paramter to change site params when it changes
       selectIMR.addParameterChangeListener(this);
       singleAttenRelParamList.addParameter(selectIMR);
     }

     // remove all the parameters except the IMR parameter
     ListIterator it = singleAttenRelParamList.getParameterNamesIterator();
     while(it.hasNext()) {
       String paramName = (String)it.next();
       if(!paramName.equalsIgnoreCase(IMR_PARAM_NAME))
         singleAttenRelParamList.removeParameter(paramName);
     }


     // now find the selceted IMR and add the parameters related to it

     // initalize imr
     AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI)supportedAttenRelsForSelectedIM.get(0);

     // find & set the selectedIMR
     imr = this.getSelectedIMR_Instance();

     // getting the iterator of the Other Parameters for IMR
     /**
      * Instead of getting hard-coding for getting the selected Params Ned added
      * a method to get the OtherParams for the selected IMR, Otherwise earlier we
      * were getting the 3 params associated with IMR's by there name. But after
      * adding the method to get the otherParams, it can also handle params that
      * are customary to the selected IMR. So this automically adds the parameters
      * associated with the IMR, which are : SIGMA_TRUNC_TYPE_NAME, SIGMA_TRUNC_LEVEL_NAME,
      * STD_DEV_TYPE_NAME and any other param assoctade with the IMR.
      */
     ListIterator lt = imr.getOtherParamsIterator();
     while(lt.hasNext()){
       ParameterAPI tempParam=(ParameterAPI)lt.next();
       //adding the parameter to the parameterList.
       tempParam.addParameterChangeListener(this);
       parameterList.addParameter(tempParam);
     }

     this.editorPanel.removeAll();
     addParameters();
     setTitle(IMR_EDITOR_TITLE);

     // get the panel for increasing the font and border
     // this is hard coding for increasing the IMR font
     // the colors used here are from ParameterEditor
     JPanel panel = this.getParameterEditor(this.IMR_PARAM_NAME).getOuterPanel();
     TitledBorder titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(new Color( 80, 80, 140 ),3),"");
     titledBorder1.setTitleColor(new Color( 80, 80, 140 ));
     Font DEFAULT_LABEL_FONT = new Font( "SansSerif", Font.BOLD, 13 );
     titledBorder1.setTitleFont(DEFAULT_LABEL_FONT);
     titledBorder1.setTitle(IMR_PARAM_NAME);
     Border border1 = BorderFactory.createCompoundBorder(titledBorder1,BorderFactory.createEmptyBorder(0,0,3,0));
     panel.setBorder(border1);

     // set the trunc level based on trunc type
     String value = (String)parameterList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).getValue();
     toggleSigmaLevelBasedOnTypeValue(value);

   }


   /**
    * this method will return the name of selected IMR
    * @return : Selected IMR name
    */
   public String getSelectedIMR_Name() {
     return singleAttenRelParamList.getValue(IMR_PARAM_NAME).toString();
   }

   /**
    * This method will return the instance of selected IMR
    * @return : Selected IMR instance
    */
   public AttenuationRelationshipAPI getSelectedIMR_Instance() {
     AttenuationRelationshipAPI imr = null;
     String selectedIMR = getSelectedIMR_Name();
     int size = imrObject.size();
     for(int i=0; i<size ; ++i) {
       imr = (AttenuationRelationshipAPI)imrObject.get(i);
       if(imr.getName().equalsIgnoreCase(selectedIMR))
         break;
     }
     return imr;
   }



   /**
    * sigma level is visible or not
    * @param value
    */
   private void toggleSigmaLevelBasedOnTypeValue(String value, int buttonIndex){
     if( value.equalsIgnoreCase("none") ) {
       if(D) System.out.println("Value = " + value + ", need to set value param off.");
       editor[buttonIndex].setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, false );
     }
     else{
       if(D) System.out.println("Value = " + value + ", need to set value param on.");
       editor[buttonIndex].setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, true );
     }
   }

   /**
    * sigma level is visible or not
    * @param value
    */
   protected void toggleSigmaLevelBasedOnTypeValue(String value){

     if( value.equalsIgnoreCase("none") ) {
       if(D) System.out.println("Value = " + value + ", need to set value param off.");
       setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, false );
     }
     else{
       if(D) System.out.println("Value = " + value + ", need to set value param on.");
       setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, true );
     }

   }

   /**
    * It will return the IMT selected by the user
    * @return : IMT selected by the user
    */
   public String getSelectedIMT() {
     return (String)getSelectedIMTparam().getValue();
   }

   /**
    *
    * @returns the Selected IMT Parameter
    */
   public ParameterAPI getSelectedIMTparam(){
     return imtParamList.getParameter(IMT_PARAM_NAME);
   }

   /**
    *
    * @param paramName
    * @returns the parameter with the paramName from the IMT parameter list
    */
   public ParameterAPI getParameter(String paramName){
     return imtParamList.getParameter(paramName);
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
       if(attenRelCheckBox[i].getText().equals(attenRelName)){
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
    * set the IMT parameter in selected IMR's
    */
   public void setIMT() {
     ParameterAPI param = getSelectedIntensityMeasure();
     ArrayList selectedAttenRels = getSelectedIMRs();
     int size = selectedAttenRels.size();
     for(int i=0;i<size;++i)
       ((AttenuationRelationshipAPI)selectedAttenRels.get(i)).setIntensityMeasure(param);
   }


   /**
    * gets the selected Intensity Measure Parameter and its dependent Parameter
    * @return
    */
   public ParameterAPI getSelectedIntensityMeasure(){
     String selectedImt = imtParamList.getValue(this.IMT_PARAM_NAME).toString();
     //set all the  parameters related to this IMT
     return getSelectedIntensityMeasure(selectedImt);
 }




 /**
  * gets the selected Intensity Measure Parameter and its dependent Parameter
  * for given IMT name
  * @param imtName
  */
 public ParameterAPI getSelectedIntensityMeasure(String imtName){
   Iterator it= imtParam.iterator();
   while(it.hasNext()){
     DependentParameterAPI param=(DependentParameterAPI)it.next();
     if(param.getName().equalsIgnoreCase(imtName))
       return param;
   }
   return null;

 }



   /**
    * This function updates the IMTeditor with the independent parameters for the selected
    * IMT, by making only those visible to the user.
    * @param imtName : It is the name of the selected IMT, based on which we make
    * its independentParameters visible.
    */

   private void updateIMT(String imtName) {
     Iterator it= imtParamList.getParametersIterator();

     //making all the IMT parameters invisible
     while(it.hasNext())
       imtEditorParamListEditor.setParameterVisible(((ParameterAPI)it.next()).getName(),false);

     //making the choose IMT parameter visible
     imtEditorParamListEditor.setParameterVisible(IMT_PARAM_NAME,true);

     it=imtParam.iterator();
     //for the selected IMT making its independent parameters visible
     while(it.hasNext()){
       DependentParameterAPI param=(DependentParameterAPI)it.next();
       if(param.getName().equalsIgnoreCase(imtName)){
         Iterator it1=param.getIndependentParametersIterator();
         while(it1.hasNext())
           imtEditorParamListEditor.setParameterVisible(((ParameterAPI)it1.next()).getName(),true);
       }
     }

  }

   /**
    *
    * @returns the normalised weights for each selected attenuationRelationship
    */
   public ArrayList getSelectedIMR_Weights(){
     ArrayList wtsList = new ArrayList();
     double totalWts =0;
     for(int i=0;i < numSupportedAttenRels;++i){
       if(wtsParameterEditor[i].isVisible()){
         double value = ((Double)wtsParameterEditor[i].getValue()).doubleValue();
         totalWts +=value;
         wtsList.add(new Double(value));
       }
    }
    int size = wtsList.size();
    for(int i=0;i<size;++i)
      wtsList.set(i,new Double(((Double)wtsList.get(i)).doubleValue()/totalWts));

    return wtsList;
   }



   /**
    *
    * @returns the Intensity Measure Parameter Editor
    */
   public ParameterListEditor getIntensityMeasureParamEditor(){
     return imtEditorParamListEditor;
   }

   /**
    * Checks to see if the Intensity Measure is supported by the AttenuationRelationship.
    * If it is supported make its parameters and check box enabled and set the
    * parameters default values, else disable the choice of that AttenuationRelationship.
    */
   public void selectIMRsForChoosenIMT(){
     ParameterAPI param = getSelectedIntensityMeasure();
     //Iterating over all the supported AttenRels to check if they support the selected IMT
     String paramName = param.getName();

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


   /**
    * Returns the ArrayList of the AttenuationRelation being supported by the selected IM
    * @return
    */
   public ArrayList getAttenRelsSupportedForSelectedIM(){
     ParameterAPI param = getSelectedIntensityMeasure();
     //Iterating over all the supported AttenRels to check if they support the selected IMT
     String paramName = param.getName();
     ArrayList attenRelsSupportedForIM = new ArrayList();
     for(int i=0;i < numSupportedAttenRels;++i){
       AttenuationRelationship attenRel = (AttenuationRelationship)attenRelsSupported.get(i);
       if(attenRel.isIntensityMeasureSupported(param)){
         attenRelsSupportedForIM.add(attenRel);
       }
     }
     return attenRelsSupportedForIM;
   }



  /**
   *
   * @returns the Metadata string for the IMR Gui Bean
   */
  public String getParameterListMetadataString(){
    String metadata = "";
    for(int i=0;i<numSupportedAttenRels;++i){
      if(attenRelCheckBox[i].isSelected()){
        metadata += "AttenuationRelationship = "+((AttenuationRelationshipAPI)attenRelsSupported.get(i)).getName()+
              " ; "+ wtsParameter[i].getName()+" = "+wtsParameter[i].getValue()+" ; "+
              "Non Identical Param: "+editor[i].getVisibleParameters().toString()+"<br>\n";

      }
    }

    //metadata +=" Identical Param: "+otherParamsEditor.getVisibleParameters().toString();
    String imtMetadata = getIMT_ParameterListMetadataString()+"\n<br>";
    metadata = imtMetadata + metadata;
    return metadata;
  }


  /**
   *
   * @returns the Metadata string for the IMT Gui Bean
   */
 private String getIMT_ParameterListMetadataString(){
   String metadata=null;
   ListIterator it = imtEditorParamListEditor.getVisibleParameters().getParametersIterator();
   int paramSize = imtEditorParamListEditor.getVisibleParameters().size();
   while(it.hasNext()){
     //iterates over all the visible parameters
     ParameterAPI tempParam = (ParameterAPI)it.next();
     //if the param name is IMT Param then it is the Dependent param
     if(tempParam.getName().equals(this.IMT_PARAM_NAME)){
       metadata = tempParam.getName()+" = "+(String)tempParam.getValue();
       if(paramSize>1)
         metadata +="[ ";
     }
     else{ //rest all are the independent params
       metadata += tempParam.getName()+" = "+(String)tempParam.getValue()+" ; ";
     }
   }
   if(paramSize>1)
     metadata = metadata.substring(0,metadata.length()-3);
   if(paramSize >1)
   metadata +=" ] ";
   return metadata;
 }


 /**
  * Switches between the multiple and single attenuation relationhship gui bean.
  */
 private void toggleBetweenSingleAndMultipleAttenRel(){
   imrPanel.removeAll();
   //if single attenuation relationship is already selected then toggle to multiple attenrel
   if(singleAttenRelSelected){
     singleAttenRelSelected = false;
     toggleButton.setText(SINGLE_ATTEN_REL);
   }
   else{ //if multiple attenuation relationships panel was selected the toggle to single attenrel.
     singleAttenRelSelected = true;
     toggleButton.setText(MULTIPLE_ATTEN_REL);
   }

 }

 /**
  * this function is called when the person tries to switch between the single
  * and multiple attenuationRelationship gui bean.
  * @param e
  */
  void toggleButton_actionPerformed(ActionEvent e) {
    toggleBetweenSingleAndMultipleAttenRel();
  }
}