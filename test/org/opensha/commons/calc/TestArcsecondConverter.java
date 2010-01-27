package org.opensha.commons.calc;

import junit.framework.TestCase;

public class TestArcsecondConverter extends TestCase {

	public TestArcsecondConverter(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	private static double globe_arc_secs = 3600 * 360;

	public void testGetDegrees() {
		assertEquals(ArcsecondConverter.getDegrees(0d), 0d);
		assertEquals(ArcsecondConverter.getDegrees(3600d), 1d);
		assertEquals(ArcsecondConverter.getDegrees(globe_arc_secs * 0.5), 180d);
		assertEquals(ArcsecondConverter.getDegrees(globe_arc_secs), 360d);
	}

	public void testGetArcseconds() {
		assertEquals(ArcsecondConverter.getArcseconds(0), 0d);
		assertEquals(ArcsecondConverter.getArcseconds(1d), 3600d);
		assertEquals(ArcsecondConverter.getArcseconds(180d), globe_arc_secs * 0.5);
		assertEquals(ArcsecondConverter.getArcseconds(360d), globe_arc_secs);
	}
	
	public void testArcToDegToArc() {
		for (double arc=0; arc<=globe_arc_secs; arc+=0.5) {
			double deg = ArcsecondConverter.getDegrees(arc);
			double newArc = ArcsecondConverter.getArcseconds(deg);
			assertEquals((float)arc, (float)newArc);
		}
	}
	
	public void testDegToArcToDeg() {
		for (double deg=0; deg<=360d; deg+=0.01) {
			double arc = ArcsecondConverter.getArcseconds(deg);
			double newDeg = ArcsecondConverter.getDegrees(arc);
			assertEquals((float)deg, (float)newDeg);
		}
	}

}
