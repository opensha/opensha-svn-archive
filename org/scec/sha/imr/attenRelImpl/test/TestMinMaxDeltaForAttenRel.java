package org.scec.sha.imr.attenRelImpl.test;


import java.util.*;

import org.scec.param.*;
import org.scec.exceptions.*;
import org.scec.util.*;


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
class TestMinMaxDeltaForAttenRel {

  /**
   *  Description of the Field
   */
  private double min;
  /**
   *  Description of the Field
   */
  private double max;
  /**
   *  Description of the Field
   */
  private double delta;
  /**
   *  Description of the Field
   */
  private final static String C = "MinMaxDelta";

  /**
   *  Number of points to calculate between x-axis min and x-axis max, i.e.
   *  the constraint range of the choosen x-axis independent variable
   */
  public final static int NUM = 101;

  /**
   *  Constructor for the MinMaxDelta object
   *
   * @param  param                    Description of the Parameter
   * @exception  ConstraintException  Description of the Exception
   */
  public TestMinMaxDeltaForAttenRel( ParameterAPI param ) throws ConstraintException {

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
        min = ((Double)param1.getWarningMin()).doubleValue();
        max = ((Double)param1.getWarningMax()).doubleValue();
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
  public TestMinMaxDeltaForAttenRel( WarningParameterAPI param ) throws ConstraintException{
    // Determine min and max ranges with which to iterate over
    min = 0;
    max = 1;


    // Also handles subclasses such as TranslatedWarningDoubleParameters */
    if( param instanceof TranslatedWarningDoubleParameter){

      try{
        TranslatedWarningDoubleParameter param1 = (TranslatedWarningDoubleParameter)param;
        min = ((Double)param1.getWarningMin()).doubleValue();
        max = ((Double)param1.getWarningMax()).doubleValue();
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

  /**
   *
   * @returns the min
   */
  public  double getMin(){
    return min;
  }


  /**
   *
   * @returns the min
   */
  public  double getMax(){
    return max;
  }


  /**
   *
   * @returns the min
   */
  public  double getDelta(){
    return delta;
  }
}
