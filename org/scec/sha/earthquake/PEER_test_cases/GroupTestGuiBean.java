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
import org.scec.sha.imr.classicImpl.*;
import org.scec.data.function.*;
import org.scec.util.*;
import org.scec.data.*;
import org.scec.sha.magdist.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.calc.HazardCurveCalculator;


public class GroupTestGuiBean implements
                         NamedObjectAPI,
                         ParameterChangeListener,
                         ParameterChangeWarningListener,
                         ParameterChangeFailListener {


  protected final static String C = "GroupTestGuiBean";
  protected final static boolean D = false;
  private String name  = "GroupTestGuiBean";


  //innner class instance
  GroupTestDefaultParameterClass groupTestParameterClass =new GroupTestDefaultParameterClass(this);


  //object for the fault
  Set1_Fault_Forecast faultcase1=new Set1_Fault_Forecast();

  //object for the Area;
  Set1_Area_Forecast faultcase2_area=new Set1_Area_Forecast();

  /**
   *  Search path for finding editors in non-default packages.
   */
  final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";

   /**
    *  Temp until figure out way to dynamically load classes during runtime
    */
   public  final static String BJF_CLASS_NAME = "org.scec.sha.imr.classicImpl.BJF_1997_IMR";
   public final static String AS_CLASS_NAME = "org.scec.sha.imr.classicImpl.AS_1997_IMR";
   public final static String C_CLASS_NAME = "org.scec.sha.imr.classicImpl.Campbell_1997_IMR";
   public final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.classicImpl.SCEMY_1997_IMR";
   public final static String F_CLASS_NAME = "org.scec.sha.imr.classicImpl.Field_2000_IMR";
   public final static String A_CLASS_NAME = "org.scec.sha.imr.classicImpl.Abrahamson_2000_IMR";


   /**
    * Param Names
    */
  private final static String TEST_PARAM_NAME = "Test Case";
  private final static String SITE_NUMBER_PARAM = "Site Number";
  private final static String IMR_PARAM_NAME = "IMR";
  private final static String IMT_PARAM_NAME =  "IMT";
  private final static String IMR_EDITOR_TITLE =  "Select IMR";
  /**
   * Test cases final static string
   */

  private final static String TEST_CASE_ONE ="1";
  private final static String TEST_CASE_TWO ="2";
  private final static String TEST_CASE_THREE ="3";
  private final static String TEST_CASE_FOUR ="4";
  private final static String TEST_CASE_FIVE ="5";
  private final static String TEST_CASE_SIX ="6";
  private final static String TEST_CASE_SEVEN ="7";
  private final static String TEST_CASE_EIGHT_ONE ="8_1";
  private final static String TEST_CASE_EIGHT_TWO ="8_2";
  private final static String TEST_CASE_EIGHT_THREE ="8_3";
  private final static String TEST_CASE_NINE_ONE ="9_1";
  private final static String TEST_CASE_NINE_TWO ="9_2";
  private final static String TEST_CASE_NINE_THREE ="9_3";
  private final static String TEST_CASE_TEN ="10";
  private final static String TEST_CASE_ELEVEN ="11";



  /**
   * static site strings
   */

  private final static String SITE_ONE = "Site-1";
  private final static String SITE_TWO = "Site-2";
  private final static String SITE_THREE = "Site-3";
  private final static String SITE_FOUR = "Site-4";
  private final static String SITE_FIVE = "Site-5";
  private final static String SITE_SIX = "Site-6";
  private final static String SITE_SEVEN = "Site-7";
  private final static String SITE_EIGHT = "Site-8";

  //source Name
  private final static String SOURCE_PARAM_NAME = "Eqk Rup Forecast";

  //Source Fault Name
  private final static String SOURCE_FAULT_ONE = "Test Set1 Fault";
  private final static String SOURCE_FAULT_AREA = "Test Set1 Area";

  // Fault 1 , Fault 2 and Area
  private final static String FAULT_ONE = "Fault 1";
  private final static String FAULT_TWO = "Fault 2";
  private final static String FAULT_AREA = "Fault Area";

  //this vector saves all the IMR classes name
  private Vector imrNamesVector=new Vector();

  //save all the IMR classes in the vector
  private Vector imrClasses;

  // hash map to mantain mapping between IMT and all IMLs supported by it
  private HashMap imt_IML_map = new HashMap();

  /**
   *  Return Test Cases editor. It contains the list of test case
   */
  private ParameterListEditor testCasesEditor = null;

  /**
   * editor for imt parameters
   */
  private ParameterListEditor imtEditor = null;

  /**
   * editor for imr parameters. It contains a list of IMRs
   */
  private ParameterListEditor imrEditor = null;

  /**
   * editor for site parameters. It contains a list of sites for the IMr
   */
  private ParameterListEditor siteEditor = null;

  /**
   * site gui bean is needed for making the site editor
   */
  private SiteGuiBean siteBean;

  /**
   * editor for imr parameters. It contains a list of IMRs
   */
  private ParameterListEditor eqkSourceEditor = null;

  /**
   *  TestCases ParameterList. List of all the test cases.
   */
  private ParameterList testCasesParamList = new ParameterList();

  /**
   *  IMT ParameterList
   */
  private ParameterList imtParamList = new ParameterList();

  /**
   *  IMR ParameterList. List of all supported IMRs.
   */
  private ParameterList imrParamList = new ParameterList();

  /**
   *  Eqk source ParameterList. List of all supported sources.
   */
  private ParameterList eqkSourceParamList = new ParameterList();


  // search path needed for making editors
  private String[] searchPaths;

  //saves the IMR objects, to the parameters related to an IMR.
  private Vector imrObject = new Vector();
  private GroupTestApplet applet= null;


  /**
   * constructor
   */
  public GroupTestGuiBean(GroupTestApplet applet) {

    this.applet = applet;

    //create the instance of magdistbean
    //this.magDistBean = new MagDistGuiBean(applet);
    // search path needed for making editors
    searchPaths = new String[3];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    searchPaths[1] = SPECIAL_EDITORS_PACKAGE;
    searchPaths[2] = "org.scec.sha.magdist" ;
    // make the site gui bean
    siteBean = new SiteGuiBean(this, this, this);

    //MAKE changes in this function for any change in test cases
    initTestCasesParamListAndEditor();

    // Create all the available IMRs
    // to add more IMRs, change this function
    initImrParamListAndEditor( );

    //initialize the IMT and IMLs
    initImtParamListAndEditor();

    //init eqkSourceParamList. List of all available sources at this time
    initEqkSourceParamListAndEditor();

    // Create site parameters
    updateSiteParamListAndEditor( );

    //set the site based on the selected test case
    groupTestParameterClass.setParams(testCasesParamList.getParameter(SITE_NUMBER_PARAM).getValue().toString());
  }



  /**
    * Creates a class instance from a string of the full class name including packages.
    * This is how you dynamically make objects at runtime if you don't know which\
    * class beforehand. For example, if you wanted to create a BJF_1997_IMR you can do
    * it the normal way:<P>
    *
    * <code>BJF_1997_IMR imr = new BJF_1997_IMR()</code><p>
    *
    * If your not sure the user wants this one or AS_1997_IMR you can use this function
    * instead to create the same class by:<P>
    *
    * <code>BJF_1997_IMR imr =
    * (BJF_1997_IMR)ClassUtils.createNoArgConstructorClassInstance("org.scec.sha.imt.classicImpl.BJF_1997_IMR");
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
   *  Make the parameter list and editor for all the supported test cases.
   *  MAKE changes here to add more test cases
   */

  protected void initTestCasesParamListAndEditor() {
    // Starting
    String S = C + ": initTestCasesParamListAndEditor(): ";
    if ( D ) System.out.println( S + "Starting:" );


    // make the new paramter list
    testCasesParamList = new ParameterList();

    // fill the test cases vector with the available test cases
    Vector testCasesVector = new Vector();
    // fill the test cases vector with the available test cases

   testCasesVector.add(TEST_CASE_ONE);
   testCasesVector.add(TEST_CASE_TWO);
   testCasesVector.add(TEST_CASE_THREE);
   testCasesVector.add(TEST_CASE_FOUR);
   testCasesVector.add(TEST_CASE_FIVE);
   testCasesVector.add(TEST_CASE_SIX);
   testCasesVector.add(TEST_CASE_SEVEN);
   testCasesVector.add(TEST_CASE_EIGHT_ONE);
   testCasesVector.add(TEST_CASE_EIGHT_TWO);
   testCasesVector.add(TEST_CASE_EIGHT_THREE);
   testCasesVector.add(TEST_CASE_NINE_ONE);
   testCasesVector.add(TEST_CASE_NINE_TWO);
   testCasesVector.add(TEST_CASE_NINE_THREE);
   testCasesVector.add(TEST_CASE_TEN);
   testCasesVector.add(TEST_CASE_ELEVEN);

   // add the available test cases

   StringParameter availableTests = new StringParameter(this.TEST_PARAM_NAME,
                              testCasesVector,(String)testCasesVector.get(0));

   availableTests.addParameterChangeListener(this);
   testCasesParamList.addParameter(availableTests);

   Vector siteNumber =new Vector();
   siteNumber.add(SITE_ONE);
   siteNumber.add(SITE_TWO);
   siteNumber.add(SITE_THREE);
   siteNumber.add(SITE_FOUR);
   siteNumber.add(SITE_FIVE);
   siteNumber.add(SITE_SIX);
   siteNumber.add(SITE_SEVEN);

   StringParameter availableSites = new StringParameter(this.SITE_NUMBER_PARAM,
                                         siteNumber,(String)siteNumber.get(0));
   availableSites.addParameterChangeListener(this);
   testCasesParamList.addParameter(availableSites);
   // now make the editor based on the paramter list
   testCasesEditor = new ParameterListEditor( testCasesParamList, searchPaths);
   testCasesEditor.setTitle( "Select Test" );

  }


  /**
   *  Create a list of all the IMRs
   */
  protected void initImrParamListAndEditor() {


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
      Iterator it= imrClasses.iterator();
      while(it.hasNext()){
        // make the IMR objects as needed to get the site params later
        AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI ) createIMRClassInstance((String)it.next(),this);
        imrObject.add(imr);
        imrNamesVector.add(imr.getName());
      }

      // add the select IMR
      StringParameter selectIMR = new StringParameter(IMR_PARAM_NAME,
                               imrNamesVector,(String)imrNamesVector.get(0));
      // listen to IMR paramter to change site params when it changes
      selectIMR.addParameterChangeListener(this);
      imrParamList.addParameter(selectIMR);
    }

    // remove all the parameters excpet the IMR parameter
    ListIterator it = imrParamList.getParameterNamesIterator();
    while(it.hasNext()) {
      String paramName = (String)it.next();
      if(!paramName.equalsIgnoreCase(IMR_PARAM_NAME))
        imrParamList.removeParameter(paramName);
    }


    // now find the selceted IMR and add the parameters related to it

    // add the trunc type param
    AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI)imrObject.get(0);
    // get the selectedIMR
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
  protected void initImtParamListAndEditor() {

    imtParamList = new ParameterList();

    // get the selected IMR
   String value = (String)imrParamList.getParameter(this.IMR_PARAM_NAME).getValue();


    //add all the supported IMT parameters
    Vector imt = new Vector();
    this.imt_IML_map = new HashMap();
    int size = this.imrObject.size();
    AttenuationRelationshipAPI imr;
    Vector iml=new Vector();


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
        imt.add(new String(param.getName()));
        Vector imlParamsVector=new Vector();
        // add all the independent parameters related to this IMT
        ListIterator it2 = param.getIndependentParametersIterator();
        if(D) System.out.println("IMT is:"+param.getName());
        while ( it2.hasNext() ) {
          iml = new Vector();
          DependentParameterAPI param2 = ( DependentParameterAPI ) it2.next();
          // fon not add SA damping in IMT
          if(param2.getName().equalsIgnoreCase(AttenuationRelationship.DAMPING_NAME))
            continue;
          DoubleDiscreteConstraint values = ( DoubleDiscreteConstraint )param2.getConstraint();
          ListIterator it3 = values.listIterator();
          while(it3.hasNext())   // add all the periods relating to the SA
            iml.add(it3.next().toString());
          StringParameter imlParam = new StringParameter(param2.getName(),
                                             iml, (String)iml.get(0));
          imlParamsVector.add(imlParam);
        }
        // mapping between this IMT and all IMLs supported by it
        if(imlParamsVector.size() > 0)
            imt_IML_map.put(param.getName(), imlParamsVector);
      }
      break;
    }

    // add the IMT paramter
    StringParameter imtParameter = new StringParameter (IMT_PARAM_NAME,imt,
                                                           (String)imt.get(0));
    imtParameter.addParameterChangeListener(this);
    imtParamList.addParameter(imtParameter);

    // add all the IMLs to the current initialized IMT
    updateIML((String)imt.get(0));

    // now make the editor based on the paramter list
    imtEditor = new ParameterListEditor( imtParamList, searchPaths);
    imtEditor.setTitle( "Select IMT" );
  }


   /**
    * init eqkSourceParamList. List of all available sources at this time
    */
    protected void initEqkSourceParamListAndEditor() {


      //add the source Parameter
      Vector faultVector=new Vector();
      faultVector.add(SOURCE_FAULT_ONE);
      faultVector.add(SOURCE_FAULT_AREA);
      StringParameter selectSource= new StringParameter(SOURCE_PARAM_NAME,
                                  faultVector, SOURCE_FAULT_ONE);
      selectSource.addParameterChangeListener(this);
      eqkSourceParamList.addParameter(selectSource);

      //getting the value of the parameters for the fault
      ListIterator it=faultcase1.getAdjustableParamsList();
      while(it.hasNext()){
        eqkSourceParamList.addParameter((ParameterAPI)it.next());
      }

      //getting the value of the parameters for the Area
      it=faultcase2_area.getAdjustableParamsList();
      while(it.hasNext()){
        eqkSourceParamList.addParameter((ParameterAPI)it.next());
      }

      // now make the editor based on the paramter list
      eqkSourceEditor = new ParameterListEditor( eqkSourceParamList, searchPaths);
      eqkSourceEditor.setTitle( "Select Forecast" );
      // fault 1 is selected initially
      setParamsInSourceVisible(this.SOURCE_FAULT_ONE);
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
   Vector imrNames = new Vector();
   imrNames.add(value);
   // now make the editor based on the parameter list
   siteEditor = this.siteBean.updateSite(imrNames);

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
        return siteEditor;
 }


  /**
   *  Gets the Eqk source Editor attribute of the GroupTestGuiBean object
   *
   * @return    The source editor value
   */
   public ParameterListEditor getEqkSourceEditor() {
     return eqkSourceEditor;
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
          initImrParamListAndEditor();
          updateSiteParamListAndEditor();
          initImtParamListAndEditor();
          applet.updateChoosenIMR();
          applet.updateChoosenIMT();
      }

      // if Truncation type changes
      if( name1.equals(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME) ){  // special case hardcoded. Not the best way to do it, but need framework to handle it.
        String value = event.getNewValue().toString();
        toggleSigmaLevelBasedOnTypeValue(value);
      }

      // if source selected by the user  changes
      if( name1.equals(this.SOURCE_PARAM_NAME) ){
        String value = event.getNewValue().toString();
        setParamsInSourceVisible(value);
        applet.updateChoosenEqkSource();
      }

      // set the default values if the test cases are chosen
      if(name1.equals(this.TEST_PARAM_NAME)) {
        String value = event.getNewValue().toString();
        groupTestParameterClass.setSiteNumberParams(value);
        applet.updateChoosenTestCase();
      }

      // update the params when a site is selected
      if(name1.equals(this.SITE_NUMBER_PARAM)) {
        String value = event.getNewValue().toString();
        groupTestParameterClass.setParams(value);
      }
  }


  /**
   * this function is called to make the paramters visible and invisible
   * based on the source selected by the user
   * @param source
   */
  private void setParamsInSourceVisible(String source) {

    // Turn off all parameters - start fresh, then make visible as required below
    ListIterator it = this.eqkSourceParamList.getParametersIterator();

    while ( it.hasNext() )
      eqkSourceEditor.setParameterInvisible( ( ( ParameterAPI ) it.next() ).getName(), false );
    //make the source parameter visible
    eqkSourceEditor.setParameterInvisible(this.SOURCE_PARAM_NAME,true);

    Vector supportedMagDists = new Vector();
    // if fault1 or fault2 is selected
    if(source.equalsIgnoreCase(this.SOURCE_FAULT_ONE))
      it = faultcase1.getAdjustableParamsList();
    else // if Area source is selected
      it = faultcase2_area.getAdjustableParamsList();

   // make the parameters visible or invisible
    while(it.hasNext()) {
      String paramName=((ParameterAPI)it.next()).getName();
      eqkSourceEditor.setParameterInvisible(paramName, true);
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
   * Update the IML based on the selected IMT
   * @param imlName
   */
  private void updateIML(String imlName) {
    // get the IML assocated with this IMT
    Vector imlParams = (Vector)this.imt_IML_map.get(imlName);

    StringParameter imtParam = (StringParameter)imtParamList.getParameter(this.IMT_PARAM_NAME);
    // make the imt param list again
    imtParamList = new ParameterList();
    imtParamList.addParameter(imtParam);
    // if there is IML associated with this IMT
    if(imlParams!=null) {
      int size = imlParams.size();

      for(int i=0; i<size ; ++i) {
        StringParameter param = ( StringParameter )imlParams.get(i);
        if(D) System.out.println("i="+i+" param:"+param.getName());
        if(!imtParamList.containsParameter(param))
          imtParamList.addParameter(param);
      }
    }
    // now make the editor based on the paramter list
    imtEditor = new ParameterListEditor( imtParamList, searchPaths);
    imtEditor.setTitle( "Select IMT" );

  }

  /**
   * Gets the probabilities functiion based on selected parameters
   * this function is called when add Graph is clicked
   */
  public void getChoosenFunction(DiscretizedFuncList funcs) {
    EqkRupForecast eqkRupForecast = null;

    // get the selected forecast model
    String selectedForecast = (String)this.eqkSourceParamList.getValue(this.SOURCE_PARAM_NAME);

    // check which forecast has been selected by the user
    if(selectedForecast.equalsIgnoreCase(this.SOURCE_FAULT_ONE)) {
      //if fault forecast is selected
      eqkRupForecast = this.faultcase1;
    } else if(selectedForecast.equalsIgnoreCase(this.SOURCE_FAULT_AREA)) {
      // if Area forecast is selected
      eqkRupForecast = this.faultcase2_area;
    }

    // intialize the hazard function
    ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();


    //set the X-axis label based on selected IMT
    // get the selected IMT, if it is SA, get the period as well
    String imt = (String)this.imtParamList.getValue(this.IMT_PARAM_NAME);

    // get the IMR names list
    int imrSize=this.imrNamesVector.size();

    // get the selected IMR
    String selectedIMR = (String)this.imrParamList.getValue(this.IMR_PARAM_NAME);

    // selected IMR object
    AttenuationRelationshipAPI imr = null;

    // make a site object to pass to each IMR
    ParameterList siteParams = siteBean.getSiteParamList();
    double longVal= siteBean.getLongitude();
    double latVal = siteBean.getLatitude();
    Site site = new Site(new Location(latVal,longVal));
    site.addParameterList(siteParams);


    // do for each IMR
    for(int i=0;i<imrSize;++i) {
      if(((String)imrNamesVector.get(i)).equalsIgnoreCase(selectedIMR)) {
        // if this IMR is selected
        initDiscretizeValues(hazFunction);
        hazFunction.setInfo(selectedIMR);



        // pass the site object to each IMR
        try {
          if(D) System.out.println("siteString:::"+site.toString());
          imr = (AttenuationRelationshipAPI)imrObject.get(i);


          imr.setIntensityMeasure(imt);

          //set all the independent parameters related to this IMT
          ListIterator it = this.imtParamList.getParameterNamesIterator();
          while(it.hasNext()) {
            String name = (String)it.next();
            if(name.equalsIgnoreCase(this.IMT_PARAM_NAME))
              continue;
            //set independent paramerts  for selected IMT in IMR
            ParameterAPI param = imr.getParameter(name);
            param.setValue(new Double((String)imtParamList.getValue(name)));
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
  class GroupTestDefaultParameterClass {


    protected final static String C = "GroupTestDefaultParameterClass";
    protected final static boolean D = false;

    protected GroupTestGuiBean groupTestGuiBean;

    public GroupTestDefaultParameterClass(GroupTestGuiBean groupTestGuiBean){
      this.groupTestGuiBean = groupTestGuiBean;
    }
    /**
     * This function sets the site Paramters and the IMR parameters based on the
     * selected test case and selected site number for that test case
     * @param siteNumber
     */

    public void setParams(String siteNumber) {
      String S = C + ":setParams()";
      if(D) System.out.println(S+"::entering");
      String value = (String)testCasesParamList.getParameter(TEST_PARAM_NAME).getValue();

      // set the mag dist params based on test case
      setMagDistParams(value);

      String selectedFault= new String(FAULT_ONE);

      //if selected test case is number 1
      if(value.equals(TEST_CASE_ONE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }


      //if selected test case is number 2
      if(value.equals(TEST_CASE_TWO)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if selected test case is number 3
      if(value.equals(TEST_CASE_THREE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if selected test case is number 4
      if(value.equals(TEST_CASE_FOUR)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_TWO);
      }

      //if selected test case is number 5
      if(value.equals(TEST_CASE_FIVE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if selected test case is number 6
      if(value.equals(TEST_CASE_SIX)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if selected test case is number 7
      if(value.equals(TEST_CASE_SEVEN)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if the selected test case is number 8_1
      if(value.equals(TEST_CASE_EIGHT_ONE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if the selected test case is number 8_2
      if(value.equals(TEST_CASE_EIGHT_TWO)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(2.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_TWO);
      }

      //if the selected test case is number 8_3
      if(value.equals(TEST_CASE_EIGHT_THREE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_2SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_ONE);
      }

      //if the selected test case is number 9_1
      if(value.equals(TEST_CASE_NINE_ONE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_TWO);
      }

      //if the selected test case is number 9_2
      if(value.equals(TEST_CASE_NINE_TWO)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(AS_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(AS_1997_IMR.SITE_TYPE_NAME).setValue(AS_1997_IMR.SITE_TYPE_ROCK);
        selectedFault = new String(FAULT_TWO);
      }

      //if the selected test case is number 9_3
      if(value.equals(TEST_CASE_NINE_THREE)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(Campbell_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(Campbell_1997_IMR.STD_DEV_TYPE_MAG_DEP);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(Campbell_1997_IMR.SITE_TYPE_NAME).setValue(Campbell_1997_IMR.SITE_TYPE_SOFT_ROCK);
        siteBean.getSiteParamList().getParameter(Campbell_1997_IMR.BASIN_DEPTH_NAME).setValue(new Double(2.0));
        selectedFault = new String(FAULT_TWO);
      }

      //if the selected test case is number 10
      if(value.equals(TEST_CASE_TEN)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);

        /**
         * This fills the default values for the forecast paramters based
         * on the selected test case 10
         */

        eqkSourceParamList.getParameter(groupTestGuiBean.faultcase2_area.DEPTH_LOWER_PARAM_NAME).setValue(new Double(5));
        eqkSourceParamList.getParameter(groupTestGuiBean.faultcase2_area.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
        selectedFault = new String(FAULT_AREA);
      }

      //if the selected test case is number 11
      if(value.equals(TEST_CASE_ELEVEN)){
        imrParamList.getParameter(IMR_PARAM_NAME).setValue(SCEMY_1997_IMR.NAME);
        imrParamList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
        imrParamList.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
        imtParamList.getParameter(IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
        siteBean.getSiteParamList().getParameter(SCEMY_1997_IMR.SITE_TYPE_NAME).setValue(SCEMY_1997_IMR.SITE_TYPE_ROCK);

        /**
         * This fills the default values for the forecast paramters based
         * on the selected test case 11
         */

        eqkSourceParamList.getParameter(groupTestGuiBean.faultcase2_area.DEPTH_LOWER_PARAM_NAME).setValue(new Double(10));
        eqkSourceParamList.getParameter(groupTestGuiBean.faultcase2_area.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
        selectedFault = new String(FAULT_AREA);
      }

      // set the latitude and longitude and selected forecast is area or fault
      //if selected site number is not 10 or 11 i.e for fault sites
      if(!value.equalsIgnoreCase(TEST_CASE_TEN) && !value.equalsIgnoreCase(TEST_CASE_ELEVEN)) {

        // it is fault test case
        eqkSourceParamList.getParameter(SOURCE_PARAM_NAME).setValue(SOURCE_FAULT_ONE);
        setForecastParams(selectedFault,value);

        // for fault site 1
        if(siteNumber.equals(SITE_ONE)) {
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(38.113));
          siteBean.getSiteParamList().getParameter(siteBean.LONGITUDE).setValue(new Double(-122.0));
        }
        // for fault site 2
        if(siteNumber.equals(SITE_TWO)) {
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(38.113));
          siteBean.getSiteParamList().getParameter(siteBean.LONGITUDE).setValue(new Double(-122.114));

        }
        // for fault site 3
        if(siteNumber.equals(SITE_THREE)) {
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(38.111));
          siteBean.getSiteParamList().getParameter(siteBean.LONGITUDE).setValue(new Double(-122.570));

        }
        // for fault site 4
        if(siteNumber.equals(SITE_FOUR)) {
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(38.000));
          siteBean.getSiteParamList().getParameter(siteBean.LONGITUDE).setValue(new Double(-122.0));

        }
        // for fault site 5
        if(siteNumber.equals(SITE_FIVE)) {
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(37.910));
          siteBean.getSiteParamList().getParameter(siteBean.LONGITUDE).setValue(new Double(-122.0));

        }
        // for fault site 6
        if(siteNumber.equals(SITE_SIX)) {
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(38.225));
          siteBean.getSiteParamList().getParameter(siteBean.LONGITUDE).setValue(new Double(-122.0));

        }
        // for fault site 7
        if(siteNumber.equals(SITE_SEVEN)) {
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(38.113));
          siteBean.getSiteParamList().getParameter(siteBean.LONGITUDE).setValue(new Double(-121.886));
        }
      } else { // for area sites

        // it is area test case
        eqkSourceParamList.getParameter(SOURCE_PARAM_NAME).setValue(SOURCE_FAULT_AREA);

        siteBean.getSiteParamList().getParameter(siteBean.LONGITUDE).setValue(new Double(-122.0));
        // for area site 1
        if(siteNumber.equals(SITE_ONE))
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(38.0));

        // for area site 2
        if(siteNumber.equals(SITE_TWO))
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(37.550));

        // for area site 3
        if(siteNumber.equals(SITE_THREE))
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(37.099));

        // for area site 4
        if(siteNumber.equals(SITE_FOUR))
          siteBean.getSiteParamList().getParameter(siteBean.LATITUDE).setValue(new Double(36.874));
      }

      // refresh the editor according to parameter values
      imrEditor.synchToModel();
      imtEditor.synchToModel();
      siteEditor.synchToModel();
      eqkSourceEditor.synchToModel();
    }

     /**
      * This function generates the combo box for the sites supported by the test case
      * It helps the user in selection for the site number for the particular test case
      * @param testCase
      */
     public void setSiteNumberParams(String testCase) {

       Vector siteNumber =new Vector();

       if(testCase.equals(TEST_CASE_TEN) || testCase.equals(TEST_CASE_ELEVEN)) {
         siteNumber.add(SITE_ONE);
         siteNumber.add(SITE_TWO);
         siteNumber.add(SITE_THREE);
         siteNumber.add(SITE_FOUR);
       }
      else {
         siteNumber.add(SITE_ONE);
         siteNumber.add(SITE_TWO);
         siteNumber.add(SITE_THREE);
         siteNumber.add(SITE_FOUR);
         siteNumber.add(SITE_FIVE);
         siteNumber.add(SITE_SIX);
         siteNumber.add(SITE_SEVEN);
      }

      // add the available parameters
       if(testCasesParamList.containsParameter(SITE_NUMBER_PARAM)){
         testCasesParamList.removeParameter(SITE_NUMBER_PARAM);
       }

       StringParameter availableSites = new StringParameter(SITE_NUMBER_PARAM,
                                  siteNumber,(String)siteNumber.get(0));
       availableSites.addParameterChangeListener(groupTestGuiBean);
       testCasesParamList.addParameter(availableSites);

       // now make the editor based on the paramter list
       testCasesEditor = new ParameterListEditor( testCasesParamList, searchPaths);
       testCasesEditor.setTitle( "Select Test" );
       availableSites.setValue(siteNumber.get(0));
     }

     /**
       * set the mag dist params according to test case
       *
       * @param testCase
       */
     public void setMagDistParams(String testCase) {


        MagFreqDistParameterEditor magEditor;

       // get the paramter based on whether fault or area is selected as forecast
        if(!testCase.equalsIgnoreCase(TEST_CASE_TEN) && ! testCase.equalsIgnoreCase(TEST_CASE_ELEVEN))
          magEditor= (MagFreqDistParameterEditor)eqkSourceEditor.getParameterEditor("Fault Mag Dist");
        else
          magEditor= (MagFreqDistParameterEditor)eqkSourceEditor.getParameterEditor("Area Mag Dist");


        magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(10.0));
        magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(101));
        // mag dist parameters for test case 1
        if(testCase.equalsIgnoreCase(TEST_CASE_ONE)) {
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.5));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        // mag dist parameters  for test case 2
        if(testCase.equalsIgnoreCase(TEST_CASE_TWO)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        // mag dist parameters  for test case 3
        if(testCase.equalsIgnoreCase(TEST_CASE_THREE)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        // mag dist parameters for test case 4
        if(testCase.equalsIgnoreCase(TEST_CASE_FOUR)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
        }

        // mag dist parameters for test case 5
        if(testCase.equalsIgnoreCase(TEST_CASE_FIVE)) {
          magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(1001));
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TO_CUM_RATE);
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(5.0));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.5));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_TO_MORATE).setValue(new Double(1.8e16));
        }


        // mag dist parameters for test case 6
        if(testCase.equalsIgnoreCase(TEST_CASE_SIX)) {
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.GAUSSIAN_TO_MORATE).setValue(new Double(1.8e16));
          magEditor.getParameter(MagFreqDistParameterEditor.STD_DEV).setValue(new Double(0.25));
          magEditor.getParameter(MagFreqDistParameterEditor.MEAN).setValue(new Double(6.2));
          magEditor.getParameter(MagFreqDistParameterEditor.TRUNCATION_REQ).setValue(MagFreqDistParameterEditor.TRUNCATE_FROM_RIGHT);
          magEditor.getParameter(MagFreqDistParameterEditor.TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.2));
        }
        // mag dist parameters for test case 7
        if(testCase.equalsIgnoreCase(TEST_CASE_SEVEN)) {
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.YC_BVALUE).setValue(new Double(0.9));
          magEditor.getParameter(MagFreqDistParameterEditor.YC_DELTA_MAG_CHAR).setValue(new Double(0.5));
          magEditor.getParameter(MagFreqDistParameterEditor.YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
          magEditor.getParameter(MagFreqDistParameterEditor.YC_MAG_LOWER).setValue(new Double(5.0));
          magEditor.getParameter(MagFreqDistParameterEditor.YC_MAG_PRIME).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.YC_MAG_UPPER).setValue(new Double(6.5));
          magEditor.getParameter(MagFreqDistParameterEditor.YC_TO_MORATE).setValue(new Double(1.8e16));
        }

        //mag dist parameters for the test case 8_1
        if(testCase.equalsIgnoreCase(TEST_CASE_EIGHT_ONE)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        //mag dist parameters for the test case 8_2
       if(testCase.equalsIgnoreCase(TEST_CASE_EIGHT_TWO)) {

         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
         magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
         magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        //mag dist parameters for the test case 8_3
       if(testCase.equalsIgnoreCase(TEST_CASE_EIGHT_THREE)) {

         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
         magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
         magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
        }

        //mag dist parameters for the test case 9_1
       if(testCase.equalsIgnoreCase(TEST_CASE_NINE_ONE)) {

         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
         magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
         magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
        }

        //mag dist parameters for the test case 9_2
       if(testCase.equalsIgnoreCase(TEST_CASE_NINE_TWO)) {

          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
          magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
          magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
        }

        //mag dist parameters for the test case 9_1
       if(testCase.equalsIgnoreCase(TEST_CASE_NINE_THREE)) {

         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MORATE);
         magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
         magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
        }

        // mag dist parameters for test case 10
       if(testCase.equalsIgnoreCase(TEST_CASE_TEN)) {
         magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
         magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.GR_TO_MORATE);
         magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(5.0));
         magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.5));
         magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
         magEditor.getParameter(MagFreqDistParameterEditor.TO_CUM_RATE).setValue(new Double(.0395));
        }

        // mag dist parameters for test case 11
       if(testCase.equalsIgnoreCase(TEST_CASE_ELEVEN)) {
          magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
          magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.GR_TO_MORATE);
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(5.0));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.5));
          magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
          magEditor.getParameter(MagFreqDistParameterEditor.TO_CUM_RATE).setValue(new Double(.0395));
        }
        magEditor.getChoosenFunction();
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
        eqkSourceParamList.getParameter(groupTestGuiBean.faultcase1.SIGMA_PARAM_NAME).setValue(faultcase1.DEFAULT_SIGMA_VAL);

        // magLengthSigma parameter is changed if the test case chosen is 3
        if(testCaseVal.equalsIgnoreCase(TEST_CASE_THREE))
          eqkSourceParamList.getParameter(groupTestGuiBean.faultcase1.SIGMA_PARAM_NAME).setValue(new Double(0.2));
        // set the parameters for fault1
        if(faultType.equals(FAULT_ONE)) {
          eqkSourceParamList.getParameter(groupTestGuiBean.faultcase1.DIP_PARAM_NAME).setValue(new Double(90.0));
          eqkSourceParamList.getParameter(groupTestGuiBean.faultcase1.RAKE_PARAM_NAME).setValue(new Double(0.0));
        }
        // set the parameters for fault 2
        if(faultType.equals(FAULT_TWO)) {
          eqkSourceParamList.getParameter(groupTestGuiBean.faultcase1.DIP_PARAM_NAME).setValue(new Double(60.0));
          eqkSourceParamList.getParameter(groupTestGuiBean.faultcase1.RAKE_PARAM_NAME).setValue(new Double(90.0));
        }
      }
  }
}
