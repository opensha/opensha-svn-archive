package org.scec.util.tests;

import junit.framework.*;
import org.scec.util.*;


public class TestFaultUtils extends TestCase {

  public TestFaultUtils(String s) {
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
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
    }
    catch(Exception e)
    {
      System.err.println("Exception thrown:  "+e);
      assertTrue(true);
    }
  }
}
