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
 * <p>Title:CB_2003_test </p>
 * <p>Description: Checks for the proper implementation of the CB_2003_AttenRel
 * class.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta & Vipin Gupta
 * @version 1.0
 */
public class CB_2003_test extends TestCase implements ParameterChangeWarningListener {


  CB_2003_AttenRel cb_2003 = null;
  //Tolerence to check if the results fall within the range.
  private static double tolerence = .0001; //default value for the tolerence


  private static final String RESULT_SET_PATH = "AttenRelResultSet/";
  private static final String CB_2003_RESULTS = RESULT_SET_PATH +"Cambell_Bozorgnia2003TestData.txt";


  public CB_2003_test(final String name) {
    super(name);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }


  public void testCB2003_Creation() {
    // create the instance of the AS_1997
    cb_2003 = new CB_2003_AttenRel(this);
    AttenRelResultsChecker attenRelChecker = new AttenRelResultsChecker(cb_2003,CB_2003_RESULTS, tolerence);
    boolean result =attenRelChecker.readResultFile();
    int testNumber;
    if(result == false){
     testNumber = attenRelChecker.getTestNumber();
     this.assertTrue("CB-2003 Test Failed for following test Set-"+testNumber,result);
   }
   else
      this.assertTrue("CB-2003 Passed all the test",result);
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
    System.out.println("Tolerence :"+tolerence);
    junit.swingui.TestRunner.run(CB_2003_test.class);
  }

}