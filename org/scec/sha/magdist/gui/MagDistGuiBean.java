package org.scec.sha.magdist.gui;

import java.util.HashMap;
import java.util.Vector;
import java.lang.reflect.*;
import java.util.Iterator;
import java.util.ListIterator;

import org.scec.exceptions.*;
import org.scec.sha.magdist.*;
import org.scec.param.*;
import org.scec.param.editor.ParameterListEditor;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;

/**
 * <p>Title: MagDistGuiBean</p>
 * <p>Description: </p>This class is a java bean container for all the Gui
 *  elements and controller elements for one particular Mag Dist. This allows all the
 *  components to be packaged up in this one class and then for every MagDist that
 *  is created there will be one instance of this bean. This allows these beans
 *  to be easily swapped in and out when you are examining different Mag Dist's in
 *  the main tester applet application.<p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin gupta and Vipin Gupta
 * Date : Aug 11,2002
 * @version 1.0
 */

public class MagDistGuiBean implements ParameterChangeListener {

  protected final static String C = "MagDistGuiBean";
  protected final static boolean D = true;



  /**
   *  Temp until figure out way to dynamically load classes during runtime
   */
  protected final static String GAUSSIAN_NAME = "Gaussian Distribution";
  protected final static String GR_NAME = "GuttenbergRichter Distribution";
  protected final static String SINGLE_NAME = "Single Distribution";
  protected final static String SUMMED_NAME = "Summed Distribution";

  protected final static String DISTRIBUTION_NAME="Choose Distribution";


   /**
    *  Just a placeholder name for this particular MagDist Gui bean.
    */
    protected String name;

    /**
     *  Search path for finding editors in non-default packages.
     */
    final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";

    MagFreqDistTesterApplet applet = null;

    /**
     * Params string value
     */

     private static final String PARAMS_TO_SET=new String("Params to Set");
     private static final String SET_ALL_PARAMS_BUT=new String("Set All Params BUT");
     private static final String MIN=new String("Min");
     private static final String MAX=new String("Max");
     private static final String NUM=new String("Num");


   /**
    * Single Magnitude Frequency Distribution Parameter string list  constant
    */
     private static final String RATE_AND_MAG =new String("Rate & Mag");
     private static final String MAG_AND_MORATE =new String("Mag & Moment Rate");
     private static final String RATE_AND_MORATE=new String("Rate & Moment Rate");
     private static final String RATE=new String("Rate");
     private static final String MAG=new String("Mag");
     private static final String MO_RATE=new String("Moment Rate");



  /**
   * GuttenbergRichter Magnitude Frequency Distribution Parameter string list constant
   */

    private static final String TO_MORATE=new String("Total Moment Rate");
    private static final String TO_CUM_RATE=new String("Total Cum Rate");
    private static final String MAG_UPPER=new String("Mag Upper");
    private static final String MAG_LOWER=new String("Mag Lower");
    private static final String BVALUE=new String("b Value");
    private static final String FIX=new String("fix");
    private static final String FIX_TO_MORATE=new String("Fix Total Moment Rate");
    private static final String FIX_TO_CUM_RATE=new String("Fix Total CUM Rate");


  /**
   * Gaussian Magnitude Frequency Distribution Parameter string list constant
   */

    private static final String MEAN=new String("Mean");
    private static final String STD_DEV=new String("Std Dev");
    private static final String TRUNCATION_REQ=new String("Truncation Required");
    private static final String TRUNCATE_FROM_RIGHT= new String("Truncate from Right");
    private static final String TRUNCATE_ON_BOTH_SIDES= new String("Truncate on Both Sides");
    private static final String TRUNCATE_NUM_OF_STD_DEV= new String("Truncation(# of Std Devs)");
    private static final String NONE= new String("None");


    /**
     *  The Mag Dist is the tester Distribution that will perform
     *  calculations as needed by the Gui.
     */
    protected String magDistClassName = null;


    /**
     *  Parameters that control the graphing gui. Some of
     *  these are dynamically generated from particular independent parameters.
     */
    protected ParameterList controlsParamList = new ParameterList();


    /**
     *  ParameterList of all independent parameters
     */
    protected ParameterList independentParams = new ParameterList();

    /**
     *  This is the paramater list editor that contains all the control
     *  paramaters such as x axis y axis.
     */
    protected ParameterListEditor controlsEditor = null;

    /**
     *  This is the paramater list editor that contains all the independent
     *  paramaters depending on which x axis and y axis are chosen some
     *  paramaters will be made visible or invisible. This is done through this
     *  editor.
     */
    protected ParameterListEditor independentsEditor = null;


    /**
     *  This is name of various classes
     */
    protected final static String GaussianMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.GaussianMagFreqDist";
    protected final static String GuttenbergRichterMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.GuttenbergRichterMagFreqDist";
    protected final static String SingleMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.SingleMagFreqDist";
    protected final static String SummedMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.SummedMagFreqDist";



  /**
     *  Constructor for the MagDistGuiBean object. This constructor is passed in a
     *  MagDist class name, a name for the Gui bean, and the main applet. From this
     *  info. the MagFreqDist class is created at run time along with the paramater
     *  change listener just by the name of the classes.Finally the paramater
     *  editors are created for the independent and control paramaters.
     *
     * @param  className  Fully qualified package and class name of the MagDist
     *      class
     * @param  name       Placeholder name for this Gui bean so it could be
     *      referenced in a hash table or hash map.
     * @param  applet     The main applet application that will use these beans
     *      to swap in and out different Mag Dist's.
     */
    public MagDistGuiBean(MagFreqDistTesterApplet applet) {

        // Starting
        String S = C + ": Constructor(): ";
        if ( D ) System.out.println( S + "Starting:" );
        this.applet = applet;


        initControlsParamListAndEditor( GAUSSIAN_NAME);

        // Create independent parameters
        initIndependentParamListAndEditor(  );

        // Update which parameters should be invisible
        synchRequiredVisibleParameters();
        // All done
        if ( D ) System.out.println( S + "Ending:" );
    }



    /**
     *  <b> FIX *** FIX *** FIX </b> This needs to be fixed along with the whole
     *  function package. Right now only Doubles can be plotted on x-axis as
     *  seen by IncrementalMagFreqDist.<P>
     *
     * @param  applet  Description of the Parameter
     */
    protected void initControlsParamListAndEditor( String dName) {

        // Starting
        String S = C + ": initControlsParamListAndEditor(): ";
        if ( D ) System.out.println( S + "Starting:" );

         /**
         * Adding the distribution name to the ControlEditorList.
         */
        controlsParamList = new ParameterList();
        Vector distName=new Vector();
        distName.add(GAUSSIAN_NAME);
        distName.add(GR_NAME);
        distName.add(SINGLE_NAME);
        StringParameter distributionName =new StringParameter(DISTRIBUTION_NAME,distName,dName);
        controlsParamList.addParameter(distributionName);
        if(dName.equalsIgnoreCase(GR_NAME))
          magDistClassName = new String(GuttenbergRichterMagFreqDist_CLASS_NAME);
        if(dName.equalsIgnoreCase(GAUSSIAN_NAME))
          magDistClassName = new String(GaussianMagFreqDist_CLASS_NAME);
        if(dName.equalsIgnoreCase(SINGLE_NAME))
          magDistClassName = new String(SingleMagFreqDist_CLASS_NAME);

        if ( magDistClassName == null || magDistClassName.trim().equalsIgnoreCase("") )
            throw new ParameterException( S + "Distribution is null, unable to continue." );
        if ( applet == null )
            throw new ParameterException( S + "Applet is null, unable to continue." );

        // make the min, delta and num Parameter

        DoubleParameter minParamter = new DoubleParameter(MIN,new Double(0));
        DoubleParameter maxParamter = new DoubleParameter(MAX,new Double(10));
        IntegerParameter numParamter = new IntegerParameter(NUM,new Integer(101));



        // Now make the parameters list
        // At this point all values have been set for the IM type, xaxis, and the yaxis

         controlsParamList.addParameter( minParamter );
         controlsParamList.addParameter( numParamter );
         controlsParamList.addParameter( maxParamter );


       if(magDistClassName.equalsIgnoreCase(GuttenbergRichterMagFreqDist_CLASS_NAME)) {
           Vector vStrings=new Vector();
           vStrings.add(TO_MORATE);
           vStrings.add(TO_CUM_RATE);
           vStrings.add(MAG_UPPER);
           StringParameter setParamsBut=new StringParameter(SET_ALL_PARAMS_BUT,vStrings,TO_MORATE);
           controlsParamList.addParameter(setParamsBut);
        }

        if(magDistClassName.equalsIgnoreCase(SingleMagFreqDist_CLASS_NAME)) {
           Vector vStrings=new Vector();
           vStrings.add(RATE_AND_MAG);
           vStrings.add(MAG_AND_MORATE);
           vStrings.add(RATE_AND_MORATE);
           StringParameter paramsToSet=new StringParameter(PARAMS_TO_SET,vStrings,RATE_AND_MAG);
           controlsParamList.addParameter(paramsToSet);
        }

        // Now make the Editor for the list
        controlsEditor = new ParameterListEditor( controlsParamList, this, applet );
        controlsEditor.setTitle( "Graph Controls" );

        // All done
        if ( D )
            System.out.println( S + "Ending: Created imr parameter change listener " );

  }



  /**
     *  This function gets the independent paramaters lists from the Mag Dist and
     *  then creates the list editor. These editors know what type of Gui
     *  element to present in the list based on the data type of each paramater.
     *  There is a default location where it looks for these editor classes if
     *  it cannot be found there it will look in the special editors package
     *  file path.
     *
     * @param  applet                  Description of the Parameter
     * @exception  ParameterException  Description of the Exception
     */
    private void initIndependentParamListAndEditor( )
             throws ParameterException {

        // Starting
        String S = C + ": initIndependentParamEditor(): ";
        if ( D )
            System.out.println( S + "Starting:" );
        if (  magDistClassName == null || magDistClassName.trim().equalsIgnoreCase("") )
            throw new ParameterException( S + "MagDist is null, unable to init independent parameters." );

       // Initialize the parameter list
        independentParams = new ParameterList();


       /**
        * add parameters for single distribution
        */
        if(magDistClassName.equalsIgnoreCase(SingleMagFreqDist_CLASS_NAME)) {
           DoubleParameter rate=new DoubleParameter(RATE);
           DoubleParameter mag = new DoubleParameter(MAG);
           DoubleParameter moRate=new DoubleParameter(MO_RATE);
           independentParams.addParameter(rate);
           independentParams.addParameter(mag);
           independentParams.addParameter(moRate);
        }



       /**
        * Add parameters for Gaussian distribution
        */
        if(magDistClassName.equalsIgnoreCase(GaussianMagFreqDist_CLASS_NAME)) {
           DoubleParameter mean = new DoubleParameter(MEAN);
           DoubleParameter stdDev = new DoubleParameter(STD_DEV);
           DoubleParameter totMoRate = new DoubleParameter(TO_MORATE);
           Vector vStrings=new Vector();
           vStrings.add(NONE);
           vStrings.add(TRUNCATE_FROM_RIGHT);
           vStrings.add(TRUNCATE_ON_BOTH_SIDES);
           StringParameter truncType=new StringParameter(TRUNCATION_REQ,vStrings,NONE);
           DoubleParameter truncLevel = new DoubleParameter(TRUNCATE_NUM_OF_STD_DEV);
           independentParams.addParameter(mean);
           independentParams.addParameter(stdDev);
           independentParams.addParameter(totMoRate);
           independentParams.addParameter(truncType);
           independentParams.addParameter(truncLevel);
        }

        /**
         * Add parameters for Guttenberg-Richter distribution
         */
         if(magDistClassName.equalsIgnoreCase(GuttenbergRichterMagFreqDist_CLASS_NAME)) {
           DoubleParameter magLower = new DoubleParameter(MAG_LOWER);
           DoubleParameter magUpper = new DoubleParameter(MAG_UPPER);
           DoubleParameter bValue = new DoubleParameter(BVALUE);
           DoubleParameter toCumRate = new DoubleParameter(TO_CUM_RATE);
           DoubleParameter toMoRate = new DoubleParameter(TO_MORATE);
           independentParams.addParameter(magLower);
           independentParams.addParameter(bValue);
           independentParams.addParameter(magUpper);
           independentParams.addParameter(toCumRate);
           independentParams.addParameter(toMoRate);
           Vector vStrings = new Vector ();
           vStrings.add(FIX_TO_CUM_RATE);
           vStrings.add(FIX_TO_MORATE);
           StringParameter fix = new StringParameter(FIX,vStrings,FIX_TO_CUM_RATE);
           independentParams.addParameter(fix);
         }
        String[] searchPaths = new String[2];
        searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
        searchPaths[1] = SPECIAL_EDITORS_PACKAGE;

         // Build editor list
        independentsEditor = new ParameterListEditor( independentParams, this, applet, searchPaths );
        independentsEditor.setTitle( "Distribution Parameters" );

        // All done
        if ( D )
            System.out.println( S + "Ending: Created imr parameter change listener " );

    }


    /**
     *  Description of the Method
     *
     * @exception  ParameterException  Description of the Exception
     */
    protected void synchRequiredVisibleParameters() throws ParameterException {



         String S = C + ":synchRequiredVisibleParameters:";


         /**
          * Setting the classes for the chosen Distribution
          */
        boolean refresh = false;
        String distributionName=controlsParamList.getParameter(DISTRIBUTION_NAME).getValue().toString();

        /* check if distribution selection changed */
        if(distributionName.equalsIgnoreCase(GR_NAME) &&
                       !magDistClassName.equalsIgnoreCase(this.GuttenbergRichterMagFreqDist_CLASS_NAME)) {
          magDistClassName = GuttenbergRichterMagFreqDist_CLASS_NAME;
          refresh = true;
        }

        if(distributionName.equalsIgnoreCase(GAUSSIAN_NAME) &&
                      !magDistClassName.equalsIgnoreCase(this.GaussianMagFreqDist_CLASS_NAME)) {

           magDistClassName = GaussianMagFreqDist_CLASS_NAME;
           refresh = true;
        }

        if(distributionName.equalsIgnoreCase(SINGLE_NAME)  &&
                     !magDistClassName.equalsIgnoreCase(this.SingleMagFreqDist_CLASS_NAME)) {
          magDistClassName = SingleMagFreqDist_CLASS_NAME;
          refresh = true;
        }

        /* if distribution selection changed, refresh the editors */
        if(refresh) {
          initControlsParamListAndEditor(distributionName);
          initIndependentParamListAndEditor();
        }
        // Turn off all parameters - start fresh, then make visible as required below
        // SWR - Looks like a bug here in setParameterInvisible() - don't want to fix right now, the boolean
        // below should be true, not false.
        ListIterator it = independentParams.getParametersIterator();
        while ( it.hasNext() )
          independentsEditor.setParameterInvisible( ( ( ParameterAPI ) it.next() ).getName(),true);

        /**
         * if Single Mag Freq dist is selected
         * depending on the parameter chosen from the PARAMS_TO_SET, it redraws
         * all the independent parameters again and draws the text boxes to fill
         * in the values.
         */
        if(this.magDistClassName.equalsIgnoreCase(this.SingleMagFreqDist_CLASS_NAME)) {

           String paramToSet=controlsParamList.getParameter(PARAMS_TO_SET).getValue().toString();

           if(paramToSet.equalsIgnoreCase(RATE_AND_MAG)) {
              independentsEditor.setParameterInvisible(MO_RATE,false);
           }
           if(paramToSet.equalsIgnoreCase(MAG_AND_MORATE)) {
              independentsEditor.setParameterInvisible(RATE,false);
           }
           if(paramToSet.equalsIgnoreCase(RATE_AND_MORATE)) {
             independentsEditor.setParameterInvisible(MAG,false);
           }
        }


        /**
         *  If Guttenberg Richter Freq dist is selected
         */
        if(this.magDistClassName.equalsIgnoreCase(this.GuttenbergRichterMagFreqDist_CLASS_NAME)) {
          String paramToSet=controlsParamList.getParameter(SET_ALL_PARAMS_BUT).getValue().toString();

          if(paramToSet.equalsIgnoreCase(TO_MORATE)) {
            independentsEditor.setParameterInvisible(TO_MORATE,false);
            independentsEditor.setParameterInvisible(FIX,false);
          }

          if(paramToSet.equalsIgnoreCase(TO_CUM_RATE)) {
            independentsEditor.setParameterInvisible(TO_CUM_RATE,false);
            independentsEditor.setParameterInvisible(FIX,false);
          }

          if(paramToSet.equalsIgnoreCase(MAG_UPPER))
            independentsEditor.setParameterInvisible(MAG_UPPER,false);
        }

        /**
         * If Gaussian Freq dist is selected
         */
       if(this.magDistClassName.equalsIgnoreCase(this.GaussianMagFreqDist_CLASS_NAME)) {

           String truncReq=independentParams.getParameter(TRUNCATION_REQ).getValue().toString();

           if(truncReq.equalsIgnoreCase(NONE))
              independentsEditor.setParameterInvisible(TRUNCATE_NUM_OF_STD_DEV,false);
           }
           else
              independentsEditor.setParameterInvisible(TRUNCATE_NUM_OF_STD_DEV,true);


        // refresh the GUI
        //controlsEditor.validate();
        //controlsEditor.repaint();



        // All done
        if ( D )
            System.out.println( S + "Ending: " );
    }





   /**
     *  Gets the name attribute of the MagDistGuiBean object
     *
     * @return    The name value
     */
    public String getName() {
        return name;
    }

     /**
     *  Gets the controlsEditor attribute of the MagDistGuiBean object
     *
     * @return    The controlsEditor value
     */
    public ParameterListEditor getControlsEditor() {
        return controlsEditor;
    }

    /**
     *  Gets the independentsEditor attribute of the MagDistGuiBean object
     *
     * @return    The independentsEditor value
     */
    public ParameterListEditor getIndependentsEditor() {
        return independentsEditor;
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

        if ( this.controlsParamList.containsParameter( name1 )  || this.independentParams.containsParameter(name1)) {
            if ( D )
                System.out.println( S + "Control or independent Parameter changed, need to update gui parameter editors" );
            synchRequiredVisibleParameters();
        }
        if(D)
          System.out.println("Name1::"+name1);
        if(name1.equalsIgnoreCase(DISTRIBUTION_NAME)){
         this.applet.updateChoosenMagDist();
        }

    }

    /**
     *  Resets all GUI controls back to the model values. Some models have been
     *  changed when iterating over an independent variable. This function
     *  ensures these changes are reflected in the independent parameter list.
     */
    public void synchToModel() {
        independentsEditor.synchToModel();
    }



    /**
     * Sets the paramsInIteratorVisible attribute of the MagDistGuiBean object
     *
     * @param  it  The new paramsInIteratorVisible value
     */
    private void setParamsInIteratorVisible( ListIterator it ) {

        while ( it.hasNext() ) {
            String name = ( ( ParameterAPI ) it.next() ).getName();
            independentsEditor.setParameterInvisible( name, true );
        }

    }


    /**
     *  Controller function. Dispacter function. Based on which Mag Dist was
     *  choosen, and which parameters are set. determines which dependent
     *  variable discretized function to return.
     *
     * @return                          The choosenFunction value
     * @exception  ConstraintException  Description of the Exception
     */
    public IncrementalMagFreqDist getChoosenFunction()
             throws ConstraintException {


        // Starting
        String S = C + ": getChoosenFunction():";
        if ( D )
            System.out.println( S + "Starting" );



        IncrementalMagFreqDist magDist = null;
        Double min = (Double)controlsParamList.getParameter(MIN).getValue();
        Double max = (Double)controlsParamList.getParameter(MAX).getValue();
        Integer num = (Integer)controlsParamList.getParameter(NUM).getValue();
        //Integer num = new Integer(numDouble.toString());

        /*
         * If Single MagDist is selected
         */
        if(magDistClassName.equalsIgnoreCase(SingleMagFreqDist_CLASS_NAME)) {
            SingleMagFreqDist single =new SingleMagFreqDist(min.doubleValue(),
                                            max.doubleValue(),num.intValue());
            String paramToSet=controlsParamList.getParameter(PARAMS_TO_SET).getValue().toString();
           // if rate and mag are set
           if(paramToSet.equalsIgnoreCase(RATE_AND_MAG)) {
              Double rate = (Double)independentParams.getParameter(RATE).getValue();
              Double mag = (Double)independentParams.getParameter(MAG).getValue();
              single.setMagAndRate(mag.doubleValue(),rate.doubleValue());
           }
           // if mag and moment rate are set
           if(paramToSet.equalsIgnoreCase(MAG_AND_MORATE)) {
              Double mag = (Double)independentParams.getParameter(MAG).getValue();
              Double moRate = (Double)independentParams.getParameter(MO_RATE).getValue();
              single.setMagAndMomentRate(mag.doubleValue(),moRate.doubleValue());
           }
           // if rate and moment  rate are set
           if(paramToSet.equalsIgnoreCase(RATE_AND_MORATE)) {
               Double rate = (Double)independentParams.getParameter(RATE).getValue();
               Double moRate = (Double)independentParams.getParameter(MO_RATE).getValue();
               single.setRateAndMomentRate(rate.doubleValue(),moRate.doubleValue());
           }
          magDist =  (IncrementalMagFreqDist) single;
        }


         /*
         * If Gaussian MagDist is selected
         */
       if(magDistClassName.equalsIgnoreCase(GaussianMagFreqDist_CLASS_NAME)) {
              Double mean = (Double)independentParams.getParameter(MEAN).getValue();
              Double stdDev = (Double)independentParams.getParameter(STD_DEV).getValue();
              Double totMoRate = (Double)independentParams.getParameter(TO_MORATE).getValue();
              String truncTypeValue = independentParams.getParameter(TRUNCATION_REQ).getValue().toString();
              int truncType = 0;
              if(truncTypeValue.equalsIgnoreCase(TRUNCATE_FROM_RIGHT))
                 truncType = 1;
              else if(truncTypeValue.equalsIgnoreCase(TRUNCATE_ON_BOTH_SIDES))
                 truncType = 2;
              GaussianMagFreqDist gaussian;
              if(truncType !=0){
                 Double truncLevel = (Double)independentParams.getParameter(TRUNCATE_NUM_OF_STD_DEV).getValue();
                 gaussian = new GaussianMagFreqDist(min.doubleValue(),max.doubleValue(),num.intValue(),
                          mean.doubleValue(), stdDev.doubleValue(),
                          totMoRate.doubleValue(),truncLevel.doubleValue(),truncType);
              }
              else {
                 gaussian = new GaussianMagFreqDist(min.doubleValue(),max.doubleValue(),
                            num.intValue(),mean.doubleValue(), stdDev.doubleValue(),
                            totMoRate.doubleValue());
              }
              magDist =  (IncrementalMagFreqDist) gaussian;
        }


         /*
         * If Guttenberg Richter MagDist is selected
         */
       if(magDistClassName.equalsIgnoreCase(GuttenbergRichterMagFreqDist_CLASS_NAME)) {
           GuttenbergRichterMagFreqDist gR =
                    new GuttenbergRichterMagFreqDist(min.doubleValue(),max.doubleValue(),
                         num.intValue());

           Double magLower = (Double)independentParams.getParameter(MAG_LOWER).getValue();
           Double bValue = (Double)independentParams.getParameter(BVALUE).getValue();
           String setAllParamsBut = controlsParamList.getParameter(SET_ALL_PARAMS_BUT).getValue().toString();
           // if set all parameters except total moment rate
           if(setAllParamsBut.equalsIgnoreCase(TO_MORATE)) {
              Double magUpper =  (Double)independentParams.getParameter(MAG_UPPER).getValue();
              Double toCumRate = (Double)independentParams.getParameter(TO_CUM_RATE).getValue();
              gR.setAllButTotMoRate(magLower.doubleValue(),magUpper.doubleValue(),
                                    toCumRate.doubleValue(), bValue.doubleValue());
           }
           // if set all parameters except total cumulative rate
           if(setAllParamsBut.equalsIgnoreCase(TO_CUM_RATE)) {
             Double magUpper =  (Double)independentParams.getParameter(MAG_UPPER).getValue();
             Double toMoRate = (Double)independentParams.getParameter(TO_MORATE).getValue();
             gR.setAllButTotCumRate(magLower.doubleValue(),magUpper.doubleValue(),
                                    toMoRate.doubleValue(), bValue.doubleValue());
           }
           // if set all parameters except total moment rate
           if(setAllParamsBut.equalsIgnoreCase(MAG_UPPER)) {
             Double toCumRate = (Double)independentParams.getParameter(TO_CUM_RATE).getValue();
             Double toMoRate = (Double)independentParams.getParameter(TO_MORATE).getValue();
             String  fix = independentParams.getParameter(FIX).getValue().toString();
             boolean relaxCumRate = false;
             if(fix.equalsIgnoreCase(FIX_TO_MORATE))
                relaxCumRate = true;
             gR.setAllButMagUpper(magLower.doubleValue(),toMoRate.doubleValue(),
                                  toCumRate.doubleValue(),bValue.doubleValue(),
                                  relaxCumRate);
           }
          magDist =  (IncrementalMagFreqDist) gR;
       }
     return magDist;
  }
  public String getMagDistName(){
      return controlsParamList.getParameter(DISTRIBUTION_NAME).getValue().toString();
  }
}