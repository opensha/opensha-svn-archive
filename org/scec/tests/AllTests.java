
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
    // Example of how to add an individual test class into this method
    // suite.addTest(new TestSuite(org.scec.util.tests.FaultUtilsTests.class));
    suite.addTest(org.scec.util.tests.UtilSuite.suite());
    suite.addTest(org.scec.data.tests.DataSuite.suite());
    suite.addTest(org.scec.data.region.tests.RegionSuite.suite());
    return suite;
  }
}
