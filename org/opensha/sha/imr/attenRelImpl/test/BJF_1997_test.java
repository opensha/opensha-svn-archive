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

  private static final String RESULT_SET_PATH = "org/opensha/sha/imr/attenRelImpl/test/AttenRelResultSetFiles/";
  private static final String BOORE_1997_RESULTS = RESULT_SET_PATH +"BOORE.txt";

  //Tolerence to check if the results fall within the range.
  private static double tolerence = .01; //default value for the tolerence

  /**String to see if the user wants to output all the parameter setting for the all the test set
   * or wants to see only the failed test result values, with the default being only the failed tests
   **/
  private static String showParamsForTests = "fail"; //other option can be "both" to show all results

  //Instance of the class that does the actual comparison for the AttenuationRelationship classes
  AttenRelResultsChecker attenRelChecker;


  public BJF_1997_test(final String name) {
    super(name);
  }

  protected void setUp() {
    // create the instance of the BJF_1997
    bjf_1997 = new BJF_1997_AttenRel(this);
    attenRelChecker = new AttenRelResultsChecker(bjf_1997,this.BOORE_1997_RESULTS,this.tolerence);
  }

  protected void tearDown() {
  }


  public void testBJF1997_Creation() {

    boolean result =attenRelChecker.readResultFile();

    /**
     * If any test for the BJF failed
     */
    if(result == false)
      this.assertNull(attenRelChecker.getFailedTestParamsSettings(),attenRelChecker.getFailedTestParamsSettings());

    //if the all the succeeds and their is no fail for any test
    else {
      this.assertTrue("BJF-1997 Test succeeded for all the test cases",result);
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
   junit.swingui.TestRunner.run(BJF_1997_test.class);
  }

}
