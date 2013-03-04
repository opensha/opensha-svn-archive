package scratch.peter.ucerf3.calc;

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
import org.opensha.nshmp2.calc.ERF_ID;
import org.opensha.nshmp2.calc.HazardResultWriter;
import org.opensha.nshmp2.calc.HazardResultWriterSites;
import org.opensha.nshmp2.calc.ThreadedHazardCalc;
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
public class UC3_LocalCalc {

//	static final String COMPOUND_SOL_PATH = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/compound/2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL.zip";
	static final String COMPOUND_SOL_PATH = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2012_10_14-fm3-logic-tree-sample-x5_run0_COMPOUND_SOL.zip";
//	static final String COMPOUND_SOL_PATH = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/FM3_1_ZENGBB_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip";
//	 private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2/test";
//	private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/UC3.2-conv-mean";
	private static final String OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/RTGM/data/UC3/tmp";
	private static final String S = File.separator;

	UC3_LocalCalc(String solSetPath, int solIdx, String outDir,
		Map<String, Location> siteMap, Period[] periods, boolean epi)
			throws IOException, InterruptedException, ExecutionException {

		UCERF3_FaultSysSol_ERF erf = UC3_CalcUtils.getUC3_ERF(
			solSetPath, solIdx, IncludeBackgroundOption.EXCLUDE,false, true, 1.0);
		erf.updateForecast();
		EpistemicListERF wrappedERF = ERF_ID.wrapInList(erf);
		LocationList locs = new LocationList();
		for (Location loc : siteMap.values()) {
			locs.add(loc);
		}
		
		for (Period period : periods) {
//			String outPath = outDir + S + erf.getName() + S + period + S;
			String outPath = outDir + S + period + S;
			System.out.println(outPath);
			File outFile = new File(outPath + "NSHMP08_WUS_curves.csv");
			HazardResultWriter writer = new HazardResultWriterSites(outFile,
				period, siteMap);
			ThreadedHazardCalc thc = new ThreadedHazardCalc(wrappedERF, locs,
				period, epi, writer);
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
		boolean epi = false;

		String sitePath = "/Users/pmpowers/projects/OpenSHA/tmp/curves/sites/test.txt";
		Map<String,Location> siteMap = UC3_CalcUtils.readSiteFile(sitePath);

		try {
			new UC3_LocalCalc(solSetPath, idx, OUT_DIR, siteMap, periods, epi);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

}
