package org.opensha.commons.data.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DataSuite extends TestCase
{

  public DataSuite(String s)
  {
      super(s);
  }

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new TestSuite(DataPoint2DTests.class));
    suite.addTest(new TestSuite(LocationTests.class));
    suite.addTest(new TestSuite(DataPoint2DTreeMapTests.class));
    suite.addTest(new TestSuite(TimeSpanTests.class));
    return suite;
  }

  public static void main(String args[])
  {
        junit.swingui.TestRunner.run(DataSuite.class);
  }
}
