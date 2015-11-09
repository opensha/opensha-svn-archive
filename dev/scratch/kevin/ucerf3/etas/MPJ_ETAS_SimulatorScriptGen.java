package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opensha.commons.geo.Location;
import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.StampedeScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;

import scratch.UCERF3.erf.ETAS.ETAS_Simulator.TestScenario;
import scratch.UCERF3.erf.ETAS.ETAS_Params.U3ETAS_MaxCharFactorParam;
import scratch.UCERF3.erf.ETAS.ETAS_Params.U3ETAS_ProbabilityModelOptions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MPJ_ETAS_SimulatorScriptGen {

	public static void main(String[] args) throws IOException {
		File localDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations");
		
		boolean stampede = true;
		int threads = 2;
		boolean smallTest = false;
		double duration = 1000;
		
//		Scenarios scenario = Scenarios.LA_HABRA;
//		Scenarios[] scenarios = Scenarios.values();
//		Scenarios[] scenarios = {Scenarios.MOJAVE_7};
//		Scenarios[] scenarios = {Scenarios.NAPA};
//		Scenarios[] scenarios = {Scenarios.SPONTANEOUS};
		
//		TestScenario[] scenarios = {TestScenario.MOJAVE_M7};
//		TestScenario[] scenarios = {TestScenario.MOJAVE_M5, TestScenario.MOJAVE_M5p5, TestScenario.MOJAVE_M6pt3_ptSrc,
//				TestScenario.MOJAVE_M6pt3_FSS, TestScenario.MOJAVE_M7, TestScenario.MOJAVE_M7pt4, TestScenario.MOJAVE_M7pt8};
//		TestScenario[] scenarios = {TestScenario.MOJAVE_M5p5, TestScenario.MOJAVE_M6pt3_ptSrc,
//				TestScenario.MOJAVE_M6pt3_FSS, TestScenario.MOJAVE_M7};
		TestScenario[] scenarios = { null };
//		U3ETAS_ProbabilityModelOptions[] probModels = U3ETAS_ProbabilityModelOptions.values();
//		U3ETAS_ProbabilityModelOptions[] probModels = {U3ETAS_ProbabilityModelOptions.FULL_TD,
//				U3ETAS_ProbabilityModelOptions.NO_ERT};
		U3ETAS_ProbabilityModelOptions[] probModels = {U3ETAS_ProbabilityModelOptions.FULL_TD};
//		U3ETAS_ProbabilityModelOptions[] probModels = {U3ETAS_ProbabilityModelOptions.POISSON};
//		boolean[] grCorrs = { false, true };
		boolean[] grCorrs = { false };
//		double[] maxCharFactors = { U3ETAS_MaxCharFactorParam.DEFAULT_VALUE };
//		double[] maxCharFactors = { 10 };
		boolean applyLongTermRates = false;
		
		String nameAdd = null;
//		String nameAdd = "launch-debug";
		
		boolean histCatalog;
		String queue;
		int startYear, numSims, nodes, mins;
		if (duration > 1) {
			// long simulation
			queue = null;
			startYear = 2012;
			histCatalog = true;
			numSims = 100;
			nodes = 15;
			mins = 24*60;
			Preconditions.checkState(!smallTest);
			Preconditions.checkState(scenarios.length == 1 && scenarios[0] == null);
		} else {
			histCatalog = false;
			startYear = 2014;
			if (smallTest) {
				queue = "development";
				numSims = 200;
				nodes = 10;
				mins = 2*60;
				if (nameAdd == null)
					nameAdd = "";
				else
					nameAdd += "-";
				nameAdd += "quick_test";
			} else {
				queue = null;
				numSims = 10000;
				nodes = 40;
				mins = 18*60;
			}
		}
		
		String dateStr = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
//		String dateStr = "2015_10_15";
		
		boolean timeIndep = false;
		
		boolean binary = numSims > 1000 || duration > 200;
		
		int memGigs;
		int ppn;
		if (stampede)
			ppn = 16;
		else
			ppn = 8;
		
		File remoteDir, remoteSolFile, cacheDir;
		FastMPJShellScriptWriter mpjWrite;
		BatchScriptWriter pbsWrite;
		
		if (stampede) {
			memGigs = 26;
			remoteDir = new File("/work/00950/kevinm/ucerf3/etas_sim");
			remoteSolFile = new File("/work/00950/kevinm/ucerf3/inversion/compound_plots/2013_05_10-ucerf3p3-production-10runs/"
					+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip");
			mpjWrite = new FastMPJShellScriptWriter(StampedeScriptWriter.JAVA_BIN, memGigs*1024,
					null, StampedeScriptWriter.FMPJ_HOME, false);
			mpjWrite.setUseLaunchWrapper(true);
			pbsWrite = new StampedeScriptWriter();
			cacheDir = new File(remoteDir, "cache_fm3p1_ba");
		} else {
			memGigs = 9;
			remoteDir = new File("/home/scec-02/kmilner/ucerf3/etas_sim");
			remoteSolFile = new File("/home/scec-02/kmilner/ucerf3/inversion_compound_plots/"
					+ "2013_05_10-ucerf3p3-production-10runs/"
					+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip");
			mpjWrite = new FastMPJShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, memGigs*1024,
					null, USC_HPCC_ScriptWriter.FMPJ_HOME, false);
			pbsWrite = new USC_HPCC_ScriptWriter();
			cacheDir = new File(remoteDir, "cache_fm3p1_ba");
		}
		
		File histCatalogFile = null;
		if (histCatalog)
			histCatalogFile = new File(remoteDir, "ofr2013-1165_EarthquakeCat.txt");
		
		mpjWrite.setAutoMemDetect(false);
		
		List<File> classpath = new ArrayList<File>();
		classpath.add(new File(remoteDir, "commons-cli-1.2.jar"));
		
		boolean exactDispatch = numSims / nodes == threads;
		
		for (TestScenario scenario : scenarios) {
			String scenarioName;
			if (scenario == null)
				scenarioName = "spontaneous";
			else
				scenarioName = scenario.name().toLowerCase();
			if (duration > 1d) {
				if (duration == Math.floor(duration))
					scenarioName += "-"+(int)duration+"yr";
				else
					scenarioName += "-"+(float)duration+"yr";
			}
			for (U3ETAS_ProbabilityModelOptions probModel : probModels) {
//				for (double maxCharFactor : maxCharFactors) {
				for (boolean grCorr : grCorrs) {
					String grStr;
					if (grCorr)
						grStr = "-grCorr";
					else
						grStr = "";
					String jobName = dateStr+"-"+scenarioName;
					if (nameAdd != null && !nameAdd.isEmpty())
						jobName += "-"+nameAdd;
//					jobName += "-"+probModel.name().toLowerCase()+"-maxChar"+(float)maxCharFactor;
					jobName += "-"+probModel.name().toLowerCase()+grStr;
					if (timeIndep)
						jobName += "-indep";
					if (!applyLongTermRates)
						jobName += "-noApplyLTR";
					
					File localJobDir = new File(localDir, jobName);
					if (!localJobDir.exists())
						localJobDir.mkdir();
					File remoteJobDir = new File(remoteDir, jobName);
					
					System.out.println(jobName);
					
					List<File> subClasspath = Lists.newArrayList(classpath);
					subClasspath.add(new File(remoteJobDir, "OpenSHA_complete.jar"));
					mpjWrite.setClasspath(subClasspath);
					
					File pbsFile = new File(localJobDir, jobName+".pbs");
					
					String argz;
					
					if (exactDispatch) {
						argz = "--min-dispatch "+threads+" --max-dispatch "+threads+" --exact-dispatch "+threads;
					} else {
						argz = "--min-dispatch 1 --max-dispatch "+threads;
					}
					
					argz += " --threads "+threads
							+" --num "+numSims+" --sol-file "+remoteSolFile.getAbsolutePath();
					
					argz += " --duration "+(float)duration+" --start-year "+startYear;
					
					argz += " --prob-model "+probModel.name();
					
//					argz += " --max-char-factor "+maxCharFactor;
					if (grCorr)
						argz += " --impose-gr";
					
					argz += " --apply-long-term-rates "+applyLongTermRates;
					
					if (timeIndep)
						argz += " --indep";
					
					if (binary)
						argz += " --binary";
					
					if (scenario != null) {
						if (scenario.getFSS_Index() >= 0)
							argz += " --trigger-rupture-id "+scenario.getFSS_Index();
						Location loc = scenario.getLocation();
						if (loc != null)
							argz += " --trigger-loc "+(float)loc.getLatitude()
								+","+(float)loc.getLongitude()+","+(float)loc.getDepth();
						if (scenario.getMagnitude() > 0)
							argz += " --trigger-mag "+(float)scenario.getMagnitude();
					}
					if (histCatalogFile != null)
						argz += " --trigger-catalog "+histCatalogFile.getAbsolutePath();
					
					argz += " "+cacheDir.getAbsolutePath()+" "+remoteJobDir.getAbsolutePath();
					
					List<String> script = mpjWrite.buildScript(MPJ_ETAS_Simulator.class.getName(), argz);
					
					script = pbsWrite.buildScript(script, mins, nodes, ppn, queue);
					pbsWrite.writeScript(pbsFile, script);
				}
			}
		}
	}

}
