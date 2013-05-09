package scratch.peter.ucerf3.calc;

import static org.opensha.nshmp2.util.Period.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.data.Site;
import org.opensha.commons.geo.Location;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.calc.ERF_ID;
import org.opensha.nshmp2.calc.HazardCalc;
import org.opensha.nshmp2.calc.HazardResult;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.utils.FaultSystemIO;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class TestCalc {

	private static final String S = File.separator;

	TestCalc(String solSetPath, List<Location> locs,
		Period period, boolean epi) {
		UCERF3_FaultSysSol_ERF erf = UC3_CalcUtils.getUC3_ERF(solSetPath,
			IncludeBackgroundOption.EXCLUDE, false, true, 1.0);
		erf.updateForecast();
		EpistemicListERF wrappedERF = ERF_ID.wrapInList(erf);
		Stopwatch sw = new Stopwatch();
		for (Location loc : locs) {
			System.out.println("Starting calc for " + loc);
			sw.reset().start();
			Site site = new Site(loc);
			HazardCalc hc = HazardCalc.create(wrappedERF, site, period, epi);
			HazardResult result = hc.call();
			System.out.println(result.curve());
			System.out.println("Compute time: " + sw.stop().elapsed(TimeUnit.SECONDS));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Period period = GM0P00;
		String solSetPath = "/Users/pmpowers/projects/OpenSHA/tmp/UC33/src/bravg/2013_05_03-ucerf3p3-production-first-five_MEAN_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";

		boolean epi = false;

		List<Location> locs = Lists.newArrayList(
			NEHRP_TestCity.LOS_ANGELES.location()
			);

		try {
			new TestCalc(solSetPath, locs, period, epi);
//			mendoTest(solSetPath);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}
	
	private static void mendoTest(String path) throws Exception {
		
		File file = new File(path);
		FaultSystemSolution fss = FaultSystemIO.loadSol(file);
		for(int s=0; s<fss.getRupSet().getNumSections(); s++) {
			System.out.println(fss.getRupSet().getFaultSectionData(s).getName());
		}
	}

}
