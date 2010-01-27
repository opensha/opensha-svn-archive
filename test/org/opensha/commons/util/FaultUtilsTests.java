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

package org.opensha.commons.util;

import junit.framework.TestCase;

import org.opensha.commons.util.FaultUtils;



public class FaultUtilsTests extends TestCase {

    public FaultUtilsTests(String s) {
    super(s);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testAssertValidStrike() {
    double strike1=  -1.0;
    try {
      FaultUtils.assertValidStrike(strike1);
      assertTrue("Should throw Exception with strike : " + strike1,false);
    }
    catch(Exception e)
    {
      // System.err.println("Exception thrown as Expected:  "+e);
      assertTrue(true);
    }
  }
}
