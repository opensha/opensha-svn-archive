package org.opensha.commons.calc;

import junk.bpt.TestBPT_DistCalcOld;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.opensha.commons.geo.LocationUtilsTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	LocationUtilsTest.class,
	TestFaultMomentCalc.class,
	TestFunctionListCalc.class,
	TestGaussianDistCalc.class,
	TestFractileCurveCalculator.class,
	TestMomentMagCalc.class
})

public class CalcSuite
{

	public static void main(String args[])
	{
		org.junit.runner.JUnitCore.runClasses(CalcSuite.class);
	}
}
