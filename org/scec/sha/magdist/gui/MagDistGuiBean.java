package org.scec.sha.magdist.gui;

import java.util.HashMap;
import java.lang.reflect.*;

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
    *  Just a placeholder name for this particular IMR Gui bean.
    */
    protected String name;

    /**
     *  Search path for finding editors in non-default packages.
     */
    final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";

    MagFreqDistTesterApplet applet = null;


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
    public MagDistGuiBean( String className, String name, MagFreqDistTesterApplet applet ) {

        // Starting
        String S = C + ": Constructor(): ";
        if ( D ) System.out.println( S + "Starting:" );
        this.name = name;
        this.applet = applet;

        // Create MagFreqDist class dynamically from string name
        if ( className == null || className.equals( "" ) )
            throw new ParameterException( S + "MagFreqDist Class name cannot be empty or null" );
        magDistClassName=className;
        // Create the control parameters for this Distribution
        initControlsParamListAndEditor( applet );

        // Create independent parameters
        initIndependentParamListAndEditor( applet );

        // Update which parameters should be invisible
        synchRequiredVisibleParameters();

        // All done
        if ( D ) System.out.println( S + "Ending:" );
    }



    /**
     *  <b> FIX *** FIX *** FIX </b> This needs to be fixed along with the whole
     *  function package. Right now only Doubles can be plotted on x-axis as
     *  seen by DiscretizedFunction2DAPI.<P>
     *
     * @param  applet  Description of the Parameter
     */
    protected void initControlsParamListAndEditor( MagFreqDistTesterApplet applet ) {

        // Starting
        String S = C + ": initControlsParamListAndEditor(): ";
        if ( D ) System.out.println( S + "Starting:" );

        if ( magDistClassName == null || magDistClassName.trim().equalsIgnoreCase("") )
            throw new ParameterException( S + "Distribution is null, unable to continue." );
        if ( applet == null )
            throw new ParameterException( S + "Applet is null, unable to continue." );

        // make the min, delta and num Parameter
        DoubleParameter minParamter = new DoubleParameter("Min:");
        DoubleParameter deltaParamter = new DoubleParameter("Delta");
        DoubleParameter numParamter = new DoubleParameter("Num");



        // Now make the parameters list
        // At this point all values have been set for the IM type, xaxis, and the yaxis
        controlsParamList = new ParameterList();
        controlsParamList.addParameter( minParamter );
        controlsParamList.addParameter( deltaParamter );
        controlsParamList.addParameter( numParamter );

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
    private void initIndependentParamListAndEditor( MagFreqDistTesterApplet applet )
             throws ParameterException {

        // Starting
        String S = C + ": initIndependentParamEditor(): ";
        if ( D )
            System.out.println( S + "Starting:" );
        if (  magDistClassName == null || magDistClassName.trim().equalsIgnoreCase("") )
            throw new ParameterException( S + "MagDist is null, unable to init independent parameters." );

       // Initialize the parameter list
        independentParams = new ParameterList();

        DoubleParameter temp = new DoubleParameter("Temp:");
        independentParams.addParameter(temp);

         DoubleParameter temp1 = new DoubleParameter("Temp1:");
        independentParams.addParameter(temp1);
         DoubleParameter temp2 = new DoubleParameter("Temp2:");
        independentParams.addParameter(temp2);

        String[] searchPaths = new String[2];
        searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
        searchPaths[1] = SPECIAL_EDITORS_PACKAGE;

         // Build editor list
        independentsEditor = new ParameterListEditor( independentParams, this, applet, searchPaths );
        independentsEditor.setTitle( "Independent Variables" );

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
        if ( this.controlsParamList.containsParameter( name1 ) ) {
            if ( D )
                System.out.println( S + "Control Parameter changed, need to update gui parameter editors" );
            synchRequiredVisibleParameters();
        }
    }

}