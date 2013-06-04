package scratch.peter.ucerf3.calc;

import static com.google.common.base.Charsets.US_ASCII;
import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME;
import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME;
import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.CPT_PARAM_NAME;
import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.GRID_SPACING_PARAM_NAME;
import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.LOG_PLOT_NAME;
import static org.opensha.nshmp2.tmp.TestGrid.CA_RELM;
import static org.opensha.nshmp2.util.Period.*;
import static scratch.UCERF3.enumTreeBranches.DeformationModels.ABM;
import static scratch.UCERF3.enumTreeBranches.DeformationModels.GEOLOGIC;
import static scratch.UCERF3.enumTreeBranches.DeformationModels.NEOKINEMA;
import static scratch.UCERF3.enumTreeBranches.DeformationModels.ZENGBB;
import static scratch.peter.curves.ProbOfExceed.*;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.opensha.commons.data.xyz.ArbDiscrGeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.elements.PSXYPolygon;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.DataUtils;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.peter.curves.ProbOfExceed;
import scratch.peter.nshmp.CurveContainer;
import scratch.peter.nshmp.NSHMP_DataUtils;
import scratch.peter.nshmp.NSHMP_PlotUtils;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC33_MapMaker {

	private static final Splitter SPLIT = Splitter.on('_');
	private static final Joiner JOIN = Joiner.on(',');
	private static String format = "%.3f";
	private static final String S = File.separator;
	private static final String ROOT = "tmp/UC33/maps/";
	private static final String SRC = ROOT + "src/";

	public static void main(String[] args) throws IOException {
//		makeBrAvgRatioMapUC33();
//		makePrelimBrAvgHazardMaps();
//		doInversionRunAnalysis("UC33-10runs-3sec", GM3P00, PE2IN50);
//		buildLogicTreeVarMaps();
//		buildBrAvgMaps();
//		buildBrAvgHazardMaps();
		
//		buildGridImplCheckNSHMP();
		buildUC3gmpe13();
//		buildBrAvgHazardMaps();
	}

	/*
	 * Used to make ratio maps for UCERF3.2 report 
	 */
	private static void buildLogicTreeVarMaps() throws IOException {
		List<String> brOverList = null;
		String brUnder = null;
		String branches = null;
		String srcDir = SRC + "UC33/";
		String outDir = ROOT + "LogicTreeRatios/";
		
//		// Mmax and Mgt5 comparisons
//		brOverList = Lists.newArrayList(
//			"M565-MX73", "M579-MX73", "M596-MX73",
//			"M565-MX76", "M579-MX76", "M596-MX76",
//			"M565-MX79", "M579-MX79", "M596-MX79");
//		brUnder = "M579-MX76";
//		makeRatioMaps(srcDir, outDir, brOverList, brUnder);
		
//		// other branch node comparisons
//		brOverList = Lists.newArrayList(
//			"FM31", "FM32", 
//			"DM_ZBB", "DM_ABM", "DM_GEOL", "DM_NEOK",
//			"MS_SH09M", "MS_ELLB", "MS_ELLBSL", "MS_HB08", "MS_SHCSD",
//			"DSR_TAP", "DSR_UNI",
//			"M565", "M579", "M596",
//			"MX73", "MX76", "MX79",
//			"UC2", "UC3");
//		brUnder = "all";
//		makeRatioMaps(srcDir, outDir, brOverList, brUnder);
		
//		// ratio map UC33 over NSMP (1440 branches)
//		branches = "all";
//		makeNSHMPratioMap(srcDir, outDir, branches);
//		branches = "UC3";
//		makeNSHMPratioMap(srcDir, outDir, branches);
//		branches = "UC2";
//		makeNSHMPratioMap(srcDir, outDir, branches);
	}
	
	// check ratio of optimized NSHMP to new grid implementation
	private static void buildGridImplCheckNSHMP() {
		TestGrid grid = CA_RELM;
		double spacing = 0.1;
		String outDir = ROOT + "NewGridSrcImpl/";
		
		List<Period> periods = Lists.newArrayList(GM0P20); //GM0P00, GM0P20, GM1P00);
		List<ProbOfExceed> PEs = Lists.newArrayList(PE2IN50); //, PE10IN50);
		
		for (Period p : periods) {
			for (ProbOfExceed pe : PEs) {
				GeoDataSet over = loadSingle(SRC + "nshmp_ca-13/", pe, grid, p, spacing);
				GeoDataSet under = loadSingle(SRC + "nshmp_ca-08/", pe, grid, p, spacing);
			
//				GeoDataSet over = loadSingle(SRC + "nshmp_ca-08/", pe, grid, p, spacing);
//				GeoDataSet under = loadSingle(SRC + "nshmp_ca/", pe, grid, p, spacing);

				GeoDataSet xyzRatio = GeoDataSetMath.divide(over, under);
				GeoDataSet xyzDiff = GeoDataSetMath.subtract(over, under);
			
				String id = "nshmp13-08";
//				String id = "nshmp08newGrd";
				
				String ratioDir = outDir + id + "-ratio-" + p + "-" + pe;
				makeRatioPlot(xyzRatio, 0.1, grid.bounds(), ratioDir, "GM ratio", true, 0.1, true, false);
				String diffDir = outDir +  id + "-diff-" + p + "-" + pe;
				makeDiffPlot(xyzDiff, 0.1, grid.bounds(), diffDir, "GM diff", 0.3, true, false);
			}
		}
	}
	
	private static void buildUC3gmpe13() {
		TestGrid grid = CA_RELM;
		double spacing = 0.1;
		String outDir = ROOT + "NewGMPE/";
		
		List<Period> periods = Lists.newArrayList(GM1P00); //, GM0P20, GM1P00);
		List<ProbOfExceed> PEs = Lists.newArrayList(PE2IN50); //, PE10IN50);
		GriddedRegion gr = grid.grid(spacing);

		String srcOver = SRC + "UC33brAvg-FM-DM-13";
		String srcUnder = SRC + "UC33brAvg-FM-DM-08";
		
		for (Period p : periods) {
			CurveContainer brAvgCCover = buildBrAvgCurveContainer(new File(srcOver), p);
			CurveContainer brAvgCCunder = buildBrAvgCurveContainer(new File(srcUnder), p);
			for (ProbOfExceed pe : PEs) {
				// brAvg data
				GeoDataSet over = NSHMP_DataUtils.extractPE(brAvgCCover, gr, pe);
				GeoDataSet under = NSHMP_DataUtils.extractPE(brAvgCCunder, gr, pe);
				GeoDataSet xyzRatio = GeoDataSetMath.divide(over, under);
				GeoDataSet xyzDiff = GeoDataSetMath.subtract(over, under);
				
				String id = "UC3-08-13";
				String ratioDir = outDir + id + "-ratio-" + p + "-" + pe;
				makeRatioPlot(xyzRatio, 0.1, grid.bounds(), ratioDir, "GM ratio", true, 0.1, true, false);
				String diffDir = outDir +  id + "-diff-" + p + "-" + pe;
				makeDiffPlot(xyzDiff, 0.1, grid.bounds(), diffDir, "GM diff", 0.2, true, false);
			}
		}
	}
	
	
	// ground motion maps and ratios for NSMP and UC3 using the 8brAvg solutions
	private static void buildBrAvgHazardMaps() throws IOException {
		TestGrid grid = CA_RELM;
		double spacing = 0.1;
		
		List<Period> periods = Lists.newArrayList(GM0P00, GM0P20, GM1P00);
		List<ProbOfExceed> PEs = Lists.newArrayList(PE2IN50, PE10IN50);
		
		
		// NSHMP maps
		Map<String, GeoDataSet> nshmpXYZ = Maps.newHashMap();
		for (Period p : periods) {
			for (ProbOfExceed pe : PEs) {
				// nshmp data
				GeoDataSet xyzNSHMP = loadSingle(SRC + "nshmp_ca", pe, grid, p, spacing);
				String mapID = p.name() + "-" + pe.name();
				nshmpXYZ.put(mapID,  xyzNSHMP);
				// nshmp hazard map
//				String dir = ROOT + "Hazard/nshmp-" + mapID;
//				makeHazardMap(xyzNSHMP, spacing, p, pe, grid, dir);
			}
		}
		
		// UC3 8brAvgMaps
		Map<String, GeoDataSet> brAvgXYZ = Maps.newHashMap();
		String srcBase = SRC + "UC33brAvg-FM-DM-13";
		GriddedRegion gr = grid.grid(spacing);
		for (Period p : periods) {
			File srcDir = new File(srcBase);
			CurveContainer brAvgCC = buildBrAvgCurveContainer(srcDir, p);
			for (ProbOfExceed pe : PEs) {
				// brAvg data
				GeoDataSet xyzBrAvg = NSHMP_DataUtils.extractPE(brAvgCC, gr, pe);
				String mapID = p.name() + "-" + pe.name();
				brAvgXYZ.put(mapID,  xyzBrAvg);
				// braAvg hazard map
				String dir = ROOT + "Hazard/brAvg13-" + mapID;
				makeHazardMap(xyzBrAvg, spacing, p, pe, grid, dir);
			}
		}
		
		// UC3 / NSHMP
		for (String id : brAvgXYZ.keySet()) {
			GeoDataSet xyzNSHMP = nshmpXYZ.get(id);
			GeoDataSet xyzBrAvg = brAvgXYZ.get(id);
			GeoDataSet ratio = GeoDataSetMath.divide(xyzBrAvg, xyzNSHMP);
			GeoDataSet diff = GeoDataSetMath.subtract(xyzBrAvg, xyzNSHMP);
			String ratioDir = ROOT + "Hazard/brAvg13_sup_nshmp-" + id;
			String diffDir = ROOT + "Hazard/brAvg13_diff_nshmp-" + id;
			makeRatioPlot(ratio, 0.1, grid.bounds(), ratioDir, "GM ratio", true, 0.3, true, false);
			double diffScale = id.contains("GM0P200") ? 0.4 : 0.2; // larger scale for 5Hz
			makeDiffPlot(diff, 0.1, grid.bounds(), diffDir, "GM diff", diffScale, true, false);
		}
		
	}
	
	// map and ratio of full UC3 logic tree over NSHMP
	private static void buildHazardMap() {
		
	}
	
	// builds the maps that compare FM-DM and FM-DM-MS brAVg maps to full tree
	private static void buildBrAvgMaps() throws IOException {
		TestGrid grid = CA_RELM;
		ProbOfExceed pe = PE2IN50;
		Period p = GM0P00;
		String suffix = "";
		double spacing = 0.1;
		String dlDir = ROOT + "BranchAvgComparison/";
		GriddedRegion gr = grid.grid(spacing);

		// build brAvg curve containers
		File srcDir = new File(SRC + "UC33brAvg-FM-DM");
		CurveContainer cc_FM_DM = buildBrAvgCurveContainer(srcDir, p);
		GeoDataSet xyz_FM_DM = NSHMP_DataUtils.extractPE(cc_FM_DM, gr, pe);
		// makeHazardMap(xyz_FM_DM, spacing, p, pe, grid, dlDir + "map-FM-DM/");
		
		srcDir = new File(SRC + "UC33brAvg-FM-DM-MS");
		CurveContainer cc_FM_DM_MS = buildBrAvgCurveContainer(srcDir, p);
		GeoDataSet xyz_FM_DM_MS = NSHMP_DataUtils.extractPE(cc_FM_DM_MS, gr, pe);
		// makeHazardMap(xyz_FM_DM_MS, spacing, p, pe, grid, dlDir + "map-FM-DM-MS/");
				
		// load full tree denominator
		File brUnderFile = new File(dlDir + "../LogicTreeRatios/branchsets/all.txt");
		GeoDataSet xyzUnder = loadMulti(SRC + "UC33/", brUnderFile, pe, grid, p, suffix);
		
		String dlPath = null;
		GeoDataSet xyzOver = null;
		GeoDataSet xyzRatio = null;
		
		// FM-DM map
		xyzOver = NSHMP_DataUtils.extractPE(cc_FM_DM, gr, pe);
		
		dlPath = dlDir + "FM-DM-0.1/";
		// ratio data sets are corrupted by gmt so recreate when using for multiple maps
		xyzRatio = GeoDataSetMath.divide(xyzOver, xyzUnder);
		makeRatioPlot(xyzRatio, spacing, grid.bounds(), dlPath, "brAvg/fullTree", true, 0.1, true, false);
		dlPath = dlDir + "FM-DM-0.05/";
		xyzRatio = GeoDataSetMath.divide(xyzOver, xyzUnder);
		makeRatioPlot(xyzRatio, spacing, grid.bounds(), dlPath, "brAvg/fullTree", true, 0.05, true, false);
		dlPath = dlDir + "FM-DM-0.01/";
		xyzRatio = GeoDataSetMath.divide(xyzOver, xyzUnder);
		makeRatioPlot(xyzRatio, spacing, grid.bounds(), dlPath, "brAvg/fullTree", true, 0.01, true, false);

		// FM-DM-MS map
		xyzOver = NSHMP_DataUtils.extractPE(cc_FM_DM_MS, gr, pe);

		dlPath = dlDir + "FM-DM-MS-0.1/";
		xyzRatio = GeoDataSetMath.divide(xyzOver, xyzUnder);
		makeRatioPlot(xyzRatio, spacing, grid.bounds(), dlPath, "brAvg/fullTree", true, 0.1, true, false);
		dlPath = dlDir + "FM-DM-MS-0.05/";
		xyzRatio = GeoDataSetMath.divide(xyzOver, xyzUnder);
		makeRatioPlot(xyzRatio, spacing, grid.bounds(), dlPath, "brAvg/fullTree", true, 0.05, true, false);
		dlPath = dlDir + "FM-DM-MS-0.01/";
		xyzRatio = GeoDataSetMath.divide(xyzOver, xyzUnder);
		makeRatioPlot(xyzRatio, spacing, grid.bounds(), dlPath, "brAvg/fullTree", true, 0.01, true, false);
		
	}
	
	private static void makeHazardMap(GeoDataSet xyz, double spacing, Period p, ProbOfExceed pe, TestGrid grid, String outDir) {
		double[] minmax = NSHMP_PlotUtils.getRange(p);
		GMT_CPT_Files cpt = NSHMP_PlotUtils.getCPT(p);
		String label = pe + " " + p.getLabel() + " (g)";
		makeMapPlot(xyz, spacing, grid.bounds(), outDir, label, minmax[0], minmax[1], cpt, true, false);
	}
	
	private static CurveContainer buildBrAvgCurveContainer(File srcDir, Period p) {
		TestGrid grid = CA_RELM;
		double spacing = 0.1;

		Map<String, Double> fmWts = Maps.newHashMap();
		fmWts.put("FM31", 0.5);
		fmWts.put("FM32", 0.5);
		Map<String, Double> dmWts = Maps.newHashMap();
		dmWts.put("ABM", 0.1);
		dmWts.put("GEOL", 0.3);
		dmWts.put("NEOK", 0.3);
		dmWts.put("ZENGBB", 0.3);
		Map<String, Double> msWts = Maps.newHashMap();
		msWts.put("ELLB", 0.2);
		msWts.put("ELLBSL", 0.2);
		msWts.put("HB08", 0.2);
		msWts.put("SH09M", 0.2);
		msWts.put("SHCSD", 0.2);

		CurveContainer brAvgCC = null;

		for (File brAvgDir : srcDir.listFiles()) {
			if (!brAvgDir.isDirectory()) continue;
			String brAvgName = brAvgDir.getName();
			String brAvgPath = brAvgDir.getPath() + S + grid + S + p + S + "curves.csv";
			File brAvgFile = new File(brAvgPath);
			System.out.println("path: " + brAvgPath);
			
			// brAvg weight
			Iterator<String> ids = SPLIT.split(brAvgName).iterator();
			ids.next(); // skip first part
			Double fm = fmWts.get(ids.next());
			Double dm = dmWts.get(ids.next());
			Double ms = (ids.hasNext()) ? msWts.get(ids.next()) : 1.0;
			double wt = fm * dm * ms;
			System.out.println("wt: " + wt);
			
			// create and weight curve container
			CurveContainer cc = CurveContainer.create(brAvgFile, grid, spacing);
			cc.scale(wt);
			if (brAvgCC == null) {
					brAvgCC = cc;
			} else {
					brAvgCC.add(cc);
			}
		}
		return brAvgCC;
	}
	

	
	// UCERF3.3 node ratio maps
	private static void makeRatioMaps(String srcDir, String outDir,
			List<String> brOverList, String brUnder) throws IOException {
		TestGrid grid = CA_RELM;
		ProbOfExceed pe = PE2IN50;
		Period p = GM0P00;
		String suffix = "";
		
		File brUnderFile = new File(outDir + "branchsets", brUnder + ".txt");
		System.out.println(brUnderFile);
		GeoDataSet xyzUnder = loadMulti(srcDir, brUnderFile, pe, grid, p, suffix);

		for (String brOver : brOverList) {
			File brOverFile = new File(outDir + "branchsets", brOver + ".txt");
			GeoDataSet xyzOver = loadMulti(srcDir, brOverFile, pe, grid, p, suffix);
		
			GeoDataSet xyz = GeoDataSetMath.divide(xyzOver, xyzUnder);

			String dlDir = outDir + brOver + "_sup_" + brUnder;
			makeRatioPlot(xyz, 0.1, grid.bounds(), dlDir, "GM ratio", true, 0.1, true, false);
		}
	}
	
	// UCERF3.3 NSHMP ratio maps
	private static void makeNSHMPratioMap(String srcDir, String outDir,
			String branches) throws IOException {
		TestGrid grid = CA_RELM;
		ProbOfExceed pe = PE2IN50;
		Period p = GM0P00;
		
		GeoDataSet xyzNSHMP = loadSingle(SRC + "nshmp_ca/", pe, grid, p, 0.1);
		File branchFile = new File(outDir + "branchsets", branches + ".txt");
		GeoDataSet xyzUC32 = loadMulti(srcDir, branchFile, pe, grid, p, "");
		GeoDataSet xyz = GeoDataSetMath.divide(xyzUC32, xyzNSHMP);
		String dlDir = outDir + branches + "_sup_NSHMP";
		makeRatioPlot(xyz, 0.1, grid.bounds(), dlDir, "GM ratio", true, 0.3, false, false);
	}

	
	public static void makeRatioPlot(GeoDataSet xyz, double spacing,
			double[] bounds, String dlDir, String title, boolean log,
			double logScale, boolean smooth, boolean showFaults) {
		double scale = log ? logScale : 0.2;
		GMT_MapGenerator mapGen = NSHMP_PlotUtils.create(bounds);
		mapGen.setParameter(COLOR_SCALE_MIN_PARAM_NAME, log ? -scale : 1-scale);
		mapGen.setParameter(COLOR_SCALE_MAX_PARAM_NAME, log ? scale : 1+scale);
		mapGen.setParameter(GRID_SPACING_PARAM_NAME, spacing);
		CPTParameter cptParam = (CPTParameter) mapGen.getAdjustableParamsList()
				.getParameter(CPT_PARAM_NAME);
		GMT_CPT_Files cpt = log ? GMT_CPT_Files.UCERF3_HAZ_RATIO_P3 : GMT_CPT_Files.GMT_POLAR;
		cptParam.setValue(cpt.getFileName());
		mapGen.setParameter(LOG_PLOT_NAME, log ? true : false);
		
		try {
			GMT_Map map = mapGen.getGMTMapSpecification(xyz);
			map.setCustomLabel(title);
			map.setRescaleCPT(smooth);
			if (showFaults) {
//				addFaultTraces(FaultModels.FM2_1, map, Color.BLACK);
				addFaultTraces(FaultModels.FM3_1, map, Color.BLACK);
//				addFaultTraces(FaultModels.FM3_2, map, Color.BLACK);
			}
			NSHMP_PlotUtils.makeMap(map, mapGen, "No metadata", dlDir);
			
			// copy map.pdf to dl dir
			File pdfFrom = new File(dlDir, "map.pdf");
			File pdfTo = new File(dlDir + ".pdf");
			Files.copy(pdfFrom, pdfTo);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void makeDiffPlot(GeoDataSet xyz, double spacing,
			double[] bounds, String dlDir, String title,
			double scale, boolean smooth, boolean showFaults) {
		GMT_MapGenerator mapGen = NSHMP_PlotUtils.create(bounds);
		mapGen.setParameter(COLOR_SCALE_MIN_PARAM_NAME, -scale);
		mapGen.setParameter(COLOR_SCALE_MAX_PARAM_NAME, scale);
		mapGen.setParameter(GRID_SPACING_PARAM_NAME, spacing);
		CPTParameter cptParam = (CPTParameter) mapGen.getAdjustableParamsList()
				.getParameter(CPT_PARAM_NAME);
		GMT_CPT_Files cpt = GMT_CPT_Files.UCERF3_HAZ_RATIO_P3;
		cptParam.setValue(cpt.getFileName());
		mapGen.setParameter(LOG_PLOT_NAME, false);
		
		try {
			GMT_Map map = mapGen.getGMTMapSpecification(xyz);
			map.setCustomLabel(title);
			map.setRescaleCPT(smooth);
			if (showFaults) {
//				addFaultTraces(FaultModels.FM2_1, map, Color.BLACK);
				addFaultTraces(FaultModels.FM3_1, map, Color.BLACK);
//				addFaultTraces(FaultModels.FM3_2, map, Color.BLACK);
			}
			NSHMP_PlotUtils.makeMap(map, mapGen, "No metadata", dlDir);
			
			// copy map.pdf to dl dir
			File pdfFrom = new File(dlDir, "map.pdf");
			File pdfTo = new File(dlDir + ".pdf");
			Files.copy(pdfFrom, pdfTo);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


	private static void addFaultTraces(FaultModels fm, GMT_Map map, Color c) {
		List<FaultSectionPrefData> faults = fm.fetchFaultSections();
		for (FaultSectionPrefData fspd : faults) {
			PSXYPolygon poly = new PSXYPolygon(fspd.getFaultTrace());
			poly.setPenColor(c);
			poly.setPenWidth(2);
			map.addPolys(poly);
		}
	}

	private static GeoDataSet loadSingle(String dir, ProbOfExceed pe,
			TestGrid grid, Period p, double spacing) {
		File curves = new File(dir + S + grid + S + p + S + "curves.csv");
		CurveContainer cc = CurveContainer.create(curves, grid, spacing);
		GriddedRegion gr = grid.grid(spacing);
		return NSHMP_DataUtils.extractPE(cc, gr, pe);
	}
	
	private static GeoDataSet loadMulti(String srcDir, File branchListFile,
			ProbOfExceed pe, TestGrid grid, Period p, String suffix)
			throws IOException {

		List<String> branchNames = Files.readLines(branchListFile, US_ASCII);
		System.out.println("Loading: " + branchListFile.getName());
		
		// create wt list
		List<Double> wtList = Lists.newArrayList();
		for (String brName : branchNames) {
			LogicTreeBranch branch = LogicTreeBranch.fromFileName(brName);
			wtList.add(branch.getAprioriBranchWt());
		}
		DataUtils.asWeights(wtList);
		
		String cPath = grid + S + p + S + "curves.csv";
		GriddedRegion gr = grid.grid(0.1);
		CurveContainer mapcc = null;
		
		int idx = 0;
		for (String brName : branchNames) {
			if (idx % 100 == 0) System.out.print(idx + " ");
			String brID = brName + suffix;
			String brPath = srcDir + S + brID + S + cPath;
			File brFile = new File(brPath);
			CurveContainer cc = CurveContainer.create(brFile, grid, 0.1);
			cc.scale(wtList.get(idx++));

			if (mapcc == null) {
				mapcc = cc;
			} else {
				mapcc.add(cc);
			}
		}
		return NSHMP_DataUtils.extractPE(mapcc, gr, pe);
	}
	
	
	
	
	
	// UCERF3.3 prelim branchAvg over comparable UC32 branch avg.
	private static void makeBrAvgRatioMapUC33() throws IOException {

		TestGrid grid = CA_RELM;
		ProbOfExceed pe = PE2IN50;
		Period p = GM0P00;
		double spacing = 0.1;

		String over = SRC + "UC33brAvg5x_fm32";
		String under = SRC + "UC32brAvg5x_fm32";
		String out = ROOT + "UC-33-32-brAvg-fm32";

		GeoDataSet xyzOver = loadSingle(over, pe, grid, p, spacing);
		GeoDataSet xyzUnder = loadSingle(under, pe, grid, p, spacing);
		GeoDataSet xyz = GeoDataSetMath.divide(xyzOver, xyzUnder);

		makeRatioPlot(xyz, 0.1, grid.bounds(), out, "GM ratio",
			true, 0.1, true, true);
	}
	
	// UCERF3.3 prelim ground motion maps
	private static void makePrelimBrAvgHazardMaps() {
		
		TestGrid grid = CA_RELM;
		ProbOfExceed pe = PE2IN50;
		Period p = GM0P00;
		double spacing = 0.1;
		
		String src33 = SRC + "UC33brAvg_prelim";
		String src32 = SRC + "UC32brAvg_cf33";

		GeoDataSet xyz33 = loadSingle(src33, pe, grid, p, spacing);
		GeoDataSet xyz32 = loadSingle(src32, pe, grid, p, spacing);

		String out33 = ROOT + "UC33brAvgMap";
		String out32 = ROOT + "UC32brAvgMap";

		// map
		double[] minmax = NSHMP_PlotUtils.getRange(p);
		GMT_CPT_Files cpt = NSHMP_PlotUtils.getCPT(p);
		String label = pe + " " + p.getLabel() + " (g)";
		
		makeMapPlot(xyz33, spacing, grid.bounds(), out33, label,
			minmax[0], minmax[1], cpt, true, true);
		makeMapPlot(xyz32, spacing, grid.bounds(), out32, label,
			minmax[0], minmax[1], cpt, true, true);
	}
	
	// UCERF3.3 tests of convergence; investigates the distribution of mean
	// hazard/ground motion across multiple inversion runs; sample = 10 runs
	// and we have maps for each
	private static void doInversionRunAnalysis(String dir, Period period, ProbOfExceed pe) throws IOException {
//		String dir = "UC33-10runs-PGA";
		String path1 = "UC33-brAvg-fm31-run";
		String path2 = "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_WITH_IND_RUNS_FM3_1_run";
		String path3 = "_MEAN_BRANCH_AVG_SOL";
		TestGrid grid = CA_RELM;
		double spacing = 0.1;
		int runCount = 10;
		GriddedRegion gr = grid.grid(spacing);
		
//		String under = SRC + "UC33brAvg5x_fm31";
//		GeoDataSet xyzUnder = loadSingle(under, pe, grid, period, spacing);
		
		// init stat arrays
		List<double[]> solMeans = Lists.newArrayList();
		for (int i=0; i<gr.getNodeCount(); i++) {
			solMeans.add(new double[10]);
		}
		
		// aggregate stat data and output ratio maps
		for (int i=0; i<runCount; i++) {
			String solDir = SRC + dir + S + path1 + i + S + path2 + i + path3;
			System.out.println("Processing sol: " + i);
			GeoDataSet xyzSol = loadSingle(solDir, pe, grid, period, spacing);
			for (int j=0; j<gr.getNodeCount(); j++) {
				solMeans.get(j)[i] = xyzSol.get(j);
			}
			
//			// maps - NOTE denominator is only first 5 inv runs, not 10
//			GeoDataSet xyz = GeoDataSetMath.divide(xyzSol, xyzUnder);
//			String out = ROOT + "UC33invTest-" + period + "-" + pe + "-" + i + S;
//			makeRatioPlot(xyz, 0.1, grid.bounds(), out, "GM ratio",
//				true, true, false);
		}
		
		LocationList locs = gr.getNodeList();
		
		// compute stats
		List<Double> means = Lists.newArrayList();
		List<Double> stds = Lists.newArrayList();
		
		Mean meanCalc = new Mean();
		StandardDeviation stdCalc = new StandardDeviation();
		for (int i=0; i<gr.getNodeCount(); i++) {
			double[] runData = solMeans.get(i);
			double mean = meanCalc.evaluate(runData);
			double std = stdCalc.evaluate(runData, mean);
			means.add(mean);
			stds.add(std);
		}
		
		// set mean and meanOverStd data
//		GriddedGeoDataSet xyzMean = new GriddedGeoDataSet(gr, true);
//		for (int i=0; i<gr.getNodeCount(); i++) {
//			xyzMean.set(i, means.get(i));
//		}
		
//		// compare mean(mean of 10 runs) to mean (5 runs - combined sol) 
//		GeoDataSet xyz = GeoDataSetMath.divide(xyzMean, xyzUnder);
//		String out = ROOT + "UC33invTest-10xMean-5xMean-" + period + "-" + pe;
//		makeRatioPlot(xyz, 0.1, grid.bounds(), out, "GM ratio",
//			true, true, false);

		// mean over std data
		String outPath = ROOT + "MapConvTests/stats/";
		String periodHead = (period.equals(GM0P00) ? "PGA" : "3sec");
		String peHead = (pe.equals(PE2IN50) ? "2in50" : (pe.equals(PE10IN50) ? "10in50" : "1in100"));
		String label = periodHead + "-" + peHead;
		File datFile = new File(outPath, "invStats-" + label + ".csv");
		List<String> headDat = Lists.newArrayList("lat", "lon", label+"-mean", label+"-std");
		String header = JOIN.join(headDat) + "\n";
		Files.write(header, datFile, US_ASCII);
		for (int i=0; i<locs.size(); i++) {
			Location loc = locs.get(i);
			List<String> locDat = Lists.newArrayList(
				String.format(format, loc.getLatitude()),
				String.format(format, loc.getLongitude()),
				Double.toString(means.get(i)),
				Double.toString(stds.get(i)));
			Files.append(JOIN.join(locDat) + "\n", datFile, Charsets.US_ASCII);
		}
		
	}


	
	public static void makeMapPlot(GeoDataSet xyz, double spacing, double[] bounds,
			String dlDir, String title, double scaleMin, double scaleMax,
			GMT_CPT_Files cpt, boolean smooth, boolean showFaults) {
		GMT_MapGenerator mapGen = NSHMP_PlotUtils.create(bounds);
		mapGen.setParameter(COLOR_SCALE_MIN_PARAM_NAME, scaleMin);
		mapGen.setParameter(COLOR_SCALE_MAX_PARAM_NAME, scaleMax);
		mapGen.setParameter(GRID_SPACING_PARAM_NAME, spacing);
		CPTParameter cptParam = (CPTParameter) mapGen.getAdjustableParamsList()
				.getParameter(CPT_PARAM_NAME);
		cptParam.setValue(cpt.getFileName());
		mapGen.setParameter(LOG_PLOT_NAME, false);
		
		try {
			GMT_Map map = mapGen.getGMTMapSpecification(xyz);
			map.setCustomLabel(title);
			map.setRescaleCPT(smooth);
			if (showFaults) {
//				addFaultTraces(FaultModels.FM2_1, map, Color.BLUE);
				addFaultTraces(FaultModels.FM3_1, map, Color.BLACK);
//				addFaultTraces(FaultModels.FM3_2, map, Color.BLACK);
			}
			NSHMP_PlotUtils.makeMap(map, mapGen, "No metadata", dlDir);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


}
