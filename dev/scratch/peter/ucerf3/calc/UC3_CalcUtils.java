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
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.VariableLogicTreeBranch;
import scratch.UCERF3.utils.FaultSystemIO;

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
	 * Loads a single solution. This could be an 'averaged' solution (e.g. as
	 * used for inversion convergence analyses) assuming you you want averaged
	 * rupture rates (for a single solution from the averaged one use the
	 * constructor that takes and index argument). This could also be a solution
	 * for a single logic tree branch or a 'branch averaged' solution.
	 * 
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
		
		InversionFaultSystemSolution fss = getSolution(solPath);
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(fss);
		erf.setName(nameFromPath(solPath));
		initUC3(erf, bgOpt, aleatoryMagArea, filterAftShk, duration);
		return erf;
	}
	
	/**
	 * Loads one solution of an 'averaged' or mean solution. An average solution
	 * represents multiple runs of the same logic tree branch. If the supplied
	 * index is -1, then the supplied solution (with mean rupture rates) is used
	 * to initialize the ERF.
	 * 
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
		
		AverageFaultSystemSolution afss = getAvgSolution(solPath);
		InversionFaultSystemSolution fss = (idx == -1) ? afss : afss.getSolution(idx);
		String erfName = nameFromPath(solPath) + "_" + idx;
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(fss);
		erf.setName(erfName);
		initUC3(erf, bgOpt, aleatoryMagArea, filterAftShk, duration);
		return erf;
	}

	/**
	 * Loads one solution of an 'averaged' or mean solution. An average solution
	 * represents multiple runs of the same logic tree branch. If the supplied
	 * index is -1, then the supplied solution (with mean rupture rates) is used
	 * to initialize the ERF.
	 * 
	 * @param solPath
	 * @param idx
	 * @param bgOpt
	 * @param aleatoryMagArea
	 * @param filterAftShk
	 * @param duration
	 * @return a UC3 erf
	 */
	public static UCERF3_FaultSysSol_ERF getUC3_ERF_Compound(
			String solPath,
			int idx,
			IncludeBackgroundOption bgOpt,
			boolean aleatoryMagArea,
			boolean filterAftShk,
			double duration) {
		
		checkArgument(idx != -1, "Index cannot be -1 for compound sol.");
		CompoundFaultSystemSolution cfss = getCompoundSolution(solPath);
		List<LogicTreeBranch> branches = Lists.newArrayList(cfss
			.getBranches());
		LogicTreeBranch branch = branches.get(idx);
		InversionFaultSystemSolution ifss = cfss.getSolution(branch);
		String erfName = branch.buildFileName();
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(ifss);
		erf.setName(erfName);
		initUC3(erf, bgOpt, aleatoryMagArea, filterAftShk, duration);
		return erf;
	}

	/**
	 * Loads a 'compound' solution. Such a solution generally has 'COMPOUND_SOL'
	 * included in its name and represents multiple logic tree branches wrapped
	 * up together. {@code branchID} is a {@code String} that is the file name
	 * used to identify a branch (see {@link LogicTreeBranch#buildFileName()}.
	 * 
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
		InversionFaultSystemSolution fss = null;
		LogicTreeBranch branch = null;
		try {
			branch = LogicTreeBranch.fromFileName(branchID);
			fss = cfss.getSolution(branch);
		} catch (Exception e) {
			// try to handle as eqn set var logic tree brnach name instead
			branch = VariableLogicTreeBranch.fromFileName(branchID);
			fss = cfss.getSolution(branch);
		}
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(fss);
		erf.setName(branchID);
		initUC3(erf, bgOpt, aleatoryMagArea, filterAftShk, duration);
		return erf;
	}
		
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
	public static InversionFaultSystemSolution getSolution(String path) {
		try {
			File file = new File(path);
			return FaultSystemIO.loadInvSol(file);
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
			return FaultSystemIO.loadAvgInvSol(file);
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
	
	public static List<String> readBranchFile(String path) throws IOException {
		File f = new File(path);
		return Files.readLines(f, US_ASCII);
	}


	public static void main(String[] args) {
//		String solPath = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2013_01_14-UC32-COMPOUND_SOL.zip";
//		String branch = "FM3_1_ABM_Shaw09Mod_DsrUni_CharConst_M5Rate7.6_MMaxOff7.2_NoFix_SpatSeisU2";
//		UCERF3_FaultSysSol_ERF erf = UC3_CalcUtils.getUC3_ERF(solPath,
//			branch, IncludeBackgroundOption.INCLUDE, false,
//			true, 1.0);
//		erf.updateForecast();
		String solPath = "tmp/UC33/src/bravg/2013_01_14-stampede_3p2_production_runs_fm3p1_dm_scale_subset_MEAN_BRANCH_AVG_SOL.zip";
		UCERF3_FaultSysSol_ERF erf = UC3_CalcUtils.getUC3_ERF(solPath,
			IncludeBackgroundOption.INCLUDE, false,
			true, 1.0);
		erf.updateForecast();

	}
}
