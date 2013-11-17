package scratch.UCERF3.analysis;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityOptions;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.logicTree.LogicTreeBranch;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MPJ_ERF_ProbGainCalc extends MPJTaskCalculator {
	
	private CompoundFaultSystemSolution cfss;
	private List<LogicTreeBranch> branches;
	private FaultSystemSolutionERF erf;
	
	private File outputDir;

	public MPJ_ERF_ProbGainCalc(CommandLine cmd) throws ZipException, IOException {
		super(cmd);
		File compoundFile = new File(cmd.getOptionValue("cfss"));
		Preconditions.checkArgument(compoundFile.exists(),
				"Compound file doesn't exist: "+compoundFile.getAbsolutePath());
		cfss = CompoundFaultSystemSolution.fromZipFile(compoundFile);
		branches = Lists.newArrayList(cfss.getBranches());
		Collections.sort(branches);
		
		erf = new FaultSystemSolutionERF();
		
		if (cmd.hasOption("averi") || cmd.hasOption("nts")) {
			Preconditions.checkState(cmd.hasOption("averi") && cmd.hasOption("nts"),
					"must specify both averaging options together");
			boolean aveRI = Boolean.parseBoolean(cmd.getOptionValue("averi"));
			boolean aveNTS = Boolean.parseBoolean(cmd.getOptionValue("nts"));
			erf.testSetBPT_CalcType(aveRI, aveNTS);
		}
		
		erf.getTimeSpan().setDuration(Double.parseDouble(cmd.getOptionValue("duration")));
		
		if (cmd.hasOption("cov")) {
			MagDependentAperiodicityOptions cov = MagDependentAperiodicityOptions.valueOf(cmd.getOptionValue("cov"));
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
			erf.setParameter(MagDependentAperiodicityParam.NAME, cov);
		}
		
		outputDir = new File(cmd.getOptionValue("dir"));
		if (rank == 0)
			Preconditions.checkState(outputDir.exists() || outputDir.mkdirs());
	}

	@Override
	protected int getNumTasks() {
		return branches.size();
	}

	@Override
	protected void calculateBatch(int[] batch) throws Exception {
		for (int index : batch) {
			LogicTreeBranch branch = branches.get(index);
			String name = branch.buildFileName();
			
			FaultSystemSolution sol = cfss.getSolution(branch);
			erf.setSolution(sol);
			
			File subOutputFile = new File(outputDir, name+"_subs.csv");
			File parentOutputFile = new File(outputDir, name+"_parents.csv");
			
			FaultSysSolutionERF_Calc.writeSubSectionTimeDependenceCSV(erf, subOutputFile);
			FaultSysSolutionERF_Calc.writeParentSectionTimeDependenceCSV(erf, parentOutputFile);
		}
	}

	@Override
	protected void doFinalAssembly() throws Exception {
		// TODO Auto-generated method stub

	}
	
	protected static Options createOptions() {
		Options options = MPJTaskCalculator.createOptions();
		
		Option cfssOption = new Option("cfss", "compound-sol", true, "Compound Fault System Solution File");
		cfssOption.setRequired(true);
		options.addOption(cfssOption);
		
		Option dirOption = new Option("dir", "output-dir", true, "Output Directory");
		dirOption.setRequired(true);
		options.addOption(dirOption);
		
		Option durationOption = new Option("dur", "duration", true, "Forecast Duration");
		durationOption.setRequired(true);
		options.addOption(durationOption);
		
		Option aperiodOption = new Option("cov", "aperiodicity", true, "Aperiodicity enum name");
		aperiodOption.setRequired(false);
		options.addOption(aperiodOption);
		
		Option aveRIOption = new Option("averi", "ave-ri", true, "Average RI's (boolean)");
		aveRIOption.setRequired(false);
		options.addOption(aveRIOption);
		
		Option aveNormTSOption = new Option("nts", "ave-norm-time-since", true, "Average Normalized Time Since (boolean)");
		aveNormTSOption.setRequired(false);
		options.addOption(aveNormTSOption);
		
		return options;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		args = MPJTaskCalculator.initMPJ(args);

		try {
			Options options = createOptions();

			CommandLine cmd = parse(options, args, MPJ_ERF_ProbGainCalc.class);
			
			MPJ_ERF_ProbGainCalc driver = new MPJ_ERF_ProbGainCalc(cmd);
			
			driver.run();
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

}
