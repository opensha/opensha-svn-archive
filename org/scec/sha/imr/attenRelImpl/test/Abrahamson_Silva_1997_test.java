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

  /**String to see if the user wants to output all the parameter setting for the all the test set
   * or wants to see only the failed test result values, with the default being only the failed tests
   **/
  private static String showParamsForTests = "fail"; //other option can be "both" to show all results

  private static final String RESULT_SET_PATH = "AttenRelResultSet/";
  private static final String ABRAHAMSON_1997_RESULTS = RESULT_SET_PATH +"Abrahamson_Silva1997TestData.txt";

  //Instance of the class that does the actual comparison for the AttenuationRelationship classes
  AttenRelResultsChecker attenRelChecker;

  public Abrahamson_Silva_1997_test(final String name) {
    super(name);
  }

  protected void setUp() {
    // create the instance of the AS_1997
    as_1997 = new AS_1997_AttenRel(this);
    attenRelChecker = new AttenRelResultsChecker(as_1997,ABRAHAMSON_1997_RESULTS, tolerence);
  }

  protected void tearDown() {
  }


  public void testAS1997_Creation() {

    boolean result =attenRelChecker.readResultFile();
    int testNumber;
    testNumber = attenRelChecker.getTestNumber();

    /**
     * If any test for the BJF failed
     */
      if(this.showParamsForTests.equalsIgnoreCase("fail") && result == false){
        Vector failedTestsVector = attenRelChecker.getFailedTestResultNumberList();
        int size = failedTestsVector.size();
        for(int i=0;i<size;++i){
          int failedTestNumber = ((Integer)failedTestsVector.get(i)).intValue();
          this.assertTrue("AS-1997 Test Failed for test Set-"+failedTestNumber+
          " with following set of params :\n"+(String)attenRelChecker.getControlParamsValueForAllTests().get(failedTestNumber -1)+
          (String)attenRelChecker.getIndependentParamsValueForAllTests().get(failedTestNumber -1),result);
        }
      }
      //if the user wants to see all the tests param values
      else if( this.showParamsForTests.equalsIgnoreCase("both")){
        Vector controlParams = attenRelChecker.getControlParamsValueForAllTests();
        Vector independentParams = attenRelChecker.getIndependentParamsValueForAllTests();
        int size = controlParams.size();
        for(int i=0;i<size;++i){
          this.assertNotNull("AS-1997 test Set-"+(i+1)+
          " with following set of params :\n"+(String)controlParams.get(i)+
          (String)independentParams.get(i),new Boolean(result));
        }
      }
      //if the all the succeeds and their is no fail for any test
      else {
        this.assertTrue("AS-1997 Test succeeded for all the test cases",result);
      }
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
   junit.swingui.TestRunner.run(Abrahamson_Silva_1997_test.class);
  }

}