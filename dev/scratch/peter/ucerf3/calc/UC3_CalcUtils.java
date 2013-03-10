package scratch.peter.ucerf3.calc;

import static com.google.common.base.Charsets.US_ASCII;
import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UC3_CalcUtils {

	private static final Splitter SPLIT = Splitter.on(',');

	/**
	 * Loads a single solution at solPath.
	 * @param solPath
	 * @param bgOpt 
	 * @param aleatoryMagArea 
	 * @param filterAftShk 
	 * @param duration 
	 * @return a UC3 erf
	 */
	public static UCERF3_FaultSysSol_ERF getUC3_ERF(
			String solPath,
			IncludeBackgroundOption bgOpt,
			boolean aleatoryMagArea,
			boolean filterAftShk,
			double duration) {
		
		FaultSystemSolution fss = getSolution(solPath);
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(fss);
		initUC3(erf, bgOpt, aleatoryMagArea, filterAftShk, duration);
		erf.setName(nameFromPath(solPath));
		return erf;
	}
	
	/**
	 * Assumes solPath points to either an 'average' fss or a COMPOUND_SOL. 
	 * @param solPath
	 * @param idx
	 * @param bgOpt 
	 * @param aleatoryMagArea 
	 * @param filterAftShk 
	 * @param duration 
	 * @return a UC3 erf
	 */
	public static UCERF3_FaultSysSol_ERF getUC3_ERF(
			String solPath,
			int idx,
			IncludeBackgroundOption bgOpt,
			boolean aleatoryMagArea,
			boolean filterAftShk,
			double duration) {
		
		FaultSystemSolution fss = null;
		String erfName = null;
		boolean compoundSol = solPath.contains("COMPOUND_SOL");
		
		if (compoundSol) {
			checkArgument(idx != -1, "Index cannot be -1 for compound sol.");
			CompoundFaultSystemSolution cfss = getCompoundSolution(solPath);
			List<LogicTreeBranch> branches = Lists.newArrayList(cfss
				.getBranches());
			LogicTreeBranch branch = branches.get(idx);
			fss = cfss.getSolution(branch);
			erfName = branch.buildFileName();
		} else {
			AverageFaultSystemSolution afss = getAvgSolution(solPath);
			fss = (idx == -1) ? afss : afss.getSolution(idx);
			erfName = nameFromPath(solPath) + "_" + idx;
		}

		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(fss);
		erf.setName(erfName);
		initUC3(erf, bgOpt, aleatoryMagArea, filterAftShk, duration);
		return erf;
	}

	/**
	 * Assumes solPath points to a COMPOUND_SOL
	 * @param solPath
	 * @param branchID
	 * @param bgOpt 
	 * @param aleatoryMagArea 
	 * @param filterAftShk 
	 * @param duration 
	 * @return a UC3 erf
	 */
	public static UCERF3_FaultSysSol_ERF getUC3_ERF(
			String solPath,
			String branchID,
			IncludeBackgroundOption bgOpt,
			boolean aleatoryMagArea,
			boolean filterAftShk,
			double duration) {
		
		CompoundFaultSystemSolution cfss = getCompoundSolution(solPath);
		LogicTreeBranch branch = LogicTreeBranch.fromFileName(branchID);
		FaultSystemSolution fss = cfss.getSolution(branch);
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(fss);
		erf.setName(branchID);
		initUC3(erf, bgOpt, aleatoryMagArea, filterAftShk, duration);
		return erf;
	}
	
	/**
	 * Convert FSS to UC3_FSS
	 * @param fss
	 * @return a UC3 fss
	 */
//	public static UCERF3_FaultSysSol_ERF toUC3(FaultSystemSolution fss) {
//		InversionFaultSystemSolution invFss = new InversionFaultSystemSolution(
//		fss);
//		return new UCERF3_FaultSysSol_ERF(invFss);
//	}
	
	private static String nameFromPath(String solPath) {
		int ssIdx1 = StringUtils.lastIndexOf(solPath, "/");
		int ssIdx2 = StringUtils.lastIndexOf(solPath, ".");
		return solPath.substring(ssIdx1, ssIdx2);
	}
	
	/**
	 * Initialize a UC3 FSS
	 * @param erf
	 * @param bg
	 * @param aleatoryMagArea
	 * @param filterAftershocks
	 * @param duration
	 */
	@SuppressWarnings("unchecked")
	public static void initUC3(
		UCERF3_FaultSysSol_ERF erf,
		IncludeBackgroundOption bg,
		boolean aleatoryMagArea,
		boolean filterAftershocks,
		double duration) {
		
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(bg);
		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(
			aleatoryMagArea ? 0.12 : 0.0);
		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME)
			.setValue(filterAftershocks);
		erf.getTimeSpan().setDuration(duration);
	}

	/**
	 * Returns an average fault system solution at the specified path.
	 * @param path
	 * @return an AFSS
	 */
	public static SimpleFaultSystemSolution getSolution(String path) {
		try {
			File file = new File(path);
			return SimpleFaultSystemSolution.fromZipFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns an average fault system solution at the specified path.
	 * @param path
	 * @return an AFSS
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
	 * @return a CFSS
	 */
	public static CompoundFaultSystemSolution getCompoundSolution(String path) {
		try {
			File file = new File(path);
			return CompoundFaultSystemSolution.fromZipFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Reads a text file with lines of the format:
	 * 		SITE_NAME,34.0,-117.0
	 * 
	 * @param path
	 * @return a {@code Map} of site names and their locations
	 * @throws IOException
	 */
	public static Map<String, Location> readSiteFile(String path) throws IOException {
		File f = new File(path);
		List<String> lines = Files.readLines(f, US_ASCII);
		Map<String, Location> siteMap = Maps.newHashMap();
		for (String line : lines) {
			if (line.startsWith("#")) continue;
			Iterator<String> it = SPLIT.split(line).iterator();
			String name = it.next();
			double lat = Double.parseDouble(it.next());
			double lon = Double.parseDouble(it.next());
			siteMap.put(name, new Location(lat, lon));
		}
		return ImmutableMap.copyOf(siteMap);
	}


}
