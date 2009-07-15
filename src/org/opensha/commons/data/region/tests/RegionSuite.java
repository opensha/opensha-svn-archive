package org.opensha.commons.data.region.tests;

import junit.framework.*;

public class RegionSuite extends TestCase
{

    public RegionSuite(String s)
  {
      super(s);
  }

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new TestSuite(GeographicRegionTests.class));
    suite.addTest(new TestSuite(RectangularGeoRegionTests.class));
    return suite;
  }

  public static void main(String args[])
  {
        junit.swingui.TestRunner.run(RegionSuite.class);
  }
}
