package org.scec.sha.imr.attenRelImpl.test;

import java.io.*;
import java.util.*;

import org.scec.sha.imr.*;
import org.scec.param.*;
import org.scec.data.function.*;
import org.scec.exceptions.*;
import org.scec.util.*;
import org.scec.data.*;

/**
 * <p>Title: AttenRelResultsChecker</p>
 * <p>Description: This class provide the common implementation of the functions for all the
 * AttenuationRelationships testcases classes</p>
 * @author :Ned Field, Nitin Gupta and Vipin Gupta
 * @created :July 7, 2003
 * @version 1.0
 */

public class AttenRelResultsChecker {

  private ParameterList list = new ParameterList();
  private AttenuationRelationshipAPI imr;
  private String resultFile= null;

  //hardcode strings string val for all the X and Y axis values that are shown
  //in the combo box for the user selection as the control params editor
  public final static String X_AXIS_NAME = "X-Axis";
  public final static String Y_AXIS_NAME = "Y-Axis";
  public final static String Y_AXIS_MEDIAN = "Median";
  public final static String Y_AXIS_STD_DEV = "Std. Dev.";
  public final static String Y_AXIS_EXCEED_PROB = "Exceed Prob.";
  public final static String Y_AXIS_IML_AT_PROB = "IML at Exceed Prob.";

  //static int values for the Y values in IMR
  public final static int MEAN = 1;
  public final static int STD_DEV = 2;
  public final static int EXCEED_PROB = 3;
  public final static int IML_AT_EXCEED_PROB = 4;


  //this needs to read from the file for the different Intensity Measures,
  //but cuurently we only check for "SA"
  private final static String SA_NAME="SA";

  //checks to see if we need Log for IMT
  private boolean translateIMR = true;

  //these are the comments in the file where the data results for the test result in that IMR starts and ends
  public static final String START_RESULT_VALUES = "#start of result values for this test set";
  public static final String END_RESULT_VALUES = "#end of result values for this test set";

  public AttenRelResultsChecker(AttenuationRelationshipAPI imr, String file) {
    this.imr = imr;
    this.resultFile = file;
    //initially the parameterList is empty but when te constructr is called
    //we add the parameters to te paramList
    //adding the ExceedProb Params to the ParamList
    ListIterator it =imr.getExceedProbIndependentParamsIterator();
    while(it.hasNext()){
      ParameterAPI param = (ParameterAPI)it.next();
      if ( !( list.containsParameter( param.getName() ) ) )
        list.addParameter( param );
    }

    //adding the IML@ Exceed Prob params to the ParamList
    it =imr.getIML_AtExceedProbIndependentParamsIterator();
    while(it.hasNext()){
      ParameterAPI param = (ParameterAPI)it.next();
      if ( !( list.containsParameter( param.getName() ) ) )
        list.addParameter( param );
    }

    //adding the mean Independent params to the ParamList
    it =imr.getMeanIndependentParamsIterator();
    while(it.hasNext()){
      ParameterAPI param = (ParameterAPI)it.next();
      if ( !( list.containsParameter( param.getName() ) ) )
        list.addParameter( param );
    }

    //adding the Std. Dev Independent params to the ParamList
    it =imr.getStdDevIndependentParamsIterator();
    while(it.hasNext()){
      ParameterAPI param = (ParameterAPI)it.next();
      if ( !( list.containsParameter( param.getName() ) ) )
        list.addParameter( param );
    }

    //set the defaults values for that AttenuationRelationship
    imr.setParamDefaults();

    //**** This temporary as we are getting the result for the SA Period , later
    //we will read the value from the file ****/
    //set the intensity measure for the AttenuationRelationship
    imr.setIntensityMeasure(this.SA_NAME);

  }

  /**
   * This function reads the AttenuationRelationship Result set data file,
   * does the calculation for each test set in the file and compares whether
   * the result produced by the SHA code is within the acceptable range of the
   * actual result that is stored in the file with each test set.
   * Each test set corresponds to different parameters setting to get the target result.
   * That result set is like a benchMark for us to compare the SHA outputs for those parameters
   * settings with the result in the file.
   */
  public void readResultFile(){
    try{
      //which file to read for the AttenREl testcase
      FileReader fr = new FileReader(resultFile);
      BufferedReader br = new BufferedReader(fr);

      //Arbitrary function to store the target IMR values for that test result set
      ArbitrarilyDiscretizedFunc targetFunction = null;
      //contains the value for the X-Axis
      ParameterAPI xAxisParam =null;
      //stores the test number which we testing from the file
      int testNumber=0;
      // reads the first line in the file
      String str = (br.readLine()).trim();
      //keep reading the file until file pointer reaches the end of file.
      while(str !=null){
        //stores the int value for the selected Y-axis param
        int yAxisValue =0;
        //if the line contains nothing, just skip that line and read next
        if(str.equalsIgnoreCase(""))
          str = br.readLine();
        //if the line contains some data
        else{

          //if the String read is the X-Axis Param value in the file
          if(str.startsWith(this.X_AXIS_NAME)){
            String st = str.substring(str.indexOf("=")+1).trim();
            xAxisParam = imr.getParameter(st);
          }
          //if the string read is the new set of the test case
          else if(str.startsWith("Set")){
            String st = str.substring(str.indexOf("-")+1).trim();
            testNumber = Integer.parseInt(st);
          }

          //if the String read is the Y-Axis Param value in the file
          else if(str.startsWith(this.Y_AXIS_NAME)){
            String st = str.substring(str.indexOf("=")+1).trim();
            if(st.equalsIgnoreCase(this.Y_AXIS_MEDIAN))
              yAxisValue = this.MEAN;
            else if(st.equalsIgnoreCase(this.Y_AXIS_STD_DEV))
              yAxisValue = this.STD_DEV;
            else if(st.equalsIgnoreCase(this.Y_AXIS_EXCEED_PROB))
              yAxisValue = this.EXCEED_PROB;
            else if(st.equalsIgnoreCase(this.Y_AXIS_IML_AT_PROB))
              yAxisValue = this.IML_AT_EXCEED_PROB;
          }
          /*
          reading the target result for the AttenuationRelationship with the
          given parameter setting and storing the actual result(benchmark) in the
          ArbitrarilyDiscretizedFunc , so that we can compare this target result
          with the result we will obtain with those parameter settings for that IMR.
          */
          else if(str.equalsIgnoreCase(this.START_RESULT_VALUES)){
            targetFunction =new ArbitrarilyDiscretizedFunc();
            str = (br.readLine()).trim();
            int i=0;
            //if we have started reading the target result keep reading it
            //until we have reached the ending comments
            while(str.equalsIgnoreCase(this.END_RESULT_VALUES)){
              targetFunction.set(i,(new Double(str)).doubleValue());
              ++i;
              str = (br.readLine()).trim();
              if(str.equalsIgnoreCase(""))
                str = (br.readLine()).trim();
            }
            // Get the Discretized Function - calculation done here
            DiscretizedFuncAPI function = getFunctionForXAxis( xAxisParam,yAxisValue  );
            //str = (br.readLine()).trim();
          }
          //reading the parameter names and their value from the file
          else{
            //when we get parameter , check to see which parameter it is actually
            //becuase its value will have to be set accordingly
            String st = str.substring(0,str.indexOf("=")).trim();

            //we only need to get the parameters whose names have been given in the
            //file, result of the params will be set with the default values
            ParameterAPI tempParam = list.getParameter(st);
            st = str.substring(str.indexOf("=")+1).trim();
            if(tempParam instanceof StringParameter)
              tempParam.setValue(st);
            if(tempParam instanceof DoubleParameter)
              tempParam.setValue(new Double(st));
            if(tempParam instanceof IntegerParameter)
              tempParam.setValue(new Integer(st));
          }
        }
        //reads the next line in the file
        str = (br.readLine()).trim();
      }
    }catch(Exception e){
      e.printStackTrace();
    }
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
  private DiscretizedFuncAPI getFunctionForXAxis( ParameterAPI xAxisParam, int type)
      throws ConstraintException {


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
        imr.setIntensityMeasure( SA_NAME );

        DataPoint2D point = new DataPoint2D( val.doubleValue(), getCalculation( type ));
        function.set( point );

      }

      // return to original state
      period.setValue( oldVal );
      imr.setIntensityMeasure(SA_NAME);

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
          TestMinMaxDeltaForAttenRel minmaxdelta =
              new TestMinMaxDeltaForAttenRel( (WarningParameterAPI)transParam );
          function = buildFunction( transParam, type, function, minmaxdelta );

        }
        else{
          // Calculate min and max values from constraint
          TestMinMaxDeltaForAttenRel minmaxdelta = new TestMinMaxDeltaForAttenRel( (WarningParameterAPI)xAxisParam );
          function = buildFunction( xAxisParam, type, function, minmaxdelta );
        }
      }
      else{
        // Calculate min and max values from constraint
        TestMinMaxDeltaForAttenRel minmaxdelta = new TestMinMaxDeltaForAttenRel( (WarningParameterAPI)xAxisParam );
        function = buildFunction( xAxisParam, type, function, minmaxdelta );
      }

    }

    // Constraint contains a min and a max
    else if ( ParamUtils.isDoubleConstraint( xAxisParam ) ) {

      // Calculate min and max values from constraint
      TestMinMaxDeltaForAttenRel minmaxdelta = new TestMinMaxDeltaForAttenRel( xAxisParam );
      function = buildFunction( xAxisParam, type, function, minmaxdelta );
    }

    else
      throw new ConstraintException( "Not supported as an independent parameter: " );

    return function;
  }



  private ArbDiscrFuncWithParams buildFunction(
      ParameterAPI xAxisParam,
      int type,
      ArbDiscrFuncWithParams function,
      TestMinMaxDeltaForAttenRel minmaxdelta ){

    // Fetch the independent variable selected in the x-axis choice
    ParameterAPI independentParam = imr.getParameter( xAxisParam.getName() );
    Object oldVal = independentParam.getValue();

    double val = minmaxdelta.getMin();
    int index=0;

    if( independentParam instanceof WarningDoubleParameter &&
        xAxisParam instanceof TranslatedWarningDoubleParameter){

      ((TranslatedWarningDoubleParameter)xAxisParam).setParameter(
          (WarningDoubleParameter)independentParam
          );


      while ( index < TestMinMaxDeltaForAttenRel.NUM ) {

        // if it's just beyond the max (due to numerical imprececion) make it the max
        if(val > minmaxdelta.getMax()) val = minmaxdelta.getMax();
        xAxisParam.setValue( new Double( val ) );
        DataPoint2D point = new DataPoint2D( val , getCalculation( type ) );
        function.set( point );
        val += minmaxdelta.getDelta();
        index++;
      }

    }
    else{

      while ( index < TestMinMaxDeltaForAttenRel.NUM ) {

        // if it's just beyond the max (due to numerical imprececion) make it the max
        if(val > minmaxdelta.getMax()) val = minmaxdelta.getMax();
        independentParam.setValue( new Double( val ) );
        DataPoint2D point = new DataPoint2D( val , getCalculation( type ) );
        function.set( point );
        val += minmaxdelta.getDelta();
        index++;
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

}