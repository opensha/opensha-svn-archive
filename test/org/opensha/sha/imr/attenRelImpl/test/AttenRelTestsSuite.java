/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
