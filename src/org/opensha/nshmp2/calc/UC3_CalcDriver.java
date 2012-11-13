package org.opensha.nshmp2.calc;

import static com.google.common.base.Charsets.US_ASCII;
import static org.opensha.nshmp2.util.Period.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.ClassUtils;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.earthquake.EpistemicListERF;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.logicTree.LogicTreeBranch;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * Class handles UC3 branch hazard calculations at a list of locations.
 * Intended for use 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC3_CalcDriver {

	private static final String S = File.separator;
	private static final Splitter SPLIT = Splitter.on(',');

	UC3_CalcDriver(String solSetPath, int solIdx, String sitePath, String outDir,
		Period[] periods, boolean epiUncert)
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
		Map<String, Location> siteMap = readSiteFile(sitePath);
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
		
		Period[] periods = { GM0P00, GM0P20, GM1P00 };
		Set<NEHRP_TestCity> cities = NEHRP_TestCity.getCA(); // EnumSet.of(LOS_ANGELES);
		boolean epiUnc = false;

		try {
			new UC3_CalcDriver(solSetPath, erfIdx, siteFile, outDir, periods, epiUnc);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}
	
	private Map<String, Location> readSiteFile(String path) throws IOException {
		File f = new File(path);
		List<String> lines = Files.readLines(f, US_ASCII);
		Map<String, Location> siteMap = Maps.newHashMap();
		for (String line : lines) {
			Iterator<String> it = SPLIT.split(line).iterator();
			String name = it.next();
			double lat = Double.parseDouble(it.next());
			double lon = Double.parseDouble(it.next());
			siteMap.put(name, new Location(lat, lon));
		}
		return ImmutableMap.copyOf(siteMap);
	}

}
