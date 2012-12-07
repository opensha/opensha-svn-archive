package scratch.peter.nshmp;

import static com.google.common.base.Charsets.US_ASCII;
import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME;
import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME;
import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.CPT_PARAM_NAME;
import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.LOG_PLOT_NAME;
import static org.opensha.nshmp2.tmp.TestGrid.*;
import static org.opensha.nshmp2.util.Period.*;
import static scratch.UCERF3.enumTreeBranches.DeformationModels.*;
import static scratch.UCERF3.enumTreeBranches.FaultModels.*;
import static scratch.UCERF3.enumTreeBranches.InversionModels.*;
import static scratch.UCERF3.enumTreeBranches.MaxMagOffFault.*;
import static scratch.UCERF3.enumTreeBranches.MomentRateFixes.*;
import static scratch.UCERF3.enumTreeBranches.ScalingRelationships.*;
import static scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels.*;
import static scratch.UCERF3.enumTreeBranches.SpatialSeisPDF.*;
import static scratch.UCERF3.enumTreeBranches.TotalMag5Rate.*;
import static scratch.peter.curves.ProbOfExceed.*;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.DataUtils;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;

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
import scratch.peter.curves.ProbOfExceed;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC3_MapMaker {
	
	private static final String S = File.separator;
	private static final String LF = IOUtils.LINE_SEPARATOR;
	private static final String ROOT = "tmp/UC3maps/";
	private static final String SUFFIX = "_0p1";

	public static void main(String[] args) {
//		generateBranchList();
//		buildMaps();
		makeMultiBranchMap();
	}
	
	private static void makeMultiBranchMap() {
		TestGrid grid = CA_RELM;
		ProbOfExceed pe = PE2IN50;
		Period p = GM0P00;

//		String treeSrcDir = "FM-DM-MS-DSR-UV";
//		String branchList = "ref-U3-80.txt";
//		GeoDataSet over = mergeBranches(treeSrcDir, branchList, pe, grid, p);

//		String treeSrcDir2 = "FM-DM-MS-DSR-UV";
//		String branchList2 = "ref-U2-80.txt";
//		GeoDataSet under = mergeBranches(treeSrcDir2, branchList2, pe, grid, p);

//		String nshmpSrcDir = "nshmp_ca";
//		GeoDataSet under = nshmpReference(nshmpSrcDir, pe, grid, p);
		
		// =====
		
//		String treeSrcDir = "tree_refNoBG";
//		String branchList = "ref-U3-80.txt";
//		GeoDataSet over = mergeBranches(treeSrcDir, branchList, pe, grid, p);

//		String nshmpSrcDir = "nshmp_ca_nobg";
//		GeoDataSet under = nshmpReference(nshmpSrcDir, pe, grid, p);

		// =====
		
//		String treeSrcDir = "FM-DM-MS-DSR-UV";
//		String branchList = "ref-160.txt";
//		GeoDataSet over = mergeBranches(treeSrcDir, branchList, pe, grid, p);

//		String treeSrcDir2 = "FM-DM-MS-DSR-UV-M576-MX72";
//		String branchList2 = "ref-M576-MX72-160.txt";
//		GeoDataSet under = mergeBranches(treeSrcDir2, branchList2, pe, grid, p);

		// =====
		
//		String treeSrcDir2 = "FM-DM-MS-DSR-UV-M576-MX76";
//		String branchList2 = "ref-M576-MX76-U2-80.txt";
//		GeoDataSet over = mergeBranches(treeSrcDir2, branchList2, pe, grid, p);

//		String nshmpSrcDir = "nshmp_ca";
//		GeoDataSet under = nshmpReference(nshmpSrcDir, pe, grid, p);

		// =====
		
		String uc3srcDir = "uc3uc2mapTAP_0p1";
		GeoDataSet over = loadSingle(uc3srcDir, pe, grid, p);

		String nshmpSrcDir = "muc2up_fm2p1_nobg_0p1"; //"nshmp_ca_nobg";
		GeoDataSet under = loadSingle(nshmpSrcDir, pe, grid, p);

		GeoDataSet xyzout = GeoDataSetMath.divide(over, under);
		
		String dlDir = ROOT + "maps/UC3UC2MAPTAP-UC2FM2P1-nobg/PGA-10p50-log";
		makeRatioPlot(xyzout, grid.bounds(), dlDir, "10% in 50 : PGA", true);
		
//		GMT_CPT_Files cpt = NSHMP_PlotUtils.getCPT(GM0P00);
//		double[] minmax = NSHMP_PlotUtils.getRange(GM0P00);
//		String dlDir = "/Users/pmpowers/projects/OpenSHA/tmp/UC3maps/maps/UC3-NSHMP/PGA-2p50-map";
//		makeMapPlot(mapxyz, grid.bounds(), dlDir, "2% in 50 PGA (g)", minmax[0], minmax[1], cpt);

	}
	
	private static GeoDataSet loadSingle(String src, ProbOfExceed pe, 
			TestGrid grid, Period p) {
		File nshmp = new File(ROOT + "src" + S + src + S + grid + S + p + S + "curves.csv");
		CurveContainer nshmpcc = CurveContainer.create(nshmp, grid);
		GriddedRegion gr = grid.grid(0.1);
		return NSHMP_DataUtils.extractPE(nshmpcc, gr, pe);
	}
	
	private static GeoDataSet loadMulti(String srcDir, String branchList,
			ProbOfExceed pe, TestGrid grid, Period p) {
		File branchListFile = new File(ROOT + "bsets", branchList);
		List<String> branchNames = null;
		try {
			branchNames = Files.readLines(branchListFile, US_ASCII);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		// create wt list
		
		List<Double> wtList = Lists.newArrayList();
		for (String brName : branchNames) {
			LogicTreeBranch branch = LogicTreeBranch.fromFileName(brName);
			wtList.add(branch.getAprioriBranchWt());
		}
		double[] wts = DataUtils.asWeights(Doubles.toArray(wtList));
		
		
		String cPath = grid + S + p + S + "curves.csv";
		GriddedRegion gr = grid.grid(0.1);
		GeoDataSet mapxyz = null;
		
		int idx = 0;
		for (String brName : branchNames) {
			System.out.println(brName);
			String brID = brName + SUFFIX;
			String brPath = ROOT + "src" + S + srcDir + S + brID + S + cPath;
			File brFile = new File(brPath);
			CurveContainer cc = CurveContainer.create(brFile, grid);
			GeoDataSet xyz = NSHMP_DataUtils.extractPE(cc, gr, pe);
			xyz = NSHMP_GeoDataUtils.multiply(xyz, wts[idx++]);
			if (mapxyz == null) {
				mapxyz = xyz;
			} else {
				mapxyz = GeoDataSetMath.add(mapxyz, xyz);
			}
		}
		
		return mapxyz;
	}
	
	/*
	 * Used to make ratio maps for UCERF3.1 report 
	 */
	private static void buildMaps() {
		String mapGrpName = null;
		String refDir = null;
		String brDir = null;
		boolean log = true;
		try {
			mapGrpName = "FM31-[DM]-[MS]-TAP-87-76-U3";
			refDir = "FM-DM-MS-DSR";
			brDir = "FM-DM-MS-DSR";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "FM31-[DM]-[MS]-UNI-87-76-U3";
			refDir = "FM-DM-MS-DSR";
			brDir = "FM-DM-MS-DSR";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "FM32-[DM]-[MS]-TAP-87-76-U3";
			refDir = "FM-DM-MS-DSR";
			brDir = "FM-DM-MS-DSR";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "FM32-[DM]-[MS]-UNI-87-76-U3";
			refDir = "FM-DM-MS-DSR";
			brDir = "FM-DM-MS-DSR";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "[FM]-ZENG-S09M-[DSR]-87-76-U3";
			refDir = "FM-DM-MS-DSR";
			brDir = "FM-DM-MS-DSR";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "FM31-[DM]-S09M-TAP-87-76-U2";
			refDir = "FM-DM-MS-UV";
			brDir = "FM-DM-MS-UV";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "FM31-[DM]-S09M-TAP-87-76-U3";
			refDir = "FM-DM-MS-UV";
			brDir = "FM-DM-MS-DSR";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "FM32-[DM]-S09M-TAP-87-76-U2";
			refDir = "FM-DM-MS-UV";
			brDir = "FM-DM-MS-UV";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "FM32-[DM]-S09M-TAP-87-76-U3";
			refDir = "FM-DM-MS-UV";
			brDir = "FM-DM-MS-DSR";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "FM31-ZENG-S09M-TAP-[M5]-[MM]-U3";
			refDir = "FM-M5-MM";
			brDir = "FM-M5-MM";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

			mapGrpName = "FM32-ZENG-S09M-TAP-[M5]-[MM]-U3";
			refDir = "FM-M5-MM";
			brDir = "FM-M5-MM";
			makeRatioMaps(refDir, brDir, mapGrpName, log);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	// make ratio maps from list of logic tree branches
	
	// for each map set need list and reference
	
	// dir = FM-DM-MS-DSR
	// FM31-[DM]-[MS]-TAP-87-76-U3 / FM31-ZENG-S09M-TAP-87-76-U3
	// FM31-[DM]-[MS]-UNI-87-76-U3 / FM31-ZENG-S09M-UNI-87-76-U3
	// FM32-[DM]-[MS]-TAP-87-76-U3 / FM32-ZENG-S09M-TAP-87-76-U3
	// FM32-[DM]-[MS]-UNI-87-76-U3 / FM32-ZENG-S09M-UNI-87-76-U3
	
	// dir = FM-DM-MS-DSR
	// [FM]-ZENG-S09M-[DSR]-87-76-U3 / FM31-ZENG-S09M-TAP-87-76-U3
	
	// dir = FM-DM-MS-UV (for U2) FM-DM-MS-DSR (for U3)
	// FM31-[DM]-S09M-TAP-87-76-U2 / FM31-ZENG-S09M-TAP-87-76-U2
	// FM31-[DM]-S09M-TAP-87-76-U3 / FM31-ZENG-S09M-TAP-87-76-U2
	// FM32-[DM]-S09M-TAP-87-76-U2 / FM32-ZENG-S09M-TAP-87-76-U2
	// FM32-[DM]-S09M-TAP-87-76-U3 / FM32-ZENG-S09M-TAP-87-76-U2
	
	// dir = FM-M5-MM
	// FM31-ZENG-S09M-TAP-[M5]-[MM]-U3 / FM31-ZENG-S09M-TAP-87-76-U3
	// FM32-ZENG-S09M-TAP-[M5]-[MM]-U3 / FM32-ZENG-S09M-TAP-87-76-U3
	
	private static void makeRatioMaps(String refDir, String brDir,
			String listName, boolean log) throws IOException {
		
		File branchListFile = new File(ROOT + "bsets", listName + ".txt");
		List<String> branchNames = Files.readLines(branchListFile, US_ASCII);
		TestGrid grid = CA_RELM;
		ProbOfExceed pe = PE2IN50;
		
		// reference branch is first in list
		// make ratios of all subsequent branches
		String refID = branchNames.get(0) + SUFFIX;
		String cPath = grid + S + GM0P00 + S + "curves.csv";
		String refPath = ROOT + "src" + S + refDir + S + refID + S + cPath;
		File refFile = new File(refPath);
		
		for (String brName : Iterables.skip(branchNames, 1)) {
			String dlDir = ROOT + "maps" + S + listName + (log ? "-log" : "") + S + brName + S;
			String brID = brName + SUFFIX;
			String brPath = ROOT + "src" + S + brDir + S + brID + S + cPath;
			File brFile = new File(brPath);
			
			makeRatioMap(dlDir, brFile, refFile, grid, pe, "Ratio", log);
		}
		
	}
	
	private static void makeRatioMap(String dlDir, File fOver, File fUnder,
			TestGrid grid, ProbOfExceed pe, String title,
			boolean log) {
		
		GriddedRegion gr = grid.grid(0.1);
		CurveContainer cc = null;
		cc = CurveContainer.create(fOver, grid);
		GeoDataSet xyzOver = NSHMP_DataUtils.extractPE(cc, gr, pe);
		cc = CurveContainer.create(fUnder, grid);
		GeoDataSet xyzUnder = NSHMP_DataUtils.extractPE(cc, gr, pe);
		
		GeoDataSet xyz = GeoDataSetMath.divide(xyzOver, xyzUnder);
		makeRatioPlot(xyz, grid.bounds(), dlDir, title, log);
	}
		
	private static void makeRatioPlot(GeoDataSet xyz, double[] bounds,
			String dlDir, String title, boolean log) {
		double scale = log ? 0.3 : 0.2;
		GMT_MapGenerator map = NSHMP_PlotUtils.create(bounds);
		map.setParameter(COLOR_SCALE_MIN_PARAM_NAME, log ? -scale : 1-scale);
		map.setParameter(COLOR_SCALE_MAX_PARAM_NAME, log ? scale : 1+scale);
		CPTParameter cptParam = (CPTParameter) map.getAdjustableParamsList()
				.getParameter(CPT_PARAM_NAME);
		GMT_CPT_Files cpt = log ? GMT_CPT_Files.UCERF3_RATIOS : GMT_CPT_Files.GMT_POLAR;
		cptParam.setValue(cpt.getFileName());
		map.setParameter(LOG_PLOT_NAME, log ? true : false);
		
		try {
			NSHMP_PlotUtils.makeMap(xyz, map, title, "No metadata", dlDir);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private static void makeMapPlot(GeoDataSet xyz, double[] bounds,
			String dlDir, String title, double scaleMin, double scaleMax, GMT_CPT_Files cpt) {
		GMT_MapGenerator map = NSHMP_PlotUtils.create(bounds);
		map.setParameter(COLOR_SCALE_MIN_PARAM_NAME, scaleMin);
		map.setParameter(COLOR_SCALE_MAX_PARAM_NAME, scaleMax);
		CPTParameter cptParam = (CPTParameter) map.getAdjustableParamsList()
				.getParameter(CPT_PARAM_NAME);
		cptParam.setValue(cpt.getFileName());
		map.setParameter(LOG_PLOT_NAME, false);
		
		try {
			NSHMP_PlotUtils.makeMap(xyz, map, title, "No metadata", dlDir);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}



	private static void generateBranchList() {
		// dir = FM-DM-MS-DSR
		// FM31-[DM]-[MS]-TAP-87-76-U3 / FM31-ZENG-S09M-TAP-87-76-U3
		// FM31-[DM]-[MS]-UNI-87-76-U3 / FM31-ZENG-S09M-UNI-87-76-U3
		// FM32-[DM]-[MS]-TAP-87-76-U3 / FM32-ZENG-S09M-TAP-87-76-U3
		// FM32-[DM]-[MS]-UNI-87-76-U3 / FM32-ZENG-S09M-UNI-87-76-U3
		
		// dir = FM-DM-MS-DSR
		// [FM]-ZENG-S09M-[DSR]-87-76-U3 / FM31-ZENG-S09M-TAP-87-76-U3
		
		// dir = FM-DM-MS-UV (for U2) FM-DM-MS-DSR (for U3)
		// FM31-[DM]-S09M-TAP-87-76-U2 / FM31-ZENG-S09M-TAP-87-76-U2
		// FM31-[DM]-S09M-TAP-87-76-U3 / FM31-ZENG-S09M-TAP-87-76-U2
		// FM32-[DM]-S09M-TAP-87-76-U2 / FM32-ZENG-S09M-TAP-87-76-U2
		// FM32-[DM]-S09M-TAP-87-76-U3 / FM32-ZENG-S09M-TAP-87-76-U2
		
		// dir = FM-M5-MM
		// FM31-ZENG-S09M-TAP-[M5]-[MM]-U3 / FM31-ZENG-S09M-TAP-87-76-U3
		// FM32-ZENG-S09M-TAP-[M5]-[MM]-U3 / FM32-ZENG-S09M-TAP-87-76-U3

		
		String fileName = "ref-M576-MX72-U2-80";
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
			UNIFORM, TAPERED); //UNIFORM, TAPERED);
		Set<InversionModels> invModels = EnumSet.of(
			CHAR_CONSTRAINED);
		Set<TotalMag5Rate> totM5rate = EnumSet.of(
			RATE_7p6); //RATE_7p6, RATE_8p7, RATE_10p0);
		Set<MaxMagOffFault> mMaxOff = EnumSet.of(
			MAG_7p2); // MAG_7p2, MAG_7p6, MAG_8p0);
		Set<MomentRateFixes> momentFix = EnumSet.of(
			NONE);
		Set<SpatialSeisPDF> spatialSeis = EnumSet.of(
			UCERF2); // UCERF2, UCERF3);

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
		File out = new File("tmp/UC3maps/bsets", fileName + ".txt");
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

	
	// make composite map across FM-DM-MS-DSR
	// need to get weight for each map and normalize wprior to summing
	

}
