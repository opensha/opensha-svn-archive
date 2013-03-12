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

import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;

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
			IncludeBackgroundOption.INCLUDE, false, true, 1.0);
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
		String solSetPath = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2013_01_14-UC32-MEAN_BRANCH_AVG_SOL_FM31.zip";

		boolean epi = false;

		List<Location> locs = Lists.newArrayList(
			NEHRP_TestCity.LOS_ANGELES.location()
			);

		try {
			new TestCalc(solSetPath, locs, period, epi);
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

}
