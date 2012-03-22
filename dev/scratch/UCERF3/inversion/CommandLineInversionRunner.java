package scratch.UCERF3.inversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.LogicTreeBranch;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.ProgressTrackingCompletionCriteria;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.PaleoProbabilityModel;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_MFD_ConstraintFetcher.TimeAndRegion;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class CommandLineInversionRunner {
	
	public enum InversionOptions {
		DEFAULT_ASEISMICITY("aseis", "default-aseis", "Aseis", true,
				"Default Aseismicity Value"),
		MFD_MODIFICATION("mfd", "mfd-mod", "MFDMod", true,
				"MFD modification factor for increasing or decreasing MFDS"),
		MFD_CONSTRAINT_RELAX("relmfd", "relax-mfd", "RelaxMFD", false,
				"Flag to reduce MFD constraint weights"),
		OFF_FUALT_ASEIS("offaseis", "off-fault-aseis", "OffAseis", true,
				"Off fault aseismicity factor");
		
		private String shortArg, argName, fileName, description;
		private boolean hasOption;
		
		private InversionOptions(String shortArg, String argName, String fileName, boolean hasOption,
				String description) {
			this.shortArg = shortArg;
			this.argName = argName;
			this.fileName = fileName;
			this.hasOption = hasOption;
			this.description = description;
		}

		public String getShortArg() {
			return shortArg;
		}

		public String getArgName() {
			return argName;
		}
		
		public String getCommandLineArgs() {
			return getCommandLineArgs(null);
		}
		
		public String getCommandLineArgs(double option) {
			return getCommandLineArgs((float)option+"");
		}
		
		public String getCommandLineArgs(String option) {
			String args = "--"+argName;
			if (hasOption) {
				Preconditions.checkArgument(option != null && !option.isEmpty());
				args += " "+option;
			}
			return args;
		}
		
		public String getFileName() {
			return getFileName(null);
		}
		
		public String getFileName(double option) {
			return getFileName((float)option+"");
		}

		public String getFileName(String option) {
			if (hasOption) {
				Preconditions.checkArgument(option != null && !option.isEmpty());
				return fileName+option;
			}
			return fileName;
		}

		public boolean hasOption() {
			return hasOption;
		}
	}
	
	protected static Options createOptions() {
		Options ops = ThreadedSimulatedAnnealing.createOptionsNoInputs();
		
		for (InversionOptions invOp : InversionOptions.values()) {
			Option op = new Option(invOp.shortArg, invOp.argName, invOp.hasOption, invOp.description);
			op.setRequired(false);
			ops.addOption(op);
		}
		
		Option rupSetOp = new Option("branch", "branch-prefix", true, "Prefix for file names." +
				"Should be able to parse logic tree branch from this");
		rupSetOp.setRequired(true);
		ops.addOption(rupSetOp);
	
		Option lightweightOp = new Option("light", "lightweight", false, "Only write out a bin file for the solution." +
				"Leave the rup set if the prefix indicates run 0");
		lightweightOp.setRequired(false);
		ops.addOption(lightweightOp);
		
		Option dirOp = new Option("dir", "directory", true, "Directory to store inputs");
		dirOp.setRequired(true);
		ops.addOption(dirOp);
		
		return ops;
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				ClassUtils.getClassNameWithoutPackage(CommandLineInversionRunner.class),
				options, true );
		System.exit(2);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = createOptions();
		
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);
			
			boolean lightweight = cmd.hasOption("lightweight");
			
			// get the directory/logic tree branch
			File dir = new File(cmd.getOptionValue("directory"));
			if (!dir.exists())
				dir.mkdir();
			String prefix = cmd.getOptionValue("branch-prefix");
			LogicTreeBranch branch = LogicTreeBranch.parseFileName(prefix);
			Preconditions.checkState(branch.isFullySpecified(),
					"Branch is not fully fleshed out! Prefix: "+prefix+", branch: "+branch);
			
			LaughTestFilter laughTest = LaughTestFilter.getDefault();
			String aseisArg = InversionOptions.DEFAULT_ASEISMICITY.argName;
			double defaultAseis = 0d;
			if (cmd.hasOption(aseisArg)) {
				String aseisVal = cmd.getOptionValue(aseisArg);
				defaultAseis = Double.parseDouble(aseisVal);
			}
			
			// first build the rupture set
			System.out.println("Building RupSet");
			FaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(
					branch.getFaultModel(), branch.getDefModel(), branch.getMagArea(),
					branch.getAveSlip(), branch.getSlipAlong(), branch.getInvModel(), laughTest, defaultAseis);
			
			// now build the inversion inputs
			
			// mfd constraint modification
			String mfdModArg = InversionOptions.MFD_MODIFICATION.argName;
			double mfdConstraintModifier = 1;
			if (cmd.hasOption(mfdModArg)) {
				String mfdMod = cmd.getOptionValue(mfdModArg);
				mfdConstraintModifier = Double.parseDouble(mfdMod);
			}
			
			// mfd relax flag
			double mfdEqualityConstraintWt = InversionConfiguration.DEFAULT_MFD_EQUALITY_WT;
			double mfdInequalityConstraintWt = InversionConfiguration.DEFAULT_MFD_INEQUALITY_WT;
			
			String relaxArg = InversionOptions.MFD_CONSTRAINT_RELAX.argName;
			if (cmd.hasOption(relaxArg)) {
				mfdEqualityConstraintWt = 1;
				mfdInequalityConstraintWt = 1;
			}
			
			// off fault aseis
			double offFaultAseisFactor = 0;
			String offFaultArg = InversionOptions.OFF_FUALT_ASEIS.argName;
			if (cmd.hasOption(offFaultArg)) {
				String offFaultMod = cmd.getOptionValue(offFaultArg);
				offFaultAseisFactor = Double.parseDouble(offFaultMod);
			}
			
			System.out.println("Building Inversion Configuration");
			InversionConfiguration config = InversionConfiguration.forModel(branch.getInvModel(),
					rupSet, offFaultAseisFactor, mfdConstraintModifier,
					mfdEqualityConstraintWt, mfdInequalityConstraintWt);
			
			ArrayList<PaleoRateConstraint> paleoRateConstraints =
				UCERF3_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
			
			PaleoProbabilityModel paleoProbabilityModel =
				PaleoProbabilityModel.loadUCERF3PaleoProbabilityModel();
			
			InversionInputGenerator gen = new InversionInputGenerator(rupSet, config,
					paleoRateConstraints, null, paleoProbabilityModel);
			
			System.out.println("Building Inversion Inputs");
			gen.generateInputs();
			
			System.out.println("Writing RupSet");
			config.updateRupSetInfoString(rupSet);
			String info = rupSet.getInfoString();
			
			File rupSetFile = new File(dir, prefix+"_rupSet.zip");
			new SimpleFaultSystemRupSet(rupSet).toZipFile(rupSetFile);
			// now clear it out of memory
			config = null;
			rupSet = null;
			gen.setRupSet(null);
			System.gc();
			
			System.out.println("Column Compressing");
			gen.columnCompress();
			
			DoubleMatrix2D A = gen.getA();
			double[] d = gen.getD();
			double[] initialState = gen.getInitial();
			DoubleMatrix2D A_ineq = gen.getA_ineq();
			double[] d_ineq = gen.getD_ineq();
			double[] minimumRuptureRates = gen.getMinimumRuptureRates();
			List<Integer> rangeEndRows = gen.getRangeEndRows();
			List<String> rangeNames = gen.getRangeNames();
			
			for (int i=0; i<rangeEndRows.size(); i++) {
				System.out.println(i+". "+rangeNames.get(i)+": "+rangeEndRows.get(i));
			}
			
			gen = null;
			System.gc();
			
			System.out.println("Creating TSA");
			ThreadedSimulatedAnnealing tsa = ThreadedSimulatedAnnealing.parseOptions(cmd, A, d,
					initialState, A_ineq, d_ineq, minimumRuptureRates, rangeEndRows, rangeNames);
			CompletionCriteria criteria = ThreadedSimulatedAnnealing.parseCompletionCriteria(cmd);
			if (!(criteria instanceof ProgressTrackingCompletionCriteria)) {
				File csvFile = new File(dir, prefix+".csv");
				criteria = new ProgressTrackingCompletionCriteria(criteria, csvFile);
			}
			System.out.println("Starting Annealing");
			tsa.iterate(criteria);
			System.out.println("Annealing DONE");
			info += "\n";
			info += "\n****** Simulated Annealing Metadata ******";
			info += "\n"+tsa.getMetadata(args, criteria);
			info += "\n******************************************";
			FileWriter fw = new FileWriter(new File(dir, prefix+"_metadata.txt"));
			fw.write(info);
			fw.close();
			
			System.out.println("Writing solution bin files");
			tsa.writeBestSolution(new File(dir, prefix+".bin"));
			
			if (!lightweight) {
				System.out.println("Loading RupSet");
				rupSet = SimpleFaultSystemRupSet.fromZipFile(rupSetFile);
				rupSet.setInfoString(info);
				double[] rupRateSolution = tsa.getBestSolution();
				rupRateSolution = InversionInputGenerator.adjustSolutionForMinimumRates(
						rupRateSolution, minimumRuptureRates);
				SimpleFaultSystemSolution sol = new SimpleFaultSystemSolution(rupSet, rupRateSolution);
				System.out.println("Writing solution");
				File solutionFile = new File(dir, prefix+"_sol.zip");
				sol.toZipFile(solutionFile);
				
				System.out.println("Writing Plots");
				tsa.writePlots(criteria, new File(dir, prefix));
				
				InversionFaultSystemSolution invSol = new InversionFaultSystemSolution(sol);
				// MFD plots
				try {
					writeMFDPlots(invSol, dir, prefix);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					writePaleoPlots(paleoRateConstraints, invSol, dir, prefix);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("Deleting RupSet (no longer needed)");
			rupSetFile.delete();
		} catch (MissingOptionException e) {
			System.err.println(e.getMessage());
			printHelp(options);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			printHelp(options);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("DONE");
		System.exit(0);
	}
	
	private static void writeMFDPlots(InversionFaultSystemSolution invSol, File dir, String prefix) throws IOException {
		UCERF2_MFD_ConstraintFetcher ucerf2Fetch = new UCERF2_MFD_ConstraintFetcher();
		
		List<MFD_InversionConstraint> origMFDConstraints = invSol.getOrigMFDConstraints();
		
		if (origMFDConstraints.size() == 2) {
			// add all cal
			MFD_InversionConstraint allConst;
			if (invSol.isUcerf3MFDs())
				allConst = UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850);
			else {
				Region reg = new CaliforniaRegions.RELM_TESTING();
				ucerf2Fetch.setRegion(reg);
				allConst = ucerf2Fetch.getTargetMFDConstraint();
			}
			origMFDConstraints.add(0, allConst);
		}
		
		int cnt = 0;
		for (MFD_InversionConstraint constraint : origMFDConstraints) {
			cnt++;
			HeadlessGraphPanel gp = invSol.getHeadlessMFDPlot(constraint, ucerf2Fetch);
			Region reg = constraint.getRegion();
			String regName = reg.getName();
			if (regName == null || regName.isEmpty())
				regName = "Uknown"+cnt;
			regName = regName.replaceAll(" ", "_");
			File file = new File(dir, prefix+"_MFD_"+regName);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");
		}
	}
	
	private static void writePaleoPlots(ArrayList<PaleoRateConstraint> paleoRateConstraints, FaultSystemSolution sol,
			File dir, String prefix)
			throws IOException {
		HeadlessGraphPanel gp = UCERF3_PaleoRateConstraintFetcher.getHeadlessSegRateComparison(
				paleoRateConstraints, Lists.newArrayList(sol));
		
		File file = new File(dir, prefix+"_paleo_fit");
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
	}

}
