
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
   
    suite.addTest(org.scec.util.tests.UtilSuite.suite());
    suite.addTest(org.scec.data.tests.DataSuite.suite());
    suite.addTest(org.scec.data.region.tests.RegionSuite.suite());
    suite.addTest(new TestSuite(org.scec.sha.earthquake.rupForecastImpl.step.tests.STEPTests.class));
    return suite;
    
    // Example of how to add a testSuite and an individual test class into this method
    // suite.addTest(org.scec.util.tests.UtilSuite.suite());
    // suite.addTest(new TestSuite(org.scec.util.tests.FaultUtilsTests.class));
  }
}
