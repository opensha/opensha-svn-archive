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

package org.opensha.commons.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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

    suite.addTest(org.opensha.commons.util.UtilSuite.suite());
    suite.addTest(org.opensha.commons.data.DataSuite.suite());
    //suite.addTest(org.opensha.commons.data.region.tests.RegionSuite.suite());
    suite.addTest(new TestSuite(org.opensha.sha.earthquake.rupForecastImpl.step.tests.STEPTests.class));
    return suite;

    // Example of how to add a testSuite and an individual test class into this method
    // suite.addTest(org.opensha.util.tests.UtilSuite.suite());
    // suite.addTest(new TestSuite(org.opensha.util.tests.FaultUtilsTests.class));
  }
}
