package org.opensha.commons.calc;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestArcsecondConverter {

	public TestArcsecondConverter() {
	}
	
	private static double globe_arc_secs = 3600 * 360;

	@Test
	public void testGetDegrees() {
		assertTrue(ArcsecondConverter.getDegrees(0d) == 0d);
		assertTrue(ArcsecondConverter.getDegrees(3600d) == 1d);
		assertTrue(ArcsecondConverter.getDegrees(globe_arc_secs * 0.5) == 180d);
		assertTrue(ArcsecondConverter.getDegrees(globe_arc_secs) == 360d);
	}

	@Test
	public void testGetArcseconds() {
		assertTrue(ArcsecondConverter.getArcseconds(0) == 0d);
		assertTrue(ArcsecondConverter.getArcseconds(1d) == 3600d);
		assertTrue(ArcsecondConverter.getArcseconds(180d) == globe_arc_secs * 0.5);
		assertTrue(ArcsecondConverter.getArcseconds(360d) == globe_arc_secs);
	}
	
	@Test
	public void testArcToDegToArc() {
		for (double arc=0; arc<=globe_arc_secs; arc+=0.5) {
			double deg = ArcsecondConverter.getDegrees(arc);
			double newArc = ArcsecondConverter.getArcseconds(deg);
			assertTrue((float)arc == (float)newArc);
		}
	}
	
	@Test
	public void testDegToArcToDeg() {
		for (double deg=0; deg<=360d; deg+=0.01) {
			double arc = ArcsecondConverter.getArcseconds(deg);
			double newDeg = ArcsecondConverter.getDegrees(arc);
			assertTrue((float)deg == (float)newDeg);
		}
	}

}
