package org.scec.sha.imr.classicImpl.gui;


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

/**
 *  <b>Title:</b> IMRGuiBean<p>
 *
 *  <b>Description:</b> This class is a java bean container for all the Gui
 *  elements and controller elements for one particular IMR. This allows all the
 *  components to be packaged up in this one class and then for every IMR that
 *  is created there will be one instance of this bean. This allows these beans
 *  to be easily swapped in and out when you are examining different IMR's in
 *  the main tester applet application.<p>
 *
 * @author     Steven W. Rock
 * @created    February 28, 2002
 * @see        BJF_1997_IMR
 * @see        AS_1997_IMR
 * @version    1.0
 */

public class ClassicIMRGuiBean
         implements
        NamedObjectAPI,
        ParameterChangeListener
{


    protected final static String C = "IMRGuiBean";
    protected final static boolean D = false;

    public final static String IM_NAME = "Intensity Measure";
    public final static String IM_V1 = "PGA";
    public final static String IM_V2 = "SA";
    public final static String X_AXIS_NAME = "X-Axis";
    public final static String Y_AXIS_NAME = "Y-Axis";
    public final static String Y_AXIS_V1 = "Median";
    public final static String Y_AXIS_V2 = "Std. Dev.";
    public final static String Y_AXIS_V3 = "Exceed Prob.";
    public final static String Y_AXIS_V4 = "Get IML at Exceed Prob.";

    public final static int MEAN = 1;
    public final static int STD_DEV = 2;
    public final static int EXCEED_PROB = 3;
    public final static int IML_AT_EXCEED_PROB = 4;

    private final static int IM = 10;
    private final static int Y_AXIS = 11;
    private final static int X_AXIS = 12;

    protected static HashMap yAxisMap = new HashMap();

    //StringParameter xaxis = null;


    /**
     *  Number of points to calculate between x-axis min and x-axis max, i.e.
     *  the constraint range of the choosen x-axis independent variable
     */
    public final static int NUM = 100;

    /**
     *  Search path for finding editors in non-default packages.
     */
    final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";

    /**
     *  The IMR is the tester IMR that will perform the exceedence probability
     *  calculations as needed by the Gui.
     */
    protected ClassicIMRAPI imr = null;

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
     *  Just a placeholder name for this particular IMR Gui bean.
     */
    protected String name;

    /**
     *  Parameters that control the graphing gui, specifically the IM Types
     *  picklist, the Y-Values picklist, and the X-Values picklist. Some of
     *  these are dynamically generated from particular independent parameters.
     */
    protected ParameterList controlsParamList = null;

    /**
     *  Placeholder for currently selected IM
     */
    protected ParameterAPI selectedIM = null;

    /**
     *  ParameterList of all independent parameters
     */
    protected ParameterList independentParams = new ParameterList();


    ClassicIMRTesterApp applet = null;

    protected ArrayList translatedList = new ArrayList();
    private boolean translateIMR = true;

    /**
     *  Constructor for the IMRGuiBean object. This constructor is passed in a
     *  IMR class name, a name for the Gui bean, and the main applet. From this
     *  info. the IMR class is created at run time along with the paramater
     *  change listener just by the name of the classes.Finally the paramater
     *  editors are created for the independent and control paramaters.
     *
     * @param  className  Fully qualified package and class name of the IMR
     *      class
     * @param  name       Placeholder name for this Gui bean so it could be
     *      referenced in a hash table or hash map.
     * @param  applet     The main applet application that will use these beans
     *      to swap in and out different IMR's.
     */
    public ClassicIMRGuiBean( String className, String name, ClassicIMRTesterApp applet ) {

        // Starting
        String S = C + ": Constructor(): ";
        if ( D ) System.out.println( S + "Starting:" );
        this.name = name;
        this.applet = applet;

        // Create IMR class dynamically from string name
        if ( className == null || className.equals( "" ) )
            throw new ParameterException( S + "IMR Class name cannot be empty or null" );
        imr = ( ClassicIMRAPI ) createIMRClassInstance( className,  (org.scec.param.event.ParameterChangeWarningListener)applet );
        imr.setParamDefaults();

        // Create the control parameters for this imr
        initControlsParamListAndEditor( applet );

        // Create independent parameters
        initIndependentParamListAndEditor( applet );

        // Update which parameters should be invisible
        synchRequiredVisibleParameters();

        // All done
        if ( D ) System.out.println( S + "Ending:" );
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
    public static Object createIMRClassInstance( String className, org.scec.param.event.ParameterChangeWarningListener listener){
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
     *  Sets the name attribute of the IMRGuiBean object
     *
     * @param  newName  The new name value
     */
    public void setName( String newName ) {
        name = newName;
    }


    /**
     * Sets the paramsInIteratorVisible attribute of the IMRGuiBean object
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
     *  Returns the iterator over all controls parameters, such as x and y axis
     *  values.
     *
     * @return    The Controls Iterator
     */
    public ListIterator getControlsIterator() {
        return controlsParamList.getParametersIterator();
    }

    /**
     *  Used by the GUI to get the selected Intensity Measure
     *
     * @return    The selectedIMParameter value
     */
    public ParameterAPI getSelectedIMParam() {
        return selectedIM;
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
     *  Gets the imr attribute of the IMRGuiBean object
     *
     * @return    The imr value
     */
    public ClassicIMRAPI getImr() {
        return imr;
    }

    /**
     *  Gets the controlsEditor attribute of the IMRGuiBean object
     *
     * @return    The controlsEditor value
     */
    public ParameterListEditor getControlsEditor() {
        return controlsEditor;
    }

    /**
     *  Gets the independentsEditor attribute of the IMRGuiBean object
     *
     * @return    The independentsEditor value
     */
    public ParameterListEditor getIndependentsEditor() {
        return independentsEditor;
    }


    /**
     *  Returns the value of a graph picklist control as a string, dermined by
     *  type.
     *
     * @param  type                 1 for Intensity Measure Choice, 2 for Y-Axis
     *      choice and 3 for X-Axis choice.
     * @return                      The string value of the desired picklist.
     * @throws  ParameterException  Thrown if an invalid type, must be 1-3.
     */
    protected String getGraphControlsParamValue( int type ) throws ParameterException {

        String paramName = null;
        switch ( type ) {
            case IM:
                paramName = IM_NAME;
                break;
            case Y_AXIS:
                paramName = Y_AXIS_NAME;
                break;
            case X_AXIS:
                paramName = X_AXIS_NAME;
                break;
            default:
                throw new ParameterException( C + ": getGraphControlsParamValue(): Unsupported graph control type." );
        }

        // Extracting choosen IM picklist value in GUI
        return controlsParamList.getParameter( paramName ).getValue().toString();
    }


    /**
     *  Builds the Y-Axis Name, which may include units, and includes the IM
     *  Type choosen, either "SA" or "PGM". Solely used for labeling the graph
     *
     * @return    The iMTYAxisLabel value
     */
    public String getGraphIMYAxisLabel() {

        String S = C + ": getGraphIMYAxisLabel():";
        if ( D )
            System.out.println( S + ":Starting:" );
        String label = "";

        // Get choosen graph controls values
        String yAxisName = getGraphControlsParamValue( Y_AXIS );
        String xAxisName = getGraphControlsParamValue( X_AXIS );
        String imName = getGraphControlsParamValue( IM );

        // Get choosen intensity measure parameter to extract units
        ParameterAPI imParam = this.imr.getParameter( imName );
        String imUnits = imParam.getUnits();

        // Determine whether to add log to lable - and add IM type choosen
        // if ( !yAxisName.equals( Y_AXIS_V3 ) ) label = yAxisName + " ln-" + imName;
        // else label = yAxisName + ' ' + imName;

        // SWR - Changed mean so that it returns the real mean, used to be the log of the mean
        // if IML at exceed Prob. is chosen, then only show IM
        if(!yAxisName.equals( Y_AXIS_V4 )) label = yAxisName + ' ' + imName;
        else  label = imName;

        // Determine if units should be added
        if ( (yAxisName.equals( Y_AXIS_V1 ) || yAxisName.equals( Y_AXIS_V4 ))
             && !ClassUtils.isEmptyString( imUnits ) )
            label += " (" + imUnits + ')';

        // All done
        if ( D )
            System.out.println( S + ":Ending: label = " + label );
        return label;
    }


    /**
     *  Returns which X-Axis were choosen, appending the units if present in the
     *  parameter. Used for Plot labeling of the x-axis.
     *
     * @return    The xAxisLabel value
     */
    public String getGraphXAxisLabel() {

        // Get choosen x_axis parameter name
        String xAxisName = getGraphControlsParamValue( X_AXIS );

        // get the parameter, units, add to label string
        ParameterAPI param = imr.getParameter( xAxisName );
        String units = param.getUnits();
        if ( !ClassUtils.isEmptyString( units ) )
            xAxisName += " (" + units + ')';
        return xAxisName;
    }


    /**
     *  Builds a Plot title string of the form "y-axis label vs. x-axis label".
     *  The x and y axis labels are obtained by calling getXAxisLabel() and
     *  getIMYAxisLabel()
     *
     * @return                          The xYAxisTitle value
     * @exception  ConstraintException  Description of the Exception
     */
    public String getGraphXYAxisTitle() throws ConstraintException {
        return getGraphControlsParamValue( Y_AXIS ) + " vs. " + getGraphControlsParamValue( X_AXIS );
    }


    protected void setIgnoreWarnings(boolean ignoreWarning){

        if( !translateIMR) return;
        ListIterator it = translatedList.listIterator();
        while(it.hasNext()){
            ((TranslatedWarningDoubleParameter)it.next()).setIgnoreWarning(ignoreWarning);
        }
    }

    /**
     *  Controller function. Dispacter function. Based on which Y-Axis was
     *  choosen, determines which dependent variable discretized function to
     *  return. Once the discretized function has been calculated (by other
     *  functions), the x-axis and y-axis name is set in the Discretized
     *  function, and the independent parameters that were used in the model
     *  calculation are set in the function.
     *
     * @return                          The choosenFunction value
     * @exception  ConstraintException  Description of the Exception
     */
    public DiscretizedFuncAPI getChoosenFunction()
             throws ConstraintException {

        // Starting
        String S = C + ": getChoosenFunction():";
        if ( D )
            System.out.println( S + "Starting" );

        // Determines from the IM Picklist in the GUI which IM parameter
        // to set as current IM in the IMR. This allows the IMR to be able
        // to calculate which coefficients to use to calculate the functions


       // Get choosen graph controls values
        String yAxisName = getGraphControlsParamValue( Y_AXIS );
        String xAxisName = getGraphControlsParamValue( X_AXIS );

        setIgnoreWarnings(true);
        imr.setIntensityMeasure( getGraphControlsParamValue( IM ) );
        setIgnoreWarnings(false);



        // Determine which Y=Axis choice to process
        if ( !yAxisMap.containsKey( yAxisName ) ) throw new ConstraintException( S + "Invalid choice choosen for y-axis." );
        int type = ( ( Integer ) yAxisMap.get( yAxisName ) ).intValue();
        if ( D ) System.out.println( S + "Type = " + type );

        // Get X-Axis parameter
        ParameterAPI xAxisParam = imr.getParameter( xAxisName );

        // Ensure X-Axis constraint Double or DoubleDiscrete Constraint
        if ( !ParamUtils.isDoubleOrDoubleDiscreteConstraint( xAxisParam ) )
            throw new ConstraintException( S + "X-Axis must contain double or double discrete constraint." );

        // Get the Discretized Function - calculation done here
        DiscretizedFuncAPI function = getFunctionForXAxis( xAxisParam, type );

        // Clone the parameter list used to calculate this Discretized Function
        ParameterList clones = independentsEditor.getVisibleParametersCloned();

        /**
         * @todo FIX - Legend IMR translation done here.
         * may be poor design, what if IMR types change to another type in future.
         * Translated parameters should deal directly with ParameterAPI, not specific subclass
         * types.
         */
        if( translateIMR){
            ParameterAPI imrParam = (ParameterAPI)imr.getIntensityMeasure().clone();
            if( imrParam instanceof WarningDoubleParameter){

                WarningDoubleParameter warnParam = (WarningDoubleParameter)imrParam;
                TranslatedWarningDoubleParameter transParam = new TranslatedWarningDoubleParameter(warnParam);
                transParam.setTranslate(true);

                if( clones.containsParameter(warnParam.getName()) ){
                    clones.removeParameter( warnParam.getName() );
                    clones.addParameter(transParam);
                }

            }
        }


        if ( function != null ) {
            ((ArbDiscrFuncWithParams)function).setParameterList( clones );
            function.setName(imr.getName());
        }
        return function;
    }



    /**
     *  Function needs to be fixed because point may not go to the end, i.e. max
     *  because of math errors with delta = (max - min)/num. <p>
     *
     *  SWR - A way to increase performace may be to create a cache of Doubles,
     *  with the vaules set. If the value 20.1 occurs many times, use the same
     *  pointer in the DiscretizedFunction2DAPI
     *
     * @param  xAxisParam               Description of the Parameter
     * @param  type                     Description of the Parameter
     * @return                          The meansForXAxis value
     * @exception  ConstraintException  Description of the Exception
     */
    private DiscretizedFuncAPI getFunctionForXAxis( ParameterAPI xAxisParam, int type )
             throws ConstraintException {

        // Starting
        String S = C + ": getFunctionForXAxis():";
        if ( D )
            System.out.println( S + "Param = " + xAxisParam.getName() );
        ArbDiscrFuncWithParams function = new ArbDiscrFuncWithParams();
        String s = "";

        // constraint contains the only possible values, iterate over possible values to calc the mean
        if ( ParamUtils.isDoubleDiscreteConstraint( xAxisParam ) ) {

            // Get the period constraints to iterate over
            String paramName = xAxisParam.getName();
            DoubleDiscreteParameter period = ( DoubleDiscreteParameter ) imr.getParameter( paramName );
            DoubleDiscreteConstraint constraint = ( DoubleDiscreteConstraint ) period.getConstraint();

            Object oldVal = period.getValue();

            // Loop over all periods calculating the mean
            ListIterator it = constraint.listIterator();
            while ( it.hasNext() ) {

                // Set the parameter with the next constraint value in the list
                Double val = ( Double ) it.next();
                period.setValue( val );

                // This determines which are the current coefficients to use, i.e. if this
                // x-axis choosen is Period, this function call will update the SA with this
                // new period constraint value (SA and Period have same constraints. Then the SA
                // will be passed into the IMR which will set the new coefficients because the SA period
                // has been changed. Recall the coefficients are stored in a hash table "IM Name/Period" as the key
                imr.setIntensityMeasure( getGraphControlsParamValue( IM ) );

                DataPoint2D point = new DataPoint2D( val.doubleValue(), getCalculation( type ));
                function.set( point );

            }

            // return to original state
            period.setValue( oldVal );
            imr.setIntensityMeasure( getGraphControlsParamValue( IM ) );

        }
        // Constraint contains a min and a max
        else if( ParamUtils.isWarningParameterAPI( xAxisParam ) ){



            /**
             * @todo FIX - Axis IMR translation done here.
             * may be poor design, what if IMR types change to another type in future.
             * Translated parameters should deal directly with ParameterAPI, not specific subclass
             * types. Something for phase II.
             */
            if( translateIMR){


                ParameterAPI imrParam = (ParameterAPI)imr.getIntensityMeasure().clone();

                String xAxisName = xAxisParam.getName();
                String imrName = imrParam.getName();


                if(  xAxisName.equalsIgnoreCase(imrName) && xAxisParam instanceof WarningDoubleParameter){

                    WarningDoubleParameter warnParam = (WarningDoubleParameter)xAxisParam;
                    TranslatedWarningDoubleParameter transParam = new TranslatedWarningDoubleParameter(warnParam);
                    transParam.setTranslate(true);


                    // Calculate min and max values from constraint
                    MinMaxDelta minmaxdelta = new MinMaxDelta( (WarningParameterAPI)transParam );
                    function = buildFunction( transParam, type, function, minmaxdelta );

                }
                else{
                    // Calculate min and max values from constraint
                    MinMaxDelta minmaxdelta = new MinMaxDelta( (WarningParameterAPI)xAxisParam );
                    function = buildFunction( xAxisParam, type, function, minmaxdelta );
                }
            }
            else{
                // Calculate min and max values from constraint
                MinMaxDelta minmaxdelta = new MinMaxDelta( (WarningParameterAPI)xAxisParam );
                function = buildFunction( xAxisParam, type, function, minmaxdelta );
            }



        }

        // Constraint contains a min and a max
        else if ( ParamUtils.isDoubleConstraint( xAxisParam ) ) {

            // Calculate min and max values from constraint
            MinMaxDelta minmaxdelta = new MinMaxDelta( xAxisParam );
            function = buildFunction( xAxisParam, type, function, minmaxdelta );
        }

        else
            throw new ConstraintException( S + "Not supported as an independent parameter: " + name );

        return function;
    }

    private ArbDiscrFuncWithParams buildFunction(
        ParameterAPI xAxisParam,
        int type,
        ArbDiscrFuncWithParams function,
        MinMaxDelta minmaxdelta
    ){

        // Fetch the independent variable selected in the x-axis choice
        ParameterAPI independentParam = imr.getParameter( xAxisParam.getName() );
        Object oldVal = independentParam.getValue();

        double val = minmaxdelta.min;
        double newVal;

        if( independentParam instanceof WarningDoubleParameter &&
            xAxisParam instanceof TranslatedWarningDoubleParameter){

            ((TranslatedWarningDoubleParameter)xAxisParam).setParameter(
               (WarningDoubleParameter)independentParam
            );


            while ( val <= minmaxdelta.max ) {

                BigDecimal bdB = new BigDecimal( val );
                bdB = bdB.setScale( 2, BigDecimal.ROUND_UP );
                newVal = bdB.doubleValue();

                xAxisParam.setValue( new Double( newVal ) );
                DataPoint2D point = new DataPoint2D( newVal , getCalculation( type ) );
                function.set( point );
                val += minmaxdelta.delta;
            }

        }
        else{

            while ( val <= minmaxdelta.max ) {

                BigDecimal bdB = new BigDecimal( val );
                bdB = bdB.setScale( 2, BigDecimal.ROUND_UP );
                newVal = bdB.doubleValue();

                independentParam.setValue( new Double( newVal ) );
                DataPoint2D point = new DataPoint2D( newVal , getCalculation( type ) );
                function.set( point );
                val += minmaxdelta.delta;
            }


        }



        if( ParamUtils.isWarningParameterAPI( independentParam ) ){
            ( (WarningParameterAPI) independentParam ).setValueIgnoreWarning(oldVal);
        }
        else independentParam.setValue( oldVal );


        return function;
    }


    /**
     *  Returns the intensity measure relationship calculation for either mean,
     *  std. dev or exceedence probability depending on which type is desired.
     *
     * @param  type  1 for mean, 2 for std. dev. and 3 for exceedence
     *      probability
     * @return       The imr calculation
     */
    private double getCalculation( int type ) {
        double result =  0.0;
        switch ( type ) {
            case MEAN:
                result = Math.exp( imr.getMean() );
                break;
            case EXCEED_PROB:
                result = imr.getExceedProbability();
                break;
            case STD_DEV:
                result = imr.getStdDev();
                break;
            case IML_AT_EXCEED_PROB :
                result = Math.exp(imr.getIML_AtExceedProb());
                break;
        }
        return result;
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
        if (D) System.out.println(S+"parametr changed:"+name1);
        if ( this.controlsParamList.containsParameter( name1 ) ) {
            if ( D )
                System.out.println( S +":Control Parameter changed, need to update gui parameter editors" );
            synchRequiredVisibleParameters();
        }
        else if( name1.equals(ClassicIMR.SIGMA_TRUNC_TYPE_NAME) ){  // special case hardcoded. Not the best way to do it, but need framework to handle it.

        //    System.out.println(S + ClassicIMR.SIGMA_TRUNC_TYPE_NAME + " has changed");
            String value = event.getNewValue().toString();
            toggleSigmaLevelBasedOnTypeValue(value);

        }
    }

    protected void toggleSigmaLevelBasedOnTypeValue(String value){

        if( value.equalsIgnoreCase("none") ) {
            if(D) System.out.println("Value = " + value + ", need to set value param off.");
            independentsEditor.setParameterInvisible( ClassicIMR.SIGMA_TRUNC_LEVEL_NAME, false );
        }
        else{
            if(D) System.out.println("Value = " + value + ", need to set value param on.");
            independentsEditor.setParameterInvisible( ClassicIMR.SIGMA_TRUNC_LEVEL_NAME, true );
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
     * @param  applet  Description of the Parameter
     */
    protected void initControlsParamListAndEditor( ClassicIMRTesterApp applet ) {

        // Starting
        String S = C + ": initControlsParamListAndEditor(): ";
        if ( D ) System.out.println( S + "Starting:" );

        if ( imr == null )
            throw new ParameterException( S + "Imr is null, unable to continue." );
        if ( applet == null )
            throw new ParameterException( S + "Applet is null, unable to continue." );

        // Get required iterators to build constraints
        ListIterator supportedIntensityMeasureIterator = imr.getSupportedIntensityMeasuresIterator();
        ListIterator meanIndependentParamsIterator = imr.getMeanIndependentParamsIterator();

        // Make a Y-Axis picklist Parameter. Y-Axis possible choices
        // Selected is the Y_AXIS_V1
        StringConstraint yaxisConstraint = new StringConstraint();
        yaxisConstraint.addString( Y_AXIS_V1 );
        yaxisConstraint.addString( Y_AXIS_V2 );
        yaxisConstraint.addString( Y_AXIS_V3 );
        yaxisConstraint.addString( Y_AXIS_V4 );
        StringParameter yaxis = new StringParameter( Y_AXIS_NAME, yaxisConstraint, Y_AXIS_V1 );
        yaxis.addParameterChangeListener(this);
        // IM Choices picklist Parameter - Note these choices are now all DoubleParameters
        // Selected is first returned from ListIterator
        boolean first = true;
        DependentParameterAPI imParam = null;
        String name = "";
        StringConstraint imConstraint = new StringConstraint();
        while ( supportedIntensityMeasureIterator.hasNext() ) {
            DependentParameterAPI param = ( DependentParameterAPI ) supportedIntensityMeasureIterator.next();
            name = param.getName();
            if ( first ) {
                first = false;
                imParam = param;
            }
            imConstraint.addString( name );
        }
        StringParameter im = new StringParameter( IM_NAME, imConstraint, "", imParam.getName() );
        im.addParameterChangeListener(this);

        // X-axis choices - picks only double and discrete doubles as possible values
        // Selected is first returned from ListIterator
        StringConstraint xAxisConstraint = new StringConstraint();
        first = true;
        String val = null;
        name = null;
        while ( meanIndependentParamsIterator.hasNext() ) {
            ParameterAPI param = ( ParameterAPI ) meanIndependentParamsIterator.next();
            // Fix so that all data types can be supported on x-axis
            if ( !( param instanceof StringParameter ) ) {
                name = param.getName();
                if ( first ) {
                    first = false;
                    val = name;
                }
                if ( !xAxisConstraint.containsString( name ) )
                    xAxisConstraint.addString( name );
            }
        }

        // Now add IM independent parameters to x-axis list
        ListIterator imParamsIterator = imParam.getIndependentParametersIterator();
        while ( imParamsIterator.hasNext() ) {
            Object obj = imParamsIterator.next();

            ParameterAPI param = ( ParameterAPI ) obj;
            // Fix so that all data types can be supported on x-axis
            if ( !( param instanceof StringParameter ) ) {
                name = param.getName();
                if ( !xAxisConstraint.containsString( name ) )
                    xAxisConstraint.addString( name );
            }
        }
        StringParameter xaxis = new StringParameter( X_AXIS_NAME, xAxisConstraint, val );
        xaxis.addParameterChangeListener(this);
        // Now make the parameters list
        // At this point all values have been set for the IM type, xaxis, and the yaxis
        controlsParamList = new ParameterList();
        controlsParamList.addParameter( im );
        controlsParamList.addParameter( yaxis );
        controlsParamList.addParameter( xaxis );


        // Now make the Editor for the list
        controlsEditor = new ParameterListEditor( controlsParamList);
        controlsEditor.setTitle( "Graph Controls" );

        // update the im choice in the imr
        imr.setIntensityMeasure( getGraphControlsParamValue( IM ) );

        // All done
        if ( D )
            System.out.println( S + "Ending: Created imr parameter change listener " );

    }


    /**
     *  This function gets the independent paramaters lists from the IMR and
     *  then creates the list editor. These editors know what type of Gui
     *  element to present in the list based on the data type of each paramater.
     *  There is a default location where it looks for these editor classes if
     *  it cannot be found there it will look in the special editors package
     *  file path.
     *
     * @param  applet                  Description of the Parameter
     * @exception  ParameterException  Description of the Exception
     */
    private void initIndependentParamListAndEditor( ClassicIMRTesterApp applet )
             throws ParameterException {

        // Starting
        String S = C + ": initIndependentParamEditor(): ";
        if ( D ) System.out.println( S + "Starting:" );
        if ( imr == null ) throw new ParameterException( S + "Imr is null, unable to init independent parameters." );

        // Initialize the parameter list
        independentParams = new ParameterList();

        // Add mean parameters
        ListIterator it = imr.getMeanIndependentParamsIterator();
        while ( it.hasNext() ) {
            ParameterAPI param = ( ParameterAPI ) it.next();
            param.addParameterChangeListener(this);
            param.addParameterChangeFailListener(applet);
            if ( !( independentParams.containsParameter( param.getName() ) ) )
                independentParams.addParameter( param );

        }

        // Add std parameters
        it = imr.getStdDevIndependentParamsIterator();
        while ( it.hasNext() ) {
            ParameterAPI param = ( DependentParameterAPI ) it.next();
            param.addParameterChangeListener(this);
            param.addParameterChangeFailListener(applet);
            if ( !( independentParams.containsParameter( param.getName() ) ) )
                independentParams.addParameter( param );

        }

        // Add additional exceedence probability parameters
        it = imr.getExceedProbIndependentParamsIterator();
        while ( it.hasNext() ) {
            ParameterAPI param = ( DependentParameterAPI ) it.next();
            param.addParameterChangeListener(this);
            param.addParameterChangeFailListener(applet);
            if ( !( independentParams.containsParameter( param.getName() ) ) )
                independentParams.addParameter( param );

        }

        // Add IML at exceedence probability parameters
        it = imr.getIML_AtExceedProbIndependentParamsIterator();
        while ( it.hasNext() ) {
            ParameterAPI param = ( DependentParameterAPI ) it.next();
            param.addParameterChangeListener(this);
            param.addParameterChangeFailListener(applet);
            if ( !( independentParams.containsParameter( param.getName() ) ) )
                independentParams.addParameter( param );

        }

        // Add im parameters and their independent parameters
        it = imr.getSupportedIntensityMeasuresIterator();
        while ( it.hasNext() ) {
            DependentParameterAPI param = ( DependentParameterAPI ) it.next();
            param.addParameterChangeListener(this);
            param.addParameterChangeFailListener(applet);
           // System.out.println(param.getName());
            if ( !( independentParams.containsParameter( param.getName() ) ) ){

                /** @todo Log Translated Params goes here */
                if( translateIMR && ( param instanceof WarningDoubleParameter) ){
                    TranslatedWarningDoubleParameter transParam =
                        new TranslatedWarningDoubleParameter( (WarningDoubleParameter)param);
                    independentParams.addParameter( transParam );
                    translatedList.add( transParam );
                }
                else independentParams.addParameter( param );

            }

            ListIterator it2 = param.getIndependentParametersIterator();
            while ( it2.hasNext() ) {
                ParameterAPI param2 = ( ParameterAPI ) it2.next();
            //    System.out.println(param2.getName());
                if ( !( independentParams.containsParameter( param2.getName() ) ) )
                    independentParams.addParameter( param2 );

            }
        }


        // Add supported IM parameters to independentparameter list,
        // used for setting iml in the im. Modifies exceedence calculation
        // it = imr.getSupportedIntensityMeasureIterator();
        // while(it.hasNext() ){ list.addParameter( (ParameterAPI)it.next() ); }

        // Build package names search path
        String[] searchPaths = new String[2];
        searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
        searchPaths[1] = SPECIAL_EDITORS_PACKAGE;

        // Build editor list
        independentsEditor = new ParameterListEditor( independentParams, searchPaths );
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

        String S = C + ": getGraphIMYAxisLabel():";
        if ( D )
            System.out.println( S + ":Starting:" );

        // Get choosen graph controls values
        String imName = getGraphControlsParamValue( IM );
        String xAxisName = getGraphControlsParamValue( X_AXIS );
        String yAxisName = getGraphControlsParamValue( Y_AXIS );

        if ( D ) System.out.println( S + ":X-Axis: " + xAxisName );
        if ( D ) System.out.println( S + ":Y-Axis: " + yAxisName );
        if ( D ) System.out.println( S + ":IM Name: " + imName );

        // Can't do anything if we don't have an im relationship
        if ( imr == null )
            throw new ParameterException( S + "Imr is null, unable to continue." );

        // Turn off all parameters - start fresh, then make visible as required below
        // SWR - Looks like a bug here in setParameterInvisible() - don't want to fix right now, the boolean
        // below should be true, not false.
        ListIterator it = this.independentParams.getParametersIterator();
        while ( it.hasNext() )
            independentsEditor.setParameterInvisible( ( ( ParameterAPI ) it.next() ).getName(), false );

        DependentParameterAPI imParam = null;

        // Add im parameters independent parameters to list
        imParam = ( DependentParameterAPI ) imr.getParameter( imName );
        ListIterator imIt = imParam.getIndependentParametersIterator();
        setParamsInIteratorVisible( imIt );

        // Determine which y-axis function was choosen - then add it's required parameters
        if ( yAxisName.equals( Y_AXIS_V1 ) )
           //if mean is selected
            setParamsInIteratorVisible( imr.getMeanIndependentParamsIterator() );
        else if ( yAxisName.equals( Y_AXIS_V2 ) )
            // if std dev is selected
            setParamsInIteratorVisible( imr.getStdDevIndependentParamsIterator() );
        else if ( yAxisName.equals( Y_AXIS_V3 ) ) {
           //if exceed Prob is selected
            setParamsInIteratorVisible( imr.getExceedProbIndependentParamsIterator() );

            // Hardcoded for special values
            ParameterEditorAPI paramEditor = independentsEditor.getParameterEditor(ClassicIMR.SIGMA_TRUNC_TYPE_NAME);
            if( paramEditor != null ){
                String value = paramEditor.getParameter().getValue().toString();
                toggleSigmaLevelBasedOnTypeValue(value);
            }
        }
        // if IML at Exceed Prob is selected
        else if ( yAxisName.equals( Y_AXIS_V4 ) ) {
            setParamsInIteratorVisible( imr.getIML_AtExceedProbIndependentParamsIterator());
            // Hardcoded for special values
            ParameterEditorAPI paramEditor = independentsEditor.getParameterEditor(ClassicIMR.SIGMA_TRUNC_TYPE_NAME);
            if( paramEditor != null ){
               String value = paramEditor.getParameter().getValue().toString();
               toggleSigmaLevelBasedOnTypeValue(value);
            }
        }

        else
            throw new ParameterException( S + "Invalid Y Axis choice" );


        // REbuild x-axis choice picklist from scratch
        // X-axis choices - picks only double and discrete doubles as possible values
        // Selected is first returned from ListIterator

        StringConstraint xAxisConstraint = new StringConstraint();

        xAxisConstraint = addToXAxisConstraint(
                imr.getMeanIndependentParamsIterator(), xAxisConstraint
                 );

        xAxisConstraint = addToXAxisConstraint(
                imr.getStdDevIndependentParamsIterator(), xAxisConstraint
                 );
        xAxisConstraint = addToXAxisConstraint(
                imParam.getIndependentParametersIterator(), xAxisConstraint
                 );

        // First value in x axis constraint list
        String val = xAxisConstraint.listIterator().next().toString();

        // Add im parameter to x-axis choices if exceedence probability was choosen
        // for the y axis
        if ( yAxisName.equals( Y_AXIS_V3 ) )
            xAxisConstraint.addString( imParam.getName() );

        // Add exceed. prob parameter to x-axis choices if IML atExceedProb was choosen
        // for the y axis (name hard coded for now; not sure how to get it in general)
        if ( yAxisName.equals( Y_AXIS_V4 ) )
            xAxisConstraint.addString( "Exceed. Prob." );


        // check that original x-axis choice is still viable
        if ( xAxisConstraint.isAllowed( xAxisName ) )
            val = xAxisName;

        // Get the x-axis editor
        ConstrainedStringParameterEditor editor =
                ( ConstrainedStringParameterEditor ) controlsEditor.getParameterEditor( ClassicIMRGuiBean.X_AXIS_NAME );

        // Get the x-axis parameter
        StringParameter param = ( StringParameter ) editor.getParameter();


        // Create the new parameter
        StringParameter param2 = new StringParameter(
                param.getName(),
                xAxisConstraint,
                param.getUnits(),
                val
                 );
        param2.addParameterChangeListener(this);
        // swap editors
        controlsEditor.replaceParameterForEditor( param.getName(), param2 );

        // Make the choosen im visible. Note may be turned off again in the next
        // step because the im parameter is also an x-axis choice to iterate
        // over intensity measure level
        if ( yAxisName.equals( Y_AXIS_V3 ) )
            independentsEditor.setParameterInvisible( imName, true );

        // Make the choosen x-axis invisible in the independent parameter list
        independentsEditor.setParameterInvisible( val, false );

        // refresh the GUI
        controlsEditor.validate();
        controlsEditor.repaint();

        independentsEditor.validate();
        independentsEditor.repaint();

        // All done
        if ( D )
            System.out.println( S + "Ending: " );
    }

    /**
     *  Adds a feature to the ToXAxisConstraint attribute of the IMRGuiBean
     *  object
     *
     * @param  it               The feature to be added to the ToXAxisConstraint
     *      attribute
     * @param  xAxisConstraint  The feature to be added to the ToXAxisConstraint
     *      attribute
     * @return                  Description of the Return Value
     */
    private StringConstraint addToXAxisConstraint( ListIterator it, StringConstraint xAxisConstraint ) {

        boolean add = true;
        while ( it.hasNext() ) {

            add = true;

            ParameterAPI param = ( ParameterAPI ) it.next();
            if ( !( param instanceof StringParameter ) ) {

                // If DoubleDiscreteConstraint check that it has more than one value to plot on xaxis
                ParameterConstraintAPI constraint = param.getConstraint();
                if ( constraint instanceof DiscreteParameterConstraintAPI ) {
                    int size = ( ( DiscreteParameterConstraintAPI ) constraint ).getAllowedValues().size();
                    if ( size < 2 )
                        add = false;
                }
                if ( add ) {
                    name = param.getName();
                    if ( !xAxisConstraint.containsString( name ) )
                        xAxisConstraint.addString( name );
                }
            }
        }
        return xAxisConstraint;

    }

    public void setTranslateIMR(boolean translateIMR) {
        this.translateIMR = translateIMR;
    }

    public boolean isTranslateIMR() {
        return translateIMR;
    }



    /**
     *  <p>
     *
     *  Title: MinMaxDelta</p> <p>
     *
     *  Description: Determines the min and max values from constraints, then
     *  calculates the delta between points given a desired number of points on
     *  the x-axis</p> Note: This has to be updated to include
     *  IntegerConstraints <p>
     *
     * SWR: Note - This may have a bug in this code. I haven't looked at this yet.
     * At one point I call getMin().doubleValue. What happens if this is NaN or
     * -/+ Infinity? This has to be tested
     *
     *  Copyright: Copyright (c) 2001</p> <p>
     *
     *  Company: </p>
     *
     * @author     Steven W. Rock
     * @created    April 17, 2002
     * @version    1.0
     */
    class MinMaxDelta {

        /**
         *  Description of the Field
         */
        protected double min;
        /**
         *  Description of the Field
         */
        protected double max;
        /**
         *  Description of the Field
         */
        protected double delta;
        /**
         *  Description of the Field
         */
        private final static String C = "MinMaxDelta";

        /**
         *  Constructor for the MinMaxDelta object
         *
         * @param  param                    Description of the Parameter
         * @exception  ConstraintException  Description of the Exception
         */
        public MinMaxDelta( ParameterAPI param ) throws ConstraintException {

            // Make sure this parameter has a constraint from which we can extract a Double value
            if ( !ParamUtils.isDoubleOrDoubleDiscreteConstraint( param ) )
                throw new ConstraintException( C + ": Constructor(): " +
                        "Parameter must have Double or DoubleDiscrete Constraint, unable to calculate"
                         );

            // Determine min and max ranges with which to iterate over
            min = 0;
            max = 1;

            // Also handles subclasses such as TranslatedWarningDoubleParameters */
            if( param instanceof TranslatedWarningDoubleParameter){

                try{
                    TranslatedWarningDoubleParameter param1 = (TranslatedWarningDoubleParameter)param;
                    min = param1.getWarningMin().doubleValue();
                    max = param1.getWarningMax().doubleValue();
                }
                catch( Exception e){
                    throw new ConstraintException(e.toString());
                }
            }
            else{

                // Extract constraint
                ParameterConstraintAPI constraint = param.getConstraint();

                // Get min/max from Double Constraint
                if ( ParamUtils.isDoubleConstraint( param ) ) {
                    min = ( ( DoubleConstraint ) constraint ).getMin().doubleValue();
                    max = ( ( DoubleConstraint ) constraint ).getMax().doubleValue();
                }
                // Check each value of discrete values and determine high and low values
                else if ( ParamUtils.isDoubleDiscreteConstraint( param ) ) {

                    DoubleDiscreteConstraint con = ( DoubleDiscreteConstraint ) constraint;

                    int size = con.size();
                    if ( size > 0 ) {
                        ListIterator it = con.listIterator();
                        Double DD = ( Double ) it.next();

                        min = DD.doubleValue();
                        max = max;

                        while ( it.hasNext() ) {
                            Double DD2 = ( Double ) it.next();
                            double val = DD2.doubleValue();
                            if ( val > max )
                                max = val;
                            else if ( val < min )
                                min = val;
                        }
                    }
                }
            }

            // Calculate delta between points on axis
            delta = ( max - min ) / ( NUM - 1 );
        }

        /**
         *  Constructor for the MinMaxDelta object
         *
         * @param  param                    Description of the Parameter
         * @exception  ConstraintException  Description of the Exception
         */
        public MinMaxDelta( WarningParameterAPI param ) throws ConstraintException{
            // Determine min and max ranges with which to iterate over
            min = 0;
            max = 1;


            // Also handles subclasses such as TranslatedWarningDoubleParameters */
            if( param instanceof TranslatedWarningDoubleParameter){

                try{
                    TranslatedWarningDoubleParameter param1 = (TranslatedWarningDoubleParameter)param;
                    min = param1.getWarningMin().doubleValue();
                    max = param1.getWarningMax().doubleValue();
                }
                catch( Exception e){
                    throw new ConstraintException(e.toString());
                }
            }
            else{

                // Extract constraint
                //ParameterConstraintAPI constraint =
                ParameterConstraintAPI constraint = param.getWarningConstraint();
                if( constraint == null ) constraint = param.getConstraint();

                // Get min/max from Double Constraint
                if ( ParamUtils.isDoubleConstraint( param ) ) {
                    min = ( ( DoubleConstraint ) constraint ).getMin().doubleValue();
                    max = ( ( DoubleConstraint ) constraint ).getMax().doubleValue();
                }
                // Check each value of discrete values and determine high and low values
                else if ( ParamUtils.isDoubleDiscreteConstraint( param ) ) {

                    DoubleDiscreteConstraint con = ( DoubleDiscreteConstraint ) constraint;

                    int size = con.size();
                    if ( size > 0 ) {
                        ListIterator it = con.listIterator();
                        Double DD = ( Double ) it.next();

                        min = DD.doubleValue();
                        max = max;

                        while ( it.hasNext() ) {
                            Double DD2 = ( Double ) it.next();
                            double val = DD2.doubleValue();
                            if ( val > max )
                                max = val;
                            else if ( val < min )
                                min = val;
                        }
                    }
                }
            }

            // Calculate delta between points on axis
            delta = ( max - min ) / ( NUM - 1 );
        }


    }
    static {
        yAxisMap.put( Y_AXIS_V1, new Integer( MEAN ) );
        yAxisMap.put( Y_AXIS_V2, new Integer( STD_DEV ) );
        yAxisMap.put( Y_AXIS_V3, new Integer( EXCEED_PROB ) );
        yAxisMap.put( Y_AXIS_V4, new Integer( IML_AT_EXCEED_PROB) );
    }
}


