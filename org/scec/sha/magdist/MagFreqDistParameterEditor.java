package org.scec.sha.magdist;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.border.*;
import java.lang.RuntimeException;


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
 * @author     Nitin & Vipin Gupta, and Ned Field
 * @created    Oct 18, 2002
 * @version    1.0
 */

public class MagFreqDistParameterEditor extends ParameterEditor
    implements ParameterChangeListener, ActionListener {

    /** Class name for debugging. */
    protected final static String C = "MagFreqDistParameterEditor";
    /** If true print out debug statements. */
    protected final static boolean D = false;
    private Insets defaultInsets = new Insets( 4, 4, 4, 4 );



    public final static String DISTRIBUTION_NAME="Choose Distribution";


   /**
    *  Just a placeholder name for this particular MagDist Gui bean.
    */
    protected String name;

    private MagFreqDistParameter magDistParam;
    /**
     *  Search path for finding editors in non-default packages.
     */
    private String[] searchPaths;
    final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";


    /**
     * Paramter List for holding all parameters
     */
    ParameterList parameterList;

    /**
     * ParameterListEditor for holding parameters
     */
    ParameterListEditor editor;

    /**
     * Params string value
     */
     public static final String MAG_DIST_TITLE = new String("Mag Dist Params");
     public static final String PARAMS_TO_SET=new String("Params to Set");
     public static final String SET_ALL_PARAMS_BUT=new String("Set All Params BUT");
     public static final String MIN=new String("Min");
     public static final String MIN_INFO=new String("Minimum magnitude of distribution");
     public static final String MAX=new String("Max");
     public static final String MAX_INFO=new String("Maximum magnitude of distribution");
     public static final String NUM=new String("Num");
     public static final String NUM_INFO=new String("Number of points in distribution");

     /**
      * strings for the units of all rate and moment-rate parameters
      */
     public static final String RATE_UNITS=new String("/yr");
     public static final String MO_RATE_UNITS=new String("Nm/yr");

     /**
      * info string for all b-value parameters
      */
     public static final String BVALUE_INFO=new String("b in ralationship: log(rate) = a-b*magnitude");

   /**
    * Single Magnitude Frequency Distribution Parameter string list  constant
    */
     public static final String RATE_AND_MAG =new String("Rate & Mag");
     public static final String MAG_AND_MORATE =new String("Mag & Moment Rate");
     public static final String RATE_AND_MORATE=new String("Rate & Moment Rate");
     public static final String RATE=new String("Rate");
     public static final String MAG=new String("Mag");
     public static final String MO_RATE=new String("Moment Rate");


  /**
   * GutenbergRichter Magnitude Frequency Distribution Parameter string list constant
   */
    public static final String GR_TO_MORATE=new String("GR Total Moment Rate");
    public static final String TO_CUM_RATE=new String("Total Cum Rate");
    public static final String GR_MAG_UPPER=new String("Mag Upper");
    public static final String GR_MAG_UPPER_INFO=new String("Magnitude of the last non-zero rate");
    public static final String GR_MAG_LOWER=new String("Mag Lower");
    public static final String GR_MAG_LOWER_INFO=new String("Magnitude of the first non-zero rate");
    public static final String GR_BVALUE=new String("b Value");
    public static final String FIX=new String("fix");
    public static final String FIX_INFO=new String("Only one of these can be matched exactly");
    public static final String FIX_TO_MORATE=new String("Fix Total Moment Rate");
    public static final String FIX_TO_CUM_RATE=new String("Fix Total CUM Rate");


    /**
     * Young and Coppersmith, 1985 Char
     */
    public static final String YC_MAG_UPPER=new String("Mag Upper");
    public static final String YC_MAG_UPPER_INFO=new String("The last magnitude with non-zero rate");
    public static final String YC_MAG_LOWER=new String("Mag Lower");
    public static final String YC_MAG_LOWER_INFO=new String("The first magnitude with non-zero rate");
    public static final String YC_BVALUE=new String("b Value");
    public static final String YC_DELTA_MAG_CHAR = new String("Delta Mag Char");
    public static final String YC_DELTA_MAG_CHAR_INFO = new String("Width of the characteristic part (below Mag Upper)");
    public static final String YC_MAG_PRIME = new String("Mag Prime");
    public static final String YC_MAG_PRIME_INFO = new String("Last magnitude of the GR part");
    public static final String YC_DELTA_MAG_PRIME = new String("Delta Mag Prime");
    public static final String YC_DELTA_MAG_PRIME_INFO = new String("Distance below Mag Prime where rate on GR equals that on the char. part");
    public static final String YC_TO_MORATE=new String("YC Total Moment Rate");


  /**
   * Gaussian Magnitude Frequency Distribution Parameter string list constant
   */
    public static final String GAUSSIAN_TO_MORATE=new String("Total Moment Rate");
    public static final String MEAN=new String("Mean");
    public static final String STD_DEV=new String("Std Dev");
    public static final String TRUNCATION_REQ=new String("Truncation Type");
    public static final String TRUNCATE_FROM_RIGHT= new String("Upper");
    public static final String TRUNCATE_ON_BOTH_SIDES= new String("Upper and Lower");
    public static final String TRUNCATE_NUM_OF_STD_DEV= new String("Truncation Level(# of Std Devs)");
    public static final String NONE= new String("None");

    /**
     *  This is name of various classes
     */
    protected final static String GaussianMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.GaussianMagFreqDist";
    protected final static String GutenbergRichterMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.GutenbergRichterMagFreqDist";
    protected final static String YC_1985_CharMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.YC_1985_CharMagFreqDist";
    protected final static String SingleMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.SingleMagFreqDist";
    protected final static String SummedMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.SummedMagFreqDist";


    JButton button = new JButton("Update MagDist");

    public MagFreqDistParameterEditor()  {
      button.addActionListener(this);
    }

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
    public void setParameter(ParameterAPI param)  {
        //super();
       // Starting
        String S = C + ": Constructor(): ";
        if ( D ) System.out.println( S + "Starting:" );
        // remove the previous editor
        this.removeAll();
        this.magDistParam = (MagFreqDistParameter) param;

        // make the params editor
        initParamListAndEditor();
        editor = new ParameterListEditor(parameterList,searchPaths);
        editor.setTitle(MAG_DIST_TITLE);
        this.add(editor,  new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
              , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        button.setForeground(new Color(80,80,133));
        button.setBackground(new Color(200,200,230));
        Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
        button.setBorder(border);
        this.add(button,  new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
                      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

        // Update which parameters should be invisible
        synchRequiredVisibleParameters();
        // All done
        if ( D ) System.out.println( S + "Ending:" );
    }

    /**
     * whether you want the update button to be visible or not
     *
     * @param visible : If it it true, update button is visible else not visible
     *   By default it is visible
     */
    public void setUpdateButtonVisible(boolean visible) {
      button.setVisible(visible);
    }

    /**
     * Main GUI Initialization point. This block of code is updated by JBuilder
     * when using it's GUI Editor.
     */
    protected void jbInit() throws Exception {

        // Main component

        this.setLayout( new GridBagLayout());


        // Build package names search path
       searchPaths = new String[3];
       searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
       searchPaths[1] = SPECIAL_EDITORS_PACKAGE;
       searchPaths[2] = "org.scec.sha.magdist" ;
    }

    /**
     * This function when update Mag dist is called
     *
     * @param ae
     */
    public void actionPerformed(ActionEvent ae ) {
      try{
        this.getChoosenFunction();
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,e.getMessage(),"Incorrect Values",JOptionPane.ERROR_MESSAGE);
      }
    }

    /**
     * Called when the parameter has changed independently from
     * the editor, such as with the ParameterWarningListener.
     * This function needs to be called to to update
     * the GUI component ( text field, picklsit, etc. ) with
     * the new parameter value.
     */
    public void synchToModel() {
      this.editor.synchToModel();
    }


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
        minParameter.setInfo(MIN_INFO);
        DoubleParameter maxParameter = new DoubleParameter(MAX,new Double(10));
        maxParameter.setInfo(MAX_INFO);
        IntegerParameter numParameter = new IntegerParameter(NUM, (int) 0, Integer.MAX_VALUE, new Integer(101));
        numParameter.setInfo(NUM_INFO);

        // Add these to the parameters list
         parameterList.addParameter( minParameter );
         parameterList.addParameter( numParameter );
         parameterList.addParameter( maxParameter );

         // add Parameters for single Mag freq dist
         DoubleParameter rate=new DoubleParameter(RATE, 0, Double.POSITIVE_INFINITY, RATE_UNITS);
         DoubleParameter mag = new DoubleParameter(MAG);
         DoubleParameter moRate=new DoubleParameter(MO_RATE, 0, Double.POSITIVE_INFINITY, MO_RATE_UNITS);
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
         DoubleParameter stdDev = new DoubleParameter(STD_DEV, 0, Double.POSITIVE_INFINITY);
         DoubleParameter toMoRate = new DoubleParameter(GAUSSIAN_TO_MORATE, 0, Double.POSITIVE_INFINITY, MO_RATE_UNITS);
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
         magLower.setInfo(GR_MAG_LOWER_INFO);
         DoubleParameter magUpper = new DoubleParameter(GR_MAG_UPPER);
         magUpper.setInfo(GR_MAG_UPPER_INFO);
         DoubleParameter bValue = new DoubleParameter(GR_BVALUE,0, Double.POSITIVE_INFINITY);
         bValue.setInfo(BVALUE_INFO);
         DoubleParameter toCumRate = new DoubleParameter(TO_CUM_RATE, 0, Double.POSITIVE_INFINITY, RATE_UNITS);
         DoubleParameter gr_toMoRate = new DoubleParameter(GR_TO_MORATE, 0, Double.POSITIVE_INFINITY, MO_RATE_UNITS);
         vStrings=new Vector();
         vStrings.add(GR_TO_MORATE);
         vStrings.add(TO_CUM_RATE);
         vStrings.add(GR_MAG_UPPER);
         StringParameter setParamsBut=new StringParameter(SET_ALL_PARAMS_BUT,vStrings,GR_TO_MORATE);
         parameterList.addParameter(setParamsBut);
         setParamsBut.addParameterChangeListener(this);
         parameterList.addParameter(gr_toMoRate);
         parameterList.addParameter(magLower);
         parameterList.addParameter(magUpper);
         parameterList.addParameter(bValue);
         parameterList.addParameter(toCumRate);
         //controlsParamList.addParameter(gr_toMoRate);
         Vector vStrings1 = new Vector ();
         vStrings1.add(FIX_TO_CUM_RATE);
         vStrings1.add(FIX_TO_MORATE);
         StringParameter fix = new StringParameter(FIX,vStrings1,FIX_TO_CUM_RATE);
         fix.setInfo(FIX_INFO);
         parameterList.addParameter(fix);
         fix.addParameterChangeListener(this);



         /**
          * Add paramters for Youngs and Coppersmith 1985 char distribution
          */
         DoubleParameter yc_magLower = new DoubleParameter(YC_MAG_LOWER);
         yc_magLower.setInfo(YC_MAG_LOWER_INFO);
         DoubleParameter yc_magUpper = new DoubleParameter(YC_MAG_UPPER);
         yc_magUpper.setInfo(YC_MAG_UPPER_INFO);
         DoubleParameter deltaMagChar = new DoubleParameter(YC_DELTA_MAG_CHAR, 0, Double.POSITIVE_INFINITY);
         deltaMagChar.setInfo(YC_DELTA_MAG_CHAR_INFO);
         DoubleParameter magPrime = new DoubleParameter(YC_MAG_PRIME);
         magPrime.setInfo(YC_MAG_PRIME_INFO);
         DoubleParameter deltaMagPrime = new DoubleParameter(YC_DELTA_MAG_PRIME, 0, Double.POSITIVE_INFINITY);
         deltaMagPrime.setInfo(YC_DELTA_MAG_PRIME_INFO);
         DoubleParameter yc_bValue = new DoubleParameter(YC_BVALUE, 0, Double.POSITIVE_INFINITY);
         yc_bValue.setInfo(BVALUE_INFO);
         DoubleParameter yc_toMoRate = new DoubleParameter(YC_TO_MORATE, 0, Double.POSITIVE_INFINITY, MO_RATE_UNITS);
         //parameterList.addParameter(yc_magLower);
         //parameterList.addParameter(yc_magUpper);
         parameterList.addParameter(deltaMagChar);
         parameterList.addParameter(magPrime);
         parameterList.addParameter(deltaMagPrime);

         //parameterList.addParameter(yc_bValue);
         parameterList.addParameter(yc_toMoRate);

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
          editor.setParameterInvisible( ( ( ParameterAPI ) it.next() ).getName(), false);

        // make the min, max, num and select dist to be visible
        editor.setParameterInvisible(this.MIN,true);
        editor.setParameterInvisible(this.MAX,true);
        editor.setParameterInvisible(this.NUM,true);
        editor.setParameterInvisible(this.DISTRIBUTION_NAME,true);

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

      editor.setParameterInvisible(PARAMS_TO_SET, true);
      String paramToSet=parameterList.getParameter(PARAMS_TO_SET).getValue().toString();
      // if Rate and Mag is selected
      if(paramToSet.equalsIgnoreCase(RATE_AND_MAG)) {
        editor.setParameterInvisible(RATE, true);
        editor.setParameterInvisible(MAG, true);
        editor.setParameterInvisible(MO_RATE, false);
      }
      // if Mag and Mo Rate is selected
      if(paramToSet.equalsIgnoreCase(MAG_AND_MORATE)) {
        editor.setParameterInvisible(RATE, false);
        editor.setParameterInvisible(MAG, true);
        editor.setParameterInvisible(MO_RATE, true);
      }
      // if Rate and Mo Rate is selected
      if(paramToSet.equalsIgnoreCase(RATE_AND_MORATE)) {
        editor.setParameterInvisible(RATE, true);
        editor.setParameterInvisible(MAG, false);
        editor.setParameterInvisible(MO_RATE, true);
      }
  }

    /**
     * make the parameters related to GAUSSIAN Mag dist visible
     */
    private void setGaussianDistParamsVisible() {
      // set all the parameters visible
      editor.setParameterInvisible(MEAN, true);
      editor.setParameterInvisible(STD_DEV, true);
      editor.setParameterInvisible(GAUSSIAN_TO_MORATE, true);
      editor.setParameterInvisible(TRUNCATION_REQ, true);

      String truncReq=parameterList.getParameter(TRUNCATION_REQ).getValue().toString();

      // make the truncation level visible only if truncation req is NOT NONE
      if(truncReq.equalsIgnoreCase(NONE))
      editor.setParameterInvisible(TRUNCATE_NUM_OF_STD_DEV,false);
      else
        editor.setParameterInvisible(TRUNCATE_NUM_OF_STD_DEV,true);

    }

    /**
     * make the parameters related to Gutenberg Richter Mag dist visible
     */
    private void setGR_DistParamsVisible() {

      editor.setParameterInvisible(SET_ALL_PARAMS_BUT, true);
      editor.setParameterInvisible(this.GR_MAG_LOWER, true);
      editor.setParameterInvisible(this.GR_BVALUE, true);

      // now make the params visible/invisible based on params desired to be set by user
      String paramToSet=parameterList.getParameter(SET_ALL_PARAMS_BUT).getValue().toString();

      // set all paramerts except total Mo rate
      if(paramToSet.equalsIgnoreCase(GR_TO_MORATE)) {
        editor.setParameterInvisible(TO_CUM_RATE,true);
        editor.setParameterInvisible(GR_MAG_UPPER,true);
        editor.setParameterInvisible(GR_TO_MORATE,false);
        editor.setParameterInvisible(FIX,false);
      }

      // set all parameters except cumulative rate
      if(paramToSet.equalsIgnoreCase(TO_CUM_RATE)) {
        editor.setParameterInvisible(GR_MAG_UPPER,true);
        editor.setParameterInvisible(GR_TO_MORATE,true);
        editor. setParameterInvisible(TO_CUM_RATE,false);
        editor.setParameterInvisible(FIX,false);
      }

      // set all parameters except mag upper
      if(paramToSet.equalsIgnoreCase(GR_MAG_UPPER)) {
        editor.setParameterInvisible(TO_CUM_RATE,true);
        editor.setParameterInvisible(GR_TO_MORATE,true);
        editor.setParameterInvisible(FIX,true);
        editor.setParameterInvisible(GR_MAG_UPPER,false);
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
      editor.setParameterInvisible(YC_MAG_LOWER, true);
      editor.setParameterInvisible(YC_MAG_UPPER, true);
      editor.setParameterInvisible(YC_DELTA_MAG_CHAR, true);
      editor.setParameterInvisible(YC_MAG_PRIME, true);
      editor.setParameterInvisible(YC_DELTA_MAG_PRIME, true);
      editor.setParameterInvisible(YC_BVALUE, true);
      editor.setParameterInvisible(YC_TO_MORATE, true);
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
     * Sets the paramsInIteratorVisible attribute of the MagDistGuiBean object
     *
     * @param  it  The new paramsInIteratorVisible value
     */
    private void setParamsInIteratorVisible( ListIterator it ) {

        while ( it.hasNext() ) {
            String name = ( ( ParameterAPI ) it.next() ).getName();
            editor.setParameterInvisible( name, true );
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
              if(mag.doubleValue()>max.doubleValue() || mag.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of Mag must lie between the min and max value");
              }
              single.setMagAndRate(mag.doubleValue(),rate.doubleValue());
              if(D) System.out.println(S+" after setting SINGLE DIST");
           }
           // if mag and moment rate are set
           if(paramToSet.equalsIgnoreCase(MAG_AND_MORATE)) {
              Double mag = (Double)parameterList.getParameter(MAG).getValue();
              Double moRate = (Double)parameterList.getParameter(MO_RATE).getValue();
              if(mag.doubleValue()>max.doubleValue() || mag.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of Mag must lie between the min and max value");
              }
              single.setMagAndMomentRate(mag.doubleValue(),moRate.doubleValue());
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

              if(magUpper.doubleValue()>max.doubleValue() || magUpper.doubleValue()<min.doubleValue()){
               throw new java.lang.RuntimeException("Value of MagUpper must lie between the min and max value");
              }
              if(magLower.doubleValue()>magUpper.doubleValue()){
               throw new java.lang.RuntimeException("Value of MagLower must be <= to MagUpper");
              }

              gR.setAllButTotMoRate(magLower.doubleValue(),magUpper.doubleValue(),
                                    toCumRate.doubleValue(), bValue.doubleValue());
           }
           // if set all parameters except total cumulative rate
           if(setAllParamsBut.equalsIgnoreCase(TO_CUM_RATE)) {
             Double magUpper =  (Double)parameterList.getParameter(GR_MAG_UPPER).getValue();
             Double toMoRate = (Double)parameterList.getParameter(GR_TO_MORATE).getValue();
             if(magUpper.doubleValue()>max.doubleValue() || magUpper.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagUpper must lie between the min and max value");
             }
             if(magLower.doubleValue()>magUpper.doubleValue()){
               throw new java.lang.RuntimeException("Value of MagLower must be <= to MagUpper");
             }
             gR.setAllButTotCumRate(magLower.doubleValue(),magUpper.doubleValue(),
                                    toMoRate.doubleValue(), bValue.doubleValue());

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
           yc.setAllButTotCharRate(magLower.doubleValue(), magUpper.doubleValue(),
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
   * returns the MagDistName
   * @return
   */
  public String getMagDistName() {
    return parameterList.getParameter(DISTRIBUTION_NAME).getValue().toString();
  }

  /**
   * returns the Min of the magnitude for the distribution
   * @return
   */
  public double getMin(){
    return ((Double)parameterList.getParameter(MIN).getValue()).doubleValue();
  }

  /**
   * returns the Max of the magnitude for thr distribution
   * @return
   */
  public double getMax(){
    return ((Double)parameterList.getParameter(MAX).getValue()).doubleValue();
  }

  /**
   * returns the Number of magnitudes for the Distribution
   * @return
   */
  public int getNum(){
    return ((Integer)parameterList.getParameter(NUM).getValue()).intValue();
  }

  /**
   * returns the ParamterList for the MagfreqDistParameter
   * @return
   */
  public ParameterList getParamterList() {
    return parameterList;
  }

  /** Returns each parameter for the MagFreqDist */
  public ParameterAPI getParameter(String name) throws ParameterException {
    return parameterList.getParameter(name);
  }

}

