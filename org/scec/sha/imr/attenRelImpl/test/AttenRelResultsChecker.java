package org.scec.sha.imr.attenRelImpl.test;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import org.scec.sha.imr.*;
import org.scec.param.*;
import org.scec.data.function.*;
import org.scec.exceptions.*;
import org.scec.util.*;
import org.scec.data.*;
import org.scec.sha.propagation.*;

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

  //Deciaml format to restrict the result to 6 places of decimal for the IMR computed values
  private DecimalFormat decimalFormat=new DecimalFormat("0.000000##");

  //this needs to read from the file for the different Intensity Measures,
  //but cuurently we only check for "SA"
  private final static String SA_NAME="SA";

  //checks to see if we need Log for IMT
  private boolean translateIMR = true;

  //checks if the resulted values lies within the this tolerence range
  private double tolerence = .0001; //default value for the tolerence

  //these are the comments in the file where the data results for the test result in that IMR starts and ends
  public static final String START_RESULT_VALUES = "#start of result values for this test set";
  public static final String END_RESULT_VALUES = "#end of result values for this test set";

  //stores the test number which we testing from the AttenRel metadata file
  int testNumber=0;

  //Vector to store the failed testCases
  private Vector testCaseNumberVector = new Vector();

  //Vector to store the ControlParams name and Value
  private Vector controlParamVector = new Vector();

  //Vector to store the IndependentParams name and Value
  private Vector independentParamVector = new Vector();


  //String to Stores the controls param with their values
  private String xControlName;
  private String yControlName;
  private String intensityMeasureName;


  public AttenRelResultsChecker(AttenuationRelationshipAPI imr, String file, double tolerence) {
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

    //It is temporary as right now we set the intensity measure to be only "SA"
    this.intensityMeasureName = this.SA_NAME;

    //set the tolernce value for that IMR
    this.tolerence = tolerence;

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
  public boolean readResultFile(){
    try{
      //which file to read for the AttenREl testcase
      FileReader fr = new FileReader(resultFile);
      BufferedReader br = new BufferedReader(fr);
      //read the first line in the file which is the name of the AttenuationRelationship and discard it
      //becuase we do nothing with it currently
      br.readLine();
      //arrayList to store the target IMR values for that test result set
      ArrayList targetFunction = null;
      //contains the value for the X-Axis
      ParameterAPI xAxisParam =null;
      // reads the first line in the file
      String str = (br.readLine()).trim();
      //stores the int value for the selected Y-axis param
      int yAxisValue =0;
      //keep reading the file until file pointer reaches the end of file.
      while(str !=null){
        str= str.trim();
        System.out.println("For("+testNumber+")"+str);

        //if the line contains nothing, just skip that line and read next
        if(str.equalsIgnoreCase(""))
          str = br.readLine();
        //if the line contains some data
        else{

          //if the String read is the X-Axis Param value in the file
          if(str.startsWith(this.X_AXIS_NAME)){
            String st = str.substring(str.indexOf("=")+1).trim();
            xAxisParam = imr.getParameter(st);

            //name and value for the X-Control Parameter
            xControlName = str;
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

            //name and value for the Y-Control Parameter
            this.yControlName = str;
          }
          /*
          reading the target result for the AttenuationRelationship with the
          given parameter setting and storing the actual result(benchmark) in the
          ArrayList , so that we can compare this target result
          with the result we will obtain with those parameter settings for that IMR.
          */
          else if(str.equalsIgnoreCase(this.START_RESULT_VALUES)){
            /*if(testNumber == 19)
              System.out.println("Hello");*/
            targetFunction =new ArrayList();
            str = (br.readLine()).trim();
            //System.out.println("For("+testNumber+"):"+str);
            int i=0;
            //if we have started reading the target result keep reading it
            //until we have reached the ending comments
            while(!str.equalsIgnoreCase(this.END_RESULT_VALUES)){
              System.out.println("For("+testNumber+"):"+(new Double(str.trim())).doubleValue());
              targetFunction.add((new Double(str.trim())));
              //System.out.println(i+":  "+targetFunction.getY(i));
              str = (br.readLine()).trim();
              if(str.equalsIgnoreCase(""))
                str = (br.readLine()).trim();
            }
            // Get the Discretized Function - calculation done here
            DiscretizedFuncAPI function = getFunctionForXAxis( xAxisParam,yAxisValue  );
            for(int j=0;j<function.getNum();++j)
              System.out.println("OpenSHA value for the test:"+testNumber+"; is:"+function.getY(j));
            //compare the computed result using SHA with the target result for the defined set of parameters
            boolean result =compareResults(function, targetFunction);
            //if the test was failure the add it to trhe test cases Vecotr that stores the values for  that failed
            if(result == false)
              this.testCaseNumberVector.add(new Integer(this.getTestNumber()));

            //adding the Control Param names and Value to Vector for all the test cases
            this.controlParamVector.add(this.getControlParametersValueForTest());
            //adding the Independent Param names and Value to Vector for all the test cases
            this.independentParamVector.add(this.getIndependentParametersValueForTest());
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
            //setting the value of the param based on which type it is: StringParameter,
            //DoubleParameter,IntegerParameter or WarningDoublePropagationEffectParameter(special parameter for propagation)
            if(tempParam instanceof StringParameter)
              tempParam.setValue(st);
            if(tempParam instanceof DoubleParameter)
              tempParam.setValue(new Double(st));
            if(tempParam instanceof IntegerParameter)
              tempParam.setValue(new Integer(st));
            if(tempParam instanceof WarningDoublePropagationEffectParameter) {
              ((WarningDoublePropagationEffectParameter)tempParam).setIgnoreWarning(true);
              tempParam.setValue(new Double(st));
            }
          }
          //reads the next line in the file
          str = br.readLine();
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }
    // test cases vector that contains the failed test number
    //if the size of this vector is not zero then return false(to make sure that some test did failed)
    if(this.testCaseNumberVector.size() >0)
      return false;
    return true;
  }

  /**
   * This function compares the values we obtained after running the values for
   * the IMR and the target Values( our benchmark)
   * @param function = values we got after running the OpenSHA code for the IMR
   * @param targetFunction = values we are comparing with to see if OpenSHA does correct calculation
   * @return
   */
  private boolean compareResults(DiscretizedFuncAPI function,
                                 ArrayList targetFunction){
    int num = function.getNum();
    if(num != targetFunction.size())
      return false;
    else{
      for(int i=0;i<num;++i){
        //value of the function that we obtained from the SHA code
        double val =(new Double(decimalFormat.format(function.getY(i)))).doubleValue();
        //comparing each value we obtained after doing the IMR calc with the target result
        //and making sure that values lies with the .1% range of the target values.
        double targetValue = ((Double)(targetFunction.get(i))).doubleValue();

        //comparing if the values lies within the actual tolerence range of the target result
        if(Math.abs(val-targetValue)<= this.tolerence)
          continue;
        else
          return false;
      }
    }
    return true;
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


  /**
   * This function returns the testNumber in the metadata file of the AttenuationRelationship
   * for which the test failed so that it specifies to the user which test occured in the
   * failure.
   * @returns the test number from the metadata for which test failed.
   */
  public int getTestNumber(){
    return this.testNumber;
  }

  /**
   *
   * @returns the name and value for the independent params setting for the test cases
   */
  private String getIndependentParametersValueForTest(){
    String independentParamValue="";

    ListIterator it  = list.getParametersIterator();
    while(it.hasNext()){
      ParameterAPI tempParam = (ParameterAPI)it.next();
      independentParamValue +=tempParam.getName()+" = "+tempParam.getValue()+"\n\t";
    }
    return independentParamValue +"\n\t";
  }

  /**
   *
   * @returns the name and Value for the control params setting of the test cases IMR
   */
  private String getControlParametersValueForTest(){
    return this.intensityMeasureName+";"+this.xControlName+";"+this.yControlName+"\n\t";
  }

  /**
   *
   * @returns the Vector that contains the Values for control param value for all the test cases
   */

  public Vector getControlParamsValueForAllTests(){
    return this.controlParamVector;
  }

  /**
   *
   * @returns the Vector that contains the Values for independent param value for all the test cases
   */
  public Vector getIndependentParamsValueForAllTests(){
    return this.independentParamVector;
  }

  /**
   *
   * @returns the Vector for the testCases number that failed
   */
  public Vector getFailedTestResultNumberList(){
    return this.testCaseNumberVector;
  }
}