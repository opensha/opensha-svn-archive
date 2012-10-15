package org.opensha.nshmp2.calc;

import static org.opensha.nshmp.NEHRP_TestCity.*;
import static org.opensha.nshmp2.util.Period.*;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
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
	static final String COMPOUND_SOL_PATH = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/compound/2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL.zip ";

	// private static final String OUT_DIR =
	// "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2";
	private static final String OUT_DIR = "/Volumes/Scratch/rtgm/UC3tmp";
	private static final String S = File.separator;

	UC3_CalcWrapper(String solSetPath, int solIdx, String outDir,
		LocationList locs, Period[] periods, boolean epiUncert)
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
			fss = afss.getSolution(solIdx);
			int ssIdx1 = StringUtils.lastIndexOf(solSetPath, "/");
			int ssIdx2 = StringUtils.lastIndexOf(solSetPath, ".");
			erfName = solSetPath.substring(ssIdx1, ssIdx2) + "_" + solIdx;

		}

		UCERF3_FaultSysSol_ERF erf = getUC3_ERF(fss);
		EpistemicListERF wrappedERF = ERF_ID.wrapInList(erf);

		for (Period period : periods) {
			String outPath = outDir + S + erfName + S + period + S;
			System.out.println(outPath);
			File outFile = new File(outPath + "NSHMP08_WUS_curves.csv");
			HazardResultWriter writer = new HazardResultWriterCities(outFile,
				period);
			ThreadedHazardCalc thc = new ThreadedHazardCalc(wrappedERF, locs,
				period, epiUncert, writer);
			thc.calculate(null);
		}
	}

	// try {
	// writer = new HazardResultWriterCities(outFile, period);
	// } catch (IOException ioe) {
	// ioe.printStackTrace();
	// System.exit(1);
	// }

	// UC3_CalcWrapper(UCERF3_FaultSysSol_ERF erf, LocationList locs, Period[]
	// periods,
	// boolean epiUncert) {
	//
	// ThreadedHazardCalc thc = null;
	//
	//
	//
	// try {
	// thc = new ThreadedHazardCalc(erf, locs, period, epiUncert, writer);
	// thc.calculate(null);
	// } catch (ExecutionException ee) {
	// ee.printStackTrace();
	// } catch (InterruptedException ie) {
	// ie.printStackTrace();
	// } catch (IOException ioe) {
	// ioe.printStackTrace();
	// }
	// }

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Period[] periods = { GM0P00, GM0P20, GM1P00 };
		String solSetPath = COMPOUND_SOL_PATH;
		int idx = 3;
		Set<NEHRP_TestCity> cities = NEHRP_TestCity.getCA(); // EnumSet.of(LOS_ANGELES);
		boolean epiUnc = false;

		LocationList locs = new LocationList();
		for (NEHRP_TestCity city : cities) {
			locs.add(city.location());
		}

		try {
			new UC3_CalcWrapper(solSetPath, idx, OUT_DIR, locs, periods, epiUnc);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}

		// CompoundFaultSystemSolution cfss =
		// getCompoundSolution(COMPOUND_SOL_PATH);
		// Iterable<LogicTreeBranch> branches = cfss.getBranches();
		// LogicTreeBranch branch = Iterables.get(branches, 5);
		// String erfName = branch.buildFileName();
		//
		//
		// try {
		//
		// FaultSystemSolution fss = cfss.getSolution(branch);
		//
		// // Stopwatch sw = new Stopwatch();
		// // sw.start();
		// // sw.stop();
		// // System.out.println("init time: " + sw.elapsedMillis());
		//
		// UCERF3_FaultSysSol_ERF erf = getUC3_ERF(fss);
		// EpistemicListERF wrappedERF = ERF_ID.wrapInList(erf);
		//
		// String outPath = OUT_DIR + S + erfName + S + period + S;
		// File outFile = new File(outPath + "NSHMP08_WUS_curves.csv");
		// // File mpjOutDir = new File(outPath);
		//
		// HazardResultWriter writer = new HazardResultWriterCities(outFile,
		// period);
		// // try {
		// // writer = singleFile ?
		// // new HazardResultWriterLocal(localOutFile, period) :
		// // new HazardResultWriterMPJ(mpjOutDir);
		// // } catch (IOException ioe) {
		// // ioe.printStackTrace();
		// // }
		// boolean epiUnc = false;
		//
		// // Set<NEHRP_TestCity> cities = EnumSet.of(LOS_ANGELES);
		// // LocationList locs = new LocationList();
		// // for (NEHRP_TestCity city : cities) {
		// // locs.add(city.location());
		// // }
		//
		// new UC3_CalcWrapper(erf, locs, periods, epiUnc);
		//
		// } catch (IOException ioe) {
		// ioe.printStackTrace();
		// }
	}

	public static AverageFaultSystemSolution getAvgSolution(String path) {
		try {
			File file = new File(path);
			return AverageFaultSystemSolution.fromZipFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static CompoundFaultSystemSolution getCompoundSolution(String path) {
		try {
			File cfssFile = new File(path);
			return CompoundFaultSystemSolution.fromZipFile(cfssFile);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
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
		erf.updateForecast();
		return erf;
	}

}
