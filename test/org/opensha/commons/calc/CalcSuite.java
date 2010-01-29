package org.opensha.commons.calc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	RelativeLocationTest.class,
	TestArcsecondConverter.class,
	TestBPT_DistCalcOld.class,
	TestFaultMomentCalc.class,
	TestFunctionListCalc.class,
	TestGaussianDistCalc.class
})

public class CalcSuite
{

	public static void main(String args[])
	{
		org.junit.runner.JUnitCore.runClasses(CalcSuite.class);
	}
}
