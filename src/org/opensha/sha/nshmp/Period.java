package org.opensha.sha.nshmp;

import java.util.List;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

/**
 * Enum identifying different commonly used periods. This class provides the
 * period-dependent intensity measure levels for which hazard is calculated in
 * the NSHMP. The arrays are the x-values of any hazard curve and are sorted
 * ascending.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public enum Period {

	/** Ground motions for PGA. */
	GM0P00(0.00, Values.per0p00),
	/** Ground motions for SA at 0.1 sec. */
	GM0P10(0.10, Values.per0p10),
	/** Ground motions for SA at 0.2 sec. */
	GM0P20(0.20, Values.per0p20),
	/** Ground motions for SA at 0.3 sec. */
	GM0P30(0.30, Values.per0p30),
	/** Ground motions for SA at 0.5 sec. */
	GM0P50(0.50, Values.per0p50),
	/** Ground motions for SA at 0.75 sec. */
	GM0P75(0.75, Values.per0p75),
	/** Ground motions for SA at 1.0 sec. */
	GM1P00(1.00, Values.per1p00),
	/** Ground motions for SA at 2.0 sec. */
	GM2P00(2.00, Values.per2p00),
	/** Ground motions for SA at 3.0 sec. */
	GM3P00(3.00, Values.per3p00),
	/** Ground motions for SA at 4.0 sec. */
	GM4P00(4.00, Values.per4p00),
	/** Ground motions for SA at 5.0 sec. */
	GM5P00(5.00, Values.per5p00);

	private List<Double> imls;
	private double period;

	private Period(double period, double[] valueArray) {
		this.period = period;
		imls = ImmutableList.copyOf(Doubles.asList(valueArray));
	}

	/**
	 * Returns the list of ground motion values for this item.
	 * @return the ground motion value list
	 */
	public List<Double> getIMLs() {
		return imls;
	}

	/**
	 * Returns the numeric period for this <code>Period</code> identifier.
	 * @return the numeric period (in seconds)
	 */
	public double getValue() {
		return period;
	}

	/**
	 * Returns the <code>Period</code> for the supplied numeric period, or
	 * <code>null</code> is no matching <code>Period</code> exists.
	 * @param period to look up
	 * @return the corresonding <code>Period</code>
	 */
	public static Period valueForPeriod(double period) {
		for (Period p : Period.values()) {
			if (p.period == period) return p;
		}
		return null;
	}

	/**
	 * Returns a function with x-values initialized to values for this item.
	 * @return a function initialized with the values for this item
	 */
	public ArbitrarilyDiscretizedFunc getFunction() {
		ArbitrarilyDiscretizedFunc f = new ArbitrarilyDiscretizedFunc();
		for (double d : getIMLs()) {
			f.set(d, 1);
		}
		return f;
	}

	private static class Values {
		static double[] per0p00 = { 0.0050, 0.0070, 0.0098, 0.0137, 0.0192, 0.0269, 0.0376, 0.0527, 0.0738, 0.103, 0.145, 0.203, 0.284, 0.397, 0.556, 0.778, 1.09, 1.52, 2.13 };
		static double[] per0p10 = { 0.0025, 0.0060, 0.0098, 0.0137, 0.0192, 0.0269, 0.0376, 0.0527, 0.0738, 0.103, 0.145, 0.203, 0.284, 0.397, 0.556, 0.778, 1.09, 1.52, 2.13, 3.3 };
		static double[] per0p20 = { 0.0050, 0.0075, 0.0113, 0.0169, 0.0253, 0.0380, 0.0570, 0.0854, 0.128, 0.192, 0.288, 0.432, 0.649, 0.973, 1.46, 2.19, 3.28, 4.92, 7.38 };
		static double[] per0p30 = { 0.0025, 0.0050, 0.0075, 0.0113, 0.0169, 0.0253, 0.0380, 0.0570, 0.0854, 0.128, 0.192, 0.288, 0.432, 0.649, 0.973, 1.46, 2.19, 3.28, 4.92, 7.38 };
		static double[] per0p50 = per0p30;
		static double[] per0p75 = per0p10;
		static double[] per1p00 = { 0.0025, 0.00375, 0.00563, 0.00844, 0.0127, 0.0190, 0.0285, 0.0427, 0.0641, 0.0961, 0.144, 0.216, 0.324, 0.487, 0.730, 1.09, 1.64, 2.46, 3.69, 5.54 };
		static double[] per2p00 = per0p10;
		static double[] per3p00 = per0p10;
		static double[] per4p00 = per0p10;
		static double[] per5p00 = per0p10;
	}
}
