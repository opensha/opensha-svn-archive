package scratch.peter.ucerf3.calc;

import static org.opensha.nshmp2.util.Period.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.ClassUtils;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.calc.ERF_ID;
import org.opensha.nshmp2.calc.HazardResultWriter;
import org.opensha.nshmp2.calc.HazardResultWriterSites;
import org.opensha.nshmp2.calc.ThreadedHazardCalc;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.peter.ucerf3.calc.UC3_CalcUtils;

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

	public UC3_CalcDriver(String solSetPath, int solIdx, String sitePath, String outDir,
		Period[] periods, boolean epiUncert)
			throws IOException, InterruptedException, ExecutionException {

		UCERF3_FaultSysSol_ERF erf = UC3_CalcUtils.getUC3_ERF(
			solSetPath, solIdx, IncludeBackgroundOption.INCLUDE, false, true, 1.0);
		erf.updateForecast();
		EpistemicListERF wrappedERF = ERF_ID.wrapInList(erf);
		Map<String, Location> siteMap = UC3_CalcUtils.readSiteFile(sitePath);
		LocationList locs = new LocationList();
		for (Location loc : siteMap.values()) {
			locs.add(loc);
		}
		
		for (Period period : periods) {
			String outPath = outDir + S + erf.getName() + S + period + S;
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
	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("USAGE: " +
					ClassUtils.getClassNameWithoutPackage(UC3_CalcDriver.class) +
					" <filepath> <sitefile> <erfIndex> <outDir>");
			System.exit(1);
		}
		
		String solSetPath = args[0];
		String siteFile = args[1];
		int erfIdx = Integer.parseInt(args[2]);
		String outDir = args[3];
		
		Period[] periods = { GM0P00, GM0P20, GM1P00, GM4P00 };
		Set<NEHRP_TestCity> cities = NEHRP_TestCity.getCA(); // EnumSet.of(LOS_ANGELES);
		boolean epiUnc = false;

		try {
			new UC3_CalcDriver(solSetPath, erfIdx, siteFile, outDir, periods, epiUnc);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}
	
}
