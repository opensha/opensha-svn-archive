package org.opensha.commons.calc;

import org.opensha.commons.util.TestUtils;

import junit.framework.TestCase;

public class TestFaultMomentCalc extends TestCase {

	public TestFaultMomentCalc(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetMoment() {
		assertEquals(FaultMomentCalc.getMoment(1, 1), 3e10);
		assertEquals(FaultMomentCalc.getMoment(1, 5), 1.5e11);
		assertEquals(FaultMomentCalc.getMoment(10, 1), 3e11);
		assertEquals(FaultMomentCalc.getMoment(10, 5), 1.5e12);
		assertEquals(FaultMomentCalc.getMoment(100, 1), 3e12);
		assertEquals(FaultMomentCalc.getMoment(100, 5), 1.5e13);
	}

	public void testGetSlip() {
		assertEquals(FaultMomentCalc.getSlip(1, 3e10), 1d);
		assertEquals(FaultMomentCalc.getSlip(1, 1.5e11), 5d);
		assertEquals(FaultMomentCalc.getSlip(10, 3e11), 1d);
		assertEquals(FaultMomentCalc.getSlip(10, 1.5e12), 5d);
		assertEquals(FaultMomentCalc.getSlip(100, 3e12), 1d);
		assertEquals(FaultMomentCalc.getSlip(100, 1.5e13), 5d);
	}
	
	public void testSlipFromMoment() {
		int tests = 0;
		for (double area=1.0; area<10000d; area*=1.25) {
			for (double slip=0.1; slip<10; slip+=0.1) {
				double moment = FaultMomentCalc.getMoment(area, slip);
				double calcSlip = FaultMomentCalc.getSlip(area, moment);
				assertEquals((float)slip, (float)calcSlip);
				tests++;
			}
		}
		System.out.println("Tested " + tests + " points");
	}
	
	public void testMomentFromSlip() {
		int tests = 0;
		for (double area=1.0; area<10000d; area*=1.25) {
			for (double moment=3e9; moment<10e15; moment*=1.25) {
				double slip = FaultMomentCalc.getSlip(area, moment);
				double calcMoment = FaultMomentCalc.getMoment(area, slip);
				assertTrue(TestUtils.getPercentDiff(calcMoment, moment) < 0.01);
				tests++;
			}
		}
		System.out.println("Tested " + tests + " points");
	}

}

