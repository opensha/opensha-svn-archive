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
import org.opensha.commons.util.ClassUtils;
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
 * Class handles UC3 branch hazard calculations at a list of locations.
 * Intended for use 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC3_CalcDriver {

	private static final String S = File.separator;

	UC3_CalcDriver(String solSetPath, int solIdx, String outDir,
		LocationList locs, Period[] periods, boolean epiUncert)
			throws IOException, InterruptedException, ExecutionException {

		FaultSystemSolution fss = null;
		String erfName = null;

		boolean compoundSol = solSetPath.contains("COMPOUND_SOL");
		
		if (compoundSol) {
			CompoundFaultSystemSolution cfss = UC3_CalcWrapper.getCompoundSolution(solSetPath);
			List<LogicTreeBranch> branches = Lists.newArrayList(cfss.getBranches());
			System.out.println("numsols: " + branches.size());
			LogicTreeBranch branch = branches.get(solIdx);
			fss = cfss.getSolution(branch);
			erfName = branch.buildFileName();
			
		} else { // average solution
			AverageFaultSystemSolution afss = UC3_CalcWrapper.getAvgSolution(solSetPath);
			System.out.println("numsols: " + afss.getNumSolutions());
			fss = afss.getSolution(solIdx);
			int ssIdx1 = StringUtils.lastIndexOf(solSetPath, "/");
			int ssIdx2 = StringUtils.lastIndexOf(solSetPath, ".");
			erfName = solSetPath.substring(ssIdx1, ssIdx2) + "_" + solIdx;

		}

		UCERF3_FaultSysSol_ERF erf = UC3_CalcWrapper.getUC3_ERF(fss);
		erf.updateForecast();
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


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("USAGE: " +
					ClassUtils.getClassNameWithoutPackage(UC3_CalcDriver.class) +
					" <filepath> <erfIndex> <outDir>");
			System.exit(1);
		}
		
		String solSetPath = args[0];
		int erfIdx = Integer.parseInt(args[1]);
		String outDir = args[2];
		
		Period[] periods = { GM0P00, GM0P20, GM1P00 };
		Set<NEHRP_TestCity> cities = NEHRP_TestCity.getCA(); // EnumSet.of(LOS_ANGELES);
		boolean epiUnc = false;

		LocationList locs = new LocationList();
		for (NEHRP_TestCity city : cities) {
			locs.add(city.location());
		}

		try {
			new UC3_CalcDriver(solSetPath, erfIdx, outDir, locs, periods, epiUnc);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

}
