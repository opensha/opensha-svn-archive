package org.opensha.nshmp2.calc;

import static org.opensha.nshmp.NEHRP_TestCity.*;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC3_CalcWrapper {

	public static final String COMPOUND_SOL_PATH = 
			"/Users/pmpowers/projects/OpenSHA/tmp/invSols/compound/2012_10_10-fm3-ref-branch-vars-redo_COMPOUND_SOL.zip";
//	static final String COMPOUND_SOL_PATH = 
//			"/Users/pmpowers/projects/OpenSHA/tmp/invSols/compound/2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL.zip";

	private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2";
	// private static final String OUT_DIR = "/Volumes/Scratch/rtgm/UC3";
	// private static final String OUT_DIR = "";
	private static final String S = File.separator;

	//
	UC3_CalcWrapper(EpistemicListERF erf, LocationList locs, Period period,
		boolean epiUncert, HazardResultWriter writer) {

		ThreadedHazardCalc thc = null;

		try {
			thc = new ThreadedHazardCalc(erf, locs, period, epiUncert, writer);
			thc.calculate(null);
		} catch (ExecutionException ee) {
			ee.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// HazardCalcWrapper(TestGrid grid, Period period, File out) {
	// this(grid.grid().getNodeList(), period, out);
	// }

	// HazardCalcWrapper(File config) {
	// try {
	// HazardCalcConfig hcConfig = new HazardCalcConfig(config);
	// this(hcConfig.grid, hcConfig.period, hc.name);
	// }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Period period = Period.GM1P00;
			CompoundFaultSystemSolution cfss = getCompoundSolution(COMPOUND_SOL_PATH);
			Iterable<LogicTreeBranch> branches = cfss.getBranches();
			LogicTreeBranch branch = Iterables.get(branches, 5);
			String erfName = branch.buildFileName();
			System.out.println("doing: " + erfName);

			FaultSystemSolution fss = cfss.getSolution(branch);

			Stopwatch sw = new Stopwatch();
			sw.start();
			UCERF3_FaultSysSol_ERF erf = getUC3_ERF(fss);
			sw.stop();
			System.out.println("init time: " + sw.elapsedMillis());

			String outPath = OUT_DIR + S + erfName + S + period + S;
			File outFile = new File(outPath + "NSHMP08_WUS_curves.csv");
			// File mpjOutDir = new File(outPath);

			HazardResultWriter writer = new HazardResultWriterCities(outFile,
				period);
			// try {
			// writer = singleFile ?
			// new HazardResultWriterLocal(localOutFile, period) :
			// new HazardResultWriterMPJ(mpjOutDir);
			// } catch (IOException ioe) {
			// ioe.printStackTrace();
			// }
			boolean epiUnc = false;

			Set<NEHRP_TestCity> cities = EnumSet.of(LOS_ANGELES);
			LocationList locs = new LocationList();
			for (NEHRP_TestCity city : cities) {
				locs.add(city.location());
			}

			EpistemicListERF wrappedERF = ERF_ID.wrapInList(erf);
			new UC3_CalcWrapper(wrappedERF, locs, period, epiUnc, writer);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static AverageFaultSystemSolution getMeanSolution(String path) {
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
	 * @param faultSysSolZipFile
	 * @return
	 */
	public static UCERF3_FaultSysSol_ERF getUC3_ERF(FaultSystemSolution fss) {
		InversionFaultSystemSolution invFss = new InversionFaultSystemSolution(fss);
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(invFss);
		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.0);
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME).setValue(true);
		erf.getTimeSpan().setDuration(1d);
		erf.updateForecast();
		return erf;
	}


}
