package org.opensha.commons.calc;

public class ArcsecondConverter {
	
	private static final double RADIANS_PER_ARC_SECOND = StrictMath.PI / 648000d;
	
	public static double getDegrees(double arcSeconds) {
		double radians = RADIANS_PER_ARC_SECOND * arcSeconds;
		return StrictMath.toDegrees(radians);
	}
	
	public static double getArcseconds(double degrees) {
		double radians = StrictMath.toRadians(degrees);
		return radians / RADIANS_PER_ARC_SECOND;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double degrees = getDegrees(30);
		double secs = getArcseconds(degrees);
		
		System.out.println("30 -> " + degrees + " -> " + secs);
	}

}
