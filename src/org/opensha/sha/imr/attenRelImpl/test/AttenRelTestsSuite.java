package org.opensha.sha.imr.attenRelImpl.test;


import javax.swing.UIManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class AttenRelTestsSuite extends TestCase {


  //Look and Feel for the Mac
  static{
  try { UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName()); }
  catch ( Exception e ) {}
  }

  public AttenRelTestsSuite(){
    super("AttenRel Tests");
  }

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new TestSuite(Spudich_1999_test.class));
    suite.addTest(new TestSuite(Shakemap_2003_test.class));
    suite.addTest(new TestSuite(Abrahamson_2000_test.class));
    suite.addTest(new TestSuite(Abrahamson_Silva_1997_test.class));
    suite.addTest(new TestSuite(BJF_1997_test.class));
    suite.addTest(new TestSuite(CB_2003_test.class));
    suite.addTest(new TestSuite(SCEMY_1997_test.class));
    suite.addTest(new TestSuite(Campbell_1997_test.class));
    suite.addTest(new TestSuite(Field_2000_test.class));
    suite.addTest(new TestSuite(AS_2008_test.class));
    suite.addTest(new TestSuite(BA_2008_test.class));
    suite.addTest(new TestSuite(CB_2008_test.class));
    suite.addTest(new TestSuite(CY_2008_test.class));
    suite.addTest(new TestSuite(NGA08_Site_EqkRup_Tests.class));
    return suite;
  }

  public static void main(String args[])
  {
    junit.swingui.TestRunner.run(AttenRelTestsSuite.class);
  }
}
