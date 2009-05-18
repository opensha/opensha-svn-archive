package org.opensha.commons.util.tests;

import junit.framework.*;

import org.opensha.commons.util.FaultUtils;
import org.opensha.util.*;


public class FaultUtilsTests extends TestCase {

    public FaultUtilsTests(String s) {
    super(s);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testAssertValidStrike() {
    double strike1=  -1.0;
    try {
      FaultUtils.assertValidStrike(strike1);
      assertTrue("Should throw Exception with strike : " + strike1,false);
    }
    catch(Exception e)
    {
      // System.err.println("Exception thrown as Expected:  "+e);
      assertTrue(true);
    }
  }
}
