package org.scec.sha.magdist;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JButton;

import org.scec.gui.LabeledBoxPanel;
import org.scec.param.editor.*;
import org.scec.param.*;
import org.scec.exceptions.*;
import org.scec.param.event.*;

/**
 *  <b>Title:</b> MagFreqDistParameterEditor<p>
 *
 *  <b>Description:</b> The main Parameter Editor Panel that takes in a
 *  MagFreqDist parameter. All the Mag dist parameters are
 *  presented in a Scroll Pane so all parameters are accessable, no matter the
 *  size of the containing application<p>
 *
 * @author     Nitin Gupta, Vipin Gupta
 * @created    Oct 18, 2002
 * @version    1.0
 */

public class MagFreqDistParameterEditor extends ParameterListEditor
    implements ParameterChangeListener, ActionListener {

    /** Class name for debugging. */
    protected final static String C = "MagFreqDistParameterEditor";
    /** If true print out debug statements. */
    protected final static boolean D = false;
    private Insets defaultInsets = new Insets( 4, 4, 4, 4 );



  protected final static String DISTRIBUTION_NAME="Choose Distribution";


   /**
    *  Just a placeholder name for this particular MagDist Gui bean.
    */
    protected String name;

    private MagFreqDistParameter magDistParam;
    /**
     *  Search path for finding editors in non-default packages.
     */
    final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";


    /**
     * Params string value
     */
     private static final String MAG_DIST_TITLE = new String("Mag Dist Params");
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
   * GutenbergRichter Magnitude Frequency Distribution Parameter string list constant
   */
    private static final String GR_TO_MORATE=new String("Total Moment Rate");
    private static final String TO_CUM_RATE=new String("Total Cum Rate");
    private static final String GR_MAG_UPPER=new String("Mag Upper");
    private static final String GR_MAG_LOWER=new String("Mag Lower");
    private static final String GR_BVALUE=new String("b Value");
    private static final String FIX=new String("fix");
    private static final String FIX_TO_MORATE=new String("Fix Total Moment Rate");
    private static final String FIX_TO_CUM_RATE=new String("Fix Total CUM Rate");


    /**
     * Young and Coppersmith, 1985 Char
     */
    private static final String YC_MAG_UPPER=new String("Mag Upper");
    private static final String YC_MAG_LOWER=new String("Mag Lower");
    private static final String YC_BVALUE=new String("b Value");
    private static final String YC_DELTA_MAG_CHAR = new String("Delta Mag Char");
    private static final String YC_MAG_PRIME = new String("Mag Prime");
    private static final String YC_DELTA_MAG_PRIME = new String("Delta Mag Prime");
    private static final String YC_TO_MORATE=new String("Total Moment Rate");


  /**
   * Gaussian Magnitude Frequency Distribution Parameter string list constant
   */
    private static final String GAUSSIAN_TO_MORATE=new String("Total Moment Rate");
    private static final String MEAN=new String("Mean");
    private static final String STD_DEV=new String("Std Dev");
    private static final String TRUNCATION_REQ=new String("Truncation Required");
    private static final String TRUNCATE_FROM_RIGHT= new String("Truncate from Right");
    private static final String TRUNCATE_ON_BOTH_SIDES= new String("Truncate on Both Sides");
    private static final String TRUNCATE_NUM_OF_STD_DEV= new String("Truncation(# of Std Devs)");
    private static final String NONE= new String("None");

    /**
     *  This is name of various classes
     */
    protected final static String GaussianMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.GaussianMagFreqDist";
    protected final static String GutenbergRichterMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.GutenbergRichterMagFreqDist";
    protected final static String YC_1985_CharMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.YC_1985_CharMagFreqDist";
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
    public MagFreqDistParameterEditor(MagFreqDistParameter param)  {
        //super();
        // Starting
        String S = C + ": Constructor(): ";
        if ( D ) System.out.println( S + "Starting:" );

        this.setLayout( new GridBagLayout());

        this.magDistParam = param;

        // Build package names search path
       searchPaths = new String[1];
       searchPaths[0] = ParameterListEditor.getDefaultSearchPath();

        // make the params editor
        initParamListAndEditor();


        // Update which parameters should be invisible
        synchRequiredVisibleParameters();

        // All done
        if ( D ) System.out.println( S + "Ending:" );
    }

    /**
     * This function when update Mag dist is called
     *
     * @param ae
     */
    public void actionPerformed(ActionEvent ae ) {
      this.getChoosenFunction();
    }
    /**
     * this function is called when focus is lost from the panel
     * On focus lost, we need to update the mag dist
     *
     * @param fe
     */
    /*public void focusLost( FocusEvent fe) {
       if (D) System.out.println(this.C+"Focus lost event");
       boolean focusInPanel=false;
       // make this as focus lost listener for all components in this editor
       Component params[] = editorPanel.getComponents();
       int size=params.length;
       for(int i=0; i<size && !focusInPanel; ++i)
            focusInPanel = params[i].hasFocus();

       // if we loose focus from this panel, calculate Mag Dist
       if(!focusInPanel) {
         if(D) System.out.println(this.C+" " +" calculating magdist again");
         this.getChoosenFunction();
       }
    }*/


    /**
     * this function is called when focus is gained
     *
     * @param fe
     */
    /*public void focusGained( FocusEvent fe) {

    }*/


    /**
     *  <b> FIX *** FIX *** FIX </b> This needs to be fixed along with the whole
     *  function package. Right now only Doubles can be plotted on x-axis as
     *  seen by IncrementalMagFreqDist.<P>
     *
     * @param  applet  Description of the Parameter
     */
    protected void initParamListAndEditor()  {

        // Starting
        String S = C + ": initControlsParamListAndEditor(): ";
        if ( D ) System.out.println( S + "Starting:" );

         /**
         * Adding the distribution name to the ControlEditorList.
         */
        parameterList = new ParameterList();
        StringParameter distributionName =new StringParameter(DISTRIBUTION_NAME,
            this.magDistParam.getAllowedMagDists(),
            (String)this.magDistParam.getAllowedMagDists().get(0));
        parameterList.addParameter(distributionName);
        distributionName.addParameterChangeListener(this);

        // make the min, delta and num Parametter
        DoubleParameter minParameter = new DoubleParameter(MIN,new Double(0));
        DoubleParameter maxParameter = new DoubleParameter(MAX,new Double(10));
        IntegerParameter numParameter = new IntegerParameter(NUM,new Integer(101));


        // Now make the parameters list
        // At this point all values have been set for the IM type, xaxis, and the yaxis

         parameterList.addParameter( minParameter );
         parameterList.addParameter( numParameter );
         parameterList.addParameter( maxParameter );

         // add Parameters for single Mag freq dist
         DoubleParameter rate=new DoubleParameter(RATE);
         DoubleParameter mag = new DoubleParameter(MAG);
         DoubleParameter moRate=new DoubleParameter(MO_RATE);
         Vector vStrings=new Vector();
         vStrings.add(RATE_AND_MAG);
         vStrings.add(MAG_AND_MORATE);
         vStrings.add(RATE_AND_MORATE);
         StringParameter paramsToSet=new StringParameter(PARAMS_TO_SET,vStrings,RATE_AND_MAG);
         parameterList.addParameter(paramsToSet);
         paramsToSet.addParameterChangeListener(this);
         parameterList.addParameter(rate);
         parameterList.addParameter(mag);
         parameterList.addParameter(moRate);


         /**
          * Add parameters for Gaussian distribution
          */
         DoubleParameter mean = new DoubleParameter(MEAN);
         DoubleParameter stdDev = new DoubleParameter(STD_DEV);
         DoubleParameter toMoRate = new DoubleParameter(GAUSSIAN_TO_MORATE);
         vStrings=new Vector();
         vStrings.add(NONE);
         vStrings.add(TRUNCATE_FROM_RIGHT);
         vStrings.add(TRUNCATE_ON_BOTH_SIDES);
         StringParameter truncType=new StringParameter(TRUNCATION_REQ,vStrings,NONE);
         truncType.addParameterChangeListener(this);
         DoubleParameter truncLevel = new DoubleParameter(TRUNCATE_NUM_OF_STD_DEV);
         parameterList.addParameter(mean);
         parameterList.addParameter(stdDev);
         parameterList.addParameter(toMoRate);
         parameterList.addParameter(truncType);
         parameterList.addParameter(truncLevel);


          /**
           * Add parameters for Gutenberg-Richter distribution
           */
         DoubleParameter magLower = new DoubleParameter(GR_MAG_LOWER);
         DoubleParameter magUpper = new DoubleParameter(GR_MAG_UPPER);
         DoubleParameter bValue = new DoubleParameter(GR_BVALUE);
         DoubleParameter toCumRate = new DoubleParameter(TO_CUM_RATE);
         DoubleParameter gr_toMoRate = new DoubleParameter(GR_TO_MORATE);
         vStrings=new Vector();
         vStrings.add(GR_TO_MORATE);
         vStrings.add(TO_CUM_RATE);
         vStrings.add(GR_MAG_UPPER);
         StringParameter setParamsBut=new StringParameter(SET_ALL_PARAMS_BUT,vStrings,GR_TO_MORATE);
         parameterList.addParameter(setParamsBut);
         setParamsBut.addParameterChangeListener(this);
         parameterList.addParameter(magLower);
         parameterList.addParameter(magUpper);
         parameterList.addParameter(bValue);
         parameterList.addParameter(toCumRate);
         //controlsParamList.addParameter(gr_toMoRate);
         Vector vStrings1 = new Vector ();
         vStrings1.add(FIX_TO_CUM_RATE);
         vStrings1.add(FIX_TO_MORATE);
         StringParameter fix = new StringParameter(FIX,vStrings1,FIX_TO_CUM_RATE);
         parameterList.addParameter(fix);
         fix.addParameterChangeListener(this);



         /**
          * Add paramters for Youngs and Coppersmith 1985 char distribution
          */
         DoubleParameter yc_magLower = new DoubleParameter(YC_MAG_LOWER);
         DoubleParameter yc_magUpper = new DoubleParameter(YC_MAG_UPPER);
         DoubleParameter deltaMagChar = new DoubleParameter(YC_DELTA_MAG_CHAR);
         DoubleParameter magPrime = new DoubleParameter(YC_MAG_PRIME);
         DoubleParameter deltaMagPrime = new DoubleParameter(YC_DELTA_MAG_PRIME);
         DoubleParameter yc_bValue = new DoubleParameter(YC_BVALUE);
         DoubleParameter yc_toMoRate = new DoubleParameter(YC_TO_MORATE);
         //parameterList.addParameter(yc_magLower);
         //parameterList.addParameter(yc_magUpper);
         parameterList.addParameter(deltaMagChar);
         parameterList.addParameter(magPrime);
         parameterList.addParameter(deltaMagPrime);
         setTitle(MAG_DIST_TITLE);
         //parameterList.addParameter(yc_bValue);
         //parameterList.addParameter(yc_toMoRate);


         addParameters();

       /*  // make this as focus lost listener for all components in this editor
         Component params[]=this.editorPanel.getComponents();
         int size=params.length;
         for(int i=0; i<size ; ++i) {
           Component textParams[] = ((ParameterEditor)params[i]).getComponents();
           int textParamSize = textParams.length;
           if(D) System.out.println(C+"size: "+textParamSize);
           for(int j=0; j < textParamSize; ++j)
             textParams[j].addFocusListener(this);
         }*/

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

        String distributionName=parameterList.getParameter(DISTRIBUTION_NAME).getValue().toString();


        // Turn off all parameters - start fresh, then make visible as required below
        ListIterator it = parameterList.getParametersIterator();
        while ( it.hasNext() )
          setParameterInvisible( ( ( ParameterAPI ) it.next() ).getName(), false);

        // make the min, max, num and select dist to be visible
        setParameterInvisible(this.MIN,true);
        setParameterInvisible(this.MAX,true);
        setParameterInvisible(this.NUM,true);
        setParameterInvisible(this.DISTRIBUTION_NAME,true);

        /**
         * if Single Mag Freq dist is selected
         * depending on the parameter chosen from the PARAMS_TO_SET, it redraws
         * all the independent parameters again and draws the text boxes to fill
         * in the values.
         */
        if(distributionName.equalsIgnoreCase(SingleMagFreqDist.NAME))
           this.setSingleDistParamsVisible();

        /**
         *  If Gutenberg Richter Freq dist is selected
         */
        if(distributionName.equalsIgnoreCase(GutenbergRichterMagFreqDist.NAME))
          this.setGR_DistParamsVisible();

        /**
         * If Gaussian Freq dist is selected
         */
       if(distributionName.equalsIgnoreCase(GaussianMagFreqDist.NAME))
          this.setGaussianDistParamsVisible();

       /**
        * If YC Freq dist is selected
        */
      if(distributionName.equalsIgnoreCase(YC_1985_CharMagFreqDist.NAME))
         this.setYC_DistParamsVisible();

       // All done
        if ( D )
            System.out.println( S + "Ending: " );
    }

    /**
     * make the parameters related to SINGLE Mag dist visible
     */
    private void setSingleDistParamsVisible() {

      setParameterInvisible(PARAMS_TO_SET, true);
      String paramToSet=parameterList.getParameter(PARAMS_TO_SET).getValue().toString();
      // if Rate and Mag is selected
      if(paramToSet.equalsIgnoreCase(RATE_AND_MAG)) {
        setParameterInvisible(RATE, true);
        setParameterInvisible(MAG, true);
        setParameterInvisible(MO_RATE, false);
      }
      // if Mag and Mo Rate is selected
      if(paramToSet.equalsIgnoreCase(MAG_AND_MORATE)) {
        setParameterInvisible(RATE, false);
        setParameterInvisible(MAG, true);
        setParameterInvisible(MO_RATE, true);
      }
      // if Rate and Mo Rate is selected
      if(paramToSet.equalsIgnoreCase(RATE_AND_MORATE)) {
        setParameterInvisible(RATE, true);
        setParameterInvisible(MAG, false);
        setParameterInvisible(MO_RATE, true);
      }
  }

    /**
     * make the parameters related to GAUSSIAN Mag dist visible
     */
    private void setGaussianDistParamsVisible() {
      // set all the parameters visible
      setParameterInvisible(MEAN, true);
      setParameterInvisible(STD_DEV, true);
      setParameterInvisible(GAUSSIAN_TO_MORATE, true);
      setParameterInvisible(TRUNCATION_REQ, true);

      String truncReq=parameterList.getParameter(TRUNCATION_REQ).getValue().toString();

      // make the truncation level visible only if truncation req is NOT NONE
      if(truncReq.equalsIgnoreCase(NONE))
      setParameterInvisible(TRUNCATE_NUM_OF_STD_DEV,false);
      else
        setParameterInvisible(TRUNCATE_NUM_OF_STD_DEV,true);

    }

    /**
     * make the parameters related to Gutenberg Richter Mag dist visible
     */
    private void setGR_DistParamsVisible() {

      setParameterInvisible(SET_ALL_PARAMS_BUT, true);
      setParameterInvisible(this.GR_MAG_LOWER, true);
      setParameterInvisible(this.GR_BVALUE, true);

      // now make the params visible/invisible based on params desired to be set by user
      String paramToSet=parameterList.getParameter(SET_ALL_PARAMS_BUT).getValue().toString();

      // set all paramerts except total Mo rate
      if(paramToSet.equalsIgnoreCase(GR_TO_MORATE)) {
        setParameterInvisible(TO_CUM_RATE,true);
        setParameterInvisible(GR_MAG_UPPER,true);
        setParameterInvisible(GR_TO_MORATE,false);
        setParameterInvisible(FIX,false);
      }

      // set all parameters except cumulative rate
      if(paramToSet.equalsIgnoreCase(TO_CUM_RATE)) {
        setParameterInvisible(GR_MAG_UPPER,true);
        setParameterInvisible(GR_TO_MORATE,true);
        setParameterInvisible(TO_CUM_RATE,false);
        setParameterInvisible(FIX,false);
      }

      // set all parameters except mag upper
      if(paramToSet.equalsIgnoreCase(GR_MAG_UPPER)) {
        setParameterInvisible(TO_CUM_RATE,true);
        setParameterInvisible(GR_TO_MORATE,true);
        setParameterInvisible(FIX,true);
        setParameterInvisible(GR_MAG_UPPER,false);
      }
    }

    /**
     * make the parameters related to Youngs and Coppersmith Mag dist visible
     */
    private void setYC_DistParamsVisible() {
      // set the  paramters in YC visible
      DoubleParameter yc_magLower = new DoubleParameter(YC_MAG_LOWER);
      DoubleParameter yc_magUpper = new DoubleParameter(YC_MAG_UPPER);
      DoubleParameter deltaMagChar = new DoubleParameter(YC_DELTA_MAG_CHAR);
      DoubleParameter magPrime = new DoubleParameter(YC_MAG_PRIME);
      DoubleParameter deltaMagPrime = new DoubleParameter(YC_DELTA_MAG_PRIME);
      DoubleParameter yc_bValue = new DoubleParameter(YC_BVALUE);
      DoubleParameter yc_toMoRate = new DoubleParameter(YC_TO_MORATE);
      setParameterInvisible(YC_MAG_LOWER, true);
      setParameterInvisible(YC_MAG_UPPER, true);
      setParameterInvisible(YC_DELTA_MAG_CHAR, true);
      setParameterInvisible(YC_MAG_PRIME, true);
      setParameterInvisible(YC_DELTA_MAG_PRIME, true);
      setParameterInvisible(YC_BVALUE, true);
      setParameterInvisible(YC_TO_MORATE, true);
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

        if(name1.equalsIgnoreCase(this.DISTRIBUTION_NAME)) {
            try { // if selectde distribution changes
              synchRequiredVisibleParameters();
            }catch (Exception e) {
              System.out.println(this.C+" "+e.toString());
              e.printStackTrace();
            }
        }
        else { // if only paramerts within a paramter change
          String distributionName=parameterList.getParameter(DISTRIBUTION_NAME).getValue().toString();
          /** if Single Mag Freq dist is selected*/
          if(distributionName.equalsIgnoreCase(SingleMagFreqDist.NAME))
           this.setSingleDistParamsVisible();

        /** If Gutenberg Richter Freq dist is selected */
        if(distributionName.equalsIgnoreCase(GutenbergRichterMagFreqDist.NAME))
          this.setGR_DistParamsVisible();

        /**If Gaussian Freq dist is selected*/
       if(distributionName.equalsIgnoreCase(GaussianMagFreqDist.NAME))
          this.setGaussianDistParamsVisible();

       /**If YC Freq dist is selected*/
      if(distributionName.equalsIgnoreCase(YC_1985_CharMagFreqDist.NAME))
         this.setYC_DistParamsVisible();

        }

    }

    /**
     *  Resets all GUI controls back to the model values. Some models have been
     *  changed when iterating over an independent variable. This function
     *  ensures these changes are reflected in the independent parameter list.
     */
    public void synchToModel() {
        synchToModel();
    }



    /**
     * Sets the paramsInIteratorVisible attribute of the MagDistGuiBean object
     *
     * @param  it  The new paramsInIteratorVisible value
     */
    private void setParamsInIteratorVisible( ListIterator it ) {

        while ( it.hasNext() ) {
            String name = ( ( ParameterAPI ) it.next() ).getName();
            setParameterInvisible( name, true );
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
    public void getChoosenFunction()
             throws ConstraintException {


        // Starting
        String S = C + ": getChoosenFunction():";
        if ( D )
            System.out.println( S + "Starting" );


        String distributionName=parameterList.getParameter(DISTRIBUTION_NAME).getValue().toString();


        IncrementalMagFreqDist magDist = null;
        try{
            Double min = (Double)parameterList.getParameter(MIN).getValue();
            Double max = (Double)parameterList.getParameter(MAX).getValue();
            Integer num = (Integer)parameterList.getParameter(NUM).getValue();

        if(min.doubleValue() > max.doubleValue()) {
           throw new java.lang.RuntimeException("Min Value cannot be less than the Max Value");
        }


        /*
         * If Single MagDist is selected
         */
        if(distributionName.equalsIgnoreCase(SingleMagFreqDist.NAME)) {
            if(D) System.out.println(S+" selected distribution is SINGLE");
            SingleMagFreqDist single =new SingleMagFreqDist(min.doubleValue(),
                                            max.doubleValue(),num.intValue());
            String paramToSet=parameterList.getParameter(PARAMS_TO_SET).getValue().toString();
           // if rate and mag are set
           if(paramToSet.equalsIgnoreCase(RATE_AND_MAG)) {
              if(D) System.out.println(S+" Rate and mag is selected in SINGLE");
              Double rate = (Double)parameterList.getParameter(RATE).getValue();
              Double mag = (Double)parameterList.getParameter(MAG).getValue();
              single.setMagAndRate(mag.doubleValue(),rate.doubleValue());
              if(mag.doubleValue()>max.doubleValue() || mag.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of Mag must lie between the min and max value");
              }
              if(D) System.out.println(S+" after setting SINGLE DIST");
           }
           // if mag and moment rate are set
           if(paramToSet.equalsIgnoreCase(MAG_AND_MORATE)) {
              Double mag = (Double)parameterList.getParameter(MAG).getValue();
              Double moRate = (Double)parameterList.getParameter(MO_RATE).getValue();
              single.setMagAndMomentRate(mag.doubleValue(),moRate.doubleValue());
              if(mag.doubleValue()>max.doubleValue() || mag.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of Mag must lie between the min and max value");
              }

           }
           // if rate and moment  rate are set
           if(paramToSet.equalsIgnoreCase(RATE_AND_MORATE)) {
               Double rate = (Double)parameterList.getParameter(RATE).getValue();
               Double moRate = (Double)parameterList.getParameter(MO_RATE).getValue();
               single.setRateAndMomentRate(rate.doubleValue(),moRate.doubleValue());
           }
          magDist =  (IncrementalMagFreqDist) single;
        }


         /*
         * If Gaussian MagDist is selected
         */
       if(distributionName.equalsIgnoreCase(GaussianMagFreqDist.NAME)) {
              Double mean = (Double)parameterList.getParameter(MEAN).getValue();
              Double stdDev = (Double)parameterList.getParameter(STD_DEV).getValue();
              Double totMoRate = (Double)parameterList.getParameter(GAUSSIAN_TO_MORATE).getValue();
              String truncTypeValue = parameterList.getParameter(TRUNCATION_REQ).getValue().toString();
              if(mean.doubleValue()>max.doubleValue() || mean.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of Mean must lie between the min and max value");
              }
              int truncType = 0;
              if(truncTypeValue.equalsIgnoreCase(TRUNCATE_FROM_RIGHT))
                 truncType = 1;
              else if(truncTypeValue.equalsIgnoreCase(TRUNCATE_ON_BOTH_SIDES))
                 truncType = 2;
              GaussianMagFreqDist gaussian;
              if(truncType !=0){
                 Double truncLevel = (Double)parameterList.getParameter(TRUNCATE_NUM_OF_STD_DEV).getValue();
              if(truncLevel.doubleValue()<0){
                 throw new java.lang.RuntimeException("Value of "+ TRUNCATE_NUM_OF_STD_DEV+" must be  positive");
              }

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
         * If Gutenberg Richter MagDist is selected
         */
       if(distributionName.equalsIgnoreCase(GutenbergRichterMagFreqDist.NAME)) {
           GutenbergRichterMagFreqDist gR =
                    new GutenbergRichterMagFreqDist(min.doubleValue(),max.doubleValue(),
                         num.intValue());

           Double magLower = (Double)parameterList.getParameter(GR_MAG_LOWER).getValue();
           Double bValue = (Double)parameterList.getParameter(GR_BVALUE).getValue();
           String setAllParamsBut = parameterList.getParameter(SET_ALL_PARAMS_BUT).getValue().toString();
           if(magLower.doubleValue()>max.doubleValue() || magLower.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagLower must lie between the min and max value");
           }
           // if set all parameters except total moment rate
           if(setAllParamsBut.equalsIgnoreCase(GR_TO_MORATE)) {
              Double magUpper =  (Double)parameterList.getParameter(GR_MAG_UPPER).getValue();
              Double toCumRate = (Double)parameterList.getParameter(TO_CUM_RATE).getValue();
              gR.setAllButTotMoRate(magLower.doubleValue(),magUpper.doubleValue(),
                                    toCumRate.doubleValue(), bValue.doubleValue());
              if(magUpper.doubleValue()>max.doubleValue() || magUpper.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagUpper must lie between the min and max value");
              }
              if(magLower.doubleValue()>magUpper.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagLower must be less than or equal to MagUpper");
              }
           }
           // if set all parameters except total cumulative rate
           if(setAllParamsBut.equalsIgnoreCase(TO_CUM_RATE)) {
             Double magUpper =  (Double)parameterList.getParameter(GR_MAG_UPPER).getValue();
             Double toMoRate = (Double)parameterList.getParameter(GR_TO_MORATE).getValue();
             gR.setAllButTotCumRate(magLower.doubleValue(),magUpper.doubleValue(),
                                    toMoRate.doubleValue(), bValue.doubleValue());
             if(magUpper.doubleValue()>max.doubleValue() || magUpper.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagUpper must lie between the min and max value");
             }
             if(magLower.doubleValue()>magUpper.doubleValue()){
               throw new java.lang.RuntimeException("Value of MagLower must be less than or equal to MagUpper");
             }
           }
           // if set all parameters except mag upper
           if(setAllParamsBut.equalsIgnoreCase(GR_MAG_UPPER)) {
             Double toCumRate = (Double)parameterList.getParameter(TO_CUM_RATE).getValue();
             Double toMoRate = (Double)parameterList.getParameter(GR_TO_MORATE).getValue();
             String  fix = parameterList.getParameter(FIX).getValue().toString();
             boolean relaxCumRate = false;
             if(fix.equalsIgnoreCase(FIX_TO_MORATE))
                relaxCumRate = true;
             gR.setAllButMagUpper(magLower.doubleValue(),toMoRate.doubleValue(),
                                  toCumRate.doubleValue(),bValue.doubleValue(),
                                  relaxCumRate);
           }
          magDist =  (IncrementalMagFreqDist) gR;
        }


        /*
         * If Young and Coppersmith 1985 MagDist is selected
         */
       if(distributionName.equalsIgnoreCase(YC_1985_CharMagFreqDist.NAME)) {

           Double magLower = (Double)parameterList.getParameter(YC_MAG_LOWER).getValue();
           Double magUpper = (Double)parameterList.getParameter(YC_MAG_UPPER).getValue();
           Double deltaMagChar = (Double)parameterList.getParameter(YC_DELTA_MAG_CHAR).getValue();
           Double magPrime = (Double)parameterList.getParameter(YC_MAG_PRIME).getValue();
           Double deltaMagPrime = (Double)parameterList.getParameter(YC_DELTA_MAG_PRIME).getValue();
           Double bValue = (Double)parameterList.getParameter(YC_BVALUE).getValue();
           Double toMoRate = (Double)parameterList.getParameter(YC_TO_MORATE).getValue();
           // check that maglowe r value lies betwenn min and max
           if(magLower.doubleValue()>max.doubleValue() || magLower.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagLower must lie between the min and max value");
           }
           // check that magUpper value lies between min and max
           if(magUpper.doubleValue()>max.doubleValue() || magUpper.doubleValue()<min.doubleValue()){
               throw new java.lang.RuntimeException("Value of MagUpper must lie between the min and max value");
           }

           /* double min,int num,double delta, double magLower,
                              double magUpper, double deltaMagChar, double magPrime,
                              double deltaMagPrime, double bValue, double totMoRate*/
           YC_1985_CharMagFreqDist yc =
                  new YC_1985_CharMagFreqDist(min.doubleValue(),max.doubleValue(),
                         num.intValue());
           yc.setAll(magLower.doubleValue(), magUpper.doubleValue(),
                     deltaMagChar.doubleValue(), magPrime.doubleValue(),
                     deltaMagPrime.doubleValue(), bValue.doubleValue(),
                     toMoRate.doubleValue());
           magDist =  (IncrementalMagFreqDist) yc;
        }


       }catch(java.lang.NumberFormatException e){
           throw new NumberFormatException("Value entered must be a valid Numerical Value");
       }
        catch(java.lang.NullPointerException e){
          throw new NullPointerException("Enter All values");
       }
     if(D) System.out.println(S+" before calling setValue in magDistParam");
     this.magDistParam.setValue(magDist);
     if(D) System.out.println(S+" after calling setValue in magDistParam");
  }

   /**
     * VERY IMPORTANT setup function. This is where all the parameter editors
     * are dynamcally created based by parameter getType() function. It uses
     * the ParameterEditorFactory to create the editors. THe search path is
     * set for the factory, each ParameterEditor is created, and then added
     * as a JPanel ( base class of all Editors ) to this list GUI scrolling list.
     */
    protected void addParameters() {

        if ( parameterList == null )
            return;

        ListIterator it = parameterList.getParameterNamesIterator();
        int counter = 0;
        //boolean first = true;

        // Set additional search paths for non-standard editors
        if ( ( searchPaths != null ) || ( searchPaths.length > 0 ) )
            ParameterEditorFactory.setSearchPaths( this.searchPaths );

        parameterEditors.clear();
        while ( it.hasNext() ) {

            Object obj1 = it.next();
            String name = ( String ) obj1;

            ParameterAPI param = parameterList.getParameter( name );

            // if(obj instanceof ParameterAPI){
            //ParameterAPI param = (ParameterAPI)obj;
            ParameterEditor panel = ParameterEditorFactory.getEditor( param );

            parameterEditors.put( param.getName(), panel );

            editorPanel.add( panel, new GridBagConstraints( 0, counter, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
            counter++;
            //}
        }
        JButton button = new JButton("Update MagDist");
        editorPanel.add( button, new GridBagConstraints( 0, counter, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        button.addActionListener(this);

    }


  public String getMagDistName() {
    return parameterList.getParameter(DISTRIBUTION_NAME).getValue().toString();
  }
  public double getMin(){
    return ((Double)parameterList.getParameter(MIN).getValue()).doubleValue();
  }

  public double getMax(){
    return ((Double)parameterList.getParameter(MAX).getValue()).doubleValue();
  }

  public int getNum(){
    return ((Integer)parameterList.getParameter(NUM).getValue()).intValue();
  }

}

