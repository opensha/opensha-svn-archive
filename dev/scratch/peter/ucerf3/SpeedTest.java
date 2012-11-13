package scratch.peter.ucerf3;

import java.io.File;

import org.opensha.commons.data.Site;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.calc.ERF_ID;
import org.opensha.nshmp2.calc.HazardCalc;
import org.opensha.nshmp2.calc.HazardResult;
import org.opensha.nshmp2.calc.UC3_CalcWrapper;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;

import com.google.common.base.Stopwatch;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class SpeedTest {

	static final String COMPOUND_SOL_PATH = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip";

	private SpeedTest() {
		try {
			File fssZip = new File(COMPOUND_SOL_PATH);
			SimpleFaultSystemSolution fss = SimpleFaultSystemSolution.fromZipFile(fssZip);
			UCERF3_FaultSysSol_ERF erf = UC3_CalcWrapper.getUC3_ERF(fss);
			erf.updateForecast();
//			erf.getParameter(IncludeBackgroundParam.NAME).setValue(
//			IncludeBackgroundOption.ONLY);
			EpistemicListERF erfList = ERF_ID.wrapInList(erf);

			Site site = new Site(NEHRP_TestCity.LOS_ANGELES.location());
			Period period = Period.GM0P00;
			boolean epiUncert = false;
			
			Stopwatch sw = new Stopwatch();
			sw.start();
			
			HazardCalc calc = HazardCalc.create(erfList, site, period, epiUncert);
			HazardResult hr = calc.call();
			System.out.println(sw.stop().elapsedMillis());
			System.out.println(hr.curve());
			
//			return wrapInList(erf);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SpeedTest();

	}

}
