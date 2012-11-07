package scratch.peter.curves;

import static scratch.UCERF3.enumTreeBranches.DeformationModels.*;
import static scratch.UCERF3.enumTreeBranches.FaultModels.*;
import static scratch.UCERF3.enumTreeBranches.InversionModels.*;
import static scratch.UCERF3.enumTreeBranches.MaxMagOffFault.*;
import static scratch.UCERF3.enumTreeBranches.MomentRateFixes.*;
import static scratch.UCERF3.enumTreeBranches.ScalingRelationships.*;
import static scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels.*;
import static scratch.UCERF3.enumTreeBranches.SpatialSeisPDF.*;
import static scratch.UCERF3.enumTreeBranches.TotalMag5Rate.*;

import static com.google.common.base.Charsets.US_ASCII;
import static org.opensha.nshmp2.util.Period.*;
import static org.opensha.sra.rtgm.RTGM.Frequency.*;
import static scratch.peter.curves.ProbOfExceed.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.DataUtils;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.imr.NSHMP08_WUS;
import org.opensha.nshmp2.util.Period;
import org.opensha.sra.rtgm.RTGM;
import org.opensha.sra.rtgm.RTGM.Frequency;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.peter.nshmp.CurveContainer;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

/**
 * Utilities for organizing UC2 & UC3 logic tree branch results (hazard curves).
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class CurveUtils {

	private static final Splitter SPLIT = Splitter.on(',');
	private static final Joiner JOIN = Joiner.on(',');
	private static final Mean MEAN = new Mean();
	private static final String S = File.separator;
	private static final String LF = IOUtils.LINE_SEPARATOR;
	private static final List<String> STAT_FIELDS = Lists.newArrayList("stat",
		"2in50", "10in50", "rtgm");
	private static final List<String> SUMMARY_FIELDS = Lists.newArrayList("wt",
		"2in50", "10in50", "rtgm");
	private static final List<String> CITY_FIELDS = Lists.newArrayList("city",
		"2in50", "10in50", "rtgm");
	private static final String UC3_ROOT = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/";
	private static final String CURVE_FILE = "NSHMP08_WUS_curves";
	private static final String PARAM_FILE = "NSHMP08_WUS_params";

	public static void main(String[] args) {

		// generateFortranCityData();

//		 generateBranchSummaries();

//		try {
//			String src = "convABM_src";
//			String out = "convABM";
//			reorganizeUC3branchResults(src, out, true);
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
		
		generateBranchList();

	}

	/**
	 * UCERF3 logic tree reorganizer. Currently UC3 logic tree hazard curves
	 * computed for NEHRP test cities are grouped by logic tree branch; it is
	 * better to initialize a branch erf and loop location (cities) than to loop
	 * branch erf's at each location (city). This utility method groups curves
	 * for each city in a single file and writes statistical curve summaries.
	 * 
	 * @param src
	 * @param out
	 * @param ignoreWts
	 * @throws IOException
	 */
	public static void reorganizeUC3branchResults(String src, String out,
			boolean ignoreWts) throws IOException {
		// convert solutions grouped by branch to solutions grouped by city
		Set<Period> periods = EnumSet.of(GM0P00, GM0P20, GM1P00);

		BiMap<String, Integer> indexMap = HashBiMap.create();
		Map<Integer, Double> wtMap = Maps.newHashMap();

		File srcDir = new File(UC3_ROOT + src);
		File[] branchDirs = srcDir.listFiles();

		APrioriBranchWeightProvider wtProvider = new APrioriBranchWeightProvider();
		int index = 0;
		for (File branch : branchDirs) {
			if (!branch.isDirectory()) continue;
			String branchName = branch.getName();
			indexMap.put(branchName, index);
			LogicTreeBranch ltb = LogicTreeBranch.fromFileName(branchName);
			double wt = ignoreWts ? 1.0 : wtProvider.getWeight(ltb);
			wtMap.put(index, wt);
			index++;
		}

		// normalize weights
		if (!ignoreWts) {
			Collection<Double> wts = wtMap.values();
			double sum = DataUtils.sum(Doubles.toArray(wts));
			System.out.println("Weight sum: " + sum);
			for (int idx : wtMap.keySet()) {
				double wt = wtMap.get(idx);
				wtMap.put(idx, wt / sum);
			}
		}

		Map<Period, Table<Integer, NEHRP_TestCity, String>> curveMap = Maps
			.newHashMap();
		Table<Integer, NEHRP_TestCity, String> table = null;
		table = ArrayTable.create(indexMap.values(), NEHRP_TestCity.getCA());
		curveMap.put(GM0P00, table);
		table = ArrayTable.create(indexMap.values(), NEHRP_TestCity.getCA());
		curveMap.put(GM0P20, table);
		table = ArrayTable.create(indexMap.values(), NEHRP_TestCity.getCA());
		curveMap.put(GM1P00, table);

		for (File branch : branchDirs) {
			if (!branch.isDirectory()) continue;
			String branchName = branch.getName();
			int branchIdx = indexMap.get(branchName);
			double branchWt = wtMap.get(branchIdx);
			for (Period period : periods) {
				table = curveMap.get(period);
				File periodDir = new File(branch, period.name());
				File curveFile = new File(periodDir, CURVE_FILE + ".csv");
				List<String> lines = Files.readLines(curveFile, US_ASCII);
				for (String line : Iterables.skip(lines, 1)) {
					String cityStr = StringUtils.substringBefore(line, ",");
					NEHRP_TestCity city = NEHRP_TestCity.valueOf(cityStr);
					String curveStr = StringUtils.substringAfter(StringUtils
						.substringAfter(
							StringUtils.substringAfter(
								StringUtils.substringAfter(line, ","), ","),
							","), ",");
					String curveStrOut = branchIdx + "," + branchWt + "," +
						curveStr;
					table.put(branchIdx, city, curveStrOut);
				}
			}
		}

		for (Period period : periods) {
			table = curveMap.get(period);
			for (NEHRP_TestCity city : NEHRP_TestCity.getCA()) {

				File curvesOut = new File(UC3_ROOT + out, period.name() + S +
					city.name() + S + CURVE_FILE + ".csv");
				Files.createParentDirs(curvesOut);
				// header
				Iterable<String> gmVals = Collections2.transform(period
					.getFunction().xValues(), Functions.toStringFunction());
				List<String> headers = Lists.newArrayList("ERF#", "wt");
				Iterable<String> cityFields = Iterables.concat(headers, gmVals);
				String cityHeader = JOIN.join(cityFields) + LF;
				Files.write(cityHeader, curvesOut, US_ASCII);

				File paramsOut = new File(UC3_ROOT + out, period.name() + S +
					city.name() + S + PARAM_FILE + ".csv");
				Files.createParentDirs(paramsOut);
				Files.write("ERF#,BranchName" + LF, paramsOut, US_ASCII);

				// data
				Map<Integer, String> indexMapInverse = indexMap.inverse();
				for (int i = 0; i < indexMap.size(); i++) {
					String curveLine = table.get(i, city) + LF;
					String paramLine = i + "," + indexMapInverse.get(i) + LF;
					Files.append(curveLine, curvesOut, US_ASCII);
					Files.append(paramLine, paramsOut, US_ASCII);
				}
			}
		}
	}

	/**
	 * Utility method to create summaries of logic tree branch hazard curves.
	 */
	public static void generateBranchSummaries() {
		Iterable<Period> periods = EnumSet.of(GM0P00, GM0P20, GM1P00);
		Iterable<NEHRP_TestCity> cities = NEHRP_TestCity.getCA(); // EnumSet.of(VENTURA);
		String imrID = NSHMP08_WUS.SHORT_NAME;
		// String dir = "/Volumes/Scratch/rtgm/UCERF2-TimeIndep";
		String dir = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/convABM";
		try {
			runBranchSummaries(dir, imrID, periods, cities);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/*
	 * Create summaries of logic tree branch hazard curves.
	 */
	private static void runBranchSummaries(String dir, String imrID,
			Iterable<Period> periods, Iterable<NEHRP_TestCity> cities)
			throws IOException {
		for (Period p : periods) {
			for (NEHRP_TestCity c : cities) {
				File srcDir = new File(dir + S + p.name() + S + c.name());
				File srcFile = new File(srcDir, imrID + "_curves.csv");
				File statFile = new File(srcDir, imrID + "_curves_stats.csv");
				File sumFile = new File(srcDir, imrID + "_curves_summary.csv");
				summarizeBranches(srcFile, statFile, sumFile, p);
			}
		}
	}

	/*
	 * Reads an erf branch file that has columns eith wts, rtgm values and a
	 * hazard curve values and outputs one summary file with min, max, and mean
	 * data and another with just wt, pe2in50, and pe10in50.
	 */
	private static void summarizeBranches(File in, File stat, File summary,
			Period period) throws IOException {
		List<String> lines = Files.readLines(in, US_ASCII);
		List<Double> weights = Lists.newArrayList();
		List<Double> rtgms = Lists.newArrayList();
		List<Double> pe2in50s = Lists.newArrayList();
		List<Double> pe10in50s = Lists.newArrayList();
		XY_DataSetList curves = new XY_DataSetList();

		// create function: first line has ERF#, wt, rtgm, gm-vals ...
		Iterable<String> firstLine = SPLIT.split(lines.get(0));
		DiscretizedFunc curveModel = new ArbitrarilyDiscretizedFunc();
		for (String gmStr : Iterables.skip(firstLine, 2)) {
			double gmVal = Double.parseDouble(gmStr);
			curveModel.set(gmVal, 0.0);
		}

		for (String line : Iterables.skip(lines, 1)) {
			Iterable<String> vals = SPLIT.split(line);

			double weight = Double.parseDouble(Iterables.get(vals, 1));
			weights.add(weight);

			DiscretizedFunc curve = curveModel.deepClone();
			int idx = 0;
			for (String val : Iterables.skip(vals, 2)) {
				double annRate = Double.parseDouble(val);
				curve.set(idx++, annRate);
			}
			curves.add(curve);

			double pe2in50 = getPE(curve, PE2IN50);
			pe2in50s.add(pe2in50);

			double pe10in50 = getPE(curve, PE10IN50);
			pe10in50s.add(pe10in50);

			double rtgm = getRTGM(curve, period);
			rtgms.add(rtgm);
		}

		// write PEs
		Iterable<String> summaryFields = Iterables.concat(SUMMARY_FIELDS);
		String summaryHeader = JOIN.join(summaryFields) + LF;
		Files.write(summaryHeader, summary, US_ASCII);
		for (int i = 0; i < weights.size(); i++) {
			Iterable<String> lineData = createSummaryData(weights.get(i),
				pe2in50s.get(i), pe10in50s.get(i), rtgms.get(i));
			String summaryLine = JOIN.join(lineData) + LF;
			Files.append(summaryLine, summary, US_ASCII);
		}

		// calc and write stats
		double[] wtVals = Doubles.toArray(weights);
		double[] rtgmVals = Doubles.toArray(rtgms);
		double[] pe2in50Vals = Doubles.toArray(pe2in50s);
		double[] pe10in50Vals = Doubles.toArray(pe10in50s);

		double minRTGM = Doubles.min(rtgmVals);
		double maxRTGM = Doubles.max(rtgmVals);
		double meanRTGM = MEAN.evaluate(rtgmVals, wtVals);

		double min2in50 = Doubles.min(pe2in50Vals);
		double max2in50 = Doubles.max(pe2in50Vals);
		double mean2in50 = MEAN.evaluate(pe2in50Vals, wtVals);

		double min10in50 = Doubles.min(pe10in50Vals);
		double max10in50 = Doubles.max(pe10in50Vals);
		double mean10in50 = MEAN.evaluate(pe10in50Vals, wtVals);

		FractileCurveCalculator fcc = new FractileCurveCalculator(curves,
			weights);
		XY_DataSet minCurve = fcc.getMinimumCurve();
		XY_DataSet maxCurve = fcc.getMaximumCurve();
		XY_DataSet meanCurve = fcc.getMeanCurve();

		// header
		Iterable<String> statFields = Iterables.concat(STAT_FIELDS,
			Iterables.skip(firstLine, 2));
		String statHeader = JOIN.join(statFields) + LF;
		Files.write(statHeader, stat, US_ASCII);

		// mean
		Iterable<String> meanDat = createData("mean", mean2in50, mean10in50,
			meanRTGM, meanCurve);
		String meanLine = JOIN.join(meanDat) + LF;
		Files.append(meanLine, stat, US_ASCII);

		// min
		Iterable<String> minDat = createData("min", min2in50, min10in50,
			minRTGM, minCurve);
		String minLine = JOIN.join(minDat) + LF;
		Files.append(minLine, stat, US_ASCII);

		// max
		Iterable<String> maxDat = createData("max", max2in50, max10in50,
			maxRTGM, maxCurve);
		String maxLine = JOIN.join(maxDat) + LF;
		Files.append(maxLine, stat, US_ASCII);

	}

	private static double getPE(DiscretizedFunc f, ProbOfExceed pe) {
		return f.getFirstInterpolatedX_inLogXLogYDomain(pe.annualRate());
	}

	private static double getRTGM(DiscretizedFunc f, Period p) {
		if (!(p == GM0P20 || p == GM1P00)) return 0;
		Frequency freq = p.equals(GM0P20) ? SA_0P20 : SA_1P00;
		RTGM rtgm = RTGM.create(f, freq, 0.8).call();
		return rtgm.get();
	}

	private static Iterable<String> createData(String label, double pe2in50,
			double pe10in50, double rtgm, XY_DataSet curve) {

		Iterable<String> intercepts = Lists.newArrayList(label,
			Double.toString(pe2in50), Double.toString(pe10in50),
			Double.toString(rtgm));

		Iterable<String> values = Collections2.transform(curve.yValues(),
			Functions.toStringFunction());

		return Iterables.concat(intercepts, values);
	}

	private static Iterable<String> createSummaryData(double wt,
			double pe2in50, double pe10in50, double rtgm) {
		Iterable<String> values = Lists.newArrayList(Double.toString(wt),
			Double.toString(pe2in50), Double.toString(pe10in50),
			Double.toString(rtgm));
		return values;
	}

	/*
	 * Mine fortran results for curves and exceedance ground motions at
	 * specified NEHRP test cities.
	 */

	public static void generateFortranCityData() {
		String srcPath = "/Volumes/Scratch/nshmp-sources/FortranUpdate";
		String outPath = "/Volumes/Scratch/rtgm/FortranUpdate";

		Iterable<Period> periods = EnumSet.of(GM0P00, GM0P20, GM1P00);
		Iterable<NEHRP_TestCity> cities = NEHRP_TestCity.getCA(); // EnumSet.of(VENTURA);

		for (Period p : periods) {
			File src = new File(srcPath + S + p.name() + S + "curves.dat");
			File out = new File(outPath + S + p.name() + S + "FORT_curves.csv");
			try {
				Files.createParentDirs(out);
				deriveCityData(cities, src, out, p);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private static void deriveCityData(Iterable<NEHRP_TestCity> cities,
			File src, File out, Period period) throws IOException {

		// header
		Iterable<String> gmVals = Collections2.transform(period.getFunction()
			.xValues(), Functions.toStringFunction());
		Iterable<String> cityFields = Iterables.concat(CITY_FIELDS, gmVals);
		String cityHeader = JOIN.join(cityFields) + LF;
		Files.write(cityHeader, out, US_ASCII);

		// extract data
		CurveContainer cc = CurveContainer.create(src);
		for (NEHRP_TestCity city : cities) {
			Location loc = city.location();
			DiscretizedFunc curve = cc.getCurve(loc);
			double pe2in50 = getPE(curve, PE2IN50);
			double pe10in50 = getPE(curve, PE10IN50);
			double rtgm = getRTGM(curve, period);
			Iterable<String> cityData = createData(city.name(), pe2in50,
				pe10in50, rtgm, curve);
			String cityLine = JOIN.join(cityData) + LF;
			Files.append(cityLine, out, US_ASCII);
		}
	}
	
	private static void generateBranchList() {
		
		String fileName = "ref32Single";
		Set<FaultModels> fltModels = EnumSet.of(
			FM3_2); // FM3_1, FM3_2);
		Set<DeformationModels> defModels = EnumSet.of(
//			ABM, ZENG);
			ABM, GEOLOGIC, NEOKINEMA, ZENG);
		Set<ScalingRelationships> scalingRel = EnumSet.of(
//			ELLSWORTH_B, SHAW_2009_MOD);
			ELLSWORTH_B, ELLB_SQRT_LENGTH, HANKS_BAKUN_08,
			SHAW_CONST_STRESS_DROP, SHAW_2009_MOD);
		Set<SlipAlongRuptureModels> slipRup = EnumSet.of(
			TAPERED); //UNIFORM, TAPERED);
		Set<InversionModels> invModels = EnumSet.of(
			CHAR_CONSTRAINED);
		Set<TotalMag5Rate> totM5rate = EnumSet.of(
			RATE_8p7); //RATE_7p6, RATE_8p7, RATE_10p0);
		Set<MaxMagOffFault> mMaxOff = EnumSet.of(
			MAG_7p6); // MAG_7p2, MAG_7p6, MAG_8p0);
		Set<MomentRateFixes> momentFix = EnumSet.of(
			NONE);
		Set<SpatialSeisPDF> spatialSeis = EnumSet.of(
			UCERF3); // UCERF2, UCERF3);

		List<Set<? extends LogicTreeBranchNode<?>>> branchSets = Lists.newArrayList();
		branchSets.add(fltModels);
		branchSets.add(defModels);
		branchSets.add(scalingRel);
		branchSets.add(slipRup);
		branchSets.add(invModels);
		branchSets.add(totM5rate);
		branchSets.add(mMaxOff);
		branchSets.add(momentFix);
		branchSets.add(spatialSeis);
		
		int count = 0;
		Set<List<LogicTreeBranchNode<?>>> branches = Sets.cartesianProduct(branchSets);
		try {
		File out = new File("tmp/invSolSets", fileName + ".txt");
		Files.write("", out, US_ASCII);
		for (List<LogicTreeBranchNode<?>> branch : branches) {
			LogicTreeBranch ltb = LogicTreeBranch.fromValues(branch);
			Files.append(ltb.buildFileName() + LF, out, US_ASCII);
			System.out.println((count++) + " " + ltb.buildFileName());
		}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
