package org.scec.sha.gui;

/**
 * <p>Title: GroupTestGuiBean</p>
 * <p>Description: GUI Bean for PEER Test cases</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vipin Gupta and Nitin Gupta  Date: Sept 23, 2002
 * @version 1.0
 */

import java.lang.reflect.*;
import java.math.*;
import java.util.*;
import javax.swing.JOptionPane;

import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.sha.imr.*;
import org.scec.sha.imr.attenRelImpl.*;
import org.scec.data.function.*;
import org.scec.util.*;
import org.scec.data.*;
import org.scec.sha.magdist.*;
import org.scec.sha.magdist.parameter.*;
import org.scec.sha.magdist.gui.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.PEER_TestCases.*;
import org.scec.sha.calc.HazardCurveCalculator;
import org.scec.sha.calc.DisaggregationCalculator;
import org.scec.sha.gui.controls.*;


public class HazardCurveGuiBean implements
                         NamedObjectAPI,
                         ParameterChangeListener,
                         ParameterChangeWarningListener,
                         ParameterChangeFailListener {


  protected final static String C = "HazardCurveGuiBean";
  protected final static boolean D = false;
  private String name  = "GroupTestGuiBean";


  //Disaggregation String
  private String disaggregationString= null;


  //instance of class that sets default parameters for each selected test case
  HazardCurveDefaultParameterClass hazardCurveParameterClass;

  /**
   *  Search path for finding editors in non-default packages.
   */
  final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";

   /**
    *  The object class names for all the supported attenuation ralations (IMRs)
    *  Temp until figure out way to dynamically load classes during runtime
    */
   public final static String BJF_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.BJF_1997_AttenRel";
   public final static String AS_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.AS_1997_AttenRel";
   public final static String C_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Campbell_1997_AttenRel";
   public final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.SCEMY_1997_AttenRel";
   public final static String F_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Field_2000_AttenRel";
   public final static String A_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel";
   public final static String CB_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.CB_2003_AttenRel";

    // IMR GUI Editor & Parameter names
    public final static String IMR_PARAM_NAME = "IMR";
    public final static String IMR_EDITOR_TITLE =  "Select IMR";
    private ParameterListEditor imrEditor = null;
    private ParameterList imrParamList = new ParameterList();
    //this vector saves the names of all the supported IMRs
    private Vector imrNamesVector=new Vector();
    //this vector holds the full class names of all the supported IMRs
    private Vector imrClasses;
    //saves the IMR objects, to the parameters related to an IMR.
    private Vector imrObject = new Vector();

    // IMT GUI Editor & Parameter names
    public final static String IMT_PARAM_NAME =  "IMT";
    private ParameterListEditor imtEditor = null;
    private ParameterList imtParamList = new ParameterList();
    //stores the IMT Params for the choosen IMR
    private Vector imtParam;



    // Site Gui Editor
    private SiteParamListEditor siteParamEditor;

    /**
     *  The object class names for all the supported Eqk Rup Forecasts
     */
    public final static String PEER_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_FaultForecast";
    public final static String PEER_AREA_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_AreaForecast";
    public final static String PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_NonPlanarFaultForecast";
    public final static String PEER_LISTRIC_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_ListricFaultForecast";
    public final static String PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_MultiSourceForecast";

    //this vector saves the names of all the supported Eqk Rup Forecasts
    private Vector erfNamesVector=new Vector();
    //this vector holds the full class names of all the supported Eqk Rup Forecasts
    private Vector erfClasses;
    //saves the erf objects
    private Vector erfObject = new Vector();


    // ERF Editor stuff
    public final static String ERF_PARAM_NAME = "Eqk Rup Forecast";
    // these are to store the list of independ params for chosen ERF
    private ParameterListEditor erf_Editor = null;
    public final static String ERF_EDITOR_TITLE =  "Select Forecast";
    private ParameterList erf_IndParamList = new ParameterList();

    // search path needed for making editors
    private String[] searchPaths;

    private HazardCurveApplet applet= null;


  /*
   * *********************************************
   * Hard Coded stuff for PEER test cases below
   */


  //  TestCases ParameterList & its editor
  private ParameterList testCasesParamList = new ParameterList();
  private ParameterListEditor testCasesEditor = null;


  //This string is sent by the applet and lets the GUIBean know which test case set
  //and site has been selecetd by the user.
  private String selectedTest;
  private String selectedSite;
  private String selectedSet;

  /**
   * constructor
   */
  public HazardCurveGuiBean(HazardCurveApplet applet) {

    this.applet = applet;

    // search path needed for making editors
    searchPaths = new String[3];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    searchPaths[1] = SPECIAL_EDITORS_PACKAGE;
    searchPaths[2] = "org.scec.sha.magdist.gui" ;


    // Create all the available IMRs
    // to add more IMRs, change this function
    init_imrParamListAndEditor( );

    //initialize the IMTs
    init_imtParamListAndEditor();

    //init erf_IndParamList. List of all available ERFs at this time
    init_erf_IndParamListAndEditor();

    // make the site gui bean
    siteParamEditor = new SiteParamListEditor();

    // Create site parameters
    updateSiteParamListAndEditor( );

    // Stuff hard coded for PEER test cases below
    // ******************************************

    // this class handles all the hard coding stuff needed for test cases
    hazardCurveParameterClass =new HazardCurveDefaultParameterClass(this);


    //set the site based on the selected test case
    //hazardCurveParameterClass.setParams();
  }


  /**
   * This method extracts the selected Site and the selected TestCase set
   * @param testAndSite: Contains both the site and the Selected Test Cases Set
   */
  public void setTestCaseAndSite(String testAndSite){
    int firstIndex=testAndSite.indexOf("-");
    int lastIndex = testAndSite.lastIndexOf("-");
    selectedSet = testAndSite.substring(0,firstIndex);
    selectedTest = testAndSite.substring(firstIndex+1,lastIndex);
    selectedSite = testAndSite.substring(lastIndex+1);
    hazardCurveParameterClass.setParams();
  }


  /**
    * Creates a class instance from a string of the full class name including packages.
    * This is how you dynamically make objects at runtime if you don't know which\
    * class beforehand. For example, if you wanted to create a BJF_1997_AttenRel you can do
    * it the normal way:<P>
    *
    * <code>BJF_1997_AttenRel imr = new BJF_1997_AttenRel()</code><p>
    *
    * If your not sure the user wants this one or AS_1997_AttenRel you can use this function
    * instead to create the same class by:<P>
    *
    * <code>BJF_1997_AttenRel imr =
    * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.scec.sha.imt.attenRelImpl.BJF_1997_AttenRel");
    * </code><p>
    *
    */
   public Object createIMRClassInstance( String className, org.scec.param.event.ParameterChangeWarningListener listener){
       String S = C + ": createIMRClassInstance(): ";
       try {

           Class listenerClass = Class.forName( "org.scec.param.event.ParameterChangeWarningListener" );
           Object[] paramObjects = new Object[]{ listener };
           Class[] params = new Class[]{ listenerClass };
           Class imrClass = Class.forName( className );
           Constructor con = imrClass.getConstructor( params );
           Object obj = con.newInstance( paramObjects );
           return obj;
       } catch ( ClassCastException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( ClassNotFoundException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( NoSuchMethodException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( InvocationTargetException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( IllegalAccessException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( InstantiationException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       }

   }


   /**
    * Creates a class instance from a string of the full class name including packages.
    * This is how you dynamically make objects at runtime if you don't know which\
    * class beforehand.
    *
    */
   public Object createERFClassInstance( String className){
     String S = C + ": createERFClassInstance(): ";
     try {
       Object[] paramObjects = new Object[]{};
       Class[] params = new Class[]{};
       Class erfClass = Class.forName( className );
       Constructor con = erfClass.getConstructor(params);
       Object obj = con.newInstance( paramObjects );
       return obj;
     } catch ( ClassCastException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( ClassNotFoundException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( NoSuchMethodException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( InvocationTargetException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( IllegalAccessException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( InstantiationException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     }

   }




  /**
   *  Create a list of all the IMRs
   */
  protected void init_imrParamListAndEditor() {


    // if we are entering this function for the first time, then make imr objects
    if(!imrParamList.containsParameter(IMR_PARAM_NAME)) {
      imrParamList = new ParameterList();

      //add the available IMRs
      imrClasses = new Vector();
      imrClasses.add( BJF_CLASS_NAME );
      imrClasses.add( AS_CLASS_NAME );
      imrClasses.add( C_CLASS_NAME );
      imrClasses.add( SCEMY_CLASS_NAME );
      imrClasses.add( F_CLASS_NAME );
      imrClasses.add( A_CLASS_NAME );
      imrClasses.add( CB_CLASS_NAME );
      Iterator it= imrClasses.iterator();
      while(it.hasNext()){
        // make the IMR objects as needed to get the site params later
        AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI ) createIMRClassInstance((String)it.next(),this);
        imrObject.add(imr);
        imrNamesVector.add(imr.getName());
      }

      // make the IMR selection paramter
      StringParameter selectIMR = new StringParameter(IMR_PARAM_NAME,
                               imrNamesVector,(String)imrNamesVector.get(0));
      // listen to IMR paramter to change site params when it changes
      selectIMR.addParameterChangeListener(this);
      imrParamList.addParameter(selectIMR);
    }

    // remove all the parameters except the IMR parameter
    ListIterator it = imrParamList.getParameterNamesIterator();
    while(it.hasNext()) {
      String paramName = (String)it.next();
      if(!paramName.equalsIgnoreCase(IMR_PARAM_NAME))
        imrParamList.removeParameter(paramName);
    }


    // now find the selceted IMR and add the parameters related to it

    // initalize imr
    AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI)imrObject.get(0);

    // find & set the selectedIMR
    String selectedIMR = imrParamList.getValue(IMR_PARAM_NAME).toString();
    int size = imrObject.size();
    for(int i=0; i<size ; ++i) {
      imr = (AttenuationRelationshipAPI)imrObject.get(i);
      if(imr.getName().equalsIgnoreCase(selectedIMR))
        break;
    }


    ParameterAPI typeParam = imr.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME);
    imrParamList.addParameter(typeParam);
    typeParam.addParameterChangeListener(this);


    // add trunc level
    ParameterAPI levelParam = imr.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME);
    imrParamList.addParameter(levelParam);

    //add the sigma param for IMR
    ParameterAPI sigmaParam = imr.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME);
    sigmaParam.setValue(((StringParameter)sigmaParam).getAllowedStrings().get(0));
    imrParamList.addParameter(sigmaParam);

    imrEditor = new ParameterListEditor(imrParamList,searchPaths);
    imrEditor.setTitle(IMR_EDITOR_TITLE);
    // set the trunc level based on trunc type
    String value = (String)imrParamList.getValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME);
    toggleSigmaLevelBasedOnTypeValue(value);

  }

  /**
   *  Create a list of all the IMTs
   */

   protected void init_imtParamListAndEditor() {

     imtParamList = new ParameterList();
     // get the selected IMR
    String value = (String)imrParamList.getParameter(this.IMR_PARAM_NAME).getValue();
    //add all the supported IMT parameters
     int size = this.imrObject.size();
     AttenuationRelationshipAPI imr;

     //vector to store all the IMT's supported by an IMR
     Vector imt=new Vector();
     imtParam = new Vector();

    // loop over each IMR
     for(int i=0; i < size ; ++i) {
       imr = (AttenuationRelationshipAPI)imrObject.get(i);
       // if this is not the selected IMR then continue
       if(!imr.getName().equalsIgnoreCase(value))
         continue;
       Iterator it1 = imr.getSupportedIntensityMeasuresIterator();

       //loop over each IMT and get their independent parameters
        while ( it1.hasNext() ) {
          DependentParameterAPI param = ( DependentParameterAPI ) it1.next();
          StringParameter param1=new StringParameter(param.getName());

          // add all the independent parameters related to this IMT
          // NOTE: this will only work for DoubleDiscrete independent parameters; it's not general!
          // this also converts these DoubleDiscreteParameters to StringParameters
          ListIterator it2 = param.getIndependentParametersIterator();
          if(D) System.out.println("IMT is:"+param.getName());
          while ( it2.hasNext() ) {
            Vector indParamOptions = new Vector();
            ParameterAPI param2 = (ParameterAPI ) it2.next();
            DoubleDiscreteConstraint values = ( DoubleDiscreteConstraint )param2.getConstraint();
            ListIterator it3 = values.listIterator();
            while(it3.hasNext())   // add all the periods relating to the SA
              indParamOptions.add(it3.next().toString());
            StringParameter independentParam = new StringParameter(param2.getName(),
                                                   indParamOptions, (String)indParamOptions.get(0));

            // added by Ned so the default period is 1.0 sec (this is a hack).
            if( ((String) independentParam.getName()).equals("SA Period") ) {
                independentParam.setValue(new String("1.0"));
            }

            param1.addIndependentParameter(independentParam);
          }
          imtParam.add(param1);
          imt.add(param.getName());
        }
        break;
      }

     // add the IMT paramter
      StringParameter imtParameter = new StringParameter (IMT_PARAM_NAME,imt,
                                                             (String)imt.get(0));
      imtParameter.addParameterChangeListener(this);
      imtParamList.addParameter(imtParameter);

     /* gets the iterator for each supported IMT and iterates over all its indepenedent
      * parameters to add them to the common Vector to display in the IMT Panel
      **/

     Iterator it=imtParam.iterator();

     while(it.hasNext()){
       Iterator it1=((DependentParameterAPI)it.next()).getIndependentParametersIterator();
       while(it1.hasNext())
         imtParamList.addParameter((ParameterAPI)it1.next());
     }


     // now make the editor based on the paramter list
     imtEditor = new ParameterListEditor( imtParamList, searchPaths);
     imtEditor.setTitle( "Select IMT" );
     // update the current IMT
     updateIMT((String)imt.get(0));

   }


   /**
    *
    * @returns the disaggregation string to the applet
    */
   String getDisaggregationString(){
     return disaggregationString;
   }

   /**
    * init erf_IndParamList. List of all available forecasts at this time
    */
    protected void init_erf_IndParamListAndEditor() {

      EqkRupForecastAPI erf;

      //add the available ERFs
      erfClasses = new Vector();
      erfClasses.add( PEER_FAULT_FORECAST_CLASS_NAME );
      erfClasses.add( PEER_AREA_FORECAST_CLASS_NAME );
      erfClasses.add( PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME );
      erfClasses.add( PEER_LISTRIC_FAULT_FORECAST_CLASS_NAME );
      erfClasses.add( PEER_MULTI_SOURCE_FORECAST_CLASS_NAME );

      Iterator it= erfClasses.iterator();

      while(it.hasNext()){
        // make the ERF objects to get their adjustable parameters
        erf = (EqkRupForecastAPI ) createERFClassInstance((String)it.next());
        if(D)
          System.out.println("Iterator Class:"+erf.getName());
        erfObject.add(erf);
        erfNamesVector.add(erf.getName());
      }

      // make the forecast selection parameter
      StringParameter selectSource= new StringParameter(ERF_PARAM_NAME,
                                  erfNamesVector, (String)erfNamesVector.get(0));
      selectSource.addParameterChangeListener(this);
      erf_IndParamList.addParameter(selectSource);


      // now make the editor based on the paramter list
      erf_Editor = new ParameterListEditor( erf_IndParamList, searchPaths);
      erf_Editor.setTitle( this.ERF_EDITOR_TITLE );


      // forecast 1  is selected initially
      setParamsInForecast((String)erfNamesVector.get(0));
   }


 /**
  * update the site paramter list based on the selected IMR
  *
  * @throws ParameterException
  */
 private void updateSiteParamListAndEditor()
      throws ParameterException {

   //starting
   String S = C + ": updateSiteParamListAndEditor(): ";


   // get the selected IMR
   String value = (String)imrParamList.getParameter(this.IMR_PARAM_NAME).getValue();
   AttenuationRelationshipAPI imr;
   int size = imrObject.size();
   // loop over each IMR
   for(int i=0; i < size ; ++i) {
     imr = (AttenuationRelationshipAPI)imrObject.get(i);
     // if this is not the selected IMR then continue
     if(imr.getName().equalsIgnoreCase(value)) {
        siteParamEditor.replaceSiteParams(imr.getSiteParamsIterator());
        break;
     }
   }
 }


 /**
  *  Gets the name attribute of the IMRGuiBean object
  *
  * @return    The name value
  */
  public String getName() {
        return name;
  }


  /**
   *  Gets the testCasesEditor attribute of the GroupTestGuiBean object
   *
   * @return    The testCasesEditor value
   */
  public ParameterListEditor getTestCasesEditor() {
        return testCasesEditor;
 }

  /**
   *  Gets the imtEditor attribute of the GroupTestGuiBean object
   *
   * @return    The imtEditor value
   */
  public ParameterListEditor getIMTEditor() {
        return imtEditor;
 }


 /**
  *  Gets the imrEditor attribute of the GroupTestGuiBean object
  *
  * @return    The imrEditor value
  */
  public ParameterListEditor getImrEditor() {
        return imrEditor;
 }

 /**
  *  Gets the siteEditor attribute
  *
  * @return    The siteEditor value
  */
  public SiteParamListEditor getSiteEditor() {
        return siteParamEditor;
 }




  /**
   *  Gets the Eqk source Editor attribute of the GroupTestGuiBean object
   *
   * @return    The source editor value
   */
   public ParameterListEditor get_erf_Editor() {
     return erf_Editor;
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

      // if IMT selection then update
      if (name1.equalsIgnoreCase(this.IMT_PARAM_NAME)) {
        updateIMT((String)event.getNewValue());
        applet.updateChoosenIMT();
      }

      // if IMR selection changed, update the site parameter list
      if ( name1.equalsIgnoreCase(this.IMR_PARAM_NAME)) {
          init_imrParamListAndEditor();
          updateSiteParamListAndEditor();
          init_imtParamListAndEditor();
          applet.updateChoosenIMR();
          applet.updateChoosenIMT();
      }

      // if Truncation type changes
      if( name1.equals(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME) ){  // special case hardcoded. Not the best way to do it, but need framework to handle it.
        String value = event.getNewValue().toString();
        toggleSigmaLevelBasedOnTypeValue(value);
      }

      // if source selected by the user  changes
      if( name1.equals(this.ERF_PARAM_NAME) ){
        String value = event.getNewValue().toString();
        setParamsInForecast(value);
        applet.updateChoosenEqkSource();
      }


  }


  /**
   * this function is called to add the paramters based on the forecast
   *  selected by the user
   * @param forecast
   */
  private void setParamsInForecast(String selectedForecast) {

    ParameterAPI chooseERF_Param = this.erf_IndParamList.getParameter(this.ERF_PARAM_NAME);
    erf_IndParamList = new ParameterList();
    erf_IndParamList.addParameter(chooseERF_Param);

    // remove all the existing parameters in the editor
    erf_Editor.removeAll();

    // get the selected forecast
    int size = this.erfNamesVector.size();
    String erfName;
    EqkRupForecastAPI erf = null;
    for(int i=0; i<size; ++i) {
      erfName = (String)erfNamesVector.get(i);
      if(selectedForecast.equalsIgnoreCase(erfName)) { // we found selected forecast in the lsit
        erf = (EqkRupForecastAPI)this.erfObject.get(i);
        break;
      }
    }

    Iterator it = erf.getAdjustableParamsList();

   // make the parameters visible based on selected forecast
    while(it.hasNext()) erf_IndParamList.addParameter((ParameterAPI)it.next());

    // now make the editor based on the paramter list
    erf_Editor = new ParameterListEditor( erf_IndParamList, searchPaths);
    erf_Editor.setTitle( this.ERF_EDITOR_TITLE );


    //checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
    //calls the method to make the update MagDist button invisible

    MagFreqDistParameterEditor magDistEditor=getMagDistEditor();
    if(magDistEditor !=null)
      magDistEditor.setUpdateButtonVisible(false);

  }


  /**
   * sigma level is visible or not
   * @param value
   */
  protected void toggleSigmaLevelBasedOnTypeValue(String value){

    if( value.equalsIgnoreCase("none") ) {
      if(D) System.out.println("Value = " + value + ", need to set value param off.");
      imrEditor.setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, false );
    }
    else{
      if(D) System.out.println("Value = " + value + ", need to set value param on.");
      imrEditor.setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, true );
    }

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
      imtEditor.setParameterVisible(((ParameterAPI)it.next()).getName(),false);

    //making the choose IMT parameter visible
    imtEditor.setParameterVisible(IMT_PARAM_NAME,true);

    it=imtParam.iterator();
    //for the selected IMT making its independent parameters visible
    while(it.hasNext()){
      DependentParameterAPI param=(DependentParameterAPI)it.next();
      if(param.getName().equalsIgnoreCase(imtName)){
        Iterator it1=param.getIndependentParametersIterator();
        while(it1.hasNext())
          imtEditor.setParameterVisible(((ParameterAPI)it1.next()).getName(),true);
      }
    }
  }

  /**
   * Gets the probabilities functiion based on selected parameters
   * this function is called when add Graph is clicked
   */
  public void getChoosenFunction(DiscretizedFuncList funcs) {
    EqkRupForecastAPI eqkRupForecast = null;

    // get the selected forecast model
    String selectedForecast = (String)this.erf_IndParamList.getValue(this.ERF_PARAM_NAME);



    //then checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
    //calls the method to update the magDistParams.
    MagFreqDistParameterEditor magEditor=getMagDistEditor();

    if(magEditor!=null)
      magEditor.setMagDistFromParams();


    // check which forecast has been selected by the user
    int size = this.erfNamesVector.size();
    String erfName;
    for(int i=0; i<size; ++i) {
      erfName = (String)erfNamesVector.get(i);
      if(selectedForecast.equalsIgnoreCase(erfName)) { // we found selected forecast in the list
        eqkRupForecast = (EqkRupForecastAPI)this.erfObject.get(i);
        break;
      }
    }

    // intialize the hazard function
    ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();


    //set the X-axis label based on selected IMT
    // get the selected IMT, if it is SA, get the period as well
    String imt = (String)this.imtParamList.getValue(this.IMT_PARAM_NAME);


    //get the value of the selected IMT
    String selectedImt=(String)imtParamList.getParameter(IMT_PARAM_NAME).getValue();


    // get the IMR names list
    int imrSize=this.imrNamesVector.size();

    // get the selected IMR
    String selectedIMR = (String)this.imrParamList.getValue(this.IMR_PARAM_NAME);

    // selected IMR object
    AttenuationRelationshipAPI imr = null;

    // make a site object to pass to each IMR
    Site site = siteParamEditor.getSite();


    // do for each IMR
    for(int i=0;i<imrSize;++i) {
      if(((String)imrNamesVector.get(i)).equalsIgnoreCase(selectedIMR)) {
        // if this IMR is selected
        initDiscretizeValues(hazFunction);
        hazFunction.setInfo(selectedIMR);

        // set the IMT in the IMR
        try {
          if(D) System.out.println("siteString:::"+site.toString());
          imr = (AttenuationRelationshipAPI)imrObject.get(i);

          //set all the  parameters related to this IMT
          Iterator it= imtParam.iterator();
          while(it.hasNext()){
            DependentParameterAPI param=(DependentParameterAPI)it.next();
            if(param.getName().equalsIgnoreCase(selectedImt))
              imr.setIntensityMeasure(param);
          }
          break;
        } catch (Exception ex) {
          if(D) System.out.println(C + ":Param warning caught"+ex);
          ex.printStackTrace();
        }
      }
    }


   // calculate the hazard curve
    HazardCurveCalculator calc = new HazardCurveCalculator();
    try {
      calc.getHazardCurve(hazFunction, site, imr, eqkRupForecast);
    }catch (RuntimeException e) {
          JOptionPane.showMessageDialog(applet, e.getMessage(),
            "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
          //e.printStackTrace();
          return;
   }

   //inititialising the disaggregation String
   disaggregationString=null;
   //checking the disAggregation flag
   if(applet.getDisaggregationFlag()){
     DisaggregationCalculator disaggCalc = new DisaggregationCalculator();
     double selectedProb= applet.getDisaggregationProbablity();
     int num = hazFunction.getNum();

     //if selected Prob is not within the range of the Exceed. prob of Hazard Curve function
     if(selectedProb > hazFunction.getY(0) || selectedProb < hazFunction.getY(num-1))
       JOptionPane.showMessageDialog(applet,
                                     new String("Chosen Probability is not"+
                                     " within the range of the min and max prob."+
                                     " in the Hazard Curve"),
                                     "Disaggregation Prob. selection error message",
                                     JOptionPane.OK_OPTION);
     else{
       //gets the Disaggregation data
       double iml= hazFunction.getFirstInterpolatedX(selectedProb);
       disaggCalc.disaggregate(Math.log(iml),site,imr,eqkRupForecast);
       disaggregationString=disaggCalc.getResultsString();
     }
   }
    // add the function to the function list
    funcs.add(hazFunction);

    // set the X-axis label
    funcs.setXAxisName(imt);
    funcs.setYAxisName("Probability of Exceedance");
  }


  /**
   * gets the lists of all the parameters that exists in the ERF parameter Editor
   * then checks if the magFreqDistParameter exists inside it , if so then returns the MagEditor
   * else return null.
   * @returns MagFreDistParameterEditor
   */
  public MagFreqDistParameterEditor getMagDistEditor(){

    ListIterator lit = erf_IndParamList.getParametersIterator();
    while(lit.hasNext()){
      ParameterAPI param=(ParameterAPI)lit.next();
      if(param instanceof MagFreqDistParameter){
        MagFreqDistParameterEditor magDistEditor=((MagFreqDistParameterEditor)erf_Editor.getParameterEditor(param.getName()));
        return magDistEditor;
      }
    }
    return null;
  }

  /**
   *  Shown when a Constraint error is thrown on a ParameterEditor
   *
   * @param  e  Description of the Parameter
   */
  public void parameterChangeFailed( ParameterChangeFailEvent e ) {

    String S = C + " : parameterChangeWarning(): ";
    if(D) System.out.println(S + "Starting");


    StringBuffer b = new StringBuffer();

    ParameterAPI param = ( ParameterAPI ) e.getSource();


    ParameterConstraintAPI constraint = param.getConstraint();
    String oldValueStr = e.getOldValue().toString();
    String badValueStr = e.getBadValue().toString();
    String name = param.getName();

    try{
      // only show messages for site parameters
      if(this.imrParamList.getParameter(name)==null)
        return;
    } catch(ParameterException paramException) {
      // we do not need to do anything in case this paramter is not in site paramters list
      return;
    }

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
        applet, b.toString(),
        "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
        );

    if(D) System.out.println(S + "Ending");

  }


  /**
   *  Function that must be implemented by all Listeners for
   *  ParameterChangeWarnEvents.
   *
   * @param  event  The Event which triggered this function call
   */
  public void parameterChangeWarning( ParameterChangeWarningEvent e ){

    String S = C + " : parameterChangeWarning(): ";
    if(D) System.out.println(S + "Starting");


    // only display messages if paramters are set at back
    StringBuffer b = new StringBuffer();

    WarningParameterAPI param = e.getWarningParameter();


    try{
      Double min = param.getWarningMin();
      Double max = param.getWarningMax();

      String name = param.getName();

      // only show messages for site parameters
      if(this.imrParamList.getParameter(name)==null)
        return;

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

    catch(ParameterException paramException) {
      // we do not need to do anything in case this paramter is not in site paramters list
      param.setValueIgnoreWarning( e.getNewValue() );
      return;
    }
    catch( Exception ee){

      String name = param.getName();

      b.append( "You have exceeded the recommended range for: \n");
      b.append( name + '\n' );
      b.append( "Click Yes to accept the new value: " );
      b.append( e.getNewValue().toString() );
      b.append( name );


    }
    if(D) System.out.println(S + b.toString());

    int result = 0;

    if(D) System.out.println(S + "Showing Dialog");

    result = JOptionPane.showConfirmDialog( applet, b.toString(),
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

    if(D) System.out.println(S + "Ending");
  }



  /**
   * Initialize the X values and the prob as 1
   *
   * @param arb
   */
  private void initDiscretizeValues(ArbitrarilyDiscretizedFunc arb){
    arb.set(.001,1);
    arb.set(.01,1);
    arb.set(.05,1);
    arb.set(.15,1);
    arb.set(.1,1);
    arb.set(.2,1);
    arb.set(.25,1);
    arb.set(.3,1);
    arb.set(.4,1);
    arb.set(.5,1);
    arb.set(.6,1);
    arb.set(.7,1);
    arb.set(.8,1);
    arb.set(.9,1);
    arb.set(1.0,1);
    arb.set(1.1,1);
    arb.set(1.2,1);
    arb.set(1.3,1);
    arb.set(1.4,1);
    arb.set(1.5,1);
  }

  /**
   *
   * @return the ERF object saved in the Vector
   */
  public Vector getErfVector(){
    return erfObject;
  }


  /**
   *
   * @return the selected Set  chosen by the user to plot hazard curve
   */
  public String getSelectedSet(){
    return selectedSet;
  }

  /**
   *
   * @return the selected Test Case chosen by the user to plot hazard curve
   */
  public String getSelectedTest(){
    return selectedTest;
  }

  /**
   *
   * @return the selected Site chosen by the user to plot hazard curve
   */
  public String getSelectedSite(){
    return selectedSite;
  }

  public ParameterList getIMRParamList(){
    return imrParamList;
  }

  public ParameterList getIMTParamList(){
    return imtParamList;
  }

  public ParameterList getERF_IndParamList(){
    return erf_IndParamList;
  }
}
