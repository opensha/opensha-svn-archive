package org.opensha.nshmp2.calc;

import static org.opensha.nshmp.NEHRP_TestCity.*;
import static org.opensha.nshmp2.util.Period.*;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC3_CalcWrapper {

	// public static final String COMPOUND_SOL_PATH =
	// "/Volumes/Scratch/UC3/compound/2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL.zip";
//	static final String COMPOUND_SOL_PATH = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/compound/2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL.zip";
//	static final String COMPOUND_SOL_PATH = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2012_10_14-fm3-logic-tree-sample-x5_run0_COMPOUND_SOL.zip";
	static final String COMPOUND_SOL_PATH = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_VarZeros_mean_sol.zip";
//	 private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2/test";
	private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/convVar0mean";
	private static final String S = File.separator;

	UC3_CalcWrapper(String solSetPath, int solIdx, String outDir,
		Map<String, Location> siteMap, Period[] periods, boolean epiUncert)
			throws IOException, InterruptedException, ExecutionException {

		FaultSystemSolution fss = null;
		String erfName = null;

		boolean compoundSol = solSetPath.contains("COMPOUND_SOL");

		if (compoundSol) {
			CompoundFaultSystemSolution cfss = getCompoundSolution(solSetPath);
			List<LogicTreeBranch> branches = Lists.newArrayList(cfss
				.getBranches());
			LogicTreeBranch branch = branches.get(solIdx);
			fss = cfss.getSolution(branch);
			erfName = branch.buildFileName();

		} else {
			AverageFaultSystemSolution afss = getAvgSolution(solSetPath);
			if (solIdx == -1) {
				fss = afss;
			} else {
				fss = afss.getSolution(solIdx);
			}
			int ssIdx1 = StringUtils.lastIndexOf(solSetPath, "/");
			int ssIdx2 = StringUtils.lastIndexOf(solSetPath, ".");
			erfName = solSetPath.substring(ssIdx1, ssIdx2) + "_" + solIdx;

		}

		UCERF3_FaultSysSol_ERF erf = getUC3_ERF(fss);
		erf.updateForecast();
		EpistemicListERF wrappedERF = ERF_ID.wrapInList(erf);
		LocationList locs = new LocationList();
		for (Location loc : siteMap.values()) {
			locs.add(loc);
		}
		
		for (Period period : periods) {
			String outPath = outDir + S + erfName + S + period + S;
			System.out.println(outPath);
			File outFile = new File(outPath + "NSHMP08_WUS_curves.csv");
			HazardResultWriter writer = new HazardResultWriterSites(outFile,
				period, siteMap);
			ThreadedHazardCalc thc = new ThreadedHazardCalc(wrappedERF, locs,
				period, epiUncert, writer);
			thc.calculate(null);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		Period[] periods = { GM0P00, GM0P20, GM1P00 };
		String solSetPath = COMPOUND_SOL_PATH;
		int idx = -1;
		boolean epiUnc = false;

		String sitePath = "/Users/pmpowers/projects/OpenSHA/tmp/curves/sites/SRPsites1.txt";
		Map<String,Location> siteMap = UC3_CalcDriver.readSiteFile(sitePath);
//		Map<String,Location> siteMap = NEHRP_TestCity.asMap();
//		siteMap.clear();
//		NEHRP_TestCity city = NEHRP_TestCity.LOS_ANGELES;
//		siteMap.put(city.name(), city.location());

		try {
			new UC3_CalcWrapper(solSetPath, idx, OUT_DIR, siteMap, periods, epiUnc);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Returns an average fault system solution at the specified path.
	 * @param path
	 * @return
	 */
	public static AverageFaultSystemSolution getAvgSolution(String path) {
		try {
			File file = new File(path);
			return AverageFaultSystemSolution.fromZipFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * Returns a compound fault system solution at the specified path.
	 * @param path
	 * @return
	 */
	public static CompoundFaultSystemSolution getCompoundSolution(String path) {
		try {
			File cfssFile = new File(path);
			return CompoundFaultSystemSolution.fromZipFile(cfssFile);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * Returns an inversion based ERF for the supplied fault system solution.
	 * Assumes the supplied FSS is an inversion solution.
	 * 
	 * @param faultSysSolZipFile
	 * 
	 * @return
	 */
	public static UCERF3_FaultSysSol_ERF getUC3_ERF(FaultSystemSolution fss) {
		InversionFaultSystemSolution invFss = new InversionFaultSystemSolution(
			fss);
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(invFss);
		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.0);
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(
			IncludeBackgroundOption.INCLUDE);
		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME)
			.setValue(true);
		erf.getTimeSpan().setDuration(1d);
		return erf;
	}

}
