
package org.scec.tests;

import junit.framework.*;

public class AllTests extends TestCase {

  public AllTests(String s) {
    super(s);
  }
  
  public static void main (String[] args) 
  {
    junit.swingui.TestRunner.run(AllTests.class);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(org.scec.util.tests.TestFaultUtils.class);
    return suite;
  }
}
