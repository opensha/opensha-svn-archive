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
 * <p>Title:BJF_1997_test </p>
 * <p>Description: Checks for the proper implementation of the BJF_1997_AttenRel
 * class.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta & Vipin Gupta
 * @version 1.0
 */
public class BJF_1997_test extends TestCase implements ParameterChangeWarningListener {


  BJF_1997_AttenRel bjf_1997 = null;

  private static final String RESULT_SET_PATH = "AttenRelResultSet/";
  private static final String BOORE_1997_RESULTS = RESULT_SET_PATH +"Boore1997TestData.txt";

  //Tolerence to check if the results fall within the range.
  private static double tolerence = .0001; //default value for the tolerence


  public BJF_1997_test(final String name) {
    super(name);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }


  public void testBJF1997_Creation() {
    // create the instance of the AS_1997
    bjf_1997 = new BJF_1997_AttenRel(this);
    AttenRelResultsChecker attenRelChecker = new AttenRelResultsChecker(bjf_1997,
                                                this.BOORE_1997_RESULTS,this.tolerence);
    boolean result =attenRelChecker.readResultFile();
    int testNumber;
    if(result == false){
      testNumber = attenRelChecker.getTestNumber();
      this.assertTrue("BJF Test Failed for following test Set-"+testNumber,result);
    }
    else
      this.assertTrue("BJF Passed all the test",result);
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
   junit.swingui.TestRunner.run(BJF_1997_test.class);
  }

}