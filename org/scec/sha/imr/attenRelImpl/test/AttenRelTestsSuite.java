package org.scec.sha.imr.attenRelImpl.test;


import junit.framework.*;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class AttenRelTestsSuite extends TestCase {


  /*
  private static final String SADIGH_1997_RESULTS = RESULT_SET_PATH +"Sadigh1997TestData.txt";*/

  public AttenRelTestsSuite(){
    super("AttenRel Tests");
  }

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new TestSuite(Abrahamson_2000_test.class));
    suite.addTest(new TestSuite(Abrahamson_Silva_1997_test.class));
    suite.addTest(new TestSuite(BJF_1997_test.class));
    suite.addTest(new TestSuite(CB_2003_test.class));
    return suite;
  }


  public static void main(String args[])
  {
    junit.swingui.TestRunner.run(AttenRelTestsSuite.class);
  }
}