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
 * <p>Title:Abrahamson_Silva_1997_test </p>
 * <p>Description: Checks for the proper implementation of the AS_1997_AttenRel
 * class.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta & Vipin Gupta
 * @version 1.0
 */
public class Abrahamson_Silva_1997_test extends TestCase implements ParameterChangeWarningListener {


  AS_1997_AttenRel as_1997 = null;

  //Tolerence to check if the results fall within the range.
  private static double tolerence = .0001; //default value for the tolerence

  private static final String RESULT_SET_PATH = "AttenRelResultSet/";
  private static final String ABRAHAMSON_1997_RESULTS = RESULT_SET_PATH +"Abrahamson_Silva1997TestData.txt";


  public Abrahamson_Silva_1997_test(final String name) {
    super(name);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }


  public void testAbrahamson2000_Creation() {
    // create the instance of the AS_1997
    as_1997 = new AS_1997_AttenRel(this);
    AttenRelResultsChecker attenRelChecker = new AttenRelResultsChecker(as_1997,
                                                 ABRAHAMSON_1997_RESULTS, tolerence);
    attenRelChecker.readResultFile();
  }

  public void parameterChangeWarning(ParameterChangeWarningEvent e){
    return;
  }



  /**
   * Run the test case
   * @param args
   */

  public static void main (String[] args)
  {
    if(args.length !=0)
      tolerence=(new Double(args[0])).doubleValue();
    junit.swingui.TestRunner.run(Abrahamson_Silva_1997_test.class);
  }

}