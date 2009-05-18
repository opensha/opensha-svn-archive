package org.opensha.commons.util.tests;

import junit.framework.*;

public class UtilSuite extends TestCase
{

    public UtilSuite(String s)
  {
      super(s);
  }

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new TestSuite(FaultUtilsTests.class));
    return suite;
  }

  public static void main(String args[])
  {
        junit.swingui.TestRunner.run(UtilSuite.class);
  }
}
