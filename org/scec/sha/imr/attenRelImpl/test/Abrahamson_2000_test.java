package org.scec.sha.imr.attenRelImpl.test;


import junit.framework.*;
import java.util.*;

import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.imr.attenRelImpl.*;
import org.scec.sha.propagation.*;

import org.scec.exceptions.*;
import org.scec.data.function.*;
import org.scec.util.*;
import org.scec.data.*;

/**
 *
 * <p>Title:Abrahamson_2000_test </p>
 * <p>Description: Checks for the proper implementation of the Abrahamson_2000_AttenRel
 * class.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta & Vipin Gupta
 * @version 1.0
 */
public class Abrahamson_2000_test extends TestCase implements ParameterChangeWarningListener {


  Abrahamson_2000_AttenRel abrahamson_2000 = null;

  //static int values for the Y values in IMR
  public final static int MEAN = 1;
  public final static int STD_DEV = 2;
  public final static int EXCEED_PROB = 3;
  public final static int IML_AT_EXCEED_PROB = 4;

  private boolean translateIMR = true;

  public Abrahamson_2000_test(final String name) {
    super(name);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }


  public void testAbrahamson2000_Creation() {
    // create the instance of the Abrahamson_2000
    abrahamson_2000 = new Abrahamson_2000_AttenRel(this);
    ParameterAPI param = abrahamson_2000.getParameter("SA Period");
    abrahamson_2000.setParamDefaults();
    abrahamson_2000.getParameter(DistanceRupParameter.NAME).setValue(new Double(10.00));
    //abrahamson_2000.getParameter(Abrahamson_2000_AttenRel.SITE_TYPE_NAME).setValue(new String(Abrahamson_2000_AttenRel.SITE_TYPE_ROCK));
    abrahamson_2000.getParameter(Abrahamson_2000_AttenRel.MAG_NAME).setValue(new Double(7.5));
    /*abrahamson_2000.getParameter(Abrahamson_2000_AttenRel.X_NAME).setValue(new Double(0.00));
    abrahamson_2000.getParameter(Abrahamson_2000_AttenRel.THETA_NAME).setValue(new Double(90.00));*/
    abrahamson_2000.setIntensityMeasure(abrahamson_2000.SA_NAME);
    // Get the Discretized Function - calculation done here
    DiscretizedFuncAPI function = getFunctionForXAxis( param, this.STD_DEV );
    for(int i=0;i<function.getNum();++i)
    System.out.println("Function values("+i+"):"+function.getY(i));
  }

  public void parameterChangeWarning(ParameterChangeWarningEvent e){
    return;
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
      DoubleDiscreteParameter period = ( DoubleDiscreteParameter ) abrahamson_2000.getParameter( paramName );
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
        abrahamson_2000.setIntensityMeasure( abrahamson_2000.SA_NAME );

        DataPoint2D point = new DataPoint2D( val.doubleValue(), getCalculation( type ));
        function.set( point );

      }

      // return to original state
      period.setValue( oldVal );
      abrahamson_2000.setIntensityMeasure( abrahamson_2000.SA_NAME );

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


        ParameterAPI imrParam = (ParameterAPI)abrahamson_2000.getIntensityMeasure().clone();

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
    ParameterAPI independentParam = abrahamson_2000.getParameter( xAxisParam.getName() );
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
              result = Math.exp( abrahamson_2000.getMean() );
              break;
          case EXCEED_PROB:
              result = abrahamson_2000.getExceedProbability();
              break;
          case STD_DEV:
              result = abrahamson_2000.getStdDev();
              break;
          case IML_AT_EXCEED_PROB :
              result = Math.exp(abrahamson_2000.getIML_AtExceedProb());
              break;
      }
      return result;
  }

  /**
   * Run the test case
   * @param args
   */

  public static void main (String[] args)
  {
    junit.swingui.TestRunner.run(Abrahamson_2000_test.class);
  }

}