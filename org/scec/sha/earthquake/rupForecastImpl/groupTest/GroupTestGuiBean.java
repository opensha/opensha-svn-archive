package org.scec.sha.earthquake.rupForecastImpl.groupTest;

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
import org.scec.sha.magdist.gui.MagDistGuiBean;
import org.scec.sha.magdist.gui.MagFreqDistTesterAPI;

public class GroupTestGuiBean implements
                         NamedObjectAPI,
                         ParameterChangeListener {


  protected final static String C = "GroupTestGuiBean";
  protected final static boolean D = true;
  private String name  = "GroupTestGuiBean";


  /**
   *  Search path for finding editors in non-default packages.
   */
  final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";

  /**
    *  Temp until figure out way to dynamically load classes during runtime
    */
   protected final static String BJF_CLASS_NAME = "org.scec.sha.imr.classicImpl.BJF_1997_IMR";
   protected final static String AS_CLASS_NAME = "org.scec.sha.imr.classicImpl.AS_1997_IMR";
   protected final static String C_CLASS_NAME = "org.scec.sha.imr.classicImpl.Campbell_1997_IMR";
   protected final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.classicImpl.SCEMY_1997_IMR";
   protected final static String F_CLASS_NAME = "org.scec.sha.imr.classicImpl.Field_2000_IMR";
   protected final static String A_CLASS_NAME = "org.scec.sha.imr.classicImpl.Abrahamson_2000_IMR";


  private final static String TEST_PARAM_NAME = "Test Cases";
  private final static String IMR_PARAM_NAME = "IMR NAMES";
  private final static String SIGMA_PARAM_NAME =  "Sigma";
  private final static String TRUNCTYPE_PARAM_NAME =  "Trunc-Type";
  private final static String TRUNCLEVEL_PARAM_NAME =  "Trunc-Level";
  //SigmaType
  private final static String SIGMA_TOTAL =  "Total";
  private final static String SIGMA_ZERO =  "Zero";

  //TruncType
  private final static String ZERO =  "0";
  private final static String ONE =  "1";
  private final static String TWO =  "2";

  //timespan Variable
  private final static String TIMESPAN_PARAM_NAME = "Timespan(#yrs)";

  //source Name
  private final static String SOURCE_PARAM_NAME = "Source";

  //Source Fault Name
  private final static String SOURCE_FAULT_ONE = "Fault-1";
  private final static String SOURCE_FAULT_TWO = "Fault-2";
  private final static String SOURCE_FAULT_AREA = "Area";

  //this vector saves all the IMR classes name
  Vector imrNamesVector=new Vector();

  // this vector saves all the present test cases
  Vector testCasesVector;

  //save all the IMR classes in the vector
  Vector imrClasses;


  /**
   *  This is the paramater list editor that contains all the control
   *  paramaters such as x axis y axis.
   */
  protected ParameterListEditor controlsEditor = null;

 /**
   * editor for site paramters
   */
  private ParameterListEditor siteEditor = null;


  /**
   *  Parameters that control the graphing gui, specifically the IM Types
   *  picklist, the Y-Values picklist, and the X-Values picklist. Some of
   *  these are dynamically generated from particular independent parameters.
   */
  private ParameterList controlsParamList = new ParameterList();


  /**
   *  ParameterList of all independent parameters
   */
  private ParameterList independentParams = new ParameterList();

  /**
   *  Site ParameterList
   */
  private ParameterList siteParamList = new ParameterList();


  //saves the IMR objects, to the parameters related to an IMR.
  private Vector imrObject = new Vector();
  private GroupTestApplet applet= null;

  //mag dit bean instance for the mag Freq Dist implementations
  private MagDistGuiBean magDistBean;


  /**
   * constructor
   */
  public GroupTestGuiBean(GroupTestApplet applet) {


    this.applet = applet;
   // fill the test cases vector with the available test cases
    testCasesVector = new Vector();
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
        ClassicIMRAPI imr = (ClassicIMRAPI ) createIMRClassInstance((String)it.next(),applet);
        imrObject.add(imr);
        imrNamesVector.add(imr.getName());
    }

    //create the instance of magdistbean
    this.magDistBean = new MagDistGuiBean(applet);

    // Create the control parameters for this imr
    initControlsParamListAndEditor( );

    // Create independent parameters
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
   *  <b> FIX *** FIX *** FIX </b> This needs to be fixed along with the whole
   *  function package. Right now only Doubles can be plotted on x-axis as
   *  seen by DiscretizedFunction2DAPI.<P>
   *
   *  One thing to note is that all graph constrols in this list are
   *  Parameters with String constraints.<p>
   *
   *  Then a new controls paramater editor list for these paramaters are
   *  created.
   *
   */

  protected void initControlsParamListAndEditor() {

    // Starting
    String S = C + ": initControlsParamListAndEditor(): ";
    if ( D ) System.out.println( S + "Starting:" );


    // make the new paramter list
    controlsParamList = new ParameterList();


    // add the available test cases
    StringParameter availableTests = new StringParameter(this.TEST_PARAM_NAME,
                               testCasesVector,(String)testCasesVector.get(0));
    controlsParamList.addParameter(availableTests);
    availableTests.addParameterChangeListener(this);
    // add the select IMR
    StringParameter selectIMR = new StringParameter(IMR_PARAM_NAME,
                               imrNamesVector,(String)imrNamesVector.get(0));
    controlsParamList.addParameter(selectIMR);
    selectIMR.addParameterChangeListener(this);
    //add the sigma parameter
    Vector sigmaVector =new Vector();
    sigmaVector.add(SIGMA_TOTAL);
    sigmaVector.add(SIGMA_ZERO);
    StringParameter selectSigma= new StringParameter(this.SIGMA_PARAM_NAME,
                        sigmaVector,SIGMA_TOTAL);
    controlsParamList.addParameter(selectSigma);
    selectSigma.addParameterChangeListener(this);
    //add the truncType parameter
    Vector truncVector= new Vector();
    truncVector.add(ZERO);
    truncVector.add(ONE);
    truncVector.add(TWO);

    StringParameter selectTruncType= new StringParameter(this.TRUNCTYPE_PARAM_NAME,
                            truncVector, ZERO);
    selectTruncType.addParameterChangeListener(this);
    controlsParamList.addParameter(selectTruncType);

    //add the TruncLevel Parameter
    DoubleParameter selectTruncLevel = new DoubleParameter(this.TRUNCLEVEL_PARAM_NAME);
    selectTruncLevel.addParameterChangeListener(this);
    selectTruncLevel.addParameterChangeFailListener(applet);
    controlsParamList.addParameter(selectTruncLevel);

    //add the source Parameter
    Vector faultVector=new Vector();
    faultVector.add(this.SOURCE_FAULT_ONE);
    faultVector.add(this.SOURCE_FAULT_TWO);
    faultVector.add(this.SOURCE_FAULT_AREA);
    StringParameter selectSource= new StringParameter(this.SOURCE_PARAM_NAME,
                                  faultVector, SOURCE_FAULT_ONE);
    selectSource.addParameterChangeListener(this);
    controlsParamList.addParameter(selectSource);

    // add the TimeSpan Parameter
    DoubleParameter selectTimeSpan = new DoubleParameter(this.TIMESPAN_PARAM_NAME,new Double(1.0));
    controlsParamList.addParameter(selectTimeSpan);
    selectTimeSpan.addParameterChangeFailListener(applet);
    selectTimeSpan.addParameterChangeListener(this);


    String[] searchPaths = new String[2];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    searchPaths[1] = SPECIAL_EDITORS_PACKAGE;
    // now make the editor based on the paramter list
    controlsEditor = new ParameterListEditor( controlsParamList);
    controlsEditor.setTitle( "Graph Controls" );


    // All done
    if ( D )
        System.out.println( S + "Ending: Created imr parameter change listener " );

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

   siteParamList = new ParameterList();
   // get the selected IMR
   String value = (String)controlsParamList.getParameter(this.IMR_PARAM_NAME).getValue();


   // now find the object corresponding to the selected IMR
   int numIMRs = imrObject.size();
   for(int i=0; i<numIMRs; ++i) {
     ClassicIMRAPI imr = (ClassicIMRAPI)imrObject.get(i);
     // if this is the selected IMR
     if(imr.getName().equalsIgnoreCase(value)) {
       Iterator it = imr.getSiteParamsIterator();
       siteParamList.addParameter((ParameterAPI)it.next());
       break;
     }
   }

  // now make the site editor based on the param list
   siteEditor = new ParameterListEditor( siteParamList );
   siteEditor.setTitle( "Site Paramters" );
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
   *  Gets the controlsEditor attribute of the GroupTestGuiBean object
   *
   * @return    The controlsEditor value
   */
  public ParameterListEditor getControlsEditor() {
        return controlsEditor;
 }

 /**
  *  Gets the siteEditor attribute of the GroupTestGuiBean object
  *
  * @return    The siteEditor value
  */
 public ParameterListEditor getSiteEditor() {
        return this.siteEditor;
 }

/**
 *  Gets the independentsEditor attribute of the MagDistGuiBean object
 *
 * @return    The independentsEditor value
 */
 public ParameterListEditor getMagDistIndependentsEditor() {
       return this.magDistBean.getIndependentsEditor();
 }

 /**
  *  Gets the controlssEditor attribute of the MagDistGuiBean object
  *
  * @return    The controlsEditor value
  */
  public ParameterListEditor getMagDistControlsEditor() {
      return this.magDistBean.getControlsEditor();
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
      if ( name1.equalsIgnoreCase(this.IMR_PARAM_NAME)) {
          if ( D )
              System.out.println( S + "Control Parameter changed, need to update gui parameter editors" );
          this.updateSiteParamListAndEditor();
      }

  }

}