package org.opensha.sha.util;

import java.awt.geom.Point2D;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;

public class ComponentConversionUtil {
	
	/**
	 * RotD100/RotD50 factors as a function of period from Table 1 of Shahi & Baker 2014.
	 * 
	 * We convert from the natural log values provided in the table because they ahve more
	 * significant digits.
	 */
	private static DiscretizedFunc rotD100_over_RotD50;
	
	static {
		DiscretizedFunc ln_rotd100_over_rotd50 = new ArbitrarilyDiscretizedFunc();
		
		ln_rotd100_over_rotd50.set(0.01,	0.176);
		ln_rotd100_over_rotd50.set(0.02,	0.175);
		ln_rotd100_over_rotd50.set(0.03,	0.172);
		ln_rotd100_over_rotd50.set(0.05,	0.171);
		ln_rotd100_over_rotd50.set(0.075,	0.172);
		ln_rotd100_over_rotd50.set(0.1,		0.172);
		ln_rotd100_over_rotd50.set(0.15,	0.182);
		ln_rotd100_over_rotd50.set(0.2,		0.187);
		ln_rotd100_over_rotd50.set(0.25,	0.196);
		ln_rotd100_over_rotd50.set(0.3,		0.198);
		ln_rotd100_over_rotd50.set(0.4,		0.206);
		ln_rotd100_over_rotd50.set(0.5,		0.206);
		ln_rotd100_over_rotd50.set(0.75,	0.213);
		ln_rotd100_over_rotd50.set(1.0,		0.216);
		ln_rotd100_over_rotd50.set(1.5,		0.217);
		ln_rotd100_over_rotd50.set(2.0,		0.218);
		ln_rotd100_over_rotd50.set(3.0,		0.221);
		ln_rotd100_over_rotd50.set(4.0,		0.231);
		ln_rotd100_over_rotd50.set(5.0,		0.235);
		ln_rotd100_over_rotd50.set(7.5,		0.251);
		ln_rotd100_over_rotd50.set(10.0,	0.258);
		
		rotD100_over_RotD50 = new ArbitrarilyDiscretizedFunc();
		
		for (Point2D pt : ln_rotd100_over_rotd50)
			rotD100_over_RotD50.set(pt.getX(), Math.exp(pt.getY()));
	}
	
	/**
	 * @param period
	 * @return Linear RotD100/RotD50 scaling factor for the given period from Shahi & Baker 2014
	 */
	public static double getRotD100overRotD50Factor(double period) {
		return rotD100_over_RotD50.getInterpolatedY(period);
	}
	
	/**
	 * 
	 * @param period
	 * @param saRotD50 in the linear domain
	 * @return converted RotD100 value in the linear domain
	 */
	public static double convertRotD50toRotD100(double period, double saRotD50) {
		return saRotD50 * getRotD100overRotD50Factor(period);
	}
	
	/**
	 * @param period
	 * @param hazCurve RotD50 hazard curve
	 * @return Hazard curve where X values have been raplaced with RotD100 values
	 */
	public static DiscretizedFunc convertRotD50toRotD100(double period, DiscretizedFunc hazCurve) {
		return scaleXVals(getRotD100overRotD50Factor(period), hazCurve);
	}
	
	private static DiscretizedFunc scaleXVals(double ratio, DiscretizedFunc hazCurve) {
		ArbitrarilyDiscretizedFunc scaled = new ArbitrarilyDiscretizedFunc();
		scaled.setName(hazCurve.getName());
		scaled.setInfo(hazCurve.getInfo());
		
		for (Point2D pt : hazCurve)
			scaled.set(pt.getX()*ratio, pt.getY());
		
		return scaled;
	}

}
