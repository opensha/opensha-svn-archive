package scratchJavaDevelopers.matt.tests;

import org.opensha.util.tests.FaultUtilsTests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {
	 public static Test suite() {
		    TestSuite suite = new TestSuite();
		    suite.addTest(new TestSuite(STEP_mainTest.class));
		    suite.addTest(new TestSuite(BackGroundRatesGridTest.class));
		    return suite;	
		  }

}
