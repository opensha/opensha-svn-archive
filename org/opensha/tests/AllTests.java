
package org.opensha.tests;

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

    suite.addTest(org.opensha.util.tests.UtilSuite.suite());
    suite.addTest(org.opensha.data.tests.DataSuite.suite());
    suite.addTest(org.opensha.data.region.tests.RegionSuite.suite());
    suite.addTest(new TestSuite(org.opensha.sha.earthquake.rupForecastImpl.step.tests.STEPTests.class));
    return suite;

    // Example of how to add a testSuite and an individual test class into this method
    // suite.addTest(org.opensha.util.tests.UtilSuite.suite());
    // suite.addTest(new TestSuite(org.opensha.util.tests.FaultUtilsTests.class));
  }
}
