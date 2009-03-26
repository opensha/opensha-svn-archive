package org.opensha.sha.imr.attenRelImpl.test;


import junit.framework.*;
import java.util.*;

import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.imr.attenRelImpl.*;


import org.opensha.exceptions.*;
import org.opensha.data.function.*;
import org.opensha.util.*;
import org.opensha.data.*;

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
  private static double tolerence = .01; //default value for the tolerence

  /**String to see if the user wants to output all the parameter setting for the all the test set
   * or wants to see only the failed test result values, with the default being only the failed tests
   **/
  private static String showParamsForTests = "fail"; //other option can be "both" to show all results

  private static final String RESULT_SET_PATH = "org/opensha/sha/imr/attenRelImpl/test/AttenRelResultSetFiles/";
  private static final String ABRAHAMSON_1997_RESULTS = RESULT_SET_PATH +"AS1997.txt";

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

    /**
     * If any test for the AS-1997 failed
     */
    if(result == false)
      this.assertNull(attenRelChecker.getFailedTestParamsSettings(),attenRelChecker.getFailedTestParamsSettings());

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
