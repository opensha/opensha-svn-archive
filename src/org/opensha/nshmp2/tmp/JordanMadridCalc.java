package org.opensha.nshmp2.tmp;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.geo.Location;
import org.opensha.nshmp2.calc.HazardResult;
import org.opensha.nshmp2.erf.NSHMP2008;
import org.opensha.nshmp2.util.NSHMP_Utils;
import org.opensha.nshmp2.util.Period;

import scratch.peter.nga.Functions2;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Add comments here
 *
 * @author Peter Powers
 */
public class JordanMadridCalc {

	private static final Location MEMPHIS = new Location(36.0, -89.6);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		computeCurves();
//		computeFractiles();
		System.out.println(NSHMP_Utils.rateToProb(0.517117504, 1));
	}
	
	private static void computeCurves() {
		NSHMP2008 ceusERF = NSHMP2008.createCUES_Memphis();
		TimeSpan ts = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		ts.setDuration(1);
		ceusERF.setTimeSpan(ts);
		ceusERF.updateForecast();

		System.out.println(ceusERF.getSourceCount());
		System.out.println(ceusERF.getRuptureCount());

		Site site = new Site(MEMPHIS);
		Period p = Period.GM0P00;
		boolean epiUncert = false;

		JordanMadridHazardCalc hc = JordanMadridHazardCalc.create(ceusERF, site, p, epiUncert);
		HazardResult result = hc.call();
		System.out.println(result.curve());
		
		// this requires some post processing of curves to combine
		// the 750 and 1500 yr cluster model branches
	}
	
//	mean curve:
//		X, Y Data:
//	0.0050	0.042821225
//	0.0070	0.034725703
//	0.0098	0.02761235
//	0.0137	0.02153472
//	0.0192	0.01644407
//	0.0269	0.012380576
//	0.0376	0.009285903
//	0.0527	0.006982283
//	0.0738	0.005337124
//	0.103	0.0041923416
//	0.145	0.0033781463
//	0.203	0.0028220303
//	0.284	0.002420201
//	0.397	0.0020970835
//	0.556	0.0017851178
//	0.778	0.0014319386
//	1.09	0.0010087548
//	1.52	5.7102775E-4
//	2.13	2.1449673E-4

//	public static final String PATH = "tmp/forJordan/Memphis/Memphis_ALL.csv";
//	public static final String FRAC_PATH = "tmp/forJordan/Memphis/MemphisFractiles.csv";
	public static final String PATH = "tmp/forJordan/SanAndreas/SanAndreas_ALL.csv";
	public static final String FRAC_PATH = "tmp/forJordan/SanAndreas/SanAndreasFractiles.csv";
	private static final Splitter S = Splitter.on(',').omitEmptyStrings();

	public static void computeFractiles() {
		try {
		XY_DataSetList fList = new XY_DataSetList();
		List<Double> weights = Lists.newArrayList();
		
		File f = new File(PATH);
		List<String> lines = Files.readLines(f, Charsets.US_ASCII);
		System.out.println(lines.size());
		
		Iterable<Double> Xs = Iterables.transform(
			Iterables.skip(S.split(lines.get(0)), 2), Functions2.STR_2_DBL);
//		System.out.println(Iterables.toString(Xs));
		
		for (String line : Iterables.skip(lines, 1)) {

			Iterable<Double> values = Iterables.transform(
				Iterables.skip(S.split(line), 1), Functions2.STR_2_DBL);
			weights.add(Iterables.get(values, 0));
			Iterable<Double> Ys = Iterables.transform(Iterables.skip(values, 1), new ToProbFunction());
//			System.out.println(Iterables.toString(Iterables.skip(values, 1)));
			checkState(Iterables.size(Xs) == Iterables.size(Ys));
			
			ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
			Iterator<Double> yIt = Ys.iterator();
			for (double x : Xs) {
				func.set(x,  yIt.next());
			}
			fList.add(func);
			System.out.println(func);
		}
		
		File fracFile = new File(FRAC_PATH);
		writeFractiles(fracFile, fList, weights);
//		FractileCurveCalculator fcc = new FractileCurveCalculator(fList, weights);
//		
//		System.out.println(fcc.getMeanCurve());
//		System.out.println(fcc.getFractile(0.50));
		
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	
	private static void writeFractiles(File f, XY_DataSetList fList,
			List<Double> weights) {
		
		FractileCurveCalculator fcc = new FractileCurveCalculator(fList, weights);
		List<Double> Xs = fList.get(0).xValues();
		try {
			Files.write(createHeader(Xs), f, Charsets.US_ASCII);
		
			Files.append(createLine("mean", fcc.getMeanCurve().yValues()), f, Charsets.US_ASCII);

			Files.append(createLine("min", fcc.getMinimumCurve().yValues()), f, Charsets.US_ASCII);
			Files.append(createLine(" 2%", fcc.getFractile(0.02).yValues()), f, Charsets.US_ASCII);
			Files.append(createLine("16%", fcc.getFractile(0.16).yValues()), f, Charsets.US_ASCII);
			Files.append(createLine("50%", fcc.getFractile(0.50).yValues()), f, Charsets.US_ASCII);
			Files.append(createLine("84%", fcc.getFractile(0.84).yValues()), f, Charsets.US_ASCII);
			Files.append(createLine("98%", fcc.getFractile(0.98).yValues()), f, Charsets.US_ASCII);
			Files.append(createLine("max", fcc.getMaximumCurve().yValues()), f, Charsets.US_ASCII);
		
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	private static final Joiner J = Joiner.on(',').useForNull(" ");
	private static final String LF = "\n";

	private static String createHeader(List<Double> xs) {
		return createLine("frac", xs);
	}
	
	private static String createLine(String id, List<Double> ys) {
		List<String> headData = Lists.newArrayList(id);
		for (Double x : ys) {
			headData.add(String.format("%.8g", x));
		}
		headData.add(LF);
		return J.join(headData);
	}
	
	private static class ToProbFunction implements Function<Double, Double> {
		@Override
		public Double apply(Double annRate) {
			return NSHMP_Utils.rateToProb(annRate, 1);
		}
	}


}
