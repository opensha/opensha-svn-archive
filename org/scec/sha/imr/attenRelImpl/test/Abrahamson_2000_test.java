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
  //Tolerence to check if the results fall within the range.
  private static double tolerence = .0001; //default value for the tolerence


  private static final String RESULT_SET_PATH = "AttenRelResultSet/";
  private static final String ABRAHAMSON_2000_RESULTS = RESULT_SET_PATH +"Abrahamson2000TestData.txt";


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
    AttenRelResultsChecker attenRelChecker = new
        AttenRelResultsChecker(abrahamson_2000,ABRAHAMSON_2000_RESULTS, tolerence);
    boolean result =attenRelChecker.readResultFile();
    int testNumber;
    if(result == false){
      testNumber = attenRelChecker.getTestNumber();
      this.assertTrue("Abrahamson-2000 Test Failed for following test Set-"+testNumber,result);
    }
    else
      this.assertTrue("Abrahamson-2000 Passed all the test",result);
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
      tolerence=(new Double(args[0].trim())).doubleValue();
    junit.swingui.TestRunner.run(Abrahamson_2000_test.class);
  }

}