package org.scec.sha.magdist.gui;

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
import org.scec.sha.magdist.parameter.*;
import org.scec.sha.magdist.*;


/**
 *  <b>Title:</b> MagFreqDistParameterEditor<p>
 *
 *  b>Description:</b> This is a MagFreqDistParameter editor. All parameters listed
 * in the constraint of the MagFreqDistParameter are listed as choices, and below
 * are shown the associated independent parameters than need to be filled in to
 * make the dedired distribution.<p>
 *
 * @author     Nitin & Vipin Gupta, and Ned Field
 * @created    Oct 18, 2002
 * @version    1.0
 */

public class MagFreqDistParameterEditor extends ParameterEditor
    implements ParameterChangeListener,
    ParameterChangeFailListener,
    ActionListener {

    /** Class name for debugging. */
    protected final static String C = "MagFreqDistParameterEditor";
    /** If true print out debug statements. */
    protected final static boolean D = false;
    private Insets defaultInsets = new Insets( 4, 4, 4, 4 );


    /**
     * the string for the distribution choice parameter
     */
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

    //Checks if the magDist Params have been changed
    private boolean magDistParamsChange = true;

    /**
     * Paramter List for holding all parameters
     */
    ParameterList parameterList;

    /**
     * ParameterListEditor for holding parameters
     */
    ParameterListEditor editor;

     // title of Parameter List Editor
     public static final String MAG_DIST_TITLE = new String("Mag Dist Params");

     /**
      * Name and Info strings of params needed by all distributions
      */
     public static final String MIN=new String("Min");
     public static final String MIN_INFO=new String("Minimum magnitude of the discetized function");
     public static final String MAX=new String("Max");
     public static final String MAX_INFO=new String("Maximum magnitude of the discetized function");
     public static final String NUM=new String("Num");
     public static final String NUM_INFO=new String("Number of points in  the discetized function");

     /**
      * Name, units, and info strings for parameters needed by more than one distribution (shared)
      */
     // Moment rate stuff:
     public static final String TOT_MO_RATE = new String("Total Moment Rate");
     public static final String MO_RATE_UNITS=new String("Nm/yr");
     // Total cumulative rate stuff:
     public static final String TOT_CUM_RATE=new String("Total Cumulative Rate");
     public static final String RATE_UNITS=new String("/yr");
     // Gutenberg-Richter dist stuff (used by Y&C dist as well):
     public static final String GR_MAG_UPPER=new String("Mag Upper");
     public static final String GR_MAG_UPPER_INFO=new String("Magnitude of the last non-zero rate");
     public static final String GR_MAG_LOWER=new String("Mag Lower");
     public static final String GR_MAG_LOWER_INFO=new String("Magnitude of the first non-zero rate");
     public static final String GR_BVALUE=new String("b Value");
     public static final String BVALUE_INFO=new String("b in: log(rate) = a-b*magnitude");
     // Set all params but
     public final static String SET_ALL_PARAMS_BUT=new String("Set All Params But");
     // The fix (constrain) options
     public static final String FIX=new String("Constrain");
     public static final String FIX_INFO=new String("Only one of these can be matched exactly due to mag discretization");
     public static final String FIX_TOT_MO_RATE=new String("Total Moment Rate");
     public static final String FIX_TO_CUM_RATE=new String("Total Cum. Rate");
     public static final String FIX_RATE=new String("Rate");



   /**
    * Single Magnitude Frequency Distribution Parameter names
    */
     public static final String RATE=new String("Rate");
     public static final String MAG=new String("Mag");
     public static final String MO_RATE=new String("Moment Rate");
     public static final String SINGLE_PARAMS_TO_SET=new String("Single Dist. Params");
     public static final String RATE_AND_MAG =new String("Rate & Mag");
     public static final String MAG_AND_MO_RATE =new String("Mag & Moment Rate");
     public static final String RATE_AND_MO_RATE=new String("Rate & Moment Rate");
     StringConstraint sdFixOptions;

  /**
   * Gutenberg-Richter Magnitude Frequency Distribution Parameter names
   * (most are common and therefor defined above)
   */
    StringConstraint grSetAllButOptions;
    StringConstraint grFixOptions;

    /**
     * Young and Coppersmith, 1985 Char dist. parameter names
     */
    public static final String YC_DELTA_MAG_CHAR = new String("Delta Mag Char");
    public static final String YC_DELTA_MAG_CHAR_INFO = new String("Width of the characteristic part (below Mag Upper)");
    public static final String YC_MAG_PRIME = new String("Mag Prime");
    public static final String YC_MAG_PRIME_INFO = new String("Last magnitude of the GR part");
    public static final String YC_DELTA_MAG_PRIME = new String("Delta Mag Prime");
    public static final String YC_DELTA_MAG_PRIME_INFO = new String("Distance below Mag Prime where rate on GR equals that on the char. part");
    public static final String YC_TOT_CHAR_RATE = new String("Total Char. Rate");
    public static final String YC_TOT_CHAR_RATE_INFO = new String("Total rate of events above (magUpper-deltaMagChar)");
    StringConstraint ycSetAllButOptions;

   /**
    * Gaussian Magnitude Frequency Distribution Parameter string list constant
    */
    public static final String MEAN=new String("Mean");
    public static final String STD_DEV=new String("Std Dev");
    public static final String TRUNCATION_REQ=new String("Truncation Type");
    public static final String TRUNCATE_UPPER_ONLY= new String("Upper");
    public static final String TRUNCATE_ON_BOTH_SIDES= new String("Upper and Lower");
    public static final String TRUNCATE_NUM_OF_STD_DEV= new String("Truncation Level(# of Std Devs)");
    public static final String NONE= new String("None");
    StringConstraint gdSetAllButOptions;

    JButton button = new JButton("Update MagDist");


    /**
     * Constructor
     */
    public MagFreqDistParameterEditor()  {
      button.addActionListener(this);
    }


    /**
     *
     */
    public void setParameter(ParameterAPI param)  {

        String S = C + ": Constructor(): ";
        if ( D ) System.out.println( S + "Starting:" );
        // remove the previous editor
        removeAll();
        magDistParam = (MagFreqDistParameter) param;

        // make the params editor
        initParamListAndEditor();
        editor = new ParameterListEditor(parameterList,searchPaths);
        editor.setTitle(MAG_DIST_TITLE);
        add(editor,  new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
              , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        button.setForeground(new Color(80,80,133));
        button.setBackground(new Color(200,200,230));
        //Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
        //button.setBorder(border);
        add(button,  new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
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
       searchPaths = new String[4];
       searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
       searchPaths[1] = SPECIAL_EDITORS_PACKAGE;
       searchPaths[2] = "org.scec.sha.magdist.gui" ;
       searchPaths[3] = "org.scec.sha.fault.parameter.gui" ;
    }

    /**
     * This function when update Mag dist is called
     *
     * @param ae
     */
    public void actionPerformed(ActionEvent ae ) {
      try{
        setMagDistFromParams();
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,e.getMessage(),"Incorrect Values",JOptionPane.ERROR_MESSAGE);
      }
    }

    /**
     * Called when the parameter has changed independently from
     * the editor, such as with the ParameterWarningListener.
     * This function needs to be called to to update
     * the GUI component ( text field, picklist, etc. ) with
     * the new parameter value.
     */
    public void synchToModel() {
      editor.synchToModel();
    }


    /**
     *
     */
    protected void initParamListAndEditor()  {

        // Starting
        String S = C + ": initControlsParamListAndEditor(): ";
        if ( D ) System.out.println( S + "Starting:" );

        /**
         * Make the parameter that lists the choice of distributions
         */
        parameterList = new ParameterList();
        StringParameter distributionName =new StringParameter(DISTRIBUTION_NAME,
            magDistParam.getAllowedMagDists(),
            (String) magDistParam.getAllowedMagDists().get(0));
        distributionName.addParameterChangeListener(this);

        // make the min, delta and num Parameters
        DoubleParameter minParameter = new DoubleParameter(MIN,new Double(0));
        minParameter.setInfo(MIN_INFO);
        minParameter.addParameterChangeListener(this);
        DoubleParameter maxParameter = new DoubleParameter(MAX,new Double(10));
        maxParameter.setInfo(MAX_INFO);
        maxParameter.addParameterChangeListener(this);
        IntegerParameter numParameter = new IntegerParameter(NUM, (int) 0, Integer.MAX_VALUE, new Integer(101));
        numParameter.addParameterChangeFailListener(this);
        numParameter.addParameterChangeListener(this);
        numParameter.setInfo(NUM_INFO);

         // Make the other common parameters (used by more than one distribution)
         DoubleParameter totMoRate=new DoubleParameter(TOT_MO_RATE, 0, Double.POSITIVE_INFINITY,
                                                       MO_RATE_UNITS, new Double(1e19));
         totMoRate.addParameterChangeFailListener(this);
         totMoRate.addParameterChangeListener(this);
         DoubleParameter magLower = new DoubleParameter(GR_MAG_LOWER, new Double(5));
         magLower.setInfo(GR_MAG_LOWER_INFO);
         magLower.addParameterChangeListener(this);
         DoubleParameter magUpper = new DoubleParameter(GR_MAG_UPPER, new Double(8));
         magUpper.addParameterChangeListener(this);
         magUpper.setInfo(GR_MAG_UPPER_INFO);
         DoubleParameter bValue = new DoubleParameter(GR_BVALUE,0, Double.POSITIVE_INFINITY, new Double(1));
         bValue.addParameterChangeFailListener(this);
         bValue.setInfo(BVALUE_INFO);
         bValue.addParameterChangeListener(this);
         DoubleParameter totCumRate = new DoubleParameter(TOT_CUM_RATE, 0, Double.POSITIVE_INFINITY,
                                                          RATE_UNITS, new Double(3.33));
         totCumRate.addParameterChangeFailListener(this);
         totCumRate.addParameterChangeListener(this);


         // add Parameters for single Mag freq dist
         DoubleParameter rate=new DoubleParameter(RATE, 0, Double.POSITIVE_INFINITY, RATE_UNITS, new Double(0.005));
         rate.addParameterChangeFailListener(this);

         rate.addParameterChangeListener(this);
         DoubleParameter moRate=new DoubleParameter(MO_RATE, 0, Double.POSITIVE_INFINITY, MO_RATE_UNITS, new Double(1e19));
         moRate.addParameterChangeFailListener(this);
         moRate.addParameterChangeListener(this);
         DoubleParameter mag = new DoubleParameter(MAG, new Double(8));
         mag.addParameterChangeListener(this);
         Vector vStrings=new Vector();
         vStrings.add(RATE_AND_MAG);
         vStrings.add(MAG_AND_MO_RATE);
         vStrings.add(RATE_AND_MO_RATE);
         StringParameter singleParamsToSet=new StringParameter(SINGLE_PARAMS_TO_SET,
                                           vStrings,(String)vStrings.get(0));
         singleParamsToSet.addParameterChangeListener(this);
         Vector vStrings3 = new Vector ();
         vStrings3.add(FIX_RATE);
         vStrings3.add(FIX_TOT_MO_RATE);
         sdFixOptions = new StringConstraint(vStrings3);

         /**
          * Make parameters for Gaussian distribution
          */
         DoubleParameter mean = new DoubleParameter(MEAN, new Double(8));
         mean.addParameterChangeListener(this);
         DoubleParameter stdDev = new DoubleParameter(STD_DEV, 0, Double.POSITIVE_INFINITY, new Double(0.25));
         stdDev.addParameterChangeFailListener(this);
         stdDev.addParameterChangeListener(this);
         vStrings=new Vector();
         vStrings.add(TOT_CUM_RATE);
         vStrings.add(TOT_MO_RATE);
         gdSetAllButOptions = new StringConstraint(vStrings);
         vStrings=new Vector();
         vStrings.add(NONE);
         vStrings.add(TRUNCATE_UPPER_ONLY);
         vStrings.add(TRUNCATE_ON_BOTH_SIDES);
         StringParameter truncType=new StringParameter(TRUNCATION_REQ,vStrings,TRUNCATE_UPPER_ONLY);
         truncType.addParameterChangeListener(this);
         DoubleParameter truncLevel = new DoubleParameter(TRUNCATE_NUM_OF_STD_DEV, 0, Double.POSITIVE_INFINITY, new Double (1));
         truncLevel.addParameterChangeFailListener(this);
         truncLevel.addParameterChangeListener(this);

          /**
           * Make parameters for Gutenberg-Richter distribution
           */
         vStrings=new Vector();
         vStrings.add(TOT_MO_RATE);
         vStrings.add(TOT_CUM_RATE);
         vStrings.add(GR_MAG_UPPER);
         grSetAllButOptions = new StringConstraint(vStrings);
         Vector vStrings1 = new Vector ();
         vStrings1.add(FIX_TO_CUM_RATE);
         vStrings1.add(FIX_TOT_MO_RATE);
         grFixOptions = new StringConstraint(vStrings1);

         /**
          * Make paramters for Youngs and Coppersmith 1985 char distribution
          */
         DoubleParameter deltaMagChar = new DoubleParameter(YC_DELTA_MAG_CHAR, 0,
                                                            Double.POSITIVE_INFINITY, new Double(1));
         deltaMagChar.setInfo(YC_DELTA_MAG_CHAR_INFO);
         deltaMagChar.addParameterChangeFailListener(this);
         deltaMagChar.addParameterChangeListener(this);
         DoubleParameter magPrime = new DoubleParameter(YC_MAG_PRIME, new Double(7));
         magPrime.setInfo(YC_MAG_PRIME_INFO);
         magPrime.addParameterChangeListener(this);
         DoubleParameter deltaMagPrime = new DoubleParameter(YC_DELTA_MAG_PRIME, 0, Double.POSITIVE_INFINITY, new Double(1));
         deltaMagPrime.setInfo(YC_DELTA_MAG_PRIME_INFO);
         deltaMagPrime.addParameterChangeFailListener(this);
         deltaMagPrime.addParameterChangeListener(this);
         DoubleParameter totCharRate = new DoubleParameter(YC_TOT_CHAR_RATE, 0, Double.POSITIVE_INFINITY, new Double(0.01));
         totCharRate.setInfo(YC_TOT_CHAR_RATE_INFO);
         totCharRate.addParameterChangeFailListener(this);
         totCharRate.addParameterChangeListener(this);
         vStrings=new Vector();
         vStrings.add(YC_TOT_CHAR_RATE);
         vStrings.add(TOT_MO_RATE);
         ycSetAllButOptions = new StringConstraint(vStrings);

         // make the set all but paramter needed by YC, Gaussian and GR
         StringParameter setAllBut=new StringParameter(SET_ALL_PARAMS_BUT,
                                      ycSetAllButOptions,
                                      (String)ycSetAllButOptions.getAllowedStrings().get(0));
         setAllBut.addParameterChangeListener(this);

         // make the fix parameter needed by Single and GR dists
         StringParameter fixParam = new StringParameter(FIX,grFixOptions,FIX_TO_CUM_RATE);
         fixParam.setInfo(FIX_INFO);
         fixParam.addParameterChangeListener(this);

         // Add the parameters to the list (order is preserved)
         parameterList.addParameter(distributionName);
         parameterList.addParameter( minParameter );
         parameterList.addParameter( numParameter );
         parameterList.addParameter( maxParameter );
         // put ones that are always shown next
         parameterList.addParameter(magLower);
         parameterList.addParameter(magUpper);
         parameterList.addParameter(bValue);
         parameterList.addParameter(deltaMagChar);
         parameterList.addParameter(magPrime);
         parameterList.addParameter(deltaMagPrime);
         parameterList.addParameter(mean);
         parameterList.addParameter(stdDev);
         parameterList.addParameter(truncType);
         parameterList.addParameter(truncLevel);
         // now add the params that present choices
         parameterList.addParameter(setAllBut);
         parameterList.addParameter(singleParamsToSet);
         // now add params that depend on choices
         parameterList.addParameter(totCharRate);
         parameterList.addParameter(mag);
         parameterList.addParameter(rate);
         parameterList.addParameter(moRate);
         parameterList.addParameter(totMoRate);
         parameterList.addParameter(totCumRate);
         // now add params that present choice dependent on above choice
         parameterList.addParameter(fixParam);




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
        StringParameter setAllButParam =
                             ( StringParameter)parameterList.getParameter(SET_ALL_PARAMS_BUT);
        // Create the new (temporary) parameters
        StringParameter param2, param3;


        // Turn off all parameters - start fresh, then make visible as required below
        ListIterator it = parameterList.getParametersIterator();
        while ( it.hasNext() )
          editor.setParameterVisible( ( ( ParameterAPI ) it.next() ).getName(), false);

        // make the min, max, num and select dist to be visible
        editor.setParameterVisible(this.MIN,true);
        editor.setParameterVisible(this.MAX,true);
        editor.setParameterVisible(this.NUM,true);
        editor.setParameterVisible(this.DISTRIBUTION_NAME,true);

        /**
         * if Single Mag Freq dist is selected
         * depending on the parameter chosen from the SINGLE_PARAMS_TO_SET, it redraws
         * all the independent parameters again and draws the text boxes to fill
         * in the values.
         */
        if(distributionName.equalsIgnoreCase(SingleMagFreqDist.NAME)) {
          // change the fixParameter constraints
          param3 = new StringParameter(FIX,sdFixOptions,
                             (String)sdFixOptions.getAllowedStrings().get(0));
          param3.addParameterChangeListener(this);
          param3.setInfo(this.FIX_INFO);
          editor.replaceParameterForEditor( FIX, param3 );

           this.setSingleDistParamsVisible();
        }

        /**
         *  If Gutenberg Richter Freq dist is selected
         */
        if(distributionName.equalsIgnoreCase(GutenbergRichterMagFreqDist.NAME)) {

          // change the constraints in set all but parameter
         param2 = new StringParameter(setAllButParam.getName(),
                                      grSetAllButOptions,
                                     (String)grSetAllButOptions.getAllowedStrings().get(0));
         param2.addParameterChangeListener(this);
         // swap editors
         editor.replaceParameterForEditor( setAllButParam.getName(), param2 );

         // change the fixParameter constraints
         param3 = new StringParameter(FIX,grFixOptions,
                                      (String)grFixOptions.getAllowedStrings().get(0));
         param3.addParameterChangeListener(this);
         param3.setInfo(this.FIX_INFO);
         editor.replaceParameterForEditor( FIX, param3 );


         setGR_DistParamsVisible();
        }

        /**
         * If Gaussian Freq dist is selected
         */
       if(distributionName.equalsIgnoreCase(GaussianMagFreqDist.NAME)) {

         // change the constraints in set all but parameter
         param2 = new StringParameter(setAllButParam.getName(),
                                      gdSetAllButOptions,
                                      (String)gdSetAllButOptions.getAllowedStrings().get(0));
         param2.addParameterChangeListener(this);
         // swap editors
         editor.replaceParameterForEditor( setAllButParam.getName(), param2 );

         this.setGaussianDistParamsVisible();
       }

       /**
        * If YC Freq dist is selected
        */
      if(distributionName.equalsIgnoreCase(YC_1985_CharMagFreqDist.NAME)) {

        // change the constraints in set all but parameter
        param2 = new StringParameter(setAllButParam.getName(),
                                     ycSetAllButOptions,
                                     (String)ycSetAllButOptions.getAllowedStrings().get(0));
        param2.addParameterChangeListener(this);

        // swap editors
        editor.replaceParameterForEditor( setAllButParam.getName(), param2 );

        this.setYC_DistParamsVisible();
      }

      editor.validate();
      editor.repaint();

       // All done
        if ( D )
            System.out.println( S + "Ending: " );
    }

    /**
     * make the parameters related to SINGLE Mag dist visible
     */
    private void setSingleDistParamsVisible() {

      editor.setParameterVisible(SINGLE_PARAMS_TO_SET, true);
      String paramToSet=parameterList.getParameter(SINGLE_PARAMS_TO_SET).getValue().toString();
      // if Rate and Mag is selected
      if(paramToSet.equalsIgnoreCase(RATE_AND_MAG)) {
        editor.setParameterVisible(RATE, true);
        editor.setParameterVisible(MAG, true);
        editor.setParameterVisible(MO_RATE, false);
        editor.setParameterVisible(FIX,false);
      }
      // if Mag and Mo Rate is selected
      if(paramToSet.equalsIgnoreCase(MAG_AND_MO_RATE)) {
        editor.setParameterVisible(RATE, false);
        editor.setParameterVisible(MAG, true);
        editor.setParameterVisible(MO_RATE, true);
        editor.setParameterVisible(FIX,false);
      }
      // if Rate and Mo Rate is selected
      if(paramToSet.equalsIgnoreCase(RATE_AND_MO_RATE)) {
        editor.setParameterVisible(RATE, true);
        editor.setParameterVisible(MAG, false);
        editor.setParameterVisible(MO_RATE, true);
        editor.setParameterVisible(FIX,true);
      }
  }

    /**
     * make the parameters related to GAUSSIAN Mag dist visible
     */
    private void setGaussianDistParamsVisible() {
      // set all the parameters visible
      editor.setParameterVisible(MEAN, true);
      editor.setParameterVisible(STD_DEV, true);
      editor.setParameterVisible(TRUNCATION_REQ, true);
      editor.setParameterVisible(SET_ALL_PARAMS_BUT, true);

      // now make the params visible/invisible based on params desired to be set by user
      StringParameter param = (StringParameter)parameterList.getParameter(SET_ALL_PARAMS_BUT);
      String paramToSet = param.getValue().toString();

      // set all paramerts except total Mo rate
      if(paramToSet.equalsIgnoreCase(TOT_MO_RATE)) {
        editor.setParameterVisible(TOT_CUM_RATE,true);
        editor.setParameterVisible(TOT_MO_RATE,false);
      }
      else {
        editor.setParameterVisible(TOT_CUM_RATE,false);
        editor.setParameterVisible(TOT_MO_RATE,true);
      }

      String truncReq=parameterList.getParameter(TRUNCATION_REQ).getValue().toString();

      // make the truncation level visible only if truncation req is NOT NONE
      if(truncReq.equalsIgnoreCase(NONE))
      editor.setParameterVisible(TRUNCATE_NUM_OF_STD_DEV,false);
      else
        editor.setParameterVisible(TRUNCATE_NUM_OF_STD_DEV,true);

    }

    /**
     * make the parameters related to Gutenberg Richter Mag dist visible
     */
    private void setGR_DistParamsVisible() {

      editor.setParameterVisible(SET_ALL_PARAMS_BUT, true);
      editor.setParameterVisible(GR_MAG_LOWER, true);
      editor.setParameterVisible(GR_BVALUE, true);

      // now make the params visible/invisible based on params desired to be set by user
      StringParameter param = (StringParameter)parameterList.getParameter(SET_ALL_PARAMS_BUT);
      String paramToSet = param.getValue().toString();

      // set all paramerts except total Mo rate
      if(paramToSet.equalsIgnoreCase(TOT_MO_RATE)) {
        editor.setParameterVisible(TOT_CUM_RATE,true);
        editor.setParameterVisible(GR_MAG_UPPER,true);
        editor.setParameterVisible(TOT_MO_RATE,false);
        editor.setParameterVisible(FIX,false);
      }

      // set all parameters except cumulative rate
      if(paramToSet.equalsIgnoreCase(TOT_CUM_RATE)) {
        editor.setParameterVisible(GR_MAG_UPPER,true);
        editor.setParameterVisible(TOT_MO_RATE,true);
        editor. setParameterVisible(TOT_CUM_RATE,false);
        editor.setParameterVisible(FIX,false);
      }

      // set all parameters except mag upper
      if(paramToSet.equalsIgnoreCase(GR_MAG_UPPER)) {
        editor.setParameterVisible(TOT_CUM_RATE,true);
        editor.setParameterVisible(TOT_MO_RATE,true);
        editor.setParameterVisible(FIX,true);
        editor.setParameterVisible(GR_MAG_UPPER,false);
      }
    }

    /**
     * make the parameters related to Youngs and Coppersmith Mag dist visible
     */
    private void setYC_DistParamsVisible() {
      editor.setParameterVisible(GR_MAG_LOWER, true);
      editor.setParameterVisible(GR_MAG_UPPER, true);
      editor.setParameterVisible(YC_DELTA_MAG_CHAR, true);
      editor.setParameterVisible(YC_MAG_PRIME, true);
      editor.setParameterVisible(YC_DELTA_MAG_PRIME, true);
      editor.setParameterVisible(GR_BVALUE, true);
      editor.setParameterVisible(SET_ALL_PARAMS_BUT, true);


      // now make the params visible/invisible based on params desired to be set by user
      StringParameter param = (StringParameter)parameterList.getParameter(SET_ALL_PARAMS_BUT);
      String paramToSet = param.getValue().toString();

      // set all paramerts except total Mo rate
      if(paramToSet.equalsIgnoreCase(TOT_MO_RATE)) {
        editor.setParameterVisible(YC_TOT_CHAR_RATE,true);
        editor.setParameterVisible(TOT_MO_RATE,false);
      }
      else {
        editor.setParameterVisible(YC_TOT_CHAR_RATE,false);
        editor.setParameterVisible(TOT_MO_RATE,true);
      }

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
        else { // if only parameters within a distribution change
          String distributionName=parameterList.getParameter(DISTRIBUTION_NAME).getValue().toString();
          /** if Single Mag Freq dist is selected*/
          if(distributionName.equalsIgnoreCase(SingleMagFreqDist.NAME))
           setSingleDistParamsVisible();

        /** If Gutenberg Richter Freq dist is selected */
        if(distributionName.equalsIgnoreCase(GutenbergRichterMagFreqDist.NAME))
          setGR_DistParamsVisible();

        /**If Gaussian Freq dist is selected*/
       if(distributionName.equalsIgnoreCase(GaussianMagFreqDist.NAME))
          setGaussianDistParamsVisible();

       /**If YC Freq dist is selected*/
      if(distributionName.equalsIgnoreCase(YC_1985_CharMagFreqDist.NAME))
         setYC_DistParamsVisible();

        }

        magDistParamsChange = true;

    }





    /**
     * Sets the paramsInIteratorVisible attribute of the MagDistGuiBean object
     *
     * @param  it  The new paramsInIteratorVisible value
     */
    private void setParamsInIteratorVisible( ListIterator it ) {

        while ( it.hasNext() ) {
            String name = ( ( ParameterAPI ) it.next() ).getName();
            editor.setParameterVisible( name, true );
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
    public void setMagDistFromParams()
             throws ConstraintException {


      // Starting
      String S = C + ": getChoosenFunction():";
      if ( D )
        System.out.println( S + "Starting" );

      if(magDistParamsChange) {

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
            String paramToSet=parameterList.getParameter(SINGLE_PARAMS_TO_SET).getValue().toString();
            String fix=parameterList.getParameter(FIX).getValue().toString();

            // if rate and mag are set
            if(paramToSet.equalsIgnoreCase(RATE_AND_MAG)) {
              if(D) System.out.println(S+" Rate and mag is selected in SINGLE");
              Double rate = (Double)parameterList.getParameter(RATE).getValue();
              Double mag = (Double)parameterList.getParameter(MAG).getValue();
              if(mag.doubleValue()>max.doubleValue() || mag.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of Mag must lie between the min and max value");
              }
              try {
                single.setMagAndRate(mag.doubleValue(),rate.doubleValue());
              }catch(RuntimeException e){
                throw new java.lang.RuntimeException("The chosen magnitude must fall on one of the discrete x-axis values");
              }

              if(D) System.out.println(S+" after setting SINGLE DIST");
            }
            // if mag and moment rate are set
            if(paramToSet.equalsIgnoreCase(MAG_AND_MO_RATE)) {
              Double mag = (Double)parameterList.getParameter(MAG).getValue();
              Double moRate = (Double)parameterList.getParameter(MO_RATE).getValue();
              if(mag.doubleValue()>max.doubleValue() || mag.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of Mag must lie between the min and max value");
              }
              try {
                single.setMagAndMomentRate(mag.doubleValue(),moRate.doubleValue());
              }catch(RuntimeException e){
                throw new java.lang.RuntimeException("The chosen magnitude must fall on one of the discrete x-axis values");
              }
            }
            // if rate and moment rate are set
            if(paramToSet.equalsIgnoreCase(RATE_AND_MO_RATE)) {
              Double rate = (Double)parameterList.getParameter(RATE).getValue();
              Double moRate = (Double)parameterList.getParameter(MO_RATE).getValue();
              if(fix.equals(FIX_RATE))
                single.setRateAndMomentRate(rate.doubleValue(),moRate.doubleValue(),true);
              else
                single.setRateAndMomentRate(rate.doubleValue(),moRate.doubleValue(),false);
            }
            magDist =  (IncrementalMagFreqDist) single;
          }


         /*
          * If Gaussian MagDist is selected
         */
          if(distributionName.equalsIgnoreCase(GaussianMagFreqDist.NAME)) {
            Double mean = (Double)parameterList.getParameter(MEAN).getValue();
            Double stdDev = (Double)parameterList.getParameter(STD_DEV).getValue();
            String truncTypeValue = parameterList.getParameter(TRUNCATION_REQ).getValue().toString();
            if(mean.doubleValue()>max.doubleValue() || mean.doubleValue()<min.doubleValue()){
              throw new java.lang.RuntimeException("Value of Mean must lie between the min and max value");
            }
            int truncType = 0;
            if(truncTypeValue.equalsIgnoreCase(TRUNCATE_UPPER_ONLY))
              truncType = 1;
            else if(truncTypeValue.equalsIgnoreCase(TRUNCATE_ON_BOTH_SIDES))
              truncType = 2;

            GaussianMagFreqDist gaussian = new GaussianMagFreqDist(min.doubleValue(),max.doubleValue(),num.intValue());

            String setAllParamsBut = parameterList.getParameter(SET_ALL_PARAMS_BUT).getValue().toString();

            if(truncType !=0){
              Double truncLevel = (Double)parameterList.getParameter(TRUNCATE_NUM_OF_STD_DEV).getValue();
              if(truncLevel.doubleValue()<0)
                throw new java.lang.RuntimeException("Value of "+ TRUNCATE_NUM_OF_STD_DEV+" must be  positive");

              if(setAllParamsBut.equalsIgnoreCase(TOT_CUM_RATE)) {
                Double totMoRate = (Double)parameterList.getParameter(TOT_MO_RATE).getValue();
                gaussian.setAllButCumRate(mean.doubleValue(), stdDev.doubleValue(),
                    totMoRate.doubleValue(),truncLevel.doubleValue(), truncType);
              }
              else {
                Double totCumRate = (Double)parameterList.getParameter(TOT_CUM_RATE).getValue();
                gaussian.setAllButTotMoRate(mean.doubleValue(), stdDev.doubleValue(),
                    totCumRate.doubleValue(),truncLevel.doubleValue(), truncType);
              }
            }
            else {
              if(setAllParamsBut.equalsIgnoreCase(TOT_CUM_RATE)) {
                Double totMoRate = (Double)parameterList.getParameter(TOT_MO_RATE).getValue();
                gaussian.setAllButCumRate(mean.doubleValue(), stdDev.doubleValue(),
                    totMoRate.doubleValue());
              }
              else {
                Double totCumRate = (Double)parameterList.getParameter(TOT_CUM_RATE).getValue();
                gaussian.setAllButTotMoRate(mean.doubleValue(), stdDev.doubleValue(),
                    totCumRate.doubleValue());
              }
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
            if(setAllParamsBut.equalsIgnoreCase(TOT_MO_RATE)) {
              Double magUpper =  (Double)parameterList.getParameter(GR_MAG_UPPER).getValue();
              Double totCumRate = (Double)parameterList.getParameter(TOT_CUM_RATE).getValue();

              if(magUpper.doubleValue()>max.doubleValue() || magUpper.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagUpper must lie between the min and max value");
              }
              if(magLower.doubleValue()>magUpper.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagLower must be <= to MagUpper");
              }
              try {
                gR.setAllButTotMoRate(magLower.doubleValue(),magUpper.doubleValue(),
                                      totCumRate.doubleValue(), bValue.doubleValue());
              }catch(RuntimeException e){
                throw new java.lang.RuntimeException("magUpper and MagLower must fall on one of the discrete x-axis values");
              }

            }
            // if set all parameters except total cumulative rate
            if(setAllParamsBut.equalsIgnoreCase(TOT_CUM_RATE)) {
              Double magUpper =  (Double)parameterList.getParameter(GR_MAG_UPPER).getValue();
              Double toMoRate = (Double)parameterList.getParameter(TOT_MO_RATE).getValue();
              if(magUpper.doubleValue()>max.doubleValue() || magUpper.doubleValue()<min.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagUpper must lie between the min and max value");
              }
              if(magLower.doubleValue()>magUpper.doubleValue()){
                throw new java.lang.RuntimeException("Value of MagLower must be <= to MagUpper");
              }
              try {
                gR.setAllButTotCumRate(magLower.doubleValue(),magUpper.doubleValue(),
                                       toMoRate.doubleValue(), bValue.doubleValue());
              }catch(RuntimeException e){
                throw new java.lang.RuntimeException("magUpper and MagLower must fall on one of the discrete x-axis values");
              }

            }
            // if set all parameters except mag upper
            if(setAllParamsBut.equalsIgnoreCase(GR_MAG_UPPER)) {
              Double toCumRate = (Double)parameterList.getParameter(TOT_CUM_RATE).getValue();
              Double toMoRate = (Double)parameterList.getParameter(TOT_MO_RATE).getValue();
              String  fix = parameterList.getParameter(FIX).getValue().toString();
              boolean relaxTotMoRate = true;
              if(fix.equalsIgnoreCase(FIX_TOT_MO_RATE))
                relaxTotMoRate = false;
              try {
                gR.setAllButMagUpper(magLower.doubleValue(),toMoRate.doubleValue(),
                                     toCumRate.doubleValue(),bValue.doubleValue(),
                                     relaxTotMoRate);
              }catch(RuntimeException e){
                throw new java.lang.RuntimeException("MagLower must fall on one of the discrete x-axis values");
              }

            }
            magDist =  (IncrementalMagFreqDist) gR;
          }


        /*
          * If Young and Coppersmith 1985 MagDist is selected
         */
          if(distributionName.equalsIgnoreCase(YC_1985_CharMagFreqDist.NAME)) {

            double magLower = ((Double)parameterList.getParameter(GR_MAG_LOWER).getValue()).doubleValue();
            double magUpper = ((Double)parameterList.getParameter(GR_MAG_UPPER).getValue()).doubleValue();
            double deltaMagChar = ((Double)parameterList.getParameter(YC_DELTA_MAG_CHAR).getValue()).doubleValue();
            double magPrime = ((Double)parameterList.getParameter(YC_MAG_PRIME).getValue()).doubleValue();
            double deltaMagPrime = ((Double)parameterList.getParameter(YC_DELTA_MAG_PRIME).getValue()).doubleValue();
            double bValue = ((Double)parameterList.getParameter(GR_BVALUE).getValue()).doubleValue();


            // check that maglowe r value lies betwenn min and max
            if(magLower>max.doubleValue() || magLower<min.doubleValue()){
              throw new java.lang.RuntimeException("Value of MagLower must lie between the min and max value");
            }
            // check that magUpper value lies between min and max
            if(magUpper>max.doubleValue() || magUpper<min.doubleValue()){
              throw new java.lang.RuntimeException("Value of MagUpper must lie between the min and max value");
            }

            // creat the distribution
            YC_1985_CharMagFreqDist yc =
                new YC_1985_CharMagFreqDist(min.doubleValue(),max.doubleValue(), num.intValue());

            // Check that the mags fall on valid x increments:
            int trialInt;
            try  { trialInt = yc.getXIndex(magLower);
            } catch(RuntimeException e) {
              throw new java.lang.RuntimeException("MagLower must fall on one of the discrete x-axis values");
            }
            try { trialInt = yc.getXIndex(magUpper); }
            catch(RuntimeException e) {
              throw new java.lang.RuntimeException("MagUpper must fall on one of the discrete x-axis values");
            }
            try { trialInt = yc.getXIndex(magPrime); }
            catch(RuntimeException e) {
              throw new java.lang.RuntimeException("MagPrime must fall on one of the discrete x-axis values");
            }
            try { trialInt = yc.getXIndex(magPrime-deltaMagPrime); }
            catch(RuntimeException e) {
              throw new java.lang.RuntimeException("MagPrime-DeltaMagPrime must fall on one of the discrete x-axis values");
            }
            try {trialInt = yc.getXIndex(magUpper-deltaMagChar); }
            catch(RuntimeException e) {
              throw new java.lang.RuntimeException("MagUpper-DeltaMagChar must fall on one of the discrete x-axis values");
            }

            String setAllParamsBut = parameterList.getParameter(SET_ALL_PARAMS_BUT).getValue().toString();

            if(setAllParamsBut.equalsIgnoreCase(YC_TOT_CHAR_RATE)) {
              double totMoRate = ((Double) parameterList.getParameter(TOT_MO_RATE).getValue()).doubleValue();
              yc.setAllButTotCharRate(magLower, magUpper,
                                      deltaMagChar, magPrime,
                                      deltaMagPrime, bValue,
                                      totMoRate);
            }
            else {
              double totCharRate = ((Double)parameterList.getParameter(YC_TOT_CHAR_RATE).getValue()).doubleValue();
              yc.setAllButTotMoRate(magLower, magUpper,
                                    deltaMagChar, magPrime,
                                    deltaMagPrime, bValue,
                                    totCharRate);
            }

            magDist =  (IncrementalMagFreqDist) yc;
          }

        }catch(java.lang.NumberFormatException e){
          throw new NumberFormatException("Value entered must be a valid Numerical Value");
        }
        catch(java.lang.NullPointerException e){
          throw new NullPointerException("Enter All values");
        }
        if(D) System.out.println(S+" before calling setValue in magDistParam");
        magDistParam.setValue(magDist);
        if(D) System.out.println(S+" after calling setValue in magDistParam");
        magDistParamsChange=false;
      }

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


         b.append( "The value ");
         b.append( badValueStr );
         b.append( " is not permitted for '");
         b.append( name );
         b.append( "'.\n" );

         b.append( "Resetting to ");
         b.append( oldValueStr );
         b.append( ". The constraints are: \n");
         b.append( constraint.toString() );
         b.append( "\n" );

         JOptionPane.showMessageDialog(
             this, b.toString(),
             "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
             );

         if(D) System.out.println(S + "Ending");

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

