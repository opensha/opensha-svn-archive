package org.opensha.sha.nshmp;

import static org.opensha.sha.nshmp.EpiUncert.*;
import static org.opensha.sha.nshmp.Period.*;
import static org.opensha.sha.imr.AttenRelRef.*;
import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultCode.*;

import java.awt.geom.Point2D;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math.util.MathUtils;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.eq.cat.util.MagnitudeType;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;
import org.opensha.commons.param.Parameter;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultCode;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.EqkRuptureParams.MagParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceJBParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.nshmp.imr.AB2006_140_AttenRel;
import org.opensha.sha.nshmp.imr.AB2006_140_AttenRel.StressDrop;
import org.opensha.sha.nshmp.imr.Campbell_2003_AttenRel;
import org.opensha.sha.nshmp.imr.FrankelEtAl_1996_AttenRel;
import org.opensha.sha.nshmp.imr.SilvaEtAl_2002_AttenRel;
import org.opensha.sha.nshmp.imr.SomervilleEtAl_2001_AttenRel;
import org.opensha.sha.nshmp.imr.TP2005_AttenRel;
import org.opensha.sha.nshmp.imr.ToroEtAl_1997_AttenRel;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

/**
 * Class wraps lookup tables that are used to speed up hazard calculations for
 * gridded sources. Internally, <code>Map</code>s are used to store
 * precalculated hazard curves.
 * 
 * @author Peter Powers
 * @version $Id$
 */
@Deprecated
public class RateTable {

	private static Joiner joiner = Joiner.on('-');
	// table tests??

	// [distance, magnitude, period, rate (for a given GM), epi branches?]

	// mega map that can be searched via:
	//
	// [distance, magnitude, period, uncert branch]
	//
	// internally distance and mag values are converted and/or scaled up to
	// integers to eliminate decimal precision errors
	// private Map<Integer, Map<Integer, Map<Period, Map<EpiUncert,
	// DiscretizedFunc>>>> curves;
	private Map<String, DiscretizedFunc> curves;

	private static int dR = 5;

	private static final double DTOR = 5.0; // rup top for CEUS gridded
	private static final double DTOR_SQ = DTOR * DTOR;

	private RateTable() {
		curves = Maps.newHashMap();
	}

	// /**
	// * Create an instance of a ground motion lookup table.
	// *
	// * @param R max distance
	// * @param dR distance increment
	// * @param nM number of magnitudes
	// * @param minM minimum magnitude
	// * @param dM magnitude increment
	// * @param periods of interest
	// * @param epi <code>true</code> if tables contain epistemic uncertainty,
	// * <code>false</code> otherwise
	// * @return a lookup table
	// */
	// public static RateTable create(int R, int dR, int nM, double minM,
	// double dM, Set<Period> periods, FaultCode magConv, ScalarIMR imr) {
	//
	// RateTable table = new RateTable();
	// table.dR = dR;
	//
	// for (Period p : periods) { // period
	// imr.setIntensityMeasure((p == GM0P00) ? PGA_Param.NAME
	// : SA_Param.NAME);
	// imr.getParameter(PeriodParam.NAME).setValue(p.getValue());
	// // System.out.println(imr.getParameter(PeriodParam.NAME).getValue());
	//
	// for (int j = 0; j < nM; j++) { // magnitude
	// double mag = minM + j * dM;
	// int mIdx = magKey(mag);
	// // mblgToMw passes thru if code is invalid
	// if (!(imr instanceof ToroEtAl_1997_AttenRel))
	// mag = Utils.mblgToMw(magConv, mag);
	// imr.getParameter(MagParam.NAME).setValue(mag);
	//
	// int numDistBins = R / dR;
	// for (int dIdx = 0; dIdx < numDistBins; dIdx++) { // distance
	// double rjb = (dIdx + 0.5) * dR;
	// // try to set rjb first
	// try {
	// Parameter<Double> rjbParam = imr
	// .getParameter(DistanceJBParameter.NAME);
	// rjbParam.setValue(rjb);
	// } catch (ParameterException pe1) {
	// // then try rRup
	// try {
	// Parameter<Double> rrupParam = imr
	// .getParameter(DistanceRupParameter.NAME);
	// rrupParam.setValue(Math.sqrt(rjb * rjb + DTOR_SQ));
	// } catch (ParameterException pe2) {
	// pe2.printStackTrace();
	// }
	// }
	//
	// DiscretizedFunc f = imr.getExceedProbabilities(p
	// .getFunction());
	// String key = table.createKey(dIdx, mIdx, p);
	// // System.out.println(key);
	// // System.out.println(f);
	// table.curves.put(key, f);
	//
	// } // distance
	// } // magnitude
	// } // period
	// return table;
	// }

	/**
	 * Create an instance of a ground motion lookup table.
	 * 
	 * @param R max distance
	 * @param dR distance increment
	 * @param nM number of magnitudes
	 * @param minM minimum magnitude
	 * @param dM magnitude increment
	 * @param periods of interest
	 * @param magConv magnitude conversion flag (Johnson oor AB)
	 * @param imrs map of IMRs and their respective weights
	 * @param epi <code>true</code> if tables contain epistemic uncertainty,
	 *        <code>false</code> otherwise
	 * @return a lookup table
	 */
	public static RateTable create(int R, int dR, int nM, double minM,
			double dM, Period p, FaultCode magConv,
			Map<ScalarIMR, Double> imrs) {

		RateTable table = new RateTable();
		table.dR = dR;

		for (ScalarIMR imr : imrs.keySet()) {
//			System.out.println(imr);
			imr.setParamDefaults();
			double imrWeight = imrs.get(imr);

//			for (Period p : periods) { // period
			imr.setIntensityMeasure((p == GM0P00) ? PGA_Param.NAME
				: SA_Param.NAME);
			imr.getParameter(PeriodParam.NAME).setValue(p.getValue());


			for (int j = 0; j < nM; j++) { // magnitude
				double mag = minM + j * dM;
				int mIdx = magKey(mag);
				
				// mblgToMw passes thru if code is invalid -- this currently
				// assumes we're working with CEUS
//					if (!(imr instanceof ToroEtAl_1997_AttenRel))
//						mag = Utils.mblgToMw(magConv, mag);
				
				if (magConv == M_CONV_AB || magConv == M_CONV_J) {
					if (imr instanceof ToroEtAl_1997_AttenRel) {
						// update hidden Toro param to use mblg coeffs
						imr.getOtherParams().getParameter("Magnitude Type")
							.setValue(MagnitudeType.LG_PHASE);
					} else {
						// otherwise convert mag to Mw for other imrs
						mag = Utils.mblgToMw(magConv, mag);
					}
				}					
				
				imr.getParameter(MagParam.NAME).setValue(mag);

				int numDistBins = R / dR;
				for (int dIdx = 0; dIdx < numDistBins; dIdx++) { // distance
					double rjb = (dIdx + 0.5) * dR;
					// try to set rjb first
					try {
						Parameter<Double> rjbParam = imr
							.getParameter(DistanceJBParameter.NAME);
						rjbParam.setValue(rjb);
					} catch (ParameterException pe1) {
						// then try rRup
						try {
							Parameter<Double> rrupParam = imr
								.getParameter(DistanceRupParameter.NAME);
							rrupParam.setValue(Math.sqrt(rjb * rjb +
								DTOR_SQ));
						} catch (ParameterException pe2) {
							pe2.printStackTrace();
						}
					}

					DiscretizedFunc f = imr.getExceedProbabilities(p
						.getFunction());

					f.scale(imrWeight);
					String key = table.createKey(dIdx, mIdx);
					if (table.curves.containsKey(key)) {
						Utils.addFunc(table.curves.get(key), f);
					} else {
						table.curves.put(key, f);
					}
				} // distance
			} // magnitude
		}
		return table;
	}

	/**
	 * Returns the hazard curve for the supplied distnance, magnitude, period,
	 * and mean ground motion epistemic uncertainty branch.
	 * @param d distance of interest
	 * @param M magnitude of interest
	 * @param p period of interest
	 * @return the corresponding curve
	 */
	public DiscretizedFunc get(double d, double M) {
//	public DiscretizedFunc get(double d, double M, Period p) {
//		System.out.println(curves.size());
//		System.out.println("d: " + d);
//		System.out.println("M: " + M);
//		System.out.println("p: " + p);
//		System.out.println("k: " + createKey(d, M, p));
//		System.out.println(curves.get(createKey(d, M, p)));
//		
//		for (String key : curves.keySet()) {
//		System.out.println(key);
//		}
//		return curves.get(createKey(d, M, p));
		return curves.get(createKey(d, M));
	}
	
	public int size() {
		return curves.size();
	}


	// NOTE this needs to find its way into CEUS erf
	// also neeed to move mag conversion out or imrs, for ceus erf will need
	// to create lookup tables for each mag conversion method

	public static void main(String[] args) {
		// Set<Period> periods = EnumSet.of(GM0P00, GM0P20, GM1P00, GM2P00);
		Set<Period> periods = EnumSet.of(GM0P00);

		Map<ScalarIMR, Double> imrs = Maps.newHashMap();

//		for (int i=0; i<200; i++) {
//			double rjb = (i + 0.5) * 5;
//			System.out.println(rjb);
//		}
		// XXXXX
		// ScalarIMR imr = new SomervilleEtAl_2001_AttenRel(null);
		// imr.setParamDefaults();
		// imr.getParameter("Mag. Conversion Method").setValue(FaultCode.M_CONV_AB);

//		ScalarIMR imr = FEA_1996.instance(null);
//		imr.setParamDefaults();

//		ScalarIMR imr = AB_2006_140.instance(null);
//		imr.setParamDefaults();

//		ScalarIMR imr = AB_2006_200.instance(null);
//		imr.setParamDefaults();
		

		// ScalarIMR imr = new Campbell_2003_AttenRel(null);
		// imr.setParamDefaults();

		// ScalarIMR imr = new TP2005_AttenRel(null);
		// imr.setParamDefaults();

		// ScalarIMR imr = new SilvaEtAl_2002_AttenRel(null);
		// imr.setParamDefaults();

		// ScalarIMR imr = new ToroEtAl_1997_AttenRel(null);
		// imr.setParamDefaults();

		// System.out.println(imr.getParameter("Magnitude Type").getValue());

//		imrs.put(imr, 1.0);
//		RateTable table = RateTable.create(120, 5, 24, 5.05, 0.1, periods,
//			FaultCode.M_CONV_AB, imrs);

		Map<ScalarIMR, Double> imrGrdMap = Maps.newHashMap();
		imrGrdMap.put(TORO_1997.instance(null), 0.25);
		imrGrdMap.put(FEA_1996.instance(null), 0.125);
		imrGrdMap.put(AB_2006_140.instance(null), 0.125);
		imrGrdMap.put(AB_2006_200.instance(null), 0.125);
		imrGrdMap.put(CAMPBELL_2003.instance(null), 0.125);
		imrGrdMap.put(TP_2005.instance(null), 0.125);
		imrGrdMap.put(SILVA_2002.instance(null), 0.125);

		double mMin = 5.05;
		int mNum = 25;
		int rMax = 1000;

		Stopwatch sw = new Stopwatch();
		sw.start();
		RateTable table = RateTable.create(rMax, 5, mNum, mMin, 0.1, Period.GM0P00,
			FaultCode.M_CONV_AB, imrGrdMap);
		sw.stop();
		System.out.println(table.curves.size());
		System.out.println(sw.elapsedTime(TimeUnit.MILLISECONDS));
		System.out.println(table.curves.get("2-655"));
		System.out.println(table.curves.get("0-505"));

//		DiscretizedFunc f = table.curves.get("2-735");
//		int len = 1000000;
//		List<Double> Rs = Lists.newArrayListWithCapacity(len);
//		List<Double> Ms = Lists.newArrayListWithCapacity(1000000);
//		for (int i=0; i<len; i++) {
////			Ms.add(mMin + mDelta * Math.random());
//			double m = MathUtils.round(mMin + Math.round(Math.random() * (mNum-1)) * 0.1, 2);
//			Ms.add(m);
//			Rs.add(rMax * Math.random());
//		}
//		double[] rArray = Doubles.toArray(Rs);
//		double[] mArray = Doubles.toArray(Ms);
//		System.out.println("rRange: " + Doubles.min(rArray) + " " + Doubles.max(rArray));
//		System.out.println("mRange: " + Doubles.min(mArray) + " " + Doubles.max(mArray));
//		
//		sw.reset().start();
//		for (int i=0; i<len; i++) {
//			try {
//				Utils.addFunc(f, table.get(rArray[i], mArray[i]));
//			} catch (NullPointerException npe) {
//				System.out.println("npe for: " + i + " " + rArray[i] + " " + mArray[i]);
//				System.out.println(createKey(rArray[i], mArray[i]));
//				break;
//			}
//		}
//		sw.stop();
//		System.out.println(sw.elapsedTime(TimeUnit.MILLISECONDS));

		
	}

	private static int distKey(double d, double interval) {
		return (int) (d / interval);
	}

	private static int magKey(double M) {
		return (int) Math.round(M * 100);
	}

	private void put(int dIdx, int mIdx, Period p, EpiUncert u,
			DiscretizedFunc f) {

	}

	// flavor used when initializing map
	private static String createKey(int rIdx, int mIdx) {
		return joiner.join(rIdx, mIdx);
	}

	// flavor used when fetching curves
	private static String createKey(double r, double m) {
		return joiner.join(distKey(r, dR), magKey(m));
	}

	// private static DiscretizedFunc getCurve(ScalarIMR imr, double dist,
	// double mag, Period per, double unc) {
	//
	// double std = imr.getStdDev();
	// double mean = imr.getMean();
	// DiscretizedFunc func = per.getFunction();
	//
	// Iterator<Point2D> it = func.getPointsIterator();
	// while (it.hasNext()) {
	// Point2D point = it.next();
	// double x = point.getX();
	// double y = Utils.getExceedProbability(mean, std, x,
	// GaussTruncation.ONE_SIDED, 3);
	// point.setLocation(x, y);
	// }
	//
	// return intensityMeasureLevels;
	//
	// return null;
	// }
}
