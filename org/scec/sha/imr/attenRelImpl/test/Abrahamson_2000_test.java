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

  private static final String RESULT_SET_PATH = "/AttenRelResultSet/";
  private static final String ABRAHAMSON_SILVA_1997_RESULTS = RESULT_SET_PATH +"Abrahamson-Silva1997TestData.txt";


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
    AttenRelResultsChecker attenRelChecker = new AttenRelResultsChecker(abrahamson_2000,this.ABRAHAMSON_SILVA_1997_RESULTS);
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
    junit.swingui.TestRunner.run(Abrahamson_2000_test.class);
  }

}