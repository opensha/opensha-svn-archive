package scratch.peter.ucerf3;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.opensha.commons.data.Site;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.calc.ERF_ID;
import org.opensha.nshmp2.calc.HazardCalc;
import org.opensha.nshmp2.calc.HazardResult;
import org.opensha.nshmp2.calc.UC3_CalcWrapper;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
public class SpeedTest {

	private static DateFormat timeFmt = SimpleDateFormat.getTimeInstance();
	
	private SpeedTest() {
		try {
//			String path = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip";
//			File fssZip = new File(COMPOUND_SOL_PATH);
//			SimpleFaultSystemSolution fss = SimpleFaultSystemSolution.fromZipFile(fssZip);
//			UCERF3_FaultSysSol_ERF erf = UC3_CalcWrapper.getUC3_ERF(fss);
			
			String path = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2012_10_29-tree-fm31_x7-fm32_x1_COMPOUND_SOL.zip";

			System.out.println("*** PRE GET COMPOUND " + timeFmt.format(System.currentTimeMillis()));
			CompoundFaultSystemSolution cfss = UC3_CalcWrapper.getCompoundSolution(path);
			System.out.println("*** POST GET COMPOUND " + timeFmt.format(System.currentTimeMillis()));

			System.out.println("*** PRE GET FSS " + timeFmt.format(System.currentTimeMillis()));
//			FaultSystemSolution fss = Iterables.get(cfss, 0);
			List<LogicTreeBranch> branches = Lists.newArrayList(cfss
				.getBranches());
			LogicTreeBranch branch = branches.get(0);
			System.out.println("*** GET FSS getSol START" + timeFmt.format(System.currentTimeMillis()));
			FaultSystemSolution fss = cfss.getSolution(branch);
			System.out.println("*** POST GET FSS " + timeFmt.format(System.currentTimeMillis()));


			System.out.println("*** PRE GET ERF " + timeFmt.format(System.currentTimeMillis()));
			UCERF3_FaultSysSol_ERF erf = getUC3_ERF(fss);
			System.out.println("*** POST GET ERF " + timeFmt.format(System.currentTimeMillis()));
			
			
//			erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.12);
			System.out.println("AleatoryMagAreaStdDevVal: " + erf.getParameter(AleatoryMagAreaStdDevParam.NAME).getValue());
			
			System.out.println("*** PRE UPDATE FORECAST " + timeFmt.format(System.currentTimeMillis()));
			erf.updateForecast();
			System.out.println("*** POST UPDATE FORECAST " + timeFmt.format(System.currentTimeMillis()));
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
	
	
	public static UCERF3_FaultSysSol_ERF getUC3_ERF(FaultSystemSolution fss) {
		System.out.println("********* GET ERF inv fss " + timeFmt.format(System.currentTimeMillis()));
		InversionFaultSystemSolution invFss = new InversionFaultSystemSolution(
			fss);
		System.out.println("********* GET ERF uc3 erf " + timeFmt.format(System.currentTimeMillis()));
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(invFss);
		System.out.println("********* GET ERF uc3 params " + timeFmt.format(System.currentTimeMillis()));
//		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.0);
		erf.getParameter(IncludeBackgroundParam.NAME).setValue(
			IncludeBackgroundOption.INCLUDE);
		erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME)
			.setValue(true);
		erf.getTimeSpan().setDuration(1d);
		System.out.println("********* GET ERF uc3 return " + timeFmt.format(System.currentTimeMillis()));
		return erf;
	}


}
