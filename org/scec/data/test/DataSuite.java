package org.scec.data.tests;

import junit.framework.*;

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
    return suite;
  }
  
  public static void main(String args[]) 
  {
        junit.swingui.TestRunner.run(DataSuite.class);
  }  
}