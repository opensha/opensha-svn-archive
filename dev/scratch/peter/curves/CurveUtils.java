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
import static org.opensha.nshmp.NEHRP_TestCity.*;
import static com.google.common.base.Charsets.US_ASCII;
import static com.google.common.base.Preconditions.checkArgument;
import static org.opensha.nshmp2.util.Period.*;
import static org.opensha.sra.rtgm.RTGM.Frequency.*;
import static scratch.peter.curves.ProbOfExceed.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
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
import org.opensha.nshmp2.calc.UC3_CalcDriver;
import org.opensha.nshmp2.calc.UC3_CalcWrapper;
import org.opensha.nshmp2.imr.NSHMP08_WUS;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sra.rtgm.RTGM;
import org.opensha.sra.rtgm.RTGM.Frequency;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
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
import com.google.common.primitives.Chars;
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
	private static final Joiner TAB_JOIN = Joiner.on('\t');
	
	
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

	public static void main(String[] args) throws IOException {

//		 generateFortranCityData();

//		String srcPath = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/tree/SRP1440";
//		String locFile = srcPath + "/SRPsites.txt";
//		String curveDir = srcPath + "/reduce";
//		generateBranchSummaries2(locFile, curveDir, true);
		
//		String srcPath = "/Users/pmpowers/projects/OpenSHA/tmp/hazard/";
//		String locFile = srcPath + "sites.txt";
//		String curveDir = srcPath + "NEHRP-PBR-SRP/UC2-TimeIndep";
//		generateBranchSummaries2(locFile, curveDir, false);

		String srcPath = "/Users/pmpowers/projects/OpenSHA/tmp/hazard/";
		String locFile = srcPath + "sites.txt";
		String curveDir = srcPath + "NEHRP-PBR-SRP/UC3";
		generateBranchSummaries2(locFile, curveDir, true);

//		String treePath = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/tree/SRP1440";
//		File srcDir = new File(treePath + "/src");
//		File outDir = new File(treePath + "/reduce");
//		File locFile = new File(treePath + "/SRPsites.txt");
//		reorganizeUC3branchResults(srcDir, outDir, locFile, false);
			
//		String treePath = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/tree/PBR1440";
//		File srcDir = new File(treePath + "/src");
//		File outDir = new File(treePath + "/reduce");
//		File locFile = new File(treePath + "/PBRsites.txt");
//		reorganizeUC3branchResults(srcDir, outDir, locFile, false);

//		File srcDir = new File(UC3_ROOT + "convVar0/src");
//		File outDir = new File(UC3_ROOT + "convVar0");
//		File locFile = new File("/Users/pmpowers/projects/OpenSHA/tmp/curves/sites/SRPsites1.txt");
//		reorganizeUC3branchResults(srcDir, outDir, locFile, true);
		
//		File srcDir = new File(UC3_ROOT + "tree_src/PalmdaleTree");
//		File outDir = new File(UC3_ROOT + "PalmdaleTree");
//		File locFile = new File("/Users/pmpowers/projects/OpenSHA/tmp/curves/sites/palm.txt");
//		reorganizeUC3branchResults(srcDir, outDir, locFile, false);

//		generateBranchList();

//		fix10in50s();
		
//		listAllBranches();
//		 writeConvTestMags();
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
	public static void reorganizeUC3branchResults(File srcDir, File outDir,
			File locFile, boolean ignoreWts) throws IOException {
		
		// convert solutions grouped by branch to solutions grouped by city
		Set<Period> periods = EnumSet.of(GM0P00, GM0P20, GM1P00);
		
		// create location list
		List<String> locLines = Files.readLines(locFile, US_ASCII);
		List<String> locNames = Lists.newArrayList();
		for (String line : locLines) {
			locNames.add(Iterables.get(SPLIT.split(line), 0));
		}

		BiMap<String, Integer> indexMap = HashBiMap.create();
		Map<Integer, Double> wtMap = Maps.newHashMap();

		File[] branchDirs = srcDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return !f.getName().startsWith(".");
			}
		});

		for (int i=0; i<branchDirs.length; i++) {
			System.out.println(i + " " + branchDirs[i].getName());
		}

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

		Map<Period, Table<Integer, String, String>> curveMap = Maps
			.newHashMap();
		Table<Integer, String, String> table = null;
		table = ArrayTable.create(indexMap.values(), locNames);
		curveMap.put(GM0P00, table);
		table = ArrayTable.create(indexMap.values(), locNames);
		curveMap.put(GM0P20, table);
		table = ArrayTable.create(indexMap.values(), locNames);
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
					String locStr = StringUtils.substringBefore(line, ",");
					String curveStr = StringUtils.substringAfter(StringUtils
						.substringAfter(
							StringUtils.substringAfter(
								StringUtils.substringAfter(line, ","), ","),
							","), ",");
					String curveStrOut = branchIdx + "," + branchWt + "," +
						curveStr;
					table.put(branchIdx, locStr, curveStrOut);
				}
			}
		}

		for (Period period : periods) {
			table = curveMap.get(period);
			for (String name : locNames) {

				File curvesOut = new File(outDir, period.name() + S +
					name + S + CURVE_FILE + ".csv");
				Files.createParentDirs(curvesOut);
				// header
				Iterable<String> gmVals = Collections2.transform(period
					.getFunction().xValues(), Functions.toStringFunction());
				List<String> headers = Lists.newArrayList("ERF#", "wt");
				Iterable<String> cityFields = Iterables.concat(headers, gmVals);
				String cityHeader = JOIN.join(cityFields) + LF;
				Files.write(cityHeader, curvesOut, US_ASCII);

				File paramsOut = new File(outDir, period.name() + S +
					name + S + PARAM_FILE + ".csv");
				Files.createParentDirs(paramsOut);
				Files.write("ERF#,BranchName" + LF, paramsOut, US_ASCII);

				// data
				Map<Integer, String> indexMapInverse = indexMap.inverse();
				for (int i = 0; i < indexMap.size(); i++) {
					String curveLine = table.get(i, name) + LF;
					String paramLine = i + "," + indexMapInverse.get(i) + LF;
					Files.append(curveLine, curvesOut, US_ASCII);
					Files.append(paramLine, paramsOut, US_ASCII);
				}
			}
		}
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
	public static void reorganizeUC3branchResults(File srcDir, File outDir,
			boolean ignoreWts) throws IOException {
		// convert solutions grouped by branch to solutions grouped by city
		Set<Period> periods = EnumSet.of(GM0P00, GM0P20, GM1P00);

		BiMap<String, Integer> indexMap = HashBiMap.create();
		Map<Integer, Double> wtMap = Maps.newHashMap();

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

				File curvesOut = new File(outDir, period.name() + S +
					city.name() + S + CURVE_FILE + ".csv");
				Files.createParentDirs(curvesOut);
				// header
				Iterable<String> gmVals = Collections2.transform(period
					.getFunction().xValues(), Functions.toStringFunction());
				List<String> headers = Lists.newArrayList("ERF#", "wt");
				Iterable<String> cityFields = Iterables.concat(headers, gmVals);
				String cityHeader = JOIN.join(cityFields) + LF;
				Files.write(cityHeader, curvesOut, US_ASCII);

				File paramsOut = new File(outDir, period.name() + S +
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
//		 String dir = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC2-TimeIndep";
//		String dir = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/convABM";
		String dir = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/treeFM32single";
		try {
			// boolean is tornado
			runBranchSummaries(dir, imrID, periods, cities, true);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void generateBranchSummaries2(String locPath, String curveDir,
			boolean tornado) {
		Iterable<Period> periods = EnumSet.of(GM0P00, GM0P20, GM1P00);
		List<String> locNames = Lists.newArrayList();
		File locFile = new File(locPath);
		try {
		List<String> locLines = Files.readLines(locFile, US_ASCII);
			for (String line : locLines) {
				if (line.startsWith("#")) continue;
				locNames.add(Iterables.get(SPLIT.split(line), 0));
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		String imrID = NSHMP08_WUS.SHORT_NAME;
		try {
			// boolean is tornado
			runBranchSummaries2(curveDir, imrID, periods, locNames, tornado);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/*
	 * Create summaries of logic tree branch hazard curves.
	 */
	private static void runBranchSummaries2(String dir, String imrID,
			Iterable<Period> periods, Iterable<String> locNames,
			boolean tornado) throws IOException {
		for (Period p : periods) {
			for (String name : locNames) {
				System.out.println(name + " " + p);
				File srcDir = new File(dir + S + p.name() + S + name);
				File srcFile = new File(srcDir, imrID + "_curves.csv");
				File branchFile = new File(srcDir, imrID + "_params.csv");
				File statFile = new File(srcDir, imrID + "_stats.csv");
				File sumFile = new File(srcDir, imrID + "_summary.csv");
				File torRTGM_File = null, tor2in50_File = null, tor10in50_File = null;
				if (tornado) {
					torRTGM_File = new File(srcDir, imrID + "_tornado_rtgm.csv");
					tor2in50_File = new File(srcDir, imrID + "_tornado_2in50.csv");
					tor10in50_File = new File(srcDir, imrID + "_tornado_10in50.csv");
				}
				summarizeBranches(srcFile, branchFile, statFile, sumFile,
					torRTGM_File, tor2in50_File, tor10in50_File, p);
			}
		}
	}


	/*
	 * Create summaries of logic tree branch hazard curves.
	 */
	private static void runBranchSummaries(String dir, String imrID,
			Iterable<Period> periods, Iterable<NEHRP_TestCity> cities,
			boolean tornado) throws IOException {
		for (Period p : periods) {
			for (NEHRP_TestCity c : cities) {
				File srcDir = new File(dir + S + p.name() + S + c.name());
				File srcFile = new File(srcDir, imrID + "_curves.csv");
				File branchFile = new File(srcDir, imrID + "_params.csv");
				File statFile = new File(srcDir, imrID + "_stats.csv");
				File sumFile = new File(srcDir, imrID + "_summary.csv");
				File torRTGM_File = null, tor2in50_File = null, tor10in50_File = null;
				if (tornado) {
					torRTGM_File = new File(srcDir, imrID + "_tornado_rtgm.csv");
					tor2in50_File = new File(srcDir, imrID + "_tornado_2in50.csv");
					tor10in50_File = new File(srcDir, imrID + "_tornado_10in50.csv");
				}
				summarizeBranches(srcFile, branchFile, statFile, sumFile,
					torRTGM_File, tor2in50_File, tor10in50_File, p);
			}
		}
	}

	/*
	 * Reads an erf branch file that has columns with wts, rtgm values and a
	 * hazard curve values and outputs one summary file with min, max, and mean
	 * data and another with just wt, pe2in50, and pe10in50.
	 */
	private static void summarizeBranches(File curveFile, File branchFile,
			File stat, File summary, File torRTGM, File tor2in50,
			File tor10in50, Period period)
					throws IOException {
		List<String> curveLines = Files.readLines(curveFile, US_ASCII);
		List<Double> weights = Lists.newArrayList();
		List<Double> rtgms = Lists.newArrayList();
		List<Double> pe2in50s = Lists.newArrayList();
		List<Double> pe10in50s = Lists.newArrayList();
		XY_DataSetList curves = new XY_DataSetList();
		
		// branch list and index reverse lookup 
		List<String> branchLines = Files.readLines(branchFile, US_ASCII);
		List<String> branchList = Lists.newArrayList();

		// create model function: first line has ERF#, wt, rtgm, gm-vals ...
		Iterable<String> firstLine = SPLIT.split(curveLines.get(0));
		DiscretizedFunc curveModel = new ArbitrarilyDiscretizedFunc();
		for (String gmStr : Iterables.skip(firstLine, 2)) {
			double gmVal = Double.parseDouble(gmStr);
			curveModel.set(gmVal, 0.0);
		}

		// fill curves, pe intercepts and rtgm lists
		for (String line : Iterables.skip(curveLines, 1)) {
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
		
		// fill branch list and index lookup map
		for (String line : Iterables.skip(branchLines, 1)) {
			Iterable<String> vals = SPLIT.split(line);
			String branchName = Iterables.get(vals, 1);
			branchList.add(branchName);
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
		
		// write tornado data
		if (torRTGM != null) {
			TornadoData tdRTGM = new UC3_TornadoBuilder(branchList, rtgms).build();
			Files.write(tdRTGM.toSortedString(), torRTGM, US_ASCII);
			TornadoData td2in50 = new UC3_TornadoBuilder(branchList, pe2in50s).build();
			Files.write(td2in50.toSortedString(), tor2in50, US_ASCII);
			TornadoData td10in50 = new UC3_TornadoBuilder(branchList, pe10in50s).build();
			Files.write(td10in50.toSortedString(), tor10in50, US_ASCII);
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

	public static void generateFortranCityData() throws IOException {
		String srcPath = "/Volumes/Scratch/nshmp-sources/FortranUpdate";
		String outPath = "/Volumes/Scratch/rtgm/FortranUpdate";

		Iterable<Period> periods = EnumSet.of(GM0P00, GM0P20, GM1P00);
		Map<String, Location> siteMap = 
				UC3_CalcDriver.readSiteFile("tmp/curves/sites/all.txt");
		Iterable<NEHRP_TestCity> cities = NEHRP_TestCity.getCA(); // EnumSet.of(VENTURA);

		for (Period p : periods) {
			File src = new File(srcPath + S + p.name() + S + "curves.dat");
			File out = new File(outPath + S + p.name() + S + "FORT_curves.csv");
			try {
				Files.createParentDirs(out);
				deriveCityData(siteMap, src, out, p);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private static void deriveCityData(Map<String, Location> siteMap,
			File src, File out, Period period) throws IOException {

		// header
		Iterable<String> gmVals = Collections2.transform(period.getFunction()
			.xValues(), Functions.toStringFunction());
		Iterable<String> cityFields = Iterables.concat(CITY_FIELDS, gmVals);
		String cityHeader = JOIN.join(cityFields) + LF;
		Files.write(cityHeader, out, US_ASCII);

		// extract data
		CurveContainer cc = CurveContainer.create(src);
		for (String siteName : siteMap.keySet()) {
			Location loc = siteMap.get(siteName);
			DiscretizedFunc curve = cc.getCurve(loc);
			double pe2in50 = getPE(curve, PE2IN50);
			double pe10in50 = getPE(curve, PE10IN50);
			double rtgm = getRTGM(curve, period);
			Iterable<String> cityData = createData(siteName, pe2in50,
				pe10in50, rtgm, curve);
			String cityLine = JOIN.join(cityData) + LF;
			Files.append(cityLine, out, US_ASCII);
		}
	}
	
	private static void generateBranchList() {
		
		String fileName = "tree_refNoBG_tap";
		Set<FaultModels> fltModels = EnumSet.of(
			FM3_1, FM3_2); //FM3_2); // FM3_1, FM3_2);
		Set<DeformationModels> defModels = EnumSet.of(
//			ZENG);
			ABM, GEOLOGIC, NEOKINEMA, ZENG);
		Set<ScalingRelationships> scalingRel = EnumSet.of(
//			ELLSWORTH_B, ELLB_SQRT_LENGTH, HANKS_BAKUN_08,
//			SHAW_CONST_STRESS_DROP);
//			SHAW_2009_MOD);
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
	
	static class UC3_TornadoBuilder {
		
		private List<Double> values;
		private List<String> branchNames;
		private Map<String, Integer> branchIdxMap = Maps.newHashMap();
		
		UC3_TornadoBuilder(List<String> branchNames, List<Double> values) {
			checkArgument(branchNames.size() == values.size());
			this.branchNames = branchNames;
			this.values = values;
			int idx = 0;
			for (String name : branchNames) {
				branchIdxMap.put(name, idx++);
			}
		}
		
		public TornadoData build() {			

			List<Class> classList = Lists.newArrayList(); // for ordering
			Map<Class, Set<LogicTreeBranchNode>> nodeMap = Maps.newHashMap();

			// init class to node variant map
			for (Class clazz : LogicTreeBranch.getLogicTreeNodeClasses()) {
				Set<LogicTreeBranchNode> nodeSet = Sets.newHashSet();
				classList.add(clazz);
				nodeMap.put(clazz, nodeSet);
			}

			// fill nodeMap wtih valid logic tree variants
			for (String name : branchNames) {
				LogicTreeBranch ltb = LogicTreeBranch.fromFileName(name);
				for (LogicTreeBranchNode node : ltb) {
					Class clazz = LogicTreeBranch.getEnumEnclosingClass(node.getClass());
					Set<LogicTreeBranchNode> nodeSet = nodeMap.get(clazz);
					nodeSet.add(node);
				}
			}

			// median value and logic tree branch
			int medIdx = medianIndex(values);
			double medVal = values.get(medIdx);
			String medBrName = branchNames.get(medIdx);
			LogicTreeBranch medLTB = LogicTreeBranch.fromFileName(medBrName);
			
			// loop all valid nodes gathering values for branch variants
			TornadoData td = new TornadoData(medVal);
			for (Class clazz : classList) {
				Set<LogicTreeBranchNode> nodeSet = nodeMap.get(clazz);
				for (LogicTreeBranchNode node : nodeSet) {
					LogicTreeBranch ltb = (LogicTreeBranch) medLTB.clone();
					ltb.setValue(node);
					String brName = ltb.buildFileName();
					int brIdx = branchIdxMap.get(brName);
					double brVal = values.get(brIdx);
					td.add(clazz, (Enum) node, brVal);
				}
			}

			return td;
		}
		
	}
	
	/*
	 * If values.size() is odd, method return the index of the median value. If
	 * values.size() is even the index of values.size()/2 is returned.
	 */
	private static int medianIndex(List<Double> values) {
		double[] sortedVals = Doubles.toArray(values);
		Arrays.sort(sortedVals);
		int idx = (sortedVals.length - 1) / 2;
		double median = sortedVals[idx];
		return values.indexOf(median);
	}
	
	
	
	private static final String fix10in50path = "/Users/pmpowers/Documents/OpenSHA/RTGM/data";
	private static final String[] fixList = {
		"FortranUpdate", "FSS_UC2map", "MeanUCERF2", "MeanUCERF2update",
		"MeanUCERF2update_FM2P1", "ModMeanUCERF2update_FM2P1", "NSHMP_CA_SHA", 
		"NSHMP_CA_SHA-epi", "NSHMP_SHA", "NSHMP_SHA-epi"
	};
	
	private static void fix10in50s() {
		try {
			Set<Period> periods = EnumSet.of(GM0P00, GM0P20, GM1P00);
			for (String dir : fixList) {
				for (Period p : periods) {
					String path = fix10in50path + S + dir + S + p + S;
					File curveFile = new File(path + "NSHMP08_WUS_curves.csv");
					if (!curveFile.exists()) {
						curveFile = new File(path + "NSHMP08_curves.csv");
					}
					if (!curveFile.exists()) {
						curveFile = new File(path + "FORT_curves.csv");
					}
					
					List<String> linesIn = Files.readLines(curveFile, US_ASCII);
					Iterable<String> firstLine = SPLIT.split(linesIn.get(0));
					DiscretizedFunc curveModel = new ArbitrarilyDiscretizedFunc();
					for (String gmStr : Iterables.skip(firstLine, 4)) {
						double gmVal = Double.parseDouble(gmStr);
						curveModel.set(gmVal, 0.0);
					}
					Files.write(linesIn.get(0) + LF, curveFile, US_ASCII);
	
					// fill curves, pe intercepts and rtgm lists
					for (String line : Iterables.skip(linesIn, 1)) {
						Iterable<String> lineIter = SPLIT.split(line);
						List<String> lineList = Lists.newArrayList(lineIter);
						
						DiscretizedFunc curve = curveModel.deepClone();
						int idx = 0;
						for (String val : Iterables.skip(lineIter, 4)) {
							double annRate = Double.parseDouble(val);
							curve.set(idx++, annRate);
						}
						double pe10in50 = getPE(curve, PE10IN50);
						lineList.set(2, Double.toString(pe10in50));
						String fixedLine = JOIN.join(lineList) + LF;
						Files.append(fixedLine, curveFile, US_ASCII);
					}
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static void listAllBranches() {
		try {
			String path1440 = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2012_10_29-tree-fm31_x7-fm32_x1_COMPOUND_SOL.zip";
			CompoundFaultSystemSolution cfss = UC3_CalcWrapper
				.getCompoundSolution(path1440);
			List<LogicTreeBranch> branches = Lists.newArrayList(cfss
				.getBranches());
			File out = new File("tmp/branchlist1440.txt");
			Files.write("", out, US_ASCII);
			int idx = 0;

			for (LogicTreeBranch branch : branches) {
				Files.append((idx++) + " " + branch.buildFileName() + LF, out,
					US_ASCII);
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	
	private static void writeConvTestMags() throws IOException {
		// rupture of interest: 29749
		int fssRupIdx = 29749;
		int maxIdx = 1;
		
		File out = new File("tmp/SRPconvTestRupRates.txt");
		String header = TAB_JOIN.join("FSSidx","fssRate","fssMag","erfRate","erfMag") + LF;
		Files.write(header, out, US_ASCII);
		
		// load conv fss
		String convSolPath = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip";
		AverageFaultSystemSolution afss = UC3_CalcWrapper.getAvgSolution(convSolPath);
		for (int i=0; i<maxIdx; i++) {
			FaultSystemSolution fss = afss.getSolution(i);
			double fssRupRate = fss.getRateForRup(fssRupIdx);
			double fssRupMag = fss.getMagForRup(fssRupIdx);
			System.out.println(fssRupRate + "\t" + fssRupMag);
		
			UCERF3_FaultSysSol_ERF erf = UC3_CalcWrapper.getUC3_ERF(fss);
			erf.updateForecast();
			int srcIdx = -1;
			for (int j=0; j<erf.getNumFaultSystemSources(); j++) {
				int rupIdx = erf.getFltSysRupIndexForSource(j);
				if (rupIdx == fssRupIdx) {
					srcIdx = j;
					break;
				}
			}
			checkArgument(srcIdx != -1);
			ProbEqkSource src = erf.getSource(srcIdx);
			System.out.println(src.getSourceMetadata());
			ProbEqkRupture rup = src.getRupture(0);
			double erfRupRate = rup.getMeanAnnualRate(1d);
			double erfRupMag = rup.getMag();
			String outLine = TAB_JOIN.join(i, fssRupRate, fssRupMag, erfRupRate, erfRupMag) + LF;
			System.out.println(outLine);
			Files.append(outLine, out, US_ASCII);
		}
	}
	
}
