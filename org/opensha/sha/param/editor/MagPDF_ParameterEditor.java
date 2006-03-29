package org.opensha.sha.param.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.JOptionPane;
import java.lang.RuntimeException;


import org.opensha.param.editor.*;
import org.opensha.param.*;
import org.opensha.exceptions.*;
import org.opensha.param.event.*;
import org.opensha.sha.param.*;
import org.opensha.sha.magdist.*;


/**
 *  <b>Title:</b> MagPDF_ParameterEditor<p>
 *
 *  b>Description:</b> This is a MagFreqDistParameter editor. All parameters listed
 * in the constraint of the MagFreqDistParameter are listed as choices, and below
 * are shown the associated independent parameters than need to be filled in to
 * make the desired distribution.<p>
 *
 * @author     Nitin & Vipin Gupta, and Ned Field
 * @created    Oct 18, 2002
 * @version    1.0
 */

public class MagPDF_ParameterEditor extends ParameterEditor
    implements ParameterChangeListener,
    ParameterChangeFailListener,
    ActionListener {

    /** Class name for debugging. */
    protected final static String C = "MagPDF_ParameterEditor";
    /** If true print out debug statements. */
    protected final static boolean D = false;
    private Insets defaultInsets = new Insets( 4, 4, 4, 4 );

    private MagPDF_Parameter magPDF_Param;

    //Checks if the magDist Params have been changed
    private boolean magPDF_ParamsChange = true;
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
    public static final String MAG_DIST_TITLE = new String("Mag PDF Params");

    //Instance for the framee to show the all parameters in this editor
    protected JDialog frame;


    //keeps track if it has got the magdist parameter list and added listeners to it
    //Also keeps track if we have already got the parameters options for the magdist parameters
    private boolean addedListenersToParameters ;


    /**
     * Constructor
     */
    public MagPDF_ParameterEditor()  {

    }

    public MagPDF_ParameterEditor(ParameterAPI model){
      super(model);
      setParameter(model);
  }

    /**
     *
     */
    public void setParameter(ParameterAPI param)  {

        String S = C + ": Constructor(): ";
        if ( D ) System.out.println( S + "Starting:" );
        setParameterInEditor(param);
        valueEditor = new JButton("Set "+param.getName());
        ((JButton)valueEditor).addActionListener(this);
        add(valueEditor,  new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
            , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        // remove the previous editor
        //removeAll();
        magPDF_Param = (MagPDF_Parameter) param;

        // make the params editor
        initParamList();
        editor = new ParameterListEditor(parameterList);
        editor.setTitle(MAG_DIST_TITLE);

        // Update which parameters should be invisible
        synchRequiredVisibleParameters();
        // All done
        if ( D ) System.out.println( S + "Ending:" );
    }


    /**
     * This function is called when the user click for the ParameterListParameterEditor Button
     *
     * @param ae
     */
    public void actionPerformed(ActionEvent ae ) {

        frame = new JDialog();
        frame.setModal(true);
        frame.setSize(300,400);
        frame.setTitle("Set "+magPDF_Param.getName());
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(editor,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));

        //Adding Button to update the forecast
        JButton button = new JButton();
        button.setText("Update "+magPDF_Param.getName());
        button.setForeground(new Color(80,80,133));
        button.setBackground(new Color(200,200,230));
        button.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            button_actionPerformed(e);
          }
        });
        frame.getContentPane().add(button,new GridBagConstraints(0, 2, 1, 1, 0.0,0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        frame.setVisible(true);
        frame.pack();
  }

    /**
     * Checks whether you want to show the Mag Freq Dist Param Editor as button or a panel
     * This function mostly come in handy if instead of displaying this parameter
     * as the button user wants to show it as the Parameterlist in the panel.
     * @param visible : If it it true, button is visible else not visible
     * By default it is visible
     */
    public void setMagFreqDistParamButtonVisible(boolean visible) {
      valueEditor.setVisible(visible);
    }

    /**
     * Function that returns the magFreDist Param as a parameterListeditor
     * so that user can display it as the panel in window rather then
     * button.
     * @return
     */
    public ParameterListEditor getMagFreDistParameterEditor(){
      return editor;
    }

    /**
     * Main GUI Initialization point. This block of code is updated by JBuilder
     * when using it's GUI Editor.
     */
    protected void jbInit() throws Exception {
      //super.jbInit();
      // Main component
      this.setLayout( new GridBagLayout());
    }

    /**
     * This function when update Mag dist is called
     *
     * @param ae
     */
    public void button_actionPerformed(ActionEvent e) {
      try{
        setMagDistFromParams();
        frame.dispose();
      }catch(RuntimeException ee){
        JOptionPane.showMessageDialog(this,ee.getMessage(),"Incorrect Values",JOptionPane.ERROR_MESSAGE);
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
    protected void initParamList()  {

        // Starting
        String S = C + ": initControlsParamListAndEditor(): ";
        if ( D ) System.out.println( S + "Starting:" );

        /**
         * get adjustable params from MagFreqDistParam and add listeners to them
         */
        parameterList = magPDF_Param.getAdjustableParams();
        //do it if not done already ( allows the person to just do it once)
        if(!addedListenersToParameters){
          ListIterator it  = parameterList.getParametersIterator();
          while(it.hasNext()){
            ParameterAPI param = (ParameterAPI)it.next();
            param.addParameterChangeFailListener(this);
            param.addParameterChangeListener(this);
          }
          addedListenersToParameters = true;
        }
  }




    /**
     *  Description of the Method
     *
     * @exception  ParameterException  Description of the Exception
     */
    protected void synchRequiredVisibleParameters() throws ParameterException {

      String S = C + ":synchRequiredVisibleParameters:";

        String distributionName=parameterList.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).getValue().toString();

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
           setSingleDistParamsVisible();
        }

        /**
         *  If Gutenberg Richter Freq dist is selected
         */
        if(distributionName.equalsIgnoreCase(GutenbergRichterMagFreqDist.NAME)) {
         setGR_DistParamsVisible();
        }

        /**
         * If Gaussian Freq dist is selected
         */
       if(distributionName.equalsIgnoreCase(GaussianMagFreqDist.NAME)) {
         this.setGaussianDistParamsVisible();
       }

       /**
        * If YC Freq dist is selected
        */
      if(distributionName.equalsIgnoreCase(YC_1985_CharMagFreqDist.NAME)) {
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
      editor.setParameterVisible(MagFreqDistParameter.MAG, true);
    }

    /**
     * make the parameters related to GAUSSIAN Mag dist visible
     */
    private void setGaussianDistParamsVisible() {
      // set all the parameters visible
      editor.setParameterVisible(MagFreqDistParameter.MEAN, true);
      editor.setParameterVisible(MagFreqDistParameter.STD_DEV, true);
      editor.setParameterVisible(MagFreqDistParameter.TRUNCATION_REQ, true);

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

      editor.setParameterVisible(MagFreqDistParameter.GR_MAG_LOWER, true);
      editor.setParameterVisible(MagFreqDistParameter.GR_BVALUE, true);
      editor.setParameterVisible(MagFreqDistParameter.GR_MAG_UPPER,true);
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

        magPDF_ParamsChange = true;

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
      if(magPDF_ParamsChange) {
        magPDF_Param.setMagDist(this.parameterList);
        magPDF_ParamsChange=false;
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

