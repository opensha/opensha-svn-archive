package org.scec.sha.earthquake.PEER_test_cases;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vipin Gupta and Nitin Gupta  Date: Sept 23, 2002
 * @version 1.0
 */

import java.lang.reflect.*;
import java.math.*;
import java.util.*;

import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.sha.imr.*;
import org.scec.data.function.*;
import org.scec.util.*;
import org.scec.data.*;
import org.scec.sha.magdist.*;


public class GroupTestGuiBean implements
                         NamedObjectAPI,
                         ParameterChangeListener {


  protected final static String C = "GroupTestGuiBean";
  protected final static boolean D = false;
  private String name  = "GroupTestGuiBean";

  //object for the fault
  FaultCaseSet1_Fault faultcase1=new FaultCaseSet1_Fault();

  //object for the Area;
  FaultCaseSet2_Area faultcase2_area=new FaultCaseSet2_Area();

  /**
   *  Search path for finding editors in non-default packages.
   */
  final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";
  private final static String SA_DAMPING = "SA Damping";
  /**
    *  Temp until figure out way to dynamically load classes during runtime
    */
   protected final static String BJF_CLASS_NAME = "org.scec.sha.imr.classicImpl.BJF_1997_IMR";
   protected final static String AS_CLASS_NAME = "org.scec.sha.imr.classicImpl.AS_1997_IMR";
   protected final static String C_CLASS_NAME = "org.scec.sha.imr.classicImpl.Campbell_1997_IMR";
   protected final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.classicImpl.SCEMY_1997_IMR";
   protected final static String F_CLASS_NAME = "org.scec.sha.imr.classicImpl.Field_2000_IMR";
   protected final static String A_CLASS_NAME = "org.scec.sha.imr.classicImpl.Abrahamson_2000_IMR";

   /**
    * Param Names
    */
  private final static String TEST_PARAM_NAME = "Test Cases";
  private final static String IMR_PARAM_NAME = "IMR NAMES";
  private final static String SIGMA_PARAM_NAME =  "Mag Length Sigma";
  private final static String TRUNCTYPE_PARAM_NAME =  "Trunc-Type";
  private final static String TRUNCLEVEL_PARAM_NAME =  "Trunc-Level";
  private final static String IMT_PARAM_NAME =  "Select IMT";

  private final static String MAG_DIST_PARAM_NAME = "Mag Dist";


  // dip name
  private final static String DIP_PARAM_NAME = "Dip";
  //source Name
  private final static String SOURCE_PARAM_NAME = "Source";

  //Source Fault Name
  private final static String SOURCE_FAULT_ONE = "Fault";
  private final static String SOURCE_FAULT_AREA = "Area";

  // default value for timespan field
  private Double DEFAULT_TIMESPAN_VAL = new Double(1);
  // default grid spacing is 1km
  private Double DEFAULT_GRID_VAL = new Double(1);
  //default rupture offset is 1km
  private Double DEFAULT_OFFSET_VAL = new Double(1);

  // values for Mag length sigma
  private Double SIGMA_PARAM_MIN = new Double(0);
  private Double SIGMA_PARAM_MAX = new Double(1);
  private Double DEFAULT_SIGMA_VAL = new Double(0.5);

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
    testCasesVector.add("1");
    testCasesVector.add("2");
    testCasesVector.add("3");
    testCasesVector.add("4");
    testCasesVector.add("5");
    testCasesVector.add("6");
    testCasesVector.add("7");
    testCasesVector.add("8");
    testCasesVector.add("9_1");
    testCasesVector.add("9_2");
    testCasesVector.add("9_3");
    testCasesVector.add("10");
    testCasesVector.add("11");

    // add the available test cases
    StringParameter availableTests = new StringParameter(this.TEST_PARAM_NAME,
                               testCasesVector,(String)testCasesVector.get(0));
    testCasesParamList.addParameter(availableTests);

    // now make the editor based on the paramter list
    testCasesEditor = new ParameterListEditor( testCasesParamList, searchPaths);
    testCasesEditor.setTitle( "Test Cases" );

  }


  /**
   *  Create a list of all the IMRs
   */
  protected void initImrParamListAndEditor() {

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
        ClassicIMRAPI imr = (ClassicIMRAPI ) createIMRClassInstance((String)it.next(),applet);
        imrObject.add(imr);
        imrNamesVector.add(imr.getName());
    }

    // add the select IMR
    StringParameter selectIMR = new StringParameter(IMR_PARAM_NAME,
                               imrNamesVector,(String)imrNamesVector.get(0));
    // listen to IMR paramter to change site params when it changes
    selectIMR.addParameterChangeListener(this);
    imrParamList.addParameter(selectIMR);

    // add the trunc type param
    ClassicIMRAPI imr = (ClassicIMRAPI)imrObject.get(0);
    ParameterAPI typeParam = imr.getParameter(ClassicIMR.SIGMA_TRUNC_TYPE_NAME);
    imrParamList.addParameter(typeParam);
    typeParam.addParameterChangeListener(this);

    // add trunc level
    ParameterAPI levelParam = imr.getParameter(ClassicIMR.SIGMA_TRUNC_LEVEL_NAME);
    imrParamList.addParameter(levelParam);

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
    ClassicIMRAPI imr;
    Vector iml=new Vector();
    Vector imlParamsVector=new Vector();

    // loop over each IMR
    for(int i=0; i < size ; ++i) {
      imr = (ClassicIMRAPI)imrObject.get(i);

     // if this is not the selected IMR then continue
     if(!imr.getName().equalsIgnoreCase(value))
       continue;

      Iterator it1 = imr.getSupportedIntensityMeasuresIterator();
      //loop over each IMT and find IML
      while ( it1.hasNext() ) {
        DependentParameterAPI param = ( DependentParameterAPI ) it1.next();
        imt.add(new String(param.getName()));

        // add all the independent parameters related to this IMT
        ListIterator it2 = param.getIndependentParametersIterator();
        if(D) System.out.println("IMT is:"+param.getName());
        while ( it2.hasNext() ) {
          iml = new Vector();
          DependentParameterAPI param2 = ( DependentParameterAPI ) it2.next();
          // fon not add SA damping in IMT
          if(param2.getName().equalsIgnoreCase(SA_DAMPING))
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
      faultVector.add(this.SOURCE_FAULT_ONE);
      faultVector.add(this.SOURCE_FAULT_AREA);
      StringParameter selectSource= new StringParameter(this.SOURCE_PARAM_NAME,
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

   // make all the parameters as invisible
   Iterator it = imrParamList.getParameterNamesIterator();
   while(it.hasNext()) {
     name = (String)it.next();
     // remove site parameters related to previous IMR
     if(!name.equalsIgnoreCase(IMR_PARAM_NAME) &&
        !name.equalsIgnoreCase(ClassicIMR.SIGMA_TRUNC_TYPE_NAME) &&
        !name.equalsIgnoreCase(ClassicIMR.SIGMA_TRUNC_LEVEL_NAME))

     imrParamList.removeParameter(name);
   }



   // get the selected IMR
   String value = (String)imrParamList.getParameter(this.IMR_PARAM_NAME).getValue();

   // now find the object corresponding to the selected IMR
   int numIMRs = imrObject.size();
   for(int i=0; i<numIMRs; ++i) {
     ClassicIMRAPI imr = (ClassicIMRAPI)imrObject.get(i);

     // if this is the selected IMR
     if(imr.getName().equalsIgnoreCase(value)) {
       // add std dev parameter
       //it =  imr.getStdDevIndependentParamsIterator();
       //while(it.hasNext())
        // imrParamList.addParameter((ParameterAPI)it.next());
       imrParamList.addParameter(imr.getParameter("Std Dev Type"));
       it = imr.getSiteParamsIterator();
       while(it.hasNext())
         imrParamList.addParameter((ParameterAPI)it.next());
       break;
     }
   }

   // now make the editor based on the paramter list
  imrEditor = new ParameterListEditor( imrParamList, searchPaths);
  imrEditor.setTitle( "Select IMR" );

  // set level visible/invisible based on trunc level
   ParameterAPI typeParam = imrParamList.getParameter(ClassicIMR.SIGMA_TRUNC_TYPE_NAME);
   toggleSigmaLevelBasedOnTypeValue(typeParam.getValue().toString());
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
          updateSiteParamListAndEditor();
          initImtParamListAndEditor();
          applet.updateChoosenIMR();
          applet.updateChoosenIMT();
      }

      // if Truncation type changes
      if( name1.equals(ClassicIMR.SIGMA_TRUNC_TYPE_NAME) ){  // special case hardcoded. Not the best way to do it, but need framework to handle it.
        String value = event.getNewValue().toString();
        toggleSigmaLevelBasedOnTypeValue(value);
      }

      // if source selected by the user  changes
      if( name1.equals(this.SOURCE_PARAM_NAME) ){
        String value = event.getNewValue().toString();
        setParamsInSourceVisible(value);
        applet.updateChoosenEqkSource();
       // applet.updateChoosenMagDist();
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
      imrEditor.setParameterInvisible( ClassicIMR.SIGMA_TRUNC_LEVEL_NAME, false );
    }
    else{
      if(D) System.out.println("Value = " + value + ", need to set value param on.");
      imrEditor.setParameterInvisible( ClassicIMR.SIGMA_TRUNC_LEVEL_NAME, true );
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
}
