package org.scec.sha.param.editor;

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
import org.scec.sha.param.*;
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

    private MagFreqDistParameter magDistParam;

    //Checks if the magDist Params have been changed
    private boolean magDistParamsChange = true;
    // name of the is mag Freq dist
    private String name;
    /**
     * Paramter List for holding all parameters
     */
    private ParameterList parameterList;

    /**
     * ParameterListEditor for holding parameters
     */
    private ParameterListEditor editor;

     // title of Parameter List Editor
     public static final String MAG_DIST_TITLE = new String("Mag Dist Params");
    private JButton button = new JButton("Update MagDist");
    // String Constraints
    private StringConstraint sdFixOptions,  grSetAllButOptions, grFixOptions,
        ycSetAllButOptions, gdSetAllButOptions;



    /**
     * Constructor
     */
    public MagFreqDistParameterEditor()  {
      button.addActionListener(this);
    }

    public MagFreqDistParameterEditor(ParameterAPI model){
      super(model);
      this.setParameter(model);
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
        editor = new ParameterListEditor(parameterList);
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
    public void refreshParamEditor() {
      editor.refreshParamEditor();
    }


    /**
     *
     */
    protected void initParamListAndEditor()  {

        // Starting
        String S = C + ": initControlsParamListAndEditor(): ";
        if ( D ) System.out.println( S + "Starting:" );

        /**
         * get adjustable params from MagFreqDistParam and add listeners to them
         */
        parameterList = magDistParam.getAdjustableParams();
        ListIterator it  = parameterList.getParametersIterator();
        while(it.hasNext()){
          ParameterAPI param = (ParameterAPI)it.next();
          param.addParameterChangeFailListener(this);
          param.addParameterChangeListener(this);
        }
        // String Constraints
        sdFixOptions = magDistParam.getSingleDistFixOptions();
        grSetAllButOptions = magDistParam.getGRSetAllButOptions();
        grFixOptions = magDistParam.getGRFixOptions();
        ycSetAllButOptions = magDistParam.getYCSetAllButOptions();
        gdSetAllButOptions = magDistParam.getGaussianDistSetAllButOptions();

  }




    /**
     *  Description of the Method
     *
     * @exception  ParameterException  Description of the Exception
     */
    protected void synchRequiredVisibleParameters() throws ParameterException {

      String S = C + ":synchRequiredVisibleParameters:";

        String distributionName=parameterList.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).getValue().toString();
        StringParameter setAllButParam =
                             ( StringParameter)parameterList.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT);
        // Create the new (temporary) parameters
        StringParameter param2, param3;


        // Turn off all parameters - start fresh, then make visible as required below
        ListIterator it = parameterList.getParametersIterator();
        while ( it.hasNext() )
          editor.setParameterVisible( ( ( ParameterAPI ) it.next() ).getName(), false);

        // make the min, max, num and select dist to be visible
        editor.setParameterVisible(MagFreqDistParameter.MIN,true);
        editor.setParameterVisible(MagFreqDistParameter.MAX,true);
        editor.setParameterVisible(MagFreqDistParameter.NUM,true);
        editor.setParameterVisible(MagFreqDistParameter.DISTRIBUTION_NAME,true);

        /**
         * if Single Mag Freq dist is selected
         * depending on the parameter chosen from the SINGLE_PARAMS_TO_SET, it redraws
         * all the independent parameters again and draws the text boxes to fill
         * in the values.
         */
        if(distributionName.equalsIgnoreCase(SingleMagFreqDist.NAME)) {
          // change the fixParameter constraints
          param3 = new StringParameter(MagFreqDistParameter.FIX, sdFixOptions,
                             (String)sdFixOptions.getAllowedStrings().get(0));

          param3.addParameterChangeListener(this);
          param3.setInfo(MagFreqDistParameter.FIX_INFO);
          editor.replaceParameterForEditor( MagFreqDistParameter.FIX, param3 );

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
         param3 = new StringParameter(MagFreqDistParameter.FIX,grFixOptions,
                                      (String)grFixOptions.getAllowedStrings().get(0));
         param3.addParameterChangeListener(this);
         param3.setInfo(MagFreqDistParameter.FIX_INFO);
         editor.replaceParameterForEditor( MagFreqDistParameter.FIX, param3 );


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

      editor.setParameterVisible(MagFreqDistParameter.SINGLE_PARAMS_TO_SET, true);
      String paramToSet=parameterList.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).getValue().toString();
      // if Rate and Mag is selected
      if(paramToSet.equalsIgnoreCase(MagFreqDistParameter.RATE_AND_MAG)) {
        editor.setParameterVisible(MagFreqDistParameter.RATE, true);
        editor.setParameterVisible(MagFreqDistParameter.MAG, true);
        editor.setParameterVisible(MagFreqDistParameter.MO_RATE, false);
        editor.setParameterVisible(MagFreqDistParameter.FIX,false);
      }
      // if Mag and Mo Rate is selected
      if(paramToSet.equalsIgnoreCase(MagFreqDistParameter.MAG_AND_MO_RATE)) {
        editor.setParameterVisible(MagFreqDistParameter.RATE, false);
        editor.setParameterVisible(MagFreqDistParameter.MAG, true);
        editor.setParameterVisible(MagFreqDistParameter.MO_RATE, true);
        editor.setParameterVisible(MagFreqDistParameter.FIX,false);
      }
      // if Rate and Mo Rate is selected
      if(paramToSet.equalsIgnoreCase(MagFreqDistParameter.RATE_AND_MO_RATE)) {
        editor.setParameterVisible(MagFreqDistParameter.RATE, true);
        editor.setParameterVisible(MagFreqDistParameter.MAG, false);
        editor.setParameterVisible(MagFreqDistParameter.MO_RATE, true);
        editor.setParameterVisible(MagFreqDistParameter.FIX,true);
      }
  }

    /**
     * make the parameters related to GAUSSIAN Mag dist visible
     */
    private void setGaussianDistParamsVisible() {
      // set all the parameters visible
      editor.setParameterVisible(MagFreqDistParameter.MEAN, true);
      editor.setParameterVisible(MagFreqDistParameter.STD_DEV, true);
      editor.setParameterVisible(MagFreqDistParameter.TRUNCATION_REQ, true);
      editor.setParameterVisible(MagFreqDistParameter.SET_ALL_PARAMS_BUT, true);

      // now make the params visible/invisible based on params desired to be set by user
      StringParameter param = (StringParameter)parameterList.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT);
      String paramToSet = param.getValue().toString();

      // set all paramerts except total Mo rate
      if(paramToSet.equalsIgnoreCase(MagFreqDistParameter.TOT_MO_RATE)) {
        editor.setParameterVisible(MagFreqDistParameter.TOT_CUM_RATE,true);
        editor.setParameterVisible(MagFreqDistParameter.TOT_MO_RATE,false);
      }
      else {
        editor.setParameterVisible(MagFreqDistParameter.TOT_CUM_RATE,false);
        editor.setParameterVisible(MagFreqDistParameter.TOT_MO_RATE,true);
      }

      String truncReq=parameterList.getParameter(MagFreqDistParameter.TRUNCATION_REQ).getValue().toString();

      // make the truncation level visible only if truncation req is NOT NONE
      if(truncReq.equalsIgnoreCase(MagFreqDistParameter.NONE))
      editor.setParameterVisible(MagFreqDistParameter.TRUNCATE_NUM_OF_STD_DEV,false);
      else
        editor.setParameterVisible(MagFreqDistParameter.TRUNCATE_NUM_OF_STD_DEV,true);

    }

    /**
     * make the parameters related to Gutenberg Richter Mag dist visible
     */
    private void setGR_DistParamsVisible() {

      editor.setParameterVisible(MagFreqDistParameter.SET_ALL_PARAMS_BUT, true);
      editor.setParameterVisible(MagFreqDistParameter.GR_MAG_LOWER, true);
      editor.setParameterVisible(MagFreqDistParameter.GR_BVALUE, true);

      // now make the params visible/invisible based on params desired to be set by user
      StringParameter param = (StringParameter)parameterList.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT);
      String paramToSet = param.getValue().toString();

      // set all paramerts except total Mo rate
      if(paramToSet.equalsIgnoreCase(MagFreqDistParameter.TOT_MO_RATE)) {
        editor.setParameterVisible(MagFreqDistParameter.TOT_CUM_RATE,true);
        editor.setParameterVisible(MagFreqDistParameter.GR_MAG_UPPER,true);
        editor.setParameterVisible(MagFreqDistParameter.TOT_MO_RATE,false);
        editor.setParameterVisible(MagFreqDistParameter.FIX,false);
      }

      // set all parameters except cumulative rate
      if(paramToSet.equalsIgnoreCase(MagFreqDistParameter.TOT_CUM_RATE)) {
        editor.setParameterVisible(MagFreqDistParameter.GR_MAG_UPPER,true);
        editor.setParameterVisible(MagFreqDistParameter.TOT_MO_RATE,true);
        editor. setParameterVisible(MagFreqDistParameter.TOT_CUM_RATE,false);
        editor.setParameterVisible(MagFreqDistParameter.FIX,false);
      }

      // set all parameters except mag upper
      if(paramToSet.equalsIgnoreCase(MagFreqDistParameter.GR_MAG_UPPER)) {
        editor.setParameterVisible(MagFreqDistParameter.TOT_CUM_RATE,true);
        editor.setParameterVisible(MagFreqDistParameter.TOT_MO_RATE,true);
        editor.setParameterVisible(MagFreqDistParameter.FIX,true);
        editor.setParameterVisible(MagFreqDistParameter.GR_MAG_UPPER,false);
      }
    }

    /**
     * make the parameters related to Youngs and Coppersmith Mag dist visible
     */
    private void setYC_DistParamsVisible() {
      editor.setParameterVisible(MagFreqDistParameter.GR_MAG_LOWER, true);
      editor.setParameterVisible(MagFreqDistParameter.GR_MAG_UPPER, true);
      editor.setParameterVisible(MagFreqDistParameter.YC_DELTA_MAG_CHAR, true);
      editor.setParameterVisible(MagFreqDistParameter.YC_MAG_PRIME, true);
      editor.setParameterVisible(MagFreqDistParameter.YC_DELTA_MAG_PRIME, true);
      editor.setParameterVisible(MagFreqDistParameter.GR_BVALUE, true);
      editor.setParameterVisible(MagFreqDistParameter.SET_ALL_PARAMS_BUT, true);


      // now make the params visible/invisible based on params desired to be set by user
      StringParameter param = (StringParameter)parameterList.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT);
      String paramToSet = param.getValue().toString();

      // set all paramerts except total Mo rate
      if(paramToSet.equalsIgnoreCase(MagFreqDistParameter.TOT_MO_RATE)) {
        editor.setParameterVisible(MagFreqDistParameter.YC_TOT_CHAR_RATE,true);
        editor.setParameterVisible(MagFreqDistParameter.TOT_MO_RATE,false);
      }
      else {
        editor.setParameterVisible(MagFreqDistParameter.YC_TOT_CHAR_RATE,false);
        editor.setParameterVisible(MagFreqDistParameter.TOT_MO_RATE,true);
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

        if(name1.equalsIgnoreCase(MagFreqDistParameter.DISTRIBUTION_NAME)) {
            try { // if selectde distribution changes
              synchRequiredVisibleParameters();
            }catch (Exception e) {
              System.out.println(this.C+" "+e.toString());
              e.printStackTrace();
            }
        }
        else { // if only parameters within a distribution change
          String distributionName=parameterList.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).getValue().toString();
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
      String S = C + ": setMagDistFromParams():";
      if ( D ) System.out.println( S + "Starting" );
      if(magDistParamsChange) {
        magDistParam.setMagDist(this.parameterList);
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
       return parameterList.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).getValue().toString();
     }

     /**
      * returns the Min of the magnitude for the distribution
      * @return
      */
     public double getMin(){
       return ((Double)parameterList.getParameter(MagFreqDistParameter.MIN).getValue()).doubleValue();
     }

     /**
      * returns the Max of the magnitude for thr distribution
      * @return
      */
     public double getMax(){
       return ((Double)parameterList.getParameter(MagFreqDistParameter.MAX).getValue()).doubleValue();
     }

     /**
      * returns the Number of magnitudes for the Distribution
      * @return
      */
     public int getNum(){
       return ((Integer)parameterList.getParameter(MagFreqDistParameter.NUM).getValue()).intValue();
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

  /** returns the parameterlist */
  public ParameterList getParameterList() {
    return this.parameterList;
  }

}

