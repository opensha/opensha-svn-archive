package org.scec.sha.earthquake.PEER_test_cases;

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
import org.scec.sha.calc.HazardCurveCalculator;


public class PEER_TestsGuiBean implements
                         NamedObjectAPI,
                         ParameterChangeListener,
                         ParameterChangeWarningListener,
                         ParameterChangeFailListener {


  protected final static String C = "PEER_TestsGuiBean";
  protected final static boolean D = false;
  private String name  = "GroupTestGuiBean";


  //innner class instance
  PEER_TestDefaultParameterClass peerTestParameterClass;

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
    private final static String IMR_PARAM_NAME = "IMR";
    private final static String IMR_EDITOR_TITLE =  "Select IMR";
    private ParameterListEditor imrEditor = null;
    private ParameterList imrParamList = new ParameterList();
    //this vector saves the names of all the supported IMRs
    private Vector imrNamesVector=new Vector();
    //this vector holds the full class names of all the supported IMRs
    private Vector imrClasses;
    //saves the IMR objects, to the parameters related to an IMR.
    private Vector imrObject = new Vector();

    // IMT GUI Editor & Parameter names
    private final static String IMT_PARAM_NAME =  "IMT";
    private ParameterListEditor imtEditor = null;
    private ParameterList imtParamList = new ParameterList();
    //stores the IMT Params for the choosen IMR
    private Vector imtParam;



    // Site Gui Editor
    private SiteParamListEditor siteParamEditor;

    /**
     *  The object class names for all the supported Eqk Rup Forecasts
     */
    public final static String PEER_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_test_cases.PEER_FaultForecast";
    public final static String PEER_AREA_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_test_cases.PEER_AreaForecast";
    public final static String PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_test_cases.PEER_ListricFaultForecast";
    public final static String PEER_LISTRIC_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_test_cases.PEER_NonPlanarFaultForecast";
    public final static String PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_test_cases.PEER_MultiSourceForecast";

    //this vector saves the names of all the supported Eqk Rup Forecasts
    private Vector erfNamesVector=new Vector();
    //this vector holds the full class names of all the supported Eqk Rup Forecasts
    private Vector erfClasses;
    //saves the erf objects
    private Vector erfObject = new Vector();


    // ERF Editor stuff
    private final static String ERF_PARAM_NAME = "Eqk Rup Forecast";
    // these are to store the list of independ params for chosen ERF
    private ParameterListEditor erf_Editor = null;
    private final static String ERF_EDITOR_TITLE =  "Select Forecast";
    private ParameterList erf_IndParamList = new ParameterList();

    // hash map to mantain mapping between IMT and all IMLs supported by it
    private HashMap imt_IML_map = new HashMap();

    // search path needed for making editors
    private String[] searchPaths;

    private PEER_TestsApplet applet= null;


  /*
   * *********************************************
   * Hard Coded stuff for PEER test cases below
   */


  //  TestCases ParameterList & its editor
  private ParameterList testCasesParamList = new ParameterList();
  private ParameterListEditor testCasesEditor = null;


  //This string is sent by the applet and lets the GUIBean know which test case
  //and site has been selecetd by the user.
  private String selectedTest;
  private String selectedSite;


  /**
   * constructor
   */
  public PEER_TestsGuiBean(PEER_TestsApplet applet) {

    this.applet = applet;

    // search path needed for making editors
    searchPaths = new String[3];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    searchPaths[1] = SPECIAL_EDITORS_PACKAGE;
    searchPaths[2] = "org.scec.sha.magdist.gui" ;


    // Create all the available IMRs
    // to add more IMRs, change this function
    init_imrParamListAndEditor( );

    //initialize the IMT and IMLs
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
    peerTestParameterClass =new PEER_TestDefaultParameterClass(this);


    //set the site based on the selected test case
    //peerTestParameterClass.setParams();
  }


  /**
   * This method extracts the selected Site and the selected TestCase
   * @param testAndSite: Contains both the site and the Selected Test Case
   */
  public void setTestCaseAndSite(String testAndSite){
    int firstIndex=testAndSite.indexOf("-");
    int lastIndex = testAndSite.lastIndexOf("-");
    selectedTest = testAndSite.substring(firstIndex+1,lastIndex);
    selectedSite = testAndSite.substring(lastIndex+1);
    peerTestParameterClass.setParams();
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

       //loop over each IMT and find IML
        while ( it1.hasNext() ) {
          DependentParameterAPI param = ( DependentParameterAPI ) it1.next();
          StringParameter param1=new StringParameter(param.getName());
          Vector imlParamsVector=new Vector();

          // add all the independent parameters related to this IMT
          ListIterator it2 = param.getIndependentParametersIterator();
          if(D) System.out.println("IMT is:"+param.getName());
          while ( it2.hasNext() ) {
            Vector iml = new Vector();
            ParameterAPI param2 = (ParameterAPI ) it2.next();
            DoubleDiscreteConstraint values = ( DoubleDiscreteConstraint )param2.getConstraint();
            ListIterator it3 = values.listIterator();
            while(it3.hasNext())   // add all the periods relating to the SA
              iml.add(it3.next().toString());
            StringParameter independentParam = new StringParameter(param2.getName(),
                                               iml, (String)iml.get(0));
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
     // add all the IMLs to the current initialized IMT
     updateIML((String)imt.get(0));

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
  public ParameterListEditor getSiteEditor() {
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

      // if IMT selection then update the IML
      if (name1.equalsIgnoreCase(this.IMT_PARAM_NAME)) {
        updateIML((String)event.getNewValue());
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
      if(selectedForecast.equalsIgnoreCase(erfName)) { // we found seledcted forecast in the lsit
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


    //gets the lists of all the parameters that exists in the ERF parameter Editor
    //then checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
    //calls the method to make the update MagDist button invisible
    ListIterator lit = erf_IndParamList.getParametersIterator();
    while(lit.hasNext()){
      ParameterAPI param=(ParameterAPI)lit.next();
      if(param instanceof MagFreqDistParameter)
        ((MagFreqDistParameterEditor)erf_Editor.getParameterEditor(param.getName())).setUpdateButtonVisible(false);
    }
  }


  /**
   * sigma level is visible or not
   * @param value
   */
  protected void toggleSigmaLevelBasedOnTypeValue(String value){

    if( value.equalsIgnoreCase("none") ) {
      if(D) System.out.println("Value = " + value + ", need to set value param off.");
      imrEditor.setParameterInvisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, false );
    }
    else{
      if(D) System.out.println("Value = " + value + ", need to set value param on.");
      imrEditor.setParameterInvisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, true );
    }

  }

  /**
   * This function updates the IMTeditor with the independent parameters for the selected
   * IMT, by making only those visible to the user.
   * @param imlName : It is the name of the selected IMT, based on which we make
   * its independentParameters visible.
   */

  private void updateIML(String imlName) {
    Iterator it= imtParamList.getParametersIterator();

    //making all the IMT parameters invisible
    while(it.hasNext())
      imtEditor.setParameterInvisible(((ParameterAPI)it.next()).getName(),false);

    //making the choose IMT parameter visible
    imtEditor.setParameterInvisible(IMT_PARAM_NAME,true);

    it=imtParam.iterator();
    //for the selected IMT making its independent parameters visible
    while(it.hasNext()){
      DependentParameterAPI param=(DependentParameterAPI)it.next();
      if(param.getName().equalsIgnoreCase(imlName)){
        Iterator it1=param.getIndependentParametersIterator();
        while(it1.hasNext())
          imtEditor.setParameterInvisible(((ParameterAPI)it1.next()).getName(),true);
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


    //gets the lists of all the parameters that exists in the ERF parameter Editor
    //then checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
    //calls the method to update the magDistParams.
    ListIterator lit = erf_IndParamList.getParametersIterator();
    while(lit.hasNext()){
      ParameterAPI param=(ParameterAPI)lit.next();
      if(param instanceof MagFreqDistParameter)
        ((MagFreqDistParameterEditor)erf_Editor.getParameterEditor(param.getName())).setMagDistFromParams();
    }

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
    // add the function to the function list
    funcs.add(hazFunction);

    // set the X-axis label
    funcs.setXAxisName(imt);
    funcs.setYAxisName("Probability of Exceedance");
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
   * <p>Title: GroupTestDefaultParameterClass</p>
   * <p>Description: this class is the inner class that sets the default parameters
   * for the selected test peer case </p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: </p>
   * @author : Nitin Gupta and Vipin Gupta
   * @version 1.0
   */
  class PEER_TestDefaultParameterClass {


    protected final static String C = "GroupTestDefaultParameterClass";
    protected final static boolean D = false;

    // Fault 1 , Fault 2 and Area
    private final static String FAULT_ONE = "Fault 1";
    private final static String FAULT_TWO = "Fault 2";
    private final static String FAULT_AREA = "Fault Area";


    private  PEER_FaultForecast peer_Fault_ERF;
    private  PEER_AreaForecast peer_Area_ERF;

    protected PEER_TestsGuiBean peerTestGuiBean;

    public PEER_TestDefaultParameterClass(PEER_TestsGuiBean peerTestGuiBean){
      this.peerTestGuiBean = peerTestGuiBean;

      // hard coded values for setting in the test cases
      peer_Fault_ERF = (PEER_FaultForecast)erfObject.get(0);
      peer_Area_ERF =  (PEER_AreaForecast)erfObject.get(1);

    }
    /**
     * This function sets the site Paramters and the IMR parameters based on the
     * selected test case and selected site number for that test case
     * @param siteNumber
     */

    public void setParams() {
      String S = C + ":setParams()";
      if(D) System.out.println(S+"::entering");

      if(!selectedTest.equalsIgnoreCase(applet.TEST_CASE_TEN) && !selectedTest.equalsIgnoreCase(applet.TEST_CASE_ELEVEN))
          // it is fault test case
        erf_IndParamList.getParameter(ERF_PARAM_NAME).setValue(peer_Fault_ERF.getName());
      else // if it area case
        erf_IndParamList.getParameter(ERF_PARAM_NAME).setValue(peer_Area_ERF.getName());


      // set the mag dist params based on test case
      setMagDistParams(selectedTest);


      String selectedFault= new String(FAULT_ONE);
      ParameterList siteParams = siteParamEditor.getParameterList();

      //if selected test case is number 1
      if(selectedTest.equals(applet.TEST_CASE_ONE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }


      //if selected test case is number 2
      if(selectedTest.equals(applet.TEST_CASE_TWO)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if selected test case is number 3
      if(selectedTest.equals(applet.TEST_CASE_THREE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if selected test case is number 4
      if(selectedTest.equals(applet.TEST_CASE_FOUR)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_TWO);
      }

      //if selected test case is number 5
      if(selectedTest.equals(applet.TEST_CASE_FIVE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if selected test case is number 6
      if(selectedTest.equals(applet.TEST_CASE_SIX)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if selected test case is number 7
      if(selectedTest.equals(applet.TEST_CASE_SEVEN)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if the selected test case is number 8_1
      if(selectedTest.equals(applet.TEST_CASE_EIGHT_ONE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if the selected test case is number 8_2
      if(selectedTest.equals(applet.TEST_CASE_EIGHT_TWO)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(2.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_TWO);
      }

      //if the selected test case is number 8_3
      if(selectedTest.equals(applet.TEST_CASE_EIGHT_THREE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_2SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if the selected test case is number 9_1
      if(selectedTest.equals(applet.TEST_CASE_NINE_ONE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_TWO);
      }

      //if the selected test case is number 9_2
      if(selectedTest.equals(applet.TEST_CASE_NINE_TWO)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(AS_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(AS_1997_AttenRel.SITE_TYPE_NAME).setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_TWO);
      }

      //if the selected test case is number 9_3
      if(selectedTest.equals(applet.TEST_CASE_NINE_THREE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(Campbell_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(Campbell_1997_AttenRel.STD_DEV_TYPE_MAG_DEP);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParamEditor.getParameterList().getParameter(Campbell_1997_AttenRel.SITE_TYPE_NAME).setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
        siteParams.getParameter(Campbell_1997_AttenRel.BASIN_DEPTH_NAME).setValue(new Double(2.0));
        selectedFault = new String(FAULT_TWO);
      }

      //if the selected test case is number 10
      if(selectedTest.equals(applet.TEST_CASE_TEN)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);

        /**
         * This fills the default values for the forecast paramters based
         * on the selected test case 10
         */
        erf_IndParamList.getParameter(peer_Area_ERF.DEPTH_LOWER_PARAM_NAME).setValue(new Double(5));
        erf_IndParamList.getParameter(peer_Area_ERF.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
        erf_IndParamList.getParameter(peer_Area_ERF.DIP_PARAM_NAME).setValue(new Double(90));
        erf_IndParamList.getParameter(peer_Area_ERF.RAKE_PARAM_NAME).setValue(new Double(0));
        erf_IndParamList.getParameter(peer_Area_ERF.TIMESPAN_PARAM_NAME).setValue(new Double(1.0));
        erf_IndParamList.getParameter(peer_Area_ERF.GRID_PARAM_NAME).setValue(new Double(1.0));

        selectedFault = new String(FAULT_AREA);
      }

      //if the selected test case is number 11
      if(selectedTest.equals(applet.TEST_CASE_ELEVEN)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);

        /**
         * This fills the default values for the forecast paramters based
         * on the selected test case 11
         */

        erf_IndParamList.getParameter(peer_Area_ERF.DEPTH_LOWER_PARAM_NAME).setValue(new Double(10));
        erf_IndParamList.getParameter(peer_Area_ERF.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
        erf_IndParamList.getParameter(peer_Area_ERF.DIP_PARAM_NAME).setValue(new Double(90));
        erf_IndParamList.getParameter(peer_Area_ERF.RAKE_PARAM_NAME).setValue(new Double(0));
        erf_IndParamList.getParameter(peer_Area_ERF.TIMESPAN_PARAM_NAME).setValue(new Double(1.0));
        erf_IndParamList.getParameter(peer_Area_ERF.GRID_PARAM_NAME).setValue(new Double(1.0));

        selectedFault = new String(FAULT_AREA);
      }

      // set the latitude and longitude and selected forecast is area or fault
      //if selected site number is not 10 or 11 i.e for fault sites
      if(!selectedTest.equalsIgnoreCase(applet.TEST_CASE_TEN) && !selectedTest.equalsIgnoreCase(applet.TEST_CASE_ELEVEN)) {

        // it is fault test case
        setForecastParams(selectedFault,selectedTest);

        // for fault site 1
        if(selectedSite.equals(applet.SITE_ONE)) {
          siteParamEditor.getParameterList().getParameter(siteParamEditor.LATITUDE).setValue(new Double(38.113));
          siteParams.getParameter(siteParamEditor.LONGITUDE).setValue(new Double(-122.0));
        }
        // for fault site 2
        if(selectedSite.equals(applet.SITE_TWO)) {
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(38.113));
          siteParams.getParameter(siteParamEditor.LONGITUDE).setValue(new Double(-122.114));

        }
        // for fault site 3
        if(selectedSite.equals(applet.SITE_THREE)) {
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(38.111));
          siteParams.getParameter(siteParamEditor.LONGITUDE).setValue(new Double(-122.570));

        }
        // for fault site 4
        if(selectedSite.equals(applet.SITE_FOUR)) {
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(38.000));
          siteParams.getParameter(siteParamEditor.LONGITUDE).setValue(new Double(-122.0));

        }
        // for fault site 5
        if(selectedSite.equals(applet.SITE_FIVE)) {
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(37.910));
          siteParams.getParameter(siteParamEditor.LONGITUDE).setValue(new Double(-122.0));

        }
        // for fault site 6
        if(selectedSite.equals(applet.SITE_SIX)) {
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(38.225));
          siteParams.getParameter(siteParamEditor.LONGITUDE).setValue(new Double(-122.0));

        }
        // for fault site 7
        if(selectedSite.equals(applet.SITE_SEVEN)) {
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(38.113));
          siteParams.getParameter(siteParamEditor.LONGITUDE).setValue(new Double(-121.886));
        }
      } else { // for area sites

        // it is area test case
        erf_IndParamList.getParameter(ERF_PARAM_NAME).setValue(this.peer_Area_ERF.getName());

        siteParams.getParameter(siteParamEditor.LONGITUDE).setValue(new Double(-122.0));
        // for area site 1
        if(selectedSite.equals(applet.SITE_ONE))
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(38.0));

        // for area site 2
        if(selectedSite.equals(applet.SITE_TWO))
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(37.550));

        // for area site 3
        if(selectedSite.equals(applet.SITE_THREE))
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(37.099));

        // for area site 4
        if(selectedSite.equals(applet.SITE_FOUR))
          siteParams.getParameter(siteParamEditor.LATITUDE).setValue(new Double(36.874));
      }

      // refresh the editor according to parameter values
      imrEditor.synchToModel();
      imtEditor.synchToModel();
      siteParamEditor.synchToModel();
      erf_Editor.synchToModel();
    }


     /**
       * set the mag dist params according to test case
       *
       * @param testCase
       */
     public void setMagDistParams(String testCase) {


        MagFreqDistParameterEditor magEditor;

        magEditor= (MagFreqDistParameterEditor)erf_Editor.getParameterEditor("Mag Dist");

        magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(10.0));
        magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(101));
        // mag dist parameters for test case 1
        if(testCase.equalsIgnoreCase(applet.TEST_CASE_ONE)) {
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.5));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        // mag dist parameters  for test case 2
        if(testCase.equalsIgnoreCase(applet.TEST_CASE_TWO)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        // mag dist parameters  for test case 3
        if(testCase.equalsIgnoreCase(applet.TEST_CASE_THREE)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        // mag dist parameters for test case 4
        if(testCase.equalsIgnoreCase(applet.TEST_CASE_FOUR)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
        }

        // mag dist parameters for test case 5
        if(testCase.equalsIgnoreCase(applet.TEST_CASE_FIVE)) {
          magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(1001));
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_CUM_RATE);
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(5.0));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.5));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
          magEditor.getParameter(MagFreqDistParameterEditor.TOT_MO_RATE).setValue(new Double(1.8e16));
        }


        // mag dist parameters for test case 6
        if(testCase.equalsIgnoreCase(applet.TEST_CASE_SIX)) {
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.TOT_MO_RATE).setValue(new Double(1.8e16));
          magEditor.getParameter(MagFreqDistParameterEditor.STD_DEV).setValue(new Double(0.25));
          magEditor.getParameter(MagFreqDistParameterEditor.MEAN).setValue(new Double(6.2));
          magEditor.getParameter(MagFreqDistParameterEditor.TRUNCATION_REQ).setValue(MagFreqDistParameterEditor.TRUNCATE_UPPER_ONLY);
          magEditor.getParameter(MagFreqDistParameterEditor.TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.2));
        }
        // mag dist parameters for test case 7
        if(testCase.equalsIgnoreCase(applet.TEST_CASE_SEVEN)) {
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
          magEditor.getParameter(MagFreqDistParameterEditor.YC_DELTA_MAG_CHAR).setValue(new Double(0.5));
          magEditor.getParameter(MagFreqDistParameterEditor.YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(5.0));
          magEditor.getParameter(MagFreqDistParameterEditor.YC_MAG_PRIME).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.5));
          magEditor.getParameter(MagFreqDistParameterEditor.TOT_MO_RATE).setValue(new Double(1.8e16));
        }

        //mag dist parameters for the test case 8_1
        if(testCase.equalsIgnoreCase(applet.TEST_CASE_EIGHT_ONE)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        //mag dist parameters for the test case 8_2
       if(testCase.equalsIgnoreCase(applet.TEST_CASE_EIGHT_TWO)) {

         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
         magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
         magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        //mag dist parameters for the test case 8_3
       if(testCase.equalsIgnoreCase(applet.TEST_CASE_EIGHT_THREE)) {

         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
         magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
         magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        //mag dist parameters for the test case 9_1
       if(testCase.equalsIgnoreCase(applet.TEST_CASE_NINE_ONE)) {

         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
         magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
         magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
        }

        //mag dist parameters for the test case 9_2
       if(testCase.equalsIgnoreCase(applet.TEST_CASE_NINE_TWO)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
        }

        //mag dist parameters for the test case 9_1
       if(testCase.equalsIgnoreCase(applet.TEST_CASE_NINE_THREE)) {

         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
         magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
         magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
        }

        // mag dist parameters for test case 10
       if(testCase.equalsIgnoreCase(applet.TEST_CASE_TEN)) {
         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_MO_RATE);
         magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(5.0));
         magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.5));
         magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
         magEditor.getParameter(MagFreqDistParameterEditor.TOT_CUM_RATE).setValue(new Double(.0395));
        }

        // mag dist parameters for test case 11
       if(testCase.equalsIgnoreCase(applet.TEST_CASE_ELEVEN)) {
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_MO_RATE);
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(5.0));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.5));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
          magEditor.getParameter(MagFreqDistParameterEditor.TOT_CUM_RATE).setValue(new Double(.0395));
        }
        magEditor.setMagDistFromParams();
      }

      /**
       * Fault-1 or Fault-2 source
       * This functions fills the default values for the forecast paramters based
       * on the selected fault which is passed as the argument to the function.
       * Based on the selected test case we are modifying the value of the magLengthSigma
       * @param faultType : tells whether the fault1 or fault2
       * @param testCaseVal: tells which test case is selected
       */

      public void setForecastParams(String faultType, String testCaseVal){

        // add sigma for maglength(0-1)
        erf_IndParamList.getParameter(peer_Fault_ERF.SIGMA_PARAM_NAME).setValue(peer_Fault_ERF.DEFAULT_SIGMA_VAL);
        // set the common parameters like timespan, grid spacing
        erf_IndParamList.getParameter(peer_Fault_ERF.TIMESPAN_PARAM_NAME).setValue(new Double(1.0));
        erf_IndParamList.getParameter(peer_Fault_ERF.GRID_PARAM_NAME).setValue(new Double(1.0));
        erf_IndParamList.getParameter(peer_Fault_ERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));

        // magLengthSigma parameter is changed if the test case chosen is 3
        if(testCaseVal.equalsIgnoreCase(applet.TEST_CASE_THREE))
          erf_IndParamList.getParameter(peer_Fault_ERF.SIGMA_PARAM_NAME).setValue(new Double(0.2));
        // set the parameters for fault1
        if(faultType.equals(FAULT_ONE)) {
          erf_IndParamList.getParameter(peer_Fault_ERF.DIP_PARAM_NAME).setValue(new Double(90.0));
          erf_IndParamList.getParameter(peer_Fault_ERF.RAKE_PARAM_NAME).setValue(new Double(0.0));
        }
        // set the parameters for fault 2
        if(faultType.equals(FAULT_TWO)) {
          erf_IndParamList.getParameter(peer_Fault_ERF.DIP_PARAM_NAME).setValue(new Double(60.0));
          erf_IndParamList.getParameter(peer_Fault_ERF.RAKE_PARAM_NAME).setValue(new Double(90.0));
        }
      }
  }
}
